package dpozinen.deluge.db

import dpozinen.deluge.db.entities.DataPointEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface DataPointRepo : JpaRepository<DataPointEntity, Long> {

    @Query(value = """
            select distinct on (torrent_id)
                    id, torrent_id, time, up_speed, down_speed, uploaded, downloaded
            from data_point_entity
            order by time desc
    """, nativeQuery = true)
    fun findTopOrderByTimeDescPerTorrent(): List<DataPointEntity>

}
