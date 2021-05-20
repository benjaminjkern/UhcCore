# Battle Royale Game Servers

### URGENT

- Players can occasionally spawn in trees
    - Run a postliminary check
- Team colors can occasionally occur twice

### Verify FIxed

- Verify primedtnt, arrows, and fireballs all work with UHCPlayerDeathEvent

### Medium Urgency

- /help and /member don't work in game server, looks bad
- Chat
    - Allow spectators to still choose between team chat and non-team chat
        - Add a little message saying they switched to global chat when they died
    - Team chat should switch to global when you're the only real player alive on your team
- Ending game
    - admin ending game chose admin as winner and gave admin a real win (Not actually a huge deal)
    - if last real team alive dies rather than choosing to /end, it can give them a win
- Spectators
    - Can prevent other players from placing blocks
    - Spectators can pick up arrows from the ground
- Game lags a lot at beginning (sometimes)
- Does not remove spectators who leave game from list
- When announcing scenarios add a clause for no scenarios
- List health is fucked up
- Allow spectators to /end game if one real player left
- Have players spawn directly in the lobby instead of being teleported there when they first join the server
- Sidebar
    - When game is over it displays weird incorrect info on the sidebar
    - Coords aren't in??
        - Unless explicitly changed it sets to default settings, which would explain the gameover ones too.
- Generation
    - Consider making it so ores only spawn in exposed caves
    - Put more flowers in map
    - Diamonds higher up maybe
    - New biomes
    - Villages spawn in biomes other than plains
- All pets should have team colors in their name tag

### Low urgency

- Upon reconnection to lobby the server should relay who is online and alive (?)
- Allow spectators to see inventories of players and chests (without opening the chest)
- When leaving server it attaches a nickname to you
- Announce team eliminations in non-solo games
- Spectators
  - make it so they can teleport to center
  - make it so they can spectate a player in first person
- Come up with way to punish players who leave while game is counting down
- Stop cheaters in some capacity
- Tips and tricks while waiting for game to start
- Clan Tags
- Should be able to sort the list by team or by name or both
- Pretty sure that unless lukcy leaves is on, shears dont give apples at all
- Actionbar api
- Change menu title for scenarios menu (in game)
- The lightining when people die breaks the block theyre standing in
- Rebrand everywhere that says UHC to Battle royale
  - I do a pretty good job of this but its not everywehere
- Should kill all mobs if the tps is low
- Change names of command items so that it doesnt say "Right click to " yadada
- I would like friendly fire to be on but it needs to be clear you are committing friendly fire
- Voting GUI
  - Make the scenario voter tell you how many votes you have left - not sure how
  - Maybe show empty voting slots
  - Rotating text in vote inventory - not sure if its possible
- Randomized Fancy lobbys
- Make enderchest consistent between games but disabled on:
  - Lil Cheat (I cant think of a way around this one)
  - Dont waste your time (Maybe just make it so you cant put god tools in the ender chest)
- Display somehow which scenarios are incompatible with which other ones
- /newmap command with force and also a vote
- Use one of your votes to downvote - patrons only
- Signs drop when lobby is unloaded
- A BUNCH of deprecation warnings on the server source
- Code restructuring
  - Also move all the isActivated shit to separate event listeners
  - Clean up the plugin and get rid of shit I dont want, trim it down to the fully customized plugin that I want
- Give players a map of where the wall is and where they are and where teammates are
    - Perhaps allow players to pin on map to show team
- Slowness warnings still happen when loading in lobby
- Pitch not finite warnings when starting game
- Nicknames and possibly rating in tablist, definitely with colors though, nicknames need to be done from the server level though
- Advertise that shears give double apples and enderchests are consistent between games
- Make it so you can see players on your team through walls
