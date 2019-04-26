package net.dankito.accounting.service.tax

import net.dankito.accounting.data.dao.tax.ITaxOfficeDao
import net.dankito.accounting.data.model.tax.TaxOffice


open class TaxOfficeService(protected val dao: ITaxOfficeDao) : ITaxOfficeService {

    override fun saveOrUpdate(taxOffice: TaxOffice) {
        return dao.saveOrUpdate(taxOffice)
    }

    override fun saveOrUpdate(taxOffices: List<TaxOffice>) {
        taxOffices.forEach { taxOffice ->
            saveOrUpdate(taxOffice)
        }
    }

}