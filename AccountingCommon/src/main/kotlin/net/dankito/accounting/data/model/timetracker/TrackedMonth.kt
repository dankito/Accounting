package net.dankito.accounting.data.model.timetracker

import java.time.LocalDate


class TrackedMonth(val month: LocalDate,
                   val days: List<TrackedDay>,
                   trackedTimeInSeconds: Int = days.sumBy { it.trackedTimeInSeconds } )
    : TrackedTimeUnit(trackedTimeInSeconds) {


    val firstTrackedDay: LocalDate?
        get() = days.sortedBy { it.date }.firstOrNull()?.date

    val lastTrackedDay: LocalDate?
        get() = days.sortedByDescending { it.date }.firstOrNull()?.date


    val firstDayOfTrackedMonth: LocalDate
        get() = month.withDayOfMonth(1)

    val lastDayOfTrackedMonth: LocalDate
        get() = month.withDayOfMonth(month.lengthOfMonth())


    override fun toString(): String {
        return "${month.month} / ${month.year}: $trackedTimeString"
    }

}