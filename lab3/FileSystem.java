import java.io.RandomAccessFile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

class FileSystem
{
	private char[] diskName;
    private RandomAccessFile file;
    private boolean valid = true;

	public FileSystem(char[] diskName)
	{
		this.diskName = diskName;
        
        try{
            new RandomAccessFile(new String(diskName), "r").close(); // this will throw FileNotFoundException if the file doesn't exist
            this.file = new RandomAccessFile(new String(diskName), "rws");
        }catch(Exception e){
            this.valid = false;
        }
	}

	public boolean isValidFileSystem(){
		return valid;
	}

	public String toString(){
		return new String(diskName);	
	}
	
	/**
	 * Allocate space for a new file on disc
	 */
	public int create(char[] name, int size) throws IOException{
        // checks uniqueness of filename
        if(Inode.findInode(name, file) != null){
            System.err.println("File already exists with that name");
            return 1;
        }
        
		// grabbing our freeBlockList
        byte[] freeBlockList = new byte[128];
        file.seek(0);
		file.read(freeBlockList); // read in our 128 long bytemap of our blocks
        
        // lets see how many free blocks we have using our bytemap
		int counter = 0;
        for(int i = 0; i < 128; i++){
			if(freeBlockList[i] == (byte)0x00) {
				counter++;
			}
		}
		if(counter < size){ // Disc space check
			System.err.println("Not enough space on disk");
			return 1;
		}
        
        // iterate through the inodes, find a free inode, populate the inode's used, filename, and size fields
        Inode allocatedInode = null;
        Inode currentInode;
        int currentInodeByteOffset;
        byte[] inodeByteBuffer = new byte[56];
		for(int currentInodeIndex = 0; currentInodeIndex < 16; currentInodeIndex++){
			currentInodeByteOffset = (128 + (56 * currentInodeIndex)); // this is the currentInode's offset
            
            file.seek(currentInodeByteOffset);
			file.read(inodeByteBuffer);
            currentInode = new Inode(inodeByteBuffer, currentInodeByteOffset);
            
            // if the inode is not being used
            if(currentInode.isUsed() == false){
                allocatedInode = currentInode;
                
                currentInode.setUsed(true);
                currentInode.setFileName(name);
                currentInode.setSize(size);
                
                file.seek(currentInodeByteOffset);
                file.write(currentInode.toBytes());
                
                break;
            }
		}
        // did we find a free inode?
		if(allocatedInode == null){
			System.err.println("Too many files on disk");
			return 1;
		}
        
        // find free blocks using our bytemap, set the blocks to used, then tell the Inode where those blocks are
        int numAllocatedBlocks = 0;
        for(int currentBlockIndex = 0; currentBlockIndex < 128; currentBlockIndex++){
            if(freeBlockList[currentBlockIndex] == (byte)0x00){
                freeBlockList[currentBlockIndex] = 0x01; // in our bytemap, mark this block as used
                
                allocatedInode.setBlockPtr(numAllocatedBlocks, currentBlockIndex);
                numAllocatedBlocks++;
            }
            
            if(numAllocatedBlocks == size)
                break;
        }
        
        file.seek(0);
        file.write(freeBlockList);
        
        file.seek(allocatedInode.getOffset());
        file.write(allocatedInode.toBytes());
        
        System.out.println("File successfully created!");
        
		return 0; //success
	}

	
	/**
	 * Remove this file from the disc
	 */
	public int delete(char[] name) throws IOException{
        // grabbing our freeBlockList
        byte[] freeBlockList = new byte[128];
        file.seek(0);
		file.read(freeBlockList); // read in our 128 long bytemap of our blocks
        
        Inode currentInode;
        int currentInodeByteOffset;
        
        byte[] inodeByteBuffer = new byte[56];
        
        // iterate over inodes, looking for a used inode with a matching filename
		for (int currentInodeIndex = 0; currentInodeIndex < 16; currentInodeIndex++) {
            currentInodeByteOffset = 128 + (currentInodeIndex * 56);  // account for bytemap (size 128)
            
            file.seek(currentInodeByteOffset);
			file.read(inodeByteBuffer);
            
            currentInode = new Inode(inodeByteBuffer, currentInodeByteOffset);
            
            // if the inode is used and has a matching filename
            if(currentInode.isUsed() && Arrays.equals(currentInode.getFileName(), name)){
                for(int j = 0; j < currentInode.getSize(); j++){
                    freeBlockList[currentInode.getBlockPtr(j)] = (byte)0x00;
                }
                
                currentInode.setUsed(false);
                
                file.seek(currentInodeByteOffset);
                file.write(currentInode.toBytes()); // write this freed inode to disk
                
                file.seek(0);
                file.write(freeBlockList); // we must update the freeBlockList because more blocks are now free
                
                System.out.println("File successfully deleted!");
                
                return 0; //success
            }
        }
                          
        System.out.println("Could not find file with that name");
        return 1;
	}

	
	/**
	 * List names of all files on disk
	 */
	public int ls() throws IOException{
		Inode[] allUsed;
        
        allUsed = Inode.getAllUsed(file);
        
        for(int i = 0; i < allUsed.length; i++){
            System.out.println(new String(allUsed[i].getFileName()) + " " + allUsed[i].getSize());
        }
        
        System.out.println("Files successfully listed!");
        
		return 0; //success
	}

	
	/**
	 * read this block from this file
	 */
	public int read(char[] name, int blockNum, byte[] buf) throws IOException{
		Inode currentInode;
        
        currentInode = Inode.findInode(name, file);
        
        if(currentInode == null){
            System.err.println("Could not find file with that name");
            return 1;
        }
        
        file.seek(currentInode.getBlockPtr(blockNum) * 1024);
        file.read(buf, 0 , 1024);
        
        System.out.println("File successfully read!");
        
		return 0; //successful
	}


	public int write(char[] name, int blockNum, byte[] buf) throws IOException{
        Inode currentInode;
        
        currentInode = Inode.findInode(name, file);

        if(currentInode == null){
            System.err.println("Could not find file with that name");
            return 1;
        }
        
        file.seek(currentInode.getBlockPtr(blockNum) * 1024);
        file.write(buf);

        System.out.println("File successfully written!");
        
		return 0; //successful
	}
}