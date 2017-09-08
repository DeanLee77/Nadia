package ruleParser;



import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import factValuePackage.*;
import nodePackage.*;

/*
*
*  must comply with following rules;
*  1. 'A IS B' or 'A'  statement is a Value_conclsion Node
*  2. 'A IS CALC (B*(C - D) + E)' statement is a Expression_conclusion Node
*  3. 'A = B', 'A < B', 'A <= B', 'A > B' or 'A >= B' is a Comparison Node
*  4. statement not containing any keywords is a Boolean Node
*  5. any other statements containing 'INPUT' or 'FIXED' keyword are META node
*  
*  6. 'A = B' format cannot be a parent rule, in other words it must be a child statement.
*     However, 'A IS B' can be a parent so that if 'A = B' statement or item is written in a format of 'A IS B' then it can become a parent
*  	Note: if a statement is in a form of 'A is B' it is different from 'A IS B' format, and it will be a Boolean Node
*  7. Expression_conclusion Node must have a child statement containing 'NEED'(translated as 'AND') and/or 'WANT'(translated as 'OR') keyword.
*  
*  8. Comparison Node can not be a parent statement.
*  
*  9. Meta statement pattern matching is as follows;
*     9.1 ULU (U-'FIXED', L-'the gender-x is accepted', U-'IS FALSE')
*     9.2 ULUY (U-'FIXED', L-'the legislation start date', U-'IS', Y-'1/1/1988')
*     9.3 ULULOY (U- 'FIXED' L- 'the', U- 'DOB', L- 'must be', O- '>=', Y- '01/01/1988'
*     9.3 ULU (U- 'INPUT', L- 'the boy's name', U- 'AS TEXT')
*         Note: 'INPUT' type indicates which type of its value is, and the type is stated after keyword 'AS'


*/

public class RuleSetParser implements IScanFeeder {

//	enum LineType {META, VALUE_CONCLUSION, EXPR_CONCLUSION, COMPARISON, WARNING}
	
	final Pattern META_PATTERN_MATCHER = Pattern.compile("(^U)([MLU]*)([NoDaMLDeHaUrlId]*$)");
	Pattern valueConclusionMatcher; //value of this variable is different in handleParent case and handleChild case
	final Pattern EXPRESSION_CONCLUSION_MATCHER = Pattern.compile("(^[LMDa]+)(U)(C)");
	final Pattern COMPARISON_MATCHER = Pattern.compile("(^U)([MLUDa]+)(O)([MLUNoDaDeHaUrlId]*$)");
	final Pattern ITERATE_MATCHER = Pattern.compile("(^U)([MLUNoDa]+)(I)([MLU]+$)");
	final Pattern WARNING_MATCHER = Pattern.compile("WARNING");
	LineType matchTypes[] = LineType.values();
	NodeSet nodeSet = new NodeSet();
	List<Dependency> dependencyList = new ArrayList<>();
	
