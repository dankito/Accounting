package net.dankito.accounting.service.timetracker.harvest

import ch.aaap.harvestclient.api.filter.TimeEntryFilter
import ch.aaap.harvestclient.core.Harvest
import ch.aaap.harvestclient.domain.reference.dto.ProjectReferenceDto
import net.dankito.accounting.data.model.timetracker.*
import net.dankito.accounting.service.timetracker.ITimeTrackerImporter
import net.dankito.accounting.service.timetracker.TimeEntriesGrouper


open class HarvestTimeTrackerImporter : ITimeTrackerImporter {

    companion object {
        private const val SecondsPerHour = 60 * 60
    }


    protected val timeEntriesGrouper = TimeEntriesGrouper()


    override fun retrieveTrackedTimes(account: TimeTrackerAccount): TrackedTimes {
        val config = HarvestConfigBuilder(account).createConfig()
        val harvest = Harvest(config)

        val harvestEntries = harvest.timesheets().list(TimeEntryFilter.emptyFilter())

        return mapEntries(harvestEntries)
    }

    protected open fun mapEntries(harvestEntries: MutableList<ch.aaap.harvestclient.domain.TimeEntry>): TrackedTimes {
        val tasksByHarvestTaskId = harvestEntries.mapNotNull { it.task }.associateBy({ it.id }, { Task(it.name) })
        val projectsByHarvestId = harvestEntries.mapNotNull { it.project as? ProjectReferenceDto }
            .associateBy({ it.id }, { Project(it.name) })

        val entries = mapEntries(harvestEntries, projectsByHarvestId, tasksByHarvestTaskId)

        val days = timeEntriesGrouper.groupByDays(entries)

        return TrackedTimes(
            entries, days, timeEntriesGrouper.groupByMonths(days),
            projectsByHarvestId.values.toList(),
            tasksByHarvestTaskId.values.toList()
        )
    }

    protected open fun mapEntries(harvestEntries: List<ch.aaap.harvestclient.domain.TimeEntry>,
                           projectsByHarvestId: Map<Long, Project>,
                           tasksByHarvestTaskId: Map<Long, Task>): List<TimeEntry> {

        return harvestEntries.map { entry ->
            TimeEntry(
                    entry.hours?.let { (it * SecondsPerHour).toInt() } ?: 0,
                    entry.spentDate,
                    entry.notes ?: "",
                    (entry.project as? ProjectReferenceDto)?.let { projectsByHarvestId[it.id] },
                    entry.task?.let { tasksByHarvestTaskId[it.id] }
            )
        }
    }

}