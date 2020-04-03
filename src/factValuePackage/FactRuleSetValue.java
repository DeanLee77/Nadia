package factValuePackage;


public class FactRuleSetValue extends FactValue{

	private String value; //this 'value' represents a name of a rule set.
	private String defaultValue = ""; //this 'defaultValue' also represents a name of a rule set.
	
	public FactRuleSetValue(String s) {
		setValue(s);
		setDefaultValue(s);
	}


	public void setValue(String s) {
		this.value = s;
	}
	
	@Override
	public FactValueType getType() 
	{
		return FactValueType.RULE_SET;
	}

	@Override
	public <T> void setDefaultValue(T str)
	{
		defaultValue = (String)str;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getValue() {
		return value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getDefaultValue() {
		return defaultValue;
	}

}

