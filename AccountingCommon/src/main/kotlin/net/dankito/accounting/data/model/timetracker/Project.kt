package net.dankito.accounting.data.model.timetracker


open class Project(val name: String, timeEntries: MutableList<TimeEntry> = ArrayList())
    : TimeEntriesContainer(timeEntries) {

    override fun toString(): String {
        return name
    }

}