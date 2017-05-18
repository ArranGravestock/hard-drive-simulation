/**
 * Global constants
 */

class C
{
  /** Size in bytes of a disk block */
  public static final int BLOCKSIZE   = 64;
  public static final int FAT_ENTRIES = BLOCKSIZE/4;

  public static final int FAT_BLOCKS= 1;
  public static final int TOTAL_FAT_ENTRIES = FAT_ENTRIES * FAT_BLOCKS;


  public static final int DIR_NAME_SIZE = 8;
  public static final int DIR_SIZE_BYTES = (DIR_NAME_SIZE + 4 + 4 );
  public static final int DIR_ENTRIES = BLOCKSIZE/DIR_SIZE_BYTES;

  public static final int DIR_BLOCKS= 1;
  public static final int TOTAL_DIR_ENTRIES = DIR_ENTRIES * DIR_BLOCKS;

  /** Offset of the FAT block */
  public static final int FAT_START  = 0;  //  First FAT block 
  /** Offset of the Directory block */
  public static final int DIR_START  = 1;  //  First block of master directory 
  /** Offset of the start of the data blocks */
  public static final int DATA_START = 2;  //  First data block 

  public static final int MAX_OPEN_FILES = 4; // Size of open table 

  public static final int FREE     = -1; //  FAT marker END of chain 
  public static final int END      = -2; //  FAT marker END of chain 
  public static final int NO_ITEM  = -3; //  No new disk block can be allocated 
  public static final int E_EOF    = -4; //  End Of File detected 
  public static final int EOVERFLOW= -6; // Overflow error 
  public static final int ENOSPC   = -7; // Overflow error 
  public static final int ENFILE   = -8; // End of file 
  public static final int EACCES   = -9; // Error access 

  
  public static final int OK       = 7;  //  All OK 
  public static final int ERROR    = -10;//  All not OK 

  public static final int O_WRONLY = 1;  // Write only 
  public static final int O_CREAT  = 2;  // Create 
  public static final int O_RDONLY = 4;  // Read only 

  public static final int SEEK_SET = 1000;
  public static final int SEEK_CUR = 1001;
  public static final int SEEK_END = 1002;
}
