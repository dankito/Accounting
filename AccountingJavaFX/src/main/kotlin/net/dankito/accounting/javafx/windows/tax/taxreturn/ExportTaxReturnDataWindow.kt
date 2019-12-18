package net.dankito.accounting.javafx.windows.tax.taxreturn

import javafx.beans.property.Property
import javafx.beans.property.SimpleDoubleProperty
import javafx.beans.property.SimpleStringProperty
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
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.event.DocumentsUpdatedEvent
import net.dankito.accounting.javafx.di.AppComponent
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.utils.datetime.asLocalDate
import net.dankito.utils.datetime.asUtilDate
import net.dankito.utils.events.IEventBus
import net.dankito.utils.events.ISubscribedEvent
import net.dankito.utils.javafx.os.JavaFxOsService
import net.dankito.utils.javafx.ui.controls.doubleTextfield
import net.dankito.utils.javafx.ui.dialogs.Window
import net.dankito.utils.javafx.ui.extensions.addStyleToCurrentStyle
import net.dankito.utils.javafx.util.FXUtils
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import org.slf4j.LoggerFactory
import tornadofx.*
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*
import javax.inject.Inject


class ExportTaxReturnDataWindow(private val overviewPresenter: OverviewPresenter) : Window() {

    companion object {

        private val AmountFormat = DecimalFormat("0.00")

        private val DateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.MEDIUM)

        private const val VerticalSpaceBetweenSections = 6.0

        private const val HorizontalSpaceAfterLabel = 4.0

        private const val AmountsLabelsWidth = 400.0

        private const val ButtonsHeight = 34.0
        private const val ButtonsWidth = 200.0

