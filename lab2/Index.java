import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

public class Index{
	// the inverted_index maps words in the text files (strings) to another hashmap. this other hashmap maps the filenames to an array list of line numbers (Integers).
	private static ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<Integer>>> inverted_index = new ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<Integer>>>();
	
	protected static final ArrayList<Reducer> reducers = new ArrayList<Reducer>();
	protected static final ArrayList<Mapper> mappers = new ArrayList<Mapper>();
	
	// count of the mapper threads
	private static AtomicInteger mapper_threads_remaining_count;
	
	public static void main(String[] args){
		// n = number of reducer threads
		int n = Integer.parseInt(args[0]);
		// k = number of mapper threads
		int k = args.length - 1;
		// we start out with k mapper threads
		mapper_threads_remaining_count = new AtomicInteger(k);
		
		// create/start n reducer threads and add them to array list
		for(int i = 0; i < n; i++){
			Reducer reducer = new Reducer();
			reducers.add(reducer);
			reducer.start();
		}
		
		// create/start k mappers and add them to an array list
		for(int i = 0; i < k; i++){
			Mapper mapper = new Mapper(args[i + 1], k); // we send the mapper thread the text file which it will be parsing and we also send it the number of reducer threads. This allows the mapper to send a "job" randomly to a recucer thread.
			mappers.add(mapper);
			mapper.start();
		}
	}
	
	// called by the Reducer objects when they want to add a word to the inverted index
	// this method IS thead safe!
	public static void addWordToInvertedIndex(String word, String filename, int line_number){
		// if its the first time we've seen the word, create/add a hashmap
		inverted_index.putIfAbsent(word, new ConcurrentHashMap<String, ArrayList<Integer>>()); // 	
		
		// if its the forst time we've seen this word in this file, create/add an array list
		inverted_index.get(word).putIfAbsent(filename, Collections.synchronizedList(new ArrayList())<Integer>()); 
		
		inverted_index.get(word).get(filename).add(line_number);	
	}
	
	// called from the Mapper theads when they finish running
	public static void mapperThreadComplete(){
		int mappers_left = mapper_threads_remaining_count.decrementAndGet();
		
		// if all mappers have finished, print out inverted index
		if(mappers_left == 0){
			Printer printer = new Printer(inverted_index);
			printer.print();
	
			System.exit(0);
		}
	}

}

// indexes the word/filename/line_number of an IndexingJob taken from the buffer
class Reducer extends Thread{
	private static final int BUFFER_SIZE = 10;
	
	private LinkedList<IndexingJob> buffer = new LinkedList<IndexingJob>();
	
	private JobsBuffer jobs_buffer = new JobsBuffer(BUFFER_SIZE);
	
	private Semaphore lock = new Semaphore(1);
	private Semaphore full = new Semaphore(BUFFER_SIZE);
	private Semaphore empty = new Semaphore(0);
	
	public void run(){
		while(true){
			IndexingJob job = nextIndexingJob();
			
			String word = job.getWord();
			String name_of_file_where_word_occured = job.getFileName();
			int line_number_in_file_where_word_occured = job.getLineNumber();
			
			Index.addWordToInvertedIndex(word, name_of_file_where_word_occured, line_number_in_file_where_word_occured);
		}
	}
	
	public Semaphor getLockSemaphor(){
		return lock;	
	}
	public Semaphor getFullSemaphor(){
		return full;	
	}
	public Semaphor getEmptySemaphor(){
		return empty;	
	}
	public JobsBuffer getJobsBuffer(){
		return jobs_buffer;	
	}
	
	// called by Mapper objects
	private void nextIndexingJob(){
		empty.acquire();
		lock.acquire();
		
		IndexingJob job = jobs_buffer.getJob();
		
		lock.release();
		full.release();	
		
		return job;
	}
}

class Mapper extends Thread{
	private String filename;
	private int k; // number of reducer threads. this lets us randomly send a "job" to a reducer thread.
	
	public Mapper(String filename, int k){
		this.filename = filename;	
		this.k = k;
	}
	
