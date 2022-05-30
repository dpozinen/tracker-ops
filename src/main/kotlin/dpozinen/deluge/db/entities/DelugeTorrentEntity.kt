package dpozinen.deluge.db.entities

import java.time.LocalDate
import javax.persistence.Entity
import javax.persistence.Id

@Entity
class DelugeTorrentEntity(
    @Id val id: String,
    val name: String,
    val size: Long,
    val dateAdded: LocalDate,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DelugeTorrentEntity

        if (id != other.id) return false
        if (name != other.name) return false
        if (size != other.size) return false
        if (dateAdded != other.dateAdded) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + dateAdded.hashCode()
        return result
    }
}
