package dpozinen.deluge

import dpozinen.deluge.mutations.Filter
import org.springframework.stereotype.Component

@Component
class Validator {

    fun validate(dto: Filter.Dto): Pair<Boolean, Filter?> {

        val hasOpposingOperators = dto.operators.any { dto.operators.contains(it.opposite()) }

        if (hasOpposingOperators) return false to null

        return true to Filter(dto.by, dto.value, dto.operators)
    }

}
