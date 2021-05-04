package educanet;

import educanet.models.Square;
import educanet.utils.FileUtils;
import org.joml.Matrix4f;
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

    private static final float[] vertices = {
            0.3f, 0.0f, 0.0f, // 0 -> Top right
            0.3f, -0.3f, 0.0f, // 1 -> Bottom right
            0.0f, -0.3f, 0.0f, // 2 -> Bottom left
            0.0f, 0.0f, 0.0f, // 3 -> Top left
    };

    private static final float[] colors = {
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f,
    };

    private static final float[] colorsRed = {
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
            1.0f, 0.0f, 0.0f,
    };

    private static final int[] indices = {
            0, 1, 3, // First triangle
            1, 2, 3 // Second triangle
    };

    private static int squareVaoId;
    private static int squareVboId;
    private static int squareEboId;
    private static int colorsId;
    private static int uniformMatrixLocation;
    private static Matrix4f matrix = new Matrix4f()
            .identity()
            .translate(0.1f, 0.1f, 0.1f);
    // 4x4 -> FloatBuffer of size 16
    private static FloatBuffer matrixFloatBuffer = BufferUtils.createFloatBuffer(16);

    private static float xPlayer = vertices[9];
    private static float yPlayer = vertices[10];
    private static float xPlayer2 = vertices[3];
    private static float yPlayer2 = vertices[4];

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

        // Generate all the ids
        squareVaoId = GL33.glGenVertexArrays();
        squareVboId = GL33.glGenBuffers();
        squareEboId = GL33.glGenBuffers();
        colorsId = GL33.glGenBuffers();

        // Get uniform location
        uniformMatrixLocation = GL33.glGetUniformLocation(Shaders.shaderProgramId, "matrix");

        // Tell OpenGL we are currently using this object (vaoId)
        GL33.glBindVertexArray(squareVaoId);

        // Tell OpenGL we are currently writing to this buffer (eboId)
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, squareEboId);
        IntBuffer ib = BufferUtils.createIntBuffer(indices.length)
                .put(indices)
                .flip();
        GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, ib, GL33.GL_STATIC_DRAW);

        // Change to VBOs...
        // Tell OpenGL we are currently writing to this buffer (vboId)
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, squareVboId);

        FloatBuffer fb = BufferUtils.createFloatBuffer(vertices.length)
                .put(vertices)
                .flip();

        // Send the buffer (positions) to the GPU
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, fb, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 0, 0);
        GL33.glEnableVertexAttribArray(0);

        // Clear the buffer from the memory (it's saved now on the GPU, no need for it here)
        MemoryUtil.memFree(fb);

        // Change to Color...
        // Tell OpenGL we are currently writing to this buffer (colorsId)
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, colorsId);

        FloatBuffer cb = BufferUtils.createFloatBuffer(colors.length)
                .put(colorsRed)
                .flip();

        // Send the buffer (positions) to the GPU
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, cb, GL33.GL_STATIC_DRAW);
        GL33.glVertexAttribPointer(1, 3, GL33.GL_FLOAT, false, 0, 0);
        GL33.glEnableVertexAttribArray(1);

        GL33.glUseProgram(Shaders.shaderProgramId);

        // Sending Mat4 to GPU
        matrix.get(matrixFloatBuffer);
        GL33.glUniformMatrix4fv(uniformMatrixLocation, false, matrixFloatBuffer);

        // Clear the buffer from the memory (it's saved now on the GPU, no need for it here)
        MemoryUtil.memFree(cb);
        MemoryUtil.memFree(fb);
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

        //GL33.glBindVertexArray(gradient.vaoId);
        //GL33.glDrawElements(GL33.GL_TRIANGLES, gradient.indices.length, GL33.GL_UNSIGNED_INT, 0);
        // Draw using the glDrawElements function

        for(int y = 0; y<rows; y++) {
            for(int x = 0; x<cols; x++) {
                mazeArray[y][x].matrix.get(matrixFloatBuffer);
                GL33.glUniformMatrix4fv(uniformMatrixLocation, false, matrixFloatBuffer);

                GL33.glBindVertexArray(mazeArray[y][x].vaoId);
                GL33.glDrawElements(GL33.GL_TRIANGLES, mazeArray[y][x].indices.length, GL33.GL_UNSIGNED_INT, 0);
            }
        }
        matrix.get(matrixFloatBuffer);
        GL33.glUniformMatrix4fv(uniformMatrixLocation, false, matrixFloatBuffer);

        GL33.glBindVertexArray(squareVaoId);
        GL33.glDrawElements(GL33.GL_TRIANGLES, indices.length, GL33.GL_UNSIGNED_INT, 0);


    }

    public static void update(long window) {
        /*float color = 0.01f;
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
        System.out.println(grd[1]);
        if(grd[1] >= 1.0f) goDown = true;
        else if(grd[1] <= 0.0f) goDown = false;
        if(goDown) grd[1] -= 0.01f;
        else grd[1] += 0.01f;
        gradient = gradient(grd);*/

        if(GLFW.glfwGetKey(window, GLFW.GLFW_KEY_D) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT) == GLFW.GLFW_PRESS) {
            matrix = matrix.translate(0.02f, 0f, 0f);
            xPlayer += 0.02f;
            xPlayer2 += 0.02f;
        }
        if(GLFW.glfwGetKey(window, GLFW.GLFW_KEY_A) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT) == GLFW.GLFW_PRESS) {
            matrix = matrix.translate(-0.02f, 0f, 0f);
            xPlayer -= 0.02f;
            xPlayer2 -= 0.02f;
        }
        if(GLFW.glfwGetKey(window, GLFW.GLFW_KEY_W) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_UP) == GLFW.GLFW_PRESS) {
            matrix = matrix.translate(0f, 0.02f, 0f);
            yPlayer += 0.02f;
            yPlayer2 += 0.02f;
        }
        if(GLFW.glfwGetKey(window, GLFW.GLFW_KEY_S) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_DOWN) == GLFW.GLFW_PRESS) {
            matrix = matrix.translate(0f, -0.02f, 0f);
            yPlayer -= 0.02f;
            yPlayer2 -= 0.02f;
        }

        // TODO: Send to GPU only if position updated
       if(checkCollisions()) System.out.println("colission");

    }

    public static boolean checkCollisions() {

        for(int y = 0; y<rows; y++) {
            for(int x = 0; x<cols; x++) {
                    float xCoord = mazeArray[y][x].vertices[9];
                    float yCoord = mazeArray[y][x].vertices[10];
                    float xCoord2 = mazeArray[y][x].vertices[3];
                    float yCoord2 = mazeArray[y][x].vertices[4];
                    System.out.println(xCoord + " " + yCoord + " " + xCoord2 + " " + yCoord2);
                    System.out.println(xPlayer + " " + yPlayer + " " + xPlayer2 + " " + yPlayer2);

                    if (((xPlayer > xCoord && xPlayer < xCoord2) || (xPlayer2 > xCoord && xPlayer2 < xCoord2))
                                && ((yPlayer > yCoord && yPlayer < yCoord2) || (yPlayer2 > yCoord && yPlayer2 < yCoord2))) {
                        return true;
                    }
            }
        }
        return false;
    }

}
