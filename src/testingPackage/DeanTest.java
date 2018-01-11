package testingPackage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import factValuePackage.FactBooleanValue;
import factValuePackage.FactIntegerValue;
import factValuePackage.FactValue;
import nodePackage.DependencyType;
import nodePackage.Node;
import nodePackage.ValueConclusionLine;
import ruleParser.Tokenizer;
import ruleParser.Tokens;

public class DeanTest {

	public static void main(String[] args) {
		
		List<Node> lst = new ArrayList<>();
		
		lst.add(new ValueConclusionLine("haha",Tokenizer.getTokens("haha")));
		System.out.println(lst.get(0).getNodeName());
		lst.remove(lst.get(0));
		System.out.println(lst.size());
		
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/MM/yyyy");
		LocalDate today = LocalDate.now();
		String now = (today).format(formatter);
		System.out.println(now);
		
		String jsonString = "{\"service\":[{\"1st service\":{\"type1\":\"type1\", \"type2\":\"type2\"}},{\"2nd service\":{\"type1\":\"type3\",\"type2\":\"type4\"}}]}";
		String jsonString1 = "{\"service\":{\"1st service\":{\"type1\":\"type1\", \"type2\":\"type2\"}}}";
		ObjectMapper mapper = new ObjectMapper();
	    try {
	    		JsonNode actualObj1 = mapper.readTree(jsonString1);
			Iterator<JsonNode> serviceListIterator1 = actualObj1.path("service").elements();
			List<JsonNode> serviceList1 = new ArrayList<>();
			serviceListIterator1.forEachRemaining(serviceList1::add);
			System.out.println(serviceList1.size());
			System.out.println(serviceList1.get(0));
			System.out.println(actualObj1.get("service"));

			
			JsonNode actualObj = mapper.readTree(jsonString);
			Iterator<JsonNode> serviceListIterator = actualObj.path("service").elements();
			List<JsonNode> serviceList = new ArrayList<>();
			serviceListIterator.forEachRemaining(serviceList::add);
			System.out.println(serviceList.size());
			System.out.println(serviceList.get(1).get("2nd service").get("type1").asText());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    		
		FactBooleanValue fbv = FactValue.parse(true);
		System.out.println("fbv: "+fbv.getValue());
	
		System.out.println("fbv negation: "+fbv.negatingValue());
		System.out.println("fbv: "+fbv.getValue());

		int dp = 3;
		System.out.println((dp&(DependencyType.getNot()|DependencyType.getKnown())) == (DependencyType.getNot()|DependencyType.getKnown()));
		
		String str = "\"double quoted\"";
		Pattern p = Pattern.compile("([\"\'])(.*)([\"\'])");
		Matcher m = p.matcher(str);
		m.find();
		System.out.println("str: "+str);
		System.out.println(m.group(2));
		
		Pattern pt = Pattern.compile("^[\\d\\-()\\s\\+\\\\]*$");
		String phone = "(+23)423\\12312";
		Matcher ma= pt.matcher(phone);
		String test = "^[\\d\\-()\\s\\+]*$";
		System.out.println(ma.find());
		System.out.println(phone.replaceAll("[\\-\\(\\)\\s\\+\\\\]", ""));
		
		String s = "OR NOT we have person's name and dob";
		System.out.println(s.trim().replaceAll("^(OR\\s?|AND\\s?)(MANDATORY|OPTIONALLY|POSSIBLY)?(\\s?NOT|\\s?KNOWN)*", ""));
		String sDiff = s.replace(s.trim().replaceAll("^(OR\\s?|AND\\s?)(MANDATORY|OPTIONALLY|POSSIBLY)?(\\s?NOT|\\s?KNOWN)*", "").trim(), "").trim();
		System.out.println("sDiff: "+sDiff);
		
		String ss = "person's number is 12312213213123124";
		Tokens st = Tokenizer.getTokens(ss);
													
		System.out.println("last token: "+st.tokensList.get(st.tokensList.size()-1));
		HashMap<String, String> hm = new HashMap<>();
		hm.put("h1", "h1");
		hm.put("h2", "h2");
		hm.put("h3", "h3");
		hm.put("h4", "h4");
		hm.put("h5", "h5");
		
		HashMap<String, String> hm2 = hm;
		hm2.put("h6", "h6");
		System.out.println("hm size: "+hm.size());
		testingMap(hm2);
		System.out.println("hm size2: "+hm.size());
		
		
		FactIntegerValue fiv = FactIntegerValue.parse(12);
		System.out.println(fiv.getValue().toString());
		
		String exString = "number of drinks the person consumes a week IS CALC ( (number of drinks the person consumes an hour * hours of drinks a day+(one day/two day))*5)";
		Tokens tks = Tokenizer.getTokens(exString);
		System.out.println(tks.tokensString);
		
		LocalDate date1 = LocalDate.of(2001, 11, 1);
		LocalDate date2 = LocalDate.of(2001, 10, 31);
		System.out.println(date1.isAfter(date2));
		String script = "var localDate = Java.type(\"java.time.LocalDate\"); localDate.of(1994,12,11).isAfter(localDate.of(1990,1,1));";
		String script1 = "new Date(2017,11,1) > new Date(2017,10,31);";
		String script2 = "var localDate = java.time.LocalDate; var chronoUnit = java.time.temporal.ChronoUnit; var diffYears = chronoUnit.YEARS.between(localDate.of(1955,7,02), localDate.of(1994,4,6)); diffYears;";
		ScriptEngineManager factory = new ScriptEngineManager();
		ScriptEngine engine = factory.getEngineByName("nashorn");
		try {
			System.out.println("result: "+engine.eval(script2));
			
		} catch (ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Pattern pattern = Pattern.compile("[-+/*()0-9?:;,.\"](\\s*)");
		
		String strr = "( number of drinks the person consumes an hour * hours of drinks a day * (5-1))";
		Matcher mat = pattern.matcher(strr);
		System.out.println("matching: "+mat.find());
		
		String testStr = "this IS IN LIST that";
		System.out.println(testStr.contains("IS IN LIST"));
		
		String exString1 = "NONE person's health condition ITERATE: LIST OF person’s health check-up history";
		Tokens tks1 = Tokenizer.getTokens(exString1);
		
//		RuleSetReader ilr = new RuleSetReader();
//		ilr.setStreamSource(TopoSortingTest.class.getResourceAsStream("testing NOT and KNOWN.txt"));
//		RuleSetParser isf = new RuleSetParser();		
//		RuleSetScanner rsc = new RuleSetScanner(ilr,isf);
//		rsc.scanRuleSet();
		System.out.println("lastIndexOf: "+testStr.lastIndexOf("ST"));
		System.out.println("substring: "+testStr.substring(0, testStr.lastIndexOf("IS IN")+"IS IN".length()));
		
	}
	
	public static void testingMap(HashMap<String, String> testingMap)
	{
		testingMap.put("hm7", "hm7");
	}

}
