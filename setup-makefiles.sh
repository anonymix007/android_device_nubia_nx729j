#!/bin/bash
#
# SPDX-FileCopyrightText: 2016 The CyanogenMod Project
# SPDX-FileCopyrightText: 2017-2024 The LineageOS Project
# SPDX-License-Identifier: Apache-2.0
#

set -e

DEVICE=nx729j
VENDOR=nubia

# Load extract_utils and do some sanity checks
MY_DIR="${BASH_SOURCE%/*}"
if [[ ! -d "${MY_DIR}" ]]; then MY_DIR="${PWD}"; fi

ANDROID_ROOT="${MY_DIR}/../../.."

HELPER="${ANDROID_ROOT}/tools/extract-utils/extract_utils.sh"
if [ ! -f "${HELPER}" ]; then
    echo "Unable to find helper script at ${HELPER}"
    exit 1
fi
source "${HELPER}"

function vendor_imports() {
    cat <<EOF >>"$1"
		"vendor/qcom/common/system/telephony",
		"vendor/qcom/common/vendor/adreno-t",
		"vendor/qcom/common/vendor/display/5.15",
EOF
}

function lib_to_package_fixup_vendor_variants() {
    if [ "$2" != "vendor" ]; then
        return 1
    fi

    case "$1" in
        com.qualcomm.qti.dpm.api@1.0 | \
            vendor.qti.imsrtpservice@3.0)
            echo "$1-vendor"
            ;;
        libwpa_client) ;;
        *)
            return 1
            ;;
    esac
}

function lib_to_package_fixup() {
    lib_to_package_fixup_clang_rt_ubsan_standalone "$1" ||
        lib_to_package_fixup_proto_3_9_1 "$1" ||
        lib_to_package_fixup_vendor_variants "$@"
}

# Initialize the helper
setup_vendor "${DEVICE}" "${VENDOR}" "${ANDROID_ROOT}"

# Warning headers and guards
write_headers

write_makefiles "${MY_DIR}/proprietary-files.txt"

if [ -f "${MY_DIR}/proprietary-files-carriersettings.txt" ]; then
    write_makefiles "${MY_DIR}/proprietary-files-carriersettings.txt"

    write_rro_package "CarrierConfigOverlay" "com.android.carrierconfig" product
    write_single_product_packages "CarrierConfigOverlay"
fi

if [ -f "${MY_DIR}/proprietary-firmware.txt" ]; then
    append_firmware_calls_to_makefiles "${MY_DIR}/proprietary-firmware.txt"
fi

# Finish
write_footers
