package net.dankito.accounting.data.model.timetracker

import net.dankito.accounting.data.model.BaseEntity
import javax.persistence.Column
import javax.persistence.Entity


/**
 * The reason for introducing TimeTrackerDate was as
 *   a) CouchbaseLite JPA is not capable of persisting LocalDate (would need JPA 2.2)
 *   b) to keep AccountingCommon compatible with Android
 */
@Entity
open class TimeTrackerDate(

    @Column
    val year: Int,

    @Column
    val month: Int,

    @Column
    val day: Int

) : BaseEntity(), Comparable<TimeTrackerDate> {

    internal constructor() : this(0, 1, 1) // for object deserializers


    open fun atFirstDayOfMonth(): TimeTrackerDate {
        return TimeTrackerDate(this.year, this.month, 1)
    }

    open fun atLastDayOfMonth(): TimeTrackerDate {
        return TimeTrackerDate(this.year, this.month, calculateLastDayOfMonth(this))
    }

    protected open fun calculateLastDayOfMonth(date: TimeTrackerDate): Int {
        when (date.month) {
            1, 3, 5, 7, 8, 10, 12 -> return 31
            4, 6, 9, 11 -> return 30
            2 -> return calculateLastDayOfFebruary(date)
        }

        return -1 // never comes to this
    }

    protected open fun calculateLastDayOfFebruary(date: TimeTrackerDate): Int {
        if (date.year % 400 == 0) {
            return 29
        }
        else if (date.year % 100 == 0) {
            return 28
        }
        else if (date.year % 4 == 0) {
            return 29
        }

        return 28
    }


    override fun compareTo(other: TimeTrackerDate): Int {
        val yearCompare = this.year.compareTo(other.year)
        if (yearCompare != 0) {
            return yearCompare
        }

        val monthCompare = this.month.compareTo(other.month)
        if (monthCompare != 0) {
            return monthCompare
        }

        val dayCompare = this.day.compareTo(other.day)
        if (dayCompare != 0) {
            return dayCompare
        }

        return 0
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TimeTrackerDate) return false

        if (day != other.day) return false
        if (month != other.month) return false
        if (year != other.year) return false

        return true
    }

    override fun hashCode(): Int {
        var result = day
        result = 31 * result + month
        result = 31 * result + year
        return result
    }


    override fun toString(): String {
        return String.format("%02d.%02d.%04d", day, month, year)
    }

}