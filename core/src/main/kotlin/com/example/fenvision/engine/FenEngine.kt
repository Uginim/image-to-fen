package com.example.fenvision.engine

import com.example.fenvision.fen.FenBuilder
import com.example.fenvision.ml.PieceClassifier
import com.example.fenvision.vision.BoardDetector
import com.example.fenvision.vision.GridExtractor

class FenEngine(
    private val detector: BoardDetector,
    private val grid: GridExtractor,
    private val classifier: PieceClassifier
) {
    /**
     * Converts a raw image (bytes) into a FEN string using provided components.
     * This module is pure Kotlin and testable; implementations are injected.
     */
    fun imageToFen(imageBytes: ByteArray): String {
        val warped = detector.detectAndNormalize(imageBytes)
        val patches = grid.split(warped)

        // Build 8x8 matrix (rank 8..1, file a..h)
        val matrix = Array(8) { CharArray(8) { '1' } }
        for (p in patches) {
            val pred = classifier.predict(p.pixels)
            val fileIdx = p.coord.file - 'a'
            val rankIdxFromTop = 8 - p.coord.rank // rank 8 at top row index 0
            matrix[rankIdxFromTop][fileIdx] = pred.label.fenChar ?: '1'
        }
        val rows: List<List<Char>> = matrix.map { it.toList() }
        return FenBuilder.build(rows)
    }
}
