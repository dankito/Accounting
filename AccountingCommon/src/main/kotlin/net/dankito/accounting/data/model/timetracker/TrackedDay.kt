package net.dankito.accounting.data.model.timetracker

import java.time.LocalDate


open class TrackedDay(val date: LocalDate,
                      val entries: List<TimeEntry>,
                      trackedTimeInSeconds: Int = entries.sumBy { it.trackedTimeInSeconds } )
    : TrackedTimeUnit(trackedTimeInSeconds) {

    override fun toString(): String {
        return "$date: $trackedTimeString"
    }

}