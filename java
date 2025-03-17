import java.util.*;
import javax.media.opengl.*;
import javax.swing.*;
import com.sun.opengl.util.Animator;
import com.sun.opengl.util.GLUT;
import com.sun.opengl.util.texture.Texture;
import com.sun.opengl.util.texture.TextureIO;
import javax.media.opengl.glu.GLU;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

class RubikSolver extends JFrame implements KeyListener, GLEventListener {
    private static GL gl;
    private static GLU glu;
    private static GLUT glut;
    private static GLCanvas canvas;
    private static float rotarX = 0, rotarY = 0, rotarZ = 0;
    private static String initialState = "UUUUUUUUURRRRRRRRRFFFFFFFFFDDDDDDDDDLLLLLLLLLBBBBBBBBB";
    private static List<String> solution;
    private static int stepIndex = 0;

    public RubikSolver() {
        setTitle("Cubo de Rubik - A*");
        setSize(700, 600);
        setResizable(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        canvas = new GLCanvas();
        canvas.addGLEventListener(this);
        canvas.addKeyListener(this);
        getContentPane().add(canvas);
        
        Animator animator = new Animator(canvas);
        animator.start();
        
        solution = solveAStar(initialState);
    }

    static class CubeState {
        String state;
        int gCost;
        int hCost;
        CubeState parent;
        
        CubeState(String state, int gCost, int hCost, CubeState parent) {
            this.state = state;
            this.gCost = gCost;
            this.hCost = hCost;
            this.parent = parent;
        }
        
        int fCost() {
            return gCost + hCost;
        }
    }
    
    static int heuristic(String state) {
        int cost = 0;
        for (int i = 0; i < state.length(); i++) {
            if (state.charAt(i) != 'S') {
                cost++;
            }
        }
        return cost;
    }
    
    static List<String> generateMoves(String state) {
        List<String> nextStates = new ArrayList<>();
        nextStates.add(rotateFace(state, 'F'));
        nextStates.add(rotateFace(state, 'R'));
        nextStates.add(rotateFace(state, 'U'));
        return nextStates;
    }
    
    static String rotateFace(String state, char face) {
        char[] newState = state.toCharArray();
        return new String(newState);
    }
    
    static List<String> solveAStar(String initialState) {
        PriorityQueue<CubeState> openSet = new PriorityQueue<>(Comparator.comparingInt(CubeState::fCost));
        Set<String> closedSet = new HashSet<>();
        
        openSet.add(new CubeState(initialState, 0, heuristic(initialState), null));
        
        while (!openSet.isEmpty()) {
            CubeState current = openSet.poll();
            
            if (current.hCost == 0) {
                return reconstructPath(current);
            }
            
            closedSet.add(current.state);
            
            for (String nextState : generateMoves(current.state)) {
                if (closedSet.contains(nextState)) continue;
                
                int gCost = current.gCost + 1;
                int hCost = heuristic(nextState);
                CubeState nextCubeState = new CubeState(nextState, gCost, hCost, current);
                
                openSet.add(nextCubeState);
            }
        }
        return null;
    }
    
    static List<String> reconstructPath(CubeState cubeState) {
        List<String> path = new ArrayList<>();
        while (cubeState != null) {
            path.add(cubeState.state);
            cubeState = cubeState.parent;
        }
        Collections.reverse(path);
        return path;
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        gl = drawable.getGL();
        glu = new GLU();
        glut = new GLUT();
        gl.glEnable(GL.GL_DEPTH_TEST);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        gl = drawable.getGL();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
        gl.glTranslatef(0, 0, -5);
        gl.glRotatef(rotarX, 1f, 0f, 0f);
        gl.glRotatef(rotarY, 0f, 1f, 0f);
        gl.glRotatef(rotarZ, 0f, 0f, 1f);
        glut.glutSolidCube(2);
        if (solution != null && stepIndex < solution.size()) {
            initialState = solution.get(stepIndex++);
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {}

    @Override
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            stepIndex = 0;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
    @Override
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RubikSolver().setVisible(true));
    }
}
