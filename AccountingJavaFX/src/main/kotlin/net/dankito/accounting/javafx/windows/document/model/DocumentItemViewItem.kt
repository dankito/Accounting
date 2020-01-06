package net.dankito.accounting.javafx.windows.document.model

import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleFloatProperty
import javafx.beans.property.SimpleStringProperty
import net.dankito.accounting.data.model.DocumentItem
import tornadofx.ItemViewModel


open class DocumentItemViewItem(item: DocumentItem? = null) : ItemViewModel<DocumentItem>(item) {

    val description = bind(DocumentItem::description) as SimpleStringProperty

    val netAmount = bind(DocumentItem::netAmount) as SimpleDoubleProperty

    val vatRate = bind(DocumentItem::valueAddedTaxRate) as SimpleFloatProperty

    val vat = bind(DocumentItem::valueAddedTax) as SimpleDoubleProperty

    val grossAmount = bind(DocumentItem::grossAmount) as SimpleDoubleProperty

}