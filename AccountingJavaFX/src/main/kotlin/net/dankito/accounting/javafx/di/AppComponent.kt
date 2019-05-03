package net.dankito.accounting.javafx.di

import dagger.Component
import net.dankito.accounting.di.CommonComponent
import net.dankito.accounting.di.CommonUtilsModule
import net.dankito.accounting.di.DaoModule
import net.dankito.accounting.di.ServiceModule
import net.dankito.accounting.javafx.windows.banking.BankAccountTransactionDetailsWindow
import net.dankito.accounting.javafx.windows.banking.EditAutomaticAccountTransactionImportWindow
import net.dankito.accounting.javafx.windows.banking.EditBankAccountWindow
import net.dankito.accounting.javafx.windows.invoice.CreateInvoiceWindow
import net.dankito.accounting.javafx.windows.mainwindow.MainWindow
import net.dankito.accounting.javafx.windows.mainwindow.controls.*
import net.dankito.accounting.javafx.windows.person.EditPersonWindow
import net.dankito.accounting.javafx.windows.tax.elster.ElsterTaxDeclarationWindow
import net.dankito.accounting.javafx.windows.timetracker.EditTimeTrackerAccountWindow
import javax.inject.Singleton


@Singleton
@Component(modules = [
    JavaFxModule::class, PresenterModule::class, JavaCommonModule::class,
    ServiceModule::class, DaoModule::class, CommonUtilsModule::class
])
interface AppComponent : CommonComponent {

    companion object {
        lateinit var component: AppComponent
    }


    fun inject(mainWindow: MainWindow)

    fun inject(mainMenuBar: MainMenuBar)

    fun inject(overviewTab: OverviewTab)

    fun inject(documentsOverview: DocumentsOverview)

    fun inject(summaryPane: SummaryPane)

    fun inject(bankAccountsTab: BankAccountsTab)


    fun inject(editPersonWindow: EditPersonWindow)

    fun inject(createInvoiceWindow: CreateInvoiceWindow)

    fun inject(editTimeTrackerAccountWindow: EditTimeTrackerAccountWindow)


    fun inject(editBankAccountWindow: EditBankAccountWindow)

    fun inject(editAutomaticAccountTransactionImportWindow: EditAutomaticAccountTransactionImportWindow)

    fun inject(bankAccountTransactionDetailsWindow: BankAccountTransactionDetailsWindow)


    fun inject(elsterTaxDeclarationWindow: ElsterTaxDeclarationWindow)

}