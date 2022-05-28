package dpozinen.deluge.db.entities

import org.hibernate.annotations.CreationTimestamp
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import javax.persistence.*
import javax.persistence.EnumType.STRING

@Entity
@EntityListeners(AuditingEntityListener::class)
class DataPointEntity(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long? = null,

    @Column(nullable = false) val torrentId: String,

    @Column(nullable = false) @Enumerated(STRING) val graph: Graph,

    @CreatedDate var time: LocalDateTime? = null,

    @Column(nullable = false) val data: Long
) {

    enum class Graph {
        UP_SPEED,
        DOWN_SPEED,
        UPLOADED,
        DOWNLOADED
    }
}