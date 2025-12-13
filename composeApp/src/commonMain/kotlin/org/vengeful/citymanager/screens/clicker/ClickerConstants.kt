package org.vengeful.citymanager.screens.clicker

object ClickerConstants {
    // Цена улучшения для сохранения прогресса (в кликах)
    const val SAVE_PROGRESS_UPGRADE_PRICE = 1000

    // Курс обмена: сколько кликов за 1 единицу валюты
    const val CLICKS_TO_MONEY_EXCHANGE_RATE = 50

    // Система прокачки множителя кликов
    const val CLICK_MULTIPLIER_BASE_PRICE = 100  // Стоимость первого улучшения
    const val CLICK_MULTIPLIER_PRICE_MULTIPLIER = 1.5  // Множитель стоимости каждого следующего уровня
}
