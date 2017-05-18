
/**
 * Contains: mount, umount, close, fdisk, read (a block) write (a block)
 */

import java.io.*;

class FileSystemBasicIO
{
  private RandomAccessFile fs;
  protected FD openTable[] = new FD[C.MAX_OPEN_FILES];

  /**
   * Mount a partition, simulated in a file
   */

  public boolean mount( String name )
  {
    try
    {
      fs = new RandomAccessFile(name,"rw"); 
    }
    catch ( Exception err )
    {
      FATAL.message( "Exception mount"  + err.getMessage() );
      return false;
    }
    return true;
  }

  /**
   * Un-mount a partition
   */

  public boolean umount()
  {
    try
    {
      for ( int i=0; i<C.MAX_OPEN_FILES; i++ )
      {
        if ( ! openTable[i].free )
          close(i);
      }
      fs.close(); 
      fs = null;
    }
    catch ( Exception err )
    {
      FATAL.message( "Exception umount"  + err.getMessage() );
      return false;
    }
    fs = null;
    return true;
  }

  /**
   * So will be overloaded by the real close
   *  but visible to FileSystem_BASIC_IO
   */

  public int close( int fd )
  {
    FATAL.message("FileSystemBasicIO.close(%d) :  " + 
                  " Should not be called", fd );
    return 0;
  }

  /**
   * Create a partition simulated in a file
   *  The partition is unformatted and must be formatted
   *  with a file system using mkfs.
   */

  public boolean fdisk( String name )
  {
    try
    {
      File file  = new File( name );
      FileOutputStream ostream = new FileOutputStream( file );
      PrintWriter pw           = new PrintWriter( ostream );
      pw.print( "Empty" );
      pw.flush();
      ostream.close();
    }
    catch ( Exception err )
    {
      FATAL.message( "Exception create partition " +
                          err.getMessage() );
      return false;
    }
    return true;
  }

  /**
   * Read a block from the partition
   * @return the block read
   */

  public Block read( int bNumber )
  {
    Block block   = new Block();
    byte  bytes[] = new byte[ C.BLOCKSIZE ];

    try
    {
      fs.seek( bNumber * C.BLOCKSIZE );
      
      
      int res = fs.read(bytes); 
      if ( res != C.BLOCKSIZE )
      {
        FATAL.message( "read fail from block <%d> <%d>\n", 
                       bNumber, res );
      }
      block.setFromByteArray( bytes );
      return block;
    }
    catch ( Exception err )
    {
      FATAL.message( "Exception read: " + err.getMessage() );
    }
    return block;
  }

  /**
   * Write a block to the partition
   */

  public void write( Block block, int bNumber )
  {
    try
    {
      fs.seek( bNumber * C.BLOCKSIZE );
      byte bytes[] = block.asByteArray();
      
      fs.write(bytes); 
    }
    catch ( Exception err )
    {
      FATAL.message( "write fail to block <%d>\n", bNumber );
    }
  }
}

