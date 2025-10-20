package org.vengeful.citymanager.data

import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.Rights

interface IServerInteractor {
    suspend fun getPersons(): List<Person>
    suspend fun getPersonById(id: Int): Person?
    suspend fun addPerson(person: Person)
    suspend fun deletePerson(id: Int)
    suspend fun getPersonsByRights(rights: Rights): List<Person>
}