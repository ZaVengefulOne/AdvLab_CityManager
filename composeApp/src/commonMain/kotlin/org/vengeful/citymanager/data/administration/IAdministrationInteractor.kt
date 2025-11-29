package org.vengeful.citymanager.data.administration

import org.vengeful.citymanager.models.AdministrationConfig

interface IAdministrationInteractor {
    suspend fun getAdministrationConfig(): AdministrationConfig
}
