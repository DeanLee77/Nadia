package inferencePackage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import factValuePackage.FactValue;
import nodePackage.NodeSet;

import factValuePackage.*;
import nodePackage.*;


public class InferenceEngine {
	private NodeSet nodeSet;
	private Node targetNode;
    private AssessmentState ast;
    private Assessment ass;
    private List<Node> nodeFactList;
    private ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");
 
    
    
   
//    private int ruleIndex = 0;

    public InferenceEngine(NodeSet nodeSet)
    {
	    	this.nodeSet = nodeSet;
	    	ast = newAssessmentState();
	    	HashMap<String, FactValue> tempFactMap = nodeSet.getFactMap();
	    	HashMap<String, FactValue> tempWorkingMemory = ast.getWorkingMemory();
	    	
	    if(!tempFactMap.isEmpty())
	    	{
	    		tempFactMap.keySet().stream().forEach(key-> tempWorkingMemory.put(key, tempFactMap.get(key))); 	
	    	}
	    	nodeFactList = new ArrayList<>(nodeSet.getNodeSortedList().size()*2); // contains all rules set as a fact given by a user from a ruleList
    	
    }

    public void addNodeSet(NodeSet nodeSet2)
    {
    	
    }
    
    public void setNodeSet(NodeSet nodeSet)
    {
	    	this.nodeSet = nodeSet;
	    	ast = newAssessmentState();
	    	HashMap<String, FactValue> tempFactMap = nodeSet.getFactMap();
	    	HashMap<String, FactValue> tempWorkingMemory = ast.getWorkingMemory();
	    	
	    if(!tempFactMap.isEmpty())
	    	{
	    		tempFactMap.keySet().stream().forEach(key-> tempWorkingMemory.put(key, tempFactMap.get(key))); 	
	    	}
	    	nodeFactList = new ArrayList<>(nodeSet.getNodeSortedList().size()*2); // contains all rules set as a fact given by a user from a ruleList
	
    }
    
    public NodeSet getNodeSet()
    {
    		return this.nodeSet;
    }
    public AssessmentState getAssessmentState()
    {
    		return this.ast;
    }
    
    public AssessmentState newAssessmentState()
    {
	    	int initialSize = nodeSet.getNodeSortedList().size() * 2;
	    	AssessmentState ast = new AssessmentState();
	    	List<String> inclusiveList = new ArrayList<>(initialSize);
	    	List<String> summaryList = new ArrayList<>(initialSize);
	    	ast.setInclusiveList(inclusiveList);
	    	ast.setSummaryList(summaryList);
	    	
	    	return ast;
    	
    }
    
    public void setAssessment(Assessment ass)
    {
    		this.ass = ass;
    }
    public Assessment getAssessment()
    {
    		return this.ass;
    }
    public ScriptEngine getScriptEngine()
    {
    		return this.scriptEngine;
    }
    /*
     * this method is to extract all variableName of Nodes, and put them into a List<String>
     * it may be useful to display and ask a user to select which information they do have even before starting Inference process
     */
    public List<String> getListOfVariableNameAndValueOfNodes()
    {
	    	List<String> variableNameList = new ArrayList<>();
	    	nodeSet.getNodeMap().values().stream().forEachOrdered(node -> {
	    		variableNameList.add(node.getVariableName());
	    		FactValueType nodeFactValueType = node.getFactValue().getType();
	    		if(nodeFactValueType.equals(FactValueType.BOOLEAN) || nodeFactValueType.equals(FactValueType.STRING))
	    		{
	    			variableNameList.add(node.getFactValue().getValue().toString());
	    		}
    		});
	    	
	    	
	    	return variableNameList;
    }
    /*
     * this method allows to store all information via GUI even before starting Inference process. 
     */
    public void addNodeFact(String nodeVariableName, FactValue fv)
    {
	    	nodeSet.getNodeMap().values().stream()
	    								 .forEachOrdered((node) -> {
	    									 if(node.getVariableName().equals(nodeVariableName)||
	    										node.getFactValue().getValue().toString().equals(nodeVariableName))
											 {
	    										 	nodeFactList.add(node);
											 }
	    								});  
		ast.getWorkingMemory().put(nodeVariableName, fv);

    }
    
    /*
     * this method is to find all relevant Nodes(immediate child nodes of the most parent) with given information from a user
     * while finding out all relevant factors, all given information will be stored in AssessmentState.workingMemory
     */
    public List<Node> findRelevantFactors()
    {
	    	List<Node> relevantFactorList = new ArrayList<>();
	    	if(!nodeFactList.isEmpty())
	    	{
	    		nodeFactList.stream().forEachOrdered(node -> {
	    			if(!nodeSet.getDependencyMatrix().getFromParentDependencyList(node.getNodeId()).isEmpty())
	    			{
	    				Node relevantNode = auxFindRelevantFactors(node);
	    				relevantFactorList.add(relevantNode);
	    			}
	    		});
	    	}
	    	return relevantFactorList;
    }
    
    public Node auxFindRelevantFactors(Node node)
    {
	    	Node relevantFactorNode = null;
	    	List<Integer> incomingDependencyList = nodeSet.getDependencyMatrix().getFromParentDependencyList(node.getNodeId()); // it contains all id of parent node where dependency come from
	    	if(!incomingDependencyList.isEmpty())
	    	{
	    		for(int i = 0; i < incomingDependencyList.size(); i++)
	    		{
	    			Node parentNode = nodeSet.getNodeMap().get(nodeSet.getNodeIdMap().get(incomingDependencyList.get(i)));
	    			if(!nodeSet.getDependencyMatrix().getFromParentDependencyList(parentNode.getNodeId()).isEmpty() 
	    					&& !parentNode.getNodeName().equals(nodeSet.getNodeSortedList().get(0).getNodeName()))
	    			{
	    				relevantFactorNode = auxFindRelevantFactors(parentNode);
	
	    			}
	    		}
	    		
	    	}
	    	return relevantFactorNode;
    }
    
