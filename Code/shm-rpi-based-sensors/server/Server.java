import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.io.DataInputStream;
import java.io.DataOutputStream;


public class Server {
	
	private static String rawDataPath = "./Test_RawDataPlot.csv";

	@SuppressWarnings("unused")
	public static void main(String args[]) throws IOException, SocketException, ClassNotFoundException{	
		
		Scanner scan = new Scanner(System.in);

		// ===== Input Parameters ======= //
		int lengthOfDataset = 2500;        
		int samplingFrequency = 250; // Hz
		int numberOfNodes = 1;
		int numberOfPeaks = 3;
		int property = 2;// 1:= get acceleration from the node, 2:= get raw displacements, 3:= get max/min displacements  
		
		// ============================== //
		
		double[][] x_rawAccelerationData = new double[numberOfNodes][lengthOfDataset];
		double[][] x_rawAccelerationData2 = new double[numberOfNodes][lengthOfDataset];
		double[][] x_rawDisplacementData = new double[numberOfNodes][lengthOfDataset];
		double[][] x_rawDisplacementData2 = new double[numberOfNodes][lengthOfDataset];
		double x_max_displacement [] = new double[numberOfNodes];
        double x_max_displacement2[] = new double[numberOfNodes];
        double x_min_displacement [] = new double[numberOfNodes];
        double x_min_displacement2[] = new double[numberOfNodes];
        
		double[][] y_rawAccelerationData = new double[numberOfNodes][lengthOfDataset];
		double[][] y_rawAccelerationData2 = new double[numberOfNodes][lengthOfDataset];
		double[][] y_rawDisplacementData = new double[numberOfNodes][lengthOfDataset];
		double[][] y_rawDisplacementData2 = new double[numberOfNodes][lengthOfDataset];
		double y_max_displacement [] = new double[numberOfNodes];
        double y_max_displacement2[] = new double[numberOfNodes];
        double y_min_displacement [] = new double[numberOfNodes];
        double y_min_displacement2[] = new double[numberOfNodes];
        
		double[][] z_rawAccelerationData = new double[numberOfNodes][lengthOfDataset];
		double[][] z_rawAccelerationData2 = new double[numberOfNodes][lengthOfDataset];
		double[][] z_rawDisplacementData = new double[numberOfNodes][lengthOfDataset];
		double[][] z_rawDisplacementData2 = new double[numberOfNodes][lengthOfDataset];
		double z_max_displacement [] = new double[numberOfNodes];
        double z_max_displacement2[] = new double[numberOfNodes];
        double z_min_displacement [] = new double[numberOfNodes];
        double z_min_displacement2[] = new double[numberOfNodes];        
		
        double[][] rawTimeData = new double[numberOfNodes][lengthOfDataset];		
        double[][] x_detFrequencies	= new double[numberOfNodes][numberOfPeaks];
        double[][] x_detFrequencies2	= new double[numberOfNodes][numberOfPeaks];
        double[][] y_detFrequencies	= new double[numberOfNodes][numberOfPeaks];
        double[][] y_detFrequencies2	= new double[numberOfNodes][numberOfPeaks];
        double[][] z_detFrequencies	= new double[numberOfNodes][numberOfPeaks];
        double[][] z_detFrequencies2	= new double[numberOfNodes][numberOfPeaks];
        
        System.out.println("Server is running");

        ServerSocket 		ss[]	= new ServerSocket[numberOfNodes];
        Socket 				s[]		= new Socket[numberOfNodes];
        DataOutputStream 	OUT[] 	= new DataOutputStream[numberOfNodes];
        DataInputStream 	IN[] 	= new DataInputStream[numberOfNodes];
        
        for(int node = 0; node < numberOfNodes; node++){
        	
        	int numberConn = 1234+node;
        	System.out.println("Connecting node " + numberConn );
        	ss[node] = new ServerSocket(numberConn);
    		s[node] = ss[node].accept();
            OUT[node] 	= new DataOutputStream	(s[node].getOutputStream());
            IN[node] 	= new DataInputStream	(s[node].getInputStream());
        	System.out.println("Node " + numberConn + " connected" );    
        }		
        
        //sending data to the sensor nodes
        for(int node = 0; node < numberOfNodes; node++){
            OUT[node].writeInt(lengthOfDataset);
    		OUT[node].writeInt(samplingFrequency);
    		OUT[node].writeInt(numberOfPeaks);
    		OUT[node].writeInt(property);
            OUT[node].flush();
      	    }      
        
        System.out.println("setup parameters transmitted\n"
        		+ "-------------------- \n \n"
        		+ "Server is ready to recieve data :) !\n\n"
        		+ "To start process, press Enter\n");
        
///!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        
        String dummy = scan.nextLine();
        
        //System.out.println("Waiting for the data...");
        
        FileWriter writer = new FileWriter(rawDataPath);
        
        // Send the command to the node 
        
        for (int node = 0; node<numberOfNodes; node++) {
        	OUT[node].writeInt(1);        	
    		OUT[node].flush();
    		//double response = IN[node].readDouble();
    		System.out.println("Node "+node+" on the run...\n");
        }
        
        for(int node = 0; node < numberOfNodes; node++){
        	       	        	
        	if (property == 1) {
        		
        		writer.write("Node: "+(node+1)+"\nSensor 1 , , , Sensor2 \nx,y,z,x,y,z\n");
            	for (int i = 0; i < lengthOfDataset; i++) {
            		x_rawAccelerationData[node][i]=IN[node].readDouble();
            		x_rawAccelerationData2[node][i]=IN[node].readDouble();
            		y_rawAccelerationData[node][i]=IN[node].readDouble();
            		y_rawAccelerationData2[node][i]=IN[node].readDouble();            		
            		z_rawAccelerationData[node][i]=IN[node].readDouble();
            		z_rawAccelerationData2[node][i]=IN[node].readDouble();            		
            		
            		writer.write(x_rawAccelerationData[node][i] +", "+y_rawAccelerationData[node][i]+", "+z_rawAccelerationData[node][i]+", "
            					+x_rawAccelerationData2[node][i]+", "+y_rawAccelerationData2[node][i]+", "+z_rawAccelerationData2[node][i]+"\n");            		
        		}
            	double deltaT=IN[node].readDouble();
            	System.out.println("Raw accleration data is written in"+rawDataPath+"\n"+deltaT);
            }
            else if(property == 2) {
            		writer.write("Node: "+(node+1)+"\nSensor 1 , , , Sensor2 \nx,y,z,x,y,z\n");
            	for (int i = 0; i < lengthOfDataset; i++) {
            		x_rawDisplacementData[node][i]=IN[node].readDouble();
            		x_rawDisplacementData2[node][i]=IN[node].readDouble();
            		y_rawDisplacementData[node][i]=IN[node].readDouble();
            		y_rawDisplacementData2[node][i]=IN[node].readDouble();
            		z_rawDisplacementData[node][i]=IN[node].readDouble();
            		z_rawDisplacementData2[node][i]=IN[node].readDouble();
            		
            		writer.write(x_rawDisplacementData[node][i] + ", "+y_rawDisplacementData[node][i]+", "+z_rawDisplacementData[node][i]+", "
            					+x_rawDisplacementData2[node][i]+", "+y_rawDisplacementData2[node][i]+", "+z_rawDisplacementData2[node][i]+"\n");
            	}
            	System.out.println("Raw displacement data is written in: "+rawDataPath);
            }
            else if (property == 3) { 
            	x_max_displacement[node]=IN[node].readDouble();
                x_min_displacement[node]=IN[node].readDouble();
                x_max_displacement2[node]=IN[node].readDouble();
                x_min_displacement2[node]=IN[node].readDouble();
                y_max_displacement[node]=IN[node].readDouble();
                y_min_displacement[node]=IN[node].readDouble();
                y_max_displacement2[node]=IN[node].readDouble();
                y_min_displacement2[node]=IN[node].readDouble();
                z_max_displacement[node]=IN[node].readDouble();
                z_min_displacement[node]=IN[node].readDouble();
                z_max_displacement2[node]=IN[node].readDouble();
                z_min_displacement2[node]=IN[node].readDouble();                
                
                System.out.println("Node "+(node+1)+" Sensor 1 Max displacement X = "+ x_max_displacement[node] +", Min displacement = "+x_min_displacement[node]+"\n");
                System.out.println("Node "+(node+1)+" Sensor 2 Max displacement X = "+ x_max_displacement2[node]+", Min displacement = "+x_min_displacement2[node]+"\n\n");            
                System.out.println("Node "+(node+1)+" Sensor 1 Max displacement Y = "+ y_max_displacement[node] +", Min displacement = "+y_min_displacement[node]+"\n");
                System.out.println("Node "+(node+1)+" Sensor 2 Max displacement Y = "+ y_max_displacement2[node]+", Min displacement = "+y_min_displacement2[node]+"\n\n");            
                System.out.println("Node "+(node+1)+" Sensor 1 Max displacement Z = "+ z_max_displacement[node] +", Min displacement = "+z_min_displacement[node]+"\n");
                System.out.println("Node "+(node+1)+" Sensor 2 Max displacement Z = "+ z_max_displacement2[node]+", Min displacement = "+z_min_displacement2[node]+"\n\n");            
                //System.out.println("Min/max values of displacements are written in: "+rawDataPath);
                }   	
            else if (property == 4) {
            	for (int i = 0; i < numberOfPeaks; i++) {
            		x_detFrequencies[node][i]=IN[node].readDouble();
            		x_detFrequencies2[node][i]=IN[node].readDouble();
            		y_detFrequencies[node][i]=IN[node].readDouble();
            		y_detFrequencies2[node][i]=IN[node].readDouble();
            		z_detFrequencies[node][i]=IN[node].readDouble();
            		z_detFrequencies2[node][i]=IN[node].readDouble();
            		
            		writer.write(x_detFrequencies[node][i] + "  ");
            		writer.write(x_detFrequencies2[node][i] + "\n");
            		writer.write(y_detFrequencies[node][i] + "  ");
            		writer.write(y_detFrequencies2[node][i] + "\n");
            		writer.write(z_detFrequencies[node][i] + "  ");
            		writer.write(z_detFrequencies2[node][i] + "\n");
                    System.out.println("Frequencies are written in: "+rawDataPath);
            	}
            }
        }        
        for(int node = 0; node < numberOfNodes; node++){
        	IN[node].close();
            OUT[node].close();
            ss[node].close(); 
            writer.close();
      	    }
  	    }
}
