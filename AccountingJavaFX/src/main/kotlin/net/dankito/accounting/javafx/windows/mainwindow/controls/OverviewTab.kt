package net.dankito.accounting.javafx.windows.mainwindow.controls

import javafx.scene.layout.Priority
import tornadofx.*


class OverviewTab : View() {

    companion object {
        private const val DocumentsOverviewSpace = 12.0
    }


    override val root = hbox {

        vbox {
            hboxConstraints {
                hGrow = Priority.ALWAYS
            }

            add(DocumentsOverview("main.window.tab.overview.revenues.label"))

            add(DocumentsOverview("main.window.tab.overview.expenditures.label").apply {
                root.vboxConstraints {
                    marginTop = DocumentsOverviewSpace
                }
            })
        }


        vbox {
            minWidth = 200.0
        }
    }

}