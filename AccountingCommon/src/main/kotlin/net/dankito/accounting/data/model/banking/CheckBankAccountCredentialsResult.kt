package net.dankito.accounting.data.model.banking


class CheckBankAccountCredentialsResult(val successful: Boolean,
                                        val error: Exception?) {

    override fun toString(): String {
        if (successful) {
            return "Successful, credentials are correct"
        }
        else {
            return "Error: $error"
        }
    }

}