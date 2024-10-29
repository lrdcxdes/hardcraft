package dev.lrdcxdes.hardcraft.economy.shop

import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random

class MarketSystem(
    private val initialPrice: Double,
    private val initialLiquidity: Double,
    var currentLiquidity: Double,
    private val config: MarketConfig = MarketConfig()
) {
    data class MarketConfig(
        val minPriceMultiplier: Double = 0.45,        // Минимальный множитель цены продажи
        val maxPriceMultiplier: Double = 2.5,         // Максимальный множитель цены
        val volatilityFactor: Double = 0.15,          // Фактор волатильности цен
        val demandMultiplier: Double = 1.2,           // Множитель спроса
        val supplyMultiplier: Double = 0.8,           // Множитель предложения
        val eventImpactFactor: Double = 0.25,         // Влияние событий на цены
        val timeDecayFactor: Double = 0.95,           // Фактор времени на стабилизацию цен
        val seasonalityEnabled: Boolean = true,        // Влияние сезонности
        val manipulationThreshold: Double = 100.0,     // Порог для определения манипуляций
        val priceChangeLimit: Double = 0.3             // Максимальное изменение цены за одну транзакцию
    )

    private val k = initialPrice * initialLiquidity
    private var lastPrice = initialPrice
    private var demandIndex = 1.0
    private var supplyIndex = 1.0
    private var priceHistory = mutableListOf<PricePoint>()
    private var marketEvents = mutableListOf<MarketEvent>()

    private var cachedAskPrice: Double? = null
    private var cachedBidPrice: Double? = null
    private var pricesCacheTimestamp: Long = 0
    private val CACHE_DURATION = 30000 // 30 секунд для кэша цен

    data class PricePoint(
        val timestamp: Long,
        val price: Double,
        val volume: Double,
        val type: TransactionType
    )

    data class MarketEvent(
        val type: EventType,
        val impact: Double,
        val duration: Long,
        val startTime: Long
    )

    enum class EventType {
        FESTIVAL,          // Игровые фестивали повышают спрос
        WAR,              // Военные действия повышают цены на ресурсы
        NATURAL_DISASTER, // Природные катастрофы влияют на доступность
        TRADE_BOOM,       // Торговый бум увеличивает объемы
        SHORTAGE         // Дефицит ресурсов
    }

    enum class TransactionType {
        BUY, SELL
    }

    // Получение цены покупки с возможным кэшированием
    fun getAskPrice(): Double {
        val currentTime = System.currentTimeMillis()
        if (cachedAskPrice == null || currentTime - pricesCacheTimestamp > CACHE_DURATION) {
            cachedAskPrice = calculateAskPrice()
            cachedBidPrice = calculateBidPrice()
            pricesCacheTimestamp = currentTime
        }
        return cachedAskPrice!!
    }

    // Получение цены продажи с возможным кэшированием
    fun getBidPrice(): Double {
        val currentTime = System.currentTimeMillis()
        if (cachedBidPrice == null || currentTime - pricesCacheTimestamp > CACHE_DURATION) {
            cachedAskPrice = calculateAskPrice()
            cachedBidPrice = calculateBidPrice()
            pricesCacheTimestamp = currentTime
        }
        return cachedBidPrice!!
    }

    // Метод для принудительного сброса кэша цен
    private fun invalidatePriceCache() {
        cachedAskPrice = null
        cachedBidPrice = null
        pricesCacheTimestamp = 0
    }


    // Внутренний метод расчета цены покупки
    private fun calculateAskPrice(): Double {
        var price = (k / currentLiquidity) * getDemandMultiplier()
        price *= getEventMultiplier()
        price *= getSeasonalMultiplier()
        price = applyVolatility(price)
        price = limitPriceChange(price)
        return price.coerceIn(
            initialPrice * config.minPriceMultiplier,
            initialPrice * config.maxPriceMultiplier
        )
    }

    // Внутренний метод расчета цены продажи
    private fun calculateBidPrice(): Double {
        var price = (k / currentLiquidity) * getSupplyMultiplier()
        price *= getEventMultiplier()
        price *= getSeasonalMultiplier()
        price = applyVolatility(price)
        price = limitPriceChange(price)
        return price.coerceIn(
            initialPrice * config.minPriceMultiplier,
            initialPrice * config.maxPriceMultiplier
        )
    }

    // Выполнение покупки с обновлением индексов
    fun buy(amount: Double): Double {
        require(amount <= currentLiquidity) { "Insufficient liquidity" }

        val price = calculateAskPrice()
        currentLiquidity -= amount

        // Обновляем индексы спроса и предложения
        updateDemandIndex(amount, TransactionType.BUY)

        // Записываем транзакцию
        recordTransaction(price, amount, TransactionType.BUY)

        // Проверяем на манипуляции
        checkForManipulation()

        // Сбрасываем кэш после транзакции
        invalidatePriceCache()

        return price
    }

    // Выполнение продажи с обновлением индексов
    fun sell(amount: Double): Double {
        require(currentLiquidity + amount <= initialLiquidity * 2) { "Exceeds maximum liquidity" }

        val price = calculateBidPrice()
        currentLiquidity += amount

        // Обновляем индексы спроса и предложения
        updateSupplyIndex(amount, TransactionType.SELL)

        // Записываем транзакцию
        recordTransaction(price, amount, TransactionType.SELL)

        // Проверяем на манипуляции
        checkForManipulation()

        // Сбрасываем кэш после транзакции
        invalidatePriceCache()

        return price
    }

    // Расчет цен для массовых операций
    fun calculateBulkPrice(amount: Double, type: TransactionType): Double {
        var totalPrice = 0.0
        var tempLiquidity = currentLiquidity

        for (i in 1..amount.toInt()) {
            val price = when (type) {
                TransactionType.BUY -> k / tempLiquidity * getDemandMultiplier()
                TransactionType.SELL -> k / tempLiquidity * getSupplyMultiplier()
            }
            totalPrice += price
            tempLiquidity += if (type == TransactionType.BUY) -1.0 else 1.0
        }

        return totalPrice
    }

    // Получение мультипликатора спроса
    private fun getDemandMultiplier(): Double {
        return config.demandMultiplier * demandIndex
    }

    // Получение мультипликатора предложения
    private fun getSupplyMultiplier(): Double {
        return config.supplyMultiplier * supplyIndex
    }

    // Применение волатильности к цене
    private fun applyVolatility(price: Double): Double {
        val volatility = Random.nextDouble(-config.volatilityFactor, config.volatilityFactor)
        return price * (1 + volatility)
    }

    // Получение мультипликатора событий
    private fun getEventMultiplier(): Double {
        val currentTime = System.currentTimeMillis()
        return marketEvents
            .filter { event ->
                currentTime - event.startTime <= event.duration
            }
            .fold(1.0) { acc, event ->
                acc * (1 + event.impact * config.eventImpactFactor)
            }
    }

    // Получение сезонного мультипликатора
    private fun getSeasonalMultiplier(): Double {
        if (!config.seasonalityEnabled) return 1.0

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        var multiplier = 1.0

        // Увеличение цен в прайм-тайм
        if (hour in 17..23) {
            multiplier *= 1.1
        }

        // Увеличение цен в выходные
        if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
            multiplier *= 1.15
        }

        return multiplier
    }

    // Ограничение изменения цены
    private fun limitPriceChange(newPrice: Double): Double {
        val maxChange = lastPrice * config.priceChangeLimit
        val limitedPrice = newPrice.coerceIn(
            lastPrice - maxChange,
            lastPrice + maxChange
        )
        lastPrice = limitedPrice
        return limitedPrice
    }

    // Запись транзакции в историю
    private fun recordTransaction(price: Double, amount: Double, type: TransactionType) {
        priceHistory.add(
            PricePoint(
                System.currentTimeMillis(),
                price,
                amount,
                type
            )
        )

        // Ограничиваем размер истории
        if (priceHistory.size > 1000) {
            priceHistory.removeAt(0)
        }
    }

    // Обновление индекса спроса
    private fun updateDemandIndex(amount: Double, type: TransactionType) {
        val impact = amount / initialLiquidity
        demandIndex *= when (type) {
            TransactionType.BUY -> (1 + impact * 0.1)
            TransactionType.SELL -> (1 - impact * 0.05)
        }
        demandIndex = demandIndex.coerceIn(0.5, 2.0)
    }

    // Обновление индекса предложения
    private fun updateSupplyIndex(amount: Double, type: TransactionType) {
        val impact = amount / initialLiquidity
        supplyIndex *= when (type) {
            TransactionType.SELL -> (1 + impact * 0.1)
            TransactionType.BUY -> (1 - impact * 0.05)
        }
        supplyIndex = supplyIndex.coerceIn(0.5, 2.0)
    }

    // Проверка на манипуляции рынком
    private fun checkForManipulation() {
        val recentTransactions = priceHistory.takeLast(10)
        if (recentTransactions.size < 10) return

        val volumes = recentTransactions.map { it.volume }
        val averageVolume = volumes.average()
        val volumeVariance = volumes.map { (it - averageVolume).pow(2) }.average()

        if (volumeVariance > config.manipulationThreshold) {
            // Применяем штрафной коэффициент к индексам
            demandIndex *= 0.9
            supplyIndex *= 0.9
        }
    }

    // Добавление рыночного события
    fun addMarketEvent(type: EventType, impact: Double, duration: Long) {
        marketEvents.add(
            MarketEvent(
                type,
                impact,
                duration,
                System.currentTimeMillis()
            )
        )
    }

    // Получение статистики рынка
    fun getMarketStatistics(): MarketStatistics {
        val recentTransactions = priceHistory.takeLast(100)
        return MarketStatistics(
            averagePrice = recentTransactions.map { it.price }.average(),
            volumeTraded = recentTransactions.sumOf { it.volume },
            priceVolatility = calculateVolatility(recentTransactions),
            demandIndex = demandIndex,
            supplyIndex = supplyIndex,
            activeEvents = marketEvents.count {
                System.currentTimeMillis() - it.startTime <= it.duration
            }
        )
    }

    // Расчет волатильности цен
    private fun calculateVolatility(transactions: List<PricePoint>): Double {
        if (transactions.isEmpty()) return 0.0
        val prices = transactions.map { it.price }
        val average = prices.average()
        return sqrt(prices.map { (it - average).pow(2) }.average())
    }

    data class MarketStatistics(
        val averagePrice: Double,
        val volumeTraded: Double,
        val priceVolatility: Double,
        val demandIndex: Double,
        val supplyIndex: Double,
        val activeEvents: Int
    )
}
