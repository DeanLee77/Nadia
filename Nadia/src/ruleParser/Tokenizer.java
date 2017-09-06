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
	Pattern iteratePattern = Pattern.compile("^(ITERATE:([\\s]*)LIST OF)(.)");
	Pattern upperMatch = Pattern.compile("^([:\\p{Upper}_\\s]+(?!\\p{Lower}))"); 
	Pattern lowerMatch = Pattern.compile("^((?!(ht|f)tp(s)?:\\/\\/www\\.)([\\p{Lower}-'\\s]+(?!\\d)))");
	Pattern mixedMatch = Pattern.compile("^(\\p{Upper}[\\p{Lower}-'\\s]+)+");		
	Pattern operatorPattern = Pattern.compile("^([<>=]+)");
	Pattern calculationPattern = Pattern.compile("^(\\()([\\s|([\\d]+)(?!/.)|\\w|\\W]*)(\\))");
	Pattern numberPattern = Pattern.compile("^(\\d+)(?!/|\\.|\\d)+");
	Pattern decimalNumberPattern = Pattern.compile("^([\\d]+\\.\\d+)(?!\\d)");
	Pattern datePattern = Pattern.compile("^([0-2]?[0-9]|3[0-1])/(0?[0-9]|1[0-2])/([0-9][0-9])?[0-9][0-9]|^([0-9][0-9])?[0-9][0-9]/(0?[0-9]|1[0-2])/([0-2]?[0-9]|3[0-1])");
	Pattern urlPattern = Pattern.compile( "^((ht|f)tp(s?):\\/\\/www\\.)(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);
	Pattern uuidPattern = Pattern.compile("[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}");
	Pattern hashPattern = Pattern.compile("^([-]?)([0-9a-f]*$)(?!\\-)*");
	/*
	 * the order of Pattern in the array of 'matchPatterns' is extremely important because some patterns won't work if other patterns are invoked earlier than them
	 * especially 'I' pattern
	 */
	Pattern matchPatterns[] = {spaceMatch, iteratePattern, mixedMatch, upperMatch, lowerMatch, operatorPattern, calculationPattern, numberPattern, decimalNumberPattern, datePattern, urlPattern, uuidPattern, hashPattern};
	String tokenType[] = {"S", "I", "M", "U", "L", "O", "C", "No", "De", "Da", "Url", "Id", "Ha"};
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
				
				text = text.substring(matcher.end());
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
