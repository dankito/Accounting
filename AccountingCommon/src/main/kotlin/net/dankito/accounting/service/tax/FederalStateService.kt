package net.dankito.accounting.service.tax

import net.dankito.accounting.data.dao.tax.IFederalStateDao
import net.dankito.accounting.data.model.tax.FederalState


open class FederalStateService(protected val dao: IFederalStateDao) : IFederalStateService {

    override fun getAll(): List<FederalState> {
        return dao.getAll()
    }


    override fun saveOrUpdate(federalState: FederalState) {
        dao.saveOrUpdate(federalState)
    }

    override fun saveOrUpdate(federalStates: List<FederalState>) {
        federalStates.forEach { federalState ->
            saveOrUpdate(federalState)
        }
    }


    override fun delete(federalState: FederalState) {
        dao.delete(federalState)
    }

    override fun delete(federalStates: List<FederalState>) {
        federalStates.forEach { federalState ->
            delete(federalState)
        }
    }

}