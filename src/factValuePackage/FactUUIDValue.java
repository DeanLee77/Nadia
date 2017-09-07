package factValuePackage;



public class FactUUIDValue extends FactValue{

	
	private String value;
	private String defaultValue;
	
	public FactUUIDValue(String uuid) {
		setValue(uuid);
	}



	public void setValue(String uuid) {
		this.value = uuid;
	}
	

	
	@Override
	public FactValueType getType() {
		return FactValueType.UUID;
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
