package net.dankito.accounting.javafx.windows.mainwindow

import javafx.scene.control.TabPane
import net.dankito.accounting.data.model.event.BankAccountAddedEvent
import net.dankito.accounting.javafx.di.AppComponent
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.accounting.javafx.windows.mainwindow.controls.BankAccountsTab
import net.dankito.accounting.javafx.windows.mainwindow.controls.MainMenuBar
import net.dankito.accounting.javafx.windows.mainwindow.controls.OverviewTab
import net.dankito.utils.PackageInfo
import net.dankito.utils.events.IEventBus
import tornadofx.*
import javax.inject.Inject


class MainWindow : Fragment(String.format(FX.messages["application.title"], PackageInfo.getAppVersionFromManifest())) {

    @Inject
    lateinit var overviewPresenter: OverviewPresenter

    @Inject
    lateinit var eventBus: IEventBus


    private lateinit var tabPane: TabPane


    init {
        AppComponent.component.inject(this)

        initLogic()
    }

    private fun initLogic() {
        if (overviewPresenter.isBankAccountAdded == false) { // only if no BankAccount is added yet as otherwise BankAccountsTab gets displayed directly
            eventBus.subscribe(BankAccountAddedEvent::class.java) {
                runLater {
                    tabPane.addBankAccountsTab()
                }
            }
        }
    }


    override val root = borderpane {
        prefHeight = 620.0
        prefWidth = 1150.0

        top = MainMenuBar().root

        center {
            tabPane = tabpane {
                useMaxWidth = true
                tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

                tab(messages["main.window.tab.overview.title"]) {
                    add(OverviewTab().root)
                }

                if (overviewPresenter.isBankAccountAdded) {
                    addBankAccountsTab()
                }

            }
        }

    }

    private fun TabPane.addBankAccountsTab() {
        tab(messages["main.window.tab.bank.accounts"]) {
            add(BankAccountsTab().root)
        }
    }

}