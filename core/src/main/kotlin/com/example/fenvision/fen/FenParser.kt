package com.example.fenvision.fen

/**
 * FEN 문자열을 파싱하여 8x8 보드 매트릭스로 변환합니다.
 */
object FenParser {
    /**
     * FEN 문자열을 파싱합니다.
     *
     * @param fen 전체 FEN 문자열 (예: "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
     * @return FenData 파싱된 FEN 데이터
     */
    fun parse(fen: String): FenData {
        val parts = fen.trim().split(" ")
        require(parts.size == 6) { "FEN must have 6 parts separated by spaces" }

        val boardPart = parts[0]
        val sideToMove = parts[1][0]
        val castling = parts[2]
        val enPassant = parts[3]
        val halfmove = parts[4].toInt()
        val fullmove = parts[5].toInt()

        val board = parseBoardPart(boardPart)

        return FenData(
            board = board,
            sideToMove = sideToMove,
            castling = castling,
            enPassant = enPassant,
            halfmove = halfmove,
            fullmove = fullmove
        )
    }

    /**
     * FEN의 보드 부분만 파싱합니다.
     *
     * @param boardPart "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR"
     * @return 8x8 매트릭스 (rank 8부터 rank 1 순서)
     */
    private fun parseBoardPart(boardPart: String): List<List<Char>> {
        val ranks = boardPart.split("/")
        require(ranks.size == 8) { "Board must have 8 ranks" }

        return ranks.map { parseRank(it) }
    }

    /**
     * FEN의 한 랭크를 파싱합니다.
     *
     * @param rank "rnbqkbnr" 또는 "8" 또는 "p4ppp"
     * @return 8개 칸의 리스트 ('1' = 빈칸)
     */
    private fun parseRank(rank: String): List<Char> {
        val result = mutableListOf<Char>()

        for (c in rank) {
            when {
                c.isDigit() -> {
                    // 빈칸 개수
                    val emptyCount = c.toString().toInt()
                    repeat(emptyCount) { result.add('1') }
                }
                else -> {
                    // 기물
                    result.add(c)
                }
            }
        }

        require(result.size == 8) { "Each rank must have 8 squares, got ${result.size} in '$rank'" }
        return result
    }
}

/**
 * 파싱된 FEN 데이터
 */
data class FenData(
    val board: List<List<Char>>,  // 8x8 매트릭스 (rank 8 -> rank 1)
    val sideToMove: Char,          // 'w' or 'b'
    val castling: String,          // "KQkq", "Kq", "-" 등
    val enPassant: String,         // "e3", "-" 등
    val halfmove: Int,             // 50수 규칙 카운터
    val fullmove: Int              // 수 번호
)
