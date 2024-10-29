package dev.lrdcxdes.hardcraft.sql

import dev.lrdcxdes.hardcraft.Hardcraft
import org.bukkit.Material
import java.sql.Connection
import java.sql.DriverManager
import java.io.File
import java.sql.ResultSet

class DatabaseManager {
    private val plugin = Hardcraft.instance
    lateinit var connection: Connection
    private val dbFile = File(plugin.dataFolder, "database.db")

    fun connect(): Boolean {
        try {
            // Ensure plugin directory exists
            if (!plugin.dataFolder.exists()) {
                plugin.dataFolder.mkdirs()
            }

            // Load the SQLite JDBC driver
            Class.forName("org.sqlite.JDBC")

            // Create connection
            connection = DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")
            plugin.logger.info("Connected to database!")
            return true
        } catch (e: Exception) {
            plugin.logger.severe("Error connecting to database: ${e.message}")
            return false
        }
    }

    fun createTables() {
        try {
            this.connection.prepareStatement(
                """
                    CREATE TABLE IF NOT EXISTS project9_players (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        uuid TEXT NOT NULL,
                        username VARCHAR(16) NOT NULL,
                        balance DOUBLE NOT NULL,
                        UNIQUE(uuid)
                    )
                """.trimIndent()
            ).use { ps ->
                // Players table (minimal version needed for friends)
                ps.execute()
                plugin.logger.info("Created table 'project9_players'!")
            }

            // Friends table
            connection.prepareStatement(
                """
                    CREATE TABLE IF NOT EXISTS project9_friends (
                        player_uuid TEXT NOT NULL,
                        friend_uuid TEXT NOT NULL,
                        PRIMARY KEY (player_uuid, friend_uuid),
                        FOREIGN KEY (player_uuid) REFERENCES project9_players(uuid) ON DELETE CASCADE,
                        FOREIGN KEY (friend_uuid) REFERENCES project9_players(uuid) ON DELETE CASCADE
                    )
                """.trimIndent()
            ).use { ps ->
                ps.execute()
                plugin.logger.info("Created table 'project9_friends'!")
            }

            connection.prepareStatement(
                """
                CREATE TABLE IF NOT EXISTS project9_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    material VARCHAR(48) NOT NULL,
                    init_price DOUBLE NOT NULL,
                    init_volume DOUBLE NOT NULL,
                    volume DOUBLE NOT NULL
                )
                """.trimIndent()
            ).use { ps ->
                ps.execute()
                plugin.logger.info("Created table 'project9_items'!")
            }

            // config table
            connection.prepareStatement(
                """
                CREATE TABLE IF NOT EXISTS project9_config (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    k VARCHAR(256) NOT NULL,
                    v VARCHAR(256) NOT NULL,
                    t VARCHAR(16) NOT NULL,
                    UNIQUE (k)
                )
                """.trimIndent()
            ).use { ps ->
                ps.execute()
                plugin.logger.info("Created table 'project9_config'!")
            }
        } catch (e: Exception) {
            plugin.logger.severe("Error creating tables: ${e.message}")
        }
    }

    fun disconnect() {
        if (::connection.isInitialized && !connection.isClosed) {
            connection.close()
            plugin.logger.info("Disconnected from database!")
        }
    }

    fun havePlayer(uuid: String): Boolean {
        try {
            connection.prepareStatement(
                """
                SELECT TRUE FROM project9_players WHERE uuid = ?
            """.trimIndent()
            ).use { statement ->
                statement.setString(1, uuid)
                val result = statement.executeQuery()
                while (result.next()) return true
            }
        } catch (e: Exception) {
            plugin.logger.severe("Error creating/updating player: ${e.message}")
        }
        return false
    }

    fun createPlayer(uuid: String, username: String) {
        try {
            connection.prepareStatement(
                """
                INSERT OR REPLACE INTO project9_players (uuid, username, balance)
                VALUES (?, ?, 0.0)
            """.trimIndent()
            ).use { statement ->
                statement.setString(1, uuid)
                statement.setString(2, username)
                statement.execute()
            }
        } catch (e: Exception) {
            plugin.logger.severe("Error creating/updating player: ${e.message}")
        }
    }

    fun changePlayerBalance(uuid: String, amount: Double, action: String): Boolean {
        // Change player's balance
        try {
            this.connection.prepareStatement(
                """
                    UPDATE project9_players
                    SET balance = CASE
                        WHEN ? = 'set' THEN ?
                        WHEN ? = 'add' THEN balance + ?
                        WHEN ? = 'remove' THEN balance - ?
                    END
                    WHERE uuid = ?
                    """.trimIndent()
            ).use { sql ->
                sql.setString(1, action)
                sql.setDouble(2, amount)
                sql.setString(3, action)
                sql.setDouble(4, amount)
                sql.setString(5, action)
                sql.setDouble(6, amount)
                sql.setString(7, uuid)
                sql.execute()
                return true
            }
        } catch (e: Exception) {
            plugin.logger.severe("Error changing player balance: ${e.message}")
        }
        return false
    }

