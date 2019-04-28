package net.dankito.accounting.service.timetracker

import net.dankito.accounting.data.model.timetracker.TimeEntry
import net.dankito.accounting.data.model.timetracker.TrackedDay
import net.dankito.accounting.data.model.timetracker.TrackedMonth
import net.dankito.accounting.service.timetracker.harvest.TrackedHarvestDay
import java.time.LocalDate


class TimeEntriesGrouper {

    fun groupByDays(entries: List<TimeEntry>): List<TrackedDay> {
        return entries
                .groupBy( { it.date }, { it } )
                .map { TrackedHarvestDay(it.key, it.value) }
    }

    fun groupByMonths(days: List<TrackedDay>): List<TrackedMonth> {
        return days
                .groupBy( { LocalDate.of(it.date.year, it.date.monthValue, 1) }, { it } )
                .map { TrackedMonth(it.key, it.value) }
    }

}