cmake_minimum_required(VERSION 3.22.1)

project("llama-jni")

# Limit concurrent compilation to 2 jobs to prevent memory exhaustion on Windows
set_property(GLOBAL PROPERTY JOB_POOLS compile_job_pool=2)
set(CMAKE_JOB_POOL_COMPILE compile_job_pool)

# ---- llama.cpp repo structure ----
set(LLAMACPP_DIR "${CMAKE_CURRENT_SOURCE_DIR}/llama")

# Detect Architecture for GGML CPU backend
if (ANDROID_ABI STREQUAL "arm64-v8a" OR ANDROID_ABI STREQUAL "armeabi-v7a")
    set(ARCH_DIR "arm")
elseif (ANDROID_ABI STREQUAL "x86_64" OR ANDROID_ABI STREQUAL "x86")
    set(ARCH_DIR "x86")
else()
    message(WARNING "Unknown ABI: ${ANDROID_ABI}, using generic implementation")
endif()

# ---- GGML backend source files ----
set(GGML_SRC
    ${LLAMACPP_DIR}/ggml/src/ggml.c
    ${LLAMACPP_DIR}/ggml/src/ggml.cpp
    ${LLAMACPP_DIR}/ggml/src/ggml-alloc.c
    ${LLAMACPP_DIR}/ggml/src/ggml-backend.cpp
    ${LLAMACPP_DIR}/ggml/src/ggml-backend-dl.cpp
    ${LLAMACPP_DIR}/ggml/src/ggml-backend-reg.cpp
    ${LLAMACPP_DIR}/ggml/src/ggml-backend-meta.cpp
    ${LLAMACPP_DIR}/ggml/src/ggml-quants.c
    ${LLAMACPP_DIR}/ggml/src/ggml-threading.cpp
    ${LLAMACPP_DIR}/ggml/src/ggml-opt.cpp
    ${LLAMACPP_DIR}/ggml/src/gguf.cpp
    # CPU backend — core dispatcher
    ${LLAMACPP_DIR}/ggml/src/ggml-cpu/ggml-cpu.cpp
    ${LLAMACPP_DIR}/ggml/src/ggml-cpu/ggml-cpu.c
    ${LLAMACPP_DIR}/ggml/src/ggml-cpu/traits.cpp
    ${LLAMACPP_DIR}/ggml/src/ggml-cpu/binary-ops.cpp
    ${LLAMACPP_DIR}/ggml/src/ggml-cpu/unary-ops.cpp
    ${LLAMACPP_DIR}/ggml/src/ggml-cpu/ops.cpp
    ${LLAMACPP_DIR}/ggml/src/ggml-cpu/vec.cpp
    ${LLAMACPP_DIR}/ggml/src/ggml-cpu/repack.cpp
    ${LLAMACPP_DIR}/ggml/src/ggml-cpu/hbm.cpp
    # CPU backend — quantization dispatcher
    ${LLAMACPP_DIR}/ggml/src/ggml-cpu/quants.c
)

# Add architecture-specific implementations if detected
if (ARCH_DIR)
    list(APPEND GGML_SRC
        ${LLAMACPP_DIR}/ggml/src/ggml-cpu/arch/${ARCH_DIR}/quants.c
        ${LLAMACPP_DIR}/ggml/src/ggml-cpu/arch/${ARCH_DIR}/repack.cpp
        ${LLAMACPP_DIR}/ggml/src/ggml-cpu/arch/${ARCH_DIR}/cpu-feats.cpp
    )
endif()

# ---- Core llama library source files ----
set(LLAMA_SRC
    ${LLAMACPP_DIR}/src/llama.cpp
    ${LLAMACPP_DIR}/src/llama-adapter.cpp
    ${LLAMACPP_DIR}/src/llama-arch.cpp
    ${LLAMACPP_DIR}/src/llama-batch.cpp
    ${LLAMACPP_DIR}/src/llama-chat.cpp
    ${LLAMACPP_DIR}/src/llama-context.cpp
    ${LLAMACPP_DIR}/src/llama-cparams.cpp
    ${LLAMACPP_DIR}/src/llama-grammar.cpp
    ${LLAMACPP_DIR}/src/llama-graph.cpp
    ${LLAMACPP_DIR}/src/llama-hparams.cpp
    ${LLAMACPP_DIR}/src/llama-impl.cpp
    ${LLAMACPP_DIR}/src/llama-io.cpp
    ${LLAMACPP_DIR}/src/llama-kv-cache.cpp
    ${LLAMACPP_DIR}/src/llama-kv-cache-iswa.cpp
    ${LLAMACPP_DIR}/src/llama-memory.cpp
    ${LLAMACPP_DIR}/src/llama-memory-hybrid.cpp
    ${LLAMACPP_DIR}/src/llama-memory-hybrid-iswa.cpp
    ${LLAMACPP_DIR}/src/llama-memory-recurrent.cpp
    ${LLAMACPP_DIR}/src/llama-mmap.cpp
    ${LLAMACPP_DIR}/src/llama-model.cpp
    ${LLAMACPP_DIR}/src/llama-model-loader.cpp
    ${LLAMACPP_DIR}/src/llama-model-saver.cpp
    ${LLAMACPP_DIR}/src/llama-quant.cpp
    ${LLAMACPP_DIR}/src/llama-sampler.cpp
    ${LLAMACPP_DIR}/src/llama-vocab.cpp
    ${LLAMACPP_DIR}/src/unicode.cpp
    ${LLAMACPP_DIR}/src/unicode-data.cpp
)

# ---- Collect all per-model source files ----
file(GLOB LLAMA_MODELS_SRC "${LLAMACPP_DIR}/src/models/*.cpp")

# ---- Build ggml as a static library ----
add_library(ggml STATIC ${GGML_SRC})
target_include_directories(ggml PUBLIC
    ${LLAMACPP_DIR}/ggml/include
    ${LLAMACPP_DIR}/ggml/src
    ${LLAMACPP_DIR}/ggml/src/ggml-cpu
)
target_compile_definitions(ggml PRIVATE
    NDEBUG
    GGML_USE_CPU
    GGML_VERSION="0.10.0"
    GGML_COMMIT="unknown"
    _GNU_SOURCE
)
# -fno-finite-math-only required by vec.h
target_compile_options(ggml PRIVATE -O2 -ffast-math -fno-finite-math-only)

# ---- Build llama as a static library (core + all models) ----
add_library(llama STATIC ${LLAMA_SRC} ${LLAMA_MODELS_SRC})
target_include_directories(llama PUBLIC
    ${LLAMACPP_DIR}/include
    ${LLAMACPP_DIR}/src
    ${LLAMACPP_DIR}/src/models
    ${LLAMACPP_DIR}/ggml/include
    ${LLAMACPP_DIR}/ggml/src
)
target_compile_options(llama PRIVATE -O2 -std=c++17)
target_link_libraries(llama ggml)

# ---- JNI bridge shared library ----
add_library(llama-jni SHARED
    llama-jni.cpp
)
target_include_directories(llama-jni PRIVATE
    ${LLAMACPP_DIR}/include
    ${LLAMACPP_DIR}/ggml/include
)
target_link_libraries(llama-jni
    llama
    ggml
    android
    log
)

# Support 16 KB memory page sizes (Android 15 requirement)
target_link_options(llama-jni PRIVATE "-Wl,-z,max-page-size=16384")
