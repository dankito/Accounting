package net.dankito.accounting.data.model.timetracker

import net.dankito.accounting.data.model.BaseEntity
import java.util.*
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.OneToMany


@Entity
open class TrackedTimes(

    @OneToMany(cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ], orphanRemoval = true)
    val entries: List<TimeEntry>,

    @OneToMany(cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ], orphanRemoval = true)
    val days: List<TrackedDay>,

    @OneToMany(cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ], orphanRemoval = true)
    val months: List<TrackedMonth>,

    @OneToMany(cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ], orphanRemoval = true)
    val projects: List<Project>,

    @OneToMany(cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ], orphanRemoval = true)
    val task: List<Task>,

    @Column
    val retrieved: Date = Date()

) : BaseEntity() {

    internal constructor() : this(listOf(), listOf(), listOf(), listOf(), listOf()) // for object deserializers

}