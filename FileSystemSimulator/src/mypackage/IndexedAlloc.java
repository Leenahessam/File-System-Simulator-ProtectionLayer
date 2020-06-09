package mypackage;

import java.util.Vector;

public class IndexedAlloc {
    public int index;
    public String path;
    public Vector<Integer> blocks = new Vector<>();
    public Vector<String> dir = new Vector<>();
}