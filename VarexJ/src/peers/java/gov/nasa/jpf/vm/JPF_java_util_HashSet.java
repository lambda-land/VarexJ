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
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import gov.nasa.jpf.annotation.MJI;


public class JPF_java_util_HashSet extends NativePeer {

	// XXX peer method names are defined as: name__parameterTypes__returntype
	// I:int, V:void, Z: boolean ...
	// you can replace the return value and the parameters by the corresponding Conditional<Type>
	
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
		set.remove(argRef, ctx);
		mySet.put(objref, set);
		return true;// always true
	}
	
	@MJI
	public Conditional<Integer> mycontains__Ljava_lang_Object_2__I(MJIEnv env, int objref, final int argRef, FeatureExpr ctx) {
		VSet set = mySet.get(objref);
		return set.contains(argRef, ctx);
	}

	@MJI
	public Conditional<Integer> size____I(MJIEnv env, int objref, FeatureExpr ctx) {
		VSet set = mySet.get(objref);
		//System.out.println("from jpf "+ set.toString() + " " + set.setSize());
		//set.printf();
		//System.out.println("*********************" + set.toString());
		return set.size(ctx);
		
	}
	
	@MJI
	public Conditional<Integer> get__I__Ljava_lang_Object_2(final MJIEnv env, int objref, final int index, FeatureExpr ctx) {
		VSet set = mySet.get(objref);
		return set.get(index,ctx);
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
	//static Conditional<Integer>[][] dp;
	private Conditional<Integer>[] res;
	private Conditional<Integer>[] prev;
	private Conditional<Integer>[] cur;
	
	public VSet(){
		this.set = new HashMap<Integer, FeatureExpr>();
	}
	
	public void add(Integer key, FeatureExpr ctx){
		//System.out.println("add key is " + key + " with ctx " + ctx);
		FeatureExpr tmp  = this.set.get(key);
		if(tmp != null){
			this.set.put(key,  tmp.or(ctx));
		}else{
			this.set.put(key,  ctx);
		}
		res = null;
	}
	
	public void remove(Integer key, FeatureExpr ctx){
		//System.out.println("remove " + key + " " + ctx);
		for (Map.Entry<Integer, FeatureExpr> entry : this.set.entrySet()) {
			//System.out.println("entry " + entry.getKey() + " " + entry.getValue());
			Integer k = entry.getKey();
			if(k.compareTo(key) == 0) {
				FeatureExpr v = entry.getValue().and(ctx.not());
				if(v.isContradiction()) {
					//System.out.println("set.remove " + k);
					this.set.remove(k);
				} else {
					entry.setValue(v);
				}
				break;
			}
		}
		res = null;
	}
	public void clear(){
		
	}
	
	public Conditional<Integer> size(FeatureExpr ctx){
		//System.out.println("passing ctx is " + ctx);
		Conditional<Integer> t = new One<>(0);
		//Iterator it = this.set.entrySet().iterator();
		for (Map.Entry<Integer, FeatureExpr> entry : this.set.entrySet()) {
		   // Integer key = entry.getKey();
		    FeatureExpr value = entry.getValue();
			t = t.mapf(value.and(ctx), new BiFunction<FeatureExpr, Integer, Conditional<Integer>>() {
				@Override
				public Conditional<Integer> apply(FeatureExpr ctx, Integer x) {
					// @SuppressWarnings("unchecked")
					return ChoiceFactory.create(ctx, new One<>(x + 1), new One<>(x));
				}
			}).simplify(ctx);
		}
		//System.out.println("size from size function "+ t);
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
		res = (Conditional<Integer>[]) new Conditional[n];
		prev = (Conditional<Integer>[]) new Conditional[n];
		cur = (Conditional<Integer>[]) new Conditional[n];
		
        for(int i = 0; i < n; i++){
        	res[i] = new One<>(0);
        	prev[i] = new One<>(0);
        	cur[i] = new One<>(0);
        }
		//initial step
		//prev[n-1] = ChoiceFactory.create(myList[n-1].value, new One<>(myList[n-1].key), new One<>(0));
		
        if(myList[n-1].value.isTautology()) {
            prev[n-1] = new One<>(myList[n-1].key);
        } else if(myList[n-1].value.isTautology()) {
            prev[n-1] = new One<>(0);
        } else {
            prev[n-1] = ChoiceFactory.create(myList[n-1].value, new One<>(myList[n-1].key), new One<>(0));
        }
		
		for(int i = n-2; i >= 0; i--){
			//prev[i] = ChoiceFactory.create(myList[i].value, new One<>(myList[i].key), prev[i+1]);
			
            if(myList[i].value.isTautology()) {
                prev[i] = new One<>(myList[i].key);
            } else if(myList[i].value.isTautology()) {
                prev[i] = prev[i+1];
            } else {
                prev[i] = ChoiceFactory.create(myList[i].value, new One<>(myList[i].key), prev[i+1]);
            }
		}
		res[0] = prev[0];
		//create the dp table 
		for(int i = 1; i < n; i++){
			for(int j = n-i-1; j >= 0; j--){
			    if(myList[i].value.isTautology()) {
			        cur[j] = prev[j+1];
			    } else if(myList[i].value.isTautology()) {
			        cur[j] = cur[j+1];
			    } else {
			        cur[j] = ChoiceFactory.create(myList[j].value, prev[j+1], cur[j+1]);
				}
			    //dp[i][j] = ChoiceFactory.create(myList[j].value, dp[i-1][j+1], dp[i][j+1]);
			}
			res[i] = cur[0];
			//switch array
			Conditional<Integer>[] tmp;
			tmp = cur;
			cur = prev;
			prev =tmp;
		}
	
	}


	public void printf(){
		if(res == null){
			createTable();
		}
		for(int i = 0; i < set.size(); i++){
			System.out.print(res[i] + "\n");
		}
		//System.out.print(res[0] + "\n");
	}

	
	public Conditional<Integer> get(int index, FeatureExpr ctx){
		if(res == null) {
			System.out.println("this size is " + set.size());
			createTable();
		}
		return res[index].simplify(ctx);
	}
	
	public Conditional<Integer> contains(int argRef, FeatureExpr ctx){
		//System.out.println("before contains size is " + set.size());
		FeatureExpr tmp = this.set.get(argRef);
		//System.out.println( "*******contains****" + ctx + " " + fe + " " + ChoiceFactory.create(ctx.and(fe), new One<>(1), new One<>(0)).simplify(fe));
		//System.out.println("ref = " + argRef + ", ctx = " + ctx + ", tmp = " + tmp);
		//System.out.println("after contains size is " + set.size());
		if(tmp == null) return new One<>(0);
		return ChoiceFactory.create(tmp, new One<>(1), new One<>(0)).simplify(ctx);
	}


	public int setSize(){
		return set.size();
	}
	@Override
	public String toString(){
		return this.set.entrySet().toString();
	}
	
}