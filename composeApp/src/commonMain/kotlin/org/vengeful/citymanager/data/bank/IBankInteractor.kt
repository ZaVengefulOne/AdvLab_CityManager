package org.vengeful.citymanager.data.bank

import org.vengeful.citymanager.models.BankAccount

interface IBankInteractor {
    suspend fun getAllBankAccounts(): List<BankAccount>
    suspend fun getBankAccountByPersonId(personId: Int): BankAccount?
    suspend fun getBankAccountById(id: Int): BankAccount?
    suspend fun getBankAccountByEnterpriseName(enterpriseName: String): BankAccount?
    suspend fun createBankAccount(
        personId: Int?,
        enterpriseName: String?,
        creditAmount: Double,
        personBalance: Double? = null
    ): BankAccount

    suspend fun closeCredit(accountId: Int): BankAccount
    suspend fun updateBankAccount(bankAccount: BankAccount, personBalance: Double? = null): Boolean
    suspend fun deleteBankAccount(id: Int): Boolean
}
