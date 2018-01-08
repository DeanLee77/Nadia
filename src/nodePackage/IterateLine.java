package nodePackage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.script.ScriptEngine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import factValuePackage.FactBooleanValue;
import factValuePackage.FactListValue;
import factValuePackage.FactValue;
import factValuePackage.FactValueType;
import inferencePackage.Assessment;
import inferencePackage.AssessmentState;
import inferencePackage.InferenceEngine;
import inferencePackage.TopoSort;
import ruleParser.Tokens;

public class IterateLine extends Node {

	private String numberOfTarget;
	private NodeSet iterateNodeSet;
	private String givenListName;
	private int givenListSize;
	private InferenceEngine iterateIE;

	

	public IterateLine(String childText, Tokens tokens)
	{
		super(childText, tokens);
		numberOfTarget = "";
		givenListName = "";
		givenListSize = 0;
		
	}

	public String getGivenListName() {
		return this.givenListName;
	}
	public NodeSet createIterateNodeSet(NodeSet parentNodeSet)
	{

		DependencyMatrix parentDM = parentNodeSet.getDependencyMatrix();
		HashMap<String, Node> parentNodeMap = parentNodeSet.getNodeMap();
		HashMap<Integer, String> parentNodeIdMap = parentNodeSet.getNodeIdMap();
		
		
		HashMap<String, Node> thisNodeMap = new HashMap<>();
		HashMap<Integer, String> thisNodeIdMap = new HashMap<>();
		List<Dependency> tempDependencyList = new ArrayList<>();
		NodeSet newNodeSet = new NodeSet();

		thisNodeMap.put(this.nodeName, this);
		thisNodeIdMap.put(this.nodeId, this.nodeName);
		IntStream.range(1, this.givenListSize+1).forEachOrdered(nTh -> {
			parentDM.getToChildDependencyList(this.nodeId).parallelStream().forEach((item)->{
				if(this.getNodeId()+1 != item) // not first question id
				{
					Node tempChildNode = parentNodeMap.get(parentNodeIdMap.get(item));
					LineType lt = tempChildNode.getLineType();
					
					Node tempNode = null;
					String nextNThInString = ordinal(nTh);

					if(lt.equals(LineType.VALUE_CONCLUSION) || lt.equals(LineType.EXPR_CONCLUSION))
					{
	    					tempNode = new ValueConclusionLine(nextNThInString+" "+this.getVariableName()+" "+tempChildNode.getNodeName(), tempChildNode.getTokens());
					}
	    				else if(lt.equals(LineType.COMPARISON))
	    				{
	    					tempNode = new ComparisonLine(nextNThInString+" "+this.getVariableName()+" "+tempChildNode.getNodeName(), tempChildNode.getTokens());
	    					FactValue tempNodeFv = ((ComparisonLine)tempNode).getRHS(); 
	    					if(tempNodeFv.getType().equals(FactValueType.STRING))
	    					{
	    						FactValue tempFv = FactValue.parse(nextNThInString+" "+this.getVariableName()+" "+tempNodeFv);
	    						tempNode.setValue(tempFv);
	    					}
	    				}

    				
					thisNodeMap.put(tempNode.getNodeName(), tempNode);
					thisNodeIdMap.put(tempNode.getNodeId(), tempNode.getNodeName());
					tempDependencyList.add(new Dependency(this, tempNode, parentDM.getDependencyType(this.nodeId, item)));
					
					createIterateNodeSetAux(parentDM, parentNodeMap, parentNodeIdMap, thisNodeMap, thisNodeIdMap, tempDependencyList, item, nextNThInString);

				}
				else // first question id
				{
					Node firstIterateQuestionNode = parentNodeSet.getNodeByNodeId(parentNodeSet.getDependencyMatrix().getToChildDependencyList(this.getNodeId()).stream().min((id1, id2) -> Integer.compare(id1, id2)).get());
					thisNodeMap.put(firstIterateQuestionNode.getNodeName(), firstIterateQuestionNode);
					thisNodeIdMap.put(item, firstIterateQuestionNode.getNodeName());
					tempDependencyList.add(new Dependency(this, firstIterateQuestionNode, parentDM.getDependencyType(this.nodeId, item)));
				}
			});			
		});
		
		int numberOfRules = Node.getStaticNodeId();
		
		int[][] dependencyMatrix = new int[numberOfRules][numberOfRules];
	
		
		tempDependencyList.forEach(dp -> {
			int parentId = dp.getParentNode().getNodeId();
			int childId = dp.getChildNode().getNodeId();
			int dpType = dp.getDependencyType();
			dependencyMatrix[parentId][childId] = dpType;
		});
		
		newNodeSet.setNodeIdMap(thisNodeIdMap);
		newNodeSet.setNodeMap(thisNodeMap);
		newNodeSet.setDependencyMatrix(new DependencyMatrix(dependencyMatrix));
		newNodeSet.setFactMap(parentNodeSet.getFactMap());
		newNodeSet.setNodeSortedList(TopoSort.bfsTopoSort(thisNodeMap, thisNodeIdMap, parentDM.getDependencyMatrixArray()));
		
		return newNodeSet;
	
	}
	
