package net.dankito.accounting.javafx.windows.mainwindow.controls

import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.scene.text.TextAlignment
import net.dankito.accounting.data.model.AccountingPeriod
import net.dankito.accounting.javafx.windows.mainwindow.OverviewPresenter
import net.dankito.utils.javafx.ui.extensions.setBorder
import tornadofx.*


class SummaryPane(private val presenter: OverviewPresenter) : View() {

    companion object {
        private const val SummaryAmountLabelWidth = 90.0
    }


    private val previousPeriodLabel = SimpleStringProperty()

    private val currentPeriodLabel = SimpleStringProperty()


    private val previousPeriodReceivedVat = SimpleStringProperty()

    private val previousPeriodSpentVat = SimpleStringProperty()

    private val previousPeriodVatBalance = SimpleStringProperty()


    private val currentPeriodReceivedVat = SimpleStringProperty()

    private val currentPeriodSpentVat = SimpleStringProperty()

    private val currentPeriodVatBalance = SimpleStringProperty()


    private val previousPeriodNetRevenues = SimpleStringProperty()

    private val previousPeriodExpenditures = SimpleStringProperty()

    private val previousPeriodBalance = SimpleStringProperty()


    private val currentPeriodNetRevenues = SimpleStringProperty()

    private val currentPeriodExpenditures = SimpleStringProperty()

    private val currentPeriodBalance = SimpleStringProperty()


    init {
        updateValues()

        presenter.addDocumentsUpdatedListenerInAMemoryLeakWay {
            runLater { updateValues() }
        }
    }


    override val root = vbox {
        minWidth = 240.0

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
            }
        }


        currentAndPreviousPeriodSummary("turnover",
            previousPeriodLabel,
            previousPeriodNetRevenues, "main.window.tab.overview.summary.pane.net.revenues",
            previousPeriodExpenditures, "expenditures",
            previousPeriodBalance, "balance",
            currentPeriodLabel,
            currentPeriodNetRevenues, "main.window.tab.overview.summary.pane.net.revenues",
            currentPeriodExpenditures, "expenditures",
            currentPeriodBalance, "balance"
        )


        currentAndPreviousPeriodSummary("value.added.tax",
            previousPeriodLabel,
            previousPeriodReceivedVat, "main.window.tab.overview.summary.pane.received.vat",
            previousPeriodSpentVat, "main.window.tab.overview.summary.pane.spent.vat",
            previousPeriodVatBalance, "balance",
            currentPeriodLabel,
            currentPeriodReceivedVat, "main.window.tab.overview.summary.pane.received.vat",
            currentPeriodSpentVat, "main.window.tab.overview.summary.pane.spent.vat",
            currentPeriodVatBalance, "balance"
        )
    }

    private fun EventTarget.currentAndPreviousPeriodSummary(categoryResourceKey: String,
                previousPeriodLabel: SimpleStringProperty,
                previousReceivedAmountString: SimpleStringProperty, previousReceivedLabelResourceKey: String,
                previousSpentAmountString: SimpleStringProperty, previousSpentLabelResourceKey: String,
                previousBalanceAmountString: SimpleStringProperty, previousBalanceLabelResourceKey: String,
                currentPeriodLabel: SimpleStringProperty,
                currentReceivedAmountString: SimpleStringProperty, currentReceivedLabelResourceKey: String,
                currentSpentAmountString: SimpleStringProperty, currentSpentLabelResourceKey: String,
                currentBalanceAmountString: SimpleStringProperty, currentBalanceLabelResourceKey: String): Pane {

        return vbox {
            this.setBorder()

            paddingAll = 4.0

            label(messages[categoryResourceKey])

            periodSummary(
                previousPeriodLabel,
                previousReceivedAmountString, previousReceivedLabelResourceKey,
                previousSpentAmountString, previousSpentLabelResourceKey,
                previousBalanceAmountString, previousBalanceLabelResourceKey
            )

            periodSummary(
                currentPeriodLabel,
                currentReceivedAmountString, currentReceivedLabelResourceKey,
                currentSpentAmountString, currentSpentLabelResourceKey,
                currentBalanceAmountString, currentBalanceLabelResourceKey
            )


            vboxConstraints {
                marginTop = 6.0
            }

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


    private fun updateValues() {
        previousPeriodLabel.value = if (presenter.accountingPeriod == AccountingPeriod.Quarterly)
            messages["main.window.tab.overview.summary.pane.previous.quarter"]
        else messages["main.window.tab.overview.summary.pane.previous.month"]

        currentPeriodLabel.value = if (presenter.accountingPeriod == AccountingPeriod.Quarterly)
            messages["main.window.tab.overview.summary.pane.current.quarter"]
        else messages["main.window.tab.overview.summary.pane.current.month"]

        updateTurnoverValues()

        updateVatValues()
    }

    private fun updateTurnoverValues() {
        previousPeriodNetRevenues.value = presenter.getCurrencyString(presenter.calculatePreviousAccountingPeriodNetRevenues())

        previousPeriodExpenditures.value = presenter.getCurrencyString(presenter.calculatePreviousAccountingPeriodExpenditures())

        previousPeriodBalance.value = presenter.getCurrencyString(presenter.calculatePreviousAccountingPeriodBalance())


        currentPeriodNetRevenues.value = presenter.getCurrencyString(presenter.calculateCurrentAccountingPeriodNetRevenues())

        currentPeriodExpenditures.value = presenter.getCurrencyString(presenter.calculateCurrentAccountingPeriodExpenditures())

        currentPeriodBalance.value = presenter.getCurrencyString(presenter.calculateCurrentAccountingPeriodBalance())
    }

    private fun updateVatValues() {
        previousPeriodReceivedVat.value = presenter.getCurrencyString(presenter.calculatePreviousAccountingPeriodReceivedVat())
        previousPeriodSpentVat.value = presenter.getCurrencyString(presenter.calculatePreviousAccountingPeriodSpentVat())
        previousPeriodVatBalance.value = presenter.getCurrencyString(presenter.calculatePreviousAccountingPeriodVatBalance())

        currentPeriodReceivedVat.value = presenter.getCurrencyString(presenter.calculateCurrentAccountingPeriodReceivedVat())
        currentPeriodSpentVat.value = presenter.getCurrencyString(presenter.calculateCurrentAccountingPeriodSpentVat())
        currentPeriodVatBalance.value = presenter.getCurrencyString(presenter.calculateCurrentAccountingPeriodVatBalance())
    }

}