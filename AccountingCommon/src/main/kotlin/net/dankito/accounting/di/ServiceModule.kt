package net.dankito.accounting.di

import dagger.Module
import dagger.Provides
import net.dankito.accounting.data.dao.*
import net.dankito.accounting.data.dao.banking.IBankAccountDao
import net.dankito.accounting.data.dao.banking.IBankAccountTransactionDao
import net.dankito.accounting.data.dao.banking.IBankAccountTransactionsDao
import net.dankito.accounting.data.dao.filter.IEntityFilterDao
import net.dankito.accounting.data.dao.filter.IFilterDao
import net.dankito.accounting.data.dao.invoice.ICreateInvoiceSettingsDao
import net.dankito.accounting.data.dao.tax.IFederalStateDao
import net.dankito.accounting.data.dao.tax.ITaxOfficeDao
import net.dankito.accounting.data.dao.timetracker.*
import net.dankito.accounting.service.address.AddressService
import net.dankito.accounting.service.address.IAddressService
import net.dankito.accounting.service.banking.BankAccountService
import net.dankito.accounting.service.banking.HbciBankingClient
import net.dankito.accounting.service.banking.IBankAccountService
import net.dankito.accounting.service.banking.IBankingClient
import net.dankito.accounting.service.document.DocumentService
import net.dankito.accounting.service.document.IDocumentService
import net.dankito.accounting.service.filter.CollectionFilter
import net.dankito.accounting.service.filter.FilterService
import net.dankito.accounting.service.filter.ICollectionFilter
import net.dankito.accounting.service.filter.IFilterService
import net.dankito.accounting.service.invoice.IInvoiceService
import net.dankito.accounting.service.invoice.InvoiceService
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
import net.dankito.accounting.service.timetracker.ITimeTrackerService
import net.dankito.accounting.service.timetracker.TimeTrackerService
import net.dankito.utils.events.IEventBus
import java.io.File
import javax.inject.Named
import javax.inject.Singleton


@Module
class ServiceModule {


    @Provides
    @Singleton
    fun provideSettingsService(dao: IAppSettingsDao, invoiceService: IInvoiceService,
                               elsterTaxDeclarationService: IElsterTaxDeclarationService)
            : ISettingsService {

        return SettingsService(dao, invoiceService, elsterTaxDeclarationService)
    }


    @Provides
    @Singleton
    fun provideDocumentService(dao: IDocumentDao, documentItemDao: IDocumentItemDao, eventBus: IEventBus) : IDocumentService {
        return DocumentService(dao, documentItemDao, eventBus)
    }


    @Provides
    @Singleton
    fun providePersonService(personDao: IPersonDao, companyDao: ICompanyDao, addressDao: IAddressDao) : IPersonService {
        return PersonService(personDao, companyDao, addressDao)
    }

    @Provides
    @Singleton
    fun provideAddressService(dao: IAddressDao) : IAddressService {
        return AddressService(dao)
    }


    @Provides
    @Singleton
    fun provideInvoiceService(dao: ICreateInvoiceSettingsDao, personService: IPersonService) : IInvoiceService {
        return InvoiceService(dao, personService)
    }

    @Provides
    @Singleton
    fun provideTimeTrackerService(accountDao: ITimeTrackerAccountDao, trackedTimesDao: ITrackedTimesDao,
                                  timeEntryDao: ITimeEntryDao, trackedDayDao: ITrackedDayDao,
                                  trackedMonthDao: ITrackedMonthDao, projectDao: IProjectDao, taskDao: ITaskDao)
            : ITimeTrackerService {

        return TimeTrackerService(accountDao, trackedTimesDao, timeEntryDao, trackedDayDao, trackedMonthDao,
            projectDao, taskDao)
    }


    @Provides
    @Singleton
    fun provideBankingClient(@Named(DaoModule.DataFolderKey) dataFolder: File) : IBankingClient {

        return HbciBankingClient(dataFolder)
    }

    @Provides
    @Singleton
    fun provideBankAccountService(bankingClient: IBankingClient, accountDao: IBankAccountDao,
                                  transactionsDao: IBankAccountTransactionsDao,
                                  transactionDao: IBankAccountTransactionDao, eventBus: IEventBus) : IBankAccountService {

        return BankAccountService(bankingClient, accountDao, transactionsDao, transactionDao, eventBus)
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


    @Provides
    @Singleton
    fun provideCollectionFilter() : ICollectionFilter {
        return CollectionFilter()
    }


    @Provides
    @Singleton
    fun provideFilterService(collectionFilter: ICollectionFilter, entityFilterDao: IEntityFilterDao,
                             filterDao: IFilterDao) : IFilterService {

        return FilterService(collectionFilter, entityFilterDao, filterDao)
    }

}