import java.util.Scanner;
// test
class Driver{
	public static void main(String[] args){
		FileSystem currentFS = null;
		
		Scanner in = new Scanner(System.in);
		
		while(true){
			String command = in.nextLine();
			String[] commandArray = command.split("\\s+"); // splits by whitespace. found answer at http://stackoverflow.com/questions/225337/how-do-i-split-a-string-with-any-whitespace-chars-as-delimiters
			
			if(validArgsForCreate(commandArray))
				create(currentFS, commandArray[1], Integer.parseInt(commandArray[2]));
			else if(validArgsForDelete(commandArray))
				delete(currentFS, commandArray[1]);
			else if(validArgsForList(commandArray))
				list(currentFS);
			else if(validArgsForRead(commandArray))
				read(currentFS, commandArray[1], Integer.parseInt(commandArray[2]));
			else if(validArgsForWrite(commandArray))
				write(currentFS, commandArray[1], Integer.parseInt(commandArray[2]));
			else if(validArgsForUpdatingCurrentFileSystem(commandArray)){
				currentFS = getUpdatedFileSystem(currentFS, new FileSystem(commandArray[0].toCharArray()));
			}
			else
				System.out.println("Invalid command");
		}
	}
	
	public static boolean validArgsForUpdatingCurrentFileSystem(String[] args){
		if(args.length != 1)
			return false;
			
		// we can assume args[0] is a valid string
		
		return true;
	}
	
	public static FileSystem getUpdatedFileSystem(FileSystem oldFS, FileSystem newFS){
		if(newFS.validFileSystem()){
			System.out.println("Using new file system: " + newFS.toString());
			return newFS;
		}
		else{
			System.out.println("Could not find file system with name: " + newFS.toString());
			return oldFS;
		}
	}
	
	public static boolean validArgsForCreate(String[] args){
		if(args.length != 3)
			return false;
		if(!args[0].equals("C") && !args[0].equals("c"))
			return false;
		
		// we can assume args[1] is a valid string
		
		try{
			Integer.parseInt(args[2]);	
		}
		catch(NumberFormatException e){
			return false;	
		}
		
		return true;
	}
	
	public static void create(FileSystem fs, String fileName, int size){
		if(!checkFileName(fileName) || !checkSize(size))
			return;
		
		fs.create(fileNameToCharArray(fileName), size);	
	}
	
	public static boolean validArgsForDelete(String[] args){
		if(args.length != 3)
			return false;
		if(!args[0].equals("D") && !args[0].equals("d"))
			return false;
		
		// we can assume args[1] is a valid string
		
		return true;	
	}
	
	public static void delete(FileSystem fs, String fileName){
		if(!checkFileName(fileName))
			return;
			
		fs.delete(fileNameToCharArray(fileName));
	}
	
	public static boolean validArgsForList(String[] args){
		if(args.length != 1)
			return false;
		if(!args[0].equals("L") && !args[0].equals("l"))
			return false;
			
		return true;
	}
	
	public static void list(FileSystem fs){
		fs.ls();
	}
	
	public static boolean validArgsForRead(String[] args){
		if(args.length != 3)
			return false;
		if(!args[0].equals("R") && !args[0].equals("r"))
			return false;
		
		// we can assume args[1] is a valid string
		
		try{
			Integer.parseInt(args[2]); // will throw exception and return false iff args[2] is not an integer
			
			if(Integer.parseInt(args[2]) < 0 || Integer.parseInt(args[2]) > 7)
				return false;
		}
		catch(NumberFormatException e){
			return false;	
		}
		
		return true;
	}
	
	public static void read(FileSystem fs, String fileName, int blockNum){
		if(!checkFileName(fileName) || !checkSize(blockNum))
			return;
		
		char[] buffer = new char[1024];
		fs.read(fileNameToCharArray(fileName), blockNum, buffer);
		
		// do something with buffer
	}
	
	public static boolean validArgsForWrite(String[] args){
		if(args.length != 3)
			return false;
		if(!args[0].equals("W") && !args[0].equals("w"))
			return false;
		
		// we can assume args[1] is a valid string
		
		try{
			Integer.parseInt(args[2]);
			
			if(Integer.parseInt(args[2]) < 0 || Integer.parseInt(args[2]) > 7)
				return false;
		}
		catch(NumberFormatException e){
			return false;	
		}
		
		return true;
	}
	
	public static void write(FileSystem fs, String fileName, int blockNum){
		if(!checkFileName(fileName) || !checkSize(blockNum))
			return;
		
		// dummy data
		char[] buffer = new char[1024];
		for(int i = 0; i < 1024; i++){
			buffer[i] = (char)(i % 256);	
		}
		
		fs.write(fileNameToCharArray(fileName), blockNum, buffer);
	}
	
	public static boolean checkFileName(String fileName){
		if(fileName.length() > 8){
			System.out.println("file name is too long (8 max)");
			
			return false;
		}
		
		return true;
	}
	
	public static boolean checkSize(int size){	
		if(size < 0 || size > 8){
			System.out.println("file size must be positive and no greater than 8");
			return false;
		}	
		
		return true;
	}
	
	public static char[] fileNameToCharArray(String fileName){
		char[] fileNameChar = new char[8];
		for(int i = 0; i < fileName.length(); i++){
			fileNameChar[i] = fileName.charAt(i);	
		}
		for(int i = fileName.length(); i < 8; i++){
			fileNameChar[i] = '\0';	
		}
		
		return fileNameChar;
	}
}
