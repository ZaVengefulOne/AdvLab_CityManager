package org.vengeful.citymanager

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.vengeful.citymanager.data.IServerInteractor
import org.vengeful.citymanager.data.ServerInteractor
import org.vengeful.citymanager.models.Person
import org.vengeful.citymanager.models.Rights


class MainViewModel(private val serverInteractor: IServerInteractor)  {

    val scope = CoroutineScope(Dispatchers.IO)
    private val _persons = MutableStateFlow<List<Person>>(emptyList())
    val persons: StateFlow<List<Person>> get() = _persons

    private val _curPerson = MutableStateFlow<Person?>(null)
    val curPerson: StateFlow<Person?> get() = _curPerson

    fun getPersons(){
        scope.launch {
            val persons = serverInteractor.getPersons()
            _persons.value = persons
        }
    }

    fun getPersonById(id: Int){
        scope.launch {
            val person = serverInteractor.getPersonById(id)
            _curPerson.value = person
        }
    }

    fun getPersonsByRights(rights: Rights){
        scope.launch {
            val persons = serverInteractor.getPersonsByRights(rights)
            _persons.value = persons
        }
    }

    fun addPerson(person: Person){
        scope.launch {
            serverInteractor.addPerson(person)
        }
    }

    fun deletePerson(id: Int){
        scope.launch {
            serverInteractor.deletePerson(id)
        }
    }
}