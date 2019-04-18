// Author: Jacob Wilkins

import java.io.*;
import java.util.*;

public class DistVec {
	
	static int graph[][]; // Keeps track of the costs between routers
	static int inter[][]; // Keeps track of intermediary distances
	static int route[][]; // Keeps track of the updated routes of each router
	
	public static void main(String[] args) throws Exception {
		File file = new File(args[0]); // Makes File using specified file name
		BufferedReader br = new BufferedReader(new FileReader(file)); // BF for reading the input file
		
		Vector<Integer> nodes = new Vector<Integer>(); // Vector keeping track of routers
		Vector<String> connections = new Vector<String>(); // Vector keeping track of connections
		
		// Read connection information from input file
		String link; 
		while ((link = br.readLine()) != null) {
			connections.add(link); // Add link to connections Vector
			String[] connection = link.split(" "); // Tokenize link, getting two routers and the cost
			
			// If the first router isn't in the vector yet, add it
			if (!nodes.contains(Integer.parseInt(connection[0]))) {
				nodes.add(Integer.parseInt(connection[0]));
			}
			// If the second router isn't in the vector yet, add it
			if (!nodes.contains(Integer.parseInt(connection[1]))) {
				nodes.add(Integer.parseInt(connection[1]));
			}
		}
		
		// Distance vector graph setup
		int numNodes = nodes.size(); // Number of nodes in the graph
		graph = new int[numNodes][numNodes]; // Initialize graph array
		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {
				if (i == j) { // Link cost of routers to themselves is 0
					graph[i][j] = 0;
				} else { // Else, the link cost is initialized to infinity (16)
					graph[i][j] = 16;
				}
			}
		}
		
		route = new int[numNodes][numNodes]; // Initialize route array
		inter = new int[numNodes][numNodes]; // Initialize inter array
		
		// Goes through the connections vector and adds each one to the graph array bidirectionally
		for (int i = 0; i < connections.size(); i++) {
			link = connections.get(i);
			String[] connection = link.split(" ");
			int j = nodes.indexOf(Integer.parseInt(connection[0]));
			int k = nodes.indexOf(Integer.parseInt(connection[1]));
			graph[j][k] = Integer.parseInt(connection[2]);
			graph[k][j] = Integer.parseInt(connection[2]);
		}
		
		Boolean stepBy = false; // Used to determine the mode being used
		Scanner scan = new Scanner(System.in); // Used to take input from the user
		System.out.println("1: Step by Step");
		System.out.println("2: Without Intervention");
		System.out.print("Enter number for corresponding mode: ");
		String mode = scan.nextLine(); // User chooses the mode
		
		if (mode.equals("1")) { // If mode is 1, set mode to "Step by Step"
			stepBy = true;
		} else if (mode.equals("2")) { // If mode is 2, set mode to "Without Intervention"
			stepBy = false;
		} else { // Otherwise, the mode isn't recognized and the program exits
			System.out.println("Unrecognized mode.\nExiting...");
			System.exit(0);
		}
		
		tableSetup(numNodes); // Set up the route and inter tables
		distVecCalc(numNodes, nodes, stepBy); // Calculate the distance vector table
		System.out.println("Initial DV Table: \n");
		printTable(numNodes, nodes); // print the initial distance vector table
		
