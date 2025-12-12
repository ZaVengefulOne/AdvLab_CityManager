package org.vengeful.citymanager.personService

import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.Rights

interface IPersonRepository {
    fun getCount(): Int
    fun allPersons(): List<Person>
    fun personsByRights(rights: List<Rights>): List<Person>
    fun personByRight(right: Rights): List<Person>?
    fun personByName(name: String, lastName: String?): Person?
    fun personById(id: Int): Person?
    fun addPerson(person: Person) : Person
    fun updatePerson(person: Person): Person?
    fun removePerson(id: Int): Boolean
    fun updatePersonBalance(personId: Int, amount: Double): Boolean
    fun addToPersonBalance(personId: Int, amount: Double): Boolean
}
