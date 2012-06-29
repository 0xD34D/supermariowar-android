LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := SDL_mixer

LOCAL_C_INCLUDES := \
	$(LOCAL_PATH) \
 	$(LOCAL_PATH)/../SDL/include \
	$(LOCAL_PATH)/../tremor/include \
	$(LOCAL_PATH)/../ogg/include

LOCAL_CFLAGS := -O3 -ffast-math

LOCAL_CPP_EXTENSION := .cpp

LOCAL_SRC_FILES := $(notdir $(wildcard $(LOCAL_PATH)/*.c))

LOCAL_SHARED_LIBRARIES := 
LOCAL_STATIC_LIBRARIES := SDL
LOCAL_LDLIBS :=

include $(BUILD_STATIC_LIBRARY)