		// Loops the link change process
		while (true) {
			// Change a link cost
			System.out.print("Enter the source node for the cost change: ");
			String source = scan.nextLine(); // Get the source router
			System.out.print("Enter the destination node for the cost change: ");
			String dest = scan.nextLine(); // Get the destination router
			System.out.print("Enter the new cost (16 for link failure): ");
			String cost = scan.nextLine(); // Get the new cost
			
			// Update graph array using new cost bidirectionally
			int j = nodes.indexOf(Integer.parseInt(source));
			int k = nodes.indexOf(Integer.parseInt(dest));
			graph[j][k] = Integer.parseInt(cost);
			graph[k][j] = Integer.parseInt(cost);
			
			tableSetup(numNodes); // Set up tables again
			distVecCalc(numNodes, nodes, stepBy); // Recalculate DV table for updated graph
			System.out.println("New DV Table: \n");
			printTable(numNodes, nodes); // Print new DV table
			
			System.out.print("Enter \"ENTER\" to continue OR \"q\" to quit: ");
			String control = scan.nextLine(); // Check whether user ones to continue or quit
			if (control.equals("q")) {
				System.exit(0);
			}
		}
	}
	
	// Calculates the distance vectors of all the routers
	static void distVecCalc(int numNodes, Vector<Integer> nodes, Boolean stepBy) {
		long start = 0;
		if (!stepBy) { // Start timer if not in stey by step mode
			start = System.currentTimeMillis();
		}
		int source = 0, cycles = 0, stableCount = 0;
		printTable(numNodes, nodes);
		for (int i = 0; i < 4*numNodes; i++) { // I iterate for 4 time the number of routers to give the table a chance to fully update
			Boolean stable = updateTable(numNodes, source); // Updates the table at the given source router and checks if the table is stable
			// If the table is stable twice, the algorithm exits
			// I do this because sometimes the table doesn't update for one step and then updates in the next
			if (stable) {
				stableCount++; // Increase the counter for stable signals
				if (stableCount == 2) {
					System.out.println("Stable state detected.");
					break; // Exit loop when table is significantly stable
				}
			}
			System.out.println("Updating...\n");
			printTable(numNodes, nodes); // Print updated table
			cycles++; // counts the number of cycles
			source++; // updates the source router
			if (source == numNodes) { // If the max router is test, it starts over from 0
				source = 0;
			}
			// Implement Step by Step mode (This will be ignored in Without Intervention mode)
			if (stepBy) { // Stop algorithm after each step, until user presses ENTER
				System.out.println("Press \"ENTER\" to continue...");
				Scanner scan = new Scanner(System.in);
				scan.nextLine();
			}
		}

		if (!stepBy) { // Print the time elapsed
			long finish = System.currentTimeMillis();
			long timeElapsed = finish - start;
			System.out.println("Elapsed Time: " + timeElapsed + " ms");
		}
		System.out.println("Number of cycles: " + cycles + "\n"); // Print number of cycles
	}
	
	// Update the table given a source router
	static Boolean updateTable(int numNodes, int source) {
		Boolean stable = true; // Stable indicator is true by default
		// Update values based on Bellman-Ford Equation
		for (int i = 0; i < numNodes; i++) {
			if (graph[source][i] != 16) { // Checks for links
				int cost = graph[source][i]; // Gets link cost
				// Check the inter distance for each router
				for (int j = 0; j < numNodes; j++) {
					int interDist = route[i][j];
					if (inter[i][j] == source) {
						interDist = 16;
					}
					// Check if the link cost plus inter distance is less than the vurrent DV value in route
					if ((cost + interDist) < route[source][j]) {
						route[source][j] = cost + interDist; // Update the distance vector
						inter[source][j] = i; // Update the inter distance
						stable = false; // Indicate that the table is not stable since it is still being updated
					}
				}
			}
		}
		return stable;
	}
	
	// Set up the route and inter tables
	static void tableSetup(int numNodes) {
		for (int i = 0; i < numNodes; i++) {
			for (int j = 0; j < numNodes; j++) {
				if (i == j) {
					route[i][j] = 0; // Set the distance vector to themself to 0
					inter[i][j] = i; // Set the inter distance to the row node
				} else {
					route[i][j] = 16; // All other values are set to infinity
					inter[i][j] = 100; // Set the inter distance to 100
				}
			}
		}
	}
  
	// Prints the distance vectors in the route table
	static void printTable(int numNodes, Vector<Integer> nodes) {
		System.out.print("  ");
		for (int i = 0; i < nodes.size(); i ++) {
			System.out.print(nodes.get(i) + "          ");
		}
		System.out.println();
		
		for (int i = 0; i < numNodes; i++) {
			System.out.print(nodes.get(i) + " ");
			for (int j = 0; j < numNodes; j++) {
				System.out.print("Dist: " + route[i][j] + "    "); // Each distance vector is printed
			}
			System.out.println();
		}
		System.out.println();
	}
	
}
