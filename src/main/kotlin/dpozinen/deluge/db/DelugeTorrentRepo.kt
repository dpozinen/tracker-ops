package dpozinen.deluge.db

import dpozinen.deluge.db.entities.DelugeTorrentEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DelugeTorrentRepo : JpaRepository<DelugeTorrentEntity, String>