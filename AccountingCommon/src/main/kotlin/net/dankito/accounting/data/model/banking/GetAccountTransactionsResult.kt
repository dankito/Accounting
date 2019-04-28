package net.dankito.accounting.data.model.banking


class GetAccountTransactionsResult(val successful: Boolean,
                                   val transactions: BankAccountTransactions?,
                                   val error: Exception?) {

    override fun toString(): String {
        if (successful) {
            return "Successful: $transactions"
        }
        else {
            return "Error: $error"
        }
    }

}