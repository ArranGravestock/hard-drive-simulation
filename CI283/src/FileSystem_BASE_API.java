/**
 * Low level toy file system code
 */

class FileSystem_BASE_API extends FileSystemBasicIO
{


  public FileSystem_BASE_API()
  {
    FATAL.setFileSystem( this );            // So can acccess 

    for ( int i=0; i<C.MAX_OPEN_FILES; i++ )  // Clear the openTable 
      openTable[i] = new FD();
  }

  /**
   * Make a file system destroying any previous data on the partition
   */

  public void mkfs( String name )
  {
    mount( name );             // Opens simulated disk so can format 
    Block b = new Block();

    // Need to create all the blocks in the partition first 

    final int BLOCKS_IN_PARTITION =
                C.FAT_BLOCKS + C.DIR_BLOCKS + C.TOTAL_FAT_ENTRIES;

    for (int i=0; i<BLOCKS_IN_PARTITION; i++ )
    {
      write( b, i );
    }


    // Set the FAT to free 

    for (int i=0; i<C.TOTAL_FAT_ENTRIES; i++ )
    {
      writeEntryFAT( i, C.FREE );
    }

    //  Set all directory entries to free 

    Directory de = new Directory( "", 0, 0, C.O_RDONLY );

    for (int i=0; i<C.TOTAL_DIR_ENTRIES; i++ )
    {
      writeEntryDir( i,  de );
    }

    // Fill disk blocks with the character . 
    // There can only be TOTAL_FAT_ENTRIES blocks 

    for ( int i=0; i<C.BLOCKSIZE; i++ )
    {
      b.setByte(i, (byte) '.');
    }

    for (int i=0; i<C.TOTAL_FAT_ENTRIES; i++ )
    {
      write( b, C.DATA_START+i );
    }
    umount();                  // closes simulated disk 
  }

  /**
   * Allocate a new (free) disk block to use
   * The block is found by looking for a free block in the FAT
   * @return disk block number or any error
   */

  protected int getDiskBlock()
  {
    int res = C.NO_ITEM;
    
    boolean ov = DEBUG.set(false);
    Block b = read( C.FAT_START );           // get FAT 
    for (int i=0; i<C.FAT_ENTRIES; i++ )
    {
      if ( b.getFATentry(i)==C.FREE )        // Found free disk block 
      {
        b.setFATentry(i, C.END );            // Mark as used (end of chain) 
        write( b, C.FAT_START );             // update FAT 
        res = i;
        break;
      }
    }
    DEBUG.set( ov );
    DEBUG.trace( " GetDiskBlock: res = %3d", res );
    return res;
  }


  /**
   * DEBUG 
   * Print the state of the file system
   * This may fail if the file system is corrupt.
   */

  public void printStateOfFileSystem()
  {
    System.out.printf("\n--- Dump of the state of file system ---\n" );
    System.out.printf( "Block size    FAT entries   Dir entries\n" );
    System.out.printf( "%-12d  %-12d  %-12d\n",
                        C.BLOCKSIZE, C.TOTAL_FAT_ENTRIES, 
                        C.TOTAL_DIR_ENTRIES);

    Block b;
    System.out.println("\n--- Contents of Open file table ---" );
    for (int i=0; i<C.MAX_OPEN_FILES; i++ )
    {
      if ( !openTable[i].free )
      {
        System.out.println( 
          "    State    First    Current Current  Size     Directory" );
        System.out.println(
          " fd          block    block   position in bytes position" );

        System.out.printf( "%3d%-8s %-8d %-8d %-8d %-8d %-8d\n", i,
                            (openTable[i].free ? " Free" : " Used") ,
                            openTable[i].firstBlock,
                            openTable[i].curBlock,
                            openTable[i].curPos,
                            openTable[i].bytes,
                    	    openTable[i].dirPos );
                           
      }
    }

    System.out.printf("\n\n--- Contents of FAT ---");
    b = read( C.FAT_START );
    for (int i=0; i<C.FAT_ENTRIES; i++ )
    {
      System.out.printf( i%5==0 ? "\n  " : " " );
	  System.out.printf( "[%3d] ", i );
      switch ( b.getFATentry(i) )
      {
        case C.FREE  : System.out.print("FREE "); break;
        case C.END   : System.out.print("END  "); break;
        default      : System.out.printf("%4d ", b.getFATentry(i) );
      }
    }
    System.out.printf("\n\n");
  
    b = read( C.DIR_START );

    System.out.printf("--- Contents of master directory ---\n");
    for (int i=0; i<C.DIR_ENTRIES; i++ )
    {
      Directory de = b.getDIRentry( i );
      System.out.printf("  (%2d) Name = [%-8s]", i, de.getName() );

      System.out.printf(" File size = %4d",     de.getBytes() );
      System.out.printf(" First block = %4d", de.getStart() );
      System.out.printf(" File permission type = %4d\n", de.getPermission());
    }
  }
  
