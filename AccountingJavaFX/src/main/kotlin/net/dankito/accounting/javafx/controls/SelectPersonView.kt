package net.dankito.accounting.javafx.controls

import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import net.dankito.accounting.data.model.person.NaturalOrLegalPerson
import net.dankito.accounting.data.model.person.PersonType
import net.dankito.accounting.javafx.presenter.SelectPersonPresenter
import net.dankito.accounting.javafx.windows.person.model.RequiredField
import tornadofx.*


open class SelectPersonView<T : NaturalOrLegalPerson>(
    protected val presenter: SelectPersonPresenter,
    protected val selectedPerson: Property<T>,
    protected val typesOfPersonsToEdit: PersonType,
    protected val requiredFields: List<RequiredField>
) : View() {

    companion object {

        private const val ButtonsWidth = 110.0

    }

    protected val isAPersonSelected = SimpleBooleanProperty(selectedPerson.value != null)

    protected val availablePersons = FXCollections.observableArrayList(presenter.getAllOfType(typesOfPersonsToEdit)) as ObservableList<T>


    override val root = hbox {

        combobox<T>(selectedPerson, availablePersons) {
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
        presenter.showCreatePersonWindow(typesOfPersonsToEdit, requiredFields) { createdPerson ->
            createdPerson?.let {
                showAvailablePersons()

                selectedPerson.value = createdPerson as T
            }
        }
    }

    protected open fun editSelectedPerson() {
        presenter.showEditPersonWindow(selectedPerson.value, requiredFields) { didUserSavePerson, _ ->
            if (didUserSavePerson) {
                showAvailablePersons()
            }
        }
    }

    protected open fun showAvailablePersons() {
        availablePersons.setAll(presenter.getAllOfType(typesOfPersonsToEdit) as List<T>)
    }

}