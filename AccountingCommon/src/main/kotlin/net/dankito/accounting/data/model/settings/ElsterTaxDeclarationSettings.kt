package net.dankito.accounting.data.model.settings

import net.dankito.accounting.data.model.BaseEntity
import net.dankito.accounting.data.model.Person
import net.dankito.tax.elster.model.Bundesland
import net.dankito.tax.elster.model.Finanzamt
import javax.persistence.*


@Entity
class ElsterTaxDeclarationSettings(

    @OneToOne(fetch = FetchType.EAGER, cascade = [ CascadeType.PERSIST ])
    @JoinColumn(name = TaxPayerJoinColumnName)
    var taxpayer: Person?,

    @Transient // TODO
    var bundesland: Bundesland,

    @Transient // TODO
    var finanzamt: Finanzamt,

    @Column(name = TaxNumberColumnName)
    var taxNumber: String,

    @Column(name = CertificateFilePathColumnName)
    var certificateFilePath: String? = null,

    @Column(name = CertificatePasswordColumnName)
    var certificatePassword: String? = null,

    @Column(name = LastSelectedElsterXmlFilePathColumnName)
    var lastSelectedElsterXmlFilePath: String? = null

) : BaseEntity() {

    companion object {

        const val TaxPayerJoinColumnName = "tax_payer"

        const val TaxNumberColumnName = "tax_number"

        const val CertificateFilePathColumnName = "certificate_file_path"

        const val CertificatePasswordColumnName = "certificate_password"

        const val LastSelectedElsterXmlFilePathColumnName = "last_selected_elster_xml_file_path"

    }


    internal constructor() : this(null, Bundesland("", -1), Finanzamt("", -1), "") // for object deserializers

}