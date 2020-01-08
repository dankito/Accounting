package net.dankito.accounting.data.model.invoice

import net.dankito.text.extraction.info.model.InvoiceData
import java.io.File


open class InvoiceData(
    val file: File,
    val extractedText: String?,
    val data: InvoiceData?, // TODO: may map to own class
    val error: Exception?
) {


    val couldExtractText: Boolean
        get() = extractedText.isNullOrBlank() == false

    val couldExtractInvoiceData: Boolean
        get() = data?.couldExtractInvoiceData == true

    val succesful: Boolean
        get() = couldExtractText && couldExtractInvoiceData && error == null


    override fun toString(): String {
        if (error != null) {
            return "Error occurred: couldExtractText = $couldExtractText, couldExtractInvoiceData = $couldExtractInvoiceData, error = $error"
        }

        return "Success: $data"
    }

}