package net.dankito.accounting.javafx.windows.mainwindow.model

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import net.dankito.accounting.data.model.banking.BankAccountTransaction
import tornadofx.ItemViewModel


class BankAccountTransactionUsageItemViewModel : ItemViewModel<BankAccountTransaction>() {

    val type = bind { SimpleStringProperty(item?.type) }

    val showSenderOrReceiver = bind { SimpleBooleanProperty(item?.showSenderOrReceiver ?: false) }

    val senderOrReceiverName = bind { SimpleStringProperty(item?.senderOrReceiverName) }

    val usage = bind { SimpleStringProperty(item?.usage) }

}