package net.dankito.accounting.javafx.windows.mainwindow

import javafx.scene.control.TabPane
import net.dankito.accounting.data.dao.JsonDocumentDao
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.accounting.javafx.service.Router
import net.dankito.accounting.javafx.windows.mainwindow.controls.MainMenuBar
import net.dankito.accounting.javafx.windows.mainwindow.controls.OverviewTab
import net.dankito.accounting.service.document.DocumentService
import net.dankito.utils.PackageInfo
import net.dankito.utils.ThreadPool
import tornadofx.*
import java.io.File


class MainWindow : Fragment(String.format(FX.messages["application.title"], PackageInfo.getAppVersionFromManifest())) {


    private val dataFolder = File("data")

    private val router = Router(this)


    private val documentsService = DocumentService(JsonDocumentDao(dataFolder))

    private val overviewPresenter = OverviewPresenter(documentsService, router)


    private val threadPool = ThreadPool()


    init {

    }


    override val root = borderpane {
        prefHeight = 620.0
        prefWidth = 1150.0

        top = MainMenuBar().root

        center {
            tabpane {
                useMaxWidth = true
                tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

                tab(messages["main.window.tab.overview.title"]) {
                    add(OverviewTab(overviewPresenter).root)
                }
            }
        }

    }

}