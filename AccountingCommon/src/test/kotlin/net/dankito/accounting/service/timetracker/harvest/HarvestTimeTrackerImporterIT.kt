package net.dankito.accounting.service.timetracker.harvest

import net.dankito.accounting.data.model.timetracker.TimeTrackerAccount
import net.dankito.accounting.data.model.timetracker.TimeTrackerType
import net.dankito.accounting.data.model.timetracker.TrackedTimes
import net.dankito.utils.datetime.asUtilDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.Ignore
import org.junit.Test
import java.time.LocalDate


@Ignore // not an automatic test, set your Harvest credentials in createTimeTrackerAccount()
class HarvestTimeTrackerImporterIT {

    private val underTest = HarvestTimeTrackerImporter()


    @Test
    fun retrieveTrackedTimes() {

        // when
        val trackedTimes = underTest.retrieveTrackedTimes(createTimeTrackerAccount())


        // then
        assertThat(trackedTimes).isNotNull

        trackedTimes?.let {
            assertThat(trackedTimes.entries).hasSize(672)
            assertThat(trackedTimes.days).hasSize(226)
            assertThat(trackedTimes.months).hasSize(13)
            assertThat(trackedTimes.projects).hasSize(15)
            assertThat(trackedTimes.tasks).hasSize(5)
        }
    }

    @Test
    fun retrieveTrackedTimes_SetRetrieved() {

        // given
        val account = createTimeTrackerAccount()

        val retrieved = LocalDate.now().minusMonths(1).asUtilDate()
        account.trackedTimes = TrackedTimes(listOf(), listOf(), listOf(), listOf(), listOf(), retrieved)


        // when
        val trackedTimes = underTest.retrieveTrackedTimes(account)


        // then
        assertThat(trackedTimes).isNotNull

        trackedTimes?.let {
            assertThat(trackedTimes.entries).isNotEmpty
            assertThat(trackedTimes.days).isNotEmpty
            assertThat(trackedTimes.months).hasSize(2)
            assertThat(trackedTimes.projects).isNotEmpty
            assertThat(trackedTimes.tasks).isNotEmpty
        }
    }


    private fun createTimeTrackerAccount(): TimeTrackerAccount {
        // set your credentials here
        return TimeTrackerAccount(TimeTrackerType.Harvest, "", "", "")
    }

}