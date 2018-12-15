import java.net.Socket;
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
	public static void main(String args[]) throws IOException, ClassNotFoundException{		

		// ===== Input Parameters ======= //
		int lengthOfDataset = 2048;        
		int samplingFrequency = 128; // Hz
		int numberOfNodes = 1;
		int numberOfPeaks = 3;
		// ============================== //
		
		double[][] rawAccelerationData = new double[numberOfNodes][lengthOfDataset];
		double[][] rawAccelerationData2 = new double[numberOfNodes][lengthOfDataset];
		double[][] rawDisplacementData = new double[numberOfNodes][lengthOfDataset];
		double[][] rawDisplacementData2 = new double[numberOfNodes][lengthOfDataset];
        double[][] rawTimeData = new double[numberOfNodes][lengthOfDataset];		
        
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
            OUT[node].flush();
      	    }      

        double[][]	detFrequencies	= new double[numberOfNodes][numberOfPeaks];
        double[][]	detFrequencies2	= new double[numberOfNodes][numberOfPeaks];
        
        System.out.println("setup parameters transmitted\n"
        		+ "-------------------- \n \n"
        		+ "Server is ready to recieve data :) !\n");
///!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        for(int node = 0; node < numberOfNodes; node++){
    		OUT[node].writeInt(1); // The sensor nodes are stopped until they received this random value
            OUT[node].flush();
        	System.out.println("\n \n Acceleration node  " + (node + 1));
            for(int i = 0; i < lengthOfDataset; i++){
	        	rawAccelerationData[node][i] = IN[node].readDouble(); // reading Acc. data from the nodes
	        	rawAccelerationData2[node][i] = IN[node].readDouble();	        	
	        	System.out.print(rawAccelerationData[node][i] + "	");
	        	System.out.println(rawAccelerationData2[node][i]);	        		
	        	}
            System.out.println("================ Acceleration node  " + (node + 1) + " finished ================= ");
            for (int i=0;i<lengthOfDataset; i++) {
            	rawDisplacementData[node][i] = IN[node].readDouble(); // reading Acc. data from the nodes
	        	rawDisplacementData2[node][i] = IN[node].readDouble();
	        	System.out.print(rawDisplacementData[node][i] + "	");
	        	System.out.println(rawDisplacementData2[node][i]);	        
	            }
            System.out.println("================ Displacement node  " + (node + 1) + " finished ================= ");
        	}
//!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        for(int node = 0; node < numberOfNodes; node++){
        for(int i = 0; i < numberOfPeaks; i++){
        	detFrequencies[node][i] = IN[node].readDouble(); // reading Acc. data from the nodes
        	detFrequencies2[node][i] = IN[node].readDouble();
        	}
        }
        
      	FileWriter writer = new FileWriter(rawDataPath + ".txt");
      	
      	for(int node = 0; node < numberOfNodes; node++){
      	for(int k = 0; k < lengthOfDataset; k++){
      		writer.write(rawAccelerationData[node][k]  + "	");
      		writer.write(rawAccelerationData2[node][k]  + "\n");
      	    }
      	writer.write("\n \n \n");
      	}
      	
      	writer.flush();
      	writer.close();
        
        System.out.println("acceleration-data written into " + rawDataPath);
        System.out.println("\n-----------------\nServer shutting down");
        
        System.out.println("acceloration-data written into " + rawDataPath);
        
        System.out.print("\n \n recieved Frequencies at first sensor: " );

      	for(int node = 0; node < numberOfNodes; node++){
        for (int i = 0; i < numberOfPeaks; i++) {
        	detFrequencies[node][i]=detFrequencies[node][i]*((double)samplingFrequency)/lengthOfDataset;
        	System.out.println();
        	System.out.print(detFrequencies[node][i]);
			System.out.println();
			System.out.println(detFrequencies2[node][i]);
		}
      	}
     
        for(int node = 0; node < numberOfNodes; node++){
        	IN[node].close();
            OUT[node].close();
            ss[node].close(); 
      	    }
  	}

}
