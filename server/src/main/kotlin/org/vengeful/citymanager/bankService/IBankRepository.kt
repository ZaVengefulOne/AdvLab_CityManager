package org.vengeful.citymanager.bankService

import org.vengeful.citymanager.models.BankAccount

interface IBankRepository {
    fun getAllBankAccounts(): List<BankAccount>
    fun getBankAccountByPersonId(personId: Int): BankAccount?
    fun getBankAccountById(id: Int): BankAccount?
    fun createBankAccount(
        personId: Int?,
        enterpriseName: String?,
        creditAmount: Double,
        personBalance: Double? = null,
    ): BankAccount

    fun closeCredit(accountId: Int): BankAccount?
    fun updateBankAccount(bankAccount: BankAccount, personBalance: Double? = null): BankAccount?
    fun getBankAccountByEnterpriseName(enterpriseName: String): BankAccount?
    fun deleteBankAccount(id: Int): Boolean
}
