package net.dankito.accounting.javafx.windows.banking.controls

import javafx.geometry.Insets
import javafx.geometry.Pos
import net.dankito.accounting.data.model.banking.BankAccountTransaction
import net.dankito.accounting.javafx.windows.mainwindow.model.BankAccountTransactionUsageItemViewModel
import net.dankito.utils.javafx.ui.extensions.ensureOnlyUsesSpaceIfVisible
import tornadofx.*


class UsageCellFragment : TableCellFragment<BankAccountTransaction, BankAccountTransaction>() {

    companion object {
        private val LabelMargin = Insets(4.0, 0.0, 4.0, 4.0)
    }


    val entry = BankAccountTransactionUsageItemViewModel().bindToItem(this)


    override val root = vbox {
        prefHeight = 94.0
        alignment = Pos.CENTER_LEFT

        label(entry.type) {
            vboxConstraints {
                margin = LabelMargin
            }
        }

        label(entry.senderOrReceiverName) {
            visibleWhen(entry.showSenderOrReceiver)
            ensureOnlyUsesSpaceIfVisible()

            vboxConstraints {
                margin = LabelMargin
            }
        }

        label(entry.usage) {
            vboxConstraints {
                margin = LabelMargin
            }
        }
    }

}