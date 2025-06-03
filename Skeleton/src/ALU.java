/*
  Author: Lakshay Bansal
  This file implements the core execution of an ALU in a computer simulation.
*/

public class ALU {

    // Define opcode width and named constants for each opcode.
    private static final int OPCODE_WIDTH = 5;

    private static final int OPCODE_NOP          = 0;
    private static final int OPCODE_ADD          = 1;
    private static final int OPCODE_AND          = 2;
    private static final int OPCODE_MULTIPLY     = 3;
    private static final int OPCODE_LEFT_SHIFT   = 4;
    private static final int OPCODE_SUBTRACT     = 5;
    private static final int OPCODE_OR           = 6;
    private static final int OPCODE_RIGHT_SHIFT  = 7;
    private static final int OPCODE_COMPARE      = 11;

    public Word16 instruction = new Word16();
    public Word32 op1 = new Word32();
    public Word32 op2 = new Word32();
    public Word32 result = new Word32();
    public Bit less = new Bit(false);
    public Bit equal = new Bit(false);

    /**
     * Executes the current instruction held in the instruction register.
     * It decodes the 5-bit opcode, and then dispatches to the appropriate operation.
     */
    public void doInstruction() {
        int op = decodeOpcode();

        // If the opcode is NOP, do nothing.
        if (op == OPCODE_NOP) {
            return;
        }

        executeOperation(op);
    }

    /**
     * Decodes the 5-bit opcode from the instruction register.
     * Bit 0 is the most significant bit.
     *
     * @return The decoded integer opcode.
     */
    private int decodeOpcode() {
        int op = 0;
        for (int i = 0; i < OPCODE_WIDTH; i++) {
            Bit temp = new Bit(false);
            instruction.getBitN(i, temp);
            if (temp.getValue() == Bit.boolValues.TRUE) {
                // Set the bit at position (OPCODE_WIDTH-1-i)
                op |= (1 << (OPCODE_WIDTH - 1 - i));
            }
        }
        return op;
    }

    /**
     * Dispatches the execution based on the decoded opcode.
     *
     * @param op the decoded opcode
     */
    private void executeOperation(int op) {
        switch (op) {
            case OPCODE_ADD:
                Adder.add(op1, op2, result);
                break;
            case OPCODE_AND:
                Word32.and(op1, op2, result);
                break;
            case OPCODE_MULTIPLY:
                multiply(op1, op2, result);
                break;
            case OPCODE_LEFT_SHIFT:
                int leftShiftAmount = TestConverter.toInt(op2);  // op2 holds the shift amount
                Shifter.LeftShift(op1, leftShiftAmount, result);
                break;
            case OPCODE_SUBTRACT:
                Adder.subtract(op1, op2, result);
                break;
            case OPCODE_OR:
                Word32.or(op1, op2, result);
                break;
            case OPCODE_RIGHT_SHIFT:
                int rightShiftAmount = TestConverter.toInt(op2);
                Shifter.RightShift(op1, rightShiftAmount, result);
                break;
            case OPCODE_COMPARE:
                executeCompare();
                break;
            default:
                throw new IllegalArgumentException("Unknown opcode: " + op);
        }
    }

    /**
     * Executes the COMPARE operation. It converts op1 and op2 to integers,
     * then sets the flags 'less' and 'equal' accordingly.
     */
    private void executeCompare() {
        int aVal = TestConverter.toInt(op1);
        int bVal = TestConverter.toInt(op2);
        if (aVal < bVal) {
            less.assign(Bit.boolValues.TRUE);
            equal.assign(Bit.boolValues.FALSE);
        } else if (aVal == bVal) {
            less.assign(Bit.boolValues.FALSE);
            equal.assign(Bit.boolValues.TRUE);
        } else {
            less.assign(Bit.boolValues.FALSE);
            equal.assign(Bit.boolValues.FALSE);
        }
    }

    /**
     * Implements multiplication using the shift-and-add algorithm.
     * For each true bit in b (from MSB to LSB), it shifts a appropriately and
     * accumulates the result in the product.
     *
     * @param a      The first multiplicand.
     * @param b      The second multiplicand.
     * @param result The Word32 variable to hold the final product.
     */
    private void multiply(Word32 a, Word32 b, Word32 result) {
        Word32 product = new Word32(); // Assume initialization to zero.
        // Iterate over each bit in b from bit 31 (MSB) down to 0 (LSB)
        for (int j = 31; j >= 0; j--) {
            Bit bit = new Bit(false);
            b.getBitN(j, bit);
            if (bit.getValue() == Bit.boolValues.TRUE) {
                int shiftAmt = 31 - j;
                Word32 shifted = new Word32();
                Shifter.LeftShift(a, shiftAmt, shifted);
                Word32 newProduct = new Word32();
                Adder.add(product, shifted, newProduct);
                newProduct.copy(product);
            }
        }
        product.copy(result);
    }
}
