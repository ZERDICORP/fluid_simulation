package just.curiosity.fluid_simulation.gui.interaction;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

public final class Mouse implements MouseListener, MouseMotionListener, MouseWheelListener {
  private static int latestX;
  private static int latestY;
  private static int currentX;
  private static int currentY;
  private static int diffX;
  private static int diffY;
  private static boolean dragged;

  public int getX() {
    return currentX;
  }

  public int getY() {
    return currentY;
  }

  public int getDiffX() {
    return diffX;
  }

  public int getDiffY() {
    return diffY;
  }

  public boolean isDragged() {
    return dragged;
  }

  public void reset() {
    diffX = 0;
    diffY = 0;
    currentX = 0;
    currentY = 0;
  }

  @Override
  public void mouseDragged(MouseEvent mouseEvent) {
    currentX = mouseEvent.getX();
    currentY = mouseEvent.getY();

    diffX = currentX - latestX;
    diffY = currentY - latestY;

    latestX = currentX;
    latestY = currentY;
  }

  @Override
  public void mousePressed(MouseEvent mouseEvent) {
    dragged = true;

    latestX = mouseEvent.getX();
    latestY = mouseEvent.getY();
  }

  @Override
  public void mouseReleased(MouseEvent mouseEvent) {
    dragged = false;
  }

  @Override
  public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
  }

  @Override
  public void mouseClicked(MouseEvent mouseEvent) {
  }

  @Override
  public void mouseMoved(MouseEvent mouseEvent) {
  }

  @Override
  public void mouseEntered(MouseEvent mouseEvent) {
  }

  @Override
  public void mouseExited(MouseEvent mouseEvent) {
  }
}
