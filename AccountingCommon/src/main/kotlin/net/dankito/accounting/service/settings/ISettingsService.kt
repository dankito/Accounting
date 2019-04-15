package net.dankito.accounting.service.settings

import net.dankito.accounting.data.model.settings.AppSettings


interface ISettingsService {

    val appSettings: AppSettings


    fun saveAppSettings()

}