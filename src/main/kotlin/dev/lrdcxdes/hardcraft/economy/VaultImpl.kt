package dev.lrdcxdes.hardcraft.economy

import dev.lrdcxdes.hardcraft.Hardcraft
import net.milkbowl.vault.economy.Economy
import net.milkbowl.vault.economy.EconomyResponse
import net.milkbowl.vault.economy.EconomyResponse.ResponseType
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

class VaultImpl : Economy {
    override fun isEnabled(): Boolean {
        return true
    }

    override fun currencyNamePlural(): String {
        // TODO
        return ""
    }

    override fun currencyNameSingular(): String {
        // TODO
        return ""
    }

    override fun format(v: Double): String {
        val bd = BigDecimal(v).setScale(2, RoundingMode.HALF_EVEN)
        return bd.toDouble().toString()
    }

    override fun fractionalDigits(): Int {
        return -1
    }

    @Deprecated("Deprecated in Java")
    override fun createPlayerAccount(name: String): Boolean {
        return createAccount(Bukkit.getOfflinePlayer(name).uniqueId)
    }

    override fun createPlayerAccount(player: OfflinePlayer): Boolean {
        return createAccount(player.uniqueId)
    }

    @Deprecated("Deprecated in Java")
    override fun createPlayerAccount(name: String, world: String): Boolean {
        return createAccount(Bukkit.getOfflinePlayer(name).uniqueId)
    }

    override fun createPlayerAccount(player: OfflinePlayer, world: String): Boolean {
        return createAccount(player.uniqueId)
    }

    private fun createAccount(uuid: UUID): Boolean {
        val havePlayer = Hardcraft.database.havePlayer(uuid.toString())
        return havePlayer
    }

    @Deprecated("Deprecated in Java")
    override fun depositPlayer(name: String, amount: Double): EconomyResponse {
        return deposit(Bukkit.getOfflinePlayer(name).uniqueId, amount)
    }

    override fun depositPlayer(player: OfflinePlayer, amount: Double): EconomyResponse {
        return deposit(player.uniqueId, amount)
    }

    @Deprecated("Deprecated in Java")
    override fun depositPlayer(name: String, world: String, amount: Double): EconomyResponse {
        return deposit(Bukkit.getOfflinePlayer(name).uniqueId, amount)
    }

    override fun depositPlayer(player: OfflinePlayer, world: String, amount: Double): EconomyResponse {
        return deposit(player.uniqueId, amount)
    }

    private fun deposit(uuid: UUID, amount: Double): EconomyResponse {
        if (!Hardcraft.database.changePlayerBalance(uuid.toString(), amount, "add")) {
            return EconomyResponse(0.0, 0.0, ResponseType.FAILURE, "Failed to deposit funds.")
        }
        return EconomyResponse(amount, getBalance(uuid), ResponseType.SUCCESS, "")
    }

    @Deprecated("Deprecated in Java")
    override fun getBalance(name: String): Double {
        return getBalance(Bukkit.getOfflinePlayer(name).uniqueId)
    }

    override fun getBalance(player: OfflinePlayer): Double {
        return getBalance(player.uniqueId)
    }

    @Deprecated("Deprecated in Java")
    override fun getBalance(name: String, world: String): Double {
        return getBalance(Bukkit.getOfflinePlayer(name).uniqueId)
    }

    override fun getBalance(player: OfflinePlayer, world: String): Double {
        return getBalance(player.uniqueId)
    }

    private fun getBalance(uuid: UUID): Double {
        return Hardcraft.database.getBalance(uuid.toString())
    }

    override fun getName(): String {
        return "Economy"
    }

    @Deprecated("Deprecated in Java")
    override fun has(name: String, amount: Double): Boolean {
        return has(Bukkit.getOfflinePlayer(name).uniqueId, amount)
    }

    override fun has(player: OfflinePlayer, amount: Double): Boolean {
        return has(player.uniqueId, amount)
    }

