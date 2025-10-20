package org.vengeful.citymanager

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.ui.tooling.preview.Preview

import org.vengeful.citymanager.di.KoinInjector
import org.vengeful.citymanager.di.initKoin
import org.vengeful.citymanager.uikit.PersonDialog

@Composable
@Preview
fun App() {
    initKoin()
    MaterialTheme {
        val mainViewModel = KoinInjector.mainViewModel
        val persons = mainViewModel.persons.collectAsState().value
        val curPerson = mainViewModel.curPerson.collectAsState().value

        var showAddDialog by remember { mutableStateOf(false) }
        val getId = remember { mutableStateOf("") }
        val delId = remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primaryContainer)
                .safeContentPadding()
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Button(onClick = {
                    mainViewModel.getPersons()
                }) {
                    Text(text = "Получить всех")
                }
                Row {
                    Button(onClick = {
                        mainViewModel.getPersonById(getId.value.toInt())
                    }) {
                        Text(text = "Получить человека")
                    }
                    TextField(
                        value = getId.value,
                        onValueChange = {
                            getId.value = it
                        },
                        modifier = Modifier,
                        enabled = true
                    )
                }
                Row {
                    Button(onClick = {
                        mainViewModel.deletePerson(delId.value.toInt())
                    }) {
                        Text(text = "Удалить человека")
                    }
                    TextField(
                        value = delId.value,
                        onValueChange = {
                            delId.value = it
                        },
                        modifier = Modifier,
                        enabled = true
                    )
                }
                Button(onClick = { showAddDialog = true }) {
                    Text("Добавить человека")
                }
            }
            if (showAddDialog) {
                PersonDialog(
                    onDismiss = { showAddDialog = false },
                    onAddPerson = { person ->
                        mainViewModel.addPerson(person)
                    }
                )
            }
            Column {
                Text("Все записи: $persons")
                Text("Полученная запись: $curPerson")
            }
        }
    }
}