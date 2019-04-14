package net.dankito.accounting.javafx.windows.tax.elster

import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.VBox
import javafx.stage.FileChooser
import net.dankito.accounting.data.model.Person
import net.dankito.accounting.javafx.presenter.ElsterTaxPresenter
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.accounting.javafx.windows.tax.elster.controls.TaxNumberInput
import net.dankito.tax.elster.model.*
import net.dankito.utils.datetime.asLocalDate
import net.dankito.utils.io.FileUtils
import net.dankito.utils.javafx.ui.controls.currencyLabel
import net.dankito.utils.javafx.ui.controls.doubleTextfield
import net.dankito.utils.javafx.ui.controls.intTextfield
import net.dankito.utils.javafx.ui.dialogs.Window
import tornadofx.*
import java.io.File


class ElsterTaxDeclarationWindow(private val presenter: ElsterTaxPresenter,
                                 private val overviewPresenter: OverviewPresenter) : Window() {

    companion object {
        private const val VerticalSpaceBetweenSections = 6.0

        private const val HorizontalSpaceAfterLabel = 4.0

        private const val VatAmountsLabelsWidth = 400.0

        private const val TaxPayerLabelsWidth = 116.0

        private const val TaxPayerButtonsWidth = 110.0

        private const val CertificateTextfieldsHeight = 28.0

        private const val ElsterButtonsHeight = 34.0
        private const val ElsterButtonsWidth = 200.0
    }


    private val jahr = SimpleObjectProperty<Steuerjahr>()

    private val zeitraum = SimpleObjectProperty<Voranmeldungszeitraum>()


    private val revenuesWith19PercentVatNetAmount = SimpleIntegerProperty()

    private val revenuesWith7PercentVatNetAmount = SimpleIntegerProperty()

    private val receivedVatWith19Percent = SimpleDoubleProperty()
    private val receivedVatWith7Percent = SimpleDoubleProperty()

    private val spentVatWith19Percent = SimpleDoubleProperty()

    private val spentWith7Percent = SimpleDoubleProperty()

    private val vatBalance = SimpleDoubleProperty()


    private val taxpayer = SimpleObjectProperty<Person>()

    private val bundesland = SimpleObjectProperty<Bundesland>()

    private val finanzamt = SimpleObjectProperty<Finanzamt>()

    private val certificateFilePath = SimpleStringProperty("")
    private val isCertificateFileSet = SimpleBooleanProperty(false)
    private var lastSelectedCertificateFile: File? = null

    private val certificatePassword = SimpleStringProperty("")


    private val isATaxpayerSelected = SimpleBooleanProperty(false)

    private val allPersons = FXCollections.observableArrayList<Person>()


    private val finanzaemterForBundesland: MutableMap<Bundesland, List<Finanzamt>> = mutableMapOf()

    private val bundeslaender = FXCollections.observableArrayList<Bundesland>()

    private val finanzaemterForSelectedBundesland = FXCollections.observableArrayList<Finanzamt>()


    private var lastSelectedElsterXmlFile: File? = null

    private var taxNumberInput: TaxNumberInput = TaxNumberInput()


    private val areRequiredFieldsForElsterXmlProvided = SimpleBooleanProperty(false)

    private val areRequiredFieldsForElsterUploadProvided = SimpleBooleanProperty(false)


    init {
        initFields()
    }

    override fun onUndock() {
        presenter.close()

        super.onUndock()
    }


    override val root = vbox {

        prefWidth = 850.0
        paddingAll = 2.0


        hbox {
            alignment = Pos.CENTER_LEFT

            label(messages["year"])

            combobox<Steuerjahr>(jahr, Steuerjahr.values().asList()) {

                cellFormat { text = it.jahr }

                hboxConstraints {
                    marginLeft = HorizontalSpaceAfterLabel
                    marginRight = 24.0
                }
            }

            label(messages["period"])

            combobox<Voranmeldungszeitraum>(zeitraum, Voranmeldungszeitraum.values().asList()) {

                cellFormat { text = it.name.replace("_", ". ") } // replace() for I_Kalenderviertel etc.

                hboxConstraints {
                    marginLeft = HorizontalSpaceAfterLabel
                }
            }
        }

        netAmountField("elster.tax.declaration.window.revenues.with.19.percent.vat", revenuesWith19PercentVatNetAmount, receivedVatWith19Percent)

        netAmountField("elster.tax.declaration.window.revenues.with.7.percent.vat", revenuesWith7PercentVatNetAmount, receivedVatWith7Percent)

        vatAmountField("elster.tax.declaration.window.spent.vat.with.19.percent", spentVatWith19Percent)

        vatAmountField("elster.tax.declaration.window.spent.vat.with.7.percent", spentWith7Percent)

        vatAmountField("elster.tax.declaration.window.vat.balance", vatBalance, true) {
            vboxConstraints {
                marginBottom = 12.0
            }
        }

        hbox {
            alignment = Pos.CENTER_LEFT

            label(messages["taxpayer"]) {
                prefWidth = TaxPayerLabelsWidth
            }

            combobox<Person>(taxpayer, allPersons) {
                prefWidth = 300.0

                cellFormat { text = it.name }

                valueProperty().addListener { _, _, newValue -> isATaxpayerSelected.value = newValue != null }

                hboxConstraints {
                    marginRight = HorizontalSpaceAfterLabel
                }
            }

            button(messages["edit..."]) {
                prefWidth = TaxPayerButtonsWidth

                enableWhen(isATaxpayerSelected)

                action { editSelectedPerson() }
            }

            button(messages["new..."]) {
                prefWidth = TaxPayerButtonsWidth

                action { createNewPerson() }

                hboxConstraints {
                    marginLeft = 12.0
                }
            }

            vboxConstraints {
                marginTop = VerticalSpaceBetweenSections
            }
        }

        hbox {
            alignment = Pos.CENTER_LEFT

            label(messages["tax.office"]) {
                prefWidth = TaxPayerLabelsWidth
            }

            combobox(bundesland, bundeslaender) {
                prefWidth = 230.0

                cellFormat { text = it.name }

                valueProperty().addListener { _, _, newValue -> selectedBundeslandChanged(newValue) }

                hboxConstraints {
                    marginRight = 6.0
                }
            }

            combobox(finanzamt, finanzaemterForSelectedBundesland) {
                prefWidth = 300.0

                cellFormat { text = it.name }

            }

            vboxConstraints {
                marginTop = VerticalSpaceBetweenSections
            }
        }

        hbox {
            alignment = Pos.CENTER_LEFT

            label(messages["elster.tax.declaration.window.tax.number.label"]) {
                prefWidth = TaxPayerLabelsWidth
            }

            add(taxNumberInput)

            vboxConstraints {
                marginTop = VerticalSpaceBetweenSections
            }
        }

        hbox {
            alignment = Pos.CENTER_LEFT

            label(messages["elster.tax.declaration.window.certificate.file.label"]) {
                prefWidth = TaxPayerLabelsWidth
            }

            textfield(certificateFilePath) {
                prefHeight = CertificateTextfieldsHeight
                prefWidth = 400.0

                textProperty().addListener { _, _, newValue -> checkIfCertificateFileExists(newValue) }

                hboxConstraints {
                    marginRight = 6.0
                }
            }

            button(messages["..."]) {
                prefHeight = CertificateTextfieldsHeight
                prefWidth = 50.0

                action { selectCertificateFile() }
            }

            vboxConstraints {
                marginTop = VerticalSpaceBetweenSections
            }
        }

        hbox {
            alignment = Pos.CENTER_LEFT

            label(messages["elster.tax.declaration.window.certificate.password.label"]) {
                prefWidth = TaxPayerLabelsWidth
            }

            textfield(certificatePassword) {
                prefHeight = CertificateTextfieldsHeight
                prefWidth = 250.0
            }

            vboxConstraints {
                marginTop = VerticalSpaceBetweenSections
            }
        }

        hbox {
            alignment = Pos.CENTER_LEFT

            button(messages["elster.tax.declaration.window.create.elster.xml"]) {
                prefHeight = ElsterButtonsHeight
                prefWidth = ElsterButtonsWidth

                enableWhen(areRequiredFieldsForElsterXmlProvided)

                action { createUmsatzsteuerVoranmeldungXmlFile() }
            }

            button(messages["elster.tax.declaration.window.upload.to.elster"]) {
                prefHeight = ElsterButtonsHeight
                prefWidth = ElsterButtonsWidth

                enableWhen(areRequiredFieldsForElsterUploadProvided)

                action { makeUmsatzsteuerVoranmeldung() }
            }

            vboxConstraints {
                marginTop = VerticalSpaceBetweenSections
            }
        }

    }

    private fun EventTarget.vatAmountField(labelResourceKey: String, value: Property<Number>,
                                           allowNegativeNumbers: Boolean = false,
                                           additionalVatProperty: SimpleDoubleProperty? = null, op: HBox.() -> Unit = {}): Pane {

        return hbox {
            alignment = Pos.CENTER_LEFT

            label(messages[labelResourceKey]) {
                prefWidth = VatAmountsLabelsWidth
            }

            if (additionalVatProperty != null) {
                intTextfield(value, false) {
                    alignment = Pos.CENTER_RIGHT

                    prefWidth = 100.0

                    hboxConstraints {
                        marginLeft = HorizontalSpaceAfterLabel
                        marginRight = 4.0
                    }
                }
            }
            else {
                doubleTextfield(value, 2, allowNegativeNumbers) {
                    alignment = Pos.CENTER_RIGHT

                    prefWidth = 100.0

                    hboxConstraints {
                        marginLeft = HorizontalSpaceAfterLabel
                        marginRight = 4.0
                    }
                }
            }

            label(overviewPresenter.getUserCurrencySymbol())

            additionalVatProperty?.let {
                label(messages["elster.tax.declaration.window.vat.for.net.amount.hint"]) {
                    hboxConstraints {
                        marginLeft = 10.0
                        marginRight = 2.0
                    }
                }

                currencyLabel(additionalVatProperty)
            }

            vboxConstraints {
                marginTop = VerticalSpaceBetweenSections
            }

            op(this)
        }
    }

    private fun EventTarget.netAmountField(labelResourceKey: String, value: Property<Number>,
                                           additionalVatProperty: SimpleDoubleProperty, op: VBox.() -> Unit = {}): Pane {

        return vbox {
            vatAmountField(labelResourceKey, value, false, additionalVatProperty)

            label(messages["elster.tax.declaration.window.enter.net.amount.without.cent.hint"]) {
                vboxConstraints {
                    marginBottom = 4.0
                }
            }

            op(this)
        }
    }


    private fun initFields() {
        presenter.getAllFinanzaemterAsync { finanzaemter ->
            retrievedFinanzaemterOffUiThread(finanzaemter)
        }

        initYearAndPeriod()

        showVatAmountsForPeriod()

        showAvailablePersons()
        // TODO: select last selected taxpayer

        areRequiredFieldsForElsterXmlProvided.bind(isATaxpayerSelected.and(taxNumberInput.isEnteredTaxNumberValid))
        areRequiredFieldsForElsterUploadProvided.bind(areRequiredFieldsForElsterXmlProvided.and(isCertificateFileSet))
    }

    private fun initYearAndPeriod() {
        val previousPeriodEndDate = overviewPresenter.getPreviousAccountingPeriodEndDate()

        presenter.getYearFromPeriod(previousPeriodEndDate)?.let { year ->
            jahr.value = year
        }

        presenter.getVoranmeldungszeitrumFromPeriod(previousPeriodEndDate, overviewPresenter.accountingPeriod)?.let {
            zeitraum.value = it
        }

        jahr.addListener { _, _, _ -> showVatAmountsForPeriod() }
        zeitraum.addListener { _, _, _ -> showVatAmountsForPeriod() }
    }

    private fun showVatAmountsForPeriod() {
        val periodStart = presenter.getSelectedPeriodStartDate(jahr.value, zeitraum.value)
        val periodEnd = overviewPresenter.getAccountingPeriodEndDate(periodStart.asLocalDate()!!,
            presenter.getAccountingPeriodFromZeitraum(zeitraum.value))

        val revenuesInPeriod = overviewPresenter.getDocumentsInPeriod(overviewPresenter.getRevenues(), periodStart, periodEnd)
        val expendituresInPeriod = overviewPresenter.getDocumentsInPeriod(overviewPresenter.getExpenditures(), periodStart, periodEnd)

        val revenuesWith19PercentVatInPeriod = revenuesInPeriod.filter { it.valueAddedTaxRate == 19f }
        this.revenuesWith19PercentVatNetAmount.value = revenuesWith19PercentVatInPeriod.sumByDouble { it.netAmount }.toInt()
        this.receivedVatWith19Percent.value = revenuesWith19PercentVatInPeriod.sumByDouble { it.valueAddedTax }

        val revenuesWith7PercentVatInPeriod = revenuesInPeriod.filter { it.valueAddedTaxRate == 7f }
        this.revenuesWith7PercentVatNetAmount.value = revenuesWith7PercentVatInPeriod.sumByDouble { it.netAmount }.toInt()
        this.receivedVatWith7Percent.value = revenuesWith7PercentVatInPeriod.sumByDouble { it.valueAddedTax }

        this.spentVatWith19Percent.value = expendituresInPeriod.filter { it.valueAddedTaxRate == 19f }.sumByDouble { it.valueAddedTax }
        this.spentWith7Percent.value = expendituresInPeriod.filter { it.valueAddedTaxRate == 7f }.sumByDouble { it.valueAddedTax }

        this.vatBalance.value = receivedVatWith19Percent.value + receivedVatWith7Percent.value -
                (spentVatWith19Percent.value + spentWith7Percent.value)
    }

    private fun retrievedFinanzaemterOffUiThread(finanzaemter: Map<Bundesland, List<Finanzamt>>) {
        finanzaemterForBundesland.putAll(finanzaemter)

        runLater {
            bundeslaender.setAll(finanzaemterForBundesland.keys.sortedBy { it.name })

            if (bundeslaender.isNotEmpty() && bundesland.value == null) {
                bundesland.value = bundeslaender[0]
            }
        }
    }

    private fun createNewPerson() {
        presenter.showCreatePersonWindow { createdPerson ->
            createdPerson?.let {
                showAvailablePersons()

                taxpayer.value = createdPerson
            }
        }
    }

    private fun editSelectedPerson() {
        presenter.showEditPersonWindow(taxpayer.value) { didUserSavePerson ->
            if (didUserSavePerson) {
                showAvailablePersons()
            }
        }
    }

    private fun showAvailablePersons() {
        allPersons.setAll(presenter.getAllPersons())
    }

    private fun selectedBundeslandChanged(newValue: Bundesland) {
        finanzaemterForSelectedBundesland.setAll(finanzaemterForBundesland[newValue]?.sortedBy { it.name } ?: listOf())
        if (finanzaemterForSelectedBundesland.isNotEmpty()) {
            finanzamt.value = finanzaemterForSelectedBundesland[0]
        } else {
            finanzamt.value = null
        }
    }


    private fun checkIfCertificateFileExists(certificateFilePath: String) {
        isCertificateFileSet.value = File(certificateFilePath).exists()
    }

    private fun selectCertificateFile() {
        val fileChooser = FileChooser()

        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter(
            messages["elster.tax.declaration.window.certificate.file.extension.filter.description"], "*.pfx", "*.PFX"))

        val currentSelectedCertificate = File(certificateFilePath.value)
        if (currentSelectedCertificate.exists()) {
            fileChooser.initialDirectory = currentSelectedCertificate.parentFile
            fileChooser.initialFileName = currentSelectedCertificate.name
        }
        else if (lastSelectedCertificateFile != null) {
            fileChooser.initialDirectory = lastSelectedCertificateFile?.parentFile
            fileChooser.initialFileName = lastSelectedCertificateFile?.name
        }

        fileChooser.showOpenDialog(currentStage)?.let { selectedFile ->
            certificateFilePath.value = selectedFile.absolutePath
        }
    }


    private fun makeUmsatzsteuerVoranmeldung() {
        presenter.makeUmsatzsteuerVoranmeldung(createUmsatzsteuerVoranmeldungData())
    }

    private fun createUmsatzsteuerVoranmeldungXmlFile() {
        val fileChooser = FileChooser()

        fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter(
            messages["elster.tax.declaration.window.elster.xml.file.extension.filter.description"], "*.xml", "*.XML"))

        lastSelectedElsterXmlFile?.let { lastSelectedElsterXmlFile ->
            fileChooser.initialDirectory = lastSelectedElsterXmlFile.parentFile
            fileChooser.initialFileName = lastSelectedElsterXmlFile.name
        }

        fileChooser.showSaveDialog(currentStage)?.let { xmlOutputFile ->
            val xmlString = presenter.createUmsatzsteuerVoranmeldungXmlFile(createUmsatzsteuerVoranmeldungData())

            FileUtils().writeToTextFile(xmlString, xmlOutputFile)
        }
    }

    private fun createUmsatzsteuerVoranmeldungData(): UmsatzsteuerVoranmeldung {
        val taxNumber = taxNumberInput.taxNumber.value

        return UmsatzsteuerVoranmeldung(jahr.value, zeitraum.value, finanzamt.value, taxNumber,
            mapPersonToSteuerpflichtiger(taxpayer.value), File(certificateFilePath.value), certificatePassword.value,
            revenuesWith19PercentVatNetAmount.value, revenuesWith7PercentVatNetAmount.value,
            spentVatWith19Percent.value + spentWith7Percent.value, vatBalance.value)
    }

    private fun mapPersonToSteuerpflichtiger(person: Person): Steuerpflichtiger {
        val address = person.primaryAddress

        return Steuerpflichtiger(person.firstName, person.lastName, address.street, address.streetNumber,
            address.zipCode, address.city, address.country, "", "") // TODO: set telephone and e-mail
    }
}