package net.dankito.accounting.javafx.windows.mainwindow.controls

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import net.dankito.accounting.javafx.di.AppComponent
import net.dankito.accounting.javafx.presenter.MainWindowPresenter
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.utils.javafx.ui.extensions.fixedHeight
import tornadofx.*
import javax.inject.Inject


class MainMenuBar : View() {

    @Inject
    protected lateinit var presenter: MainWindowPresenter

    @Inject
    protected lateinit var overviewPresenter: OverviewPresenter


    init {
        AppComponent.component.inject(this)
    }


    override val root =
        menubar {
            fixedHeight = 30.0

            menu(messages["main.window.menu.file"]) {

                menu(messages["main.window.menu.file.new"]) {

                    item(messages["main.window.menu.file.new.bank.account"]) {
                        action { presenter.showCreateBankAccountWindow() }
                    }

                    separator()

                    item(messages["main.window.menu.file.new.invoice"], KeyCodeCombination(KeyCode.I, KeyCombination.SHORTCUT_DOWN)) {
                        action { overviewPresenter.showCreateInvoiceWindow() }
                    }

                    item(messages["main.window.menu.file.new.revenue"], KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN)) {
                        action { overviewPresenter.showCreateRevenueWindow() }
                    }

                    item(messages["main.window.menu.file.new.expenditure"], KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN)) {
                        action { overviewPresenter.showCreateExpenditureWindow() }
                    }

                }

                separator()

                item(messages["main.window.menu.file.quit"], KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN)) {
                    action { primaryStage.close() }
                }
            }
        }

}