    /*
     * this method uses 'BACKWARD-CHAININING', and it will return node to be asked of a given assessment, which has not been determined and 
     * does not have any child nodes if the goal node of the given assessment has still not been determined.
     */
    public Node getNextQuestion(Assessment ass)
    {
	    	if(!ast.getInclusiveList().contains(ass.getGoalNode().getNodeName()))
		{
	    		ast.getInclusiveList().add(ass.getGoalNode().getNodeName());
		}
    	
	    	/*
	    	 * Default goal rule of a rule set which is a parameter of InferenceEngine will be evaluated by forwardChaining when any rule is evaluated within the rule set
	    	 */
	    	if(ast.getWorkingMemory().get(ass.getGoalNode().getNodeName())== null || !ast.allMandatoryNodeDetermined())
	    	{
	    		for (int i = ass.getGoalNodeIndex(); i < nodeSet.getNodeSortedList().size(); i++)
	  	       {
	    				Node node = nodeSet.getNodeSortedList().get(i);
	  	            
	  	
	  	            /*
	  	             * Step1. does the rule currently being been checked have child rules && not yet evaluated && is in the inclusiveList?
	  	             *     if no then ask a user to evaluate the rule, 
	  	             *                and do back propagating with a result of the evaluation (note that this part will be handled in feedAnswer())
	  	             *     if yes then move on to following step
	  	             *     
	  	             * Step2. does the rule currently being been checked have child rules? 
	  	             *     if yes then add the child rules into the inclusiveList
	  	             */
	    				int nodeId = node.getNodeId();
	    				if(i != ass.getGoalNodeIndex())
	    				{
	    					List<Integer> parentDependencyList = nodeSet.getDependencyMatrix().getFromParentDependencyList(nodeId);
	    					if(!parentDependencyList.isEmpty())
	    					{
	    						
	    						parentDependencyList.parallelStream().forEachOrdered(parentId -> {
	    							if((nodeSet.getDependencyMatrix().getDependencyType(parentId, nodeId)&DependencyType.getMandatory()) == DependencyType.getMandatory()
	    									&& !ast.isInclusiveList(node.getNodeName())
	    									&& !isIterateLineChild(node.getNodeId()))
	    							{
	    								ast.addItemToMandatoryList(node.getNodeName());
	    							}
	    						});
	    					}
	    				}
	    				if(nodeId != ass.getGoalNode().getNodeId() && node.getLineType().equals(LineType.ITERATE) && !ast.getWorkingMemory().containsKey(node.getNodeName()))
	    				{	
	    					FactValue givenListNameFv = this.ast.getWorkingMemory().get(((IterateLine)node).getGivenListName());
	    					String givenListName = "";
	    					if(givenListNameFv != null)
	    					{
	    						givenListName = givenListNameFv.toString().trim();
	    					}
	    					if(givenListName.length() > 0)
	    					{
	    						((IterateLine)node).iterateFeedAnswers(givenListName, this.nodeSet, this.ast, ass);
	    					}
	    					else
	    					{
	    						if(!this.ast.getWorkingMemory().containsKey(node.getNodeName()))
		    					{
		    						ass.setNodeToBeAsked(node);
			    					int indexOfRuleToBeAsked = i;
				  	            	System.out.println("indexOfRuleToBeAsked : "+indexOfRuleToBeAsked);
				  	            	
		    						return ((IterateLine)node).getIterateNextQuestion(this.nodeSet, this.ast);
		    					}
	    					}	    					
	    				}
	    				else if(!hasChildren(nodeId) && ast.getInclusiveList().contains(node.getNodeName()) 
	  	            			&& !canEvaluate(node))
	  	            {
	  	            		ass.setNodeToBeAsked(node);
		  	            	int indexOfRuleToBeAsked = i;
		  	            	System.out.println("indexOfRuleToBeAsked : "+indexOfRuleToBeAsked);
		  	            	return ass.getNodeToBeAsked();
	  	            }
		            else if(hasChildren(nodeId) && !ast.getWorkingMemory().containsKey(node.getVariableName()) 
	            			&& !ast.getWorkingMemory().containsKey(node.getNodeName()) && ast.getInclusiveList().contains(node.getNodeName()))
		            {
		            		addChildRuleIntoInclusiveList(node);
		            }
	  	       }
	    	} 	
	    	return ass.getNodeToBeAsked();
    }
    
    
    public List<String> getQuestionsFromNodeToBeAsked(Node nodeToBeAsked)
    {
    		List<String> questionList = new ArrayList<>();
    		LineType lineTypeOfNodeToBeAsked = nodeToBeAsked.getLineType();
    		// the most child node line types are as follows
    		// ValueConclusionLine type
    		if(lineTypeOfNodeToBeAsked.equals(LineType.VALUE_CONCLUSION))
    		{
    			/*
    			 * if the line format is 'A -statement' then node's nodeName and variableName has same value so that either of them can be asked as a question
    			 * 
    			 * if the line format is 'A IS IN LIST B' then the value of node's variableName is 'A' and the value of node's value is 'B' so that only 'A' needs to be asked.
    			 * list 'B' has to be provided in 'FIXED' list
    			 * 
    			 * In conclusion, if the line type is 'ValueConclusionLine' then node's variableName should be asked regardless its format
    			 */

			questionList.add(nodeToBeAsked.getVariableName());
    		}
    		// ComparionLine type
    		else if(lineTypeOfNodeToBeAsked.equals(LineType.COMPARISON))
    		{
    			if(!this.ast.getWorkingMemory().containsKey(((ComparisonLine)nodeToBeAsked).getLHS()))
    			{
    				questionList.add(((ComparisonLine)nodeToBeAsked).getLHS());
    			}

			
    			if(!TypeAlreadySet(nodeToBeAsked.getFactValue()))
    			{
    				questionList.add(nodeToBeAsked.getFactValue().getValue().toString());
    			}
    		}
    		
	    	questionList.stream().forEachOrdered(item->ast.getInclusiveList().add(item));
	    	return questionList;
    }
    
    
    public HashMap<String,FactValueType> findTypeOfElementToBeAsked(Node node)
    {
	    	/*
	    	 * FactValueType can be handled as of 16/06/2017 is as follows;
	    	 *  1. TEXT, STRING;
	    	 *  2. INTEGER, NUMBER;
	    	 *  3. DOUBLE, DECIMAL;
	    	 *  4. BOOLEAN;
	    	 *  5. DATE;
	    	 *  6. HASH;
	    	 *  7. UUID; and
	    	 *  8. URL.   
	    	 * rest of them (LIST, RULE, RULE_SET, OBJECT, UNKNOWN, NULL) can't be handled at this stage
	    	 */
	    	FactValueType fvt = null;
	    	HashMap<String,FactValueType> factValueTypeMap = new HashMap<>();
	    	/*
	    	 * In a case of that if type of toBeAsked node is ComparisonLine type with following conditions;
	    	 *    - the type of the node's variable to compare is already set as 
	    	 *      DefiString (eg. 'dean' or "dean"), Integer (eg. 1, or 2), Double (eg. 1.2 or 2.1), Date (eg. 21/3/1299), Hash, UUID, or URL 
	    	 *   then don't need to look into InputMap or FactMap to check the element's type of 'toBeAsked node' 
	    	 *   simply because we can check by looking at type of value variable because two different type CANNOT be compared
	    	 *   
	    	 * If neither type of variable is NOT defined in INPUT/FACT list nor the above case, and value of nodeVariable is same as value of nodeValueString  
	    	 * then the engine will recognise a nodeVariable or/and nodeVlaue as a boolean type 
	    	 * so that the question for a nodeVariable or/and a nodeValue seeks boolean type of answer
	    	 *   
	    	 */
	    	
	    	String nodeVariableName = node.getVariableName();
	    	String nodeValueString = node.getFactValue().getValue().toString();
	    	boolean TypeAlreadySet = TypeAlreadySet(node.getFactValue());
	    	HashMap<String, FactValue> tempFactMap = this.nodeSet.getFactMap();
	    	HashMap<String, FactValue> tempInputMap = this.nodeSet.getInputMap();
	    	LineType nodeLineType = node.getLineType();	   
	    	
	    	//ComparisonLine type node and type of the node's value is clearly defined 
	    	if(LineType.COMPARISON.equals(nodeLineType))
	    	{
	    		FactValueType nodeRHSType = ((ComparisonLine)node).getRHS().getType();
	    		if(!nodeRHSType.equals(FactValueType.STRING))
    			{
	    			if(nodeRHSType.equals(FactValueType.DEFI_STRING))
	    			{
		    			fvt = FactValueType.STRING;
		    		}
		    		else if(TypeAlreadySet)
		    		{
		    			fvt= nodeRHSType;
		    		}
		    		factValueTypeMap.put(((ComparisonLine)node).getLHS(), fvt);
	    				
    			}
	    		else if(nodeRHSType.equals(FactValueType.STRING))
	    		{
	    			if(tempInputMap.containsKey(((ComparisonLine)node).getLHS()))
	    			{
	    				fvt = tempInputMap.get(((ComparisonLine)node).getLHS()).getType();
	    			}
	    			else if(tempInputMap.containsKey(((ComparisonLine)node).getRHS().getValue().toString()))
	    			{
	    				fvt = tempInputMap.get(((ComparisonLine)node).getRHS().getValue().toString()).getType();
	    			}
	    			else if(tempFactMap.containsKey(((ComparisonLine)node).getLHS()))
	    			{
	    				fvt = tempFactMap.get(((ComparisonLine)node).getLHS()).getType();
	    			}
	    			else if(tempFactMap.containsKey(((ComparisonLine)node).getRHS().getValue().toString()))
	    			{
	    				fvt = tempFactMap.get(((ComparisonLine)node).getRHS().getValue().toString()).getType();
	    			}
	    			factValueTypeMap.put(((ComparisonLine)node).getLHS(), fvt);
    				factValueTypeMap.put(((ComparisonLine)node).getRHS().getValue().toString(), fvt);
	    		}
	    		
	    	}
	    	//ComparisonLine type node and type of the node's value is not clearly defined and not defined in INPUT nor FIXED list
	    	//ValueConclusionLine type node and it is 'A-statement' line, and variableName is not defined neither INPUT nor FIXED 
	    	else if(LineType.VALUE_CONCLUSION.equals(nodeLineType))
	    	{
	    		if(tempInputMap.containsKey(nodeVariableName))
	    		{
	    			fvt = tempInputMap.get(nodeVariableName).getType();
	    		}
	    		else if(ast.getWorkingMemory().containsKey(nodeValueString))
	    		{
	    			FactValue tempFv= ast.getWorkingMemory().get(nodeValueString);
	    			if(tempFv.getType().equals(FactValueType.LIST))
	    			{
	    				fvt = ((FactValue)((FactListValue<?>)tempFv).getValue().get(0)).getType();
	    			}
	    			else
	    			{
	    				fvt = tempFv.getType();
	    			}
	    		}
	    		else
	    		{
	    			fvt = FactValueType.BOOLEAN;
	    			
	    		}
	    		factValueTypeMap.put(nodeVariableName, fvt);
	    		
	    	}
	    	
	    	return factValueTypeMap;
    }
    
    public boolean TypeAlreadySet(FactValue value)
    {
    		boolean hasAlreadySetType = false;
    		
    		FactValueType factValueType = value.getType();
    		if(factValueType.equals(FactValueType.DEFI_STRING) || factValueType.equals(FactValueType.INTEGER) || factValueType.equals(FactValueType.DOUBLE) 
    				|| factValueType.equals(FactValueType.DATE) || factValueType.equals(FactValueType.BOOLEAN) || factValueType.equals(FactValueType.UUID) 
    				|| factValueType.equals(FactValueType.URL) || factValueType.equals(FactValueType.HASH))
		{
    			hasAlreadySetType = true;
		}
    		return hasAlreadySetType;
    }
    
