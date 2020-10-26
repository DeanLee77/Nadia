package ruleParser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Tokenizer { 

public static Tokens getTokens(String text)
{
	
	List<String> tokenStringList = new ArrayList<>();
	List<String> tokenList = new ArrayList<>();
	String tokenString="";
	
	Pattern spaceMatch = Pattern.compile("^\\s+");
	Pattern ruleSetPattern = Pattern.compile("^(RULE SET:)");
	Pattern iteratePattern = Pattern.compile("^(ITERATE:([\\s]*)LIST OF)(.)");
	Pattern upperMatch = Pattern.compile("^([:'\\’,\\.\\p{Upper}_\\s]+(?!\\p{Lower}))"); 
	Pattern lowerMatch = Pattern.compile("^([\\p{Lower}-'\\’,\\.\\s]+(?!\\d))");
	Pattern mixedMatch = Pattern.compile("^(\\p{Upper}[\\p{Lower}-'\\’,\\.\\s]+)+");		
	Pattern operatorPattern = Pattern.compile("^([<>=]+)");
	Pattern calculationPattern = Pattern.compile("^(\\()([\\s+|([\\d]+)(?!/.)|\\w|\\W]*)(\\))");
	Pattern numberPattern = Pattern.compile("^(\\d+)(?!\\w|\\-|/|\\.|\\d)+");
	Pattern decimalNumberPattern = Pattern.compile("^([\\d]+\\.\\d+)(?!\\d)");
	Pattern datePattern = Pattern.compile("^([0-2]?[0-9]|3[0-1])/(0?[0-9]|1[0-2])/([0-9][0-9])?[0-9][0-9]|^([0-9][0-9])?[0-9][0-9]/(0?[0-9]|1[0-2])/([0-2]?[0-9]|3[0-1])");
	Pattern urlPattern = Pattern.compile( "^(ht|f)tp[s*]?\\:(\\p{Graph}|\\p{XDigit}|\\p{Space})*$");
	Pattern uuidPattern = Pattern.compile("^[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}");
	Pattern hashPattern = Pattern.compile("^([-]?)([0-9a-f]{10,}$)(?!\\-)*");
	Pattern quotedPattern = Pattern.compile("^([\"\\“])(.*)([\"\\”])(\\.)*");
	/*
	 * the order of Pattern in the array of 'matchPatterns' is extremely important because some patterns won't work if other patterns are invoked earlier than them
	 * especially 'I' pattern. 'I' pattern must come before 'U' pattern, 'Url' pattern must come before 'L' pattern with current patterns.
	 */
	Pattern matchPatterns[] = {spaceMatch, quotedPattern, ruleSetPattern, iteratePattern, mixedMatch, upperMatch, urlPattern, operatorPattern, calculationPattern, numberPattern, hashPattern, decimalNumberPattern, datePattern, uuidPattern, lowerMatch};
	String tokenType[] = {"S", "Q", "R", "I", "M", "U", "Url", "O", "C", "No", "Ha", "De", "Da", "Id", "L"};
	int textLength = text.length();
	
	while(textLength!=0) {
	
		for(int i = 0; i < matchPatterns.length; i++) {
			
			Pattern p =  matchPatterns[i];
			Matcher matcher = p.matcher(text);
					
			if(matcher.find() == true)
			{	
				String group = matcher.group();
				
				// ignore space tokens
				if(!tokenType[i].equals("S"))
				{						
					tokenStringList.add(tokenType[i]);
					tokenList.add(group.trim());
					tokenString += tokenType[i];
				}
				
				text = text.substring(matcher.end()).trim();
				textLength = text.length();
				break;
			}
			if(i >= matchPatterns.length-1)
			{
				textLength =0;
				tokenString = "WARNING";
			}
		}
	
	}		
	
	Tokens tokens = new Tokens(tokenList, tokenStringList, tokenString);		
	
	return tokens;
	
}


}
