package net.dankito.accounting.javafx.presenter

import net.dankito.accounting.data.model.Person
import net.dankito.accounting.javafx.service.Router
import net.dankito.accounting.service.person.IPersonService


class SelectPersonPresenter(
    private val personService: IPersonService,
    private val router: Router
) {

    fun getAllPersons(): List<Person> {
        return personService.getAllPersons()
    }


    fun showCreatePersonWindow(createdPersonCallback: (Person?) -> Unit) {
        val newPerson = Person()

        showEditPersonWindow(newPerson) { userDidSavePerson ->
            createdPersonCallback( if (userDidSavePerson) newPerson else null )
        }
    }

    fun showEditPersonWindow(person: Person, userDidEditPersonCallback: (Boolean) -> Unit) {
        router.showEditPersonWindow(person, userDidEditPersonCallback)
    }

}