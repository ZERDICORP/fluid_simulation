package just.curiosity.fluid_simulation;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import just.curiosity.fluid_simulation.constants.Const;
import just.curiosity.fluid_simulation.fluid.Fluid;
import just.curiosity.fluid_simulation.gui.Window;
import just.curiosity.fluid_simulation.gui.interaction.Keyboard;
import just.curiosity.fluid_simulation.gui.interaction.Mouse;

public class Main {
  private static final int size;
  private static final int sqW;
  private static final Fluid fluid;
  private static final Keyboard keyboard;
  private static final Mouse mouse;
  private static final int[] pixelBuffer;
  private static final Window window;
  private static boolean isRunning;

  static {
    isRunning = true;
    size = 100;
    sqW = 5;
    fluid = new Fluid(size, Const.FLUID_DENSITY, Const.FLUID_VISCOSITY, Const.FLUID_ITERATIONS, Const.FLUID_SPEED);
    keyboard = new Keyboard();
    mouse = new Mouse();

    final BufferedImage bufferedImage = new BufferedImage(
      size * sqW,
      size * sqW,
      BufferedImage.TYPE_INT_RGB);

    pixelBuffer = ((DataBufferInt) bufferedImage.getRaster()
      .getDataBuffer())
      .getData();

    window = new Window(bufferedImage, keyboard, mouse);
  }

  private static void fillRectOnPixelBuffer(int sX, int sY, int value) {
    final int realX = sX * sqW;
    final int realY = sY * sqW;
    final int RGB = new Color(value, value, value).getRGB();

    for (int y = realY; y < realY + sqW; y++) {
      for (int x = realX; x < realX + sqW; x++) {
        pixelBuffer[y * (size * sqW) + x] = RGB;
      }
    }
  }

  private static void interactionControl() {
    if (keyboard.getCurrentKeyCode() == KeyEvent.VK_ESCAPE) {
      stop();
    }

    if (mouse.isDragged()) {
      fluid.addDensity(mouse.getX() / sqW, mouse.getY() / sqW, Const.DENSITY_AMOUNT);
      fluid.addVelocity(mouse.getX() / sqW, mouse.getY() / sqW,
        mouse.getDiffX() * Const.MOUSE_DIFF_FACTOR,
        mouse.getDiffY() * Const.MOUSE_DIFF_FACTOR);
    }

    mouse.reset();
  }

  private static void updateAndDraw() {
    interactionControl();

    Arrays.fill(pixelBuffer, Const.BACKGROUND_COLOR_RGB);

    fluid.step();
    fluid.fade(Const.FADE_SPEED);

    for (int y = 0; y < size; y++) {
      for (int x = 0; x < size; x++) {
        final int value = (int) Math.min(255, Math.max(0, fluid.getDensity(x, y)));
        fillRectOnPixelBuffer(x, y, value);
      }
    }

    window.draw();
  }

  public static void start() {
    long start = System.currentTimeMillis();
    int frames = 0;

    while (isRunning) {
      long end = System.currentTimeMillis();
      if (end - start >= 1000) {
        System.out.print("\rFPS: " + frames + "    ");
        frames = 0;
        start = end;
      }

      updateAndDraw();

      frames++;
    }

    window.dispose();
  }

  private static void stop() {
    isRunning = false;
  }

  public static void main(String[] args) {
    start();
  }
}
