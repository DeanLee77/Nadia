package nodePackage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;

import factValuePackage.FactValue;
import ruleParser.Tokens;


public abstract class Node {
	
	
	protected static int staticNodeId = 0;
	protected int nodeId;
	protected String nodeName;
	protected int nodeLine;
	protected String variableName;
	protected FactValue value;
    protected Tokens tokens;
    
	public Node(String parentText, Tokens tokens)
	{
		nodeId = staticNodeId;
		staticNodeId++;
		this.tokens = tokens;

		initialisation(parentText, tokens);	
	}
	
	public abstract void initialisation(String parentText, Tokens tokens);
	public abstract LineType getLineType();
	public abstract FactValue selfEvaluate(HashMap<String, FactValue> workingMemory, ScriptEngine nashorn);
	
	public void setNodeLine(int nodeLine)
	{
		this.nodeLine = nodeLine;
	}
	public int getNodeLine()
	{
		return this.nodeLine;
	}
	
	public static int getStaticNodeId()
	{
		return staticNodeId;
	}
	public int getNodeId()
	{
		return this.nodeId;
	}
	public String getNodeName()
	{
		return this.nodeName;
	}
	public Tokens getTokens()
	{
		return this.tokens;
	}

	public String getVariableName()
	{
		return variableName;
	}
	public FactValue getFactValue()
	{
		return this.value;
	}
	
	protected void setValue(String lastTokenString, String lastToken)
	{
		switch(lastTokenString)
		{
			case "No":
				this.value = FactValue.parse(Integer.parseInt(lastToken));
				break;
			case "Do":
				this.value = FactValue.parse(Double.parseDouble(lastToken));
				break;
			case "Da":
				this.value = FactValue.parse(LocalDate.parse(lastToken, DateTimeFormatter.ofPattern("d/M/yyyy")));
				break;
			case "Url":
				this.value = FactValue.parseURL(lastToken);
				break;
			case "Id":
				this.value = FactValue.parseUUID(lastToken);
				break;
			case "Ha":
				this.value = FactValue.parseHash(lastToken);
				break;
			case "Q":
				this.value = FactValue.parseDefiString(lastToken);
				break;
			case "L":
			case "M":
			case "U":
			case "C":
				if(this.isBoolean(lastToken))
				{
					this.value = lastToken.equalsIgnoreCase("false")? FactValue.parse(false): FactValue.parse(true);
				}
				else
				{
					Pattern defiStringPattern = Pattern.compile("(^[\'\"])(.*)([\'\"]$)");
					Matcher matcher = defiStringPattern.matcher(lastToken);
					if(matcher.find())
					{
						String newS = matcher.group(2);
						this.value = FactValue.parseDefiString(newS);
					}
					else
					{
						this.value = FactValue.parse(lastToken);
					}
				}
				break;
		}
	}
	public void setValue(FactValue fv)
	{
		this.value = fv;
	}
	protected boolean isBoolean(String str)
	{
		return str.matches("[FfAaLlSsEe]+")||str.matches("[TtRrUuEe]+")? true: false;
	}
	
	protected boolean isInteger(String str)
	{
		return str.equals("No")? true: false;
	}
	
	protected boolean isDouble(String str)
	{
		return str.equals("De")?true: false;
	}
	
	protected boolean isDate(String str)
	{
		 return str.equals("Da")? true: false;
	}
	
	protected boolean isURL(String str)
	{
		return str.equals("Url")? true: false;
	}
	
	protected boolean isHash(String str)
	{
		return str.equals("Ha")? true: false;
	}
	
	protected boolean isUUID(String str)
	{
		return str.equals("Id")? true: false;
	}
}
