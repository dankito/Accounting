package net.dankito.accounting.javafx.windows.mainwindow.controls

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

        label(entry.otherName) {
            visibleWhen(entry.showOtherName)
            ensureOnlyUsesSpaceIfVisible()

            vboxConstraints {
                margin = LabelMargin
            }
        }

        label(entry.usage1) {
            vboxConstraints {
                margin = LabelMargin
            }
        }

        label(entry.usage2) {
            visibleWhen(entry.isUsage2Set)
            ensureOnlyUsesSpaceIfVisible()

            vboxConstraints {
                margin = Insets(0.0, LabelMargin.right, LabelMargin.bottom, LabelMargin.left)
            }
        }
    }

}