    public boolean isIterateLineChild(int nodeId)
    {
    		boolean isIterateLineChild = false;
    		List<Integer> tempList = new ArrayList<>();
    		List<Node> iterateLineList = nodeSet.getNodeMap().values().stream().filter(node->node.getLineType().equals(LineType.ITERATE)).collect(Collectors.toList());
    		
    		iterateLineList.forEach(iNode -> {
    			List<Integer> iterateChildNodeList = this.nodeSet.getDependencyMatrix().getToChildDependencyList(iNode.getNodeId());
    			if(iterateChildNodeList.contains(nodeId))
    			{
    				tempList.add(1);
    			}
    			else
    			{
    				isIterateLineChildAux(tempList, iterateChildNodeList, nodeId);
    			}
    		});
    		
    		if(tempList.size() > 0 )
    		{
    			isIterateLineChild = true;
    		}
    		else
    		{
    			if(this.getAssessmentState().getMandatoryList().contains(this.nodeSet.getNodeIdMap().get(nodeId)))
    			{
    				this.getAssessmentState().getMandatoryList().remove(this.nodeSet.getNodeIdMap().get(nodeId));
    			}
    		}
    		
    		return isIterateLineChild;
    }
    
    public void isIterateLineChildAux(List<Integer> tempList, List<Integer> iterateChildNodeList, int nodeId)
    {
	    	iterateChildNodeList.forEach(id -> {
					List<Integer> iterateChildNodeListAux = this.nodeSet.getDependencyMatrix().getToChildDependencyList(id);
					if(iterateChildNodeList.contains(nodeId))
					{
						tempList.add(1);
					}
					else
					{
						isIterateLineChildAux(tempList, iterateChildNodeListAux, nodeId);
					}
		});
    }
    
    /*
     * this is to check whether or not a node can be evaluated with all information in the workingMemory. If there is information for a value of node's value(FactValue) or variableName, then the node can be evaluated otherwise not.
     * In order to do it, AssessmentState.workingMemory must contain a value for variable of the rule, 
     * and rule type must be either COMPARISON, ITERATE or VALUE_CONCLUSION because they are the ones only can be the most child nodes, 
     * and other type of node must be a parent of other types of node.	
     */
    public boolean canEvaluate(Node node)
    {
    	
	    	boolean canEvaluate = false;
	    	LineType lineType = node.getLineType();

	    	if(lineType.equals(LineType.VALUE_CONCLUSION))
	    	{
	    		if(((ValueConclusionLine)node).getIsPlainStatementFormat() && ast.getWorkingMemory().containsKey(node.getVariableName()))
	    		{
		    	    	/*
		    	    	 * If the node is in plain statement format then varibaleName has same value as nodeName,
		    	    	 * and if a value for either variableName or nodeName of the node is in workingMemory then it means the node has already been evaluated.
		    	    	 * Hence, 'canEvaluate' needs to be 'true' in this case.
		    	    	 */
	    			canEvaluate = true;
	    		}
	    		else if(node.getTokens().tokensList.stream().anyMatch((s) -> s.equals("IS IN LIST:")) 
	    				&& ast.getWorkingMemory().containsKey(node.getFactValue().getValue().toString())
	    				&& ast.getWorkingMemory().containsKey(node.getVariableName()))
			{
	    			canEvaluate = true;
	    			FactValue fv = node.selfEvaluate(ast.getWorkingMemory(), this.scriptEngine);

	    			/*
		    		 * the reason why ast.setFact() is used here rather than this.feedAndwerToNode() is that LineType is already known, and target node object is already found. 
		    		 * node.selfEvaluation() returns a value of the node's self-evaluation hence, node.getNodeName() is used to store a value for the node itself into a workingMemory
		    		 */
		    		ast.setFact(node.getNodeName(), fv);
			}
	    	}
	    	else if(lineType.equals(LineType.COMPARISON))
	    	{
	    		FactValue nodeRhsValue = ((ComparisonLine)node).getRHS();
	    		if(!nodeRhsValue.getType().equals(FactValueType.STRING) 
	    				&& ast.getWorkingMemory().containsKey(((ComparisonLine)node).getLHS()))
	    		{
	    			canEvaluate = true;
	    			if(!ast.getWorkingMemory().containsKey(node.getNodeName())) 
	    			{
	    				ast.setFact(node.getNodeName(), node.selfEvaluate(ast.getWorkingMemory(), this.scriptEngine));
	    			}
	    		}
	    		else if(nodeRhsValue.getType().equals(FactValueType.STRING) 
	    				&& ast.getWorkingMemory().containsKey(((ComparisonLine)node).getLHS()) 
	    				&& ast.getWorkingMemory().containsKey(((ComparisonLine)node).getRHS().getValue().toString()))
	    		{
	    			canEvaluate = true;
	    			if(!ast.getWorkingMemory().containsKey(node.getNodeName())) 
	    			{
	    				ast.setFact(node.getNodeName(), node.selfEvaluate(ast.getWorkingMemory(), this.scriptEngine));
	    			}
	    		}
	    	}

	    	return canEvaluate;
    }
    

