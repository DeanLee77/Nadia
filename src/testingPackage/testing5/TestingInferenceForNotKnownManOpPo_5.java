package testingPackage.testing5;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;

import factValuePackage.FactValue;
import factValuePackage.FactValueType;
import inferencePackage.Assessment;
import inferencePackage.InferenceEngine;
import nodePackage.Node;
import ruleParser.RuleSetParser;
import ruleParser.RuleSetReader;
import ruleParser.RuleSetScanner;
import testingUtilPackage.NodeObject_For_Inference_Test;

public class TestingInferenceForNotKnownManOpPo_5 {

	public static void main(String[] args) throws IOException {
		
		HashMap<String, NodeObject_For_Inference_Test> nameMap = new HashMap<>();
		BufferedReader br = new BufferedReader(new InputStreamReader(TestingInferenceForNotKnownManOpPo_5.class.getResourceAsStream("testing NOT, KNOWN, Mandatory, Possibly, and Optionally inference.txt")));
		String line;
		while((line = br.readLine()) != null)
		{
			String[]lineArray = line.split("-");
			String[]lineArraySecondLevel = lineArray[1].split(":");
			NodeObject_For_Inference_Test nfit = new NodeObject_For_Inference_Test(lineArray[0],lineArraySecondLevel);
			nameMap.put(lineArray[0], nfit);
		}
		br.close();
		

		RuleSetReader ilr = new RuleSetReader();
		ilr.setStreamSource(TestingInferenceForNotKnownManOpPo_5.class.getResourceAsStream("testing NOT, KNOWN, Mandatory, Possibly, and Optionally.txt"));
		RuleSetParser isf = new RuleSetParser();		
		RuleSetScanner rsc = new RuleSetScanner(ilr,isf);
		rsc.scanRuleSet();
		rsc.establishNodeSet(null);
		
		InferenceEngine ie = new InferenceEngine(isf.getNodeSet());
		Assessment ass = new Assessment(isf.getNodeSet(), isf.getNodeSet().getNodeSortedList().get(0).getNodeName());
//		Scanner scan = new Scanner(System.in);
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
				if(i == 0)
				{
					answer = "false";
				}
				else if(question.equals("person's dob"))
				{
					answer = "false";
				}
				else if(question.equals("the person was born in Australia"))
				{
					answer = "false";
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
		ie.getAssessmentState().getSummaryList().stream().forEachOrdered(node ->{
			System.out.println(node+" : "+workingMemory.get(node).getValue().toString());
		});
		
	}

}
