/***
 * Copyright (c) 2000-2007 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
 
 /******************************************************************************
 * Portions modified by VMware are provided under the EPL and ASL
 * Copyright (c) 2002, 2010, VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * The Eclipse Public License is available at 
 * http://www.eclipse.org/legal/epl-v10.html and the Apache License v2.0
 * is available at http://www.opensource.org/licenses/apache2.0.php.
 * You may elect to redistribute this code under either of these licenses. 
 * 
 * Contributors:
 *   VMware Inc.
 *****************************************************************************/
 
package org.eclipse.gemini.blueprint.test.internal.util;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * ASM based class for determining a class imports. The code is heavily based on an Eugene Kuleshov's ASM <a
 * href="http://asm.objectweb.org/doc/tutorial-asm-2.0.html">tutorial</a>.
 * 
 * <p/>The main differences from the original source in the article are the 1.4 compatibility, the handling of class
 * objects not instantiated (MyClass.class.getName()) as these are specially handled by the compiler and analysis of
 * inner classes, including ones from different packages.
 * 
 * @author Costin Leau
 */
public class DependencyVisitor extends ClassVisitor {

	private final AnnotationVisitor av = new AnnotationVisitor(Opcodes.ASM5) {
			@Override
			public void visit(String name, Object value) {
				DependencyVisitor.this.visit(name, value);
			}

			@Override
			public void visitEnum(String name, String desc, String value) {
				DependencyVisitor.this.visitEnum(name, desc, value);
			}

			@Override
			public AnnotationVisitor visitAnnotation(String name, String desc) {
				return DependencyVisitor.this.visitAnnotation(name, desc);
			}

			@Override
			public AnnotationVisitor visitArray(String name) {
				return DependencyVisitor.this.visitArray(name);
			}

			@Override
			public void visitEnd() {
				DependencyVisitor.this.visitEnd();
			}
	};

	private final SignatureVisitor sv = new SignatureVisitor(Opcodes.ASM5) {
		@Override
		public void visitFormalTypeParameter(String name) {
			DependencyVisitor.this.visitFormalTypeParameter(name);
		}

		@Override
		public SignatureVisitor visitClassBound() {
			return DependencyVisitor.this.visitClassBound();
		}

		@Override
		public SignatureVisitor visitInterfaceBound() {
			return DependencyVisitor.this.visitInterfaceBound();
		}

		@Override
		public SignatureVisitor visitSuperclass() {
			return DependencyVisitor.this.visitSuperclass();
		}

		@Override
		public SignatureVisitor visitInterface() {
			return DependencyVisitor.this.visitInterface();
		}

		@Override
		public SignatureVisitor visitParameterType() {
			return DependencyVisitor.this.visitParameterType();
		}

		@Override
		public SignatureVisitor visitReturnType() {
			return DependencyVisitor.this.visitReturnType();
		}

		@Override
		public SignatureVisitor visitExceptionType() {
			return DependencyVisitor.this.visitExceptionType();
		}

		@Override
		public void visitBaseType(char descriptor) {
			DependencyVisitor.this.visitBaseType(descriptor);
		}

		@Override
		public void visitTypeVariable(String name) {
			DependencyVisitor.this.visitTypeVariable(name);
		}

		@Override
		public SignatureVisitor visitArrayType() {
			return DependencyVisitor.this.visitArrayType();
		}

		@Override
		public void visitClassType(String name) {
			DependencyVisitor.this.visitClassType(name);
		}

		@Override
		public void visitInnerClassType(String name) {
			DependencyVisitor.this.visitInnerClassType(name);
		}

		@Override
		public void visitTypeArgument() {
			DependencyVisitor.this.visitTypeArgument();
		}

		@Override
		public SignatureVisitor visitTypeArgument(char wildcard) {
			return DependencyVisitor.this.visitTypeArgument(wildcard);
		}

		@Override
		public void visitEnd() {
			DependencyVisitor.this.visitEnd();
		}
	};

	private final FieldVisitor fv = new FieldVisitor(Opcodes.ASM5) {
		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			return DependencyVisitor.this.visitAnnotation(desc, visible);
		}

