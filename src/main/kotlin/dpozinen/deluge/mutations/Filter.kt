package dpozinen.deluge.mutations

import dpozinen.deluge.core.DelugeState
import dpozinen.deluge.mutations.By.ByPredicate
import dpozinen.deluge.mutations.By.DATE
import dpozinen.deluge.mutations.By.NAME
import dpozinen.deluge.mutations.By.STATE
import mu.KotlinLogging.logger
import java.time.LocalDate

class Filter(
    private val by: By,
    val value: Comparable<*>,
    private val operators: List<Operator> = listOf(Operator.IS)
) : Mutation {

    private val log = logger { }

    override fun perform(state: DelugeState): DelugeState {
        if (invalid()) return state

        val mutations = addSelf(state)

        return runCatching {
            state.torrents.filter { predicate().test(it) }
        }.onFailure {
            log.warn { "Failed to apply '$this' due to $it. Previous state preserved." }
        }.getOrDefault(state.torrents)
            .let { state.with(it, mutations) }
    }

    private fun predicate() = when (by) {
        NAME, STATE -> typedPredicate<String>()
        DATE -> typedPredicate<Long>()
        else -> typedPredicate<Double>()
    }

    private inline fun <reified C : Comparable<C>> typedPredicate(): ByPredicate {
        return operators.map { predicate<C>(it) }.reduce { a, b -> a.or(b) }
    }

    private inline fun <reified C : Comparable<C>> predicate(operator: Operator) =
        when (operator) {
            Operator.IS -> predicateBy<C> { eq(it) }
            Operator.IS_NOT -> predicateBy<C> { !eq(it) }
            Operator.CONTAINS -> predicateBy<C> { contains(it) }
            Operator.NOT_CONTAINS -> predicateBy<C> { !contains(it) }
            Operator.GREATER -> predicateBy<C> { greater(it) }
            Operator.LESS -> predicateBy<C> { less(it) }
        }

    private fun <C : Comparable<C>> predicateBy(predicate: (C) -> Boolean) =
        ByPredicate { predicate(it.getterBy<C>(by).call(it)) }

    private fun eq(any: Any?) = any == value

    private fun contains(string: Any) = (string as String).contains(value as String)

    private fun greater(any: Any) =
        when (any) {
            is Double -> any > (value as Double)
            is Int -> any > (value as Int)
            is Short -> any > (value as Short)
            is Long -> any > (value as Long)
            is LocalDate -> any.isAfter(value as LocalDate)
            else -> (any as Number).toLong() > (value as Number).toLong()
        }

    private fun less(any: Any) =
        when (any) {
            is Double -> any < (value as Double)
            is Int -> any < (value as Int)
            is Short -> any < (value as Short)
            is Long -> any < (value as Long)
            is LocalDate -> any.isBefore(value as LocalDate)
            else -> (any as Number).toLong() < (value as Number).toLong()
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

        return by == other.by
    }

    override fun hashCode() = by.hashCode()

    override fun toString() =
        "Filter where $by ${operators.joinToString(separator = " or ") { it.name }} $value"

    private fun invalid() = operators.any { operators.contains(it.opposite()) }
}