    fun addFriend(playerUuid: String, friendUuid: String) {
        try {
            connection.prepareStatement(
                """
                INSERT OR IGNORE INTO project9_friends (player_uuid, friend_uuid)
                VALUES (?, ?)
            """.trimIndent()
            ).use { statement ->
                statement.setString(1, playerUuid)
                statement.setString(2, friendUuid)
                statement.execute()
            }
        } catch (e: Exception) {
            plugin.logger.severe("Error adding friend: ${e.message}")
        }
    }

    fun removeFriend(playerUuid: String, friendUuid: String) {
        try {
            connection.prepareStatement(
                """
                DELETE FROM project9_friends
                WHERE player_uuid = ? AND friend_uuid = ?
            """.trimIndent()
            ).use { statement ->
                statement.setString(1, playerUuid)
                statement.setString(2, friendUuid)
                statement.execute()
            }
        } catch (e: Exception) {
            plugin.logger.severe("Error removing friend: ${e.message}")
        }
    }

    fun getFriends(playerUuid: String): List<String> {
        val friends = mutableListOf<String>()
        try {
            connection.prepareStatement(
                """
                SELECT f.friend_uuid, p.username
                FROM project9_friends f
                JOIN project9_players p ON f.friend_uuid = p.uuid
                WHERE f.player_uuid = ?
            """.trimIndent()
            ).use { statement ->
                statement.setString(1, playerUuid)
                val result = statement.executeQuery()
                while (result.next()) {
                    friends.add(result.getString("username"))
                }
            }
        } catch (e: Exception) {
            plugin.logger.severe("Error getting friends: ${e.message}")
        }
        return friends
    }

    fun isFriend(playerUuid: String, friendUuid: String): Boolean {
        try {
            connection.prepareStatement(
                """
                SELECT COUNT(*)
                FROM project9_friends
                WHERE player_uuid = ? AND friend_uuid = ?
            """.trimIndent()
            ).use { statement ->
                statement.setString(1, playerUuid)
                statement.setString(2, friendUuid)
                val result = statement.executeQuery()
                if (result.next()) {
                    return result.getInt(1) > 0
                }
            }
        } catch (e: Exception) {
            plugin.logger.severe("Error checking friend status: ${e.message}")
        }
        return false
    }

    fun getPlayerUsername(uuid: String): String {
        try {
            connection.prepareStatement(
                """
                SELECT username
                FROM project9_players
                WHERE uuid = ?
            """.trimIndent()
            ).use { statement ->
                statement.setString(1, uuid)
                val result = statement.executeQuery()
                if (result.next()) {
                    return result.getString("username")
                }
            }
        } catch (e: Exception) {
            plugin.logger.severe("Error getting player username: ${e.message}")
        }
        return ""
    }

    fun getPlayerUUID(targetName: String): String? {
        try {
            connection.prepareStatement(
                """
                SELECT uuid
                FROM project9_players
                WHERE username = ?
            """.trimIndent()
            ).use { statement ->
                statement.setString(1, targetName)
                val result = statement.executeQuery()
                if (result.next()) {
                    return result.getString("uuid")
                }
            }
        } catch (e: Exception) {
            plugin.logger.severe("Error getting player uuid: ${e.message}")
        }
        return ""
    }

    fun getBalance(uuid: String): Double {
        try {
            connection.prepareStatement(
                """
                SELECT balance
                FROM project9_players
                WHERE uuid = ?
            """.trimIndent()
            ).use { statement ->
                statement.setString(1, uuid)
                val result = statement.executeQuery()
                if (result.next()) {
                    return result.getDouble("balance")
                }
            }
        } catch (e: Exception) {
            plugin.logger.severe("Error getting player uuid: ${e.message}")
        }
        return 0.0
    }

    fun havePlayer(uuid: String, amount: Double): Boolean {
        try {
            connection.prepareStatement(
                """
                SELECT balance
                FROM project9_players
                WHERE uuid = ?
            """.trimIndent()
            ).use { statement ->
                statement.setString(1, uuid)
                val result = statement.executeQuery()
                if (result.next()) {
                    return result.getDouble("balance") >= amount
                }
            }
        } catch (e: Exception) {
            plugin.logger.severe("Error getting player uuid: ${e.message}")
        }
        return false
    }

