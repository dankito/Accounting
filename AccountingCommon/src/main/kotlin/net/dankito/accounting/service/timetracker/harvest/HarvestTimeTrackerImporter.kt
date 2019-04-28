package net.dankito.accounting.service.timetracker.harvest

import ch.aaap.harvestclient.api.filter.TimeEntryFilter
import ch.aaap.harvestclient.core.Harvest
import ch.aaap.harvestclient.domain.reference.dto.ProjectReferenceDto
import net.dankito.accounting.data.model.timetracker.*
import net.dankito.accounting.service.timetracker.ITimeTrackerImporter
import org.slf4j.LoggerFactory


open class HarvestTimeTrackerImporter : ITimeTrackerImporter {

    companion object {
        private const val SecondsPerHour = 60 * 60

        private val log = LoggerFactory.getLogger(HarvestTimeTrackerImporter::class.java)
    }


    override fun retrieveTrackedTimes(account: TimeTrackerAccount): TrackedTimes? {
        try {
            val config = HarvestConfigBuilder(account).createConfig()
            val harvest = Harvest(config)

            val harvestEntries = harvest.timesheets().list(TimeEntryFilter.emptyFilter())

            return mapEntries(harvestEntries)
        } catch (e: Exception) {
            log.error("Could not retrieve tracked times", e)
        }

        return null
    }

    protected open fun mapEntries(harvestEntries: List<ch.aaap.harvestclient.domain.TimeEntry>): TrackedTimes {
        val tasksByHarvestTaskId = harvestEntries.mapNotNull { it.task }.associateBy({ it.id }, { Task(it.name) })
        val projectsByHarvestId = harvestEntries.mapNotNull { it.project as? ProjectReferenceDto }
            .associateBy({ it.id }, { Project(it.name) })

        val entries = mapEntries(harvestEntries, projectsByHarvestId, tasksByHarvestTaskId)

        val days = groupByDays(entries)
        val months = groupByMonths(days)

        val projects = groupProjectEntries(projectsByHarvestId)
        val tasks = groupTaskEntries(tasksByHarvestTaskId)

        return TrackedTimes(entries, days, months, projects, tasks)
    }

    protected open fun mapEntries(harvestEntries: List<ch.aaap.harvestclient.domain.TimeEntry>,
                           projectsByHarvestId: Map<Long, Project>,
                           tasksByHarvestTaskId: Map<Long, Task>): List<TimeEntry> {

        return harvestEntries.map { entry ->
            mapToTimeEntry(entry, projectsByHarvestId, tasksByHarvestTaskId)
        }
    }

    protected open fun mapToTimeEntry(harvestEntry: ch.aaap.harvestclient.domain.TimeEntry,
                          projectsByHarvestId: Map<Long, Project>, tasksByHarvestTaskId: Map<Long, Task>): TimeEntry {

        val project = (harvestEntry.project as? ProjectReferenceDto)?.let { projectsByHarvestId[it.id] }
        val task = harvestEntry.task?.let { tasksByHarvestTaskId[it.id] }

        val entry = TimeEntry(
            harvestEntry.hours?.let { (it * SecondsPerHour).toInt() } ?: 0,
            TimeTrackerDate(harvestEntry.spentDate.year, harvestEntry.spentDate.monthValue, harvestEntry.spentDate.dayOfMonth),
            harvestEntry.notes ?: "",
            project,
            task
        )

        project?.let { it.addEntry(entry) }
        task?.let { it.addEntry(entry) }

        return entry
    }

    protected open fun groupProjectEntries(projectsByHarvestId: Map<Long, Project>): List<Project> {
        val projects = projectsByHarvestId.values.toList()

        projects.forEach { project ->
            project.trackedDays = groupByDays(project.trackedTimeEntries)
            project.trackedMonths = groupByMonths(project.trackedDays)
        }
        return projects
    }

    protected open fun groupTaskEntries(tasksByHarvestTaskId: Map<Long, Task>): List<Task> {
        val tasks = tasksByHarvestTaskId.values.toList()

        tasks.forEach { task ->
            task.trackedDays = groupByDays(task.trackedTimeEntries)
            task.trackedMonths = groupByMonths(task.trackedDays)
        }
        return tasks
    }

    protected open fun groupByDays(entries: List<TimeEntry>): List<TrackedDay> {
        return entries
            .groupBy( { it.date }, { it } )
            .map { TrackedHarvestDay(it.key, it.value) }
    }

    protected open fun groupByMonths(days: List<TrackedDay>): List<TrackedMonth> {
        return days
            .groupBy( { it.date.atFirstDayOfMonth() }, { it } )
            .map { TrackedMonth(it.key, it.value) }
    }

}