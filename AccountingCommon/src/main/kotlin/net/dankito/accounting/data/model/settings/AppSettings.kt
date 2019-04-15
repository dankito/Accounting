package net.dankito.accounting.data.model.settings

import net.dankito.accounting.data.model.AccountingPeriod
import net.dankito.accounting.data.model.BaseEntity


class AppSettings(var accountingPeriod: AccountingPeriod,
                  val elsterTaxDeclarationSettings: ElsterTaxDeclarationSettings // TODO: or save only for German users?

) : BaseEntity() {


    private constructor() : this(AccountingPeriod.Monthly, ElsterTaxDeclarationSettings()) // for object deserializers

}