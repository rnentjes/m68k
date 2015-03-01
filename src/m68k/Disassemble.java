package m68k;

import m68k.cpu.Cpu;
import m68k.cpu.MC68000;
import m68k.memory.AddressSpace;
import m68k.memory.MemorySpace;

import java.io.File;
import java.io.FileInputStream;

/**
 * Date: 6-10-14
 * Time: 21:02
 */
public class Disassemble {

    public static void main(String[] args) throws Exception {

        File file = new File("asm/custom.rom");
        //File file = new File("asm/Kickstart1.3.rom");
        byte [] data = new byte[(int) file.length()];

        FileInputStream in = new FileInputStream(file);

        in.read(data);

        AddressSpace memory = new MemorySpace(512);

        int pc = 0;
        for (int index = 0; index < data.length; index++) {
            memory.writeByte(pc++, data[index]);
        }

        Cpu cpu = new MC68000();
        cpu.setAddressSpace(memory);
        cpu.reset();	//init cpu

        Monitor monitor = new Monitor(cpu,memory);

        monitor.run();
        //monitor.handleDisassemble(new String [] { "d", "0", "10" });

    }
}