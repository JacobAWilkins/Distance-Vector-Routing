# Distance-Vector-Routing

### Compilation:

javac DistV ec.java

### Run:

Format: java DistV ec [input file]

Example: java DistV ec input.txt

### Instructions

First, you will be prompted to pick the mode. You can choose 1 for “Step by Step” or 2
for “Without Intervention.” Enter the number and press ENTER.

- In “Step by Step” mode, you must press ENTER to progress through each step of
the algorithm until the distance vector table is stable. Once stable, the number of
steps taken will be displayed.

- In “Without Intervention” mode, the algorithm will run through without stops and
display the elapsed time and number of steps taken.

Next, you will be given prompts to change the cost of a link. First, you must enter the
source router, then the destination router, and finally the changed cost of the link. After all of this information is gathered, the program will update the distance vector table from its previous state.
