package inferencePackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import factValuePackage.FactValue;
import nodePackage.*;


/*
 * HashMap<Object, FactValue> workingMemory
 *    first 'String' Key represents a Node's variableName and/or nodeName, or it could be a NodeSet's name in further development on. 
 *    second 'FactValue' Value represents the Rule's value or Fact's value.
 *    
 * List<String> inclusiveList
 *    it stores all relevant rule as assessment goes by, and the parameter String represents rule.getName()
 *    
 * List<String> exclusiveList
 *    it stores all irrelevant rule as assessment goes by, and the parameter String represents rule.getName()
 */
public class AssessmentState {
	private HashMap<String, FactValue> workingMemory;
	private List<String> inclusiveList;
	private List<String> questionedList; //may not be needed
	private List<Node> summaryList;
	public AssessmentState()
	{
		this.workingMemory = new HashMap<>();
		this.inclusiveList = new ArrayList<>();  // this is to capture all relevant rules 
		this.questionedList = new ArrayList<>();  // this is to capture all irrelevant rules, and may not be needed
		this.summaryList = new ArrayList<>(); // this is to store all determined rules within assessment in order.
	}
	/*
	 * this method is to get workingMemory 
	 */
	public HashMap<String, FactValue> getWorkingMemory()
	{
		return this.workingMemory;
	}
	/*
	 * this method is to set workingMemory from RuleSet instance. this method has to be executed after AssessmentState object initialization.
	 * all facts instance will be transfered from ruleMap in RuleSet instance to workingMemory in AssessmentState instance 
	 */
	public void transferFactMapToWorkingMemory(NodeSet nodeSet)
	{
		if(!nodeSet.getFactMap().isEmpty())
		{
			this.workingMemory = nodeSet.transferFactMapToWorkingMemory(this.workingMemory);
		}
	}
	/*
	 * this is simply for setting workingMemory with a given workingMemory
	 */
	public void setWorkingMemory(HashMap<String, FactValue> workingMemory)
	{
		this.workingMemory = workingMemory;
	}
	/*
	 * it allows a user to look up the workingMemory
	 * @return FactValue
	 */
	public FactValue lookupWorkingMemory(String keyName)
	{
		return this.workingMemory.get(keyName); 
	}
	
	/*
	 * it is to get List<String> inclusiveList
	 */
	public List<String> getInclusiveList()
	{
		return this.inclusiveList;
	}
	public void setInclusiveList(List<String> inclusiveList)
	{
		this.inclusiveList = inclusiveList;
	}
	public boolean isInclusiveList(String name)
	{
		boolean isInTheList = false;
		if(this.inclusiveList.contains(name))
		{
			isInTheList = true;
		}
		return isInTheList;
	}
	
	public List<String> getQuestionedList()
	{
		return this.questionedList;
	}
	public void setQuestionedList(List<String> questionedList)
	{
		this.questionedList = questionedList;
	}
	public boolean lookupQuestionedList(String name)
	{
		boolean isInTheList = false;
		if(this.questionedList.contains(name))
		{
			isInTheList = true;
		}
		return isInTheList;
	}
	

	
	/*
	 * this method is to set a rule as a fact in the workingMemory 
	 * before this method is called, nodeName should be given and look up nodeMap in NodeSet to find variableName of the node
	 * then the variableName of the node should be passed to this method.
	 */

	public void setFact(String nodeVariableName, FactValue value)
	{
		workingMemory.put(nodeVariableName, value);
	}
	
	public FactValue getFact(String name)
	{
		return workingMemory.get(name);
	}
	
	public void removeFact(String name)
	{
		workingMemory.remove(name);
	}
	
	
	//Below lines are all SummaryList related
	
	public void addItemToSummaryList(Node node)
	{
		this.summaryList.add(node);
	}
	public List<Node> getSummaryList()
	{
		return this.summaryList;
	}
	public void setSummaryList(List<Node> summaryList)
	{
		this.summaryList = summaryList;
	}

}
