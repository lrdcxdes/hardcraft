package dev.lrdcxdes.hardcraft.sql

import dev.lrdcxdes.hardcraft.Hardcraft
import java.sql.Connection
import java.sql.DriverManager
import java.io.File

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
                        id INT PRIMARY KEY,
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

    fun has(uuid: String, amount: Double): Boolean {
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
}