    @Deprecated("Deprecated in Java")
    override fun has(name: String, world: String, amount: Double): Boolean {
        return has(Bukkit.getOfflinePlayer(name).uniqueId, amount)
    }

    override fun has(player: OfflinePlayer, world: String, amount: Double): Boolean {
        return has(player.uniqueId, amount)
    }

    private fun has(uuid: UUID, amount: Double): Boolean {
        return Hardcraft.database.havePlayer(uuid.toString(), amount)
    }

    @Deprecated("Deprecated in Java")
    override fun hasAccount(name: String): Boolean {
        return hasAccount(Bukkit.getOfflinePlayer(name).uniqueId)
    }

    override fun hasAccount(player: OfflinePlayer): Boolean {
        return hasAccount(player.uniqueId)
    }

    @Deprecated("Deprecated in Java")
    override fun hasAccount(name: String, world: String): Boolean {
        return hasAccount(Bukkit.getOfflinePlayer(name).uniqueId)
    }

    override fun hasAccount(player: OfflinePlayer, world: String): Boolean {
        return hasAccount(player.uniqueId)
    }

    private fun hasAccount(uuid: UUID): Boolean {
        return Hardcraft.database.havePlayer(uuid.toString())
    }

    @Deprecated("Deprecated in Java")
    override fun withdrawPlayer(name: String, amount: Double): EconomyResponse {
        return withdraw(Bukkit.getOfflinePlayer(name).uniqueId, amount)
    }

    override fun withdrawPlayer(player: OfflinePlayer, amount: Double): EconomyResponse {
        return withdraw(player.uniqueId, amount)
    }

    @Deprecated("Deprecated in Java")
    override fun withdrawPlayer(name: String, world: String, amount: Double): EconomyResponse {
        return withdraw(Bukkit.getOfflinePlayer(name).uniqueId, amount)
    }

    override fun withdrawPlayer(player: OfflinePlayer, world: String, amount: Double): EconomyResponse {
        return withdraw(player.uniqueId, amount)
    }

    private fun withdraw(uuid: UUID, amount: Double): EconomyResponse {
        if (!Hardcraft.database.changePlayerBalance(uuid.toString(), amount, "remove")) {
            return EconomyResponse(0.0, 0.0, ResponseType.FAILURE, "Failed to withdraw funds.")
        }
        return EconomyResponse(amount, getBalance(uuid), ResponseType.SUCCESS, "")
    }

    override fun hasBankSupport(): Boolean {
        return false
    }

    override fun getBanks(): List<String>? {
        return null
    }

    @Deprecated("Deprecated in Java", ReplaceWith("null"))
    override fun isBankMember(arg0: String, arg1: String): EconomyResponse? {
        return null
    }

    override fun isBankMember(arg0: String, arg1: OfflinePlayer): EconomyResponse? {
        return null
    }

    @Deprecated("Deprecated in Java", ReplaceWith("null"))
    override fun isBankOwner(arg0: String, arg1: String): EconomyResponse? {
        return null
    }

    override fun isBankOwner(arg0: String, arg1: OfflinePlayer): EconomyResponse? {
        return null
    }

    override fun bankBalance(arg0: String): EconomyResponse? {
        return null
    }

    override fun bankDeposit(arg0: String, arg1: Double): EconomyResponse? {
        return null
    }

    override fun bankHas(arg0: String, arg1: Double): EconomyResponse? {
        return null
    }

    override fun bankWithdraw(arg0: String, arg1: Double): EconomyResponse? {
        return null
    }

    @Deprecated("Deprecated in Java", ReplaceWith("null"))
    override fun createBank(arg0: String, arg1: String): EconomyResponse? {
        return null
    }

    override fun createBank(arg0: String, arg1: OfflinePlayer): EconomyResponse? {
        return null
    }

    override fun deleteBank(arg0: String): EconomyResponse? {
        return null
    }
}