	@Override
	public void handleParent(String parentText, int lineNumber) {
		
		Node data = nodeSet.getNodeMap().get(parentText);
		
		if(data == null)
		{
			/*
			 * the reason for using '*' at the last group of pattern within meta and valueConclusion is that 
			 * the last group contains No, Da, De, Ha, Url, Id. 
			 * In order to track more than one character within the square bracket of last group '*' needs to be used.
			 * 
			 */
			valueConclusionMatcher = Pattern.compile("(^[LM]+)(U)?([MLNoDaDeHaUrlId]*$)(?!C)"); // parent statement must not have operators in the middle of the statement, hence there is no 'O' of Token.tokenString in the regex.
			 
			 
			Tokens tokens = Tokenizer.getTokens(parentText);
			
			Pattern matchPatterns[] = {META_PATTERN_MATCHER, valueConclusionMatcher, EXPRESSION_CONCLUSION_MATCHER, WARNING_MATCHER};
			Pattern p;
			Matcher matcher;
			for(int i = 0; i < matchPatterns.length; i++) {
				
				p =  matchPatterns[i];
				matcher = p.matcher(tokens.tokensString);
				if(matcher.find() == true) {
					switch(i) {
						case 3:  //warningMatcher case
							handleWarning(parentText);
							break;
						case 0:  //metaMatcher case
							data = new MetadataLine(parentText, tokens);
							if(data.getFactValue().getValue().equals("WARNING"))
							{
								handleWarning(parentText);
							}
							break;
						case 1:  //valueConclusionMatcher case
							data = new ValueConclusionLine(parentText, tokens);
							if(matcher.group(2) != null || tokens.tokensString.equals("L"))
							{
								String variableName = data.getVariableName();
								Node tempNode = data;
								/*
								 * following lines are to look for any nodes having a its nodeName with 'needs ' word or any operators due to the reason that
								 * the node could be used to define a node previously used as a child node for other nodes
								 */
								List<String> possibleParentNodeKeyList = nodeSet.getNodeMap().keySet().stream().filter(key -> key.matches("(.[^\\(]+)?(\\s[<>=]+\\s?)?(WANTS |NEEDS )?("+variableName+")(\\s[<>=]+)*(.[^\\)(IS)]+)*")).collect(Collectors.toList());
								if(!possibleParentNodeKeyList.isEmpty())
								{
									possibleParentNodeKeyList.stream().forEachOrdered(item -> {
										this.dependencyList.add(new Dependency(nodeSet.getNodeMap().get(item), tempNode, DependencyType.getOr())); //Dependency Type :OR
									});
								}
							}					
							
							if(data.getFactValue().getValue().equals("WARNING"))
							{
								handleWarning(parentText);
							}
							break;
						case 2:  //experessionConclusionMatcher case
							data = new ExprConclusionLine(parentText, tokens);
							String variableName = data.getVariableName();
							Node tempNode = data;
							/*
							 * following lines are to look for any nodes having a its nodeName with 'needs ' word or any operators but not having a word of 'IS' keyword due to the reason that
							 * the node could be used to define a node previously used as a child node for other nodes.
							 * However, it is excluding nodes having 'IS' keyword because if it has the keyword then it should have child nodes to define the node otherwise the entire rule set has NOT been written in correct way
							 */
							List<String> possibleParentNodeKeyList = nodeSet.getNodeMap().keySet().stream().filter(key -> key.matches("(.+)?(\\\\s[<>=]+\\\\s?)?(WANTS |NEEDS )?("+variableName+")(\\s*[<>=]*)(\\s+.[^(IS)]*)*")).collect(Collectors.toList());
							if(!possibleParentNodeKeyList.isEmpty())
							{
								possibleParentNodeKeyList.stream().forEachOrdered(item -> {
									this.dependencyList.add(new Dependency(nodeSet.getNodeMap().get(item), tempNode, DependencyType.getOr())); //Dependency Type :OR
								});
							}
							if(data.getFactValue().getValue().equals("WARNING"))
							{
								handleWarning(parentText);
							}
							break;
						default:
							handleWarning(parentText);
							break;				
					
					}
					data.setNodeLine(lineNumber);
					if(data.getLineType().equals(LineType.META))
					{
						if(((MetadataLine)data).getMetaType().equals(MetaType.INPUT))
						{
							this.nodeSet.getInputMap().put(data.getVariableName(), data.getFactValue());
						}
						else if(((MetadataLine)data).getMetaType().equals(MetaType.FIXED))
						{
							this.nodeSet.getFactMap().put(data.getVariableName(), data.getFactValue());
						}
					}
					else
					{
						this.nodeSet.getNodeMap().put(data.getNodeName(), data);
						this.nodeSet.getNodeIdMap().put(data.getNodeId(), data.getNodeName());
						this.dependencyList.add(new Dependency(data, data, 0));
					}
					break;
				}
			}	 
		}			
	}

