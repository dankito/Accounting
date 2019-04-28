package net.dankito.accounting.service.timetracker

import net.dankito.accounting.data.model.timetracker.TimeTrackerAccount
import net.dankito.accounting.data.model.timetracker.TrackedTimes


interface ITimeTrackerService {

    fun getAccounts(): List<TimeTrackerAccount>

    fun saveOrUpdate(account: TimeTrackerAccount)

    fun saveOrUpdate(trackedTimes: TrackedTimes)

}