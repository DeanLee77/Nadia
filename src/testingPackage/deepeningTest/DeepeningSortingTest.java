package testingPackage.deepeningTest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

import factValuePackage.FactValueType;
import inferencePackage.Assessment;
import inferencePackage.InferenceEngine;
import inferencePackage.TopoSort;
import nodePackage.Node;
import ruleParser.RuleSetParser;
import ruleParser.RuleSetReader;
import ruleParser.RuleSetScanner;
import testingPackage.inferenceEngineTest.Fact;
import testingPackage.inferenceEngineTest.InferenceEngineTest;
import testingPackage.testing9.Testing_Whole_Features_Of_ValueConclusionLine_ComparisonLine_and_ExprConclusionLine_9;

public class DeepeningSortingTest {
	
	public static void main(String[] args)
	{
		RuleSetReader ilr = new RuleSetReader();
		ilr.setStreamSource(Testing_Whole_Features_Of_ValueConclusionLine_ComparisonLine_and_ExprConclusionLine_9.class.getResourceAsStream("Testing for whole features of ValueConclusionLine, ComparisonLine and ExprConclusionLine.txt"));
		RuleSetParser isf = new RuleSetParser();		
		RuleSetScanner rsc = new RuleSetScanner(ilr,isf);
		rsc.scanRuleSet();
		rsc.establishNodeSet(null);
		
		InferenceEngine ie = new InferenceEngine(isf.getNodeSet());
		Assessment ass = new Assessment(isf.getNodeSet(), isf.getNodeSet().getNodeSortedList().get(0).getNodeName());
		
		
		Node theMostParent = ie.getNodeSet().getNodeSortedList().get(0);
		List<Integer> firstChildListOfTheMostParent = ie.getNodeSet().getDependencyMatrix().getToChildDependencyList(theMostParent.getNodeId());
		List<List<Integer>> ListOfLeafChildList = new ArrayList<>();
		firstChildListOfTheMostParent.stream().forEach(child -> {
			ListOfLeafChildList.add(TopoSort.findAllLeafChild(child, ie.getNodeSet().getDependencyMatrix()));
		});
		
		IntStream.range(0, firstChildListOfTheMostParent.size()).forEach(i ->{
			Node firstChildNode = ie.getNodeSet().getNodeByNodeId(firstChildListOfTheMostParent.get(i));
			System.out.println("First Level Child-Node: "+ firstChildNode.getNodeName());
			
			List<Integer> leafChildList = ListOfLeafChildList.get(i);
			leafChildList.stream().forEach(leaf -> {
				Node leafChildNode = ie.getNodeSet().getNodeByNodeId(leaf);
				HashMap<String,FactValueType> questionFvtMap = ie.findTypeOfElementToBeAsked(leafChildNode);
				
				System.out.println("-------------------------------------------------------------");
				
				for(String question: ie.getQuestionsFromNodeToBeAsked(leafChildNode))
				{
					System.out.println("questionFvt :"+questionFvtMap.get(question));
					System.out.println("Question: " + question+"?");
				}
			});
			System.out.println("==================================================================================");
		});
		
		
	}
}
