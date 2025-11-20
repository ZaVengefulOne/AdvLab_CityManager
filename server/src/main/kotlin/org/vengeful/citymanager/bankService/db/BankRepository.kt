package org.vengeful.citymanager.bankService.db

import org.jetbrains.exposed.sql.transactions.transaction
import org.vengeful.citymanager.bankService.IBankRepository
import org.vengeful.citymanager.models.BankAccount
import org.vengeful.citymanager.personService.db.PersonDao

class BankRepository : IBankRepository {

    override fun getAllBankAccounts(): List<BankAccount> = transaction {
        BankAccountDao.all().map { it.toBankAccount() }
    }

    override fun getBankAccountByPersonId(personId: Int): BankAccount? = transaction {
        BankAccountDao.find { BankAccounts.personId eq personId }
            .firstOrNull()
            ?.toBankAccount()
    }

    override fun getBankAccountById(id: Int): BankAccount? = transaction {
        BankAccountDao.findById(id)?.toBankAccount()
    }

    override fun createBankAccount(
        personId: Int?,
        enterpriseName: String?, // НОВОЕ
        depositAmount: Double,
        creditAmount: Double
    ): BankAccount = transaction {
        // Если personId указан, проверяем существование
        personId?.let { id ->
            val existing = BankAccountDao.find { BankAccounts.personId eq id }.firstOrNull()
            if (existing != null) {
                throw IllegalStateException("Bank account for person $id already exists")
            }
        }

        val bankAccountDao = BankAccountDao.new {
            this.personId = personId?.let {
                PersonDao.findById(it)?.id
            }
            this.enterpriseName = enterpriseName
            this.depositAmount = depositAmount
            this.creditAmount = creditAmount
        }

        bankAccountDao.toBankAccount()
    }

    override fun updateBankAccount(bankAccount: BankAccount): BankAccount? = transaction {
        BankAccountDao.findById(bankAccount.id)?.apply {
            depositAmount = bankAccount.depositAmount
            creditAmount = bankAccount.creditAmount
            enterpriseName = bankAccount.enterpriseName
        }?.toBankAccount()
    }

    override fun deleteBankAccount(id: Int): Boolean = transaction {
        BankAccountDao.findById(id)?.delete() != null
    }
}