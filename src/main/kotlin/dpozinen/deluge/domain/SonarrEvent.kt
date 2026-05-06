package dpozinen.deluge.domain

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME
import dpozinen.deluge.domain.SonarrEventType.Download
import dpozinen.deluge.domain.SonarrEventType.Grab
import dpozinen.deluge.domain.SonarrEventType.Test


@JsonTypeInfo(use = NAME, include = As.EXISTING_PROPERTY, property = "eventType")
@JsonSubTypes(
    Type(value = GrabSonarrEvent::class, name = "Grab"),
    Type(value = DownloadSonarrEvent::class, name = "Download"),
    Type(value = TestSonarrEvent::class, name = "Test"),
)
interface SonarrEvent {
    fun eventType(): SonarrEventType
}

data class GrabSonarrEvent(
    @param:JsonProperty("series")
    val series: Series
) : SonarrEvent {
    override fun eventType() = Grab
}

data class DownloadSonarrEvent(
    @param:JsonProperty("series")
    val series: Series,
    @param:JsonProperty("episodeFile")
    val episodeFile: EpisodeFile
) : SonarrEvent {
    override fun eventType() = Download
}

data class Series(
    @param:JsonProperty("path")
    val path: String
)

data class EpisodeFile(
    @param:JsonProperty("relativePath")
    val relativePath: String,
    @param:JsonProperty("path")
    val path: String
)

class TestSonarrEvent : SonarrEvent {
    override fun eventType() = Test
}

enum class SonarrEventType {
    Grab, Download, Test
}