package org.vengeful.citymanager.bankService.db

import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption
import org.vengeful.citymanager.models.BankAccount
import org.vengeful.citymanager.personService.db.PersonDao
import org.vengeful.citymanager.personService.db.Persons

object BankAccounts : IntIdTable("bank_accounts") {
    val personId = reference("person_id", Persons.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val enterpriseName = varchar("enterprise_name", 255).nullable()
    val creditAmount = double("credit_amount").default(0.0)
}

class BankAccountDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BankAccountDao>(BankAccounts)

    var personId by BankAccounts.personId
    var enterpriseName by BankAccounts.enterpriseName // НОВОЕ
    var creditAmount by BankAccounts.creditAmount

    var person by PersonDao optionalReferencedOn BankAccounts.personId

    fun toBankAccount() = BankAccount(
        id = id.value,
        personId = personId?.value,
        enterpriseName = enterpriseName,
        creditAmount = creditAmount
    )
}
