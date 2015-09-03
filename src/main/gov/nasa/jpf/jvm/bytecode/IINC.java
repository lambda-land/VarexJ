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
package gov.nasa.jpf.jvm.bytecode;

import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import cmu.conditional.Conditional;
import cmu.utils.ComplexityPrinter;
import de.fosd.typechef.featureexpr.FeatureExpr;

/**
 * Increment local variable by constant No change
 */
public class IINC extends LocalVariableInstruction {

	protected int increment;

	public IINC(int localVarIndex, int increment) {
		super(localVarIndex);
		this.increment = increment;
	}

	@Override
	public Conditional<Instruction> execute(final FeatureExpr ctx, ThreadInfo ti) {
		StackFrame frame = ti.getModifiableTopFrame();
		int before = ((Conditional)frame.stack.getLocal(index)).size();
		frame.IINC(ctx, index, increment);
		int after = ((Conditional)frame.stack.getLocal(index)).size();
		ComplexityPrinter.addComplex(before, getClass().getSimpleName(), ctx, frame.getMethodInfo(), ti);
		return getNext(ctx, ti);
	}
	
	@Override
	protected Number instruction(Number v1, Number v2) {
		return v1.intValue() + v2.intValue();
	}

	public int getLength() {
		return 3; // opcode, index, const
	}

	@Override
	public int getByteCode() {
		return 0x84; // ?? wide
	}

	@Override
	public void accept(InstructionVisitor insVisitor) {
		insVisitor.visit(this);
	}

	public int getIndex() {
		return index;
	}

	public int getIncrement() {
		return increment;
	}

	@Override
	public String getBaseMnemonic() {
		return "iinc";
	}

}
