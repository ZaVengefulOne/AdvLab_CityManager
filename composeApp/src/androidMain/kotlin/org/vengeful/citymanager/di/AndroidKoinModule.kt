package org.vengeful.citymanager.di

import org.koin.dsl.module
import org.vengeful.citymanager.ui.auth.HackerLoginViewModel
import org.vengeful.citymanager.ui.UsersListViewModel
import org.vengeful.citymanager.ui.PersonsListViewModel

val androidModule = module {
    factory { HackerLoginViewModel() }
    factory { UsersListViewModel(get()) }
    factory { PersonsListViewModel(get(), get(), get()) }
}

