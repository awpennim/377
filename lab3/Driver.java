import java.util.Scanner;

class Driver{
	public static void main(String[] args){
		FileSystem currentFS = null;
		
		Scanner in = new Scanner(System.in);
		
		while(true){
			String command = in.nextLine();
			String[] commandArray = command.split("\\s+"); // splits by whitespace. found answer at http://stackoverflow.com/questions/225337/how-do-i-split-a-string-with-any-whitespace-chars-as-delimiters
			
			switch(commandArray[0].toUpperCase()){
				case "C":	create(currentFS);
							break;
				case "D":	delete(currentFS);
							break;
				case "L":	list(currentFS);
							break;
				case "R":	read(currentFS);
							break;
				case "W":	write(currentFS);
							break;
				default:	if(commandArray.length == 1) currentFS = new FileSystem(commandArray[0].toCharArray()); else System.out.println("Invalid Command");
			}	
		}
	}
	
	public static void create(FileSystem fs){
		System.out.println("Create");
		return;	
	}
	
	public static void delete(FileSystem fs){
		System.out.println("Delete");
		return;	
	}
	
	public static void list(FileSystem fs){
		System.out.println("List");
		return;	
	}
	
	public static void read(FileSystem fs){
		System.out.println("Read");
		return;	
	}
	
	public static void write(FileSystem fs){
		System.out.println("Write");
		return;	
	}
}
