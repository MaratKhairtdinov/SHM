 
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import com.pi4j.io.i2c.I2CFactory;

import fourier.FrequencySpectrum;
import fourier.PeakPicking;

public class PP_OnRaspPi_Main{
	
	static int 		samplingRate;     	// [Hz = s/n]
	static int 		lengthOfDataset; 	// [n]
	static int 		numberOfPeaks;		// [n]
	static double 	acceleration[];		// [x-acceleration, y-acceleration, z-acceleration][in g]		
	
	public static String serverIP = "192.168.0.12";
	
    public static void main(String[] args) throws IOException, I2CFactory.UnsupportedBusNumberException, InterruptedException, ClassNotFoundException {
    	/*
    	 * Initiating communication to the server:
    	 */
        Socket s = new Socket(serverIP,1234);
        Scanner sc = new Scanner(s.getInputStream());
        PrintStream p = new PrintStream(s.getOutputStream());
        ObjectOutputStream out = new ObjectOutputStream(s.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(s.getInputStream());
        
        /*
         * Receiving setup parameters and calculating delta t
         */
        int[] setupParameters = new int[3];
        setupParameters 	= (int[]) in.readObject();
        lengthOfDataset 	= setupParameters[0];
        samplingRate		= setupParameters[1];
        numberOfPeaks		= setupParameters[2];

        int deltaT = (int) 1000 / samplingRate;
        
        double [] accelerationData_x = new double[lengthOfDataset];
        double [] accelerationData_x2 = new double[lengthOfDataset];

        SensorConnection sensorOne = new SensorConnection(0);
        SensorConnection sensorTwo = new SensorConnection(1);
        
        for (int i = 0; i < lengthOfDataset; i++) 
        {
        	acceleration = sensorOne.readAcceleration();
        	accelerationData_x[i] = sensorOne.readAcceleration()[0];
        	accelerationData_x2[i] = sensorTwo.readAcceleration()[0];
        	Thread.sleep(deltaT); 
		}
        
        /*
         * Calculating the frequency spectrum of the stored data, processing the peak picking analysis
         */
        FrequencySpectrum 	fSpec = new FrequencySpectrum(accelerationData_x, deltaT);      
        PeakPicking 		pp 	  = new PeakPicking(numberOfPeaks, fSpec);
        int [] 				detectedPeaks = pp.getPeaks();
        
        FrequencySpectrum 	fSpec2 = new FrequencySpectrum(accelerationData_x, deltaT);      
        PeakPicking 		pp2 	  = new PeakPicking(numberOfPeaks, fSpec);
        int [] 				detectedPeaks2 = pp.getPeaks();

        /*
    	 * transmitting acceleration-data to the server
    	 */
    	for (int i = 0; i < accelerationData_x.length; i++) {
    		out.writeDouble(accelerationData_x[i]);	
		}
    	for (int i = 0; i < accelerationData_x.length; i++) {
    		out.writeDouble(accelerationData_x2[i]);	
		}
    	
    	/*
    	 * transmitting the detected peaks to the server
    	 * 
    	 */
    	for (int i = 0; i < detectedPeaks.length; i++) {
    		out.writeDouble(detectedPeaks[i]);
		}
    	for (int i = 0; i < detectedPeaks.length; i++) {
    		out.writeDouble(detectedPeaks2[i]);
		}
    	
    	s.close();sc.close();p.close();out.close();in.close();
    	
    }
}