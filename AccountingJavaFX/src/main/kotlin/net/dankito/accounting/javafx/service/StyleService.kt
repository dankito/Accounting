package net.dankito.accounting.javafx.service


open class StyleService {

    companion object {
        const val DefaultStyle = ""

        const val CurrentAccountingPeriodStyle = "-fx-background-color: linear-gradient( from 0% 0% to 0% 100%, white, cornflowerblue );"

        const val PreviousAccountingPeriodStyle = "-fx-background-color: linear-gradient( from 0% 0% to 0% 100%, white, orange );"
    }


    open val defaultStyle = DefaultStyle

    open val currentAccountingPeriodStyle = CurrentAccountingPeriodStyle

    open val previousAccountingPeriodStyle = PreviousAccountingPeriodStyle

}