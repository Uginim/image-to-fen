package com.example.fenvision.ml

enum class Label(val fenChar: Char?) {
    EMPTY(null),
    wK('K'), wQ('Q'), wR('R'), wB('B'), wN('N'), wP('P'),
    bK('k'), bQ('q'), bR('r'), bB('b'), bN('n'), bP('p');
}

data class Prediction(val label: Label, val confidence: Double)

interface PieceClassifier {
    fun predict(patchBytes: ByteArray): Prediction
}
