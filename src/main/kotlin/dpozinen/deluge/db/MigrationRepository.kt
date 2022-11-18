package dpozinen.deluge.db

import dpozinen.deluge.domain.DataPoint
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.sql.ResultSet
import java.time.*
import java.time.format.DateTimeFormatter

@Component
class MigrationRepository(private val jdbcTemplate: NamedParameterJdbcTemplate) {

    private val query = """
        select * from deluge_torrent_entity as torrent
        inner join data_point_entity as dp on dp.torrent_id = torrent.id
        order by torrent.name
        limit 500
        offset :offset
    """.trimIndent()

    fun findAll(offset: Int): List<DataPoint> {
        return jdbcTemplate.query(
            query,
            mapOf("offset" to offset)
        ) { it, _ ->
            DataPoint(
                torrentId = it.getString("torrent_id"),
                name = it.getString("name"),
                size = it.getLong("size"),
                dateAdded = parseDate(it),
                upSpeed = it.getLong("up_speed"),
                downSpeed = it.getLong("down_speed"),
                uploaded = it.getLong("uploaded"),
                downloaded = it.getLong("downloaded"),
                timestamp = it.getTimestamp("time").toInstant()
            )
        }
    }

    private fun parseDate(it: ResultSet) = Instant.ofEpochSecond(
        LocalDate.parse(it.getString("date_added"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                             .toEpochSecond(LocalTime.NOON, ZoneOffset.UTC)
        )


}