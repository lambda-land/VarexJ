package gov.nasa.jpf.vm.va;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cmu.conditional.Conditional;
import cmu.conditional.One;
import de.fosd.typechef.featureexpr.FeatureExpr;
import de.fosd.typechef.featureexpr.FeatureExprFactory;
import gov.nasa.jpf.vm.va.IStackHandler.Type;
import gov.nasa.jpf.vm.va.StackHandlerFactory.SHFactory;



public class TestAll {
    public static boolean verbose = false;
    public static long testStackWith(IStackHandler stack, int operationsNum, FeatureExpr[] fes, String[] operations, Conditional<Integer>[] conditionalValues) {
        for (int i = 0; i < operationsNum / 2; i++) stack.push(FeatureExprFactory.True(), One.ONE, false);
        long start = System.nanoTime();
        for (int i = 0; i < operationsNum; i++) {
            if (operations[i].equals("push")) {
                stack.push(fes[i], conditionalValues[i], false);
//                if(i % 10 == 0) {
//                    stack.peek(fes[(int)(Math.random()*fes.length)], (int)(Math.random()*2));
//                    stack.isRef(fes[i], 0);
//                }
            } else {
                Conditional<Integer> res = stack.pop(fes[i], Type.INT );
            }
            
        }
        stack.pop(FeatureExprFactory.True());
        long end = System.nanoTime();
        long duration = (end - start);
        return duration;
    }

    public static long[] testBufferedStack(int stackSize, NonStaticFeature[] options, int feComplexity, double ratio,
            int conditionalSize, int operationsNum, double possibility) {
          
        
            FeatureExpr fe = Main.randomFEComlexity(options, feComplexity);
            FeatureExpr[] fes = new FeatureExpr[operationsNum];
            String[] operations = new String[operationsNum];
            Conditional<Integer>[] conditionalValues = new Conditional[operationsNum];
          
        
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
            if(options.length == 0) {
            for(int i = 0; i < fes.length; i++)
                fes[i] = FeatureExprFactory.True();
            }else {
            for (int i = 0; i < operationsNum; i++) {
                if (i == 0)
                    fes[0] = fe;
                else {
                    if (nextFe.get(i) == 1)
                        fes[i] = fes[i - 1];
                    else {
                        fes[i] = Main.randomFEComlexity(options, feComplexity);
                        while (fes[i].equivalentTo(fes[i - 1])) {
                            fes[i] = Main.randomFEComlexity(options, feComplexity);
                        }
                    }
                }
            }
            }
            if (verbose) {
                for (int i = 0; i < operationsNum; i++) {
                    System.out.println( operations[i] + " " + fes[i] + " " + nextFe.get(i) + " " + conditionalValues[i]);
                }
                  
              }

            StackFactory.activateCStack();
            long[] ret = new long[8];
            for (SHFactory factory : StackHandlerFactory.SHFactory.values()) {
               if(factory == StackHandlerFactory.SHFactory.Hybid ||  factory == StackHandlerFactory.SHFactory.HybridBuffered) continue;
               StackHandlerFactory.setFactory(factory);
               IStackHandler checkStack;
               
               long minTime = Long.MAX_VALUE;
               for(int i = 0; i < 10; i++) {
                   checkStack = StackHandlerFactory.createStack(FeatureExprFactory.True(), stackSize, stackSize);
                   minTime = Math.min(testStackWith(checkStack, operationsNum, fes, operations, conditionalValues), minTime);
               }
               ret[factory.ordinal()] = minTime; 
               System.out.println("c " + minTime);
           }
            
          
           
            StackFactory.activateVStack();
            for (SHFactory factory : StackHandlerFactory.SHFactory.values()) {
               if(factory == StackHandlerFactory.SHFactory.Hybid ||  factory == StackHandlerFactory.SHFactory.HybridBuffered) continue;
               StackHandlerFactory.setFactory(factory);
               IStackHandler checkStack;
               
               long minTime = Long.MAX_VALUE;
               for(int i = 0; i < 10; i++) {
                   checkStack = StackHandlerFactory.createStack(FeatureExprFactory.True(), stackSize, stackSize);
                   minTime = Math.min(testStackWith(checkStack, operationsNum, fes, operations, conditionalValues), minTime);
               }
               ret[factory.ordinal() + 4] = minTime;
               System.out.println("v " + minTime);
           }
            
            
            conditionalValues = null;
            operations = null;
            fes = null;
          
            return ret;
        
        }

