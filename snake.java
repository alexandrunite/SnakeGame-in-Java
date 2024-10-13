import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class SnakeGame extends JPanel implements ActionListener, KeyListener {
    private class Tile {
        int x, y;
        Tile(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    private final int boardWidth;
    private final int boardHeight;
    private final int tileSize = 25;
    private int gameSpeed = 100;

    private Tile snakeHead;
    private ArrayList<Tile> snakeBody;

    private Tile food;
    private ArrayList<Tile> obstacles;
    private Random random;

    private int velocityX;
    private int velocityY;

    private Timer gameLoop;

    private boolean gameOver = false;
    private boolean paused = false;

    private int score = 0;
    private int level = 1;

    public SnakeGame(int boardWidth, int boardHeight) {
        this.boardWidth = boardWidth;
        this.boardHeight = boardHeight;
        setPreferredSize(new Dimension(this.boardWidth, this.boardHeight));
        setBackground(Color.black);
        addKeyListener(this);
        setFocusable(true);

        snakeHead = new Tile(5, 5);
        snakeBody = new ArrayList<>();

        food = new Tile(10, 10);
        obstacles = new ArrayList<>();
        random = new Random();
        placeFood();
        placeObstacles();

        velocityX = 1;
        velocityY = 0;

        gameLoop = new Timer(gameSpeed, this);
        gameLoop.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        for(int i = 0; i < boardWidth / tileSize; i++) {
            g.drawLine(i * tileSize, 0, i * tileSize, boardHeight);
            g.drawLine(0, i * tileSize, boardWidth, i * tileSize);
        }

        g.setColor(Color.red);
        g.fill3DRect(food.x * tileSize, food.y * tileSize, tileSize, tileSize, true);

        g.setColor(Color.green);
        g.fill3DRect(snakeHead.x * tileSize, snakeHead.y * tileSize, tileSize, tileSize, true);

        for (Tile snakePart : snakeBody) {
            g.fill3DRect(snakePart.x * tileSize, snakePart.y * tileSize, tileSize, tileSize, true);
        }

        g.setColor(Color.gray);
        for (Tile obstacle : obstacles) {
            g.fill3DRect(obstacle.x * tileSize, obstacle.y * tileSize, tileSize, tileSize, true);
        }

        g.setFont(new Font("Arial", Font.PLAIN, 16));
        g.setColor(Color.white);
        if (gameOver) {
            g.drawString("Game Over - Score: " + score + " - Level: " + level, tileSize, tileSize);
        } else if (paused) {
            g.drawString("Paused - Score: " + score + " - Level: " + level, tileSize, tileSize);
        } else {
            g.drawString("Score: " + score + " - Level: " + level, tileSize, tileSize);
        }
    }

    public void placeFood() {
        int x, y;
        do {
            x = random.nextInt(boardWidth / tileSize);
            y = random.nextInt(boardHeight / tileSize);
            food = new Tile(x, y);
        } while (collisionWithSnake(food) || collisionWithObstacles(food));
    }

    public void placeObstacles() {
        obstacles.clear();
        int numObstacles = level * 2;
        for (int i = 0; i < numObstacles; i++) {
            int x, y;
            Tile obstacle;
            do {
                x = random.nextInt(boardWidth / tileSize);
                y = random.nextInt(boardHeight / tileSize);
                obstacle = new Tile(x, y);
            } while (collisionWithSnake(obstacle) || collision(food, obstacle));
            obstacles.add(obstacle);
        }
    }

    public void move() {
        if (paused || gameOver) return;

        if (collision(snakeHead, food)) {
            snakeBody.add(new Tile(food.x, food.y));
            score += 10;
            if (score % 50 == 0) {
                level++;
                gameSpeed = Math.max(50, gameSpeed - 10);
                gameLoop.setDelay(gameSpeed);
                placeObstacles();
            }
            placeFood();
        }

        for (int i = snakeBody.size() - 1; i >= 0; i--) {
            Tile snakePart = snakeBody.get(i);
            if (i == 0) {
                snakePart.x = snakeHead.x;
                snakePart.y = snakeHead.y;
            } else {
                Tile prevSnakePart = snakeBody.get(i - 1);
                snakePart.x = prevSnakePart.x;
                snakePart.y = prevSnakePart.y;
            }
        }

        snakeHead.x += velocityX;
        snakeHead.y += velocityY;

        if (snakeHead.x < 0 || snakeHead.x >= boardWidth / tileSize ||
            snakeHead.y < 0 || snakeHead.y >= boardHeight / tileSize) {
            gameOver = true;
        }

        for (Tile snakePart : snakeBody) {
            if (collision(snakeHead, snakePart)) {
                gameOver = true;
            }
        }

        for (Tile obstacle : obstacles) {
            if (collision(snakeHead, obstacle)) {
                gameOver = true;
            }
        }
    }

    public boolean collision(Tile tile1, Tile tile2) {
        return tile1.x == tile2.x && tile1.y == tile2.y;
    }

    public boolean collisionWithSnake(Tile tile) {
        if (collision(snakeHead, tile)) return true;
        for (Tile snakePart : snakeBody) {
            if (collision(snakePart, tile)) return true;
        }
        return false;
    }

    public boolean collisionWithObstacles(Tile tile) {
        for (Tile obstacle : obstacles) {
            if (collision(obstacle, tile)) return true;
        }
        return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) {
            if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                resetGame();
            }
            return;
        }
        if (e.getKeyCode() == KeyEvent.VK_P) {
            paused = !paused;
            return;
        }
        if (paused) return;
        if (e.getKeyCode() == KeyEvent.VK_UP && velocityY != 1) {
            velocityX = 0;
            velocityY = -1;
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN && velocityY != -1) {
            velocityX = 0;
            velocityY = 1;
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT && velocityX != 1) {
            velocityX = -1;
            velocityY = 0;
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT && velocityX != -1) {
            velocityX = 1;
            velocityY = 0;
        }
    }

    public void resetGame() {
        snakeHead = new Tile(5, 5);
        snakeBody.clear();
        score = 0;
        level = 1;
        gameSpeed = 100;
        velocityX = 1;
        velocityY = 0;
        gameOver = false;
        paused = false;
        gameLoop.setDelay(gameSpeed);
        placeFood();
        placeObstacles();
        gameLoop.start();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyReleased(KeyEvent e) {}
}
