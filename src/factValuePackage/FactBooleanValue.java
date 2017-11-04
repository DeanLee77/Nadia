package factValuePackage;


public class FactBooleanValue<T> extends FactValue{
	
	private boolean value;
	private Boolean defaultValue; // it is a Boolean Object type because if it is a primitive type then default value is already defined as 'false' 
	
	public FactBooleanValue(boolean booleanValue) {
		setValue(booleanValue);
	}


	public void setValue(boolean booleanValue) {
		this.value = booleanValue;
	}
	
	public FactValue negatingValue()
	{
		return FactValue.parse(!this.value);
	}
	
	@Override
	public FactValueType getType() {
		return FactValueType.BOOLEAN;
	}


	@SuppressWarnings("hiding")
	@Override
	public <T> void setDefaultValue(T defaultValue) {
		this.defaultValue = (Boolean)defaultValue;		
	}

	@SuppressWarnings("unchecked")
	@Override
	public Boolean getValue() {
		return value;

	}

	@SuppressWarnings("unchecked")
	@Override
	public Boolean getDefaultValue() {
		return defaultValue;
	}
}
