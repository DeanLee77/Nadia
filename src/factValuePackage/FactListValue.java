package factValuePackage;

import java.util.List;


public class FactListValue<T> extends FactValue{
	
private List<FactValue> listValue;
private FactValue defaultValue;
	
	public FactListValue(List<FactValue> i) {
		setListValue(i);
	}

	public List<FactValue> getListValue() {
		return listValue;
	}

	public void setListValue(List<FactValue> listValue) {
		this.listValue = listValue;
	}
	
	public void addFactValueToListValue(FactValue fv)
	{
		this.listValue.add(fv);
	}
	

	@Override
	public FactValueType getType() {
		return FactValueType.LIST;
	}
	
	
	@SuppressWarnings("hiding")
	@Override
	public <T> void setDefaultValue(T defaultValue) {
		
		this.defaultValue =  (FactValue)defaultValue;
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FactValue> getValue() {

		return listValue;
	}

	@SuppressWarnings("unchecked")
	@Override
	public FactValue getDefaultValue() {

		return defaultValue;
	}
	
	
	public boolean compare(FactValue factValue2) {
		boolean equal = false;
		FactValueType factValue2Type = factValue2.getType();
		if(factValue2Type.equals(FactValueType.LIST))
		{
			int factValue2ListSize = ((FactListValue)factValue2).getListValue().size();
			int trueCount = 0;
			for(int i = 0; i < factValue2ListSize; i++)
			{
				if(this.listValue.get(i).getType().equals(((FactValue) ((FactListValue)factValue2).getListValue().get(i)).getType())
						&&this.listValue.get(i).equals(factValue2))
				{
					trueCount++;
				}
			}
			if(factValue2ListSize == this.listValue.size() && trueCount == factValue2ListSize)
			{
				equal = true;
			}
			
		}
		else
		{
			System.out.println("FactListValue comparison is failed");
		}
		
		return equal;
	}

	

	

}
