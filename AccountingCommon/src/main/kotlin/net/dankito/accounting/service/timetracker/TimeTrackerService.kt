package net.dankito.accounting.service.timetracker

import net.dankito.accounting.data.dao.timetracker.*
import net.dankito.accounting.data.model.timetracker.TimeTrackerAccount
import net.dankito.accounting.data.model.timetracker.TrackedTimes


open class TimeTrackerService(

    protected val accountDao: ITimeTrackerAccountDao,
    protected val trackedTimesDao: ITrackedTimesDao,
    protected val timeEntryDao: ITimeEntryDao,
    protected val trackedDayDao: ITrackedDayDao,
    protected val trackedMonthDao: ITrackedMonthDao,
    protected val projectDao: IProjectDao,
    protected val taskDao: ITaskDao

) : ITimeTrackerService {

    override fun getAccounts(): List<TimeTrackerAccount> {
        return accountDao.getAll()
    }


    override fun saveOrUpdate(account: TimeTrackerAccount) {
        account.trackedTimes?.let { saveOrUpdate(it) }

        accountDao.saveOrUpdate(account)
    }

    override fun saveOrUpdate(trackedTimes: TrackedTimes) {
        trackedTimesDao.saveOrUpdate(trackedTimes)
    }

}