        public static void testFeature(int stackSize, int randomFEComlexity, double ratio, int conditionalSize, int operationsNum, double possibility) {
            System.setProperty("FEATUREEXPR", "BDD");
            int n = 20, m = 8, nums = 100;
            
            double[][] res = new double[n + 1][m + 1];
            long[] ans = new long[8];
       
            for (int i = 0; i <= 10; i++) {
                System.out.println("No." + i);
        
                for (int j = 0; j < nums; j++) {
                    System.out.print("No."+ i + "nums " + j + " ");
                    
                    NonStaticFeature[] options = Main.getOptions(i);
                    
                    ans = testBufferedStack(stackSize, options, randomFEComlexity, ratio, conditionalSize, operationsNum, possibility);
                    if (verbose) System.out.println(ans[0] + " " + ans[1]);
                    for(int k = 0; k < ans.length; k++) {
                        res[i][k] += ans[k]/1000;
                    }
                }
                
                for(int k = 0; k < ans.length; k++) {
                    res[i][k] = res[i][k]/nums;
                    System.out.print(res[i][k] + ";");
                }
                System.out.println();
                
                System.out.println(stackSize + " "+ randomFEComlexity+ " " + ratio + " " + conditionalSize +" " + operationsNum + " "+ possibility);
            }
            System.out.println("CDefault;CHybrid;CBuffered;CHybridBuffered;VDefault;VHybrid;VBuffered;VHybridBuffered");
            
            for (int i = 0; i < res.length; i++) {
               for(int j = 0; j < ans.length; j++) {
                   System.out.print(res[i][j]+";");
               }
               System.out.println();
            }
            for (int i = 0; i < res.length; i++) {
                for(int j = 0; j < ans.length; j++) {
                    if(res[i][j] == 0.0) continue;
                    System.out.print(res[i][j]+";");
                }
                System.out.println();
             }
        }
        
        public static void possibilityTest(int stackSize, int randomFEComlexity, double ratio, int conditionalSize, int operationsNum, double possibility) {
            System.setProperty("FEATUREEXPR", "BDD");
            int n = 20, m = 8, nums = 100;
            NonStaticFeature[] options = Main.getOptions(6);
           
            double[][] res = new double[n + 1][m + 1];
            long[] ans = new long[8];
       
            for (int i = 0; i <= n; i++) {
                System.out.println("No." + i);
        
                for (int j = 0; j < nums; j++) {
                    System.out.print("No."+ i + "nums " + j + " ");
                    
                    
                    possibility = (double) (n - i) / n;
                    //possibility = 0;
                    System.out.println("possibility " + possibility);
                    ans = testBufferedStack(stackSize, options, randomFEComlexity, ratio, conditionalSize, operationsNum, possibility);
                    if (Main.flag) System.out.println(ans[0] + " " + ans[1]);
                    for(int k = 0; k < ans.length; k++) {
                        res[i][k] += ans[k]/1000;
                    }
                }
                
                for(int k = 0; k < ans.length; k++) {
                    res[i][k] = res[i][k]/nums;
                    System.out.print(res[i][k] + ";");
                }
                System.out.println();
                
                System.out.println(stackSize + " "+ randomFEComlexity+ " " + ratio + " " + conditionalSize +" " + operationsNum + " "+ possibility);
            }
            System.out.println("CDefault;CHybrid;CBuffered;CHybridBuffered;VDefault;VHybrid;VBuffered;VHybridBuffered");
            
            for (int i = 0; i < res.length; i++) {
               for(int j = 0; j < ans.length; j++) {
                   System.out.print(res[i][j]+";");
               }
               System.out.println();
            }
            for (int i = 0; i < res.length; i++) {
                for(int j = 0; j < ans.length; j++) {
                    if(res[i][j] == 0.0) continue;
                    System.out.print(res[i][j]+";");
                }
                System.out.println();
             }
        }
       public static void testBuffer() {
           StackHandlerFactory.activateBufferedStackHandler();
           IStackHandler bstack = StackHandlerFactory.createStack(FeatureExprFactory.True(),10, 10);
           Long a = new Long(1000001);
           Long b = new Long(22222222);
           
           bstack.push(FeatureExprFactory.True(), a, false);
           bstack.push(FeatureExprFactory.True(), b, true);
           System.out.println(bstack.isRef(FeatureExprFactory.True(), 1)); //false
           
           StackHandlerFactory.activateDefaultStackHandler();
           IStackHandler cstack = StackHandlerFactory.createStack(FeatureExprFactory.True(),10, 10);
           cstack.push(FeatureExprFactory.True(), a, false);
           cstack.push(FeatureExprFactory.True(), b, true);
           System.out.println(cstack.isRef(FeatureExprFactory.True(), 1));//true
           
       }
       public static void testBufferPeek() {
           StackHandlerFactory.activateBufferedStackHandler();
           IStackHandler bstack = StackHandlerFactory.createStack(FeatureExprFactory.True(),10, 10);
           
           StackHandlerFactory.activateDefaultStackHandler();
           IStackHandler cstack = StackHandlerFactory.createStack(FeatureExprFactory.True(),10, 10);
           
           FeatureExpr t = FeatureExprFactory.True();
           
           Long a = new Long(0);
           Long b = new Long(-1);
           
           Integer c = new Integer(333);
           
           //bstack.push(t, c);
           bstack.push(t, c);
           bstack.push(t, b);
           System.out.println(bstack.peek(FeatureExprFactory.True(), 1));//true

           //cstack.push(t, c);
           cstack.push(t, c);
           cstack.push(t, b);
           System.out.println(cstack.peek(FeatureExprFactory.True(), 1));//true

           
       }
       
       public static void main(String[] args) {
           //testFeature(500, 1, 0.1, 1, 200, 0.90);
           //possibilityTest(500, 1, 0.1, 1, 500, 0);
           //ratioTest(200, 1, 0, 1, 100, 0.9);
           testBuffer();
        }
}
