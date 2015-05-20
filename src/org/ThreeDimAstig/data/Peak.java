package org.data;

public class Peak {
	private int x_,y_,value_, n_;
	private double sx_,sy_;
	
	public Peak(int x, int y, int value){
		x_ = x;
		y_ = y;
		sx_ = 0;
		sy_ = 0;
		n_ = 1;
		value_ = value;
	}
	
	public Peak(int n, int x, int y, int value){
		x_ = x;
		y_ = y;
		sx_ = 0;
		sy_ = 0;
		n_ = n;
		value_ = value;
	}
	
	public Peak(int x, int y, double sx, double sy, int slice, int value){
		x_ = x;
		y_ = y;
		sx_ = sx;
		sy_ = sy;
		n_ = slice;
		value_ = value;
	}

	public void set(int x, int y, int value){
		x_ = x;
		y_ = y;
		value_ = value;
	}

	public void set(int x, int y, double sx, double sy){
		x_ = x;
		y_ = y;
		sx_ = sx;
		sy_ = sy;
	}

	public void set(int x, int y, double sx, double sy, int slice, int value){
		x_ = x;
		y_ = y;
		sx_ = sx;
		sy_ = sy;
		n_ = slice;
		value_ = value;
	}

	public void setX(int x){
		x_ = x;
	}
	
	public void setY(int y){
		y_ = y;
	}

	public void setSX(double sx){
		sx_ = sx;
	}
	
	public void setSY(double sy){
		sy_ = sy;
	}

	public void setSlice(int slice){
		n_ = slice;
	}

	public void setValue(int value){
		value_ = value;
	}


	public int getX(){
		return x_;
	}
	
	public int getY(){
		return y_;
	}

	
	public int getSlice(){
		return n_;
	}

	public double getSX(){
		return sx_;
	}
	
	public double getSY(){
		return sy_;
	}
	
	public int getValue(){
		return value_;
	}

	public void print(){
		System.out.println("["+x_+","+y_+","+value_+"]");
	}
	
	public String toString(){
		String s = "["+x_+","+y_+","+value_+"]";
		return s;
	}
	
}
