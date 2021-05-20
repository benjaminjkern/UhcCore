# Bots

### URGENT

- Either bots don't disappear fully after they die or timebomb causes some entity to remain that attracts bots
- If bots are underground they can't come back out

### Verify fixed

- Bots sometimes just stay in the same spot
    - I think it's because they cannot reach their target
    - May also have to do with trying to target invisible entities and then being told not to without telling them a proper new entity to go to 
- Bots keep dying behind the wall
- Bots can route down into caves

### Medium Urgency

- Bots dont pick up mob drops when I know I put in code that is supposed to make them do that
- Make bots naturally glow for all cases (i.e. if they pick up a glowing potion effect)
- Can effectively stop bots from getting to you by just going into water and sniping them
    - Options to fix:
        - Give bots boats (If even for free)
            - I don't know that this will even work cuz I don't know if they know how to pilot boats or get out of boats
        - Give bots bows and arrows
        - Try to make them less retarded in the water
- Can also effectively stop bots from getting to you by putting walls between you or by towering up 4 blocks
- Bots giving themselves stuff
    - Make work with king midas, dont wanna have the bots have a weird advantage
    - Make work with cutclean (? Maybe not, seems like not a huge deal)
        
### Low Urgency

- Killbots command doesnt work with sudden death (Or king, possibly also vampire)
- Bots try to fire rockets through arrows
    - Check if rockets do damage and try to intervene?
    - Enderpearls are also temporarily blocked because they make bots shoot players up in the air - this should just be fixed entirely
        - Something else does this as well but I'm not sure what
    - Overwrite bot plugin
- Bots should be able to pick up items and experience all the time, not just forced to absorb when they get a kill
- Mobs don't target bots on their own
- Bots should run from mobs if their health is low
- If bots have flint and steel or a bucket of lava they should try to use it
- Bots don't take fall damage
- Fix bots with curse of vanishing and binding
  - Make it work I guess?
- The tablist is disabled for bots rn but it should in the future: - This might actually be doable
  - Show all bots, not just the ones that are loaded
  - Show when bots are respawned in spectator mode
  - Show the bot's teams and health
