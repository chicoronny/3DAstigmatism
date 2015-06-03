package org.filters;

public class MedianFilter {

	
		
	//////////////////////////////////////////////
	//// Fast median
	////  from Battiato and al, An efficient algorithm for the approximate median selection problem
	private static void Swap(int size, float[] A, int i, int j){
		if(i >= size || j >= size){
			return;
		}    
		if(i == j){
			return;
		}
	
		float buff = A[i];
		A[i] = A[j];
		A[j] = buff;
	}
	
	
	private static void adjustTriplet(int size,float[] A, int i, int step){
		int j = i+step;
		int k = i+2;
	
		if(A[i]<A[j]){
			if(A[k]<A[i]){
				Swap(size,A,i,j);
			} else if (A[k]<A[j]){
				Swap(size,A,j,k);
			}
		} else {
			if(A[i]<A[k]){
				Swap(size,A,i,j);
			} else if(A[k]>A[j]){
				Swap(size,A,j,k);
			}
		}
	}
	
	private static void selectionSort(int dim, float[] A, int left, int size, int step){
		int min;
		for(int i=left;i<left+(size-1)*step;i=i+step){
			min = i;
			for(int j=i+step;j<left+size*step;j=j+step){
				if(A[j]<A[min]){
					min = j;
				}
			}
			Swap(dim,A,i,min);
		}
	}
	
	public static int fastmedian(float A[], int dim){
	
		///////////////////////////////////////////
		/// Size of the array
		int size = dim;
	
		///////////////////////////////////////////
		///  Median calculation
		int LeftToRight = 0;
		
		// Parameters
		int threshold = 2;                                    					  // pass as argument !!!!!!!!!
		
		// Definitions
		int left = 0;
		int rem = 0;            
		int step = 1;
		int i,j;
		int median;
		
		/// Run
		while(size > threshold){
			LeftToRight = 1 - LeftToRight;
			rem = size%3;
			if(LeftToRight == 1){
				i = left;
			} else {
				i = left+(3+rem)*step;
			}
			for(j = 0; j<(size/3-1);j++){
				adjustTriplet(dim,A,i,step);
				i = i + 3*step;
			}
			if(LeftToRight == 1){
				left = left + step;
			} else {
				i = left;
				left = left + (1+rem)*step;
			}
			selectionSort(dim,A,i,3+rem,step);
			if(rem == 2){
				if (LeftToRight == 1){
					Swap(dim,A,i+step,i+2*step);
				} else {
					Swap(dim,A,i+2*step,i+3*step);
				}
			}
			step = 3*step;
			size = size/3;
		}
		selectionSort(dim,A,left,size,step);
		median = (int) A[left + (step*(size-1)/2)];	
		
		// return median value
		return median;
	}
	
	/////////////////////////////////////////////////
	//// Exact median
	public double Exactmedian(int n, double[] x) {
		double temp;
		int i, j;
		// the following two loops sort the array x in ascending order
		for(i=0; i<n-1; i++) {
			for(j=i+1; j<n; j++) {
				if(x[j] < x[i]) {
					// swap elements
					temp = x[i];
					x[i] = x[j];
					x[j] = temp;
				}
			}
		}
		
		if(n%2==0) {
			// if there is an even number of elements, return mean of the two elements in the middle
			//return (x[(n-1)/2]+x[(n+1)/2])/2;
			return x[n/2];
		} else {
			// else return the element in the middle
			return x[n/2];
		}
	}

}
