package org.vengeful.citymanager.personService

import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.Rights

class FakePersonRepository : IPersonRepository {

    val persons = mutableListOf(
        Person(id = 1, firstName = "John", lastName = "Doe", rights = Rights.Police),
        Person(id = 2, firstName = "Jane", lastName = "Doe", rights = Rights.Medic),
        Person(id = 3, firstName = "Vengeful", lastName = "Doe", rights = Rights.Any),
    )

    override fun allPersons(): List<Person> = persons

    override fun personsByRights(rights: Rights): List<Person> = persons.filter {
        it.rights == rights
    }

    override fun personByName(name: String, lastName: String): Person? = persons.find {
        it.firstName.equals(name, ignoreCase = true) && it.lastName.equals(lastName, ignoreCase = true)
    }

    override fun personById(id: Int): Person? = persons.find { it.id == id }


    override fun addPerson(person: Person) {
        if (personById(person.id) != null) {
            throw  IllegalArgumentException("Person with id ${person.id} already exists")
        }
        persons.add(person)
    }

    override fun removePerson(id: Int): Boolean {
        return persons.removeIf { it.id == id }
    }
}