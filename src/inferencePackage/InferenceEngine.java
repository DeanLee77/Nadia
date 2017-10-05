package inferencePackage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import factValuePackage.*;
import nodePackage.*;


public class InferenceEngine {
	private NodeSet nodeSet;
    private AssessmentState ast;
    private List<Node> nodeFactList;
    private ScriptEngine scriptEngine = new ScriptEngineManager().getEngineByName("nashorn");
 
    
    
   
//    private int ruleIndex = 0;

    public InferenceEngine(NodeSet nodeSet)
    {
	    	this.nodeSet = nodeSet;
	    	ast = newAssessmentState();
	    	
	    	nodeFactList = new ArrayList<>(nodeSet.getNodeSortedList().size()*2); // contains all rules set as a fact given by a user from a ruleList
    	
    }

    public void addNodeSet(NodeSet nodeSet2)
    {
    	
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
	    	List<String> exclusiveList = new ArrayList<>(initialSize);
	    	List<Node> summaryList = new ArrayList<>(initialSize);
	    	ast.setInclusiveList(inclusiveList);
	    	ast.setExclusiveList(exclusiveList);
	    	ast.setSummaryList(summaryList);
	    	
	    	return ast;
    	
    }
    
    
    
    /*
     * this method is to extract all variableName of Nodes, and put them into a List<String>
     * it may be useful to display and ask a user to select which information they do have even before starting Inference process
     */
    public List<String> getListOfVariableNameOfNodes()
    {
	    	List<String> variableNameList = null;
	    	nodeSet.getNodeMap().values().stream().forEachOrdered(node -> variableNameList.add(node.getVariableName()));
	    	
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
	    	if(ast.getWorkingMemory().get(ass.getGoalNode().getNodeName())== null)
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
	  	            if (!hasChildren(node) && ast.getInclusiveList().contains(node.getNodeName()) && !canEvaluate(node))
	  	            {
		  	            	ass.setNodeToBeAsked(node);
		  	            	int indexOfRuleToBeAsked = i;
		  	            	System.out.println("indexOfRuleToBeAsked : "+indexOfRuleToBeAsked);
		  	            	return ass.getNodeToBeAsked();
	  	            }
		            else
		            {
		            	
			            	if(hasChildren(node) && !ast.getWorkingMemory().containsKey(node.getVariableName()) 
			            			&& !ast.getWorkingMemory().containsKey(node.getNodeName()) && ast.getInclusiveList().contains(node.getNodeName()))
			            	{
			            		addChildRuleIntoInclusiveList(node);
			            	}
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
    			 * if the line format is 'A -statement' then node's name and variableName has same value so that either of them can be asked as a question
    			 * 
    			 * if the line format is 'A IS IN LIST B' then the value of node's variableName is 'A' and the value of node's value is 'B' so that only 'A' needs to be asked.
    			 * list 'B' has to be provided in 'INPUT' or 'FIXED' list
    			 * 
    			 * In conclusion, if the line type is 'ValueConclusionLine' then node's variableName should be asked regardless its format
    			 */

			questionList.add(nodeToBeAsked.getVariableName());
    		}
    		// ComparionLine type
    		else if(lineTypeOfNodeToBeAsked.equals(LineType.COMPARISON))
    		{
			questionList.add(nodeToBeAsked.getVariableName());
			
    			if(!nodeToBeAsked.getFactValue().getType().equals(FactValueType.DEFI_STRING) || !hasAlreadySetType(nodeToBeAsked.getFactValue()))
    			{
    				questionList.add(nodeToBeAsked.getFactValue().getValue().toString());
    			}
    		}
	    	
	    	return questionList;
    }
    
