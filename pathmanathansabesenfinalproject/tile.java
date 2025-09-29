
public class tile {
    private boolean revealed;
    private int row, col, value; // 99=mine, 0..8=count

    public tile(boolean revealed, int row, int col, int value) {
        this.revealed = revealed;
        this.row = row;
        this.col = col;
        this.value = value;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void setRevealed(boolean r) {
        revealed = r;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int v) {
        value = v;
    }

    /** 0..63 index helper if ever needed */
    public int getTileNumber() {
        return row * 8 + col;
    }
}
