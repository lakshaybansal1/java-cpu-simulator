/*
Author - Lakshay Bansal
This file shifter class implements the logical bit shifting operation on 32 bits words.
 */
public class Shifter {

    public static void LeftShift(Word32 a, int n, Word32 result) {
        for (int i = 0; i < 32; i++) {
            if (i + n < 32) {
                Bit temp = new Bit(false);
                a.getBitN(i + n, temp);
                result.setBitN(i, temp);

            } else {
                result.setBitN(i, new Bit(false));
            }
        }
    }

    public static void RightShift(Word32 a, int n, Word32 result) {
        for (int i = 31; i >= 0; i--) {
            if (i - n >= 0) {
                Bit temp = new Bit(false);
                a.getBitN(i - n, temp);
                result.setBitN(i, temp);
            } else {
                result.setBitN(i, new Bit(false));
            }
        }
    }
}