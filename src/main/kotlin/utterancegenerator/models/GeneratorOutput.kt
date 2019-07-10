package utterancegenerator.models

data class GeneratorOutput(val interactionModel: InteractionModel)
data class InteractionModel(val languageModel: LanguageModel)
data class LanguageModel(
    val intents: List<Intent> = listOf(),
    val types: List<Type> = listOf(),
    val invocationName: String
)

data class Intent(
    val name: String,
    val samples: List<String> = listOf(),
    val slots: List<SlotDefinition> = listOf()
)

data class SlotDefinition(val name: String, val type: String)
data class Type(val name: String, val values: List<CustomSlotDefinition> = listOf())
data class CustomSlotDefinition(val id: String, val name: CustomSlotName)
data class CustomSlotName(val value: String, val synonyms: List<String> = listOf())
