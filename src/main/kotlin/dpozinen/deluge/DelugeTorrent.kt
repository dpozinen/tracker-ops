package dpozinen.deluge

import dpozinen.deluge.mutations.By
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

data class DelugeTorrent(
    val id: String,
    val name: String,
    val state: String,
    val progress: String,
    val size: String,
    val downloaded: String,
    val ratio: String,
    val uploaded: String,
    val downloadSpeed: String,
    val eta: String,
    val uploadSpeed: String,
    val date: String
) {

    /**
     * @return getter for a field corresponding to the [By]
     * @param by getter field name
     */
    fun getterBy(by: By): KProperty1.Getter<DelugeTorrent, String> {
        return this::class.memberProperties
            .filter { it.name == by.property() }
            .map { it.getter }
            .filterIsInstance<KProperty1.Getter<DelugeTorrent, String>>()
            .first()
    }
}

