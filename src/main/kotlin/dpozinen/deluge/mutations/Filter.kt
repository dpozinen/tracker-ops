package dpozinen.deluge.mutations

import dpozinen.deluge.DelugeState
import dpozinen.deluge.mutations.By.*
import mu.KotlinLogging
import java.time.LocalDate

class Filter(
    private val by: By,
    val value: String,
    private val operators: List<Operator> = listOf(Operator.IS)
) : Mutation {

    private val log = KotlinLogging.logger {  }

    private lateinit var comparable: Comparable<*>

    override fun perform(state: DelugeState): DelugeState {
        val mutations = addSelf(state)

        val filteredTorrents = runCatching {
             state.torrents.filter { predicate().test(it) }
        }.onFailure {
            log.warn { "Failed to apply '$this' due to $it. Previous state preserved." }
        }.getOrDefault(state.torrents)

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
        this.comparable = comparable.comparable(value)
        return operators.map { predicate(it, comparable) }.reduce { a, b -> a.or(b) }
    }

    private inline fun <reified C : Comparable<C>> predicate(operator: Operator, comparable: ByComparable<C>) =
        when (operator) {
            Operator.IS -> predicateBy(comparable) { eq(it) }
            Operator.IS_NOT -> predicateBy(comparable) { !eq(it) }
            Operator.CONTAINS -> predicateBy(comparable) { contains(it) }
            Operator.NOT_CONTAINS -> predicateBy(comparable) { !contains(it) }
            Operator.GREATER -> predicateBy(comparable) { greater(it) }
            Operator.LESS -> predicateBy(comparable) { less(it) }
        }

    private fun <C : Comparable<C>> predicateBy(comparator: ByComparable<C>, predicate: (C) -> Boolean): ByPredicate  {
        return ByPredicate { predicate.invoke(comparator.comparable(it.getterBy(by).call(it))) }
    }

    private fun eq(any: Any?) = any == comparable

    private fun contains(string: Any) = (string as String).contains(comparable as String)

    private fun greater(any: Any) = when (any) {
        is Double -> any > (comparable as Double)
        is Int -> any > (comparable as Int)
        is Short -> any > (comparable as Short)
        is Long -> any > (comparable as Long)
        is LocalDate -> any.isAfter(comparable as LocalDate)
        else -> (any as Number).toLong() > (comparable as Number).toLong()
    }

    private fun less(any: Any) = when (any) {
        is Double -> any < (comparable as Double)
        is Int -> any < (comparable as Int)
        is Short -> any < (comparable as Short)
        is Long -> any < (comparable as Long)
        is LocalDate -> any.isBefore(comparable as LocalDate)
        else -> (any as Number).toLong() < (comparable as Number).toLong()
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

    override fun toString() =
        "Filter where $by ${operators.joinToString(separator = " or ") { it.name }} $value"

    data class Dto(val by: By, val value: String = "", val operators: List<Operator> = listOf(Operator.IS))

}