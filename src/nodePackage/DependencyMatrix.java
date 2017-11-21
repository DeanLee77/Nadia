package nodePackage;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class DependencyMatrix {

	
	/*
	 *  order of dependency type
	 *  1. MANDATORY
	 *  2. OPTIONAL
	 *  3. POSSIBLE
	 *  4. AND
	 *  5. OR
	 *  6. NOT
	 *  7. KNOWN
	 *  
	 *  int value will be '1' if any one of them is true case otherwise '0'
	 *  for instance, if a rule is in 'MANDATORY AND NOT' dependency then 
	 *  dependency type value is '1001010'
	 *  
	 *  if there is no dependency then value is 0000000
	 */
	private int[][] dependencyMatrix;
	private int dependencyMatrixSize;
	
	public DependencyMatrix(int[][]dependencyMatrix)
	{
		this.dependencyMatrix = dependencyMatrix;
		this.dependencyMatrixSize = this.dependencyMatrix[0].length;
	}
	
	public int[][] getDependencyMatrixArray()
	{
		return this.dependencyMatrix;
	}
	public int getDependencyType(int parentRuleId, int childRuleId)
	{
		return this.dependencyMatrix[parentRuleId][childRuleId];
	}
	public List<Integer> getToChildDependencyList(int nodeId)
	{		
		return IntStream.range(0, this.dependencyMatrixSize).filter(i -> i != nodeId && this.dependencyMatrix[nodeId][i] != 0 ).boxed().collect(Collectors.toList());
	}
	
	public List<Integer> getOrToChildDependencyList(int nodeId)
	{
		int orDependency = DependencyType.getOr();
		return IntStream.range(0, this.dependencyMatrixSize).filter(i -> i != nodeId && (this.dependencyMatrix[nodeId][i] & orDependency) == orDependency)
															.boxed().collect(Collectors.toList());
	}
	public List<Integer> getAndToChildDependencyList(int nodeId)
	{
		int andDependency = DependencyType.getAnd();

		return IntStream.range(0, this.dependencyMatrixSize).filter(i -> i != nodeId && (this.dependencyMatrix[nodeId][i] & andDependency) == andDependency)
															.boxed().collect(Collectors.toList());
	}
	public List<Integer> getMandatoryToChildDependencyList(int nodeId)
	{
		int mandatoryDependency = DependencyType.getMandatory();
		return IntStream.range(0, this.dependencyMatrixSize).filter(i -> i != nodeId && (this.dependencyMatrix[nodeId][i] & mandatoryDependency) == mandatoryDependency)
															.boxed().collect(Collectors.toList());
	}
	
	
	public List<Integer> getFromParentDependencyList(int nodeId)
	{
		return IntStream.range(0, this.dependencyMatrixSize).filter(i -> i != nodeId && this.dependencyMatrix[i][nodeId] != 0)
															.boxed().collect(Collectors.toList());
	}
	
	public boolean hasMandatoryChildNode(int nodeId)
	{
		return getMandatoryToChildDependencyList(nodeId).size() > 0;
	}
}
