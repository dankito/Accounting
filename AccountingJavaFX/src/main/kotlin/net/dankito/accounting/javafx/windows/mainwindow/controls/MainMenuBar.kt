package net.dankito.accounting.javafx.windows.mainwindow.controls

import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import tornadofx.*


class MainMenuBar : View() {

    override val root =
        menubar {
            minHeight = 30.0
            maxHeight = 30.0

            menu(messages["main.window.menu.file"]) {

//                separator()

                item(messages["main.window.menu.file.quit"], KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN)) {
                    action { primaryStage.close() }
                }
            }
        }

}