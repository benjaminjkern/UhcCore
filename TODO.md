# Server in general

- Implement voting
- Implement Ranks / Patreon

> New features

- Advertise & Get a consistent player base
- Voting:
  - Add supply crates
  - Add double vote power
- Multiple different games
- Multiple lobby servers - once there are more players
- Upgrade Server infrastructure - this was done but can always be added to
- Easy suggestion box
- A way to keep track of which scenarios are popular
- Server website
- Auction: once a week we give stuff away outside of the game and you can put in tickets
- Server-wide nicknames
- Easy Server-wide group setting
- Information about being a member

# Lobby

- Breaking blocks doesnt say anything
  - In spawn it shouldnt say anything but outside of spawn it should say "Yadaydada owns that!"
- Fire spread should be active in lobby
- Fix all the gay stuff with griefprevention
- Dying in lobby says you have no bed, it shouldnt
- Make patron menu actually take you to patron zone
- Make spawn town
- Make top charts work

> New features

- Trying to break restricted land should remind you to donate
- Make rating only stay if you are a member "Want your ranking to stay when you log off?"
- Figure out how to do the infobook automatically show to new players
  - Update infobook to be correct
- Add info around spawn on:
  - What YEUH is
  - What being a member does
  - What being a patron does
  - People with top ratings, kills, wins, and something else I think maybe most time spent on server idk
- Make boats float in lava
- Let members claim 100 blocks to start with
- Make stats menu
- Make stats clear and save and clear cache every 30 minutes or so
  - Save the people who are currently online
- Make list from console show all players
- Make stats from console show stats in text form
- Hide commands that players dont need to see
- Add bit to the help menu for:
  - Actually explain the server
  - Explain being a member
  - Display what commands you can use
- Make list menu show whether in-game players are alive or dead
- Make games menu and show who's in them and a console equivalent

# UHC Game Servers

> Bugs

- Material/UniversalMaterial have bugs that throw exceptions on legacy materials
  - Causes lag on: Randomized Crafting (?) And flower power
- A BUNCH of deprecation warnings (452?)
- Pretty sure that unless lukcy leaves is on, shears dont give apples at all
- Signs drop when lobby is unloaded
  - Not a big deal tbh
- Slowness warnings still happen when loading in lobby

> New features

- Make list menu that allows spectators to teleport

  - Make it so you spectators can teleport to bots - not sure how
  - I'm thinkin overhaul spectators in general with a menu that can teleport to people including bots
    - List all players with who's alive and who's not
  - Spectators shouldnt be able to fly through stuff - switch to invisible flying adventure mode, possibly, although I'm not sure how to make it non-collidable
  - Spectators cant tp currently

- Make stats menu
- Command to see which scenarios are currently active more advertised
- Nicknames and rating in tablist (?)
- /newmap command with force
- Use one of your votes to downvote - patrons only
- Advertise that shears give double apples and enderchests are consistent between games
  - fix shears thing
- Make enderchest consistent between games but disabled on:
  - Lil Cheat (I cant think of a way around this one)
  - Dont waste your time (Maybe just make it so you cant put god tools in the ender chest)
- Add a bigger warning on getting close to UHC wall
- Make the wall move
  - Give players a map of where the wall is and where they are and where teammates are
  - Perhaps allow players to pin on map to show team
- Voting GUI
  - Make the scenario voter tell you how many votes you have left - not sure how
  - Rotating text in vote inventory - not sure if its possible
- When joining, Title or subtitle should splash on screen, also change it to say "Pick" instead of vote, so it doesnt interfere with server voting
- If you hit a teammate it should warn you the first time, cancel it, and warn the rest of the team if you do it again but it does damage the second time (maybe put a cooldown in) EITHER DO THIS OR DISABLE FRIENDLYFIRE

### Bots

- Every once in a while bots make the server lag, not entirely sure what it is
  - killing all entities helps sometimes for a bit, I think bots are causing a shit ton of mobs to spawn
  - The only thing is this doesn't always happen
