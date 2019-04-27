package net.dankito.accounting.di

import dagger.Module
import dagger.Provides
import net.dankito.accounting.data.dao.*
import net.dankito.accounting.data.dao.invoice.CreateInvoiceSettingsDao
import net.dankito.accounting.data.dao.invoice.ICreateInvoiceSettingsDao
import net.dankito.accounting.data.dao.tax.FederalStateDao
import net.dankito.accounting.data.dao.tax.IFederalStateDao
import net.dankito.accounting.data.dao.tax.ITaxOfficeDao
import net.dankito.accounting.data.dao.tax.TaxOfficeDao
import net.dankito.jpa.entitymanager.EntityManagerConfiguration
import net.dankito.jpa.entitymanager.IEntityManager
import java.io.File
import javax.inject.Singleton


@Module
class DaoModule(private val dataFolder: File = File("data")) {

    @Provides
    @Singleton
    fun provideEntityManagerConfiguration() : EntityManagerConfiguration {
        return EntityManagerConfiguration(dataFolder.path, "accounting")
    }



    @Provides
    @Singleton
    fun provideAppSettingsDao(entityManager: IEntityManager) : IAppSettingsDao {
        return AppSettingsDao(entityManager)
    }


    @Provides
    @Singleton
    fun provideDocumentDao(entityManager: IEntityManager) : IDocumentDao {
        return DocumentDao(entityManager)
    }

    @Provides
    @Singleton
    fun provideDocumentItemDao(entityManager: IEntityManager) : IDocumentItemDao {
        return DocumentItemDao(entityManager)
    }


    @Provides
    @Singleton
    fun providePersonDao(entityManager: IEntityManager) : IPersonDao {
        return PersonDao(entityManager)
    }

    @Provides
    @Singleton
    fun provideCompanyDao(entityManager: IEntityManager) : ICompanyDao {
        return CompanyDao(entityManager)
    }

    @Provides
    @Singleton
    fun provideAddressDao(entityManager: IEntityManager) : IAddressDao {
        return AddressDao(entityManager)
    }


    @Provides
    @Singleton
    fun provideCreateInvoiceSettingsDao(entityManager: IEntityManager) : ICreateInvoiceSettingsDao {
        return CreateInvoiceSettingsDao(entityManager)
    }


    @Provides
    @Singleton
    fun provideFederalStateDao(entityManager: IEntityManager) : IFederalStateDao {
        return FederalStateDao(entityManager)
    }

    @Provides
    @Singleton
    fun provideTaxOfficeDao(entityManager: IEntityManager) : ITaxOfficeDao {
        return TaxOfficeDao(entityManager)
    }

    @Provides
    @Singleton
    fun provideElsterTaxDeclarationSettingsDao(entityManager: IEntityManager) : IElsterTaxDeclarationSettingsDao {
        return ElsterTaxDeclarationSettingsDao(entityManager)
    }

}