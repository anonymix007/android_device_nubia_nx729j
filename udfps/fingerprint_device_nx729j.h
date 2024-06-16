/*
 * Copyright (C) 2024 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

#pragma once

#include <cstring>

#include <hardware/fingerprint.h>
#include <hardware/hardware.h>

#define CUSTOMIZED_COMMAND "FingerService"
#define CUSTOMIZED_COMMAND_LEN strlen("FingerService")

struct fingerprint_device_gf95xx {
    hw_device_t common;
    fingerprint_notify_t notify;
    int (*set_notify)(fingerprint_device_gf95xx *dev, fingerprint_notify_t notify);
    uint64_t (*pre_enroll)(fingerprint_device_gf95xx *dev);
    int (*enroll)(fingerprint_device_gf95xx *dev, const hw_auth_token_t *hat, uint32_t gid, uint32_t timeout_sec);
    int (*post_enroll)(fingerprint_device_gf95xx *dev);
    uint64_t (*get_authenticator_id)(fingerprint_device_gf95xx *dev);
    int (*cancel)(fingerprint_device_gf95xx *dev);
    int (*enumerate)(fingerprint_device_gf95xx *dev);
    int (*remove)(fingerprint_device_gf95xx *dev, uint32_t gid, uint32_t fid);
    int (*set_active_group)(fingerprint_device_gf95xx *dev, uint32_t gid, const char *store_path);
    int (*authenticate)(fingerprint_device_gf95xx *dev, uint64_t operation_id, uint32_t gid);
    int (*sendCustomizedCommand)(fingerprint_device_gf95xx *device, uint32_t cmd, uint32_t extras, const char *msg, ssize_t len);
    void *reserved[4];
};
