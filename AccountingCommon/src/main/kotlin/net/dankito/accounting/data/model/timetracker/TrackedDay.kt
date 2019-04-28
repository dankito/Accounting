package net.dankito.accounting.data.model.timetracker

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.OneToMany
import javax.persistence.OneToOne


@Entity
open class TrackedDay(

    @OneToOne(cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ])
    val date: TimeTrackerDate,

    @OneToMany(cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ])
    val entries: List<TimeEntry>,

    trackedTimeInSeconds: Int = entries.sumBy { it.trackedTimeInSeconds }

) : TrackedTimeUnit(trackedTimeInSeconds) {


    internal constructor() : this(TimeTrackerDate(), listOf()) // for object deserializers


    override fun toString(): String {
        return "$date: $trackedTimeString"
    }

}