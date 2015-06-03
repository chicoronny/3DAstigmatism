package org.data;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class TextWriter {

	public void saveNew(String s, double[] z){
		PrintWriter writer;
		try {
		   writer = new PrintWriter(new FileWriter(s+".txt", true));
		   for(int i=0;i<z.length;i++){
			   writer.write(z[i]+"\n");
		   }
		   writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
		      e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
		  e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public double[] loadFile(String s) throws IOException{
		ArrayList<Double> array = new ArrayList<Double>();
		
		FileReader fileReader = new FileReader(s);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
        	if(isNumeric(line)){
        		array.add(Double.parseDouble(line));
        	}
        }
        bufferedReader.close();
        
        int size = array.size();																/// why not use Double[] and array.toArray(new Double)
        double[] a = new double[size];
        for(int i=0;i<size;i++){
        	a[i] = array.get(i);
        }
		return a;
	}
	
	public static boolean isNumeric(String str)  
	{  
	  try  
	  {  
	    @SuppressWarnings("unused")
		double d = Double.parseDouble(str);  
	  }  
	  catch(NumberFormatException nfe)  
	  {  
	    return false;  
	  }  
	  return true;  
	}

}
