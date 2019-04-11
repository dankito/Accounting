package net.dankito.accounting.javafx.windows.mainwindow.controls

import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import net.dankito.accounting.data.model.AccountingPeriod
import net.dankito.accounting.javafx.windows.mainwindow.OverviewPresenter
import tornadofx.*


class SummaryPane(private val presenter: OverviewPresenter) : View() {

    companion object {
        private const val SummaryAmountLabelWidth = 80.0
    }


    private val previousPeriodLabel = SimpleStringProperty()

    private val previousPeriodReceivedVat = SimpleStringProperty()

    private val previousPeriodSpentVat = SimpleStringProperty()

    private val previousPeriodVatBalance = SimpleStringProperty()


    private val currentPeriodLabel = SimpleStringProperty()

    private val currentPeriodReceivedVat = SimpleStringProperty()

    private val currentPeriodSpentVat = SimpleStringProperty()

    private val currentPeriodVatBalance = SimpleStringProperty()


    init {
        updateVatValues()

        presenter.addDocumentsUpdatedListenerInAMemoryLeakWay {
            runLater { updateVatValues() }
        }
    }


    override val root = vbox {
        minWidth = 200.0

        paddingAll = 2.0
        paddingLeft = 4.0

        label(messages["main.window.tab.overview.summary.pane.accounting.period.label"])

        anchorpane {
            useMaxWidth = true

            combobox(null, AccountingPeriod.values().asList()) {
                value = presenter.accountingPeriod

                cellFormat {
                    text = if (it == AccountingPeriod.Quarterly) messages["accounting.period.quarterly"]
                    else messages["accounting.period.monthly"]
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


        vbox {
            this.border = Border(BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii(4.0), BorderWidths(2.0)))

            paddingAll = 4.0

            label(messages["value.added.tax"])

            periodSummary(previousPeriodLabel,
                previousPeriodReceivedVat, "main.window.tab.overview.summary.pane.received.vat",
                previousPeriodSpentVat, "main.window.tab.overview.summary.pane.spent.vat",
                previousPeriodVatBalance, "main.window.tab.overview.summary.pane.vat.balance"
            )

            periodSummary(currentPeriodLabel,
                currentPeriodReceivedVat, "main.window.tab.overview.summary.pane.received.vat",
                currentPeriodSpentVat, "main.window.tab.overview.summary.pane.spent.vat",
                currentPeriodVatBalance, "main.window.tab.overview.summary.pane.vat.balance"
            )

        }
    }

    private fun EventTarget.periodSummary(periodLabel: SimpleStringProperty,
                                          receivedAmountString: SimpleStringProperty, receivedLabelResourceKey: String,
                                          spentAmountString: SimpleStringProperty, spentLabelResourceKey: String,
                                          balanceAmountString: SimpleStringProperty, balanceLabelResourceKey: String): Pane {

        return vbox {
            label(periodLabel) {

                vboxConstraints {
                    marginTop = 8.0
                    marginLeft = 6.0
                }
            }

            amountWithLabel(receivedAmountString, receivedLabelResourceKey)

            amountWithLabel(spentAmountString, spentLabelResourceKey)

            amountWithLabel(balanceAmountString, balanceLabelResourceKey)
        }
    }

    private fun EventTarget.amountWithLabel(amountString: SimpleStringProperty, labelResourceKey: String = ""): Pane {
        return anchorpane {

            vboxConstraints {
                marginTop = 2.0
            }

            label(messages[labelResourceKey]) {
                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = SummaryAmountLabelWidth
                    bottomAnchor = 0.0
                }
            }

            label(amountString) {
                textAlignment = TextAlignment.RIGHT

                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = 0.0
                    bottomAnchor = 0.0
                }
            }

        }
    }




    private fun updateVatValues() {
        previousPeriodLabel.value = if (presenter.accountingPeriod == AccountingPeriod.Quarterly)
                                    messages["main.window.tab.overview.summary.pane.previous.quarter"]
                                    else messages["main.window.tab.overview.summary.pane.previous.month"]
        previousPeriodReceivedVat.value = presenter.getCurrencyString(presenter.calculatePreviousAccountingPeriodReceivedVat())
        previousPeriodSpentVat.value = presenter.getCurrencyString(presenter.calculatePreviousAccountingPeriodSpentVat())
        previousPeriodVatBalance.value = presenter.getCurrencyString(presenter.calculatePreviousAccountingPeriodVatBalance())

        currentPeriodLabel.value = if (presenter.accountingPeriod == AccountingPeriod.Quarterly)
            messages["main.window.tab.overview.summary.pane.current.quarter"]
        else messages["main.window.tab.overview.summary.pane.current.month"]
        currentPeriodReceivedVat.value = presenter.getCurrencyString(presenter.calculateCurrentAccountingPeriodReceivedVat())
        currentPeriodSpentVat.value = presenter.getCurrencyString(presenter.calculateCurrentAccountingPeriodSpentVat())
        currentPeriodVatBalance.value = presenter.getCurrencyString(presenter.calculateCurrentAccountingPeriodVatBalance())
    }

}