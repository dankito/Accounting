package net.dankito.accounting.service.timetracker.harvest

import net.dankito.accounting.data.model.timetracker.TimeEntry
import net.dankito.accounting.data.model.timetracker.TrackedDay
import java.time.LocalDate


class TrackedHarvestDay(date: LocalDate, entries: List<TimeEntry>)
    : TrackedDay(date, entries) {

    /**
     * Displayed tracked time for a day in Harvest WebUI dependents on rounded minutes for each time entry
     */
    override val totalTrackedMinutesRounded: Int
        get() = entries.sumBy { it.totalTrackedMinutesRounded }

}