import java.awt.Color;
import java.util.Vector;
import java.util.Stack;

public class SeamCarver {
    private Picture pic;
    private int width;
    private int height;
    private Vector<Vector<Color>> buffer;
    
    public SeamCarver(Picture picture) {        
        this.width = picture.width();
        this.height = picture.height();
        buffer = new Vector<Vector<Color>>();
        for (int i = 0; i < picture.height(); i++) {
            Vector<Color> v = new Vector<Color>();
            for (int j = 0; j < picture.width(); j++) {
                Color o = picture.get(j, i);
                Color c = new Color(o.getRed(), o.getGreen(), o.getBlue());
                v.add(c);
            }
            buffer.add(v);
        }
    }
    
    public Picture picture() {                      // current picture
        pic = new Picture(this.width, this.height);
        for (int i = 0; i < this.height; i++) {
            Vector<Color> v = buffer.get(i);
            for (int j = 0; j < this.width; j++) {
                pic.set(j, i, v.get(j));
            }
        }
        return pic;
    }
    
    public int width() {                        // width of current picture
        return this.width;
    }
    
    public int height() {                       // height of current picture
        return this.height;
    }
    
    private double absquare(int a, int b) {
        return (a - b) * (a - b);
    }
    
    private double delt(Color a, Color b) {
        return absquare(a.getRed(), b.getRed()) + absquare(a.getGreen(), b.getGreen()) + absquare(a.getBlue(), b.getBlue());
    }
    
    private boolean checkX(int x) {
        if (x >= 0 && x <= this.width - 1) return true;
        //System.out.println("Illegal x value: " + x);
        return false;
    }
    
    private boolean checkY(int y) {
        if (y >= 0 && y <= this.height - 1) return true;
        else { 
            //System.out.println("Illegal y value: " + y + " with height of " + height);
            return false;
        }
    }
    
    public  double energy(int x, int y)
        throws java.lang.IndexOutOfBoundsException {           // energy of pixel at column x and row y
        if (checkX(x) == false || checkY(y) == false) throw new java.lang.IndexOutOfBoundsException();
        if (x == 0 || y == 0 || x == this.width - 1 || y == this.height - 1)
            return 195075; //255*255 + 255*255 + 255*255
        //caculate dual gradient energy
        Color left = this.buffer.get(y).get(x - 1);
        Color right = this.buffer.get(y).get(x + 1);
        Color up = this.buffer.get(y - 1).get(x);
        Color down = this.buffer.get(y + 1).get(x);
        double h = delt(left, right);
        double v = delt(up, down);
        return h + v;
    }
    
    private void relax(int v, int w, double[] distTo, int[] vecTo, IndexMinPQ<Double> pq) { 
        double eWeight = 0;
        if (w < this.width * this.height + 2 - 1 - this.width)
            eWeight = energy((w - 1) % this.width, (w - 1) / this.width);
        if (distTo[w] > distTo[v] + eWeight) {
            distTo[w] = distTo[v] + eWeight;
            vecTo[w] = v;
            if (pq.contains(w)) pq.change(w, distTo[w]);
            else                pq.insert(w, distTo[w]);
        }
    }
    
    private void transpose() {
        Vector<Vector<Color>> tb = new Vector<Vector<Color>>();
        for (int i = 0; i < this.width; i++) {
            Vector<Color> v = new Vector<Color>();
            for (int j = 0; j < this.height; j++) {
                v.add(buffer.get(j).get(i));
            }
            tb.add(v);
        }
        int tmp = this.width;
        this.width = height;
        this.height = tmp;
        this.buffer = tb;        
    }
    
    public int[] findHorizontalSeam() {           // sequence of indices for horizontal seam
        transpose();
        int[] res = findVerticalSeam();
        transpose();
        return res;
    }
    
    public int[] findVerticalSeam() {             // sequence of indices for vertical seam
        if (this.width == 1) { //only one possible vertical seam
            int[] seam = new int[this.height];
            for (int i = 0; i < this.height; i++) {
                seam[i] = i;
            }
            return seam;
        }
        
        int vNum = 2 + this.width * this.height;
        double[] distTo = new double[vNum];          // distTo[v] = distance  of shortest s->v path
        int[] vecTo = new int[vNum];    // edgeTo[v] = last edge on shortest s->v path
        IndexMinPQ<Double> pq;    // priority queue of vertices       
        
        for (int v = 0; v < vNum; v++)
            distTo[v] = Double.POSITIVE_INFINITY;
        distTo[0] = 0.0;

        // relax vertices in order of distance from s
        pq = new IndexMinPQ<Double>(vNum);
        pq.insert(0, distTo[0]);
        while (!pq.isEmpty()) {
            int v = pq.delMin();         
            //for (DirectedEdge e : G.adj(v))
            if (v == 0) {
                for (int i = 1; i <= this.width; i++) {                    
                    relax(0, i, distTo, vecTo, pq);
                }
            } else if (v >= vNum - 1 - this.width) {
                relax(v, vNum - 1, distTo, vecTo, pq);
            } else {
                relax(v, v + this.width, distTo, vecTo, pq);
                if (v % this.width == 1) {                    
                    relax(v, v + this.width + 1, distTo, vecTo, pq);
                    continue;
                } else if (v % this.width == 0) {
                    relax(v, v + this.width - 1, distTo, vecTo, pq);
                    continue;
                }
                relax(v, v + this.width - 1, distTo, vecTo, pq);
                relax(v, v + this.width + 1, distTo, vecTo, pq);
            }            
        }
        int p = vNum - 1;
        Stack<Integer> path = new Stack<Integer>(); 
        while (vecTo[p] != 0) {
            path.push(vecTo[p]);
            p = vecTo[p];
        }
        int index = 0;
        int[] res = new int[this.height];
        while (!path.empty()) {
            res[index++] = (path.pop() - 1) % this.width;
        }
        return res;
    }
    
    public void removeHorizontalSeam(int[] a) throws java.lang.IllegalArgumentException {  // remove horizontal seam from picture
        transpose();
        removeVerticalSeam(a);
        transpose();
        /*
        if (a.length != this.width || this.height <= 1) throw new java.lang.IllegalArgumentException();
        for (int i = 1; i < a.length; i++) {
            if (absquare(a[i - 1], a[i]) > 1) throw new java.lang.IllegalArgumentException();
        }
        
        for (int i = 0; i < a.length; i++) {
            //System.out.println();
            buffer.get(a[i]).remove(i);
        }
        this.height = this.height - 1;
        */
    }
    
    public void removeVerticalSeam(int[] a) {    // remove vertical seam from picture
        if (a.length != this.height || this.width <= 1) throw new java.lang.IllegalArgumentException();
        for (int i = 1; i < a.length; i++) {
            if (absquare(a[i - 1], a[i]) > 1) throw new java.lang.IllegalArgumentException();
        }
        
        for (int i = 0; i < this.height; i++) {
            buffer.get(i).remove(a[i]);
        }
        this.width = this.width - 1;
    }        
}