import javax.swing.*;

public class Main extends JFrame {
    final int size = 500;
    final boolean fullScreen = false; // Fullscreen

    public Main(){
        System.out.println("Creating");

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // this closes the display on exit
        if (fullScreen){
            this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        } else {
            this.setSize(size, size); // sets the size
        }
        this.setTitle("Conway's Game of Life"); // sets the title

        Display gamePanel;
        if (fullScreen){
            gamePanel = new Display(this.getBounds().width, this.getBounds().height);
        } else {
            gamePanel = new Display(500, 500);
        }
        this.add(gamePanel);
        this.pack();

        System.out.println("Visable");
        this.setLocationRelativeTo(null);
        this.setVisible(true);

        gamePanel.start();
    }

    public static void main(String[] args) {
        new Main();
    }
}
