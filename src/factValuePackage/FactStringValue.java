package factValuePackage;


public class FactStringValue extends FactValue {
	
	private String value;
	private String defaultValue;
	
	public FactStringValue(String s) {
		setValue(s);
	}


	public void setValue(String s) {
		this.value = s;
	}
	
	
	@Override
	public FactValueType getType() {
		return FactValueType.STRING;
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
