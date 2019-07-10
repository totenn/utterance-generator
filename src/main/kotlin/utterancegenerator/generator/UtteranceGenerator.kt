package utterancegenerator.generator

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import utterancegenerator.models.CustomSlotDefinition
import utterancegenerator.models.CustomSlotName
import utterancegenerator.models.GeneratorOutput
import utterancegenerator.models.Intent
import utterancegenerator.models.InteractionModel
import utterancegenerator.models.LanguageModel
import utterancegenerator.models.SlotDefinition
import utterancegenerator.models.Type

class UtteranceGenerator {
    companion object {
        private val symbolRegex = Regex("(?m)^([\\w._]+|\\{\\w._]+}):")
        private val simpleTokenRegex = Regex("\\{([^{}]+)}")
        private val slotTokenRegex = Regex("\\{\\{([|\\w:._ ]+)}}")
        private val repeatedWhitespaceRegex = Regex("\\s+")
        private val objectMapper = jacksonObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
    }

    fun parse(grammar: String): String {
        val symbols = symbolRegex.findAll(grammar).map { it.groupValues[1] }
        val symbolDefinitions = symbols.zip(symbols.drop(1).plus("")).map {
            it.first to findSymbolDefinition(grammar, it.first, it.second)
        }

        val intentDefinitions = symbolDefinitions
            .filter { it.first != "Invocation" && !it.first.startsWith("{") }
            .map {
                Intent(
                    it.first,
                    it.second.flatMap { replaceSimpleTokens(it).flatMap { replaceSlotTokens(it) } }
                        .map { trimExtraWhitespace(it) }
                        .filter { it != "" }
                        .distinct()
                        .toList(),
                    it.second.flatMap { extractSlotDefinitions(it) }.distinct().toList()
                )
            }.toList()
        val typeDefinitions = symbolDefinitions
            .filter { it.first != "Invocation" && it.first.startsWith("{") }
            .map {
                Type(
                    it.first.drop(1).dropLast(1),
                    it.second.map { extractCustomSlotDefinition(it) }
                )
            }.toList()
        val invocationName = symbolDefinitions.first { it.first == "Invocation" }.second[0]

        return objectMapper.writeValueAsString(
            GeneratorOutput(
                InteractionModel(
                    LanguageModel(
                        intentDefinitions,
                        typeDefinitions,
                        invocationName
                    )
                )
            )
        )
    }

    private fun findSymbolDefinition(
        grammar: String,
        thisSymbol: String,
        nextSymbol: String
    ): List<String> {
        return if (nextSymbol != "") {
            grammar.substringAfter("$thisSymbol:").substringBefore(nextSymbol)
        } else {
            grammar.substringAfter("$thisSymbol:")
        }.trim().split("\n")
    }

    private fun replaceSimpleTokens(intentDefinitionLine: String): List<String> {
        val nextToken = simpleTokenRegex.find(intentDefinitionLine)?.groupValues?.getOrNull(1)
            ?: return listOf(intentDefinitionLine)
        val stringBefore = intentDefinitionLine.substringBefore("{$nextToken}")
        val stringAfter = intentDefinitionLine.substringAfter("{$nextToken}")

        return nextToken.split("|").flatMap { tokenElement ->
            replaceSimpleTokens(stringAfter).map {
                stringBefore + tokenElement + it
            }
        }
    }

    private fun replaceSlotTokens(intentDefinitionLine: String): List<String> {
        val nextToken = slotTokenRegex.find(intentDefinitionLine)?.groupValues?.getOrNull(1)
            ?: return listOf(intentDefinitionLine)
        val stringBefore = intentDefinitionLine.substringBefore("{{$nextToken}}")
        val stringAfter = intentDefinitionLine.substringAfter("{{$nextToken}}")

        return nextToken.split("|").flatMap { tokenElement ->
            replaceSlotTokens(stringAfter).map {
                if (tokenElement != "") {
                    stringBefore + "{${tokenElement.substringBefore(":")}}" + it
                } else {
                    stringBefore + it
                }
            }
        }
    }

    private fun extractSlotDefinitions(intentDefinitionLine: String): List<SlotDefinition> {
        return slotTokenRegex.findAll(intentDefinitionLine).toList().flatMap {
            it.groupValues[1].split("|").mapNotNull {
                if (it != "") {
                    val splitDefinition = it.split(":")
                    if (splitDefinition.size < 2) {
                        SlotDefinition(
                            splitDefinition[0],
                            splitDefinition[0]
                        )
                    } else {
                        SlotDefinition(
                            splitDefinition[0],
                            splitDefinition[1]
                        )
                    }
                } else null
            }
        }
    }

    private fun extractCustomSlotDefinition(customSlotDefinitionLine: String): CustomSlotDefinition {
        if (!customSlotDefinitionLine.startsWith("{")) {
            return CustomSlotDefinition(
                customSlotDefinitionLine,
                CustomSlotName(
                    customSlotDefinitionLine,
                    listOf()
                )
            )
        }

        val splitBetweenIdAndValueSynonyms = customSlotDefinitionLine.drop(1).dropLast(1).split(":")
        val splitBetweenValueAndSynomyms = splitBetweenIdAndValueSynonyms[1].split("|")

        val id = splitBetweenIdAndValueSynonyms[0]
        val value = splitBetweenValueAndSynomyms[0]
        val synonyms = splitBetweenValueAndSynomyms.drop(1)

        return CustomSlotDefinition(
            id,
            CustomSlotName(
                value,
                synonyms
            )
        )
    }

    private fun trimExtraWhitespace(string: String): String {
        return string.replace(repeatedWhitespaceRegex) { it.value[0].toString() }.trim()
    }
}
