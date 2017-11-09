package nodePackage;

import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.script.ScriptEngine;

import factValuePackage.*;
import ruleParser.Tokens;

public class ValueConclusionLine extends Node{

	private boolean isPlainStatementFormat;

	/*
	 * ValueConclusionLine format is as follows;
	 *  1. 'A-statement IS B-statement';
	 *  2. 'A-item name IS IN LIST: B-list name'; or
	 *  3. 'A-statement'(plain statement line) including statement of 'A' type from a child node of ExprConclusionLine type which are 'NEEDS' and 'WANTS'.
	 * When the inference engine reaches at a ValueConclusionLine and needs to ask a question to a user, 
	 * this rule must be either 'A (plain statement)' or 'A IS B' format due to the reason that other than the two format cannot be a parent rule.
	 * Hence, the question can be from either variableName or ruleName, and a result of the question will be inserted into the workingMemory.
	 * However, when the engine reaches at the line during forward-chaining then the key for the workingMemory will be a ruleName,
	 * and value for the workingMemory will be set as a result of propagation.
	 * 
	 * If the rule statement is in a format of 'A-statement' then a default value of variable 'value' will be set as 'false'
	 * 
	 */
	public ValueConclusionLine(String nodeText, Tokens tokens)
	{
		super(nodeText, tokens);
		
	}
	
	@Override
	public void initialisation(String nodeText, Tokens tokens)
	{
		int tokensStringListSize = tokens.tokensStringList.size(); //tokens.tokensStringList.size is same as tokens.tokensList.size
		
		this.isPlainStatementFormat = !tokens.tokensList.stream().anyMatch((s) -> s.contains("IS")); //this will exclude 'IS' and 'IS IN  LIST:' within the given 'tokens'

		String lastToken = null;
		if(!isPlainStatementFormat) //the line must be a parent line in this case other than a case of the rule contains 'IS IN LIST:'
		{
			
			this.variableName = nodeText.substring(0, nodeText.indexOf("IS")).trim();
			lastToken = tokens.tokensList.get(tokensStringListSize-1);
		}
		else //this is a case of that the line is in a 'A-statement' format
		{
			
			this.variableName = nodeText;
			lastToken = "false";

		}
		
		this.nodeName = nodeText;						
		String lastTokenString = tokens.tokensStringList.get(tokensStringListSize-1);
		this.setValue(lastTokenString, lastToken);
	}
	
	public boolean getIsPlainStatementFormat()
	{
		return this.isPlainStatementFormat;
	}

	
	@Override
	public LineType getLineType()
	{
		return LineType.VALUE_CONCLUSION;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public FactValue selfEvaluate(HashMap<String, FactValue> workingMemory, ScriptEngine nashorn)
	{
		FactValue fv = null;
		/*
		 * Negation and Known type are a part of dependency 
		 * hence, only checking its variableName value against the workingMemory is necessary.
		 * type is as follows;
		 *	1. the rule is a plain statement
		 *	2. the rule is a statement of 'A IS B'
		 * 	3. the rule is a statement of 'A IS IN LIST: B'
		 * 	4. the rule is a statement of 'needs(wants) A'. this is from a child node of ExprConclusionLine type 
		 */
		
		if(!this.isPlainStatementFormat)
		{
			if(tokens.tokensList.stream().anyMatch((s) -> s.equals("IS")))
			{
				fv = this.value;
			}
			else if(tokens.tokensList.stream().anyMatch((s) -> s.equals("IS IN LIST:")))
			{
				boolean lineValue = false;
				String listName = this.getFactValue().getValue().toString();
				if(workingMemory.get(listName) != null)
				{
					String variableValueInString = workingMemory.get(this.variableName).getValue().toString();
					lineValue = variableValueInString != null?
					((FactListValue<?>)workingMemory.get(listName)).getValue().stream().anyMatch((factValue)->((FactStringValue)factValue).getValue().equals(variableValueInString))
					:
					((FactListValue<?>)workingMemory.get(listName)).getValue().stream().anyMatch((factValue)->((FactStringValue)factValue).getValue().equals(this.variableName));
					
					
				}
				
				fv = FactValue.parse(lineValue);
			}
		}
		
		
		
		
		return fv;
	}
}
