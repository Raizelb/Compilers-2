package comp207p.main;

import org.apache.bcel.classfile.*;
import org.apache.bcel.generic.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;


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
            instList.delete(handle1);
            instList.delete(handle2);
            instList.delete(handle3);
        } catch (TargetLostException e) {
            e.printStackTrace();
        }
    }

    private void removeInstructions(InstructionList instList, InstructionHandle handle1, InstructionHandle handle2) {
        try {
            instList.delete(handle1);
            instList.delete(handle2);
        } catch (TargetLostException e) {
            e.printStackTrace();
        }
    }

    private void removeInstructions(InstructionList instList, InstructionHandle handle) {
        try {
            instList.delete(handle);
        } catch (TargetLostException e) {
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

    private boolean checkLoopModification(InstructionHandle handle) {
        //check if trying to load a value that is going to be modified in current for/while loop (shouldn't be folded)
        if (handle.getInstruction() instanceof ILOAD) {
            int index = ((ILOAD) handle.getInstruction()).getIndex();
            int position = handle.getPosition();
            InstructionHandle target = null;

            InstructionHandle handle1 = handle;
            boolean modifiedBeforeGoto = false;
            while (handle1 != null) {
                if (handle1.getInstruction() instanceof GOTO) {
                    InstructionHandle targetLocal = ((GOTO) handle1.getInstruction()).getTarget();
                    int targetPos = targetLocal.getPosition();
                    if (modifiedBeforeGoto && targetPos <= position) {
                        return true;
                    }
                    if (targetPos < position && (target == null || target.getPosition() > targetPos)) {
                        target = targetLocal;
                    }
                }
                if (handle1.getInstruction() instanceof ISTORE && ((ISTORE) handle1.getInstruction()).getIndex() == index) {
                    modifiedBeforeGoto = true;
                }
                if (handle1.getInstruction() instanceof IINC && ((IINC) handle1.getInstruction()).getIndex() == index) {
                    modifiedBeforeGoto = true;
                }
                handle1 = handle1.getNext();
            }

            // checks within the loop before the load instruction's position
            if(target == null) { return false; }

            handle1 = target;
            while (handle1.getPosition() < position) {
                if (handle1.getInstruction() instanceof ISTORE && ((ISTORE) handle1.getInstruction()).getIndex() == index) {
                    return true;
                }
                if (handle1.getInstruction() instanceof IINC && ((IINC) handle1.getInstruction()).getIndex() == index) {
                    return true;
                }
                handle1 = handle1.getNext();
            }
        }
        return false;
    }

    private int loadIntValue(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen) {
        if (handle.getInstruction() instanceof ILOAD) {
            int index = ((ILOAD) handle.getInstruction()).getIndex();
            int increments = 0;

            InstructionHandle handle1 = handle;
            while (handle1 != null) {
                if (handle1.getInstruction() instanceof ISTORE && index == ((ISTORE) handle1.getInstruction()).getIndex()) {
                    return getIntValue(handle1.getPrev(), instList, cpgen) + increments;
                }
                if (handle1.getInstruction() instanceof IINC && ((IINC) handle1.getInstruction()).getIndex() == index) {
                    increments += ((IINC) handle1.getInstruction()).getIncrement();
                }
                handle1 = handle1.getPrev();
            }
        }

        System.out.println("Error loadIntValue()");
        return 0;
    }

    private float loadFloatValue(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen) {
        if (handle.getInstruction() instanceof FLOAD) {
            int index = ((FLOAD) handle.getInstruction()).getIndex();
            InstructionHandle handle1 = handle.getPrev();
            while (handle1 != null) {
                if (handle1.getInstruction() instanceof FSTORE && index == ((FSTORE) handle1.getInstruction()).getIndex()) {
                    return getFloatValue(handle1.getPrev(), instList, cpgen);
                }
                handle1 = handle1.getPrev();
            }
        }

        System.out.println("Error loadFloatValue()");
        return 0;
    }

    private long loadLongValue(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen) {
        if (handle.getInstruction() instanceof LLOAD) {
            int index = ((LLOAD) handle.getInstruction()).getIndex();
            InstructionHandle handle1 = handle.getPrev();
            while (handle1 != null) {
                if (handle1.getInstruction() instanceof LSTORE && index == ((LSTORE) handle1.getInstruction()).getIndex()) {
                    return getLongValue(handle1.getPrev(), instList, cpgen);
                }
                handle1 = handle1.getPrev();
            }
        }

        System.out.println("Error loadLongValue()");
        return 0;
    }

    private double loadDoubleValue(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen) {
        if (handle.getInstruction() instanceof DLOAD) {
            int index = ((DLOAD) handle.getInstruction()).getIndex();
            InstructionHandle handle1 = handle.getPrev();
            while (handle1 != null) {
                if (handle1.getInstruction() instanceof DSTORE && index == ((DSTORE) handle1.getInstruction()).getIndex()) {
                    return getDoubleValue(handle1.getPrev(), instList, cpgen);
                }
                handle1 = handle1.getPrev();
            }
        }

        System.out.println("Error loadDoubleValue()");
        return 0;
    }

    private void optimizeMethod(ClassGen cgen, ConstantPoolGen cpgen, Method method) {
        // Get the Code of the method, which is a collection of bytecode instructions
        Code methodCode = method.getCode();

        // Now get the actual bytecode data in byte array,
        // and use it to initialise an InstructionList
        InstructionList instList = new InstructionList(methodCode.getCode());

        // Initialise a method generator with the original method as the baseline
        MethodGen methodGen = new MethodGen(method, cgen.getClassName(), cpgen);

        // InstructionHandle is a wrapper for actual Instructions
        for (InstructionHandle handle : instList.getInstructionHandles()) {

            if (handle.getInstruction() instanceof IADD) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                int prevVal = getIntValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
                int prevVal2 = getIntValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addInteger(prevVal2 + prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof ISUB) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                int prevVal = getIntValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
                int prevVal2 = getIntValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addInteger(prevVal2 - prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof IMUL) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                int prevVal = getIntValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
                int prevVal2 = getIntValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addInteger(prevVal2 * prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof IDIV) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                int prevVal = getIntValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
                int prevVal2 = getIntValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addInteger(prevVal2 / prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof IREM) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                int prevVal = getIntValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
                int prevVal2 = getIntValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addInteger(prevVal2 % prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof INEG) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                int prevVal = getIntValue(prev, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addInteger(-prevVal)));
                removeInstructions(instList, handle, prev);
            }

            if (handle.getInstruction() instanceof FADD) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                float prevVal = getFloatValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
                float prevVal2 = getFloatValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addFloat(prevVal2 + prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof FSUB) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                float prevVal = getFloatValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
                float prevVal2 = getFloatValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addFloat(prevVal2 - prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof FMUL) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                float prevVal = getFloatValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
                float prevVal2 = getFloatValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addFloat(prevVal2 * prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof FDIV) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                float prevVal = getFloatValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
                float prevVal2 = getFloatValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addFloat(prevVal2 / prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof FREM) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                float prevVal = getFloatValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
                float prevVal2 = getFloatValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addFloat(prevVal2 % prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof FNEG) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                float prevVal = getFloatValue(prev, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addFloat(-prevVal)));
                removeInstructions(instList, handle, prev);
            }

            if (handle.getInstruction() instanceof LADD) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                long prevVal = getLongValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
                long prevVal2 = getLongValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC2_W(cgen.getConstantPool().addLong(prevVal2 + prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof LSUB) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                long prevVal = getLongValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
                long prevVal2 = getLongValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC2_W(cgen.getConstantPool().addLong(prevVal2 - prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof LMUL) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                long prevVal = getLongValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
                long prevVal2 = getLongValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC2_W(cgen.getConstantPool().addLong(prevVal2 * prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof LDIV) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                long prevVal = getLongValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
                long prevVal2 = getLongValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC2_W(cgen.getConstantPool().addLong(prevVal2 / prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof LREM) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                long prevVal = getLongValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
                long prevVal2 = getLongValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC2_W(cgen.getConstantPool().addLong(prevVal2 % prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof LNEG) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                long prevVal = getLongValue(prev, instList, cpgen);

                instList.insert(handle, new LDC2_W(cgen.getConstantPool().addLong(-prevVal)));
                removeInstructions(instList, handle, prev);
            }

            if (handle.getInstruction() instanceof DADD) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                double prevVal = getDoubleValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
                double prevVal2 = getDoubleValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC2_W(cgen.getConstantPool().addDouble(prevVal2 + prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof DSUB) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                double prevVal = getDoubleValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
                double prevVal2 = getDoubleValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC2_W(cgen.getConstantPool().addDouble(prevVal2 - prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof DMUL) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                double prevVal = getDoubleValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
                double prevVal2 = getDoubleValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC2_W(cgen.getConstantPool().addDouble(prevVal2 * prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof DDIV) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                double prevVal = getDoubleValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
                double prevVal2 = getDoubleValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC2_W(cgen.getConstantPool().addDouble(prevVal2 / prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof DREM) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                double prevVal = getDoubleValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
                double prevVal2 = getDoubleValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC2_W(cgen.getConstantPool().addDouble(prevVal2 % prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof DNEG) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                double prevVal = getDoubleValue(prev, instList, cpgen);

                instList.insert(handle, new LDC2_W(cgen.getConstantPool().addDouble(-prevVal)));
                removeInstructions(instList, handle, prev);
            }

            if(handle.getInstruction() instanceof LCMP) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                long prevVal = getLongValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
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
                if(checkLoopModification(prev)) { continue; }
                float prevVal = getFloatValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
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
                if(checkLoopModification(prev)) { continue; }
                float prevVal = getFloatValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
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
                if(checkLoopModification(prev)) { continue; }
                double prevVal = getDoubleValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
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
                if(checkLoopModification(prev)) { continue; }
                double prevVal = getDoubleValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
                double prevVal2 = getDoubleValue(prev2, instList, cpgen);

                if(prevVal2 < prevVal) {
                    instList.insert(handle, new ICONST (0));
                } else {
                    instList.insert(handle, new ICONST (1));
                }

                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof IF_ICMPEQ || handle.getInstruction() instanceof IF_ICMPGE ||
                    handle.getInstruction() instanceof IF_ICMPGT || handle.getInstruction() instanceof IF_ICMPLE ||
                    handle.getInstruction() instanceof IF_ICMPLT || handle.getInstruction() instanceof IF_ICMPNE) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                int prevVal = getIntValue(prev, instList, cpgen);

                InstructionHandle prev2 = prev.getPrev();
                if(checkLoopModification(prev2)) { continue; }
                int prevVal2 = getIntValue(prev2, instList, cpgen);

                instList.insert(prev, new LDC(cgen.getConstantPool().addInteger(prevVal)));
                instList.insert(prev2, new LDC(cgen.getConstantPool().addInteger(prevVal2)));
                removeInstructions(instList, prev, prev2);
            }

            if (handle.getInstruction() instanceof IFEQ || handle.getInstruction() instanceof IFGE ||
                    handle.getInstruction() instanceof IFGT || handle.getInstruction() instanceof IFLE ||
                    handle.getInstruction() instanceof IFLT || handle.getInstruction() instanceof IFNE) {
                InstructionHandle prev = handle.getPrev();
                if(checkLoopModification(prev)) { continue; }
                int prevVal = getIntValue(prev, instList, cpgen);

                instList.insert(prev, new LDC(cgen.getConstantPool().addInteger(prevVal)));
                removeInstructions(instList, prev);
            }
        }

        // setPositions(true) checks whether jump handles are all within the current method
        instList.setPositions(true);

        // Get the StackMapTable
        Attribute[] attributes = methodGen.getCodeAttributes();
        StackMapTable smt = null;
        for (Attribute attribute : attributes) {
            if(attribute instanceof StackMapTable) {
                smt = (StackMapTable) attribute;
            }
        }

        // Get the StackMapTableEntry list
        StackMapTableEntry[] smtearray = null;
        if(smt != null) {
            smtearray = smt.getStackMapTable();
        }

        // Update delta offsets in StackMapTable
        if(smtearray != null) {
            ArrayList<Integer> targets = new ArrayList<>();
            for (InstructionHandle handle : instList.getInstructionHandles()) {
                if(handle.getInstruction() instanceof BranchInstruction) {
                    InstructionHandle target = ((BranchInstruction) handle.getInstruction()).getTarget();
                    targets.add(target.getPosition());
                }
            }
            Collections.sort(targets);

            int prev = -1;
            for (int i = 0; i < targets.size(); i++) {
                String smte = smtearray[i].toString();
                if(smte.startsWith("(SAME_LOCALS_1_STACK")) {
                    smtearray[i] = new StackMapTableEntry(targets.get(i) - prev + 63, targets.get(i) - prev - 1,
                            smtearray[i].getTypesOfLocals(), smtearray[i].getTypesOfStackItems(), smtearray[i].getConstantPool());
                } else if(smte.startsWith("(SAME")) {
                    smtearray[i] = new StackMapTableEntry(targets.get(i) - prev - 1, targets.get(i) - prev - 1,
                            smtearray[i].getTypesOfLocals(), smtearray[i].getTypesOfStackItems(), smtearray[i].getConstantPool());
                } else {
                    smtearray[i].setByteCodeOffsetDelta(targets.get(i) - prev - 1);
                }
                prev = targets.get(i);
            }
        }

        // set edited instruction list
        methodGen.setInstructionList(instList);

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
        System.out.println("Optimising " + cgen.getClassName());

        Method[] methods = cgen.getMethods();
        for (Method m : methods) {
            System.out.println("Optimising " + m.getName());
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