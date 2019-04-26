package net.dankito.accounting.javafx.windows.mainwindow.controls

import javafx.scene.layout.Priority
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.accounting.service.address.IAddressService
import net.dankito.accounting.service.person.IPersonService
import net.dankito.accounting.service.tax.IFederalStateService
import net.dankito.accounting.service.tax.ITaxOfficeService
import net.dankito.accounting.service.tax.elster.IElsterTaxDeclarationService
import net.dankito.utils.ThreadPool
import tornadofx.*


class OverviewTab(presenter: OverviewPresenter, personService: IPersonService, addressService: IAddressService,
                  elsterTaxDeclarationService: IElsterTaxDeclarationService, federalStatesService: IFederalStateService,
                  taxOfficeService: ITaxOfficeService, threadPool: ThreadPool) : View() {

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


        add(SummaryPane(presenter, personService, addressService, elsterTaxDeclarationService, federalStatesService,
            taxOfficeService, threadPool))
    }

}