package net.dankito.accounting.javafx.windows.mainwindow.controls

import javafx.scene.layout.Priority
import net.dankito.accounting.javafx.windows.mainwindow.OverviewPresenter
import tornadofx.*


class OverviewTab(presenter: OverviewPresenter) : View() {

    companion object {
        private const val DocumentsOverviewSpace = 12.0
    }


    override val root = hbox {

        vbox {
            hboxConstraints {
                hGrow = Priority.ALWAYS
            }

            add(RevenuesOverview(presenter))

            add(ExpendituresOverview(presenter).apply {
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