package nodePackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

import factValuePackage.FactValue;


public class NodeSet {
	private String nodeSetName;
	private HashMap<String, Node> nodeMap ;
	private HashMap<Integer, String> nodeIdMap;
	private List<Node> sortedNodeList;
	private HashMap<String, FactValue> inputMap;
	private HashMap<String, FactValue> factMap;
	private Node defaultGoalNode;
	private DependencyMatrix dependencyMatrix;
	
	public NodeSet()
	{
		this.nodeSetName = "";
		this.inputMap = new HashMap<>();
		this.factMap = new HashMap<>();
		this.nodeMap = new HashMap<>();
		this.nodeIdMap = new HashMap<>();
		this.sortedNodeList = new ArrayList<>();

	}
	
	public DependencyMatrix getDependencyMatrix()
	{
		return this.dependencyMatrix;
	}
	public void setDependencyMatrix(int[][] dependencyMatrix)
	{
		this.dependencyMatrix = new DependencyMatrix(dependencyMatrix);
	}
	
	public String getNodeSetName()
	{
		return this.nodeSetName;
	}
	public void setNodeSetName(String nodeSetName)
	{
		this.nodeSetName = nodeSetName;
	}
	
	public void setNodeIdMap(HashMap<Integer, String> nodeIdMap)
	{
		this.nodeIdMap = nodeIdMap;
	}
	public HashMap<Integer, String> getNodeIdMap()
	{
		return this.nodeIdMap;
	}
	public void setNodeMap(HashMap<String, Node> nodeMap)
	{
		this.nodeMap = nodeMap;
	}
	public HashMap<String, Node> getNodeMap()
	{
		return this.nodeMap;
	}
	
	public void setNodeSortedList(List<Node> sortedNodeList)
	{
		this.sortedNodeList = sortedNodeList;
	}
	public List<Node> getNodeSortedList()
	{
		return this.sortedNodeList;
	}
	
	public HashMap<String, FactValue> getInputMap()
	{
		return this.inputMap;
	}
	
	public HashMap<String, FactValue> getFactMap()
	{
		return this.factMap;
	}
	
	public Node getNode(int nodeIndex)
	{
		return sortedNodeList.get(nodeIndex);
	}
	
	public Node getNode(String nodeName)
	{
		return nodeMap.get(nodeName);
	}
	
	public Node getNodeByNodeId(int nodeId)
	{
		return getNode(getNodeIdMap().get(nodeId));
	}
	
	 public int findNodeIndex(String nodeName)
    {
        int nodeIndex = IntStream.range(0, getNodeSortedList().size()).filter(i -> getNodeSortedList().get(i).getNodeName().equals(nodeName)).toArray()[0];
        
        return nodeIndex;
    }
	
	public void setDefaultGoalNode(String name)
	{
		this.defaultGoalNode = this.nodeMap.get(name);
	}
	public Node getDefaultGoalNode()
	{
		return this.defaultGoalNode;
	}
	public HashMap<String, FactValue> transferFactMapToWorkingMemory(HashMap<String, FactValue> workingMemory)
	{
		inputMap.forEach((k,v)->workingMemory.put(k, v));
		
		return workingMemory;
	}
	
	

}
