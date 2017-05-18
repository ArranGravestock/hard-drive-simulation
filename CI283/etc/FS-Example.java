class FSExample
{
    public static void main( String args[] )
    {
	FileSystemAPI S = new FileSystemAPI();
	S.fdisk( "sda1.dsk" );
	//Create partition
	S.mkfs( "sda1.dsk ");
	//Format partition
	S.mount( "sda1.dsk" );
	//Mount partition
	String msg = "Hello world";
	int fd = S.open( "file.txt", C.O_WRONLY|C.O_CREAT, 0777 );
	if ( fd < 0 )
	    {
		System.out.println( "Failed to create file" );
		System.exit(-1);
	    }
	S.write( fd, msg.getBytes(), msg.length() );
	S.close(fd );
	//Close file
	S.umount();
	//unmount partition
	S.mount( "sda1.dsk" );
	//Mount partition
	fd = S.open( "file.txt", C.O_RDONLY, 0 );
	if ( fd < 0 )
	    {
		System.out.println( "Failed to open file" );
		System.exit(-1);
	    }
	final int MAX_BUF = 1000;
	byte[] byteBuf= new byte[MAX_BUF];
	int len = S.read( fd, byteBuf, MAX_BUF );
	if ( len > 0 )
	    System.out.printf( "<%s>\n",
			       new String( byteBuf, 0, len ) );
	else
	    System.out.printf("Error <%d> from file\n", len );
	S.close(fd);
	//Close file
	S.umount();
	//unmount partition
    }
}
