//
// Copyright (C) 2007 United States Government as represented by the
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

import java.util.Locale;

import cmu.conditional.One;
import de.fosd.typechef.featureexpr.FeatureExpr;
import gov.nasa.jpf.annotation.MJI;
@SuppressWarnings("deprecation")
public class JPF_java_util_Locale extends NativePeer {

  static Locale getLocale (MJIEnv env, int locref, FeatureExpr ctx) {

    //--- check first if it's one of the standard locales (ci is obviously loaded at this point
    ClassInfo ci = env.getClassInfo(locref);  // Locale is final, so we can do this
    ElementInfo sei = ci.getStaticElementInfo();

    if (locref == sei.getReferenceField("US").getValue()) return Locale.US;
    if (locref == sei.getReferenceField("GERMAN").getValue()) return Locale.GERMAN;
    if (locref == sei.getReferenceField("ENGLISH").getValue()) return Locale.ENGLISH;
    if (locref == sei.getReferenceField("FRENCH").getValue()) return Locale.FRENCH;
    if (locref == sei.getReferenceField("JAPANESE").getValue()) return Locale.JAPANESE;
    if (locref == sei.getReferenceField("CHINESE").getValue()) return Locale.CHINESE;
    //... we should have a bunch more


    //--- if it wasn't any of these, get the fields and just construct it

    String country, language, variant;
    FieldInfo fiBase = ci.getInstanceField("baseLocale");
    
    if (fiBase != null){ // Java >= 1.7
      int baseLocref = env.getReferenceField(ctx, locref, fiBase);
      country = env.getStringObject(ctx, env.getReferenceField(ctx,baseLocref, "region").getValue());
      language = env.getStringObject(ctx, env.getReferenceField(ctx, baseLocref, "language").getValue());
      variant = env.getStringObject(ctx, env.getReferenceField(ctx, baseLocref, "variant").getValue());
            
    } else {  // Java < 1.7
      country = env.getStringObject(ctx, env.getReferenceField(ctx,locref, "country").getValue());
      language = env.getStringObject(ctx, env.getReferenceField(ctx, locref, "language").getValue());
      variant = env.getStringObject(ctx, env.getReferenceField(ctx, locref, "variant").getValue());
    }
    
    Locale locale = new Locale(language,country,variant); 
    return locale;
  }
  
  @MJI
  public int getDisplayName__Ljava_util_Locale_2__Ljava_lang_String_2 (MJIEnv env, int objref, int locref, FeatureExpr ctx) {
    Locale locale = getLocale(env, locref, ctx);
    String name = locale.getDisplayName();
    return env.newString(ctx, name);
  }
  
  @MJI
  public int getDisplayVariant__Ljava_util_Locale_2__Ljava_lang_String_2 (MJIEnv env, int objref, int locref, FeatureExpr ctx) {
    Locale locale = getLocale(env, locref, ctx);
    String variant = locale.getDisplayVariant();
    return env.newString(ctx, variant);    
  }
  
  @MJI
  public int getDisplayCountry__Ljava_util_Locale_2__Ljava_lang_String_2 (MJIEnv env, int objref, int locref, FeatureExpr ctx) {
    Locale locale = getLocale(env, locref, ctx);
    String country = locale.getDisplayCountry();
    return env.newString(ctx, country);

  }

  @MJI
  public int getDisplayLanguage__Ljava_util_Locale_2__Ljava_lang_String_2 (MJIEnv env, int objref, int locref, FeatureExpr ctx) {
    Locale locale = getLocale(env, locref, ctx);
    String language = locale.getDisplayLanguage();
    return env.newString(ctx, language);
  }

  @MJI
  public int getISO3Country____Ljava_lang_String_2 (MJIEnv env, int objref, FeatureExpr ctx) {
    Locale locale = getLocale(env, objref, ctx);
    String s = locale.getISO3Country();
    return env.newString(ctx, s);    
  }

  @MJI
  public int getISO3Language____Ljava_lang_String_2 (MJIEnv env, int objref, FeatureExpr ctx) {
    Locale locale = getLocale(env, objref, ctx);
    String s = locale.getISO3Language();
    return env.newString(ctx, s);
  }

  //--- the static ones
  @MJI
  public int getISOCountries_____3Ljava_lang_String_2 (MJIEnv env, int clsref, FeatureExpr ctx) {
    String[] s = Locale.getISOCountries();

    int aref = env.newObjectArray("java.lang.String", s.length);
    for (int i=0; i<s.length; i++) {
      env.setReferenceArrayElement(ctx, aref, i, new One<>(env.newString(ctx, s[i])));
    }
    
    return aref;
  }
  
  @MJI
  public int getISOLanguages_____3Ljava_lang_String_2 (MJIEnv env, int clsref, FeatureExpr ctx) {
    String[] s = Locale.getISOLanguages();

    int aref = env.newObjectArray("java.lang.String", s.length);
    for (int i=0; i<s.length; i++) {
      env.setReferenceArrayElement(ctx, aref, i, new One<>(env.newString(ctx, s[i])));
    }
    
    return aref;    
  }

}
