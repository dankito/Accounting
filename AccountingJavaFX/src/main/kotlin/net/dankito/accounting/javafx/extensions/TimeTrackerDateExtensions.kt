package net.dankito.accounting.javafx.extensions

import net.dankito.accounting.data.model.timetracker.TimeTrackerDate
import java.time.LocalDate


fun TimeTrackerDate.asLocalDate(): LocalDate {
    return LocalDate.of(this.year, this.month, this.day)
}