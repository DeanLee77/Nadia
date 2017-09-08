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
		ilr.setFileSource("testingRuleFile.txt");
//

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
			System.out.println(ie.findTypeOfElementToBeAsked(nextQuestionNode));
			System.out.println("Question: "+nextQuestionNode.getNodeName() +" ?");
			
			ie.getQuestionsfromNodeToBeAsked(nextQuestionNode).stream().forEachOrdered(question -> {
				System.out.println("Question: " + question);
				String answer = scan.nextLine();
				final String finalAnswer = answer;
				ie.feedAnswerToNode(nextQuestionNode.getNodeName(), question, finalAnswer);
			});

			
		}
	
	
	}

}