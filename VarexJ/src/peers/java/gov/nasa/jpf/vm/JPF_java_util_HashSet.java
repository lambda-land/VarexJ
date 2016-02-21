package gov.nasa.jpf.vm;

import java.util.Vector;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import cmu.conditional.BiFunction;
import cmu.conditional.ChoiceFactory;
import cmu.conditional.Conditional;
import cmu.conditional.Function;
import cmu.conditional.One;
import de.fosd.typechef.featureexpr.FeatureExpr;
import gov.nasa.jpf.annotation.MJI;


public class JPF_java_util_HashSet extends NativePeer {

	// XXX peer method names are defined as: name__parameterTypes__returntype
	// I:int, V:void, Z: boolean ...
	// you can replace the return value and the parameters by the corresponding Conditional<Type>
	
	// TODO replace Conditional<LinkedList<Integer>> by a varaibility-aware data structure
	final Map<Integer, VSet> mySet = new HashMap<>();

	@MJI
	public void $init____V(MJIEnv env, int objref, FeatureExpr ctx) {
			mySet.put(objref, new VSet());

	}


	@MJI
	public boolean add__Ljava_lang_Object_2__Z(MJIEnv env, int objref, final int argRef, FeatureExpr ctx) {
		VSet set = mySet.get(objref);
		set.add(argRef, ctx);
		mySet.put(objref, set);
		return true;// always true
	}
	
	@MJI
	public boolean remove__Ljava_lang_Object_2__Z(MJIEnv env, int objref, final int argRef, FeatureExpr ctx) {
		VSet set = mySet.get(objref);
		set.remove(argRef);
		mySet.put(objref, set);
		return true;// always true
	}
	
	@MJI
	public Conditional<Integer> mycontains__Ljava_lang_Object_2__I(MJIEnv env, int objref, final int argRef, FeatureExpr ctx) {
		VSet set = mySet.get(objref);
		return set.contains(argRef);
	}

	@MJI
	public Conditional<Integer> size____I(MJIEnv env, int objref, FeatureExpr ctx) {
		VSet set = mySet.get(objref);
		//System.out.println("from jpf "+ set.toString());
		//set.printf();
		return set.size(ctx);
		
	}
	
	@MJI
	public Conditional<Integer> get__I__Ljava_lang_Object_2(final MJIEnv env, int objref, final int index, FeatureExpr ctx) {
		VSet set = mySet.get(objref);
		return set.get(index);
	}

}

class VSet {
	
	class Entry{
		public int key;
		public FeatureExpr value; 
		public Entry(int key, FeatureExpr value){
			this.key = key;
			this.value = value;
		}
	}
	
	private Map<Integer, FeatureExpr> set;
	static Entry [] myList;
	static Conditional<Integer>[][] dp;
	
	public VSet(){
		this.set = new HashMap<Integer, FeatureExpr>();
	}
	
	public void add(Integer key, FeatureExpr ctx){
		FeatureExpr tmp  = this.set.get(key);
		if(tmp != null){
			this.set.put(key,  tmp.or(ctx));
		}else{
			this.set.put(key,  ctx);
		}
		dp = null;
	}
	
	public void remove(Integer key){
		this.set.remove(key);
		dp = null;
	}
	public void clear(){
		
	}

	public boolean isEmpty(){
		return this.set.isEmpty();
	}
	
	public Conditional<Integer> size(FeatureExpr ctx){
		Conditional<Integer> t = new One<>(0);
		//Iterator it = this.set.entrySet().iterator();
		for (Map.Entry<Integer, FeatureExpr> entry : this.set.entrySet()) {
		   // Integer key = entry.getKey();
		    FeatureExpr value = entry.getValue();
			t = t.mapf(value, new BiFunction<FeatureExpr, Integer, Conditional<Integer>>() {
				@Override
				public Conditional<Integer> apply(FeatureExpr ctx, Integer x) {
					// @SuppressWarnings("unchecked")
					return ChoiceFactory.create(ctx, new One<>(x + 1), new One<>(x));
				}
			}).simplify();
		}
		return t;
	}

	 public void toList(){
		myList = new Entry[this.set.size()];
		int i = 0; 
		for(Map.Entry<Integer, FeatureExpr> entry : this.set.entrySet()){
			 Integer key = entry.getKey();
			 FeatureExpr value = entry.getValue();
			 myList[i] = new Entry(key,value);
			 i++;
		}
	}

	 public void createTable(){
		this.toList();
		final int n = this.set.size();
		dp =  (Conditional<Integer>[][]) new Conditional[n][n];
		
		for(int i = 0; i < dp.length; i++){
        	for(int j = 0; j < dp[i].length; j++){
        		dp[i][j] = new One<>(0);
        	}
        }
        
		//initial step
		dp[0][n-1] = ChoiceFactory.create(myList[n-1].value, new One<>(myList[n-1].key), new One<>(0));
		for(int i = n-2; i >= 0; i--){
			dp[0][i] = ChoiceFactory.create(myList[i].value, new One<>(myList[i].key), dp[0][i+1]);
		}
		
		//create the dp table 
		for(int i = 1; i < n; i++){
			for(int j = n-i-1; j >= 0; j--){
				dp[i][j] = ChoiceFactory.create(myList[j].value, dp[i-1][j+1], dp[i][j+1]);
			}
		}
	
	}

	
	public void printf(){
		for(int i = 0; i < set.size() -1; i++){
			System.out.print(dp[0][i].toString());
		}
		for(int i = 0; i < set.size() -1; i++){
			System.out.println(myList[i].key);
		}	
	}
	
	public Conditional<Integer> get(int index){
		if(dp == null) createTable();
		return dp[index][0];
	}
	
	public Conditional<Integer> contains(int argRef){
		FeatureExpr ctx = this.set.get(argRef);
		if(ctx != null) return ChoiceFactory.create(ctx, new One<>(1), new One<>(0));
		else return new One<>(0);
	}

	@Override
	public String toString(){
		return this.set.entrySet().toString();
	}
	
}