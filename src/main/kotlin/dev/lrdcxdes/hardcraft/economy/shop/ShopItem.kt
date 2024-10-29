package dev.lrdcxdes.hardcraft.economy.shop

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class ShopItem(
    val id: Int,
    initPrice: Double,
    val volume: Double,
    val material: Material,
    dbVolume: Double
) {
    private val pool: MarketSystem = MarketSystem(initPrice, volume, dbVolume)

    val buyPrice: Double
        get() = pool.getAskPrice()

    val sellPrice: Double
        get() = pool.getBidPrice()

    val nowVolume: Double
        get() = pool.currentLiquidity

    var lore: List<Component> = listOf()

    fun toItemStack(): ItemStack {
        return ItemStack(material)
    }

    fun calculateBullkBuyPrice(amount: Double): Double {
        return pool.calculateBulkPrice(amount, MarketSystem.TransactionType.BUY)
    }

    fun calculateBulkSellPrice(amount: Double): Double {
        return pool.calculateBulkPrice(amount, MarketSystem.TransactionType.SELL)
    }

    fun buy(amount: Double) {
        pool.buy(amount)
    }

    fun sell(amount: Double) {
        pool.sell(amount)
    }
}