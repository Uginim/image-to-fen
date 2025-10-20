package com.example.fenvision.vision

/**
 * Pure Kotlin API for board detection & grid extraction.
 * Actual implementation (OpenCV/other) will live in another module/app.
 */

data class BoardWarpResult(
    val normalizedBoard: ByteArray, // placeholder for a raster image; choose your own format later
    val sideHint: SideHint? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BoardWarpResult

        if (!normalizedBoard.contentEquals(other.normalizedBoard)) return false
        if (sideHint != other.sideHint) return false

        return true
    }

    override fun hashCode(): Int {
        var result = normalizedBoard.contentHashCode()
        result = 31 * result + (sideHint?.hashCode() ?: 0)
        return result
    }
}

enum class SideHint { WHITE_BOTTOM, BLACK_BOTTOM }

/** Coordinates in chess terms. */
data class SquareCoord(val file: Char, val rank: Int)

data class SquarePatch(
    val coord: SquareCoord,
    val pixels: ByteArray // placeholder: raw/encoded patch; implementation-defined
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SquarePatch

        if (coord != other.coord) return false
        if (!pixels.contentEquals(other.pixels)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = coord.hashCode()
        result = 31 * result + pixels.contentHashCode()
        return result
    }
}

interface BoardDetector {
    /** Detects a chessboard and returns a normalized top-down raster. */
    fun detectAndNormalize(imageBytes: ByteArray): BoardWarpResult
}

interface GridExtractor {
    /** Splits a normalized board into 64 square patches a8..h1 order. */
    fun split(board: BoardWarpResult): List<SquarePatch>
}
