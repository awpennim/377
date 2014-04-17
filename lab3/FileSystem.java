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

	public int create(byte name[], int size) throws IOException{
        byte[] freeBlockList = new byte[128];
        
        file.seek(0);
        file.read(freeBlockList);
        
        int counter = 0;
        for(int i = 0; i < 128; i++){
            if(freeBlockList[i] == 0)
                counter++;
        }
        if(counter < size){
            System.err.println("Not enough space on disk");
            return 1;
        }
        
        int indexOfFirstFreeInode = -1;
        byte[] inodeUsed = new byte[4];
        int used;
        for(int i = 0; i < 16; i++){
            file.seek((56 * i) + 52);
            
            file.read(inodeUsed);
        
            used = inodeUsed;
            
            if(used == 0){
                indexOfFirstFreeInode = i;
                break;
            }
        }
        if(i == -1){
            System.err.println("Too many files on disk");
            return 1;
        }
        

		// Step 1: check to see if we have sufficient free space on disk by
		// reading in the free block list. To do this:

        
		// allocate a new file of this size

		//TODO: implement


		// Step 2: we look  for a free inode om disk
		// Read in a inode
		// check the "used" field to see if it is free
		// If not, repeat the above two steps until you find a free inode
		// Set the "used" field to 1
		// Copy the filename to the "name" field
		// Copy the file size (in units of blocks) to the "size" field

		// Step 3: Allocate data blocks to the file
		// for(i=0;i<size;i++)
		// Scan the block list that you read in Step 1 for a free block
		// Once you find a free block, mark it as in-use (Set it to 1)
		// Set the blockPointer[i] field in the inode to this block number.
		// 
		// end for

		// Step 4: Write out the inode and free block list to disk
		//  Move the file pointer to the start of the file 
		// Write out the 128 byte free block list
		// Move the file pointer to the position on disk where this inode was stored
		// Write out the inode

		return 0; //success
	}

	public int delete(byte name[]) throws IOException{
		// Delete the file with this name

		// Step 1: Locate the inode for this file
		// Move the file pointer to the 1st inode (129th byte)
		// Read in a inode
		// If the iinode is free, repeat above step.
		// If the iinode is in use, check if the "name" field in the
		// inode matches the file we want to delete. IF not, read the next
		//  inode and repeat

		// Step 2: free blocks of the file being deleted
		// Read in the 128 byte free block list (move file pointer to start
		//of the disk and read in 128 bytes)
		// Free each block listed in the blockPointer fields as follows:
		// for(i=0;i< inode.size; i++) 
		// freeblockList[ inode.blockPointer[i] ] = 0;

		// Step 3: mark inode as free
		// Set the "used" field to 0.

		// Step 4: Write out the inode and free block list to disk
		//  Move the file pointer to the start of the file 
		// Write out the 128 byte free block list
		// Move the file pointer to the position on disk where this inode was stored
		// Write out the inode

		return 0; //success
	}

	public int ls() throws IOException{
		// List names of all files on disk

		// Step 1: read in each inode and print!
		// Move file pointer to the position of the 1st inode (129th byte)
		// for(i=0;i<16;i++)
		// REad in a inode
		// If the inode is in-use
		// print the "name" and "size" fields from the inode
		// end for

		return 0; //success
	}

	public int read(byte name[], int blockNum, byte buf[]) throws IOException{

		// read this block from this file

		// Step 1: locate the inode for this file
		// Move file pointer to the position of the 1st inode (129th byte)
		// Read in a inode
		// If the inode is in use, compare the "name" field with the above file
		// IF the file names don't match, repeat

		// Step 2: Read in the specified block
		// Check that blockNum < inode.size, else flag an error
		// Get the disk address of the specified block
		// That is, addr = inode.blockPointer[blockNum]
		// move the file pointer to the block location (i.e., to byte #
		//addr*1024 in the file)

		// Read in the block! => Read in 1024 bytes from this location
		//into the buffer "buf"
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