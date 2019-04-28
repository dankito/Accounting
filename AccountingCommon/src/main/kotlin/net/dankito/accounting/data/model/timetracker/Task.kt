package net.dankito.accounting.data.model.timetracker

import javax.persistence.Column
import javax.persistence.Entity


@Entity
open class Task(

    @Column
    val name: String,

    timeEntries: MutableList<TimeEntry> = ArrayList()

) : TimeEntriesContainer(timeEntries) {


    internal constructor() : this("") // for object deserializers


    override fun toString(): String {
        return name
    }

}