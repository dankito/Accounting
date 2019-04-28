package net.dankito.accounting.data.model.timetracker


open class TrackedTimes(val entries: List<TimeEntry>, val days: List<TrackedDay>, val months: List<TrackedMonth>,
                        val projects: List<Project>, val task: List<Task>)