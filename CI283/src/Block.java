/**
 * A block in the toy file system
 */

class Block
{
  private byte buffer[];

  /**
   * Constructor, create a zero filled block
   */

  public Block()
  {
    buffer = new byte[C.BLOCKSIZE];
    for ( int i=0; i<C.BLOCKSIZE; i++ )
     buffer[i]=0;
  }

  /**
   * Get contents of data blocks 
   * @return contents of block as byte array
   */


  public byte[] asByteArray()
  {
    return buffer;
  }

  /**
   * Set contents of the data block from the byte array
   *     which must be of size C.BLOCKSIZE
   */

  public void setFromByteArray( byte[] bytes )
  {
    buffer = bytes;
  }


  /**
   * Enter a byte into the data block
   */

  public void setByte( int pos, byte c )
  {
    buffer[pos] =  c;
  }

  /**
   * Get individual byte's from the data block 
   * @return contents of block as byte array
   */

  public byte getByte( int pos )
  {
    return (byte) buffer[pos];
  }

  // FAT entry 

  /**
   * Get individual FAT entry from the disk block 
   * @return contents of FAT entry at pos
   */

  public int getFATentry( int pos )
  {
    if ( pos >= C.FAT_ENTRIES || pos < 0 )
    {
      FATAL.message( "Block.getFATentry(%d)", pos );
    }
    int index = pos * 4;

    int value = ( ( (int)buffer[index+0] & 0xFF ) << 24) |
                ( ( (int)buffer[index+1] & 0xFF ) << 16) |
                ( ( (int)buffer[index+2] & 0xFF ) << 8)  |
                  ( (int)buffer[index+3] & 0xFF );
    return value;
  }

  /**
   * Set into the disk block a FAT entry at pos
   */

  public void setFATentry( int pos, int value )
  {
    if ( pos >= C.FAT_ENTRIES )
    {
      FATAL.message( "Block.putFATentry(%d)", pos );
    }
    int index = pos * 4;

    buffer[index+0] = (byte) ( (value>> 24 ) & 0xFF);
    buffer[index+1] = (byte) ( (value>> 16 ) & 0xFF);
    buffer[index+2] = (byte) ( (value>> 8  ) & 0xFF);
    buffer[index+3] = (byte) ( value & 0xFF );
  }

  // Directory entry 


  /**
   * Set Directory entry into the disk block at pos
   */

  public void setDIRentry( int pos, Directory de )
  {
    if ( pos >= C.DIR_ENTRIES )
    {
      FATAL.message( "Block.setDIRentry(%d)", pos );
    }
    int index = pos * C.DIR_SIZE_BYTES;

    String aName = de.getName() + "         ";
    for ( int i=0; i<C.DIR_NAME_SIZE; i++ ) 
      buffer[index+i] = (byte) aName.charAt(i);

    index = (pos*C.DIR_SIZE_BYTES + C.DIR_NAME_SIZE + 0)/4;
    setFATentry(  index, de.getStart() );
     
    index = (pos*C.DIR_SIZE_BYTES + C.DIR_NAME_SIZE + 4)/4;
    setFATentry(  index, de.getBytes() );
    
    index	  = (pos*C.DIR_SIZE_BYTES + C.DIR_NAME_SIZE + 8)/4;
    setFATentry(  index, de.getPermission() );
  }

  /**
   * Get from the disk block a Directory entry at pos
   */

  public Directory getDIRentry( int pos )
  {
    if ( pos >= C.DIR_ENTRIES )
    {
      FATAL.message( "Block.setDIRentry(%d)", pos );
    }
    int index = pos * C.DIR_SIZE_BYTES;

    String name = "";
    for ( int i=0; i<C.DIR_NAME_SIZE; i++ ) 
      name += (char) buffer[index+i];

    index     = (pos*C.DIR_SIZE_BYTES + C.DIR_NAME_SIZE + 0)/4;
    int start =  getFATentry(  index );

    index     = (pos*C.DIR_SIZE_BYTES + C.DIR_NAME_SIZE + 4)/4;
    int bytes = getFATentry(  index );
    
    index	  = (pos*C.DIR_SIZE_BYTES + C.DIR_NAME_SIZE + 8)/4;
    int permission = getFATentry(index);

    return new Directory( name.trim(), start, bytes, permission );
  }

}
