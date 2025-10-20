package com.example.fenvision.fen

/** Build FEN strings from a 64-square description. */
object FenBuilder {
    /**
     * @param board 8 ranks × 8 files, indexed by rank (8..1) then file (a..h)
     * @param sideToMove 'w' or 'b'
     * @param castling something like "KQkq" or "-"
     * @param enPassant like "e3" or "-"
     */
    fun build(
        board: List<List<Char>>, // 8x8 of FEN piece chars or '1' for empty placeholder
        sideToMove: Char = 'w',
        castling: String = "-",
        enPassant: String = "-",
        halfmove: Int = 0,
        fullmove: Int = 1
    ): String {
        require(board.size == 8 && board.all { it.size == 8 }) { "board must be 8x8" }
        val ranks = board.joinToString("/") { compressRow(it) }
        return "$ranks $sideToMove $castling $enPassant $halfmove $fullmove"
    }

    private fun compressRow(chars: List<Char>): String {
        val sb = StringBuilder()
        var emptyRun = 0
        for (c in chars) {
            if (c == '1') emptyRun++ else {
                if (emptyRun > 0) { sb.append(emptyRun); emptyRun = 0 }
                sb.append(c)
            }
        }
        if (emptyRun > 0) sb.append(emptyRun)
        return sb.toString()
    }
}
