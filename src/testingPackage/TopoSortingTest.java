package testingPackage;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import nodePackage.Node;
import ruleParser.RuleSetParser;
import ruleParser.RuleSetReader;
import ruleParser.RuleSetScanner;

public class TopoSortingTest {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		RuleSetReader ilr = new RuleSetReader();
		ilr.setFileSource("Wedding Planner.txt");



		RuleSetParser isf = new RuleSetParser();		
		RuleSetScanner rsc = new RuleSetScanner(ilr,isf);
		rsc.scanRuleSet();
		rsc.establishNodeSet();
		List<String> nameList = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader("ToposortedNodeName.txt"));
		String line;
		while((line = br.readLine()) != null)
		{
			nameList.add(line);
		}
		
		IntStream.range(0, nameList.size()).forEach(i->{
			if(!isf.getNodeSet().getNodeSortedList().get(i).getNodeName().equals(nameList.get(i)))
			{
				System.out.println("not correctly sorted");
				System.out.println("node set: "+isf.getNodeSet().getNodeSortedList().get(i).getNodeName());
				System.out.println("nameList: "+nameList.get(i));
				return ;
			}
			return;
		});
		
		System.out.println("Yep!!! it's been finished correctly");
	}

}
