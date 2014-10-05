package m68k.cpu.assemble;

import m68k.cpu.*;
import m68k.cpu.instructions.MOVE;
import m68k.memory.AddressSpace;

import java.util.*;

/**
 * User: rnentjes
 * Date: 5-10-14
 * Time: 12:57
 *
 * http://goldencrystal.free.fr/M68kOpcodes.pdf
 * http://www.raidenii.net/files/datasheets/cpu/68000_opcodes.pdf
 */
public class Assembler {

    private Map<String, Integer> labelLocations     = new HashMap<String, Integer>();
    private Map<String, Set<LabelUsage>> labelUsage = new HashMap<String, Set<LabelUsage>>();

    public SortedSet<Binary> assemble(String [] source) {
        return new TreeSet<Binary>();
    }

    private int pc;
    private AddressSpace mmu;

    private int getLabel(String label) {
        Integer result = labelLocations.get(label);

        if (result == null) {
            result = -1;
        }

        return result;
    }

    private void setLabel(String label) {
        labelLocations.put(label, pc);

        Set<LabelUsage> usages = labelUsage.get(label);

        if (usages != null) {
            for (LabelUsage usage : usages) {
                switch(usage.size) {
                    case Byte:
                        mmu.writeByte(usage.address, pc - usage.address + 1);
                        break;
                    case Word:
                        mmu.writeWord(usage.address, pc);
                        break;
                    case Long:
                        mmu.writeLong(usage.address, pc);
                        break;
                    case Unsized:
                        throw new IllegalStateException("Label has no size! "+label);
                }
            }
        }
    }

    private void labelUsage(String label, int address, Size size, boolean relative) {
        Set<LabelUsage> addresses = labelUsage.get(label);

        if (addresses == null) {
            addresses = new HashSet<LabelUsage>();

            labelUsage.put(label, addresses);
        }

        addresses.add(new LabelUsage(label, address, size, relative));
    }

    private Integer getLabelAddress(String label) {
        return labelLocations.get(label);
    }

    public DisassembledInstruction parseLine(String line) {
        return null;
    }

    public AssembledOperand parseOperand(String operand) {
        String lower = operand.trim().toLowerCase();

        if (lower.endsWith(":")) {
            setLabel(lower.substring(0, lower.length() - 1));

            return new AssembledOperand(operand, 0, 0);
        }

        char ch = lower.charAt(0);

        int bytes = 0;
        int memory_read = 0;
        AddressingMode mode = AddressingMode.NA;
        Conditional conditional = Conditional.NA;
        int register = 0;

        switch(ch) {
            case '#':
                mode = AddressingMode.IMMEDIATE;
                memory_read = parseValue(operand.substring(1));
                break;
            case 'd':
                mode = AddressingMode.IMMEDIATE_DATA;
                register = Integer.parseInt(lower.substring(1));
                break;
            case 'a':
                mode = AddressingMode.IMMEDIATE_ADDRESS;
                register = Integer.parseInt(lower.substring(1));
                break;
            case 's':
                mode = AddressingMode.SR;
                break;
            case 'c':
                mode = AddressingMode.CCR;
                break;
        }

        return new AssembledOperand(lower, bytes, memory_read, mode, conditional, register);
    }

    public int parseValue(String value) {
        if (value.charAt(0) == '$') {
            return Integer.parseInt(value.substring(1), 16);
        } else {
            return Integer.parseInt(value);
        }
    }

    public void printSymbols() {

    }

    public static void main(String[] args) {
        DisassembledInstruction asm1 = new DisassembledInstruction(0, 0x303c, "move.w",
                new DisassembledOperand("#$1234", 2, 0x1234),
                new DisassembledOperand("d0", 0, 0));

        DisassembledInstruction asm2 = new DisassembledInstruction(4, 0x0600, "addi.n",
                new DisassembledOperand("#$34", 2, 0x0034),
                new DisassembledOperand("d0", 0, 0));

        StringBuilder builder = new StringBuilder();

        asm1.formatInstruction(builder);
        builder.append("\n");
        asm2.formatInstruction(builder);

        System.out.println(builder.toString());

        Assembler asm = new Assembler();

        AssembledOperand op1 = asm.parseOperand("D0");
        AssembledOperand op2 = asm.parseOperand("D1");

        System.out.println("OP1: " + op1);
        System.out.println("OP2: " + op2);

        Cpu cpu = new MC68000();
        MOVE move = new MOVE(cpu);

        int pc = 0;
        AssembledInstruction instruction = new AssembledInstruction("move.b", Size.Byte, op1, op2);
        DisassembledInstruction dis = move.assemble(pc, instruction);
        pc += dis.size();

        System.out.println("DIS: " + dis);

        instruction = new AssembledInstruction("move.w", Size.Word, op1, op2);
        dis = move.assemble(pc, instruction);
        pc += dis.size();

        System.out.println("DIS: " + dis);

        instruction = new AssembledInstruction("move.w", Size.Word, asm.parseOperand("#$ffff"), asm.parseOperand("d4"));
        dis = move.assemble(pc, instruction);
        pc += dis.size();

        System.out.println("DIS: " + dis);

        instruction = new AssembledInstruction("move.l", Size.Long, asm.parseOperand("#$1234ffff"), asm.parseOperand("d4"));
        dis = move.assemble(pc, instruction);
        pc += dis.size();

        System.out.println("DIS: " + dis);
    }


}
