# Hardcraft - Core Plugin for Better Than Night (BTNMC)

![BTN Banner](https://wiki.btnmc.net/~gitbook/image?url=https%3A%2F%2F1303704132-files.gitbook.io%2F%7E%2Ffiles%2Fv0%2Fb%2Fgitbook-x-prod.appspot.com%2Fo%2Fspaces%252FsI7eYaKLuEvyEDQFIjcw%252Fuploads%252Fgit-blob-a92cd5b4548e92eeb6f24ec7583201ebab3b75f7%252F%25D0%2591%25D0%25B5%25D0%25B7%2520%25D0%25BD%25D0%25B0%25D0%25B7%25D0%25B2%25D0%25B8.png%3Falt%3Dmedia&width=1024&dpr=2&quality=100&sign=5301514d&sv=2)

**Hardcraft** is the core plugin that powers the **Better Than Night (BTN)** Minecraft server. It's designed to create a more challenging and immersive vanilla experience by introducing a wealth of new mechanics, a deep progression system, and rich environmental interactions.

---

### Join the Adventure!

*   **Minecraft Version:** 1.21+
*   **Server IP:** `play.btnmc.net`
*   **Discord:** [https://discord.gg/cjPvW9xfJ7](https://discord.gg/cjPvW9xfJ7)
*   **Wiki:** [https://wiki.btnmc.net](https://wiki.btnmc.net)

---

## Key Features

Hardcraft transforms the vanilla Minecraft experience with a wide array of features designed to challenge players and encourage strategic thinking.

### 🧩 Races & Classes
Choose a unique identity with deep gameplay implications.
*   **13 Unique Races:** Select from a diverse roster including Humans, Elves, Dwarves, Giants, Vampires, Goblins, and the elemental Cible. Each race has distinct attributes, abilities, and restrictions that fundamentally change your playstyle.
*   **14 Specialized Classes:** Define your role with classes like the spell-casting Wizard, the healing Cleric, the resourceful Caveman, or the musical Bard. Each class offers unique perks and abilities.

### 🌡️ Immersive Survival Mechanics
The world is alive and poses new environmental challenges.
*   **Temperature & Seasons:** Your survival depends on managing your body temperature. Biomes, seasons, time of day, light sources, and armor all affect you. Food spoils based on ambient temperature, and crops grow differently throughout the year.
*   **Phobias:** Face your fears with new psychological debuffs. **Nyctophobia** (fear of the dark) brings paranoia and negative effects in low light, while **Bathophobia** (fear of depths) applies mining fatigue when you venture too far underground.
*   **Innate Weakness:** You start weaker. Attacking with a bare hand is ineffective, and breaking hard blocks without the proper tools will cause you physical strain and damage.

### 🛠️ Overhauled Crafting & Progression
Crafting is more complex and rewarding.
*   **Custom Workbenches:** Vanilla workbenches have been re-purposed. The **Stonecutter** is now essential for processing wood, the **Cauldron** is used for simple alchemy, the **Loom** for leather goods, and the **Fletching Table** for ranged weapons.
*   **New Crafting Recipes:** Hundreds of vanilla recipes have been replaced with more logical and challenging alternatives. Discover new recipes for tools, food, and utility blocks.
*   **Sieving:** Use a Brush in water to sieve materials like sand and gravel for a chance to find valuable resources.

### 💰 Dynamic Economy & Player Trading
Engage in a living, breathing economy.
*   **Dynamic Marketplace:** Trade with special "Nitwit" Villagers at a shop where prices fluctuate based on supply, demand, server-wide liquidity, and even the time of day.
*   **Player-to-Player Economy:** Use `/pay` to securely transfer money to other players and `/balance` to check your funds.

### 🐾 Enhanced Mobs & AI
The creatures of the world are smarter and more dynamic.
*   **New Behaviors:** Mobs now interact with the world and each other. Peaceful animals will raid untended crops, and predators will hunt other animals.
*   **Unique Interactions:** Mobs are more complex. You can attempt to disarm a Creeper with shears, steal a bow from a Skeleton, or tame colorful Slimes.
*   **New Threats:** Beware of Squids at night, which may attack players in the water, and prepare for the terrifying Kraken boss that can spawn during thunderstorms.

### 🌍 World & Player Enhancements
The world itself is more dynamic and interactive.
*   **Guardian Blocks:** Protect your base from griefing by placing special Guardian Blocks that create a resource-fueled protected region.
*   **Block Physics:** Most blocks you place are now affected by gravity, requiring more careful construction and engineering.
*   **Natural Disasters:** Be on the lookout for destructive tornadoes that can form during storms, tearing through the landscape and tossing entities into the air.

## Building from Source (For Developers)

This project is built with Gradle and requires Java 21.

### Dependencies
*   [Paper API](https://papermc.io/) (1.21+)
*   [SkinsRestorer API](https://www.spigotmc.org/resources/skinsrestorer.2124/)

### Build Steps
1.  Clone the repository:
    ```bash
    git clone https://github.com/lrdcxdes/hardcraft.git
    cd hardcraft
    ```
2.  Build the project using Gradle:
    ```bash
    ./gradlew shadowJar
    ```
3.  The final JAR file will be located in the `build/libs/` directory.

## License
This project is open-source and available under the MIT License. See the `LICENSE` file for more details.
