package net.dankito.accounting.di

import dagger.Module
import dagger.Provides
import net.dankito.accounting.service.ValueAddedTaxCalculator
import net.dankito.utils.IThreadPool
import net.dankito.utils.ThreadPool
import net.dankito.utils.events.IEventBus
import net.dankito.utils.events.IRxEventBus
import net.dankito.utils.events.RxEventBus
import net.dankito.utils.web.client.IWebClient
import net.dankito.utils.web.client.OkHttpWebClient
import javax.inject.Singleton


@Module
class CommonUtilsModule {

    @Provides
    @Singleton
    fun provideRxEventBus() : IRxEventBus {
        return RxEventBus()
    }

    @Provides
    @Singleton
    fun provideEventBus(rxEventBus: IRxEventBus) : IEventBus {
        return rxEventBus
    }


    @Provides
    @Singleton
    fun provideWebClient() : IWebClient {
        return OkHttpWebClient()
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