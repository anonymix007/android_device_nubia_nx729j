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
		"device/nubia/nx729j",
		"hardware/nubia",
		"vendor/qcom/common/system/telephony",
		"vendor/qcom/common/vendor/adreno-t",
		"vendor/qcom/common/vendor/bt/hci",
		"vendor/qcom/common/vendor/display",
		"vendor/qcom/common/vendor/display/5.15",
		"vendor/qcom/common/vendor/media-5.15",
		"vendor/qcom/common/vendor/perf",
EOF
}

function lib_to_package_fixup_vendor_variants() {
    if [ "$2" != "vendor" ]; then
        return 1
    fi

    case "$1" in
        com.qualcomm.qti.dpm.api@1.0 | \
        com.qualcomm.qti.imscmservice@1.0 | \
        com.qualcomm.qti.imscmservice@2.0 | \
        com.qualcomm.qti.imscmservice@2.1 | \
        com.qualcomm.qti.imscmservice@2.2 | \
        com.qualcomm.qti.uceservice@2.0 | \
        com.qualcomm.qti.uceservice@2.1 | \
        com.qualcomm.qti.uceservice@2.2 | \
        com.qualcomm.qti.uceservice@2.3 | \
        libmpbase | \
        libskia | \
        libtensorflowlite_jni | \
        vendor.qti.data.factory@2.0 | \
        vendor.qti.data.factory@2.1 | \
        vendor.qti.data.factory@2.2 | \
        vendor.qti.data.factory@2.3 | \
        vendor.qti.data.factory@2.4 | \
        vendor.qti.data.factory@2.5 | \
        vendor.qti.data.factory@2.6 | \
        vendor.qti.data.factory@2.7 | \
        vendor.qti.data.mwqem@1.0 | \
        vendor.qti.data.slm@1.0 | \
        vendor.qti.diaghal@1.0 | \
        vendor.qti.hardware.data.cne.internal.api@1.0 | \
        vendor.qti.hardware.data.cne.internal.constants@1.0 | \
        vendor.qti.hardware.data.cne.internal.server@1.0 | \
        vendor.qti.hardware.data.cne.internal.server@1.1 | \
        vendor.qti.hardware.data.cne.internal.server@1.2 | \
        vendor.qti.hardware.data.cne.internal.server@1.3 | \
        vendor.qti.hardware.data.connection@1.0 | \
        vendor.qti.hardware.data.connection@1.1 | \
        vendor.qti.hardware.data.connectionfactory-V1-ndk | \
        vendor.qti.hardware.data.dataactivity-V1-ndk | \
        vendor.qti.hardware.data.dynamicdds@1.0 | \
        vendor.qti.hardware.data.dynamicdds@1.1 | \
        vendor.qti.hardware.data.flow@1.0 | \
        vendor.qti.hardware.data.flow@1.1 | \
        vendor.qti.hardware.data.iwlan@1.0 | \
        vendor.qti.hardware.data.iwlan@1.1 | \
        vendor.qti.hardware.data.ka-V1-ndk | \
        vendor.qti.hardware.data.latency@1.0 | \
        vendor.qti.hardware.data.lce@1.0 | \
        vendor.qti.hardware.data.qmi@1.0 | \
        vendor.qti.hardware.dpmservice@1.0 | \
        vendor.qti.hardware.dpmservice@1.1 | \
        vendor.qti.hardware.embmssl@1.0 | \
        vendor.qti.hardware.embmssl@1.1 | \
        vendor.qti.hardware.factory@1.0 | \
        vendor.qti.hardware.factory@1.1 | \
        vendor.qti.hardware.limits@1.0 | \
        vendor.qti.hardware.limits@1.1 | \
        vendor.qti.hardware.limits@1.2 | \
        vendor.qti.hardware.mwqemadapter@1.0 | \
        vendor.qti.hardware.radio.am@1.0 | \
        vendor.qti.hardware.radio.am-V1-ndk | \
        vendor.qti.hardware.radio.atcmdfwd@1.0 | \
        vendor.qti.hardware.radio.atcmdfwd-V1-ndk | \
        vendor.qti.hardware.radio.ims@1.0 | \
        vendor.qti.hardware.radio.ims@1.1 | \
        vendor.qti.hardware.radio.ims@1.2 | \
        vendor.qti.hardware.radio.ims@1.3 | \
        vendor.qti.hardware.radio.ims@1.4 | \
        vendor.qti.hardware.radio.ims@1.5 | \
        vendor.qti.hardware.radio.ims@1.6 | \
        vendor.qti.hardware.radio.ims@1.7 | \
        vendor.qti.hardware.radio.ims@1.8 | \
        vendor.qti.hardware.radio.ims-V9-ndk | \
        vendor.qti.hardware.radio.internal.deviceinfo@1.0 | \
        vendor.qti.hardware.radio.internal.deviceinfo-V1-ndk | \
        vendor.qti.hardware.radio.lpa@1.0 | \
        vendor.qti.hardware.radio.lpa@1.1 | \
        vendor.qti.hardware.radio.lpa@1.2 | \
        vendor.qti.hardware.radio.lpa@1.3 | \
        vendor.qti.hardware.radio.qcrilhook@1.0 | \
        vendor.qti.hardware.radio.qcrilhook-V1-ndk | \
        vendor.qti.hardware.radio.qtiradio@1.0 | \
        vendor.qti.hardware.radio.qtiradio@2.0 | \
        vendor.qti.hardware.radio.qtiradio@2.1 | \
        vendor.qti.hardware.radio.qtiradio@2.2 | \
        vendor.qti.hardware.radio.qtiradio@2.3 | \
        vendor.qti.hardware.radio.qtiradio@2.4 | \
        vendor.qti.hardware.radio.qtiradio@2.5 | \
        vendor.qti.hardware.radio.qtiradio@2.6 | \
        vendor.qti.hardware.radio.qtiradioconfig-V3-ndk | \
        vendor.qti.hardware.radio.qtiradio-V9-ndk | \
        vendor.qti.hardware.radio.uim@1.0 | \
        vendor.qti.hardware.radio.uim@1.1 | \
        vendor.qti.hardware.radio.uim@1.2 | \
        vendor.qti.hardware.radio.uim_remote_client@1.0 | \
        vendor.qti.hardware.radio.uim_remote_client@1.1 | \
        vendor.qti.hardware.radio.uim_remote_client@1.2 | \
        vendor.qti.hardware.radio.uim_remote_server@1.0 | \
        vendor.qti.hardware.sigma_miracast@1.0 | \
        vendor.qti.hardware.slmadapter@1.0 | \
        vendor.qti.hardware.wifidisplaysession@1.0 | \
        vendor.qti.ims.callcapability@1.0 | \
        vendor.qti.ims.callinfo@1.0 | \
        vendor.qti.ims.configservice@1.0 | \
        vendor.qti.ims.configservice@1.1 | \
        vendor.qti.ims.connection@1.0 | \
        vendor.qti.ims.factory@1.0 | \
        vendor.qti.ims.factory@1.1 | \
        vendor.qti.ims.factory@2.0 | \
        vendor.qti.ims.factory@2.1 | \
        vendor.qti.ims.factory@2.2 | \
        vendor.qti.ims.rcsconfig@1.0 | \
        vendor.qti.ims.rcsconfig@1.1 | \
        vendor.qti.ims.rcsconfig@2.0 | \
        vendor.qti.ims.rcsconfig@2.1 | \
        vendor.qti.ims.rcssip@1.0 | \
        vendor.qti.ims.rcssip@1.1 | \
        vendor.qti.ims.rcssip@1.2 | \
        vendor.qti.ims.rcsuce@1.0 | \
        vendor.qti.ims.rcsuce@1.1 | \
        vendor.qti.ims.rcsuce@1.2 | \
        vendor.qti.imsrtpservice@3.0 | \
        vendor.qti.imsrtpservice@3.1 | \
        vendor.qti.latency@2.0 | \
        vendor.qti.latency@2.1 | \
        vendor.qti.latency@2.2 | \
        libvmmem)
            echo "$1_vendor"
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
