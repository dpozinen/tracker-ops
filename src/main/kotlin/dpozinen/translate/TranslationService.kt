package dpozinen.translate

import com.github.dnbn.submerge.api.parser.ParserFactory
import com.github.dnbn.submerge.api.subtitle.ass.ASSSub
import com.github.dnbn.submerge.api.subtitle.common.TimedLine
import com.github.dnbn.submerge.api.subtitle.common.TimedTextFile
import com.google.cloud.translate.v3.LocationName
import com.google.cloud.translate.v3.TranslateTextRequest
import com.google.cloud.translate.v3.TranslationServiceClient
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.io.InputStream


//@Service
class TranslationService {

    private val translationServiceClient = TranslationServiceClient.create()

    //    TODO(nice event)
    suspend fun process(event: Map<*, *>) {
        val file: TimedTextFile = ASSSub()
        ParserFactory.getParser("srt").parse(InputStream.nullInputStream(), "")
            .timedLines
            .toList()
            .pmap { translate(it) }
            .fold(file) { subs, line -> subs.add(line) }
    }

    fun translateText(projectId: String, targetLanguage: String, text: String) {

    }

    private fun translate(line: TimedLine): TimedLine {
        translationServiceClient.use { client ->
            val parent = LocationName.of("eminent-bond-404120", "global")
            val request = TranslateTextRequest.newBuilder()
                .setParent(parent.toString())
                .setMimeType("text/plain")
                .setTargetLanguageCode("uk")
                .addContents(line.textLines.joinToString())
                .build()
            val response = client.translateText(request)

            // Display the translation for each input text provided
            response.translationsList.forEach { translation ->
                System.out.printf("Translated text: %s\n", translation.translatedText)
            }
            return line
        }
    }
}


fun <A, B> List<A>.pmap(func: suspend (A) -> B): List<B> = runBlocking {
    map { async(IO) { func(it) } }.map { it.await() }
}

@Suppress("UNCHECKED_CAST")
fun TimedTextFile.add(line: TimedLine): TimedTextFile {
    val timedLines: MutableSet<TimedLine> = this.timedLines as MutableSet<TimedLine>
    timedLines.add(line)
    return this
}