    /* 
     * this method is to add fact or set a node as a fact by using AssessmentState.setFact() method. it also is used to feed an answer to a being asked node.
     * once a fact is added then forward-chain is used to update all effected nodes' state, and workingMemory in AssessmentState class will be updated accordingly
     * the reason for taking nodeName instead nodeVariableName is that it will be easier to find an exact node with nodeName
     * rather than nodeVariableName because a certain nodeVariableName could be found in several nodes
     */
    @SuppressWarnings({"unchecked" })
	public <T> void feedAnswerToNode(Node targetNode, String questionName, T nodeValue, FactValueType nodeValueType, Assessment ass)
    {
	    	FactValue fv = null;
	    	
	    	if(nodeValueType.equals(FactValueType.BOOLEAN))
	    	{
	    		fv = FactValue.parse(Boolean.parseBoolean((String)nodeValue));
	    	}
	    	else if(nodeValueType.equals(FactValueType.DATE))
	    	{
	    		/*
	    		 * the string of nodeValue date format is dd/MM/YYYY
	    		 */
	    		String[] dateArray = ((String)nodeValue).split("/");
	    		LocalDate factValueInDate = LocalDate.of(Integer.parseInt(dateArray[2]), Integer.parseInt(dateArray[1]), Integer.parseInt(dateArray[0]));
	    		
	    		fv = FactValue.parse(factValueInDate);
	    	}
	    	else if(nodeValueType.equals(FactValueType.DOUBLE))
	    	{
	    		fv = FactValue.parse(Double.parseDouble((String)nodeValue));
	    	}
	    	else if(nodeValueType.equals(FactValueType.INTEGER))
	    	{
	    		fv = FactValue.parse(Integer.parseInt((String)nodeValue));
	    	}
	    	else if(nodeValueType.equals(FactValueType.LIST))
	    	{
	    		fv = FactValue.parse((List<FactValue>)nodeValue);
	    	}
	    	else if(nodeValueType.equals(FactValueType.STRING))
	    {
	    		fv = FactValue.parse((String) nodeValue);
	    }
	    	else if(nodeValueType.equals(FactValueType.DEFI_STRING))
	    	{
	    		fv = FactValue.parseDefiString((String) nodeValue);
	    	}
	    	else if(nodeValueType.equals(FactValueType.HASH))
	    	{
	    		fv = FactValue.parseHash((String)nodeValue);
	    	}
	    	else if(nodeValueType.equals(FactValueType.URL))
	    	{
	    		fv = FactValue.parseURL((String)nodeValue);
	    	}
	    	else if(nodeValueType.equals(FactValueType.UUID))
	    	{
	    		fv = FactValue.parseUUID((String)nodeValue);
	    	}
	    	
	    	if(fv != null && !ass.getNodeToBeAsked().getLineType().equals(LineType.ITERATE))
	    	{
	    		ast.setFact(questionName, fv);
			ast.addItemToSummaryList(questionName);// add currentRule into SummeryList as the rule determined

	    		
	    		if(targetNode.getLineType().equals(LineType.VALUE_CONCLUSION) && !((ValueConclusionLine)targetNode).getIsPlainStatementFormat())
	    		{
	    			FactValue selfEvalFactValue = targetNode.selfEvaluate(ast.getWorkingMemory(), this.scriptEngine);
		    		ast.setFact(targetNode.getNodeName(), selfEvalFactValue); // add the value of targetNode itself into the workingMemory	
				ast.addItemToSummaryList(targetNode.getNodeName());// add currentRule into SummeryList as the rule determined
	    		}
	    		else if(targetNode.getLineType().equals(LineType.COMPARISON))
	    		{
	    			FactValue rhsValue = ((ComparisonLine)targetNode).getRHS();
	    			if((rhsValue.getType().equals(FactValueType.STRING) 
	    					&& ast.getWorkingMemory().containsKey(rhsValue.getValue().toString())) 
	    				|| !rhsValue.getType().equals(FactValueType.STRING)
    				  )
	    			{
	    				FactValue selfEvalFactValue = targetNode.selfEvaluate(ast.getWorkingMemory(), this.scriptEngine);
	    				ast.setFact(targetNode.getNodeName(), selfEvalFactValue); // add the value of targetNode itself into the workingMemory	
					ast.addItemToSummaryList(targetNode.getNodeName());// add currentRule into SummeryList as the rule determined
	    			}
	    			
	    		}
	    		
	    	 	
	    		/*
	        	 * once any rules are set as fact and stored into the workingMemory, back-propagation(forward-chaining) needs to be done
	        	 */
	    		backPropagating(nodeSet.findNodeIndex(targetNode.getNodeName()));
	        
	    	}
	    	else if(ass.getNodeToBeAsked().getLineType().equals(LineType.ITERATE))
	    	{
    			((IterateLine)ass.getNodeToBeAsked()).iterateFeedAnswers(targetNode, questionName, nodeValue, nodeValueType, this.nodeSet, ast, ass);
    			if(((IterateLine)ass.getNodeToBeAsked()).canBeSelfEvaluated(ast.getWorkingMemory()))
    			{
    	    			backPropagating(nodeSet.findNodeIndex(ass.getNodeToBeAsked().getNodeName()));
    			}
	    	}
    }
    
   
    public void backPropagating(int nodeIndex)
    {
    		List<Node> nodeSortedList = nodeSet.getNodeSortedList();
    		int sortedListSize = nodeSortedList.size();
    		IntStream.range(0, sortedListSize).forEachOrdered(i ->{
    			//current index = sortedListSize - (i+1)
    			Node tempNode = nodeSortedList.get(sortedListSize - (i+1));
			LineType lineType = tempNode.getLineType();
			
			int tempNodeId = tempNode.getNodeId();
			List<Integer> parentDependencyList = nodeSet.getDependencyMatrix().getFromParentDependencyList(tempNodeId);
			if(!parentDependencyList.isEmpty())
			{
				
				parentDependencyList.stream().forEachOrdered(parentId -> {
					if((nodeSet.getDependencyMatrix().getDependencyType(parentId, tempNodeId)&DependencyType.getMandatory()) == DependencyType.getMandatory()
							&& !ast.isInclusiveList(tempNode.getNodeName())
							&& !isIterateLineChild(tempNode.getNodeId()))
					{
						ast.addItemToMandatoryList(tempNode.getNodeName());
					}
				});
			}

    			if(nodeIndex < (sortedListSize - (i+1))) //case of all nodes located after the nodeIndex
    			{
    				if(hasChildren(tempNode.getNodeId()))
    				{
    					 if(!ast.getWorkingMemory().containsKey(tempNode.getNodeName())
	    		    			   	 && canDetermine(tempNode, lineType)
	    		    		   )
		    		    	 {
    						 if(!lineType.equals(LineType.EXPR_CONCLUSION))
    						 {
    	     						ast.addItemToSummaryList(tempNode.getNodeName());// add currentRule into SummeryList as the rule determined
    						 }
		    		    	 }
    				}
    				else
    				{
    					/*
    					 * ValueConclusionLine in 'A-statement' format does not need to be considered here due to the reason that
    					 * the case should be in the workingMemory if it is already asked.
    					 */
    					if(lineType.equals(LineType.VALUE_CONCLUSION) 
							&& !((ValueConclusionLine)tempNode).getIsPlainStatementFormat() 
							&& ast.getWorkingMemory().containsKey(((ValueConclusionLine)tempNode).getVariableName()))
    					{
						FactValue fv = tempNode.selfEvaluate(ast.getWorkingMemory(), scriptEngine);

    						ast.setFact(tempNode.getNodeName(), fv);
    						ast.addItemToSummaryList(tempNode.getNodeName());// add currentRule into SummeryList as the rule determined
    					}
    					else if(lineType.equals(LineType.COMPARISON) 
    							&& ast.getWorkingMemory().containsKey(((ComparisonLine)tempNode).getLHS())
    							&& ast.getWorkingMemory().containsKey(((ComparisonLine)tempNode).getRHS().getValue().toString())) 
        				{
    						FactValue fv = tempNode.selfEvaluate(ast.getWorkingMemory(), scriptEngine);

    						ast.setFact(tempNode.getNodeName(), fv);
    						ast.addItemToSummaryList(tempNode.getNodeName());// add currentRule into SummeryList as the rule determined
        				}

    				}
    				
    			}
    			else // case of all nodes located before the nodeIndex
    			{
    				/*
    				 * it the tempNode is located before the nodeIndex then there is need to check whether or not the tempNode is in the inclusiveList due to the reason that
    				 * evaluating only relevant node could speed the propagation faster. In addition only relevant nodes can be traced by checking the inclusiveList.
    				 */
    				
    				if(ast.getInclusiveList().contains(tempNode.getNodeName()))
	    	    		{
	    	    			/*
	    	    			 * once a user feeds an answer to the engine, the engine will propagate the entire NodeSet or Assessment base on the answer
	    	    			 * during the back-propagation, the engine checks if current node;
	    	    			 * 1. has been determined;
	    	    			 * 2. has any child nodes;
	    	    			 * 3. can be determined with given facts in the workingMemory.
	    	    			 * 
    	    			     * once the current checking node meets the condition then add it to the summaryList for summary view.
	    		    	     */
	    		    	   if (!ast.getWorkingMemory().containsKey(tempNode.getNodeName()) 
	    		    			   && hasChildren(tempNode.getNodeId())
	    		    			   && canDetermine(tempNode, lineType)
	    		    		  )
	    		    	   {
	    		    		   if(!lineType.equals(LineType.EXPR_CONCLUSION))
	    		    		   {
	    		    			   ast.addItemToSummaryList(tempNode.getNodeName()); // add currentRule into SummeryList as the rule determined
	    		    		   }

	    		    	   }
	    	    		}
    			}
    		});
    		
    }

    
    /*
     *this method is to find all parent rules of a given rule, and add them into the ' inclusiveList' for future reference
     */
    public void addParentIntoInclusiveList(Node node)
    {
    		List<Integer> nodeInDependencyList = nodeSet.getDependencyMatrix().getFromParentDependencyList(node.getNodeId());
        if(!nodeInDependencyList.isEmpty()) // if rule has parents
        {
	        	nodeInDependencyList.stream().forEachOrdered(i -> {
	        		Node parentNode = nodeSet.getNodeMap().get(nodeSet.getNodeIdMap().get(i));
	        		if(!ast.getInclusiveList().contains(parentNode.getNodeName()))
	        		{
	        			ast.getInclusiveList().add(parentNode.getNodeName());
	        		}
	        	});
          
        }
    }
    
