package org.vengeful.citymanager.bankService.db

import org.jetbrains.exposed.sql.transactions.transaction
import org.vengeful.citymanager.bankService.IBankRepository
import org.vengeful.citymanager.models.BankAccount
import org.vengeful.citymanager.personService.IPersonRepository
import org.vengeful.citymanager.personService.db.PersonDao

class BankRepository(
    private val personRepository: IPersonRepository
) : IBankRepository {

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
        enterpriseName: String?,
        creditAmount: Double,
        personBalance: Double?
    ): BankAccount = transaction {
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
            // При создании счета кредит устанавливается, но НЕ добавляется к балансу автоматически
            this.creditAmount = creditAmount
        }

        // Для личных счетов обрабатываем только баланс (если указан)
        if (personId != null) {
            if (personBalance != null) {
                // Если указан баланс, устанавливаем его напрямую
                personRepository.updatePersonBalance(personId, personBalance)
                println("Created account for person $personId: set balance to $personBalance, credit: $creditAmount")
            }
            // Кредит при создании НЕ добавляется к балансу - его можно взять позже через редактирование
        }

        bankAccountDao.toBankAccount()
    }

    override fun updateBankAccount(bankAccount: BankAccount, personBalance: Double?): BankAccount? = transaction {
        BankAccountDao.findById(bankAccount.id)?.apply {
            val oldCreditAmount = creditAmount
            creditAmount = bankAccount.creditAmount

            enterpriseName = if (bankAccount.personId == null) {
                bankAccount.enterpriseName?.takeIf { it.isNotBlank() } ?: enterpriseName
            } else {
                null
            }

            // Для личных счетов
            if (personId != null) {
                if (personBalance != null) {
                    // Если указан баланс - устанавливаем его напрямую (пользователь изменил баланс вручную)
                    personRepository.updatePersonBalance(personId!!.value, personBalance)
                    println("Updated account ${bankAccount.id}: set balance to $personBalance (manual change)")
                } else {
                    // Если баланс не указан - применяем логику изменения кредита
                    if (oldCreditAmount != creditAmount) {
                        val creditDifference = creditAmount - oldCreditAmount
                        if (creditDifference > 0) {
                            // Увеличение кредита - добавляем к балансу
                            personRepository.addToPersonBalance(personId!!.value, creditDifference)
                            println("Updated account ${bankAccount.id}: added credit $creditDifference to balance")
                        } else if (creditDifference < 0) {
                            // Уменьшение кредита - вычитаем из баланса (если баланс достаточен)
                            val person = personRepository.personById(personId!!.value)
                            if (person != null && person.balance >= -creditDifference) {
                                personRepository.addToPersonBalance(personId!!.value, creditDifference)
                                println("Updated account ${bankAccount.id}: subtracted ${-creditDifference} from balance")
                            } else {
                                throw IllegalStateException("Недостаточно средств для уменьшения кредита. Баланс: ${person?.balance ?: 0.0}, Требуется: ${-creditDifference}")
                            }
                        }
                    }
                }
            }
        }?.toBankAccount()
    }

    override fun closeCredit(accountId: Int): BankAccount? = transaction {
        BankAccountDao.findById(accountId)?.let { accountDao ->
            if (accountDao.personId == null) {
                throw IllegalStateException("Cannot close credit for enterprise account")
            }

            val currentCredit = accountDao.creditAmount
            if (currentCredit <= 0) {
                throw IllegalStateException("Account has no credit to close")
            }

            val person = personRepository.personById(accountDao.personId!!.value)
            if (person == null) {
                throw IllegalStateException("Person not found")
            }

            if (person.balance < currentCredit) {
                throw IllegalStateException("Недостаточно средств для закрытия кредита. Баланс: ${person.balance}, Кредит: $currentCredit")
            }

            // Списываем кредит с баланса
            personRepository.addToPersonBalance(accountDao.personId!!.value, -currentCredit)

            // Устанавливаем кредит в 0
            accountDao.creditAmount = 0.0

            println("Closed credit for account $accountId: subtracted $currentCredit from balance")
            accountDao.toBankAccount()
        }
    }

    override fun getBankAccountByEnterpriseName(enterpriseName: String): BankAccount? = transaction {
        BankAccountDao.find { BankAccounts.enterpriseName eq enterpriseName }
            .firstOrNull()
            ?.toBankAccount()
    }

    override fun deleteBankAccount(id: Int): Boolean = transaction {
        BankAccountDao.findById(id)?.delete() != null
    }
}
