package educanet;

import educanet.models.Square;
import educanet.utils.FileUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Random;

public class Game {

    public static String[] mazeInput = FileUtils.readFile("src/main/java/educanet/lvl1.txt").split("\n");
    public static int rows = mazeInput.length;
    public static int cols = mazeInput[0].length() - mazeInput[0].replace(";", "").length()+1;;
    //public static float squareSize = 1.0f/((float)size/2);
    public static Square[][] mazeArray = new Square[rows][cols];
    public static Square gradient;
    public static boolean goDown;
    public static float[] grd = {
            /*0.0f, 0.0f, 0.5f,
            0.0f, 0.5f, 0.0f,
            0.5f, 0.0f, 0.0f,
            0.5f, 0.5f, 0.0f,*/
            0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f,
    };
    public static void init(long window) {

        //System.out.println(squareSize);
        //System.out.println(mazeText);
        float[] white = {
                1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
                1.0f, 1.0f, 1.0f,
        };

        //float xCord = -1.0f;
        //float yCord = 1.0f-squareSize;

        float maze[][] = new float[rows][cols];

        for (int i = 0; i < rows; i++) {
            String[] vals = mazeInput[i].trim().split(";");
            for (int j = 0; j < cols; j++) {
                maze[i][j] = Float.parseFloat(vals[j]);
            }
        }
        gradient = gradient(grd);
        for(int y = 0; y<rows; y++) {
            for (int x = 0; x < cols; x++) {
                System.out.println(maze[y][0]);
                Square square;
                square = createSquare(maze[y][0], maze[y][1], maze[y][2], white);
                mazeArray[y][x] = square;
                //codePos++;


                //xCord += squareSize;
            }
            //xCord = -1.0f;
            //yCord -= squareSize;
        }


        Shaders.initShaders();
    }

    private static Square gradient(float[] color) {
        int[] indices = {
                0, 1, 3, // First triangle
                1, 2, 3 // Second triangle
        };

        float[] vertices = {
                1.0f, 1.0f, 0.0f, // 0 -> Top right
                1.0f, -1.0f, 0.0f, // 1 -> Bottom right
                -1.0f, -1.0f, 0.0f, // 2 -> Bottom left
                -1.0f, 1.0f, 0.0f, // 3 -> Top left
        };

        return new Square(vertices, indices, color);
    }

    private static Square createSquare(float x, float y, float size, float[] color) {
        int[] indices = {
                0, 1, 3, // First triangle
                1, 2, 3 // Second triangle
        };

        float[] vertices = {
                x + size, y, 0.0f, // 0 -> Top right
                x + size, y - size, 0.0f, // 1 -> Bottom right
                x, y - size, 0.0f, // 2 -> Bottom left
                x, y, 0.0f, // 3 -> Top left


        };
        float[] vertices2 = {
                0.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.0f,
                0.0f, 0.0f, 0.0f,
        };


        if(color[3] != 0.0f) return new Square(vertices, indices, color);
        else return new Square(vertices2, indices, color);

    }

    public static void render(long window) {
        GL33.glUseProgram(Shaders.shaderProgramId);

        GL33.glBindVertexArray(gradient.vaoId);
        GL33.glDrawElements(GL33.GL_TRIANGLES, gradient.indices.length, GL33.GL_UNSIGNED_INT, 0);
        // Draw using the glDrawElements function
        for(int y = 0; y<rows; y++) {
            for(int x = 0; x<cols; x++) {
                GL33.glBindVertexArray(mazeArray[y][x].vaoId);
                GL33.glDrawElements(GL33.GL_TRIANGLES, mazeArray[y][x].indices.length, GL33.GL_UNSIGNED_INT, 0);
            }
        }


    }

    public static void update(long window) {
        float color = 0.01f;
        Random rand = new Random();
        int i = rand.nextInt(12);
        //for(int i=0; i<grd.length; i+= (Math.random() * ((3 - 1) + 1)) + 1) {
            if(grd[i] >= 1.0f) goDown = true;
            if(grd[i] <= 0.0f) goDown = false;
            //if(grd[i] == 0 | grd[i] == 3 | grd[i] == 6 | grd[i] == 9) color = 0.03f;
            else color = 0.05f;
            if(goDown) grd[i] -= color;
            else grd[i] += color;
        //}
        /*System.out.println(grd[1]);
        if(grd[1] >= 1.0f) goDown = true;
        else if(grd[1] <= 0.0f) goDown = false;
        if(goDown) grd[1] -= 0.01f;
        else grd[1] += 0.01f;*/
        gradient = gradient(grd);

    }

}
