package net.dankito.accounting.di

import dagger.Module
import dagger.Provides
import net.dankito.accounting.data.dao.*
import net.dankito.accounting.data.dao.tax.IFederalStateDao
import net.dankito.accounting.data.dao.tax.ITaxOfficeDao
import net.dankito.accounting.service.address.AddressService
import net.dankito.accounting.service.address.IAddressService
import net.dankito.accounting.service.document.DocumentService
import net.dankito.accounting.service.document.IDocumentService
import net.dankito.accounting.service.person.IPersonService
import net.dankito.accounting.service.person.PersonService
import net.dankito.accounting.service.settings.ISettingsService
import net.dankito.accounting.service.settings.SettingsService
import net.dankito.accounting.service.tax.FederalStateService
import net.dankito.accounting.service.tax.IFederalStateService
import net.dankito.accounting.service.tax.ITaxOfficeService
import net.dankito.accounting.service.tax.TaxOfficeService
import net.dankito.accounting.service.tax.elster.ElsterTaxDeclarationService
import net.dankito.accounting.service.tax.elster.IElsterTaxDeclarationService
import javax.inject.Singleton


@Module
class ServiceModule {


    @Provides
    @Singleton
    fun provideSettingsService(dao: IAppSettingsDao, elsterTaxDeclarationService: IElsterTaxDeclarationService)
            : ISettingsService {

        return SettingsService(dao,elsterTaxDeclarationService)
    }


    @Provides
    @Singleton
    fun provideDocumentService(dao: IDocumentDao) : IDocumentService {
        return DocumentService(dao)
    }


    @Provides
    @Singleton
    fun providePersonService(dao: IPersonDao) : IPersonService {
        return PersonService(dao)
    }

    @Provides
    @Singleton
    fun provideAddressService(dao: IAddressDao) : IAddressService {
        return AddressService(dao)
    }


    @Provides
    @Singleton
    fun provideFederalStateService(dao: IFederalStateDao) : IFederalStateService {
        return FederalStateService(dao)
    }

    @Provides
    @Singleton
    fun provideTaxOfficeService(dao: ITaxOfficeDao) : ITaxOfficeService {
        return TaxOfficeService(dao)
    }

    @Provides
    @Singleton
    fun provideElsterTaxDeclarationService(dao: IElsterTaxDeclarationSettingsDao) : IElsterTaxDeclarationService {
        return ElsterTaxDeclarationService(dao)
    }

}