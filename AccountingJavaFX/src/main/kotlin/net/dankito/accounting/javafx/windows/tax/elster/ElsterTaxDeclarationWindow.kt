package net.dankito.accounting.javafx.windows.tax.elster

import javafx.beans.property.*
import javafx.collections.FXCollections
import javafx.event.EventTarget
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.Label
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.*
import javafx.stage.FileChooser
import javafx.stage.Screen
import javafx.stage.Stage
import net.dankito.accounting.data.model.AccountingPeriod
import net.dankito.accounting.data.model.Person
import net.dankito.accounting.data.model.event.DocumentsUpdatedEvent
import net.dankito.accounting.data.model.tax.FederalState
import net.dankito.accounting.data.model.tax.TaxOffice
import net.dankito.accounting.javafx.controls.SelectPersonView
import net.dankito.accounting.javafx.di.AppComponent
import net.dankito.accounting.javafx.presenter.ElsterTaxPresenter
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.accounting.javafx.presenter.SelectPersonPresenter
import net.dankito.accounting.javafx.windows.tax.elster.controls.TaxNumberInput
import net.dankito.tax.elster.model.*
import net.dankito.tax.elster.test.TestFinanzamt
import net.dankito.tax.elster.test.TestHerstellerID
import net.dankito.tax.elster.test.Teststeuernummern
import net.dankito.utils.datetime.asLocalDate
import net.dankito.utils.datetime.asUtilDate
import net.dankito.utils.events.IEventBus
import net.dankito.utils.events.ISubscribedEvent
import net.dankito.utils.io.FileUtils
import net.dankito.utils.javafx.ui.controls.currencyLabel
import net.dankito.utils.javafx.ui.controls.doubleTextfield
import net.dankito.utils.javafx.ui.controls.intTextfield
import net.dankito.utils.javafx.ui.dialogs.Window
import net.dankito.utils.javafx.ui.extensions.addStyleToCurrentStyle
import net.dankito.utils.javafx.ui.extensions.ensureOnlyUsesSpaceIfVisible
import net.dankito.utils.javafx.util.FXUtils
import tornadofx.*
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import javax.inject.Inject


class ElsterTaxDeclarationWindow(private val overviewPresenter: OverviewPresenter) : Window() {

    companion object {

        private val UploadedFilesFolder = File("ElsterUpload")

        private val OutputFilesDateTimeFormat = SimpleDateFormat("yyyy.MM.dd_HH-mm-ss")


        private const val VerticalSpaceBetweenSections = 6.0

        private const val HorizontalSpaceAfterLabel = 4.0

        private const val VatAmountsLabelsWidth = 400.0

        private const val TaxPayerLabelsWidth = 116.0

        private const val CertificateAndElsterXmlFileLabelsWidth = 130.0
        private const val CertificateAndElsterXmlFileTextfieldsHeight = 28.0
        private const val CertificateAndElsterXmlFileTextfieldsWidth = 450.0

        private const val SelectFileButtonWidth = 50.0

        private const val ElsterButtonsHeight = 34.0
        private const val ElsterButtonsWidth = 200.0

    }


    @Inject
    protected lateinit var presenter: ElsterTaxPresenter

    @Inject
    protected lateinit var selectPersonPresenter: SelectPersonPresenter

    @Inject
    protected lateinit var eventBus: IEventBus


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

    private val federalState = SimpleObjectProperty<FederalState>()

    private val taxOffice = SimpleObjectProperty<TaxOffice>()

    private val elsterXmlFilePath = SimpleStringProperty()

    private val certificateFilePath = SimpleStringProperty()
    private val isCertificateFileSet = SimpleBooleanProperty(false)

    private val certificatePassword = SimpleStringProperty()


    private val isATaxpayerSelected = SimpleBooleanProperty(taxpayer.value != null) // TODO: also check if all required Person fields are set


    private val federalStates = FXCollections.observableArrayList<FederalState>()

    private val taxOfficesForSelectedFederalState = FXCollections.observableArrayList<TaxOffice>()


    private var taxNumberInput: TaxNumberInput = TaxNumberInput()

    private val isUploadToElsterSelected = SimpleBooleanProperty(false)


