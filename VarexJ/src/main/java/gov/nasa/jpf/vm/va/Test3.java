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
 * notice : this file providing the possibility of True(ctx); 
 */
public class Test3 {

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
    
    public static FeatureExpr randomFEComlexity(NonStaticFeature[] options, int size, double truePossibility) {
        if (size == 0 || Math.random() < truePossibility)
            return FeatureExprFactory.True();
        if (options.length == size)
            return Main.randomFEGen(options);
        else {
            int sz = (int) (Math.random() * (size + 1));
            // System.out.println(sz);
            FeatureExpr f = options[(int) (Math.random() * options.length)].a;
            for (int i = 0; i < sz - 1; i++) {
                if (Math.random() < 0.5) {
                    f = f.and(options[(int) (Math.random() * options.length)].a);
                } else {
                    f = f.or(options[(int) (Math.random() * options.length)].a);
                }
            }
            if (Math.random() < 0.8) {
                return f;
            } else {
                return f.not();
            }
        }
    }
    
    
    public static long testStackWith(IVStack stack, int operationsNum, FeatureExpr[] fes, String[] operations, Conditional<Integer>[] conditionalValues) {
        for(int i = 0; i < operationsNum/2; i++) stack.push(FeatureExprFactory.True(), new One<>(1), false);
        long start = System.nanoTime();
       
        for (int i = 0; i < operationsNum; i++) {
            if (operations[i].equals("push")) {
                stack.push(fes[i], conditionalValues[i], false);
            } else {
                stack.pop(fes[i], 1);
            }
        }
        long end = System.nanoTime();
        long duration = (end - start);
    
        return duration;
    }
    public static long[] testBufferedStack(int stackSize, NonStaticFeature[] options, int feComplexity, double ratio,
            int conditionalSize, int operationsNum, double possibility, double truePossibility) {
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
            long[] ans = new long[3];
        
            int pushNum = Main.GenOp(operationsNum, operations);
            //GenOp(operationsNum, operations);
            Main.GenValues(options, pushNum, ratio, operations, conditionalValues);
        
            // generate nextFe with possibility
            List<Integer> nextFe = new ArrayList<>();
            for (int i = 0; i < operationsNum * possibility; i++) {
                nextFe.add(1);
            }
            for (int i = 0; i < operationsNum - operationsNum * possibility; i++) {
                nextFe.add(0);
            }
        
            Collections.shuffle(nextFe);
            
            for (int i = 0; i < operationsNum; i++) {
                if (i == 0)
                    fes[0] = fe;
                else {
                    if (nextFe.get(i) == 1)
                        fes[i] = fes[i - 1];
                    else {
                        fes[i] = randomFEComlexity(options, feComplexity, truePossibility);
                        while (fes[i].equivalentTo(fes[i - 1])) {
                            fes[i] = randomFEComlexity(options, feComplexity, truePossibility);
                        }
                    }
                }
            }
           
            if (flag) {
                for (int i = 0; i < operationsNum; i++) System.out.print(nextFe.get(i) + " ");
                System.out.println();
              for (int i = 0; i < operationsNum; i++) {
                  System.out.println( operations[i] + " " + fes[i]);
              }
                
            }
            
            System.out.println("bpeek");
            ans[0] = testStackWith(bstack, operationsNum, fes, operations, conditionalValues);
            ans[0] = Math.min(ans[0], testStackWith(bstack3, operationsNum, fes, operations, conditionalValues));
            System.out.println("bstack " + ans[0]);
        
            bstack = null;
            bstack2 = null;
            bstack3 = null;
        
            System.out.println("vpeek");
            ans[1] = testStackWith(vstack, operationsNum, fes, operations, conditionalValues);
            ans[1] = Math.min(ans[1], testStackWith(vstack3, operationsNum, fes, operations, conditionalValues));
            System.out.println("vstack " + ans[1]);
            vstack = null;
            vstack2 = null;
            vstack3 = null;
            
     
            return ans;
    }
    public static void main(String[] args) {
        
        int n = 20, m = 2, nums = 20;
        NonStaticFeature[] options = Main.getOptions(20);
       
         
     
        int stackSize = 200;
        int randomFEComlexity = 1;
        double ratio = 0.4;
        int conditionalSize = 3;
        int operationsNum = 50;
        double possibility = 0.8;
        double truePossibility = 0.2;
        ChoiceFactory.activateTreeChoice();
    
        double[][] res = new double[n + 1][m + 1];
        long[] ans = new long[2];
        double ans_r;
    
        // (double) (n - i) / n
    
        for (int i = 0; i <= 10; i++) {
            System.out.println("No." + i);
            double bsum = 0, vsum = 0;
    
            for (int j = 0; j < nums; j++) {
                options = Main.getOptions(5+i);
                System.out.print("No." + i + " nums " + j + " ");
                ans = testBufferedStack(200, options, randomFEComlexity, ratio, conditionalSize, operationsNum, possibility, truePossibility);
          
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
