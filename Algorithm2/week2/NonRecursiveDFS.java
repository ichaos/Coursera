/*
 * Nonrecursive depth-first search
 */
public class NonRecursiveDFS {
    private boolean[] marked;
    private int count;
    
    public NonRecursiveDFS(Graph g, int s) {
        marked = new boolean[g.V()];
        count = 0;
        dfs(g, s);
    }
    
    private void dfs(Graph g, int s) {
        Stack<int> sk = new Stack<int>();
        sk.push(s);
        marked[s] = true;
        while (!sk.empty()) {
            int top = sk.pop();
            count++;
            for (int w : g.adj(v)) {
                if (!marked[w]) {
                    sk.push(w);
                    marked[w] = true;
                }
            }
        }
    }
    
    public boolean marked(int v) {
        return marked[v];
    }
    
    public int count() {
        return count;
    }
    
    //test client
    public static void main(String[] args) {
    }
}