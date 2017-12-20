package nodePackage;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.script.ScriptEngine;

import factValuePackage.FactBooleanValue;
import factValuePackage.FactListValue;
import factValuePackage.FactValue;
import inferencePackage.TopoSort;
import ruleParser.Tokens;

public class IterateLine extends Node {

	private String numberOfTarget;
	private NodeSet iterateNodeSet;
	private String givenListName;

	

	public IterateLine(String childText, Tokens tokens)
	{
		super(childText, tokens);
		numberOfTarget = "";
		iterateNodeSet = new NodeSet();	
		givenListName = "";
		
	}

	public void createNodeSet(NodeSet parentNodeSet)
	{
		DependencyMatrix parentDM = parentNodeSet.getDependencyMatrix();
		HashMap<String, Node> parentNodeMap = parentNodeSet.getNodeMap();
		HashMap<Integer, String> parentNodeIdMap = parentNodeSet.getNodeIdMap();
		
		HashMap<String, Node> thisNodeMap = new HashMap<>();
		HashMap<Integer, String> thisNodeIdMap = new HashMap<>();
		parentDM.getToChildDependencyList(this.getNodeId()).parallelStream().forEach((item)->{
			final String tempChildName = parentNodeIdMap.get(item);
			thisNodeMap.put(tempChildName, parentNodeMap.get(tempChildName));
			thisNodeIdMap.put(item, tempChildName);
			createNodeSetAux(parentDM, parentNodeMap, parentNodeIdMap, thisNodeMap, thisNodeIdMap, item);
		});
		
		this.iterateNodeSet.setNodeIdMap(thisNodeIdMap);
		this.iterateNodeSet.setNodeMap(thisNodeMap);
		this.iterateNodeSet.setDependencyMatrix(parentDM);
		this.iterateNodeSet.setFactMap(parentNodeSet.getFactMap());
		this.iterateNodeSet.setNodeSortedList(TopoSort.bfsTopoSort(thisNodeMap, thisNodeIdMap, parentDM.getDependencyMatrixArray()));
	}

	public void createNodeSetAux(DependencyMatrix parentDM, HashMap<String, Node> parentNodeMap, HashMap<Integer, String> parentNodeIdMap, HashMap<String, Node> thisNodeMap, HashMap<Integer, String> thisNodeIdMap, int parentId)
	{
		List<Integer> childDependencyList = parentDM.getToChildDependencyList(parentId);
		if(childDependencyList.size()>0)
		{
			childDependencyList.parallelStream().forEach((item)->{
				final String tempChildName = parentNodeIdMap.get(item);
				thisNodeMap.put(tempChildName, parentNodeMap.get(tempChildName));
				thisNodeIdMap.put(item, tempChildName);
				createNodeSetAux(parentDM, parentNodeMap, parentNodeIdMap, thisNodeMap, parentNodeIdMap, item);
			});
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
		
		int sizeOfGivenList = ((FactListValue<?>)workingMemory.get(this.givenListName)).getValue().size();
		
		if(sizeOfGivenList == IntStream.range(1, sizeOfGivenList+1).filter(item->workingMemory.get(this.variableName+"["+item+"]") != null).boxed().collect(Collectors.toList()).size())
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
		
		return IntStream.range(1, sizeOfGivenList+1).filter(item->workingMemory.get(this.variableName+"["+item+"]").getValue().toString() == "true").boxed().collect(Collectors.toList()).size();
	}
}
