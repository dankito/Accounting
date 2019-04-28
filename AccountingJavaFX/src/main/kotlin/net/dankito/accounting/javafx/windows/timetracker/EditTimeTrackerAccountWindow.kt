package net.dankito.accounting.javafx.windows.timetracker

import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import net.dankito.accounting.data.model.timetracker.TimeTrackerAccount
import net.dankito.accounting.data.model.timetracker.TimeTrackerType
import net.dankito.accounting.javafx.di.AppComponent
import net.dankito.accounting.javafx.presenter.TimeTrackerAccountPresenter
import net.dankito.utils.javafx.ui.dialogs.Window
import tornadofx.*
import javax.inject.Inject


class EditTimeTrackerAccountWindow(private val account: TimeTrackerAccount,
                                   private val userDidEditTimeTrackerAccountCallback: ((Boolean) -> Unit)? = null
) : Window() {

    companion object {

        private const val ButtonsHeight = 36.0
        private const val ButtonsWidth = 120.0
        private const val ButtonsHorizontalSpace = 12.0

    }


    @Inject
    lateinit var presenter: TimeTrackerAccountPresenter


    private val selectedTimeTrackerType = SimpleObjectProperty<TimeTrackerType>(account.type)

    private val accountName = SimpleStringProperty(account.accountName)

    private val username = SimpleStringProperty(account.username)

    private val password = SimpleStringProperty(account.password)


    private val allTimeTrackerAccountTypes = FXCollections.observableArrayList(*TimeTrackerType.values())


    init {
        AppComponent.component.inject(this)
    }


    override val root = form {

        fieldset {

            field(messages["edit.time.tracker.account.window.type"]) {

                combobox(selectedTimeTrackerType, allTimeTrackerAccountTypes) {
                    useMaxWidth = true
                }

            }

            field(messages["edit.time.tracker.account.window.account.name"]) {

                textfield(accountName)

            }

            field(messages["edit.time.tracker.account.window.username"]) {

                textfield(username)

            }

            field(messages["edit.time.tracker.account.window.password"]) {

                passwordfield(password)

            }

        }

        anchorpane {
            prefHeight = ButtonsHeight

            hboxConstraints {
                marginTop = 12.0
            }

            button(messages["ok"]) {
                prefWidth = ButtonsWidth

                isDefaultButton = true

                action { saveAndClose() }

                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = 0.0
                    bottomAnchor = 0.0
                }
            }

            button(messages["cancel"]) {
                prefWidth = ButtonsWidth

                isCancelButton = true

                action { askUserToSaveChangesAndClose() }

                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = ButtonsWidth + ButtonsHorizontalSpace
                    bottomAnchor = 0.0
                }
            }
        }

    }


    private fun saveAndClose() {
        account.type = selectedTimeTrackerType.value
        account.accountName = accountName.value
        account.username = username.value
        account.password = password.value

        presenter.saveOrUpdate(account)

        closeWindow(true)
    }

    private fun askUserToSaveChangesAndClose() {
        // TODO: check for unsaved changes and ask user if he/she likes to save them

        closeWindow(false)
    }

    private fun closeWindow(didSaveAccount: Boolean) {
        userDidEditTimeTrackerAccountCallback?.invoke(didSaveAccount)

        close()
    }

}