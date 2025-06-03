import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for verfying that the  processor correctly sums data
 * under different memory access patter, and to report the clock cyles.
 * for performance comparison.
 */

public class Cachetesting {
    /**
     * Test summing a contiguous array of 20 words in forward order.
     * Verifies the result and print clock cycle for performance.
     */



    @Test
    public void sumArrayForward() {

        String program = """
            copy 200 r1
            copy 0   r2

            """ + "load r1 r3\nadd r3 r2\nadd 1 r1\n".repeat(20) + """
            syscall 0
            halt
            """;
        // Execute and capture processor state
        Processor p = runArray(program);

        // Extract the computed sum from output and assert correctness
        int sum = extractR2(p);


        assertEquals(210, sum, "Expected sum 210, but got " + sum);
        // Print cycles for performance analysis.
        System.out.println("Forward Sum Clock Cycles: " + p.clockCycles);

    }
    /**
     * Test summing a contiguous array of 20 words in backward order.
     * Useful for measuring cache effects with reverse traversal.
     */
    @Test
    public void sumArrayBackward() {
        // Set r1 to last element at address 219, zero r2
        String program = """
            copy 219 r1
            copy 0   r2

      
            """ + "load r1 r3\nadd r3 r2\nsubtract 1 r1\n".repeat(20) + """
            syscall 0
            halt
            """;

        Processor p = runArray(program);
        int sum = extractR2(p);
        assertEquals(210, sum, "Expected sum 210, but got " + sum);
        System.out.println("Backward Sum Clock Cycles: " + p.clockCycles);
    }

    /**
     * Test summing values in a linked list of 20 nodes.
     * Each node has a value followed by a pointer to next node.
     */
    @Test
    public void sumLinkedList() {
        String program = """
            copy 200 r1    // head ptr
            copy 0   r2    // sum

            """ + "load r1 r3\nadd r3 r2\nadd 1 r1\nload r1 r1\n".repeat(19) + """
            load r1 r3
            add r3 r2
            syscall 0
            halt
            """;

        Processor p = runLinkedList(program);
        int sum = extractR2(p);
        assertEquals(210, sum, "Expected sum 210, but got " + sum);
        System.out.println("Linkedlist sum cycles: " + p.clockCycles);

    }

    /**
     * Helper to load, initialize data memory for array tests,
     * run the processor, and return its state.
     */
    private static Processor runArray(String program) {
        Processor p = loadProcessor(program);
        for (int i = 0; i < 20; i++) {
            p.dataMemory[200 + (program.startsWith("copy 219") ? (19 - i) : i)] = i + 1;
        }
        p.run();
        return p;
    }

    /**
     * Helper to build a simple linked list in data memory and execute.
     */
    private static Processor runLinkedList(String program) {
        Processor p = loadProcessor(program);
        for (int i = 0; i < 20; i++) {
            p.dataMemory[200 + 2 * i] = i + 1;
            if (i < 19) p.dataMemory[200 + 2 * i + 1] = 200 + 2 * (i + 1);
        }
        p.run();
        return p;
    }
    /**
     * Common loader: assemble, convert to binary words, load into memory.
     */
    private static Processor loadProcessor(String program) {
        List<String> lines = program.lines()
                .map(line -> line.replaceAll("//.*$", "").trim())
                .filter(line -> !line.isEmpty())
                .toList();
        String[] asm = lines.toArray(new String[0]);
        String[] bits16 = Assembler.assemble(asm);
        String[] words32 = Assembler.finalOutput(bits16);
        Memory mem = new Memory();
        mem.load(words32);
        return new Processor(mem);
    }

    /**
     * Extracts the computed sum from the processor's output stream.
     * Parses the 'r2:' line, converts from bit-string to integer.
     */
    private static int extractR2(Processor p) {
        String r2Line = p.output.stream()
                .filter(l -> l.startsWith("r2:"))
                .findFirst()
                .orElseThrow();
        String bits = r2Line.substring(3).replace(",", "").replace('t', '1').replace('f', '0');
        return (int) Long.parseLong(bits, 2);
    }
}
