package testingPackage.testing1;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;

import factValuePackage.FactValue;
import ruleParser.Tokenizer;
import ruleParser.Tokens;

public class TokenizerTesting_1 {

	public static void main(String[] args) throws IOException {
		
		FactValue fv = FactValue.parse(true);
		System.out.println("fv.getType(): "+fv.getType());
		// TODO Auto-generated method stub
		String fileName = "Tokenizer_Testing.txt";

		BufferedReader br = new BufferedReader(new InputStreamReader(TokenizerTesting_1.class.getResourceAsStream(fileName)));

			String line;
			String textString = "";
			int lineTracking = 0;
			Tokens tk = null;
			while ((line = br.readLine()) != null) 
			{
				line = line.trim();
				if(!line.equals("") && !line.matches("^\\/.*"))
				{
					if(lineTracking == 0)
					{
						textString = line;
						tk = Tokenizer.getTokens(line);
						lineTracking++;

					}
					else if(lineTracking == 1)
					{
						System.out.println("text string: "+textString);
						System.out.println("tk.tokenString: "+tk.tokensString);
						System.out.println("expected tokenString line :"+line);
						System.out.println("\n");
						if(!tk.tokensString.equals(line))
						{
							System.out.println("above line is not same as below line" );
							return ;
						}
						else
						{
							lineTracking = 0;
						}
					}
				}
								
				
			}


	}

}
