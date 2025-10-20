package com.example.fenvision.image

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ImageUtilsTest {

    @Test
    fun `load image from file`() {
        // 테스트 이미지 경로 (프로젝트 루트 기준)
        val testImagePath = findTestImage()

        val imageData = ImageUtils.loadImage(testImagePath)

        // 이미지가 로드되었는지 확인
        assertTrue(imageData.pixels.isNotEmpty(), "Image pixels should not be empty")
        assertTrue(imageData.width > 0, "Image width should be positive")
        assertTrue(imageData.height > 0, "Image height should be positive")

        println("✅ Loaded image: ${imageData.width}x${imageData.height}, ${imageData.pixels.size} bytes")
    }

    @Test
    fun `get image info without loading pixels`() {
        val testImagePath = findTestImage()

        val info = ImageUtils.getImageInfo(testImagePath)

        assertTrue(info.width > 0, "Width should be positive")
        assertTrue(info.height > 0, "Height should be positive")
        assertTrue(info.sizeInBytes > 0, "File size should be positive")

        println("✅ Image info: ${info.width}x${info.height}, ${info.sizeInBytes} bytes, format: ${info.format}")
    }

    @Test
    fun `resize large image to 800px`() {
        val testImagePath = findTestImage()

        val resized = ImageUtils.loadAndResize(testImagePath, maxSize = 800)

        // 리사이징 확인
        assertTrue(resized.width <= 800, "Width should be <= 800")
        assertTrue(resized.height <= 800, "Height should be <= 800")

        println("✅ Resized to: ${resized.width}x${resized.height}")
    }

    @Test
    fun `resize keeps aspect ratio`() {
        val testImagePath = findTestImage()

        val original = ImageUtils.loadImage(testImagePath)
        val resized = ImageUtils.resize(original, maxSize = 400)

        // 비율 확인
        val originalRatio = original.width.toFloat() / original.height
        val resizedRatio = resized.width.toFloat() / resized.height

        val ratioDifference = kotlin.math.abs(originalRatio - resizedRatio)
        assertTrue(ratioDifference < 0.01f, "Aspect ratio should be preserved")

        println("✅ Aspect ratio preserved: $originalRatio vs $resizedRatio")
    }

    @Test
    fun `load and resize multiple times for memory test`() {
        val testImagePath = findTestImage()

        // 여러 번 로드/리사이징 (메모리 누수 확인)
        repeat(5) { i ->
            val resized = ImageUtils.loadAndResize(testImagePath, maxSize = 800)
            println("  Iteration ${i + 1}: ${resized.width}x${resized.height}")
        }

        println("✅ Multiple load/resize completed without crash")
    }

    @Test
    fun `throw exception for non-existent file`() {
        val nonExistentFile = File("non_existent_image.png")

        assertThrows<IllegalArgumentException> {
            ImageUtils.loadImage(nonExistentFile)
        }

        println("✅ Exception thrown for non-existent file")
    }

    @Test
    fun `save and reload image`() {
        val testImagePath = findTestImage()
        val tempFile = File.createTempFile("test_image", ".png")
        tempFile.deleteOnExit()

        try {
            // 로드
            val original = ImageUtils.loadImage(testImagePath)

            // 저장
            ImageUtils.saveImage(original, tempFile)

            // 다시 로드
            val reloaded = ImageUtils.loadImage(tempFile)

            // 크기 확인
            assertEquals(original.width, reloaded.width, "Width should match")
            assertEquals(original.height, reloaded.height, "Height should match")

            println("✅ Save and reload successful: ${reloaded.width}x${reloaded.height}")
        } finally {
            tempFile.delete()
        }
    }

    /**
     * 테스트 이미지 찾기
     *
     * Gradle 테스트는 프로젝트 루트에서 실행되므로
     * datas/case1/image1.png를 찾을 수 있습니다.
     */
    private fun findTestImage(): File {
        // 여러 경로 시도
        val candidates = listOf(
            File("datas/case1/image1.png"),  // 프로젝트 루트
            File("../datas/case1/image1.png"),  // 한 단계 위
            File("../../datas/case1/image1.png")  // 두 단계 위
        )

        val testImage = candidates.firstOrNull { it.exists() }
            ?: throw IllegalStateException(
                "Test image not found. Tried: ${candidates.map { it.absolutePath }}"
            )

        println("📁 Using test image: ${testImage.absolutePath}")
        return testImage
    }
}
