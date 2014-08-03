import java.util.Random;

/**
 * Wrap the Java random number generator.
 */

class PRNG implements IPRNG {

  /**
   * Java random number generator
   */
  private long seedValue;
  private Random rng;

  /**
   * Build a new pseudo random number generator with the given seed.
   */
  public PRNG(long mySeed) {
    seedValue = mySeed;
    rng = new Random(seedValue);
  }
   
  /**
   * Return the next double value between 0.0 and 1.0
   */
  public double next() {
    return rng.nextDouble();
  }
  
  /**
   * Reset the PRNG to the original seed
   */
  public void startOver() {
    rng.setSeed(seedValue);
  }
}