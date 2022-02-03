import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

public class Display extends JPanel implements Runnable {
    Thread thread;

    int state = 0;
    final int tileSize = 5;
    int visibleMapSizeX;
    int visibleMapSizeY;

    HashMap<Point, Boolean> cellState = new HashMap<>();

    // Camera offset
    int offsetX;
    int offsetY;

    int FPS = 60; // FPS
    int tps = 5; // ticks per second
    int ticks = 0;

    boolean mouseClicked = false;
    Point mousePos;

    public Display(int width, int height){
        this.setPreferredSize(new Dimension(width, height));
        visibleMapSizeX = (int)(width/(double)tileSize);
        visibleMapSizeY = (int)(height/(double)tileSize);

        this.setBackground(Color.white);
        this.setDoubleBuffered(true);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

                mouseClicked = true;
                mousePos = new Point(e.getX(), e.getY());
            }
        });

        offsetX = 0;
        offsetY = 0;
    }

    public void start(){
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        System.out.println("Running");

        double drawInterval = 1000000000/FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (thread != null){ // game loop

            if (Keyboard.isKeyPressed(10)) state = (state + 1) % 2;

            if (mouseClicked){
                int tempMouseX = (int)(mousePos.x / (double)tileSize) + offsetX;
                int tempMouseY = (int)(mousePos.y / (double)tileSize) + offsetY;
                Point tempMousePoint = new Point(tempMouseX, tempMouseY);

                if (cellState.containsKey(tempMousePoint)){
                    if (cellState.get(tempMousePoint)){
                        cellState.replace(tempMousePoint, false);
                    } else{
                        cellState.replace(tempMousePoint, true);
                    }
                } else {
                    cellState.putIfAbsent(tempMousePoint, true);
                }

                mouseClicked = false;
            }

            switch (state) {
                case 0:
                    break;
                case 1:
                    ticks = (ticks + 1) % (FPS/tps);
                    if (ticks == 0) update();
                    break;
            }

            getMovement();

            repaint();

            try {
                double remainingTime = nextDrawTime - System.nanoTime();
                remainingTime = clamp0(remainingTime / 1000000);

                Thread.sleep((long)remainingTime);

                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void paint(Graphics g){
        // System.out.println("Painting");

        Graphics2D graphics = (Graphics2D) g;

        for (int y = 0; y < visibleMapSizeY; y++){
            for (int x = 0; x < visibleMapSizeX; x++){
                int baseX = x + Math.round(offsetX);
                int baseY = y + Math.round(offsetY);
                Point point = new Point(baseX, baseY);

                //System.out.println(point);

                if (cellState.getOrDefault(point, false) ){
                    //System.out.println("Should be Black");
                    graphics.setColor(Color.BLACK);
                } else {
                    graphics.setColor(Color.white);
                }

                graphics.fillRect(x * tileSize, y * tileSize, tileSize, tileSize);
            }
        }

        graphics.dispose();
    }

    public void update(){
        HashMap<Point, Boolean> tempCellState = new HashMap<>(cellState);

        for (Map.Entry<Point, Boolean> entry : cellState.entrySet()) {
            if (entry.getValue()){
                if (getNeighbors(entry.getKey()) == 2 || getNeighbors(entry.getKey()) == 3){
                    tempCellState.replace(entry.getKey(), true);
                } else {
                    tempCellState.replace(entry.getKey(), false);
                }

                getDeadNeighbors(tempCellState, entry.getKey());
            }
        }

        cellState = tempCellState;
    }

    public void getMovement(){
        if (Keyboard.isKeyPressed(87)){ // w
            offsetY -= 1;
        }

        if (Keyboard.isKeyPressed(83)){ // s
            offsetY += 1;
        }

        if (Keyboard.isKeyPressed(65)){ // a
            offsetX -= 1;
        }

        if (Keyboard.isKeyPressed(68)){ // d
            offsetX += 1;
        }

        if (Keyboard.isKeyPressed(38)){ // up
            tps += 1;
            if (tps > 20) tps = 20;
        }

        if (Keyboard.isKeyPressed(40)){ // down
            tps -= 1;
            if (tps < 1) tps = 1;
        }
    }

    private void getDeadNeighbors(HashMap<Point, Boolean> map, Point point){
        for (int y = -1; y <= 1; y++) {
            for (int x = -1; x <= 1; x++) {
                if (x == 0 && y == 0) continue;

                Point tempPoint = new Point(point.x + x, point.y + y);
                if (!cellState.getOrDefault(new Point(tempPoint.x, tempPoint.y), false) && getNeighbors(new Point(tempPoint.x, tempPoint.y)) == 3) {
                    if (map.containsKey(tempPoint)) {
                        map.replace(tempPoint, true);
                    } else {
                        map.putIfAbsent(tempPoint, true);
                    }
                }
            }
        }

    }

    private int getNeighbors(Point point){
        int count = 0;

        if (cellState.getOrDefault(new Point(point.x - 1, point.y), false)) count++;
        if (cellState.getOrDefault(new Point(point.x + 1, point.y), false)) count++;
        if (cellState.getOrDefault(new Point(point.x, point.y - 1), false)) count++;
        if (cellState.getOrDefault(new Point(point.x, point.y + 1), false)) count++;
        if (cellState.getOrDefault(new Point(point.x - 1, point.y - 1), false)) count++;
        if (cellState.getOrDefault(new Point(point.x + 1, point.y - 1), false)) count++;
        if (cellState.getOrDefault(new Point(point.x - 1, point.y + 1), false)) count++;
        if (cellState.getOrDefault(new Point(point.x + 1, point.y + 1), false)) count++;

        return count;
    }

    private double clamp0(double value){
        if (value < 0){
            return 0;
        } else{
            return value;
        }
    }
}
