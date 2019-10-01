package net.dankito.accounting.javafx.windows.mainwindow.controls

import javafx.scene.layout.Priority
import net.dankito.accounting.javafx.di.AppComponent
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import tornadofx.*
import javax.inject.Inject


class OverviewTab : View() {

    companion object {
        private const val DocumentsOverviewSpace = 12.0
    }


    @Inject
    protected lateinit var presenter: OverviewPresenter


    init {
        AppComponent.component.inject(this)
    }


    override val root = hbox {

        vbox {
            hboxConstraints {
                hGrow = Priority.ALWAYS
            }

            add(CreatedInvoicesOverview(presenter).apply {
                root.minHeight = 80.0

            })

            add(RevenuesOverview(presenter))

            add(ExpendituresOverview(presenter).apply {
                root.vboxConstraints {
                    marginTop = DocumentsOverviewSpace
                }
            })
        }


        add(SummaryPane())
    }

}