- Bots make server lag at beginning of game - doesnt happen as much anymore
- Bot's teams don't show up on their name tag initially, but they do show up if they switch teams
- Bots don't drop anything when killed
- Bots suffocate in walls a lot and its dumb
  - They don't run away from the wall
  - They also still have issues with water and lava
  - They do it a lot less now
- Bots should be able to heal themselves if players can - if players have golden apples or potions or whatever
- Make sure bots are doing appropriate damage when using enchanted tools and also wearing enchanted armor cuz it definitely felt like I was doing like way more damage than I shouldve been
- Bots shouldn't go for neutral mobs
- Bots should run from mobs if their health is low
- Mobs don't target bots on their own
- Bots don't take fall damage
- Eggs do damage to teammates and dont cause the bot to stop guarding
- Having two bots on a team with a player screws it up a bit
- Sometimes just sorta stop moving and get stuck behind the wall
  - Could be having issues pathfinding
- Pretty sure bots just jump into lava
- Sometimes they still keep the slowness and blindess potion effects on game start

> Future features

- Make bots have an AI that learns (NO IDEA HOW TO DO THIS)
- The tablist is disabled for bots rn but it should in the future:
  - Show all bots, not just the ones that are loaded
  - Show when bots are respawned in spectator mode
  - Show the bot's teams and health

# Scenarios

- Random: Pick a random scenario for the rest of the vote counts (If a scenario is picked before random, that one stays in)
- None: Everything here and below gets negated (If a scenario is picked before random, that one stays in)

> These two could be put in the inventory so that theyre viewed as constants

- Nether Start:
  - Bots SUCK at this one
- Politics
  - On switch dog should become docile (maybe in general)
  - On switch the player should be immortal but also cant do damage for 5 seconds
  - Make it say if you die of natural causes, you don't join another team
- Super heros
  - being kicked or leaving and coming back screws up the sneaking
  - Not compatible with teams at all **NEED TO TEST**
  - Make it more obvious what your power is (Maybe dont need this but two people have had the same issue)
  - If the player attacks a zombie, a tamed dog will still attack the zombie, not sure if this is fixable
  - make lucky power compatible with ez money (always cook instead of dropping ore) and triple ore (triple ore) and king midas (always drop gold)
  - Give the ability to disable superpowers for the case of like creeper or ice or whatever
  - Still some errors that happen
- Shared inventory: throws exceptions that lag the server
- Ez money, triple ores, randomized drops, etc. all still apply when in creative mode. Across the board they shouldnt work.
- Randomzied drops:
  - Doesnt check tool - not entirely sure how I feel about this
  - Should work for mob drops too
  - Shouldnt drop barriers or bedrock
  - Sometimes stone bricks break instantly - pretty sure its only infested stone bricks but still not sure why
- Randomized crafts:
  - Make sure frunaces and stuff still work (Not important cuz its not in rn)
- Dont waste your time:
  - Crafting multiple items doesnt enchant all of them
- Sky high
  - Should do actual damage that makes sounds, idk why its not

### Compatibility

- Randomized Drops should be incompatible with:
  - specifically the luck power in super powers
    - actually maybe not cuz the luck power does other things
- Vein Miner should be fully compatible with:
  - Lag World
  - king midas
  - triple ores
  - king midas
- Lag world should be fully compatible with:
  - Timber
- Make sure Vein works with lagworld and midas and cutclean and lagworld works with timber

### Next Week:

Get rid of:

- Duos
- Sky high
- Fly high

Add:

- Chicken fight: Duos but one player rides the other, all items collected by the bottom dude go to the top dude, bottom controls movement and top controls item usage. Health is shared and all teams get double health
- Fish gang: You are given infinite water breathing and enchanted boots that let you swim and whatnot underwater as well as night vision, however you cannot breathe on land for longer than 10 seconds. You may bring buckets of water with you and stand in them to get oxygen
- Weakest Link: Every minute, the person with the lowest health dies

### Other ideas:

