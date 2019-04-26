package net.dankito.accounting.javafx.presenter

import net.dankito.accounting.data.model.Person
import net.dankito.accounting.service.person.IPersonService


class EditPersonPresenter(private val personService: IPersonService) {

    fun saveOrUpdate(person: Person) {
        personService.saveOrUpdate(person)
    }

}