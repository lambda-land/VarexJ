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

import cmu.conditional.BiFunction;
import cmu.conditional.Conditional;
import cmu.conditional.Function;
import cmu.conditional.One;
import cmu.conditional.VoidBiFunction;
import de.fosd.typechef.featureexpr.FeatureExpr;
import gov.nasa.jpf.annotation.MJI;

public class JPF_java_lang_StringBuilder extends NativePeer {

	@Deprecated
	int appendString(FeatureExpr ctx, MJIEnv env, int objref, String s) {
		return appendString(ctx, env, objref, new One<>(s));
	}

	Conditional<Integer> appendString(FeatureExpr ctx, final MJIEnv env, final Conditional<Integer> objref, final Conditional<String> conditionalS) {
		return objref.mapf(ctx, new BiFunction<FeatureExpr, Integer, Conditional<Integer>>() {

			@Override
			public Conditional<Integer> apply(FeatureExpr ctx, Integer objref) {
				return new One<>(appendString(ctx, env, objref, conditionalS));
			}
			
		});
	}
	
	int appendString(FeatureExpr ctx, final MJIEnv env, final int objref, final Conditional<String> conditionalS) {
		Conditional<Integer> condAref = env.getReferenceField(ctx, objref, "value");

		condAref.mapf(ctx, new VoidBiFunction<FeatureExpr, Integer>() {

			@Override
			public void apply(FeatureExpr ctx, final Integer aref) {
				conditionalS.mapf(ctx, new VoidBiFunction<FeatureExpr, String>() {

					@Override
					public void apply(FeatureExpr ctx, final String s) {
						final int slen = s.length();
						final int alen = env.getArrayLength(ctx, aref);
						Conditional<Integer> count = env.getIntField(objref, "count");
						count.mapf(ctx, new VoidBiFunction<FeatureExpr, Integer>() {

							@Override
							public void apply(FeatureExpr ctx, Integer count) {
								if (Conditional.isContradiction(ctx)) {
									return;
								}
								int i, j;
								int n = count + slen;

								if (n < alen) {
									for (i = count, j = 0; i < n; i++, j++) {
										env.setCharArrayElement(ctx, aref, i, new One<>(s.charAt(j)));
									}
								} else {
									int m = 3 * alen / 2;
									if (m < n) {
										m = n;
									}
									int arefNew = env.newCharArray(ctx, m);
									for (i = 0; i < count; i++) {
										env.setCharArrayElement(ctx, arefNew, i, env.getCharArrayElement(aref, i));
									}
									for (j = 0; i < n; i++, j++) {
										env.setCharArrayElement(ctx, arefNew, i, new One<>(s.charAt(j)));
									}
									env.setReferenceField(ctx, objref, "value", arefNew);
								}

								env.setIntField(ctx, objref, "count", new One<>(n));
							}

						});
					}

				});
			}
		});
		return objref;
	}

	// we skip the AbstractStringBuilder ctor here, which is a bit dangerous
	// This is only justified because StringBuilders are used everywhere (implicitly)
	@MJI
	public void $init____V(MJIEnv env, int objref, FeatureExpr ctx) {
		int aref = env.newCharArray(ctx, 16);
		env.setReferenceField(ctx, objref, "value", aref);
	}

	@MJI
	public void $init__I__V(final MJIEnv env, final int objref, Conditional<Integer> len, FeatureExpr ctx) {
		len.mapf(ctx, new VoidBiFunction<FeatureExpr, Integer>() {

			@Override
			public void apply(FeatureExpr ctx, Integer len) {
				int aref = env.newCharArray(ctx, len);
				env.setReferenceField(ctx, objref, "value", aref);
			}
		});
	}

