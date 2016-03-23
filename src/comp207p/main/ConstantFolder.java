package comp207p.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;
import org.apache.bcel.util.InstructionFinder;


public class ConstantFolder
{
	ClassParser parser = null;
	ClassGen gen = null;

	JavaClass original = null;
	JavaClass optimized = null;

	public ConstantFolder(String classFilePath)
	{
		try{
			this.parser = new ClassParser(classFilePath);
			this.original = this.parser.parse();
			this.gen = new ClassGen(this.original);
		} catch(IOException e){
			e.printStackTrace();
		}
	}

    // we rewrite integer constants with 5 :)
    private void optimizeMethod(ClassGen cgen, ConstantPoolGen cpgen, Method method)
    {
        // Get the Code of the method, which is a collection of bytecode instructions
        Code methodCode = method.getCode();

        // Now get the actualy bytecode data in byte array,
        // and use it to initialise an InstructionList
        InstructionList instList = new InstructionList(methodCode.getCode());

        // Initialise a method generator with the original method as the baseline
        MethodGen methodGen = new MethodGen(method.getAccessFlags(), method.getReturnType(), method.getArgumentTypes(), null, method.getName(), cgen.getClassName(), instList, cpgen);

        // InstructionHandle is a wrapper for actual Instructions
        for (InstructionHandle handle : instList.getInstructionHandles())
        {
            // if the instruction inside is iconst
            if (handle.getInstruction() instanceof IADD)
            {
                // insert new one with integer 5, and...
                InstructionHandle prev = handle.getPrev();
                InstructionHandle prev2 = prev.getPrev();
                if(prev.getInstruction() instanceof LDC && prev2.getInstruction() instanceof LDC) {

                    String prevVal = prev.getInstruction().toString(cpgen.getConstantPool());
                    int prevInt = Integer.parseInt(prevVal.substring(4));

                    String prevVal2 = prev2.getInstruction().toString(cpgen.getConstantPool());
                    int prevInt2 = Integer.parseInt(prevVal2.substring(4));

                    instList.insert(handle, new LDC(cgen.getConstantPool().addInteger(prevInt+prevInt2)));
                    try {
                        // delete the old one
                        instList.delete(prev);
                        instList.delete(prev2);
                        instList.delete(handle);

                    } catch (TargetLostException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }

        // setPositions(true) checks whether jump handles
        // are all within the current method
        instList.setPositions(true);

        // set max stack/local
        methodGen.setMaxStack();
        methodGen.setMaxLocals();

        // generate the new method with replaced iconst
        Method newMethod = methodGen.getMethod();
        // replace the method in the original class
        cgen.replaceMethod(method, newMethod);

    }
    private void optimize()
    {
        // load the original class into a class generator
        ClassGen cgen = new ClassGen(original);
        ConstantPoolGen cpgen = cgen.getConstantPool();

        // Do your optimization here
        Method[] methods = cgen.getMethods();
        for (Method m : methods)
        {
            optimizeMethod(cgen, cpgen, m);

        }

        // we generate a new class with modifications
        // and store it in a member variable
        this.optimized = cgen.getJavaClass();
    }
	
	public void write(String optimisedFilePath)
	{
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