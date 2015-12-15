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

/**
 * According the the definition of invokeinterface the method must be resolved prior to any other checks.
 * See http://java.sun.com/docs/books/jvms/second_edition/html/Instructions2.doc6.html#invokeinterface.Description
 *
 * Also described in [ 1147107 ]  unresolved instanceof etc. on &lt;null&gt; not compliant.
 */
public class TestResolveOnInvokeInterface  extends TestJPF {
	static String[] JPF_CONFIGURATION = new String[] { "+nhandler.delegateUnhandledNative", "+search.class=.search.RandomSearch", "+choice=MapChoice" };

	@Test
	public void main() {
		if (verifyUnhandledException(NullPointerException.class.getName(), JPF_CONFIGURATION)) {
			doInvoke(null);
		}
	}


  static interface A {
    String genString();
  }

  public static void main(String[] args) {
    try {
      doInvoke(null);
    } catch (final NullPointerException npe) {
      System.out.println("Got NPE");
    }
  }

  static String doInvoke(final A a) {
    // A should be resolved here
    return a.genString();
  }
}
