public class TestConverter {
    // Converts a 32-bit integer into a Word32.
    public static void fromInt(int value, Word32 result) {
        for (int i = 0; i < Word32.BIT_LENGTH; i++) {

            Bit.boolValues bitValue = ((value >>> (Word32.BIT_LENGTH - 1 - i)) & 1) == 1
                    ? Bit.boolValues.TRUE
                    : Bit.boolValues.FALSE;
            result.setBitN(i, new Bit(bitValue));
        }
    }

    // Converts a Word32 into a 32-bit integer.
    public static int toInt(Word32 value) {

        int resultInt = 0;
        for (int i = 0; i < Word32.BIT_LENGTH; i++) {
            Bit bit = new Bit(Bit.boolValues.FALSE);
            value.getBitN(i, bit);
            if (bit.getValue() == Bit.boolValues.TRUE) {
                resultInt |= (1 << (Word32.BIT_LENGTH - 1 - i));
            }
        }
        return resultInt;
    }


    public static int word16ToInt(Word16 value) {
        int resultInt = 0;
        for (int i = 0; i < 16; i++) {
            Bit bit = new Bit(Bit.boolValues.FALSE);
            value.getBitN(i, bit);
            if (bit.getValue() == Bit.boolValues.TRUE) {
                resultInt |= (1 << (16 - 1 - i));
            }
        }
        return resultInt;
    }
}
