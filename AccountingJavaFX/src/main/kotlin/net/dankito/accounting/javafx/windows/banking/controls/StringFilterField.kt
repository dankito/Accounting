package net.dankito.accounting.javafx.windows.banking.controls

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Pos
import net.dankito.accounting.javafx.windows.banking.model.StringFilterOption
import tornadofx.*


class StringFilterField(titleResourceKey: String,
                        filterField: SimpleBooleanProperty,
                        enteredFilter: SimpleStringProperty,
                        filterOption: SimpleObjectProperty<StringFilterOption>,
                        ignoreCase: SimpleBooleanProperty) : View() {


    override val root = vbox {

        checkbox(messages[titleResourceKey], filterField)

        hbox {
            alignment = Pos.CENTER_LEFT

            vboxConstraints {
                marginTop = 4.0
                marginLeft = 12.0
            }

            combobox(filterOption, StringFilterOption.values().toList()) {

            }

            checkbox(messages["ignore.case"], ignoreCase) {
                hboxConstraints {
                    marginLeft = 6.0
                    marginRight = 6.0
                }
            }

            textfield(enteredFilter) {
                useMaxWidth = true
            }

        }

    }

}