package gov.nasa.jpf.vm.va;

import gov.nasa.jpf.jvm.JVMStackFrame;
import gov.nasa.jpf.vm.*;
import gov.nasa.jpf.vm.va.IStackHandler.Type;
import cmu.conditional.*;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import java.lang.*;
import java.util.*;
import java.util.*;
/*
 * notice : this file providing the possibility of pop ctx same with previous; 
 */
public class Test2 {

    static LinkedList<FeatureExpr> ctxPool = new LinkedList<>();
    FeatureExpr lastPushCtx = FeatureExprFactory.True();
    static boolean flag = true;

    public static void activateTreeVStack() {
        ChoiceFactory.activateTreeChoice();
        StackFactory.activateVStack();
    }
    
    public static void activateTreeCStack() {
        ChoiceFactory.activateTreeChoice();
        StackFactory.activateCStack();
    }
    
    
    
    public static long testStackWith(IVStack stack, int operationsNum, FeatureExpr[] fes, String[] operations, Conditional<Integer>[] conditionalValues) {
        for(int i = 0; i < 1; i++) stack.push(FeatureExprFactory.True(), new One<>(1), false);
        long start = System.nanoTime();
       
        for (int i = 0; i < operationsNum; i++) {
            if (operations[i].equals("push")) {
                stack.push(fes[i], conditionalValues[i], false);
            } else {
                stack.pop(fes[i], 1);
                // Conditional<Integer> res = stack.pop(fes[i], 0);
                // System.out.println("peek value" + res);
            }
        }
        long end = System.nanoTime();
        long duration = (end - start);
    
        return duration;
    }
    public static long[] testBufferedStack(int stackSize, NonStaticFeature[] options, int feComplexity, double ratio,
            int conditionalSize, int operationsNum, double possibility) {
            // System.out.println("testAll " + ratio);
            IVStack vstack = new VStack(stackSize);
            IVStack vstack2 = new VStack(stackSize);
            IVStack vstack3 = new VStack(stackSize);
        /*
            IVStack bstack = new ConditionalStack(stackSize);
            IVStack bstack2 = new ConditionalStack(stackSize);
            IVStack bstack3 = new ConditionalStack(stackSize);
        */
             
             IVStack bstack = new BufferedStack(stackSize); 
             IVStack bstack2 = new  BufferedStack(stackSize); 
             IVStack bstack3 = new BufferedStack(stackSize);
             
        
            FeatureExpr fe = Main.randomFEComlexity(options, feComplexity);
            FeatureExpr[] fes = new FeatureExpr[operationsNum];
            String[] operations = new String[operationsNum];
            Conditional<Integer>[] conditionalValues = new Conditional[operationsNum];
            Conditional<Integer>[] cOutput = new Conditional[operationsNum];
            Conditional<Integer>[] vOutput = new Conditional[operationsNum];
            long[] ans = new long[2];
        
            
            
            int pushNum = Main.GenOp(operationsNum, operations);
            Main.GenValues(options, pushNum, ratio, operations, conditionalValues);
        
            
            ctxPool.clear();
            // generate nextFe with possibility
            int popNum = operationsNum - pushNum;
            //System.out.println("\npopNum " + popNum + " pushNum " + pushNum + " operations " + operationsNum);
            
            LinkedList<Integer> seqCtx = new LinkedList<>();
            for(int i = 0; i < (int)(operationsNum * possibility); i++) {
                seqCtx.add(1);
            }
            for(int i = 0; i < operationsNum - operationsNum * possibility; i++) seqCtx.add(0);
            Collections.shuffle(seqCtx);
            
            if(flag) {
                for(int i = 0; i < seqCtx.size(); i++) {
                    System.out.print(seqCtx.get(i) + " ");
                }
            }
            System.out.println("");
            for(int i = 0; i < 1; i++) ctxPool.add(FeatureExprFactory.True());
            for(int i = 0; i < operations.length; i++) {
                if(operations[i] == "push") {
                   fes[i] = Main.randomFEComlexity(options, feComplexity);
                   ctxPool.add(fes[i]);
                   //System.out.println("add " + fes[i]);
                } else {
                   if(seqCtx.peekLast() == 1) {
                       fes[i] = ctxPool.pollLast();
                       seqCtx.pollLast();
                   } else {
                       seqCtx.pollLast();
                       int tmp = (int)(Math.random() * (ctxPool.size() - 1));
                    
                       fes[i] = ctxPool.get(tmp);
                       ctxPool.remove(tmp);
                       
                   }
                   //System.out.println("remove " + fes[i]);
                   //System.out.println("ctxPool" + ctxPool.size());
                }
            }
            
            
           
            if (flag) {
                for(int i = 0; i < operations.length; i++) {
                    System.out.println(operations[i] + " " + fes[i]);
                }
                System.out.println();
                //return ans;
            }
    

                       
            
            ans[0] = testStackWith(bstack, operationsNum, fes, operations, conditionalValues);
            ans[0] = Math.min(ans[0], testStackWith(bstack3, operationsNum, fes, operations, conditionalValues));
            System.out.println("bstack " + ans[0]);
        
            bstack = null;
            bstack2 = null;
            bstack3 = null;
        
            ans[1] = testStackWith(vstack, operationsNum, fes, operations, conditionalValues);
            ans[1] = Math.min(ans[1], testStackWith(vstack3, operationsNum, fes, operations, conditionalValues));
            System.out.println("vstack " + ans[1]);
            vstack = null;
            vstack2 = null;
            vstack3 = null;
            return ans;
        
        }
    public static void main(String[] args) {
        
        int n = 20, m = 2, nums = 40;
        NonStaticFeature[] options = Main.getOptions(20);
       
         
     
        int stackSize = 200;
        int randomFEComlexity = 1;
        double ratio = 0.3;
        int conditionalSize = 1;
        int operationsNum = 70;
        double possibility = 0.85;
        ChoiceFactory.activateTreeChoice();
    
        double[][] res = new double[n + 1][m + 1];
        long[] ans = new long[2];
        double ans_r;
    
        // (double) (n - i) / n
    
        for (int i = 0; i <= 10; i++) {
            System.out.println("No." + i);
            double bsum = 0, vsum = 0;
    
            for (int j = 0; j < nums; j++) {
                options = Main.getOptions(10+i);
                System.out.print("nums " + j + " ");
                ans = testBufferedStack(200, options, randomFEComlexity, 0, conditionalSize, operationsNum, possibility);
          
                System.out.println(ans[0] + " " + ans[1]);
                bsum += ans[0] / 1000;
                vsum += ans[1] / 1000;
    
            }
            res[i][0] = bsum / nums;
            res[i][1] = vsum / nums;
            System.out.println(" ");
            System.out.println(res[i][0] + ";" + res[i][1] + "; ");
        }
        System.out.println("bsum; vsum");
        for (int i = 0; i < n; i++) {
            System.out.println(res[i][0] + ";" + res[i][1]);
        }
    }
}
