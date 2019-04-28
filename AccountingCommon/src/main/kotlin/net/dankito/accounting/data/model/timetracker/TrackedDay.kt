package net.dankito.accounting.data.model.timetracker

import java.time.LocalDate
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.OneToMany


@Entity
open class TrackedDay(

    @Column
    val date: LocalDate,

    @OneToMany(cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ])
    val entries: List<TimeEntry>,

    trackedTimeInSeconds: Int = entries.sumBy { it.trackedTimeInSeconds }

) : TrackedTimeUnit(trackedTimeInSeconds) {


    internal constructor() : this(LocalDate.now(), listOf()) // for object deserializers


    override fun toString(): String {
        return "$date: $trackedTimeString"
    }

}