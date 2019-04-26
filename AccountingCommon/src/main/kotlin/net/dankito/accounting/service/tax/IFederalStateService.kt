package net.dankito.accounting.service.tax

import net.dankito.accounting.data.model.tax.FederalState


interface IFederalStateService {

    fun getAll(): List<FederalState>


    fun saveOrUpdate(federalState: FederalState)

    fun saveOrUpdate(federalStates: List<FederalState>)


    fun delete(federalState: FederalState)

    fun delete(federalStates: List<FederalState>)

}