    public FactValueType findTypeOfElementToBeAsked(Node node)
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
	    	boolean hasAlreadySetType = hasAlreadySetType(node.getFactValue());
	    	HashMap<String, FactValue> tempFactMap = this.nodeSet.getFactMap();
	    	HashMap<String, FactValue> tempInputMap = this.nodeSet.getInputMap();
	    	boolean isComparisonLineType = node.getLineType().equals(LineType.COMPARISON);
	    	boolean isValueConclusionLineType = node.getLineType().equals(LineType.VALUE_CONCLUSION);
	   
	    	
	    	//ComparisonLine type node and type of the node's value is clearly defined 
	    	if(isComparisonLineType)
	    	{
	    		FactValueType nodeRHSType = ((ComparisonLine)node).getRHS().getType();
	    		if(nodeRHSType.equals(FactValueType.DEFI_STRING))
	    		{
	    			fvt = FactValueType.STRING;
	    		}
	    		else if(hasAlreadySetType)
	    		{
	    			fvt= nodeRHSType;
	    		}
	    		else if(nodeRHSType.equals(FactValueType.STRING) || nodeRHSType.equals(FactValueType.TEXT))
	    		{
	    			if(tempInputMap.containsKey(((ComparisonLine)node).getLHS()))
	    			{
	    				fvt = tempInputMap.get(((ComparisonLine)node).getLHS()).getType();
	    			}
	    			else if(tempInputMap.containsKey(((ComparisonLine)node).getRHS().getValue().toString()))
	    			{
	    				fvt = tempInputMap.get(((ComparisonLine)node).getRHS().getValue().toString()).getType();
	    			}
	    		}
	    	}
	    	//ComparisonLine type node and type of the node's value is not clearly defined and not defined in INPUT nor FIXED list
	    	//ValueConclusionLine type node and it is 'A-statement' line, and variableName is not defined neither INPUT nor FIXED 
	    	else if(isValueConclusionLineType)
	    	{
	    		if(((ValueConclusionLine)node).getIsPlainStatementFormat() && tempInputMap.containsKey(nodeVariableName))
	    		{
	    			fvt = tempInputMap.get(nodeVariableName).getType();
	    		}
	    		else
	    		{
	    			fvt = FactValueType.BOOLEAN;
	    		}
	    	}
	    	else
	    	{
	        	FactValue factValueForNodeVariable = tempFactMap.get(nodeVariableName) == null? tempInputMap.get(nodeVariableName):tempFactMap.get(nodeVariableName);
	        	FactValueType factValueTypeForNodeVariable = factValueForNodeVariable != null? factValueForNodeVariable.getType():null;
	        	FactValue factValueForNodeValue = tempFactMap.get(nodeValueString) == null? tempInputMap.get(nodeValueString):tempFactMap.get(nodeValueString);
	        	FactValueType factValueTypeForNodeValue = factValueForNodeValue != null? factValueForNodeValue.getType():null;
	        	if((factValueTypeForNodeVariable != null && factValueTypeForNodeVariable.equals(FactValueType.BOOLEAN)) || (factValueTypeForNodeValue != null && factValueTypeForNodeValue.equals(FactValueType.BOOLEAN)))
	        	{
	        		fvt = FactValueType.BOOLEAN;
	        	}
	        	else if((factValueTypeForNodeVariable != null && factValueTypeForNodeVariable.equals(FactValueType.DATE)) || (factValueTypeForNodeValue != null && factValueTypeForNodeValue.equals(FactValueType.DATE)))
	        	{
	        		fvt = FactValueType.DATE;
	        	}
	        	else if((factValueTypeForNodeVariable != null && (factValueTypeForNodeVariable.equals(FactValueType.DECIMAL) || factValueTypeForNodeVariable.equals(FactValueType.DOUBLE))) || (factValueTypeForNodeValue != null && (factValueTypeForNodeValue.equals(FactValueType.DECIMAL) || factValueTypeForNodeValue.equals(FactValueType.DOUBLE))))
	        	{
	        		fvt = FactValueType.DOUBLE;
	        	}
	        	else if((factValueTypeForNodeVariable != null && factValueTypeForNodeVariable.equals(FactValueType.HASH)) || (factValueTypeForNodeValue != null && factValueTypeForNodeValue.equals(FactValueType.HASH)))
	        	{
	        		fvt = FactValueType.HASH;
	        	}
	        	else if((factValueTypeForNodeVariable != null && factValueTypeForNodeVariable.equals(FactValueType.URL)) || (factValueTypeForNodeValue != null && factValueTypeForNodeValue.equals(FactValueType.URL)))
	        	{
	        		fvt = FactValueType.URL;
	        	}
	        	else if((factValueTypeForNodeVariable != null && factValueTypeForNodeVariable.equals(FactValueType.UUID)) || (factValueTypeForNodeValue != null && factValueTypeForNodeValue.equals(FactValueType.UUID)))
	        	{
	        		fvt = FactValueType.UUID;
	        	}
	        	else if((factValueTypeForNodeVariable != null && (factValueTypeForNodeVariable.equals(FactValueType.INTEGER) || factValueTypeForNodeVariable.equals(FactValueType.NUMBER))) || (factValueTypeForNodeValue != null && (factValueTypeForNodeValue.equals(FactValueType.INTEGER) || factValueTypeForNodeValue.equals(FactValueType.NUMBER))))
	        	{
	        		fvt = FactValueType.INTEGER;
	        	}
	        	else if((factValueTypeForNodeVariable != null && (factValueTypeForNodeVariable.equals(FactValueType.STRING)|| factValueTypeForNodeVariable.equals(FactValueType.TEXT))) || (factValueTypeForNodeValue != null && (factValueTypeForNodeValue.equals(FactValueType.STRING) || factValueTypeForNodeValue.equals(FactValueType.TEXT))))
	        	{
	        		fvt = FactValueType.STRING;
	        	}
	    	}
	
	    	
	    	return fvt;
    }
    
    public boolean hasAlreadySetType(FactValue value)
    {
    		boolean hasAlreadySetType = false;
    		
    		FactValueType factValueType = value.getType();
    		if(!factValueType.equals(FactValueType.NULL) || !factValueType.equals(FactValueType.OBJECT) || !factValueType.equals(FactValueType.STRING) || !factValueType.equals(FactValueType.TEXT) || !factValueType.equals(FactValueType.UNKNOWN))
		{
    			hasAlreadySetType = true;
		}
    		return hasAlreadySetType;
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
		    	    	 * The reason being to check only if there is a value for a variableName of the node in the workingMemory is that
		    	    	 * only a value for variableName of the node is needed to evaluate the node in this case of ValueConclusionLine because
		    	    	 * variableName and nodeName will have a same value if the node is the most child node, which means that the statement for the node does NOT contain
		    	    	 * 'IS' keyword. 
		    	    	 */
	    			canEvaluate = true;
	    			
	    			/*
		    		 * the reason why ast.setFact() is used here rather than this.feedAndwerToNode() is that LineType is already known, and target node object is already found. 
		    		 * node.selfEvaluation() returns a value of the node's self-evaluation hence, node.getNodeName() is used to store a value for the node itself into a workingMemory
		    		 */
		    		ast.setFact(node.getNodeName(), node.selfEvaluate(ast.getWorkingMemory(), this.scriptEngine));
	    		}
	    	}
	    	else if(lineType.equals(LineType.COMPARISON))
	    	{
	    		
	    	}
	    	else if(lineType.equals(LineType.EXPR_CONCLUSION))
	    	{
	    		
	    	}
	    	else if(lineType.equals(LineType.ITERATE))
	    	{
	    		
	    	}
	    	return canEvaluate;
    }

    /* 
     * this method is to add fact or set a node as a fact by using AssessmentState.setFact() method. it also is used to feed an answer to a being asked node.
     * once a fact is added then forward-chain is used to update all effected nodes' state, and workingMemory in AssessmentState class will be updated accordingly
     * the reason for taking nodeName instead nodeVariableName is that it will be easier to find an exact node with nodeName
     * rather than nodeVariableName because a certain nodeVariableName could be found in several nodes
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public <T> void feedAnswerToNode(String NodeName, String questionName, T nodeValue, FactValueType nodeValueType)
    {
	    	Node targetNode = nodeSet.getNodeMap().get(NodeName);	    	
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
	    	
	    	
	    	/*
	    	 * once an answer is given to the engine, then followings need to be done;
	    	 *  1. The engine needs to know if the answer is for the node's variableName or the node's value(FactValue)
	    	 *  2. set a value of a question, which is a value for a variableName or value of the node, being asked to a user in a workingMemory
	    	 *  3. set a value of the node itself in a workingMemory.
	    	 *     
	    	 */
	    	if(questionName.equals(targetNode.getVariableName()))
	    	{
	        	ast.setFact(targetNode.getVariableName(), fv);
	    	}
	    	else if(questionName.equals(targetNode.getFactValue().getValue().toString()))
	    	{
	        	ast.setFact(targetNode.getFactValue().getValue().toString(), fv);
	    	}
	    	
	    	FactValue selfEvalFactValue = targetNode.selfEvaluate(ast.getWorkingMemory(), this.scriptEngine);
	    	
	    	if(selfEvalFactValue != null)
	    	{
	    		ast.setFact(targetNode.getNodeName(), selfEvalFactValue); // add the value of targetNode itself into the workingMemory	
    			LineType lineType = targetNode.getLineType();
	    		Boolean canHaveNot = (lineType.equals(LineType.VALUE_CONCLUSION) || lineType.equals(LineType.COMPARISON)|| lineType.equals(LineType.ITERATE))?true:false;
	    		Boolean canHaveKnown = lineType.equals(LineType.VALUE_CONCLUSION);
	    		IntStream.range(0, nodeSet.getDependencyMatrix().getDependencyMatrixArray()[0].length).forEach(i -> {
	    			int dependencyType = nodeSet.getDependencyMatrix().getDependencyMatrixArray()[i][targetNode.getNodeId()]; 
	    			
	    			if(dependencyType!= 0)
	    			{
	    				if(canHaveKnown && ((dependencyType&(DependencyType.getNot()|DependencyType.getKnown())) == (DependencyType.getNot()|DependencyType.getKnown())))
	    				{
	    					ast.setFact("NOT KNOWN"+targetNode.getNodeName(), ((FactBooleanValue)selfEvalFactValue).negatingValue());
	    				}
	    				/*
	    				 * ValueConclusionLine, ComparisonLine and IterateLine can have 'NOT'
	    				 */
	    				else if((dependencyType & DependencyType.getNot()) == DependencyType.getNot() && canHaveNot && !ast.getWorkingMemory().containsKey("NOT "+targetNode.getNodeName()))
	    				{
	    					ast.setFact("NOT "+targetNode.getNodeName(), ((FactBooleanValue)selfEvalFactValue).negatingValue());
	    				}
	    				/*
	    				 * ValueConclusionLine can have 'KNOWN' 
	    				 */
	    				else if((dependencyType & DependencyType.getKnown()) == DependencyType.getKnown() && canHaveKnown)
	    				{
	    					ast.setFact("NOT "+targetNode.getNodeName(), FactValue.parse(true));
	    				}
	    			}
	    		});
	
	        	/*
	        	 * Note: in order to get summary view, each rules can be found in summaryList, and 
	        	 *       actual evaluation value can be found in workingMemory by looking up with each node's variable
	        	 */
	        	ast.getSummaryList().add(targetNode);
	        	/*
	        	 * once any rules are set as fact and stored into the workingMemory, forward-chaining(back-propagation) needs to be done
	        	 */
	        	forwardChaining(nodeSet.findNodeIndex(targetNode.getNodeName()));
	    	}
    }
    
   
    public void forwardChaining(int nodeIndex)
    {
	    	/*
	    	 * all nodes prior to 'nodeIndex' in the nodeList(sortedList) of nodeSet should be updated once the node at a nodeIndex is being answered for following reasons;
	    	 * 1. regardless the nodeList is sorted with Khan's algorithm which is based on BFS, 
	    	 *    all nodes in the inclusiveList and prior to the node at a nodeIndex are possibly parent nodes of the node at nodeIndex; 
	    	 * 2. the list may be sorted based on historical statistic record, and if it is the case then the sorting is based on Deepening and Greedy algorithm with Bayesian inferencing ;
	    	 * 3. there could be a node sharing parents nodes or children nodes in the list
	    	 * And therefore, updating all nodes in the list based on a given fact is a safe way to get a next question and complete assessment.
	    	 */
	    	
	    	IntStream.range(0, nodeIndex+1).forEach(i -> {
	    		
	    		Node node = nodeSet.getNodeSortedList().get(nodeIndex-i);
	    		
	    		/* updating all nodes' state prior to nodeIndex in the sorted List 
	    		 * by setting the value of nodeVariables and nodeName of each node in workignMemory
	    		 * 
             * if the node currently being checked exists in the 'inclusiveList'
             * then check if the node has any children then update the current node's state based on the children's state
             * Note: a question will be asked if only if the being asked question is in the inclusiveLsit, however, following condition
             * is to do double check if the being asked question is in the inclusiveList
             */
	    		if(ast.getInclusiveList().contains(node.getNodeName()))
	    		{
	    			
	    			backPropagation(nodeIndex-i);
	    		}
	    		
	    		 /*
             * following 'if' condition is to do
             * adding parent nodes to 'inclusiveList' if only the current node is in the 'workingMemory' list 
             * because only parent nodes of the node that is in 'workingMemory' list are only relevant.
             * this condition helps to do faster performance
             */
	    		
            if(ast.getWorkingMemory().containsKey(node.getVariableName()) || ast.getWorkingMemory().containsKey(node.getNodeName()))
            {
	            	/*
	            	 * background of following method is as listed below
	            	 * 1. adding child rules into inclusiveList sometimes miss out relevant nodes because some nodes have more than two parents hence tracking only child nodes
	            	 * would not be enough to find all relevant nodes. As result, finding a child node of a certain node and the parent node of child node will cover all relevant nodes for an assessment
	            	 */
	            	addParentIntoInclusiveList(node); // adding all parents rules into the 'inclusiveList' if there is any
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
    
	/*
	 * once a user feeds an answer to the engine, the engine will propagate the entire NodeSet or Assessment base on the answer
	 * during the back-propagation, the engine checks if current node the engine is checking;
	 * 1. has been determined;
	 * 2. has any child nodes;
	 * 3. can be determined on the ground of various condition.
	 * 
	 *  once the current checking node meets the condition then add it to summaryList for summary view.
	 * 
	 * TODO need to consider ITERATE line type for this back-propagation due to there would be possibilities a list for the value of ITERATE will be generated or provided
	 * during other rules back-propagation
	 */
	public void backPropagation(int i) 
	{
		
	    	Node currentNode = nodeSet.getNodeSortedList().get(i);
	    	LineType currentLineType = currentNode.getLineType();
	    	/*
	     * following 'if' statement is to double check if the rule has any children or not.
	     * it will be already determined by asking a question to a user if it doesn't have any children .
	     */
	   if (!ast.getWorkingMemory().containsKey(currentNode.getVariableName()) 
			   && hasChildren(currentNode) && canDetermine(currentNode, currentLineType) )
	   {
		   		ast.getSummaryList().add(currentNode); // add currentRule into SummeryList as the rule determined
	   }
	}
    
	public boolean canDetermine(Node node, LineType lineType)
	{
	    	boolean canDetermine = false;
	    	/*
	    	 * Any type of node/line can have either 'OR' or 'AND' child nodes
	    	 * do following logic to check whether or not the node is determinable
	    	 * 1. check the node/line type
	    	 * 2. within the node/line type, check if it has 'OR' child nodes or 'AND' child nodes ( nodeSet.getDependencyMatrix.getOrOutDependency(node.getNodeId()).isEmpty() or .getAndOutDependency(node.getNodeId()).isEmpty()).
	    	 *    Apparently, dependencyType contains information of 'NOT', 'KNOWN', 'MANDATORY', 'OPTIONAL', and/or 'POSSIBLE' so that dependencyType must be checked to propagate.
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
	    	 * -----ExprConclusion Type
	    	 *    within rule text file, this node has two types of child node, 'NEEDS' and 'WANTS'.
	    	 *    'NEEDS' child rule will be translated as 'MANDATORY_AND', and 'WANTS' child node will be 'OR' in the rule structure.
	    	 *    In addition, back-propagation(evaluation) part will be done within checking dependency type,
	    	 *    due to the case of the node having 'NEEDS' and 'WANTS' type to different parent nodes at the same time.
	    	 *    the child node type can be either only 'MANDATORY_AND's, or 'MANDATORY_AND' and 'OR'.
	    	 *    As a result, followings need checking
	    	 *    E.1. if it has 'OR' child nodes
	    	 *         E.1.1 the node CAN BE EVALUATED case
	    	 *               if 'MANDATORY_OR' child node is determined, which means 'MANDATORY_AND' child node is determined
	    	 *               then trim off 'UNDETERMINED' child nodes
	    	 *         E.1.2 the node CAN'T BE EVALUATED case
	    	 *               if 'MANDATORY_OR' child node is not determined yet, which means 'MANDATORY_AND' child is not determined yet.
	    	 *               
	    	 *    E.2 if it has 'AND' child nodes
	    	 *        E.2.1 the rule CAN BE EVALUATED case
	    	 *              if all 'MANDATORY_AND' rules are determined.
	    	 * 
	    	 * -----Comparison Type
	    	 *    within rule text file, this node must not have any child nodes. 
	    	 *    However, in the rule structure of NodeSet class, the node which contains '=' operator' can have only 'MANDATORY_OR' child node of ValueConclusionLine Type or ExprConclusionLine Type if there is. 
	    	 *    The reason for having 'MANDATORY_OR' is that the node could have multiple ValueConclusion type or ExprConclusion type so that we must check all result of each child nodes to make decision on the node.
	    	 *    
	    	 *    In addition, the value of node type must exist among its child nodes' value.
	    	 *    As a result, in order to confirm that whether or not the node type can be determined, there must be a value of its variableName in the workingMemory. 
	    	 *    Back-propagation(evaluation) part will be done within the node itself by executing selfEvaluate().
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
	    	List<Integer> allFromParentDependencyList = nodeSet.getDependencyMatrix().getFromParentDependencyList(node.getNodeId());
	    	
	    	if(LineType.VALUE_CONCLUSION.equals(lineType))
	    	{
	    		/*
	    		 *	1. the rule is a plain statement
	    		 *		- evaluate based on outcome of its child nodes
	    		 *		  there only will be an outcome of entire rule statement with negation or known type value
	    		 *	2. the rule is a statement of 'A IS B'
	    		 *		- evaluate based on outcomes of its child nodes
	    		 *		  there will be an outcome for a statement of that is it true for 'A = B'?
	    		 * 	3. the rule is a statement of 'A IS IN LIST: B'
	    		 * 	4. the rule is a statement of 'needs(wants) A'. this is from a child node of ExprConclusionLine type 
	    		 */
	    		boolean isPlainStatementFormat = ((ValueConclusionLine)node).getIsPlainStatementFormat();
	    		
	    		/*
	    		 * isAnyOrDependencyTrue() method contains trimming off method to cut off any 'UNDETERMINED' state 'OR' child nodes. 
	    		 */
	    		if(andToChildDependencies.isEmpty() && !orToChildDependencies.isEmpty()) // rule has only 'OR' child rules 
	    		{
	    			
	    			if(isAnyOrDependencyTrue(node, orToChildDependencies)) //TRUE case
	    			{
	    				canDetermine = true;
	    				if(isPlainStatementFormat)
					{
	    					ast.setFact(node.getVariableName(), FactValue.parse(true));
					}
	    				else
	    				{
	    					ast.setFact(node.getVariableName(), node.getFactValue());
	    				}
	    				
	    				
	    				ast.setFact(node.getNodeName(), node.selfEvaluate(ast.getWorkingMemory(), this.scriptEngine, nodeOption));
	    			}
	    			else if(isAllOrDependencyDetermined(orToChildDependencies) && !isAnyOrDependencyTrue(node, orToChildDependencies)) //FALSE case
	    			{
	    				canDetermine = true;
	    				if(isPlainStatementFormat)
					{
	    					ast.setFact(node.getVariableName(), FactValue.parse(false));
					}
	    				else
	    				{
	    					ast.setFact(node.getVariableName(), FactValue.parse(node.getVariableName()));
	    				}
	    				    				
	    				ast.setFact(node.getNodeName(), node.selfEvaluate(ast.getWorkingMemory(), this.scriptEngine, nodeOption));
	
	    			}
	    		}
	    		else if(!nodeAndOutDependencies.isEmpty() && nodeOrOutDependencies.isEmpty())// rule has only 'AND' child rules
	    		{
	    			if(isAllAndDependencyDetermined(nodeAndOutDependencies) && isAllAndDependencyTrue(node, nodeAndOutDependencies)) // TRUE case
					{
		    				canDetermine = true;
		    				if(isPlainStatementFormat)
						{
		    					ast.setFact(node.getVariableName(), FactValue.parse(false));
						}
		    				else
		    				{
		    					ast.setFact(node.getVariableName(), FactValue.parse(node.getVariableName()));
		    				}
		    				    				
		    				ast.setFact(node.getNodeName(), node.selfEvaluate(ast.getWorkingMemory(), this.scriptEngine, nodeOption));
	
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
	    					ast.setFact(node.getVariableName(), FactValue.parse(false));
					}
	    				else
	    				{
	    					ast.setFact(node.getVariableName(), FactValue.parse(node.getVariableName()));
	    				}
	    				
	    				ast.setFact(node.getNodeName(), node.selfEvaluate(ast.getWorkingMemory(), this.scriptEngine, nodeOption));
	    				
	    			}
			
	    		}
	    	}
	    	else if(LineType.EXPR_CONCLUSION.equals(lineType))
	    	{
	
	    		if(!nodeAndOutDependencies.isEmpty() && nodeOrOutDependencies.isEmpty()) // rule has 'MANDATORY_OR' and 'OR' child rules 
	    		{
	    			for(int i=0; i < nodeOrOutDependencies.size(); i++)
	    			{
	    				int mandatoryOrDependencyType = DependencyType.getMandatory() | DependencyType.getOr();
	    				if((nodeSet.getDependencyMatrix().getDependencyMatrixArray()[node.getNodeId()][nodeOrOutDependencies.get(i)] & mandatoryOrDependencyType) == mandatoryOrDependencyType)
	    				{
	    					if(ast.getWorkingMemory().get(nodeSet.getNodeMap().get(nodeSet.getNodeIdMap().get(nodeOrOutDependencies.get(i))).getVariableName()) != null)
	    					{
	    						canDetermine = true;
	    						ast.setFact(node.getVariableName(), node.selfEvaluate(ast.getWorkingMemory(), this.scriptEngine, nodeOption)); // add currentRule into the workingMemory
	    					}
	    				}
	    				else
	    				{
	    					ast.getInclusiveList().remove(nodeSet.getNodeMap().get(nodeSet.getNodeIdMap().get(i)).getNodeName());
	    				}
	    			}    			
	    		}
	    		else if(nodeAndOutDependencies.isEmpty() &&!nodeOrOutDependencies.isEmpty())// rule has only 'MANDATORY_AND' child rules
	    		{
	    			if(allNeedsChildDetermined(node, nodeAndOutDependencies)) // TRUE case
					{
		    				canDetermine = true;
		    				/*  
	 	                 * The reason why ast.setFact() is used here rather than this.addFactToRule() is that ruleType is already known, and target rule object is already found. 
	 	                 */
	 	                ast.setFact(node.getVariableName(), node.selfEvaluate(ast.getWorkingMemory(), this.scriptEngine, nodeOption)); // add currentRule into the workingMemory
					}
			
	    		}
	    	}
	    	else if(LineType.COMPARISON.equals(lineType))
	    	{
	    		
	    		if(ast.getWorkingMemory().get(node.getVariableName()) != null)
	    		{
	    			canDetermine = true;
	    			/*  
	    			 * The reason why ast.setFact() is used here rather than this.addFactToRule() is that ruleType is already known, and target rule object is already found. 
	    			 */
	     		   ast.setFact(node.getNodeName(), node.selfEvaluate(ast.getWorkingMemory(), this.scriptEngine, nodeOption)); // add currentRule into the workingMemory
	    		}
	    	}
	    	else if(LineType.ITERATE.equals(lineType))
	    	{
	    		if(ast.getWorkingMemory().get(node.getFactValue().getValue().toString()) != null )
	    		{
	    			canDetermine = true;
	    			ast.setFact(node.getNodeName(), node.selfEvaluate(ast.getWorkingMemory(), this.scriptEngine, nodeOption));
	    		}
	    	}
	    	
	    	return canDetermine;
	}
    

   public  void evaluateAllDependenciesFromChildToParent(Node node, List<Integer> incomingDependencyList)
   {
	   
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
    public boolean hasChildren(Node node)
    {
        boolean hasChildren = false;
        if (!nodeSet.getDependencyMatrix().getOutDependencyList(node.getNodeId()).isEmpty())
        {
            hasChildren = true;
        }
        return hasChildren;
    }

    /*
    the method adds all children rules of relevant parent rule into the 'inlcusiveList' if they are not in the list.
    */
    public void addChildRuleIntoInclusiveList(Node node)
    {
	    	List<Integer> childrenListOfNode = nodeSet.getDependencyMatrix().getOutDependencyList(node.getNodeId());
	    	childrenListOfNode.stream().forEachOrdered(item -> {
	    		String childNodeName = nodeSet.getNodeMap().get(nodeSet.getNodeIdMap().get(item)).getNodeName();
	    		if(!ast.getInclusiveList().contains(childNodeName))
	    		{
	    			ast.getInclusiveList().add(childNodeName);
	    		}
	    	});
        
    }


    public boolean isAnyOrDependencyTrue(Node node, List<Integer> orOutDependencies)
    {
        boolean isAnyOrDependencyTrue = false;
        if (!orOutDependencies.isEmpty())
        {
	        	List<Integer> trueOrOutNodesList = orOutDependencies.stream().filter(i -> 
	        	ast.getWorkingMemory().get(nodeSet.getNodeIdMap().get(i)) != null && ast.getWorkingMemory().get(nodeSet.getNodeIdMap().get(i)).getValue().equals(true))
	        																 .collect(Collectors.toList());
	        	
	        	if(!trueOrOutNodesList.isEmpty())
	        	{
	        		isAnyOrDependencyTrue = true;
	        		orOutDependencies.stream().forEachOrdered(i -> {
	        			trueOrOutNodesList.stream().forEachOrdered(n -> {
	        				if(i != n)
	        				{
	        					trimDependency(node, nodeSet.getNodeMap().get(nodeSet.getNodeIdMap().get(i)));
	        				}
	        			});
	        		});
	        	}
        }
        return isAnyOrDependencyTrue;
    }

    public void trimDependency(Node parentNode, Node childNode)
    {
	    	int dpType = nodeSet.getDependencyMatrix().getDependencyMatrixArray()[parentNode.getNodeId()][childNode.getNodeId()];
	    	int mandatoryDependencyType = DependencyType.getMandatory();
	    	if((dpType & mandatoryDependencyType) != mandatoryDependencyType)
	    	{
	    		ast.getInclusiveList().remove(childNode.getNodeName());
	    	}
    }
    
    public boolean isAnyAndDependencyFalse(List<Integer> andOutDependencies)
    {
        boolean isAnyAndDependencyFalse = false;
        
        List<Integer> falseAndList = andOutDependencies.stream().filter(i -> ast.getWorkingMemory().get(nodeSet.getNodeMap().get(nodeSet.getNodeIdMap().get(i))).getValue().toString().equals("false")).collect(Collectors.toList());

        if(falseAndList.size() > 0)
        {
	        	isAnyAndDependencyFalse = true;
	        	andOutDependencies.stream().forEachOrdered(i -> {
	        		falseAndList.stream().forEachOrdered(f -> {
	        			if(i != f)
	        			{
	        				ast.getInclusiveList().remove(nodeSet.getNodeMap().get(nodeSet.getNodeIdMap().get(i)).getNodeName());
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
    
    
    public boolean isAllAndDependencyTrue(Node parentRule, List<Integer> andOutDependencies)
    {
        boolean isAllAndTrue = false;

        List<Integer> determinedTrueAndOutDependencies = andOutDependencies.stream().filter(i ->
        													ast.getWorkingMemory().get(nodeSet.getNodeMap().get(nodeSet.getNodeIdMap().get(i))).getValue().toString().equals("true")).collect(Collectors.toList());
        
        
        if(andOutDependencies != null && determinedTrueAndOutDependencies.size() == andOutDependencies.size())
        {
        		isAllAndTrue = true;
        } 

       return isAllAndTrue;
    }

    public boolean isAllAndDependencyDetermined(List<Integer> andOutDependencies)
    {
        boolean isAllAndDependencyDetermined = false;
        
        List<Integer> determinedAndOutDependencies = andOutDependencies.stream().filter(i ->
        							ast.getWorkingMemory().get(nodeSet.getNodeIdMap().get(i)) != null).collect(Collectors.toList());
        
        
        if(andOutDependencies != null && determinedAndOutDependencies.size() == andOutDependencies.size())
        {
        		isAllAndDependencyDetermined = true;
        }


        return isAllAndDependencyDetermined;
    }

    public boolean isAllOrDependencyDetermined(List<Integer> orOutDependencies)
    {
        boolean isAllOrDependencyDetermined = false;
        
        List<Integer> determinedOrOutDependencies = orOutDependencies.stream().filter(i ->
        					ast.getWorkingMemory().get(nodeSet.getNodeMap().get(nodeSet.getNodeIdMap().get(i))) != null
        				).collect(Collectors.toList());
        
        if(orOutDependencies != null && determinedOrOutDependencies.size() == orOutDependencies.size())
        {
        		isAllOrDependencyDetermined = true;
        }

        return isAllOrDependencyDetermined;
    }


    public boolean allNeedsChildDetermined(Node parentNode, List<Integer> outDependency)
    {
    	boolean allNeedsChildDetermined = false;
    	
    	int mandatoryAndDependencyType = DependencyType.getMandatory() | DependencyType.getAnd();
    	List<Integer> determinedList = outDependency.stream().filter(i -> (nodeSet.getDependencyMatrix().getDependencyMatrixArray()[parentNode.getNodeId()][i] & mandatoryAndDependencyType) == mandatoryAndDependencyType
    										&& ast.getWorkingMemory().get(nodeSet.getNodeMap().get(nodeSet.getNodeIdMap().get(i)).getVariableName())!= null).collect(Collectors.toList());
    	
    	if(outDependency.size() == determinedList.size())
    	{
    		allNeedsChildDetermined = true;
    	}    	
    	
    	return allNeedsChildDetermined;
    }
    

    
 
   
    

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
    		if(nodeSet.getDependencyMatrix().getOutDependencyList(node.getNodeId()).isEmpty())
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

