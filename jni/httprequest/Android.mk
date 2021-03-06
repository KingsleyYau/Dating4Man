# Copyright (C) 2014 The QpidNetwork Project
# HttpRequest Module Makefile
#
# Created on: 2014/10/27
# Author: Max.Chiu
# Email: Kingsleyyau@gmail.com
#

SELF_PATH := $(call my-dir)
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := httprequest

LOCAL_MODULE_FILENAME := libhttprequest

LOCAL_C_INCLUDES := $(LIBRARY_PATH)
LOCAL_C_INCLUDES += $(LIBRARY_THIRDY_PARTY_PATH)
LOCAL_C_INCLUDES += $(LIBRARY_THIRDY_PARTY_PATH)/curl/include
#LOCAL_C_INCLUDES += $(LIBRARY_PATH)/manrequesthandler
#LOCAL_C_INCLUDES += $(LIBRARY_PATH)/manrequesthandler/item
#LOCAL_C_INCLUDES += $(LIBRARY_PATH)/httpclient

LOCAL_CFLAGS = -fpermissive -Wno-write-strings

LOCAL_LDLIBS += -llog

LOCAL_LDFLAGS += -L$(LIBRARY_THIRDY_PARTY_PATH)/openssl/lib/$(TARGET_ARCH)
LOCAL_LDLIBS += -lssl -lcrypto #-lz 

#LOCAL_STATIC_LIBRARIES += z
LOCAL_STATIC_LIBRARIES += manrequesthandler
#LOCAL_STATIC_LIBRARIES += livechat
LOCAL_STATIC_LIBRARIES += androidcommon
#LOCAL_STATIC_LIBRARIES += common
#LOCAL_STATIC_LIBRARIES += json
#LOCAL_STATIC_LIBRARIES += xml
#LOCAL_STATIC_LIBRARIES += amf
#LOCAL_STATIC_LIBRARIES += httpclient
#LOCAL_STATIC_LIBRARIES += simulatorchecker
#LOCAL_SHARED_LIBRARIES += crashhandler

LOCAL_CPPFLAGS  := -std=c++11
LOCAL_CPPFLAGS	+= -fpermissive

REAL_PATH := $(realpath $(LOCAL_PATH))
LOCAL_SRC_FILES := $(call all-cpp-files-under, $(REAL_PATH))

include $(BUILD_SHARED_LIBRARY)