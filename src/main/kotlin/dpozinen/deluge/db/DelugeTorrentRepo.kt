package dpozinen.deluge.db

import dpozinen.deluge.db.entities.DelugeTorrentEntity
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
@Profile("stats")
interface DelugeTorrentRepo : JpaRepository<DelugeTorrentEntity, String> {

    @Query("select id from DelugeTorrentEntity")
    fun findAllIds(): List<String>
}