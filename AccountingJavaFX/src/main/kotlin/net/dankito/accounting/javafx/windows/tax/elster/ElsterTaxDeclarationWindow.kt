package net.dankito.accounting.javafx.windows.tax.elster

import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.Label
import javafx.scene.layout.*
import javafx.stage.FileChooser
import javafx.stage.Screen
import javafx.stage.Stage
import net.dankito.accounting.data.model.Person
import net.dankito.accounting.javafx.presenter.ElsterTaxPresenter
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.accounting.javafx.windows.tax.elster.controls.TaxNumberInput
import net.dankito.tax.elster.model.*
import net.dankito.tax.elster.test.TestFinanzamt
import net.dankito.tax.elster.test.TestHerstellerID
import net.dankito.tax.elster.test.Teststeuernummern
import net.dankito.utils.datetime.asLocalDate
import net.dankito.utils.io.FileUtils
import net.dankito.utils.javafx.ui.controls.currencyLabel
import net.dankito.utils.javafx.ui.controls.doubleTextfield
import net.dankito.utils.javafx.ui.controls.intTextfield
import net.dankito.utils.javafx.ui.dialogs.Window
import net.dankito.utils.javafx.ui.extensions.addStyleToCurrentStyle
import net.dankito.utils.javafx.util.FXUtils
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


    private val taxpayer = SimpleObjectProperty<Person>(overviewPresenter.settings.elsterTaxDeclarationSettings.taxpayer)

    private val bundesland = SimpleObjectProperty<Bundesland>(overviewPresenter.settings.elsterTaxDeclarationSettings.bundesland)

    private val finanzamt = SimpleObjectProperty<Finanzamt>(overviewPresenter.settings.elsterTaxDeclarationSettings.finanzamt)

    private var lastSelectedCertificateFile: File? = overviewPresenter.settings.elsterTaxDeclarationSettings.certificateFile
    private val certificateFilePath = SimpleStringProperty(lastSelectedCertificateFile?.absolutePath ?: "")
    private val isCertificateFileSet = SimpleBooleanProperty(checkIfCertificateFileExists(certificateFilePath.value))

    private val certificatePassword = SimpleStringProperty(overviewPresenter.settings.elsterTaxDeclarationSettings.certificatePassword)

    // TODO: remove again
    private val herstellerID = SimpleStringProperty(TestHerstellerID.T_74931.herstellerID)


    private val isATaxpayerSelected = SimpleBooleanProperty(taxpayer.value != null) // TODO: also check if all required Person fields are set

    private val allPersons = FXCollections.observableArrayList<Person>()


    private val finanzaemterForBundesland: MutableMap<Bundesland, List<Finanzamt>> = mutableMapOf()

    private val bundeslaender = FXCollections.observableArrayList<Bundesland>()

    private val finanzaemterForSelectedBundesland = FXCollections.observableArrayList<Finanzamt>()


    private var lastSelectedElsterXmlFile: File? = overviewPresenter.settings.elsterTaxDeclarationSettings.lastSelectedElsterXmlFile

    private var taxNumberInput: TaxNumberInput = TaxNumberInput(overviewPresenter.settings.elsterTaxDeclarationSettings.taxNumber)


    private val areRequiredFieldsForElsterXmlProvided = SimpleBooleanProperty(false)

    private val areRequiredFieldsForElsterUploadProvided = SimpleBooleanProperty(false)


    init {
        initFields()
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

                textProperty().addListener { _, _, newValue -> updateIsCertificateFileSet(newValue) }

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

            passwordfield(certificatePassword) {
                prefHeight = CertificateTextfieldsHeight
                prefWidth = 250.0
            }

            vboxConstraints {
                marginTop = VerticalSpaceBetweenSections
            }
        }

        // TODO: remove again
        label("Wir haben noch keine HerstellerID, deshalb bitte diese manuell eintragen.\n" +
                "Falls die TestHerstellerID (74931) verwendet wird, wird der TestMarker gesetzt, als\n" +
                "Steuernummer '198/113/10010' und als Finanzamt 'Bayern 9198' verwendet") {
            minHeight = 60.0
            maxWidth = this@vbox.prefWidth - 100.0

            vboxConstraints {
                marginTop = VerticalSpaceBetweenSections
            }
        }

        hbox {
            alignment = Pos.CENTER_LEFT

            label("HerstellerID") {
                prefWidth = TaxPayerLabelsWidth
            }

            textfield(herstellerID) {
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

                hboxConstraints {
                    marginLeft = 12.0
                }
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

                    isDisable = true
                    addStyleToCurrentStyle("-fx-opacity: 1;")

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

                    isDisable = true
                    addStyleToCurrentStyle("-fx-opacity: 1;")

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


    private fun updateIsCertificateFileSet(certificateFilePath: String) {
        isCertificateFileSet.value = checkIfCertificateFileExists(certificateFilePath)
    }

    private fun checkIfCertificateFileExists(certificateFilePath: String): Boolean {
        return File(certificateFilePath).exists()
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
            lastSelectedCertificateFile = selectedFile
        }
    }


    private fun makeUmsatzsteuerVoranmeldung() {
        updateSettings()

        val result = presenter.makeUmsatzsteuerVoranmeldung(createUmsatzsteuerVoranmeldungData())

        if (result.successful) {
            var message = messages["elster.tax.declaration.window.upload.to.elster.success.alert.message"]

            if (result.userInfo.isNotEmpty()) {
                message += System.lineSeparator() + System.lineSeparator() + messages["elster.tax.declaration.window.upload.to.elster.success.elster.info.alert.message"]
                result.userInfo.forEach { message += System.lineSeparator() + System.lineSeparator() + it }
            }

            createDialog(Alert.AlertType.INFORMATION, message,
                messages["elster.tax.declaration.window.upload.to.elster.success.alert.title"],
                currentStage, ButtonType.OK).show()
        }
        else {
            var message = messages["elster.tax.declaration.window.upload.to.elster.error.alert.message"]

            if (result.errors.isNotEmpty()) {
                message += System.lineSeparator() + System.lineSeparator() + messages["elster.tax.declaration.window.upload.to.elster.error.errors.alert.message"]
                result.errors.forEach { message += System.lineSeparator() + System.lineSeparator() + it.message +
                String.format(messages["elster.tax.declaration.window.upload.to.elster.error.error.code.alert.message"], it.errorCode)}
            }

            createDialog(Alert.AlertType.ERROR, message,
                messages["elster.tax.declaration.window.upload.to.elster.error.alert.title"],
                currentStage, ButtonType.OK).show()
        }
    }

    private fun createUmsatzsteuerVoranmeldungXmlFile() {
        updateSettings()

        val fileChooser = FileChooser()

        fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter(
            messages["elster.tax.declaration.window.elster.xml.file.extension.filter.description"], "*.xml", "*.XML"))

        lastSelectedElsterXmlFile?.let { lastSelectedElsterXmlFile ->
            fileChooser.initialDirectory = lastSelectedElsterXmlFile.parentFile
            fileChooser.initialFileName = lastSelectedElsterXmlFile.name
        }

        fileChooser.showSaveDialog(currentStage)?.let { xmlOutputFile ->
            val result = presenter.createUmsatzsteuerVoranmeldungXmlFile(createUmsatzsteuerVoranmeldungData())

            if (result.successful) {
                FileUtils().writeToTextFile(result.xmlString, xmlOutputFile)

                createDialog(Alert.AlertType.INFORMATION,
                    String.format(messages["elster.tax.declaration.window.elster.xml.file.success.alert.message"], xmlOutputFile),
                    messages["elster.tax.declaration.window.elster.xml.file.success.alert.title"],
                    currentStage, ButtonType.OK).show()
            }
            else {
                var message = messages["elster.tax.declaration.window.elster.xml.file.error.alert.message"]
                result.errors.forEach { message += System.lineSeparator() + System.lineSeparator() + it.message }

                createDialog(Alert.AlertType.INFORMATION, message,
                    messages["elster.tax.declaration.window.elster.xml.file.error.alert.title"],
                    currentStage, ButtonType.OK).show()
            }
        }
    }

    private fun createUmsatzsteuerVoranmeldungData(): UmsatzsteuerVoranmeldung {
        // TODO: remove again
        val useTestValues = herstellerID.value == TestHerstellerID.T_74931.herstellerID

        val taxNumber = if (useTestValues) Teststeuernummern.T_198_113_10010.steuernummer else taxNumberInput.taxNumber.value
        val finanzamt = if (useTestValues) TestFinanzamt.Bayern_9198.finanzamt else finanzamt.value
        val herstellerID = herstellerID.value

        return UmsatzsteuerVoranmeldung(jahr.value, zeitraum.value, finanzamt, taxNumber,
            mapPersonToSteuerpflichtiger(taxpayer.value), File(certificateFilePath.value), certificatePassword.value,
            revenuesWith19PercentVatNetAmount.value, revenuesWith7PercentVatNetAmount.value,
            spentVatWith19Percent.value + spentWith7Percent.value, vatBalance.value, herstellerID)
    }

    private fun mapPersonToSteuerpflichtiger(person: Person): Steuerpflichtiger {
        val address = person.primaryAddress

        return Steuerpflichtiger(person.firstName, person.lastName, address.street, address.streetNumber,
            address.zipCode, address.city, address.country, null, null) // TODO: set telephone and e-mail
    }


    private fun updateSettings() {
        overviewPresenter.settings.elsterTaxDeclarationSettings.apply {
            taxpayer = this@ElsterTaxDeclarationWindow.taxpayer.value
            finanzamt = this@ElsterTaxDeclarationWindow.finanzamt.value
            bundesland = this@ElsterTaxDeclarationWindow.bundesland.value
            taxNumber = taxNumberInput.taxNumber.value
            certificateFile = File(certificateFilePath.value)
            certificatePassword = this@ElsterTaxDeclarationWindow.certificatePassword.value
            lastSelectedElsterXmlFile = this@ElsterTaxDeclarationWindow.lastSelectedElsterXmlFile
        }

        overviewPresenter.saveAppSettings()
    }



    // TODO: use DialogService as soon as JavaFxUtils 2.0.0 is out
    private fun createDialog(alertType: Alert.AlertType, message: CharSequence, alertTitle: CharSequence?, owner: Stage?, vararg buttons: ButtonType): Alert {
        val alert = Alert(alertType)

        (alertTitle as? String)?.let { alert.title = it }

        owner?.let { alert.initOwner(it) }

        (message as? String)?.let { setAlertContent(alert, it) }
        alert.headerText = null

        alert.buttonTypes.setAll(*buttons)

        return alert
    }

    private fun setAlertContent(alert: Alert, content: String) {
        var maxWidth = Screen.getPrimary().visualBounds.width

        if(alert.owner != null) {
            FXUtils.getScreenWindowLeftUpperCornerIsIn(alert.owner)?.let { ownersScreen ->
                maxWidth = ownersScreen.visualBounds.width
            }
        }

        maxWidth *= 0.6 // set max width to 60 % of Screen width

        val contentLabel = Label(content)
        contentLabel.isWrapText = true
        contentLabel.prefHeight = Region.USE_COMPUTED_SIZE
        contentLabel.maxHeight = FXUtils.SizeMaxValue
        contentLabel.maxWidth = maxWidth

        val contentPane = VBox(contentLabel)
        contentPane.prefHeight = Region.USE_COMPUTED_SIZE
        contentPane.maxHeight = FXUtils.SizeMaxValue
        VBox.setVgrow(contentLabel, Priority.ALWAYS)

        alert.dialogPane.prefHeight = Region.USE_COMPUTED_SIZE
        alert.dialogPane.maxHeight = FXUtils.SizeMaxValue
        alert.dialogPane.maxWidth = maxWidth
        alert.dialogPane.content = contentPane
    }

}