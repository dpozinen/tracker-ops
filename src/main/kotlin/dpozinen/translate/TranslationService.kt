package dpozinen.translate

import com.google.cloud.translate.v3.LocationName
import com.google.cloud.translate.v3.TranslateTextRequest
import com.google.cloud.translate.v3.TranslationServiceClient
import org.springframework.stereotype.Service


@Service
class TranslationService {

//    TODO(nice event)
    fun process(event: Map<*, *>) {

    }

    fun translateText(projectId: String, targetLanguage: String, text: String) {

    }
}

fun main1() {
    TranslationServiceClient.create().use { client ->
        val parent = LocationName.of("eminent-bond-404120", "global")
        val request = TranslateTextRequest.newBuilder()
            .setParent(parent.toString())
            .setMimeType("text/plain")
            .setTargetLanguageCode("uk")
            .addContents("<i>...the great Elven-smiths forged Rings of Power.</i>")
            .build()
        val response = client.translateText(request)

        // Display the translation for each input text provided
        response.translationsList.forEach { translation ->
            System.out.printf("Translated text: %s\n", translation.translatedText)
        }
    }
}