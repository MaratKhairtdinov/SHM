import java.net.Socket;
import java.util.Scanner;
import java.io.PrintStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import com.pi4j.io.i2c.I2CBus;
import com.pi4j.io.i2c.I2CFactory;
import daq.ADXL345;

import fourier.PeakPicking;
import fourier.FrequencySpectrum;


public class SensorNode {
	static int 		samplingRate;     	// [Hz = s/n]
	static int 		lengthOfDataset; 	// [n]
	static int 		numberOfPeaks;		// [n]
	static double 	acceleration[];		// [x-acceleration, y-acceleration, z-acceleration][in g]		
	
	public static String serverIP = "192.168.61.108";
	
    public static void main(String[] args) throws IOException, I2CFactory.UnsupportedBusNumberException, InterruptedException, ClassNotFoundException {
    	/*
    	 * Initiating communication to the server:
    	 */
    	Socket s = new Socket(serverIP,1234);
        DataOutputStream out = new DataOutputStream(s.getOutputStream());
        DataInputStream in = new DataInputStream(s.getInputStream());        
        /*
         * Receiving setup parameters and calculating delta t
         */
        lengthOfDataset = in.readInt(); // the software runs until this point, waiting for data from the server
        samplingRate 	= in.readInt();
        numberOfPeaks	= in.readInt();
        
        ADXL345 adxl345 = new ADXL345(I2CBus.BUS_1, ADXL345.ADXL345_ADDRESS_ALT_LOW);
        ADXL345 adxl345_1d = new ADXL345(I2CBus.BUS_1, ADXL345.ADXL345_ADDRESS_ALT_HIGH);
        adxl345.setup();
        adxl345.writeRange(ADXL345.ADXL345_RANGE_2G);
        adxl345.writeFullResolution(true);
        adxl345.writeRate(ADXL345.ADXL345_RATE_100);
        
        adxl345_1d.setup();
        adxl345_1d.writeRange(ADXL345.ADXL345_RANGE_2G);
        adxl345_1d.writeFullResolution(true);
        adxl345_1d.writeRate(ADXL345.ADXL345_RATE_100);
        
        float scalingFactor = adxl345.getScalingFactor();
        
        short[] raw = new short[3]; //it is necessary to have a vector with 3 values
        short[] raw_1d = new short[3];
        double[] x_accelerations_s1 = new double[lengthOfDataset];
        double[] x_accelerations_s2 = new double[lengthOfDataset];
        double[] x_displacements_s1 = new double[lengthOfDataset];
        double[] x_displacements_s2 = new double[lengthOfDataset];
        double[] timeOfAcceleration = new double[lengthOfDataset];
        
        int deltaT = (int) 1000 / samplingRate; // sleeping time acc. to sampling frequency
        
        double x_current_velocity_s1 = 0;
        double x_current_velocity_s2 = 0;        
        double x_current_disp_s1 = 0;
        double x_current_disp_s2 = 0;
        for (int i = 0; i < lengthOfDataset; i++) 
        {
        	adxl345.readRawAcceleration(raw);
       		adxl345_1d.readRawAcceleration(raw_1d);
       		double x_acc_s1=(double) raw[0]*scalingFactor;
       		double x_acc_s2=(double) raw_1d[0]*scalingFactor;       		
            x_accelerations_s1[i] = x_acc_s1;
            x_accelerations_s2[i] = x_acc_s2;
            x_current_velocity_s1+=x_acc_s1*deltaT;
            x_current_velocity_s2+=x_acc_s2*deltaT;  
            x_current_disp_s1+=x_current_velocity_s1*deltaT;
            x_current_disp_s2+=x_current_velocity_s2*deltaT;
            x_displacements_s1[i] = x_current_disp_s1;
            x_displacements_s2[i] = x_current_disp_s2;            
            Thread.sleep(deltaT);    
		}
        
        // Calculating the frequency spectrum of the stored data, processing the peak picking analysis
        FrequencySpectrum 	fSpec = new FrequencySpectrum(x_accelerations_s1, deltaT);      
        PeakPicking 		pp 	  = new PeakPicking(numberOfPeaks, fSpec);
        int [] 				detectedPeaks = pp.getPeaks();
        
        FrequencySpectrum 	fSpec2 = new FrequencySpectrum(x_accelerations_s2, deltaT);      
        PeakPicking 		pp2 	  = new PeakPicking(numberOfPeaks, fSpec);
        int [] 				detectedPeaks2 = pp.getPeaks();

        // transmitting acceleration-data to the server
    	for (int i = 0; i < lengthOfDataset; i++) {
    		out.writeDouble(x_accelerations_s1[i]);	
    		out.writeDouble(x_accelerations_s2[i]);
		}
    	
    	// transmitting displacement-data to the server
    	for (int i = 0; i < lengthOfDataset; i++) {
    		out.writeDouble(x_displacements_s1[i]);	
    		out.writeDouble(x_displacements_s2[i]);
		}
    	
    	//transmitting the detected peaks to the server
    	for (int i = 0; i < detectedPeaks.length; i++) {
    		out.writeDouble(detectedPeaks[i]);
    		out.writeDouble(detectedPeaks2[i]);
    	}
    	
    	s.close();
    	out.close();
    	in.close();
    	
    }
}