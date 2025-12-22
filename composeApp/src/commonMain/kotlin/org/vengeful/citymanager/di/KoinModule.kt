package org.vengeful.citymanager.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import io.ktor.client.HttpClient
import org.koin.core.context.GlobalContext
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.parameter.ParametersHolder
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.module
import org.vengeful.citymanager.data.administration.AdministrationInteractor
import org.vengeful.citymanager.data.administration.IAdministrationInteractor
import org.vengeful.citymanager.data.backup.BackupInteractor
import org.vengeful.citymanager.data.backup.IBackupInteractor
import org.vengeful.citymanager.data.bank.BankInteractor
import org.vengeful.citymanager.data.bank.IBankInteractor
import org.vengeful.citymanager.data.library.ILibraryInteractor
import org.vengeful.citymanager.data.library.LibraryInteractor
import org.vengeful.citymanager.data.medic.IMedicInteractor
import org.vengeful.citymanager.data.medic.MedicInteractor
import org.vengeful.citymanager.data.news.INewsInteractor
import org.vengeful.citymanager.data.news.NewsInteractor
import org.vengeful.citymanager.data.persons.IPersonInteractor
import org.vengeful.citymanager.data.persons.PersonInteractor
import org.vengeful.citymanager.data.users.AuthManager
import org.vengeful.citymanager.data.users.IUserInteractor
import org.vengeful.citymanager.data.users.UserInteractor
import org.vengeful.citymanager.screens.administration.AdministrationViewModel
import org.vengeful.citymanager.screens.backup.BackupViewModel
import org.vengeful.citymanager.screens.bank.BankViewModel
import org.vengeful.citymanager.screens.clicker.ClickerViewModel
import org.vengeful.citymanager.screens.commonLibrary.ArticleViewModel
import org.vengeful.citymanager.screens.commonLibrary.CommonLibraryViewModel
import org.vengeful.citymanager.screens.main.MainViewModel
import org.vengeful.citymanager.screens.medic.MedicViewModel
import org.vengeful.citymanager.screens.my_bank.MyBankViewModel
import org.vengeful.citymanager.screens.news.NewsItemViewModel
import org.vengeful.citymanager.screens.news.NewsViewModel
import org.vengeful.citymanager.screens.niis.SeveritCleaningViewModel
import org.vengeful.citymanager.screens.stocks.StockViewModel
import org.vengeful.citymanager.screens.userManagement.UserManagementViewModel
import kotlin.reflect.KClass


val appModule = module {
    single { AuthManager() }

    single<IPersonInteractor> { PersonInteractor(get()) }
    single<IUserInteractor> { UserInteractor(get()) }
    single<IBankInteractor> { BankInteractor(get()) }
    single<IBackupInteractor> { BackupInteractor(get()) }
    single<IAdministrationInteractor> { AdministrationInteractor(get()) }
    single<IMedicInteractor> { MedicInteractor(get()) }
    single<ILibraryInteractor> { LibraryInteractor(get()) }
    single<INewsInteractor> { NewsInteractor(get()) }

    factory { AdministrationViewModel(get(), get(), get(), get()) }
    factory { MainViewModel(get(), get()) }
    factory { ClickerViewModel(get(), get(), get(), get()) }
    factory { BankViewModel(get(), get(), get(), get()) }
    factory { BackupViewModel(get()) }
    factory { UserManagementViewModel(get(), get()) }
    factory { MedicViewModel(get(), get(), get(), get(), get()) }
    factory { MyBankViewModel(get(), get(), get(), get()) }
    factory { StockViewModel(get()) }
    factory { CommonLibraryViewModel(get()) }
    factory { ArticleViewModel(get()) }
    factory { NewsViewModel(get()) }
    factory { NewsItemViewModel(get()) }
    factory { SeveritCleaningViewModel() }
}

fun initKoin() = startKoin {
    modules(appModule)
}

@Composable
inline fun <reified T : ViewModel> koinViewModel(
    qualifier: Qualifier? = null,
    parameters: ParametersHolder? = null
): T {
    val koin = remember { GlobalContext.get() }
    val factory = remember(qualifier, parameters) {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                return if (parameters != null) {
                    koin.get(modelClass, qualifier) { parameters }
                } else {
                    koin.get(modelClass, qualifier)
                }
            }
        }
    }
    return viewModel(factory = factory)
}
