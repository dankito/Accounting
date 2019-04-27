package net.dankito.accounting.javafx.di

import dagger.Module
import dagger.Provides
import net.dankito.accounting.javafx.db.JavaCouchbaseLiteEntityManager
import net.dankito.jpa.entitymanager.EntityManagerConfiguration
import net.dankito.jpa.entitymanager.IEntityManager
import javax.inject.Singleton


@Module
class JavaCommonModule {

    @Provides
    @Singleton
    fun provideEntityManager(configuration: EntityManagerConfiguration) : IEntityManager {
        return JavaCouchbaseLiteEntityManager(configuration)
    }

}