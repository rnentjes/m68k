package m68k.cpu.assemble;

import junit.framework.TestCase;
import m68k.cpu.DisassembledInstruction;
import m68k.memory.MemorySpace;

import java.text.ParseException;

/**
 * User: rnentjes
 * Date: 8-10-14
 * Time: 11:56
 */
public abstract class AssemblerTestCase extends TestCase {

    MemorySpace memory;
    Assembler asm;

    public void setUp() {
        memory = new MemorySpace(1);
        asm = new Assembler(memory);
    }

    protected void checkAsm(String asmline, String bytes) {

        try {
            DisassembledInstruction di = asm.parseLine(asmline);
            int index = 0;

            for (Byte byt : di.bytes()) {
                assertTrue(bytes.length() >= index + 2);

                byte ref = (byte) Integer.parseInt(bytes.substring(index, index + 2), 16);

                assertEquals("Wrong opcodes: '" + asmline + "' -> " + bytes, byt.byteValue(), ref);

                index += 2;

                while (index < bytes.length() - 1 && bytes.charAt(index) == ' ') {
                    index++;
                }
            }

            while(index < bytes.length()) {
                assertTrue("Not enough opcodes!", bytes.charAt(index) == ' ');
            }
        } catch (ParseException e) {
            assertFalse("ParseException in line "+ e.getErrorOffset() + ": '" + asmline + "' -> " + bytes , true);
        }

    }

}