    private val areRequiredFieldsForElsterXmlProvided = SimpleBooleanProperty(false)

    private val areRequiredFieldsForElsterUploadProvided = SimpleBooleanProperty(false)


    private val subscribedEvent: ISubscribedEvent


    init {
        AppComponent.component.inject(this)

        isUploadToElsterSelected.addListener { _, _, newValue -> currentStage?.sizeToScene() }

        subscribedEvent = eventBus.subscribe(DocumentsUpdatedEvent::class.java) {
            showVatAmountsForPeriod()
        }
    }

    override fun beforeShow(dialogStage: Stage) {
        super.beforeShow(dialogStage)

        dialogStage.setOnCloseRequest {
            subscribedEvent.unsubscribe() // to avoid memory leaks
        }
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

            add(SelectPersonView(selectPersonPresenter, taxpayer))

            vboxConstraints {
                marginTop = VerticalSpaceBetweenSections
            }
        }

        hbox {
            alignment = Pos.CENTER_LEFT

            label(messages["tax.office"]) {
                prefWidth = TaxPayerLabelsWidth
            }

            combobox(federalState, federalStates) {
                prefWidth = 230.0

                cellFormat { text = it.name }

                valueProperty().addListener { _, _, newValue -> selectedFederalStateChanged(newValue) }

                hboxConstraints {
                    marginRight = 6.0
                }
            }

            combobox(taxOffice, taxOfficesForSelectedFederalState) {
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
            vboxConstraints {
                marginTop = 18.0
                marginBottom = 6.0
            }
            val toggleGroup = ToggleGroup()

            radiobutton(messages["elster.tax.declaration.window.create.elster.xml"], toggleGroup) {
                isSelected = !!! isUploadToElsterSelected.value
            }

            radiobutton(messages["elster.tax.declaration.window.upload.to.elster"], toggleGroup) {
                selectedProperty().bindBidirectional(isUploadToElsterSelected)

                hboxConstraints {
                    marginLeft = 12.0
                }
            }
        }

        vbox {
            hiddenWhen(isUploadToElsterSelected)
            ensureOnlyUsesSpaceIfVisible()

            hbox {
                alignment = Pos.CENTER_LEFT

                label(messages["elster.tax.declaration.window.elster.xml.file.label"]) {
                    prefWidth = CertificateAndElsterXmlFileLabelsWidth
                }

                textfield(elsterXmlFilePath) {
                    prefHeight = CertificateAndElsterXmlFileTextfieldsHeight
                    prefWidth = CertificateAndElsterXmlFileTextfieldsWidth

                    hboxConstraints {
                        marginRight = 6.0
                    }
                }

                button(messages["..."]) {
                    prefHeight = CertificateAndElsterXmlFileTextfieldsHeight
                    prefWidth = SelectFileButtonWidth

                    action { selectElsterXmlFile() }
                }

                vboxConstraints {
                    marginTop = VerticalSpaceBetweenSections
                }
            }
        }

        vbox {
            visibleWhen(isUploadToElsterSelected)
            ensureOnlyUsesSpaceIfVisible()

            hbox {
                alignment = Pos.CENTER_LEFT

                label(messages["elster.tax.declaration.window.certificate.file.label"]) {
                    prefWidth = CertificateAndElsterXmlFileLabelsWidth
                }

                textfield(certificateFilePath) {
                    prefHeight = CertificateAndElsterXmlFileTextfieldsHeight
                    prefWidth = CertificateAndElsterXmlFileTextfieldsWidth

                    textProperty().addListener { _, _, _ -> updateIsCertificateFileSet() }

                    hboxConstraints {
                        marginRight = 6.0
                    }
                }

                button(messages["..."]) {
                    prefHeight = CertificateAndElsterXmlFileTextfieldsHeight
                    prefWidth = SelectFileButtonWidth

                    action { selectCertificateFile() }
                }

                vboxConstraints {
                    marginTop = VerticalSpaceBetweenSections
                }
            }

            hbox {
                alignment = Pos.CENTER_LEFT

                label(messages["elster.tax.declaration.window.certificate.password.label"]) {
                    prefWidth = CertificateAndElsterXmlFileLabelsWidth
                }

                passwordfield(certificatePassword) {
                    prefHeight = CertificateAndElsterXmlFileTextfieldsHeight
                    prefWidth = 250.0
                }

                vboxConstraints {
                    marginTop = VerticalSpaceBetweenSections
                }
            }
        }

        hbox {
            alignment = Pos.CENTER_LEFT

            button(messages["elster.tax.declaration.window.create.elster.xml"]) {
                prefHeight = ElsterButtonsHeight
                prefWidth = ElsterButtonsWidth

                enableWhen(areRequiredFieldsForElsterXmlProvided)

                hiddenWhen(isUploadToElsterSelected)
                ensureOnlyUsesSpaceIfVisible()

                action { createUmsatzsteuerVoranmeldungXmlFile() }
            }

            button(messages["elster.tax.declaration.window.upload.to.elster"]) {
                prefHeight = ElsterButtonsHeight
                prefWidth = ElsterButtonsWidth

                enableWhen(areRequiredFieldsForElsterUploadProvided)

                visibleWhen(isUploadToElsterSelected)
                ensureOnlyUsesSpaceIfVisible()

                action { makeUmsatzsteuerVoranmeldung() }
            }

            vboxConstraints {
                marginTop = VerticalSpaceBetweenSections
            }
        }


        initFields()

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
                doubleTextfield(value, allowNegativeNumbers, 2) {
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
        taxpayer.addListener { _, _, newValue -> isATaxpayerSelected.value = newValue != null }
        taxpayer.value = presenter.settings.taxpayer

        federalState.value = getInitialFederalState()

        taxOffice.value = getInitialTaxOffice()

        retrievedTaxOffices(presenter.persistedFederalStates) // show last retrieved federal states

        presenter.getAllTaxOfficesAsync { taxOffices -> // then try to get current list from Elster (requires online connection)
            retrievedTaxOfficesOffUiThread(taxOffices)
        }

        isUploadToElsterSelected.value = presenter.settings.isUploadToElsterSelected ?: false

        certificateFilePath.value = presenter.settings.certificateFilePath

        certificatePassword.value = presenter.settings.certificatePassword ?: ""

        elsterXmlFilePath.value = presenter.settings.lastSelectedElsterXmlFilePath

        taxNumberInput.setTaxNumber(presenter.settings.taxNumber)


        initYearAndPeriod()

        showVatAmountsForPeriod()

        areRequiredFieldsForElsterXmlProvided.bind(isATaxpayerSelected.and(taxNumberInput.isEnteredTaxNumberValid))
        areRequiredFieldsForElsterUploadProvided.bind(areRequiredFieldsForElsterXmlProvided.and(isCertificateFileSet))
    }

    private fun initYearAndPeriod() {
        var accountingPeriod = overviewPresenter.accountingPeriod
        var previousPeriodEndDate = overviewPresenter.getPreviousAccountingPeriodEndDate()

        // annually is not supported by Elster for Umsatzsteuervoranmeldung
        if (overviewPresenter.accountingPeriod == AccountingPeriod.Annually) {
            accountingPeriod = AccountingPeriod.Monthly

            previousPeriodEndDate = LocalDate.now().withDayOfMonth(1).minusDays(1).asUtilDate()
        }

        presenter.getYearFromPeriod(previousPeriodEndDate)?.let { year ->
            jahr.value = year
        }

        presenter.getVoranmeldungszeitrumFromPeriod(previousPeriodEndDate, accountingPeriod)?.let {
            zeitraum.value = it
        }

        jahr.addListener { _, _, _ -> showVatAmountsForPeriod() }
        zeitraum.addListener { _, _, _ -> showVatAmountsForPeriod() }
    }

    private fun showVatAmountsForPeriod() {
        val periodStart = presenter.getSelectedPeriodStartDate(jahr.value, zeitraum.value)
        val periodEnd = overviewPresenter.getAccountingPeriodEndDate(periodStart.asLocalDate(),
            presenter.getAccountingPeriodFromZeitraum(zeitraum.value))

        val revenuesInPeriod = overviewPresenter.getDocumentsInPeriod(overviewPresenter.getRevenues(), periodStart, periodEnd)
        val expendituresInPeriod = overviewPresenter.getDocumentsInPeriod(overviewPresenter.getExpenditures(), periodStart, periodEnd)

        val revenuesWith19PercentVatInPeriod = revenuesInPeriod.flatMap { it.items }.filter { it.valueAddedTaxRate == 19f }
        this.revenuesWith19PercentVatNetAmount.value = revenuesWith19PercentVatInPeriod.sumByDouble { it.netAmount }.toInt()
        this.receivedVatWith19Percent.value = revenuesWith19PercentVatInPeriod.sumByDouble { it.valueAddedTax }

        val revenuesWith7PercentVatInPeriod = revenuesInPeriod.flatMap { it.items }.filter { it.valueAddedTaxRate == 7f }
        this.revenuesWith7PercentVatNetAmount.value = revenuesWith7PercentVatInPeriod.sumByDouble { it.netAmount }.toInt()
        this.receivedVatWith7Percent.value = revenuesWith7PercentVatInPeriod.sumByDouble { it.valueAddedTax }

        this.spentVatWith19Percent.value = expendituresInPeriod.flatMap { it.items }.filter { it.valueAddedTaxRate == 19f }.sumByDouble { it.valueAddedTax }
        this.spentWith7Percent.value = expendituresInPeriod.flatMap { it.items }.filter { it.valueAddedTaxRate == 7f }.sumByDouble { it.valueAddedTax }

        this.vatBalance.value = receivedVatWith19Percent.value + receivedVatWith7Percent.value -
                (spentVatWith19Percent.value + spentWith7Percent.value)
    }

    private fun getInitialFederalState(): FederalState? {
        if (presenter.settings.federalState.federalStateId == FederalState.FederalStateIdUnset) { // initial value, FederalState not set yet
            return null
        }
        else {
            return presenter.settings.federalState
        }
    }

    private fun getInitialTaxOffice(): TaxOffice? {
        if (presenter.settings.taxOffice.taxOfficeId == TaxOffice.TaxOfficeIdUnset) { // initial value, TaxOffice not set yet
            return null
        }
        else {
            return presenter.settings.taxOffice
        }
    }

    private fun retrievedTaxOfficesOffUiThread(taxOfficesForFederalState: List<FederalState>) {
        runLater {
            retrievedTaxOffices(taxOfficesForFederalState)
        }
    }

    private fun retrievedTaxOffices(taxOfficesForFederalState: List<FederalState>) {
        federalStates.setAll(taxOfficesForFederalState.sortedBy { it.name })

        if (federalStates.isNotEmpty() && federalState.value == null) {
            federalState.value = federalStates[0]

            selectedFederalStateChanged(federalState.value)
        }
    }

    private fun selectedFederalStateChanged(newValue: FederalState) {
        showTaxOfficesForSelectedFederalState(newValue)

        if (taxOfficesForSelectedFederalState.isNotEmpty()) {
            taxOffice.value = taxOfficesForSelectedFederalState[0]
        }
        else {
            taxOffice.value = null
        }
    }

    private fun showTaxOfficesForSelectedFederalState(newValue: FederalState) {
        taxOfficesForSelectedFederalState.setAll(newValue.taxOffices.sortedBy { it.name })
    }


    private fun updateIsCertificateFileSet() {
        isCertificateFileSet.value = checkIfFileExists(certificateFilePath.value)
    }

    private fun checkIfFileExists(filePath: String?): Boolean {
        if (filePath != null) {
            return File(filePath).exists()
        }

        return false
    }

    private fun selectElsterXmlFile() {
        val fileChooser = FileChooser()

        fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter(
            messages["elster.tax.declaration.window.elster.xml.file.extension.filter.description"], "*.xml", "*.XML"))

        if (checkIfFileExists(elsterXmlFilePath.value)) {
            val elsterXmlFile = File(elsterXmlFilePath.value)
            fileChooser.initialDirectory = elsterXmlFile.parentFile
            fileChooser.initialFileName = elsterXmlFile.name
        }

        fileChooser.showSaveDialog(currentStage)?.let { xmlOutputFile ->
            elsterXmlFilePath.value = xmlOutputFile.absolutePath
            saveSettings() // needed here to save lastSelectedElsterXmlFile
        }
    }

