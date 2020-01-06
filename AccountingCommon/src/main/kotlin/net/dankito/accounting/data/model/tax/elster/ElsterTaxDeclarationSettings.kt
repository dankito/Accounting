package net.dankito.accounting.data.model.tax.elster

import net.dankito.accounting.data.model.BaseEntity
import net.dankito.accounting.data.model.Person
import net.dankito.accounting.data.model.tax.FederalState
import net.dankito.accounting.data.model.tax.TaxOffice
import javax.persistence.*


@Entity
class ElsterTaxDeclarationSettings(

    @OneToOne(fetch = FetchType.EAGER, cascade = [ CascadeType.PERSIST ])
    @JoinColumn(name = TaxPayerJoinColumnName)
    var taxpayer: Person?,

    @OneToOne(fetch = FetchType.EAGER, cascade = [ CascadeType.PERSIST ])
    @JoinColumn(name = FederalStateJoinColumnName)
    var federalState: FederalState,

    @OneToOne(fetch = FetchType.EAGER, cascade = [ CascadeType.PERSIST ])
    @JoinColumn(name = TaxOfficeJoinColumnName)
    var taxOffice: TaxOffice,

    @Column(name = TaxNumberColumnName)
    var taxNumber: String,

    @Column(name = IsUploadToElsterSelectedColumnName)
    var isUploadToElsterSelected: Boolean? = null,

    @Column(name = CertificateFilePathColumnName)
    var certificateFilePath: String? = null,

    @Column(name = CertificatePasswordColumnName)
    var certificatePassword: String? = null,

    @Column(name = LastSelectedElsterXmlFilePathColumnName)
    var lastSelectedElsterXmlFilePath: String? = null

) : BaseEntity() {

    companion object {

        const val TaxPayerJoinColumnName = "tax_payer"

        const val FederalStateJoinColumnName = "federal_state"

        const val TaxOfficeJoinColumnName = "tax_office"

        const val TaxNumberColumnName = "tax_number"

        const val IsUploadToElsterSelectedColumnName = "is_upload_uo_elster_selected"

        const val CertificateFilePathColumnName = "certificate_file_path"

        const val CertificatePasswordColumnName = "certificate_password"

        const val LastSelectedElsterXmlFilePathColumnName = "last_selected_elster_xml_file_path"

    }


    internal constructor() : this(null, FederalState(), TaxOffice(), "") // for object deserializers

}