import java.util.ArrayList;

public class Index{
	public static void main(String[] args){
		int n = Integer.parseInt(args[0]);
		
		ArrayList<Mapper> files = new ArrayList<Mapper>();
		for(int i = 0; i < args.length; i++){
			files.add(new Mapper(args[i]));
		}
		
		for(int i = 0; i < files.size(); i++){
			files.get(i).start();	
		}
	}
}

class Mapper extends Thread{
	private String filename;
	
	public Mapper(String filename){
		this.filename = filename;	
	}
	
	public void run(){
		File file = new File(filename);
		
		BufferedReader fileContents = BufferedReader(new FileReader(file))
		
		while(fileContents.hasNextLine()){
			String line = fileContents.readLine();
			
			System.out.println(line);
		}	
	}
}