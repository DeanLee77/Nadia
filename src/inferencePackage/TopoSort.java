package inferencePackage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import nodePackage.DependencyType;
import nodePackage.Node;
import nodePackage.Record;


public class TopoSort {

	/*
     *this topological sort method uses "Kahn's algorithm which is based on BFS(Breadth First Search)
     *within this method, the original 'dependencyMatrix' will lose information of dependency 
     *due to the reason that the algorithm itself uses the dependency information and delete it while topological sorting
     *Hence, this method needs to create copy of dependencyMatrix.
     */
	public static List<Node> bfsTopoSort(HashMap<String, Node> nodeMap, HashMap<Integer, String> nodeIdMap, int[][] dependencyMatrix)
	{
		List<Node> sortedNodeList = new ArrayList<>();
		int sizeOfMatrix = dependencyMatrix[0].length;
		int[][] copyOfDependencyMatrix = createCopyOfDependencyMatrix(dependencyMatrix, sizeOfMatrix);	

		List<Node> tempList = new ArrayList<>();
		List<Node> SList = fillingSList(nodeMap, nodeIdMap, tempList, copyOfDependencyMatrix);
		
		
		while(!SList.isEmpty())
		{
			Node node = SList.remove(0);
			sortedNodeList.add(node);
			Integer nodeId = node.getNodeId();
			
			
			for(int i = 0; i < sizeOfMatrix; i++)
			{
				if(nodeId != i && copyOfDependencyMatrix[nodeId][i] != 0)
				{
					copyOfDependencyMatrix[nodeId][i] = 0; // this is to remove dependency from 'node' to child node with nodeId == 'i'
					
					int numberOfIncomingEdge = sizeOfMatrix-1; // from this line, it is process to check whether or not the child node with nodeId == 'i' has any other incoming dependencies from other nodes, and the reason for subtracting 1 from matrixSize is to exclude node itself count from the matrix size.
					for(int j = 0; j < sizeOfMatrix; j++)
					{
						if(j != i && copyOfDependencyMatrix[j][i] == 0)
						{
							numberOfIncomingEdge--;
						}
					}
					if(numberOfIncomingEdge == 0) // there is no incoming dependencies for the node with nodeId == 'i'
					{
						SList.add(nodeMap.get(nodeIdMap.get(i)));
					}
				}
				
			}
//			tempList.clear(); // tempList needs to be cleared because it is used for filling SList, and it is used to avoid creating List instance over and over again.
//			SList = fillingSList(nodeMap, nodeIdMap, tempList, copyOfDependencyMatrix);
		}
		
		boolean checkDAG = false;
		for(int i = 0; i< sizeOfMatrix; i++)
		{
			for(int j = 0; j < sizeOfMatrix; j++)
			{
				if(i != j && copyOfDependencyMatrix[i][j] != 0)
				{
					checkDAG = true;
					break;
				}
			}
		}
																		
		if(checkDAG)
		{
			sortedNodeList.clear();
		}
		
		/*
		 * if size of sortedNodeList is '0' then the graph is cyclic so that RuleSet needs rewriting due to it is in incorrect format
		 */
		return sortedNodeList;

		
	}
	
	public static List<Node> fillingSList(HashMap<String, Node> nodeMap, HashMap<Integer, String> nodeIdMap, List<Node> tempList, int[][] dependencyMatrix)
	{
		int sizeOfMatrix = dependencyMatrix[0].length;
		
		for(int childRow = 0 ; childRow < sizeOfMatrix; childRow++)
		{
			int count = 0;
			for(int parentCol = 0; parentCol < sizeOfMatrix; parentCol++)
			{
				
				if(dependencyMatrix[parentCol][childRow] == 0 && parentCol != childRow) // don't count when parentCol == childRow due to the reason that there shouldn't be value at the index. 
				{
					count++;
				}
				else
				{
					continue;
				}
			}
			if(count == sizeOfMatrix-1) //exclude its own dependency 
			{
				String tempNodeName = nodeIdMap.get(childRow);
				if(tempNodeName != null)
				{
					tempList.add(nodeMap.get(tempNodeName));
				}
			}
		}//initial 'S' List for Kahn's topological algorithm.
		
		return tempList;
	}
	public static int[][] createCopyOfDependencyMatrix( int[][] dependencyMatrix , int sizeOfMatrix)
	{

		int[][] copyOfDependencyMatrix = new int[sizeOfMatrix][sizeOfMatrix];
		for(int i = 0 ; i < sizeOfMatrix; i++)
		{
			for(int j = 0; j < sizeOfMatrix; j++)
			{
				copyOfDependencyMatrix[i][j] = dependencyMatrix[i][j];
			}
		}
		
		return copyOfDependencyMatrix;
	}
	
