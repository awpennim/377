


class FileSystem
{
	private char[] diskName;
	

  public FileSystem(char diskName[]) // used to say "char diskname[16]"
{
	this.diskName = diskName;
   // open the file with the above name
   // this file will act as the "disk" for your file system

}

public boolean validFileSystem(){
	if(new String(diskName).equals("disk0"))
		return true;
		
	return false;
}

public String toString(){
	return new String(diskName);	
}

/*
public int create(char name[8], int size)
{ //create a file with this name and this size

  // high level pseudo code for creating a new file

  // Step 1: check to see if we have sufficient free space on disk by
  // reading in the free block list. To do this:
  // move the file pointer to the start of the disk file.
  // Read the first 128 bytes (the free/in-use block information)
  // Scan the list to make sure you have sufficient free blocks to
  // allocate a new file of this size

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

} // End Create



public int detete(char name[8])
{
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

} // End Delete


public int ls(void)
{ 
  // List names of all files on disk

  // Step 1: read in each inode and print!
  // Move file pointer to the position of the 1st inode (129th byte)
  // for(i=0;i<16;i++)
    // REad in a inode
    // If the inode is in-use
      // print the "name" and "size" fields from the inode
 // end for

} // End ls

public int read(char name[8], int blockNum, char buf[1024])
{

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
    
} // End read


public int write(char name[8], int blockNum, char buf[1024])
{

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

    
} // end write
*/
}