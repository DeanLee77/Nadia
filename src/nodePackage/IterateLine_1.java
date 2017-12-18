package nodePackage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.script.ScriptEngine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import factValuePackage.*;
import ruleParser.*;
import inferencePackage.*;



public class IterateLine_1 extends Node {
	
	private List<?> givenList = new ArrayList<>();
	private List<Boolean>outcomeList = new ArrayList<>(givenList.size());
	private String numberOfTarget;
	private HashMap<String, FactValue> iterateWorkingMemory;
	private HashMap<String, Node> iterativeNodeMap;
	private HashMap<Integer, String> iterativeNodeIdMap; 
	private List<Node> iterativeTopoSortedList = null; 
	private List<String> iterativeInclusiveList;
	private List<Dependency> iterativeDependencyList;
	private DependencyMatrix iterativeDependencyMatrix;
	private List<Node> summaryList;

	
	
	public IterateLine_1(String childText, Tokens tokens)
	{
		super(childText, tokens);
		numberOfTarget = "";
		iterateWorkingMemory = new HashMap<>();
		iterativeTopoSortedList = new ArrayList<>();
		iterativeDependencyList = new ArrayList<>();
		iterativeInclusiveList = new ArrayList<>();
		summaryList = new ArrayList<>();
		
	}
	
	public HashMap<String, Node> getIterativeNodeMap()
	{
		return this.iterativeNodeMap;
	}
	public void setIterativeNodeMap(HashMap<String, Node> iterativeNodeMap)
	{
		this.iterativeNodeMap = iterativeNodeMap;
	}
	
	public HashMap<Integer, String> getIterativeNodeIdMap()
	{
		return this.iterativeNodeIdMap;
	}
	
	public HashMap<String, FactValue> getIterateWorkingMemory()
	{
		return this.iterateWorkingMemory;
	}
	
	public void setDependencyMatrix(int[][] dependencyMatrix)
	{
		iterativeDependencyMatrix = new DependencyMatrix(dependencyMatrix);
	}
	public DependencyMatrix getIterativeDependencyMatrix()
	{
		return this.iterativeDependencyMatrix;
	}
	

	public void setGivenList(List<?> givenList)
	{
		this.givenList = givenList;
	}
	public List<?> getGivenList()
	{
		return this.givenList;
	}
	
	public List<Boolean> getOutcomeList()
	{
		return this.outcomeList;
	}
	