	/*
     * this topological sort method uses DFS(Depth First Search)
     * At this point of time (10th Feb 2018), this method is strictly for sorting child nodes of IterateLine 
     * The reason for using this method is only for child nodes of IterateLine is that if there is a child node of local variable type
     * then the sorted order will NOT be appropriate to produce a next question.
     * 
     * For instance, if IterateLine node has a rule as following.
     * ------------------------------------------------------------
     * ALL service ITERATE: LIST OF service history
	 *	AND number of services
	 *	AND iterate rules
	 *		OR one
	 *			AND enlistment date >= 01/07/1951
	 *			AND discharge date <= 6/12/1972
	 *			AND NOT service type IS IN LIST: Special service
	 *		OR two
	 *			AND enlistment date >= 22/05/1986
	 *			AND yearly period of service by 6/04/1994 >= 3
	 *				AND yearly period of service by 6/04/1994 IS CALC (enlistment date - 6/04/1994)
	 *					NEEDS enlistment date
	 *			AND NOT service type IS IN LIST: Special service
	 *			AND discharge date >= 07/04/1994
	 *			AND discharge date <= 30/06/2004
	 *------------------------------------------------------------
	 * and number of service is '2', then the sorted order will be as follows.
	 * 
	 * ------------------------------------------------------------
	 * 	ALL service ITERATE: LIST OF service history
	 *	number of services
	 *	1st service iterate rules
	 *	2nd service iterate rules
	 *	1st service one
	 *	1st service two
	 *	2nd service one
	 *	2nd service two
	 *	1st service enlistment date >= 01/07/1951
	 *	1st service discharge date <= 6/12/1972
	 *	1st service enlistment date >= 22/05/1986
	 *	1st service yearly period of service by 6/04/1994 >= 3
	 *	1st service service type IS IN LIST: Special service
	 *	1st service enlistment date >= 07/04/1994
	 *	1st service discharge date >= 30/06/2004
	 *	2nd service enlistment date >= 01/07/1951
	 *	2nd service discharge date <= 6/12/1972
	 *	2nd service enlistment date >= 22/05/1986
	 *	2nd service yearly period of service by 6/04/1994 >= 3
	 *	2nd service service type IS IN LIST: Special service
	 *	2nd service enlistment date >= 07/04/1994
	 *	2nd service discharge date >= 30/06/2004
	 *	1st service yearly period of service by 6/04/1994 IS CALC (enlistment date - 6/04/1994)
	 *	2nd service yearly period of service by 6/04/1994 IS CALC (enlistment date - 6/04/1994)
	 *	1st service enlistment date
	 *	2nd service enlistment date
	 *------------------------------------------------------------
	 * And therefore, there would cause 1st service question and 2nd service question mixed
	 * 
	 *    !!!!!!!!!!!!!!   I M P O R T A N T  !!!!!!!!!!!!!!!!!!!!
	 *    
	 *    This method does NOT have a mechanism to check if it is DAG or not yet.
	 *    
     */
	public static List<Node> dfsTopoSort(HashMap<String, Node> nodeMap, HashMap<Integer, String> nodeIdMap, int[][] dependencyMatrix)
	{
		List<Node> sortedList = new ArrayList<>();
		
		int[][] copyOfDependencyMatrix = createCopyOfDependencyMatrix(dependencyMatrix, dependencyMatrix[0].length);
		List<Node> S = fillingSList(nodeMap, nodeIdMap, new ArrayList<Node>(), copyOfDependencyMatrix);
		List<Integer> visitedList = new ArrayList<>();
		while(!S.isEmpty())
        {
	        	Node node = S.remove(0);
	        
	        	sortedList.add(node);
	        	visitedList.add(node.getNodeId());
			Integer nodeId = node.getNodeId();
			
			List<Integer> childIdList = new ArrayList<>();
			IntStream.range(0, copyOfDependencyMatrix.length).forEach(i->{
				if(copyOfDependencyMatrix[nodeId][i] != 0)
				{
					childIdList.add(i);
				}
			}); 
            childIdList.stream().forEachOrdered(id->{
            		Node currentNode = nodeMap.get(nodeIdMap.get(id));
            		if(!visitedList.contains(id))
        			{
            			sortedList.add(currentNode);
            			visitedList.add(id);
        			}
//            		else
//            		{
//            			sortedList.remove(currentNode);
//            			sortedList.add(currentNode); // move the current node to at the last due to it is linked to the latest defined node.
//            		}
        			deepening(nodeMap, nodeIdMap, copyOfDependencyMatrix, sortedList, visitedList, id);

            }); 
        }
		
		return sortedList;
	}
	
