import java.util.ArrayList;

public class Index{
	public static void main(String[] args){
		int n = Integer.parseInt(args[0]);
		
		ArrayList<String> files = new ArrayList<String>();
		for(int i = 0; i < args.length; i++){
			files.add(args[i]);
		}
	}
}