package net.dankito.accounting.javafx

import javafx.application.Application
import net.dankito.accounting.javafx.windows.mainwindow.MainWindow
import net.dankito.utils.javafx.ui.Utf8App


class AccountingJavaFXApp : Utf8App("Accounting_Messages", MainWindow::class) {

}


fun main(args: Array<String>) {
    Application.launch(AccountingJavaFXApp::class.java, *args)
}