package gov.nasa.jpf.vm.va;

public class dataCollect {
	Integer size;
	Integer max;
	Integer min;
	Integer ave; 
	
	public dataCollect(Integer size, Integer min, Integer max, Integer ave ){
		this.size = size;
		this.max = max;
		this.min = min;
		this.ave = ave;
	}
	
	public String toString(){
		return "The size is " + this.size + "\nMinimun elements is "+ this.min + "\nMaximum elements is " +this.max + "\nAverage is " + this.ave + "\n" + Stack.redOperations +" Redundant operations";
	}
}
