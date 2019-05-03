package net.dankito.accounting.di

import dagger.Module
import dagger.Provides
import net.dankito.accounting.service.ValueAddedTaxCalculator
import net.dankito.utils.IThreadPool
import net.dankito.utils.ThreadPool
import net.dankito.utils.events.IEventBus
import net.dankito.utils.events.RxEventBus
import javax.inject.Singleton


@Module
class CommonUtilsModule {

    @Provides
    @Singleton
    fun provideEventBus() : IEventBus {
        return RxEventBus()
    }


    @Provides
    @Singleton
    fun provideThreadPool() : IThreadPool {
        return ThreadPool()
    }


    @Provides
    @Singleton
    fun provideValueAddedTaxCalculator() : ValueAddedTaxCalculator {
        return ValueAddedTaxCalculator()
    }

}