package net.dankito.accounting.javafx.windows.mainwindow

import javafx.scene.control.TabPane
import net.dankito.accounting.data.dao.*
import net.dankito.accounting.data.dao.tax.FederalStateDao
import net.dankito.accounting.data.dao.tax.TaxOfficeDao
import net.dankito.accounting.data.db.JavaCouchbaseLiteEntityManager
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.accounting.javafx.service.Router
import net.dankito.accounting.javafx.windows.mainwindow.controls.MainMenuBar
import net.dankito.accounting.javafx.windows.mainwindow.controls.OverviewTab
import net.dankito.accounting.service.address.AddressService
import net.dankito.accounting.service.document.DocumentService
import net.dankito.accounting.service.person.PersonService
import net.dankito.accounting.service.settings.SettingsService
import net.dankito.accounting.service.tax.FederalStateService
import net.dankito.accounting.service.tax.TaxOfficeService
import net.dankito.accounting.service.tax.elster.ElsterTaxDeclarationService
import net.dankito.jpa.entitymanager.EntityManagerConfiguration
import net.dankito.utils.PackageInfo
import net.dankito.utils.ThreadPool
import tornadofx.*
import java.io.File


class MainWindow : Fragment(String.format(FX.messages["application.title"], PackageInfo.getAppVersionFromManifest())) {


    private val dataFolder = File("data")

    private val router = Router(this)


    private val entityManagerConfiguration = EntityManagerConfiguration(dataFolder.path, "accounting")

    private val entityManager = JavaCouchbaseLiteEntityManager(entityManagerConfiguration)


    private val documentsService = DocumentService(DocumentDao(entityManager))

    private val addressService = AddressService(AddressDao(entityManager))

    private val personService = PersonService(PersonDao(entityManager))

    private val federalStatesService = FederalStateService(FederalStateDao(entityManager))

    private val taxOfficeService = TaxOfficeService(TaxOfficeDao(entityManager))

    private val elsterTaxDeclarationService = ElsterTaxDeclarationService(ElsterTaxDeclarationSettingsDao(entityManager))

    private val settingsService = SettingsService(AppSettingsDao(entityManager), elsterTaxDeclarationService)


    private val threadPool = ThreadPool()


    private val overviewPresenter = OverviewPresenter(documentsService, settingsService, router)


    override val root = borderpane {
        prefHeight = 620.0
        prefWidth = 1150.0

        top = MainMenuBar().root

        center {
            tabpane {
                useMaxWidth = true
                tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

                tab(messages["main.window.tab.overview.title"]) {
                    add(OverviewTab(overviewPresenter, personService, addressService, elsterTaxDeclarationService,
                        federalStatesService, taxOfficeService, threadPool).root)
                }
            }
        }

    }

}