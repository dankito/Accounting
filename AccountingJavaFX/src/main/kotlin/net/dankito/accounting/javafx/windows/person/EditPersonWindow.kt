package net.dankito.accounting.javafx.windows.person

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.control.ToggleGroup
import net.dankito.accounting.data.model.Address
import net.dankito.accounting.data.model.person.Company
import net.dankito.accounting.data.model.person.NaturalOrLegalPerson
import net.dankito.accounting.data.model.person.Person
import net.dankito.accounting.data.model.person.PersonType
import net.dankito.accounting.javafx.di.AppComponent
import net.dankito.accounting.javafx.presenter.EditPersonPresenter
import net.dankito.utils.javafx.ui.dialogs.Window
import net.dankito.utils.javafx.ui.extensions.ensureOnlyUsesSpaceIfVisible
import tornadofx.*
import javax.inject.Inject


// TODO: add validation (e. g. a name has to be entered, country may not be longer than 20 characters, ...)
class EditPersonWindow(
    person: NaturalOrLegalPerson?,
    private val personType: PersonType,
    private val didUserSavePersonCallback: ((Boolean, NaturalOrLegalPerson?) -> Unit)? = null
) : Window() {

    companion object {

        private const val ButtonsHeight = 36.0
        private const val ButtonsWidth = 120.0
        private const val ButtonsHorizontalSpace = 12.0
    }


    @Inject
    protected lateinit var presenter: EditPersonPresenter


    private var personToEdit: NaturalOrLegalPerson = person ?: Person("", "", personType, Address("", "", "", "", ""))

    private val allowChoosingPersonType = SimpleBooleanProperty(person == null)

    private val isNaturalPerson = SimpleBooleanProperty(personToEdit is Person)

    private val companyName = SimpleStringProperty((personToEdit as? Company)?.name ?: "")

    private val firstName = SimpleStringProperty((personToEdit as? Person)?.firstName ?: "")

    private val lastName = SimpleStringProperty((personToEdit as? Person)?.lastName ?: "")

    private val street = SimpleStringProperty(personToEdit.address.street)

    private val streetNumber = SimpleStringProperty(personToEdit.address.streetNumber)

    private val zipCode = SimpleStringProperty(personToEdit.address.zipCode)

    private val city = SimpleStringProperty(personToEdit.address.city)

    private val country = SimpleStringProperty(personToEdit.address.country)


    init {
        AppComponent.component.inject(this)
    }


    fun show() {
        show(messages["edit.person.window.title"])
    }


    override val root = vbox {
        prefWidth = 450.0

        paddingTop = 2.0
        paddingLeft = 2.0
        paddingRight = 2.0

        isNaturalPerson.addListener { _, _, _ -> currentStage?.sizeToScene() }

        form {
            fieldset {

                hbox {
                    visibleWhen(allowChoosingPersonType)
                    ensureOnlyUsesSpaceIfVisible()

                    val toggleGroup = ToggleGroup()

                    radiobutton(messages["edit.person.window.edit.person"], toggleGroup, isNaturalPerson) {
                        selectedProperty().bindBidirectional(isNaturalPerson)

                        hboxConstraints {
                            marginRight = 12.0
                        }
                    }

                    radiobutton(messages["edit.person.window.edit.company"], toggleGroup)

                    vboxConstraints {
                        marginBottom = 12.0
                    }
                }

                field(messages["edit.person.window.company.name.label"]) {
                    hiddenWhen(isNaturalPerson)
                    ensureOnlyUsesSpaceIfVisible()

                    textfield(companyName)
                }

                field(messages["edit.person.window.first.name.label"]) {
                    visibleWhen(isNaturalPerson)
                    ensureOnlyUsesSpaceIfVisible()

                    textfield(firstName)
                }

                field(messages["edit.person.window.last.name.label"]) {
                    visibleWhen(isNaturalPerson)
                    ensureOnlyUsesSpaceIfVisible()

                    textfield(lastName)
                }

                field(messages["edit.person.window.street.label"]) {
                    textfield(street)
                }

                field(messages["edit.person.window.street.number.label"]) {
                    textfield(streetNumber)
                }

                field(messages["edit.person.window.zip.code.label"]) {
                    textfield(zipCode)
                }

                field(messages["edit.person.window.city.label"]) {
                    textfield(city)
                }

                field(messages["edit.person.window.country.label"]) {
                    textfield(country)
                }
            }
        }

        anchorpane {
            prefHeight = ButtonsHeight

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
        (personToEdit as? Person)?.let { person ->
            person.firstName = firstName.value
            person.lastName = lastName.value
        }

        (personToEdit as? Company)?.let { company ->
            company.name = companyName.value
        }

        personToEdit.address.street = street.value
        personToEdit.address.streetNumber = streetNumber.value
        personToEdit.address.zipCode = zipCode.value
        personToEdit.address.city = city.value
        personToEdit.address.country = country.value

        presenter.saveOrUpdate(personToEdit)

        closeWindow(true)
    }

    private fun askUserToSaveChangesAndClose() {
        // TODO: check for unsaved changes and ask user if he/she likes to save them

        closeWindow(false)
    }

    private fun closeWindow(didSavePerson: Boolean) {
        didUserSavePersonCallback?.invoke(didSavePerson, if (didSavePerson) personToEdit else null)

        close()
    }

}