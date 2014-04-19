import java.nio.ByteBuffer;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

class Inode{
    public static Inode[] getAllUsed(RandomAccessFile file) throws IOException{
        ArrayList<Inode> allUsed = new ArrayList<Inode>();
        Inode currentInode;
        
        // iterate through all our inodes, and add them to the list if they are in use
        int currentInodeByteOffset;
        byte[] inodeByteBuffer = new byte[56];
        for (int currentInodeIndex = 0; currentInodeIndex < 16; currentInodeIndex++) {
            currentInodeByteOffset = 128 + (currentInodeIndex * 56);  // account for bytemap (size 128)
            
            file.seek(currentInodeByteOffset);
			file.read(inodeByteBuffer);
            
            currentInode = new Inode(inodeByteBuffer, currentInodeByteOffset);
            
            if(currentInode.isUsed())
                allUsed.add(currentInode);
        }
        
        return allUsed.toArray(new Inode[allUsed.size()]); // idea from http://stackoverflow.com/questions/4042434/convert-arraylist-containing-strings-to-an-array-of-strings-in-java
    }
    
    public static Inode findInode(char[] name, RandomAccessFile file) throws IOException{
        Inode currentInode;
        
        // iterate through all our inodes, if they are in use and their filename matches then we return it
        int currentInodeByteOffset;
        byte[] inodeByteBuffer = new byte[56];
        for (int currentInodeIndex = 0; currentInodeIndex < 16; currentInodeIndex++) {
            currentInodeByteOffset = 128 + (currentInodeIndex * 56);  // account for bytemap (size 128)
            
            file.seek(currentInodeByteOffset);
			file.read(inodeByteBuffer);
            
            currentInode = new Inode(inodeByteBuffer, currentInodeByteOffset);
            
            if(currentInode.isUsed() && Arrays.equals(currentInode.getFileName(), name))
                return currentInode;
        }
        
        return null;
    }
    
    private char[] name;
    private byte[] nameAsBytes = new byte[16]; // used in toBytes();
    private int size;
    private byte[] sizeAsBytes = new byte[4]; // used in toBytes();
    private int[] blockPtrs = new int[8];
    private byte[] blockPtrsAsBytes = new byte[32]; // used in toBytes();
    private boolean used;
    private byte[] usedAsBytes = new byte[4]; // used in toBytes();
    
    private int offset; // Inode location (in bytes) in the file
    
    // bytes is of size 56
    public Inode(byte[] bytes, int offset){
        System.arraycopy(bytes, 0, nameAsBytes, 0, 16); // copy first 16 bytes from bytes to nameAsBytes
        System.arraycopy(bytes, 16, sizeAsBytes, 0, 4); // copy 17-20 (inclusive) bytes from bytes to sizeAsBytes
        System.arraycopy(bytes, 20, blockPtrsAsBytes, 0, 32); // copy 21-32 (inclusive) bytes from bytes to blockPtrsAsBytes
        System.arraycopy(bytes, 52, usedAsBytes, 0, 4); // copy 52-56 (inclusive) bytes from bytes to usedAsBytes
        
        // set the name
        try{
            this.name = Driver.fileNameToCharArray(new String(nameAsBytes, "UTF-16"));
        }catch(UnsupportedEncodingException e){
            System.err.println("System doesn't support UTF-16. Now Terminating.");
            System.exit(1);
        }
            
        // set the size
        this.size = ByteBuffer.wrap(sizeAsBytes).getInt();
        
        // set the blockPtrs
        for(int i = 0; i < size; i++){
            this.blockPtrs[i] =
            ByteBuffer.wrap(blockPtrsAsBytes, i * 4, 4).getInt();
        }
        
        // set the used
        this.used = (ByteBuffer.wrap(usedAsBytes).getInt() != 0);
    }
    
    public int getOffset(){
        return offset;
    }
    
    public int getBlockPtr(int index){
        return blockPtrs[index];
    }
    
    public void setBlockPtr(int index, int val){
        blockPtrs[index] = val;
        
        for(int i = 0; i < 8; i++){
            byte[] intAsBytes = ByteBuffer.allocate(4).putInt(blockPtrs[index]).array();
            
            for(int j = 0; j < 4; j++){
                blockPtrsAsBytes[(i * 4) + j] =
                    intAsBytes[j];
            }
        }
    }
    
    public char[] getFileName(){
        return this.name;
    }
    
    public void setFileName(char[] newName){
        this.name = newName;
        
        try{
            this.nameAsBytes = new String(newName).getBytes("UTF-16");
        }catch(UnsupportedEncodingException e){
            System.err.println("System doesn't support UTF-16. Now Terminating.");
            System.exit(1);
        }
    }
    
    public int getSize(){
        return this.size;
    }
    
    public void setSize(int val){
        this.size = val;
        
        this.sizeAsBytes = ByteBuffer.allocate(4).putInt(val).array();
    }
    
    public boolean isUsed(){
        return this.used;
    }
    
    public void setUsed(boolean val){
        if(val){
            this.used = true;
            this.usedAsBytes = ByteBuffer.allocate(4).putInt(1).array();
        }
        else{
            this.used = false;
            this.usedAsBytes = ByteBuffer.allocate(4).putInt(0).array();
        }
    }
    
    public byte[] toBytes(){
        byte[] returnBytes = new byte[56];
        
        System.arraycopy(nameAsBytes, 0, returnBytes, 0, 16);
        System.arraycopy(sizeAsBytes, 0, returnBytes, 16, 4);
        System.arraycopy(blockPtrsAsBytes, 0, returnBytes, 20, 32);
        System.arraycopy(usedAsBytes, 0, returnBytes, 52, 4);
        
        return returnBytes;
    }
}