package net.dankito.accounting.service.timetracker.harvest

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import net.dankito.accounting.data.model.timetracker.TimeTrackerAccount


class HarvestConfigBuilder(val accountId: String,
                           val token: String,
                           val userAgent: String = "Harvest Client",
                           val baseUrl: String = "https://api.harvestapp.com/v2/",
                           val maxRequestPerWindow: Int = 95,
                           val windowSizeSeconds: Int = 15,
                           val timezonesPath: String = "/timezones.txt",
                           val currenciesPath: String = "/currencies.txt") {

    constructor(account: TimeTrackerAccount) : this(account.username, account.password)


    fun createConfig(): Config {
        // this is kind of bad as it uses application internal knowledge for the key names - which can any time change.
        // but this application.conf bugs me really so there's no way to configure harvest client at run time via UI.

        val values = mapOf(
                "harvest" to mapOf(
                        "auth" to mapOf(
                                "accountId" to accountId,
                                "token" to token
                        ),
                        "userAgent" to userAgent,
                        "baseUrl" to baseUrl,
                        "max_request_per_window" to maxRequestPerWindow,
                        "window_size_seconds" to windowSizeSeconds,
                        "timezones_path" to timezonesPath,
                        "currencies_path" to currenciesPath
                )
        )

        return ConfigFactory.parseMap(values, "harvest")
    }
}