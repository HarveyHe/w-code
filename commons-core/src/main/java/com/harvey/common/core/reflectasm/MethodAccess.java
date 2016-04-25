/**
 * Copyright (c) 2008, Nathan Sweet
 *  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 *  3. Neither the name of Esoteric Software nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.harvey.common.core.reflectasm;

import static org.springframework.asm.Opcodes.AALOAD;
import static org.springframework.asm.Opcodes.ACC_PUBLIC;
import static org.springframework.asm.Opcodes.ACC_SUPER;
import static org.springframework.asm.Opcodes.ACC_VARARGS;
import static org.springframework.asm.Opcodes.ACONST_NULL;
import static org.springframework.asm.Opcodes.ALOAD;
import static org.springframework.asm.Opcodes.ARETURN;
import static org.springframework.asm.Opcodes.ASTORE;
import static org.springframework.asm.Opcodes.ATHROW;
import static org.springframework.asm.Opcodes.BIPUSH;
import static org.springframework.asm.Opcodes.CHECKCAST;
import static org.springframework.asm.Opcodes.DUP;
import static org.springframework.asm.Opcodes.ILOAD;
import static org.springframework.asm.Opcodes.INVOKEINTERFACE;
import static org.springframework.asm.Opcodes.INVOKESPECIAL;
import static org.springframework.asm.Opcodes.INVOKESTATIC;
import static org.springframework.asm.Opcodes.INVOKEVIRTUAL;
import static org.springframework.asm.Opcodes.NEW;
import static org.springframework.asm.Opcodes.RETURN;
import static org.springframework.asm.Opcodes.V1_1;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.asm.ClassWriter;
import org.springframework.asm.Label;
import org.springframework.asm.MethodVisitor;
import org.springframework.asm.Opcodes;
import org.springframework.asm.Type;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import com.harvey.common.core.utils.MethodInfo;
import com.harvey.common.core.utils.ReflectionUtils;

public abstract class MethodAccess {

    private static final Map<Class<?>, MethodAccess> instanceCache = new HashMap<Class<?>, MethodAccess>();
    private static final BeanWrapper beanWrapper = new BeanWrapperImpl();

    private List<MethodInfo> methodInfos;

    abstract public Object invoke(Object object, int methodIndex, Object... args);

    public Object invoke(Object object, String methodName, Class[] paramTypes, Object... args) {
        int index = getIndex(methodName, paramTypes);
        MethodInfo methodInfo = methodInfos.get(index);
        convertArgs(methodInfo, args);
        return invoke(object, index, args);
    }

    public Object invoke(Object object, String methodName, Object... args) {
        int index = getIndex(methodName, args == null ? 0 : args.length);
        MethodInfo methodInfo = methodInfos.get(index);
        convertArgs(methodInfo, args);
        return invoke(object, index, args);
    }

    public Object tryInvoke(Object object, String methodName, Object... args) {
        MethodInfo methodInfo = null;
        int index = -1;
        for (int i = 0; i < methodInfos.size(); i++) {
            if (methodInfos.get(i).getMethod().getName().equals(methodName)) {
                methodInfo = methodInfos.get(i);
                index = i;
            }
        }
        if (index > -1) {
            convertArgs(methodInfo, args);
            return invoke(object, index, args);
        }
        return null;
    }

    private void convertArgs(MethodInfo methodInfo, Object[] args) {
        Class<?>[] paramTypes = methodInfo.getParameterTypes();
        if (args == null || args.length == 0) {
            return;
        }
        for (int i = 0; i < paramTypes.length; i++) {
            if (i >= args.length) {
                break;
            }
            if (args[i] != null && !paramTypes[i].isAssignableFrom(args[i].getClass())) {
                args[i] = beanWrapper.convertIfNecessary(args[i], paramTypes[i]);
            }
        }
    }

    public boolean hasMethod(String methodName){
        for (int i = 0; i < methodInfos.size(); i++) {
            if (methodInfos.get(i).getMethod().getName().equals(methodName)) {
                return true;
            }
        }        
        return false;
    }
    
    public int getIndex(String methodName) {
        for (int i = 0; i < methodInfos.size(); i++)
            if (methodInfos.get(i).getMethod().getName().equals(methodName))
                return i;
        throw new IllegalArgumentException("Unable to find non-private method: " + methodName);
    }

    public int getIndex(String methodName, Class... paramTypes) {
        for (int i = 0; i < methodInfos.size(); i++) {
            Method method = methodInfos.get(i).getMethod();
            if (method.getName().equals(methodName) && Arrays.equals(paramTypes, method.getParameterTypes()))
                return i;
        }
        throw new IllegalArgumentException("Unable to find non-private method: " + methodName + " " + Arrays.toString(paramTypes));
    }

    public int getIndex(String methodName, int paramsCount) {
        for (int i = 0; i < methodInfos.size(); i++) {
            Method method = methodInfos.get(i).getMethod();
            if (method.getName().equals(methodName) && method.getParameterTypes().length == paramsCount)
                return i;
        }
        throw new IllegalArgumentException("Unable to find non-private method: " + methodName + " with " + paramsCount + " params.");
    }

    public List<MethodInfo> getMethodInfos() {
        return this.methodInfos;
    }

    static public MethodAccess get(Class type) {
        MethodAccess access = instanceCache.get(type);
        if (access != null) {
            return access;
        }

        String className = type.getName();
        String accessClassName = className + "MethodAccess";
        if (accessClassName.startsWith("java."))
            accessClassName = "reflectasm." + accessClassName;
        Class accessClass;

        AccessClassLoader loader = AccessClassLoader.get(type);
        synchronized (loader) {
            try {
                accessClass = loader.loadClass(accessClassName);
            } catch (ClassNotFoundException ignored) {
                List<MethodInfo> methods = ReflectionUtils.getMethodInfos(type);
                int n = methods.size();
                boolean isInterface = type.isInterface();

                String accessClassNameInternal = accessClassName.replace('.', '/');
                String classNameInternal = className.replace('.', '/');

                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                MethodVisitor mv;
                cw.visit(V1_1, ACC_PUBLIC + ACC_SUPER, accessClassNameInternal, null, "com/harvey/common/core/reflectasm/MethodAccess", null);
                {
                    mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
                    mv.visitCode();
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitMethodInsn(INVOKESPECIAL, "com/harvey/common/core/reflectasm/MethodAccess", "<init>", "()V");
                    mv.visitInsn(RETURN);
                    mv.visitMaxs(0, 0);
                    mv.visitEnd();
                }
                {
                    mv = cw.visitMethod(ACC_PUBLIC + ACC_VARARGS, "invoke", "(Ljava/lang/Object;I[Ljava/lang/Object;)Ljava/lang/Object;", null, null);
                    mv.visitCode();

                    if (!methods.isEmpty()) {
                        mv.visitVarInsn(ALOAD, 1);
                        mv.visitTypeInsn(CHECKCAST, classNameInternal);
                        mv.visitVarInsn(ASTORE, 4);

                        mv.visitVarInsn(ILOAD, 2);
                        Label[] labels = new Label[n];
                        for (int i = 0; i < n; i++)
                            labels[i] = new Label();
                        Label defaultLabel = new Label();
                        mv.visitTableSwitchInsn(0, labels.length - 1, defaultLabel, labels);

                        StringBuilder buffer = new StringBuilder(128);
                        for (int i = 0; i < n; i++) {
                            MethodInfo methodInfo = methods.get(i);
                            mv.visitLabel(labels[i]);
                            if (i == 0)
                                mv.visitFrame(Opcodes.F_APPEND, 1, new Object[] { classNameInternal }, 0, null);
                            else
                                mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                            mv.visitVarInsn(ALOAD, 4);

                            buffer.setLength(0);
                            buffer.append('(');

                            Class[] paramTypes = methodInfo.getParameterTypes();
                            Class returnType = methodInfo.getReturnType();
                            for (int paramIndex = 0; paramIndex < paramTypes.length; paramIndex++) {
                                mv.visitVarInsn(ALOAD, 3);
                                mv.visitIntInsn(BIPUSH, paramIndex);
                                mv.visitInsn(AALOAD);
                                Type paramType = Type.getType(paramTypes[paramIndex]);
                                switch (paramType.getSort()) {
                                case Type.BOOLEAN:
                                    mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
                                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
                                    break;
                                case Type.BYTE:
                                    mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
                                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B");
                                    break;
                                case Type.CHAR:
                                    mv.visitTypeInsn(CHECKCAST, "java/lang/Character");
                                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C");
                                    break;
                                case Type.SHORT:
                                    mv.visitTypeInsn(CHECKCAST, "java/lang/Short");
                                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S");
                                    break;
                                case Type.INT:
                                    mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
                                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
                                    break;
                                case Type.FLOAT:
                                    mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
                                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F");
                                    break;
                                case Type.LONG:
                                    mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
                                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J");
                                    break;
                                case Type.DOUBLE:
                                    mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
                                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D");
                                    break;
                                case Type.ARRAY:
                                    mv.visitTypeInsn(CHECKCAST, paramType.getDescriptor());
                                    break;
                                case Type.OBJECT:
                                    mv.visitTypeInsn(CHECKCAST, paramType.getInternalName());
                                    break;
                                }
                                buffer.append(paramType.getDescriptor());
                            }

                            buffer.append(')');
                            buffer.append(Type.getDescriptor(returnType));
                            int invoke;
                            if (isInterface)
                                invoke = INVOKEINTERFACE;
                            else if (Modifier.isStatic(methodInfo.getMethod().getModifiers()))
                                invoke = INVOKESTATIC;
                            else
                                invoke = INVOKEVIRTUAL;
                            mv.visitMethodInsn(invoke, classNameInternal, methodInfo.getMethod().getName(), buffer.toString());

                            switch (Type.getType(returnType).getSort()) {
                            case Type.VOID:
                                mv.visitInsn(ACONST_NULL);
                                break;
                            case Type.BOOLEAN:
                                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
                                break;
                            case Type.BYTE:
                                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
                                break;
                            case Type.CHAR:
                                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
                                break;
                            case Type.SHORT:
                                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
                                break;
                            case Type.INT:
                                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                                break;
                            case Type.FLOAT:
                                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
                                break;
                            case Type.LONG:
                                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
                                break;
                            case Type.DOUBLE:
                                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
                                break;
                            }

                            mv.visitInsn(ARETURN);
                        }

                        mv.visitLabel(defaultLabel);
                        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                    }
                    mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
                    mv.visitInsn(DUP);
                    mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
                    mv.visitInsn(DUP);
                    mv.visitLdcInsn("Method not found: ");
                    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
                    mv.visitVarInsn(ILOAD, 2);
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
                    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "(Ljava/lang/String;)V");
                    mv.visitInsn(ATHROW);
                    mv.visitMaxs(0, 0);
                    mv.visitEnd();
                }
                cw.visitEnd();
                byte[] data = cw.toByteArray();
                accessClass = loader.defineClass(accessClassName, data);
                try {
                    access = (MethodAccess) accessClass.newInstance();
                    access.methodInfos = methods;
                    instanceCache.put(type, access);
                } catch (Throwable t) {
                    throw new RuntimeException("Error constructing method access class: " + accessClassName, t);
                }
            }
        }
        return access;
    }

    private static void addDeclaredMethodsToList(Class type, ArrayList<Method> methods) {
        Method[] declaredMethods = type.getDeclaredMethods();
        for (int i = 0, n = declaredMethods.length; i < n; i++) {
            Method method = declaredMethods[i];
            int modifiers = method.getModifiers();
            // if (Modifier.isStatic(modifiers)) continue;
            if (Modifier.isPrivate(modifiers))
                continue;
            methods.add(method);
        }
    }

    private static void recursiveAddInterfaceMethodsToList(Class interfaceType, ArrayList<Method> methods) {
        addDeclaredMethodsToList(interfaceType, methods);
        for (Class nextInterface : interfaceType.getInterfaces()) {
            recursiveAddInterfaceMethodsToList(nextInterface, methods);
        }
    }
}
