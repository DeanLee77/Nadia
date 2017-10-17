package testingPackage;

import java.util.Scanner;
import java.util.stream.Stream;

import factValuePackage.FactValueType;
import inferencePackage.Assessment;
import inferencePackage.InferenceEngine;
import nodePackage.Node;
import ruleParser.RuleSetParser;
import ruleParser.RuleSetReader;
import ruleParser.RuleSetScanner;

public class OrdinaryValueConclusionTesting {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		RuleSetReader ilr = new RuleSetReader();
		ilr.setFileSource("Wedding Planner.txt");



		RuleSetParser isf = new RuleSetParser();		
		RuleSetScanner rsc = new RuleSetScanner(ilr,isf);
		rsc.scanRuleSet();
		rsc.establishNodeSet();
		InferenceEngine ie = new InferenceEngine(isf.getNodeSet());
		Assessment ass = new Assessment(isf.getNodeSet(), isf.getNodeSet().getNodeSortedList().get(0).getNodeName());
		Scanner scan = new Scanner(System.in);
		
		while(ie.getAssessmentState().getWorkingMemory().get(isf.getNodeSet().getNodeSortedList().get(0))==null)
		{
			
			Node nextQuestionNode = ie.getNextQuestion(ass);
			FactValueType questionFvt = ie.findTypeOfElementToBeAsked(nextQuestionNode);
			System.out.println("questionFvt :"+questionFvt);
			FactValueType fvt = null;
			for(String question: ie.getQuestionsFromNodeToBeAsked(nextQuestionNode))
			{
				System.out.println("Question: " + question+"?");
				String answer = scan.nextLine();			
				
				ie.feedAnswerToNode(nextQuestionNode.getNodeName(), question, answer, questionFvt);
			}

			
		}
		Stream<String> keyList = ie.getAssessmentState().getWorkingMemory().keySet().stream();
		keyList.forEach(key -> {
			System.out.println(key+" : "+ie.getAssessmentState().getWorkingMemory().get(key));
		});
		
		scan.close();

	}

}