	@Override
	public void handleChild(String parentText, String childText, int lineNumber) {
		/*
		 * the reason for using '*' at the last group of pattern within comparison is that 
		 * the last group contains No, Da, De, Ha, Url, Id. 
		 * In order to track more than one character within the square bracket of last group '*'(Matches 0 or more occurrences of the preceding expression) needs to be used.
		 * 
		 */
		Tokens tokens = Tokenizer.getTokens(childText);   
		
		int dependencyType = 0; 
		
		String firstTokenString = tokens.tokensList.get(0);
		if(firstTokenString.matches("^(AND\\s?).*")) 
		{
			dependencyType = DependencyType.getAnd();
		}
		else if(firstTokenString.matches("^(OR\\s?).*"))
		{
			dependencyType = DependencyType.getOr(); 
		}
		childText = childText.replaceFirst("OR(?=\\s)|AND(?=\\s)", "").trim();

		if(firstTokenString.matches("^(AND\\s|OR\\s)(MANDATORY\\s).*")) 
		{
			dependencyType |= DependencyType.getMandatory() ; 
		}
		else if(firstTokenString.matches("^(AND\\s|OR\\s)(OPTIONAL\\s).*"))
		{
			dependencyType |= DependencyType.getOptional(); 
		}
		else if(firstTokenString.matches("^(AND\\s|OR\\s)(POSSIBLE\\s).*"))
		{
			dependencyType |= DependencyType.getPossible(); 
		}
		childText = childText.replaceFirst("MANDATORY|OPTIONAL|POSSIBLE", "").trim();
		
		Node data = nodeSet.getNodeMap().get(childText); // remove dependencyType keywords like 'AND', 'OR', 'AND MANDATORY', and/or 'OR MANDATORY'
		
		if(data == null)
		{
			valueConclusionMatcher =Pattern.compile("(^U)([LMUDa]+$)"); // child statement for ValueConclusionLine starts with AND(OR), AND MANDATORY(OPTIONALLY, POSSIBLY) or AND (MANDATORY) (NOT) KNOWN
						
			Pattern matchPatterns[] = { valueConclusionMatcher, COMPARISON_MATCHER, ITERATE_MATCHER, WARNING_MATCHER};
			
			
			Pattern p;
			Matcher matcher;
			for(int i = 0; i < matchPatterns.length; i++) {
				
				p =  matchPatterns[i];
				matcher = p.matcher(tokens.tokensString);
						
				if(matcher.find() == true) {
					switch(i) {
						case 3:  // warningMatcher case
							handleWarning(childText);
							break;
						case 0:  // valueConclusionMatcher case
							data = new ValueConclusionLine(childText, tokens);
							
							if(data.getFactValue().getValue().equals("WARNING"))
							{
								handleWarning(parentText);
							}
							break;
						case 1:  // comparisonMatcher case
							data = new ComparisonLine(childText, tokens);
							FactValueType nodeValueType = data.getFactValue().getType();
							String valueString = data.getFactValue().getValue().toString();
							String variableName = data.getVariableName();
							Node tempNode = data;
							List<String> possibleChildNodeKeyList = nodeValueType.equals(FactValueType.STRING)? 
													nodeSet.getNodeMap().keySet().stream().filter(key -> key.matches("(^"+variableName+"\\s*)(\\sIS(.(?!IN LIST))*)*")|| key.matches("(^"+valueString+"\\s*)(\\sIS(.(?!IN LIST))*)*")).collect(Collectors.toList()):
													nodeSet.getNodeMap().keySet().stream().filter(key -> key.matches("(^"+variableName+"\\s*)(\\sIS(.(?!IN LIST))*)*")).collect(Collectors.toList());

							if(!possibleChildNodeKeyList.isEmpty())
							{
								possibleChildNodeKeyList.stream().forEachOrdered(item -> {
									this.dependencyList.add(new Dependency(tempNode, nodeSet.getNodeMap().get(item), DependencyType.getOr())); //Dependency Type :OR
								});
							}
							if(data.getFactValue().getValue().equals("WARNING"))
							{
								handleWarning(parentText);
							}
							break;
						case 2:  // iterateMatcher case
							data = new IterateLine(childText, tokens);
							if(data.getFactValue().getValue().equals("WARNING"))
							{
								handleWarning(parentText);
							}
							break;				
					
					}
					data.setNodeLine(lineNumber);
					this.nodeSet.getNodeMap().put(data.getNodeName(), data);
					this.nodeSet.getNodeIdMap().put(data.getNodeId(), data.getNodeName());
					break;

				}
			}
			
			int nodeOption = 0;

			if(firstTokenString.contains("NOT") | firstTokenString.contains("KNOWN"))
			{
				if(firstTokenString.contains("NOT"))
				{
					nodeOption |= DependencyType.getNot(); 
				}
				
				if(firstTokenString.contains("KNOWN"))
				{
					nodeOption |= DependencyType.getKnown(); 
				}
			}
			this.dependencyList.add(new Dependency(data, data, nodeOption));
		}
		
		this.dependencyList.add(new Dependency(this.nodeSet.getNode(parentText),data,dependencyType));
		

	}
	
