/**
 * This file defines a single-bit value that has basic bitwise operations:
 * AND, OR, XOR, and NOT. The internal representation is an enum boolValues.
 */
public class Bit {
    public enum boolValues { FALSE, TRUE }


    private boolValues value;

    // Constructor from boolean:
    public Bit(boolean value) {
        this.value = value ? boolValues.TRUE : boolValues.FALSE;
    }

    // Constructor from enum:
    public Bit(boolValues value) {
        this.value = value;
    }

    public boolValues getValue() {
        return this.value;
    }

    // Assign using enum:
    public void assign(boolValues val) {
        this.value = val;
    }

    // Optionally, you could also add an overload to assign from boolean:
    /*
    public void assign(boolean val) {
        this.value = val ? boolValues.TRUE : boolValues.FALSE;
    }
    */

    // ---------- Bitwise operations ----------

    public void and(Bit b2, Bit result) {
        and(this, b2, result);
    }

    public static void and(Bit b1, Bit b2, Bit result) {
        if (b1.getValue() == boolValues.TRUE && b2.getValue() == boolValues.TRUE) {
            result.assign(boolValues.TRUE);
        } else {
            result.assign(boolValues.FALSE);
        }
    }

    public void or(Bit b2, Bit result) {
        or(this, b2, result);
    }

    public static void or(Bit b1, Bit b2, Bit result) {
        if (b1.getValue() == boolValues.TRUE || b2.getValue() == boolValues.TRUE) {
            result.assign(boolValues.TRUE);
        } else {
            result.assign(boolValues.FALSE);
        }
    }

    public void xor(Bit b2, Bit result) {
        xor(this, b2, result);
    }

    public static void xor(Bit b1, Bit b2, Bit result) {
        // XOR is true if exactly one is TRUE:
        if ( (b1.getValue() == boolValues.TRUE && b2.getValue() == boolValues.FALSE)
                || (b1.getValue() == boolValues.FALSE && b2.getValue() == boolValues.TRUE) ) {
            result.assign(boolValues.TRUE);
        } else {
            result.assign(boolValues.FALSE);
        }
    }

    public void not(Bit result) {
        not(this, result);
    }

    public static void not(Bit b, Bit result) {
        if (b.getValue() == boolValues.TRUE) {
            result.assign(boolValues.FALSE);
        } else {
            result.assign(boolValues.TRUE);
        }
    }

    @Override
    public String toString() {
        return (this.value == boolValues.TRUE) ? "t" : "f";
    }
}
