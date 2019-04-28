package net.dankito.accounting.data.model.timetracker

import java.time.LocalDate


open class TimeEntry(trackedTimeInSeconds: Int,
                     val date: LocalDate,
                     val description: String = "",
                     val project: Project? = null,
                     val task: Task? = null)
    : TrackedTimeUnit(trackedTimeInSeconds) {

    init {
        project?.addEntry(this)
        task?.addEntry(this)
    }

    override fun toString(): String {
        return "$date: $trackedTimeString for '$description'"
    }

}