package nodePackage;

import java.util.HashMap;
import java.util.stream.IntStream;

import javax.script.ScriptEngine;

import factValuePackage.*;
import ruleParser.Tokens;

public class ValueConclusionLine extends Node{

	private boolean isPlainStatementFormat;

	/*
	 * when the inference engine reaches at a ValueConclusionLine and needs to ask a question to a user, 
	 * then this rule must be in statement format due to the reason that a line containing a keyword, 'IS', (exclude a case of containing 'IS IN LIST') cannot be a child.
	 * Hence, the question can be from either variableName or ruleName, and a result of the question will be inserted to the workingMemory.
	 * However, when the engine reaches at the line during forward-chaining then the key for the workingMemory will be a ruleName,
	 * and value for the workingMemory will be set as a result of propagation.
	 * 
	 * ValueConclusionLine format is 'A-statement IS B-statement', 'A-item name IS IN LIST: B-list name', or 'A-statement'(just statement line).
	 * Hence, any words except first letter should NOT be in uppercase other than keywords within a rule statement.
	 * And therefore, if a tokensString of the rule statement contains 'U' at the beginning of rule statement then the rule statement is in a format of
	 * 'A-item name IS IN LIST: B-list name' or 'A-statement' because Parent line cannot have any node options such as 'NOT', 'KNOWN', 'MANDATORY', 'OPTIONAL', and/or 'POSSIBLY'
	 * 
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
		
		this.isPlainStatementFormat = !tokens.tokensList.stream().anyMatch((s) -> s.contains("IS"));

		String lastToken = null;
		if(!isPlainStatementFormat) //the line must be a parent line in this case other than a case of the rule contains 'IS IN LIST' as a child
		{
			/*
			 * Reasons for that RegEx is not being used to extract upper-case letters at the beginning of a given string are as follows;
			 *  1. it is hard to only extract upper-case letters at the beginning of the string with 'string.replaceFirst(regex)' or 'string.replaceAll(regex)' 
			 *     because 'replaceFirst()' would NOT do excluding all relevant letters even if argument of the method is multiple, 
			 *     'replaceAll()' would excluding not only upper-case letters at the beginning but also upper-case letters in the middle of the given string
			 *  
			 *  2. RegEx would be difficult to maintain for later when other keywords are added and need being extracted 
			 */
			if(tokens.tokensString.charAt(0) == 'U') // this is a case of that the line contains 'IS IN LIST:' because first token is 'U'
			{
				String tempTokensStr = tokens.tokensString.substring(1, tokens.tokensString.length()); //exclude first upper-case letters
				int tempInt = tempTokensStr.substring(0, tempTokensStr.indexOf('U')).length();  // calculate the length of string up until the string of 'IS IN LIST'
				StringBuilder sb = new StringBuilder();
				IntStream.range(1, tempInt+1).forEachOrdered(i -> {sb.append(tokens.tokensList.get(i)+" ");}); //building a string with strings between upper-case letters beginning and 'IS IN LIST' 
				this.variableName = sb.toString().trim();
				
			}
			else //this is a case of that the line is in a 'A-statement IS B-statement' format
			{
				this.variableName = parentText.substring(0, parentText.indexOf("IS")).trim();
			}
//			this.variableName = parentText.substring(0, parentText.indexOf("IS")).replaceFirst("([(NOT(?=\\s))(KNOWN(?=\\s))]*)", "").trim();
			lastToken = tokens.tokensList.get(tokensStringListSize-1);
			this.nodeName = parentText;
		}
		else //this is a case of that the line is in a 'A-statement' format
		{
			if(tokens.tokensString.charAt(0) == 'U')  // this is to extract other keywords such as 'NOT' or/and 'KNOWN' and etc.
			{
				String tempTokensStr = tokens.tokensString.substring(1, tokens.tokensString.length()); //exclude first upper-case letters
				int tempInt = tempTokensStr.length();  // calculate the length of string 
				StringBuilder sb = new StringBuilder();
				IntStream.range(1, tempInt+1).forEachOrdered(i -> {sb.append(tokens.tokensList.get(i)+" ");}); //building a string with strings between upper-case letters beginning and 'IS IN LIST' 
				this.variableName = sb.toString().trim();
				
			}
			else 
			{
				this.variableName = parentText;
			}
			this.nodeName = parentText;
//			this.variableName = tokens.tokensString.charAt(0) == 'U'? parentText.replaceFirst("([(NOT(?=\\s))(KNOWN(?=\\s))(NEEDS(?=\\s))(WANTS(?=\\s))]*)", "").trim(): parentText; // is the line child case? then replace "NOT|KNOWN" if there is otherwise parentText as it is
						
			lastToken = "false";

		}
			
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
		

		if(this.isPlainStatementFormat && workingMemory.get(this.variableName).getType().equals(FactValueType.BOOLEAN))
		{
			fv = FactValue.parse(Boolean.parseBoolean(workingMemory.get(this.variableName).getValue().toString()));
		}
		else if(this.nodeName.contains("IS IN LIST"))
		{
			@SuppressWarnings("rawtypes")
			FactListValue tempFactValue = (FactListValue)workingMemory.get(this.value.getValue());
			
			fv = FactValue.parse(tempFactValue.getListValue().contains(this.variableName));
			
		}
		else if(this.nodeName.contains("IS") && !this.nodeName.contains("IN LIST"))
		{
			boolean lineValue = false;
			if(workingMemory.get(this.variableName) != null)
			{
				lineValue = workingMemory.get(this.variableName).getValue().equals(this.getFactValue().getValue());
			}
			
			fv = FactValue.parse(lineValue);
		}
		
		
		
		return fv;
	}
}
