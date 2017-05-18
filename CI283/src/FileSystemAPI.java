/**
 * API calls: open, creat, read, write, unlink.
 */

class FileSystemAPI extends FileSystem_BASE_API
{
  /**
   * Open a file for read/ write
   * <BR><FONT COLOR="red">YOU NEED TO ADD CODE</FONT>
   * @return a fd to the file or (error C.EACCES, C.ENFILE)
   */

// ------------------------------------------------------------------ 
// ***** FILL IN CODE *********************************************** 
// ------------------------------------------------------------------ 
//  Open a file for reading 
//  Needs to 
//   Find if file exists 
//   Return fd (File descriptor) to open file 
//     flags = O_CREATE, O_WRONLY, O_RDONLY 
// ------------------------------------------------------------------ 

	public int open(String name, int flags, int mode) {
		DEBUG.trace("<<< open     : open(\"%s\",%04o,%04o)", name, flags, mode);
		
		int res = C.EACCES;	
		//System.out.printf("mode: %d, flags: %d\n", flags, mode);
		if ((flags & C.O_CREAT) == C.O_CREAT) {
			return creat(name, mode);
		} else {
			for (int i = 0; i < C.DIR_ENTRIES; i++) {
				Directory de = readEntryDir(i);
				
				if (de.getName().equals(name)) {
					
					int fd = allocateFD();
					
					if (fd == C.NO_ITEM) {
						return C.ENFILE;
					}
						
				    openTable[fd].dirPos = i;                   // Remember directory pos 
				    openTable[fd].bytes  = de.getBytes();       // Bytes in file (total)
				    openTable[fd].curPos = 0;                   // Current pos in file 
				    openTable[fd].firstBlock= de.getStart();    // Start block not allocated
				    openTable[fd].curBlock  = de.getStart();    //  so no current block 

					res = fd;
				} 
			}
			
		}
		return res;
	}


  /**
   * Create an empty file in the filing system
   * @return a fd to the file or (error C.EOVERFLOW, C.ENFILE)
   */

// ------------------------------------------------------------------ 
// Create a new file to write to 
//  Create entry for file in master directory 
//  Create an entry in the open table for file 
//   setting contents to show empty file 
//  Return fd (File descriptor) to new file 
// ------------------------------------------------------------------ 

  public int creat( String name, int mode  )
  {
    DEBUG.trace("<<< creat    : creat(\"%s\",%04o)", name, mode);
    int res = C.EOVERFLOW;                 // No room in directory 
    for ( int i=0; i<C.DIR_ENTRIES; i++ )
    {
     Directory de = readEntryDir( i );     // Read  
     if ( de.getName().length() == 0 )     // ? is free 
     {
       int fd = allocateFD();              // Allocated a File Descriptor 
       if ( fd == C.NO_ITEM )
         return C.ENFILE;                  // Maximum no. of files open 
  
       de.setName( name );                 // File name 
       de.setStart( C.END );               // First block 
       de.setBytes( 0 );                   // Bytes in file 
       writeEntryDir( i, de );             // Update directory 
  
       openTable[fd].dirPos = i;           // Remember directory pos 
       openTable[fd].curPos = 0;           // Current pos in file 
       openTable[fd].bytes  = 0;           // Bytes in file (total)
       openTable[fd].firstBlock=C.FREE;    // Start block not allocated 
       openTable[fd].curBlock  =C.FREE;    //  so no current block 
       res = fd;                           // return File Descriptor 
       break;
     }
    }
    DEBUG.trace( ">>> creat    : result = %d", res );
    return res;                           // result 
  }

  /**
   * Write length bytes from buf[] to the file (associated with the fd)
   * @return the number of bytes written ( or C.ERROR )
   */

  public int write( int fd, byte buf[], int length )
  {
    DEBUG.trace("<<< write    : " );
    int res = length;
    for( int i=0; i<length; i++ )
    {
      int r = writeByte( fd, buf[i] );
      if ( r !=  C.OK ) 
      {
        res =  C.ERROR;
        break;
      }
    }  
 DEBUG.trace( "write %3d - %3d ", res, /*buf,*/ length );
    return res;
  }
  

  /**
   * Read upto length bytes from the file (associated with the fd) into buf[]
   * @return the number of bytes read ( or C.ERROR )
   */

  public int read( int fd, byte buf[], int length )
  {
    
    DEBUG.trace("<<< read     : " );
    int bytesRead = 0;
    for( int i=0; i<length; i++ )
    {
      int ch = readByte( fd );
      if ( ch == C.E_EOF ) break;
      if ( ch < 0 )
      {
        bytesRead = C.ERROR;
        break;
      }
      bytesRead++;
      buf[i] = (byte) ch;
    }
    DEBUG.trace( " read %3d - %3d ", bytesRead, /*buf,*/ length );
    DEBUG.trace( ">>> read" );
    return bytesRead;
  }

