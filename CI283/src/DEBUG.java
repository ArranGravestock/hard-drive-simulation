/**
 * Static class used to output debug informatoin
 */

class DEBUG
{
  public static boolean debug = false;

  /**
   * Set true/false to print debugging information 
   */

  public static boolean set( boolean state )
  {
    boolean oldState = debug;
    debug = state;
    return oldState;
  }

  /**
   * Display a text for debugging
   *   - Format the same as printf
   */

  public static void trace(String fmt, Object... params )
  {
    if ( debug )
    {
      System.out.printf( fmt, params );
      System.out.println();
    }
  }

  /**
   * Display a fatal message if assertion false
   *  - Format the same as printf
   */

  public static void assertTrue( boolean ok, String fmt, Object... params )
  {
    if ( ! ok )
    {
      debug = true;
      trace("ASSERTION failed");
      FATAL.message( fmt, params );
    }
  }
}
