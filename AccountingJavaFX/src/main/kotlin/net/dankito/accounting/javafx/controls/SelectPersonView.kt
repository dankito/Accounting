package net.dankito.accounting.javafx.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.FXCollections
import net.dankito.accounting.data.model.Person
import net.dankito.accounting.javafx.presenter.SelectPersonPresenter
import tornadofx.*


open class SelectPersonView(
    protected val presenter: SelectPersonPresenter,
    protected val selectedPerson: SimpleObjectProperty<Person>
) : View() {

    companion object {

        private const val ButtonsWidth = 110.0

    }

    protected val isAPersonSelected = SimpleBooleanProperty(selectedPerson.value != null)

    protected val availablePersons = FXCollections.observableArrayList(presenter.getAllPersons())


    override val root = hbox {

        combobox<Person>(selectedPerson, availablePersons) {
            prefWidth = 300.0

            cellFormat { text = it.name }

            valueProperty().addListener { _, _, newValue -> isAPersonSelected.value = newValue != null }
        }

        button(messages["edit..."]) {
            prefWidth = ButtonsWidth

            enableWhen(isAPersonSelected)

            action { editSelectedPerson() }

            hboxConstraints {
                marginLeft = 4.0
                marginRight = 12.0
            }
        }

        button(messages["new..."]) {
            prefWidth = ButtonsWidth

            action { createNewPerson() }

            hboxConstraints {
            }
        }

    }


    protected open fun createNewPerson() {
        presenter.showCreatePersonWindow { createdPerson ->
            createdPerson?.let {
                showAvailablePersons()

                selectedPerson.value = createdPerson
            }
        }
    }

    protected open fun editSelectedPerson() {
        presenter.showEditPersonWindow(selectedPerson.value) { didUserSavePerson ->
            if (didUserSavePerson) {
                showAvailablePersons()
            }
        }
    }

    protected open fun showAvailablePersons() {
        availablePersons.setAll(presenter.getAllPersons())
    }

}