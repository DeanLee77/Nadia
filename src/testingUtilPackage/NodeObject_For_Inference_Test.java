package testingUtilPackage;

import java.util.Random;

public class NodeObject_For_Inference_Test {
	private String name;
	private String[] valueArray;
	private String value;
	
	public NodeObject_For_Inference_Test(String name, String[] valueArray)
	{
		this.name = name;
		this.valueArray = valueArray;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getValueArray() {
		return valueArray;
	}

	public void setValueArray(String[] valueArray) {
		this.valueArray = valueArray;
	}
	
	public String getValue() {
		Random rand = new Random(); 
		this.value = this.valueArray[rand.nextInt(2)]; 
		return value;
	}
}
