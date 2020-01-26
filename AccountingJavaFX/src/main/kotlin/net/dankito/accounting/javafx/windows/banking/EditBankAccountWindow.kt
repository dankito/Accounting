package net.dankito.accounting.javafx.windows.banking

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Tooltip
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import net.dankito.accounting.data.model.banking.BankAccount
import net.dankito.accounting.data.model.banking.CheckBankAccountCredentialsResult
import net.dankito.accounting.javafx.presenter.BankAccountsPresenter
import net.dankito.utils.exception.ExceptionHelper
import net.dankito.utils.javafx.ui.dialogs.Window
import net.dankito.utils.javafx.ui.extensions.ensureOnlyUsesSpaceIfVisible
import net.dankito.utils.javafx.ui.extensions.setBackgroundToColor
import tornadofx.*
import javax.inject.Inject


class EditBankAccountWindow(private val account: BankAccount) : Window() {

    companion object {
        private val LabelMargins = Insets(6.0, 4.0, 6.0, 4.0)

        private val TextFieldHeight = 36.0
        private val TextFieldMargins = Insets(0.0, 4.0, 12.0, 4.0)

        private val ButtonHeight = 40.0
        private val ButtonWidth = 150.0
    }


    @Inject
    protected lateinit var presenter: BankAccountsPresenter


    private val bankCode = SimpleStringProperty(account.bankCode)

    private val customerId = SimpleStringProperty(account.customerId)

    private val password = SimpleStringProperty(account.password)


    private val checkEnteredCredentialsResult = SimpleStringProperty("")

    private val isEnteredCredentialsResultVisible = SimpleBooleanProperty(false)

    private val didEnteredCredentialsMatch = SimpleBooleanProperty(false)


    private val exceptionHelper = ExceptionHelper()


    init {
//        AppComponent.component.inject(this)
    }


    override val root = vbox {
        prefWidth = 350.0

        label(messages["edit.bank.account.window.bank.code.label"]) {
            vboxConstraints {
                margin = LabelMargins
            }
        }

       textfield(bankCode) {
            prefHeight = TextFieldHeight

            vboxConstraints {
                margin = TextFieldMargins
            }
        }

        label(messages["edit.bank.account.window.customer.id.and.password.hint"]) {
            font = Font.font(this.font.name, FontWeight.BOLD, this.font.size + 1)

            isWrapText = true

            vboxConstraints {
                marginTop = 12.0
                marginBottom = 6.0
            }
        }

        label(messages["edit.bank.account.window.customer.id"]) {
            vboxConstraints {
                margin = LabelMargins
            }
        }

        textfield(customerId) {
            promptText = messages["edit.bank.account.window.customer.id.hint"]
            prefHeight = TextFieldHeight

            vboxConstraints {
                margin = TextFieldMargins
            }
        }

        label(messages["edit.bank.account.window.password"]) {
            vboxConstraints {
                margin = LabelMargins
            }
        }

        passwordfield(password) {
            promptText = messages["edit.bank.account.window.password.hint"]
            prefHeight = TextFieldHeight

            vboxConstraints {
                margin = TextFieldMargins
            }
        }

        label(checkEnteredCredentialsResult) {
            visibleWhen(isEnteredCredentialsResultVisible)
            ensureOnlyUsesSpaceIfVisible()

            isWrapText = true
            font = Font(font.size + 1)

            paddingAll = 8.0

            checkEnteredCredentialsResult.addListener { _, _, _ ->
                if (didEnteredCredentialsMatch.value) {
                    setBackgroundToColor(Color.TRANSPARENT)
                }
                else {
                    setBackgroundToColor(Color.RED)
                }

                tooltip = Tooltip(checkEnteredCredentialsResult.value)

                currentWindow?.sizeToScene()
            }

            vboxConstraints {
                marginTop = 12.0
                marginBottom = 6.0
            }
        }

        hbox {
            alignment = Pos.CENTER_RIGHT

            button(messages["cancel"]) {
                minHeight = ButtonHeight
                maxHeight = minHeight
                prefWidth = ButtonWidth

                isCancelButton = true

                action { close() }

                hboxConstraints {
                    margin = Insets(6.0, 0.0, 4.0, 0.0)
                }
            }

            button(messages["ok"]) {
                minHeight = ButtonHeight
                maxHeight = minHeight
                prefWidth = ButtonWidth

                isDefaultButton = true

                action { checkEnteredCredentials() }

                hboxConstraints {
                    margin = Insets(6.0, 4.0, 4.0, 12.0)
                }
            }
        }
    }


    private fun checkEnteredCredentials() {
        val accountToCheck = BankAccount(bankCode.value, customerId.value, password.value)

        presenter.checkAccountCredentialsAsync(accountToCheck) { result ->
            runLater { retrievedCheckBankAccountCredentialsResult(accountToCheck, result) }
        }
    }

    private fun retrievedCheckBankAccountCredentialsResult(accountToCheck: BankAccount, result: CheckBankAccountCredentialsResult) {
        isEnteredCredentialsResultVisible.value = true
        didEnteredCredentialsMatch.value = result.successful

        result.error?.let { error ->
            val innerException = exceptionHelper.getInnerException(error)

            checkEnteredCredentialsResult.value = String.format(messages["edit.bank.account.window.could.not.add.account"],
                accountToCheck.bankCode, accountToCheck.customerId, innerException.localizedMessage)
        }
        ?: successfullyEnteredCredentials(accountToCheck)
    }

    private fun successfullyEnteredCredentials(accountToCheck: BankAccount) {
        account.bankCode = accountToCheck.bankCode
        account.customerId = accountToCheck.customerId
        account.password = accountToCheck.password

        checkEnteredCredentialsResult.value = messages["edit.bank.account.window.add.account.success"]

        presenter.saveAccountAndFetchTransactions(account)

        close()
    }

}