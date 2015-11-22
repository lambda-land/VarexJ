package gov.nasa.jpf.vm.va;

import java.util.*;
import de.fosd.typechef.featureexpr.FeatureExpr;

public class DataCollect {
	public Integer numCopy = 0;
	Integer size = 0;
	Integer max = 0;
	Integer min = 0;
	Integer ave = 0; 
	Integer gmax = 0;

	
	public DataCollect(){
		
	}
	
	public void set(Integer size, Integer min, Integer max, Integer ave) {
		this.size = size;
		this.max = max;
		this.min = min;
		this.ave = ave;
	}
	
	/***
	 *  StackHandler
	 *  push pop isref INC
	 *  set dup swap
	 */
	public void push(StackHandler sh, final FeatureExpr ctx, final Object value, final boolean isRef){
		calculate(sh);
		Log.getInstance().info("PUSH\n"+ sh.toString() + "\n" + this.toString());
	}
	
	public void pop(StackHandler sh, FeatureExpr ctx, final int n){
		calculate(sh);
		Log.getInstance().info("POP\n"+ sh.toString() + "\n" + this.toString());
	} 
	
	public void set(StackHandler sh, final FeatureExpr ctx, final int offset, final int value, final boolean isRef){}
	
	public void dup(StackHandler sh, final FeatureExpr ctx) {
		calculate(sh);
		Log.getInstance().info("DUP\n"+ sh.toString() + "\n" + this.toString());
	}
			
	public void isRef(StackHandler sh, final FeatureExpr ctx, final int offset) {}

	public void set(final FeatureExpr ctx, final int offset, final int value, final boolean isRef) {}

	public void getTop(StackHandler sh) {}
		
	public void setTop(StackHandler sh, final FeatureExpr ctx, final int i) {}
		
	public void clear(StackHandler sh, final FeatureExpr ctx) {}

	public void getSlots(StackHandler sh, FeatureExpr ctx) {}

	/***
	 * Stack operations
	 * 
	 * 
	 */
	public void dup_x1(StackHandler sh, final FeatureExpr ctx) {}
	
	public void dup2_x2(StackHandler sh, final FeatureExpr ctx) {}
		
	public void dup2_x1(StackHandler sh, final FeatureExpr ctx) {}

	public void dup2(StackHandler sh, final FeatureExpr ctx) {}
	
	public void dup_x2(StackHandler sh, final FeatureExpr ctx){}

	public void swap(StackHandler sh, final FeatureExpr ctx) {}
		
	public void getLength(StackHandler sh) {}
		
	public void getStack(StackHandler sh) {}
	
	public void getAllReferences(StackHandler sh) {}

	public void getLocalWidth(StackHandler sh) {}
	
	public void getMaxLocal(StackHandler sh) {}
	
	public void IINC(StackHandler sh, FeatureExpr ctx, int index, final int increment){}

	
	/***
	 *  Stack
	 *  stackcopy entrycopy 
	 *
	 */

	public void stackcopy(Stack s) {
		this.numCopy += s.top+1;
	}
	
	public void clear(Stack s){}
	
	public void setRef(Stack s, int index, boolean ref){}
	
	public void hasAnyRef(Stack s){}
	
	public void getSlots(Stack s){}
	
	public void get(Stack s, int index){}
	
	public void peek(Stack s, int offset){}
	
	public void push(Stack s, Integer value, boolean isRef){}
	
	public void isRef(Stack s, int offset){}
	
	public void isRefIndex(Stack s, int index){}
	
	public void set(Stack s, int offset, int value, boolean isRef){}
	
	public void setIndex(Stack s, int index, Integer value, boolean isRef){}
	
	public void dup2_x1(Stack s){}
	
	public void dup2(Stack s){}
	
	public void dup(Stack s){}
	
	public void dup_x2(Stack s){}
	
	public void swap(Stack s){}
	
	public void getReferences(Stack s){}
	
	public void  entrycopy(Stack s){}
	
	public void calculate(StackHandler sh){
		List<Integer> temp = sh.getTop().toList();
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
