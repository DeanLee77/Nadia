package ruleParser;

import java.util.List;
import java.util.Stack;

import nodePackage.*;
import inferencePackage.*;



public class RuleSetScanner {
	private IScanFeeder scanFeeder = null;
    private ILineReader lineReader = null;

    public RuleSetScanner(ILineReader reader, IScanFeeder feeder) {
          scanFeeder = feeder;
          lineReader = reader;
    }

    public void scanRuleSet() {

          String parent = null; 
          String iterateParent = null;
          String line = null;
          String lineTrimed;
          Stack<String> parentStack = new Stack<>();
          int previousWhitespace = 0;
          int lineNumber = 0;


          while ((line = lineReader.getNextLine()) != null) {

       	   line = line.replaceAll("\\s*(if)*\\s*$", ""); // it trims off whitespace including 'if' at the end of each line due to it could effect on calculating indentation
       	   lineTrimed = line.trim();
       	   int currentWhitespace = 0;
       	   lineNumber++;

               // check the line

               // is it empty?
               if(line.isEmpty()) 
               {
            	   		parentStack.clear();
               }
               else if(line.trim().substring(0, 2).equals("//"))
               {
        	   			//this els if statement is to handle commenting in new line only
            	   		// handling commenting in rule text file needs enhancement later
               }
               
               // does it begin with a white space?
               else if(Character.isWhitespace(line.charAt(0))) 
           	   {
               		currentWhitespace = line.length()-lineTrimed.length(); // calculating indentation level

	                	if (lineTrimed.isEmpty()) // is it a blank line? 
	                	{
	                      // blank line - no parent
	                      parent = null;
	                	} 
	                	else 
	                	{
	                		int indentationDifference = previousWhitespace - currentWhitespace;
	                   	if(indentationDifference == 0 || indentationDifference > 0) //current line is at same level as previous line || current line is in upper level than previous line
	                   	{
	                   		parentStack = handlingStackPop(parentStack, indentationDifference);
	                   		
	                   	}
	                   	else if(indentationDifference < -1) // current line is not a direct child of previous line hence the format is invalid
	                   	{
	                   		
	               			//need to handle error
	               			scanFeeder.handleWarning(lineTrimed);
	               			break;
	                   		
	                   	}
	                   	
	                   	parent = parentStack.pop();
	           			parentStack.push(parent);
	           			
	           			String tempLineTrimed = lineTrimed.replaceAll("(OR(?=\\s)|AND(?=\\s)|MANDATORY(?=\\s)|OPTIONAL(?=\\s)|POSSIBLE(?=\\s))", "").trim();

	           			parentStack.push(tempLineTrimed.trim()); // due to lineTrimed string contains keywords such as "AND", "OR", "AND KNOWN" or "OR KNOWN" so that it needs removing those keywords for the 'parentStack'
		
		           		// is an indented child
	           			scanFeeder.handleChild(parent, lineTrimed, lineNumber);	
	           			
	           			
	                		if(lineTrimed.contains("ITEM"))
	                		{
	                			if(!parent.contains("LIST"))
	                			{
	                				scanFeeder.handleWarning(lineTrimed);
	                				break;
	                			}
		                		// is an indented item child
	                			lineTrimed = lineTrimed.replace("ITEM ", "").trim();
	                			scanFeeder.handleListItem(parent, lineTrimed);
		                         
		                	}
	                		else if(lineTrimed.contains("CHECK"))
	                		{
	                			if(!(parent.contains("ITERATE") || parent.contains("CHECK")))
	                			{
	                				scanFeeder.handleWarning(lineTrimed);
	                				break;
	                			}
	                			else
	                			{
	                				if(parent.contains("ITERATE"))
	                				{
	                    				iterateParent = parent;
	                				}
	                				scanFeeder.handleIterateCheck(iterateParent, parent, lineTrimed, lineNumber);
	                			}
	                		}
	                		else if(lineTrimed.contains("NEEDS")|| lineTrimed.contains("WANTS"))
	                		{
	                			if(!parent.contains("CALC"))
	                			{
	                				scanFeeder.handleWarning(lineTrimed);
	                				break;
	                			}
	                			
	                			if(lineTrimed.contains("NEEDS"))
	                			{
	                				scanFeeder.handleNeedWant(parent, lineTrimed.replace("NEEDS", "AND MANDATORY NEEDS"), lineNumber);
	                			}
	                			else
	                			{
	                				scanFeeder.handleNeedWant(parent, lineTrimed.replace("WANTS", "OR WANTS"), lineNumber);
	                			}
	                		}
	                		else
	                		{
	                			// is an indented child
		                       scanFeeder.handleChild(parent, lineTrimed, lineNumber);
		                	}   
                    }

               } 
               // does not begin with a white space
               else {
                    // is a parent
           		parentStack.clear();
                   parent = lineTrimed;
                   scanFeeder.handleParent(parent, lineNumber);
                   parentStack.push(parent);
               }
               previousWhitespace = currentWhitespace;
          }
    }
    
    public void establishNodeSet()
    {
   	 NodeSet ns = scanFeeder.getNodeSet();
   	 ns.setDependencyMatrix(scanFeeder.createDependencyMatrix());
   	 List<Node> sortedList = TopoSort.bfsTopoSort(ns.getNodeMap(), ns.getNodeIdMap(), ns.getDependencyMatrix().getDependencyMatrixArray()); 
   	 if(sortedList.size() != 0)
   	 {
   	   	 ns.setNodeSortedList(sortedList);
   	 }
   	 else
   	 {
   		scanFeeder.handleWarning("RuleSet needs rewriting due to it is cyclic.");
   	 }
   	 //   	 scanFeeder.setNodeSet(ns);
    }
    
    public Stack<String> handlingStackPop(Stack<String>parentStack, int indentationDifference)
    {
   	 for(int i = 0; i < indentationDifference+1; i++)
   	 {
   		 parentStack.pop();
   	 }
   	 return parentStack;
    }


}

