package com.example.fenvision.vision.impl

import com.example.fenvision.vision.CornerPoints
import com.example.fenvision.vision.Point
import com.example.fenvision.vision.SideHint
import com.example.fenvision.image.ImageUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeAll
import kotlin.test.*
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO

class ManualBoardDetectorTest {

    companion object {
        @JvmStatic
        @BeforeAll
        fun setup() {
            // Ensure OpenCV is loaded before running tests
            nu.pattern.OpenCV.loadLocally()
        }
    }

    @Test
    fun `test board normalization with straight image`() {
        // Create a simple 800x800 test image (chessboard pattern)
        val testImage = createTestChessboard(800, 800)
        val imageBytes = bufferedImageToBytes(testImage)

        // Corners are already aligned (no perspective distortion)
        val corners = CornerPoints(
            topLeft = Point(0.0, 0.0),
            topRight = Point(799.0, 0.0),
            bottomRight = Point(799.0, 799.0),
            bottomLeft = Point(0.0, 799.0)
        )

        val detector = ManualBoardDetector(outputSize = 800)
        val result = detector.normalizeBoard(imageBytes, corners)

        // Verify size
        val resultInfo = ImageUtils.getImageInfo(result.normalizedBoard)
        assertEquals(800, resultInfo.width, "Width should be 800")
        assertEquals(800, resultInfo.height, "Height should be 800")

        // Verify result contains image data
        assertTrue(result.normalizedBoard.isNotEmpty(), "Normalized board should not be empty")
    }

    @Test
    fun `test board normalization with skewed image`() {
        // Create a larger image with the board in the center, skewed
        val testImage = createTestChessboard(1200, 1200)
        val imageBytes = bufferedImageToBytes(testImage)

        // Corners represent a skewed perspective
        val corners = CornerPoints(
            topLeft = Point(200.0, 100.0),
            topRight = Point(1000.0, 150.0),
            bottomRight = Point(950.0, 1000.0),
            bottomLeft = Point(150.0, 950.0)
        )

        val detector = ManualBoardDetector(outputSize = 800)
        val result = detector.normalizeBoard(imageBytes, corners)

        // Verify size is normalized to 800x800
        val resultInfo = ImageUtils.getImageInfo(result.normalizedBoard)
        assertEquals(800, resultInfo.width, "Width should be normalized to 800")
        assertEquals(800, resultInfo.height, "Height should be normalized to 800")
    }

    @Test
    fun `test side hint determination`() {
        // Create an image with white at bottom (brighter), black at top (darker)
        val testImage = createGradientChessboard(800, 800)
        val imageBytes = bufferedImageToBytes(testImage)

        val corners = CornerPoints(
            topLeft = Point(0.0, 0.0),
            topRight = Point(799.0, 0.0),
            bottomRight = Point(799.0, 799.0),
            bottomLeft = Point(0.0, 799.0)
        )

        val detector = ManualBoardDetector(outputSize = 800)
        val result = detector.normalizeBoard(imageBytes, corners)

        // Verify side hint is determined
        assertNotNull(result.sideHint, "Side hint should not be null")
        assertEquals(SideHint.WHITE_BOTTOM, result.sideHint, "Should detect white at bottom")
    }

    @Test
    fun `test corner order validation`() {
        val testImage = createTestChessboard(800, 800)
        val imageBytes = bufferedImageToBytes(testImage)

        // Test with various corner orderings
        val corners1 = CornerPoints(
            topLeft = Point(0.0, 0.0),
            topRight = Point(799.0, 0.0),
            bottomRight = Point(799.0, 799.0),
            bottomLeft = Point(0.0, 799.0)
        )

        val detector = ManualBoardDetector(outputSize = 800)

        // Should not throw exception
        assertNotNull(detector.normalizeBoard(imageBytes, corners1))
    }

    @Test
    fun `test quality validation - correct size`() {
        val testImage = createTestChessboard(800, 800)
        val imageBytes = bufferedImageToBytes(testImage)

        val corners = CornerPoints(
            topLeft = Point(0.0, 0.0),
            topRight = Point(799.0, 0.0),
            bottomRight = Point(799.0, 799.0),
            bottomLeft = Point(0.0, 799.0)
        )

        val detector = ManualBoardDetector(outputSize = 800)
        val result = detector.normalizeBoard(imageBytes, corners)

        val warnings = detector.validateQuality(result.normalizedBoard)

        // Should have no size warnings
        assertFalse(
            warnings.any { it.contains("size") },
            "Should not have size warnings for correct size"
        )
    }

    @Test
    fun `test quality validation - brightness check`() {
        // Create a very dark image
        val darkImage = BufferedImage(800, 800, BufferedImage.TYPE_INT_RGB)
        val g = darkImage.createGraphics()
        g.color = Color(20, 20, 20)  // Very dark
        g.fillRect(0, 0, 800, 800)
        g.dispose()
        val darkBytes = bufferedImageToBytes(darkImage)

        val detector = ManualBoardDetector(outputSize = 800)
        val warnings = detector.validateQuality(darkBytes)

        // Should detect that image is too dark
        assertTrue(
            warnings.any { it.contains("too dark") },
            "Should warn about dark image"
        )
    }

    @Test
    fun `test different output sizes`() {
        val testImage = createTestChessboard(1000, 1000)
        val imageBytes = bufferedImageToBytes(testImage)

        val corners = CornerPoints(
            topLeft = Point(100.0, 100.0),
            topRight = Point(900.0, 100.0),
            bottomRight = Point(900.0, 900.0),
            bottomLeft = Point(100.0, 900.0)
        )

        // Test with different output sizes
        val detector400 = ManualBoardDetector(outputSize = 400)
        val result400 = detector400.normalizeBoard(imageBytes, corners)
        val info400 = ImageUtils.getImageInfo(result400.normalizedBoard)
        assertEquals(400, info400.width)
        assertEquals(400, info400.height)

        val detector1024 = ManualBoardDetector(outputSize = 1024)
        val result1024 = detector1024.normalizeBoard(imageBytes, corners)
        val info1024 = ImageUtils.getImageInfo(result1024.normalizedBoard)
        assertEquals(1024, info1024.width)
        assertEquals(1024, info1024.height)
    }

    @Test
    fun `test unsupported detectAndNormalize throws exception`() {
        val testImage = createTestChessboard(800, 800)
        val imageBytes = bufferedImageToBytes(testImage)

        val detector = ManualBoardDetector()

        // Should throw UnsupportedOperationException
        assertFailsWith<UnsupportedOperationException> {
            detector.detectAndNormalize(imageBytes)
        }
    }

    // Helper functions

    private fun createTestChessboard(width: Int, height: Int): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g = image.createGraphics()

        val squareSize = width / 8
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val isLight = (row + col) % 2 == 0
                g.color = if (isLight) Color.WHITE else Color.DARK_GRAY
                g.fillRect(col * squareSize, row * squareSize, squareSize, squareSize)
            }
        }

        g.dispose()
        return image
    }

    private fun createGradientChessboard(width: Int, height: Int): BufferedImage {
        val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        val g = image.createGraphics()

        // Top half darker (black pieces)
        g.color = Color(100, 100, 100)
        g.fillRect(0, 0, width, height / 2)

        // Bottom half brighter (white pieces)
        g.color = Color(200, 200, 200)
        g.fillRect(0, height / 2, width, height / 2)

        g.dispose()
        return image
    }

    private fun bufferedImageToBytes(image: BufferedImage): ByteArray {
        val baos = ByteArrayOutputStream()
        ImageIO.write(image, "png", baos)
        return baos.toByteArray()
    }
}
