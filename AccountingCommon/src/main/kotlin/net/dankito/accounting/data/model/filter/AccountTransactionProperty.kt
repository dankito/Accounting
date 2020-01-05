package net.dankito.accounting.data.model.filter


enum class AccountTransactionProperty(val propertyName: String) {

    SenderOrReceiverName("senderOrReceiverName"),

    Usage("usage");


    companion object {

        fun fromPropertyName(propertyName: String): AccountTransactionProperty {
            return values().first { it.propertyName == propertyName }
        }

    }

}