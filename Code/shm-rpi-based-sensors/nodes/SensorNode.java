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
	static int 		property;
	
	public static String serverIP = "192.168.137.148";
	
    public static void main(String[] args) throws IOException, I2CFactory.UnsupportedBusNumberException, InterruptedException, ClassNotFoundException {
    	/*
    	 * Initiating communication to the server:
    	 */
    	
    	double name = 1234;
    	
    	Socket s = new Socket(serverIP,1234);
        DataOutputStream out = new DataOutputStream(s.getOutputStream());
        DataInputStream in = new DataInputStream(s.getInputStream());        
        /*
         * Receiving setup parameters and calculating delta t
         */
        lengthOfDataset = in.readInt(); // the software runs until this point, waiting for data from the server
        samplingRate 	= in.readInt();
        numberOfPeaks	= in.readInt();
        property 		= in.readInt();
        
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
        
        float scalingFactor = adxl345.getScalingFactor()/1000;
        
        short[] raw = new short[3]; //it is necessary to have a vector with 3 values
        short[] raw_1d = new short[3];
        double[] x_accelerations_s1 = new double[lengthOfDataset];
        double[] x_accelerations_s2 = new double[lengthOfDataset];
        double[] y_accelerations_s1 = new double[lengthOfDataset];
        double[] y_accelerations_s2 = new double[lengthOfDataset];
        double[] z_accelerations_s1 = new double[lengthOfDataset];
        double[] z_accelerations_s2 = new double[lengthOfDataset];
        
        double[] x_velocities_s1 = new double[lengthOfDataset];
        double[] x_velocities_s2 = new double[lengthOfDataset];
        double[] y_velocities_s1 = new double[lengthOfDataset];
        double[] y_velocities_s2 = new double[lengthOfDataset];
        double[] z_velocities_s1 = new double[lengthOfDataset];
        double[] z_velocities_s2 = new double[lengthOfDataset];
        
        double[] x_displacements_s1 = new double[lengthOfDataset];
        double[] x_displacements_s2 = new double[lengthOfDataset];
        double[] y_displacements_s1 = new double[lengthOfDataset];
        double[] y_displacements_s2 = new double[lengthOfDataset];
        double[] z_displacements_s1 = new double[lengthOfDataset];
        double[] z_displacements_s2 = new double[lengthOfDataset];
        
        double[] time_data = new double[lengthOfDataset];
        
        int deltaT = (int) 1000 / samplingRate; // sleeping time acc. to sampling frequency
        
        int dummy = in.readInt();	//start recording from this point              
        
        double x_current_velocity_s1 = 0;
        double x_current_velocity_s2 = 0;
        double y_current_velocity_s1 = 0;
        double y_current_velocity_s2 = 0;
        double z_current_velocity_s1 = 0;
        double z_current_velocity_s2 = 0;
        
        double x_current_disp_s1 = 0;
        double x_current_disp_s2 = 0;
        double y_current_disp_s1 = 0;
        double y_current_disp_s2 = 0;
        double z_current_disp_s1 = 0;
        double z_current_disp_s2 = 0;
        
        for (int i = 0; i < lengthOfDataset; i++) 
        {
        	adxl345.readRawAcceleration(raw);
       		adxl345_1d.readRawAcceleration(raw_1d); 
       		
       		double g = (double) 9.80665f;
       		
            x_accelerations_s1[i] = (double) raw[0]*scalingFactor*g;
            x_accelerations_s2[i] = (double) raw_1d[0]*scalingFactor*g;
            y_accelerations_s1[i] = (double) raw[1]*scalingFactor*g;
            y_accelerations_s2[i] = (double) raw_1d[1]*scalingFactor*g;
            z_accelerations_s1[i] = (double) (raw[2])*scalingFactor*g;
            z_accelerations_s2[i] = (double) (raw_1d[2])*scalingFactor*g;
            
            time_data[i] = System.currentTimeMillis();
            
            Thread.sleep(deltaT);    
		}        
        /*for (int i=0;i<lengthOfDataset;i++) {
        	double g=(double)9.80665f;
        	
        	x_accelerations_s1[i]*=g;
            x_accelerations_s2[i]*=g;
            y_accelerations_s1[i]*=g;
            y_accelerations_s2[i]*=g;
            z_accelerations_s1[i]*=g;
            z_accelerations_s2[i]*=g;
        }*/
        
        double deltaTime = (double) 1 / samplingRate;
       
        x_velocities_s1[0]=0;
        x_velocities_s2[0]=0;
        y_velocities_s1[0]=0;
        y_velocities_s2[0]=0;
        z_velocities_s1[0]=0;
        z_velocities_s2[0]=0;
        
        x_displacements_s1[0]=0;
        x_displacements_s2[0]=0;
        y_displacements_s1[0]=0;
        y_displacements_s2[0]=0;
        z_displacements_s1[0]=0;
        z_displacements_s2[0]=0;
        
        
        for (int i = 1; i<lengthOfDataset; i++) {
        	
        	x_current_velocity_s1+=(double)(x_accelerations_s1[i]+x_accelerations_s1[i-1])*deltaTime/2;
        	x_current_velocity_s2+=(double)(x_accelerations_s2[i]+x_accelerations_s2[i-1])*deltaTime/2;
        	y_current_velocity_s1+=(double)(y_accelerations_s1[i]+y_accelerations_s1[i-1])*deltaTime/2;
        	y_current_velocity_s2+=(double)(y_accelerations_s2[i]+y_accelerations_s2[i-1])*deltaTime/2;
        	z_current_velocity_s1+=(double)(z_accelerations_s1[i]+z_accelerations_s1[i-1])*deltaTime/2;
        	z_current_velocity_s2+=(double)(z_accelerations_s2[i]+z_accelerations_s2[i-1])*deltaTime/2;
        	
        	x_velocities_s1[i]=x_current_velocity_s1;
        	x_velocities_s2[i]=x_current_velocity_s2;
        	y_velocities_s1[i]=y_current_velocity_s1;
        	y_velocities_s2[i]=y_current_velocity_s2;
        	z_velocities_s1[i]=z_current_velocity_s1;
        	z_velocities_s2[i]=z_current_velocity_s2;
        }
        
        for (int i = 1; i<lengthOfDataset; i++) {
        	
        	x_current_disp_s1+=(double)(x_velocities_s1[i]+x_velocities_s1[i-1])*deltaTime/2;
        	x_current_disp_s2+=(double)(x_velocities_s2[i]+x_velocities_s2[i-1])*deltaTime/2;
        	y_current_disp_s1+=(double)(y_velocities_s1[i]+y_velocities_s1[i-1])*deltaTime/2;
        	y_current_disp_s2+=(double)(y_velocities_s2[i]+y_velocities_s2[i-1])*deltaTime/2;
        	z_current_disp_s1+=(double)(z_velocities_s1[i]+z_velocities_s1[i-1])*deltaTime/2;
        	z_current_disp_s2+=(double)(z_velocities_s2[i]+z_velocities_s2[i-1])*deltaTime/2;
        	
        	x_displacements_s1[i]=x_current_disp_s1;
        	x_displacements_s2[i]=x_current_disp_s2;
        	y_displacements_s1[i]=y_current_disp_s1;
        	y_displacements_s2[i]=y_current_disp_s2;
        	z_displacements_s1[i]=z_current_disp_s1;
        	z_displacements_s2[i]=z_current_disp_s2;
        }  

        // transmitting acceleration-data to the server
        if (property == 1) {
        	for (int i = 0; i < lengthOfDataset; i++) {
        		out.writeDouble(x_accelerations_s1[i]);	
        		out.writeDouble(x_accelerations_s2[i]);
        		out.writeDouble(y_accelerations_s1[i]);	
        		out.writeDouble(y_accelerations_s2[i]);
        		out.writeDouble(z_accelerations_s1[i]);	
        		out.writeDouble(z_accelerations_s2[i]);   
        		//out.writeDouble(time_data[i]);
        		//Thread.sleep(deltaT);
    		}
        	out.writeDouble(deltaTime);
        }
        else if (property == 2) {
        	for (int i = 0; i < lengthOfDataset; i++) {
        		out.writeDouble(x_displacements_s1[i]);	
        		out.writeDouble(x_displacements_s2[i]);
        		out.writeDouble(y_displacements_s1[i]);	
        		out.writeDouble(y_displacements_s2[i]);
        		out.writeDouble(z_displacements_s1[i]);	
        		out.writeDouble(z_displacements_s2[i]);
        		//Thread.sleep(deltaT);
    		}
        }
        else if (property == 3) {        	
        	double x_min_s1 = Double.MAX_VALUE;
        	double x_max_s1 = -Double.MAX_VALUE;
        	double y_min_s1 = Double.MAX_VALUE;
        	double y_max_s1 = -Double.MAX_VALUE;
        	double z_min_s1 = Double.MAX_VALUE;
        	double z_max_s1 = -Double.MAX_VALUE;
        	
        	for(int i=0; i<lengthOfDataset; i++) { 
        		if (x_displacements_s1[i]<x_min_s1) {x_min_s1=x_displacements_s1[i];}
        		if (x_displacements_s1[i]>x_max_s1) {x_max_s1=x_displacements_s1[i];}
        		if (y_displacements_s1[i]<y_min_s1) {y_min_s1=y_displacements_s1[i];}
        		if (y_displacements_s1[i]>y_max_s1) {y_max_s1=y_displacements_s1[i];}
        		if (z_displacements_s1[i]<z_min_s1) {z_min_s1=x_displacements_s1[i];}
        		if (z_displacements_s1[i]>z_max_s1) {z_max_s1=x_displacements_s1[i];}        		
        	}
        	
        	out.writeDouble(x_min_s1);
    		out.writeDouble(x_max_s1);
    		out.writeDouble(y_min_s1);
    		out.writeDouble(y_max_s1);
    		out.writeDouble(z_min_s1);
    		out.writeDouble(z_max_s1);
    		//Thread.sleep(deltaT);
        }
        
        else if (property == 4) { 
        	
            FrequencySpectrum 	x_fSpec = new FrequencySpectrum(x_accelerations_s1, deltaT);      
            PeakPicking 		x_pp 	  = new PeakPicking(numberOfPeaks, x_fSpec);
            int [] 				x_detectedPeaks = x_pp.getPeaks();
            
            FrequencySpectrum 	x_fSpec2 = new FrequencySpectrum(x_accelerations_s2, deltaT);      
            PeakPicking 		x_pp2 	  = new PeakPicking(numberOfPeaks, x_fSpec2);
            int [] 				x_detectedPeaks2 = x_pp2.getPeaks();
            
            FrequencySpectrum 	y_fSpec = new FrequencySpectrum(y_accelerations_s1, deltaT);      
            PeakPicking 		y_pp 	  = new PeakPicking(numberOfPeaks, y_fSpec);
            int [] 				y_detectedPeaks = y_pp.getPeaks();
            
            FrequencySpectrum 	y_fSpec2 = new FrequencySpectrum(y_accelerations_s2, deltaT);      
            PeakPicking 		y_pp2 	  = new PeakPicking(numberOfPeaks, y_fSpec2);
            int [] 				y_detectedPeaks2 = y_pp2.getPeaks();
            
            FrequencySpectrum 	z_fSpec = new FrequencySpectrum(z_accelerations_s1, deltaT);      
            PeakPicking 		z_pp 	  = new PeakPicking(numberOfPeaks, z_fSpec);
            int [] 				z_detectedPeaks = z_pp.getPeaks();
            
            FrequencySpectrum 	z_fSpec2 = new FrequencySpectrum(z_accelerations_s2, deltaT);      
            PeakPicking 		z_pp2 	  = new PeakPicking(numberOfPeaks, z_fSpec2);
            int [] 				z_detectedPeaks2 = z_pp2.getPeaks();           
            
            for (int i = 0; i < x_detectedPeaks.length; i++) {
        		out.writeDouble(x_detectedPeaks[i]);
        		out.writeDouble(x_detectedPeaks2[i]);
        		out.writeDouble(y_detectedPeaks[i]);
        		out.writeDouble(y_detectedPeaks2[i]);
        		out.writeDouble(z_detectedPeaks[i]);
        		out.writeDouble(z_detectedPeaks2[i]);
        	}
        }  	
    	
    	s.close();
    	out.close();
    	in.close();
    	
    }
}