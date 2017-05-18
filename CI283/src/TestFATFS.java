import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class TestFATFS {

	FileSystemAPI S;

	@Before
	public void setUp() throws Exception {
		S = new FileSystemAPI();
	}
	
	@Test
	public void testMountDisk(){
		S.mount("sda.dsk");
	}
		
	@Test
	public void testOfAPI() {
		FileSystemAPI S = new FileSystemAPI();
	    S.mkfs( "sda1.dsk" );

	    //Test mount FS" );
	    S.mount( "sda1.dsk" );

	    int fd, res;
	    final int MAX_BUF = 100;
	    byte buf[] = new byte[MAX_BUF];
	    
	    //File system test

	    // ---------------------------------- 
	  
	    //Create File_1 write a single character
	  
	    fd = S.open( "File_1", C.O_WRONLY|C.O_CREAT, 0777 );
	    assertTrue("open(\"File_1\" C.O_WRONLY|C.O_CREAT, 0777) ->" + fd , fd >= 0);
	    res = S.write( fd, toByte("A"), 1 );
	    assertTrue("write(fd, \"A\", 1 )  -> " +res, res == 1);
	    res = S.close( fd );
	    assertTrue("close(fd) -> " + res , res == C.OK);

	    // ---------------------------------- 
	  
	    //Open File_1 read a single character
	  
	    fd = S.open( "File_1", C.O_RDONLY, 0 );
	    assertTrue("S.open( \"File_1\", O_RDONLY, 0) -> " + fd, fd >= 0);
	    res = S.read( fd, buf, 1 );
	    assertTrue("S.read( fd, buf, 1 ) -> " + res, res == 1);
	    assertTrue(String.format("Read File_1 -> Expected %c got %c", 'A', (char) buf[0]), buf[0] == 'A');
	    res = S.close( fd );
	    assertTrue("S.close(fd)  -> " + res, res == C.OK);
	  
	    // ---------------------------------- 
	  
	    //Create File_2 write MAX_BUF characters
	  
	    for ( int i=0; i< MAX_BUF; i++ ) buf[i] = c(i);
	  
	    fd = S.open( "File_2", C.O_WRONLY|C.O_CREAT, 0777 );
	    assertTrue("open(\"File_1\", O_WRONLY|O_CREAT, 0777) -> " + fd, fd >= 0);
	    res = S.write( fd, buf, MAX_BUF );
	    assertTrue("write( fd, buf, MAX_BUF ) --> " + res, res == MAX_BUF);
	    res = S.close( fd );
	    assertTrue("S.close( fd ) --> " + res, res == C.OK);
	  
	    // ---------------------------------- 
	  
	    //Open File_2 read MAX_BUF characters
	  
	    fd = S.open( "File_2", C.O_RDONLY, 0 );
	    assertTrue("open(\"File_2\", C.O_RDONLY, 0) -> " + fd, fd >= 0);
	  
	    res = S.read( fd, buf, MAX_BUF );
	    assertTrue("S.read(fd, buf, MAX_BUF) -> " + res, res == MAX_BUF);
	    for ( int i=0; i<MAX_BUF; i++ )
	    {
	      assertTrue(String.format("Read File_2 -> pos = %3d Expected %c got %c", 
	                       i, c(i), (char) buf[i]), buf[i] == c(i));
	    }
	    res = S.close( fd );
	    assertTrue("S.close(fd)  -> " + res, res == C.OK);
	  
	    // ---------------------------------- 
	  
	    //Open File_2 read MAX_BUF characters check EOF
	  
	    fd = S.open( "File_2", C.O_RDONLY, 0 );
	    assertTrue("open(\"File_2\", C.O_RDONLY, 0) -> " + fd, fd >= 0);
	  
	    res = S.read( fd, buf, MAX_BUF );
	    assertTrue("S.read(fd, buf, MAX_BUF) -> %d" + res, res == MAX_BUF);
	    for ( int i=0; i<MAX_BUF; i++ )
	    {
	      assertTrue(String.format("Read File_2 -> pos = %3d Expected %c got %c", 
	                       i, (char) i, (char) buf[i]), buf[i] == c(i));
	    }
	    
	    res = S.read( fd, buf, MAX_BUF );
	    assertTrue("read( fd, buf, MAX_BUF ) -> " + res, res == 0);
	    res = S.close( fd );
	    assertTrue("close(fd)  -> " + res, res == C.OK);
	  
	    // ---------------------------------- 

	    //Create File_3 write characters till fill disk
	  
	    for ( int i=0; i< MAX_BUF; i++ ) buf[i] = c(i);
	  
	    fd = S.open( "File_3", C.O_CREAT|C.O_WRONLY, 0 );
	    assertTrue("S.open( \"File_3\", O_CREAT|O_WRONLY, 0) -> " + fd, fd >= 0);
	    for (;;)
	    {
	      res = S.write( fd, buf, MAX_BUF );
	      if ( res != MAX_BUF ) break;
	    }
	    res = S.write( fd, buf, MAX_BUF );
	    assertTrue("File system should be FULL -> " + res, res == C.ERROR);
	    res = S.close( fd );
	    assertTrue("close(fd)  -> " + res, res == C.OK);
	  
	    // ---------------------------------- 
	  
	    //Delete File_2
	  
	    res = S.unlink( "File_2" );
	    assertTrue("unlink(File_2) -> " + res, res == C.OK);
	    fd = S.open( "File_2", C.O_RDONLY, 0 );
	    assertTrue("open(\"File_2\", O_RDONLY, 0) should not exist fd -> " + fd, fd < 0);
	  
	    // ---------------------------------- 
	  
	    //Delete File_3
	  
	    res = S.unlink( "File_3" );
	    assertTrue("unlink(File_3) -> " + res, res == C.OK);
	    fd = S.open( "File_3", C.O_RDONLY, 0 );
	    assertTrue("open( \"File_3\", O_RDONLY, 0 ) should not exist fd -> " + fd, fd < 0);
	  
	    // ---------------------------------- 
	  
	    //Open File_1 read a single character
	  
	    fd = S.open( "File_1", C.O_RDONLY, 0 );
	    assertTrue("open( \"File_1\", O_RDONLY, 0) -> " + fd, fd >= 0);
	    res = S.read( fd, buf, 1 );
	    assertTrue("read( fd, buf, 1 ) -> " + res, res == 1);
	    assertTrue(String.format("Read File_1 -> Expected %c got %c", 'A', (char) buf[0]), buf[0] == 'A');
	    res = S.close( fd );
	    assertTrue("close(fd)  -> " + res, res == C.OK);
	  
	  
	    // ---------------------------------- 
	  
	    //Create File_4 write MAX_BUF characters
	  
	    for ( int i=0; i< MAX_BUF; i++ ) buf[i] = c(i);
	  
	    fd = S.open( "File_4", C.O_WRONLY|C.O_CREAT, 0777 );
	    assertTrue("open(\"File_4\", O_WRONLY|O_CREAT, 0777) -> " + fd, fd >= 0);
	    res = S.write( fd, buf, MAX_BUF );
	    assertTrue("write( fd, buf, MAX_BUF ) -> " + res, res == MAX_BUF);
	    res = S.close( fd );
	    assertTrue("close( fd ) -> " + res, res == C.OK);
	  
	    // ---------------------------------- 
	  
	  
	    //Open File_4 read MAX_BUF characters
	  
	    fd = S.open( "File_4", C.O_RDONLY, 0 );
	    assertTrue("open(\"File_2\", O_RDONLY, 0) -> " + fd, fd >= 0);
	  
	    res = S.read( fd, buf, MAX_BUF );
	    assertTrue("read( fd, buf, MAX_BUF ) -> " + res, res == MAX_BUF);
	    for ( int i=0; i<MAX_BUF; i++ )
	    {
	      assertTrue(String.format("Read File_4 -> pos = %3d Expected %c got %c", 
	                       i, c(i), (char) buf[i]), buf[i] == c(i));
	    }
	    res = S.close( fd );
	    assertTrue("close(fd)  -> " + res, res == C.OK);
	  
	  
	    // ---------------------------------- 
	  
	    //Delete File_1
	  
	    res = S.unlink( "File_1" );
	    assertTrue("unlink(File_2) -> " + res, res == C.OK);
	    fd = S.open( "File_1", C.O_RDONLY, 0 );
	    assertTrue("open(\"File_2\", O_RDONLY, 0) should not exist fd -> " + fd, fd < 0);
	  
	    // ---------------------------------- 
	  
	    //Delete File_4
	  
	    res = S.unlink( "File_4" );
	    assertTrue("unlink(File_3) -> " + res, res == C.OK);
	    fd = S.open( "File_4", C.O_RDONLY, 0 );
	    assertTrue("open(\"File_3\", O_RDONLY, 0) should not exist fd -> " + fd, fd < 0);

	    // ---------------------------------- 
	  
	    //Create file

	    fd = S.open( "File_1", C.O_WRONLY|C.O_CREAT, 0777 );
	}

	@Test
	public void basicTestOfAPI() {

		//Test create partition
		S.fdisk("sda1.dsk");

		//Test mount FS

		S.mount("sda1.dsk");

		Block b = new Block();
		for (int i = 0; i < 10; i++) {
			for (int j = 0; j < C.BLOCKSIZE; j++)
				b.setByte(j, (byte) ('A' + i + 2));
			S.write(b, i);
		}

		for (int i = 0; i < 10; i++) {
			b = S.read(i);
			for (int j = 0; j < C.BLOCKSIZE; j++) {
				byte c = b.getByte(j);
				byte ans = (byte) ('A' + i + 2);
				if (c != ans)
					FATAL.message(
							"Block read/write <%d> <%d> Block =%d pos =%d", c,
							ans, i, j);
			}
		}

		int value = -1;

		//Test read/write FAT entry

		b.setFATentry(7, value);
		int ans = b.getFATentry(7);
		if (ans != value)
			FATAL.message("<%d> <%d>", ans, value);

		for (int i = 0; i < 32; i++) {
			value = 1 << i;
			b.setFATentry(7, value);
			ans = b.getFATentry(7);
			if (ans != value)
				FATAL.message("<%d> <%d>", ans, value);
		}

		//Test read/write Directory entries
		for (int i = 0; i < C.DIR_ENTRIES; i++) {
			String name = "mas" + i;
			int start = 10 + i;
			int bytes = 100 + i;
			int permission = C.O_RDONLY;
			Directory de = new Directory(name, start, bytes, permission );
			b.setDIRentry(i, de);
			de = b.getDIRentry(i);
		}
		for (int i = 0; i < C.DIR_ENTRIES; i++) {
			String name = "mas" + i;
			int start = 10 + i;
			int bytes = 100 + i;
			Directory de = b.getDIRentry(i);
			if (!name.equals(de.getName()))
				FATAL.message("Directory name <%s> <%s>\n", name, de.getName());
			if (start != de.getStart())
				FATAL.message("Directory start <%d> <%d>\n", bytes,
						de.getStart());
			if (bytes != de.getBytes())
				FATAL.message("Directory bytes <%d> <%d>\n", bytes,
						de.getBytes());
		}
	}
	
	public static byte c(int i) {
		char c = (char) i;
		if (c < ' ' || c > '~')
			c = '.';
		return (byte) c;
	}

	public static byte[] toByte(String s) {
		byte res[] = new byte[s.length()];
		for (int i = 0; i < s.length(); i++)
			res[i] = (byte) s.charAt(i);
		return res;
	}

	public static String toString(byte buf[]) {
		String s = "";
		for (int i = 0; i < buf.length; i++)
			s += (char) buf[i];
		return s;
	}

}
