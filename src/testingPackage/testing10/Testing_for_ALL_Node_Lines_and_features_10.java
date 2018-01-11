package testingPackage.testing10;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.IntStream;

import factValuePackage.FactListValue;
import factValuePackage.FactValue;
import factValuePackage.FactValueType;
import inferencePackage.Assessment;
import inferencePackage.InferenceEngine;
import nodePackage.ComparisonLine;
import nodePackage.IterateLine;
import nodePackage.LineType;
import nodePackage.Node;
import ruleParser.RuleSetParser;
import ruleParser.RuleSetReader;
import ruleParser.RuleSetScanner;
import testingUtilPackage.NodeObject_For_Inference_Test;

public class Testing_for_ALL_Node_Lines_and_features_10 {
	public static void main(String[]args) throws IOException {
		RuleSetReader ilr = new RuleSetReader();
		ilr.setStreamSource(Testing_for_ALL_Node_Lines_and_features_10.class.getResourceAsStream("Testing for ALL Node Lines and features.txt"));
		RuleSetParser isf = new RuleSetParser();		
		RuleSetScanner rsc = new RuleSetScanner(ilr,isf);
		rsc.scanRuleSet();
		rsc.establishNodeSet();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(Testing_for_ALL_Node_Lines_and_features_10.class.getResourceAsStream("Comparison Testing for ALL Node Lines and features.txt")));
		String line;
		List<String> nodeListMock = new ArrayList<>();
		String[] tempArray;
		HashMap<String, NodeObject_For_Inference_Test> nameMap = new HashMap<>();

		while((line = br.readLine()) != null)
		{

			tempArray = line.split("&");
			nodeListMock.add(tempArray[0].trim());
			if(!line.isEmpty())
			{
				NodeObject_For_Inference_Test nfit = new NodeObject_For_Inference_Test(tempArray[0].trim(),tempArray[1].trim().split(":"));
				nameMap.put(tempArray[0].trim(), nfit);
			}
			
		}
		br.close();

