package net.dankito.accounting.data.model.timetracker

import net.dankito.accounting.data.model.BaseEntity
import javax.persistence.CascadeType
import javax.persistence.MappedSuperclass
import javax.persistence.OneToMany


@MappedSuperclass
abstract class TimeEntriesContainer(

    @OneToMany(cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ], orphanRemoval = true)
    protected val timeEntries: MutableList<TimeEntry> = mutableListOf()

) : BaseEntity() {

    internal constructor() : this(mutableListOf()) // for object deserializers


    val trackedTimeEntries: List<TimeEntry>
        get() = timeEntries.toList()

    @OneToMany
    var trackedDays: List<TrackedDay> = listOf()

    @OneToMany
    var trackedMonths: List<TrackedMonth> = listOf()


    open fun addEntry(entry: TimeEntry) {
        timeEntries.add(entry)
    }

}