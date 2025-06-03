/*
 * Author: Lakshay Bansal
 *
 * This file used for simulating the word- addressable memory.
 * It supports reading, writing and loading 32-bit words
 *
 */

public class Memory {

    public Word32 address = new Word32();
    public Word32 value = new Word32();
    private final Word32[] dram = new Word32[1000];

    public Memory() {

        for (int i = 0; i < 1000; i++) {
            dram[i] = new Word32();
        }
    }

    public int addressAsInt() {
        int addr = TestConverter.toInt(address);
        if (addr < 0 || addr > 999) {
            throw new IllegalArgumentException("Address out of bounds: " + addr);
        }
        return addr;
    }


    public void read() {
        int idx = addressAsInt();

        dram[idx].copy(value);
    }


    public void write() {
        int idx = addressAsInt();

        value.copy(dram[idx]);
    }


    public void load(String[] data) {
        for (int i = 0; i < data.length; i++) {
            String line = data[i];
            if (line.length() != 32) {
                throw new IllegalArgumentException("Each data string must be 32 characters long.");
            }
            Word32 word = new Word32();
            for (int j = 0; j < 32; j++) {
                char c = line.charAt(j);
                Bit bit;
                if (c == 't' || c == 'T') {
                    bit = new Bit(true);
                } else if (c == 'f' || c == 'F') {
                    bit = new Bit(false);
                } else {
                    throw new IllegalArgumentException("Invalid character in data: " + c);
                }
                word.setBitN(j, bit);
            }
            if (i >= dram.length) {
                throw new IllegalArgumentException("Program size exceeds memory capacity.");
            }
            dram[i] = word;
        }
    }


    public Word32 getWord(int index) {
        if (index < 0 || index >= dram.length) {
            throw new IllegalArgumentException("Address out of bounds: " + index);
        }
        return dram[index];
    }


    public void setWord(int address, Word32 word) {
        if (address < 0 || address >= dram.length) {
            throw new IllegalArgumentException("Address out of bounds: " + address);
        }
        word.copy(dram[address]);
    }

    public int getDRAMSize() {
        return dram.length;
    }

}
