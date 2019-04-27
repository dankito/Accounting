package net.dankito.accounting.javafx.windows.tax.elster.controls

import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.beans.property.ReadOnlyStringWrapper
import javafx.beans.property.SimpleIntegerProperty
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.TextField
import net.dankito.utils.javafx.ui.controls.intTextfield
import org.slf4j.LoggerFactory
import tornadofx.View
import tornadofx.hbox
import tornadofx.hboxConstraints
import tornadofx.label


class TaxNumberInput : View() {

    companion object {
        const val TaxNumberPartsSeparator = "/"

        private const val TaxNumberPartHeight = 28.0
        private const val TaxNumberPartWidth = 60.0

        private val logger = LoggerFactory.getLogger(TaxNumberInput::class.java)
    }


    protected val taxNumberPart1 = SimpleIntegerProperty()
    protected val taxNumberPart2 = SimpleIntegerProperty()
    protected val taxNumberPart3 = SimpleIntegerProperty()

    protected val taxNumberProperty = ReadOnlyStringWrapper("")
    val taxNumber = taxNumberProperty.readOnlyProperty

    protected val isEnteredTaxNumberValidProperty = ReadOnlyBooleanWrapper(false)
    val isEnteredTaxNumberValid = isEnteredTaxNumberValidProperty.readOnlyProperty


    init {
        taxNumberPart1.addListener { _, _, _ -> updateTaxNumberAndIsEnteredTaxNumberIsValid() }
        taxNumberPart2.addListener { _, _, _ -> updateTaxNumberAndIsEnteredTaxNumberIsValid() }
        taxNumberPart3.addListener { _, _, _ -> updateTaxNumberAndIsEnteredTaxNumberIsValid() }
    }


    override val root = hbox {
        alignment = Pos.CENTER_LEFT

        taxNumberPartField(taxNumberPart1)

        taxNumberPartsSeparator()

        taxNumberPartField(taxNumberPart2)

        taxNumberPartsSeparator()

        taxNumberPartField(taxNumberPart3)
    }

    private fun EventTarget.taxNumberPartField(taxNumberPart: SimpleIntegerProperty, op: TextField.() -> Unit = {}): TextField {
        return intTextfield(taxNumberPart, false) {
            prefHeight = TaxNumberPartHeight
            prefWidth = TaxNumberPartWidth

            op(this)
        }
    }

    private fun EventTarget.taxNumberPartsSeparator(op: Label.() -> Unit = {}): Label {
        return label(TaxNumberPartsSeparator) {
            hboxConstraints {
                marginLeft = 6.0
                marginRight = 6.0
            }

            op(this)
        }
    }


    private fun updateTaxNumberAndIsEnteredTaxNumberIsValid() {
        taxNumberProperty.value = "" + taxNumberPart1.value + TaxNumberPartsSeparator + taxNumberPart2.value +
                TaxNumberPartsSeparator + taxNumberPart3.value

        // TODO: this is not fully valid, also contains invalid combinations.
        isEnteredTaxNumberValidProperty.value =
                taxNumberPart1.value in 100..999 &&
                taxNumberPart2.value in 100..9999 &&
                taxNumberPart3.value in 1000..99999
    }

    private fun tryToParseTaxNumber(taxNumber: String) {
        try {
            val parts = taxNumber.split('/')

            if (parts.size == 3) {
                taxNumberPart1.value = parts[0].toInt()
                taxNumberPart2.value = parts[1].toInt()
                taxNumberPart3.value = parts[2].toInt()
            }
        } catch (e: Exception) {
            logger.warn("Could not parse tax number '$taxNumber'", e)
        }
    }


    fun setTaxNumber(taxNumber: String) {
        tryToParseTaxNumber(taxNumber)
    }

}