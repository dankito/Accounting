package net.dankito.accounting.javafx.windows.person

import javafx.beans.property.SimpleStringProperty
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import net.dankito.accounting.data.model.Person
import net.dankito.accounting.service.person.IPersonService
import net.dankito.utils.javafx.ui.dialogs.Window
import tornadofx.*


class EditPersonWindow(private val person: Person, private val personService: IPersonService,
                       private val didUserSavePersonCallback: ((Boolean) -> Unit)? = null) : Window() {

    companion object {

        private const val FieldHeight = 32.0

        private const val LabelsWidth = 100.0

        private const val ButtonsHeight = 36.0
        private const val ButtonsWidth = 120.0
        private const val ButtonsHorizontalSpace = 12.0
    }


    private val firstName = SimpleStringProperty(person.firstName)

    private val lastName = SimpleStringProperty(person.lastName)

    private val street = SimpleStringProperty(person.primaryAddress.street)

    private val streetNumber = SimpleStringProperty(person.primaryAddress.streetNumber)

    private val zipCode = SimpleStringProperty(person.primaryAddress.zipCode)

    private val city = SimpleStringProperty(person.primaryAddress.city)

    private val country = SimpleStringProperty(person.primaryAddress.country)


    fun show() {
        show(messages["edit.person.window.title"])
    }


    override val root = vbox {
        prefWidth = 450.0

        paddingTop = 2.0
        paddingLeft = 2.0
        paddingRight = 2.0

        field("edit.person.window.first.name.label", firstName)

        field("edit.person.window.last.name.label", lastName)

        field("edit.person.window.street.label", street)

        field("edit.person.window.street.number.label", streetNumber)

        field("edit.person.window.zip.code.label", zipCode)

        field("edit.person.window.city.label", city)

        field("edit.person.window.country.label", country)

        anchorpane {
            prefHeight = ButtonsHeight

            button(messages["ok"]) {
                prefWidth = ButtonsWidth

                action { saveAndClose() }

                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = 0.0
                    bottomAnchor = 0.0
                }
            }

            button(messages["cancel"]) {
                prefWidth = ButtonsWidth

                action { askUserToSaveChangesAndClose() }

                anchorpaneConstraints {
                    topAnchor = 0.0
                    rightAnchor = ButtonsWidth + ButtonsHorizontalSpace
                    bottomAnchor = 0.0
                }
            }
        }

    }

    private fun EventTarget.field(labelResourceKey: String, value: SimpleStringProperty): Pane {
        return hbox {

            label(messages[labelResourceKey]) {
                prefWidth = LabelsWidth

                prefHeight = FieldHeight

                alignment = Pos.CENTER_LEFT
            }

            textfield(value) {
                prefHeight = FieldHeight

                hboxConstraints {
                    hGrow = Priority.ALWAYS
                }
            }

            vboxConstraints {
                marginBottom = 6.0
            }
        }
    }


    private fun saveAndClose() {
        person.firstName = firstName.value
        person.lastName = lastName.value

        person.primaryAddress.street = street.value
        person.primaryAddress.streetNumber = streetNumber.value
        person.primaryAddress.zipCode = zipCode.value
        person.primaryAddress.city = city.value
        person.primaryAddress.country = country.value

        personService.saveOrUpdate(person)

        closeWindow(true)
    }

    private fun askUserToSaveChangesAndClose() {
        // TODO: check for unsaved changes and ask user if he/she likes to save them

        closeWindow(false)
    }

    private fun closeWindow(didSavePerson: Boolean) {
        didUserSavePersonCallback?.invoke(didSavePerson)

        close()
    }

}