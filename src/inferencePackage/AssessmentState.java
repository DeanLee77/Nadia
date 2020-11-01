package inferencePackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import factValuePackage.FactListValue;
import factValuePackage.FactValue;
import factValuePackage.FactValueType;
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
	private List<String> exclusiveList;
	private List<String> summaryList;
	private List<String> mandatoryList;
	public AssessmentState()
	{
		this.workingMemory = new HashMap<>();
		this.inclusiveList = new ArrayList<>();  // this is to capture all relevant rules 
		this.summaryList = new ArrayList<>(); // this is to store all determined rules within assessment in order.
		this.exclusiveList = new ArrayList<>();// this is to capture all trimmed rules 
		this.mandatoryList = new ArrayList<>();
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
	
	public void addItemToSummaryList(String node)
	{
		if(!this.summaryList.contains(node))
		{
			this.summaryList.add(node);
		}
	}
	public List<String> getSummaryList()
	{
		return this.summaryList;
	}
	public void setSummaryList(List<String> summaryList)
	{
		this.summaryList = summaryList;
	}
	
	// exclusiveList
	public List<String> getExclusiveList()
	{
		return this.exclusiveList;
	}
	public void setExclusiveList(List<String> exclusiveList)
	{
		this.exclusiveList = exclusiveList;
	}
	public boolean isInExlusiveList(String name)
	{
		boolean isInTheList = false;
		if(this.exclusiveList.contains(name))
		{
			isInTheList = true;
		}
		return isInTheList;
	}
	
	//mandatoryList
	public List<String> getMandatoryList()
	{
		return this.mandatoryList;
	}
	public void setMandatoryList(List<String> mandatoryList)
	{
		this.mandatoryList = mandatoryList;
	}
	public void addItemToMandatoryList(String nodeName)
	{
		if(!this.mandatoryList.contains(nodeName))
		{
			this.mandatoryList.add(nodeName);
		}
	}
	public boolean isInMandatoryList(String nodeName)
	{
		return this.mandatoryList.contains(nodeName);
	}
	public boolean allMandatoryNodeDetermined()
	{
		return this.mandatoryList.parallelStream().allMatch(nodeName -> this.workingMemory.containsKey(nodeName));
	}
	/*
	 * this method is to set a rule as a fact in the workingMemory 
	 * before this method is called, nodeName should be given and look up nodeMap in NodeSet to find variableName of the node
	 * then the variableName of the node should be passed to this method.
	 */

	public void setFact(String nodeVariableName, FactValue value)
	{
		if(workingMemory.containsKey(nodeVariableName))
		{
			FactValue tempFv = workingMemory.get(nodeVariableName);

			if(tempFv.getType().equals(FactValueType.LIST))
			{
				((FactListValue<?>)tempFv).getValue().add(value);
			}
			else
			{
				FactListValue<?> flv = FactValue.parse(new ArrayList<FactValue>());
				flv.addFactValueToListValue(tempFv);
				flv.addFactValueToListValue(value);
				workingMemory.put(nodeVariableName, value);
			}
		}
		else
		{
			workingMemory.put(nodeVariableName, value);
		}
	}
	
	public FactValue getFact(String name)
	{
		return workingMemory.get(name);
	}
	
	public void removeFact(String name)
	{
		workingMemory.remove(name);
	}
	
}
