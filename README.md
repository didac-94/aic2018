# AIC2018

**latest build** `betav4`

This is a repo for our bots in the [AIC2018](https://www.coliseum.ai/home) AI competition.

Even though this bot is an unfinished work it's functional and carried us to 9th place in the finals (that bot wasn't even the last iteration since we were late on the submissions due to some external factors). We're confident our last iteration (betav4) could have gotten top 4 easily. Here's an outline of the code and strategy and how could we planned to further improve it:

#### Movement
Basic (very bad) bugpath. Nothing to see here... **Improvement:** there are MANY optimizations that could have been done on the movement algorithm but this had less priority than other stuff so we took the basic one and ran with it - pun intended.

#### Combat
Combat is reduced to simple quitting and using special abilities when more or less suitable. We prioritize targets with less health. **Improvement:** Particularize combat to each class to have them perform better (e.g. archers hit and run and keep a safe distance, units back up when low on health, etc)

#### Communication
We had access to a long integer vector accessible by all units. We use this for communication and to keep track of certain information: alive ally tracking, enemy units seen, composition of our/their army, where an enemy was last seen, where to attack, where are the oaks, ID of special units like explorers, etc)
Communications are handled by the Data class, this class is instantiated for each unit and stores the common communication channels as well as local variables ("memory") for that unit.

The comm channels (aka the positions in the comm vector where each piece of information is stored) work as follows:  
- Since the order in which the units act is randomized each round we need a method to maintain and update the data we collect (if we use a channel to keep the count on a resource seen by a unit the first unit that round will only "see" it's own count, but we want all of it!). We solve that with dynamic channels
- For each different piece of data (aka a number) we want to store, we assign 3 positions in the vector to it. These 3 containers have different functions
  - Container 1 will store the data from the current round. For example, if a unit is counting trees on sight, it will add up the trees it currently sees to the number in this position.
  - Container 2 holds the value obtained from previous round. In our example, this position in the vector holds how many trees our army saw in the previous round.
  - Container 3 serves as reset. Each unit will reset this counter (to whatever the default value might be) whenever it makes use of the channel.
  - Each round we rotate the functions of our containers. Container 1 will be container 2 in the next round, 2 will be 3 and 3 will be 1.
- This way we always have access to the most accurate calculation (the one from the previous round) and keep updating the one for next round in a distributed way without caring for the order of the units.

#### Workers
Early in the game look for oaks, since they are more efficient to gather than petits. **Improvement:** if multiple workers are accessible have at least one worker constantly exploring for more oaks in the early rounds since one worker will be able to use all the available resources to plant by himself

As long as there are healthy oaks around, the workers won't plant more trees and will just mine the oaks and making more workers to boost the economy (up to certain restrictions)

If no oaks are around and planting is required, and since each worker can efficiently take care of at most 6 petits, we tell the workers to spread out of their center of mass until they are sufficiently alone and can plant their 6 trees. **Improvement:** they can and should position themselves in a  more efficient way so as to take exactly 6 units of area each, which can be done by assigning certain coordinates to each worker to be on and clearing those coordinates after it dies or leaves.

After they have found their spot they won't ever move again. **Improvement:** tell them to flee (or fight!) enemies for example.

Workers will rush and build a single barracks as soon as possible and then wait for certain economy requirements to keep building more.

#### Barracks
The barracks have 2 modes:

- Economy mode (we haven't spotted any enemy yet):
  - Maintain 2 [Knights](#knights) at all times. That's it.


- War mode (an enemy unit has been spotted):
  - Recruit troops subject to economy restrictions and in hardcoded proportions of class (70% W, 20% A, 10% K was our last try iirc). **Improvement:** Based on the observed composition of the enemy army, generate a suitable counter-composition (e.g. if enemy is all warriors make all archers, rock-paper-scissors yada, yada). This is the major improvement that was next on our list before we ran out of time.
  - Maintain the 2 [Knight](#knights) minimum.

#### Knights
The fastest unit and the most important to our strategy. As seen above we always have 2 knights at our disposal. These 2 knights take the role of explorers as soon as they spawn or as soon as one of the explorers dies. Their function is to go straight to the enemy base (the starting coordinates of the enemy spawn, those are public information) and report whenever they see an enemy unit. If explorers don't succeed in finding enemies that means there's probably no way the armies will find each other and thus we shouldn't spend any resource building an army but rather buying VP.

Additionally to being explorers, since we rush those first 2 knights, if the enemy takes slightly more time to start building combat units than the explorers take to reach their base they just wreck havoc with their workers, messing up their economy and providing some easy wins :)

More on [combat](#combat) below.

#### Warriors
The strongest unit overall and the gross of our army.  
More on [combat](#combat) below.

#### Archers
Ranged unit, beats warriors, which were the main unit used by most everyone so we always keep some around.  
More on [combat](#combat) below.

#### Ballistas
Those units were hard to control so we didn't spend any time with them. As it turns out, nobody in the contest ended up using them due to this.

# Team
We are all mathematics students at FME-UPC, the faculty of mathematics at Universitat Politécnica de Catalunya, Barcelona, Spain

Dídac Fernández - [VashMKS](https://github.com/VashMKS) on git  
Adrián Hernández - [meave96](https://github.com/meave96) on git  
Leonor Lamsdorff - [Lenok542](https://github.com/Lenok542) on git  
Andreu Huguet
