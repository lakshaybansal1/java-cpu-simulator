/*
 * Author: Lakshay Bansal.
 *  This class multiplier used for multiply of 32 words(used word32 file).

 *
 */
public class Multiplier {

    public static void multiply(Word32 a, Word32 b, Word32 result) {


        // So staring with a product of zero
        Word32 product = new Word32();

        for (int i = 0; i < 32; i++) {

            Bit curnt_Bit = new Bit(false); // getting the current bit from b.
            b.getBitN(i, curnt_Bit);

            if (curnt_Bit.getValue() == Bit.boolValues.TRUE) {
                int shiftAmount = 31 - i;

                Word32 shifted = new Word32();
                Shifter.LeftShift(a, shiftAmount, shifted);

                Word32 sum = new Word32();
                Adder.add(product, shifted, sum);
                sum.copy(product); // update product with new sum
            }
        }
        product.copy(result);

    }

}
