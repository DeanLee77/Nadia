package ruleParser;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class RuleSetReader implements ILineReader{
	
	private BufferedReader br;
	
	public void setFileSource(String filePath)
	{
		try 
		{
			br = new BufferedReader(new FileReader(filePath));
		} 
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setStreamSource(InputStream inputStream)
	{
		br = new BufferedReader(new InputStreamReader(inputStream));
	}
	
	public void setStringSource(String text)
	{
		//convert a given text into InputStream
		InputStream inputStream = new ByteArrayInputStream(text.getBytes());
		
		//read the inputStream with BufferedReader
		br = new BufferedReader(new InputStreamReader(inputStream));
	}
	
	public String getNextLine()
	{
		String line = null;
		try 
		{
			line = br.readLine();
		} 
		catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(line == null)
		{
			try 
			{
				br.close();
			} 
			catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return line;
	}
	

}

