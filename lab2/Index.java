import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Index{
	// the inverted_index maps words in the text files (strings) to another hashmap. this other hashmap maps the filenames to an array list of line numbers (Integers).
	private static ConcurrentHashMap<String, HashMap<String, ArrayList<Integer>>> inverted_index;
	
	private static ArrayList<Reducer> reducers;
	private static ArrayList<Mapper> mappers;
	
	// count of the mapper threads
	private static AtomicInteger mappers_count;
	
	public static void main(String[] args){
		// n = number of reducer threads
		int n = Integer.parseInt(args[0]);
		// k = number of mapper threads
		int k = args.length - 1;
		
		// create inverted index
		inverted_index = new ConcurrentHashMap<String, HashMap<String, ArrayList<Integer>>>();
		
		// create/start n reducer threads and add them to array list
		reducers = new ArrayList<Reducer>();
		for(int i = 0; i < n; i++){
			Reducer reducer = new Reducer();
			reducers.add(reducer);
			reducer.start();
		}
		
		// create/start k mappers and add them to an array list
		AtomicInteger mappers_count = new AtomicInteger(k);
		mappers = new ArrayList<Mapper>();
		for(int i = 0; i < k; i++){
			Mapper mapper = new Mapper(args[i + 1], mappers_count, reducers)
			mappers.add(mapper);
			mapper.start();
		}
		
	}
	
	// called by the Reducer objects
	public static void addWordToInvertedIndex(String word, String filename, int line_number){
		// if its the first time we've seen the word
		if(inverted_index.containsKey(word) == false){
			inverted_index.put(word, new HashMap<String, ArrayList<Integer>>()); // create/add a hashmap	
		}
		
		// if its the forst time we've seen this word in this file
		if(inverted_index.get(word).containsKey(filename) == false){
			inverted_index.get(word).put(filename, new ArrayList<Integer>()); // create/add an array list
		}
		
		inverted_index.get(word).get(filename).add(line_number);	
	}
	
	// called by the Mapper objects
	public static Reducer getReducer(int i){
		return reducers.get(i);	
	}
	
	// not called anywhere, but I thought it made the code better
	public static Mapper getMapper(int i){
		return mappers.get(i);	
	}
	
	public static void mapperThreadComplete(){
		int mappers_left = mappers_count.decrementAndGet();
		
		// if all mappers have finished
		if(mappers_left == 0){
			List<HashMap<String, ArrayList<Integer>>> words = inverted_index.keySet(); // list of all the words in all text files
			Collections.sort(words); // sort those words in alphabetical order
			
			// iterate over the sorted words. we will print out 1 line for each word.
			for(int i = 0; i < words.length; i++){
				System.out.print(words.get(i)); // prints the word as the first thing in this line
				
				HashMap<String, ArrayList<Integer>> word_hash_map = inverted_index.get(words.get(i)); // the hash map for a word that maps filenames to an array list of line numbers
				
				List<String> file_names = word_hash_map.keySet(); // all filenames for this word
				Collections.sort(file_names); // sort those filenames alphabetically
				
				// iterate over each filename for this word.
				for(int j = 0; j < file_names.length; j++){
					System.out.print(" " + file_names.get(j) + "@"); // prints a space then the filename. it will be followed by the line numbers
					
					ArrayList<Integer> line_numbers = file_names.get(j); // get the line numbers array list for this file name for this word
					Collections.sort(line_numbers); // sort it numerically
					
					System.out.print(line_numbers.get(0)); // the first line number is unique. we just print out the line number
					for(int l = 1; l < line_numbers.size(); l++){
						System.out.print(line_numbers.get(l)); // the following line numbers need a comma (",") printed before it.
					}
				}
				
				System.out.println(); // return to the next line
			}
	
			System.exit(0);
		}
	}
}

class Reducer extends Thread{
	private LinkedList<BufferObject> buffer = new LinkedList<BufferObject>();
	
	private Semaphore lock = new Semaphore(1);
	private Semaphore full = new Semaphore(10);
	private Semaphore empty = new Semaphore(0)
	
	public void run(){
		while(true){
			BufferObject bo = getNextBufferObject();
			
			Index.addWordToInvertedIndex(bo.getWord(), bo.getFileName(), bo.getLineNumber());
		}
	}
	
	// called by mapper objects
	public void addWordToBuffer(BufferObject bo){
		full.acquire();
		lock.acquire();
		
		buffer.add(new BufferObject(word, filename, line_number));
		
		lock.release();
		empty.release();
	}
	
	private BufferObject getNextBufferObject(){
		empty.acquire();
		lock.acquire();
			
		BufferObject bo = buffer.removeFirst();
			
		lock.release();
		full.release();	
	}
}

class Mapper extends Thread{
	private String filename;
	private int k;
	
	public Mapper(String filename, int k){
		this.filename = filename;	
		this.k = k;
	}
	
	public void run(){
		try{
			BufferedReader text_file_reader = new BufferedReader(new FileReader(new File(filename)));
			
			String line; int line_number = 1;
			while((line = text_file_reader.readLine()) != null){
				// array of all words in the current line
				String[] words = line.replaceAll("[^A-Za-z0-9 ]","").toLowerCase().split("\\s+");
				
				for(int i = 0; i < words.length; i++){
					// (words[i].hashCode() % k) sometimes returns a negative integer. so, we add k, then mod by k to get a positive integer equivalent mod k.
					Reducer reducer = Index.getReducer(((words[i].hashCode() % k) + k) % k);
					
					reducer.addWord(words[i], filename, line_number);	
				}
				
				// increment the line number
				line_number++;
			}	
		}
		catch(FileNotFoundException e){
			System.out.println("Couldn't find file: " + filename);	
		}
		catch(IOException e){
			System.out.println("Java IOException thrown!");
		}
		
		Index.mapperThreadComplete();
	}
}

// used by Reducer objets to queue a word,filename, and line_number object
class BufferObject{
	private String word;
	private String filename;
	private int line_number;
	
	public BufferObject(String word, String filename, int line_number){
		self.word = word;
		self.filename = filename;
		self.line_number = line_number;
	}
	
	public String getWord(){
		return word;	
	}
	
	public String getFileName(){
		return filename;	
	}
	
	public int getLineNumber(){
		return line_number	
	}
}