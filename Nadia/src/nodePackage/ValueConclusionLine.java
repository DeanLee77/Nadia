package nodePackage;

import java.util.HashMap;

import javax.script.ScriptEngine;

import factValuePackage.*;
import ruleParser.Tokens;

public class ValueConclusionLine extends Node{

	private boolean isInStatementFormat;

	/*
	 * when the inference engine reaches at a ValueConclusionLine and needs to ask a question to a user, 
	 * then this rule must be in statement format due to the reason that a line containing a keyword, 'IS', cannot be a child.
	 * Hence, the question can be from either variableName or ruleName, and a result of the question will be inserted to the workingMemory.
	 * However, when the engine reaches at the line during forward-chaining then the key for the workingMemory will be a ruleName,
	 * and value for the workingMemory will be set as a result of propagation.
	 * 
	 * ValueConclusionLine format is either 'A-statement IS B-statement' or 'A-statement'(just statement line).
	 * Hence, any words except first letter should NOT be uppercase other than keywords within a rule statement.
	 * And therefore, if a tokensString of the rule statement contains 'U' then the rule statement is in a format of
	 * 'A-statement IS B-statement' otherwise 'A-statement'. 
	 * If the rule statement is in a format of 'A-statement' then a default value of variable 'value' will be set as 'false'
	 * 
	 */
	public ValueConclusionLine(String parentText, Tokens tokens)
	{
		super(parentText, tokens);
		
	}
	
	@Override
	public void initialisation(String parentText, Tokens tokens)
	{
		int tokensStringListSize = tokens.tokensStringList.size(); //tokens.tokensStringList.size is same as tokens.tokensList.size
		
		
		/* 
		 * if this line is a parent then a pattern will be "(^[LM]+)(U)?([MLNoDaDeHaUrlId]*$)(?!C)",
		 * if this line is a child then a pattern will be (^U)([LMUDa]+$) due to it starts with AND(OR), AND MANDATORY(OPTIONALLY, POSSIBLY) or AND (MANDATORY) (NOT) KNOWN
		 * type is as follows;
		 *	1. the rule is a plain statement
		 *		- evaluate based on outcome of its child nodes
		 *		  there only will be an outcome of entire rule statement with negation or known type value
		 *		  , which should be handled within selfEvaluate()
		 *	2. the rule is a statement of 'A IS B'
		 *		- evaluate based on outcomes of its child nodes
		 *		  there will be an outcome for a statement of that is it true for 'A = B'?
		 *		  , and out
		 * 	3. the rule is a statement of 'A IS IN LIST: B'
		 * 	4. the rule is a statement of 'A'. this is from a child node of ExprConclusionLine type 
		 * 
		 * 
		 */
		
		this.isInStatementFormat = !tokens.tokensList.stream().anyMatch((s) -> s.contains("IS"));

		String lastToken = null;
		if(!isInStatementFormat) //the line must be a parent line in this case other than a case of the rule contains 'IS IN LIST' as a child
		{
			this.variableName = parentText.substring(0, parentText.indexOf("IS")).replaceFirst("([(NOT(?=\\s))(KNOWN(?=\\s))]*)", "").trim();
			lastToken = tokens.tokensList.get(tokensStringListSize-1);
			this.nodeName = parentText;
		}
		else
		{
			this.nodeName = parentText;
			this.variableName = tokens.tokensString.charAt(0) == 'U'? parentText.replaceFirst("([(NOT(?=\\s))(KNOWN(?=\\s))(NEEDS(?=\\s))(WANTS(?=\\s))]*)", "").trim(): parentText; // is the line child case? then replace "NOT|KNOWN" if there is otherwise parentText as it is
						
			lastToken = "false";

		}
			
		String lastTokenString = tokens.tokensStringList.get(tokensStringListSize-1);
		this.setValue(lastTokenString, lastToken);
	}
	
	public boolean getIsInStatementFormat()
	{
		return this.isInStatementFormat;
	}

	
	@Override
	public LineType getLineType()
	{
		return LineType.VALUE_CONCLUSION;
	}
	
	@Override
	public FactValue selfEvaluate(HashMap<String, FactValue> workingMemory, ScriptEngine nashorn, int nodeOption)
	{
		FactValue fv = null;
//		String firstWordOfNodeName = this.nodeName.split(" ")[0];
//		boolean isWantsNeedsItem = firstWordOfNodeName.equals("wants")||firstWordOfNodeName.equals("needs")?true:false;
		/*
		 * Negation and Known type can only be used when the line is a child line
		 * hence, only checking its variableName value against the workingMemory is necessary.
		 * type is as follows;
		 *	1. the rule is a plain statement
		 *		- evaluate based on outcome of its child nodes
		 *		  there only will be an outcome of entire rule statement with negation or known type value
		 *		  , which should be handled within selfEvaluate()
		 *	2. the rule is a statement of 'A IS B'
		 *		- evaluate based on outcomes of its child nodes
		 *		  there will be an outcome for a statement of that is it true for 'A = B'?
		 *		  , and out
		 * 	3. the rule is a statement of 'A IS IN LIST: B'
		 * 	4. the rule is a statement of 'needs(wants) A'. this is from a child node of ExprConclusionLine type 
		 */
		

		if(this.isInStatementFormat)
		{
			
			if((nodeOption & DependencyType.getNot()) == DependencyType.getNot() && !((nodeOption & DependencyType.getKnown()) == DependencyType.getKnown()))
			{
				fv = FactValue.parse(!Boolean.parseBoolean(workingMemory.get(this.variableName).getValue()));
			}
			else if(!((nodeOption & DependencyType.getNot()) == DependencyType.getNot()) && (nodeOption & DependencyType.getKnown()) == DependencyType.getKnown())
			{
				fv = FactValue.parse(workingMemory.get(this.variableName)!=null?true:false);
			}
			else if((nodeOption & DependencyType.getNot()) == DependencyType.getNot() && (nodeOption & DependencyType.getKnown()) == DependencyType.getKnown())
			{
				fv = FactValue.parse(workingMemory.get(this.variableName) != null?false:true);
			}
			else
			{
				fv = FactValue.parse(Boolean.parseBoolean(workingMemory.get(this.variableName).getValue()));
			}
		}
		else if(this.nodeName.contains("IS IN LIST"))
		{
			FactListValue tempFactValue = (FactListValue)workingMemory.get(this.value.getValue());
			
			fv = (nodeOption & DependencyType.getNot()) == DependencyType.getNot()?FactValue.parse(!tempFactValue.getListValue().contains(this.variableName)):
								FactValue.parse(tempFactValue.getListValue().contains(this.variableName));
			
		}
		else if(this.nodeName.contains("IS") && !this.nodeName.contains("IN LIST"))
		{
			boolean lineValue = false;
			if(workingMemory.get(this.variableName) != null)
			{
				lineValue = workingMemory.get(this.variableName).getValue().equals(this.getFactValue().getValue());
			}
			
			if((nodeOption & DependencyType.getNot()) == DependencyType.getNot())
			{
				fv = FactValue.parse(!lineValue);
			}
			else
			{
				fv = FactValue.parse(lineValue);
			}
		}
		
		
		
		return fv;
	}
	
	

}
