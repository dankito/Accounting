package net.dankito.accounting.javafx.presenter

import net.dankito.accounting.data.model.Person
import net.dankito.accounting.service.address.AddressService
import net.dankito.accounting.service.person.IPersonService


class EditPersonPresenter(private val personService: IPersonService, private val addressService: AddressService) {

    fun saveOrUpdate(person: Person) {
        addressService.saveOrUpdate(person.primaryAddress)

        personService.saveOrUpdate(person)
    }

}