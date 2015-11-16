package gov.nasa.jpf.vm.va;

import java.util.*;

public class DataCollect {
	public Integer numCopy = 0;
	public StackHandler owner;
	// change name
	Integer size = 0;
	Integer max = 0;
	Integer min = 0;
	Integer ave = 0; 
	Integer gmax = 0;

	
	public DataCollect(StackHandler sh){
		this.owner = sh;
	}
	
	public void set(Integer size, Integer min, Integer max, Integer ave) {
		this.size = size;
		this.max = max;
		this.min = min;
		this.ave = ave;
	}
	
	
	public void push(){
		calculate();
//		System.out.println("PUSH");
//		System.out.println(owner.toString());
//		System.out.println(this.toString());
	}
	
	public void pop(){
		calculate();
//		System.out.println("POP");
//		System.out.println(owner.toString());
//		System.out.println(this.toString());
	}
	
	public void dup(){
		calculate();
//		System.out.println("DUP");
//		System.out.println(owner.toString());
//		System.out.println(this.toString());
	}
	
	public void calculate(){
		List<Integer> temp = owner.getTop().toList();
		Integer len = temp.size();
		Integer sum = temp.get(0) + 1;
		max = temp.get(0) + 1;
		min = temp.get(0) + 1;
		for(int i = 1; i < len; i++){
			if(temp.get(i) + 1 > max)
				this.max = temp.get(i) + 1;
			if(temp.get(i) + 1 < min){
				this.min = temp.get(i) + 1;
			}
			
			sum +=  temp.get(i) + 1;
		}
		if(this.max > this.gmax){
			this.gmax = this.max;
			}
		this.ave = sum/len;
		this.size = len;
	}
	
	public String toString(){
		return "The size is " + this.size + "\nMinimun elements is "+ this.min + "\nMaximum elements is " +this.max + "\nAverage is " + this.ave + "\nNumbers of copys " + this.numCopy + "\ngmax is "+ this.gmax+ "\n";
	}
}
