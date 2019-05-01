package testingPackage.inferenceEngineTest;

import factValuePackage.FactValue;

public class Fact {
	String variableName;
	FactValue fv;
	String factValueType;
	
	public Fact()
	{
		
	}
	public Fact(String variableName, FactValue fv) 
	{
		this.variableName = variableName;
		this.fv = fv;
	}
	
	public String getFactValueType()
	{
		return this.factValueType;
	}
	public void setFactValueType(String factValueType)
	{
		this.factValueType = factValueType;
	}
	
	public String getVariableName()
	{
		return this.variableName;
	}
	public void setVariableName(String variableName)
	{
		this.variableName =  variableName;
	}
	
	public FactValue getFactValue()
	{
		return this.fv;
	}
	public void setFactValue(FactValue fv)
	{
		this.fv = fv;
	}

}
