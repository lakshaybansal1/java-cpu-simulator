/**
 * A 16-bit word supporting basic bitwise operations, using Bit.boolValues internally.
 */
public class Word16 {

    public static final int BIT_LENGTH = 16;
    private Bit[] bits = new Bit[BIT_LENGTH];

    public Word16() {
        for (int i = 0; i < BIT_LENGTH; i++) {
            bits[i] = new Bit(false);
        }
    }

    public Word16(Bit[] in) {
        if (in.length != BIT_LENGTH) {
            throw new IllegalArgumentException("Must be length " + BIT_LENGTH);
        }
        for (int i = 0; i < BIT_LENGTH; i++) {
            bits[i] = in[i];
        }
    }

    public void copy(Word16 result) {
        for (int i = 0; i < BIT_LENGTH; i++) {
            result.bits[i].assign(this.bits[i].getValue());
        }
    }

    public void setBitN(int n, Bit source) {
        bits[n].assign(source.getValue());
    }

    public void getBitN(int n, Bit result) {
        result.assign(bits[n].getValue());
    }

    public boolean equals(Word16 other) {
        for (int i = 0; i < BIT_LENGTH; i++) {
            if (this.bits[i].getValue() != other.bits[i].getValue()) {
                return false;
            }
        }
        return true;
    }

    public static boolean equals(Word16 a, Word16 b) {
        return a.equals(b);
    }

    // ---------- Bitwise ops ----------
    public void and(Word16 other, Word16 out) {
        for (int i = 0; i < BIT_LENGTH; i++) {
            this.bits[i].and(other.bits[i], out.bits[i]);
        }
    }
    public static void and(Word16 a, Word16 b, Word16 out) {
        a.and(b, out);
    }

    public void or(Word16 other, Word16 out) {
        for (int i = 0; i < BIT_LENGTH; i++) {
            this.bits[i].or(other.bits[i], out.bits[i]);
        }
    }
    public static void or(Word16 a, Word16 b, Word16 out) {
        a.or(b, out);
    }

    public void xor(Word16 other, Word16 out) {
        for (int i = 0; i < BIT_LENGTH; i++) {
            this.bits[i].xor(other.bits[i], out.bits[i]);
        }
    }
    public static void xor(Word16 a, Word16 b, Word16 out) {
        a.xor(b, out);
    }

    public void not(Word16 out) {
        for (int i = 0; i < BIT_LENGTH; i++) {
            this.bits[i].not(out.bits[i]);
        }
    }
    public static void not(Word16 a, Word16 out) {
        a.not(out);
    }

    /**
     * Returns the integer representation of this 16-bit word.
     */
    public int toInt() {

        int value = 0;
        for (int i = 0; i < BIT_LENGTH; i++) {
            if (bits[i].getValue() == Bit.boolValues.TRUE) {
                value |= (1 << (BIT_LENGTH - 1 - i));
            }
        }
        return value;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Bit bit : bits) {
            sb.append(bit.toString()).append(",");
        }
        return sb.toString();
    }
}
