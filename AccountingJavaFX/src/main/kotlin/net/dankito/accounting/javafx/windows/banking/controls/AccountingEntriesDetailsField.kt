package net.dankito.accounting.javafx.windows.banking.controls

import javafx.geometry.Pos
import javafx.scene.layout.Priority
import tornadofx.*


class AccountingEntriesDetailsField(private val fieldName: String, private val fieldValue: String) : View() {

    companion object {
        private val FieldHeight = 32.0
        private val FieldNameWidth = 200.0
        private val FieldValueWidth = 500.0
    }


    override val root = hbox {

        label(fieldName) {
            prefHeight = FieldHeight
            prefWidth = FieldNameWidth

            hboxConstraints {
                alignment = Pos.CENTER_RIGHT
                marginRight = 12.0
            }
        }

        textfield(fieldValue) {
            isEditable = false
            prefHeight = FieldHeight
            prefWidth = FieldValueWidth

            hboxConstraints {
                hgrow = Priority.ALWAYS
                marginRight = 4.0
            }
        }

        vboxConstraints {
            marginBottom = 6.0
        }

    }

}