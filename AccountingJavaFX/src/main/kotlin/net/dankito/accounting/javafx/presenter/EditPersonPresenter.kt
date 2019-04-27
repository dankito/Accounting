package net.dankito.accounting.javafx.presenter

import net.dankito.accounting.data.model.Person
import net.dankito.accounting.service.address.IAddressService
import net.dankito.accounting.service.person.IPersonService


class EditPersonPresenter(private val personService: IPersonService, private val addressService: IAddressService) {

    fun saveOrUpdate(person: Person) {
        addressService.saveOrUpdate(person.address)

        personService.saveOrUpdate(person)
    }

}