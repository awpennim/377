// line 17, try different args for exec

import java.util.Scanner;
import java.io.*;

class Closh {
	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);
		
		while(true){
			System.out.print("mysh>");
			
			// read next command
			String command = in.nextLine();
			
			//break out of the program if exit command was typed
			if(command.split("\\s+")[0].equals("exit"))
				break;
			
			try{
				Process childProcess = Runtime.getRuntime().exec(command.split("\\s+"));
				
				BufferedReader childProcessOutput = new BufferedReader(new InputStreamReader(childProcess.getInputStream()));  
            	for(String nextLine = childProcessOutput.readLine(); nextLine != null; nextLine = childProcessOutput.readLine()) 
                	System.out.println(nextLine);  
				
				boolean shouldKeepWaiting = false;
				do{
					try{
						childProcess.waitFor();	
					}
					catch(InterruptedException e){
						shouldKeepWaiting = true;
					}
				} while(shouldKeepWaiting);
			}
			catch(IOException e){
				System.out.println("command invalid");	
			}

			
		}
	}
}
