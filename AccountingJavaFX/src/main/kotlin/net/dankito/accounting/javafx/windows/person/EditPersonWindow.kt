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
import net.dankito.accounting.javafx.windows.person.model.RequiredField
import net.dankito.utils.javafx.ui.dialogs.Window
import net.dankito.utils.javafx.ui.extensions.ensureOnlyUsesSpaceIfVisible
import net.dankito.utils.javafx.ui.extensions.fixedWidth
import tornadofx.*
import javax.inject.Inject


// TODO: add validation (e. g. a name has to be entered, country may not be longer than 20 characters, ...)
class EditPersonWindow(
    person: NaturalOrLegalPerson?,
    personType: PersonType,
    private val requiredFields: List<RequiredField> = listOf(),
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

    private val haveAllRequiredFieldsBeenEntered = SimpleBooleanProperty(requiredFields.isEmpty())


    init {
        AppComponent.component.inject(this)

        initLogic()
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

                field(messages["edit.person.window.street.name.and.number.label"]) {
                    textfield(street)

                    textfield(streetNumber) {
                        fixedWidth = 60.0
                    }
                }

                field(messages["edit.person.window.postal.code.and.city.label"]) {
                    textfield(zipCode) {
                        fixedWidth = 60.0
                    }

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

                enableWhen(haveAllRequiredFieldsBeenEntered)

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


    private fun initLogic() {
        isNaturalPerson.addListener { _, _, _ -> checkIfRequiredFieldsHaveBeenEntered() }

        companyName.addListener { _, _, _ -> checkIfRequiredFieldsHaveBeenEntered() }
        firstName.addListener { _, _, _ -> checkIfRequiredFieldsHaveBeenEntered() }
        lastName.addListener { _, _, _ -> checkIfRequiredFieldsHaveBeenEntered() }

        street.addListener { _, _, _ -> checkIfRequiredFieldsHaveBeenEntered() }
        streetNumber.addListener { _, _, _ -> checkIfRequiredFieldsHaveBeenEntered() }
        zipCode.addListener { _, _, _ -> checkIfRequiredFieldsHaveBeenEntered() }
        city.addListener { _, _, _ -> checkIfRequiredFieldsHaveBeenEntered() }
        country.addListener { _, _, _ -> checkIfRequiredFieldsHaveBeenEntered() }

        checkIfRequiredFieldsHaveBeenEntered()
    }

    private fun checkIfRequiredFieldsHaveBeenEntered() {
        if (requiredFields.isNotEmpty()) {
            haveAllRequiredFieldsBeenEntered.value = requiredFields.firstOrNull { hasRequiredFieldBeenEntered(it) == false } == null
        }
    }

    private fun hasRequiredFieldBeenEntered(field: RequiredField): Boolean {
        return when (field) {
            RequiredField.CompanyName -> isNaturalPerson.value || companyName.value.isNotBlank()
            RequiredField.PersonFirstName -> isNaturalPerson.value == false || firstName.value.isNotBlank()
            RequiredField.PersonLastName -> isNaturalPerson.value == false || lastName.value.isNotBlank()

            RequiredField.AddressStreet -> street.value.isNotBlank()
            RequiredField.AddressStreetNumber -> streetNumber.value.isNotBlank()
            RequiredField.AddressZipCode -> zipCode.value.isNotBlank()
            RequiredField.AddressCity -> city.value.isNotBlank()
            RequiredField.AddressCountry -> country.value.isNotBlank()
        }
    }


    private fun saveAndClose() {
        if (personToEdit.isPersisted() == false) {
            // a new Person or Company gets created and user selected Company
            if (isNaturalPerson.value == false && personToEdit is Person) {
                personToEdit = Company(companyName.value, personToEdit.type, personToEdit.address)
            }
        }

        (personToEdit as? Person)?.let { person ->
            person.firstName = firstName.value
            person.lastName = lastName.value
        }

        (personToEdit as? Company)?.let { company ->
            company.name = companyName.value
        }

        personToEdit.apply {
            address.street = street.value
            address.streetNumber = streetNumber.value
            address.zipCode = zipCode.value
            address.city = city.value
            address.country = country.value
        }

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