import java.util.*;
import java.util.regex.*;

/**
 * Assembler for our 16‑bit ISA with 32‑bit output,
 * including macro‑expansion of large immediates in copy.
 */
public class Assembler {

    public static String[] lastAssembledProgram;

    // helper function that help to convert an integer value into  a t/f.
    private static String toBitString(int value, int bits) {
        StringBuilder sb = new StringBuilder();
        for (int i = bits - 1; i >= 0; i--) {
            sb.append(((value >> i) & 1) == 1 ? 't' : 'f');
        }
        return sb.toString();
    }

    // encodes a 5- bit immediate
    private static int encodeImmediate5(int imm) {
        if (imm < -16 || imm > 15) throw new IllegalArgumentException("5-bit immediate out of range: " + imm);
        return imm & 0x1F; // mask to keep only 5 bits.
    }

    // Encodes an 11-bit immediate
    private static int encodeImmediate11(int imm) {
        if (imm < -1024 || imm > 1023) throw new IllegalArgumentException("11-bit immediate out of range: " + imm);
        return imm < 0 ? imm + 2048 : imm;
    }

    // Main assembler method: Converts assembly instruction into 16-bit words
    public static String[] assemble(String[] instructions) {
        lastAssembledProgram = instructions;
        List<String> out16 = new ArrayList<>();
        Deque<String> work = new ArrayDeque<>();
        for (String inst : instructions) work.addLast(inst.trim());

        while (!work.isEmpty()) {
            String raw = work.pollFirst();
            String cmd = raw.toLowerCase();
            Matcher m;

            // 1) Macro‑expand large immediates for copy
            m = Pattern.compile("^copy\\s+(-?\\d+)\\s+r(\\d+)$").matcher(cmd);
            if (m.matches()) {
                int imm = Integer.parseInt(m.group(1)), rdNum = Integer.parseInt(m.group(2));
                if (Math.abs(imm) > 15) {
                    List<String> seq = new ArrayList<>();
                    seq.add("copy 0 r" + rdNum);
                    int sign = imm < 0 ? -1 : 1, abs = Math.abs(imm);
                    final int CHUNK = 15;
                    int times = abs / CHUNK, rem = abs % CHUNK;
                    for (int i = 0; i < times; i++)
                        seq.add((sign > 0 ? "add " : "subtract ") + CHUNK + " r" + rdNum);
                    if (rem > 0)
                        seq.add((sign > 0 ? "add " : "subtract ") + rem + " r" + rdNum);
                    for (int i = seq.size() - 1; i >= 0; i--)
                        work.addFirst(seq.get(i));
                    continue;
                }
            }

            // 2) Fixed opcodes
            if (cmd.equals("halt"))       { out16.add("ffffffffffffffff"); continue; }
            if (cmd.equals("return"))     { out16.add("ftftffffffffffff"); continue; }

            // 3) Regex‑based encodings

            // ADD
            m = Pattern.compile("^add\\s+(-?\\d+)\\s+r(\\d+)$").matcher(cmd);
            if (m.matches()) {
                int imm = Integer.parseInt(m.group(1)), rd = Integer.parseInt(m.group(2));
                out16.add(toBitString(1,5) + 't' + toBitString(encodeImmediate5(imm),5) + toBitString(rd,5));
                continue;
            }
            m = Pattern.compile("^add\\s+r(\\d+)\\s+r(\\d+)$").matcher(cmd);
            if (m.matches()) {
                int rs = Integer.parseInt(m.group(1)), rd = Integer.parseInt(m.group(2));
                out16.add(toBitString(1,5) + 'f' + toBitString(rs,5) + toBitString(rd,5));
                continue;
            }

            // SUBTRACT
            m = Pattern.compile("^subtract\\s+(-?\\d+)\\s+r(\\d+)$").matcher(cmd);
            if (m.matches()) {
                int imm = Integer.parseInt(m.group(1)), rd = Integer.parseInt(m.group(2));
                out16.add(toBitString(5,5) + 't' + toBitString(encodeImmediate5(imm),5) + toBitString(rd,5));
                continue;
            }
            m = Pattern.compile("^subtract\\s+r(\\d+)\\s+r(\\d+)$").matcher(cmd);
            if (m.matches()) {
                int rs = Integer.parseInt(m.group(1)), rd = Integer.parseInt(m.group(2));
                out16.add(toBitString(5,5) + 'f' + toBitString(rs,5) + toBitString(rd,5));
                continue;
            }

            // AND
            m = Pattern.compile("^and\\s+(-?\\d+)\\s+r(\\d+)$").matcher(cmd);
            if (m.matches()) {
                int imm = Integer.parseInt(m.group(1)), rd = Integer.parseInt(m.group(2));
                out16.add(toBitString(2,5) + 't' + toBitString(encodeImmediate5(imm),5) + toBitString(rd,5));
                continue;
            }
            m = Pattern.compile("^and\\s+r(\\d+)\\s+r(\\d+)$").matcher(cmd);
            if (m.matches()) {
                int rs = Integer.parseInt(m.group(1)), rd = Integer.parseInt(m.group(2));
                out16.add(toBitString(2,5) + 'f' + toBitString(rs,5) + toBitString(rd,5));
                continue;
            }

            // OR
            m = Pattern.compile("^or\\s+(-?\\d+)\\s+r(\\d+)$").matcher(cmd);
            if (m.matches()) {
                int imm = Integer.parseInt(m.group(1)), rd = Integer.parseInt(m.group(2));
                out16.add(toBitString(6,5) + 't' + toBitString(encodeImmediate5(imm),5) + toBitString(rd,5));
                continue;
            }
            m = Pattern.compile("^or\\s+r(\\d+)\\s+r(\\d+)$").matcher(cmd);
            if (m.matches()) {
                int rs = Integer.parseInt(m.group(1)), rd = Integer.parseInt(m.group(2));
                out16.add(toBitString(6,5) + 'f' + toBitString(rs,5) + toBitString(rd,5));
                continue;
            }

            // MULTIPLY
            m = Pattern.compile("^multiply\\s+(-?\\d+)\\s+r(\\d+)$").matcher(cmd);
            if (m.matches()) {
                int imm = Integer.parseInt(m.group(1)), rd = Integer.parseInt(m.group(2));
                out16.add(toBitString(3,5) + 't' + toBitString(encodeImmediate5(imm),5) + toBitString(rd,5));
                continue;
            }
            m = Pattern.compile("^multiply\\s+r(\\d+)\\s+r(\\d+)$").matcher(cmd);
            if (m.matches()) {
                int rs = Integer.parseInt(m.group(1)), rd = Integer.parseInt(m.group(2));
                out16.add(toBitString(3,5) + 'f' + toBitString(rs,5) + toBitString(rd,5));
                continue;
            }

            // LEFTSHIFT
            m = Pattern.compile("^leftshift\\s+(-?\\d+)\\s+r(\\d+)$").matcher(cmd);
            if (m.matches()) {
                int imm = Integer.parseInt(m.group(1)), rd = Integer.parseInt(m.group(2));
                out16.add(toBitString(4,5) + 't' + toBitString(encodeImmediate5(imm),5) + toBitString(rd,5));
                continue;
            }
            m = Pattern.compile("^leftshift\\s+r(\\d+)\\s+r(\\d+)$").matcher(cmd);
            if (m.matches()) {
                int rs = Integer.parseInt(m.group(1)), rd = Integer.parseInt(m.group(2));
                out16.add(toBitString(4,5) + 'f' + toBitString(rs,5) + toBitString(rd,5));
                continue;
            }

            // RIGHTSHIFT
            m = Pattern.compile("^rightshift\\s+(-?\\d+)\\s+r(\\d+)$").matcher(cmd);
            if (m.matches()) {
                int imm = Integer.parseInt(m.group(1)), rd = Integer.parseInt(m.group(2));
                out16.add(toBitString(7,5) + 't' + toBitString(encodeImmediate5(imm),5) + toBitString(rd,5));
                continue;
            }
            m = Pattern.compile("^rightshift\\s+r(\\d+)\\s+r(\\d+)$").matcher(cmd);
            if (m.matches()) {
                int rs = Integer.parseInt(m.group(1)), rd = Integer.parseInt(m.group(2));
                out16.add(toBitString(7,5) + 'f' + toBitString(rs,5) + toBitString(rd,5));
                continue;
            }

            // COMPARE
            m = Pattern.compile("^compare\\s+(-?\\d+)\\s+r(\\d+)$").matcher(cmd);
            if (m.matches()) {
                int imm = Integer.parseInt(m.group(1)), rd = Integer.parseInt(m.group(2));
                out16.add(toBitString(11,5) + 't' + toBitString(encodeImmediate5(imm),5) + toBitString(rd,5));
                continue;
            }
            m = Pattern.compile("^compare\\s+r(\\d+)\\s+r(\\d+)$").matcher(cmd);
            if (m.matches()) {
                int rs = Integer.parseInt(m.group(1)), rd = Integer.parseInt(m.group(2));
                out16.add(toBitString(11,5) + 'f' + toBitString(rs,5) + toBitString(rd,5));
                continue;
            }

            // LOAD
            m = Pattern.compile("^load\\s+(-?\\d+)\\s+r(\\d+)$").matcher(cmd);
            if (m.matches()) {
                int imm = Integer.parseInt(m.group(1)), rd = Integer.parseInt(m.group(2));
                out16.add(toBitString(18,5) + 't' + toBitString(encodeImmediate5(imm),5) + toBitString(rd,5));
                continue;
            }
            m = Pattern.compile("^load\\s+r(\\d+)\\s+r(\\d+)$").matcher(cmd);
            if (m.matches()) {
                int rs = Integer.parseInt(m.group(1)), rd = Integer.parseInt(m.group(2));
                out16.add(toBitString(18,5) + 'f' + toBitString(rs,5) + toBitString(rd,5));
                continue;
            }

            // STORE
            m = Pattern.compile("^store\\s+(-?\\d+)\\s+r(\\d+)$").matcher(cmd);
            if (m.matches()) {
                int imm = Integer.parseInt(m.group(1)), rd = Integer.parseInt(m.group(2));
                out16.add(toBitString(19,5) + 't' + toBitString(encodeImmediate5(imm),5) + toBitString(rd,5));
                continue;
            }
            m = Pattern.compile("^store\\s+r(\\d+)\\s+r(\\d+)$").matcher(cmd);
            if (m.matches()) {
                int rs = Integer.parseInt(m.group(1)), rd = Integer.parseInt(m.group(2));
                out16.add(toBitString(19,5) + 'f' + toBitString(rs,5) + toBitString(rd,5));
                continue;
            }

            // COPY small immediate
            m = Pattern.compile("^copy\\s+(-?\\d+)\\s+r(\\d+)$").matcher(cmd);
            if (m.matches()) {
                int imm = Integer.parseInt(m.group(1)), rd = Integer.parseInt(m.group(2));
                out16.add(toBitString(20,5) + 't' + toBitString(encodeImmediate5(imm),5) + toBitString(rd,5));
                continue;
            }
            // COPY register→register
            m = Pattern.compile("^copy\\s+r(\\d+)\\s+r(\\d+)$").matcher(cmd);
            if (m.matches()) {
                int rs = Integer.parseInt(m.group(1)), rd = Integer.parseInt(m.group(2));
                out16.add(toBitString(20,5) + 'f' + toBitString(rs,5) + toBitString(rd,5));
                continue;
            }

            // SYSCALL
            m = Pattern.compile("^syscall\\s+(-?\\d+)$").matcher(cmd);
            if (m.matches()) {
                int imm = Integer.parseInt(m.group(1));
                out16.add(toBitString(8,5) + toBitString(encodeImmediate11(imm),11));
                continue;
            }

            // CALL
            m = Pattern.compile("^call\\s+(-?\\d+)$").matcher(cmd);
            if (m.matches()) {
                int imm = Integer.parseInt(m.group(1));
                out16.add(toBitString(9,5) + toBitString(encodeImmediate11(imm),11));
                continue;
            }

            // BRANCH
            m = Pattern.compile("^(ble|blt|bge|bgt|beq|bne)\\s+(-?\\d+)$").matcher(cmd);
            if (m.matches()) {
                String op = m.group(1);
                int imm = Integer.parseInt(m.group(2));
                int code;
                switch (op) {
                    case "ble": code=12; break;
                    case "blt": code=13; break;
                    case "bge": code=14; break;
                    case "bgt": code=15; break;
                    case "beq": code=16; break;
                    case "bne": code=17; break;
                    default: throw new AssertionError();
                }
                out16.add(toBitString(code,5) + toBitString(encodeImmediate11(imm),11));
                continue;
            }

            throw new IllegalArgumentException("Unknown instruction: " + raw);
        }

        return out16.toArray(new String[0]);// Return list as array
    }
    // Combines pairs of 16-bit words into single 32-bit words for final memory loading.
    public static String[] finalOutput(String[] words16) {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < words16.length; i += 2) {
            String w1 = words16[i];
            String w2 = i + 1 < words16.length ? words16[i+1] : "ffffffffffffffff";
            out.add(w1 + w2);// concatenate two 16- bit to one 32- bit word.
        }
        return out.toArray(new String[0]);
    }
}
