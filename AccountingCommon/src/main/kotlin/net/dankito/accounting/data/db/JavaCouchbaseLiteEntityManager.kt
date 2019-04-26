package net.dankito.accounting.data.db

import com.couchbase.lite.Context
import net.dankito.jpa.couchbaselite.CouchbaseLiteEntityManagerBase
import net.dankito.jpa.entitymanager.EntityManagerConfiguration
import java.io.File


class JavaCouchbaseLiteEntityManager(configuration: EntityManagerConfiguration)
    : CouchbaseLiteEntityManagerBase(AccountingJavaContext(configuration.dataFolder)) {


    init {
        open(configuration)
    }


    override fun adjustDatabasePath(context: Context, configuration: EntityManagerConfiguration): String {
        // TODO: implement this in a better way as this uses implementation internal details
        return File(context.filesDir, configuration.databaseName + ".cblite2").absolutePath
    }

}
