package testingPackage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import factValuePackage.FactBooleanValue;
import factValuePackage.FactValue;
import nodePackage.DependencyType;
import ruleParser.Tokenizer;
import ruleParser.Tokens;

public class DeanTest {

	public static void main(String[] args) {
		FactBooleanValue fbv = FactValue.parse(true);
		System.out.println("fbv: "+fbv.getValue());
	
		System.out.println("fbv negation: "+fbv.negatingValue());
		System.out.println("fbv: "+fbv.getValue());

		int dp = 3;
		System.out.println((dp&(DependencyType.getNot()|DependencyType.getKnown())) == (DependencyType.getNot()|DependencyType.getKnown()));
		
		String str = "\"double quoted\"";
		Pattern p = Pattern.compile("(\")(.*)(\")");
		Matcher m = p.matcher(str);
		m.find();
		System.out.println(m.group(2));
		
	}

}
