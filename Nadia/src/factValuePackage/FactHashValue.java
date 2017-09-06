package factValuePackage;

public class FactHashValue extends FactValue{


	
	private String value;
	private String defaultValue;
	
	public FactHashValue(String hash) {
		setValue(hash);
	}



	public void setValue(String hash) {
		this.value = hash;
	}
	

	
	@Override
	public FactValueType getType() {
		return FactValueType.HASH;
	}


	@Override
	public <T> void setDefaultValue(T defaultValue) {
		this.defaultValue = (String)defaultValue;
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getValue() {
		return this.value;
	}



	@SuppressWarnings("unchecked")
	@Override
	public String getDefaultValue() {
		return this.defaultValue;
	}

}