	public void handleNeedWant(String parentText, String childText, int lineNumber)
	{
		childText = childText.replaceAll("CHECK ", "");
		Tokens tokens = Tokenizer.getTokens(childText);
		
		int dependencyType = 0; 
		String firstTokens = tokens.tokensList.get(0).trim(); 

		if(firstTokens.equals("OR WANTS"))
		{
			dependencyType = DependencyType.getOr();
		}
		else if(firstTokens.equals("AND MANDATORY NEEDS"))
		{
			dependencyType = DependencyType.getMandatory() | DependencyType.getAnd();  //  MANDATORY_AND;
		}
		String nodeNameToLookFor = childText.replaceFirst("OR(?=\\s)|AND MANDATORY", "").trim();
		
		Node data = this.iterativeNodeMap.get(nodeNameToLookFor); // replace dependencyType keywords like 'AND', 'OR', 'AND MANDATORY', and/or 'OR MANDATORY'

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
				String variableName = data.getVariableName();
				Node tempNode = data;
				
				List<String> possibleChildNodeKeyList = this.iterativeNodeMap.keySet().stream().filter(key -> key.matches("("+variableName+ ")(\\sIS)*(.[^<>=]+)*")).collect(Collectors.toList());
				if(!possibleChildNodeKeyList.isEmpty())
				{
					possibleChildNodeKeyList.stream().forEachOrdered(item -> {
						this.iterativeDependencyList.add(new Dependency(tempNode, this.iterativeNodeMap.get(item), DependencyType.getOr()));
					});
				}
				
				List<String> possibleParentNodeKeyList = this.iterativeNodeMap.keySet().stream().filter(key -> key.matches("("+variableName+")(\\s[<>=]*)(.+)")).collect(Collectors.toList());
				if(!possibleParentNodeKeyList.isEmpty())
				{
					possibleParentNodeKeyList.stream().forEachOrdered(item -> {
						this.iterativeDependencyList.add(new Dependency(this.iterativeNodeMap.get(item), tempNode, DependencyType.getOr()));
					});
				}
			}
			
		}
		data.setNodeLine(lineNumber);
		this.iterativeNodeMap.put(data.getNodeName(), data);
		this.iterativeNodeIdMap.put(data.getNodeId(), data.getNodeName());
		this.iterativeDependencyList.add(new Dependency(data,data,dependencyType));
		this.iterativeDependencyList.add(new Dependency(this.iterativeNodeMap.get(parentText.replace("CHECK", "").trim()),data,dependencyType));

	}
	
	
	public void addChild(String iterateParent, String parentText, String checkText, int lineNumber)
	{
		int dt = checkText.contains("OR") ? DependencyType.getOr() : DependencyType.getAnd();
		Tokens tokens = Tokenizer.getTokens(checkText);
		Pattern valueConclusionMatcher =Pattern.compile("(^U)([LMU]+$)"); // child statement for ValueConclusionLine starts with AND(OR), AND MANDATORY(OPTIONALLY, POSSIBLY) or AND (MANDATORY) (NOT) KNOWN
		Pattern comparisonMatcher = Pattern.compile("(^U)([MLUDa]+)(O)([MLUNoDaDeHaUrlId]*$)"); // comparison can only be child in rule format so the pattern needs re-thinking
		Pattern exprConclusionMatcher = Pattern.compile("(^U)([LMDa]+)(U)(C)"); //ExprCheck is not required to be implemented just yet, but it will be needed later on.
		Pattern warningMatcher = Pattern.compile("WARNING");
		Pattern[] matchPatterns = {warningMatcher, valueConclusionMatcher, comparisonMatcher, exprConclusionMatcher};
		
		checkText = checkText.replaceAll("OR|AND|CHECK", "").trim();
		Node data = this.iterativeNodeMap.get(checkText.trim());
		
		if(data == null)
		{
			Pattern p;
			Matcher matcher;
			for(int i = 0; i < matchPatterns.length; i++) {
				
				p =  matchPatterns[i];
				matcher = p.matcher(tokens.tokensString);
						
				if(matcher.find() == true) {
					switch(i) {
						case 0:  // warningMatcher case
							handleWarning(checkText);
							break;
						case 1:  // valueConclusionMatcher case
							
							data = new ValueConclusionLine(checkText, tokens);
							
							break;
						case 2:  // comparisonMatcher case
							data = new ComparisonLine(checkText, tokens);
							FactValueType nodeValueType = data.getFactValue().getType();
							String valueString = data.getFactValue().getValue().toString();
							String variableName = data.getVariableName();
							Node tempNode = data;
						
							List<String> possibleChildNodeKeyList = nodeValueType.equals(FactValueType.STRING)? 
									this.iterativeNodeMap.keySet().stream().filter(key -> key.matches("(^"+variableName+"\\s*)(\\sIS(.(?!IN LIST))*)*")|| key.matches("(^"+valueString+"\\s*)(\\sIS(.(?!IN LIST))*)*")).collect(Collectors.toList()):
									this.iterativeNodeMap.keySet().stream().filter(key -> key.matches("(^"+variableName+"\\s*)(\\sIS(.(?!IN LIST))*)*")).collect(Collectors.toList());

							if(!possibleChildNodeKeyList.isEmpty())
							{
								possibleChildNodeKeyList.stream().forEachOrdered(item -> {
									this.iterativeDependencyList.add(new Dependency(tempNode, this.iterativeNodeMap.get(item), DependencyType.getOr())); //Dependency Type :OR
								});
							}
							if(data.getFactValue().getValue().equals("WARNING"))
							{
								handleWarning(parentText);
							}
							break;	
						case 3: // exprConclusionMatcher case
							data = new ExprConclusionLine(checkText, tokens);
							String exVariableName = data.getVariableName();
							Node exTempNode = data;
							/*
							 * following lines are to look for any nodes having a its nodeName with 'needs ' word or any operators but not having a word of 'IS' keyword due to the reason that
							 * the node could be used to define a node previously used as a child node for other nodes.
							 * However, it is excluding nodes having 'IS' keyword because if it has the keyword then it should have child nodes to define the node otherwise the entire rule set has NOT been written in correct way
							 */
							List<String> exKeyList = this.iterativeNodeMap.keySet().stream().filter(key -> key.matches("(.+)?(\\\\s[<>=]+\\\\s?)?(WANTS |NEEDS )*("+exVariableName+")(\\s*[<>=]*)(\\s+.[^(IS)]*)*")).collect(Collectors.toList());
							if(!exKeyList.isEmpty())
							{
								exKeyList.stream().forEachOrdered(item -> {
									this.iterativeDependencyList.add(new Dependency(this.iterativeNodeMap.get(item), exTempNode, DependencyType.getOr()));
								});
							}
							if(data.getFactValue().getValue().equals("WARNING"))
							{
								handleWarning(parentText);
							}
							break;
					
					}
					data.setNodeLine(lineNumber);
					this.iterativeNodeMap.put(data.getNodeName(), data);
					this.iterativeNodeIdMap.put(data.getNodeId(), data.getNodeName());
					break;
				}
			}
			
			int nodeOption = 0;
			String firstTokenString = tokens.tokensList.get(0);

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
			this.iterativeDependencyList.add(new Dependency(data, data, nodeOption));
		}
		
		Dependency dependency; 
		if(iterateParent.equals(parentText)) // checkText is a direct child of iterate parent
		{
			dependency = new Dependency(this, data, dt);
		}
		else // checkText is not a direct child of iterate parent
		{
			parentText = parentText.replace("CHECK", "").trim();
			Node parentCheckNode = this.iterativeNodeMap.get(parentText); 
			dependency = new Dependency(parentCheckNode, data, dt);
		}
		this.iterativeDependencyList.add(dependency);
		
		
		

	}
	
	
	@Override
	public void initialisation(String childText, Tokens tokens) {
		iterativeNodeIdMap = new HashMap<>();
		iterativeNodeMap = new HashMap<>();

		/*
		 * This line can be only 'child' line hence
		 * the pattern only could be (^U)([MLNo]+)(I)([MLU]+$) 
		 */	

		
		/*
		 * variableName represents name of item in a givenList
		 * value represents a name of a givenList
		 */
		if(tokens.tokensStringList.get(1).contains("No")) // target number is set with numeric
		{
			this.numberOfTarget = tokens.tokensList.get(1);
		}
		else // target number is set with keyword of only either 'ALL', 'SOME' or 'NONE'
		{
			switch(tokens.tokensList.get(0))
			{
				case "ALL":
					this.numberOfTarget = "ALL";
					break;
				case "NONE":
					this.numberOfTarget = "0";
					break;
				case "SOME":
					this.numberOfTarget = "SOME";
					break;
			}
		}
		this.variableName = childText.replaceAll("NOT|KNOWN", "").trim();
		this.nodeName = childText.trim();
		
		int tokensStringListSize = tokens.tokensStringList.size();
		String lastToken = tokens.tokensList.get(tokensStringListSize-1); //this is a givenListName.
		String lastTokenString = tokens.tokensStringList.get(tokensStringListSize-1);
		this.setValue(lastTokenString, lastToken);
		this.iterativeNodeMap.put(nodeName, this); // this object(IterateLine Object) in 'iterateNodeMap' will be a root node(Goal Node), and CHECK item will be children node
		this.iterativeNodeIdMap.put(this.nodeId, this.nodeName);
	}

	@Override
	public LineType getLineType() {

		return LineType.ITERATE;
	}

	public int[][] createIterativeDependencyMatrix()
	{
		this.setIterativeNodeMap(handlingVirtualNode(this.iterativeDependencyList));
		/*
		 * number of rule is not always matched with the last ruleId in Node 
		 */
		int numberOfRules = Node.getStaticNodeId();
		
		int[][] dependencyMatrix = new int[numberOfRules][numberOfRules];
	
		
		this.iterativeDependencyList.forEach(dp -> {
			int parentId = dp.getParentNode().getNodeId();
			int childId = dp.getChildNode().getNodeId();
			int dpType = dp.getDependencyType();
			dependencyMatrix[parentId][childId] = dpType;
		});
		
		return dependencyMatrix;
	}
	
	public HashMap<String, Node> handlingVirtualNode(List<Dependency> dependencyList)
	{
		
		HashMap<String, Node> virtualNodeMap = new HashMap<>();
		

		this.iterativeNodeMap.values().stream().forEachOrdered((node) ->{
			virtualNodeMap.put(node.getNodeName(), node);
			List<Dependency> dpList= dependencyList.stream()
							   .filter(dp -> node.getNodeName().equals(dp.getParentNode().getNodeName()))
							   .collect(Collectors.toList());
			
			/*
			 * need to handle Mandatory, optionally, possibly NodeOptions
			 */
			int and = 0;
			int or = 0;
			if(!dpList.isEmpty())
			{
				for(Dependency dp: dpList)
				{
					if((dp.getDependencyType() & DependencyType.getAnd()) == DependencyType.getAnd()) // dp.getDependencyType is any kinds of 'AND'
					{
						and++;
					}
					else if((dp.getDependencyType() & DependencyType.getOr()) == DependencyType.getOr()) // dp.getDependencyType is any kinds of 'OR'
					{
						or++;
					}
				}
				boolean hasAndOr = (and>0 && or>0)? true:false;  
				if(hasAndOr)
				{
					String parentNodeOfVirtualNodeName = node.getNodeName();
					Node virtualNode = new ValueConclusionLine("VirtualNode-"+parentNodeOfVirtualNodeName, Tokenizer.getTokens("VirtualNode-"+parentNodeOfVirtualNodeName));
					this.iterativeNodeIdMap.put(virtualNode.getNodeId(), "VirtualNode-"+parentNodeOfVirtualNodeName);
					virtualNodeMap.put("VirtualNode-"+parentNodeOfVirtualNodeName, virtualNode);
					dependencyList.add(new Dependency(node, virtualNode, DependencyType.getOr()));
					dpList.stream()
						  .filter(dp -> (dp.getDependencyType() & DependencyType.getAnd()) == DependencyType.getAnd()).forEachOrdered(dp -> dp.setParentNode(virtualNode));
				}
			}
			
		});
		return virtualNodeMap;
	}
	
	@Override
	public FactValue selfEvaluate(HashMap<String, FactValue> workingMemory, ScriptEngine nashorn, int nodeOption) {
		
		this.givenList = (List<?>) workingMemory.get(this.getFactValue().getValue().toString());
		int givenListSize = this.givenList.size();
		this.createIterativeDependencyMatrix();
		this.iterativeTopoSortedList = TopoSort.bfsTopoSort(iterativeNodeMap, iterativeNodeIdMap, iterativeDependencyMatrix.getDependencyMatrixArray());

		for(int i = 0; i < givenListSize; i++)
		{
			inferenceIterate(nashorn, this.givenList.get(i), nodeOption);
		}
		
		return this.iterateWorkingMemory.get(this.getNodeName());
	}
	
	private <T> void inferenceIterate(ScriptEngine nashorn, T data, int dependency)
	{
		ObjectMapper mapper = new ObjectMapper();
		JsonNode dataJsonNode = mapper.valueToTree(data);
		
		while(this.iterateWorkingMemory.containsKey(this.getNodeName()))
		{
			this.iterativeTopoSortedList.stream().forEachOrdered(node -> {
				this.iterativeInclusiveList.add(node.getNodeName());
				if(!hasChildren(node) && !canEvaluate(node, nashorn))
  	            {
					FactValue fv = convertFactValue(Tokenizer.getTokens(dataJsonNode.path(node.getNodeName()).asText()));
					setFact(node.getVariableName(), fv);
  	            		setFact(node.getNodeName(),node.selfEvaluate(this.iterateWorkingMemory, nashorn, dependency));
  	            		forwardChaining(findNodeIndex(node.getNodeName()), nashorn);
  	            }
	            else
	            {
	            	
	            	if(hasChildren(node) && !this.iterateWorkingMemory.containsKey(node.getVariableName()) 
	            			&& !this.iterateWorkingMemory.containsKey(node.getNodeName()) 
	            			&& this.iterativeInclusiveList.contains(node.getNodeName()))
	            	{
	            		addChildRuleIntoInclusiveList(node);
	            	}
	            }
			});
		}
	
		this.outcomeList.add(((FactBooleanValue)this.iterateWorkingMemory.get(this.nodeName)).getValue());
		
	}

	private void forwardChaining(int nodeIndex, ScriptEngine nashorn) 
	{
		IntStream.range(0, nodeIndex+1).forEach(i -> {
    		
	    		Node node = this.iterativeTopoSortedList.get(nodeIndex-i);
	    		
	    		if(this.iterativeInclusiveList.contains(node.getNodeName()))
	    		{
	    			backPropagation(nodeIndex-i, nashorn);
	    		}
	    		
	    		
	        if(this.iterateWorkingMemory.containsKey(node.getVariableName()) || this.iterateWorkingMemory.containsKey(node.getNodeName()))
	        {
	        		addParentIntoInclusiveList(node); // adding all parents rules into the 'inclusiveList' if there is any
	        }
	    		
		});		
	}

	private void backPropagation(int i, ScriptEngine nashorn) {
		Node currentNode = this.iterativeTopoSortedList.get(i);
		LineType currentLineType = currentNode.getLineType();
		/*
         *following 'if' statement is to double check if the rule has any children or not.
         *it will be already determined by asking a question to a user if it doesn't have any children .
         */
       if (!this.iterateWorkingMemory.containsKey(currentNode.getVariableName()) 
    		   && hasChildren(currentNode) && canDetermine(currentNode, currentLineType, nashorn) )
       {
    	   	this.summaryList.add(currentNode); // add currentRule into SummeryList as the rule determined
       }		
	}

	private boolean canDetermine(Node node, LineType lineType, ScriptEngine nashorn) {
		
		List<Integer> nodeOrOutDependencies = this.iterativeDependencyMatrix.getOROutDependencyList(node.getNodeId());
    	List<Integer> nodeAndOutDependencies = this.iterativeDependencyMatrix.getAndOutDependencyList(node.getNodeId());
    	int nodeOption = this.iterativeDependencyMatrix.getDependencyType(node.getNodeId(), node.getNodeId());
    	boolean canDetermine = false;

    	if(LineType.VALUE_CONCLUSION.equals(lineType))
    	{
    		
    		boolean isPlainStatementFormat = ((ValueConclusionLine)node).getIsPlainStatementFormat();
    		
    		if(nodeAndOutDependencies.isEmpty() && !nodeOrOutDependencies.isEmpty()) // rule has only 'OR' child rules 
    		{
    			
    			if(isAnyOrDependencyTrue(node, nodeOrOutDependencies)) //TRUE case
    			{
    				canDetermine = true;
    				
    				if(isPlainStatementFormat)
				{
    					setFact(node.getVariableName(), FactValue.parse(true));
				}
    				else
    				{
    					setFact(node.getVariableName(), node.getFactValue());
    				}
    				
    				
    				setFact(node.getNodeName(), node.selfEvaluate(this.iterateWorkingMemory, nashorn, nodeOption));
    			}
    			else if(isAllOrDependencyDetermined(nodeOrOutDependencies) && !isAnyOrDependencyTrue(node, nodeOrOutDependencies)) //FALSE case
    			{
    				canDetermine = true;
    				if(isPlainStatementFormat)
    				{
    					setFact(node.getVariableName(), FactValue.parse(false));
    				}
    				else
    				{
    					setFact(node.getVariableName(), FactValue.parse(node.getVariableName()));
    				}    				
    				    				
    				setFact(node.getNodeName(), node.selfEvaluate(this.iterateWorkingMemory, nashorn, nodeOption));

    			}
    		}
    		else if(!nodeAndOutDependencies.isEmpty() && nodeOrOutDependencies.isEmpty())// rule has only 'AND' child rules
    		{
    			if(isAllAndDependencyDetermined(nodeAndOutDependencies) && isAllAndDependencyTrue(node, nodeAndOutDependencies)) // TRUE case
			{
    				canDetermine = true;
    				
    				if(isPlainStatementFormat)
    				{
    					setFact(node.getVariableName(), FactValue.parse(false));
    				}
    				else
    				{
    					setFact(node.getVariableName(), FactValue.parse(node.getVariableName()));
    				}
    				
    				setFact(node.getNodeName(), node.selfEvaluate(this.iterateWorkingMemory, nashorn, nodeOption));

			}
    			/*
    			 * 'isAnyAndDependencyFalse()' contains a trimming off dependency method 
    			 * due to the fact that all undetermined 'AND' rules need to be trimmed off when any 'AND' rule is evaluated as 'NO'
               	 * , which does not influence on determining a parent rule's evaluation.
               	 * 
    			 */
    			else if(isAllAndDependencyDetermined(nodeAndOutDependencies) && isAnyAndDependencyFalse(nodeAndOutDependencies)) //FALSE case
    			{
    				canDetermine = true;
    				
    				if(isPlainStatementFormat)
    				{
    					setFact(node.getVariableName(), FactValue.parse(false));
    				}
    				else
    				{
    					setFact(node.getVariableName(), FactValue.parse(node.getVariableName()));
    				}
    				
    				setFact(node.getNodeName(), node.selfEvaluate(this.iterateWorkingMemory, nashorn, nodeOption));
    				
    			}
		
    		}
    	}
    	else if(LineType.EXPR_CONCLUSION.equals(lineType))
    	{

    		if(!nodeAndOutDependencies.isEmpty() && nodeOrOutDependencies.isEmpty()) // rule has 'MANDATORY_OR' and 'OR' child rules 
    		{
    			for(int i=0; i < nodeOrOutDependencies.size(); i++)
    			{
    				if((this.iterativeDependencyMatrix.getDependencyMatrixArray()[node.getNodeId()][nodeOrOutDependencies.get(i)] & (DependencyType.getMandatory() | DependencyType.getOr())) == (DependencyType.getMandatory() | DependencyType.getOr()))
    				{
    					if(this.iterateWorkingMemory.get(this.iterativeNodeMap.get(this.iterativeNodeIdMap.get(nodeOrOutDependencies.get(i))).getVariableName()) != null)
    					{
    						canDetermine = true;
    						setFact(node.getVariableName(), node.selfEvaluate(this.iterateWorkingMemory, nashorn, nodeOption)); // add currentRule into the workingMemory
    					}
    				}
    				else
    				{
    					this.iterativeInclusiveList.remove(this.iterativeNodeMap.get(this.iterativeNodeIdMap.get(i)).getNodeName());
    				}
    			}    			
    		}
    		else if(nodeAndOutDependencies.isEmpty() &&!nodeOrOutDependencies.isEmpty())// rule has only 'MANDATORY_AND' child rules
    		{
    			if(allNeedsChildDetermined(node, nodeAndOutDependencies)) // TRUE case
				{
    				canDetermine = true;
    				/*  
 	                `* The reason why ast.setFact() is used here rather than this.addFactToRule() is that ruleType is already known, and target rule object is already found. 
 	                 */
 	                setFact(node.getVariableName(), node.selfEvaluate(this.iterateWorkingMemory, nashorn, nodeOption)); // add currentRule into the workingMemory
				}
		
    		}
    	}
    	else if(LineType.COMPARISON.equals(lineType))
    	{
    		
    		if(this.iterateWorkingMemory.get(node.getVariableName()) != null)
    		{
    			canDetermine = true;
    			/*  
    			 * The reason why ast.setFact() is used here rather than this.addFactToRule() is that ruleType is already known, and target rule object is already found. 
    			 */
     		   setFact(node.getNodeName(), node.selfEvaluate(this.iterateWorkingMemory, nashorn, nodeOption)); // add currentRule into the workingMemory
    		}
    	}
    	
    	return canDetermine;		
	}

	private boolean allNeedsChildDetermined(Node parentNode, List<Integer> outDependency) 
	{
		
		boolean allNeedsChildDetermined = false;
    	
	    	List<Integer> determinedList = outDependency.stream().filter(i -> (this.iterativeDependencyMatrix.getDependencyMatrixArray()[parentNode.getNodeId()][i] & (DependencyType.getMandatory() | DependencyType.getAnd())) == (DependencyType.getMandatory() | DependencyType.getAnd())
	    										&& this.iterateWorkingMemory.get(this.iterativeNodeMap.get(this.iterativeNodeIdMap.get(i)).getVariableName())!= null).collect(Collectors.toList());
	    	
	    	if(outDependency.size() == determinedList.size())
	    	{
	    		allNeedsChildDetermined = true;
	    	}    	
	    	
	    	return allNeedsChildDetermined;
	}

	private boolean isAnyAndDependencyFalse(List<Integer> andOutDependencies) 
	{
		
		boolean isAnyAndDependencyFalse = false;
        
        List<Integer> falseAndList = andOutDependencies.stream().filter(i -> this.iterateWorkingMemory.get(this.iterativeNodeMap.get(this.iterativeNodeIdMap.get(i))).getValue().toString().equals("false")).collect(Collectors.toList());

        if(falseAndList.size() > 0)
        {
        	isAnyAndDependencyFalse = true;
        	andOutDependencies.stream().forEachOrdered(i -> {
        		falseAndList.stream().forEachOrdered(f -> {
        			if(i != f)
        			{
        				this.iterativeInclusiveList.remove(this.iterativeNodeMap.get(this.iterativeNodeIdMap.get(i)).getNodeName());
        			}
        		});
        	});
        }
        else if(andOutDependencies.size() == 0)
        {
        	isAnyAndDependencyFalse = true;
        }
        return isAnyAndDependencyFalse;
	}

	private boolean isAllAndDependencyTrue(Node node, List<Integer> andOutDependencies) 
	{
		
		boolean isAllAndTrue = false;

        List<Integer> determinedTrueAndOutDependencies = andOutDependencies.stream().filter(i ->
	    	this.iterateWorkingMemory.get(this.iterativeNodeMap.get(this.iterativeNodeIdMap.get(i))).getValue().toString().equals("true")).collect(Collectors.toList());
	        
        
        if(andOutDependencies != null && determinedTrueAndOutDependencies.size() == andOutDependencies.size())
        {
	        	isAllAndTrue = true;
        } 

       return isAllAndTrue;
	}

	private boolean isAllAndDependencyDetermined(List<Integer> andOutDependencies) {
		 
		boolean isAllAndDependencyDetermined = false;
	        
        List<Integer> determinedAndOutDependencies = andOutDependencies.stream().filter(i ->
        		this.iterateWorkingMemory.get(this.iterativeNodeMap.get(this.iterativeNodeIdMap.get(i))) != null).collect(Collectors.toList());
	        
        if(andOutDependencies != null && determinedAndOutDependencies.size() == andOutDependencies.size())
        {
	        	isAllAndDependencyDetermined = true;
        }

        return isAllAndDependencyDetermined;
	}

	private boolean isAllOrDependencyDetermined(List<Integer> orOutDependencies) 
	{
		
		boolean isAllOrDependencyDetermined = false;
        
        List<Integer> determinedOrOutDependencies = orOutDependencies.stream().filter(i ->
        	this.iterateWorkingMemory.get(this.iterativeNodeMap.get(this.iterativeNodeIdMap.get(i))) != null
        ).collect(Collectors.toList());
        
        if(orOutDependencies != null && determinedOrOutDependencies.size() == orOutDependencies.size())
        {
        		isAllOrDependencyDetermined = true;
        }

        return isAllOrDependencyDetermined;
	}

	private boolean isAnyOrDependencyTrue(Node node, List<Integer> orOutDependencies) 
	{
		boolean isAnyOrDependencyTrue = false;
        if (!orOutDependencies.isEmpty())
        {
        		List<Integer> trueOrOutNodesList = orOutDependencies.stream().filter(i -> this.iterateWorkingMemory.get(this.iterativeNodeMap.get(this.iterativeNodeIdMap.get(i))).getValue().equals(true))
        																 .collect(Collectors.toList());
        	
	        	if(!trueOrOutNodesList.isEmpty())
	        	{
	        		isAnyOrDependencyTrue = true;
	        		orOutDependencies.stream().forEachOrdered(i -> {
	        			trueOrOutNodesList.stream().forEachOrdered(n -> {
	        				if(i != n)
	        				{
	        					trimDependency(node, this.iterativeNodeMap.get(this.iterativeNodeIdMap.get(i)));
	        				}
	        			});
	        		});
	        	}
        }

        return isAnyOrDependencyTrue;
	}

	private void trimDependency(Node parentNode, Node childNode) 
	{
		int dpType = this.iterativeDependencyMatrix.getDependencyMatrixArray()[parentNode.getNodeId()][childNode.getNodeId()];
	    	if((dpType & DependencyType.getMandatory()) != DependencyType.getMandatory())
	    	{
	    		this.iterativeInclusiveList.remove(childNode.getNodeName());
	    	}		
	}

	private void addParentIntoInclusiveList(Node node) 
	{
		List<Integer> nodeInDependencyList = this.iterativeDependencyMatrix.getInDependencyList(node.getNodeId());
        if(!nodeInDependencyList.isEmpty()) // if rule has parents
        {
	        	nodeInDependencyList.stream().forEachOrdered(i -> {
	        		Node parentNode = this.iterativeNodeMap.get(this.iterativeNodeIdMap.get(i));
	        		if(!this.iterativeInclusiveList.contains(parentNode.getNodeName()))
	        		{
	        			this.iterativeInclusiveList.add(parentNode.getNodeName());
	        		}
	        	});
          
        }		
	}

	private FactValue convertFactValue(Tokens tokens) 
	{

		FactValue fv = null;
		switch(tokens.tokensString)
		{
			case "De":
				fv = FactValue.parse(tokens.tokensList.get(0));
				break;
			
			case "No":
				fv = FactValue.parse(tokens.tokensList.get(0));
				break;
				
			case "Da":
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
	      		LocalDate factValueInDate = LocalDate.parse(tokens.tokensList.get(0), formatter);				
	      		fv = FactValue.parse(factValueInDate);
	      		break;
	      		
			case "Url":
				fv = FactValue.parseURL(tokens.tokensList.get(0));
				break;
				
			case "Id":
				fv = FactValue.parseUUID(tokens.tokensList.get(0));
				break;
				
			case "Ha":
				fv = FactValue.parseHash(tokens.tokensList.get(0));
		}
		return fv;
	}
	private void addChildRuleIntoInclusiveList(Node node) 
	{
		
		List<Integer> childrenListOfNode = this.iterativeDependencyMatrix.getOutDependencyList(node.getNodeId());
		childrenListOfNode.stream().forEachOrdered(item -> {
	    		String childNodeName = this.iterativeNodeMap.get(this.iterativeNodeIdMap.get(item)).getNodeName();
	    		if(!this.iterativeInclusiveList.contains(childNodeName))
	    		{
	    			this.iterativeInclusiveList.add(childNodeName);
	    		}
		});		
	}
	
	
	public boolean hasChildren(Node node)
    {
        boolean hasChildren = false;
        if (!this.iterativeDependencyMatrix.getOutDependencyList(node.getNodeId()).isEmpty())
        {
            hasChildren = true;
        }
        return hasChildren;
    }

	public boolean canEvaluate(Node node, ScriptEngine nashorn)
    {
    	
	    	boolean canEvaluate = false;
	    	LineType lineType = node.getLineType();
	    	/*
	    	 * the reason for checking only VALUE_CONCLUSION, COMPARISON and ITERATE type of node is that they are the only ones can be the most child nodes in rule structure.
	    	 * other type of node must be a parent of other types of node.
	    	 * In addition, the reason being to check only if there is a value for a variableName of the node in the workingMemory is that
	    	 * only a value for variableName of the node is needed to evaluate the node. even if the node is ValueConclusionLine, it wouldn't be matter because
	    	 * variableName and nodeName will have a same value if the node is the most child node, which means that the statement for the node does NOT contain
	    	 * 'IS' keyword. 
	    	 */
	    	if(((lineType.equals(LineType.VALUE_CONCLUSION) || lineType.equals(LineType.COMPARISON)) && this.iterateWorkingMemory.containsKey(node.getVariableName()))
	    			||(lineType.equals(LineType.ITERATE) && this.iterateWorkingMemory.containsKey(node.getNodeName())))
	    	{
	    		canEvaluate = true;
	    		/*
	    		 * the reason why ast.setFact() is used here rather than this.feedAndwerToNode() is that LineType is already known, and target node object is already found. 
	    		 * node.selfEvaluation() returns a value of the node's self-evaluation hence, node.getNodeName() is used to store a value for the node itself into a workingMemory
	    		 */
	    		setFact(node.getNodeName(), node.selfEvaluate(this.iterateWorkingMemory, nashorn, this.iterativeDependencyMatrix.getDependencyType(node.getNodeId(), node.getNodeId())));
	    	}
	    	
	    	
	    	return canEvaluate;
    }
	 
	public int findNodeIndex(String nodeName)
	{
	    int nodeIndex = IntStream.range(0, this.iterativeTopoSortedList.size()).filter(i -> this.iterativeTopoSortedList.get(i).getNodeName().equals(nodeName)).toArray()[0];
	    
	    return nodeIndex;
	}	
	
	private void setFact(String nodeName, FactValue selfEvaluate) 
	{
		this.iterateWorkingMemory.put(nodeName, selfEvaluate);
	}
	
	public String handleWarning(String parentText)
	{
		return parentText+": rule format is not matched. Please check the format again";
	}
	

}
