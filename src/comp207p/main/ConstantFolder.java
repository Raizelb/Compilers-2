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

    private int getIntValue(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen){
        if (handle.getInstruction() instanceof LDC) {
            return (int) ((LDC) handle.getInstruction()).getValue(cpgen);
        } else if (handle.getInstruction() instanceof ICONST) {
            return (int) ((ICONST) handle.getInstruction()).getValue();
        } else if (handle.getInstruction() instanceof BIPUSH) {
            return (int) ((BIPUSH) handle.getInstruction()).getValue();
        } else if (handle.getInstruction() instanceof SIPUSH) {
            return (int) ((SIPUSH) handle.getInstruction()).getValue();
        } else if (handle.getInstruction() instanceof ILOAD) {
            return loadValue(handle, instList, cpgen);
        }
        System.out.println("Error getIntValue()");
        return 0;
    }

    private int loadValue(InstructionHandle handle, InstructionList instList, ConstantPoolGen cpgen) {
        if (handle.getInstruction() instanceof ILOAD) {
            int test1 = ((ILOAD) handle.getInstruction()).getIndex();
            for (InstructionHandle handle1 : instList.getInstructionHandles()) {
                if (handle1.getInstruction() instanceof ISTORE) {
                    int test2 = ((ISTORE) handle1.getInstruction()).getIndex();
                    //short test3 = ((ISTORE) handle1.getInstruction()).getCanonicalTag();
                    int test3 = 0;
                    if (handle1.getPrev().getInstruction() instanceof LDC) {
                        test3 = (int) ((LDC) handle1.getPrev().getInstruction()).getValue(cpgen);
                    }
                    else if (handle1.getPrev().getInstruction() instanceof SIPUSH) {
                        test3 = (int) ((SIPUSH) handle1.getPrev().getInstruction()).getValue();
                    }
                    else if (handle1.getPrev().getInstruction() instanceof BIPUSH) {
                        test3 = (int) ((BIPUSH) handle1.getPrev().getInstruction()).getValue();
                    }
                    if (test1 == test2) {
                        return test3;
                    }
                }
            }
        }
        System.out.println("Error loadValue()");
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
        // MethodGen methodGen = new MethodGen(method.getAccessFlags(), method.getReturnType(), method.getArgumentTypes(),
        //        null, method.getName(), cgen.getClassName(), instList, cpgen);
        MethodGen methodGen = new MethodGen(method, cgen.getClassName(), cpgen);

        // InstructionHandle is a wrapper for actual Instructions
        for (InstructionHandle handle : instList.getInstructionHandles()) {

            if (handle.getInstruction() instanceof IADD) {
                InstructionHandle prev = handle.getPrev();
                InstructionHandle prev2 = prev.getPrev();

                int prevVal = getIntValue(prev, instList, cpgen);
                int prevVal2 = getIntValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addInteger(prevVal2 + prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof ISUB) {
                InstructionHandle prev = handle.getPrev();
                InstructionHandle prev2 = prev.getPrev();

                int prevVal = getIntValue(prev, instList, cpgen);
                int prevVal2 = getIntValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addInteger(prevVal2 - prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof IMUL) {
                InstructionHandle prev = handle.getPrev();
                InstructionHandle prev2 = prev.getPrev();

                int prevVal = getIntValue(prev, instList, cpgen);
                int prevVal2 = getIntValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addInteger(prevVal2 * prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof IDIV) {
                InstructionHandle prev = handle.getPrev();
                InstructionHandle prev2 = prev.getPrev();

                int prevVal = getIntValue(prev, instList, cpgen);
                int prevVal2 = getIntValue(prev2, instList, cpgen);

                instList.insert(handle, new LDC(cgen.getConstantPool().addInteger(prevVal2 / prevVal)));
                removeInstructions(instList, handle, prev, prev2);
            }

            if (handle.getInstruction() instanceof IREM) {
                InstructionHandle prev = handle.getPrev();
                InstructionHandle prev2 = prev.getPrev();

                int prevVal = getIntValue(prev, instList, cpgen);
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
                InstructionHandle prev2 = prev.getPrev();
                if (prev.getInstruction() instanceof LDC && prev2.getInstruction() instanceof LDC) {

                    float prevVal = (float) ((LDC) prev.getInstruction()).getValue(cpgen);
                    float prevVal2 = (float) ((LDC) prev2.getInstruction()).getValue(cpgen);

                    instList.insert(handle, new LDC(cgen.getConstantPool().addFloat(prevVal2 + prevVal)));
                    removeInstructions(instList, handle, prev, prev2);
                }
            }

            if (handle.getInstruction() instanceof FSUB) {
                InstructionHandle prev = handle.getPrev();
                InstructionHandle prev2 = prev.getPrev();
                if (prev.getInstruction() instanceof LDC && prev2.getInstruction() instanceof LDC) {

                    float prevVal = (float) ((LDC) prev.getInstruction()).getValue(cpgen);
                    float prevVal2 = (float) ((LDC) prev2.getInstruction()).getValue(cpgen);

                    instList.insert(handle, new LDC(cgen.getConstantPool().addFloat(prevVal2 - prevVal)));
                    removeInstructions(instList, handle, prev, prev2);
                }
            }

            if (handle.getInstruction() instanceof FMUL) {
                InstructionHandle prev = handle.getPrev();
                InstructionHandle prev2 = prev.getPrev();
                if (prev.getInstruction() instanceof LDC && prev2.getInstruction() instanceof LDC) {

                    float prevVal = (float) ((LDC) prev.getInstruction()).getValue(cpgen);
                    float prevVal2 = (float) ((LDC) prev2.getInstruction()).getValue(cpgen);

                    instList.insert(handle, new LDC(cgen.getConstantPool().addFloat(prevVal2 * prevVal)));
                    removeInstructions(instList, handle, prev, prev2);
                }
            }

            if (handle.getInstruction() instanceof FDIV) {
                InstructionHandle prev = handle.getPrev();
                InstructionHandle prev2 = prev.getPrev();
                if (prev.getInstruction() instanceof LDC && prev2.getInstruction() instanceof LDC) {

                    float prevVal = (float) ((LDC) prev.getInstruction()).getValue(cpgen);
                    float prevVal2 = (float) ((LDC) prev2.getInstruction()).getValue(cpgen);

                    instList.insert(handle, new LDC(cgen.getConstantPool().addFloat(prevVal2 / prevVal)));
                    removeInstructions(instList, handle, prev, prev2);
                }
            }

            if (handle.getInstruction() instanceof FREM) {
                InstructionHandle prev = handle.getPrev();
                InstructionHandle prev2 = prev.getPrev();
                if (prev.getInstruction() instanceof LDC && prev2.getInstruction() instanceof LDC) {

                    float prevVal = (float) ((LDC) prev.getInstruction()).getValue(cpgen);
                    float prevVal2 = (float) ((LDC) prev2.getInstruction()).getValue(cpgen);

                    instList.insert(handle, new LDC(cgen.getConstantPool().addFloat(prevVal2 % prevVal)));
                    removeInstructions(instList, handle, prev, prev2);
                }
            }

            if (handle.getInstruction() instanceof FNEG) {
                InstructionHandle prev = handle.getPrev();
                if (prev.getInstruction() instanceof LDC) {

                    float prevVal = (float) ((LDC) prev.getInstruction()).getValue(cpgen);

                    instList.insert(handle, new LDC(cgen.getConstantPool().addFloat(-prevVal)));
                    removeInstructions(instList, handle, prev);
                }
            }

            if (handle.getInstruction() instanceof LADD) {
                InstructionHandle prev = handle.getPrev();
                InstructionHandle prev2 = prev.getPrev();
                if (prev.getInstruction() instanceof LDC2_W && prev2.getInstruction() instanceof LDC2_W) {

                    long prevVal = (long) ((LDC2_W) prev.getInstruction()).getValue(cpgen);
                    long prevVal2 = (long) ((LDC2_W) prev2.getInstruction()).getValue(cpgen);

                    instList.insert(handle, new LDC2_W(cgen.getConstantPool().addLong(prevVal2 + prevVal)));
                    removeInstructions(instList, handle, prev, prev2);
                }
            }

            if (handle.getInstruction() instanceof LSUB) {
                InstructionHandle prev = handle.getPrev();
                InstructionHandle prev2 = prev.getPrev();
                if (prev.getInstruction() instanceof LDC2_W && prev2.getInstruction() instanceof LDC2_W) {

                    long prevVal = (long) ((LDC2_W) prev.getInstruction()).getValue(cpgen);
                    long prevVal2 = (long) ((LDC2_W) prev2.getInstruction()).getValue(cpgen);

                    instList.insert(handle, new LDC2_W(cgen.getConstantPool().addLong(prevVal2 - prevVal)));
                    removeInstructions(instList, handle, prev, prev2);
                }
            }

            if (handle.getInstruction() instanceof LMUL) {
                InstructionHandle prev = handle.getPrev();
                InstructionHandle prev2 = prev.getPrev();
                if (prev.getInstruction() instanceof LDC2_W && prev2.getInstruction() instanceof LDC2_W) {

                    long prevVal = (long) ((LDC2_W) prev.getInstruction()).getValue(cpgen);
                    long prevVal2 = (long) ((LDC2_W) prev2.getInstruction()).getValue(cpgen);

                    instList.insert(handle, new LDC2_W(cgen.getConstantPool().addLong(prevVal2 * prevVal)));
                    removeInstructions(instList, handle, prev, prev2);
                }
            }

            if (handle.getInstruction() instanceof LDIV) {
                InstructionHandle prev = handle.getPrev();
                InstructionHandle prev2 = prev.getPrev();
                if (prev.getInstruction() instanceof LDC2_W && prev2.getInstruction() instanceof LDC2_W) {

                    long prevVal = (long) ((LDC2_W) prev.getInstruction()).getValue(cpgen);
                    long prevVal2 = (long) ((LDC2_W) prev2.getInstruction()).getValue(cpgen);

                    instList.insert(handle, new LDC2_W(cgen.getConstantPool().addLong(prevVal2 / prevVal)));
                    removeInstructions(instList, handle, prev, prev2);
                }
            }

            if (handle.getInstruction() instanceof LREM) {
                InstructionHandle prev = handle.getPrev();
                InstructionHandle prev2 = prev.getPrev();
                if (prev.getInstruction() instanceof LDC2_W && prev2.getInstruction() instanceof LDC2_W) {

                    long prevVal = (long) ((LDC2_W) prev.getInstruction()).getValue(cpgen);
                    long prevVal2 = (long) ((LDC2_W) prev2.getInstruction()).getValue(cpgen);

                    instList.insert(handle, new LDC2_W(cgen.getConstantPool().addLong(prevVal2 % prevVal)));
                    removeInstructions(instList, handle, prev, prev2);
                }
            }

            if (handle.getInstruction() instanceof LNEG) {
                InstructionHandle prev = handle.getPrev();
                if (prev.getInstruction() instanceof LDC2_W) {

                    long prevVal = (long) ((LDC2_W) prev.getInstruction()).getValue(cpgen);

                    instList.insert(handle, new LDC2_W(cgen.getConstantPool().addLong(-prevVal)));
                    removeInstructions(instList, handle, prev);
                }
            }

            if (handle.getInstruction() instanceof DADD) {
                InstructionHandle prev = handle.getPrev();
                InstructionHandle prev2 = prev.getPrev();
                if (prev.getInstruction() instanceof LDC2_W && prev2.getInstruction() instanceof LDC2_W) {

                    double prevVal = (double) ((LDC2_W) prev.getInstruction()).getValue(cpgen);
                    double prevVal2 = (double) ((LDC2_W) prev2.getInstruction()).getValue(cpgen);

                    instList.insert(handle, new LDC2_W(cgen.getConstantPool().addDouble(prevVal2 + prevVal)));
                    removeInstructions(instList, handle, prev, prev2);
                }
            }

            if (handle.getInstruction() instanceof DSUB) {
                InstructionHandle prev = handle.getPrev();
                InstructionHandle prev2 = prev.getPrev();
                if (prev.getInstruction() instanceof LDC2_W && prev2.getInstruction() instanceof LDC2_W) {

                    double prevVal = (double) ((LDC2_W) prev.getInstruction()).getValue(cpgen);
                    double prevVal2 = (double) ((LDC2_W) prev2.getInstruction()).getValue(cpgen);

                    instList.insert(handle, new LDC2_W(cgen.getConstantPool().addDouble(prevVal2 - prevVal)));
                    removeInstructions(instList, handle, prev, prev2);
                }
            }

            if (handle.getInstruction() instanceof DMUL) {
                InstructionHandle prev = handle.getPrev();
                InstructionHandle prev2 = prev.getPrev();
                if (prev.getInstruction() instanceof LDC2_W && prev2.getInstruction() instanceof LDC2_W) {

                    double prevVal = (double) ((LDC2_W) prev.getInstruction()).getValue(cpgen);
                    double prevVal2 = (double) ((LDC2_W) prev2.getInstruction()).getValue(cpgen);

                    instList.insert(handle, new LDC2_W(cgen.getConstantPool().addDouble(prevVal2 * prevVal)));
                    removeInstructions(instList, handle, prev, prev2);
                }
            }

            if (handle.getInstruction() instanceof DDIV) {
                InstructionHandle prev = handle.getPrev();
                InstructionHandle prev2 = prev.getPrev();
                if (prev.getInstruction() instanceof LDC2_W && prev2.getInstruction() instanceof LDC2_W) {

                    double prevVal = (double) ((LDC2_W) prev.getInstruction()).getValue(cpgen);
                    double prevVal2 = (double) ((LDC2_W) prev2.getInstruction()).getValue(cpgen);

                    instList.insert(handle, new LDC2_W(cgen.getConstantPool().addDouble(prevVal2 / prevVal)));
                    removeInstructions(instList, handle, prev, prev2);
                }
            }

            if (handle.getInstruction() instanceof DREM) {
                InstructionHandle prev = handle.getPrev();
                InstructionHandle prev2 = prev.getPrev();
                if (prev.getInstruction() instanceof LDC2_W && prev2.getInstruction() instanceof LDC2_W) {

                    double prevVal = (double) ((LDC2_W) prev.getInstruction()).getValue(cpgen);
                    double prevVal2 = (double) ((LDC2_W) prev2.getInstruction()).getValue(cpgen);

                    instList.insert(handle, new LDC2_W(cgen.getConstantPool().addDouble(prevVal2 % prevVal)));
                    removeInstructions(instList, handle, prev, prev2);
                }
            }

            if (handle.getInstruction() instanceof DNEG) {
                InstructionHandle prev = handle.getPrev();
                if (prev.getInstruction() instanceof LDC2_W) {

                    double prevVal = (double) ((LDC2_W) prev.getInstruction()).getValue(cpgen);

                    instList.insert(handle, new LDC2_W(cgen.getConstantPool().addDouble(-prevVal)));
                    removeInstructions(instList, handle, prev);
                }
            }
        }

        methodGen.setInstructionList(instList);

        // setPositions(true) checks whether jump handles
        // are all within the current method
        // instList.setPositions(true);

        // set max stack/local
        // methodGen.setMaxStack();
        // methodGen.setMaxLocals();

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