	@MJI
	// TODO can be improved
	public void $init__Ljava_lang_String_2__V(final MJIEnv env, final int objref, Conditional<Integer> sRef, FeatureExpr ctx) {
		sRef.mapf(ctx, new VoidBiFunction<FeatureExpr, Integer>() {

			@Override
			public void apply(FeatureExpr ctx, Integer sRef) {
				if (sRef.intValue() == MJIEnv.NULL) {
					env.throwException(ctx, "java.lang.NullPointerException");
					return;
				}

				Conditional<char[]> src = env.getStringChars(sRef);

				src.mapf(ctx, new VoidBiFunction<FeatureExpr, char[]>() {

					@Override
					public void apply(final FeatureExpr ctx, final char[] src) {
						if (Conditional.isContradiction(ctx)) {
							return;
						}
						int aref = env.newCharArray(ctx, src.length + 16);
						// char[] dst = env.getCharArrayObject(aref).getValue();
						for (int i = 0; i < src.length; i++) {
							env.setCharArrayElement(ctx, aref, i, new One<>(src[i]));
						}
						// System.arraycopy(src, 0, dst, 0, src.length);
						env.setReferenceField(ctx, objref, "value", aref);
						env.setIntField(ctx, objref, "count", new One<>(src.length));
					}

				});
			}

		});
	}

	@MJI
	public Conditional<Integer> append__Ljava_lang_String_2__Ljava_lang_StringBuilder_2(final MJIEnv env, Conditional<Integer> objref, Conditional<Integer> sref, FeatureExpr ctx) {
		Conditional<String> s = sref.mapr(new Function<Integer, Conditional<String>>() {

			@Override
			public Conditional<String> apply(Integer sref) {
				return env.getConditionalStringObject(sref);
			}

		});
		s = s.simplify(ctx).map(new Function<String, String>() {

			@Override
			public String apply(final String s) {
				if (s == null) {
					return "null";
				}
				return s;
			}
		}).simplifyValues();
		return appendString(ctx, env, objref, s);
	}

	@MJI
	public int append__I__Ljava_lang_StringBuilder_2(MJIEnv env, int objref, Conditional<Integer> i, FeatureExpr ctx) {
		Conditional<String> s = i.map(new Function<Integer, String>() {

			@Override
			public String apply(Integer i) {
				return Integer.toString(i);
			}

		});

		return appendString(ctx, env, objref, s);
	}

	@MJI
	public int append__F__Ljava_lang_StringBuilder_2(MJIEnv env, int objref, float f, FeatureExpr ctx) {
		String s = Float.toString(f);

		return appendString(ctx, env, objref, s);
	}

	@MJI
	public int append__D__Ljava_lang_StringBuilder_2(MJIEnv env, int objref, Conditional<Double> d, FeatureExpr ctx) {
		Conditional<String> s = d.map(new Function<Double, String>() {

			@Override
			public String apply(Double d) {
				return Double.toString(d);
			}

		});

		return appendString(ctx, env, objref, s);
	}

	@MJI
	public int append__J__Ljava_lang_StringBuilder_2(MJIEnv env, int objref, Conditional<Long> l, FeatureExpr ctx) {
		Conditional<String> s = l.map(new Function<Long, String>() {

			@Override
			public String apply(Long l) {
				return Long.toString(l);
			}

		});

		return appendString(ctx, env, objref, s);
	}

	@MJI
	public int append__Z__Ljava_lang_StringBuilder_2(MJIEnv env, int objref, Conditional<Boolean> b, FeatureExpr ctx) {
		Conditional<String> s = b.map(new Function<Boolean, String>() {

			@Override
			public String apply(Boolean b) {
				return b ? "true" : "false";
			}

		});

		return appendString(ctx, env, objref, s);
	}

	@MJI
	public int append__C__Ljava_lang_StringBuilder_2(MJIEnv env, Conditional<Integer> objref, Conditional<Character> c, FeatureExpr ctx) {
		Conditional<String> s = c.map(new Function<Character, String>() {

			@Override
			public String apply(Character c) {
				return c + "";
			}
			
		});
		
		
		return appendString(ctx, env, objref.getValue(), s);
		//
		// int aref = env.getReferenceField(ctx, objref, "value").getValue();
		// int alen = env.getArrayLength(aref);
		//
		// int count = env.getIntField(ctx, objref, "count").getValue().intValue();
		// int i;
		// int n = count +1;
		//
		// if (n < alen) {
		// env.setCharArrayElement(ctx, aref, count, c);
		// } else {
		// int m = 3 * alen / 2;
		// int arefNew = env.newCharArray(ctx, m);
		// for (i=0; i<count; i++) {
		// env.setCharArrayElement(ctx, arefNew, i, env.getCharArrayElement(aref, i).getValue());
		// }
		// env.setCharArrayElement(ctx, arefNew, count, c);
		// env.setReferenceField(ctx, objref, "value", arefNew);
		// }
		//
		// env.setIntField(ctx, objref, "count", n);
		//
		// return objref;

	}

