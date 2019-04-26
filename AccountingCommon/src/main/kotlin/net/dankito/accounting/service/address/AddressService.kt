package net.dankito.accounting.service.address

import net.dankito.accounting.data.dao.IAddressDao
import net.dankito.accounting.data.model.Address


open class AddressService(protected val dao: IAddressDao) : IAddressService {

    override fun saveOrUpdate(address: Address) {
        dao.saveOrUpdate(address)
    }

    override fun delete(address: Address) {
        dao.delete(address)
    }

}