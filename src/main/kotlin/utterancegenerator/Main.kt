package utterancegenerator

import utterancegenerator.generator.UtteranceGenerator
import java.io.File

fun main(args: Array<String>) {
    val inputFileName = args[0]
    val outputFileName = args[1]

    val grammar = File(inputFileName).readText()
    val interactionModel = UtteranceGenerator().parse(grammar)
    File(outputFileName).writeText(interactionModel)
}
