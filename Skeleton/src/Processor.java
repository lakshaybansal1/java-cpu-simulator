import java.util.LinkedList;
import java.util.Stack;

/**
 * Simulates a processor with Instruction Cache (L1) and L2 Cache.
 */
public class Processor {
    private final Memory mem;
    private final InstructionCache icache;
    private final L2Cache l2cache;

    public LinkedList<String> output = new LinkedList<>();


    // General-purpose registers
    private final int[] registers = new int[32];
    // Backing store for data memory (no data cache)
    public final int[] dataMemory = new int[1000];

    // Program counter (counts half-words)
    private int pc = 0;

    // Call/return stack and condition flags
    private final Stack<Integer> callStack = new Stack<>();
    private boolean flagLess  = false;
    private boolean flagEqual = false;

    // Cycle counter
    int clockCycles = 0;


    /**
     * Processor Constructor:  initializes caches and memory
     * @param mem
     */
    public Processor(Memory mem) {
        this.mem     = mem;
        this.l2cache = new L2Cache(mem, this);
        this.icache  = new InstructionCache(l2cache, this);
    }

    /**
     * Run the processor until halt (opcode 0).
     */
    public void run() {
        while (true) {
            Word16 instrWord = fetch();
            Instruction instr = decode(instrWord);
            if (instr.opcode == 0) break;  // halt
            execute(instr);
        }
        System.out.println("Clock cycles: " + clockCycles);
    }

    /**
     * Fetch next instruction via L1/L2 hierarchy.
     */
    private Word16 fetch() {
        int wordIndex = pc / 2;
        // Goes through L1 → L2 → Memory
        Word32 fullWord = icache.read(wordIndex);

        Word16 inst = new Word16();
        if ((pc & 1) == 0) {
            fullWord.getTopHalf(inst); // get top 16 bits if pc is even
        } else {
            fullWord.getBottomHalf(inst); // get bottom 16 bits if pc is odd
        }
        pc++;
        return inst;
    }

    /** Decode 16-bit instruction word into opcode and operands */
    private Instruction decode(Word16 word) {
        int bits   = word.toInt() & 0xFFFF;
        int opcode = (bits >> 11) & 0x1F;

        Instruction instr = new Instruction();
        instr.opcode = opcode;

        // Opcodes with 11-bit signed immediate
        if (opcode == 8 || opcode == 9 || opcode == 10 || (opcode >= 12 && opcode <= 17)) {
            int imm = bits & 0x7FF;
            instr.immediate = (imm << 21) >> 21; // Sign-extend
        } else {
            int fmt = (bits >> 10) & 1;
            if (fmt == 0) {
                // Register format: srcReg and detReg
                instr.srcReg      = (bits >> 5) & 0x1F;
                instr.destReg     = bits & 0x1F;
                instr.isImmediate = false;
            } else {
                // Immediate Format
                int imm = (bits >> 5) & 0x1F;
                instr.immediate    = (imm << 27) >> 27; // Sign-extend
                instr.destReg      = bits & 0x1F;
                instr.isImmediate  = true;
            }
        }
        return instr;
    }

