/*
Author - Lakshay Bansal
    * This adder class used for a bit level arithmetic on 32 bit words.
 */

public class Adder {

    public static void add(Word32 a, Word32 b, Word32 result) {
        Bit carry = new Bit(false);
        for (int i = 31; i >= 0; i--) {
            Bit bitA = new Bit(false);
            Bit bitB = new Bit(false);
            a.getBitN(i, bitA);
            b.getBitN(i, bitB);

            Bit xorAB = new Bit(false);
            Bit sumBit = new Bit(false);
            Bit.xor(bitA, bitB, xorAB);
            Bit.xor(xorAB, carry, sumBit);
            result.setBitN(i, sumBit);

            Bit andAB = new Bit(false);
            Bit andXorCarry = new Bit(false);
            Bit newCarry = new Bit(false);
            Bit.and(bitA, bitB, andAB);
            Bit.and(xorAB, carry, andXorCarry);
            Bit.or(andAB, andXorCarry, newCarry);
            carry.assign(newCarry.getValue());

        }

    }


    /*
    This class used for subtraction using two's complement arithmetic.
     */
    public static void subtract(Word32 a, Word32 b, Word32 result) {
        Word32 negB = new Word32();
        Bit temp = new Bit(false);
        Bit negTemp = new Bit(false);
        for (int i = 0; i < 32; i++) {
            b.getBitN(i, temp);
            Bit.not(temp, negTemp);
            negB.setBitN(i, negTemp);
        }


        Word32 one = new Word32();
        one.setBitN(31, new Bit(true));

        Word32 negBlusOne = new Word32();
        add(negB, one, negBlusOne);
        add(a, negBlusOne, result);
    }

}