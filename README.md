# GatorRaider-StudentController

## Project Description and Motivation:
This repository contains code for a group project "GatorRaider" in the Programming Fundamentals 1 course at the University of Florida. The objective of this project was to provide students with practice working with inheritance and polymorphism in programming languages primarily, but I especially took this project as an opportunity to utilize strategic thinking and creativity in trying to accomplish the specified goal. I thoroughly enjoyed brainstorming different strategies and making them come to life. 

## Gameplay and Scoring:
This project is based around Ms. Pac Man game. In this game, Ms. Pac Man tries to score points by covering territory on the map and eating pills, while four ghosts try to eat Ms. Pac Man to limit her score. Ms. Pac Man has an autonomous behavior coded by the course faculty, while my group has the task of coding the autonomous behavior for the four ghosts. In order to assess the performance of my group's coded ghosts' behaviors, the game score with my coded ghosts is compared to a game score using ghosts with default code from the game (default behavior is nonrandom). A lower score indicates more effective performance.

## Design Goals:
Our group understood at once that the effectiveness of the ghosts depended upon a general closing down of the Ms. Pac Man but also the dynamic state of the game (ie if Ms. Pac Man eats power pill and ghosts are vulnerable). With this in mind the design goals include:
- Strategic algorithms for default ghost behaviors to corner down Ms. Pac Man
- Unique default behaviors for each ghost
- Effective teamwork and compatability of ghost behaviors
- Utilization of game state information to determine strategy for special cases (ie vulnerable mode)
- Dynamic strategic algorithms for ghost behaviors to respond to those special cases

## Features:
### Default behaviors of ghosts:
#### First Ghost
Ghost 0 “chase” has a default behavior of directly chasing the Pacman. 

This is a very straightforward strategy as this defender is most likely the one to provoke and press Ms. Pac Man.

#### Second Ghost
Ghost 1 "surround1" has a unique default behavior incorporating every element of the map (nodes). First, one of the possible neighboring nodes the ghost can move to is examined. Then the program looks at the distance between this neighboring node and another node (target node) on the map, comparing it to the distance between Ms. Pac Man and that target node. If Ms. Pac Man is closer to that target node then a counter is increased. This is then repeated with another target node on the map, again and again until every node on the map has become assessed as the target node. Then the program executes these same tests with a different neighboring node of the ghost, until all possible neighboring nodes are checked. The neighboring node with the least counter number indicates that there are fewer nodes where Ms. Pac Man can reach before this ghost can, and this is the node that the ghost will move toward. 

This strategy seeks to limit and corral Ms. Pac Man from a farther distance, into the hands of the other ghosts. If this ghost is too far away from Ms. Pac Man it will move directly closer before implementing its unique algorithm.

#### Third Ghost
Ghost 2 "surround2" has a similar default behavior to Ghost 1, but instead of only mapping out the distances between this ghost's neighbors and the target nodes to compare to Ms. Pac Man's distance to the target node, the algorithm also takes into account the Ghost 1 "surround1"'s position and runs the same tests with it. This extra addition of information allows this ghost take into account the fact that Ghost 1 is already limiting Ms. Pac Man in certain nodes that this ghost does not need to focus on, and can instead focus on limiting distances with other nodes. 

Similar to Ghost 1, this ghost seeks to corral Ms. Pac Man from a farther distance.If this ghost is too far away from Ms. Pac Man it will move directly closer before implementing its unique algorithm.

#### Fourth Ghost
Ghost 3 "ahead" has a unique default behavior of moving towards the node a few spaces ahead of the direction Ms. Pac Man is going. This uses a tracker to explore the map pathways ahead of Ms. Pac Man's movement and sets the target there. 

This strategy complements the strategy of Ghost 0, which chases from behind. 

### Dynamic responses to changing states:
#### Scatter
If Ms. Pac Man is closer to a Power Pill than the other ghosts, then all ghost default behaviors are overrided. One ghost goes to provoke Ms. Pac Man into eating the Power Pill while other ghosts scatter by getting a direction away from Ms. Pac Man.

#### Interception
If Ms. Pac Man is headed towards a Power Pill and a ghost can intercept her, then the ghost's default behavior is overrided and changed to an interception mode, targeting a junction betweeen Ms. Pac Man and the Power Pill.

#### Vulnerable
If a ghost is vulnerable, the ghost's default behavior is overrided and it moves away from Ms. Pac Man to avoid being eaten.

#### Endgame
If Ms. Pac Man is not close to a Power Pill, and there are enough ghosts in the right position to surround her, the neccessary ghosts have their default behaviors overrided and enclose on Ms. Pac Man. This condition is determined by whether enough ghosts can beat Ms. Pac Man to the surrounding junctions of her location. Once this state is activated, the ghosts first move towards the junction they are assigned to, then towards the original spot of Ms. Pac Man when this mode was activated, then directly to Ms. Pac Man, eliminating any chance of her escape.

## Performance:
Our group was very satisfied with the performance of the coded ghosts, which lowered the benchmark score between 25 and 50 percent. The new score was usually between 4500 and 5200, and the benchmark score was usually between 7000 and 8000 (variance due to Java Version). A score using random ghost behavior would be above 30,000.

## Repository Information:
The only java file in this src folder is the StudentController.java file, which contains the group developed behavior algorithms for the defenders/ghosts. The code is commented for understanding purposes.

## Installation and Testing: 
This repository includes the src folder that fits into the project repository: https://github.com/uf-cise-cs1/gatorraider 

In order to be run, the code from this src folder needs to replace the code in the src folder from the project repository. One can open the project from the project repository and paste this repository's code into its StudentController class to run.

## Contributors:
- Hubert Zhao
- Jeremy Cruz 
- Elijah Candelaria
- Scott Richardson

### Individual Contribution:
My contributions included coming up with and implementing the endgame state, as well as the surround1 and surround2 nodal algorithm behaviors.

## License:
This project is licensed under the terms of the MIT license.
