package testingPackage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import factValuePackage.FactValue;
import factValuePackage.FactValueType;
import inferencePackage.*;
import nodePackage.Node;
import ruleParser.*;


public class TestingMain {
public static void main(String[] args) {
		
//		String h = "UMLUM";
//		String h1= h.substring(1, h.length());
//		List<String> hList = new ArrayList<>();
//		hList.add("this is array");
//		hList.add("hi");
//		hList.add("what are you looking at?");
//		hList.add("don't look at me!!!");
//
//		int tempInt = h1.substring(0, h1.indexOf('U')).length();
//		StringBuilder sb = new StringBuilder();
//		IntStream.range(1, tempInt+1).forEachOrdered(i -> {sb.append(hList.get(i)+" ");});
//		System.out.println(h1.substring(0, h1.indexOf('U')));
//		System.out.println(sb.toString().trim());
//	
//		String s = "NOT KNOWN the guy's NOT name is Dean Lee";
//		String tt = "Dean fixed all bugs NOT in Nadia";
//		System.out.println(tt.replaceAll("((NOT(?=\\s))|(KNOWN(?=\\s)))+", ""));
//		Tokens token = Tokenizer.getTokens(s);
//		System.out.println(s.toLowerCase());
//		ValueConclusionLine vcl = new ValueConclusionLine(s, token);
		
		RuleSetReader ilr = new RuleSetReader();
		
//		ilr.setFileSource("ExprConclusionLine rule with NEEDS only.txt");
//		ilr.setFileSource("testingFile_For_A_IS_B_Type_Rule.txt");
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
			System.out.println("Question: "+nextQuestionNode.getNodeName() +" ?");
			FactValueType fvt = null;
			for(String question: ie.getQuestionsfromNodeToBeAsked(nextQuestionNode))
			{
				System.out.println("Question: " + question);
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