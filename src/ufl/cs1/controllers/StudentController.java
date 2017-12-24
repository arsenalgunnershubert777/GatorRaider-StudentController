package ufl.cs1.controllers;

import game.controllers.DefenderController;
import game.models.Defender;
import game.models.Game;
import game.models.Maze;
import game.models.Node;


import java.util.*;
import java.lang.Math;

public final class StudentController implements DefenderController
{
	private final int chase = 0; //identifiers, not necessary but may be helpful
	private final int surround1 = 1;
	private final int surround2 = 2;
	private final int ahead = 3;

	private List<Node> Map = new ArrayList<>(); //creates node map
	private List<Node> junctionMap = new ArrayList<>();
	private Maze maze = null;

	private int numberPaclives = 0; //tracks number of pacManlives;
	private int currentNumPaclives = 0;

	private int[] status = new int[4]; //status of game for each enemy

	private int[] enemyDirection = {-50,-50,-50,-50};

	private List<Node> nodesCovered = new ArrayList<>(); //fields for use in endgame
	private Node centralLocation;
	private List<Node> allSurroundingJunctions = new ArrayList<>();
	private List<Node> allSurroundingJunctions0 = new ArrayList<>();
	private List<Node> allSurroundingJunctions1 = new ArrayList<>();
	private List<Node> allSurroundingJunctions2 = new ArrayList<>();
	private List<Node> allSurroundingJunctions3 = new ArrayList<>();
	private int[] endgame = new int[4];
	private boolean firsttime = true;
	private Node[] matchNode = new Node[4];
	private boolean noMoreNodes = false;

	private int[] block = new int[4];
	private int[] enemiesBlocking = new int[4];
	private Node[] targetJunctionBlocking = {null, null, null, null};

	public void init(Game game) {
	}

	public void shutdown(Game game) { }

