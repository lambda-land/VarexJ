package gov.nasa.jpf.vm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
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
		VSet setList = mySet.get(objref);
		if(setList == null){
			mySet.put(objref, new VSet());
		} else {
			for (Integer element : setList.set.keySet()) {
				setList.set.put(element, setList.set.get(element).and(ctx.not()));
			}
			mySet.put(objref, setList);
		}
	}


	@MJI
	public boolean add__Ljava_lang_Object_2__Z(MJIEnv env, int objref, final int argRef, FeatureExpr ctx) {
		VSet set = mySet.get(objref);
		set.add(argRef, ctx);
		mySet.put(objref, set);
		return true;// always true
	}

	@MJI
	public Conditional<Integer> size____I(MJIEnv env, int objref, FeatureExpr ctx) {
		VSet set = mySet.get(objref);
		System.out.println("from jpf "+ set.toString());
		return new One<>(set.size());
		
	}
	@MJI
	public void printf____V(final MJIEnv env, int objref, FeatureExpr ctx) {
		VSet set = mySet.get(objref);
	
	}
	/*
	@MJI
	public Conditional<String> toString____Ljava_lang_String_2(MJIEnv env, int objref, FeatureExpr ctx){
		VSet set = mySet.get(objref);
		return new One<String>(set.t);
	}

	*/
}

class VSet {
	Map<Integer, FeatureExpr> set;
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
	}
	public void clear(){
		
	}
	
	public boolean contains(Integer key){
		return !(this.set.get(key) == null);
	}
	public boolean isEmpty(){
		return this.set.isEmpty();
	}
	
	public Iterator<Integer> iterator(){
		return set.keySet().iterator();
	}
	
	public int size(){
		return set.size();
	}
	
	@Override
	public String toString(){
		return this.set.entrySet().toString();
	}
	
}