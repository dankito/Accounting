package net.dankito.accounting.data.model.settings

import net.dankito.accounting.data.model.AccountingPeriod
import net.dankito.accounting.data.model.BaseEntity
import net.dankito.accounting.data.model.tax.elster.ElsterTaxDeclarationSettings
import javax.persistence.*


@Entity
class AppSettings(

    @Enumerated
    @Column(name = AccountingPeriodColumnName)
    var accountingPeriod: AccountingPeriod,


    @OneToOne(fetch = FetchType.EAGER, cascade = [ CascadeType.PERSIST, CascadeType.REMOVE ])
    @JoinColumn(name = ElsterTaxDeclarationSettingsJoinColumnName)
    val elsterTaxDeclarationSettings: ElsterTaxDeclarationSettings // TODO: or save only for German users?

) : BaseEntity() {

    companion object {

        const val AccountingPeriodColumnName = "accounting_period"

        const val ElsterTaxDeclarationSettingsJoinColumnName = "elster_tax_declaration_settings"

    }


    private constructor() : this(AccountingPeriod.Monthly, ElsterTaxDeclarationSettings()) // for object deserializers

}