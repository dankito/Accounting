package net.dankito.accounting.javafx.di

import dagger.Module
import dagger.Provides
import net.dankito.accounting.javafx.service.Router
import javax.inject.Singleton


@Module
class JavaFxModule {

    @Provides
    @Singleton
    fun provideRouter() : Router {
        return Router()
    }

}