	@MJI
	public int toString____Ljava_lang_String_2(final MJIEnv env, final int objref, FeatureExpr ctx) {

		Conditional<Integer> aref = env.getReferenceField(ctx, objref, "value");
		Conditional<String> s = aref.mapf(ctx, new BiFunction<FeatureExpr, Integer, Conditional<String>>() {

			@Override
			public Conditional<String> apply(FeatureExpr ctx, Integer aref) {
				Conditional<char[]> buf = env.getCharArrayObject(aref).simplify(ctx);
				return buf.mapf(ctx, new BiFunction<FeatureExpr, char[], Conditional<String>>() {

					@Override
					public Conditional<String> apply(FeatureExpr ctx, final char[] buf) {
						final Conditional<Integer> count = env.getIntField(objref, "count").simplify(ctx);
						return count.mapf(ctx, new BiFunction<FeatureExpr, Integer, Conditional<String>>() {

							@Override
							public Conditional<String> apply(FeatureExpr ctx, Integer count) {
								return new One<>(new String(buf, 0, count));
							}

						});

					}
				});
			}
		}).simplify();

		return env.newString(ctx, s);
	}
	@SuppressWarnings("deprecation")
	@MJI
	public Conditional<Integer> indexOf__Ljava_lang_String_2I__I(final MJIEnv env, int objref, final int str, final int fromIndex, FeatureExpr ctx) {
		Integer aref = env.getReferenceField(ctx, objref, "value").getValue();
		Conditional<char[]> buf = env.getCharArrayObject(aref);
		return buf.mapf(ctx, new BiFunction<FeatureExpr, char[], Conditional<Integer>>() {

			@Override
			public Conditional<Integer> apply(FeatureExpr ctx, char[] buf) {
				String indexStr = env.getStringObject(ctx, str);
				return new One<>(new String(buf).indexOf(indexStr, fromIndex));
			}
			
		});
		
	}

	@MJI
	public int substring__I__Ljava_lang_String_2(MJIEnv env, int objRef, int beginIndex, FeatureExpr ctx) {
		Integer aref = env.getReferenceField(ctx, objRef, "value").getValue();
		char[] buf = env.getCharArrayObject(aref).getValue();
		String obj = new String(buf);
		String result = obj.substring(beginIndex);
		return env.newString(ctx, result);
	}

	@MJI
	public int substring__II__Ljava_lang_String_2(MJIEnv env, int objRef, final Conditional<Integer> beginIndex, final Conditional<Integer> endIndex, FeatureExpr ctx) {
		Integer aref = env.getReferenceField(ctx, objRef, "value").getValue();
		Conditional<char[]> buf = env.getCharArrayObject(aref);
		Conditional<String> result = buf.mapf(ctx, new BiFunction<FeatureExpr, char[], Conditional<String>>() {

			@Override
			public Conditional<String> apply(FeatureExpr ctx, char[] buf) {
				String obj = new String(buf);
				return new One<>(obj.substring(beginIndex.getValue(), endIndex.getValue()));
			}
			
		});
		return env.newString(ctx, result);

	}

	@MJI
	public int delete__II__Ljava_lang_StringBuilder_2(final MJIEnv env, final int objref, final Integer beginIndex, final Integer endIndex, FeatureExpr ctx) {
		final Integer aref = env.getReferenceField(ctx, objref, "value").getValue();
		Conditional<Integer> count = env.getIntField(objref, "count");
		final int diff = endIndex - beginIndex;
		count.mapf(ctx, new VoidBiFunction<FeatureExpr, Integer>() {

			@Override
			public void apply(FeatureExpr ctx, Integer count) {
				for (int i = beginIndex, j = endIndex; i < count; i++, j++) {
					if (j < count) {
						env.setCharArrayElement(ctx, aref, i, env.getCharArrayElement(aref, j));
					} else {
						env.setCharArrayElement(ctx, aref, i, new One<>(' '));
					}
				}

				env.setIntField(ctx, objref, "count", new One<>(count - diff));
			}

		});
		return objref;

	}
}
