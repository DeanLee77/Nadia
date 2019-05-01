package testingPackage.inferenceEngineTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import factValuePackage.FactValue;
import factValuePackage.FactValueType;
import inferencePackage.Assessment;
import inferencePackage.InferenceEngine;
import nodePackage.Node;
import ruleParser.RuleSetParser;
import ruleParser.RuleSetReader;
import ruleParser.RuleSetScanner;
import testingPackage.testing9.Testing_Whole_Features_Of_ValueConclusionLine_ComparisonLine_and_ExprConclusionLine_9;
import testingUtilPackage.NodeObject_For_Inference_Test;

public class InferenceEngineTest {

	public static void main(String[] args) throws IOException {
		RuleSetReader ilr = new RuleSetReader();
		ilr.setStreamSource(Testing_Whole_Features_Of_ValueConclusionLine_ComparisonLine_and_ExprConclusionLine_9.class.getResourceAsStream("Testing for whole features of ValueConclusionLine, ComparisonLine and ExprConclusionLine.txt"));
		RuleSetParser isf = new RuleSetParser();		
		RuleSetScanner rsc = new RuleSetScanner(ilr,isf);
		rsc.scanRuleSet();
		rsc.establishNodeSet(null);
		
		InferenceEngine ie = new InferenceEngine(isf.getNodeSet());
		Assessment ass = new Assessment(isf.getNodeSet(), isf.getNodeSet().getNodeSortedList().get(0).getNodeName());
		
//		ie.getListOfVariableNameAndValueOfNodes().stream().forEach(str -> System.out.println(str));
		
		BufferedReader br = new BufferedReader(new InputStreamReader(InferenceEngineTest.class.getResourceAsStream("leaf node set.txt")));
		
		String line;
		Fact newFact = null;
		while((line = br.readLine()) != null)
		{
			
			if(!line.isEmpty())
			{
				
				String[] lineArray = line.split(":");
				if(lineArray[0].trim().equals("questionFvt"))
				{
					newFact = new Fact();
					newFact.setFactValueType(lineArray[1].trim());
				}
				else if(lineArray[0].trim().equals("Question"))
				{
					newFact.setVariableName(lineArray[1].trim());
				}
				else
				{
					newFact.setFactValue(generateFactValue(newFact.getFactValueType(), lineArray[1].trim()));
				}
				
			}
			if(newFact.getVariableName() != null && newFact.getFactValue() != null)
			{
				ie.addNodeFact(newFact.getVariableName(), newFact.getFactValue());
			}
			
		}
		br.close();
		
		List<Node> sortedNodeList = ie.getNodeSet().getNodeSortedList();
		HashMap<String, FactValue> workingMemory = ie.getAssessmentState().getWorkingMemory();
		
		ie.getAssessmentState().getInclusiveList().add(sortedNodeList.get(0).getNodeName());
		sortedNodeList.stream().forEach(node->{
			ie.addChildRuleIntoInclusiveList(node);
		});
		
		ie.backPropagating(ie.getNodeSet().getNodeSortedList().size()-1);
		Node nextQuestion = ie.getNextQuestion(ass);
		if(nextQuestion == null)
		{
			System.out.println("the rule set has been concluded");
			System.out.println("Goal Rule: ");
			System.out.println(sortedNodeList.get(0).getNodeName());
			System.out.println("Conclusion: ");
			System.out.println(workingMemory.get(sortedNodeList.get(0).getNodeName()).getValue().toString());
			
			sortedNodeList.stream().forEach(rule ->{
				
				for(String question: ie.getQuestionsFromNodeToBeAsked(rule))
				{
					if(workingMemory.containsKey(question)) {
						System.out.println("Question: " + question);
						System.out.println("Result  : "+workingMemory.get(question).getValue().toString());
					}
				}
			});;

		}
		else
		{
			HashMap<String,FactValueType> questionFvtMap = ie.findTypeOfElementToBeAsked(nextQuestion);

			for(String question: ie.getQuestionsFromNodeToBeAsked(nextQuestion))
			{
				System.out.println("questionFvt :"+questionFvtMap.get(question));
				System.out.println("Question: " + question+"?");
			}
		}
	}
	
	public static FactValue generateFactValue(String factValueType, String factValue)
	{
		if(factValueType.equals("STRING"))
		{
			return FactValue.parse(factValue);
		}
		else if(factValueType.equals("BOOLEAN"))
		{
			return FactValue.parse(Boolean.parseBoolean(factValue));
		}
		else if(factValueType.equals("INTEGER"))
		{
			return FactValue.parse(Integer.parseInt(factValue));
		}
		else if(factValueType.equals("DOUBLE"))
		{
			return FactValue.parse(Double.parseDouble(factValue));
		}
		else if(factValueType.equals("DATE"))
		{
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
     		LocalDate factValueInDate = LocalDate.parse(factValue, formatter);
     		
			return FactValue.parse(factValueInDate);
		}
		
		return null;
	}
}
