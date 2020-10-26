package nodePackage;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;

import factValuePackage.FactValue;
import ruleParser.Tokens;

public class RuleSetLine extends Node{

	private String operator;
	private FactValue lhs;
	private FactValue rhs;
	private boolean assigningReturnValue;
	
	public RuleSetLine(String childText, Tokens tokens)
	{
		super(childText, tokens);
		assigningReturnValue = isAssigningReturnValue();
	}
	
	
	@Override
	public void initialisation(String childText, Tokens tokens) {
		/*
		 * this line pattern is as (^[RML]+)(O)([RMLNoDaDeHaUrlId]*$)
		 */
		
		this.nodeName = childText;		
		/*
		 * If 'R' pattern is placed before 'O' pattern then this rule must be about comparison in according to writing rules convention, and
		 * in JavaScript engine '=' operator means assigning a value, hence if the operator is '=' then it needs to be replaced with '=='.
		 * However, if 'R' pattern is placed after 'O' pattern, and this 'O' pattern is '=' then this rule is to assign a return value from
		 * a specified rule set to a variable.
		 *
		 * If the rule pattern does NOT have 'O' pattern then the rule line is about to check if the rule set returns 'true' or 'false'
		 *
		 * E.g)
		 * RULE SET: a specified rule set = 'valueToBeCompared'    <------ comparison between a return value from a rule set and a certain value
		 * a variable = RULE SET: a specified rule set             <------ assigning a return value from a specified rule set to a certain variable
		 * RULE SET: this rule returns true                        <------ during selfEvaluation(), the evaluated value would be either 'true' or 'false'
		 */
		

		if(assigningReturnValue) // this RuleSetLine is to assign the its return value to a variable of a RuleSetLine.
		{
			Pattern ruleSetPattern = Pattern.compile("^(.*)([<>=]+)(RULE SET:)(.*)");
			Matcher matcher = ruleSetPattern.matcher(childText);
			
			if(matcher.find() == true)
			{	
				this.variableName = matcher.group(1).trim();
			}
		}
		else // this RuleSetLine is for comparison, or it is just statement.
		{
			int operatorIndex = tokens.tokensStringList.indexOf("O");
			Pattern ruleSetPattern = Pattern.compile("^(RULE SET:)(.+)([<>=]*)(.*)");
			Matcher matcher = ruleSetPattern.matcher(childText);

			if(matcher.find() == true)
			{
				this.variableName = matcher.group(2).trim(); // variableName is a name of a rule set

				if(operatorIndex != -1)
				{
					this.operator = tokens.tokensList.get(operatorIndex).matches("=")?"==":tokens.tokensList.get(operatorIndex);
				}
			}
		}
//		this.lhs = variableName;
		
		int tokensStringListSize = tokens.tokensStringList.size();
		String lastToken = tokens.tokensList.get(tokensStringListSize-1);
		String lastTokenString = tokens.tokensStringList.get(tokensStringListSize-1);
		this.setValue(lastTokenString, lastToken);
		this.rhs = this.value;
		
	}

	@Override
	public LineType getLineType() {
		
		return LineType.RULE_SET;
	}

	@Override
	public FactValue selfEvaluate(HashMap<String, FactValue> workingMemory, ScriptEngine nashorn) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private boolean isAssigningReturnValue()
	{
		//if 'tokens.tokenString' starts with 'R' then the RuleSetLine is for comparison
		//if 'tokens.tokenString' does NOT start with 'R' then the RuleSetLine is to assign the its return value to a variable of a RuleSetLine.
		int operatorIndex = tokens.tokensStringList.indexOf("O");
		return !((tokens.tokensString.charAt(0) == 'R') && (operatorIndex != -1));
	}
}

