package com.example.fenvision.demo

import com.example.fenvision.fen.FenBuilder
import com.example.fenvision.fen.FenParser
import java.io.File

/**
 * 테스트 데이터의 FEN 파일을 검증합니다.
 */
object TestDataValidator {

    /**
     * 특정 케이스의 FEN 파일을 검증합니다.
     */
    fun validateCase(caseDir: File): ValidationResult {
        val caseName = caseDir.name

        // FEN 파일 찾기
        val fenFile = caseDir.listFiles()?.find { it.name.endsWith("_fen.txt") }
            ?: return ValidationResult.Error(caseName, "FEN file not found")

        val imageFile = caseDir.listFiles()?.find { it.extension in listOf("png", "jpg", "jpeg") }
            ?: return ValidationResult.Error(caseName, "Image file not found")

        try {
            // FEN 읽기
            val fenText = fenFile.readText().trim()

            // FEN 파싱
            val data = FenParser.parse(fenText)

            // 다시 빌드
            val rebuiltFen = FenBuilder.build(
                board = data.board,
                sideToMove = data.sideToMove,
                castling = data.castling,
                enPassant = data.enPassant,
                halfmove = data.halfmove,
                fullmove = data.fullmove
            )

            // 원본과 같은지 확인
            val isValid = fenText == rebuiltFen

            return ValidationResult.Success(
                caseName = caseName,
                originalFen = fenText,
                rebuiltFen = rebuiltFen,
                isValid = isValid,
                imageFile = imageFile.name,
                fenFile = fenFile.name,
                boardPreview = boardToString(data.board)
            )

        } catch (e: Exception) {
            return ValidationResult.Error(caseName, "Parsing failed: ${e.message}")
        }
    }

    /**
     * datas/ 폴더의 모든 케이스를 검증합니다.
     */
    fun validateAll(datasDir: File): List<ValidationResult> {
        if (!datasDir.exists() || !datasDir.isDirectory) {
            return emptyList()
        }

        return datasDir.listFiles()
            ?.filter { it.isDirectory && it.name.startsWith("case") }
            ?.map { validateCase(it) }
            ?: emptyList()
    }

    /**
     * 보드를 문자열로 변환 (시각화)
     */
    private fun boardToString(board: List<List<Char>>): String {
        val sb = StringBuilder()
        board.forEachIndexed { rankIdx, rank ->
            val rankNumber = 8 - rankIdx
            sb.append("$rankNumber ")
            rank.forEach { piece ->
                sb.append(if (piece == '1') '·' else piece)
                sb.append(' ')
            }
            sb.append('\n')
        }
        sb.append("  a b c d e f g h")
        return sb.toString()
    }
}

/**
 * 검증 결과
 */
sealed class ValidationResult {
    abstract val caseName: String

    data class Success(
        override val caseName: String,
        val originalFen: String,
        val rebuiltFen: String,
        val isValid: Boolean,
        val imageFile: String,
        val fenFile: String,
        val boardPreview: String
    ) : ValidationResult()

    data class Error(
        override val caseName: String,
        val message: String
    ) : ValidationResult()
}
