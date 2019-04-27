package net.dankito.accounting.di

import dagger.Component
import javax.inject.Singleton


@Singleton
@Component(modules = [ ServiceModule::class, DaoModule::class, CommonUtilsModule::class ])
interface CommonComponent {

    companion object {
        lateinit var component: CommonComponent
    }

}