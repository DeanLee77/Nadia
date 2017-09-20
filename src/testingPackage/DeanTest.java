package testingPackage;

import ruleParser.Tokenizer;
import ruleParser.Tokens;

public class DeanTest {

	public static void main(String[] args) {
		String testStr = "INPUT Reception Fruits AS LIST";
		System.out.println(testStr.matches("(.*)(AS LIST)"));
		
		String testStr2 = "ITEM Rock ITEMmelon";
		System.out.println("test2: "+testStr2.replace("ITEM","").trim());
		
		String testStr3 = "UMLDa";
		System.out.println("test3: "+testStr3.matches("(^U)([LMU(Da)]+$)"));

		
		String testStr4 = "FIXED the groom's homepage IS https://www.theGroomHomepage.com.au AS URL";
		Tokens tk = Tokenizer.getTokens(testStr4);
		System.out.println("tk: "+tk.tokensString);
		
		String testStr5 = "FIXED the groom's homepage \"lowercase sentence\"";
		tk = Tokenizer.getTokens(testStr5);
		System.out.println("tk: "+tk.tokensString);
	}

}