    public boolean hasAllMandatoryChildAnswered(int nodeId)
    {    		

    		List<Integer> mandatoryChildDependencyList = nodeSet.getDependencyMatrix().getMandatoryToChildDependencyList(nodeId);
    		boolean hasAllMandatoryChildAnswered = false;
    		if(!mandatoryChildDependencyList.isEmpty())
    		{
    			hasAllMandatoryChildAnswered = mandatoryChildDependencyList.parallelStream()
																	 .allMatch(childId->
																					    	(ast.getWorkingMemory().containsKey(nodeSet.getNodeIdMap().get(childId))
																					    	     && hasAllMandatoryChildAnswered(childId ))
																			  );
    		}
    		else if(mandatoryChildDependencyList.isEmpty())
    		{
    			hasAllMandatoryChildAnswered = true;
    		}
    		
    		return hasAllMandatoryChildAnswered;
    }
	public boolean canDetermine(Node node, LineType lineType)
	{
	    	boolean canDetermine = false;
	    	/*
	    	 * Any type of node/line can have either 'OR' or 'AND' type of child nodes
	    	 *  
	    	 * -----ValueConclusion Type
	    	 * there will be two cases for this type
	    	 *    V.1 the format of node is 'A -statement' so that 'TRUE' or "FALSE' value outcome case
	    	 *    	   V.1.1 if it has 'OR' child nodes
	    	 *    			 V.1.1.1 TRUE case
	    	 *    					 if there is any of child node is 'true'
	    	 *    					 then trim off 'UNDETERMINED' child nodes, which are not in 'workingMemory', other than 'MANDATORY' child nodes
	    	 *    			 V.1.1.2 FALSE case
	    	 *    					 if its all 'OR' child nodes are determined and all of them are 'false'
	    	 *    	   V.1.2 if it has 'AND' child nodes
	    	 *       		 V.1.2.1 TRUE case
	    	 *       				 if its all 'AND' child nodes are determined and all of them are 'true'
	    	 *       		 V.1.2.2 FALSE case
	    	 *       				 if its all 'AND' child nodes are determined and all of them are 'false'
	    	 *                     	 , and there is no need to trim off 'UNDETERMINED' child nodes other than 'MANDATORY' child nodes
	    	 *                       because since 'virtual node' is introduced, any parent nodes won't have 'OR' and 'AND' dependency at the same time
	    	 *              
	    	 *         V.1.3 other than above scenario it can't be determined in 'V.1' case
	    	 *    
	    	 *    V.2 a case of that the value in the node text can be used as a value of its node's variable (e.g. A IS B, B can be used as a value for variable, 'A' in this case if all its child nodes or one of its child node is true)
	    	 *    	   V.2.1 if it has 'OR' child nodes
	    	 *    			 V.2.1.1 the value CAN BE USED case
	    	 *    					 if its any of child node is 'true'
	    	 *    					 then trim off 'UNDETERMINED' child nodes, which are not in 'workingMemory', other than 'MANDATORY' child nodes
	    	 *    			 V.2.1.2 the value CANNOT BE USED case
	    	 *    					 if its all 'OR' child nodes are determined and all of them are 'false'
	    	 *    	   V.2.2 if it has 'AND' child nodes
	    	 *    			 V.2.2.1 the value CAN BE USED case
	    	 *    					 if its all 'AND' child nodes are determined and all of them are 'true'
	    	 *    			 V.2.2.2 the value CANNOT BE USED case
	    	 *    					 if its all 'AND' child nodes are determined and all of them are 'false'
	    	 *                     	 , and there is no need to trim off 'UNDETERMINED' child nodes other than 'MANDATORY' child nodes
	    	 *                       because since 'virtual node' is introduced, any parent nodes won't have 'OR' and 'AND' dependency at the same time
	    	 *         
	    	 *         V.2.3 other than above scenario it can't be determined in 'V.2' case
	    	 *              
	    	 *    
	    	 * Note: the reason why only ResultType and ExpressionType are evaluated with selfEvaluation() is as follows;
	    	 *       1. ComparisonType is only evaluated by comparing a value of rule's variable in workingMemory with the value in the node
	    	 *       2. ExpressionType is only evaluated by retrieving a value(s) of needed child node(s)   
	    	 *       3. ValueConclusionType is evaluated under same combination of various condition, and trimming dependency is involved.	    	 *        
	    	 *       
	    	 */
	    	List<Integer> orToChildDependencies = nodeSet.getDependencyMatrix().getOrToChildDependencyList(node.getNodeId());
	    	List<Integer> andToChildDependencies = nodeSet.getDependencyMatrix().getAndToChildDependencyList(node.getNodeId());
	   
	    	
	    
    		if(LineType.VALUE_CONCLUSION.equals(lineType))
	    	{
	    		if(node.getNodeName().contains("IS IN LIST") 
	    				&& ast.getWorkingMemory().containsKey(node.getVariableName()) 
	    				&& ast.getWorkingMemory().containsKey(node.getFactValue().getValue().toString()))
	    		{
	    			ast.setFact(node.getNodeName(), node.selfEvaluate(ast.getWorkingMemory(), scriptEngine));
	    			canDetermine = true; 
	    		}
	    		else
	    		{
	    			boolean isPlainStatementFormat = ((ValueConclusionLine)node).getIsPlainStatementFormat();
		    		String nodeFactValueInString = node.getFactValue().getValue().toString();
		    		
		    		/*
		    		 * isAnyOrDependencyTrue() method contains trimming off method to cut off any 'UNDETERMINED' state 'OR' child nodes. 
		    		 */
		    		if(andToChildDependencies.isEmpty() && !orToChildDependencies.isEmpty()) // rule has only 'OR' child rules 
		    		{
		    			
		    			if(isAnyOrDependencyTrue(node, orToChildDependencies)) //TRUE case
		    			{
		    				int nodeId = node.getNodeId();
	    				    if(nodeSet.getDependencyMatrix().hasMandatoryChildNode(nodeId) && !hasAllMandatoryChildAnswered(nodeId))
	    					{
							return canDetermine;
	    					}
		    				canDetermine = true;
		    				
		    				handleValuConclusionLineTrueCase(node, isPlainStatementFormat, nodeFactValueInString);
		    				
		    			}
		    			else if(isAllRelevantChildDependencyDetermined(node, orToChildDependencies) && !isAnyOrDependencyTrue(node, orToChildDependencies)) //FALSE case
		    			{
		    				canDetermine = true;
		    				
		    				handleValueConclusionLineFalseCase(node, isPlainStatementFormat, nodeFactValueInString);
		    				
		    			}
		    		}
		    		else if(!andToChildDependencies.isEmpty() && orToChildDependencies.isEmpty())// node has only 'AND' child nodes
		    		{
		    			if(isAllRelevantChildDependencyDetermined(node, andToChildDependencies) && isAllAndDependencyTrue(node, andToChildDependencies)) // TRUE case
					{
		    				canDetermine = true;
		    				
		    				handleValuConclusionLineTrueCase(node, isPlainStatementFormat, nodeFactValueInString);
	    					
					}
		    			/*
		    			 * 'isAnyAndDependencyFalse()' contains a trimming off dependency method 
		    			 * due to the fact that all undetermined 'AND' child nodes need to be trimmed off when any 'AND' node is evaluated as 'NO'
	               	 * , which does not influence on determining a parent rule's evaluation.
	               	 * 
		    			 */
		    			else if(isAnyAndDependencyFalse(node, andToChildDependencies)) //FALSE case
		    			{
		    				int nodeId = node.getNodeId();
	    				    if(nodeSet.getDependencyMatrix().hasMandatoryChildNode(nodeId))
	    					{
	    						if(!hasAllMandatoryChildAnswered(nodeId))
	    						{
	    							return canDetermine;
	    						}
	    					}
		    				canDetermine = true;
		    				
		    				handleValueConclusionLineFalseCase(node, isPlainStatementFormat, nodeFactValueInString);	    					
		    			}				
		    		}
	    		}	    		
	    	}
    		else if(LineType.COMPARISON.equals(lineType))
    		{
    			if(andToChildDependencies.isEmpty() && !orToChildDependencies.isEmpty()) // rule has only 'OR' child rules 
	    		{
	    			/*
	    			 * the node might have a 'MANDATORY OR' child nodes so that the mandatory child nodes need being handled
	    			 */
	    			if(hasAnyOrChildEvaluated(node.getNodeId(), orToChildDependencies))
	    			{
	    				if(!hasAllMandatoryChildAnswered(node.getNodeId()))
	    				{
	    					return canDetermine = false;
	    				}
	    				canDetermine = true;
	    				ast.setFact(node.getNodeName(), node.selfEvaluate(ast.getWorkingMemory(), scriptEngine));
	    				ast.addItemToSummaryList(node.getNodeName());
	    			}
	    		}
	    		else if(!andToChildDependencies.isEmpty() && orToChildDependencies.isEmpty())// node has only 'AND' child nodes
	    		{
	    			/*
	    			 * in this case they are all 'MANDATORY' child nodes
	    			 */
	    			if(hasAllAndChildEvaluated(andToChildDependencies))
	    			{
	    				if(!hasAllMandatoryChildAnswered(node.getNodeId()))
	    				{
	    					return canDetermine = false;
	    				}
	    				canDetermine = true;
	    				ast.setFact(node.getNodeName(), node.selfEvaluate(ast.getWorkingMemory(), scriptEngine));
	    				ast.addItemToSummaryList(node.getNodeName());
	    			}
	    		}
    			
    		}
	    	else if(LineType.EXPR_CONCLUSION.equals(lineType))
	    	{
	    		if(andToChildDependencies.isEmpty() && !orToChildDependencies.isEmpty()) // rule has only 'OR' child rules 
	    		{
	    			/*
	    			 * the node might have a 'MANDATORY OR' child nodes so that the mandatory child nodes need being handled
	    			 */
	    			if(hasAnyOrChildEvaluated(node.getNodeId(), orToChildDependencies))
	    			{
	    				if(!hasAllMandatoryChildAnswered(node.getNodeId()))
	    				{
	    					return canDetermine = false;
	    				}
	    				canDetermine = true;
	    				ast.setFact(node.getVariableName(), node.selfEvaluate(ast.getWorkingMemory(), scriptEngine));
	    				ast.setFact(node.getNodeName(), node.selfEvaluate(ast.getWorkingMemory(), scriptEngine)); //inserting same value for node's name is for the purpose of display equation

	    				ast.addItemToSummaryList(node.getVariableName());
	    				ast.addItemToSummaryList(node.getNodeName()); // inserting node's name is to find its evaluated value from the workingMemory with its name
	    			}
	    		}
	    		else if(!andToChildDependencies.isEmpty() && orToChildDependencies.isEmpty())// node has only 'AND' child nodes
	    		{
	    			/*
	    			 * in this case they are all 'MANDATORY' child nodes
	    			 */
	    			if(hasAllAndChildEvaluated(andToChildDependencies))
	    			{
	    				if(!hasAllMandatoryChildAnswered(node.getNodeId()))
	    				{
	    					return canDetermine = false;
	    				}
	    				canDetermine = true;
	    				ast.setFact(node.getVariableName(), node.selfEvaluate(ast.getWorkingMemory(), scriptEngine));
	    				ast.setFact(node.getNodeName(), node.selfEvaluate(ast.getWorkingMemory(), scriptEngine)); //inserting same value for node's name is for the purpose of display equation

	    				ast.addItemToSummaryList(node.getVariableName());
	    				ast.addItemToSummaryList(node.getNodeName()); // inserting node's name is to find its evaluated value from the workingMemory with its name
	    			}
	    		}
	    		else
	    		{
	    			if(hasAnyOrChildEvaluated(node.getNodeId(), orToChildDependencies) && hasAllAndChildEvaluated(andToChildDependencies))
	    			{
	    				if(!hasAllMandatoryChildAnswered(node.getNodeId()))
	    				{
	    					return canDetermine = false;
	    				}
	    				canDetermine = true;
	    				ast.setFact(node.getVariableName(), node.selfEvaluate(ast.getWorkingMemory(), scriptEngine));
	    				ast.setFact(node.getNodeName(), node.selfEvaluate(ast.getWorkingMemory(), scriptEngine)); //inserting same value for node's name is for the purpose of display equation

	    				ast.addItemToSummaryList(node.getVariableName());
	    				ast.addItemToSummaryList(node.getNodeName()); // inserting node's name is to find its evaluated value from the workingMemory with its name

	    			}
	    		}
	    	}
	    	else if(LineType.ITERATE.equals(lineType))
	    	{
	    		if(((IterateLine)node).canBeSelfEvaluated(ast.getWorkingMemory()))
	    		{
	    			ast.setFact(node.getNodeName(), node.selfEvaluate(ast.getWorkingMemory(), scriptEngine));
    				ast.addItemToSummaryList(node.getVariableName());
	    		}
	    	}
 	
	    	return canDetermine;
	}
    
	
	private boolean hasAnyOrChildEvaluated(int parentNodeId, List<Integer>orToChildDependencies)
	{
		
		boolean hasAnyOrChildEvaluated = orToChildDependencies.stream().anyMatch(i -> 
																					(ast.getWorkingMemory().containsKey(nodeSet.getNodeByNodeId(i).getVariableName())
																							&& (nodeSet.getDependencyMatrix().getDependencyType(parentNodeId, i) & DependencyType.getMandatory()) == DependencyType.getMandatory()
																					)
																					|| 
																					ast.getWorkingMemory().containsKey(nodeSet.getNodeByNodeId(i).getVariableName()) 
																			   );
		
		return hasAnyOrChildEvaluated;
	}
	
