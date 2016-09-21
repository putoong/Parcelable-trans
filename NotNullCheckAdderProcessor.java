package spoon.examples;

import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtCodeSnippetStatement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtParameter;

public class NotNullCheckAdderProcessor extends
	                AbstractProcessor<CtParameter<?>> {
	
	  @Override
	  public boolean isToBeProcessed(CtParameter<?> element) {
	    return !element.getType().isPrimitive();// only for objects
	  }
	
	        public void process(CtParameter<?> element) {
	          System.out.println(element);
	          // we declare a new snippet of code to be inserted
	      CtCodeSnippetStatement snippet = getFactory().Core().createCodeSnippetStatement();
	  
	      // this snippet contains an if check
      snippet.setValue("if(" + element.getSimpleName() + " == null "
    		  + ") throw new IllegalArgumentException(\"[Spoon inserted check] null passed as parameter\")");
  
	      // we insert the snippet at the beginning of the method boby
	      if (element.getParent(CtExecutable.class).getBody()!=null) 
	      {
	        element.getParent(CtExecutable.class).getBody().insertBegin(snippet);
	      }
	          }
	        
	}
