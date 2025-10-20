package com.example.fenvision.image

/**
 * 이미지 데이터를 담는 클래스
 */
data class ImageData(
    val pixels: ByteArray,  // 이미지 픽셀 데이터
    val width: Int,         // 너비
    val height: Int,        // 높이
    val format: ImageFormat // 포맷
) {
    // Note: data class는 ByteArray를 참조로만 비교하므로
    // 내용 기반 비교를 위해 equals/hashCode를 수동으로 오버라이드합니다
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ImageData

        if (!pixels.contentEquals(other.pixels)) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (format != other.format) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pixels.contentHashCode()
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + format.hashCode()
        return result
    }
}

/**
 * 이미지 포맷
 */
enum class ImageFormat {
    PNG,
    JPEG,
    UNKNOWN
}

/**
 * 이미지 정보 (메타데이터)
 */
data class ImageInfo(
    val width: Int,
    val height: Int,
    val format: ImageFormat,
    val sizeInBytes: Long
)
