package nodePackage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;

import factValuePackage.*;
import ruleParser.*;


public class MetadataLine extends Node{
	
//	Pattern metaPatternMatcher = Pattern.compile("^ULU?[OU]?[NoDaMLDe]?");
	/*
	 * Meta type pattern list
	 * 1. ULU[NoDaMLDe]
	 * 2. U[NoDaMLDe]
	 * 3. U
	 */
	private MetaType metaType;
	private String name;
//	private FactValue value;
	public MetadataLine(String parentText, Tokens tokens)
	{
		super(parentText, tokens);
		
	}
	
	@Override
	public void initialisation(String parentText, Tokens tokens)
	{
		this.name = parentText;
		this.setMetaType(parentText);
		Pattern metaPattern;
		Matcher matcher;
		switch(this.metaType)
		{
			case FIXED:
				metaPattern = Pattern.compile("^(FIXED)(.*)(\\s[AS|IS]\\s*.*)");
				matcher = metaPattern.matcher(parentText);
				if(matcher.find())	
				{
					this.variableName = matcher.group(2).trim();
					setValue(matcher.group(3).trim(), tokens);
				}
				break;
			case INPUT:
				metaPattern = Pattern.compile("^(INPUT)(.*)(AS)(.*)[(IS)(.*)]?");
				matcher = metaPattern.matcher(parentText);
				if(matcher.find())
				{
					this.variableName = matcher.group(2).trim();
					setValue(matcher.group(4).trim(), tokens);
				}
				break;
				
		default:
			break;		
		}
	}
	
	
	public void setValue(String valueInString, Tokens tokens)
	{
		int tokenStringListSize = tokens.tokensStringList.size();
		String lastTokenString = tokens.tokensStringList.get(tokenStringListSize-1);
		String[] tempArray = valueInString.split(" ");
		String tempStr = tempArray[0];

		if(metaType.equals(MetaType.FIXED))
		{
			if(tempStr.equals("IS"))
			{
				if(this.isDate(lastTokenString))
				{
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
		      		LocalDate factValueInDate = LocalDate.parse(tempArray[1], formatter);
					this.value = FactValue.parse(factValueInDate);
				}
				else if(this.isDouble(lastTokenString))
				{
					double tempDouble = Double.parseDouble(tempArray[1]);
					this.value = FactValue.parse(tempDouble);
				}
				else if(this.isInteger(lastTokenString))
				{
					int tempInt = Integer.parseInt(tempArray[1]);
					this.value = FactValue.parse(tempInt);
				}
				else if(this.isBoolean(tempArray[1]))
				{
					this.value = tempArray[1].equalsIgnoreCase("false")? FactValue.parse(false): FactValue.parse(true);
				}
				else if(this.isHash(lastTokenString))
				{
					this.value = FactValue.parseHash(tempArray[1]);
				}
				else if(this.isURL(lastTokenString))
				{
					this.value = FactValue.parseURL(tempArray[1]);
				}
				else if(this.isUUID(lastTokenString))
				{
					this.value = FactValue.parseUUID(tempArray[1]);
				}
			}
			else if(tempStr.equals("AS"))
			{
				if(tempArray[1].equals("LIST"))
				{
					this.value = FactValue.parse(new ArrayList<FactValue>());
				}
				else
				{
					this.value = FactValue.parse("WARNING");
				}
			}
			
		}
		else if(metaType.equals(MetaType.INPUT))
		{
			if(tempArray.length > 1)
			{
				
				/*
				 * within this case 'DefaultValue' will be set due to the statement format is as follows;
				 * 'A AS 'TEXT' IS B'
				 * and 'A' is variable, 'TEXT' is a type of variable, and 'B' is a default value.
				 * if the type is 'LIST' then variable is a list then the factValue has a default value.
				 */
				String tempStr2 = tempArray[2];
				
				if(FactValueType.LIST.toString().equals(tempStr))
				{
					List<FactValue> valueList = new ArrayList<>();
					FactValue tempValue;
					if(this.isDate(lastTokenString)) // tempStr2 is date value
					{
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
			      		LocalDate factValueInDate = LocalDate.parse(tempStr2, formatter);
			      		tempValue = FactValue.parse(factValueInDate);
			      		valueList.add(tempValue);
					}
					else if(this.isDouble(lastTokenString)) //tempStr2 is double value
					{
						double tempDouble = Double.parseDouble(tempStr2);
						tempValue = FactValue.parse(tempDouble);
			      		valueList.add(tempValue);
					}
					else if(this.isInteger(lastTokenString)) //tempStr2 is integer value
					{
						int tempInt = Integer.parseInt(tempStr2);
						tempValue = FactValue.parse(tempInt);
			      		valueList.add(tempValue);
					}
					else if(this.isHash(lastTokenString)) //tempStr2 is integer value
					{
						
						tempValue = FactValue.parseHash(tempStr2);
			      		valueList.add(tempValue);

					}
					else if(this.isURL(lastTokenString)) //tempStr2 is integer value
					{
						
						tempValue = FactValue.parseURL(tempStr2);
			      		valueList.add(tempValue);

					}
					else if(this.isUUID(lastTokenString)) //tempStr2 is integer value
					{
						
						tempValue = FactValue.parseUUID(tempStr2);
			      		valueList.add(tempValue);

					}
					else if(this.isBoolean(tempStr2)) // tempStr2 is boolean value
					{
						if(tempStr2.equalsIgnoreCase("false"))
						{
							tempValue = FactValue.parse(false);

						}
						else
						{
							tempValue = FactValue.parse(true);
						}
			      		valueList.add(tempValue);

					}
					else // tempStr2 is String value
					{
						tempValue = FactValue.parse(tempStr2);
			      		valueList.add(tempValue);
					}
					
					this.value = FactValue.parse(valueList);
					this.value.setDefaultValue(tempValue);

				}
				else if(FactValueType.TEXT.toString().equals(tempStr))
				{
					this.value = FactValue.parse(tempStr2);
					this.value.setDefaultValue(tempStr2);
				}
				else if(FactValueType.DATE.toString().equals(tempStr))
				{
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
		      		LocalDate factValueInDate = LocalDate.parse(tempStr2, formatter);
		      		
		      		this.value = FactValue.parse(factValueInDate);
		      		this.value.setDefaultValue(factValueInDate);
				}
				else if(FactValueType.NUMBER.toString().equals(tempStr))
				{
					int factValueInInteger = Integer.parseInt(tempStr2);
					this.value = FactValue.parse(factValueInInteger);
					this.value.setDefaultValue(factValueInInteger);
				}
				else if(FactValueType.DECIMAL.toString().equals(tempStr))
				{
					double factValueInDouble = Double.parseDouble(tempStr2);
					this.value = FactValue.parse(factValueInDouble);
					this.value.setDefaultValue(factValueInDouble);

				}
				else if(FactValueType.BOOLEAN.toString().equals(tempStr))
				{
					if(tempStr2.equalsIgnoreCase("true"))
					{
						this.value = FactValue.parse(true);
						this.value.setDefaultValue(true);
					}
					else
					{
						this.value = FactValue.parse(false);
						this.value.setDefaultValue(false);
					}
				}
				else if(FactValueType.URL.toString().equals(tempStr))
				{
					this.value = FactValue.parseURL(tempStr2);
					this.value.setDefaultValue(tempStr2);
				}
				else if(FactValueType.HASH.toString().equals(tempStr))
				{
					this.value = FactValue.parseHash(tempStr2);
					this.value.setDefaultValue(tempStr2);
				}
				else if(FactValueType.UUID.toString().equals(tempStr))
				{
					this.value = FactValue.parseUUID(tempStr2);
					this.value.setDefaultValue(tempStr2);
				}
			}
			else 
			{
				/*
				 * case of the statement does not have value, only contains a type of the variable
				 * so that the value will not have any default values
				 */
				if(FactValueType.LIST.toString().equals(tempStr))
				{
					this.value = FactValue.parse(new ArrayList<FactValue>());
				}
				else if(FactValueType.TEXT.toString().equals(tempStr) || FactValueType.URL.toString().equals(tempStr) || FactValueType.HASH.toString().equals(tempStr) || FactValueType.UUID.toString().equals(tempStr))
				{
					this.value = FactValue.parse(" ");
				}
				else if(FactValueType.DATE.toString().equals(tempStr))
				{
					this.value = FactValue.parse(LocalDate.MIN);
				}
				else if(FactValueType.NUMBER.toString().equals(tempStr))
				{
					this.value = FactValue.parse(-1111);
				}
				else if(FactValueType.DECIMAL.toString().equals(tempStr))
				{
					this.value = FactValue.parse(-0.1111);
				}
				else if(FactValueType.BOOLEAN.toString().equals(tempStr))
				{
					this.value = FactValue.parse(Boolean.valueOf(""));
				}
				
			}
		}
		
	}
	
	public void setMetaType(String parentText)
	{
		Arrays.asList(MetaType.values()).forEach((metaType) -> {
			if(parentText.contains(metaType.toString()))
			{
				this.metaType = metaType;
			}
		});	
	}
	public MetaType getMetaType()
	{
		return this.metaType;
	}
	
	public String getName()
	{
		return this.name;
	}

	@Override
	public LineType getLineType() {
		// TODO Auto-generated method stub
		return LineType.META;
	}
	
	@Override
	public FactValue selfEvaluate(HashMap<String, FactValue> workingMemory, ScriptEngine nashorn)
	{
		FactValue fv = null;
		return fv;
	}
}
