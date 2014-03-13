import java.util.ArrayList;
import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Collections;
import java.util.HashMap;

public class Index{
	public static void main(String[] args){
		int n = Integer.parseInt(args[0]);
		int k = args.length - 1;
		
		// create inverted index
		ConcurrentHashMap<String, HashMap<String, LocationInformation>> inverted_index = new ConcurrentHashMap<String, HashMap<String, LocationInformation>>();
		
		// create n reducer threads and add them to array list
		ArrayList<Reducer> reducers = new ArrayList<Reducer>();
		for(int i = 0; i < k; i++){
			Reducer reducer = new Reducer(inverted_index);
			reducers.add(reducer);
			reducer.start();
		}
		
		// create k mappers and add them to an array list
		AtomicInteger mappers_count = new AtomicInteger(k);
		ArrayList<Mapper> mappers = new ArrayList<Mapper>();
		for(int i = 1; i < args.length; i++){
			mappers.add(new Mapper(args[i], mappers_count, reducers, inverted_index));
		}
		
		// start the mapper threads
		for(int i = 0; i < mappers.size(); i++){
			mappers.get(i).start();	
		}
	}
}

class Reducer extends Thread{
	private ConcurrentHashMap<String, HashMap<String, LocationInformation>> inverted_index;
	
	public Reducer(ConcurrentHashMap<String, HashMap<String, LocationInformation>> inverted_index){
		this.inverted_index = inverted_index;
	}
	
	public void run(){
		int i = 1;
		while(i != 0);
	}
	
	public void addWord(String word, String filename, int line_number){
		if(inverted_index.containsKey(word) == false){
			inverted_index.put(word, new HashMap<String, LocationInformation>());	
		}
		
		if(inverted_index.get(word).containsKey(filename) == false){
			inverted_index.get(word).put(filename, new LocationInformation(filename));
		}
		
		inverted_index.get(word).get(filename).addLineNumber(line_number);
	}
}

class Mapper extends Thread{
	private int k;
	private ArrayList<Reducer> reducers;
	private String filename;
	private AtomicInteger mappers_count;
	private ConcurrentHashMap<String, HashMap<String, LocationInformation>> inverted_index;
	
	public Mapper(String filename, AtomicInteger mappers_count, ArrayList<Reducer> reducers, ConcurrentHashMap<String, HashMap<String, LocationInformation>> inverted_index){
		this.filename = filename;	
		this.mappers_count = mappers_count;
		this.reducers = reducers;
		this.k = reducers.size();
		this.inverted_index = inverted_index;
	}
	
	public void run(){
		try{
			BufferedReader file_contents = new BufferedReader(new FileReader(new File(filename)));
			
			String line; int line_number = 0;
			while((line = file_contents.readLine()) != null){
				line_number++;
				
				String[] words = line.replaceAll("[^A-Za-z0-9 ]","").toLowerCase().split("\\s+");
				
				for(int i = 0; i < words.length; i++){
					reducers.get(((words[i].hashCode() % k) + k) % k).addWord(words[i], filename, line_number);	
				}
			}	
		}
		catch(FileNotFoundException e){
			System.out.println("Couldn't find file: " + filename);	
		}
		catch(IOException e){
			System.out.println("Java IOException thrown!");
		}
		
		int mappers_left = mappers_count.decrementAndGet();
		
		if(mappers_left == 0){
			System.out.println("done");	
			System.out.println(inverted_index.get("the"));
			System.exit(0);
		}
	}
}

class LocationInformation{
	private String filename;
	private ArrayList<Integer> line_numbers = new ArrayList<Integer>();
	
	public LocationInformation(String filename){
		this.filename = filename;
	}
	
	public void addLineNumber(int line_number){
		line_numbers.add(line_number);
	}
	
	public String getLineNumbers(){
		Collections.sort(line_numbers);
		
		String result = "";
		for(int i = 0; i < line_numbers.size(); i++){
			result = result + "," + line_numbers.get(i);	
		}
		
		return result;
	}
}