	public void createIterateNodeSetAux(DependencyMatrix parentDM, HashMap<String, Node> parentNodeMap, HashMap<Integer, String> parentNodeIdMap, HashMap<String, Node> thisNodeMap, HashMap<Integer, String> thisNodeIdMap, List<Dependency> tempDependencyList, int parentId, String nextNThInString)
	{
		List<Integer> childDependencyList = parentDM.getToChildDependencyList(parentId);

		if(childDependencyList.size() > 0)
		{
			childDependencyList.parallelStream().forEach((item)->{
				Node tempChildNode = parentNodeMap.get(parentNodeIdMap.get(item));
				LineType lt = tempChildNode.getLineType();
				
				Node tempNode = null;

				if(lt.equals(LineType.VALUE_CONCLUSION) || lt.equals(LineType.EXPR_CONCLUSION))
				{
						tempNode = new ValueConclusionLine(nextNThInString+" "+this.getVariableName()+" "+tempChildNode.getNodeName(), tempChildNode.getTokens());
				}
				else if(lt.equals(LineType.COMPARISON))
				{
					tempNode = new ComparisonLine(nextNThInString+" "+this.getVariableName()+" "+tempChildNode.getNodeName(), tempChildNode.getTokens());
					FactValue tempNodeFv = ((ComparisonLine)tempNode).getRHS(); 
					if(tempNodeFv.getType().equals(FactValueType.STRING))
					{
						FactValue tempFv = FactValue.parse(nextNThInString+" "+this.getVariableName()+" "+tempNodeFv);
						tempNode.setValue(tempFv);
					}
				}
						
				thisNodeMap.put(tempNode.getNodeName(), tempNode);
				thisNodeIdMap.put(tempNode.getNodeId(), tempNode.getNodeName());
				tempDependencyList.add(new Dependency(parentNodeMap.get(parentNodeIdMap.get(parentId)), tempNode,parentDM.getDependencyType(this.nodeId, item)));
				
				createIterateNodeSetAux(parentDM, parentNodeMap, parentNodeIdMap, thisNodeMap, thisNodeIdMap, tempDependencyList, item, nextNThInString);
			});
		}		
	}	
		
	public NodeSet getIterateNodeSet()
	{
		return this.iterateNodeSet;
	}
	