  /**
   * Close a file
   * @return  success (C.OK) or ( error C.ERROR )
   */

  public int close( int fd )
  {
    if ( fd >= C.MAX_OPEN_FILES || fd < 0 )
    {
      return C.ERROR;
    }

    int dirPos = openTable[fd].dirPos;

    DEBUG.trace( "<<< close    : close(%d) [%d]", fd, dirPos );
  
    Directory  de = readEntryDir( dirPos );

    de.setStart( openTable[fd].firstBlock );
    de.setBytes( openTable[fd].curPos );
    writeEntryDir( dirPos, de );
    
    freeFD( fd );
    DEBUG.trace( ">>> close    : res = %d", C.OK );
    return C.OK;
  }

  
  /**
   * Delete a file (unlink)
   * <BR><FONT COLOR="red">YOU NEED TO ADD CODE</FONT>
   * @return success ( C.OK ) or ( error C.EACCES )
   */

// ------------------------------------------------------------------ 
// ***** FILL IN CODE *********************************************** 
// ------------------------------------------------------------------ 
// unlink (delete) a file 
// ------------------------------------------------------------------ 

  public int unlink( String name )                 // Delete file 
  {
	int res = C.EACCES; 
	DEBUG.trace("<<< unlink   : unlink(\"%s\")", name );  
	DEBUG.trace("<<< unlink    : res = %d", res);
	
	for (int i = 0; i< C.DIR_ENTRIES; i++) {
		Directory de = readEntryDir(i);
		
		if (de.getName().equals(name)) {

			int startBlock = de.getStart();

			de.setName("");
			de.setBytes(0);
			de.setStart(0);
			writeEntryDir(i, de);
			
			//check further entries
			while (startBlock != C.END) {
				int next = readEntryFAT(startBlock);
				writeEntryFAT(startBlock, C.FREE) ;
				startBlock = next;
				res = C.OK;
				break;
			}
		}
	} 
    return res;
  }


// ------------------------------------------------------------------ 
// ***** FILL IN CODE *********************************************** 
// ------------------------------------------------------------------ 
// Write single character to file 
//   Using file descriptor add character to file 
//   Need to (1 of below): 
//     Allocate next block in chain 
//     Read existing data block 
//   Update open file table entry 
//   Add byte to disk block 
//   Write updated disk block back to disk 
// ------------------------------------------------------------------ 

  /**
   * Write a single byte to the file
   * <BR><FONT COLOR="red">YOU NEED TO ADD CODE</FONT>
   * @return success or any error
   */

  private int writeByte( int fd, byte ch )
  {   
	  int curPos = openTable[fd].curPos;
	  int blockPos = curPos % C.BLOCKSIZE;
	  
	  if (blockPos == 0) {
		  if (curPos == 0) {
			  int startBlock = getDiskBlock();
			  openTable[fd].curBlock = startBlock;
			  openTable[fd].firstBlock = startBlock;	  
		  } else {
			  int next = getDiskBlock();
			  
			  if (next == C.NO_ITEM) {
					return C.NO_ITEM;
			  }
			  
			  writeEntryFAT(openTable[fd].curBlock, next);
			  openTable[fd].curBlock = next;	  
		  }
	  }	  
	  
	  Block b = read(C.DATA_START + openTable[fd].curBlock) ;
	  b.setByte(blockPos, ch);
	  
	  openTable[fd].curPos++;
	  write(b, C.DATA_START + openTable[fd].curBlock);
	  
	  return C.OK;
  }

// ------------------------------------------------------------------ 
// ***** FILL IN CODE *********************************************** 
// ------------------------------------------------------------------ 
// Read single character from file 
//   Using file descriptor  
//   Read data block containing character 
//   Get character from disk block 
//   Update open file table entry 
// ------------------------------------------------------------------ 

  /**
   * Read a single byte from the file
   * <BR><FONT COLOR="red">YOU NEED TO ADD CODE</FONT>
   * @return success or any error
   */

  private int readByte( int fd )
  {
	  byte c = 0;
	  int curPos = openTable[fd].curPos;
	  
	  if (curPos >= openTable[fd].bytes) {
		 return C.E_EOF;
	  }   
	  
	  if (curPos % C.BLOCKSIZE == 0) {
		  if (curPos > 0) {
			  openTable[fd].curBlock = readEntryFAT(openTable[fd].curBlock);			  
		  }
	  }
	  
	  Block b = read(C.DATA_START + openTable[fd].curBlock);
	  c = b.getByte(curPos % C.BLOCKSIZE);
	  openTable[fd].curPos++;

	  return c;
  }


}