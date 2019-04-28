package net.dankito.accounting.javafx.presenter

import net.dankito.accounting.data.model.timetracker.TimeTrackerAccount
import net.dankito.accounting.data.model.timetracker.TimeTrackerType
import net.dankito.accounting.data.model.timetracker.TrackedTimes
import net.dankito.accounting.javafx.service.Router
import net.dankito.accounting.service.timetracker.ITimeTrackerService
import net.dankito.accounting.service.timetracker.harvest.HarvestTimeTrackerImporter


class TimeTrackerAccountPresenter(private val timeTrackerService: ITimeTrackerService, private val router: Router) {

    fun getAllTimeTrackerAccounts(): List<TimeTrackerAccount> {
        return timeTrackerService.getAccounts()
    }


    fun saveOrUpdate(account: TimeTrackerAccount) {
        timeTrackerService.saveOrUpdate(account)
    }


    fun showCreateTimeTrackerAccountWindow(createdTimeTrackerAccountCallback: (TimeTrackerAccount?) -> Unit) {
        val newTimeTrackerAccount = TimeTrackerAccount()

        showEditTimeTrackerAccountWindow(newTimeTrackerAccount) { userDidSaveTimeTrackerAccount ->
            createdTimeTrackerAccountCallback( if (userDidSaveTimeTrackerAccount) newTimeTrackerAccount else null )
        }
    }

    fun showEditTimeTrackerAccountWindow(account: TimeTrackerAccount, userDidEditTimeTrackerAccountCallback: (Boolean) -> Unit) {
        router.showEditTimeTrackerAccountWindow(account, userDidEditTimeTrackerAccountCallback)
    }


    fun importTimeTrackerDataAsync(account: TimeTrackerAccount, callback: (trackedTimes: TrackedTimes) -> Unit) {
        when (account.type) {
            TimeTrackerType.Harvest -> importTimeTrackerDataFromHarvestAsync(account, callback)
        }
    }

    private fun importTimeTrackerDataFromHarvestAsync(account: TimeTrackerAccount, callback: (trackedTimes: TrackedTimes) -> Unit) {
        val harvestImporter = HarvestTimeTrackerImporter()
        harvestImporter.retrieveTrackedTimesAsync(account, callback)
    }

}