	/*
	 * this method is used when a givenList exists as a string
	 */
	public void iterateFeedAnswers(String givenJsonString, NodeSet parentNodeSet, AssessmentState parentAst) // this method uses JSON object via jackson library
	{
/*
 * 		givenJsonString has to be in same format as Example otherwise the engine would NOT be able to enable 'IterateLine' node
 * 		--------------------------- "givenJsonString" Format ----------------------------
 *      
 *		String givenJsonString = "{
 *									\"iterateLineVariableName\":
 *									    [
 *										  {
 *											\"1st iterateLineVariableName\":
 *												{
 *												  \"1st iterateLineVariableName ruleNme1\":\"..value..\", 
 *												  \"1st iterateLineVariableName ruleNme2\":\"..value..\"
 *												}
 *										  },
 *										  {
 *											\"2nd iterateLineVariableName\":
 *												{
 *												  \"2nd iterateLineVariableName ruleName1\":\"..value..\",
 *												  \"2nd iterateLineVariableName ruleName2\":\"..value..\"}
 *										  }
 *										]
 *								 }";
 *
 *     -----------------------------  "givenJsonString" Example ----------------------------
 *     String givenJsonString = "{
 *									\"service\":
 *									    [
 *										  {
 *											\"1st service\":
 *												{
 *												  \"1st service period\":\"..value..\", 
 *												  \"1st service type\":\"..value..\"
 *												}
 *										  },
 *										  {
 *											\"2nd service\":
 *												{
 *												  \"2nd service period\":\"..value..\",
 *												  \"2nd service type\":\"..value..\"}
 *										  }
 *										]
 *								 }";
*/
		ObjectMapper mapper = new ObjectMapper();
	    try {
				JsonNode jsonObj = mapper.readTree(givenJsonString);
				Iterator<JsonNode> serviceListIterator = jsonObj.path(this.variableName).elements();
				List<JsonNode> serviceList = new ArrayList<>();
				serviceListIterator.forEachRemaining(serviceList::add);
				this.givenListSize = serviceList.size();
				
				if(this.iterateNodeSet == null)
				{
					this.iterateNodeSet = createIterateNodeSet(parentNodeSet);
					this.iterateIE = new InferenceEngine(this.iterateNodeSet);
					if(this.iterateIE.getAssessment() == null)
					{
						this.iterateIE.setAssessment(new Assessment(this.iterateNodeSet, this.getNodeName()));
					}
				} 
				
				while(!canBeSelfEvaluated(this.iterateIE.getAssessmentState().getWorkingMemory()))
				{
					Node nextQuestionNode = getIterateNextQuestion(parentNodeSet, parentAst);
					String answer ="";
					HashMap<String,FactValueType> questionFvtMap = this.iterateIE.findTypeOfElementToBeAsked(nextQuestionNode);
					for(String question: this.iterateIE.getQuestionsFromNodeToBeAsked(nextQuestionNode))
					{
						answer = jsonObj.get(this.variableName)
								        .get(nextQuestionNode.getVariableName().substring(0, nextQuestionNode.getVariableName().lastIndexOf(this.variableName)+this.variableName.length()))
								        .get(nextQuestionNode.getVariableName())
								        .asText().trim();
						
						this.iterateIE.feedAnswerToNode(nextQuestionNode, question, answer, questionFvtMap.get(question));
					}
				}
				
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * this method is used when a givenList does NOT exist
	 */
	public <T> void iterateFeedAnswers(Node targetNode, String questionName, T nodeValue, FactValueType nodeValueType, NodeSet parentNodeSet)
	{
		if(this.iterateNodeSet == null)
		{
			this.iterateNodeSet = createIterateNodeSet(parentNodeSet);
			this.iterateIE = new InferenceEngine(this.iterateNodeSet);
			if(this.iterateIE.getAssessment() == null)
			{
				this.iterateIE.setAssessment(new Assessment(this.iterateNodeSet, this.getNodeName()));
			}
		} 
		this.iterateIE.feedAnswerToNode(targetNode, questionName, nodeValue, nodeValueType);
	}
	
	
	public Node getIterateNextQuestion(NodeSet parentNodeSet, AssessmentState parentAst)
	{
		if(this.iterateNodeSet == null)
		{
			this.iterateNodeSet = createIterateNodeSet(parentNodeSet);
			this.iterateIE = new InferenceEngine(this.iterateNodeSet);
			if(this.iterateIE.getAssessment() == null)
			{
				this.iterateIE.setAssessment(new Assessment(this.iterateNodeSet, this.getNodeName()));
			}
		}
		
    		Node firstIterateQuestionNode = parentNodeSet.getNodeByNodeId(parentNodeSet.getDependencyMatrix().getToChildDependencyList(this.getNodeId()).stream().min((id1, id2) -> Integer.compare(id1, id2)).get());
    		Node questionNode = null;
    		
		if(!parentAst.getWorkingMemory().containsKey(this.value.getValue().toString())) // a list is not given yet so that the engine needs to find out more info.
		{

	    		if(!parentAst.getWorkingMemory().containsKey(firstIterateQuestionNode.getNodeName()))
	    		{
	    			questionNode = firstIterateQuestionNode;
	    		}
	    		else 
	    		{
	    			if(!canBeSelfEvaluated(parentAst.getWorkingMemory()))
	    			{
	    				questionNode = this.iterateIE.getNextQuestion(this.iterateIE.getAssessment());
	    				String nextNThInString = ordinal(findNTh(parentAst.getWorkingMemory()) + 1);
	    				Node tempNode = null;
	    				LineType lt = questionNode.getLineType();
	    				
	    				if(lt.equals(LineType.VALUE_CONCLUSION))
					{
	    					tempNode = new ValueConclusionLine(questionNode.getNodeName(), questionNode.getTokens());
					}
	    				else if(lt.equals(LineType.COMPARISON))
	    				{
	    					tempNode = new ComparisonLine(questionNode.getNodeName(), questionNode.getTokens());
	    					FactValue tempNodeFv = ((ComparisonLine)tempNode).getRHS(); 
	    					if(tempNodeFv.getType().equals(FactValueType.STRING))
	    					{
	    						FactValue tempFv = FactValue.parse(nextNThInString+" "+this.getVariableName()+" "+tempNodeFv);
	    						tempNode.setValue(tempFv);
	    					}
	    				}
	    				
	    				tempNode.setNodeVariable(nextNThInString+" "+this.getVariableName()+" "+questionNode.getVariableName());
	    				questionNode = tempNode;
	    			}
	    		}
		}
		
    		
    		return questionNode;
	}
	

	public int findNTh(HashMap<String, FactValue> workingMemory)
	{
		return IntStream.range(1, this.givenListSize+1)
						.filter(item-> workingMemory.get(ordinal(item)+" "+this.variableName) != null)
						.boxed().collect(Collectors.toList()).size();
	}
	
	public String ordinal(int i) 
	{
	    String[] sufixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
	    switch (i % 100) {
		    case 11:
		    case 12:
		    case 13:
		        return i + "th";
		    default:
		        return i + sufixes[i % 10];
	    }
	}
	
	@Override
	public void initialisation(String parentText, Tokens tokens) {
		this.numberOfTarget = tokens.tokensList.get(0);
		this.variableName = tokens.tokensList.get(1);
		int tokensStringListSize = tokens.tokensStringList.size();
		String lastToken = tokens.tokensList.get(tokensStringListSize-1); //this is a givenListName.
		String lastTokenString = tokens.tokensStringList.get(tokensStringListSize-1);
		this.setValue(lastTokenString, lastToken);
		this.givenListName = lastToken;
		
	}



	@Override
	public LineType getLineType() {
		return LineType.ITERATE;
	}

	public boolean canBeSelfEvaluated(HashMap<String, FactValue> workingMemory) {
		boolean canBeSelfEvaluated = false;
				
		if(this.givenListSize == IntStream.range(1, this.givenListSize+1).filter(item->workingMemory.get(ordinal(item)+" "+this.variableName) != null).boxed().collect(Collectors.toList()).size())
		{
			canBeSelfEvaluated = true;
		}
		
		
		return canBeSelfEvaluated;
	}

	@Override
	public FactValue selfEvaluate(HashMap<String, FactValue> workingMemory, ScriptEngine nashorn) {
		
		int numberOfTrueChildren = numberOfTrueChildren(workingMemory);
		int sizeOfGivenList = ((FactListValue<?>)workingMemory.get(this.givenListName)).getValue().size();
		FactBooleanValue<?> fbv;
		switch(this.numberOfTarget)
		{
			case "ALL":
				if(numberOfTrueChildren == sizeOfGivenList)
				{
					fbv = FactBooleanValue.parse(true);
				}
				else
				{
					fbv = FactBooleanValue.parse(false);
				}
				break;
			case "NONE":
				if(numberOfTrueChildren == 0)
				{
					fbv = FactBooleanValue.parse(true);
				}
				else
				{
					fbv = FactBooleanValue.parse(false);
				}
				break;
			case "SOME":
				if(numberOfTrueChildren > 0)
				{
					fbv = FactBooleanValue.parse(true);
				}
				else
				{
					fbv = FactBooleanValue.parse(false);
				}
				break;
			default:
				if(numberOfTrueChildren == Integer.parseInt(this.numberOfTarget))
				{
					fbv = FactBooleanValue.parse(true);
				}
				else
				{
					fbv = FactBooleanValue.parse(false);
				}
				break;
		}
		return null;
	}
	
	public int numberOfTrueChildren(HashMap<String, FactValue> workingMemory)
	{		
		int sizeOfGivenList = ((FactListValue<?>)workingMemory.get(this.givenListName)).getValue().size();
		
		return IntStream.range(1, sizeOfGivenList+1).filter(item->workingMemory.get(ordinal(item)+" "+this.variableName).getValue().toString().toLowerCase().equals("true")).boxed().collect(Collectors.toList()).size();
	}
	
	
}
