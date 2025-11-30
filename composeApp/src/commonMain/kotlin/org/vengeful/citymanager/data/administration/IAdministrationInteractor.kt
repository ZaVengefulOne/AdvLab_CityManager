package org.vengeful.citymanager.data.administration

import org.vengeful.citymanager.models.AdministrationConfig
import org.vengeful.citymanager.models.CallStatus
import org.vengeful.citymanager.models.Enterprise
import org.vengeful.citymanager.models.emergencyShutdown.EmergencyShutdownStatusResponse

interface IAdministrationInteractor {
    suspend fun getAdministrationConfig(): AdministrationConfig
    suspend fun sendMessage(text: String, sender: String): Boolean

    suspend fun callEnterprise(enterprise: Enterprise): Boolean
    suspend fun getCallStatus(enterprise: Enterprise): CallStatus
    suspend fun resetCallStatus(enterprise: Enterprise): Boolean

    suspend fun activateEmergencyShutdown(durationMinutes: Int, password: String): Boolean
    suspend fun getEmergencyShutdownStatus(): EmergencyShutdownStatusResponse
}
