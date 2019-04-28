package net.dankito.accounting.data.model.timetracker

import java.time.LocalDate
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.OneToMany


@Entity
class TrackedMonth(

    @Column
    val month: LocalDate,

    @OneToMany(cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ])
    val days: List<TrackedDay>,

    trackedTimeInSeconds: Int = days.sumBy { it.trackedTimeInSeconds }

) : TrackedTimeUnit(trackedTimeInSeconds) {


    internal constructor() : this(LocalDate.now(), listOf()) // for object deserializers


    val firstTrackedDay: LocalDate?
        get() = days.sortedBy { it.date }.firstOrNull()?.date

    val lastTrackedDay: LocalDate?
        get() = days.sortedByDescending { it.date }.firstOrNull()?.date


    val firstDayOfTrackedMonth: LocalDate
        get() = month.withDayOfMonth(1)

    val lastDayOfTrackedMonth: LocalDate
        get() = month.withDayOfMonth(month.lengthOfMonth())


    override fun toString(): String {
        return "${month.month} / ${month.year}: $trackedTimeString"
    }

}