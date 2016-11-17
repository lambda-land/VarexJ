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

class NonStaticFeature {
FeatureExpr a = FeatureExprFactory.createDefinedExternal("f" + Main.FeatureID++);
}

public class Main {
  public static int FeatureID = 0;
  public static boolean flag = false;
  
  public static NonStaticFeature[] getOptions(int nrOptions) {
      NonStaticFeature[] options = new NonStaticFeature[nrOptions];
      for (int i = 0; i < options.length; i++) {
          options[i] = new NonStaticFeature();
      }
      return options;
  }
  
  public static FeatureExpr randomFEGen(NonStaticFeature[] options) {
      int i = (int) (Math.random() * options.length);
      FeatureExpr f = options[i].a;
      if (Math.random() < 0.2) {
          f = f.not();
      }
      if (Math.random() < 0.5) {
          return f;
      } else {
          if (Math.random() < 0.5)
              return f.and(randomFEGen(options));
          else
              return f.or(randomFEGen(options));
      }
  }
  
  public static FeatureExpr randomFEComlexity(NonStaticFeature[] options, int size) {
      if(options.length == 0) return FeatureExprFactory.True();
      if(options.length == 1) return Math.random() < 0.5 ? options[0].a : options[0].a.not();
      if (size == 0) return FeatureExprFactory.True();
      if (options.length == size)
          return randomFEGen(options);
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
  
  public static Conditional<Integer> randomCIGen(NonStaticFeature[] options, int sz) {
      if (sz == 0)
          return One.valueOf((int) (Math.random() * 100000));
      else {
          return ChoiceFactory.create(randomFEComlexity(options, 1), randomCIGen(options, sz - 1), randomCIGen(options, sz - 1));
      }
  }
  
  public static Conditional<Integer> ratioGen(NonStaticFeature[] options, double ratio, int sz) {
      if (Math.random() < ratio) {
          return randomCIGen(options, sz);
      } else {
          return randomCIGen(options, 0);
      }
  }
  
  public static void activateTreeVStack() {
      ChoiceFactory.activateTreeChoice();
      StackFactory.activateVStack();
  }
  
  public static void activateTreeCStack() {
      ChoiceFactory.activateTreeChoice();
      StackFactory.activateCStack();
  }
  
  public static long testStackWith(IVStack stack, int operationsNum, FeatureExpr[] fes, String[] operations, Conditional<Integer>[] conditionalValues) {
      for (int i = 0; i < operationsNum / 2; i++) stack.push(FeatureExprFactory.True(), new One<>(1), false);
      long start = System.nanoTime();
 
      for (int i = 0; i < operationsNum; i++) {
          if (operations[i].equals("push")) {
              stack.push(fes[i], conditionalValues[i], false);
          } else {
              Conditional<Integer> res = stack.pop(fes[i], Type.INT );
          }
       
      }
      long end = System.nanoTime();
      long duration = (end - start);
      return duration;
      
  }
  
  /*
   * push f1, push f2...pop f2, pop f1
   */
  public static long test1(IVStack stack, NonStaticFeature[] options, Conditional<Integer>[] conditionalValues) {
      long start = System.nanoTime();
      
      for (int i = 0; i < options.length; i++) {
         stack.push(options[i].a, conditionalValues[i], false);
      }
      for (int i = options.length - 1; i >= 0; i--) {
          stack.pop(options[i].a, 1);
       }

      long end = System.nanoTime();
      long duration = (end - start);
  
      return duration;
  }

  /*
   * Complicated conditional values
   */
  
  public static long test2(IVStack stack, NonStaticFeature[] options, int sz) {
      Conditional<Integer> tmp = randomCIGen(options, sz);
      long start = System.nanoTime();
      for(int i = 0; i < 50; i++) {
          stack.push(options[0].a, tmp, false);
          stack.pop(options[0].a, 1);
       }

      long end = System.nanoTime();
      long duration = (end - start);
  
      return duration;
  }
  
  public static int GenOp(int operationsNum, String[] operations) {
      // generate operations and keep # of push greater than pop
      int count = 0, pushNum = 0;
      for (int i = 0; i < operationsNum; i++) {
          if (i == 0) {
              operations[0] = "push";
              pushNum++;
              continue;
          }
  
          // generate operations
          if (Math.random() < 0.5 && count > 0) {
              operations[i] = "pop";
              count--;
          } else {
              operations[i] = "push";
              pushNum++;
              count++;
          }
      }
      return pushNum;
  }

  public static void GenValues(NonStaticFeature[] options, int pushNum, double ratio, String[] operations,
      Conditional<Integer>[] conditionalValues) {
      LinkedList<Conditional<Integer>> values = new LinkedList<>();
      for (int i = 0; i < pushNum * ratio; i++) {
          values.add(randomCIGen(options, 1));
      }
      for (int i = 0; i < pushNum - pushNum * ratio; i++) {
          values.add(randomCIGen(options, 0));
      }
      if (flag) {
          System.out.println("pushNum" + pushNum);
          for (int i = 0; i < values.size(); i++) {
             // System.out.print(values.get(i) + " ");
          }
  
      }
      Collections.shuffle(values);
  
      for (int i = 0; i < operations.length; i++) {
          if (operations[i] == "pop")
              conditionalValues[i] = new One(0);
          else {
              conditionalValues[i] = values.poll();
          }
      }
  }
  
  public static long[] testBufferedStack(int stackSize, NonStaticFeature[] options, int feComplexity, double ratio,
      int conditionalSize, int operationsNum, double possibility) {
      // System.out.println("testAll " + ratio);
      IVStack vstack = new VStack(stackSize);
      IVStack vstack2 = new VStack(stackSize);
      IVStack vstack3 = new VStack(stackSize);
  
//      IVStack bstack = new ConditionalStack(stackSize);
//      IVStack bstack2 = new ConditionalStack(stackSize);
//      IVStack bstack3 = new ConditionalStack(stackSize);
       
       IVStack bstack = new BufferedStack(stackSize); 
       IVStack bstack2 = new  BufferedStack(stackSize); 
       IVStack bstack3 = new BufferedStack(stackSize);

  
      FeatureExpr fe = randomFEComlexity(options, feComplexity);
      FeatureExpr[] fes = new FeatureExpr[operationsNum];
      String[] operations = new String[operationsNum];
      Conditional<Integer>[] conditionalValues = new Conditional[operationsNum];
     
      long[] ans = new long[3];
  
      int pushNum = GenOp(operationsNum, operations);
      //GenOp(operationsNum, operations);
      GenValues(options, pushNum, ratio, operations, conditionalValues);
  
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
                  fes[i] = randomFEComlexity(options, feComplexity);
                  while (fes[i].equivalentTo(fes[i - 1])) {
                      fes[i] = randomFEComlexity(options, feComplexity);
                  }
              }
          }
      }
     
 
 
      /*
      //test1
      System.out.println("bpeek");
      ans[0] = test1(bstack, options, conditionalValues);
      ans[0] = Math.min(ans[0], test1(bstack, options, conditionalValues));
      System.out.println("bstack " + ans[0]);
  
      bstack = null;
      bstack2 = null;
      bstack3 = null;
  
      System.out.println("vpeek");
      ans[1] = test1(vstack, options, conditionalValues);
      ans[1] = Math.min(ans[1], test1(vstack, options, conditionalValues));
      System.out.println("vstack " + ans[1]);
      vstack = null;
      vstack2 = null;
      vstack3 = null;
      */
      
      /*
      //test2
      System.out.println("bpeek");
      ans[0] = test2(bstack, options, conditionalSize);
      ans[0] = Math.min(ans[0], test2(bstack, options, conditionalSize));
      System.out.println("bstack " + ans[0]);
  
      bstack = null;
      bstack2 = null;
      bstack3 = null;
  
      System.out.println("vpeek");
      ans[1] = test2(vstack, options, conditionalSize);
      ans[1] = Math.min(ans[1], test2(vstack, options, conditionalSize));
      System.out.println("vstack " + ans[1]);
      vstack = null;
      vstack2 = null;
      vstack3 = null;
      */
      
      if (flag) {
        for (int i = 0; i < operationsNum; i++) {
            System.out.println( operations[i] + " " + fes[i] + " " + nextFe.get(i) + " " + conditionalValues[i]);
        }
          
      }

  
      System.out.println("Conditional<Stack>");
      ans[0] = Long.MAX_VALUE;
      ans[1] = Long.MAX_VALUE;
      for(int i = 0; i < 10; i++) {
          long minTime = testStackWith(bstack, operationsNum, fes, operations, conditionalValues);
          bstack.clear(FeatureExprFactory.True());
          ans[0] = Math.min(minTime, ans[0]);
      }
     
      System.out.println("bstack " + ans[0]);
  
      System.out.println("VStack");
      
      for(int i = 0; i < 10; i++) {
          long minTime = testStackWith(vstack, operationsNum, fes, operations, conditionalValues);
          vstack.clear(FeatureExprFactory.True());
          ans[1] = Math.min(minTime, ans[1]);
      }
      /*
      ans[1] = testStackWith(vstack, operationsNum, fes, operations, conditionalValues);
      ans[1] = Math.min(ans[1], testStackWith(vstack2, operationsNum, fes, operations, conditionalValues));
      ans[1] = Math.min(ans[1], testStackWith(vstack3, operationsNum, fes, operations, conditionalValues));
      */
      System.out.println("vstack " + ans[1]);
      vstack = null;
      vstack2 = null;
      vstack3 = null;
    
     
      bstack = null;
      bstack2 = null;
      bstack3 = null;
  
      
      conditionalValues = null;
      operations = null;
      fes = null;
    
      return ans;
  
  }
  public static void testPop() {
      FeatureExpr a = FeatureExprFactory.createDefinedExternal("a");
      FeatureExpr b = FeatureExprFactory.createDefinedExternal("b");
      Conditional<Integer> v = ChoiceFactory.create(a,  ChoiceFactory.create(b, new One<>(1), new One<>(2)), ChoiceFactory.create(b, new One<>(3), new One<>(4)));
      IVStack test = new VStack(5);
      test.push(FeatureExprFactory.True(), new One<>(5), false);
      test.push(FeatureExprFactory.True(), v, false);
      System.out.println(test.pop(b, Type.INT));
      System.out.println("done");
      System.out.println(test.pop(b, Type.INT));
      
  }
  
  public static void testFeature() {
      System.setProperty("FEATUREEXPR", "BDD");
      int n = 20, m = 2, nums = 20;
      NonStaticFeature[] options = getOptions(10);
  
      int stackSize = 200;
      int randomFEComlexity = 1;
      double ratio = 0.1;
      int conditionalSize = 1;
      int operationsNum = 200;
      double possibility = 0.9;
  
      double[][] res = new double[n + 1][m + 1];
      long[] ans = new long[2];
      double ans_r;
  
      // (double) (n - i) / n
      //System.out.println(  System.getProperty("FEATUREEXPR"));
      for (int i = 0; i <= 10; i++) {
          System.out.println("No." + i);
          double bsum = 0, vsum = 0;
  
          for (int j = 0; j < nums; j++) {
              System.out.print("No."+ i + "nums " + j + " ");
              
              options = getOptions(2 + i);
              //possibility = (double) (n - i) / n;
              
              ans = testBufferedStack(stackSize, options, randomFEComlexity, ratio, conditionalSize, operationsNum, possibility);
              if (flag) System.out.println(ans[0] + " " + ans[1]);
              bsum += ans[0] / 1000;
              vsum += ans[1] / 1000;
  
          }
          res[i][0] = bsum / nums;
          res[i][1] = vsum / nums;
          System.out.println(" ");
          System.out.println(res[i][0] + ";" + res[i][1] + "; ");
          System.out.println(stackSize + " " + options.length + " "+ randomFEComlexity+ " " + ratio + " " + conditionalSize +" " + operationsNum + " "+ possibility);
      }
      System.out.println("bsum; vsum");
      for (int i = 0; i < n; i++) {
          System.out.println(res[i][0] + ";" + res[i][1]);
      }
  }
  
  public static void possibilityTest(int stackSize, int randomFEComlexity, double ratio, int conditionalSize, int operationsNum, double possibility) {
      System.setProperty("FEATUREEXPR", "BDD");
      int n = 50, m = 2, nums = 20;
      NonStaticFeature[] options = getOptions(5);
     
  
      double[][] res = new double[n + 1][m + 1];
      long[] ans = new long[2];
      double ans_r;
      for (int i = 0; i <= 10; i++) {
          System.out.println("No." + i);
          double bsum = 0, vsum = 0;
  
          for (int j = 0; j < nums; j++) {
              
              possibility = (double) (n - i) / n;
              System.out.print("No."+ i + "nums " + j + " " + "possibility " + possibility);
              ans = testBufferedStack(stackSize, options, randomFEComlexity, ratio, conditionalSize, operationsNum, possibility);
              if (flag) System.out.println(ans[0] + " " + ans[1]);
              bsum += ans[0] / 1000;
              vsum += ans[1] / 1000;
  
          }
          res[i][0] = bsum / nums;
          res[i][1] = vsum / nums;
          System.out.println(" ");
          System.out.println(res[i][0] + ";" + res[i][1] + "; ");
          System.out.println(stackSize + " " + options.length + " "+ randomFEComlexity+ " " + ratio + " " + conditionalSize +" " + operationsNum + " "+ possibility);
      }
      System.out.println("bsum; vsum");
      for (int i = 0; i < n; i++) {
          System.out.println(res[i][0] + ";" + res[i][1]);
      }
  }
 
  public static void main(String[] args) {
      //testFeature();
      //possibilityTest(200, 1, 0.1, 1, 100, 0);
      //ratioTest(200, 1, 0, 1, 100, 0.9);
  }
}
