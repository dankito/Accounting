package net.dankito.accounting.javafx.windows.mainwindow.controls

import javafx.scene.layout.Priority
import net.dankito.accounting.data.model.AccountingPeriod
import net.dankito.accounting.javafx.windows.mainwindow.OverviewPresenter
import tornadofx.*


class OverviewTab(presenter: OverviewPresenter) : View() {

    companion object {
        private const val DocumentsOverviewSpace = 12.0
    }


    override val root = hbox {

        vbox {
            hboxConstraints {
                hGrow = Priority.ALWAYS
            }

            add(RevenuesOverview(presenter))

            add(ExpendituresOverview(presenter).apply {
                root.vboxConstraints {
                    marginTop = DocumentsOverviewSpace
                }
            })
        }


        vbox {
            minWidth = 200.0

            paddingAll = 2.0
            paddingLeft = 4.0

            label(messages["main.window.tab.overview.summary.pane.accounting.period.label"])

            anchorpane {
                useMaxWidth = true

                combobox(null, AccountingPeriod.values().asList()) {
                    value = presenter.accountingPeriod

                    cellFormat {
                        text = if (it == AccountingPeriod.Quarterly) messages["main.window.tab.overview.summary.pane.accounting.period.quarterly"]
                                else messages["main.window.tab.overview.summary.pane.accounting.period.monthly"]
                    }

                    valueProperty().addListener { _, _, newValue -> presenter.accountingPeriod = newValue }

                    anchorpaneConstraints {
                        rightAnchor = 0.0
                    }
                }

                vboxConstraints {
                    marginTop = 4.0
                    marginBottom = 6.0
                }
            }
        }
    }

}