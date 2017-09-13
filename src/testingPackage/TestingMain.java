package testingPackage;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.IntStream;

import inferencePackage.*;
import nodePackage.Node;
import ruleParser.*;


public class TestingMain {
public static void main(String[] args) {
		
		String h = "UMLUM";
		String h1= h.substring(1, h.length());
		List<String> hList = new ArrayList<>();
		hList.add("this is array");
		hList.add("hi");
		hList.add("what are you looking at?");
		hList.add("don't look at me!!!");

		int tempInt = h1.substring(0, h1.indexOf('U')).length();
		StringBuilder sb = new StringBuilder();
		IntStream.range(1, tempInt+1).forEachOrdered(i -> {sb.append(hList.get(i)+" ");});
		System.out.println(h1.substring(0, h1.indexOf('U')));
		System.out.println(sb.toString().trim());
	
		String s = "NOT KNOWN the guy's NOT name is Dean Lee";
		String tt = "Dean fixed all bugs NOT in Nadia";
		System.out.println(tt.replaceAll("((NOT(?=\\s))|(KNOWN(?=\\s)))+", ""));
//		Tokens token = Tokenizer.getTokens(s);
//		System.out.println(s.toLowerCase());
//		ValueConclusionLine vcl = new ValueConclusionLine(s, token);
		
		RuleSetReader ilr = new RuleSetReader();
//		ilr.setFileSource("testingRuleFile.txt");
//		ilr.setFileSource("ActsTriageRules.txt");
		ilr.setFileSource("testingFile_For_A_IS_B_Type_Rule.txt");


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