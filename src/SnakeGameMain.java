import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.Timer;

public class SnakeGameMain extends JPanel implements ActionListener, KeyListener {

    private final int INITIAL_B_WIDTH = 300;
    private final int INITIAL_B_HEIGHT = 300;
    private final int DOT_SIZE = 10;
    private final int ALL_DOTS = 900;
    private final int RAND_POS = 29;
    private int delay = 180;

    private int B_WIDTH = INITIAL_B_WIDTH;
    private int B_HEIGHT = INITIAL_B_HEIGHT;

    private final int x[] = new int[ALL_DOTS];
    private final int y[] = new int[ALL_DOTS];

    private int dots;
    private int apple_x;
    private int apple_y;
    private int score;
    private int highestScore = 0;
    private long startTime;
    private long elapsedTime;
    private boolean isPaused = false;

    private boolean leftDirection = false;
    private boolean rightDirection = true;
    private boolean upDirection = false;
    private boolean downDirection = false;
    private boolean inGame = true;

    private Image ball;
    private Image apple;
    private Image head;

    private Timer timer;
    private JButton replayButton;

    private Color[] colors = {Color.GREEN, Color.ORANGE, Color.BLUE}; // Array of colors
    private int currentColorIndex = 0;

    public SnakeGameMain() {
        addKeyListener(this);
        setBackground(Color.black);
        setFocusable(true);
        setPreferredSize(new Dimension(B_WIDTH + 100, B_HEIGHT)); // Extra space for scorecard
        loadImages();
        initGame();

        // Initialize the replay button
        replayButton = new JButton("Replay");
        replayButton.setFocusable(false);
        replayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGame();
            }
        });
        replayButton.setVisible(false); // Initially hidden
        setLayout(null); // Use absolute positioning for the button
        add(replayButton);
    }

    private void loadImages() {
        ImageIcon iid = new ImageIcon(getClass().getResource("/dot.png"));
        ball = iid.getImage().getScaledInstance(DOT_SIZE, DOT_SIZE, Image.SCALE_SMOOTH);

        ImageIcon iia = new ImageIcon(getClass().getResource("/apple.png"));
        apple = iia.getImage().getScaledInstance(DOT_SIZE, DOT_SIZE, Image.SCALE_SMOOTH);

        ImageIcon iih = new ImageIcon(getClass().getResource("/head.png"));
        head = iih.getImage().getScaledInstance(DOT_SIZE, DOT_SIZE, Image.SCALE_SMOOTH);
    }

    private void initGame() {
        dots = 3;
        score = 0;
        delay = 180; // Starting speed
        startTime = System.currentTimeMillis();

        for (int z = 0; z < dots; z++) {
            x[z] = 50 - z * 10;
            y[z] = 50;
        }

        locateApple();

        timer = new Timer(delay, this);
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }

    private void doDrawing(Graphics g) {
        if (inGame) {
            g.drawImage(apple, apple_x, apple_y, this);

            for (int z = 0; z < dots; z++) {
                if (z == 0) {
                    g.drawImage(head, x[z], y[z], this);
                } else {
                    g.setColor(colors[currentColorIndex]); // Use the current color from the array
                    g.fillRect(x[z], y[z], DOT_SIZE, DOT_SIZE);
                }
            }

            Toolkit.getDefaultToolkit().sync();
        } else {
            gameOver(g);
        }
    }

    private void drawScoreCard(Graphics g) {
        elapsedTime = (System.currentTimeMillis() - startTime) / 1000;

        String scoreMsg = "Score: " + score;
        String highScoreMsg = "High Score: " + highestScore;
        String timeMsg = "Time: " + elapsedTime + "s";

        Font small = new Font("Helvetica", Font.BOLD, 12);
        g.setColor(Color.white);
        g.setFont(small);

        int scoreX = B_WIDTH + 10;

        g.drawString(scoreMsg, scoreX, 20);
        g.drawString(highScoreMsg, scoreX, 40);
        g.drawString(timeMsg, scoreX, 60);
    }

    private void gameOver(Graphics g) {
        String msg = "Game Over";
        Font small = new Font("Helvetica", Font.BOLD, 14);
        FontMetrics metr = this.getFontMetrics(small);

        g.setColor(Color.white);
        g.setFont(small);
        g.drawString(msg, (B_WIDTH - metr.stringWidth(msg)) / 2, B_HEIGHT / 2);

        // Display the replay button
        replayButton.setBounds((B_WIDTH - 80) / 2, B_HEIGHT / 2 + 20, 80, 30);
        replayButton.setVisible(true);

        // Update highest score if necessary
        if (score > highestScore) {
            highestScore = score;
        }

        // Display the scoreboard
        drawScoreCard(g);
    }

    private void resetGame() {
        inGame = true;
        leftDirection = false;
        rightDirection = true;
        upDirection = false;
        downDirection = false;
        isPaused = false;

        replayButton.setVisible(false); // Hide the replay button
        initGame(); // Re-initialize the game
        repaint(); // Redraw the panel
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (inGame && !isPaused) {
            checkApple();
            checkCollision();
            move();
        }

        repaint();
    }

    private void move() {
        for (int z = dots; z > 0; z--) {
            x[z] = x[(z - 1)];
            y[z] = y[(z - 1)];
        }

        if (leftDirection) {
            x[0] -= DOT_SIZE;
        }

        if (rightDirection) {
            x[0] += DOT_SIZE;
        }

        if (upDirection) {
            y[0] -= DOT_SIZE;
        }

        if (downDirection) {
            y[0] += DOT_SIZE;
        }
    }

    private void checkApple() {
        if ((x[0] == apple_x) && (y[0] == apple_y)) {
            dots++;
            score++; // Increment score when apple is eaten
            locateApple();

            // Increase speed with each apple eaten
            delay = Math.max(40, delay - 10); // Ensure delay doesn't go below 40ms
            timer.setDelay(delay);

            // Change the snake color
            currentColorIndex = (currentColorIndex + 1) % colors.length;
        }
    }

    private void locateApple() {
        int r = (int) (Math.random() * RAND_POS);
        apple_x = ((r * DOT_SIZE));

        r = (int) (Math.random() * RAND_POS);
        apple_y = ((r * DOT_SIZE));
    }

    private void checkCollision() {
        for (int z = dots; z > 0; z--) {
            if ((z > 4) && (x[0] == x[z]) && (y[0] == y[z])) {
                inGame = false;
            }
        }

        if (y[0] >= B_HEIGHT) {
            inGame = false;
        }

        if (y[0] < 0) {
            inGame = false;
        }

        if (x[0] >= B_WIDTH) { // Collision with the right boundary of the frame
            inGame = false;
        }

        if (x[0] < 0) { // Collision with the left boundary of the frame
            inGame = false;
        }

        if (!inGame) {
            timer.stop();
        }
    }
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if ((key == KeyEvent.VK_LEFT) && (!rightDirection)) {
            leftDirection = true;
            upDirection = false;
            downDirection = false;
        }

        if ((key == KeyEvent.VK_RIGHT) && (!leftDirection)) {
            rightDirection = true;
            upDirection = false;
            downDirection = false;
        }

        if ((key == KeyEvent.VK_UP) && (!downDirection)) {
            upDirection = true;
            rightDirection = false;
            leftDirection = false;
        }

        if ((key == KeyEvent.VK_DOWN) && (!upDirection)) {
            downDirection = true;
            rightDirection = false;
            leftDirection = false;
        }

        if (key == KeyEvent.VK_SPACE) {
            togglePause();
        }
    }

    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            timer.stop();
        } else {
            timer.start();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        SnakeGameMain game = new SnakeGameMain();
        frame.add(game);
        frame.setResizable(true); // Allow frame to be resizable
        frame.pack();
        frame.setTitle("Snake Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Add a component listener to update the game area size when the frame is resized
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                Dimension size = frame.getContentPane().getSize();
                game.B_WIDTH = size.width - 100; // Keep extra space for the scoreboard
                game.B_HEIGHT = size.height;
                game.setPreferredSize(new Dimension(game.B_WIDTH + 100, game.B_HEIGHT));
                game.repaint();
            }
        });
    }
}
