package net.dankito.accounting.data.model.timetracker

import javax.persistence.*


@Entity
open class TimeEntry(

    trackedTimeInSeconds: Int,

    @OneToOne(cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ])
    val date: TimeTrackerDate,

    @Column
    val description: String = "",


    @ManyToOne(cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ])
    @JoinColumn
    val project: Project? = null,

    @ManyToOne(cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ])
    @JoinColumn
    val task: Task? = null

) : TrackedTimeUnit(trackedTimeInSeconds) {


    internal constructor() : this(0, TimeTrackerDate()) // for object deserializers


    override fun toString(): String {
        return "$date: $trackedTimeString for '$description'"
    }

}