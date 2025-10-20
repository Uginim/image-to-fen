// =========================
// settings.gradle.kts
// =========================
rootProject.name = "image-to-fen"
include(
":fen",
":vision-api",
":ml-api",
":engine",
":demo-cli"
)

// =========================
// build.gradle.kts (root)
// =========================
plugins {
kotlin("jvm") version "2.0.20" apply false
}

allprojects {
group = "com.example.fenvision"
version = "0.1.0-SNAPSHOT"
}

subprojects {
repositories {
mavenCentral()
}
}

// ---- Common Kotlin config for JVM modules ----
project(":fen").plugins.apply("org.jetbrains.kotlin.jvm")
project(":vision-api").plugins.apply("org.jetbrains.kotlin.jvm")
project(":ml-api").plugins.apply("org.jetbrains.kotlin.jvm")
project(":engine").plugins.apply("org.jetbrains.kotlin.jvm")
project(":demo-cli").plugins.apply("org.jetbrains.kotlin.jvm")

// Configure each module with Java 17
listOf(":fen", ":vision-api", ":ml-api", ":engine", ":demo-cli").forEach { path ->
project(path).extensions.configure(org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension::class.java) {
jvmToolchain(17)
}
}

// Module dependencies
project(":engine").dependencies.apply {
add("implementation", project(":fen"))
add("implementation", project(":vision-api"))
add("implementation", project(":ml-api"))
}

project(":demo-cli").dependencies.apply {
add("implementation", project(":engine"))
add("implementation", project(":fen")) // for helpers in examples
add("implementation", project(":vision-api"))
add("implementation", project(":ml-api"))
}

// =========================
// fen/build.gradle.kts
// =========================
plugins { kotlin("jvm") }

dependencies { }

// =========================
// fen/src/main/kotlin/com/example/fenvision/fen/FenBuilder.kt
// =========================
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

// =========================
// fen/src/main/kotlin/com/example/fenvision/fen/BoardCell.kt
// =========================
package com.example.fenvision.fen

data class BoardCell(val file: Char, val rank: Int, val fenChar: Char) {
init {
require(file in 'a'..'h')
require(rank in 1..8)
}
}

// =========================
// vision-api/build.gradle.kts
// =========================
plugins { kotlin("jvm") }

dependencies { }

// =========================
// vision-api/src/main/kotlin/com/example/fenvision/vision/BoardModels.kt
// =========================
package com.example.fenvision.vision

/**
* Pure Kotlin API for board detection & grid extraction.
* Actual implementation (OpenCV/other) will live in another module/app.
  */

data class BoardWarpResult(
val normalizedBoard: ByteArray, // placeholder for a raster image; choose your own format later
val sideHint: SideHint? = null,
)

enum class SideHint { WHITE_BOTTOM, BLACK_BOTTOM }

/** Coordinates in chess terms. */
data class SquareCoord(val file: Char, val rank: Int)

data class SquarePatch(
val coord: SquareCoord,
val pixels: ByteArray // placeholder: raw/encoded patch; implementation-defined
)

interface BoardDetector {
/** Detects a chessboard and returns a normalized top-down raster. */
fun detectAndNormalize(imageBytes: ByteArray): BoardWarpResult
}

interface GridExtractor {
/** Splits a normalized board into 64 square patches a8..h1 order. */
fun split(board: BoardWarpResult): List<SquarePatch>
}

// =========================
// ml-api/build.gradle.kts
// =========================
plugins { kotlin("jvm") }

dependencies { }

// =========================
// ml-api/src/main/kotlin/com/example/fenvision/ml/PieceModels.kt
// =========================
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

// =========================
// engine/build.gradle.kts
// =========================
plugins { kotlin("jvm") }

dependencies {
implementation(project(":fen"))
implementation(project(":vision-api"))
implementation(project(":ml-api"))
}

// =========================
// engine/src/main/kotlin/com/example/fenvision/engine/FenEngine.kt
// =========================
package com.example.fenvision.engine

import com.example.fenvision.fen.FenBuilder
import com.example.fenvision.ml.Label
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

// =========================
// demo-cli/build.gradle.kts
// =========================
plugins { kotlin("jvm") }

dependencies {
implementation(project(":engine"))
implementation(project(":fen"))
implementation(project(":vision-api"))
implementation(project(":ml-api"))
}

application {
// If you apply the application plugin later, you can set mainClass here.
}

// =========================
// demo-cli/src/main/kotlin/com/example/fenvision/demo/Main.kt
// =========================
package com.example.fenvision.demo

import com.example.fenvision.engine.FenEngine
import com.example.fenvision.ml.Label
import com.example.fenvision.ml.PieceClassifier
import com.example.fenvision.ml.Prediction
import com.example.fenvision.vision.*

/**
* A tiny CLI demo using dummy implementations so the project runs without native libs.
  */
  fun main() {
  val detector = DummyBoardDetector()
  val grid = DummyGridExtractor()
  val clf = DummyPieceClassifier()
  val engine = FenEngine(detector, grid, clf)

  val fakeImage = ByteArray(0) // placeholder
  val fen = engine.imageToFen(fakeImage)
  println(fen)
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

    companion object { private var callIndex: Int = -1 }
}
