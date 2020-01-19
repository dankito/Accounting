package net.dankito.accounting.javafx.presenter

import net.dankito.accounting.data.model.person.NaturalOrLegalPerson
import net.dankito.accounting.service.person.IPersonService


class EditPersonPresenter(private val personService: IPersonService) {

    fun saveOrUpdate(person: NaturalOrLegalPerson) {
        personService.saveOrUpdate(person)
    }

}