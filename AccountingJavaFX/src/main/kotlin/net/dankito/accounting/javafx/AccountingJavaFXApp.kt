package net.dankito.accounting.javafx

import javafx.application.Application
import javafx.stage.Stage
import net.dankito.accounting.di.CommonComponent
import net.dankito.accounting.javafx.di.AppComponent
import net.dankito.accounting.javafx.di.DaggerAppComponent
import net.dankito.accounting.javafx.di.JavaFxModule
import net.dankito.accounting.javafx.windows.mainwindow.MainWindow
import net.dankito.utils.javafx.ui.Utf8App


class AccountingJavaFXApp : Utf8App("Accounting_Messages", MainWindow::class) {


    override fun beforeStart(primaryStage: Stage) {
        super.beforeStart(primaryStage)

        setupDI()
    }


    private fun setupDI() {
        val component = DaggerAppComponent.builder()
            .javaFxModule(JavaFxModule())
            .build()

        CommonComponent.component = component
        AppComponent.component = component
    }

}


fun main(args: Array<String>) {
    Application.launch(AccountingJavaFXApp::class.java, *args)
}