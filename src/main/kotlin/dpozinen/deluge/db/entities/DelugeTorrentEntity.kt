package dpozinen.deluge.db.entities

import java.time.Instant
import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class DelugeTorrentEntity(
    @Id val id: String,
    val name: String,
    val size: Long,
    val dateAdded: LocalDate,
)