		List<Integer> comparisonTempList = new ArrayList<>();
		IntStream.range(0, nodeListMock.size()).forEach(i->{
			String mockNode = nodeListMock.get(i);
			Node actualNode = isf.getNodeSet().getNodeSortedList().get(i);
			if(actualNode.getNodeName().equals("person's nationality IS \"Australian\"")
				&&mockNode.equals(actualNode.getNodeName())
				&&actualNode.getVariableName().equals("person's nationality")
				&&actualNode.getFactValue().getType().equals(FactValueType.DEFI_STRING)
				&&actualNode.getFactValue().getValue().toString().equals("Australian"))
			{
				comparisonTempList.add(i);
				
			}
			else if(actualNode.getNodeName().equals("person's name = \"troy jones\"")
					&&mockNode.equals(actualNode.getNodeName())
					&&actualNode.getVariableName().equals("person's name")
					&&actualNode.getFactValue().getType().equals(FactValueType.DEFI_STRING)
					&&actualNode.getFactValue().getValue().toString().equals("troy jones"))
			{
				comparisonTempList.add(i);
				
			}
			else if(actualNode.getNodeName().equals("person's dob > 01/01/1990")
					&&mockNode.equals(actualNode.getNodeName())
					&&((ComparisonLine)actualNode).getLHS().equals("person's dob")
					&&((ComparisonLine)actualNode).getRHS().getType().equals(FactValueType.DATE)
					&&((ComparisonLine)actualNode).getRHS().getValue().toString().equals("1990/01/01"))
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
			else if(actualNode.getNodeName().equals("person passport type = \"Australian\"")
					&&mockNode.equals(actualNode.getNodeName())
					&&((ComparisonLine)actualNode).getLHS().equals("person passport type")
					&&((ComparisonLine)actualNode).getRHS().getType().equals(FactValueType.DEFI_STRING)
					&&((ComparisonLine)actualNode).getRHS().getValue().toString().equals("Australian"))
			{
				comparisonTempList.add(i);
			}
			else if(actualNode.getNodeName().equals("person passport issued country = \"Australian\"")
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
			else if(actualNode.getNodeName().equals("person's citizenship status = \"canceled\"")
					&&mockNode.equals(actualNode.getNodeName())
					&&((ComparisonLine)actualNode).getLHS().equals("person's citizenship status")
					&&((ComparisonLine)actualNode).getRHS().getType().equals(FactValueType.DEFI_STRING)
					&&((ComparisonLine)actualNode).getRHS().getValue().toString().equals("canceled"))
			{
				comparisonTempList.add(i);
			}
			else if(actualNode.getNodeName().equals("person's legally yearly period of stay in Australia <= 20")
					&&mockNode.equals(actualNode.getNodeName())
					&&((ComparisonLine)actualNode).getLHS().equals("person's legally yearly period of stay in Australia")
					&&((ComparisonLine)actualNode).getRHS().getType().equals(FactValueType.INTEGER)
					&&((ComparisonLine)actualNode).getRHS().getValue().toString().equals("20"))
			{
				comparisonTempList.add(i);
			}
			else if(actualNode.getNodeName().equals("person's permanent residentship status = \"canceled\"")
					&&mockNode.equals(actualNode.getNodeName())
					&&((ComparisonLine)actualNode).getLHS().equals("person's permanent residentship status")
					&&((ComparisonLine)actualNode).getRHS().getType().equals(FactValueType.DEFI_STRING)
					&&((ComparisonLine)actualNode).getRHS().getValue().toString().equals("canceled"))
			{
				comparisonTempList.add(i);
			}
			else if(actualNode.getNodeName().equals("person's drinking habit = \"frequent drinker\"")
					&&mockNode.equals(actualNode.getNodeName())
					&&((ComparisonLine)actualNode).getLHS().equals("person's drinking habit")
					&&((ComparisonLine)actualNode).getRHS().getType().equals(FactValueType.DEFI_STRING)
					&&((ComparisonLine)actualNode).getRHS().getValue().toString().equals("frequent drinker"))
			{
				comparisonTempList.add(i);
			}
			else if(actualNode.getNodeName().equals("person's account IS IN LIST: premium account type")
					&&mockNode.equals(actualNode.getNodeName())
					&&actualNode.getVariableName().equals("person's account")
					&&actualNode.getFactValue().getType().equals(FactValueType.STRING)
					&&actualNode.getFactValue().getValue().toString().equals("premium account type"))
			{
				comparisonTempList.add(i);
			}
			else if(actualNode.getNodeName().equals("person's drinking habit IS \"social drinker\"")
					&&mockNode.equals(actualNode.getNodeName())
					&&actualNode.getVariableName().equals("person's drinking habit")
					&&actualNode.getFactValue().getType().equals(FactValueType.DEFI_STRING)
					&&actualNode.getFactValue().getValue().toString().equals("social drinker"))
			{
				comparisonTempList.add(i);
			}
			else if(actualNode.getNodeName().equals("person's drinking habit IS \"frequent drinker\"")
					&&mockNode.equals(actualNode.getNodeName())
					&&actualNode.getVariableName().equals("person's drinking habit")
					&&actualNode.getFactValue().getType().equals(FactValueType.DEFI_STRING)
					&&actualNode.getFactValue().getValue().toString().equals("frequent drinker"))
			{
				comparisonTempList.add(i);
			}
			else if(actualNode.getNodeName().equals("person's drinking habit IS \"heavy drinker\"")
					&&mockNode.equals(actualNode.getNodeName())
					&&actualNode.getVariableName().equals("person's drinking habit")
					&&actualNode.getFactValue().getType().equals(FactValueType.DEFI_STRING)
					&&actualNode.getFactValue().getValue().toString().equals("heavy drinker"))
			{
				comparisonTempList.add(i);
			}
			else if(actualNode.getNodeName().equals("number of drinks the person consumes a week IS CALC ( number of drinks the person consumes an hour * hours of drinks a day * (5-1))")
					&&mockNode.equals(actualNode.getNodeName())
					&&actualNode.getVariableName().equals("number of drinks the person consumes a week")
					&&actualNode.getFactValue().getType().equals(FactValueType.STRING)
					&&actualNode.getFactValue().getValue().toString().equals("( number of drinks the person consumes an hour * hours of drinks a day * (5-1))"))
			{
				comparisonTempList.add(i);
			}
			else if(actualNode.getNodeName().equals("ALL service ITERATE: LIST OF service history")
					&&mockNode.equals(actualNode.getNodeName())
					&&actualNode.getLineType().equals(LineType.ITERATE)
					&&((IterateLine)actualNode).getNumberOfTarget().equals("ALL")
					&&((IterateLine)actualNode).getGivenListName().equals("service history")
					&&actualNode.getFactValue().getType().equals(FactValueType.STRING)
					)
			{
				comparisonTempList.add(i);
			}
			else if(actualNode.getNodeName().equals("enlistment date >= 01/07/1951")
					&&mockNode.equals(actualNode.getNodeName())
					&&actualNode.getLineType().equals(LineType.COMPARISON)
					&&actualNode.getFactValue().getType().equals(FactValueType.DATE)
					)
			{
				comparisonTempList.add(i);
			}
			else if(actualNode.getNodeName().equals("discharge date <= 6/12/1972")
					&&mockNode.equals(actualNode.getNodeName())
					&&actualNode.getLineType().equals(LineType.COMPARISON)
					&&actualNode.getFactValue().getType().equals(FactValueType.DATE)
					)
			{
				comparisonTempList.add(i);
			}
			else if(actualNode.getNodeName().equals("service type IS IN LIST: Special service")
					&&mockNode.equals(actualNode.getNodeName())
					&&actualNode.getLineType().equals(LineType.VALUE_CONCLUSION)
					&&actualNode.getFactValue().getType().equals(FactValueType.DATE)
					)
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

		while(ie.getAssessmentState().getWorkingMemory().get(isf.getNodeSet().getNodeSortedList().get(0).getNodeName())==null || !ie.getAssessmentState().allMandatoryNodeDetermined())
		{
			
			Node nextQuestionNode = ie.getNextQuestion(ass);
			HashMap<String,FactValueType> questionFvtMap = ie.findTypeOfElementToBeAsked(nextQuestionNode);
			
			FactValueType fvt = null;
			String answer = "";
			
			for(String question: ie.getQuestionsFromNodeToBeAsked(nextQuestionNode))
			{
				System.out.println("questionFvt :"+questionFvtMap.get(question));
				System.out.println("Question: " + question+"?");
				
				if(question.equals("1st service discharge type"))
				{
					answer = "Medical";
				}
				else if(question.equals("2nd service discharge type"))
				{
					answer = "Dishonor";
				}
				else if(question.equals("3rd service discharge type"))
				{
					answer = "Voluntary";
				}
				else if(question.equals("3rd service service type"))
				{
					answer = "Turongga";
				}
				else if(question.equals("2nd service service type"))
				{
					answer = "Angular force";
				}
				else if(question.equals("1st service service type"))
				{
					answer = "SAP";
				}
				else if(question.equals("3rd service discharge date"))
				{
					answer = "5/12/1993";
				}
				else if(question.equals("2nd service discharge date"))
				{
					answer = "5/12/2003";
				}
				else if(question.equals("1st service discharge date"))
				{
					answer = "5/12/1970";
				}
				else if(question.equals("3rd service enlistment date"))
				{
					answer = "2/07/1995";
				}
				else if(question.equals("2nd service enlistment date"))
				{
					answer = "2/07/1995";
				}
				else if(question.equals("1st service enlistment date"))
				{
					answer = "2/07/1955";
				}
				else if(question.equals("number of services"))
				{
					answer = "3";
				}
				else if(question.equals("person's name"))
				{
					answer = "John Smith";
				}
				else if(question.equals("one of person's parents is Australian"))
				{
					answer = "true";
				}
				else if(question.equals("person's dob"))
				{
					answer = "11/12/1994";
				}
				else if(question.equals("the person missed the flight"))
				{
					answer = "false";
				}
				else if(question.equals("number of drinks the person consumes an hour"))
				{
					answer = "10";
				}
				else if(question.equals("hours of drinks a day"))
				{
					answer = "9";
				}
				else if(question.equals("person's account"))
				{
					answer = "HAHAHOHO";
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
					answer = "18";
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
				else if(question.equalsIgnoreCase("person's citizenship status"))
				{
					answer = "canceled";
				}
				else if(question.equalsIgnoreCase("person's legally yearly period of stay in Australia"))
				{
					answer = "19";
				}
				else if(question.equalsIgnoreCase("person's permanent residentship status"))
				{
					answer = "resident";
				}
//				else if(question.equals("person's dob"))
//				{
//					answer = "false";
//				}
				else if(question.equals("the person was born in Australia"))
				{
					answer = "true";
				}
				else if(i < 3)
				{
					answer = "true";
				}
				else
				{
					answer = nameMap.get(question).getValue();
				}
				System.out.println("Answer: "+answer);
				
				ie.feedAnswerToNode(nextQuestionNode, question, answer, questionFvtMap.get(question), ass);
				i++;
			}
			
			
		}

		HashMap<String, FactValue> workingMemory = ie.getAssessmentState().getWorkingMemory();
		
		
		ie.generateSortedSummaryList().stream().forEachOrdered(node ->{
			if(workingMemory.get(node).getType().equals(FactValueType.LIST))
			{
				((FactListValue<?>)workingMemory.get(node)).getValue().stream().forEach(item-> System.out.println("List Value - "+node+" : "+item));
			}
			{
				System.out.println(node+" : "+workingMemory.get(node).getValue().toString());
			}
		});	
	}

}
