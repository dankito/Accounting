package net.dankito.accounting.javafx.di

import dagger.Module
import dagger.Provides
import net.dankito.accounting.javafx.service.Router
import net.dankito.accounting.javafx.service.StyleService
import net.dankito.utils.javafx.os.JavaFxOsService
import javax.inject.Singleton


@Module
class JavaFxModule {

    @Provides
    @Singleton
    fun provideRouter() : Router {
        return Router()
    }

    @Provides
    @Singleton
    fun provideOsService() : JavaFxOsService {
        return JavaFxOsService()
    }

    @Provides
    @Singleton
    fun provideStyleService() : StyleService {
        return StyleService()
    }

}