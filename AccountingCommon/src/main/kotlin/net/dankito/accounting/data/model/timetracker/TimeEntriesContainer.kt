package net.dankito.accounting.data.model.timetracker


open class TimeEntriesContainer(protected val timeEntries: MutableList<TimeEntry> = ArrayList()) {


    val trackedTimeEntries: List<TimeEntry>
        get() = timeEntries.toList()

    var trackedDays: List<TrackedDay> = listOf()

    var trackedMonths: List<TrackedMonth> = listOf()


    open fun addEntry(entry: TimeEntry) {
        timeEntries.add(entry)
    }

}