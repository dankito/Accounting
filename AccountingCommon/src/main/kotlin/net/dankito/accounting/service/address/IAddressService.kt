package net.dankito.accounting.service.address

import net.dankito.accounting.data.model.Address


interface IAddressService {

    fun saveOrUpdate(address: Address)

    fun delete(address: Address)

}