    /** Execute instruction and update clockCycles */
    private void execute(Instruction instr) {
        switch (instr.opcode) {
            case 0:
                break;  // halt

            // ALU operations--
            case 1:  // add
                registers[instr.destReg] += instr.isImmediate
                        ? instr.immediate
                        : registers[instr.srcReg];
                clockCycles += 2;
                break;
            case 2:  // and
                registers[instr.destReg] &= instr.isImmediate
                        ? instr.immediate
                        : registers[instr.srcReg];
                clockCycles += 2;
                break;
            case 3:  // mul
                registers[instr.destReg] *= instr.isImmediate
                        ? instr.immediate
                        : registers[instr.srcReg];
                clockCycles += 10;
                break;
            case 4:  // shl
                registers[instr.destReg] <<= instr.isImmediate
                        ? instr.immediate
                        : registers[instr.srcReg];
                clockCycles += 2;
                break;
            case 5:  // sub
                registers[instr.destReg] -= instr.isImmediate
                        ? instr.immediate
                        : registers[instr.srcReg];
                clockCycles += 2;
                break;
            case 6:  // or
                registers[instr.destReg] |= instr.isImmediate
                        ? instr.immediate
                        : registers[instr.srcReg];
                clockCycles += 2;
                break;
            case 7:  // shr
                registers[instr.destReg] >>= instr.isImmediate
                        ? instr.immediate
                        : registers[instr.srcReg];
                clockCycles += 2;
                break;

            // --- Syscalls, calls, branches ---
            case 8:  // syscall
                handleSyscall(instr.immediate);
                clockCycles += 2;
                break;
            case 9:  // call
                callStack.push(pc);
                pc = pc + instr.immediate * 2 - 2;
                clockCycles += 2;
                break;
            case 10: // return
                if (!callStack.isEmpty()) pc = callStack.pop();
                clockCycles += 2;
                break;
            case 11: // compare
                if (instr.isImmediate) {
                    flagLess  = registers[instr.destReg] < instr.immediate;
                    flagEqual = registers[instr.destReg] == instr.immediate;
                } else {
                    flagLess  = registers[instr.destReg] < registers[instr.srcReg];
                    flagEqual = registers[instr.destReg] == registers[instr.srcReg];
                }
                clockCycles += 2;
                break;
            case 12: // brle
                if (flagLess || flagEqual) pc = pc + instr.immediate * 2 - 2;
                clockCycles += 2;
                break;
            case 13: // brl
                if (flagLess) pc = pc + instr.immediate * 2 - 2;
                clockCycles += 2;
                break;
            case 14: // brnl
                if (!flagLess) pc = pc + instr.immediate * 2 - 2;
                clockCycles += 2;
                break;
            case 15: // brnle
                if (!flagLess && !flagEqual) pc = pc + instr.immediate * 2 - 2;
                clockCycles += 2;
                break;
            case 16: // bre
                if (flagEqual) pc = pc + instr.immediate * 2 - 2;
                clockCycles += 2;
                break;
            case 17: // brne
                if (!flagEqual) pc = pc + instr.immediate * 2 - 2;
                clockCycles += 2;
                break;

            // Data load/store via array
            case 18: { // load via L2
                int addr = instr.isImmediate
                        ? registers[instr.destReg] + instr.immediate
                        : registers[instr.srcReg];
                int val = l2cache.readData(addr);
                registers[instr.destReg] = val;
                break;
            }
            case 19: { // store via L2
                int addr = registers[instr.destReg];
                int val  = instr.isImmediate
                        ? instr.immediate
                        : registers[instr.srcReg];
                l2cache.writeData(addr, val);
                break;
            }
            case 20: // move
                registers[instr.destReg] = instr.isImmediate
                        ? instr.immediate
                        : registers[instr.srcReg];
                clockCycles += 2;
                break;

            default:
                throw new IllegalArgumentException("Unknown opcode: " + instr.opcode);
        }
    }

    /**
     * Handle syscall operations: 0=print registers, 1=print memory
     */
    private void handleSyscall(int code) {
        if (code == 0) printRegisters();
        else if (code == 1) printMemory();
    }

    /**
     * Print all register contents.
     */
    private void printRegisters() {
        for (int i = 0; i < 32; i++) {
            String line = "r" + i + ":" + toBitString(registers[i]);
            output.add(line);
            System.out.println(line);
        }
    }
    /**
     * Print all memory contents.
     */
    private void printMemory() {
        for (int i = 0; i < dataMemory.length; i++) {
            String line = i + ":" + toBitString(dataMemory[i]);
            output.add(line);
            System.out.println(line);
        }
    }

    /**
     * Convert an integer to a bit string with 't' for 1, 'f' for 0.
     */
    private String toBitString(int v) {
        StringBuilder sb = new StringBuilder();
        for (int i = 31; i >= 0; i--) {
            sb.append(((v >> i) & 1) == 1 ? 't' : 'f').append(',');
        }
        return sb.toString();
    }

    /**
     * Helper class to represent a decoded instruction.
     */
    private static class Instruction {
        int     opcode;
        boolean isImmediate;
        int     srcReg;
        int     destReg;
        int     immediate;
    }
}
