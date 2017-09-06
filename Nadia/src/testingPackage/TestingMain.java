package testingPackage;

import java.util.Scanner;

import inferencePackage.*;
import nodePackage.Node;
import ruleParser.*;


public class TestingMain {
public static void main(String[] args) {
		
//		String s = "MANDATORY NOT KNOWN the guy's name is Dean Lee";
//		System.out.println(s.replaceFirst("([(NOT(?=\\s))(KNOWN(?=\\s))(MANDATORY(?=\\s))]*)", ""));
//		Tokens token = Tokenizer.getTokens(s);
//		System.out.println(s.toLowerCase());
//		ValueConclusionLine vcl = new ValueConclusionLine(s, token);
		
		RuleSetReader ilr = new RuleSetReader();
		ilr.setFileSource("ActsTriageRules.txt");
//		ilr.setFileSource("testingRuleForParser2.txt");
//

		RuleSetParser isf = new RuleSetParser();		
		RuleSetScanner rsc = new RuleSetScanner(ilr,isf);
		rsc.scanRuleSet();
		rsc.establishNodeSet();
		InferenceEngineV3 iev3 = new InferenceEngineV3(isf.getNodeSet());
		Assessment ass = new Assessment(isf.getNodeSet(), isf.getNodeSet().getNodeSortedList().get(0).getNodeName());
		Scanner scan = new Scanner(System.in);
		
		while(iev3.getAssessmentState().getWorkingMemory().get(isf.getNodeSet().getNodeSortedList().get(0))==null)
		{
			
			Node nextQuestionNode = iev3.getNextQuestion(ass);
			System.out.println(iev3.findTypeOfElementToBeAsked(nextQuestionNode));
			System.out.println("Question: "+nextQuestionNode.getNodeName());
			
			iev3.getQuestionsfromNodeToBeAsked(nextQuestionNode).stream().forEachOrdered(question -> {
				System.out.println("Question: " + question);
				String answer = scan.nextLine();
				final String finalAnswer = answer;
				iev3.feedAnswerToNode(nextQuestionNode.getNodeName(), question, finalAnswer);
			});

			
		}
	
	
	}

}