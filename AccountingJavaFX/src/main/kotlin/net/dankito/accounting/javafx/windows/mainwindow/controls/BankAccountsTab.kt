package net.dankito.accounting.javafx.windows.mainwindow.controls

import javafx.scene.layout.Priority
import net.dankito.accounting.data.model.event.BankAccountTransactionsUpdatedEvent
import net.dankito.accounting.data.model.event.UpdatingBankAccountTransactionsEvent
import net.dankito.accounting.javafx.di.AppComponent
import net.dankito.accounting.javafx.presenter.BankAccountsPresenter
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.banking.ui.javafx.controls.AccountTransactionsView
import net.dankito.banking.ui.presenter.BankingPresenter
import net.dankito.utils.events.IEventBus
import tornadofx.View
import tornadofx.runLater
import tornadofx.vbox
import tornadofx.vboxConstraints
import java.text.DateFormat
import javax.inject.Inject


class BankAccountsTab : View() {

    companion object {

        private val ValueDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM)

    }


    @Inject
    protected lateinit var oldPresenter: BankAccountsPresenter

    @Inject
    protected lateinit var presenter: BankingPresenter

    @Inject
    protected lateinit var overviewPresenter: OverviewPresenter

    @Inject
    protected lateinit var eventBus: IEventBus


    init {
        AppComponent.component.inject(this)

        eventBus.subscribe(UpdatingBankAccountTransactionsEvent::class.java) {
            runLater {
                // TODO
            }
        }

        eventBus.subscribe(BankAccountTransactionsUpdatedEvent::class.java) {
            runLater {
                // TODO
            }
        }
    }


    override val root = vbox {
        add(AccountTransactionsView(presenter).apply {
            vboxConstraints {
                vGrow = Priority.ALWAYS
            }
        })

    }

}