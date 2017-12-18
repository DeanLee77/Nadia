package nodePackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.script.ScriptEngine;

import factValuePackage.FactValue;
import inferencePackage.TopoSort;
import ruleParser.RuleSetParser;
import ruleParser.RuleSetReader;
import ruleParser.Tokens;

public class IterateLine extends Node {

	private String numberOfTarget;
	private NodeSet iterateNodeSet;

	

	public IterateLine(String childText, Tokens tokens)
	{
		super(childText, tokens);
		numberOfTarget = "";
		iterateNodeSet = new NodeSet();		
		
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
		// TODO Auto-generated method stub
		
	}



	@Override
	public LineType getLineType() {
		return LineType.ITERATE;
	}



	@Override
	public FactValue selfEvaluate(HashMap<String, FactValue> workingMemory, ScriptEngine nashorn) {
		// TODO Auto-generated method stub
		return null;
	}
}
