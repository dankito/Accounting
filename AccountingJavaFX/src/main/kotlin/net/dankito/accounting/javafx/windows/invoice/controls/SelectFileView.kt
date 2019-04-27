package net.dankito.accounting.javafx.windows.invoice.controls

import javafx.geometry.Pos
import javafx.scene.control.TextField
import javafx.scene.layout.Priority
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import net.dankito.accounting.javafx.windows.invoice.model.SelectFileType
import tornadofx.*
import java.io.File


class SelectFileView(private val labelText: String,
                     private val type: SelectFileType,
                     private val selectedFilePath: String = "",
                     private val extensionFilter: List<FileChooser.ExtensionFilter>? = null,
                     private val selectedFilePathChangedListener: ((String) -> Unit)? = null)
    : View() {


    private var selectedFileTextField: TextField by singleAssign()


    val selectedFile: File
        get() = File(selectedFileTextField.text)


    override val root = hbox {
        minHeight = 32.0
        maxHeight = minHeight
        alignment = Pos.CENTER_LEFT

        label(labelText) {
            minWidth = 100.0
            maxWidth = minWidth
        }

        selectedFileTextField = textfield(selectedFilePath) {
            useMaxHeight = true

            textProperty().addListener { _, _, newValue -> selectedFilePathChangedListener?.invoke(newValue) }

            hboxConstraints {
                marginLeft = 6.0
                marginRight = 6.0

                hGrow = Priority.ALWAYS
            }
        }

        button("...") {
            action { selectFileOrDirectoryAndSetTextFieldValue() }

            useMaxHeight = true

            hboxConstraints {
                marginTopBottom(2.0)
            }
        }
    }



    private fun selectFileOrDirectoryAndSetTextFieldValue() {
        selectFileOrDirectory()?.let { selectedFile ->
            selectedFileTextField.text = selectedFile.absolutePath
        }
    }

    private fun selectFileOrDirectory(): File? {
        return when (type) {
            SelectFileType.SelectDirectory -> selectDirectory()
            else -> selectFile()
        }
    }

    private fun selectFile(): File? {
        val fileChooser = FileChooser()
        fileChooser.title = labelText

        val selectedFile = this.selectedFile
        fileChooser.initialDirectory = getInitialDirectory(selectedFile)
        if (selectedFile.isFile) {
            fileChooser.initialFileName = selectedFile.name
        }

        extensionFilter?.let { extensionFilter ->
            fileChooser.extensionFilters.addAll(extensionFilter)
        }

        when (type) {
            SelectFileType.SaveFile -> return fileChooser.showSaveDialog(currentWindow)
            else -> return fileChooser.showOpenDialog(currentWindow)
        }
    }

    private fun selectDirectory(): File? {
        val directoryChooser = DirectoryChooser()

        directoryChooser.title = labelText

        directoryChooser.initialDirectory = getInitialDirectory()

        return directoryChooser.showDialog(currentWindow)
    }

    private fun getInitialDirectory(): File? {
        return getInitialDirectory(this.selectedFile)
    }

    private fun getInitialDirectory(selectedFile: File): File? {
        when {
            selectedFile.exists() && selectedFile.isDirectory -> return selectedFile
            selectedFile.parentFile?.isDirectory ?: false -> return selectedFile.parentFile
            else -> return null
        }
    }
}