	@Override
	public void handleNeedWant(String parentText, String childText, int lineNumber)
	{
		Tokens tokens = Tokenizer.getTokens(childText);
		
		int dependencyType = 0; 
		String firstTokens = tokens.tokensList.get(0).trim(); 
		String nodeNameToLookFor = "";
		if(firstTokens.equals("OR WANTS"))
		{
			dependencyType = DependencyType.getOr(); // OR;
		}
		else if(firstTokens.equals("AND MANDATORY NEEDS"))
		{
			dependencyType = DependencyType.getMandatory() | DependencyType.getAnd();  //  MANDATORY_AND;
		}

		nodeNameToLookFor = childText.replaceFirst("OR(?=\\s)|AND MANDATORY", "").trim();

		Node data = nodeSet.getNodeMap().get(nodeNameToLookFor); // replace dependencyType keywords like 'AND', 'OR', 'AND MANDATORY', and/or 'OR MANDATORY'

		while(data == null)
		{
			tokens = Tokenizer.getTokens(childText);
			data = new ValueConclusionLine(nodeNameToLookFor, tokens);
			if(data.getFactValue().getValue().equals("WARNING"))
			{
				handleWarning(parentText);
				break;
			}
			else
			{
				final String variableName = data.getVariableName();
				final Node tempNode = data;
				
				List<String> possibleChildNodeKeyList = nodeSet.getNodeMap().keySet().stream().filter(key -> key.matches("("+variableName+ ")(\\sIS)*(.[^<>=]+)*")).collect(Collectors.toList());
				if(!possibleChildNodeKeyList.isEmpty())
				{
					possibleChildNodeKeyList.stream().forEachOrdered(item -> {
						this.dependencyList.add(new Dependency(tempNode, nodeSet.getNodeMap().get(item), DependencyType.getOr()));
					});
				}
				
				List<String> possibleParentNodeKeyList = nodeSet.getNodeMap().keySet().stream().filter(key -> key.matches("(.+)?(\\\\s[<>=]*)?("+variableName+")(\\s[<>=]*)(.[^(IS)]+)")).collect(Collectors.toList());
				if(!possibleParentNodeKeyList.isEmpty())
				{
					possibleParentNodeKeyList.stream().forEachOrdered(item -> {
						this.dependencyList.add(new Dependency(nodeSet.getNodeMap().get(item), tempNode, DependencyType.getOr()));
					});
				}
			}
			this.dependencyList.add(new Dependency(data,data,dependencyType));
			
		}
		data.setNodeLine(lineNumber);
		this.nodeSet.getNodeMap().put(data.getNodeName(), data);
		this.nodeSet.getNodeIdMap().put(data.getNodeId(), data.getNodeName());
		this.dependencyList.add(new Dependency(this.nodeSet.getNode(parentText),data,dependencyType));

	}
	
	@Override
	public void handleListItem(String parentText, String itemText) {
		Tokens tokens = Tokenizer.getTokens(itemText);
		FactValue fv;
		if(tokens.tokensString.equals("Da"))
		{
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
     		LocalDate factValueInDate = LocalDate.parse(itemText, formatter);
     		fv = FactValue.parse(factValueInDate);
		}
		else if(tokens.tokensString.equals("De"))
		{
			fv = FactValue.parse(Double.parseDouble(itemText));
		}
		else if(tokens.tokensString.equals("No"))
		{
			fv = FactValue.parse(Integer.parseInt(itemText));
		}
		else if(tokens.tokensString.equals("Ha"))
		{
			fv = FactValue.parseHash(itemText);
		}
		else if(tokens.tokensString.equals("Url"))
		{
			fv = FactValue.parseURL(itemText);
		}
		else if(tokens.tokensString.equals("Id"))
		{
			fv = FactValue.parseUUID(itemText);
		}
		else if(itemText.matches("FfAaLlSsEe")||itemText.matches("TtRrUuEe"))
		{
			fv =FactValue.parse(Boolean.parseBoolean(itemText));
		}
		else
		{
			fv = FactValue.parse(itemText);
		}
		String stringToGetFactValue = (parentText.substring(5, parentText.indexOf("AS"))).trim();
		((FactListValue)this.nodeSet.getInputMap().get(stringToGetFactValue)).getListValue().add(fv);
	}
	
	@Override
	public void handleIterateCheck(String iterateParent, String parentText, String checkText, int lineNumber)
	{
		if(checkText.contains("NEEDS") || checkText.contains("WANTS"))
		{
			if(checkText.contains("NEEDS"))
			{
				((IterateLine)this.nodeSet.getNodeMap().get(iterateParent)).handleNeedWant(parentText, checkText.replace("NEEDS", "AND MANDATORY NEEDS"), lineNumber);

			}
			else
			{
				((IterateLine)this.nodeSet.getNodeMap().get(iterateParent)).handleNeedWant(parentText, checkText.replace("WANTS", "OR WANTS"), lineNumber);

			}
		}
		else
		{
			((IterateLine)this.nodeSet.getNodeMap().get(iterateParent)).addChild(iterateParent, parentText, checkText, lineNumber);
		}
		
	}
	
