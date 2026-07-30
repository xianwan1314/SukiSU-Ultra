package com.sukisu.ultra.ui.util

import java.util.Locale

internal const val BOOT_IMAGE_KIND_BOOT = "boot"
internal const val BOOT_IMAGE_KIND_INIT_BOOT = "init_boot"
internal const val BOOT_IMAGE_KIND_VENDOR_BOOT = "vendor_boot"
internal const val BOOT_IMAGE_KIND_UNKNOWN = "unknown"

internal const val VIVO_BOOT_PATCH_COMMAND = "boot-patch-vivo"
private const val VIVO_KMI_SUFFIX = "_vivo"

internal fun isSupportedBootImageKind(kind: String?): Boolean {
    return when (kind) {
        BOOT_IMAGE_KIND_BOOT,
        BOOT_IMAGE_KIND_INIT_BOOT,
        BOOT_IMAGE_KIND_VENDOR_BOOT,
        -> true

        else -> false
    }
}

internal fun isVendorBootTarget(bootImageKind: String?, partition: String?): Boolean {
    return bootImageKind == BOOT_IMAGE_KIND_VENDOR_BOOT || partition == BOOT_IMAGE_KIND_VENDOR_BOOT
}

internal fun orderSupportedKmis(kmis: List<String>): List<String> {
    return kmis
}

private fun preferVivoVariantByCurrentKmi(
    currentKmi: String,
    supportedKmis: List<String>
): String? {
    val match = Regex("""android\d+-(\d+)\.(\d+)""").find(currentKmi) ?: return null
    val major = match.groupValues[1].toIntOrNull() ?: return null
    val minor = match.groupValues[2].toIntOrNull() ?: return null
    if (major > 6 || major == 6 && minor >= 6) return null

    val vivoKmi = "$currentKmi$VIVO_KMI_SUFFIX"
    return supportedKmis.firstOrNull { it == vivoKmi }
}

internal fun resolvePreferredKmi(
    preferredKmi: String?,
    currentKmi: String,
    supportedKmis: List<String>
): String? {
    return when {
        !preferredKmi.isNullOrBlank() && preferredKmi != currentKmi -> preferredKmi
        currentKmi.isBlank() -> null
        else -> preferVivoVariantByCurrentKmi(currentKmi, supportedKmis) ?: currentKmi
    }
}

internal fun describeBootTarget(bootImageKind: String?, partition: String?): String {
    return when {
        isVendorBootTarget(bootImageKind, partition) -> "$BOOT_IMAGE_KIND_VENDOR_BOOT.img"
        bootImageKind == BOOT_IMAGE_KIND_INIT_BOOT || partition == BOOT_IMAGE_KIND_INIT_BOOT -> "$BOOT_IMAGE_KIND_INIT_BOOT.img"
        bootImageKind == BOOT_IMAGE_KIND_BOOT || partition == BOOT_IMAGE_KIND_BOOT -> "$BOOT_IMAGE_KIND_BOOT.img"
        else -> "boot image"
    }
}

internal fun resolveBootImageKindForOutput(bootImageKind: String?, partition: String?): String? {
    return when {
        isVendorBootTarget(bootImageKind, partition) -> BOOT_IMAGE_KIND_VENDOR_BOOT
        bootImageKind == BOOT_IMAGE_KIND_INIT_BOOT || partition == BOOT_IMAGE_KIND_INIT_BOOT -> BOOT_IMAGE_KIND_INIT_BOOT
        bootImageKind == BOOT_IMAGE_KIND_BOOT || partition == BOOT_IMAGE_KIND_BOOT -> BOOT_IMAGE_KIND_BOOT
        else -> null
    }
}

internal fun detectBootImageKindByName(fileName: String?): String? {
    val normalized = fileName
        ?.trim()
        ?.substringAfterLast('/')
        ?.substringAfterLast('\\')
        ?.lowercase(Locale.ROOT)
        ?: return null

    return when {
        normalized.endsWith("$BOOT_IMAGE_KIND_VENDOR_BOOT.img") -> BOOT_IMAGE_KIND_VENDOR_BOOT
        normalized.endsWith("$BOOT_IMAGE_KIND_INIT_BOOT.img") -> BOOT_IMAGE_KIND_INIT_BOOT
        normalized.endsWith("$BOOT_IMAGE_KIND_BOOT.img") -> BOOT_IMAGE_KIND_BOOT
        else -> null
    }
}
