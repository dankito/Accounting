package net.dankito.accounting.data.model.timetracker


open class TrackedTimeUnit(val trackedTimeInSeconds: Int) {

    open val totalTrackedMinutes: Int
        get() = (trackedTimeInSeconds / 60.0).toInt()

    open val totalTrackedMinutesRounded: Int
        get() = Math.round(trackedTimeInSeconds / 60.0).toInt()

    val decimalHours: Double
        get() = trackedTimeInSeconds / 3600.0


    open val trackedTimeString: String
        get() = createTimeString(totalTrackedMinutes)

    open val trackedTimeRoundedString: String
        get() = createTimeString(totalTrackedMinutesRounded)

    open val decimalHoursString: String
        get() = getDecimalHoursString()


    protected open fun createTimeString(totalTrackedMinutes: Int): String {
        val hours = totalTrackedMinutes / 60
        val minutes = totalTrackedMinutes % 60

        return "$hours:%02d".format(minutes)
    }

    open fun getDecimalHoursString(countDecimalPlaces: Int = 2): String {
        return String.format("%.${countDecimalPlaces}f", decimalHours)
    }

}