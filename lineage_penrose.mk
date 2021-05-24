#
# Copyright (C) 2018-2021 ArrowOS
#
# SPDX-License-Identifier: Apache-2.0
#

# Inherit common products
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/full_base_telephony.mk)

# Inherit device configurations
$(call inherit-product, device/blackshark/penrose/device.mk)

# Inherit common LineageOS configurations
$(call inherit-product, vendor/lineage/config/common_full_phone.mk)

PRODUCT_CHARACTERISTICS := nosdcard

PRODUCT_BRAND := blackshark
PRODUCT_DEVICE := penrose
PRODUCT_MANUFACTURER := blackshark
PRODUCT_MODEL := SHARK PRS-A0
PRODUCT_NAME := lineage_penrose

PRODUCT_GMS_CLIENTID_BASE := android-blackshark
