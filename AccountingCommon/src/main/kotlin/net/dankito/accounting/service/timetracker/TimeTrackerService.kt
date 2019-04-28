package net.dankito.accounting.service.timetracker

import net.dankito.accounting.data.dao.timetracker.ITimeTrackerAccountDao
import net.dankito.accounting.data.model.timetracker.TimeTrackerAccount


open class TimeTrackerService(protected val accountDao: ITimeTrackerAccountDao) : ITimeTrackerService {

    override fun getAccounts(): List<TimeTrackerAccount> {
        return accountDao.getAll()
    }


    override fun saveOrUpdate(account: TimeTrackerAccount) {
        accountDao.saveOrUpdate(account)
    }

}