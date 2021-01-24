# Battle Royale Game Servers

> URGENT

- Fix all the async things because I'm pretty sure thats why its crashing so much
- The list inventory is slow
- Spectators
  - They shouldnt be able to pick up exp
  - Can block arrows, they shouldnt be able to
  - can open doors, shouldnt be able to

> New features / Less Urgent

- Stop cheaters in some capacity
- Maps straight up shouldnt be able to load if the center is ocean
  - Nothing I do appears to fix this
- Fix all scenarios with curse of vanishing
- Dynamically set scoreboard on scenarios
- Make shears always able to get apples
- The lightining when people die breaks the theyre standing in
- Worldedit items sometimes reload as axe and compass which is annoying
- Announce team eliminations in non-solo games
- Randomized Fancy lobbys
- Border small warning should be bigger
- Make bossbar less annoying
- Switch events to a generalized uhcdeadplayerevent that runs before and can cancel a regular deadplayer event
  - Verify primedtnt, arrows, and fireballs all work with this event
  - Also move all the isActivated shit to separate event listeners
- Change menu title for scenarios menu
- Should show rating at all times
- Spectators
  - make it so they can teleport to center
  - make it so they can spectate a pllayer in first person
  - When they die, they shouldnt be teleported to the middle, they should just become a ghost where they died
- Should be able to sort the list by name team or by name or both
- Have players spawn in the lobby instead of being teleported there
- Command to see which scenarios are currently active more advertised
- Make the subtitle bit display for as long as the title
- Change names of command items so that it doesnt say "Right click to " yadada
- Fix help, rules, stats, and rating command on this server make them align with lobby server
- Rebrand everywhere that says UHC to Battle royale
- Display somehow which scenarios are incompatible with which other ones
- Make the wall move
  - Give players a map of where the wall is and where they are and where teammates are
  - Perhaps allow players to pin on map to show team
- The game start countdown is erratic as shit
- Should kill all mobs if the tps is low
- Make it so you can see players on your team through walls
- Upon reconnection to lobby the server should relay who is online and alive
- A BUNCH of deprecation warnings on the server source
- Pretty sure that unless lukcy leaves is on, shears dont give apples at all
- Signs drop when lobby is unloaded
  - Not a big deal tbh
- Slowness warnings still happen when loading in lobby
- Nicknames and rating in tablist (?)
- /newmap command with force and also a vote
- Use one of your votes to downvote - patrons only
- Advertise that shears give double apples and enderchests are consistent between games
  - fix shears thing
- Make enderchest consistent between games but disabled on:
  - Lil Cheat (I cant think of a way around this one)
  - Dont waste your time (Maybe just make it so you cant put god tools in the ender chest)
- Voting GUI
  - Make the scenario voter tell you how many votes you have left - not sure how
  - Maybe show empty voting slots
  - Rotating text in vote inventory - not sure if its possible
- If you hit a teammate it should warn you the first time, cancel it, and warn the rest of the team if you do it again but it does damage the second time (maybe put a cooldown in) EITHER DO THIS OR DISABLE FRIENDLYFIRE
- Clean up the plugin and get rid of shit I dont want, trim it down to the fully customized plugin that I want
