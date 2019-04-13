package net.dankito.accounting.javafx.windows.document

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleFloatProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.utils.datetime.DateConvertUtils
import net.dankito.utils.javafx.ui.dialogs.Window
import tornadofx.*
import java.time.LocalDate
import java.util.*


class EditDocumentWindow(private val document: Document, private val presenter: OverviewPresenter) : Window() {

    companion object {

        private const val FieldHeight = 30.0
        private const val FieldVerticalSpace = 4.0

        private const val LabelsWidth = 135.0

        private const val AmountTextFieldsWidth = 160.0

        private const val DatePickerWidth = 160.0

        private const val ButtonsHeight = 38.0
        private const val ButtonsWidth = 120.0
        private const val ButtonsHorizontalSpace = 12.0

    }


    private val documentDescription = SimpleStringProperty(document.documentDescription)

    private val vatRate = SimpleFloatProperty(if (document.isValueAddedTaxRateSet) document.valueAddedTaxRate
                                                else presenter.getDefaultVatRateForUser())

    private val totalAmount = SimpleDoubleProperty(if (document.isTotalAmountSet) document.totalAmount else 0.0)

    private val paymentDate = SimpleObjectProperty<LocalDate>(DateConvertUtils.asLocalDate(document.paymentDate ?: Date()))


    override val root = vbox {

        prefWidth = 450.0

        hbox {
            minHeight = FieldHeight
            maxHeight = FieldHeight
            useMaxWidth = true

            vboxConstraints { marginBottom = FieldVerticalSpace }

            label("edit.document.window.document.description.label")

            textfield(documentDescription) {
                minHeight = FieldHeight
                maxHeight = FieldHeight

                hboxConstraints {
                    hGrow = Priority.ALWAYS
                }
            }
        }

        anchorpane {
            minHeight = FieldHeight
            maxHeight = FieldHeight
            useMaxWidth = true

            vboxConstraints { marginBottom = FieldVerticalSpace }

            label("edit.document.window.vat.rate.label")

            combobox(vatRate, presenter.getVatRatesForUser()) {
                cellFormat { text = "$it %" }

                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = 0.0
                    bottomAnchor = 0.0
                }
            }
        }

        anchorpane {
            minHeight = FieldHeight
            maxHeight = FieldHeight
            useMaxWidth = true

            vboxConstraints { marginBottom = FieldVerticalSpace }

            label("edit.document.window.total.amount.label")

            textfield(totalAmount) {
                minWidth = AmountTextFieldsWidth
                maxWidth = AmountTextFieldsWidth

                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = 0.0
                    bottomAnchor = 0.0
                }
            }
        }

        anchorpane {
            minHeight = FieldHeight
            maxHeight = FieldHeight
            useMaxWidth = true

            vboxConstraints { marginBottom = FieldVerticalSpace }

            label("edit.document.window.payment.date.label")

            datepicker(paymentDate) {
                minWidth = DatePickerWidth
                maxWidth = DatePickerWidth

                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = 0.0
                    bottomAnchor = 0.0
                }
            }
        }

        anchorpane {
            minHeight = ButtonsHeight
            maxHeight = ButtonsHeight
            useMaxWidth = true

            button(messages["ok"]) {
                minWidth = ButtonsWidth
                maxWidth = ButtonsWidth

                action { saveAndClose() }

                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = 0.0
                    bottomAnchor = 0.0
                }
            }

            button(messages["cancel"]) {
                minWidth = ButtonsWidth
                maxWidth = ButtonsWidth

                action { askIfChangesShouldBeSavedAndClose() }

                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = ButtonsWidth + ButtonsHorizontalSpace
                    bottomAnchor = 0.0
                }
            }
        }
    }

    fun EventTarget.label(textResourceKey: String = ""): Label {
        return label(messages[textResourceKey], null).apply {
            useMaxHeight = true
            minWidth = LabelsWidth
            maxHeight = LabelsWidth

            anchorpaneConstraints {
                topAnchor = 0.0
                bottomAnchor = 0.0
            }
        }
    }


    private fun saveAndClose() {
        document.documentDescription = documentDescription.value
        document.valueAddedTaxRate = vatRate.value
        document.totalAmount = totalAmount.value
        document.paymentDate = DateConvertUtils.asUtilDate(paymentDate.value)

        presenter.saveOrUpdate(document)

        close()
    }

    private fun askIfChangesShouldBeSavedAndClose() {
        // TODO: check if changes are made and if so ask user if he/she likes to save them

        close()
    }

}