	public void run(){
		try{
			BufferedReader text_file_reader = new BufferedReader(new FileReader(new File(filename)));
			
			String line_of_text; int current_line_number = 1;
			while((line_of_text = text_file_reader.readLine()) != null){
				parseLineAndSendToReducers(line_of_text, current_line_number);
				
				// increment the line number
				current_line_number++;
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
	
	private void parseLineAndSendToReducers(String line_of_text, int line_number){
		String[] words_in_current_line = line_of_text.replaceAll("[^A-Za-z0-9 ]","").toLowerCase().split("\\s+"); // array of all words in the current line (minus non-alphanumeric characters and whitespaces)
				
		for(int i = 0; i < words.length; i++){
			// (words[i].hashCode() % k) sometimes returns a negative integer. so, we add k, then mod by k to get a positive integer equivalent mod k.
			int random_reducer_id = ((words[i].hashCode() % k) + k) % k
			
			Reducer random_reducer = Index.reducers.get(random_reducer_id);
			
			sendJobToReducer(new IndexingJob(words_in_current_line[i], filename, line_number), random_reducer);	
		}
	}
	
	private void sendJobToReducer(IndexingJob job, Reducer reducer){
		reducer.getFullSemaphor().acquire(); // ask the full semaphor if we can add a job to the buffer
		reducer.getLockSemaphor().acquire(); // acquire the lock to opperate on the jobs buffer
		
		reducer.getJobsBuffer().addJob(job);
		
		reducer.getLockSemaphor().release(); // release the lock to operate on the jobs buffer
		reducer.getEmptySemaphor().release(); // tell the empty semaphor that a space in the buffer opened up.	
	}
}

// used by Reducer and Mapper to buffer a Struct containing word, file name, and line number infomration.
class IndexingJob{
	private String word;
	private String filename;
	private int line_number;
	
	public IndexingJob(String word, String filename, int line_number){
		this.word = word;
		this.filename = filename;
		this.line_number = line_number;
	}
	
	public String getWord(){
		return word;	
	}
	
	public String getFileName(){
		return filename;	
	}
	
	public int getLineNumber(){
		return line_number;	
	}
}

// this buffer is NOT thead safe to add/remove jobs. synchronization problems must be handled outside of this class!
class JobsBuffer{
	IndexingJob[] jobs_buffer;
	private long jobs_buffer_head_pointer;
	private long jobs_buffer_length;
	
	public JobsBuffer(int size){
		jobs_buffer = new IndexJob[size];
		jobs_buffer_head_pointer = 0;
		jobs_buffer_length = 0;
	}
	
	// by calling this, we assume you've checked to make sure the buffer isn't full
	public void addJob(IndexJob new_job){
		jobs_buffer[(jobs_buffer_head_pointer + jobs_buffer_length) % 10] = new_job;	
		jobs_buffer_lenght++;
	}
	
	// by calling this, we assumed you've checked to make sure the buffer has at least one job in it;
	public IndexingJob getJob(){
		IndexJob next_job = jobs_buffer[(jobs_buffer_head_pointer) % 10];
		
		jobs_buffer_head_pointer++;
		jobs_buffer_length--;
		
		return next_job;
	}
}

// used by Index to print out the inverted index
class Printer{
	private ConcurrentHashMap<String, ConcurrentHashMap<String, ArrayList<Integer>>> inverted_index;
	
	public Printer(inverted_index){
		this.inverted_index = inverted_index;
	}
	
	public void print(){
		ArrayList<HashMap<String, ArrayList<Integer>>> words = new ArrayList<HashMap<String, ArrayList<Integer>>>(inverted_index.keys()); // list of all the words in all text files
		Collections.sort(words); // sort those words in alphabetical order
		
		// iterate over the sorted words. we will print out 1 line for each word.
		for(int i = 0; i < words.size(); i++){
			System.out.print(words.get(i)); // prints the word as the first thing in this line
			
			printFileNamesAndLineNumbers(inverted_index.get(words.get(i));
			
			System.out.println(); // return to the next line
		}
	}	
	
		
	private void printFileNamesAndLineNumbers(ConcurrentHashMap<String, ArrayList<Integer>> file_name_to_line_numbers){
		Object[] file_names_list = file_name_to_line_numbers.keySet().toArray();
		Arrays.sort(file_names_list)
		
		// iterate over each filename for this word.
		for(int j = 0; j < file_names_list.length; j++){
			System.out.print(" " + (String)(file_names_list[j]) + "@"); // prints a space then the filename. it will be followed by the line numbers
			
			printLineNumbers(file_name_to_line_numbers.get((String)(file_names_list[j])));
		}	
	}
	
	private void printLineNumbers(ArrayList<Integer> line_numbers){
		Collections.sort(line_numbers); // sort it numerically
			
		System.out.print(line_numbers.get(0)); // the first line number is unique. we just print out the line number
		for(int l = 1; l < line_numbers.size(); l++){
			System.out.print(line_numbers.get(l)); // the following line numbers need a comma (",") printed before it.
		}
	}
}