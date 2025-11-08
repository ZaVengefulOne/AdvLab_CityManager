package org.vengeful.citymanager.data

import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.Rights

interface IPersonInteractor {
    suspend fun getPersons(): List<Person>
    suspend fun getPersonById(id: Int): Person?
    suspend fun getPersonByName(name: String, lastName: String): Person?
    suspend fun getPersonsByRights(rights: List<Rights>): List<Person>
    suspend fun addPerson(person: Person)
    suspend fun updatePerson(person: Person)
    suspend fun deletePerson(id: Int)
    suspend fun getPersonsByRights(rights: Rights): List<Person>
}