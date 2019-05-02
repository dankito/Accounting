package net.dankito.accounting.service.util.db

import net.dankito.jpa.couchbaselite.CouchbaseLiteEntityManagerBase
import net.dankito.jpa.entitymanager.EntityManagerConfiguration
import net.dankito.utils.io.FileUtils
import org.junit.After
import java.io.File


abstract class DatabaseBasedTest {

    protected val fileUtils = FileUtils()


    protected val dataFolder = File("testData")

    protected val entityManagerConfiguration = EntityManagerConfiguration(dataFolder.path, "accounting")

    protected val entityManager: CouchbaseLiteEntityManagerBase


    init {
        clearDataFolder()

        entityManager = JavaCouchbaseLiteEntityManager(entityManagerConfiguration)
    }

    @After
    open fun cleanCreatedDataAfterTest() {
        clearDataFolder()
    }


    protected open fun clearDataFolder() {
        fileUtils.deleteFolderRecursively(dataFolder)
    }

}