	private boolean hasAllAndChildEvaluated(List<Integer> andToChildDependencies)
	{
		boolean hasAllAndChildEvaluated = andToChildDependencies.stream().allMatch(i -> ast.getWorkingMemory().containsKey(nodeSet.getNodeByNodeId(i).getVariableName()));
				
		return hasAllAndChildEvaluated;
	}
	
	private void handleValuConclusionLineTrueCase(Node node, boolean isPlainStatementFormat, String nodeFactValueInString)
	{
		ast.setFact(node.getNodeName(), FactValue.parse(true));
		
		if(!isPlainStatementFormat)
		{
			if(ast.getWorkingMemory().containsKey(nodeFactValueInString))
			{
				ast.setFact(node.getVariableName(),  ast.getWorkingMemory().get(nodeFactValueInString));
			}
			else
			{
				ast.setFact(node.getVariableName(), node.getFactValue());
			}
			ast.addItemToSummaryList(node.getVariableName());
		}
	}
	private void handleValueConclusionLineFalseCase(Node node, boolean isPlainStatementFormat, String nodeFactValueInString)
	{
		ast.setFact(node.getNodeName(), FactValue.parse(false));
		
		if(!isPlainStatementFormat)
		{
			if(ast.getWorkingMemory().containsKey(nodeFactValueInString))
			{
				ast.setFact(node.getVariableName(), FactValue.parse("NOT "+ast.getWorkingMemory().get(nodeFactValueInString)));
			}
			else
			{
				ast.setFact(node.getVariableName(), FactValue.parse("NOT "+nodeFactValueInString));
			}
			ast.addItemToSummaryList(node.getVariableName());
		}
	}
    
    public String getDefaultGoalRuleQuestion() 
    {
    		return nodeSet.getDefaultGoalNode().getNodeName();
    }
    public String getAssessmentGoalRuleQuestion(Assessment ass)
    {
    		return ass.getGoalNode().getNodeName();
    }
    
    public FactValue getDefaultGoalRuleAnswer() 
    {
    		return ast.getWorkingMemory().get(nodeSet.getDefaultGoalNode().getVariableName());
    }
    
    public FactValue getAssessmentGoalRuleAnswer(Assessment ass)
    {
    		return ast.getWorkingMemory().get(ass.getGoalNode().getVariableName());
    }


    /*
     Returns boolean value that can determine whether or not the given rule has any children
     this method is used within the process of backward chaining.
     */
    public boolean hasChildren(int nodeId)
    {
        boolean hasChildren = false;
        if (!nodeSet.getDependencyMatrix().getToChildDependencyList(nodeId).isEmpty())
        {
            hasChildren = true;
            nodeSet.getDependencyMatrix().getToChildDependencyList(nodeId).stream().forEach(item->{
	            	Node nodeName = nodeSet.getNodeByNodeId(item);
	            	addChildRuleIntoInclusiveList(nodeName);
            });
        }
        return hasChildren;
    }

    /*
    the method adds all children rules of relevant parent rule into the 'inlcusiveList' if they are not in the list.
    */
    public void addChildRuleIntoInclusiveList(Node node)
    {

	 	List<Integer> childrenListOfNode = nodeSet.getDependencyMatrix().getToChildDependencyList(node.getNodeId());
    	    	childrenListOfNode.stream().forEachOrdered(item -> {
    	    		String childNodeName = nodeSet.getNodeMap().get(nodeSet.getNodeIdMap().get(item)).getNodeName();
    	    		if(!ast.getInclusiveList().contains(childNodeName) && !ast.getExclusiveList().contains(childNodeName))
    	    		{
    	    			ast.getInclusiveList().add(childNodeName);
    	    		}
    	    	});

    }


    public boolean isAnyOrDependencyTrue(Node node, List<Integer> orChildDependencies)
    {
        boolean isAnyOrDependencyTrue = false;
        targetNode = node;
        if (!orChildDependencies.isEmpty())
        {	        	
	        	
	        	List<Integer> trueOrChildList = new ArrayList<>();
	        	orChildDependencies.stream().forEachOrdered(item -> {
	        		if(ast.isInclusiveList(nodeSet.getNodeIdMap().get(item)) 
	        			&& ast.getWorkingMemory().containsKey(nodeSet.getNodeIdMap().get(item)))
	        		{
	        			if((nodeSet.getDependencyMatrix().getDependencyType(targetNode.getNodeId(), item)&DependencyType.getKnown()) == DependencyType.getKnown()
						  &&((nodeSet.getDependencyMatrix().getDependencyType(targetNode.getNodeId(), item)&DependencyType.getNot()) != DependencyType.getNot())
					  )
	        			{
	        				trueOrChildList.add(item);
	        				if(!ast.getWorkingMemory().containsKey("KNOWN "+nodeSet.getNodeIdMap().get(item)))
	        				{
	        					ast.setFact("KNOWN "+nodeSet.getNodeIdMap().get(item), FactBooleanValue.parse(true));
	        					ast.addItemToSummaryList("KNOWN "+nodeSet.getNodeIdMap().get(item));
	        				}
	        				
	        			}
	        			else if(ast.getWorkingMemory().get(nodeSet.getNodeIdMap().get(item)).getValue().equals(true)
							  &&((nodeSet.getDependencyMatrix().getDependencyType(targetNode.getNodeId(), item)&DependencyType.getNot()) != DependencyType.getNot()))
	        			{
	        				
	        				trueOrChildList.add(item);
	        			}
	        			else if(ast.getWorkingMemory().get(nodeSet.getNodeIdMap().get(item)).getValue().equals(false)
							  &&((nodeSet.getDependencyMatrix().getDependencyType(targetNode.getNodeId(), item)&DependencyType.getNot()) == DependencyType.getNot()))
	        			{
	        				trueOrChildList.add(item);
	        				
	        				if(!ast.getWorkingMemory().containsKey("NOT "+nodeSet.getNodeIdMap().get(item)))
	        				{
	        					ast.setFact("NOT "+nodeSet.getNodeIdMap().get(item), FactBooleanValue.parse(true));
	        					ast.addItemToSummaryList("NOT "+nodeSet.getNodeIdMap().get(item));
	        				}

	        			}
	        		}
	        	});
	        	
	        	
	        	if(!trueOrChildList.isEmpty())
	        	{
	        		isAnyOrDependencyTrue = true;
	        		orChildDependencies.stream().forEachOrdered(i -> {
	        			trueOrChildList.stream().forEachOrdered(n -> {
	        				if(i != n)
	        				{
	        					trimDependency(node, i);
	        				}
	        			});
	        		});
	        	}
        }
        return isAnyOrDependencyTrue;
    }

	public void trimDependency(Node parentNode, int childNodeId)
    {
		int parentNodeId = parentNode.getNodeId();
		int dpType = nodeSet.getDependencyMatrix().getDependencyMatrixArray()[parentNodeId][childNodeId];
		int mandatoryDependencyType = DependencyType.getMandatory();
		List<Integer> parentDependencyList = nodeSet.getDependencyMatrix().getFromParentDependencyList(childNodeId);
		
		if((parentDependencyList.size()>1 																		      				// the child has more than one parent,
				&& parentDependencyList.parallelStream()																				// all parents have been determined
								       .allMatch(parent -> ast.getWorkingMemory().containsKey(nodeSet.getNodeIdMap().get(parent)))
			    && parentDependencyList.parallelStream()																				//the child has no Mandatory dependency parents
								       .noneMatch(parent -> (nodeSet.getDependencyMatrix().getDependencyMatrixArray()[parent][childNodeId]&mandatoryDependencyType)==mandatoryDependencyType))
			||
			(parentDependencyList.size() == 1                                    													    // the child has only one parent
			&& ((dpType & mandatoryDependencyType) != mandatoryDependencyType) ))   													// the dependency is not 'MANDATORY'
	    	{
	    		ast.getInclusiveList().remove(nodeSet.getNodeIdMap().get(childNodeId));
	    		if(!ast.getExclusiveList().contains(nodeSet.getNodeIdMap().get(childNodeId)))
    			{
	    			ast.getExclusiveList().add(nodeSet.getNodeIdMap().get(childNodeId));
    			}
	    		List<Integer> childDependencyListOfChildNode = nodeSet.getDependencyMatrix().getToChildDependencyList(childNodeId);
	    		if(!childDependencyListOfChildNode.isEmpty())
	    		{
	    			childDependencyListOfChildNode.stream().forEach(item->{
	    				trimDependency(nodeSet.getNodeByNodeId(childNodeId),item);
	    			});
	    		}
	    	}
		
    }
    
