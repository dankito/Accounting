package net.dankito.accounting.javafx.di

import dagger.Module
import dagger.Provides
import net.dankito.accounting.di.DaoModule
import net.dankito.accounting.javafx.presenter.*
import net.dankito.accounting.javafx.service.Router
import net.dankito.accounting.service.ValueAddedTaxCalculator
import net.dankito.accounting.service.address.IAddressService
import net.dankito.accounting.service.banking.IBankAccountService
import net.dankito.accounting.service.document.IDocumentService
import net.dankito.accounting.service.filter.ICollectionFilter
import net.dankito.accounting.service.filter.IFilterService
import net.dankito.accounting.service.invoice.IInvoiceService
import net.dankito.accounting.service.person.IPersonService
import net.dankito.accounting.service.settings.ISettingsService
import net.dankito.accounting.service.tax.IFederalStateService
import net.dankito.accounting.service.tax.ITaxOfficeService
import net.dankito.accounting.service.tax.elster.IElsterTaxDeclarationService
import net.dankito.accounting.service.timetracker.ITimeTrackerService
import net.dankito.utils.IThreadPool
import net.dankito.utils.events.IEventBus
import net.dankito.utils.javafx.os.JavaFxOsService
import java.io.File
import javax.inject.Named
import javax.inject.Singleton


@Module
class PresenterModule {

    @Provides
    @Singleton
    fun provideMainWindowPresenter(router: Router) : MainWindowPresenter {

        return MainWindowPresenter(router)
    }

    @Provides
    @Singleton
    fun provideOverviewPresenter(documentService: IDocumentService, settingsService: ISettingsService,
                                 bankAccountService: IBankAccountService, filterService: IFilterService,
                                 eventBus: IEventBus, router: Router, vatCalculator: ValueAddedTaxCalculator
    ) : OverviewPresenter {

        return OverviewPresenter(documentService, settingsService, bankAccountService, filterService, eventBus,
            router, vatCalculator)
    }

    @Provides
    @Singleton
    fun provideBankAccountsPresenter(accountService: IBankAccountService, collectionFilter: ICollectionFilter,
                                     router: Router) : BankAccountsPresenter {

        return BankAccountsPresenter(accountService, collectionFilter, router)
    }


    @Provides
    @Singleton
    fun provideEditPersonPresenter(personService: IPersonService, addressService: IAddressService) : EditPersonPresenter {

        return EditPersonPresenter(personService, addressService)
    }


    @Provides
    @Singleton
    fun provideCreateInvoicePresenter(invoiceService: IInvoiceService, vatCalculator: ValueAddedTaxCalculator,
                                      osService: JavaFxOsService) : CreateInvoicePresenter {

        return CreateInvoicePresenter(invoiceService, vatCalculator, osService)
    }


    @Provides
    @Singleton
    fun provideTimeTrackerAccountPresenter(timeTrackerService: ITimeTrackerService, router: Router, threadPool: IThreadPool)
            : TimeTrackerAccountPresenter {

        return TimeTrackerAccountPresenter(timeTrackerService, router, threadPool)
    }


    @Provides
    @Singleton
    fun provideElsterTaxPresenter(settingsService: IElsterTaxDeclarationService,
                                  personService: IPersonService,
                                  federalStateService: IFederalStateService,
                                  taxOfficeService: ITaxOfficeService,
                                  eventBus: IEventBus,
                                  router: Router,
                                  threadPool: IThreadPool,
                                  @Named(DaoModule.LogFilesFolderKey) logFilesFolder: File
    ) : ElsterTaxPresenter {

        return ElsterTaxPresenter(
            settingsService, personService, federalStateService, taxOfficeService, eventBus,
            router, threadPool, logFilesFolder
        )
    }

}