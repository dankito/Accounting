package net.dankito.accounting.javafx.windows.mainwindow.controls

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import net.dankito.accounting.javafx.di.AppComponent
import net.dankito.accounting.javafx.presenter.MainWindowPresenter
import tornadofx.*
import javax.inject.Inject


class MainMenuBar : View() {

    @Inject
    protected lateinit var presenter: MainWindowPresenter


    init {
        AppComponent.component.inject(this)
    }


    override val root =
        menubar {
            minHeight = 30.0
            maxHeight = 30.0

            menu(messages["main.window.menu.file"]) {

                menu(messages["main.window.menu.file.add"]) {

                    item(messages["main.window.menu.file.add.bank.account"]) {
                        action { presenter.showCreateBankAccountWindow() }
                    }

                }

                separator()

                item(messages["main.window.menu.file.quit"], KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN)) {
                    action { primaryStage.close() }
                }
            }
        }

}