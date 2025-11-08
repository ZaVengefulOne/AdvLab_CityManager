package org.vengeful.citymanager.di

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module
import org.vengeful.citymanager.screens.administration.AdministrationViewModel
import org.vengeful.citymanager.data.IPersonInteractor
import org.vengeful.citymanager.data.users.IUserInteractor
import org.vengeful.citymanager.data.PersonInteractor
import org.vengeful.citymanager.data.users.AuthManager
import org.vengeful.citymanager.data.users.UserInteractor
import org.vengeful.citymanager.screens.main.MainViewModel


val appModule = module {
    single { AuthManager() }

    single<IPersonInteractor> { PersonInteractor() }
    single<IUserInteractor> { UserInteractor(get()) }

    single { AdministrationViewModel(get()) }
    single { MainViewModel(get()) }
}

fun initKoin() = startKoin {
    modules(appModule)
}

object KoinInjector : KoinComponent {
    val administrationViewModel: AdministrationViewModel by inject()
    val mainViewModel: MainViewModel by inject()
}