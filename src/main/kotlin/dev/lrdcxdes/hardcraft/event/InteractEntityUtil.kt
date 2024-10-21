package dev.lrdcxdes.hardcraft.event

import java.util.*

class InteractEntityUtil {
    companion object {
        private val lastClickTime = mutableMapOf<UUID, Long>()

        fun setLastClickTime(player: UUID) {
            lastClickTime[player] = System.currentTimeMillis()
        }

        fun getLastClickTime(player: UUID): Long {
            return lastClickTime[player] ?: 0
        }

        fun canInteract(player: UUID, seconds: Int): Boolean {
            if (!lastClickTime.containsKey(player)) {
                return true
            }
            return System.currentTimeMillis() - lastClickTime[player]!! > seconds * 1000
        }
    }
}