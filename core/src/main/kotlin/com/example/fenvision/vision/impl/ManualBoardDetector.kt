package com.example.fenvision.vision.impl

import com.example.fenvision.vision.*
import com.example.fenvision.image.ImageData
import com.example.fenvision.image.ImageUtils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import java.awt.image.BufferedImage

/**
 * Manual board detector that requires user to specify the four corners of the chessboard.
 * Performs perspective transformation to normalize the board to a top-down view.
 */
class ManualBoardDetector(
    private val outputSize: Int = 800
) : BoardDetector {

    init {
        // Load OpenCV native library
        nu.pattern.OpenCV.loadLocally()
    }

    /**
     * Detects and normalizes a chessboard using manually specified corners.
     *
     * @param imageBytes The input image as a byte array
     * @param corners The four corners of the chessboard (topLeft, topRight, bottomRight, bottomLeft)
     * @return BoardWarpResult containing the normalized 800x800 board and side hint
     */
    fun normalizeBoard(imageBytes: ByteArray, corners: CornerPoints): BoardWarpResult {
        // Load image
        val imageData = ImageUtils.loadImage(imageBytes)
        val mat = bufferedImageToMat(imageData.image)

        // Define source points (from corners)
        val srcPoints = MatOfPoint2f(
            org.opencv.core.Point(corners.topLeft.x, corners.topLeft.y),
            org.opencv.core.Point(corners.topRight.x, corners.topRight.y),
            org.opencv.core.Point(corners.bottomRight.x, corners.bottomRight.y),
            org.opencv.core.Point(corners.bottomLeft.x, corners.bottomLeft.y)
        )

        // Define destination points (normalized square)
        val dstPoints = MatOfPoint2f(
            org.opencv.core.Point(0.0, 0.0),
            org.opencv.core.Point(outputSize.toDouble() - 1, 0.0),
            org.opencv.core.Point(outputSize.toDouble() - 1, outputSize.toDouble() - 1),
            org.opencv.core.Point(0.0, outputSize.toDouble() - 1)
        )

        // Calculate homography matrix (perspective transform)
        val transformMatrix = Imgproc.getPerspectiveTransform(srcPoints, dstPoints)

        // Apply perspective transformation
        val warpedMat = Mat()
        Imgproc.warpPerspective(
            mat,
            warpedMat,
            transformMatrix,
            Size(outputSize.toDouble(), outputSize.toDouble())
        )

        // Determine side hint based on corner brightness
        val sideHint = determineSideHint(warpedMat)

        // Convert back to byte array
        val normalizedBytes = matToByteArray(warpedMat)

        // Clean up
        mat.release()
        warpedMat.release()
        transformMatrix.release()
        srcPoints.release()
        dstPoints.release()

        return BoardWarpResult(normalizedBytes, sideHint)
    }

    /**
     * Required implementation of BoardDetector interface.
     * This implementation requires manual corner specification, so it cannot work
     * with just image bytes. Use normalizeBoard() directly instead.
     */
    override fun detectAndNormalize(imageBytes: ByteArray): BoardWarpResult {
        throw UnsupportedOperationException(
            "ManualBoardDetector requires corner points. Use normalizeBoard(imageBytes, corners) instead."
        )
    }

    /**
     * Determines which side (white/black) is at the bottom based on corner brightness.
     * White pieces are typically lighter than black pieces.
     */
    private fun determineSideHint(normalizedBoard: Mat): SideHint {
        val height = normalizedBoard.rows()
        val squareSize = height / 8

        // Sample bottom-left corner (a1) and top-left corner (a8)
        val bottomLeftRect = Rect(0, height - squareSize, squareSize, squareSize)
        val topLeftRect = Rect(0, 0, squareSize, squareSize)

        val bottomLeftSquare = Mat(normalizedBoard, bottomLeftRect)
        val topLeftSquare = Mat(normalizedBoard, topLeftRect)

        // Calculate average brightness
        val bottomBrightness = Core.mean(bottomLeftSquare).`val`[0]
        val topBrightness = Core.mean(topLeftSquare).`val`[0]

        bottomLeftSquare.release()
        topLeftSquare.release()

        // If bottom is brighter, assume white is at bottom
        return if (bottomBrightness > topBrightness) {
            SideHint.WHITE_BOTTOM
        } else {
            SideHint.BLACK_BOTTOM
        }
    }

    /**
     * Converts BufferedImage to OpenCV Mat.
     */
    private fun bufferedImageToMat(image: BufferedImage): Mat {
        val mat = Mat(image.height, image.width, CvType.CV_8UC3)
        val data = ByteArray(image.width * image.height * 3)

        var index = 0
        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val rgb = image.getRGB(x, y)
                val r = (rgb shr 16) and 0xFF
                val g = (rgb shr 8) and 0xFF
                val b = rgb and 0xFF
                data[index++] = b.toByte()  // OpenCV uses BGR
                data[index++] = g.toByte()
                data[index++] = r.toByte()
            }
        }

        mat.put(0, 0, data)
        return mat
    }

    /**
     * Converts OpenCV Mat to byte array (PNG format).
     */
    private fun matToByteArray(mat: Mat): ByteArray {
        val bufferedImage = matToBufferedImage(mat)
        val baos = ByteArrayOutputStream()
        ImageIO.write(bufferedImage, "png", baos)
        return baos.toByteArray()
    }

    /**
     * Converts OpenCV Mat to BufferedImage.
     */
    private fun matToBufferedImage(mat: Mat): BufferedImage {
        val type = if (mat.channels() == 1) BufferedImage.TYPE_BYTE_GRAY else BufferedImage.TYPE_3BYTE_BGR
        val bufferedImage = BufferedImage(mat.cols(), mat.rows(), type)
        val data = ByteArray(mat.cols() * mat.rows() * mat.channels())
        mat.get(0, 0, data)

        if (type == BufferedImage.TYPE_BYTE_GRAY) {
            bufferedImage.raster.setDataElements(0, 0, mat.cols(), mat.rows(), data)
        } else {
            var index = 0
            for (y in 0 until mat.rows()) {
                for (x in 0 until mat.cols()) {
                    val b = data[index++].toInt() and 0xFF
                    val g = data[index++].toInt() and 0xFF
                    val r = data[index++].toInt() and 0xFF
                    val rgb = (r shl 16) or (g shl 8) or b
                    bufferedImage.setRGB(x, y, rgb)
                }
            }
        }

        return bufferedImage
    }

    /**
     * Validates the quality of the normalized board.
     * Returns a list of warnings if any quality issues are detected.
     */
    fun validateQuality(boardBytes: ByteArray): List<String> {
        val warnings = mutableListOf<String>()
        val imageData = ImageUtils.getImageInfo(boardBytes)

        // Check size
        if (imageData.width != outputSize || imageData.height != outputSize) {
            warnings.add("Image size is ${imageData.width}x${imageData.height}, expected ${outputSize}x${outputSize}")
        }

        // Check brightness
        val mat = bufferedImageToMat(ImageUtils.loadImage(boardBytes).image)
        val meanBrightness = Core.mean(mat).`val`[0]

        if (meanBrightness < 30) {
            warnings.add("Image is too dark (brightness: ${meanBrightness.toInt()})")
        } else if (meanBrightness > 225) {
            warnings.add("Image is too bright (brightness: ${meanBrightness.toInt()})")
        }

        // Check contrast
        val stdDev = MatOfDouble()
        Core.meanStdDev(mat, MatOfDouble(), stdDev)
        val contrast = stdDev.toArray()[0]

        if (contrast < 20) {
            warnings.add("Image has low contrast (${contrast.toInt()})")
        }

        mat.release()
        stdDev.release()

        return warnings
    }
}
