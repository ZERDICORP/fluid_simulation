package just.curiosity.fluid_simulation.gui;

import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import just.curiosity.fluid_simulation.gui.interaction.Keyboard;
import just.curiosity.fluid_simulation.gui.interaction.Mouse;

public final class Window extends JFrame {
  private final BufferedImage bufferedImage;

  public Window(BufferedImage bufferedImage, Keyboard keyboard, Mouse mouse) {
    this.bufferedImage = bufferedImage;

    addMouseListener(mouse);
    addMouseMotionListener(mouse);
    addMouseWheelListener(mouse);
    addKeyListener(keyboard);

    setTitle("Fluid Simulation");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(bufferedImage.getWidth(), bufferedImage.getHeight());
    setLocationRelativeTo(null);
    setResizable(false);
    setFocusable(true);
    setVisible(true);
  }

  public void draw() {
    BufferStrategy bufferStrategy = this.getBufferStrategy();
    if (bufferStrategy == null) {
      this.createBufferStrategy(3);
      return;
    }

    Graphics graphics = bufferStrategy.getDrawGraphics();

    graphics.drawImage(bufferedImage, 0, 0, null);
    bufferStrategy.show();
    graphics.dispose();
  }
}
