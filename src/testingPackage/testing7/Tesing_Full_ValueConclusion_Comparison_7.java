package testingPackage.testing7;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

import factValuePackage.FactValue;
import factValuePackage.FactValueType;
import inferencePackage.Assessment;
import inferencePackage.InferenceEngine;
import nodePackage.*;
import ruleParser.RuleSetParser;
import ruleParser.RuleSetReader;
import ruleParser.RuleSetScanner;
import testingUtilPackage.NodeObject_For_Inference_Test;

public class Tesing_Full_ValueConclusion_Comparison_7 {

	
	public static void main(String[] args) throws IOException {
		RuleSetReader ilr = new RuleSetReader();
		ilr.setStreamSource(Tesing_Full_ValueConclusion_Comparison_7.class.getResourceAsStream("Testing full ValueConclusion and Comparison.txt"));
		RuleSetParser isf = new RuleSetParser();		
		RuleSetScanner rsc = new RuleSetScanner(ilr,isf);
		rsc.scanRuleSet();
		rsc.establishNodeSet(null);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(Tesing_Full_ValueConclusion_Comparison_7.class.getResourceAsStream("Comparison for Testing full ValueConclusion and Comparison.txt")));
		String line;
		List<String> nodeListMock = new ArrayList<>();
		String[] tempArray;
		HashMap<String, NodeObject_For_Inference_Test> nameMap = new HashMap<>();

		while((line = br.readLine()) != null)
		{

			tempArray = line.split("-");
			nodeListMock.add(tempArray[0].trim());
			System.out.println("line: "+line);
			
			NodeObject_For_Inference_Test nfit = new NodeObject_For_Inference_Test(tempArray[0].trim(),tempArray[1].trim().split(":"));
			nameMap.put(tempArray[0].trim(), nfit);
		}
		br.close();

		List<Integer> comparisonTempList = new ArrayList<>();
		IntStream.range(0, nodeListMock.size()).forEach(i->{
			String mockNode = nodeListMock.get(i);
			Node actualNode = isf.getNodeSet().getNodeSortedList().get(i);
			if(actualNode.getNodeName().equals("person's nationality IS “Australian”")
				&&mockNode.equals(actualNode.getNodeName())
				&&actualNode.getVariableName().equals("person's nationality")
				&&actualNode.getFactValue().getType().equals(FactValueType.DEFI_STRING)
				&&actualNode.getFactValue().getValue().toString().equals("Australian"))
			{
				comparisonTempList.add(i);
				
			}
			else if(actualNode.getNodeName().equals("person's name IS IN LIST: name list")
					&&mockNode.equals(actualNode.getNodeName())
					&&actualNode.getVariableName().equals("person's name")
					&&actualNode.getFactValue().getType().equals(FactValueType.STRING)
					&&actualNode.getFactValue().getValue().toString().equals("name list"))
					
			{
				comparisonTempList.add(i);
				
			}
			else if(actualNode.getNodeName().equals("person passport type = “Australian”")
					&&mockNode.equals(actualNode.getNodeName())
					&&((ComparisonLine)actualNode).getLHS().equals("person passport type")
					&&((ComparisonLine)actualNode).getRHS().getType().equals(FactValueType.DEFI_STRING)
					&&((ComparisonLine)actualNode).getRHS().getValue().toString().equals("Australian"))
			{
				comparisonTempList.add(i);
			}
			else if(actualNode.getNodeName().equals("person passport issued country = “Australian”")
					&&mockNode.equals(actualNode.getNodeName())
					&&((ComparisonLine)actualNode).getLHS().equals("person passport issued country")
					&&((ComparisonLine)actualNode).getRHS().getType().equals(FactValueType.DEFI_STRING)
					&&((ComparisonLine)actualNode).getRHS().getValue().toString().equals("Australian"))
			{
				comparisonTempList.add(i);
			}
			else if(actualNode.getNodeName().equals("person age >18")
					&&mockNode.equals(actualNode.getNodeName())
					&&((ComparisonLine)actualNode).getLHS().equals("person age")
					&&((ComparisonLine)actualNode).getRHS().getType().equals(FactValueType.INTEGER)
					&&((ComparisonLine)actualNode).getRHS().getValue().toString().equals("18"))
			{
				comparisonTempList.add(i);
			}
			else if(actualNode.getNodeName().equals("a number of countries the person has travelled so far >= 40")
					&&mockNode.equals(actualNode.getNodeName())
					&&((ComparisonLine)actualNode).getLHS().equals("a number of countries the person has travelled so far")
					&&((ComparisonLine)actualNode).getRHS().getType().equals(FactValueType.INTEGER)
					&&((ComparisonLine)actualNode).getRHS().getValue().toString().equals("40"))
			{
				comparisonTempList.add(i);
			}
			else if(actualNode.getNodeName().equals("current location of person's passport = the place the person normally locate the passport")
					&&mockNode.equals(actualNode.getNodeName())
					&&((ComparisonLine)actualNode).getLHS().equals("current location of person's passport")
					&&((ComparisonLine)actualNode).getRHS().getType().equals(FactValueType.STRING)
					&&((ComparisonLine)actualNode).getRHS().getValue().toString().equals("the place the person normally locate the passport"))
			{
				comparisonTempList.add(i);
			}
			else if(mockNode.equals(actualNode.getNodeName()))
			{
				comparisonTempList.add(i);
			}
		});
		if(comparisonTempList.size() ==  nodeListMock.size())
		{
			System.out.println("Node side has been tested and passed");
		}
		
