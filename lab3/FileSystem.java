import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.io.IOException;

class FileSystem
{
	private char[] diskName;
    private RandomAccessFile file;
    private boolean valid = true;

	public FileSystem(char diskName[]) // used to say "char diskname[16]"
	{
		this.diskName = diskName;
        
        try{
            this.file = new RandomAccessFile(new String(diskName), "rws");
        }catch(FileNotFoundException e){
            valid = false;
        }
	}

	public boolean validFileSystem(){
		return valid;
	}

	public String toString(){
		return new String(diskName);	
	}
	
	
	//TODO: Oh fuck. We need to convert byte[] stuff into sane (read: comparable) data
	// http://stackoverflow.com/questions/5616052/how-can-i-convert-a-4-byte-array-to-an-integer
	// why did we decide to write this in javaaaaaaa
	
	/**
	 * Allocate space for a new file on disc
	 */
	public int create(byte name[], int size) throws IOException{
		byte[] freeBlockList = new byte[128];
		int counter = 0;
		int indexOfFirstFreeInode = -1;
		int indexOfFirstFreeBlock = -1;
		byte[] inodeUsed = new byte[4];
		int used;

		file.seek(0); // Reset the file pointer to 0 just in case
		file.read(freeBlockList);
		for(int i = 0; i < 128; i++){
			if(freeBlockList[i] == 0 && indexOfFirstFreeBlock == -1) {
				indexOfFirstFreeBlock = i; // store the first free block index if one exists
				counter++;
			} else if (freeBlockList[i] == 0) {
				counter++;
			}
		}
		if(counter < size){ // Disc space check
			System.err.println("Not enough space on disk");
			return 1;
		}
		for(int i = 0; i < 16; i++){ // File count check
			file.seek((56 * i) + 52);
			file.read(inodeUsed);
			used = inodeUsed; // Also, store the first free inode index if one exists
			if(used == 0){
				indexOfFirstFreeInode = i;
				break;
			}
		}
		if(i == -1){
			System.err.println("Too many files on disk");
			return 1;
		}
                
        //TODO: Create a new file of the specified size // Presumably this will get read into the FS as a bytestream
        file.seek((56 * indexOfFirstFreeInode);
        file.write(name);
        file.seek(56 * indexOfFirstFreeInode + 16);
        file.write(size);
        file.seek(56 * indexOfFirstFreeInode + 52);
        file.write(1);
        file.seek(indexOfFirstFreeBlock);
        file.write(1);
        file.seek(56 * indexOfFirstFreeInode + 20); // This should be the first space in the blockPointers[] array
        file.write(indexOfFirstFreeBlock);
        //TODO: Is it possible to write to multiple blocks in a single pass? If so, we need to account for this.
        
        file.seek(1024 * (indexOfFirstFreeBlock + 1); // The block list is offset by 1
        file.write(); // Write the new file into the block
		return 0; //success
	}

	
	/**
	 * Remove this file from the disc
	 */
	public int delete(byte name[]) throws IOException{
		int currentInode = 129;
		byte[] currentFileName;
		file.seek(129);
		for (int i = 0; i < 16; i++) {
			file.read(currentFileName);
			// TODO: Replace the comparison stuff with something realistic
			if (new String(currentFileName).equals(new String(name)) { // Zero out the current inode;
				file.seek(currentInode);
				file.write(0); // name; This may need to be set to write 8 zeroes
				file.seek(currentInode + 16); // size
				file.write(0);
				file.seek(currentInode + 20); // blockPtr[]
				for (int j = 0; j < 32) {
					
					//TODO: read each block number from blockPtr[] and free it in the freeBlockList
					
					file.write(0); // write 32 bytes of zeroes
				}
				file.seek(currentInode + 52); // used
				file.write(0);
				break;
			}
			currentInode = currentInode + 56;
		}
		return 0; //success
	}

	
	/**
	 * List names of all files on disk
	 */
	public int ls() throws IOException{
		int currentInode = 129;
		byte[] currentFileName = new byte[8];
		byte[] currentFileSize = new byte[4];
		byte[] currentFileUse = new byte[4];
		String fileInfo = "";
		for (int i = 0; i < 16; i++) {
			file.seek(currentInode + 52); // used
			file.read(currentFileUse);
			if (currentFileUse != 0) { //TODO: do a proper comparison
				file.seek(currentInode);
				file.read(currentFileName);
				file.seek(currentInode + 4);
				file.read(currentFileSize);
				fileInfo += "Name: " + (new String(currentFileName)) + " Size: " + (new String(currentFileSize)); //TODO: print byte[]s properly
			}
			currentInode = currentInode + 56;
		}
		return 0; //success
	}

	
	/**
	 * read this block from this file
	 */
	public int read(byte name[], int blockNum, byte buf[]) throws IOException{
		int currentInode = 129;
		byte[] currentFileName;
		file.seek(129);
		for (int i = 0; i < 16; i++) {
			file.read(currentFileName);
			// TODO: Replace the comparison stuff with something realistic
			if (new String(currentFileName).equals(new String(name)) {
				file.seek(currentInode + 20); // blockPtr[]
				for (int j = 0; j < 32) {
					//TODO: read each block number from blockPtr[]
					// Step 2: Read in the specified block
					// Check that blockNum < inode.size, else flag an error
					// Get the disk address of the specified block
					// That is, addr = inode.blockPointer[blockNum]
					// move the file pointer to the block location (i.e., to byte #
					//addr*1024 in the file)
					// Read in the block! => Read in 1024 bytes from this location
					//into the buffer "buf"
				}
				break;
			}
			currentInode = currentInode + 56;
		}
		return 0; //successful
	}


	public int write(byte name[], int blockNum, byte buf[]) throws IOException{

		// write this block to this file

		// Step 1: locate the inode for this file
		// Move file pointer to the position of the 1st inode (129th byte)
		// Read in a inode
		// If the inode is in use, compare the "name" field with the above file
		// IF the file names don't match, repeat

		// Step 2: Write to the specified block
		// Check that blockNum < inode.size, else flag an error
		// Get the disk address of the specified block
		// That is, addr = inode.blockPointer[blockNum]
		// move the file pointer to the block location (i.e., byte # addr*1024)

		// Write the block! => Write 1024 bytes from the buffer "buff" to 
		//  this location

		return 0; //successful
	}
}