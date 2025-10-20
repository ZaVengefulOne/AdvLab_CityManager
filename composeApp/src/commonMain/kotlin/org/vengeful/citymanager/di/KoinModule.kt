package org.vengeful.citymanager.di

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module
import org.vengeful.citymanager.MainViewModel
import org.vengeful.citymanager.data.IServerInteractor
import org.vengeful.citymanager.data.ServerInteractor


val appModule = module {
    single<IServerInteractor> { ServerInteractor() }
    single { MainViewModel(get()) }
}

fun initKoin() = startKoin {
    modules(appModule)
}

object KoinInjector : KoinComponent {
    val mainViewModel: MainViewModel by inject()
}