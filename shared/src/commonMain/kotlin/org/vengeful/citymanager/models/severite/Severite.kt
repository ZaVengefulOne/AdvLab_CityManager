package org.vengeful.citymanager.models.severite

import kotlinx.serialization.Serializable

@Serializable
data class Severite(
    val id: Int,
    val purity: SeveritePurity,
    val createdAt: Long
)
@Serializable
enum class SeveritePurity {
    CONTAMINATED,    // Загрязнённый (3 элемента)
    NORMAL,          // Обычный (5 элементов)
    CRYSTAL_CLEAR    // Кристально чистый (7 элементов)
}

fun getSeveritePurity(purity: SeveritePurity): String = when (purity) {
    SeveritePurity.CRYSTAL_CLEAR -> "Кристально чистый северит"
    SeveritePurity.NORMAL -> "Обычный северит"
    SeveritePurity.CONTAMINATED -> "Загрязнённый северит"
}

@Serializable
data class SeveriteCounts(
    val contaminated: Int,
    val normal: Int,
    val crystalClear: Int
)
@Serializable
data class AddSeveriteRequest(
    val purity: String
)
@Serializable
data class SellSeveriteRequest(
    val severiteIds: List<Int>
)
@Serializable
data class SellSeveriteResult(
    val totalAmount: Double,
    val soldCount: Int
)

