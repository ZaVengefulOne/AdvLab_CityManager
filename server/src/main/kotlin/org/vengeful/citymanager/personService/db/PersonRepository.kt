package org.vengeful.citymanager.personService.db

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.Rights
import org.vengeful.citymanager.personService.IPersonRepository
import org.vengeful.citymanager.personService.db.PersonRights.personId
import org.vengeful.citymanager.personService.db.PersonRights.rightId

class PersonRepository : IPersonRepository {

    fun initializeRights() = transaction {
        Rights.entries.forEach { rights ->
            RightDao.getOrCreate(rights)
        }
    }

    // Получить всех
    override fun allPersons(): List<Person> = transaction {
        PersonDao.all().map { it.toPerson() }
    }

    // Поиск по определённому праву
    override fun personByRight(right: Rights): List<Person> = transaction {
        val rightDao = RightDao.find { RightsTable.name eq right.name }.firstOrNull()
        rightDao?.persons?.map { it.toPerson() } ?: emptyList()
    }

    // Поиск по нескольким правам
    override fun personsByRights(rights: List<Rights>): List<Person> = transaction {
        val rightNames = rights.map { it.name }
        PersonDao.find {
            Persons.id inSubQuery PersonRights
                .slice(PersonRights.personId)
                .select {
                    PersonRights.rightId inList RightDao.find { RightsTable.name inList rightNames }.map { it.id }
                }
        }.map { it.toPerson() }
    }

    override fun personByName(name: String, lastName: String): Person? = transaction {
        PersonDao.find { (Persons.firstName eq name) and (Persons.lastName eq lastName) }.map { it.toPerson() }
            .firstOrNull()
    }

    override fun personById(id: Int): Person? = transaction {
        PersonDao.findById(id)?.toPerson()
    }

    override fun addPerson(person: Person): Person = transaction {
        val personDao = PersonDao.new(person.id) {
            firstName = person.firstName
            lastName = person.lastName
        }

        // Добавляем права
        person.rights.forEach { right ->
            val rightDao = RightDao.getOrCreate(right)
            PersonRights.insert {
                it[personId] = personDao.id
                it[rightId] = rightDao.id
            }
        }

        personDao.toPerson()
    }

    override fun updatePerson(person: Person): Person? = transaction {
        PersonDao.findById(person.id)?.apply {
            firstName = person.firstName
            lastName = person.lastName

            // Получаем текущие права как Set для удобства сравнения
            val currentRightsSet = rights.map { it.right }.toSet()
            val newRightsSet = person.rights.toSet()

            // Права для удаления
            val rightsToRemove = currentRightsSet - newRightsSet
            rightsToRemove.forEach { rightToRemove ->
                val rightDao = RightDao.find { RightsTable.name eq rightToRemove.name }.first()
                PersonRights.deleteWhere {
                    (PersonRights.personId eq id) and (PersonRights.rightId eq rightDao.id)
                }
            }

            // Права для добавления
            val rightsToAdd = newRightsSet - currentRightsSet
            rightsToAdd.forEach { rightToAdd ->
                val rightDao = RightDao.getOrCreate(rightToAdd)
                PersonRights.insert {
                    it[personId] = id
                    it[rightId] = rightDao.id
                }
            }
        }?.toPerson()
    }

    override fun removePerson(id: Int): Boolean = transaction {
        PersonRights.deleteWhere { PersonRights.personId eq id }
        PersonDao.findById(id)?.delete() != null
    }

    fun clearDB() = transaction {
        PersonDao.all().forEach { it.delete() }
    }
}