package gov.nasa.jpf.vm.va;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.org.apache.bcel.internal.generic.StackInstruction;

import de.fosd.typechef.featureexpr.FeatureExpr;
import gov.nasa.jpf.vm.va.StackHandlerFactory.SHFactory;

public class Store {

	private static boolean verbose = false;

	private static Map<MeasuringStackHandler, List<LogEntry>> entries = new HashMap<>();

	private static List<Measurement> measures = new ArrayList<>();
	
	private Store() {
	}

	public static void add(MeasuringStackHandler handler, String methodName, Method method, Object... args) {
		List<LogEntry> instructions = entries.get(handler);
		if (instructions == null) {
			instructions = new ArrayList<>();
			entries.put(handler, instructions);
		}
		try {
			if (method != null) {
				method = IStackHandler.class.getMethod(method.getName(), method.getParameterTypes());
			}
			instructions.add(new LogEntry(methodName, method, args));
		} catch (NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
	}

	public static void print() {
		try (PrintWriter writer = new PrintWriter("/home/meng/stacklog.csv", "UTF-8")){
			writer.print(';');
			for (SHFactory factory : StackHandlerFactory.SHFactory.values()) {
				writer.print(factory);
				writer.print(';');
			}
	
			for (SHFactory factory : StackHandlerFactory.SHFactory.values()) {
				writer.print("V" + factory);
				writer.print(';');
			}
			writer.println();
			
			System.out.println("reexecute stack operations");
			
			for (List<LogEntry> entry : entries.values()) {
				if (verbose) {
					System.out.println(entry.get(0).methodName);
					for (LogEntry logEntry : entry) {
						System.out.println(logEntry.stackInstruction + Arrays.toString(logEntry.args));
					}
					System.out.println();
				}
				StackFactory.activateCStack();
				Object[] initArgs = entry.get(0).args;
				//if(entry.get(0).methodName.equals("<init>") || entry.get(0).methodName.equals("<clinit>")) continue;
				Measurement measurement = new Measurement(entry.get(0).methodName);
				measures.add(measurement);
				for (SHFactory factory : StackHandlerFactory.SHFactory.values()) {
					StackHandlerFactory.setFactory(factory);
					IStackHandler checkStack = StackHandlerFactory.createStack2((FeatureExpr) initArgs[0],
							(int) initArgs[1], (int) initArgs[2]);
					
					long start = System.nanoTime();
					for (LogEntry logEntry : entry) {
						if (logEntry.stackInstruction == null) {
							// case constructor
							continue;
						}
						try {
							if (verbose) {
								System.out.print("invoke: " + logEntry.stackInstruction.getName());
								System.out.println(" args: " + Arrays.toString(logEntry.args));
							}
							logEntry.stackInstruction.invoke(checkStack, logEntry.args);
						} catch (SecurityException | IllegalAccessException | InvocationTargetException e) {
							start = 0;
							break;
						}
					}
					long end = System.nanoTime();
					long duration = (end - start);
					
					measurement.measurement[factory.ordinal()] = duration;
					
				}
				StackFactory.activateVStack();
				initArgs = entry.get(0).args;
				//if(entry.get(0).methodName.equals("<init>") || entry.get(0).methodName.equals("<clinit>")) continue;
			    //measurement = new Measurement(entry.get(0).methodName);
				//measures.add(measurement);
				for (SHFactory factory : StackHandlerFactory.SHFactory.values()) {
					StackHandlerFactory.setFactory(factory);
					IStackHandler checkStack = StackHandlerFactory.createStack2((FeatureExpr) initArgs[0],
							(int) initArgs[1], (int) initArgs[2]);
					
					long start = System.nanoTime();
					for (LogEntry logEntry : entry) {
						if (logEntry.stackInstruction == null) {
							// case constructor
							continue;
						}
						try {
							if (verbose) {
								System.out.print("invoke: " + logEntry.stackInstruction.getName());
								System.out.println(" args: " + Arrays.toString(logEntry.args));
							}
							logEntry.stackInstruction.invoke(checkStack, logEntry.args);
						} catch (SecurityException | IllegalAccessException | InvocationTargetException e) {
							e.printStackTrace();
							start = 0;
							break;
						}
					}
					long end = System.nanoTime();
					long duration = (end - start);
					
					measurement.measurement[factory.ordinal() + 3] = duration;
					
				}

			}
			
			System.out.println("Order measurements");
			Collections.sort(measures, new Comparator<Measurement>() {

				@Override
				public int compare(Measurement o1, Measurement o2) {
					return Long.compare(o1.measurement[SHFactory.Buffered.ordinal()], o2.measurement[SHFactory.Buffered.ordinal()]);
				}
			});
			
			System.out.println("Print measurements");
			int num = 0;
			long sum = 0, vsum = 0;
			for (Measurement measurement : measures) {
					sum += measurement.measurement[2];
					vsum += measurement.measurement[5];

					writer.println(measurement.toString());
				}
				
			
			writer.println("sum is "+ sum + " vsum is" + vsum);
			writer.println("dif is the time minus vtime and the num is the number of stackhandler runs faster than original one");
		} catch (FileNotFoundException | UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

	}

	private static class LogEntry {
		String methodName;
		Method stackInstruction;
		Object[] args;

		public LogEntry(String methodName, Method method, Object[] args) {
			this.methodName = methodName;
			this.stackInstruction = method;
			this.args = args;
		}

	}
	
	static class Measurement {

		String methodName;

		long[] measurement = new long[6];

		public Measurement(String methodName) {
			this.methodName = methodName;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append(methodName);
			for (int i = 0; i < measurement.length; i++) {
				builder.append(';');
				//if(i == 1 || i == 4) 
					builder.append(measurement[i]);
			}
			return builder.toString();
		}
	}
}