  // ------------------------------------------------------------------ 
  // print contents of data block 
  // ------------------------------------------------------------------ 
  
  /**
   * print the contents of a data block
   */

  void dumpDataBlock( int block )
  {
    Block b  = read( C.DATA_START+block );
    System.out.printf(" Dump of data block %3d", block );
    for ( int j=0; j<C.BLOCKSIZE; j++ )
    {
      System.out.print( j%78==0 ? "\n" : "" );
      char c = (char)b.getByte(j);
      System.out.print( (c >= ' ' && c <= '~') ? c : ' ' );
    }
    System.out.println();
  }
  

  /**
   * Read a FAT entry from disk from position pos
   * @return the value in the FAT entry
   */

  public int readEntryFAT( int pos )
  {
    Block b;
    b = read( C.FAT_START );
    int res = b.getFATentry( pos );
    return res;
  }

  /**
   * Write a FAT entry back to disk at position pos with newValue
   */

  public void writeEntryFAT( int pos, int newValue )
  {
    Block b;
    b = read( C.FAT_START );
    b.setFATentry( pos, newValue );
    write( b, C.FAT_START );
  }

  // Build API 

  /**
   * Allocate a file descriptor (fd)
   * @return the fd or error ( C.NO_ITEM )
   */

  protected int allocateFD()
  {
    int res = C.NO_ITEM;
  
    boolean ov = DEBUG.set(false);
    for (int i=0; i<C.MAX_OPEN_FILES; i++ ) // For all fd's 
    {
     if ( openTable[i].free )            // Is free ? 
     {
       openTable[i].free = false;        // Marks as used 
       res = i;
       break;
     }
    }
    DEBUG.set( ov );
    DEBUG.trace( " Get File Des: res = %3d", res );
    return res;
  }

  /**
   * Mark the fd entry as free
   */

  protected void freeFD( int fd )
  {
    if ( fd >= C.MAX_OPEN_FILES || fd < 0 )
    {
      FATAL.message("freeFD : called with (%d)\n", fd );
    }
    openTable[ fd ].free = true;
  }


  /**
   * Read a directory entry from the disk
   * @return the directory entry
   */

  protected Directory readEntryDir( int pos )
  {
    if ( pos >= C.DIR_ENTRIES || pos < 0 )
    {
      FATAL.message("readEntryDir : called with (%d)\n", pos );
    }
    Block b = read( C.DIR_START );
    return b.getDIRentry(pos);
  }

  /**
   * Write a directory entry to the disk
   */
    
  protected void writeEntryDir( int pos, Directory de )
  {
    if ( pos >= C.DIR_ENTRIES || pos < 0 )
    {
      FATAL.message("WriteEntryDir : called with (%d)\n", pos );
    }

    boolean ov = DEBUG.set(false);
  
    Block b = read( C.DIR_START );
    DEBUG.set( ov );

    b.setDIRentry( pos, de );
    
    DEBUG.trace(
          "WriteEntryDir : Pos = %3d Name = [%-8s] Start = %3d Size = %4d",
            pos, de.getName(), de.getStart(), de.getBytes() );
  
    ov = DEBUG.set(false);
    write( b, C.DIR_START );
    DEBUG.set( ov );
  }


}
