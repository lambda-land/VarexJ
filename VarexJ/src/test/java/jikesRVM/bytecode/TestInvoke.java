/*
 *  This file is part of the Jikes RVM project (http://jikesrvm.org).
 *
 *  This file is licensed to You under the Eclipse Public License (EPL);
 *  You may not use this file except in compliance with the License. You
 *  may obtain a copy of the License at
 *
 *      http://www.opensource.org/licenses/eclipse-1.0.php
 *
 *  See the COPYRIGHT.txt file distributed with this work for information
 *  regarding copyright ownership.
 */
package jikesRVM.bytecode;

import org.junit.Test;

import gov.nasa.jpf.util.test.TestJPF;

/*
*/
public class TestInvoke extends TestJPF {
	static String[] JPF_CONFIGURATION = new String[] { "+nhandler.delegateUnhandledNative", "+search.class=.search.RandomSearch", "+choice=MapChoice" };

	@Test
	public void main() {
		if (verifyNoPropertyViolation(JPF_CONFIGURATION)) {
			main(null);
		}
	}

  static interface MyInterface {
    void performMagic();
  }

  static class TypeA {
    TypeA() {System.out.println("TypeA.<init>()");}

    void f() { System.out.println("TypeA.f()"); }
  }

  static class TypeB extends TypeA {
    TypeB() {System.out.println("TypeB.<init>()");}

    //invokevirtual
    void f() { System.out.println("TypeB.f()"); }

    //invokestatic
    static int g(int value) { return 3 + value; }
  }

  static class TypeC extends TypeB implements MyInterface {
    TypeC() {System.out.println("TypeC.<init>()");}

    void test() {
      System.out.println("TypeC.test()");
      myPrivate();
    }

    //invokeinterface
    public void performMagic() {
      System.out.println("TypeC.performMagic()");
    }

    //invokespecial
    private void myPrivate() {
      System.out.println("TypeC.myPrivate()");
    }
  }

  public static void main(String[] args) {
    final TypeA a = new TypeA();
    final TypeB b = new TypeB();
    final TypeC c = new TypeC();

    callF(a);
    callF(b);
    callPerformMagic(c);

    c.test();

    System.out.print("invokestatic TypeB.g() Expected: 42 Actual: ");
    System.out.println(TypeB.g(39));
    assertEquals(42, TypeB.g(39));

    System.out.print("invokestatic TypeC.g() Expected: 16 Actual: ");
    System.out.println(TypeC.g(13));
    assertEquals(16, TypeC.g(13));
  }

//  @NoInline
  private static void callF(TypeA a) {
    a.f();
  }

//  @NoInline
  private static void callPerformMagic(MyInterface myInterface) {
    myInterface.performMagic();
  }
}
