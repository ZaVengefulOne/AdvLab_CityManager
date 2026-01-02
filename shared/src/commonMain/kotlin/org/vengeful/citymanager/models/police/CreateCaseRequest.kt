package org.vengeful.citymanager.models.police

import kotlinx.serialization.Serializable

@Serializable
data class CreateCaseRequest(
    val complainantPersonId: Int? = null,
    val complainantName: String,
    val suspectPersonId: Int? = null,
    val suspectName: String,
    val statementText: String,
    val violationArticle: String,
    val status: CaseStatus = CaseStatus.OPEN
)



