package io.github.inho.deepl_local

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeepLTranslateRequest(
    val text: List<String>,
    @SerialName("target_lang") val targetLang: String,
    @SerialName("source_lang") val sourceLang: String? = null
)

@Serializable
data class DeepLTranslateResponse(
    val translations: List<Translation>
) {
    @Serializable
    data class Translation(
        val text: String,
        @SerialName("detected_source_language") val detectedSourceLanguage: String? = null
    )
}

enum class Language(val code: String, val displayName: String) {
    AUTO("", "언어 감지"),
    KOREAN("KO", "한국어"),
    ENGLISH("EN-US", "영어"),
    JAPANESE("JA", "일본어"),
    CHINESE("ZH", "중국어"),
    GERMAN("DE", "독일어"),
    FRENCH("FR", "프랑스어"),
    SPANISH("ES", "스페인어")
}


