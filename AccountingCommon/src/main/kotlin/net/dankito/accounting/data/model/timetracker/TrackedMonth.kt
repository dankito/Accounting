package net.dankito.accounting.data.model.timetracker

import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.OneToMany
import javax.persistence.OneToOne


@Entity
class TrackedMonth(

    @OneToOne(cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ])
    val month: TimeTrackerDate,

    @OneToMany(cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ])
    val days: List<TrackedDay>,

    trackedTimeInSeconds: Int = days.sumBy { it.trackedTimeInSeconds }

) : TrackedTimeUnit(trackedTimeInSeconds) {


    internal constructor() : this(TimeTrackerDate(), listOf()) // for object deserializers


    val firstTrackedDay: TimeTrackerDate?
        get() = days.sortedBy { it.date }.firstOrNull()?.date

    val lastTrackedDay: TimeTrackerDate?
        get() = days.sortedByDescending { it.date }.firstOrNull()?.date


    val firstDayOfTrackedMonth: TimeTrackerDate
        get() = month.atFirstDayOfMonth()

    val lastDayOfTrackedMonth: TimeTrackerDate
        get() = month.atLastDayOfMonth()


    override fun toString(): String {
        return "${month.month} / ${month.year}: $trackedTimeString"
    }

}