    fun hasItem(material: Material): Boolean {
        // Check if item exists
        try {
            connection.prepareStatement(
                """
                    SELECT COUNT(*)
                    FROM project9_items
                    WHERE material = ?
                    """.trimIndent()
            ).use { sql ->
                sql.setString(1, material.name)
                val result = sql.executeQuery()
                if (result.next()) {
                    return result.getInt(1) > 0
                }
            }
        } catch (e: Exception) {
            plugin.logger.severe("Error checking if item exists: ${e.message}")
        }

        return false
    }

    fun addItem(material: Material, initPrice: Double, volume: Double) {
        // Add item to database
        try {
            connection.prepareStatement(
                """
                    INSERT INTO project9_items (material, init_price, init_volume, volume)
                    VALUES (?, ?, ?, ?)
                    """.trimIndent()
            ).use { sql ->
                sql.setString(1, material.name)
                sql.setDouble(2, initPrice)
                sql.setDouble(3, volume)
                sql.setDouble(4, volume)
                sql.execute()
            }
        } catch (e: Exception) {
            plugin.logger.severe("Error adding item: ${e.message}")
        }
    }

    fun getItem(material: Material): ResultSet? {
        // Get item from database
        try {
            val sql = connection.prepareStatement(
                """
                    SELECT *
                    FROM project9_items
                    WHERE material = ?
                    """.trimIndent()
            )
            sql.setString(1, material.name)
            val result = sql.executeQuery()
            if (result.next()) {
                return result
            }
        } catch (e: Exception) {
            plugin.logger.severe("Error getting item: ${e.message}")
        }

        return null
    }

    fun updateItem(id: Int, initPrice: Double, initVolume: Double) {
        // Update item in database
        try {
            connection.prepareStatement(
                """
                    UPDATE project9_items
                    SET init_price = ?, init_volume = ?
                    WHERE id = ?
                    """.trimIndent()
            ).use { sql ->
                sql.setDouble(1, initPrice)
                sql.setDouble(2, initVolume)
                sql.setInt(3, id)
                sql.execute()
            }
        } catch (e: Exception) {
            plugin.logger.severe("Error updating item: ${e.message}")
        }
    }

    fun changeItemVolume(id: Int, amount: Double, action: String) {
        // Change item's volume
        try {
            connection.prepareStatement(
                """
                    UPDATE project9_items
                    SET volume = CASE
                        WHEN ? = 'set' THEN ?
                        WHEN ? = 'add' THEN volume + ?
                        WHEN ? = 'remove' THEN volume - ?
                    END
                    WHERE id = ?
                    """.trimIndent()
            ).use { sql ->
                sql.setString(1, action)
                sql.setDouble(2, amount)
                sql.setString(3, action)
                sql.setDouble(4, amount)
                sql.setString(5, action)
                sql.setDouble(6, amount)
                sql.setInt(7, id)
                sql.execute()
            }
        } catch (e: Exception) {
            plugin.logger.severe("Error changing item volume: ${e.message}")
        }
    }

    private val typeMaps = mapOf<String, (ResultSet) -> Any>(
        "int" to { rs -> rs.getInt("v") },
        "double" to { rs -> rs.getDouble("v") },
        "string" to { rs -> rs.getString("v") },
        "long" to { rs -> rs.getLong("v") }
    )

    fun getConfig(key: String): Any? {
        // Get config value
        try {
            connection.prepareStatement(
                """
                    SELECT v, t
                    FROM project9_config
                    WHERE k = ?
                    """.trimIndent()
            ).use { sql ->
                sql.setString(1, key)
                val result = sql.executeQuery()
                if (result.next()) {
                    return typeMaps[result.getString("t")]?.invoke(result)
                }
            }
        } catch (e: Exception) {
            plugin.logger.severe("Error getting config value: ${e.message}")
        }

        return null
    }

    private fun getType(value: Any): String {
        return when (value) {
            is Int -> "int"
            is Double -> "double"
            is String -> "string"
            is Long -> "long"
            else -> throw IllegalArgumentException("Unsupported type")
        }
    }

    fun setConfig(key: String, value: Any) {
        // Set config value
        try {
            connection.prepareStatement(
                """
                REPLACE INTO project9_config (k, v, t)
                VALUES (?, ?, ?)
                """.trimIndent()
            ).use { sql ->
                sql.setString(1, key)
                sql.setObject(2, value)
                val type = getType(value)
                sql.setString(3, type)
                sql.execute()
            }
        } catch (e: Exception) {
            plugin.logger.severe("Error setting config value: ${e.message}")
        }
    }
}