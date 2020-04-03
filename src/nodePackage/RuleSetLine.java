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
		 * this line pattern is as (^[RML]+)(O)([MLNoDaDeHaUrlId]*$)
		 */
		
		this.nodeName = childText;		
		/*
		 * In JavaScript engine '=' operator means assigning a value, hence if the operator is '=' then it needs to be replaced with '=='. 
		 */
		int operatorIndex = tokens.tokensStringList.indexOf("O");
		this.operator = tokens.tokensList.get(operatorIndex).matches("=")?"==":tokens.tokensList.get(operatorIndex);
		
		if(operator.equals("=="))
		{
			
		}
		else
		{
			this.variableName = childText.split(this.operator)[0].trim();
		}
		
		if(assigningReturnValue) // this RuleSetLine is for comparison
		{
			Pattern ruleSetPattern = Pattern.compile("^(.*)([<>=]*)(RULE SET:)(.[^<>=]*)([<>=]*)(.*)");
			Matcher matcher = ruleSetPattern.matcher(childText);
			
			if(matcher.find() == true)
			{	
				this.variableName = matcher.group(4).trim();
			}
		}
		else // this RuleSetLine is to assign the its return value to a variable of a RuleSetLine.
		{
			Pattern ruleSetPattern = Pattern.compile("^(.*)([<>=]*)(RULE SET:)(.[^<>=]*)([<>=]*)(.*)");
			Matcher matcher = ruleSetPattern.matcher(childText);
			
			if(matcher.find() == true)
			{	
				this.variableName = matcher.group(2).trim();
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
 
		return !(tokens.tokensString.charAt(0) == 'R');
	}
}

