/**
 * The entry in the openTable used to describe an open file
 */

class FD
{
  public int     firstBlock=0;    //  First block of a file 
  public int     curBlock  =0;    //  Current block read/write 
  public int     curPos    =0;    //  Current position in file 
  public int     bytes     =0;    //  Bytes in the file 
  public int     dirPos    =0;    //  Directory entry position 
  public boolean free      =true; //  FD free 
}
