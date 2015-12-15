//
// Copyright (C) 2006 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
// (NASA).  All Rights Reserved.
// 
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3.  The NOSA has been approved by the Open Source
// Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
// 
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//
package gov.nasa.jpf.vm;

import java.io.PrintWriter;
import java.util.HashSet;

import de.fosd.typechef.featureexpr.FeatureExpr;

/**
 * helper class to store context of an exception
 */
public class ExceptionInfo {
  ElementInfo  ei;
  ThreadInfo ti;
  final FeatureExpr ctx;
  
  public ExceptionInfo (FeatureExpr ctx, ThreadInfo xThread, ElementInfo xEi) {
	  this.ctx = ctx;
    ti = xThread;
    ei = xEi;
  }
  
  public FeatureExpr getCtx() {
	return ctx;
  }
  
  public ElementInfo getException() {
    return ei;
  }
  
  public int getExceptionReference () {
    return ei.getObjectRef();
  }
  
  public String getExceptionClassname() {
    return ei.getClassInfo().getName();
  }

    // Instead of returning only the current Exception name, also return the names of
    // its super classes
    public HashSet<String> getExceptionClassnames() {
        HashSet<String> set = new HashSet<>();
        ClassInfo info = ei.getClassInfo();
        set.add(info.getName());
        while (info.getSuperClass() != null){
            info = info.getSuperClass();
            set.add(info.getName());
        }
        return set;
    }
  
  public String getDetails() {
    StringBuilder sb = new StringBuilder();
    sb.append(getExceptionClassname());
    
    int msgRef = ei.getReferenceField("detailMessage").getValue();
    if (msgRef != MJIEnv.NULL){
      ElementInfo eiMsg = ti.getElementInfo(msgRef);
      sb.append(" : ");
      sb.append(eiMsg.asString().getValue());
    }
      
    String s = sb.toString();
    return s;
  }
  
  public String getCauseClassname() {
    int causeRef = ei.getReferenceField("cause").getValue();
    if (causeRef != MJIEnv.NULL){
      ElementInfo eiCause = ti.getElementInfo(causeRef);
      return eiCause.getClassInfo().getName();
    }
    
    return null;
  }
  public String getCauseDetails() {
    int causeRef = ei.getReferenceField("cause").getValue();
    if (causeRef != MJIEnv.NULL){
      ElementInfo eiCause = ti.getElementInfo(causeRef);
      int msgRef = eiCause.getReferenceField("detailMessage").getValue();
      if (msgRef != MJIEnv.NULL){
        ElementInfo eiMsg = ti.getElementInfo(msgRef);
        return eiMsg.asString().getValue();
      }
    }

    return null;
  }

  
  public ThreadInfo getThread() {
    return ti;
  }
  
  public void printOn (PrintWriter pw){
    ti.printStackTrace(ctx, pw, ei.getObjectRef());
  }
}
