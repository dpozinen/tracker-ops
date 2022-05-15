package dpozinen.deluge

import kotlin.reflect.full.memberProperties

data class DelugeTorrent(
    val id: String,
    val name: String,
    val state: String,
    val progress: Short,
    val size: String,
    val downloaded: String,
    val ratio: String,
    val uploaded: String,
    val downloadSpeed: String,
    val eta: String,
    val uploadSpeed: String,
    val date: String
) {
    fun value(by: Mutation.By): Comparable<Any> {
        return this::class.memberProperties
            .filter { it.name == by.property() }
            .map { it.getter.call(this) }
            .first()!! as Comparable<Any>
    }
}

