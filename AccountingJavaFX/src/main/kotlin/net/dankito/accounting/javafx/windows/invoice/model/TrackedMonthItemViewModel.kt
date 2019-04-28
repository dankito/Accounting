package net.dankito.accounting.javafx.windows.invoice.model

import javafx.beans.property.SimpleStringProperty
import net.dankito.accounting.data.model.timetracker.TrackedMonth
import net.dankito.accounting.javafx.extensions.asLocalDate
import tornadofx.ItemViewModel
import java.time.format.DateTimeFormatter


class TrackedMonthItemViewModel : ItemViewModel<TrackedMonth>() {

    companion object {
        val MonthNameDateFormat = DateTimeFormatter.ofPattern("MMMM yyyy")
    }


    val month = bind { SimpleStringProperty(item?.month?.let { MonthNameDateFormat.format(it.asLocalDate()) }) }

    val trackedHours = bind { SimpleStringProperty(item?.decimalHoursString) }

}