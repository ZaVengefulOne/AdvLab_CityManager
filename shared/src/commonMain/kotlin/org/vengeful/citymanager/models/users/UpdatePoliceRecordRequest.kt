package org.vengeful.citymanager.models.users

import kotlinx.serialization.Serializable
import org.vengeful.citymanager.models.police.PoliceRecord

@Serializable
data class UpdatePoliceRecordRequest(
    val record: PoliceRecord
)


