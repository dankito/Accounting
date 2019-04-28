package net.dankito.accounting.data.model.timetracker

import net.dankito.accounting.data.model.BaseEntity
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.OneToOne


@Entity
class TimeTrackerAccount(

    @Column(name = TypeColumnName)
    var type: TimeTrackerType,

    @Column(name = AccountNameColumnName)
    var accountName: String,

    @Column(name = UsernameColumnName)
    var username: String,

    @Column(name = PasswordColumnName)
    var password: String,

    @OneToOne(cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ] )
    var trackedTimes: TrackedTimes? = null

): BaseEntity() {

    companion object {

        const val TypeColumnName = "type"

        const val AccountNameColumnName = "account_name"

        const val UsernameColumnName = "username"

        const val PasswordColumnName = "password"

    }


    constructor() : this(TimeTrackerType.Harvest, "", "", "") // for object deserializers


    override fun toString(): String {
        return accountName
    }

}