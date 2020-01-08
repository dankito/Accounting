package net.dankito.accounting.javafx.windows.mainwindow.controls

import com.sun.javafx.scene.control.skin.MenuButtonSkinBase
import com.sun.javafx.scene.control.skin.SplitMenuButtonSkin
import javafx.collections.FXCollections
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.stage.FileChooser
import javafx.stage.Screen
import net.dankito.accounting.data.model.Document
import net.dankito.accounting.data.model.event.AccountingPeriodChangedEvent
import net.dankito.accounting.data.model.event.DocumentsUpdatedEvent
import net.dankito.accounting.data.model.invoice.InvoiceData
import net.dankito.accounting.javafx.di.AppComponent
import net.dankito.accounting.javafx.presenter.OverviewPresenter
import net.dankito.accounting.javafx.service.StyleService
import net.dankito.utils.IThreadPool
import net.dankito.utils.events.IEventBus
import net.dankito.utils.javafx.ui.color.UiColors
import net.dankito.utils.javafx.ui.controls.searchtextfield
import net.dankito.utils.javafx.util.FXUtils
import org.slf4j.LoggerFactory
import tornadofx.*
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.concurrent.schedule


abstract class DocumentsOverview(titleResourceKey: String, protected val presenter: OverviewPresenter) : View() {

    companion object {
        protected const val ControlBarHeight = 35.0

        protected const val TitleLabelLeftMargin = 2.0
        protected const val TitleLabelTopBottomMargin = 5.0

        protected const val SearchFieldWidth = 200.0
        protected const val SearchFieldTopBottomMargin = 3.0

        protected const val DropDownButtonWidth = 20.0
        protected const val AddDocumentButtonWidth = ControlBarHeight + DropDownButtonWidth
        protected const val AddDocumentButtonTopBottomMargin = 1.0

        private val logger = LoggerFactory.getLogger(DocumentsOverview::class.java)
    }


    protected abstract fun retrieveDocuments(): List<Document>

    protected abstract fun showCreateNewDocumentWindow(extractedData: InvoiceData? = null)


    @Inject
    protected lateinit var styleService: StyleService

    @Inject
    protected lateinit var eventBus: IEventBus

    @Inject
    protected lateinit var threadPool: IThreadPool


    protected var tableView: DocumentsTable by singleAssign()

    protected var allDocumentsOfType = listOf<Document>()

    protected val displayedDocuments = FXCollections.observableArrayList<Document>()

    protected var currentDocumentsFilterTerm = ""

    protected var previouslySelectedFile: File? = null

    protected open val styleDocumentsFromCurrentAndPreviousPeriod = true


    init {
        AppComponent.component.inject(this)

        eventBus.subscribe(DocumentsUpdatedEvent::class.java) {
            runLater { loadDocumentsInBackgroundAndUpdateOnUiThread() }
        }

        eventBus.subscribe(AccountingPeriodChangedEvent::class.java) {
            runLater { updateTableView() }
        }

        loadDocumentsInBackgroundAndUpdateOnUiThreadDelayed() // so that subclasses have time to initialize
    }


    override val root = vbox {
        val title = messages[titleResourceKey]

        anchorpane {
            minHeight = ControlBarHeight
            maxHeight = ControlBarHeight

            label(title) {
                font = Font.font(font.family, FontWeight.BLACK, font.size + 1)

                anchorpaneConstraints {
                    leftAnchor = TitleLabelLeftMargin
                    topAnchor = TitleLabelTopBottomMargin
                    bottomAnchor = TitleLabelTopBottomMargin
                }
            }

            // TODO: may add icon (Lupe) before search field
            searchtextfield {
                minWidth = SearchFieldWidth
                maxWidth = SearchFieldWidth

                promptText = String.format(messages["main.window.documents.overview.search.documents.prompt"], title)

                textProperty().addListener { _, _, newValue -> filterDocuments(newValue) }

                anchorpaneConstraints {
                    topAnchor = SearchFieldTopBottomMargin
                    rightAnchor = AddDocumentButtonWidth + 12.0
                    bottomAnchor = SearchFieldTopBottomMargin
                }
            }

            // TODO: add to JavaFxUtils
            add(SplitMenuButton().apply {
                minWidth = AddDocumentButtonWidth
                maxWidth = AddDocumentButtonWidth

                text = "+"
                font = Font.font(font.family, FontWeight.BOLD, 17.0)
                textFill = Color.valueOf(UiColors.AddButtonHexColor)
                alignment = Pos.CENTER
                contentDisplay = ContentDisplay.TEXT_ONLY

                setDropDownButtonWidth()

                action { showCreateNewDocumentWindow() }

                this.items.addAll(createAddButtonMenuItems())

                anchorpaneConstraints {
                    topAnchor = AddDocumentButtonTopBottomMargin
                    rightAnchor = 0.0
                    bottomAnchor = AddDocumentButtonTopBottomMargin
                }
            })
        }


        tableView = DocumentsTable(displayedDocuments, styleDocumentsFromCurrentAndPreviousPeriod, presenter, styleService, threadPool)
        add(tableView)
    }