		@Override
		public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
			return DependencyVisitor.this.visitTypeAnnotation(typeRef, typePath, desc, visible);
		}

		@Override
		public void visitAttribute(Attribute attr) {
			DependencyVisitor.this.visitAttribute(attr);
		}

		@Override
		public void visitEnd() {
			DependencyVisitor.this.visitEnd();
		}
	};

	private final MethodVisitor mv = new MethodVisitor(Opcodes.ASM5) {
		@Override
		public void visitParameter(String name, int access) {
			DependencyVisitor.this.visitParameter(name, access);
		}

		@Override
		public AnnotationVisitor visitAnnotationDefault() {
			return DependencyVisitor.this.visitAnnotationDefault();
		}

		@Override
		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			return DependencyVisitor.this.visitAnnotation(desc, visible);
		}

		@Override
		public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
			return DependencyVisitor.this.visitTypeAnnotation(typeRef, typePath, desc, visible);
		}

		@Override
		public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
			return DependencyVisitor.this.visitParameterAnnotation(parameter, desc, visible);
		}

		@Override
		public void visitAttribute(Attribute attr) {
			DependencyVisitor.this.visitAttribute(attr);
		}

		@Override
		public void visitCode() {
			DependencyVisitor.this.visitCode();
		}

		@Override
		public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
			DependencyVisitor.this.visitFrame(type, nLocal, local, nStack, stack);
		}

		@Override
		public void visitInsn(int opcode) {
			DependencyVisitor.this.visitInsn(opcode);
		}

		@Override
		public void visitIntInsn(int opcode, int operand) {
			DependencyVisitor.this.visitIntInsn(opcode, operand);
		}

		@Override
		public void visitVarInsn(int opcode, int var) {
			DependencyVisitor.this.visitVarInsn(opcode, var);
		}

		@Override
		public void visitTypeInsn(int opcode, String type) {
			DependencyVisitor.this.visitTypeInsn(opcode, type);
		}

		@Override
		public void visitFieldInsn(int opcode, String owner, String name, String desc) {
			DependencyVisitor.this.visitFieldInsn(opcode, owner, name, desc);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc) {
			DependencyVisitor.this.visitMethodInsn(opcode, owner, name, desc, false);
		}

		@Override
		public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
			DependencyVisitor.this.visitMethodInsn(opcode, owner, name, desc, itf);
		}

		@Override
		public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
			DependencyVisitor.this.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
		}

		@Override
		public void visitJumpInsn(int opcode, Label label) {
			DependencyVisitor.this.visitJumpInsn(opcode, label);
		}

		@Override
		public void visitLabel(Label label) {
			DependencyVisitor.this.visitLabel(label);
		}

		@Override
		public void visitLdcInsn(Object cst) {
			DependencyVisitor.this.visitLdcInsn(cst);
		}

		@Override
		public void visitIincInsn(int var, int increment) {
			DependencyVisitor.this.visitIincInsn(var, increment);
		}

		@Override
		public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
			DependencyVisitor.this.visitTableSwitchInsn(min, max, dflt, labels);
		}

		@Override
		public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
			DependencyVisitor.this.visitLookupSwitchInsn(dflt, keys, labels);
		}

		@Override
		public void visitMultiANewArrayInsn(String desc, int dims) {
			DependencyVisitor.this.visitMultiANewArrayInsn(desc, dims);
		}

		@Override
		public AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
			return DependencyVisitor.this.visitInsnAnnotation(typeRef, typePath, desc, visible);
		}

		@Override
		public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
			DependencyVisitor.this.visitTryCatchBlock(start, end, handler, type);
		}

		@Override
		public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
			return DependencyVisitor.this.visitTryCatchAnnotation(typeRef, typePath, desc, visible);
		}

		@Override
		public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
			DependencyVisitor.this.visitLocalVariable(name, desc, signature, start, end, index);
		}

		@Override
		public AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
			return DependencyVisitor.this.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible);
		}

		@Override
		public void visitLineNumber(int line, Label start) {
			DependencyVisitor.this.visitLineNumber(line, start);
		}

		@Override
		public void visitMaxs(int maxStack, int maxLocals) {
			DependencyVisitor.this.visitMaxs(maxStack, maxLocals);
		}

		@Override
		public void visitEnd() {
			DependencyVisitor.this.visitEnd();
		}
	};

	public DependencyVisitor() {
		super(Opcodes.ASM7);
	}

	private AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
		tempLdc = null;
		addDesc(desc);
		return this.av;
	}

	private AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
		tempLdc = null;
		addDesc(desc);
		return this.av;
	}

	private AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
		tempLdc = null;
		addDesc(desc);
		return this.av;
	}

	private void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object[] bsmArgs) {
		for (Object o : bsmArgs) {
			if (o instanceof Type) {
				addType((Type) o);
			}
		}
	}

	private void visitParameter(String name, int access) {
		tempLdc = null;
	}

	private Set packages = new LinkedHashSet();

	private Map groups = new LinkedHashMap();

	private Map current;

	private String tempLdc;

	private String ownerName;

	private Set innerClasses = new LinkedHashSet(4);

	private static final String CLASS_NAME = Class.class.getName();

	public Map getGlobals() {
		return groups;
	}

	public Set getPackages() {
		return packages;
	}

	public Set getInnerClasses() {
		return innerClasses;
	}

	// ClassVisitor
	public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
		tempLdc = null;
		this.ownerName = name;
		String p = getGroupKey(name);
		current = (Map) groups.get(p);
		if (current == null) {
			current = new LinkedHashMap();
			groups.put(p, current);
		}

		if (signature == null) {
			addName(superName);
			addNames(interfaces);
		} else {
			addSignature(signature);
		}
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		tempLdc = null;
		addDesc(desc);
		return this.av;
	}

	public void visitAttribute(Attribute attr) {
		tempLdc = null;
	}

	public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
		tempLdc = null;
		if (signature == null) {
			addDesc(desc);
		} else {
			addTypeSignature(signature);
		}
		if (value instanceof Type)
			addType((Type) value);
		return fv;
	}

	public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
		tempLdc = null;
		if (signature == null) {
			addMethodDesc(desc);
		} else {
			addSignature(signature);
		}
		addNames(exceptions);
		return mv;
	}

	public void visitSource(String source, String debug) {
		tempLdc = null;
	}

	public void visitInnerClass(String name, String outerName, String innerName, int access) {
		tempLdc = null;
		addName(name);
		addName(outerName);

		if (!ownerName.equals(name)) {
			innerClasses.add(name);
		}
	}

	public void visitOuterClass(String owner, String name, String desc) {
		tempLdc = null;

		// addName(owner);
		// addMethodDesc(desc);
	}

	// MethodVisitor

	public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
		tempLdc = null;
		addDesc(desc);
		return this.av;
	}

	public void visitTypeInsn(int opcode, String desc) {
		tempLdc = null;
		if (desc.charAt(0) == '[')
			addDesc(desc);
		else
			addName(desc);
	}

	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		tempLdc = null;
		addName(owner);
		addDesc(desc);
	}

	public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
		String returnType = Type.getReturnType(desc).getClassName();
		if (opcode == Opcodes.INVOKESTATIC && CLASS_NAME.equals(returnType)) {
			if (tempLdc != null)
				addName(tempLdc.replace('.', '/'));
		}

		tempLdc = null;

		addName(owner);
		addMethodDesc(desc);
	}

	public void visitLdcInsn(Object cst) {
		tempLdc = null;
		if (cst instanceof Type)
			addType((Type) cst);
		else if (cst instanceof String) {
			tempLdc = (String) cst;
		}
	}

	public void visitMultiANewArrayInsn(String desc, int dims) {
		tempLdc = null;
		addDesc(desc);
	}

	public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
		tempLdc = null;
		addTypeSignature(signature);
	}

	public AnnotationVisitor visitAnnotationDefault() {
		tempLdc = null;
		return av;
	}

	public void visitCode() {
		tempLdc = null;
	}

	public void visitFrame(int i, int i1, Object[] objects, int i2, Object[] objects1) {
		tempLdc = null;
	}

	public void visitInsn(int opcode) {
		tempLdc = null;
	}

	public void visitIntInsn(int opcode, int operand) {
		tempLdc = null;
	}

	public void visitVarInsn(int opcode, int var) {
		tempLdc = null;
	}

	public void visitJumpInsn(int opcode, Label label) {
		tempLdc = null;
	}

	public void visitLabel(Label label) {
		tempLdc = null;
	}

	public void visitIincInsn(int var, int increment) {
		tempLdc = null;
	}

	public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
		tempLdc = null;
	}

	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
		tempLdc = null;
	}

	public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
		tempLdc = null;
		addName(type);
	}

	public void visitLineNumber(int line, Label start) {
		tempLdc = null;
	}

	public void visitMaxs(int maxStack, int maxLocals) {
		tempLdc = null;
	}

	// AnnotationVisitor

	public void visit(String name, Object value) {
		tempLdc = null;
		if (value instanceof Type)
			addType((Type) value);
	}

	public void visitEnum(String name, String desc, String value) {
		tempLdc = null;
		addDesc(desc);
	}

	public AnnotationVisitor visitAnnotation(String name, String desc) {
		tempLdc = null;
		addDesc(desc);
		return this.av;
	}

	public AnnotationVisitor visitArray(String name) {
		tempLdc = null;
		return this.av;
	}

	// SignatureVisitor

	public void visitFormalTypeParameter(String name) {
		tempLdc = null;
	}

	public SignatureVisitor visitClassBound() {
		tempLdc = null;
		return sv;
	}

	public SignatureVisitor visitInterfaceBound() {
		tempLdc = null;
		return sv;
	}

	public SignatureVisitor visitSuperclass() {
		tempLdc = null;
		return sv;
	}

	public SignatureVisitor visitInterface() {
		tempLdc = null;
		return sv;
	}

	public SignatureVisitor visitParameterType() {
		tempLdc = null;
		return sv;
	}

	public SignatureVisitor visitReturnType() {
		tempLdc = null;
		return sv;
	}

	public SignatureVisitor visitExceptionType() {
		tempLdc = null;
		return sv;
	}

	public void visitBaseType(char descriptor) {
		tempLdc = null;
	}

	public void visitTypeVariable(String name) {
		tempLdc = null;
	}

	public SignatureVisitor visitArrayType() {
		tempLdc = null;
		return sv;
	}

	public void visitClassType(String name) {
		tempLdc = null;
		addName(name);
	}

	public void visitInnerClassType(String name) {
		tempLdc = null;
		addName(name);
	}

	public void visitTypeArgument() {
		tempLdc = null;
	}

	public SignatureVisitor visitTypeArgument(char wildcard) {
		tempLdc = null;
		return sv;
	}

	// common

	public void visitEnd() {
		tempLdc = null;
	}

	// ---------------------------------------------

	private String getGroupKey(String name) {
		int n = name.lastIndexOf('/');
		if (n > -1)
			name = name.substring(0, n);
		packages.add(name);
		return name;
	}

	private void addName(String name) {
		if (name == null)
			return;
		String p = getGroupKey(name);
		if (current.containsKey(p)) {
			current.put(p, new Integer(((Integer) current.get(p)).intValue() + 1));
		} else {
			current.put(p, new Integer(1));
		}
	}

	private void addNames(String[] names) {
		for (int i = 0; names != null && i < names.length; i++)
			addName(names[i]);
	}

	private void addDesc(String desc) {
		addType(Type.getType(desc));
	}

	private void addMethodDesc(String desc) {
		addType(Type.getReturnType(desc));
		Type[] types = Type.getArgumentTypes(desc);
		for (int i = 0; i < types.length; i++)
			addType(types[i]);
	}

	private void addType(Type t) {
		switch (t.getSort()) {
		case Type.ARRAY:
			addType(t.getElementType());
			break;
		case Type.OBJECT:
			addName(t.getClassName().replace('.', '/'));
			break;
		}
	}

	private void addSignature(String signature) {
		if (signature != null)
			new SignatureReader(signature).accept(this.sv);
	}

	private void addTypeSignature(String signature) {
		if (signature != null)
			new SignatureReader(signature).acceptType(this.sv);
	}
}