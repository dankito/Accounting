package net.dankito.accounting.service.timetracker

import net.dankito.accounting.data.model.timetracker.TimeTrackerAccount


interface ITimeTrackerService {

    fun getAccounts(): List<TimeTrackerAccount>

    fun saveOrUpdate(account: TimeTrackerAccount)

}