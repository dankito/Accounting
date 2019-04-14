package net.dankito.accounting.javafx.windows.mainwindow.controls

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.scene.layout.Pane
import javafx.scene.text.Font
import javafx.scene.text.TextAlignment
import net.dankito.accounting.data.dao.JsonPersonDao
import net.dankito.accounting.data.model.AccountingPeriod
import net.dankito.accounting.javafx.presenter.ElsterTaxPresenter
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.accounting.javafx.windows.tax.elster.ElsterTaxDeclarationWindow
import net.dankito.accounting.service.person.PersonService
import net.dankito.utils.ThreadPool
import net.dankito.utils.javafx.ui.controls.currencyLabel
import net.dankito.utils.javafx.ui.extensions.setBorder
import tornadofx.*
import java.io.File


class SummaryPane(private val presenter: OverviewPresenter) : View() {

    companion object {
        private const val SummaryAmountLabelWidth = 90.0

        private const val ElsterButtonsHeight = 30.0
        private const val ElsterButtonsWidth = 180.0
        private const val ElsterButtonsFontSize = 12.0
    }


    private val previousPeriodLabel = SimpleStringProperty()

    private val currentPeriodLabel = SimpleStringProperty()


    private val previousPeriodReceivedVat = SimpleDoubleProperty()

    private val previousPeriodSpentVat = SimpleDoubleProperty()

    private val previousPeriodVatBalance = SimpleDoubleProperty()


    private val currentPeriodReceivedVat = SimpleDoubleProperty()

    private val currentPeriodSpentVat = SimpleDoubleProperty()

    private val currentPeriodVatBalance = SimpleDoubleProperty()


    private val previousPeriodNetRevenues = SimpleDoubleProperty()

    private val previousPeriodExpenditures = SimpleDoubleProperty()

    private val previousPeriodBalance = SimpleDoubleProperty()


    private val currentPeriodNetRevenues = SimpleDoubleProperty()

    private val currentPeriodExpenditures = SimpleDoubleProperty()

    private val currentPeriodBalance = SimpleDoubleProperty()


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

