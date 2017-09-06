package inferencePackage;

import nodePackage.*;


/*
 * the reason of having Assessment class is to allows a user to do multiple assessment within one or multiple conditions.
 */
public class Assessment {
	private Node goalNode;
	private int goalNodeIndex; // the goal rule index in ruleList of ruleSet
	/*
	 * each instance of this object has a variable of ruleToBeAsked due to the following reasons;
	 * 1. a user will be allowed to do assessment on multiple investigation points at the same time;
	 * 2. a user will be allowed to do an assessment within another assessment. 
	 */
	private Node nodeToBeAsked;
	
	public Assessment(NodeSet ns, String goalNodeName)
	{
		goalNode = ns.getNodeMap().get(goalNodeName);
		goalNodeIndex = ns.findNodeIndex(goalNodeName);
		nodeToBeAsked = null;
	}
	

	public Node getGoalNode()
	{
		return this.goalNode;
	}
	public int getGoalNodeIndex()
	{
		return this.goalNodeIndex;
	}
	
	public void setNodeToBeAsked(Node nodeToBeAsked)
	{
		this.nodeToBeAsked = nodeToBeAsked;
	}
	public Node getNodeToBeAsked()
	{
		return this.nodeToBeAsked;
	}
	 
	

}