    public boolean isAnyAndDependencyFalse(Node node, List<Integer> andChildDependencies)
    {
        boolean isAnyAndDependencyFalse = false;
        targetNode = node;

        if (!andChildDependencies.isEmpty())
        {	
	        	List<Integer> falseAndList = new ArrayList<>();
	            
            andChildDependencies.stream().forEachOrdered(item -> {
            		if(ast.getWorkingMemory().containsKey(nodeSet.getNodeIdMap().get(item)))
            		{
            			if(ast.getWorkingMemory().get(nodeSet.getNodeIdMap().get(item)).getValue().equals(false)
    							&&((nodeSet.getDependencyMatrix().getDependencyType(targetNode.getNodeId(), item)&DependencyType.getNot()) != DependencyType.getNot())
    							&&((nodeSet.getDependencyMatrix().getDependencyType(targetNode.getNodeId(), item) & DependencyType.getKnown()) != DependencyType.getKnown()))
            			{
               	 		falseAndList.add(item);
            			}
            			else if(ast.getWorkingMemory().get(nodeSet.getNodeIdMap().get(item)).getValue().equals(true)
								&&((nodeSet.getDependencyMatrix().getDependencyType(targetNode.getNodeId(), item)&DependencyType.getNot()) == DependencyType.getNot())
								&&((nodeSet.getDependencyMatrix().getDependencyType(targetNode.getNodeId(), item) & DependencyType.getKnown()) != DependencyType.getKnown())
						)
            			{
            				if(!ast.getWorkingMemory().containsKey("NOT "+nodeSet.getNodeIdMap().get(item)))
            				{
            					ast.setFact("NOT "+nodeSet.getNodeIdMap().get(item), FactBooleanValue.parse(false));
            					ast.addItemToSummaryList("NOT "+nodeSet.getNodeIdMap().get(item));
            				}
            				
               	 		falseAndList.add(item);
            			}
            			else if((nodeSet.getDependencyMatrix().getDependencyType(targetNode.getNodeId(), item) & (DependencyType.getNot()|DependencyType.getKnown())) == (DependencyType.getNot()|DependencyType.getKnown()))
            			{
            				if(!ast.getWorkingMemory().containsKey("NOT KNOWN "+nodeSet.getNodeIdMap().get(item)))
            				{
            					ast.setFact("NOT KNOWN "+nodeSet.getNodeIdMap().get(item), FactBooleanValue.parse(false));
            					ast.addItemToSummaryList("NOT KNOWN "+nodeSet.getNodeIdMap().get(item));
            				}
            				
               	 		falseAndList.add(item);
            			}
            		}
            });
            
            if(falseAndList.size() > 0)
            {
	    	        	isAnyAndDependencyFalse = true;
	    	        	andChildDependencies.stream().forEachOrdered(i -> {
	    	        		falseAndList.stream().forEachOrdered(f -> {
	    	        			if(i != f)
	    	        			{
	    	        				trimDependency(node, i);
	    	        			}
	    	        		});
	    	        	});
            }
            else if(andChildDependencies.size() == 0)
            {
            		isAnyAndDependencyFalse = true;
            }
        }
         
        		 
       
        return isAnyAndDependencyFalse;
    }
    
    
    public boolean isAllAndDependencyTrue(Node node, List<Integer> andChildDependencies)
    {
        boolean isAllAndTrue = false;
        targetNode = node;

        	List<Integer> determinedTrueAndChildDependencies = new ArrayList<>();
        andChildDependencies.stream().forEachOrdered(item -> {
        	if(ast.isInclusiveList(nodeSet.getNodeIdMap().get(item))
	        && ast.getWorkingMemory().containsKey(nodeSet.getNodeIdMap().get(item)) )
    		{
    			if(ast.getWorkingMemory().get(nodeSet.getNodeIdMap().get(item)).getValue().equals(true)
				&&((nodeSet.getDependencyMatrix().getDependencyType(targetNode.getNodeId(), item)&DependencyType.getNot()) != DependencyType.getNot()))
    			{
    				determinedTrueAndChildDependencies.add(item);
    			}
    			else if((nodeSet.getDependencyMatrix().getDependencyType(targetNode.getNodeId(), item)&DependencyType.getKnown()) == DependencyType.getKnown()
					  &&((nodeSet.getDependencyMatrix().getDependencyType(targetNode.getNodeId(), item)&DependencyType.getNot()) != DependencyType.getNot()))
    			{
    				if(!ast.getWorkingMemory().containsKey("KNOWN "+nodeSet.getNodeIdMap().get(item)))
    				{
    					ast.setFact("KNOWN "+nodeSet.getNodeIdMap().get(item), FactBooleanValue.parse(false));
    					ast.addItemToSummaryList("KNOWN "+nodeSet.getNodeIdMap().get(item));
    				}
    				
    				determinedTrueAndChildDependencies.add(item);
    			}
    			else if(ast.getWorkingMemory().get(nodeSet.getNodeIdMap().get(item)).getValue().equals(false)
					  &&((nodeSet.getDependencyMatrix().getDependencyType(targetNode.getNodeId(), item)&DependencyType.getNot()) == DependencyType.getNot())
					  && ((nodeSet.getDependencyMatrix().getDependencyType(targetNode.getNodeId(), item)&DependencyType.getKnown()) != DependencyType.getKnown()))
    			{
    				if(!ast.getWorkingMemory().containsKey("NOT "+nodeSet.getNodeIdMap().get(item)))
    				{
    					ast.setFact("NOT "+nodeSet.getNodeIdMap().get(item), FactBooleanValue.parse(false));
    					ast.addItemToSummaryList("NOT "+nodeSet.getNodeIdMap().get(item));
    				}
    				
    				determinedTrueAndChildDependencies.add(item);
    			}
    		}
        	
        });
        
        if(andChildDependencies != null && determinedTrueAndChildDependencies.size() == andChildDependencies.size())
        {
        		isAllAndTrue = true;
        } 

       return isAllAndTrue;
    }

    public boolean isAllRelevantChildDependencyDetermined(Node node, List<Integer> allChildDependencies)
    {
        boolean isAllRelevantChildDependencyDetermined = false;
        targetNode = node;
        
        List<Integer> determinedAndOutDependencies = new ArrayList<>();
        allChildDependencies.stream().forEachOrdered(item ->{
	        														if(ast.getWorkingMemory().get(nodeSet.getNodeIdMap().get(item)) != null
	    																&&(nodeSet.getDependencyMatrix().getDependencyType(targetNode.getNodeId(), item) & (DependencyType.getNot()|DependencyType.getKnown())) == (DependencyType.getNot()|DependencyType.getKnown())
	    																&& !ast.getWorkingMemory().containsKey("NOT KNOWN "+nodeSet.getNodeIdMap().get(item)))
	        														{
	        															ast.setFact("NOT KNOWN "+nodeSet.getNodeIdMap().get(item), FactBooleanValue.parse(false));
	        															ast.addItemToSummaryList("NOT KNOWN "+nodeSet.getNodeIdMap().get(item));
	        														}
	        														else if(ast.getWorkingMemory().get(nodeSet.getNodeIdMap().get(item)) != null
	        																 &&ast.getWorkingMemory().get(nodeSet.getNodeIdMap().get(item)).getValue().equals(false)
	        																 &&(nodeSet.getDependencyMatrix().getDependencyType(targetNode.getNodeId(), item)&DependencyType.getNot()) == DependencyType.getNot()
	        																 && !ast.getWorkingMemory().containsKey("NOT "+nodeSet.getNodeIdMap().get(item)))
														        {
	        															ast.setFact("NOT "+nodeSet.getNodeIdMap().get(item), FactBooleanValue.parse(true));
	        															ast.addItemToSummaryList("NOT "+nodeSet.getNodeIdMap().get(item));
														        }
	        														else if(ast.getWorkingMemory().get(nodeSet.getNodeIdMap().get(item))  != null
	        																 &&(nodeSet.getDependencyMatrix().getDependencyType(targetNode.getNodeId(), item)&DependencyType.getKnown()) == DependencyType.getKnown()
	        																 && !ast.getWorkingMemory().containsKey("KNOWN "+nodeSet.getNodeIdMap().get(item)))
	        														{
	        															ast.setFact("KNOWN "+nodeSet.getNodeIdMap().get(item), FactBooleanValue.parse(true));
	        															ast.addItemToSummaryList("KNOWN "+nodeSet.getNodeIdMap().get(item));
	        														}
	        														else if(ast.getWorkingMemory().get(nodeSet.getNodeIdMap().get(item)) != null
	        																 &&ast.getWorkingMemory().get(nodeSet.getNodeIdMap().get(item)).getValue().equals(true)
	        																 &&(nodeSet.getDependencyMatrix().getDependencyType(targetNode.getNodeId(), item)&DependencyType.getNot()) == DependencyType.getNot()
	        																 && !ast.getWorkingMemory().containsKey("NOT "+nodeSet.getNodeIdMap().get(item)))
														        {
	        															ast.setFact("NOT "+nodeSet.getNodeIdMap().get(item), FactBooleanValue.parse(false));
	        															ast.addItemToSummaryList("NOT "+nodeSet.getNodeIdMap().get(item));
														        }
	        														
	        														
	        														if(ast.isInclusiveList(nodeSet.getNodeIdMap().get(item)) 
																	&& ast.getWorkingMemory().containsKey(nodeSet.getNodeIdMap().get(item)))
																{
	        															determinedAndOutDependencies.add(item);
																}
												        }
												       );
        
        
        if(allChildDependencies != null && determinedAndOutDependencies.size() == allChildDependencies.size())
        {
        		isAllRelevantChildDependencyDetermined = true;
        }


        return isAllRelevantChildDependencyDetermined;
    }


