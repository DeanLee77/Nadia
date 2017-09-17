package testingPackage;

public class DeanTest {

	public static void main(String[] args) {
		String testStr = "INPUT Reception Fruits AS LIST";
		System.out.println(testStr.matches("(.*)(AS LIST)"));
		
		String testStr2 = "ITEM Rock ITEMmelon";
		System.out.println("test2: "+testStr2.replace("ITEM","").trim());
		

	}

}