	public static void deepening(HashMap<String, Node>nodeMap, HashMap<Integer, String> nodeIdMap, int[][] dependencyMatrix, List<Node> sortedList, List<Integer> visitedList, Integer childId)
	{
		List<Integer> childIdList = new ArrayList<>();
		IntStream.range(0, dependencyMatrix.length).forEach(i->{
			if(dependencyMatrix[childId][i] != 0)
			{
				childIdList.add(i);
			}
		});         
		
		childIdList.stream().forEachOrdered(id->{
			Node currentNode = nodeMap.get(nodeIdMap.get(id));
	    		if(!visitedList.contains(id))
			{
	    			sortedList.add(currentNode);
	    			visitedList.add(id);
			}
//	    		else
//	    		{
//	    			sortedList.remove(currentNode);
//	    			sortedList.add(currentNode); // move the current node to at the last due to it is linked to the latest defined node.
//	    		}
			deepening(nodeMap, nodeIdMap, dependencyMatrix, sortedList, visitedList, id);
        }); 
	}
	
	/*
	 * this class is another version of topological sort.
	 * the first version of topological sort used Kahn's algorithm which is based on Breadth First Search(BFS)
	 * Topological sorted list is a fundamental part to get an order list of all questions.
	 * However, it always provide same order at all times which might not be shortest path for a certain individual case therefore,
	 * this topological sort based on historical record of each node/rule is suggested.
	 * 
	 * logic for the sorting is as follows; 
	 * note: topological sort logic contains a recursive method 
	 * 1. set 'S' and 'sortedList'
	 * 2. get all data for each rules from database as a HashMap<String, Record>
	 * 3. find rules don't have any parent rules, and add them into 'S' list
	 * 4. if there is an element in the 'S' list
	 * 5. visit the element
	 *    5.1 if the element has any child rules
	 *        5.1.1 get a list of all child rules, and keep visiting until there are no non-visited rules
	 *        5.1.2 if there is not any 'OR' rules ( there are only 'AND' rules)
	 *              5.1.2.1 find the most negative rule, and add the rule into the 'sortedList'
	 *        5.1.3 if there is not any 'AND' rules ( there are only 'OR' rules)
	 *        		5.1.3.1 find the most positive rule, and add the rule into the 'sortedList'
	 * 
	 */

