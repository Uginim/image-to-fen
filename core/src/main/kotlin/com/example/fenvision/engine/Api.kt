package com.example.fenvision.engine

import com.example.fenvision.ml.PieceClassifier
import com.example.fenvision.vision.BoardDetector
import com.example.fenvision.vision.GridExtractor

/**
 * Functional-style convenience API: single call to convert image bytes to FEN.
 */
fun convertImageToFen(
    imageBytes: ByteArray,
    detector: BoardDetector,
    grid: GridExtractor,
    classifier: PieceClassifier
): String = FenEngine(detector, grid, classifier).imageToFen(imageBytes)
