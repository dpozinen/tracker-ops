package dpozinen.deluge.db

import dpozinen.deluge.db.entities.DataPointEntity
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
@Profile("stats")
interface DataPointRepo : JpaRepository<DataPointEntity, Long> {

    @Query(
        value = """
            select distinct on (torrent_id)
                    id, torrent_id, time, up_speed, down_speed, uploaded, downloaded
            from data_point_entity
            order by time desc
    """, nativeQuery = true
    )
    fun findTopOrderByTimeDescPerTorrent(): List<DataPointEntity> // todo asc/desc?

    fun findAllByTorrentIdInAndTimeGreaterThanEqualAndTimeLessThanEqualOrderByTime(
        torrentIds: Collection<String>,
        from: LocalDateTime,
        to: LocalDateTime
    ): List<DataPointEntity>

    object Extensions {

        fun DataPointRepo.findByTorrentsInTimeFrame(
            torrentIds: Collection<String>,
            from: LocalDateTime,
            to: LocalDateTime
        ): List<DataPointEntity>
        = findAllByTorrentIdInAndTimeGreaterThanEqualAndTimeLessThanEqualOrderByTime(torrentIds, from, to)

    }
}