    public List<String> generateSortedSummaryList()
    {
	    	List<String> sortedSummaryList = new ArrayList<>();
		nodeSet.getNodeSortedList().stream().forEachOrdered(node ->{
			if(ast.getSummaryList().contains(node.getNodeName()))
			{
				sortedSummaryList.add(node.getNodeName());
			}
			if(ast.getSummaryList().contains("NOT "+node.getNodeName()))
			{
				sortedSummaryList.add("NOT "+node.getNodeName());
			}
			if(ast.getSummaryList().contains("KNOWN "+node.getNodeName()))
			{
				sortedSummaryList.add("KNOWN "+node.getNodeName());
			}
			if(ast.getSummaryList().contains("NOT KNOWN "+node.getNodeName()))
			{
				sortedSummaryList.add("NOT KNOWN "+node.getNodeName());
			}
		});
		
		ast.getSummaryList().stream().forEach(nodeName->{
			if(!sortedSummaryList.contains(nodeName))
			{
				sortedSummaryList.add(1, nodeName);
			}
		});
			
		return sortedSummaryList;
    }

//    public boolean allNeedsChildDetermined(Node parentNode, List<Integer> outDependency)
//    {
//	    	boolean allNeedsChildDetermined = false;
//	    	
//	    	int mandatoryAndDependencyType = DependencyType.getMandatory() | DependencyType.getAnd();
//	    	List<Integer> determinedList = outDependency.stream().filter(i -> (nodeSet.getDependencyMatrix().getDependencyMatrixArray()[parentNode.getNodeId()][i] & mandatoryAndDependencyType) == mandatoryAndDependencyType
//	    										&& ast.getWorkingMemory().containsKey(nodeSet.getNodeMap().get(nodeSet.getNodeIdMap().get(i)).getVariableName())).collect(Collectors.toList());
//	    	
//	    	if(outDependency.size() == determinedList.size())
//	    	{
//	    		allNeedsChildDetermined = true;
//	    	}    	
//	    	
//	    	return allNeedsChildDetermined;
//    }
    

    
 
   
    

    /**
     * make a summary of the assessment rules and answers as a html document
     * using a template for the structure and replacing markers the values
     * @return html
     */
//    public String generateAssessmentSummaryFromTemplate()
//    {    		    	
//     	
//	    Map<String, String> map = new HashMap<>();
//	    
//	    
//	    int i = 0 ; 
//		for (String ruleName : ast.getInclusiveList())
//		{
//			String ruleState = ast.getWorkingMemory().get(ruleName);
//			if (ruleState != null) {
//				map.put("rules["+i+"].number", Integer.toString(i+1));//add question to array
//				map.put("rules["+i+"].question", ruleName);//add question to array
//				map.put("rules["+i+"].answer", ruleState);// add answer to array
//				i++;
//			}
//		}
//		map.put("rules[]", Integer.toString(i)); //specify size of array
//		String html = Document.getTemplate("assessment_summary.xml");
//		html = Document.replaceValuesInHtml(html, map);
//		
//	    return html;
//	}
//
//    
    
    /*
    this method is to reset 'workingMemory' list and 'inclusiveList'
    usage of this method will depend on a user. if a user wants to continue to assessment on a same veteran with same conditions
    then don't need to reset 'workingMemory' and 'inclusiveList' otherwise reset them.
    */
    public void resetWorkingMemoryAndInclusiveList()
    {
        if(!ast.getInclusiveList().isEmpty())
        {
        		ast.getInclusiveList().removeAll(ast.getInclusiveList());
        }
        if(!ast.getWorkingMemory().isEmpty())
        {
        		ast.getWorkingMemory().clear();
        }
    }
    
    
    /*
     * this is to generate Assessment Summary
     * need to modify this method to have correct summary
     */
	public ObjectNode[] generateAssessmentSummary()
	{ 
		List<ObjectNode> tempSummaryList = new ArrayList<>();
		this.getAssessmentState().getSummaryList().stream().forEachOrdered((item)->{
			ObjectNode objectNode = new ObjectMapper().createObjectNode();
			objectNode.put("nodeText", item);
			objectNode.put("nodeValue", this.getAssessmentState().getWorkingMemory().get(item).getValue().toString());
			tempSummaryList.add(objectNode);
		});
		
		return tempSummaryList.stream().toArray(ObjectNode[]::new);
    }
	
//    public String generateAssessmentSummary()
//    {    		    	
//    	StringBuilder htmlText = new StringBuilder();
//    	htmlText.append("<!DOCTYPE html>"+"\n"+
//    	                "<html>"+"\n"+
//    	                "<head><title></title></head>"+"\n"+
//    	                "<body><h3> Assessment Summary</h3>"+"\n");
//    	int summaryListSize = ast.getSummaryList().size();
//    	if(summaryListSize != 0 )
//    	{
//    		htmlText.append("<ol type =\"1\">"+"\n");
//    		
//    		for(int i = summaryListSize; i > 0; i--)
//        	{
//        		Rule rule = ast.getSummaryList().get(i);
//        		FactValue factValue = ast.getWorkingMemory().get(rule.getVariableName());
//        		FactValueType factValueType = factValue.getType();
//        		String printingValue = null;
//        		if(factValue != null)
//        		{
//        			if(factValueType.equals(FactValueType.BOOLEAN))
//            		{
//            			printingValue = Boolean.toString(((FactBooleanValue)factValue).getValue());
//            			printingValue = printingValue.toUpperCase();
//            		}
//            		else if(factValueType.equals(FactValueType.DATE))
//            		{
//            			printingValue =((FactDateValue)factValue).getValue().toString();
//            		}
//            		else if(factValueType.equals(FactValueType.DOUBLE))
//            		{
//            			printingValue = Double.toString(((FactDoubleValue)factValue).getValue());
//            		}
//            		else if(factValueType.equals(FactValueType.INTEGER))
//            		{
//            			printingValue = Integer.toString(((FactIntegerValue)factValue).getValue());
//            		}
//            		else if(factValueType.equals(FactValueType.LIST))
//            		{
//            			
//            		}
//        		}
//        		
//        		
//        		
//        		
//        		if(ruleState != null)
//        		{
//        			
//        			htmlText.append("<li>"+ruleName+" : "+ruleState+"</li>"+"\n");
//        		}
//        		
//        	}    
//        	htmlText.append("</ol>"+"\n");
//    	}
//    	htmlText.append("</body>"+"\n"+
//						"</html>"+"\n");
//    	
//     	
//    	return htmlText.toString();
//    }
    
    /*
     * this is to find a condition with a list of given keyword
     */
    public List<String> findCondition(String keyword)
    {
    	int initialSize = nodeSet.getNodeSortedList().size();
    	List<String> conditionList = new ArrayList<>(initialSize);
    	List<String> questionList = new ArrayList<>(initialSize);
    	for(Node node: nodeSet.getNodeSortedList())
    	{
    		if(nodeSet.getDependencyMatrix().getToChildDependencyList(node.getNodeId()).isEmpty())
    		{
    			questionList.add(node.getNodeName());
    		}
    	}
    	
    	String[] keywordArray = keyword.split("\\W+"); // split the keyword by none word character including whitespace.
    	int keywordArrayLength = keywordArray.length;
    	int numberOfMatched = 0;
    	for(String ruleName: questionList)
    	{
    		numberOfMatched = 0;
    		for(int i = 0; i < keywordArrayLength; i++)
    		{
    			if(ruleName.contains(keywordArray[i]))
    			{
    				numberOfMatched++;
    			}
    		}
    		if(numberOfMatched == keywordArrayLength)
    		{
    			conditionList.add(ruleName);
    		}
    	}
    	
    	return conditionList;
    }
}

