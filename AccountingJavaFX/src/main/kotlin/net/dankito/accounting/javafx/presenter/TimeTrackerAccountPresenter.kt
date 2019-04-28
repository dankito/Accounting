package net.dankito.accounting.javafx.presenter

import net.dankito.accounting.data.model.timetracker.TimeTrackerAccount
import net.dankito.accounting.data.model.timetracker.TimeTrackerType
import net.dankito.accounting.data.model.timetracker.TrackedTimes
import net.dankito.accounting.javafx.service.Router
import net.dankito.accounting.service.timetracker.ITimeTrackerImporter
import net.dankito.accounting.service.timetracker.ITimeTrackerService
import net.dankito.accounting.service.timetracker.harvest.HarvestTimeTrackerImporter
import net.dankito.utils.IThreadPool


class TimeTrackerAccountPresenter(private val timeTrackerService: ITimeTrackerService, private val router: Router,
                                  private val threadPool: IThreadPool) {

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
        val importer: ITimeTrackerImporter = when (account.type) {
            TimeTrackerType.Harvest -> HarvestTimeTrackerImporter()
        }

        importer.retrieveTrackedTimesAsync(account) { trackedTimes ->
            saveTrackedTimes(account, trackedTimes)

            callback(trackedTimes)
        }
    }

    private fun saveTrackedTimes(account: TimeTrackerAccount, trackedTimes: TrackedTimes) {
        account.trackedTimes = trackedTimes

        threadPool.runAsync {
            timeTrackerService.saveOrUpdate(account)
        }
    }

}