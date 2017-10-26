package testingPackage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

import factValuePackage.FactBooleanValue;
import factValuePackage.FactValue;
import nodePackage.DependencyType;
import ruleParser.RuleSetParser;
import ruleParser.RuleSetReader;
import ruleParser.RuleSetScanner;
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
		
		Pattern pt = Pattern.compile("^[\\d\\-()\\s\\+\\\\]*$");
		String phone = "(+23)423\\12312";
		Matcher ma= pt.matcher(phone);
		String test = "^[\\d\\-()\\s\\+]*$";
		System.out.println(ma.find());
		System.out.println(phone.replaceAll("[\\-\\(\\)\\s\\+\\\\]", ""));
		
		String s = "OR OPTIONALLY NOT KNOWN we have the person's passport";
		System.out.println(s.trim().replaceAll("^(OR\\s?|AND\\s?)(MANDATORY|OPTIONALLY|POSSIBLY)?(\\sNOT|\\sKNOWN)*", ""));
				
		System.out.println(4|2);
//		RuleSetReader ilr = new RuleSetReader();
//		ilr.setStreamSource(TopoSortingTest.class.getResourceAsStream("testing NOT and KNOWN.txt"));
//		RuleSetParser isf = new RuleSetParser();		
//		RuleSetScanner rsc = new RuleSetScanner(ilr,isf);
//		rsc.scanRuleSet();
		
	}

}
