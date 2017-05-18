/**
 * Test program for the Toy file system
 */

class Main
{
  public static void main( String args[] )
  {
    BasicTestOfAPI();
    test( "" );
    TestOfAPI(); 
  }

  public static byte c( int i )
  {
    char c = (char) i;
    if ( c < ' ' || c > '~' )
      c = '.';
    return (byte) c;
  }

  public static byte[] toByte( String s )
  {
    byte res[] = new byte[ s.length() ];
    for ( int i=0; i<s.length(); i++ )
     res[i] = (byte) s.charAt( i );
    return res;
  }


  public static String toString( byte buf[]  )
  {
    String s = "";
    for ( int i=0; i<buf.length; i++ )
      s += (char) buf[ i ];
    return s;
  }


  public static void test( String message )
  {
    if ( message.equals( "" ) )
    {
      System.out.println();
    } else {
      System.out.println("+++ Test " + message );
    }
  }

  public static void TestOfAPI()
  {
    FileSystemAPI S = new FileSystemAPI();
    S.mkfs( "sda1.dsk" );

    test( "Test mount FS" );
    S.mount( "sda1.dsk" );

    int fd, res;
    final int MAX_BUF = 100;
    byte buf[] = new byte[MAX_BUF];
    test("File system test");

    // ---------------------------------- 
  
    test("Create File_1 write a single character");
  
    fd = S.open( "File_1", C.O_WRONLY|C.O_CREAT, 0777 );
    DEBUG.assertTrue( fd >= 0, 
                 "open(\"File_1\" C.O_WRONLY|C.O_CREAT, 0777) -> %d", fd );
    res = S.write( fd, toByte("A"), 1 );
    DEBUG.assertTrue( res == 1, "write(fd, \"A\", 1 )  -> %d", res );
    res = S.close( fd );
    DEBUG.assertTrue( res == C.OK, "close(fd) -> %d", res );

    // ---------------------------------- 
  
    test("Open File_1 read a single character");
  
    fd = S.open( "File_1", C.O_RDONLY, 0 );
    DEBUG.assertTrue( fd >= 0, "S.open( \"File_1\", O_RDONLY, 0) -> %d", fd );
    res = S.read( fd, buf, 1 );
    DEBUG.assertTrue( res == 1, "S.read( fd, buf, 1 ) -> %d", res );
    DEBUG.assertTrue( buf[0] == 'A', 
                 "Read File_1 -> Expected %c got %c", 'A', (char) buf[0] );
    res = S.close( fd );
    DEBUG.assertTrue( res == C.OK, "S.close(fd)  -> %d", res );
  
    // ---------------------------------- 
  
    test("Create File_2 write MAX_BUF characters");
  
    for ( int i=0; i< MAX_BUF; i++ ) buf[i] = c(i);
  
    fd = S.open( "File_2", C.O_WRONLY|C.O_CREAT, 0777 );
    DEBUG.assertTrue( fd >= 0, 
                  "open(\"File_1\", O_WRONLY|O_CREAT, 0777) -> %d", fd );
    res = S.write( fd, buf, MAX_BUF );
    DEBUG.assertTrue( res == MAX_BUF, "write( fd, buf, MAX_BUF ) --> %d", res );
    res = S.close( fd );
    DEBUG.assertTrue( res == C.OK, "S.close( fd ) --> %d", res );
  
    // ---------------------------------- 
  
    test("Open File_2 read MAX_BUF characters");
  
    fd = S.open( "File_2", C.O_RDONLY, 0 );
    DEBUG.assertTrue( fd >= 0, "open(\"File_2\", C.O_RDONLY, 0) -> %d", fd );
  
    res = S.read( fd, buf, MAX_BUF );
    DEBUG.assertTrue( res == MAX_BUF, "S.read(fd, buf, MAX_BUF) -> %d", res );
    for ( int i=0; i<MAX_BUF; i++ )
    {
      DEBUG.assertTrue( buf[i] == c(i), 
                   "Read File_2 -> pos = %3d Expected %c got %c", 
                       i, c(i), (char) buf[i] );
    }
    res = S.close( fd );
    DEBUG.assertTrue( res == C.OK, "S.close(fd)  -> %d", res );
  
    // ---------------------------------- 
  
    test("Open File_2 read MAX_BUF characters check EOF");
  
    fd = S.open( "File_2", C.O_RDONLY, 0 );
    DEBUG.assertTrue( fd >= 0, "open(\"File_2\", C.O_RDONLY, 0) -> %d", fd );
  
    res = S.read( fd, buf, MAX_BUF );
    DEBUG.assertTrue( res == MAX_BUF, "S.read(fd, buf, MAX_BUF) -> %d", res );
    for ( int i=0; i<MAX_BUF; i++ )
    {
      DEBUG.assertTrue( buf[i] == c(i), 
                   "Read File_2 -> pos = %3d Expected %c got %c", 
                       i, (char) i, (char) buf[i] );
    }
    
    res = S.read( fd, buf, MAX_BUF );
    DEBUG.assertTrue( res == 0, "read( fd, buf, MAX_BUF ) -> %d", res );
    res = S.close( fd );
    DEBUG.assertTrue( res == C.OK, "close(fd)  -> %d", res );
  
    // ---------------------------------- 

    test("Create File_3 write characters till fill disk");
  
    for ( int i=0; i< MAX_BUF; i++ ) buf[i] = c(i);
  
    fd = S.open( "File_3", C.O_CREAT|C.O_WRONLY, 0 );
    DEBUG.assertTrue( fd >= 0, 
                 "S.open( \"File_3\", O_CREAT|O_WRONLY, 0) -> %d", fd );
    for (;;)
    {
      res = S.write( fd, buf, MAX_BUF );
      if ( res != MAX_BUF ) break;
    }
    res = S.write( fd, buf, MAX_BUF );
    DEBUG.assertTrue( res == C.ERROR, "File system should be FULL -> %d", res );
    res = S.close( fd );
    DEBUG.assertTrue( res == C.OK, "close(fd)  -> %d", res );
  
    // ---------------------------------- 
  
    test("Delete File_2");
  
    res = S.unlink( "File_2" );
    DEBUG.assertTrue( res == C.OK, "unlink(File_2) -> %d", res );
    fd = S.open( "File_2", C.O_RDONLY, 0 );
    DEBUG.assertTrue( fd < 0, 
                 "open(\"File_2\", O_RDONLY, 0) should not exist fd -> %d", fd);
  
    // ---------------------------------- 
  
    test("Delete File_3");
  
    res = S.unlink( "File_3" );
    DEBUG.assertTrue( res == C.OK, "unlink(File_3) -> %d", res );
    fd = S.open( "File_3", C.O_RDONLY, 0 );
    DEBUG.assertTrue( fd < 0, 
              "open( \"File_3\", O_RDONLY, 0 ) should not exist fd -> %d", fd);
  
    // ---------------------------------- 
  
    test("Open File_1 read a single character");
  
    fd = S.open( "File_1", C.O_RDONLY, 0 );
    DEBUG.assertTrue( fd >= 0, "open( \"File_1\", O_RDONLY, 0) -> %d", fd );
    res = S.read( fd, buf, 1 );
    DEBUG.assertTrue( res == 1, "read( fd, buf, 1 ) -> %d", res );
    DEBUG.assertTrue( buf[0] == 'A', 
                 "Read File_1 -> Expected %c got %c", 'A', (char) buf[0] );
    res = S.close( fd );
    DEBUG.assertTrue( res == C.OK, "close(fd)  -> %d", res );
    
    // ---------------------------------- 
  
    test("Create File_4 write MAX_BUF characters");
  
    for ( int i=0; i< MAX_BUF; i++ ) buf[i] = c(i);
  
    fd = S.open( "File_4", C.O_WRONLY|C.O_CREAT, 0777 );
    DEBUG.assertTrue( fd >= 0, 
                  "open(\"File_4\", O_WRONLY|O_CREAT, 0777) -> %d", fd );
    res = S.write( fd, buf, MAX_BUF );
    DEBUG.assertTrue( res == MAX_BUF, "write( fd, buf, MAX_BUF ) -> %d", res );
    res = S.close( fd );
    DEBUG.assertTrue( res == C.OK, "close( fd ) -> %d", res );
  
    // ---------------------------------- 
  
  
    test("Open File_4 read MAX_BUF characters");
  
    fd = S.open( "File_4", C.O_RDONLY, 0 );
    DEBUG.assertTrue( fd >= 0, "open(\"File_2\", O_RDONLY, 0) -> %d", fd );
  
    res = S.read( fd, buf, MAX_BUF );
    DEBUG.assertTrue( res == MAX_BUF, "read( fd, buf, MAX_BUF ) -> %d", res );
    for ( int i=0; i<MAX_BUF; i++ )
    {
      DEBUG.assertTrue( buf[i] == c(i), 
                   "Read File_4 -> pos = %3d Expected %c got %c", 
                       i, c(i), (char) buf[i] );
    }
    res = S.close( fd );
    DEBUG.assertTrue( res == C.OK, "close(fd)  -> %d", res );
  
  
    // ---------------------------------- 
  
    test("Delete File_1");
  
    res = S.unlink( "File_1" );
    DEBUG.assertTrue( res == C.OK, "unlink(File_2) -> %d", res );
    fd = S.open( "File_1", C.O_RDONLY, 0 );
    DEBUG.assertTrue( fd < 0, 
                 "open(\"File_2\", O_RDONLY, 0) should not exist fd -> %d", fd);
  
    // ---------------------------------- 
  
    test("Delete File_4");
  
    res = S.unlink( "File_4" );
    DEBUG.assertTrue( res == C.OK, "unlink(File_3) -> %d", res );
    fd = S.open( "File_4", C.O_RDONLY, 0 );
    DEBUG.assertTrue( fd < 0, 
                 ".open(\"File_3\", O_RDONLY, 0) should not exist fd -> %d",
                 fd);

    // ---------------------------------- 
  
    test("Create file");

    fd = S.open( "File_1", C.O_WRONLY|C.O_CREAT, 0777 );

    //S.printStateOfFileSystem(); 

    // ----------------------------------
       
    
  }

