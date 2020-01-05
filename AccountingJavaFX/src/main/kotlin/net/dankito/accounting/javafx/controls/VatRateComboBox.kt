package net.dankito.accounting.javafx.controls

import javafx.beans.property.SimpleFloatProperty
import javafx.event.EventTarget
import javafx.scene.control.ComboBox
import net.dankito.utils.javafx.util.converter.ZeroTo100PercentageStringConverter
import tornadofx.bind
import tornadofx.observable
import tornadofx.opcr


open class VatRateComboBox(selectedVatRate: SimpleFloatProperty?, vatRatesForUser: List<Float>)
    : ComboBox<Number>(vatRatesForUser.observable()) {

    init {
        selectedVatRate?.let { bind(it) }

        converter = ZeroTo100PercentageStringConverter()
    }

}

fun EventTarget.vatRateComboBox(selectedVatRate: SimpleFloatProperty?, vatRatesForUser: List<Float>, op: VatRateComboBox.() -> Unit = {}) =
    opcr(this, VatRateComboBox(selectedVatRate, vatRatesForUser), op)