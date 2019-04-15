package net.dankito.accounting.data.model.settings

import net.dankito.accounting.data.model.BaseEntity
import net.dankito.accounting.data.model.Person
import net.dankito.tax.elster.model.Bundesland
import net.dankito.tax.elster.model.Finanzamt
import java.io.File


class ElsterTaxDeclarationSettings(var taxpayer: Person?,
                                   var bundesland: Bundesland,
                                   var finanzamt: Finanzamt,
                                   var taxNumber: String,
                                   var certificateFile: File? = null,
                                   var certificatePassword: String? = null,
                                   var lastSelectedElsterXmlFile: File? = null
) : BaseEntity() {

    internal constructor() : this(null, Bundesland("", -1), Finanzamt("", -1), "") // for object deserializers

}