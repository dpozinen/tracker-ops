package dpozinen.deluge.db

import dpozinen.deluge.db.entities.DataPointEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DataPointRepo : JpaRepository<DataPointEntity, Long>