    public static List<Node> dfsTopoSort(HashMap<String, Node> nodeMap, HashMap<Integer, String> nodeIdMap, int[][] dependencyMatrix, HashMap<String, Record> recordMapOfNodes)
    {

    	 List<Node> sortedList = new ArrayList<>();

         if(recordMapOfNodes == null || recordMapOfNodes.isEmpty())
         {
         	sortedList = bfsTopoSort(nodeMap, nodeIdMap, dependencyMatrix);
         }
         else
         {
     		List<Node> visitedNodeList = new ArrayList<>();
     		int[][] copyOfDependencyMatrix = createCopyOfDependencyMatrix(dependencyMatrix, dependencyMatrix[0].length);
     		
     		List<Node> S = fillingSList(nodeMap, nodeIdMap, new ArrayList<Node>(), copyOfDependencyMatrix);
     		
     		while(!S.isEmpty())
             {
             	Node node = S.remove(0);
             	visitedNodeList.add(node);
                 visit(node, sortedList, recordMapOfNodes, nodeMap, nodeIdMap, visitedNodeList, dependencyMatrix); 
             }
         }
         
         return sortedList;    
    }

    /*
     * The idea of this method is to visit a rule that could get a result of parent rule of the rule as quick as it can be
     * for instance, if a 'OR' child rule is 'TRUE' then the parent rule is 'TRUE', 
     * and if a 'AND' child rule is 'FALSE' then the parent rule is 'FALSE'. 
     * AS result, visit more likely true 'OR' rule or more likely false 'AND' rule to determine a parent rule as fast as we can
     */
    public static List<Node> visit(Node node, List<Node> sortedList, HashMap<String, Record> recordMapOfNodes, HashMap<String,Node> nodeMap, HashMap<Integer, String> nodeIdMap, List<Node> visitedNodeList, int[][] dependencyMatrix)
    {
    		if(node != null)
    		{
    			sortedList.add(node);
    	        int nodeId = node.getNodeId();
    	        int orDependencyType = DependencyType.getOr();
    	        int andDependencyType = DependencyType.getAnd();
    	        List<Integer> dependencyMatrixAsList = Arrays.stream(dependencyMatrix[nodeId]).boxed().collect(Collectors.toList());
    	        List<Integer> orOutDependency = IntStream.range(0, dependencyMatrixAsList.size())
    	        											.filter(index->(dependencyMatrixAsList.get(index) & orDependencyType) == orDependencyType)
    	        											.boxed().collect(Collectors.toList());
    	        List<Integer> andOutDependency = IntStream.range(0, dependencyMatrixAsList.size())
    													 .filter(index->(dependencyMatrixAsList.get(index) & andDependencyType) == andDependencyType)
    													 .boxed().collect(Collectors.toList()); 

    	        if(!orOutDependency.isEmpty() || !andOutDependency.isEmpty())
    	        {
    	            List<Node> childRuleList = new ArrayList<>();
    	            IntStream.range(0, dependencyMatrixAsList.size())
    	            									   .filter(childIndex->dependencyMatrixAsList.get(childIndex) != 0)
    	            									   .boxed()
    	            									   .collect(Collectors.toList())
    	            									   .stream()
    	            									   .forEach(item->childRuleList.add(nodeMap.get(nodeIdMap.get(item))));
    	            
    	            
    	            if(!orOutDependency.isEmpty() && andOutDependency.isEmpty())
    	            {
    	                while(!childRuleList.isEmpty())
    	                {
    		                	/* 
    		                	 * the reason for selecting an option having more number of 'yes' is as follows
    		                	 * if it is 'OR' rule and it is 'TRUE' then it is the shortest path, and ignore other 'OR' rules
    		                	 * Therefore, looking for more likely 'TRUE' rule would be the shortest one rather than
    		                	 * looking for more likely 'FALSE' rule in terms of processing time
    		                	 */
    		                	Node theMostPositive = findTheMostPositive(childRuleList, recordMapOfNodes);
    	                    if(!visitedNodeList.contains(theMostPositive))
    	                    {
    	                    		visitedNodeList.add(theMostPositive);
    	                        sortedList = visit(theMostPositive, sortedList, recordMapOfNodes, nodeMap, nodeIdMap, visitedNodeList, dependencyMatrix);
    	                    }
    	                }

    	            }
    	            else
    	            {
    	                if(orOutDependency.isEmpty() && !andOutDependency.isEmpty())
    	                {
    		                	/* 
    		                	 * the reason for selecting an option having more number of 'yes' is as follows
    		                	 * if it is 'AND' rule and it is 'FALSE' then it is the shortest path, and ignore other 'AND' rules
    		                	 * Therefore, looking for more likely 'FALSE' rule would be the shortest one rather than
    		                	 * looking for more likely 'TRUE' rule in terms of processing time
    		                	 */	
    	                		while(!childRuleList.isEmpty())
    	                		{
    	                    		Node theMostNegative = findTheMostNegative(childRuleList, recordMapOfNodes);
    	                    		if(!visitedNodeList.contains(theMostNegative))
    	                    		{
    	                    			visitedNodeList.add(theMostNegative);
    	                    			sortedList = visit(theMostNegative, sortedList, recordMapOfNodes, nodeMap, nodeIdMap, visitedNodeList, dependencyMatrix);
    	                    		}
    	                		}
    	                }
    	            }
    	        }
    		}
        
        
        return sortedList;
    }
    
