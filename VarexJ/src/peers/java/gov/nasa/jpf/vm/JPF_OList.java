package gov.nasa.jpf.vm;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import cmu.conditional.BiFunction;
import cmu.conditional.ChoiceFactory;
import cmu.conditional.Conditional;
import cmu.conditional.Function;
import cmu.conditional.One;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import gov.nasa.jpf.annotation.MJI;

public class JPF_OList {
	private class Node{
		public Conditional<Integer> data;
		Node next;
		
		public Node(Conditional<Integer> data){
			this.data = data;
		    this.next = null;
		}
	}
	//private Conditional<Integer> size;
	private Node head;
		  
	public JPF_OList(){
		this.head = null;
		//this.size = One<Integer>(0);
	}
		  
	public Node getHead(){
		return head;
	}
	 
    public void add(int argRef, FeatureExpr ctx){
    	if (head == null) {
    		head = new Node(ChoiceFactory.create(ctx, new One<>(argRef), null));
    	}else{
    		Node tmp = head;
    		while(tmp.next != null) tmp = tmp.next;
    		tmp.next = new Node(ChoiceFactory.create(ctx, new One<>(argRef), null));
    	}	
    }
    
    public Conditional<Integer> get(int index){
    	Node tmp = head;
    	while(index != 0 && tmp.next != null){
    		tmp = tmp.next; index--;
    	}
    	return tmp.data;
    }

    public static void main(String[] args) {
    	FeatureExpr ta = FeatureExprFactory.createDefinedExternal("A");
    	JPF_OList list = new JPF_OList();
    	list.add(3, ta);
    	list.add(3, ta.not());
    	for(int i = 0; i < 2; i++){
    		System.out.println(list.get(i));
    	}
    }

}

	// XXX peer method names are defined as: name__parameterTypes__returntype
	// I:int, V:void, Z: boolean ...
	// you can replace the return value and the parameters by the corresponding Conditional<Type>
	
	// TODO replace Conditional<LinkedList<Integer>> by a varaibility-aware data structure
   
/*
class MyLinkedList {
    final Map<Integer, JPF_OList> myLists = new HashMap<>();

	@MJI
	public void $init____V(MJIEnv env, int objref, FeatureExpr ctx) {
		myLists.put(objref, new JPF_OList());
	}

	@MJI
	public Conditional<Integer> size____I(MJIEnv env, int objref, FeatureExpr ctx) {
		Conditional<LinkedList<Integer>> list = myLists.get(objref).simplify(ctx);
		return list.map(new Function<LinkedList<Integer>, Integer>() {

			@Override
			public Integer apply(LinkedList<Integer> x) {
				return x.size();
			}

		}).simplify(ctx);
	}

	@MJI
	public boolean add__Ljava_lang_Object_2__Z(MJIEnv env, int objref, final int argRef, FeatureExpr ctx) {
		Conditional<LinkedList<Integer>> list = myLists.get(objref);
		list = list.mapf(ctx, new BiFunction<FeatureExpr, LinkedList<Integer>, Conditional<LinkedList<Integer>>>() {

			@Override
			public Conditional<LinkedList<Integer>> apply(FeatureExpr ctx, LinkedList<Integer> list) {
				@SuppressWarnings("unchecked")
				LinkedList<Integer> clone = (LinkedList<Integer>) list.clone();
				clone.add(argRef);
				return ChoiceFactory.create(ctx, new One<>(clone), new One<>(list));

			}

		}).simplify();
		myLists.put(objref, list);
		return true;// always true
	}

	@MJI
	public Conditional<Integer> get__I__Ljava_lang_Object_2(final MJIEnv env, int objref, final int index, FeatureExpr ctx) {
		Conditional<LinkedList<Integer>> list = myLists.get(objref).simplify(ctx);
		return list.mapf(ctx, new BiFunction<FeatureExpr, LinkedList<Integer>, Conditional<Integer>>() {

			@Override
			public Conditional<Integer> apply(FeatureExpr ctx, LinkedList<Integer> list) {
				try {
					return One.valueOf(list.get(index));
				} catch (Exception e) {
					env.ti.createAndThrowException(ctx, e.getClass().getName());
				}
				return One.valueOf(-1);
			}

		});
	}
}

*/
