package testingPackage;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import nodePackage.Node;
import ruleParser.RuleSetParser;
import ruleParser.RuleSetReader;
import ruleParser.RuleSetScanner;

public class TopoSortingTest_2 {

	public static void main(String[] args) throws IOException {
		//this testing is to check if topological sorting is done correctly or not by comparing a sorted list of 'Wedding Planner.txt' file in RuleSetParser with 'ToposortedNodeName.txt' file
		
		RuleSetReader ilr = new RuleSetReader();
		ilr.setStreamSource(TopoSortingTest_2.class.getResourceAsStream("Wedding Planner.txt"));
		RuleSetParser isf = new RuleSetParser();		
		RuleSetScanner rsc = new RuleSetScanner(ilr,isf);
		rsc.scanRuleSet();
		rsc.establishNodeSet();
		List<String> nameList = new ArrayList<>();
		BufferedReader br = new BufferedReader(new InputStreamReader(TopoSortingTest_2.class.getResourceAsStream("ToposortedNodeName.txt")));
		String line;
		while((line = br.readLine()) != null)
		{
			nameList.add(line);
		}
		br.close();
		List<String[]> filteredList = new ArrayList<>();
		IntStream.range(0, nameList.size()).forEach(i->{
			if(!isf.getNodeSet().getNodeSortedList().get(i).getNodeName().equals(nameList.get(i)))
			{
				filteredList.add(new String[]{Integer.toString(i), nameList.get(i)});
			}
		});
		
		if(filteredList.size() >0)
		{
			IntStream.range(0, filteredList.size()).forEach(i->{
				System.out.println("node set: "+isf.getNodeSet().getNodeSortedList().get(Integer.parseInt(filteredList.get(i)[0])).getNodeName());
				System.out.println("nameList: "+nameList.get(Integer.parseInt(filteredList.get(i)[0])));
				
			});
		}
		else
		{
			System.out.println("Yep!!! it's been finished correctly");
		}
		
	}

}
