package com.example.fenvision.fen

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FenParserTest {

    @Test
    fun `parse starting position`() {
        val fen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

        val data = FenParser.parse(fen)

        // 보드 확인
        assertEquals(8, data.board.size)
        assertEquals(8, data.board[0].size)

        // rank 8 확인
        assertEquals(listOf('r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'), data.board[0])

        // rank 1 확인
        assertEquals(listOf('R', 'N', 'B', 'Q', 'K', 'B', 'N', 'R'), data.board[7])

        // 메타 정보 확인
        assertEquals('w', data.sideToMove)
        assertEquals("KQkq", data.castling)
        assertEquals("-", data.enPassant)
        assertEquals(0, data.halfmove)
        assertEquals(1, data.fullmove)
    }

    @Test
    fun `parse and rebuild should return same FEN`() {
        val originalFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

        // 파싱
        val data = FenParser.parse(originalFen)

        // 다시 빌드
        val rebuiltFen = FenBuilder.build(
            board = data.board,
            sideToMove = data.sideToMove,
            castling = data.castling,
            enPassant = data.enPassant,
            halfmove = data.halfmove,
            fullmove = data.fullmove
        )

        assertEquals(originalFen, rebuiltFen)
    }

    @Test
    fun `parse case1 FEN from test data`() {
        // case1: rnbqkbnr/p4ppp/1p1pp3/2p5/3PP3/2P2N2/PP3PPP/RNBQKB1R b KQkq - 0 5
        val fen = "rnbqkbnr/p4ppp/1p1pp3/2p5/3PP3/2P2N2/PP3PPP/RNBQKB1R b KQkq - 0 5"

        val data = FenParser.parse(fen)

        // rank 8 확인
        assertEquals(listOf('r', 'n', 'b', 'q', 'k', 'b', 'n', 'r'), data.board[0])

        // rank 7 확인 (p4ppp = p + 4 empty + ppp)
        assertEquals(listOf('p', '1', '1', '1', '1', 'p', 'p', 'p'), data.board[1])

        // rank 6 확인 (1p1pp3 = empty + p + empty + pp + 3 empty)
        assertEquals(listOf('1', 'p', '1', 'p', 'p', '1', '1', '1'), data.board[2])

        // 메타 정보
        assertEquals('b', data.sideToMove)
        assertEquals("KQkq", data.castling)
        assertEquals(5, data.fullmove)

        // 다시 빌드해서 확인
        val rebuiltFen = FenBuilder.build(
            board = data.board,
            sideToMove = data.sideToMove,
            castling = data.castling,
            enPassant = data.enPassant,
            halfmove = data.halfmove,
            fullmove = data.fullmove
        )

        assertEquals(fen, rebuiltFen)
    }

    @Test
    fun `parse case2 FEN from test data`() {
        // case2: r2k2nr/p5pp/1pnb4/5b2/8/4B3/PP1K1PPP/3R3R w - - 0 19
        val fen = "r2k2nr/p5pp/1pnb4/5b2/8/4B3/PP1K1PPP/3R3R w - - 0 19"

        val data = FenParser.parse(fen)

        // rank 8 확인 (r2k2nr)
        assertEquals(listOf('r', '1', '1', 'k', '1', '1', 'n', 'r'), data.board[0])

        // rank 7 확인 (p5pp)
        assertEquals(listOf('p', '1', '1', '1', '1', '1', 'p', 'p'), data.board[1])

        // rank 5 확인 (5b2)
        assertEquals(listOf('1', '1', '1', '1', '1', 'b', '1', '1'), data.board[3])

        // 메타 정보
        assertEquals('w', data.sideToMove)
        assertEquals("-", data.castling)  // 캐슬링 불가
        assertEquals("-", data.enPassant)
        assertEquals(19, data.fullmove)

        // 다시 빌드해서 확인
        val rebuiltFen = FenBuilder.build(
            board = data.board,
            sideToMove = data.sideToMove,
            castling = data.castling,
            enPassant = data.enPassant,
            halfmove = data.halfmove,
            fullmove = data.fullmove
        )

        assertEquals(fen, rebuiltFen)
    }

    @Test
    fun `parse complex position with en passant`() {
        val fen = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"

        val data = FenParser.parse(fen)

        assertEquals('b', data.sideToMove)
        assertEquals("KQkq", data.castling)
        assertEquals("e3", data.enPassant)  // 앙파상 가능
    }

    @Test
    fun `parse empty board`() {
        val fen = "8/8/8/8/8/8/8/8 w - - 0 1"

        val data = FenParser.parse(fen)

        // 모든 칸이 비어있어야 함
        data.board.forEach { rank ->
            assertEquals(8, rank.size)
            rank.forEach { square ->
                assertEquals('1', square)
            }
        }
    }
}
