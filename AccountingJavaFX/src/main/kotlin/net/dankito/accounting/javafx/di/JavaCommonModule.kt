package net.dankito.accounting.javafx.di

import dagger.Module
import dagger.Provides
import net.dankito.accounting.javafx.db.JavaCouchbaseLiteEntityManager
import net.dankito.jpa.entitymanager.EntityManagerConfiguration
import net.dankito.jpa.entitymanager.IEntityManager
import net.dankito.text.extraction.ITextExtractorRegistry
import net.dankito.text.extraction.TextExtractorRegistry
import net.dankito.text.extraction.info.invoice.InvoiceDataExtractor
import net.dankito.text.extraction.pdf.OpenPdfPdfTextExtractor
import net.dankito.text.extraction.pdf.pdfToTextPdfTextExtractor
import javax.inject.Singleton


@Module
class JavaCommonModule {

    @Provides
    @Singleton
    fun provideEntityManager(configuration: EntityManagerConfiguration) : IEntityManager {
        return JavaCouchbaseLiteEntityManager(configuration)
    }

    @Provides
    @Singleton
    fun providePdfToTextPdfTextExtractor() : pdfToTextPdfTextExtractor {
        return pdfToTextPdfTextExtractor()
    }

    @Provides
    @Singleton
    fun provideOpenPdfPdfTextExtractor() : OpenPdfPdfTextExtractor {
        return OpenPdfPdfTextExtractor()
    }

    @Provides
    @Singleton
    fun provideTextExtractorRegistry(pdfToTextPdfTextExtractor: pdfToTextPdfTextExtractor, openPdfPdfTextExtractor: OpenPdfPdfTextExtractor) : ITextExtractorRegistry {
        return TextExtractorRegistry(listOf(pdfToTextPdfTextExtractor, openPdfPdfTextExtractor))
    }

    @Provides
    @Singleton
    fun provideInvoiceDataExtractor() : InvoiceDataExtractor {
        return InvoiceDataExtractor()
    }

}