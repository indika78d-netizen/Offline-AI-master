#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include "llama.h"

#define TAG "LLAMA_JNI"

static struct llama_model *model = nullptr;
static struct llama_context *ctx = nullptr;

std::string jstring2str(JNIEnv* env, jstring jstr) {
    if (!jstr) return "";
    const char* chars = env->GetStringUTFChars(jstr, nullptr);
    if (!chars) return "";
    std::string ret(chars);
    env->ReleaseStringUTFChars(jstr, chars);
    return ret;
}

// Helper to add a token to a llama_batch
static void batch_add(struct llama_batch & batch, llama_token id, llama_pos pos, const std::vector<llama_seq_id> & seq_ids, bool logits) {
    batch.token   [batch.n_tokens] = id;
    batch.pos     [batch.n_tokens] = pos;
    batch.n_seq_id[batch.n_tokens] = seq_ids.size();
    for (size_t i = 0; i < seq_ids.size(); ++i) {
        batch.seq_id[batch.n_tokens][i] = seq_ids[i];
    }
    batch.logits  [batch.n_tokens] = logits;
    batch.n_tokens++;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_esh_offlineai_AIEngine_initModelNative(JNIEnv *env, jobject thiz, jstring model_path) {
    std::string path = jstring2str(env, model_path);
    
    llama_backend_init();

    auto mparams = llama_model_default_params();
    mparams.n_gpu_layers = 0; 

    model = llama_model_load_from_file(path.c_str(), mparams);
    if (!model) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Failed to load model: %s", path.c_str());
        return JNI_FALSE;
    }

    auto cparams = llama_context_default_params();
    // PERFORMANCE: Reduced context size for faster processing on mobile
    cparams.n_ctx = 512;
    cparams.n_batch = 512;
    cparams.n_threads = 4;
    cparams.n_threads_batch = 4;

    ctx = llama_init_from_model(model, cparams);
    if (!ctx) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Failed to create context");
        return JNI_FALSE;
    }

    __android_log_print(ANDROID_LOG_INFO, TAG, "AI Engine initialized successfully");
    return JNI_TRUE;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_esh_offlineai_AIEngine_generateTextNative(JNIEnv *env, jobject thiz, jstring prompt, jint max_tokens) {
    if (!ctx || !model) return env->NewStringUTF("Error: Engine not initialized");

    // Clear KV cache for a fresh generation
    llama_memory_clear(llama_get_memory(ctx), true);

    std::string input_text = jstring2str(env, prompt);
    const struct llama_vocab * vocab = llama_model_get_vocab(model);
    
    // 1. Tokenize
    std::vector<llama_token> tokens(input_text.size() + 32);
    int n_tokens = llama_tokenize(vocab, input_text.c_str(), input_text.size(), tokens.data(), tokens.size(), true, true);
    if (n_tokens < 0) {
        tokens.resize(-n_tokens);
        n_tokens = llama_tokenize(vocab, input_text.c_str(), input_text.size(), tokens.data(), tokens.size(), true, true);
    }
    if (n_tokens < 0) return env->NewStringUTF("Error: Tokenization failed");
    
    // Safety check for context length
    int max_prompt_tokens = llama_n_ctx(ctx) - max_tokens;
    if (n_tokens > max_prompt_tokens) {
        n_tokens = max_prompt_tokens;
        if (n_tokens < 1) return env->NewStringUTF("Error: Prompt too long");
    }
    tokens.resize(n_tokens);

    // 2. Setup Batch
    struct llama_batch batch = llama_batch_init(std::max(n_tokens, (int)max_tokens), 0, 1);
    batch.n_tokens = 0; // Initialize counter
    for (int i = 0; i < n_tokens; i++) {
        // Request logits only for the last token of the prompt
        batch_add(batch, tokens[i], i, {0}, i == n_tokens - 1);
    }

    // 3. Initial Decode
    int decode_res = llama_decode(ctx, batch);
    if (decode_res != 0) {
        char err[64];
        sprintf(err, "Error: Initial decode failed (%d)", decode_res);
        llama_batch_free(batch);
        return env->NewStringUTF(err);
    }

    // 4. Generation Loop
    std::string result = "";
    int n_cur = n_tokens;
    
    auto sparams = llama_sampler_chain_default_params();
    struct llama_sampler * smpl = llama_sampler_chain_init(sparams);
    llama_sampler_chain_add(smpl, llama_sampler_init_top_k(40));
    llama_sampler_chain_add(smpl, llama_sampler_init_top_p(0.95f, 1));
    llama_sampler_chain_add(smpl, llama_sampler_init_temp(0.8f));
    llama_sampler_chain_add(smpl, llama_sampler_init_dist(LLAMA_DEFAULT_SEED));

    while (n_cur < n_tokens + max_tokens) {
        llama_token new_token_id = llama_sampler_sample(smpl, ctx, -1);
        
        if (llama_vocab_is_eog(vocab, new_token_id)) break;

        char buf[128];
        int n = llama_token_to_piece(vocab, new_token_id, buf, sizeof(buf), 0, true);
        if (n > 0) result.append(buf, n);

        batch.n_tokens = 0;
        batch_add(batch, new_token_id, n_cur, {0}, true);
        
        if (llama_decode(ctx, batch) != 0) break;
        n_cur++;
    }

    llama_sampler_free(smpl);
    llama_batch_free(batch);
    
    return env->NewStringUTF(result.c_str());
}

extern "C"
JNIEXPORT void JNICALL
Java_com_esh_offlineai_AIEngine_freeModelNative(JNIEnv *env, jobject thiz) {
    if (ctx) {
        llama_free(ctx);
        ctx = nullptr;
    }
    if (model) {
        llama_model_free(model);
        model = nullptr;
    }
    llama_backend_free();
    __android_log_print(ANDROID_LOG_INFO, TAG, "AI Engine resources freed");
}
