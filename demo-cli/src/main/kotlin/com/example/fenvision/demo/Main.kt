package com.example.fenvision.demo

import com.example.fenvision.engine.FenEngine
import com.example.fenvision.ml.Label
import com.example.fenvision.ml.PieceClassifier
import com.example.fenvision.ml.Prediction
import com.example.fenvision.vision.*
import java.io.File

/**
 * A tiny CLI demo using dummy implementations so the project runs without native libs.
 */
fun main(args: Array<String>) {
    println("=== Image-to-FEN Demo ===\n")

    // 1. Dummy 엔진 테스트
    println("1. Dummy Engine Test")
    println("-".repeat(50))
    val detector = DummyBoardDetector()
    val grid = DummyGridExtractor()
    val clf = DummyPieceClassifier()
    val engine = FenEngine(detector, grid, clf)

    val fakeImage = ByteArray(0) // placeholder
    val fen = engine.imageToFen(fakeImage)
    println("Generated FEN: $fen")
    println()

    // 2. 테스트 데이터 검증
    println("2. Test Data Validation")
    println("-".repeat(50))

    // 프로젝트 루트 기준으로 경로 설정
    // Gradle이 demo-cli 안에서 실행되므로 상위 디렉토리로 이동
    val currentDir = File(System.getProperty("user.dir"))
    val projectRoot = if (currentDir.name == "demo-cli") currentDir.parentFile else currentDir
    val datasDir = File(projectRoot, "datas")

    println("Looking for test data in: ${datasDir.absolutePath}")

    if (!datasDir.exists()) {
        println("⚠️  datas/ folder not found")
        println("   Please create test cases in datas/ directory")
        return
    }

    val results = TestDataValidator.validateAll(datasDir)

    if (results.isEmpty()) {
        println("⚠️  No test cases found in datas/")
        return
    }

    results.forEach { result ->
        when (result) {
            is ValidationResult.Success -> {
                val status = if (result.isValid) "✅" else "❌"
                println("\n$status ${result.caseName}")
                println("   Image:  ${result.imageFile}")
                println("   FEN:    ${result.fenFile}")
                println("   Original: ${result.originalFen}")
                if (!result.isValid) {
                    println("   Rebuilt:  ${result.rebuiltFen}")
                }
                println("\n   Board Preview:")
                result.boardPreview.lines().forEach { line ->
                    println("   $line")
                }
            }
            is ValidationResult.Error -> {
                println("\n❌ ${result.caseName}")
                println("   Error: ${result.message}")
            }
        }
    }

    println("\n" + "=".repeat(50))
    val successCount = results.count { it is ValidationResult.Success && it.isValid }
    val totalCount = results.size
    println("Result: $successCount/$totalCount test cases passed")
}

// ---- Dummy implementations (for compile/run) ----
class DummyBoardDetector : BoardDetector {
    override fun detectAndNormalize(imageBytes: ByteArray): BoardWarpResult =
        BoardWarpResult(normalizedBoard = ByteArray(0), sideHint = SideHint.WHITE_BOTTOM)
}

class DummyGridExtractor : GridExtractor {
    override fun split(board: BoardWarpResult): List<SquarePatch> {
        // Return a simple start-position board as 64 empty patches; pieces provided by classifier
        val out = mutableListOf<SquarePatch>()
        for (r in 8 downTo 1) {
            for (f in 'a'..'h') {
                out += SquarePatch(SquareCoord(f, r), pixels = byteArrayOf())
            }
        }
        return out
    }
}

class DummyPieceClassifier : PieceClassifier {
    override fun predict(patchBytes: ByteArray): Prediction {
        // Deterministic fake: set up the standard chess starting position
        // We will fill based on an internal counter over calls using a static index.
        val idx = callIndex++
        val rank = 8 - (idx / 8)
        val file = 'a' + (idx % 8)
        val label = stdStartLabel(rank, file)
        return Prediction(label, confidence = 1.0)
    }

    private fun stdStartLabel(rank: Int, file: Char): Label = when (rank) {
        8 -> when (file) {
            'a','h' -> Label.bR
            'b','g' -> Label.bN
            'c','f' -> Label.bB
            'd' -> Label.bQ
            'e' -> Label.bK
            else -> Label.EMPTY
        }
        7 -> Label.bP
        2 -> Label.wP
        1 -> when (file) {
            'a','h' -> Label.wR
            'b','g' -> Label.wN
            'c','f' -> Label.wB
            'd' -> Label.wQ
            'e' -> Label.wK
            else -> Label.EMPTY
        }
        else -> Label.EMPTY
    }

    companion object {
        private var callIndex: Int = 0
    }
}