  public static void BasicTestOfAPI()
  {
    FileSystemAPI S = new FileSystemAPI();

    test("Test create partition" );
    S.fdisk( "sda1.dsk" );

    test("Test mount FS" );

    S.mount( "sda1.dsk" );

    Block b = new Block();
    for ( int i=0; i<10; i++ )
    {
       for ( int j=0; j<C.BLOCKSIZE; j++ )
         b.setByte( j, (byte) ('A' + i + 2) );
       S.write( b, i );
    }

    for ( int i=0; i<10; i++ )
    {
       b = S.read( i );
       for ( int j=0; j<C.BLOCKSIZE; j++ )
       {
         byte c = b.getByte( j );
         byte ans = (byte) ('A' + i + 2);
         if ( c != ans )
           FATAL.message( "Block read/write <%d> <%d> Block =%d pos =%d",
                          c, ans, i, j  );
       }
    }

    int value = -1;

    test("Test read/write FAT entry" );

    b.setFATentry( 7, value );
    int ans = b.getFATentry( 7 );
    if ( ans != value )
      FATAL.message( "<%d> <%d>", ans, value );

    for ( int i = 0; i<32; i++ )
    {
      value = 1 << i;
      b.setFATentry( 7, value );
      ans = b.getFATentry( 7 );
      if ( ans != value )
        FATAL.message( "<%d> <%d>", ans, value );
    }

    test("Test read/write Directory entries" );
    for ( int i=0; i< C.DIR_ENTRIES; i++ )
    {
      String name  = "mas" + i;
      int    start = 10 + i;
      int    bytes = 100 + i;
      int	 permission = C.O_RDONLY;
      Directory de = new Directory( name, start, bytes, permission );
      b.setDIRentry( i, de );
      de = b.getDIRentry( i );
    }
    for ( int i=0; i< C.DIR_ENTRIES; i++ )
    {
      String name  = "mas" + i;
      int    start = 10 + i;
      int    bytes = 100 + i;
      Directory de = b.getDIRentry( i );
      if ( ! name.equals( de.getName() ) )
         FATAL.message( "Directory name <%s> <%s>\n", 
                         name, de.getName() );
      if ( start != de.getStart() )
         FATAL.message( "Directory start <%d> <%d>\n", 
                        bytes, de.getStart() );
      if ( bytes != de.getBytes() )
         FATAL.message( "Directory bytes <%d> <%d>\n", 
                        bytes, de.getBytes() );
    }
  }
 
  public static void TestOfCharPos() {
	  
  }
  
//  private int readByte( int fd )
//      {
//     // get block
//     Block b = new Block();
//     // check curPos. if 64 then get next block from FAT.
//     if (openTable[fd].curPos == C.BLOCKSIZE) {
//     // reset curpos
//     openTable[fd].curPos = 0;
//     // go to fat table, get next block value
//     int bNext = readEntryFAT(openTable[fd].curBlock);
//     // update curblock
//     openTable[fd].curBlock = bNext;
//     }
//     // Catch EOF
//     if (openTable[fd].curBlock == C.END) {
//   return C.E_EOF;
//     }
//     b = read(openTable[fd].curBlock);
//     // get char
//     byte c = b.getByte(openTable[fd].curPos);
//     // update descriptor
//     openTable[fd].curPos++;
//     return c;
//      }
}
