package net.dankito.accounting.javafx.presenter

import net.dankito.accounting.data.model.person.NaturalOrLegalPerson
import net.dankito.accounting.data.model.person.PersonType
import net.dankito.accounting.javafx.service.Router
import net.dankito.accounting.service.person.IPersonService


class SelectPersonPresenter(
    private val personService: IPersonService,
    private val router: Router
) {

    fun getAll(): List<NaturalOrLegalPerson> {
        return personService.getAll()
    }

    fun getAllOfType(type: PersonType): List<NaturalOrLegalPerson> {
        return personService.getAllOfType(type)
    }


    fun showCreatePersonWindow(personType: PersonType, createdPersonCallback: (NaturalOrLegalPerson?) -> Unit) {
        showEditPersonWindow(null, personType) { _, editedPerson ->
            createdPersonCallback(editedPerson)
        }
    }

    fun showEditPersonWindow(person: NaturalOrLegalPerson, userDidEditPersonCallback: (Boolean, NaturalOrLegalPerson?) -> Unit) {
        showEditPersonWindow(person, person.type, userDidEditPersonCallback)
    }

    private fun showEditPersonWindow(person: NaturalOrLegalPerson?, personType: PersonType, userDidEditPersonCallback: (Boolean, NaturalOrLegalPerson?) -> Unit) {
        router.showEditPersonWindow(person, personType, userDidEditPersonCallback)
    }

}