    public static Node findTheMostPositive(List<Node> childNodeList, HashMap<String, Record> recordListOfNodes)
    {
    		Node theMostPositive = null;
        int yesCount = 0;
        int noCount = 0;
        float theMostPossibility = 0;
        int sum = 0;
        float result = 0;
        
        for(Node node: childNodeList)
        {
        		Record recordOfNode = recordListOfNodes.get(node.getNodeName());
            yesCount = recordOfNode != null?recordListOfNodes.get(node.getNodeName()).getTrueCount(): 0;
            noCount = recordOfNode != null? recordListOfNodes.get(node.getNodeName()).getFalseCount(): 0;
            int yesPlusNoCount = (yesCount+noCount)==0?-1:(yesCount+noCount);

            result = (float)yesCount/yesPlusNoCount;
            if(analysis(result, theMostPossibility, yesPlusNoCount, sum))
            {
                theMostPossibility = result;
                sum =  yesCount + noCount;
                theMostPositive = node;
            }
        }
        childNodeList.remove(theMostPositive);
        return theMostPositive;
        
    }
    
    public static Node findTheMostNegative(List<Node> childNodeList, HashMap<String, Record> recordListOfNodes)
    {
    		Node theMostNegative = null;
        int yesCount = 0;
        int noCount = 0;
        float theMostPossibility = 0;
        int sum = 0;
        float result = 0;
       		
        for(Node node: childNodeList)
        {
        		
        		Record recordOfNode = recordListOfNodes.get(node.getNodeName());
            yesCount = recordOfNode!= null?recordListOfNodes.get(node.getNodeName()).getTrueCount(): 0;
            noCount = recordOfNode!= null?recordListOfNodes.get(node.getNodeName()).getFalseCount(): 0;
            
            int yesPlusNoCount = (yesCount+noCount)==0?-1:(yesCount+noCount);
            result = (float)noCount/yesPlusNoCount;

            if(analysis(result, theMostPossibility, yesPlusNoCount, sum))
            {
                theMostPossibility = result;
                sum =  yesCount + noCount;
                theMostNegative = node;                
            }
            
            
        }
        childNodeList.remove(theMostNegative);
        return theMostNegative;
    }
    
    public static boolean analysis(float result, float theMostPossibility, int yesCountNoCount, int sum)
    {
        boolean highlyPossible = false;
        /*
         * firstly select an option having more cases and high possibility
         */
        if(result > theMostPossibility && yesCountNoCount >= sum)
        {
            highlyPossible = true;
        }
        /*
	    	 * secondly, even though the number of being used case is fewer, and it has a high possibility
	    	 * then still select the option
	    	 */
        else if(result >= theMostPossibility && result == 0 && theMostPossibility == 0 && yesCountNoCount > sum)
        {
        	 	highlyPossible = true;
        }
        else if(result >= theMostPossibility && result == 0 && yesCountNoCount == -1 && sum == 0 )
        {
            highlyPossible = true;
        }
        
        return highlyPossible;
    }
    
       

}