        vbox {
            setBorder()

            paddingAll = 4.0

            vboxConstraints {
                marginTop = 6.0
            }


            label(messages["main.window.tab.overview.summary.pane.spent.advance.return.for.sales.tax.label"])

            anchorpane {
                minHeight = ElsterButtonsHeight
                maxHeight = ElsterButtonsHeight

                vboxConstraints {
                    marginTop = 4.0
                }

                button(messages["main.window.tab.overview.summary.pane.spent.advance.return.for.sales.tax.create.elster.xml"]) {
                    minWidth = ElsterButtonsWidth
                    maxWidth = ElsterButtonsWidth

                    font = Font.font(ElsterButtonsFontSize)

                    action { createElsterXml() }

                    anchorpaneConstraints {
                        topAnchor = 0.0
                        rightAnchor = 0.0
                        bottomAnchor = 0.0
                    }
                }
            }

            anchorpane {
                minHeight = ElsterButtonsHeight
                maxHeight = ElsterButtonsHeight

                vboxConstraints {
                    marginTop = 4.0
                }

                button(messages["main.window.tab.overview.summary.pane.spent.advance.return.for.sales.tax.upload.to.elster"]) {
                    minWidth = ElsterButtonsWidth
                    maxWidth = ElsterButtonsWidth

                    font = Font.font(ElsterButtonsFontSize)

                    action { uploadToElster() }

                    anchorpaneConstraints {
                        topAnchor = 0.0
                        rightAnchor = 0.0
                        bottomAnchor = 0.0
                    }
                }
            }
        }
    }

    private fun EventTarget.currentAndPreviousPeriodSummary(categoryResourceKey: String,
                previousPeriodLabel: SimpleStringProperty,
                previousReceivedAmount: SimpleDoubleProperty, previousReceivedLabelResourceKey: String,
                previousSpentAmount: SimpleDoubleProperty, previousSpentLabelResourceKey: String,
                previousBalanceAmount: SimpleDoubleProperty, previousBalanceLabelResourceKey: String,
                currentPeriodLabel: SimpleStringProperty,
                currentReceivedAmount: SimpleDoubleProperty, currentReceivedLabelResourceKey: String,
                currentSpentAmount: SimpleDoubleProperty, currentSpentLabelResourceKey: String,
                currentBalanceAmount: SimpleDoubleProperty, currentBalanceLabelResourceKey: String): Pane {

        return vbox {
            this.setBorder()

            paddingAll = 4.0

            label(messages[categoryResourceKey])

            periodSummary(
                previousPeriodLabel,
                previousReceivedAmount, previousReceivedLabelResourceKey,
                previousSpentAmount, previousSpentLabelResourceKey,
                previousBalanceAmount, previousBalanceLabelResourceKey
            )

            periodSummary(
                currentPeriodLabel,
                currentReceivedAmount, currentReceivedLabelResourceKey,
                currentSpentAmount, currentSpentLabelResourceKey,
                currentBalanceAmount, currentBalanceLabelResourceKey
            )


            vboxConstraints {
                marginTop = 6.0
            }

        }
    }

    private fun EventTarget.periodSummary(periodLabel: SimpleStringProperty,
                                          receivedAmount: SimpleDoubleProperty, receivedLabelResourceKey: String,
                                          spentAmount: SimpleDoubleProperty, spentLabelResourceKey: String,
                                          balanceAmount: SimpleDoubleProperty, balanceLabelResourceKey: String): Pane {

        return vbox {
            label(periodLabel) {

                vboxConstraints {
                    marginTop = 8.0
                    marginLeft = 6.0
                }
            }

            amountWithLabel(receivedAmount, receivedLabelResourceKey)

            amountWithLabel(spentAmount, spentLabelResourceKey)

            amountWithLabel(balanceAmount, balanceLabelResourceKey)
        }
    }

    private fun EventTarget.amountWithLabel(amount: SimpleDoubleProperty, labelResourceKey: String = ""): Pane {
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

            currencyLabel(amount) {
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
        previousPeriodNetRevenues.value = presenter.calculatePreviousAccountingPeriodNetRevenues()

        previousPeriodExpenditures.value = presenter.calculatePreviousAccountingPeriodExpenditures()

        previousPeriodBalance.value = presenter.calculatePreviousAccountingPeriodBalance()


        currentPeriodNetRevenues.value = presenter.calculateCurrentAccountingPeriodNetRevenues()

        currentPeriodExpenditures.value = presenter.calculateCurrentAccountingPeriodExpenditures()

        currentPeriodBalance.value = presenter.calculateCurrentAccountingPeriodBalance()
    }

    private fun updateVatValues() {
        previousPeriodReceivedVat.value = presenter.calculatePreviousAccountingPeriodReceivedVat()
        previousPeriodSpentVat.value = presenter.calculatePreviousAccountingPeriodSpentVat()
        previousPeriodVatBalance.value = presenter.calculatePreviousAccountingPeriodVatBalance()

        currentPeriodReceivedVat.value = presenter.calculateCurrentAccountingPeriodReceivedVat()
        currentPeriodSpentVat.value = presenter.calculateCurrentAccountingPeriodSpentVat()
        currentPeriodVatBalance.value = presenter.calculateCurrentAccountingPeriodVatBalance()
    }


    private fun createElsterXml() {
        ElsterTaxDeclarationWindow(
            ElsterTaxPresenter(PersonService(JsonPersonDao(File("data"))), ThreadPool()),
            presenter
        ).show()
    }

    private fun uploadToElster() {
        ElsterTaxDeclarationWindow(
            ElsterTaxPresenter(PersonService(JsonPersonDao(File("data"))), ThreadPool()),
            presenter
        ).show()
    }

}