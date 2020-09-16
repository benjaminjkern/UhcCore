![Java CI with Gradle](https://github.com/Mezy/UhcCore/workflows/Java%20CI%20with%20Gradle/badge.svg)

# UhcCore

Automates UHC games on a dedicated 1.8 - 1.16 server.

More info can be found on the [wiki](https://github.com/Mezy/UhcCore/wiki)

I (Ben Kern) forked this from Mezy because I wanted to run my own private server with custom scenarios.

# Changes

- Fly high now spawns you high and also negates fall damage.
- 9 slots & Nether start actually show when you are on the vote screen
- Lucky Leaves now sets apples to spawn at 5%, golden apples at 0.5%, and enchanted golden apples at 0.05%, not mutually exclusive. Also it sets the ability for all trees to drop apples. This is an option in the config
- Voting works different, will only pick randomly if nobody votes at all, otherwise it will pick from what people did vote for
- Disabled: Bowless, Rodless, Horseless, Fireless, Goldless, Shieldless, Double Ores, Gone Fishing, Infinite Enchants, Permakill, Fast Smelting, Fast Leaves Decay, Hastey Boys, Nofall, and Upsidedown Crafting (I realize I could have just put these on the blacklist in the config file but whatever)
- If the game starts with only one player, it will not end unless that player dies. This was easier for me to debug things than having to remember to go into the config file and change the single player end game option back.
- When the game ends, rather than stopping/restarting the server, all players and kicked out and the plugin is simply reloaded. This saves A LOT of downtime, since the server no longer has to save the regular worlds (Which arent even accessible while this plugin is on), and the old uhc world (which gets deleted when the plugin turns back on anyways).
- Hunger, exhaustion, and saturation are now set to how they would be in a default world.
- Force starting a game starts it in 5 seconds, rather than listening to however long you have the countdown set to. Makes it easier to debug stuff when playing solo.
- Achievement Hunter was printing a lot to console

### New Scenarios

- Lag world: Blocks drop what they are supposed to but are immediately replaced, giving the effect that the world is lagging.
- Lil Cheat: All players get a set amount of time of creative mode at the beginning of the game. Default is 30 seconds.
- Swap Meet: All players swap positions every 5 minutes.
- Companion: All players spawn in with a tamed wolf that has resistance 2 and a collar that matches the color of the team the player is on. The wolf is named from a list in the plugin resources file called "doggyNames.txt"
- King Midas: All ores and mobs drop gold. Animals drop gold nuggets.
- Politics: If you are killed by a player on another team, you respawn on their team.
- Fast World: Replaces Fast Furnace and Fast Leaves, additionally makes plants faster.
- Dont Waste Time: Replaces Hastey Boys, every time you craft something it attempts to enchant that item with every possible enchantment in the game. You can set an enchantment blacklist to avoid items being given curses, or tools being stuck with silk touch (Preventing you from ever getting cobblestone)
- No Craft: All crafting recipes are disabled.
- Shared Health: Actually was in the original code, but it was disabled by Mezzy. I imagine it was because the player taking damage event would call itself recursively and cause the server to crash. This was fixed by changing it to sethealth instead of take damage
- What's Mine is Yours: All players share the same inventory.
