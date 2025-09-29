/** stores start/end and returns elapsed seconds */
public class Score {
    private final long startMs;
    private final long endMs;

    public Score(long startMs, long endMs) {
        this.startMs = startMs;
        this.endMs = endMs;
    }

    public int seconds() {
        long diff = Math.max(0, endMs - startMs);
        return (int) (diff / 1000L);
    }
}
