package nodePackage;

import java.util.HashMap;

import javax.script.ScriptEngine;
import javax.script.ScriptException;


import factValuePackage.*;
import ruleParser.Tokens;


public class ComparisonLine extends Node{
	
	private String operator;
	private String lhs;
	private FactValue rhs;
	
	public ComparisonLine(String childText, Tokens tokens)
	{
		super(childText, tokens);
		
	}
	
	@Override
	public void initialisation(String childText, Tokens tokens) {

		/*
		 * this line pattern is as (^[ML]+)(O)([MLNoDaDeHaUrlId]*$)
		 */
		
		this.nodeName = childText;		
		this.variableName = tokens.tokensList.get(0);	
		this.lhs = variableName;
		/*
		 * In javascript engine '=' operator means assigning a value, hence if the operator is '=' then it needs to be replaced with '=='. 
		 */
		int operatorIndex = tokens.tokensStringList.indexOf("O");
		this.operator = tokens.tokensList.get(operatorIndex).matches("=")?"==":tokens.tokensList.get(operatorIndex);
		
		
		int tokensStringListSize = tokens.tokensStringList.size();
		String lastToken = tokens.tokensList.get(tokensStringListSize-1);
		String lastTokenString = tokens.tokensStringList.get(tokensStringListSize-1);
		this.setValue(lastTokenString, lastToken);
		this.rhs = this.value;
		
		
	}

	
	
	public String getRuleName()
	{
		return this.nodeName;
	}
	
	public String getLHS()
	{
		return this.lhs;
	}
	
	public FactValue getRHS()
	{
		return this.rhs;
	}
	
	@Override
	public LineType getLineType()
	{
		return LineType.COMPARISON;
	}
	
	@Override
	public FactValue selfEvaluate(HashMap<String, FactValue> workingMemory, ScriptEngine nashorn)
	{
		
		/*
		 * Negation type can only be used for this line type
		 * 
		 */		
		
		FactValue workingMemoryLhsValue = workingMemory.containsKey(this.variableName)?workingMemory.get(this.variableName):null;
		FactValue workingMemoryRhsValue = this.getRHS().getType().equals(FactValueType.STRING)?
											workingMemory.get(this.getFactValue().getValue().toString())
											:
											this.getRHS();
		
		String script = "";
		
		/*
		 * There will NOT be the case of that workingMemoryRhsValue is null because the node must be in following format;
		 * - A = 12231 (int or double)
		 * - A = Adam sandler (String)
		 * - A = 11/11/1977 (Date)
		 * - A = 123123dfae1421412aer(Hash)
		 * - A = 1241414-12421312-142421312(UUID)
		 * - A = true(Boolean)
		 * - A = www.aiBrain.com(URL)
		 * - A = B(another variable) 
		 */
		
		/*
		 * if it is about date comparison then string of 'script' needs rewriting
		 */
		if((workingMemoryLhsValue!= null && workingMemoryLhsValue.getType().equals(FactValueType.DATE)) || (workingMemoryRhsValue!= null && workingMemoryRhsValue.getType().equals(FactValueType.DATE)))
		{
			if(workingMemoryRhsValue != null && workingMemoryLhsValue != null)
			{
				script = "var localDate = java.time.LocalDate; localDate.of("+((FactDateValue)workingMemoryLhsValue).getValue().getYear()+","+((FactDateValue)workingMemoryLhsValue).getValue().getMonthValue()+","+((FactDateValue)workingMemoryLhsValue).getValue().getDayOfMonth()+") "+operator+" localDate.of("+((FactDateValue)workingMemoryRhsValue).getValue().getYear()+","+((FactDateValue)workingMemoryRhsValue).getValue().getMonthValue()+","+((FactDateValue)workingMemoryRhsValue).getValue().getDayOfMonth()+");" ;
			}
			else if(workingMemoryRhsValue == null)
			{
				script = "var localDate = java.time.LocalDate; localDate.of("+((FactDateValue)workingMemoryLhsValue).getValue().getYear()+","+((FactDateValue)workingMemoryLhsValue).getValue().getMonthValue()+","+((FactDateValue)workingMemoryLhsValue).getValue().getDayOfMonth()+") "+operator+" localDate.of("+((FactDateValue)this.getFactValue()).getValue().getYear()+","+((FactDateValue)this.getFactValue()).getValue().getMonthValue()+","+((FactDateValue)this.getFactValue()).getValue().getDayOfMonth()+");" ;
			}
		}
		else
		{
			if(workingMemoryRhsValue != null && workingMemoryLhsValue != null)
			{
				script = "'"+workingMemoryLhsValue.getValue().toString()+"' "+operator+" '"+workingMemoryRhsValue.getValue().toString()+"'" ;
			}
			
		}
		boolean result;
		FactValue fv = null;
		if(workingMemoryRhsValue != null && workingMemoryLhsValue != null)
		{
			try {
				result = (boolean) nashorn.eval(script);
				fv = FactValue.parse(result);
			} catch (ScriptException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
				
		return fv;
	}
}