        private val logger = LoggerFactory.getLogger(ExportTaxReturnDataWindow::class.java)
    }


    @Inject
    protected lateinit var eventBus: IEventBus


    private val availableYears = FXCollections.observableArrayList<String>()
    private val selectedYear = SimpleStringProperty()


    private val revenuesInPeriod = FXCollections.observableArrayList<Document>()
    private val revenuesNetAmount = SimpleDoubleProperty()

    private val expendituresInPeriod = FXCollections.observableArrayList<Document>()
    private val expendituresNetAmount = SimpleDoubleProperty()


    private var lastSelectedTaxReturnDataOutputFile: File? = null


    private val subscribedEvent: ISubscribedEvent


    init {
        AppComponent.component.inject(this)

        subscribedEvent = eventBus.subscribe(DocumentsUpdatedEvent::class.java) {
            showAmountsForPeriod()
            setAvailableYears()
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

            combobox<String>(selectedYear, availableYears) {

                hboxConstraints {
                    marginLeft = HorizontalSpaceAfterLabel
                    marginRight = 24.0
                }
            }
        }

        amountField("export.tax.return.data.window.revenues.label", revenuesNetAmount)

        amountField("export.tax.return.data.window.expenditures.label", expendituresNetAmount)

        hbox {
            alignment = Pos.CENTER_LEFT

            button(messages["export.tax.return.data.window.create.tax.return.data.label"]) {
                prefHeight = ButtonsHeight
                prefWidth = ButtonsWidth

                action { exportTaxReturnData() }
            }

            vboxConstraints {
                marginTop = VerticalSpaceBetweenSections + 12
            }
        }


        initFields()

    }

    private fun EventTarget.amountField(labelResourceKey: String, value: Property<Number>,
                                        allowNegativeNumbers: Boolean = false,
                                        op: HBox.() -> Unit = {}): Pane {

        return hbox {
            alignment = Pos.CENTER_LEFT

            label(messages[labelResourceKey]) {
                prefWidth = AmountsLabelsWidth
            }

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

            label(overviewPresenter.getUserCurrencySymbol())

            vboxConstraints {
                marginTop = VerticalSpaceBetweenSections
            }

            op(this)
        }
    }


    private fun initFields() {
        // TODO: create settings
//        lastSelectedTaxReturnDataOutputFile = presenter.settings.lastSelectedElsterXmlFilePath?.let { File(it) }


        initYear()
    }

    private fun initYear() {
        selectedYear.addListener { _, _, _ -> showAmountsForPeriod() }

        setAvailableYears()

        if (availableYears.size > 1) {
            selectedYear.value = availableYears[availableYears.size - 2]
        }
        else if (availableYears.isNotEmpty()) {
            selectedYear.value = availableYears.last()
        }
    }

    private fun setAvailableYears() {
        availableYears.setAll(getYearsWithData())
    }

    private fun getYearsWithData(): List<String> {
        val revenuesAndExpenditures = overviewPresenter.getRevenues().toMutableList()
        revenuesAndExpenditures.addAll(overviewPresenter.getExpenditures())

        return revenuesAndExpenditures.mapNotNull { it.paymentDate?.asLocalDate()?.year?.toString() }.toSet().sortedDescending()
    }

    private fun showAmountsForPeriod() {
        val selectedYear = selectedYear.value.toInt()
        val periodStart = LocalDate.of(selectedYear, 1, 1).asUtilDate()
        val periodEnd = LocalDate.of(selectedYear, 12, 31).asUtilDate()

        revenuesInPeriod.setAll(overviewPresenter.getDocumentsInPeriod(overviewPresenter.getRevenues(), periodStart, periodEnd))
        this.revenuesNetAmount.value = revenuesInPeriod.sumByDouble { it.netAmount }

        val allExpendituresInPeriod = overviewPresenter.getDocumentsInPeriod(overviewPresenter.getExpenditures(), periodStart, periodEnd)
        expendituresInPeriod.setAll(allExpendituresInPeriod)
        this.expendituresNetAmount.value = expendituresInPeriod.sumByDouble { it.netAmount }
    }


    private fun exportTaxReturnData() {
        saveSettings() // TODO: find a better strategy when to save app settings

        val fileChooser = FileChooser()

        fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter(
            messages["export.tax.return.data.window.output.file.extension.filter.description"], "*.csv", "*.CSV"))

        lastSelectedTaxReturnDataOutputFile?.let { lastSelectedOutputFile ->
            fileChooser.initialDirectory = lastSelectedOutputFile.parentFile
            fileChooser.initialFileName = lastSelectedOutputFile.name
        }

        fileChooser.showSaveDialog(currentStage)?.let { outputFile ->
            lastSelectedTaxReturnDataOutputFile = outputFile
            saveSettings() // needed here to save lastSelectedTaxReturnDataOutputFile

            val error = createTaxReturnDataFile(outputFile)

            if (error != null) {
                createDialog(Alert.AlertType.INFORMATION,
                    String.format(messages["export.tax.return.data.window.create.output.file.error.alert.message"], outputFile, error),
                    messages["export.tax.return.data.window.create.output.file.error.alert.title"],
                    currentStage, ButtonType.OK).show()
            }
            else {
                createDialog(Alert.AlertType.INFORMATION,
                    String.format(messages["export.tax.return.data.window.create.output.file.success.alert.message"], outputFile),
                    messages["export.tax.return.data.window.create.output.file.success.alert.title"],
                    currentStage, ButtonType.OK).showAndWait()

                close()

                JavaFxOsService().openFileInOsDefaultApplication(outputFile)
            }
        }
    }

    // TODO: move to presenter
    private fun createTaxReturnDataFile(outputFile: File): Exception? {
        try {
            val headerNames = listOf("Datum", "Bezeichung", "Netto", "Mehrwertsteuer 7%", "Mehrwertsteuer 19%", "Brutto") // TODO: may internationalize

            val writer = BufferedWriter(FileWriter(outputFile))

            CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(*headerNames.toTypedArray())).use { csvPrinter ->
                printSection(csvPrinter, "Einkünfte:", revenuesInPeriod) // TODO: may internationalize

                printSection(csvPrinter, "Ausgaben:", expendituresInPeriod) // TODO: may internationalize
            }
        } catch (e: Exception) {
            logger.error("Could not write tax return data of year ${selectedYear.value} to file $outputFile", e)

            return e
        }

        return null
    }

    private fun printSection(csvPrinter: CSVPrinter, sectionTitle: String, documentsToPrint: List<Document>) {
        csvPrinter.println()

        csvPrinter.printRecord(sectionTitle)
        csvPrinter.println()

        documentsToPrint.sortedBy { it.paymentDate }.forEach { document ->
            csvPrinter.printRecord(
                formatNullableDate(document.paymentDate),
                document.description,
                formatAmount(document.netAmount),
                formatNullableAmount(document.getVatForVatRate(7f)),
                formatNullableAmount(document.getVatForVatRate(19f)),
                formatAmount(document.totalAmount)
            )
        }

        csvPrinter.println()
        csvPrinter.printRecord("", "Gesamt:",  // TODO: may internationalize
            formatAmount(documentsToPrint.sumByDouble { it.netAmount }),
            formatAmount(documentsToPrint.sumByDouble { it.getVatForVatRate(7f) ?: 0.0 }),
            formatAmount(documentsToPrint.sumByDouble { it.getVatForVatRate(19f) ?: 0.0 }),
            formatAmount(documentsToPrint.sumByDouble { it.totalAmount })
        )

        csvPrinter.println()
        csvPrinter.println()
    }

    private fun formatNullableAmount(amount: Double?): String {
        amount?.let {
            return formatAmount(amount)
        }

        return getNullValue()
    }

    private fun formatAmount(amount: Double): String {
        return AmountFormat.format(amount)
    }

    private fun formatNullableDate(date: Date?): String {
        date?.let {
            return DateFormat.format(date)
        }

        return getNullValue()
    }

    private fun getNullValue(): String {
        return ""
    }


    private fun saveSettings() {
//        presenter.settings.apply {
//            // TODO
////            lastSelectedElsterXmlFilePath = this@ExportTaxReturnDataWindow.lastSelectedTaxReturnDataOutputFile?.absolutePath
//        }
//
//        presenter.saveSettings()
    }


    // TODO: use DialogService as soon as JavaFxUtils 2.0.0 is out
    private fun createDialog(alertType: Alert.AlertType, message: CharSequence, alertTitle: CharSequence?, owner: Stage?, vararg buttons: ButtonType): Alert {
        val alert = Alert(alertType)

        (alertTitle as? String)?.let { alert.title = it }

        owner?.let { alert.initOwner(it) }

        (message as? String)?.let { setAlertContent(alert, it) }
        alert.headerText = null

        alert.buttonTypes.setAll(*buttons)

        alert.isResizable = true

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