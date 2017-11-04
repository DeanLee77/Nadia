package factValuePackage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FactDefiStringValue extends FactValue {
	
	private String value;
	private String defaultValue;
	private Pattern pattern = Pattern.compile("(\")(.*)(\")");
	private Matcher matcher;
	
	public FactDefiStringValue(String s) {
		matcher = pattern.matcher(s);
		if(matcher.find())
		{
			setValue(matcher.group(2));
		}
		else
		{			
			throw new IllegalStateException("FactDefiStringValue cannot be initiated");
		}
	}


	public void setValue(String s) {
		this.value = s;
	}
	
	
	@Override
	public FactValueType getType() {
		return FactValueType.DEFI_STRING;
	}

	@Override
	public <T> void setDefaultValue(T defaultValue) {
		this.defaultValue = (String)defaultValue;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getValue() {
		// TODO Auto-generated method stub
		return this.value;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getDefaultValue() {
		// TODO Auto-generated method stub
		return this.defaultValue;
	}

}

