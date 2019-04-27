package net.dankito.accounting.javafx.di

import dagger.Module
import dagger.Provides
import net.dankito.accounting.javafx.presenter.EditPersonPresenter
import net.dankito.accounting.javafx.presenter.ElsterTaxPresenter
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.accounting.javafx.service.Router
import net.dankito.accounting.service.ValueAddedTaxCalculator
import net.dankito.accounting.service.address.IAddressService
import net.dankito.accounting.service.document.IDocumentService
import net.dankito.accounting.service.person.IPersonService
import net.dankito.accounting.service.settings.ISettingsService
import net.dankito.accounting.service.tax.IFederalStateService
import net.dankito.accounting.service.tax.ITaxOfficeService
import net.dankito.accounting.service.tax.elster.IElsterTaxDeclarationService
import net.dankito.utils.IThreadPool
import javax.inject.Singleton


@Module
class PresenterModule {

    @Provides
    @Singleton
    fun provideOverviewPresenter(documentService: IDocumentService, settingsService: ISettingsService, router: Router,
                          vatCalculator: ValueAddedTaxCalculator
    ) : OverviewPresenter {

        return OverviewPresenter(documentService, settingsService, router, vatCalculator)
    }

    @Provides
    @Singleton
    fun provideEditPersonPresenter(personService: IPersonService, addressService: IAddressService) : EditPersonPresenter {

        return EditPersonPresenter(personService, addressService)
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