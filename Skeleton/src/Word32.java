/**
 * A 32-bit word supporting basic bitwise operations, storing each bit in a Bit.boolValues enum.
 */
public class Word32 {

    public static final int BIT_LENGTH = 32;
    private Bit[] bits = new Bit[BIT_LENGTH];

    public Word32() {
        for (int i = 0; i < BIT_LENGTH; i++) {
            bits[i] = new Bit(false); // all bits start as 0.
        }
    }
    public Word32(Bit[] in) {
        if (in.length != BIT_LENGTH) {
            throw new IllegalArgumentException("Input array must be length " + BIT_LENGTH);
        }
        for (int i = 0; i < BIT_LENGTH; i++) {
            bits[i] = in[i];
        }
    }

    /**
     * Sets this Word32 to match the 32-bit binary representation of 'value'.
     */
    public void setInt(int value) {
        for (int i = 0; i < BIT_LENGTH; i++) {
            int shift = BIT_LENGTH - 1 - i;
            boolean bitValue = ((value >> shift) & 1) == 1;
            bits[i].assign(bitValue ? Bit.boolValues.TRUE : Bit.boolValues.FALSE);
        }
    }

    /**
     * Returns the integer value represented by this Word32.
     */
    public int toInt() {
        int result = 0;
        for (int i = 0; i < BIT_LENGTH; i++) {
            if (bits[i].getValue() == Bit.boolValues.TRUE) {
                result |= (1 << (BIT_LENGTH - 1 - i));
            }
        }
        return result;
    }

    public void getTopHalf(Word16 result) {
        for (int i = 0; i < 16; i++) {
            result.setBitN(i, bits[i]); // pass the Bit object
        }
    }

    public void getBottomHalf(Word16 result) {
        for (int i = 0; i < 16; i++) {
            result.setBitN(i, bits[i + 16]);
        }
    }

    public void copy(Word32 result) {
        for (int i = 0; i < BIT_LENGTH; i++) {
            result.bits[i].assign(this.bits[i].getValue());
        }
    }

    public boolean equals(Word32 other) {
        for (int i = 0; i < BIT_LENGTH; i++) {
            if (this.bits[i].getValue() != other.bits[i].getValue()) {
                return false;
            }
        }
        return true;
    }

    public void getBitN(int n, Bit result) {
        result.assign(bits[n].getValue());
    }

    public void setBitN(int n, Bit source) {
        bits[n].assign(source.getValue());
    }

    // ---------- Bitwise ops ----------
    public void and(Word32 other, Word32 out) {
        for (int i = 0; i < BIT_LENGTH; i++) {
            this.bits[i].and(other.bits[i], out.bits[i]);
        }
    }
    public static void and(Word32 a, Word32 b, Word32 out) {
        a.and(b, out);
    }

    public void or(Word32 other, Word32 out) {
        for (int i = 0; i < BIT_LENGTH; i++) {
            this.bits[i].or(other.bits[i], out.bits[i]);
        }
    }
    public static void or(Word32 a, Word32 b, Word32 out) {
        a.or(b, out);
    }

    public void xor(Word32 other, Word32 out) {
        for (int i = 0; i < BIT_LENGTH; i++) {
            this.bits[i].xor(other.bits[i], out.bits[i]);
        }
    }
    public static void xor(Word32 a, Word32 b, Word32 out) {
        a.xor(b, out);
    }

    public void not(Word32 out) {
        for (int i = 0; i < BIT_LENGTH; i++) {
            this.bits[i].not(out.bits[i]);
        }
    }
    public static void not(Word32 a, Word32 out) {
        a.not(out);
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
