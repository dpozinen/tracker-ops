package dpozinen.deluge.db.entities

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import javax.persistence.*

// 70 + 8 + 8 + 8 = 94 bytes
@Entity
@EntityListeners(AuditingEntityListener::class)
class DataPointEntity(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    val id: Long? = null,

    @Column(nullable = false, length = 70)
    val torrentId: String,

    @Column(nullable = false)
    val graph: Graph,

    @CreatedDate @Column(columnDefinition = "SMALLDATETIME")
    var time: LocalDateTime? = null,

    @Column(nullable = false)
    val data: Long
) {

    enum class Graph {
        UP_SPEED,
        DOWN_SPEED,
        UPLOADED,
        DOWNLOADED
    }
}