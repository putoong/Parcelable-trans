package spoon.examples;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.os.ChkptRstrAnnotation.States;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteCallbackList;
import spoon.processing.AbstractAnnotationProcessor;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtCodeSnippetExpression;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.code.CtComment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtFor;
import spoon.reflect.code.CtStatement;
import spoon.reflect.declaration.CtAnnotation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtField;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtParameter;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.factory.Factory;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.support.compiler.jdt.testclasses.Foo;
import spoon.support.reflect.code.CtForImpl;

public class ParcelableTransPhaseOneProcessor extends
	                AbstractAnnotationProcessor <States, CtField> {  
	
	private int nameCount = 0;
	private int indexCount = 0;
	private int limitCount = 0;
	private String _PU_NON_SENSE_ = "_PU_NON_SENSE_";
	private static final String NEWLINE = "\n";
	
	
    public static void main(String[] args) throws Exception {
        spoon.Launcher.main(new String[] {
                "-p", "spoon.examples.ParcelableTransPhaseOneProcessor",
//                "/home/ericliu/Downloads/ClipboardService.java"
                "-i", "/home/ericliu/Documents/toParcelable/source/ClipboardService.java",
                "-o", "/home/ericliu/Documents/toParcelable/out/ClipboardService.java"
        });
    }

	@Override
	public void process(States annotation, CtField element) {
		// TODO Auto-generated method stub
		if(!element.getType().isPrimitive())
		{
//			System.out.print(element);
//			System.out.println(element.getType());
			CtTypeReference type = getActualTypeHelper(element.getType());
//			System.out.print(type.getSuperInterfaces());
//			System.out.println(type);
			if(type.getSuperInterfaces().size() == 0)
				transParcelable(type);
			else
			{
				for(CtTypeReference ref : type.getSuperInterfaces())
				{
//					System.out.println(ref);
					if(ref.getQualifiedName().equals("android.os.Parcelable")){
						System.out.println(type.getSimpleName() + " already imp"
								+ "lements Parcelable, no worries");
	//					transParcelable(type);
					}
					else{
//						System.out.println("before parcelable");
						transParcelable(type);
//						System.out.println("after parcelable");
					}
				}
			}
		} 
	}
	
	private void transParcelable(CtTypeReference type)
	{
		
		Factory f = getFactory();
		
		setModifier(type);
		
		// writeToParcelable
		writeToParcelable(type);
		
		System.out.println("------------------------");

		//private constructor
		readFromParcelable(type);
		
		//describeContents
		describeContents(type);
		
		//CREATOR
		creator(type);
	}
	
	private void setModifier(CtTypeReference type)
	{
		Factory f = getFactory();
		type.getDeclaration().addSuperInterface(f.Type().createReference(Parcelable.class));
		type.getDeclaration().addModifier(ModifierKind.STATIC);
	}
	
	private void describeContents(CtTypeReference type)
	{
		Factory f = getFactory();
		
		// writeToParcelable
		CtMethod method = f.Core().createMethod();
		method.setSimpleName("describeContents");
		method.setType(f.Type().INTEGER_PRIMITIVE);
		Set<ModifierKind> mSet = new HashSet<>();
		mSet.add(ModifierKind.PUBLIC);
		method.setModifiers(mSet);
		
		CtBlock body = f.Core().createBlock();
		CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
		snippet.setValue("return 0");
		body.addStatement(snippet);
		
		method.setBody(body);
		type.getTypeDeclaration().addMethod(method);
	}
	
	private void readFromParcelable(CtTypeReference type)
	{
		Factory f = getFactory();
		
		CtMethod privateCnstr = f.Core().createMethod();
		privateCnstr.setSimpleName(type.getSimpleName());
		Set<ModifierKind> pCnstrSet = new HashSet<>();
		pCnstrSet.add(ModifierKind.PRIVATE);
		privateCnstr.setModifiers(pCnstrSet);
		
		List<CtParameter<?>> pCnstrParamList = new ArrayList<CtParameter<?>>();
		CtParameter<?> pCnstrIn = f.Executable().createParameter(privateCnstr, f.Type().createReference(Parcel.class), "in");
		pCnstrParamList.add(pCnstrIn);
		privateCnstr.setParameters(pCnstrParamList);
		
		CtBlock pCnstrBody = f.Core().createBlock();
		privateCnstr.setBody(pCnstrBody);
		
		for(CtFieldReference<?> a : type.getTypeDeclaration().getAllFields())
		{
			CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();

			List<String> addition = new ArrayList<String>();
			String snippetVal = readFromParcelHelper(a.getType(), a, a.toString(), addition, 0, "");
			if(snippetVal == null || snippetVal.isEmpty())
				continue;
			snippet.setValue(snippetVal);
			for(String b : addition)
			{
				CtCodeSnippetStatement tmpSnippet = getFactory().Core().createCodeSnippetStatement();
				tmpSnippet.setValue(b);
				pCnstrBody.addStatement(tmpSnippet);
			}
			pCnstrBody.addStatement(snippet);
		}

		type.getTypeDeclaration().addMethod(privateCnstr);
	}
	
	private void writeToParcelable(CtTypeReference type)
	{
		Factory f = getFactory();
		
		// writeToParcelable
		CtMethod method = f.Core().createMethod();
		CtAnnotation<Override> anno = f.Core().createAnnotation();
		anno.setAnnotationType(f.Type().createReference(java.lang.Override.class));
		method.addAnnotation(anno);
		method.setSimpleName("writeToParcel");
		method.setType(f.Type().VOID_PRIMITIVE);
		Set<ModifierKind> mSet = new HashSet<>();
		mSet.add(ModifierKind.PUBLIC);
		method.setModifiers(mSet);
		
		List<CtParameter<?>> paramList = new ArrayList<CtParameter<?>>();
		CtParameter<?> out = f.Executable().createParameter(method, f.Type().createReference(Parcel.class), "out");
		CtParameter<?> flags = f.Executable().createParameter(method, f.Type().integerPrimitiveType(), "flags");
		paramList.add(out);
		paramList.add(flags);
		method.setParameters(paramList);
		
		CtBlock body = f.Core().createBlock();
		
		for(CtFieldReference<?> a : type.getTypeDeclaration().getAllFields())
		{
			CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();

			List<String> addition = new ArrayList<String>();
			String snippetVal = writeToParcelHelper(a.getType(), a, a.toString(), addition, 0);
			if(snippetVal == null || snippetVal.isEmpty())
				continue;
			snippet.setValue(snippetVal);
			for(String b : addition)
			{
				CtCodeSnippetStatement tmpSnippet = getFactory().Core().createCodeSnippetStatement();
				tmpSnippet.setValue(b);
				body.addStatement(tmpSnippet);
			}
			body.addStatement(snippet);
		}
		method.setBody(body);
		type.getTypeDeclaration().addMethod(method);
	}
	
	private void creator(CtTypeReference type)
	{
		Factory f = getFactory();
		
		Set<ModifierKind> nSet = new HashSet<>();
		nSet.add(ModifierKind.PUBLIC);
		nSet.add(ModifierKind.STATIC);
		nSet.add(ModifierKind.FINAL);
		CtTypeReference ctrType = f.Type().createReference("android.os.Parcelable.Creator<" + type.toString() + ">");
		
		CtExpression expr = f.Code().createCodeSnippetExpression(
				"new android.os.Parcelable.Creator<" + type.toString() + ">() {\n"
						+ methodCreateFromParcelable(type).toString() + "\n"
						+ methodNewArray(type).toString() + "\n"
						+ "}");
		
		CtField field =  f.Field().create(type.getTypeDeclaration(), nSet, ctrType, "CREATOR", expr);
		type.getTypeDeclaration().addField(field);
	}
	
	private void testString(String test)
	{
		test += "hello";
	}
	
	private String readFromParcelHelper(CtTypeReference<?> type, CtFieldReference<?> a, 
			String varName, List<String> addition, int depth, String outerName)
	{	
		System.out.print(type.getActualClass());
		System.out.print("\t");
		System.out.println(type.getActualTypeArguments());
		Class<?> clazz = type.getActualClass();
	
		if(clazz.equals(int.class))
		{
			if(depth == 0)
				return varName + " = in.readInt()";
			else
				return "in.readInt();" + NEWLINE 
						+ outerName + ".add(" + varName + ")" + NEWLINE;
		}
		if(clazz.equals(String.class))
		{
			if(depth == 0)
				return varName + " = in.readString()";
			else
				return "in.readString();" + NEWLINE
						+ outerName + ".add(" + varName + ")" + NEWLINE;
		}
		if(isParcelable(type))
		{
			if(depth == 0)
				return varName + " = in.readParcelable(" + type.toString() + ".class.getClassLoader())";
			else
				return "in.readParcelable(" + type.toString() + ".class.getClassLoader());" + NEWLINE
						+ outerName + ".add(" + varName + ")" + NEWLINE;
		}
		if(clazz.equals(RemoteCallbackList.class))
		{
			String localLimit = limitGenerator();
			String localName = nameGenerator();
			String localIndex = indexGenerator();
			
			String tmp = "int " + localLimit + " = in.readInt()";
			addition.add(tmp);
			String dummy = (depth == 0) ? _PU_NON_SENSE_ : "";
			String newStatement = (depth == 0) ? "" : " = new " + type.toString() + "();" + NEWLINE;
			return newStatement + "for(" + "int " + localIndex + " = 0; " 
						+ localIndex + " < " + localLimit + "; ++ " 
						+ localIndex + ")" + NEWLINE
					+ "{" + NEWLINE
					+ type.getActualTypeArguments().get(0).toString() + " " + localName + " = "
					+ type.getActualTypeArguments().get(0).toString() + ".Stub.asInterface(in.readStrongBinder());" + NEWLINE
					+ varName + ".register(" + localName + ");" + NEWLINE
					+ "}" + NEWLINE
					+ dummy;
		}
		if(type.getActualTypeArguments().size() > 0) // this is a container
		{	
			if(clazz.equals(HashSet.class) || clazz.equals(List.class))
			{
				String localLimit = limitGenerator();
				String localName = nameGenerator();
				String localIndex = indexGenerator();
				
				String tmp = "int " + localLimit + " = in.readInt()";
				addition.add(tmp);
				String dummy = (depth == 0) ? _PU_NON_SENSE_ : "";
				String newStatement = (depth == 0) ? "" : " new " + type.toString() + "();" + NEWLINE;
				return newStatement + "for(" + "int " + localIndex + " = 0; " 
							+ localIndex + " < " + localLimit + "; ++ " 
							+ localIndex + ")" + NEWLINE
						+ "{" + NEWLINE
						+ type.getActualTypeArguments().get(0).toString() + " " + localName + " = "
							+ readFromParcelHelper(type.getActualTypeArguments().get(0), a, localName, addition, depth + 1, varName)
						+ "}" + NEWLINE
						+ dummy;
			}
			else
				System.out.println("unkown type");
		}
		return "";
	}
	
	private String writeToParcelHelper(CtTypeReference<?> type, CtFieldReference<?> a, 
			String varName, List<String> addition, int depth)
	{
		System.out.print(type.getActualClass());
		System.out.print("\t");
		System.out.println(type.getActualTypeArguments());
		Class<?> clazz = type.getActualClass();
		
		if(clazz.equals(int.class))
		{
			if(depth == 0)
				return "out.writeInt(" + varName + ")";
			else
				return "out.writeInt(" + varName + ");" + NEWLINE;
		}
		if(clazz.equals(String.class))
		{
			if(depth == 0)
				return "out.writeString(" + varName + ")";
			else
				return "out.writeString(" + varName + ");" + NEWLINE;
		}
		if(isParcelable(type))
		{
			if(depth == 0)
				return"out.writeParcelable(" + varName + ", 0)";
			else
				return"out.writeParcelable(" + varName + ", 0);" + NEWLINE;
		}
		if(clazz.equals(RemoteCallbackList.class))
		{
			String tmp = "out.writeInt(" + varName + ".getRegisteredCallbackCount())";
			addition.add(tmp);
			String localIndex = indexGenerator();
			String dummy = (depth == 0) ? _PU_NON_SENSE_ : "";
			return "for(int " + localIndex + " = 0; " 
					+ localIndex + " < " + varName + ".getRegisteredCallbackCount(); "
					+ "++ " + localIndex + ")" + NEWLINE
					+ "{" + NEWLINE
					+ "out.writeStrongInterface(" + varName + ".getBroadcastItem(" + localIndex + "));" + NEWLINE
					+ "}" + NEWLINE
					+ dummy;
		}
		if(type.getActualTypeArguments().size() > 0) // this is a container
		{
//			if(clazz.equals(HashSet.class) || clazz.equals(List.class))
			if(isIterable(clazz))
			{
				String tmp = "out.writeInt(" + varName + ".size())";
				addition.add(tmp);
				String localName = nameGenerator();
				String dummy = (depth == 0) ? _PU_NON_SENSE_ : "";
				return "for(" + type.getActualTypeArguments().get(0).toString() 
						+ " " + localName + " : " + varName + ")" + NEWLINE
						+ "{" + NEWLINE
						+ writeToParcelHelper(type.getActualTypeArguments().get(0), a, localName, addition, depth + 1)
						+ "}" + NEWLINE
						+ dummy;
			}
			else
				System.out.println("unkown type");
		}
		return "";
	}
	
	private boolean isIterable(Class<?> clazz)
	{
		if(clazz == null || clazz.equals(Object.class))
			return false;
		
		if(clazz.equals(AbstractCollection.class))
			return true;
		
		return isIterable(clazz.getSuperclass());
	}
	
	private String nameGenerator()
	{	
		return "_pu_obj_" + Integer.toString(nameCount ++);
	}
	
	private String indexGenerator()
	{
		return "_pu_index_" + Integer.toString(indexCount ++);
	}
	
	private String limitGenerator()
	{
		return "_pu_limit_" + Integer.toString(limitCount ++);
	}
	
	private boolean isParcelable(CtTypeReference type)
	{
		List<CtTypeReference<?>> mList = type.getActualTypeArguments();
		if(mList.size() != 0) return false;
		
		boolean ret = false;
		for(CtTypeReference a : type.getSuperInterfaces())
		{
			if(a.getQualifiedName().equals("android.os.Parcelable"))
			{
				ret = true;
				System.out.println(type.toString() + " is Parcelable");
				break;
			}
		}
		return ret;
	}
	
	private CtMethod methodCreateFromParcelable(CtTypeReference type)
	{
		Factory f = getFactory();
		
		CtMethod method = f.Core().createMethod();
		method.setSimpleName("createFromParcelable");
		CtBlock body = f.Core().createBlock();
		CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
		snippet.setValue("return new " + type.toString() + "(in)");
		body.addStatement(snippet);
		method.setBody(body);
		method.setType(type);
		Set<ModifierKind> mSet = new HashSet<>();
		mSet.add(ModifierKind.PUBLIC);
		method.setModifiers(mSet);
		
		List<CtParameter<?>> paramList = new ArrayList<CtParameter<?>>();
		CtParameter<?> out = f.Executable().createParameter(method, f.Type().createReference(Parcel.class), "in");
		paramList.add(out);
		method.setParameters(paramList);
		return method;
	}
	
	private CtMethod methodNewArray(CtTypeReference type)
	{
		Factory f = getFactory();
		
		CtMethod method = f.Core().createMethod();
		method.setSimpleName("newArray");
		CtBlock body = f.Core().createBlock();
		CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
		snippet.setValue("return new " + type.toString() + "[size]");
		body.addStatement(snippet);
		method.setBody(body);
		
		CtTypeReference arrayType = f.Type().createReference(type.toString() + "[]");
		method.setType(arrayType);
		Set<ModifierKind> mSet = new HashSet<>();
		mSet.add(ModifierKind.PUBLIC);
		method.setModifiers(mSet);
		
		List<CtParameter<?>> paramList = new ArrayList<CtParameter<?>>();
		CtParameter<?> flags = f.Executable().createParameter(method, f.Type().integerPrimitiveType(), "size");
		paramList.add(flags);
		method.setParameters(paramList);
		return method;
	}
	
	private CtTypeReference getActualTypeHelper(CtTypeReference type)
	{
		List<CtTypeReference<?>> mList = type.getActualTypeArguments();
//		System.out.println(mList);
		if(mList.size() == 0) return type;
		for(CtTypeReference t : mList)
		{
			return getActualTypeHelper(t);
		}
		System.out.println("Oops..Should not be here!");
		return type;	
	}

}