	@Override
	public NodeSet getNodeSet()
	{
		return this.nodeSet;
	}
	
	@Override
	public String handleWarning(String parentText)
	{
		return parentText+": rule format is not matched. Please check the format again";
	}
	
	/*
	 * this method is to create virtual nodes where a certain node has 'AND' or 'MANDATORY_AND', and 'OR' children at the same time.
	 * when a virtual node is created, all 'AND' children should be connected to the virtual node as 'AND' children
	 * and the virtual node should be a 'OR' child of the original parent node 
	 */
	public HashMap<String, Node> handlingVirtualNode(List<Dependency> dependencyList)
	{
		
		HashMap<String, Node> virtualNodeMap = new HashMap<>();
		

		nodeSet.getNodeMap().values().stream().forEachOrdered((node) ->{
			virtualNodeMap.put(node.getNodeName(), node);
			List<Dependency> dpList= dependencyList.stream()
							   .filter(dp -> node.getNodeName().equals(dp.getParentNode().getNodeName()))
							   .collect(Collectors.toList());
			
			/*
			 * need to handle Mandatory, optionally, possibly NodeOptions
			 */
			int and = 0;
			int mandatoryAnd = 0;
			int or = 0;
			if(!dpList.isEmpty())
			{
				for(Dependency dp: dpList)
				{
					if(dp.getChildNode().getNodeId() != dp.getParentNode().getNodeId())
					{
						if(dp.getDependencyType() == DependencyType.getAnd() 
								|| dp.getDependencyType() == (DependencyType.getMandatory() | DependencyType.getAnd()) 
								|| dp.getDependencyType() == (DependencyType.getOptional() | DependencyType.getAnd())
								|| dp.getDependencyType() == (DependencyType.getPossible() | DependencyType.getAnd()))
							{
								and++;
								if(dp.getDependencyType() == (DependencyType.getMandatory() | DependencyType.getAnd()))
								{
									mandatoryAnd++;
								}
							}
							else if(dp.getDependencyType() == DependencyType.getOr()
									|| dp.getDependencyType() == (DependencyType.getMandatory() | DependencyType.getOr()) 
									|| dp.getDependencyType() == (DependencyType.getOptional() | DependencyType.getOr())
									|| dp.getDependencyType() == (DependencyType.getPossible() | DependencyType.getOr()))
							{
								or++;
							}
					}
					
				}
				boolean hasAndOr = (and>0 && or>0)? true:false;  
				if(hasAndOr)
				{
					
					String parentNodeOfVirtualNodeName = node.getNodeName();
					Node virtualNode = new ValueConclusionLine("VirtualNode-"+parentNodeOfVirtualNodeName, Tokenizer.getTokens("VirtualNode-"+parentNodeOfVirtualNodeName));
					this.nodeSet.getNodeIdMap().put(virtualNode.getNodeId(), "VirtualNode-"+parentNodeOfVirtualNodeName);
					virtualNodeMap.put("VirtualNode-"+parentNodeOfVirtualNodeName, virtualNode);
					if(mandatoryAnd > 0)
					{
						dependencyList.add(new Dependency(node, virtualNode, (DependencyType.getMandatory() | DependencyType.getOr())));
					}
					else
					{
						dependencyList.add(new Dependency(node, virtualNode, DependencyType.getOr()));
					}
					dpList.stream()
						  .filter(dp -> dp.getDependencyType() == DependencyType.getAnd() || dp.getDependencyType() == (DependencyType.getMandatory() | DependencyType.getAnd()))
						  .forEachOrdered(dp -> dp.setParentNode(virtualNode));
				}
			}
			
		});
		return virtualNodeMap;
	}
	
	@Override
	public int[][] createDependencyMatrix()
	{
		this.nodeSet.setNodeMap(handlingVirtualNode(this.dependencyList));
		/*
		 * number of rule is not always matched with the last ruleId in Node 
		 */
		int numberOfRules = Node.getStaticNodeId();
		
		int[][] dependencyMatrix = new int[numberOfRules][numberOfRules];
	
		
		this.dependencyList.forEach(dp -> {
			int parentId = dp.getParentNode().getNodeId();
			int childId = dp.getChildNode().getNodeId();
			int dpType = dp.getDependencyType();
			dependencyMatrix[parentId][childId] = dpType;
		});
		
		return dependencyMatrix;
	}

	@Override
	public void setNodeSet(NodeSet ns)
	{
		this.nodeSet = ns;
	}

}