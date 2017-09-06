package factValuePackage;


public class FactURLValue extends FactValue{

	
	private String value;
	private String defaultValue;
	
	public FactURLValue(String url) {
		setValue(url);
	}



	public void setValue(String url) {
		this.value = url;
	}
	

	
	@Override
	public FactValueType getType() {
		return FactValueType.URL;
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
