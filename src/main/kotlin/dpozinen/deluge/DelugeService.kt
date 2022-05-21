package dpozinen.deluge

import dpozinen.deluge.mutations.Mutation

interface DelugeService {
    fun addMagnet(magnet: String)
    fun torrents(): List<DelugeTorrent>
    fun mutate(mutation: Mutation)
}