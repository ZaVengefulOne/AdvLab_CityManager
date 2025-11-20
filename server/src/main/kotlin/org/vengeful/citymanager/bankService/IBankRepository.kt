package org.vengeful.citymanager.bankService

import org.vengeful.citymanager.models.BankAccount

interface IBankRepository {
    fun getAllBankAccounts(): List<BankAccount>
    fun getBankAccountByPersonId(personId: Int): BankAccount?
    fun getBankAccountById(id: Int): BankAccount?
    fun createBankAccount(
        personId: Int?,
        enterpriseName: String?,
        depositAmount: Double,
        creditAmount: Double
    ): BankAccount

    fun updateBankAccount(bankAccount: BankAccount): BankAccount?
    fun deleteBankAccount(id: Int): Boolean
}