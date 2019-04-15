package net.dankito.accounting.data.dao

import net.dankito.accounting.data.model.settings.AppSettings
import java.io.File


// TODO: replace by a database based implementation
class JsonAppSettingsDao(dataFolder: File) : IAppSettingsDao, JsonBasedDao<AppSettings>(AppSettings::class.java, dataFolder, "settings.json")