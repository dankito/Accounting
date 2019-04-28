package net.dankito.accounting.javafx.di

import dagger.Module
import dagger.Provides
import net.dankito.accounting.javafx.presenter.*
import net.dankito.accounting.javafx.service.Router
import net.dankito.accounting.service.ValueAddedTaxCalculator
import net.dankito.accounting.service.address.IAddressService
import net.dankito.accounting.service.banking.IBankAccountService
import net.dankito.accounting.service.document.IDocumentService
import net.dankito.accounting.service.invoice.IInvoiceService
import net.dankito.accounting.service.person.IPersonService
import net.dankito.accounting.service.settings.ISettingsService
import net.dankito.accounting.service.tax.IFederalStateService
import net.dankito.accounting.service.tax.ITaxOfficeService
import net.dankito.accounting.service.tax.elster.IElsterTaxDeclarationService
import net.dankito.accounting.service.timetracker.ITimeTrackerService
import net.dankito.utils.IThreadPool
import net.dankito.utils.javafx.os.JavaFxOsService
import javax.inject.Singleton


@Module
class PresenterModule {

    @Provides
    @Singleton
    fun provideOverviewPresenter(documentService: IDocumentService, settingsService: ISettingsService,
                                 bankAccountService: IBankAccountService, router: Router,
                                 vatCalculator: ValueAddedTaxCalculator
    ) : OverviewPresenter {

        return OverviewPresenter(documentService, settingsService, bankAccountService, router, vatCalculator)
    }

    @Provides
    @Singleton
    fun provideEditPersonPresenter(personService: IPersonService, addressService: IAddressService) : EditPersonPresenter {

        return EditPersonPresenter(personService, addressService)
    }


    @Provides
    @Singleton
    fun provideCreateInvoicePresenter(invoiceService: IInvoiceService, osService: JavaFxOsService) : CreateInvoicePresenter {

        return CreateInvoicePresenter(invoiceService, osService)
    }


    @Provides
    @Singleton
    fun provideTimeTrackerAccountPresenter(timeTrackerService: ITimeTrackerService, router: Router)
            : TimeTrackerAccountPresenter {

        return TimeTrackerAccountPresenter(timeTrackerService, router)
    }


    @Provides
    @Singleton
    fun provideElsterTaxPresenter(settingsService: IElsterTaxDeclarationService,
                                  personService: IPersonService,
                                  federalStateService: IFederalStateService,
                                  taxOfficeService: ITaxOfficeService,
                                  router: Router,
                                  threadPool: IThreadPool
    ) : ElsterTaxPresenter {

        return ElsterTaxPresenter(
            settingsService, personService, federalStateService, taxOfficeService, router, threadPool)
    }

}