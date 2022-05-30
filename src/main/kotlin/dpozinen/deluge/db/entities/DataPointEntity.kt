package dpozinen.deluge.db.entities

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import javax.persistence.*

// 70 + 8 * 5
@Entity
@EntityListeners(AuditingEntityListener::class)
class DataPointEntity(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long? = null,

    @Column(nullable = false, length = 70)
    val torrentId: String,

    @CreatedDate @Column(columnDefinition = "DATETIME")
    var time: LocalDateTime? = null,

    @Column(nullable = false)
    val upSpeed: Long,

    @Column(nullable = false)
    val downSpeed: Long,

    @Column(nullable = false)
    val uploaded: Long,

    @Column(nullable = false)
    val downloaded: Long,
) {

    override fun toString(): String {
        return "DataPointEntity(id=$id, torrentId='$torrentId', time=$time, upSpeed=$upSpeed, downSpeed=$downSpeed, uploaded=$uploaded, downloaded=$downloaded)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataPointEntity

        if (id != other.id) return false
        if (torrentId != other.torrentId) return false
        if (time != other.time) return false

        return isEqual(other)
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + torrentId.hashCode()
        result = 31 * result + (time?.hashCode() ?: 0)
        result = 31 * result + upSpeed.hashCode()
        result = 31 * result + downSpeed.hashCode()
        result = 31 * result + uploaded.hashCode()
        result = 31 * result + downloaded.hashCode()
        return result
    }

    fun isEqual(other: DataPointEntity): Boolean {
        if (upSpeed != other.upSpeed) return false
        if (downSpeed != other.downSpeed) return false
        if (uploaded != other.uploaded) return false
        if (downloaded != other.downloaded) return false
        return true
    }

}