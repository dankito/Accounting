package net.dankito.accounting.javafx.windows.mainwindow.controls

import javafx.geometry.Orientation
import javafx.scene.layout.Priority
import net.dankito.accounting.javafx.di.AppComponent
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import tornadofx.View
import tornadofx.hbox
import tornadofx.hboxConstraints
import tornadofx.splitpane
import javax.inject.Inject


class OverviewTab : View() {

    @Inject
    protected lateinit var presenter: OverviewPresenter


    init {
        AppComponent.component.inject(this)
    }


    override val root = hbox {

        splitpane(Orientation.VERTICAL) {
            hboxConstraints {
                hGrow = Priority.ALWAYS
            }

            add(CreatedInvoicesOverview(presenter))

            add(RevenuesOverview(presenter))

            add(ExpendituresOverview(presenter))

            setDividerPositions(0.33, 0.66)
        }


        add(SummaryPane())
    }

}