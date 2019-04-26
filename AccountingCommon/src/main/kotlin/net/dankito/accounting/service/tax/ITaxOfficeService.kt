package net.dankito.accounting.service.tax

import net.dankito.accounting.data.model.tax.TaxOffice


interface ITaxOfficeService {

    fun saveOrUpdate(taxOffice: TaxOffice)

    fun saveOrUpdate(taxOffices: List<TaxOffice>)

}