    private fun selectCertificateFile() {
        val fileChooser = FileChooser()

        fileChooser.extensionFilters.add(FileChooser.ExtensionFilter(
            messages["elster.tax.declaration.window.certificate.file.extension.filter.description"], "*.pfx", "*.PFX"))

        if (checkIfFileExists(certificateFilePath.value)) {
            val certificateFile = File(certificateFilePath.value)
            fileChooser.initialDirectory = certificateFile.parentFile
            fileChooser.initialFileName = certificateFile.name
        }

        fileChooser.showOpenDialog(currentStage)?.let { selectedFile ->
            certificateFilePath.value = selectedFile.absolutePath
            saveSettings()
        }
    }


    private fun makeUmsatzsteuerVoranmeldung() {
        saveSettings()

        val result = presenter.makeUmsatzsteuerVoranmeldung(createUmsatzsteuerVoranmeldungData(true))

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
        saveSettings() // TODO: find a better strategy when to save app settings

        val result = presenter.createUmsatzsteuerVoranmeldungXmlFile(createUmsatzsteuerVoranmeldungData(false))

        if (result.successful) {
            val xmlOutputFile = File(elsterXmlFilePath.value)
            xmlOutputFile.parentFile.mkdirs()

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

    private fun createUmsatzsteuerVoranmeldungData(uploadToElster: Boolean): UmsatzsteuerVoranmeldung {
        // TODO: remove this test settings and show an error message if HerstellerID isn't set
        val testHerstellerID = TestHerstellerID.T_74931.herstellerID
        val herstellerID = presenter.getHerstellerID() ?: testHerstellerID

        val useTestValues = herstellerID == testHerstellerID

        val taxNumber = if (useTestValues) Teststeuernummern.T_198_113_10010.steuernummer else taxNumberInput.taxNumber.value
        val taxOffice = if (useTestValues) TestFinanzamt.Bayern_9198.finanzamt else Finanzamt(taxOffice.value.name, taxOffice.value.taxOfficeId)

        val isForAMonth = zeitraum.value.ziffer.toInt() <= 12
        val outputFilename = OutputFilesDateTimeFormat.format(Date()) + "_UStVA_${jahr.value.jahr}_" +
                (if (isForAMonth) (zeitraum.value.ziffer + "_" + zeitraum.value.name) else zeitraum.value.name)
        val pdfOutputFile = if (uploadToElster) File(UploadedFilesFolder, outputFilename + ".pdf") else null
        val xmlOutputFile = if (uploadToElster) File(UploadedFilesFolder, outputFilename + ".xml") else null

        return UmsatzsteuerVoranmeldung(jahr.value, zeitraum.value, taxOffice, taxNumber,
            mapPersonToSteuerpflichtiger(taxpayer.value), File(certificateFilePath.value), certificatePassword.value, // TODO: don't pass certificateFilePath and certificatePassword if only creating XML file
            revenuesWith19PercentVatNetAmount.value, revenuesWith7PercentVatNetAmount.value,
            spentVatWith19Percent.value + spentWith7Percent.value, vatBalance.value, herstellerID,
            pdfOutputFile, xmlOutputFile)
    }

    private fun mapPersonToSteuerpflichtiger(person: Person): Steuerpflichtiger {
        val address = person.address

        return Steuerpflichtiger(person.firstName, person.lastName, address.street, address.streetNumber,
            address.zipCode, address.city, address.country, null, null) // TODO: set telephone and e-mail
    }


    private fun saveSettings() {
        presenter.settings.apply {
            taxpayer = this@ElsterTaxDeclarationWindow.taxpayer.value
            taxOffice = this@ElsterTaxDeclarationWindow.taxOffice.value
            federalState = this@ElsterTaxDeclarationWindow.federalState.value
            taxNumber = taxNumberInput.taxNumber.value
            isUploadToElsterSelected = this@ElsterTaxDeclarationWindow.isUploadToElsterSelected.value
            certificateFilePath = this@ElsterTaxDeclarationWindow.certificateFilePath.value
            certificatePassword = this@ElsterTaxDeclarationWindow.certificatePassword.value
            lastSelectedElsterXmlFilePath = elsterXmlFilePath.value
        }

        presenter.saveSettings()
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