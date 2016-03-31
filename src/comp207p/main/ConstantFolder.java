package comp207p.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;


public class ConstantFolder {
    ClassParser parser = null;
    ClassGen gen = null;

    JavaClass original = null;
    JavaClass optimized = null;

    public ConstantFolder(String classFilePath) {
        try {
            this.parser = new ClassParser(classFilePath);
            this.original = this.parser.parse();
            this.gen = new ClassGen(this.original);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeInstructions(InstructionList instList, InstructionHandle handle1, InstructionHandle handle2,
                                    InstructionHandle handle3) {
        try {
            // delete the old ones
            instList.delete(handle1);
            instList.delete(handle2);
            instList.delete(handle3);
        } catch (TargetLostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void removeInstructions(InstructionList instList, InstructionHandle handle1, InstructionHandle handle2) {
        try {
            // delete the old ones
            instList.delete(handle1);
            instList.delete(handle2);
        } catch (TargetLostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void removeInstructions(InstructionList instList, InstructionHandle handle) {
        try {
            // delete the old ones
            instList.delete(handle);
        } catch (TargetLostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private int getIntValue(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen) {
        if (handle.getInstruction() instanceof LDC) {
            return (int) ((LDC) handle.getInstruction()).getValue(cpgen);
        } else if (handle.getInstruction() instanceof ICONST) {
            return (int) ((ICONST) handle.getInstruction()).getValue();
        } else if (handle.getInstruction() instanceof BIPUSH) {
            return (int) ((BIPUSH) handle.getInstruction()).getValue();
        } else if (handle.getInstruction() instanceof SIPUSH) {
            return (int) ((SIPUSH) handle.getInstruction()).getValue();
        } else if (handle.getInstruction() instanceof ILOAD) {
            return loadIntValue(handle, instList, cpgen);
        } else if (handle.getInstruction() instanceof F2I) {
            int value = (int) getFloatValue(handle.getPrev(), instList, cpgen);
            removeInstructions(instList, handle.getPrev());
            return value;
        } else if (handle.getInstruction() instanceof L2I) {
            int value = (int) getLongValue(handle.getPrev(), instList, cpgen);
            removeInstructions(instList, handle.getPrev());
            return value;
        } else if (handle.getInstruction() instanceof D2I) {
            int value = (int) getDoubleValue(handle.getPrev(), instList, cpgen);
            removeInstructions(instList, handle.getPrev());
            return value;
        }
        System.out.println("Error getIntValue()");
        return 0;
    }

    private float getFloatValue(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen) {
        if (handle.getInstruction() instanceof LDC) {
            return (float) ((LDC) handle.getInstruction()).getValue(cpgen);
        } else if (handle.getInstruction() instanceof FCONST) {
            return (float) ((FCONST) handle.getInstruction()).getValue();
        } else if (handle.getInstruction() instanceof FLOAD) {
            return loadFloatValue(handle, instList, cpgen);
        } else if (handle.getInstruction() instanceof I2F) {
            float value = (float) getIntValue(handle.getPrev(), instList, cpgen);
            removeInstructions(instList, handle.getPrev());
            return value;
        } else if (handle.getInstruction() instanceof L2F) {
            float value = (float) getLongValue(handle.getPrev(), instList, cpgen);
            removeInstructions(instList, handle.getPrev());
            return value;
        } else if (handle.getInstruction() instanceof D2F) {
            float value = (float) getDoubleValue(handle.getPrev(), instList, cpgen);
            removeInstructions(instList, handle.getPrev());
            return value;
        }
        System.out.println("Error getFloatValue()");
        return 0;
    }

    private long getLongValue(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen) {
        if (handle.getInstruction() instanceof LDC2_W) {
            return (long) ((LDC2_W) handle.getInstruction()).getValue(cpgen);
        } else if (handle.getInstruction() instanceof LCONST) {
            return (long) ((LCONST) handle.getInstruction()).getValue();
        } else if (handle.getInstruction() instanceof LLOAD) {
            return loadLongValue(handle, instList, cpgen);
        } else if (handle.getInstruction() instanceof I2L) {
            long value = (long) getIntValue(handle.getPrev(), instList, cpgen);
            removeInstructions(instList, handle.getPrev());
            return value;
        } else if (handle.getInstruction() instanceof F2L) {
            long value = (long) getFloatValue(handle.getPrev(), instList, cpgen);
            removeInstructions(instList, handle.getPrev());
            return value;
        } else if (handle.getInstruction() instanceof D2L) {
            long value = (long) getDoubleValue(handle.getPrev(), instList, cpgen);
            removeInstructions(instList, handle.getPrev());
            return value;
        }
        System.out.println("Error getLongValue()");
        return 0;
    }

    private double getDoubleValue(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen) {
        if (handle.getInstruction() instanceof LDC2_W) {
            return (double) ((LDC2_W) handle.getInstruction()).getValue(cpgen);
        } else if (handle.getInstruction() instanceof DCONST) {
            return (double) ((DCONST) handle.getInstruction()).getValue();
        } else if (handle.getInstruction() instanceof DLOAD) {
            return loadDoubleValue(handle, instList, cpgen);
        } else if (handle.getInstruction() instanceof I2D) {
            double value = (double) getIntValue(handle.getPrev(), instList, cpgen);
            removeInstructions(instList, handle.getPrev());
            return value;
        } else if (handle.getInstruction() instanceof F2D) {
            double value = (double) getFloatValue(handle.getPrev(), instList, cpgen);
            removeInstructions(instList, handle.getPrev());
            return value;
        } else if (handle.getInstruction() instanceof L2D) {
            double value = (double) getLongValue(handle.getPrev(), instList, cpgen);
            removeInstructions(instList, handle.getPrev());
            return value;
        }
        System.out.println("Error getDoubleValue()");
        return 0;
    }

    private int loadIntValue(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen) {
        if (handle.getInstruction() instanceof ILOAD) {
            int index = ((ILOAD) handle.getInstruction()).getIndex();
            InstructionHandle handle1 = handle.getPrev();
            while (handle1.getPrev() != null) {
                if(handle1.getInstruction() instanceof ISTORE) {
                    int value = 0;
                    if (handle1.getPrev().getInstruction() instanceof LDC) {
                        value = (int) ((LDC) handle1.getPrev().getInstruction()).getValue(cpgen);
                    } else if (handle1.getPrev().getInstruction() instanceof SIPUSH) {
                        value = (int) ((SIPUSH) handle1.getPrev().getInstruction()).getValue();
                    } else if (handle1.getPrev().getInstruction() instanceof BIPUSH) {
                        value = (int) ((BIPUSH) handle1.getPrev().getInstruction()).getValue();
                    } else if (handle1.getPrev().getInstruction() instanceof ICONST) {
                        value = (int) ((ICONST) handle1.getPrev().getInstruction()).getValue();
                    }
                    if (index == ((ISTORE) handle1.getInstruction()).getIndex()) {
                        return value;
                    }
                }
                handle1 = handle1.getPrev();
            }
        }
        
        System.out.println("Error loadIntValue()");
        return 0;
    }

    private float loadFloatValue(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen) {
        if (handle.getInstruction() instanceof FLOAD) {
            int test1 = ((FLOAD) handle.getInstruction()).getIndex();
            for (InstructionHandle handle1 : instList.getInstructionHandles()) {
                if (handle1.getInstruction() instanceof FSTORE) {
                    int test2 = ((FSTORE) handle1.getInstruction()).getIndex();
                    float test3 = 0;
                    if (handle1.getPrev().getInstruction() instanceof LDC) {
                        test3 = (float) ((LDC) handle1.getPrev().getInstruction()).getValue(cpgen);
                    } else if (handle1.getPrev().getInstruction() instanceof FCONST) {
                        test3 = (float) ((FCONST) handle1.getPrev().getInstruction()).getValue();
                    }
                    if (test1 == test2) {
                        return test3;
                    }
                }
            }
        }
        System.out.println("Error loadFloatValue()");
        return 0;
    }

    private long loadLongValue(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen) {
        if (handle.getInstruction() instanceof LLOAD) {
            int test1 = ((LLOAD) handle.getInstruction()).getIndex();
            for (InstructionHandle handle1 : instList.getInstructionHandles()) {
                if (handle1.getInstruction() instanceof LSTORE) {
                    int test2 = ((LSTORE) handle1.getInstruction()).getIndex();
                    long test3 = 0;
                    if (handle1.getPrev().getInstruction() instanceof LDC2_W) {
                        test3 = (long) ((LDC2_W) handle1.getPrev().getInstruction()).getValue(cpgen);
                    } else if (handle1.getPrev().getInstruction() instanceof LCONST) {
                        test3 = (long) ((LCONST) handle1.getPrev().getInstruction()).getValue();
                    }
                    if (test1 == test2) {
                        return test3;
                    }
                }
            }
        }
        System.out.println("Error loadLongValue()");
        return 0;
    }

    private double loadDoubleValue(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen) {
        if (handle.getInstruction() instanceof DLOAD) {
            int test1 = ((DLOAD) handle.getInstruction()).getIndex();
            for (InstructionHandle handle1 : instList.getInstructionHandles()) {
                if (handle1.getInstruction() instanceof DSTORE) {
                    int test2 = ((DSTORE) handle1.getInstruction()).getIndex();
                    double test3 = 0;
                    if (handle1.getPrev().getInstruction() instanceof LDC2_W) {
                        test3 = (double) ((LDC2_W) handle1.getPrev().getInstruction()).getValue(cpgen);
                    } else if (handle1.getPrev().getInstruction() instanceof DCONST) {
                        test3 = (int) ((DCONST) handle1.getPrev().getInstruction()).getValue();
                    }
                    if (test1 == test2) {
                        return test3;
                    }
                }
            }
        }
        System.out.println("Error loadDoubleValue()");
        return 0;
    }

    // we rewrite integer constants with 5 :)
    private void optimizeMethod(ClassGen cgen, ConstantPoolGen cpgen, Method method) {
        // Get the Code of the method, which is a collection of bytecode instructions
        Code methodCode = method.getCode();

        // Now get the actualy bytecode data in byte array,
        // and use it to initialise an InstructionList
        InstructionList instList = new InstructionList(methodCode.getCode());

        // Initialise a method generator with the original method as the baseline
        MethodGen methodGen = new MethodGen(method.getAccessFlags(), method.getReturnType(), method.getArgumentTypes(),
                null, method.getName(), cgen.getClassName(), instList, cpgen);
        //MethodGen methodGen = new MethodGen(method, cgen.getClassName(), cpgen);

        // InstructionHandle is a wrapper for actual Instructions
        for (InstructionHandle handle : instList.getInstructionHandles()) {

            if (handle.getInstruction() instanceof IADD) {
                InstructionHandle prev = handle.getPrev();
                int prevVal = getIntValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                int prevVal2 = getIntValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addInteger(prevVal2 + prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof ISUB) {
                InstructionHandle prev = handle.getPrev();
                int prevVal = getIntValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                int prevVal2 = getIntValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addInteger(prevVal2 - prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof IMUL) {
                InstructionHandle prev = handle.getPrev();
                int prevVal = getIntValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                int prevVal2 = getIntValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addInteger(prevVal2 * prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof IDIV) {
                InstructionHandle prev = handle.getPrev();
                int prevVal = getIntValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                int prevVal2 = getIntValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addInteger(prevVal2 / prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof IREM) {
                InstructionHandle prev = handle.getPrev();
                int prevVal = getIntValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                int prevVal2 = getIntValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addInteger(prevVal2 % prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof INEG) {
                InstructionHandle prev = handle.getPrev();
                int prevVal = getIntValue(prev, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addInteger(-prevVal)));
                removeInstructions(instList, handle, prev);
            }

            if (handle.getInstruction() instanceof FADD) {
                InstructionHandle prev = handle.getPrev();
                float prevVal = getFloatValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                float prevVal2 = getFloatValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addFloat(prevVal2 + prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof FSUB) {
                InstructionHandle prev = handle.getPrev();
                float prevVal = getFloatValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                float prevVal2 = getFloatValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addFloat(prevVal2 - prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof FMUL) {
                InstructionHandle prev = handle.getPrev();
                float prevVal = getFloatValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                float prevVal2 = getFloatValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addFloat(prevVal2 * prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof FDIV) {
                InstructionHandle prev = handle.getPrev();
                float prevVal = getFloatValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                float prevVal2 = getFloatValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addFloat(prevVal2 / prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof FREM) {
                InstructionHandle prev = handle.getPrev();
                float prevVal = getFloatValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                float prevVal2 = getFloatValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addFloat(prevVal2 % prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof FNEG) {
                InstructionHandle prev = handle.getPrev();
                float prevVal = getFloatValue(prev, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addFloat(-prevVal)));
                removeInstructions(instList, handle, prev);
            }

            if (handle.getInstruction() instanceof LADD) {
                InstructionHandle prev = handle.getPrev();
                long prevVal = getLongValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                long prevVal2 = getLongValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC2_W(cgen.getConstantPool().addLong(prevVal2 + prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof LSUB) {
                InstructionHandle prev = handle.getPrev();
                long prevVal = getLongValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                long prevVal2 = getLongValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC2_W(cgen.getConstantPool().addLong(prevVal2 - prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof LMUL) {
                InstructionHandle prev = handle.getPrev();
                long prevVal = getLongValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                long prevVal2 = getLongValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC2_W(cgen.getConstantPool().addLong(prevVal2 * prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof LDIV) {
                InstructionHandle prev = handle.getPrev();
                long prevVal = getLongValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                long prevVal2 = getLongValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC2_W(cgen.getConstantPool().addLong(prevVal2 / prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof LREM) {
                InstructionHandle prev = handle.getPrev();
                long prevVal = getLongValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                long prevVal2 = getLongValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC2_W(cgen.getConstantPool().addLong(prevVal2 % prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof LNEG) {
                InstructionHandle prev = handle.getPrev();
                long prevVal = getLongValue(prev, instList, cpgen);

                instList.insert(handle, new LDC2_W(cgen.getConstantPool().addLong(-prevVal)));
                removeInstructions(instList, handle, prev);
            }

            if (handle.getInstruction() instanceof DADD) {
                InstructionHandle prev = handle.getPrev();
                double prevVal = getDoubleValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                double prevVal2 = getDoubleValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC2_W(cgen.getConstantPool().addDouble(prevVal2 + prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof DSUB) {
                InstructionHandle prev = handle.getPrev();
                double prevVal = getDoubleValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                double prevVal2 = getDoubleValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC2_W(cgen.getConstantPool().addDouble(prevVal2 - prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof DMUL) {
                InstructionHandle prev = handle.getPrev();
                double prevVal = getDoubleValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                double prevVal2 = getDoubleValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC2_W(cgen.getConstantPool().addDouble(prevVal2 * prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof DDIV) {
                InstructionHandle prev = handle.getPrev();
                double prevVal = getDoubleValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                double prevVal2 = getDoubleValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC2_W(cgen.getConstantPool().addDouble(prevVal2 / prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof DREM) {
                InstructionHandle prev = handle.getPrev();
                double prevVal = getDoubleValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                double prevVal2 = getDoubleValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC2_W(cgen.getConstantPool().addDouble(prevVal2 % prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof DNEG) {
                InstructionHandle prev = handle.getPrev();
                double prevVal = getDoubleValue(prev, instList, cpgen);

                instList.insert(handle, new LDC2_W(cgen.getConstantPool().addDouble(-prevVal)));
                removeInstructions(instList, handle, prev);
            }

            if (handle.getInstruction() instanceof IF_ICMPEQ) {
                InstructionHandle prev = handle.getPrev();
                int prevVal = getIntValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                int prevVal2 = getIntValue(prev2, instList, cpgen);

                if(prevVal2 == prevVal) {
                    instList.insert(handle, new ICONST (0));
                }
                else {
                    instList.insert(handle, new ICONST (1));
                }

                InstructionHandle next = handle.getNext();
                InstructionHandle next1 = next.getNext();
                InstructionHandle next2 = next1.getNext();

                removeInstructions(instList, handle, prev, prev2);
                removeInstructions(instList, next, next1, next2);
            }

            if (handle.getInstruction() instanceof IF_ICMPGE) {
                InstructionHandle prev = handle.getPrev();
                int prevVal = getIntValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                int prevVal2 = getIntValue(prev2, instList, cpgen);

                if(prevVal2 >= prevVal) {
                    instList.insert(handle, new ICONST (0));
                }
                else {
                    instList.insert(handle, new ICONST (1));
                }

                InstructionHandle next = handle.getNext();
                InstructionHandle next1 = next.getNext();
                InstructionHandle next2 = next1.getNext();

                removeInstructions(instList, handle, prev, prev2);
                removeInstructions(instList, next, next1, next2);
            }

            if (handle.getInstruction() instanceof IF_ICMPGT) {
                InstructionHandle prev = handle.getPrev();
                int prevVal = getIntValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                int prevVal2 = getIntValue(prev2, instList, cpgen);

                if(prevVal2 > prevVal) {
                    instList.insert(handle, new ICONST (0));
                }
                else {
                    instList.insert(handle, new ICONST (1));
                }

                InstructionHandle next = handle.getNext();
                InstructionHandle next1 = next.getNext();
                InstructionHandle next2 = next1.getNext();

                removeInstructions(instList, handle, prev, prev2);
                removeInstructions(instList, next, next1, next2);
            }

            if (handle.getInstruction() instanceof IF_ICMPLE) {
                InstructionHandle prev = handle.getPrev();
                int prevVal = getIntValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                int prevVal2 = getIntValue(prev2, instList, cpgen);

                if(prevVal2 <= prevVal) {
                    instList.insert(handle, new ICONST (0));
                }
                else {
                    instList.insert(handle, new ICONST (1));
                }

                InstructionHandle next = handle.getNext();
                InstructionHandle next1 = next.getNext();
                InstructionHandle next2 = next1.getNext();

                removeInstructions(instList, handle, prev, prev2);
                removeInstructions(instList, next, next1, next2);
            }

            if (handle.getInstruction() instanceof IF_ICMPLT) {
                InstructionHandle prev = handle.getPrev();
                int prevVal = getIntValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                int prevVal2 = getIntValue(prev2, instList, cpgen);

                if(prevVal2 < prevVal) {
                    instList.insert(handle, new ICONST (0));
                }
                else {
                    instList.insert(handle, new ICONST (1));
                }

                InstructionHandle next = handle.getNext();
                InstructionHandle next1 = next.getNext();
                InstructionHandle next2 = next1.getNext();

                removeInstructions(instList, handle, prev, prev2);
                removeInstructions(instList, next, next1, next2);
            }

            if (handle.getInstruction() instanceof IF_ICMPNE) {
                InstructionHandle prev = handle.getPrev();
                int prevVal = getIntValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                int prevVal2 = getIntValue(prev2, instList, cpgen);

                if(prevVal2 != prevVal) {
                    instList.insert(handle, new ICONST (0));
                }
                else {
                    instList.insert(handle, new ICONST (1));
                }

                InstructionHandle next = handle.getNext();
                InstructionHandle next1 = next.getNext();
                InstructionHandle next2 = next1.getNext();

                removeInstructions(instList, handle, prev, prev2);
                removeInstructions(instList, next, next1, next2);
            }

            if(handle.getInstruction() instanceof LCMP) {
                InstructionHandle prev = handle.getPrev();
                long prevVal = getLongValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                long prevVal2 = getLongValue(prev2, instList, cpgen);

                if(prevVal2 > prevVal) {
                    instList.insert(handle, new ICONST (1));
                } else if(prevVal2 == prevVal) {
                    instList.insert(handle, new ICONST (0));
                } else if(prevVal2 < prevVal) {
                    instList.insert(handle, new ICONST (-1));
                }

                removeInstructions(instList, handle, prev, prev2);
            }

            if(handle.getInstruction() instanceof FCMPG) {
                InstructionHandle prev = handle.getPrev();
                float prevVal = getFloatValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                float prevVal2 = getFloatValue(prev2, instList, cpgen);

                if(prevVal2 > prevVal) {
                    instList.insert(handle, new ICONST (0));
                } else {
                    instList.insert(handle, new ICONST (1));
                }

                removeInstructions(instList, handle, prev, prev2);
            }

            if(handle.getInstruction() instanceof FCMPL) {
                InstructionHandle prev = handle.getPrev();
                float prevVal = getFloatValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                float prevVal2 = getFloatValue(prev2, instList, cpgen);

                if(prevVal2 < prevVal) {
                    instList.insert(handle, new ICONST (0));
                } else {
                    instList.insert(handle, new ICONST (1));
                }

                removeInstructions(instList, handle, prev, prev2);
            }

            if(handle.getInstruction() instanceof DCMPG) {
                InstructionHandle prev = handle.getPrev();
                double prevVal = getDoubleValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                double prevVal2 = getDoubleValue(prev2, instList, cpgen);

                if(prevVal2 > prevVal) {
                    instList.insert(handle, new ICONST (0));
                } else {
                    instList.insert(handle, new ICONST (1));
                }

                removeInstructions(instList, handle, prev, prev2);
            }

            if(handle.getInstruction() instanceof DCMPL) {
                InstructionHandle prev = handle.getPrev();
                double prevVal = getDoubleValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                double prevVal2 = getDoubleValue(prev2, instList, cpgen);

                if(prevVal2 < prevVal) {
                    instList.insert(handle, new ICONST (0));
                } else {
                    instList.insert(handle, new ICONST (1));
                }

                removeInstructions(instList, handle, prev, prev2);
            }

            if(handle.getInstruction() instanceof IFEQ) {
                InstructionHandle prev = handle.getPrev();
                int prevVal = getIntValue(prev, instList, cpgen);

                if(prevVal == 0) {
                    instList.insert(handle, new ICONST (0));
                } else {
                    instList.insert(handle, new ICONST (1));
                }

                InstructionHandle next = handle.getNext();
                InstructionHandle next1 = next.getNext();
                InstructionHandle next2 = next1.getNext();

                removeInstructions(instList, handle, prev);
                removeInstructions(instList, next, next1, next2);
            }

            if(handle.getInstruction() instanceof IFGE) {
                InstructionHandle prev = handle.getPrev();
                int prevVal = getIntValue(prev, instList, cpgen);

                if(prevVal >= 0) {
                    instList.insert(handle, new ICONST (0));
                } else {
                    instList.insert(handle, new ICONST (1));
                }

                InstructionHandle next = handle.getNext();
                InstructionHandle next1 = next.getNext();
                InstructionHandle next2 = next1.getNext();

                removeInstructions(instList, handle, prev);
                removeInstructions(instList, next, next1, next2);
            }

            if(handle.getInstruction() instanceof IFGT) {
                InstructionHandle prev = handle.getPrev();
                int prevVal = getIntValue(prev, instList, cpgen);

                if(prevVal > 0) {
                    instList.insert(handle, new ICONST (0));
                } else {
                    instList.insert(handle, new ICONST (1));
                }

                InstructionHandle next = handle.getNext();
                InstructionHandle next1 = next.getNext();
                InstructionHandle next2 = next1.getNext();

                removeInstructions(instList, handle, prev);
                removeInstructions(instList, next, next1, next2);
            }

            if(handle.getInstruction() instanceof IFLE) {
                InstructionHandle prev = handle.getPrev();
                int prevVal = getIntValue(prev, instList, cpgen);

                if(prevVal <= 0) {
                    instList.insert(handle, new ICONST (0));
                } else {
                    instList.insert(handle, new ICONST (1));
                }

                InstructionHandle next = handle.getNext();
                InstructionHandle next1 = next.getNext();
                InstructionHandle next2 = next1.getNext();

                removeInstructions(instList, handle, prev);
                removeInstructions(instList, next, next1, next2);
            }

            if(handle.getInstruction() instanceof IFLT) {
                InstructionHandle prev = handle.getPrev();
                int prevVal = getIntValue(prev, instList, cpgen);

                if(prevVal < 0) {
                    instList.insert(handle, new ICONST (0));
                } else {
                    instList.insert(handle, new ICONST (1));
                }

                InstructionHandle next = handle.getNext();
                InstructionHandle next1 = next.getNext();
                InstructionHandle next2 = next1.getNext();

                removeInstructions(instList, handle, prev);
                removeInstructions(instList, next, next1, next2);
            }

            if(handle.getInstruction() instanceof IFNE) {
                InstructionHandle prev = handle.getPrev();
                int prevVal = getIntValue(prev, instList, cpgen);

                if(prevVal != 0) {
                    instList.insert(handle, new ICONST (0));
                } else {
                    instList.insert(handle, new ICONST (1));
                }

                InstructionHandle next = handle.getNext();
                InstructionHandle next1 = next.getNext();
                InstructionHandle next2 = next1.getNext();

                removeInstructions(instList, handle, prev);
                removeInstructions(instList, next, next1, next2);
            }
        }

        methodGen.setInstructionList(instList);

        // setPositions(true) checks whether jump handles
        // are all within the current method
        instList.setPositions(true);

        // set max stack/local
        methodGen.setMaxStack();
        methodGen.setMaxLocals();

        // remove local variable table
        methodGen.removeLocalVariables();


        // generate the new method with replaced instList
        Method newMethod = methodGen.getMethod();
        // replace the method in the original class
        cgen.replaceMethod(method, newMethod);
    }

    private void optimize() {
        // load the original class into a class generator
        ClassGen cgen = new ClassGen(original);
        ConstantPoolGen cpgen = cgen.getConstantPool();

        // Do your optimization here
        Method[] methods = cgen.getMethods();
        for (Method m : methods) {
            optimizeMethod(cgen, cpgen, m);
        }
        gen = cgen;

        // we generate a new class with modifications
        // and store it in a member variable
        this.optimized = gen.getJavaClass();
    }

    public void write(String optimisedFilePath) {
        this.optimize();

        try {
            FileOutputStream out = new FileOutputStream(new File(optimisedFilePath));
            this.optimized.dump(out);
        } catch (FileNotFoundException e) {
            // Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // Auto-generated catch block
            e.printStackTrace();
        }
    }
}