	public int[] update(Game game,long timeDue) {

      /*
      The gist of this program is running a dynamic system of enemies which have their own default methods under specificed
      conditions but that can also work together to surround the attacker if possible
       */


		int[] actions = new int[Game.NUM_DEFENDER]; //returns enemy actions
		List<Defender> enemies = game.getDefenders();

		currentNumPaclives = game.getLivesRemaining(); //checks if pacman has just lost a life (needed later on)
		if (numberPaclives == 0)
			numberPaclives = game.getLivesRemaining();

		Maze newmaze = game.getCurMaze(); //gathers all nodes into "Map"
		boolean first = false;
		if (maze == null || !maze.equals(newmaze)) {
			maze = newmaze;
			first = true;
			firsttime = true;
			Arrays.fill(status, 0);
		}
		if (first) {
			Map.clear();
			Map.addAll(game.getPillList());
			noMoreNodes = false;
			while (!noMoreNodes) {
				noMoreNodes = true;
				for (int i = 0; i < Map.size(); i++) {
					if (Map.get(i) != null && Map.get(i).getNumNeighbors() > 0) {
						for (int j = 0;  j < Map.get(i).getNumNeighbors(); j++) {
							if (!Map.contains(Map.get(i).getNeighbors().get(j)) && Map.get(i).getNeighbors().get(j) != null) {
								Map.add(Map.get(i).getNeighbors().get(j));
								noMoreNodes = false;
							}
						}
					}
				}
			}
			for (Node n: Map) {
				if (n.getNumNeighbors() >= 3) {
					junctionMap.clear();
					junctionMap.add(n);
				}
			}

		}



		for (int i = 0; i < 4; i++) {
			if (enemies.get(i).isVulnerable()) {
				actions[i] = enemies.get(i).getNextDir(game.getAttacker().getLocation(), false);
				status[i] = 5;
			}
			else if (status[i] == 5 && !enemies.get(i).isVulnerable())
				status[i] = 0;
		}

		Node nearestPowerPilltoAttacker = null; //checks nearest pill to attacker
		int distance = -50;
		int distancemin = -50;
		if (game.getPowerPillList().size() > 0) {
			for (Node n : game.getPowerPillList()) {
				distance = game.getAttacker().getLocation().getPathDistance(n);
				if ((distance < distancemin) || distancemin < 0) {
					nearestPowerPilltoAttacker = n;
					distancemin = distance;
				}
			}
		}

		int distance2;
		int distancemin2 = 1000000;
		int enemyMin2 = 5;
		int enemyMin3 = 5;
		int distance3;
		int distancemin3 = 1000000;
		if (nearestPowerPilltoAttacker != null) {
			for (int i = 0; i < 4; i++) {
				if (!enemies.get(i).isVulnerable()) {
					distance2 = enemies.get(i).getPathTo(nearestPowerPilltoAttacker).size(); //checks nearest enemy to attacker
					if (distance2 < distancemin2) {
						distancemin2 = distance2;
						enemyMin2 = i;
					}
				}
				if (!enemies.get(i).isVulnerable() && status[i] != 2) {
					distance3 = enemies.get(i).getPathTo(nearestPowerPilltoAttacker).size();
					if (distance3 < distancemin3) {
						distancemin3 = distance3;
						enemyMin3 = i;
					}
				}

			}
		}


		boolean attackerNearPill = false;
		int enemyDist;
		int enemyDistMin = 10000;
		int enemyMin = 5;


		if (enemyMin2 != 5 && nearestPowerPilltoAttacker != null && game.getAttacker().getLocation().getPathDistance(nearestPowerPilltoAttacker) < 30 && game.getAttacker().getPathTo(nearestPowerPilltoAttacker).size() < enemies.get(enemyMin2).getPathTo(nearestPowerPilltoAttacker).size()) {
			for (int i = 0; i < 4; i++) { //checks if attacker is close to powerpill
				enemyDist = enemies.get(i).getPathTo(game.getAttacker().getLocation()).size();
				if (enemyDist <= enemyDistMin) {
					enemyDistMin = enemyDist;
					enemyMin = i;
				}
			}

			actions[enemyMin] = enemies.get(enemyMin).getNextDir(game.getAttacker().getLocation(), true);

			for (int i = 0; i < 4; i++) {
				if (i != enemyMin) {
					actions[i] = enemies.get(i).getNextDir(game.getAttacker().getLocation(), false);
				}
			}

			Arrays.fill(status, 1);
			attackerNearPill = true;
		}


		List<Node> attackerToPill = new ArrayList<>();
		List<Node> attackerToPillJunctions = new ArrayList<>();
		Node targetJunction = null;
		if ((enemyMin2 != 5 && status[enemyMin2] != 5 && game.getAttacker().getPathTo(nearestPowerPilltoAttacker).size() > enemies.get(enemyMin2).getPathTo(nearestPowerPilltoAttacker).size() && status[enemyMin2] != 2 && status[enemyMin2] != 4 && enemies.get(enemyMin2).getPathTo(nearestPowerPilltoAttacker).size() != 0) || (enemyMin3 != 5 && status[enemyMin3] != 5 && game.getAttacker().getPathTo(nearestPowerPilltoAttacker).size() > enemies.get(enemyMin3).getPathTo(nearestPowerPilltoAttacker).size() && status[enemyMin3] != 2 && status[enemyMin3] != 4 && enemies.get(enemyMin3).getPathTo(nearestPowerPilltoAttacker).size() != 0)) {
			attackerToPill = game.getAttacker().getPathTo(nearestPowerPilltoAttacker); //determines if enemy can intercept attacker to pill
			for (Node g: attackerToPill) {
				if (g.getNumNeighbors() >= 3) {
					attackerToPillJunctions.add(g);
				}
			}

			distance3 = 0;
			int distanceMin3 = 10000;
			for (Node f: attackerToPillJunctions) {
				if (enemies.get(enemyMin2).getPathTo(f).size() <= game.getAttacker().getPathTo(f).size()) {
					distance3 = f.getPathDistance(game.getAttacker().getLocation());
					if (distance3 <= distanceMin3) {
						distanceMin3 = distance3;
						targetJunction = f;
					}
				}
			}

			if (targetJunction != null) {
				enemiesBlocking[enemyMin2] = 1;
				targetJunctionBlocking[enemyMin2] = targetJunction;
				status[enemyMin2] = 2;

			}


		}

		for (int i = 0; i < 4; i++) { //continues enemy interception if applicable

			if (status[i] == 2) {
				if (enemiesBlocking[i] == 1) {
					if (!enemies.get(i).getLocation().equals(targetJunctionBlocking[i]) && block[i] == 0) {
						actions[i] = enemies.get(i).getNextDir(targetJunctionBlocking[i], true);
						block[i] = 1;
					} else if (block[i] < 40 || (game.getAttacker().getPathTo(nearestPowerPilltoAttacker).size() <  enemies.get(i).getPathTo(nearestPowerPilltoAttacker).size())) {
						actions[i] = enemies.get(i).getNextDir(game.getAttacker().getLocation(), true);
						block[i]++;
					} else {
						block[i] = 0;
						status[i] = 0;
						enemiesBlocking[i] = 0;
						targetJunctionBlocking[i] = null;
					}
				}

			}
			else if (status[i] != 2 && enemiesBlocking[i] == 1){
				block[i] = 0;
				status[i] = 0;
				enemiesBlocking[i] = 0;
				targetJunctionBlocking[i] = null;
			}
		}











		if (firsttime) { //if endgame is not on, then this will run to check for endgame


			Node mapTarget;
			List<Node> surroundingJunctions = new ArrayList<>();

			allSurroundingJunctions.clear();
			allSurroundingJunctions0.clear();
			allSurroundingJunctions1.clear();
			allSurroundingJunctions2.clear();
			allSurroundingJunctions3.clear();

			Node currentCentralLocation = game.getAttacker().getLocation();
			allSurroundingJunctions.add(currentCentralLocation);

			int counting = 0;

			for (Node p: game.getAttacker().getLocation().getNeighbors()) { //this will start to check the closest nodes to attacker until reaching junction
				if (p != null) {
					mapTarget = p;
					allSurroundingJunctions.add(mapTarget);

					switch(counting) {
						case 0:
							allSurroundingJunctions0.add(mapTarget);
							break;
						case 1:
							allSurroundingJunctions1.add(mapTarget);
							break;
						case 2:
							allSurroundingJunctions2.add(mapTarget);
							break;
						case 3:
							allSurroundingJunctions3.add(mapTarget);
							break;
					}

					while (mapTarget != null) {

						if (mapTarget.getNumNeighbors() < 3) {
							for (Node n : mapTarget.getNeighbors()) {
								if (n != null && !allSurroundingJunctions.contains(n)) {
									allSurroundingJunctions.add(n);
									mapTarget = n;
									switch (counting) {
										case 0:
											allSurroundingJunctions0.add(mapTarget);
											break;
										case 1:
											allSurroundingJunctions1.add(mapTarget);
											break;
										case 2:
											allSurroundingJunctions2.add(mapTarget);
											break;
										case 3:
											allSurroundingJunctions3.add(mapTarget);
											break;
									}
									break;
								}
								else if (surroundingJunctions.contains(n) && allSurroundingJunctions.contains(n)) {

									mapTarget = n;
									break;
								}

							}
						}
						else if (mapTarget.getNumNeighbors() >= 3) {
							surroundingJunctions.add(mapTarget);

							allSurroundingJunctions.add(mapTarget);
							switch (counting) {
								case 0:
									allSurroundingJunctions0.add(mapTarget);
									break;
								case 1:
									allSurroundingJunctions1.add(mapTarget);
									break;
								case 2:
									allSurroundingJunctions2.add(mapTarget);
									break;
								case 3:
									allSurroundingJunctions3.add(mapTarget);
									break;
							}

							break;

						}
					}
					counting++;
				}


			}

			boolean pillInWay = false; //checks if pill is near attacker, because endgame should not happen if so
			for (Node n: allSurroundingJunctions)
				if (game.getPowerPillList().contains(n))
					pillInWay = true;


			int[][] matchingToJunction = new int[surroundingJunctions.size()][4]; //this will check if enemies can reach those junction nodes around attacker
			for (int i = 0; i < surroundingJunctions.size(); i++) {
				for (int j = 0; j < 4; j++) {
					if (!enemies.get(j).isVulnerable() && (enemies.get(j).getPathTo(surroundingJunctions.get(i)).size() <= (game.getAttacker().getPathTo(surroundingJunctions.get(i)).size()) && (enemies.get(j).getLocation().getPathDistance(surroundingJunctions.get(i)) != -1))) {
						matchingToJunction[i][j] = 1;

					}
				}
			}

			int count; //starts process of checking if enough enemies match all the junctions around attacker
			boolean goodgood;
			boolean cannot = false;
			for (int i = 0; i < surroundingJunctions.size(); i++) {
				goodgood = false;
				for (int j = 0; j < 4; j++){
					if (matchingToJunction[i][j] == 1) {
						goodgood = true;

					}
				}
				if (!goodgood) {
					cannot = true;
					break;
				}

			}
			if (!cannot) {
				count = 0;
				for (int j = 0; j < 4; j++) {
					goodgood = false;
					for (int i = 0; i < surroundingJunctions.size(); i++) {
						if (matchingToJunction[i][j] == 1) {
							goodgood = true;

						}
					}
					if (goodgood)
						count++;

				}
				if (count == surroundingJunctions.size()) {
					cannot = false;
				}
				else
					cannot = true;
			}


			int[] enemyNum = {0,0,0,0}; //looks at how many junctions each enemy can beat the attacker to
			if (!cannot && !pillInWay && firsttime) {
				for (int i = 0; i < surroundingJunctions.size(); i++) {

					for (int j = 0; j < 4; j++) {
						if (matchingToJunction[i][j] == 1) {
							enemyNum[j]++;
						}

					}

				}

			}




			if ((!cannot && !pillInWay) && currentNumPaclives == numberPaclives) {

				Arrays.fill(matchNode, null);
				Arrays.fill(endgame, 0);
				nodesCovered.clear();
				centralLocation = currentCentralLocation;

				for (int i = 1; i < 5; i++) { //matches each attacker with junction called matchNode that they will be going to
					for (int j = 0; j < 4; j++) {
						if (enemyNum[j] == i) {
							for (int k = 0; k < surroundingJunctions.size(); k++) {
								if (matchingToJunction[k][j] == 1) {

									matchNode[j] = surroundingJunctions.get(k);
									matchingToJunction[k][0] = 2;
									matchingToJunction[k][1] = 2;
									matchingToJunction[k][2] = 2;
									matchingToJunction[k][3] = 2;
									break;
								}
							}
						}
					}
				}

				for (int i = 0; i < 4; i++) {
					if (matchNode[i] != null) {
						status[i] = 4;
					}

				}
				firsttime = false;
			}
			else {
				firsttime = true;
			}


		}


		if (!firsttime) { //enters endgame mode
			for (int i = 0; i < 4; i++) {
				if (matchNode[i] != null) {
					status[i] = 4;
				}

			}

			for (int i = 0; i < 4; i++) { //enemies first head to junctions
				if (matchNode[i] != null && !enemies.get(i).getLocation().equals(matchNode[i]) && endgame[i] < 1) {
					actions[i] = enemies.get(i).getNextDir(matchNode[i], true);

				}
				else if (matchNode[i] != null && endgame[i] < 1) {
					for (Node n : matchNode[i].getNeighbors()) {
						if (n != null && allSurroundingJunctions.contains(n) && !nodesCovered.contains(n)) {
							actions[i] = enemies.get(i).getNextDir(n, true);
							nodesCovered.add(n);
							endgame[i] = 1;

						}
					}
				}
				else
					endgame[i] = 2;


			}


			for (int i = 0; i < 4; i++) {
				if (endgame[i] >= 2) {
					if (!enemies.get(i).getLocation().equals(centralLocation) && endgame[i] == 2) { //then enemies head to the original spot of attacker when endgame was called
						actions[i] = enemies.get(i).getNextDir(centralLocation, true);

						endgame[i] = 3;
					}
					else if (endgame[i] == 3) {//then enemies head to more specific attacker area
						if (allSurroundingJunctions0.size() != 0 && allSurroundingJunctions0.contains(game.getAttacker().getLocation())) {
							for (Node r : enemies.get(i).getPossibleLocations()) {
								if (allSurroundingJunctions0.contains(r))
									actions[i] = enemies.get(i).getNextDir(r, true);
							}
						} else if (allSurroundingJunctions1.size() != 0 && allSurroundingJunctions1.contains(game.getAttacker().getLocation())) {
							for (Node r : enemies.get(i).getPossibleLocations()) {
								if (allSurroundingJunctions1.contains(r))
									actions[i] = enemies.get(i).getNextDir(r, true);
							}
						} else if (allSurroundingJunctions2.size() != 0 && allSurroundingJunctions2.contains(game.getAttacker().getLocation())) {
							for (Node r : enemies.get(i).getPossibleLocations()) {
								if (allSurroundingJunctions2.contains(r))
									actions[i] = enemies.get(i).getNextDir(r, true);
							}
						} else if (allSurroundingJunctions3.size() != 0 && allSurroundingJunctions3.contains(game.getAttacker().getLocation())) {
							for (Node r : enemies.get(i).getPossibleLocations()) {
								if (allSurroundingJunctions3.contains(r))
									actions[i] = enemies.get(i).getNextDir(r, true);
							}
						} else
							actions[i] = enemies.get(i).getNextDir(game.getAttacker().getLocation(), true);

						endgame[i] = 4;


					}
					else
					{ //finally enemies head straight to attacker
						actions[i] = enemies.get(i).getNextDir(game.getAttacker().getLocation(), true);

					}

				}
			}


		}



		for (int k = 0; k < 4; k++) { //goes into normal behaviors
			switch (k) {
				case 0:
					switch (status[0]) {
						case 0:
							actions[chase] = enemies.get(chase).getNextDir(game.getAttacker().getLocation(), true); //default chaser
							break;
						case 1: //cases 1-5 are included are already made
							break;
						case 2:
							break;
						case 4:
							break;
						case 5:
							break;

					}
					break;
				case 1:
					switch (status[1]) {
						case 0: //default mapper or surrounder
							if (enemies.get(surround1).getLocation().getPathDistance(game.getAttacker().getLocation()) > Map.size()/5) {  //this is the secondary behavior of "surround1", just a chasing mechanism
								actions[surround1] = enemies.get(surround1).getNextDir(game.getAttacker().getLocation(), true); //it allows enemy to approach closer based on certain parameters and can be tweaked for best enemy dynamics
							}
							else { //this is the default behavior of surround1, looks at distances between itself and nodes comparing it to distances between attacker and nodes
								int diff;
								int min = 0;
								int node1 = 0;
								int tempnode1;
								boolean toggle1 = true;
								for (int i = 0; i < enemies.get(surround1).getLocation().getNeighbors().size(); i++) {

									tempnode1 = 0;
									if (enemies.get(surround1).getLocation().getNeighbors().get(i) != null && enemies.get(surround1).getReverse() != enemies.get(surround1).getNextDir(enemies.get(surround1).getLocation().getNeighbors().get(i), true)) {
										for (int j = 0; j < Map.size(); j++) {

											diff = enemies.get(surround1).getLocation().getNeighbors().get(i).getPathDistance(Map.get(j)) - game.getAttacker().getLocation().getPathDistance(Map.get(j));

											if (diff > -1) {
												tempnode1++;
											}
										}

										if ((tempnode1 <= node1) || node1 == 0) {

											node1 = tempnode1;
											min = i;
											toggle1 = false;
										}
									}

								}
								if (toggle1)
									actions[surround1] = enemies.get(surround1).getNextDir(game.getAttacker().getLocation(), true);
								else
									actions[surround1] = enemies.get(surround1).getNextDir(enemies.get(surround1).getLocation().getNeighbors().get(min), true);
							}
							break;
						case 1:// cases 1-5 already made, just allows to run through
							break;
						case 2:
							break;
						case 4:
							break;
						case 5:
							break;


					}
					break;
				case 2:
					switch (status[2]) {
						case 0: //default surrounder using info from enemy surround1 as well
							if (enemies.get(surround2).getLocation().getPathDistance(game.getAttacker().getLocation()) > Map.size()/5) {//secondary behavior of enemy "surround2", is just a chasing mechanism
								actions[surround2] = enemies.get(surround2).getNextDir(game.getAttacker().getLocation(), true); //allows enemy to approach closer based on certain parameters, this part can be tweaked to achieve best enemy dynamics
							}
							else {

								boolean toggle2 = true;
								int diff2;
								int diff;
								int min2 = 0;
								int node2 = 0;
								int tempnode2;
								for (int i = 0; i < enemies.get(surround2).getLocation().getNumNeighbors(); i++) {
									tempnode2 = 0;

									if (enemies.get(surround2).getLocation().getNeighbors().get(i) != null && enemies.get(surround2).getReverse() != enemies.get(surround2).getNextDir(enemies.get(surround2).getLocation().getNeighbors().get(i), true)) {

										for (int j = 0; j < Map.size(); j++) {

											diff2 = enemies.get(surround2).getLocation().getNeighbors().get(i).getPathDistance(Map.get(j)) - game.getAttacker().getLocation().getPathDistance(Map.get(j));
											diff = enemies.get(surround1).getLocation().getPathDistance(Map.get(j)) - game.getAttacker().getLocation().getPathDistance(Map.get(j)) - 1;
											if (diff < diff2)
												diff2 = diff;

											if (diff2 > -1) {
												tempnode2++;
											}

										}
										if (tempnode2 <= node2 || node2 == 0) {
											node2 = tempnode2;
											min2 = i;
											toggle2 = false;
										}

									}
								}
								if (toggle2)
									actions[surround2] = enemies.get(surround2).getNextDir(game.getAttacker().getLocation(), true);
								else
									actions[surround2] = enemies.get(surround2).getNextDir(enemies.get(surround2).getLocation().getNeighbors().get(min2), true);
							}
							break;
						case 1: //cases 1-5 stated previously
							break;
						case 2:
							break;
						case 4:
							break;
						case 5:
							break;
					}
					break;
				case 3:
					switch (status[3]) {
						case 0: //default chaser, chases at a point slightly ahead of pacman location based on pacman direction
							boolean inloop = false;
							Node target;
							int counter = 0;
							int direction = game.getAttacker().getDirection();
							target = game.getAttacker().getLocation().getNeighbor(direction);
							if (target == null) {
								for (Integer g: game.getAttacker().getPossibleDirs(true)) {
									if (g != null && g != game.getAttacker().getReverse()) {
										target = game.getAttacker().getLocation().getNeighbor(g);
										direction = g;
									}
								}

							}
							while (counter < 10) {
								counter++;
								if (target.getNumNeighbors() >= 3) {
									break;
								}
								else if (target.getNeighbor(direction) != null) {
									target = target.getNeighbor(direction);
								}
								else {
									for (int i = 0; i < 4; i++) {
										if (target.getNeighbor(i) != null && Math.abs(direction - i) != 2) {
											target = target.getNeighbor(i);
											direction = i;
										}
									}
								}


							}
							if (game.getAttacker().getPathTo(target).contains(enemies.get(ahead).getLocation()))
								actions[ahead] = enemies.get(ahead).getNextDir(game.getAttacker().getLocation(), true);
							else
								actions[ahead] = enemies.get(ahead).getNextDir(target, true);
							break;
						case 1: //already stated above
							break;
						case 2:
							break;
						case 4:
							break;
						case 5:
							break;
					}
					break;

			}
		}


		if ((currentNumPaclives != numberPaclives ||!allSurroundingJunctions.contains(game.getAttacker().getLocation()) && !firsttime)) { //turns off endgame if pacman dies or somehow escapes
			if (!(currentNumPaclives > numberPaclives)) {
				Arrays.fill(endgame, 0);
				Arrays.fill(status, 0);
				firsttime = true;
			}
			numberPaclives = currentNumPaclives;

		}

		for (int i = 0; i < 4; i++) { //turns off endgame if the random direction reverse occurs
			if (matchNode[i] != null) {
				if (enemyDirection[i] == enemies.get(i).getReverse() && enemyDirection[i] != -50 && !firsttime) {
					firsttime = true;
					Arrays.fill(endgame, 0);
					Arrays.fill(status, 0);
				}

			}
		}

		for (int i = 0; i < 4; i++) { //checks previous direction to see if direction reverse has occurred
			enemyDirection[i] = enemies.get(i).getDirection();
		}

		return actions;
	}

}
