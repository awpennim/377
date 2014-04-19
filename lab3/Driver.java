import java.util.Scanner;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

class Driver{
	public static void main(String[] args){
		FileSystem currentFS = null;
		
		Scanner in = new Scanner(System.in);
		
		while(true){
			String command = in.nextLine();
			String[] commandArray = command.split("\\s+"); // splits by whitespace. found answer at http://stackoverflow.com/questions/225337/how-do-i-split-a-string-with-any-whitespace-chars-as-delimiters
			
            if(currentFS == null && !validArgsForUpdatingCurrentFileSystem(commandArray))
                System.err.println("No filesystem selected");
            else if(validArgsForUpdatingCurrentFileSystem(commandArray))
                currentFS = getUpdatedFileSystem(currentFS, new FileSystem(commandArray[0].toCharArray()));
			else if(validArgsForCreate(commandArray))
				create(currentFS, commandArray[1], Integer.parseInt(commandArray[2]));
			else if(validArgsForDelete(commandArray))
				delete(currentFS, commandArray[1]);
			else if(validArgsForList(commandArray))
				list(currentFS);
			else if(validArgsForRead(commandArray))
				read(currentFS, commandArray[1], Integer.parseInt(commandArray[2]));
			else if(validArgsForWrite(commandArray))
				write(currentFS, commandArray[1], Integer.parseInt(commandArray[2]));
			else
				System.err.println("Invalid command");
		}
	}
	
	public static boolean validArgsForUpdatingCurrentFileSystem(String[] args){
		if(args.length != 1)
			return false;
			
        if(args[0].equals(""))
            return false;
        
        if(validArgsForWrite(args) || validArgsForRead(args) || validArgsForList(args) || validArgsForDelete(args) || validArgsForDelete(args) || validArgsForCreate(args))
            return false;
		
		return true;
	}
	
	public static FileSystem getUpdatedFileSystem(FileSystem oldFS, FileSystem newFS){
		if(newFS.isValidFileSystem()){
			System.out.println("Using file system: " + newFS.toString());
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
		
        try{
            if(fs.create(fileNameToCharArray(fileName), size) != 0)
                System.err.println("Error creating file");
        }catch(IOException e){
            System.err.println("Error creating file");
        }
	}

	public static boolean validArgsForDelete(String[] args){
		if(args.length != 2)
			return false;
		if(!args[0].equals("D") && !args[0].equals("d"))
			return false;
		
		// we can assume args[1] is a valid string
		
		return true;	
	}
	
	public static void delete(FileSystem fs, String fileName){
		if(!checkFileName(fileName))
			return;
			
        try{
            if(fs.delete(fileNameToCharArray(fileName)) != 0)
                System.err.println("Error deleting file");
        }catch(IOException e){
            System.err.println("Error deleting file");
        }
	}

	public static boolean validArgsForList(String[] args){
		if(args.length != 1)
			return false;
		if(!args[0].equals("L") && !args[0].equals("l"))
			return false;
			
		return true;
	}
	
	public static void list(FileSystem fs){
        try{
            if(fs.ls() != 0)
                System.err.println("Error listing files");
        }catch(IOException e){
            System.err.println("Error listing files");
        }
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
		
		byte[] buffer = new byte[1024];
        
        try{
            if(fs.read(fileNameToCharArray(fileName), blockNum, buffer) != 0)
                System.err.println("Error reading file");
        }catch(IOException e){
            System.err.println("Error reading file");
        }
        
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
	
        // generate dummy byte array
        String dummyString = fileName + "_" + blockNum + "_dummydata__";
        for(int i = 0; i < 10; i++)
            dummyString = dummyString + dummyString;
        byte[] dummyDataBuffer = null;
        try{
            dummyDataBuffer = dummyString.getBytes("UTF-16");
        }catch(UnsupportedEncodingException e){
            System.err.println("System doesn't support UTF-16. Now Terminating.");
            System.exit(1);
        }
        
        // populate byte array with dummy bytes
        byte[] buffer = new byte[1024];
		for(int i = 0; i < 1024; i++){
			buffer[i] = dummyDataBuffer[i];
		}
		
        try{
            if(fs.write(fileNameToCharArray(fileName), blockNum, buffer) != 0)
                System.err.println("Error writing file");
        }catch(IOException e){
            System.err.println("Error writing file");
        }
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
			System.out.println("file size must be between 0 and 7");
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
