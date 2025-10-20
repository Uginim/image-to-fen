package com.example.fenvision.fen

data class BoardCell(val file: Char, val rank: Int, val fenChar: Char) {
    init {
        require(file in 'a'..'h')
        require(rank in 1..8)
    }
}
