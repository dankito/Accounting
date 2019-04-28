package net.dankito.accounting.data.model.timetracker

import net.dankito.accounting.service.timetracker.TimeEntriesGrouper


open class TimeEntriesContainer(private val timeEntries: MutableList<TimeEntry> = ArrayList()) {

    private val grouper = TimeEntriesGrouper() // TODO: move to service

    private var calculatedTrackedDays: List<TrackedDay>? = null

    private var calculatedTrackedMonths: List<TrackedMonth>? = null

    private val calculatedValuesLock = Any()


    val trackedTimeEntries: List<TimeEntry>
        get() = timeEntries.toList()

    val trackedDays: List<TrackedDay>
        get() = getOrCalculateTrackedDays()

    val trackedMonths: List<TrackedMonth>
        get() = getOrCalculateTrackedMonths()


    open fun addEntry(entry: TimeEntry) {
        synchronized(calculatedValuesLock) {
            timeEntries.add(entry)

            calculatedTrackedDays = null
            calculatedTrackedMonths = null
        }
    }

    protected open fun getOrCalculateTrackedDays(): List<TrackedDay> {
        synchronized(calculatedValuesLock) {
            calculatedTrackedDays?.let { return it }

            val days = grouper.groupByDays(timeEntries)
            this.calculatedTrackedDays = days
            return days
        }
    }

    protected open fun getOrCalculateTrackedMonths(): List<TrackedMonth> {
        synchronized(calculatedValuesLock) {
            calculatedTrackedMonths?.let { return it }

            val months = grouper.groupByMonths(trackedDays)
            this.calculatedTrackedMonths = months
            return months
        }
    }

}