- Voldemort: eyes in the back of your head
- You can ram someone by riding into them with a horse or pig or llama or strider
- Your team randomizes every couple of minutes - only works for non solo games
- Infected: When you die a zombie spawn holding your stuff (Perhaps multiple zombies depending on how hard we want it to be)
- Everyone gets (the same) random potion effect every minute, have it only last like 5 seconds and it won’t be super strong
- Arrows are replaced with a random mob, including skeleton arrows
- Every minute you must be standing on a specific block
- Explosive Gang: Everyone gets flint and steel with unbreaking, a stack of creeper eggs, a stack of fire charges, a stack of tnt
- Gulag: When you die, you go to a gulag and battle 1v1
- Farmer: Amass the biggest army of animals and have them fight for you
- Beds explode when you try to sleep in them in the overworld like youre in the end/nether
- Assassin: Every player has 1 person they are trying to kill, and they are given a compass that points towards that person and that person is glowing for them.
  - This is the only person they are allowed to kill. If that person dies on their own, your compass points towards the next person in the cycle.
  - If you try to kill anyone else, the kill is cancelled and you die instead
- Prop hunt
- Drunk: Controls are wonky (?) and you alternate between having speed, nausea, hunger, and resistence.
  - Drink any potion or water bottle to get drunker, making all potion effects stronger. If you get to 3 you "throw up" half of your inventory, an dif you get to 4 you get poisoned, and if you get to 5, you die instantly.
  - Drinking milk will make you throw up
  - Sleeping in a bed will make the drunkness go away but you will be hungover where you are constantly slow and have mining slowness.
  - You can get your drunkness back by drinking more
- Buy teammates back with gold or Buy dead players not on your team back
- Being on fire or in lava heals you, while water poisons you
- Juggernaut: 1 person is given netherite armor, a netherite sword, a netherite pickaxe, resistance, and double health. It is 1 v everyone else.
- Wither Rush: A wither is spawned in the middle of the map Starting at 10 minutesKill it to win.
- Contracts: Kill a specific player within a time frame and win a prize
- You can only die when the timer ends in a specific number, everyone gets a different one and theyre a secret (Or maybe theyre not)
- Everything in your inventory has knockback 100
- Sneaking makes you vanish completely but also stop moving
- With 2 diamonds, a gold block, 2 feathers and 4 leather you can make hermes winged sandles that let you fly for a bit, you fall after 15 seconds and you must wait 15 seconds to use them again and fall damage is NOT negated
- Leather can be turned into a cum rag which you can jerk off into to give yourself health. If you jerk off too much in succession, you hurt yourself and your cum rag gets gross
- Drugs - ?
- Second Start: All people who die in the first 10 minutes respawn with empty inventories in the center of the map
- Realism:
  - Temperature must be regulated
    - Can wear armor to keep you warm but if you wear a full set of diamond in the middle of the desert at day time then youre gonna die of heat exhaustion
    - Depends on biome and time of day
  - Hunger goes faster
  - You must drink water: Keeps temp regulated
  - Wearing heavy armor slows you down
- No stacking: Nothing in your inventory stacks
- Hurricane: It is raining and thundering (More than usual) and wind blows you around a lot
- End Start: You start in the end.
- Every player is disguised as a block (A rare block) that snaps to the grid if you stay still for 5 seconds
- Idk if its possible but Play the game as chickens (low to the ground and Other players see you as a chicken also you float down to the ground instead of falling)
- King of the hill: Everyone gets a map, Whoever has the most kills or the most hearts if its a tie is glowing on everyone's map
- King of the hill: A crown
- You and someone else (Who you are not on a team with) have health bars swapped. But you cant just try to kill yourself because you die when they die.
- Tourettes : You will occasionally swing or place blocks or say things in the chat or drop the item in your hand and you cannot control it
- Anvils drop on everyone's head every 20 seconds
- You are given potions so you cant move or jump, negated fall damage, and an infinite stack of enderpearls. teleport and win game
- Hunger Games: A bunch of chests with loot spawn in the middle, and everyone spawns around the middle, and spectating players can give items to alive players
- UAV: All players get a map of where all the other players are
- JETPACK
- Instakill: Players instant break blocks and instant kill mobs and other players with just their fists.
- Deathmatch: When there are 2 players left OR when the time reaches 30 minutes, all players are teleported to an arena for the final death match.
