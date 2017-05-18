/**
 * static class used to print fatal message then a dump of file system
 */

class FATAL
{
  private static FileSystem_BASE_API fs;
  private static boolean       processing = false;

  public static void setFileSystem( FileSystem_BASE_API afs )
  {
    fs = afs;
  }

  /**
   * Display a FATAL message and stop
   *  - Format the same as printf
   */

  public static void message(String fmt, Object... params )
  {
    DEBUG.set(true);
    System.out.print("FATAL - " );
    System.out.printf( fmt, params );
    System.out.println();
    if ( processing ) 
       System.exit(-1);             // Recursive call give up 

    processing = true;
    fs.printStateOfFileSystem();
    System.exit(-1);
  }

}
