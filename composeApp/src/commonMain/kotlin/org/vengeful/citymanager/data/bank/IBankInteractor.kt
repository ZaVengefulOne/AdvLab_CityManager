package org.vengeful.citymanager.data.bank

import org.vengeful.citymanager.models.BankAccount

interface IBankInteractor {
    suspend fun getAllBankAccounts(): List<BankAccount>
    suspend fun getBankAccountByPersonId(personId: Int): BankAccount?
    suspend fun getBankAccountById(id: Int): BankAccount?
    suspend fun createBankAccount(personId: Int?, enterpriseName: String?, depositAmount: Double, creditAmount: Double): BankAccount
    suspend fun updateBankAccount(bankAccount: BankAccount): Boolean
    suspend fun deleteBankAccount(id: Int): Boolean
}