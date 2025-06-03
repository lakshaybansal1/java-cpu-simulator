import java.util.Arrays;

/**
 * SIMULATES a level 2 (l2) cache for instruction and data.
 *  - 4 groups, each with an 8- word block(32 words total)
 */
public class L2Cache {
    private static final int GROUP_COUNT = 4; // Number og groups (sets)

    private static final int BLOCK_SIZE  = 8; // words per block

    // instruction blocks + tags (cache storage).
    private final Word32[][] instBlocks  = new Word32[GROUP_COUNT][BLOCK_SIZE]; // 4 group of 8-words blocks
    private final int[]      instStarts  = new int[GROUP_COUNT]; // start address tags for each group

    // connections to the memory and CPU
    private final Memory    mem;
    private final Processor cpu;

    /**
     *Constructor: Initializes L2 caache with memory, processor references.
     * Allocates Word32 objects for all instruction blocks.
     */
    public L2Cache(Memory mem, Processor cpu) {
        this.mem = mem;
        this.cpu = cpu;
        Arrays.fill(instStarts, -1);
        // allocate Word32 storage
        for (int g = 0; g < GROUP_COUNT; g++) {
            for (int i = 0; i < BLOCK_SIZE; i++) {
                instBlocks[g][i] = new Word32();
            }
        }
    }

    /**
     * Fetch 8-word instruction block at 'start'.
     * - if block already cached (hit):  Add +20 cycles.
     * if block not cached (missed): refill from memory, add + 350 cycles.
     *
     * @param start Address of the first word of the block.
     * @return Array of 8 Word32 instruction.
     */
    public Word32[] fetchInstBlock(int start) {
        int group = (start / BLOCK_SIZE) % GROUP_COUNT;
        if (instStarts[group] == start) {
            // L2 cache hit
            cpu.clockCycles += 20;
        } else {
            // L2 cache miss -> refill from memory
            instStarts[group] = start; // update tag to new start.
            for (int i = 0; i < BLOCK_SIZE; i++) {
                instBlocks[group][i] = mem.getWord(start + i); // load words from main memory
            }
            cpu.clockCycles += 350;
        }
        return instBlocks[group];
    }

    /**
     * Read a data word from address 'addr'.
     * Write-through cache: always +50 cycles.
     */
    public int readData(int addr) {
        cpu.clockCycles += 50; // access delay for data read
        return cpu.dataMemory[addr]; // read directly from backing data memory
    }

    /**
     * Write a data word to address 'addr'.
     * Write-through cache: update main memory +50 cycles.
     *
     * @param addr Address to write to
     * @param value cache policy: updates main memory and costs +50 cycles.
     */
    public void writeData(int addr, int value) {
        cpu.dataMemory[addr] = value; // update backing data memory
        cpu.clockCycles += 50; // write penalty
    }
}
