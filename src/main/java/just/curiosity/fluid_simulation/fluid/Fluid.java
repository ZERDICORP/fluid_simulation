package just.curiosity.fluid_simulation.fluid;

public class Fluid {
  private final int size;
  private final int iterations;
  private final float speed;
  private final float diffusion;
  private final float viscosity;
  private final float[] velocityX;
  private final float[] previousVelocityX;
  private final float[] velocityY;
  private final float[] previousVelocityY;
  private final float[] previousDensity;
  private final float[] density;

  public Fluid(int size, int diffusion, int viscosity, int iterations, float speed) {
    this.size = size;
    this.speed = speed;
    this.diffusion = diffusion;
    this.viscosity = viscosity;
    this.iterations = iterations;
    this.previousDensity = new float[size * size];
    this.density = new float[size * size];
    this.velocityX = new float[size * size];
    this.velocityY = new float[size * size];
    this.previousVelocityX = new float[size * size];
    this.previousVelocityY = new float[size * size];
  }

  public float getDensity(int x, int y) {
    return density[toIndex(x, y)];
  }

  public void addDensity(int x, int y, float amount) {
    density[toIndex(x, y)] += amount;
  }

  public void addVelocity(int x, int y, float amountX, float amountY) {
    final int index = toIndex(x, y);
    velocityX[index] += amountX;
    velocityY[index] += amountY;
  }

  public void step() {
    diffusion(1, previousVelocityX, velocityX, viscosity);
    diffusion(2, previousVelocityY, velocityY, viscosity);

    projection(previousVelocityX, previousVelocityY, velocityX, velocityY);

    advection(1, velocityX, previousVelocityX, previousVelocityX, previousVelocityY);
    advection(2, velocityY, previousVelocityY, previousVelocityX, previousVelocityY);

    projection(velocityX, velocityY, previousVelocityX, previousVelocityY);

    diffusion(0, previousDensity, density, diffusion);
    advection(0, density, previousDensity, velocityX, velocityY);
  }

  public void fade(float fadeSpeed) {
    for (int i = 0; i < density.length; i++) {
      density[i] = Math.max(0, density[i] - fadeSpeed);
    }
  }

  private float minMax(float value, float min, float max) {
    return Math.min(max, Math.max(min, value));
  }

  private int toIndex(int x, int y) {
    x = (int) minMax(x, 0, size - 1);
    y = (int) minMax(y, 0, size - 1);
    return y * size + x;
  }

  private void diffusion(int border, float[] matrix, float[] previousMatrix, float mod) {
    final float u = speed * mod * (size - 2) * (size - 2);
    linearSolver(border, matrix, previousMatrix, u, 1 + 4 * u);
  }

  private void linearSolver(int border, float[] matrix, float[] previousMatrix, float u0, float u1) {
    for (int k = 0; k < iterations; k++) {
      for (int j = 1; j < size - 1; j++) {
        for (int i = 1; i < size - 1; i++) {
          final float l0 = matrix[toIndex(i + 1, j)];
          final float l1 = matrix[toIndex(i - 1, j)];
          final float l2 = matrix[toIndex(i, j + 1)];
          final float l3 = matrix[toIndex(i, j - 1)];

          matrix[toIndex(i, j)] = (previousMatrix[toIndex(i, j)] + (l0 + l1 + l2 + l3) * u0) * (1f / u1);
        }
      }

      setBoundaries(border, matrix);
    }
  }

  private void projection(float[] mX0, float[] mY0, float[] mX1, float[] mY1) {
    for (int j = 1; j < size - 1; j++) {
      for (int i = 1; i < size - 1; i++) {
        final float u0 = mX0[toIndex(i + 1, j)];
        final float u1 = mX0[toIndex(i - 1, j)];
        final float u2 = mY0[toIndex(i, j + 1)];
        final float u3 = mY0[toIndex(i, j - 1)];

        mY1[toIndex(i, j)] = -(u0 - u1 + u2 - u3) / size * .5f;
        mX1[toIndex(i, j)] = 0;
      }
    }

    setBoundaries(0, mY1);
    setBoundaries(0, mX1);
    linearSolver(0, mX1, mY1, 1, 4);

    for (int j = 1; j < size - 1; j++) {
      for (int i = 1; i < size - 1; i++) {
        mX0[toIndex(i, j)] -= (mX1[toIndex(i + 1, j)] - mX1[toIndex(i - 1, j)]) * size * .5f;
        mY0[toIndex(i, j)] -= (mX1[toIndex(i, j + 1)] - mX1[toIndex(i, j - 1)]) * size * .5f;
      }
    }

    setBoundaries(1, mX0);
    setBoundaries(2, mY0);
  }

  private void advection(int border, float[] matrix, float[] previousMatrix, float[] vX, float[] vY) {
    final float speedX = speed * (size - 2);
    final float speedY = speed * (size - 2);

    for (int j = 1; j < size - 1; j++) {
      for (int i = 1; i < size - 1; i++) {
        final float x = minMax(i - vX[toIndex(i, j)] * speedX, .5f, size + .5f);
        final float y = minMax(j - vY[toIndex(i, j)] * speedY, .5f, size + .5f);

        final int i0 = (int) Math.floor(x);
        final int i1 = i0 + 1;
        final int j0 = (int) Math.floor(y);
        final int j1 = j0 + 1;

        final float s1 = x - i0;
        final float s0 = 1f - s1;
        final float t1 = y - j0;
        final float t0 = 1f - t1;

        final float u0 = s0 * (t0 * previousMatrix[toIndex(i0, j0)] + t1 * previousMatrix[toIndex(i0, j1)]);
        final float u1 = s1 * (t0 * previousMatrix[toIndex(i1, j0)] + t1 * previousMatrix[toIndex(i1, j1)]);

        matrix[toIndex(i, j)] = u0 + u1;
      }
    }

    setBoundaries(border, matrix);
  }

  private void setBoundaries(int border, float[] matrix) {
    final int modX = border == 1 ? -1 : 1;
    final int modY = border == 2 ? -1 : 1;

    for (int i = 1; i < size - 1; i++) {
      matrix[toIndex(0, i)] = matrix[toIndex(1, i)] * modX;
      matrix[toIndex(size - 1, i)] = matrix[toIndex(size - 2, i)] * modX;
    }

    for (int i = 1; i < size - 1; i++) {
      matrix[toIndex(i, 0)] = matrix[toIndex(i, 1)] * modY;
      matrix[toIndex(i, size - 1)] = matrix[toIndex(i, size - 2)] * modY;
    }

    matrix[toIndex(0, 0)] = (matrix[toIndex(1, 0)] +
      matrix[toIndex(0, 1)]) * .5f;

    matrix[toIndex(0, size - 1)] = (matrix[toIndex(1, size - 1)] +
      matrix[toIndex(0, size - 2)]) * .5f;

    matrix[toIndex(size - 1, 0)] = (matrix[toIndex(size - 2, 0)] +
      matrix[toIndex(size - 1, 1)]) * .5f;

    matrix[toIndex(size - 1, size - 1)] = (matrix[toIndex(size - 2, size - 1)] +
      matrix[toIndex(size - 1, size - 2)]) * .5f;
  }
}
