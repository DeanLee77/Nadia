package testingPackage;

import java.awt.List;
import java.util.Arrays;

import ruleParser.RuleSetParser;
import ruleParser.RuleSetReader;
import ruleParser.RuleSetScanner;

public class testingNotKnownMandatoryPossiblyAndOptionally {

	public static void main(String[] args) {
		RuleSetReader ilr = new RuleSetReader();
		ilr.setStreamSource(WeddingPlanner_Inference_Test.class.getResourceAsStream("testing NOT, KNOWN, Mandatory, Possibly, and Optionally.txt"));
		RuleSetParser isf = new RuleSetParser();		
		RuleSetScanner rsc = new RuleSetScanner(ilr,isf);
		rsc.scanRuleSet();
		rsc.establishNodeSet();
		
		isf.getNodeSet().getNodeSortedList().stream().forEachOrdered(node ->{
			System.out.println("nodeName: "+node.getNodeName());
			isf.getNodeSet().getDependencyMatrix().getToChildDependencyList(node.getNodeId()).stream().forEach(dep ->{System.out.println("dependency type: "+isf.getNodeSet().getDependencyMatrix().getDependencyType(node.getNodeId(), isf.getNodeSet().getNodeMap().get(isf.getNodeSet().getNodeIdMap().get(dep)).getNodeId()));System.out.println("dependency: "+isf.getNodeSet().getNodeIdMap().get(dep));}); 
			
		});

	}

}
