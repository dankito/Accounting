package net.dankito.accounting.javafx.windows.mainwindow.model

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import net.dankito.accounting.data.model.banking.BankAccountTransaction
import tornadofx.ItemViewModel


class BankAccountTransactionUsageItemViewModel : ItemViewModel<BankAccountTransaction>() {

    val type = bind { SimpleStringProperty(item?.type) }

    val showOtherName = bind { SimpleBooleanProperty(item?.showSenderOrReceiver ?: false) }

    val otherName = bind { SimpleStringProperty(item?.senderOrReceiver) }

    val usage1 = bind { SimpleStringProperty(item?.usage1) }

    val isUsage2Set = bind { SimpleBooleanProperty(item?.usage2 != null) }

    val usage2 = bind { SimpleStringProperty(item?.usage2) }

}