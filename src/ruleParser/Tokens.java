package ruleParser;

import java.util.List;

public class Tokens 
{
	public List<String> tokensList;
	public List<String> tokensStringList;
	public String tokensString;
	
	public Tokens(List<String> tl, List<String> tsl, String ts) {
		tokensList = tl;
		tokensStringList = tsl;		
		tokensString = ts;
	}
	
}
