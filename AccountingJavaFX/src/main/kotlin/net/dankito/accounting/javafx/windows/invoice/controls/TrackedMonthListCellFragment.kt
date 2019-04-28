package net.dankito.accounting.javafx.windows.invoice.controls

import net.dankito.accounting.data.model.timetracker.TrackedMonth
import net.dankito.accounting.javafx.windows.invoice.model.TrackedMonthItemViewModel
import tornadofx.*


class TrackedMonthListCellFragment : ListCellFragment<TrackedMonth>() {

    val trackedMonth = TrackedMonthItemViewModel().bindTo(this)


    override val root = borderpane {
        prefHeight = 32.0
        useMaxWidth = true

        left {
            label(trackedMonth.month) {
                useMaxHeight = true
            }
        }

        right {
            hbox {
                useMaxHeight = true

                label(trackedMonth.trackedHours) {
                    useMaxHeight = true
                }

                label(messages["create.invoice.window.time.tracker.hours.label"]) {
                    useMaxHeight = true

                    hboxConstraints {
                        marginLeft = 2.0
                        marginRight = 18.0
                    }
                }

                button(messages["create.invoice.window.time.tracker.use.month"]) {
                    useMaxHeight = true
                    prefWidth = 125.0

                    action {
                        this@TrackedMonthListCellFragment.cell?.apply {
                            listViewProperty().value.selectionModel.select(trackedMonth.item)
                        }

                        // TODO: call CreateInvoiceWindow
                    }
                }
            }
        }

    }

}