package net.dankito.accounting.javafx.presenter

import net.dankito.accounting.data.model.banking.BankAccount
import net.dankito.accounting.javafx.service.Router


class MainWindowPresenter(private val router: Router) {

    fun showCreateBankAccountWindow() {
        val newBankAccount = BankAccount("", "", "")

        router.showEditBankAccountWindow(newBankAccount)
    }

}