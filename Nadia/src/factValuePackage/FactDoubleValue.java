package factValuePackage;


public class FactDoubleValue extends FactValue {
	
	private double value;
	private Double defaultValue; // it is a Double Object type because if it is a primitive type then default value is already defined as '0.0'
	
	public FactDoubleValue(double d) {
		setValue(d);
	}

	public void setValue(double d) {
		this.value = d;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Double getValue() {
		return value;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Double getDefaultValue()
	{			
		return this.defaultValue;
	}
	
	@Override
	public FactValueType getType() {
		return FactValueType.DECIMAL;
	}
	
	@Override
	public <T> void setDefaultValue(T defaultValue) {
		this.defaultValue = (Double)defaultValue;		
	}

}