		InferenceEngine ie = new InferenceEngine(isf.getNodeSet());
		Assessment ass = new Assessment(isf.getNodeSet(), isf.getNodeSet().getNodeSortedList().get(0).getNodeName());
		int i = 0;
		while(ie.getAssessmentState().getWorkingMemory().get(isf.getNodeSet().getNodeSortedList().get(0).getNodeName())==null)
		{
			
			Node nextQuestionNode = ie.getNextQuestion(ass);
			HashMap<String,FactValueType> questionFvtMap = ie.findTypeOfElementToBeAsked(nextQuestionNode);
			
			FactValueType fvt = null;
			String answer;
			
			for(String question: ie.getQuestionsFromNodeToBeAsked(nextQuestionNode))
			{
				System.out.println("questionFvt :"+questionFvtMap.get(question));
				System.out.println("Question: " + question+"?");
				if(question.equals("person's name"))
				{
					answer = "John Smith";
				}
				else if(question.equals("person's dob"))
				{
					answer = "11/12/1934";
				}
				else if(question.equals("the person missed the flight"))
				{
					answer = "false";
				}
				else if(i == 0)
				{
					answer = "true";
				}
				else if(question.equals("person passport type"))
				{
					answer = "Australian";
				}
				else if(question.equalsIgnoreCase("person passport issued country"))
				{
					answer = "Australia";
				}
				else if(question.equalsIgnoreCase("person age"))
				{
					answer = "19";
				}
				else if(question.equalsIgnoreCase("a number of countries the person has travelled so far"))
				{
					answer = "40";
				}
				else if(question.equalsIgnoreCase("current location of person's passport"))
				{
					answer = "there";
				}
				else if(question.equalsIgnoreCase("the place the person normally locate the passport"))
				{
					answer = "here";
				}
				else if(question.equalsIgnoreCase("person's passport is in a police station"))
				{
					answer = "false";
				}
//				else if(question.equals("person's dob"))
//				{
//					answer = "false";
//				}
//				else if(question.equals("the person was born in Australia"))
//				{
//					answer = "false";
//				}
				else if(i < 3)
				{
					answer = "true";
				}
				else
				{
					answer = nameMap.get(question).getValue();
				}
				System.out.println("Answer: "+answer);
				
				ie.feedAnswerToNode(nextQuestionNode, question, answer, questionFvtMap.get(question));
				i++;
			}

			
		}

		HashMap<String, FactValue> workingMemory = ie.getAssessmentState().getWorkingMemory();
		ie.getAssessmentState().getSummaryList().stream().forEachOrdered(node ->{
			System.out.println(node+" : "+workingMemory.get(node).getValue().toString());
		});	
	}
}
