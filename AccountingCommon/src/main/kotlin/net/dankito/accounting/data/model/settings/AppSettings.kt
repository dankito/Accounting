package net.dankito.accounting.data.model.settings

import net.dankito.accounting.data.model.AccountingPeriod
import net.dankito.accounting.data.model.BaseEntity


class AppSettings(var accountingPeriod: AccountingPeriod)
    : BaseEntity() {


    private constructor() : this(AccountingPeriod.Monthly) // for object deserializers

}