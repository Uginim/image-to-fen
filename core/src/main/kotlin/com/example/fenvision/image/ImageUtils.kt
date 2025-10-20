package com.example.fenvision.image

import java.awt.Image
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.max

/**
 * 이미지 처리 유틸리티
 *
 * 저사양 기기를 위한 메모리 최적화:
 * - 이미지 리사이징으로 메모리 사용량 감소
 * - 사용 후 즉시 리소스 해제
 */
object ImageUtils {

    /**
     * 파일에서 이미지를 로드합니다.
     *
     * @param file 이미지 파일
     * @return ImageData 이미지 데이터
     * @throws IllegalArgumentException 파일이 존재하지 않거나 읽을 수 없는 경우
     */
    fun loadImage(file: File): ImageData {
        require(file.exists()) { "File does not exist: ${file.absolutePath}" }
        require(file.isFile) { "Not a file: ${file.absolutePath}" }

        val bufferedImage = ImageIO.read(file)
            ?: throw IllegalArgumentException("Cannot read image: ${file.absolutePath}")

        return bufferedImageToImageData(bufferedImage, detectFormat(file))
    }

    /**
     * ByteArray에서 이미지를 로드합니다.
     *
     * @param bytes 이미지 바이트 배열
     * @return ImageData 이미지 데이터
     */
    fun loadImage(bytes: ByteArray): ImageData {
        val inputStream = ByteArrayInputStream(bytes)
        val bufferedImage = ImageIO.read(inputStream)
            ?: throw IllegalArgumentException("Cannot read image from bytes")

        return bufferedImageToImageData(bufferedImage, ImageFormat.UNKNOWN)
    }

    /**
     * 파일에서 이미지를 로드하고 리사이징합니다.
     *
     * 메모리 최적화를 위해 큰 이미지를 축소합니다.
     * 비율을 유지하면서 최대 크기로 제한합니다.
     *
     * @param file 이미지 파일
     * @param maxSize 최대 너비/높이 (기본: 800픽셀)
     * @return ImageData 리사이징된 이미지 데이터
     */
    fun loadAndResize(file: File, maxSize: Int = 800): ImageData {
        val original = loadImage(file)

        // 이미 작으면 리사이징 안 함
        if (original.width <= maxSize && original.height <= maxSize) {
            return original
        }

        return resize(original, maxSize)
    }

    /**
     * 이미지를 리사이징합니다.
     *
     * 비율을 유지하면서 최대 크기로 제한합니다.
     *
     * @param imageData 원본 이미지
     * @param maxSize 최대 너비/높이
     * @return ImageData 리사이징된 이미지 데이터
     */
    fun resize(imageData: ImageData, maxSize: Int): ImageData {
        // 리사이징 비율 계산
        val ratio = maxSize.toFloat() / max(imageData.width, imageData.height)

        if (ratio >= 1.0f) {
            // 이미 작으면 그대로 반환
            return imageData
        }

        val newWidth = (imageData.width * ratio).toInt()
        val newHeight = (imageData.height * ratio).toInt()

        // BufferedImage로 변환
        val original = imageDataToBufferedImage(imageData)

        // 리사이징
        val scaled = original.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH)
        val resized = BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB)
        val graphics = resized.createGraphics()
        graphics.drawImage(scaled, 0, 0, null)
        graphics.dispose()

        return bufferedImageToImageData(resized, imageData.format)
    }

    /**
     * 이미지 정보를 가져옵니다 (메타데이터만, 픽셀 로드 안 함).
     *
     * @param file 이미지 파일
     * @return ImageInfo 이미지 정보
     */
    fun getImageInfo(file: File): ImageInfo {
        require(file.exists()) { "File does not exist: ${file.absolutePath}" }

        val reader = ImageIO.getImageReadersBySuffix(file.extension).next()
        val inputStream = file.inputStream()

        try {
            reader.input = ImageIO.createImageInputStream(inputStream)

            val width = reader.getWidth(0)
            val height = reader.getHeight(0)
            val format = detectFormat(file)
            val sizeInBytes = file.length()

            return ImageInfo(width, height, format, sizeInBytes)
        } finally {
            reader.dispose()
            inputStream.close()
        }
    }

    /**
     * BufferedImage를 ImageData로 변환합니다.
     */
    private fun bufferedImageToImageData(
        bufferedImage: BufferedImage,
        format: ImageFormat
    ): ImageData {
        val outputStream = ByteArrayOutputStream()
        val formatName = when (format) {
            ImageFormat.PNG -> "png"
            ImageFormat.JPEG -> "jpg"
            ImageFormat.UNKNOWN -> "png"  // 기본값
        }

        ImageIO.write(bufferedImage, formatName, outputStream)
        val bytes = outputStream.toByteArray()

        return ImageData(
            pixels = bytes,
            width = bufferedImage.width,
            height = bufferedImage.height,
            format = format
        )
    }

    /**
     * ImageData를 BufferedImage로 변환합니다.
     */
    private fun imageDataToBufferedImage(imageData: ImageData): BufferedImage {
        val inputStream = ByteArrayInputStream(imageData.pixels)
        return ImageIO.read(inputStream)
    }

    /**
     * 파일 확장자로 이미지 포맷을 감지합니다.
     */
    private fun detectFormat(file: File): ImageFormat {
        return when (file.extension.lowercase()) {
            "png" -> ImageFormat.PNG
            "jpg", "jpeg" -> ImageFormat.JPEG
            else -> ImageFormat.UNKNOWN
        }
    }

    /**
     * 이미지를 파일로 저장합니다 (디버깅용).
     *
     * @param imageData 이미지 데이터
     * @param file 저장할 파일
     */
    fun saveImage(imageData: ImageData, file: File) {
        file.writeBytes(imageData.pixels)
    }
}