    private fun createAddButtonMenuItems(): List<MenuItem> {
        return listOf(createAddDocumentFromSearchablePdfMenuButton())
    }

    private fun createAddDocumentFromSearchablePdfMenuButton(): MenuItem {
        val item = MenuItem(messages["main.window.documents.overview.add.from.searchable.pdf"])

        item.action { addDocumentFromSearchablePdf() }

        return item
    }

    private fun SplitMenuButton.setDropDownButtonWidth() {
        skinProperty().addListener { _, _, skin ->
            (skin as? SplitMenuButtonSkin)?.let { skin ->
                try {
                    val arrowButtonField = MenuButtonSkinBase::class.java.getDeclaredField("arrowButton")

                    arrowButtonField.isAccessible = true

                    val arrowButton = arrowButtonField.get(skin) as Region
                    arrowButton.paddingHorizontal = 4.0
                    arrowButton.minWidth = DropDownButtonWidth
                    arrowButton.maxWidth = DropDownButtonWidth
                } catch (e: Exception) {
                    logger.error("Could not set arrow buttons width", e)
                }
            }
        }
    }


    private fun loadDocumentsInBackgroundAndUpdateOnUiThreadDelayed() {
        Timer().schedule(1000) {
            loadDocumentsInBackgroundAndUpdateOnUiThread()
        }
    }

    protected open fun loadDocumentsInBackgroundAndUpdateOnUiThread() {
        val retrievedDocuments = retrieveDocuments()

        retrievedDocumentsOffUiThread(retrievedDocuments)
    }

    protected open fun retrievedDocumentsOffUiThread(retrievedDocuments: List<Document>) {
        allDocumentsOfType = sortDocuments(retrievedDocuments)

        runLater {
            reapplyLastSearch()
        }
    }

    protected open fun sortDocuments(retrievedDocuments: List<Document>): List<Document> {
        return retrievedDocuments.sortedByDescending { it.paymentDate }
    }


    protected open fun reapplyLastSearch() {
        filterDocuments(currentDocumentsFilterTerm)
    }

    protected open fun filterDocuments(filterTerm: String) {
        currentDocumentsFilterTerm = filterTerm

        displayedDocuments.setAll(presenter.filterDocuments(allDocumentsOfType, filterTerm))
    }


    protected open fun updateTableView() {
        tableView.refresh()
    }


    private fun addDocumentFromSearchablePdf() {
        val fileChooser = FileChooser()

        fileChooser.extensionFilters.addAll(FileChooser.ExtensionFilter(
            messages["main.window.documents.overview.searchable.pdf.file.extension.filter.description"], "*.pdf", "*.PDF"))

        if (previouslySelectedFile?.exists() == true) {
            fileChooser.initialDirectory = previouslySelectedFile?.parentFile
            fileChooser.initialFileName = previouslySelectedFile?.name
        }

        fileChooser.showOpenDialog(currentStage)?.let { selectedPdfFile ->
            previouslySelectedFile = selectedPdfFile

            addDocumentFromFile(selectedPdfFile)
        }
    }

    private fun addDocumentFromFile(file: File) {
        val extractedData = presenter.extractInvoiceData(file)

        if (extractedData.couldExtractText == false) {
            showErrorMessage("main.window.documents.overview.error.could.not.extract.text.message", file, extractedData.error)
        }
        else if (extractedData.couldExtractInvoiceData == false) {
            showErrorMessage("main.window.documents.overview.error.could.not.extract.invoice.data.message", file, extractedData.error, extractedData.extractedText)
        }
        else {
            showCreateNewDocumentWindow(extractedData)
        }
    }

    // TODO: use DialogService as soon as JavaFxUtils 2.0.0 is out
    private fun showErrorMessage(errorMessageKey: String, vararg formatArguments: Any?) {
        val alert = Alert(Alert.AlertType.ERROR)

        alert.headerText = messages["main.window.documents.overview.error.could.not.extract.invoice.data.title"]

        currentStage?.let { alert.initOwner(it) }

        setAlertContent(alert, String.format(messages[errorMessageKey], *formatArguments))

        alert.buttonTypes.setAll(ButtonType.OK)

        alert.isResizable = true

        alert.show()
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