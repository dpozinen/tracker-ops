package dpozinen.deluge.domain

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonSubTypes.Type
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME
import dpozinen.deluge.domain.SonarrEventType.Download
import dpozinen.deluge.domain.SonarrEventType.Grab


@JsonTypeInfo(use = NAME, include = As.EXISTING_PROPERTY, property = "eventType")
@JsonSubTypes(
    Type(value = GrabSonarrEvent::class, name = "Grab"),
    Type(value = DownloadSonarrEvent::class, name = "Download"),
)
interface SonarrEvent {
    fun eventType(): SonarrEventType
}

data class GrabSonarrEvent(
    @JsonProperty("series")
    val series: Series
) : SonarrEvent {
    override fun eventType() = Grab
}

data class DownloadSonarrEvent(
    @JsonProperty("series")
    val series: Series,
    @JsonProperty("episodeFile")
    val episodeFile: EpisodeFile
) : SonarrEvent {
    override fun eventType() = Download
}

data class Series(
    @JsonProperty("path")
    val path: String
)

data class EpisodeFile(
    @JsonProperty("relativePath")
    val relativePath: String,
    @JsonProperty("path")
    val path: String
)

enum class SonarrEventType {
    Grab, Download
}