package net.dankito.accounting.service.timetracker

import net.dankito.accounting.data.model.timetracker.TimeTrackerAccount
import net.dankito.accounting.data.model.timetracker.TrackedTimes
import kotlin.concurrent.thread


interface ITimeTrackerImporter {

    fun retrieveTrackedTimesAsync(account: TimeTrackerAccount, callback: (trackedTimes: TrackedTimes) -> Unit) {
        thread {
            callback(retrieveTrackedTimes(account))
        }
    }

    fun retrieveTrackedTimes(account: TimeTrackerAccount): TrackedTimes

}