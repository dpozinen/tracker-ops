package dpozinen.deluge.mutations

import dpozinen.deluge.DelugeState
import dpozinen.deluge.mutations.By.*
import kotlin.reflect.full.isSubclassOf

class Filter(
    private val by: By,
    val value: String,
    private val operators: List<Operator> = listOf(Operator.IS)
) : Mutation {

    private lateinit var comparabale: Comparable<*>

    override fun perform(state: DelugeState): DelugeState {
        val mutations = addSelf(state)

        val filteredTorrents = state.torrents.filter { predicate().test(it) }

        return state.with(filteredTorrents, mutations)
    }

    private fun predicate(): ByPredicate {
        return when (this.by) {
            NAME -> predicate(By.name)
            STATE -> predicate(By.state)
            SIZE -> predicate(By.size)
            PROGRESS -> predicate(By.progress)
            DOWNLOADED -> predicate(By.downloaded)
            RATIO -> predicate(By.ratio)
            UPLOADED -> predicate(By.uploaded)
            ETA -> predicate(By.eta)
            DATE -> predicate(By.date)
            DOWNLOAD_SPEED -> predicate(By.downloadSpeed)
            UPLOAD_SPEED -> predicate(By.uploadSpeed)
        }
    }

    private inline fun <reified C : Comparable<C>> predicate(comparable: ByComparable<C>): ByPredicate {
        this.comparabale = comparable.comparable(value)
        return operators.map { choose(it, comparable) }.reduce { a, b -> a.or(b) }
    }

    private inline fun <reified C : Comparable<C>> choose(
        operator: Operator,
        comparable: ByComparable<C>
    ) = if (C::class == String::class)
        stringOperator(operator, comparable)
    else if (C::class.isSubclassOf(Number::class))
        numberOperator(operator, comparable)
    else throw IllegalArgumentException("todo")

    private inline fun <reified C : Comparable<C>> stringOperator(
        operator: Operator,
        comparable: ByComparable<C>
    ) = when (operator) {
        Operator.IS -> by.predicateBy(comparable) { eq(it) }
        Operator.IS_NOT -> by.predicateBy(comparable) { !eq(it) }
        Operator.CONTAINS -> by.predicateBy(comparable) { contains(it) }
        Operator.NOT_CONTAINS -> by.predicateBy(comparable) { !contains(it) }
        else -> throw IllegalArgumentException("")
    }

    private inline fun <reified C : Comparable<C>> numberOperator(
        operator: Operator,
        comparable: ByComparable<C>
    ) = when (operator) {
        Operator.IS -> by.predicateBy(comparable) { eq(it) }
        Operator.IS_NOT -> by.predicateBy(comparable) { !eq(it) }
        Operator.GREATER -> by.predicateBy(comparable) { greater(it) }
        Operator.LESS -> by.predicateBy(comparable) { less(it) }
        else -> throw IllegalArgumentException("")
    }

    private fun eq(any: Any?) = any == comparabale

    private fun contains(string: Any) = (string as String).contains(comparabale as String)

    private fun greater(number: Any) = when (number) {
        is Double -> number > (comparabale as Double)
        is Int -> number > (comparabale as Int)
        is Short -> number > (comparabale as Short)
        is Long -> number > (comparabale as Long)
        else -> (number as Number).toLong() > (comparabale as Number).toLong()
    }

    private fun less(number: Any) = when (number) {
        is Double -> number < (comparabale as Double)
        is Int -> number < (comparabale as Int)
        is Short -> number < (comparabale as Short)
        is Long -> number < (comparabale as Long)
        else -> (number as Number).toLong() < (comparabale as Number).toLong()
    }

    enum class Operator {
        GREATER { override fun opposite() = LESS },
        LESS { override fun opposite() = GREATER },
        IS { override fun opposite() = IS_NOT },
        IS_NOT { override fun opposite() = IS },
        CONTAINS { override fun opposite() = NOT_CONTAINS },
        NOT_CONTAINS { override fun opposite() = CONTAINS };

        abstract fun opposite(): Operator
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Filter

        if (by != other.by) return false

        return true
    }

    override fun hashCode(): Int {
        return by.hashCode()
    }

    override fun toString(): String {
        return "Filter where $by ${operators.joinToString(separator = " or ")} $value"
    }

    data class Dto(val by: By, val value: String, val operators: List<Operator> = listOf(Operator.IS))

}