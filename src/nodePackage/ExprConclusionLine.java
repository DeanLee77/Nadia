package nodePackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import factValuePackage.FactValue;
import ruleParser.Tokenizer;
import ruleParser.Tokens;



public class ExprConclusionLine extends Node{

	
	public ExprConclusionLine(String parentText, Tokens tokens) {
		super(parentText, tokens);
	}

	@Override
	public void initialisation(String parentText, Tokens tokens) {
		this.nodeName = parentText;
		String[] tempArray = parentText.split("IS CALC"); 
		variableName = tempArray[0].trim();
		int indexOfCInTokensStringList = tokens.tokensStringList.indexOf("C");
		this.setValue(tokens.tokensStringList.get(indexOfCInTokensStringList).trim(), tokens.tokensList.get(indexOfCInTokensStringList).trim());
	}

	
	
	
	@Override
	public LineType getLineType()
	{
		return LineType.EXPR_CONCLUSION;
	}
	
	@Override
	public FactValue selfEvaluate(HashMap<String, FactValue> workingMemory, ScriptEngine nashorn, int nodeOption)
	{
		/*
		 * calculation can only handle int, double(long) and difference in years between two dates at the moment.
		 * if difference in days or months is required then new 'keyword' must be introduced such as 'Diff Years', 'Diff Days', or 'Diff Months'
		 */
		String valueOfFactValue = (String)this.getFactValue().getValue();
		Pattern pattern = Pattern.compile("[-+/*()0-9?:;,.\"](\\s*)");
		Pattern datePattern = Pattern.compile("([0-2]?[0-9]|3[0-1])/(0?[0-9]|1[0-2])/([0-9][0-9])?[0-9][0-9]|([0-9][0-9])?[0-9][0-9]/(0?[0-9]|1[0-2])/([0-2]?[0-9]|3[0-1])");

		/*
		 * logic for this is as follows;
		 * 	1. replace all variables with actual values from 'workingMemory'
		 *  2. find out if equation is about date (difference in years) calculation or not
		 *  3. if it is about date then call 'java.time.LocalDate'and 'java.time.temporal.ChronoUnit' package then do the calculation
		 *  3-1. if it is about int or double(long) then use plain Javascript
		 *  
		 */
		
		String script = valueOfFactValue;
		final String tempScript = script;

		if( valueOfFactValue.matches(pattern.toString()))
		{
			Arrays.asList(valueOfFactValue.split(pattern.toString())).stream().forEachOrdered(item -> {
				tempScript.replaceAll(item.trim(), workingMemory.get(item.trim()).toString().trim());
			});
		}
		
		Matcher dateMatcher = datePattern.matcher(tempScript);
		List<String> dateStringList = new ArrayList<>();
		while(dateMatcher.find())
		{
			dateStringList.add(dateMatcher.group());
		}
		// if dateStringList.size() == 0 then there is no string in date format
		script = tempScript;  
		if(dateStringList.size() != 0) // case of date calculation
		{
			String[] date1Array = dateStringList.get(0).trim().split("/");
			String[] date2Array = dateStringList.get(1).trim().split("/");
			script = "var localDate = java.time.LocalDate; var chronoUnit = java.time.temporal.ChronoUnit; var diffYears = chronoUnit.YEARS.between(localDate.of("+date1Array[0].trim()+","+date1Array[1].trim()+","+date1Array[2].trim()+"), localDate.of("+date2Array[0].trim()+","+date2Array[1].trim()+","+date2Array[2].trim()+")); diffYears;";
		}
//		else // case of int or double calculation
//		{
//			 don't need to do anything due to script itself can be evaluated as it is
//		}
		

		FactValue returnValue = null;

		try {
				String nashornResult = nashorn.eval(script).toString();
				switch(Tokenizer.getTokens(nashornResult).tokensString)
				{
					case "No":
						returnValue = FactValue.parse(Integer.parseInt(nashornResult));
						break;
					case "De":
						returnValue = FactValue.parse(Double.parseDouble(nashornResult));
						break;
//				    there is no function for outcome to be a date at the moment	 E.g. The determination IS CALC (enrollment date + 5 days)
//					case "Da":
//						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//			    		LocalDate factValueInDate = LocalDate.parse(nashornResult, formatter);
//			    		returnValue = FactValue.parse(factValueInDate);
//						break;
					default:
						if(this.isBoolean(nashornResult))
						{
							returnValue = FactValue.parse(nashornResult);
						}
						else 
						{
							returnValue = FactValue.parse(nashornResult);
						}
						break;
						
				}
		} catch (ScriptException e) {
			e.printStackTrace();
		}
		return returnValue;
	}

}
