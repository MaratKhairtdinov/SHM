#!/bin/bash

sudo cd /home/pi/Desktop/shm/
sudo javac -d bin -sourcepath nodes -cp lib/rsc.jar:lib/pi4j-core.jar nodes/SensorNode.java
sudo java -cp bin:lib/rsc.jar:lib/pi4j-core.jar SensorNode

#1. check if the folder working directory exists
#2. check if the .java file / files exist (nodes & SensorNodes)
#3. once these 3 are done ... only then run the code
#4. indication of the the failed state at any point
