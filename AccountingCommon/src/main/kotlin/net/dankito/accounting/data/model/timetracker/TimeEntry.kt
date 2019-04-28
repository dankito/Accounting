package net.dankito.accounting.data.model.timetracker

import java.time.LocalDate
import javax.persistence.*


@Entity
open class TimeEntry(

    trackedTimeInSeconds: Int,

    @Column
    val date: LocalDate,

    @Column
    val description: String = "",


    @ManyToOne(cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ])
    @JoinColumn
    val project: Project? = null,

    @ManyToOne(cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ])
    @JoinColumn
    val task: Task? = null

) : TrackedTimeUnit(trackedTimeInSeconds) {


    internal constructor() : this(0, LocalDate.now()) // for object deserializers


    override fun toString(): String {
        return "$date: $trackedTimeString for '$description'"
    }

}