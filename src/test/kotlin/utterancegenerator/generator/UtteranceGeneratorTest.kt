package utterancegenerator.generator

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import utterancegenerator.models.GeneratorOutput
import java.io.File

class UtteranceGeneratorTest {
    private val utteranceGenerator = UtteranceGenerator()

    @Test
    fun everythingCorrect() {
        val result = OBJECT_MAPPER.readValue<GeneratorOutput>(
            utteranceGenerator.parse(GRAMMAR)
        ).interactionModel.languageModel
        val expected = EXPECTED_OUTPUT.interactionModel.languageModel

        assertThat(result.invocationName).isEqualTo(expected.invocationName)
        assertThat(result.intents.size).isEqualTo(expected.intents.size)
        result.intents.forEach { intent ->
            assertThat(intent.name).isIn(expected.intents.map { it.name })
            val expectedIntent = expected.intents.firstOrNull { it.name == intent.name }!!
            assertThat(intent.samples).containsExactlyInAnyOrderElementsOf(expectedIntent.samples)
            assertThat(intent.slots).containsExactlyInAnyOrderElementsOf(expectedIntent.slots)
        }
        assertThat(result.types.size).isEqualTo(expected.types.size)
        result.types.forEach { type ->
            assertThat(type.name).isIn(expected.types.map { it.name })
            val expectedType = expected.types.first { it.name == type.name }
            assertThat(type.values.size).isEqualTo(expectedType.values.size)
            type.values.forEach { value ->
                assertThat(value.id).isIn(expectedType.values.map { it.id })
                val expectedValue = expectedType.values.first { it.id == value.id }
                assertThat(value.name.value).isEqualTo(expectedValue.name.value)
                assertThat(value.name.synonyms).containsExactlyInAnyOrderElementsOf(expectedValue.name.synonyms)
            }
        }
    }

    companion object {
        private val GRAMMAR = File(javaClass.classLoader.getResource("utterances.grammar").file).readText()
        private val EXPECTED_JSON = File(javaClass.classLoader.getResource("interaction_model.json").file).readText()

        private val OBJECT_MAPPER = jacksonObjectMapper()
        private val EXPECTED_OUTPUT = OBJECT_MAPPER.readValue<GeneratorOutput>(EXPECTED_JSON)
    }
}
