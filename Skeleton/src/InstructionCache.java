/**
 * Simulates a simple Level 1(l1) instruction cache.
 * - Directed-mapped cache
 *  - block size: 8 instructions
 *  - Fetch blocks from l2 on miss
 */
public class InstructionCache {

    private static final int BLOCK_SIZE = 8; // Number of instruction per cache block.
    private final Word32[] block      = new Word32[BLOCK_SIZE];
    private int blockStart = -1;

    private final Processor cpu;
    private final L2Cache    l2;

    public InstructionCache(L2Cache l2, Processor cpu) {
        this.l2  = l2;
        this.cpu = cpu;
        for (int i = 0; i < BLOCK_SIZE; i++) {
            block[i] = new Word32();
        }
    }

    /**
     * Read wordIndex:
     *   L1 hit → +10 cycles
     *   L1 miss → L2.fetchInstBlock → refill L1 (+50 cycles)
     */
    public Word32 read(int wordIndex) {
        int start = (wordIndex / BLOCK_SIZE) * BLOCK_SIZE;
        if (start == blockStart) {
            cpu.clockCycles += 10;
        } else {
            Word32[] words = l2.fetchInstBlock(start);
            System.arraycopy(words, 0, block, 0, BLOCK_SIZE); // copy  l2 block into l1
            blockStart = start; // update block start address
            cpu.clockCycles += 50; // refill cost
        }
        return block[wordIndex - blockStart]; // return correct word from block
    }
}
