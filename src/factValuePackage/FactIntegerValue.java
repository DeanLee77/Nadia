package factValuePackage;


public class FactIntegerValue extends FactValue {
	
	private int value;
	private Integer defaultValue; // it is a Integer Object type because if it is a primitive type then default value is already defined as '0'
	
	public FactIntegerValue(int i) {
		setValue(i);
	}



	public void setValue(int value) {
		this.value = value;
	}
	
	

	@Override
	public FactValueType getType() {
		return FactValueType.NUMBER;
	}



	@Override
	public <T> void setDefaultValue(T defaultValue) {
		this.defaultValue = (Integer)defaultValue;
	}



	@SuppressWarnings("unchecked")
	@Override
	public Integer getValue() {
		return this.value;
	}



	@SuppressWarnings("unchecked")
	@Override
	public Integer getDefaultValue() {
		return this.defaultValue;
	}
}
