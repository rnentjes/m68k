package m68k.cpu.assemble;

import m68k.cpu.DisassembledInstruction;
import m68k.cpu.InstructionHandler;
import m68k.cpu.Size;
import m68k.cpu.instructions.*;
import m68k.memory.AddressSpace;
import m68k.memory.MemorySpace;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * User: rnentjes
 * Date: 5-10-14
 * Time: 12:57
 *
 * http://goldencrystal.free.fr/M68kOpcodes.pdf
 * http://www.raidenii.net/files/datasheets/cpu/68000_opcodes.pdf
 * http://oldwww.nvg.ntnu.no/amiga/MC680x0_Sections/index.HTML
 */
public class Assembler {

    private Map<String, Integer> labelLocations     = new HashMap<String, Integer>();
    private Map<String, Set<LabelUsage>> labelUsage = new HashMap<String, Set<LabelUsage>>();

    private static Map<String, InstructionHandler> commandMapping = new HashMap<String, InstructionHandler>();

    {
        commandMapping.put("move", new MOVE(null));
        commandMapping.put("add", new ADD(null));
        commandMapping.put("adda", new ADDA(null));
        commandMapping.put("addi", new ADDI(null));
        commandMapping.put("addq", new ADDQ(null));
    }

    public SortedSet<Binary> assemble(String [] source) {
        return new TreeSet<Binary>();
    }

    private int pc;
    private AddressSpace mmu;

    public Assembler(AddressSpace mmu) {
        this.mmu = mmu;
    }

    private int getLabel(String label) {
        Integer result = labelLocations.get(label);

        if (result == null) {
            result = -1;
        }

        return result;
    }

    private void setLabel(String label) {
        setLabel(label, pc);
    }

    private void setLabel(String label, int address) {
        labelLocations.put(label, address);

        Set<LabelUsage> usages = labelUsage.get(label);

        if (usages != null) {
            for (LabelUsage usage : usages) {
                switch(usage.size) {
                    case Byte:
                        mmu.writeByte(usage.address, address - usage.address + 1);
                        break;
                    case Word:
                        mmu.writeWord(usage.address, address);
                        break;
                    case Long:
                        mmu.writeLong(usage.address, address);
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
        String lower = line.trim().toLowerCase();

        if (lower.isEmpty() || lower.startsWith(";")) {
            // comment
            return new DisassembledInstruction(pc, 0, line);
        }

        if (lower.endsWith(":")) {
            setLabel(lower.substring(0, lower.length() - 1));

            return new DisassembledInstruction(pc, 0, line);
        }

        if (lower.contains(" equ ")) {
            String [] parts = lower.split("equ");

            if (parts.length != 2) {
                throw new IllegalArgumentException("Can't parse '" + line + "'");
            } else {
                setLabel(parts[0].trim(), parseValue(parts[1].trim()));
            }

            return new DisassembledInstruction(pc, 0, line);
        }

        String mnemonic = lower;
        String command;
        String ops;
        String op1 = "";
        String op2 = "";
        Size size = Size.Unsized;
        int numberOfParts = 0;
        AssembledOperand operand1 = new AssembledOperand("", 0, 0);
        AssembledOperand operand2 = new AssembledOperand("", 0, 0);

        if (lower.indexOf(' ') > 0) {
            mnemonic = lower.substring(0, lower.indexOf(' '));
            ops = lower.substring(lower.indexOf(' ')).trim();
            int lastComma = ops.lastIndexOf(',');

            if (lastComma == -1) {
                numberOfParts = 1;
                op1 = ops.trim();
            } else {
                numberOfParts = 2;
                op1 = ops.substring(0, lastComma).trim();
                op2 = ops.substring(lastComma+1).trim();
            }
        }

        if (mnemonic.indexOf('.') > -1) {
            command = mnemonic.substring(0, mnemonic.indexOf('.'));
            char sz = mnemonic.charAt(mnemonic.indexOf('.') + 1);

            switch (sz) {
                case 'b':
                    size = Size.Byte;
                    break;
                case 'w':
                    size = Size.Word;
                    break;
                case 'l':
                    size = Size.Long;
                    break;
            }
        } else {
            command = mnemonic;
        }

        if (numberOfParts >= 1) {
            operand1 = parseOperand(size, op1);
        }
        if (numberOfParts >= 2) {
            operand2 = parseOperand(size, op2);
        }

        if (commandMapping.get(command) == null) {
            throw new IllegalArgumentException("Mnemonic '"+command+"' not found!");
        } else {
            InstructionHandler handler = commandMapping.get(command);

            AssembledInstruction instruction = new AssembledInstruction(mnemonic, size, operand1, operand2);

            DisassembledInstruction disassembled = handler.assemble(pc, instruction);

            for (Byte byt : disassembled.bytes()) {
                mmu.writeByte(pc++, byt);
            }

            return disassembled;
        }
    }

    public AssembledOperand parseOperand(Size size, String operand) {
        String lower = operand.trim().toLowerCase().replaceAll("\\s", "");

        char ch = lower.charAt(0);

        int bytes = 0;
        int memory_read = 0;
        AddressingMode mode = AddressingMode.NA;
        Conditional conditional = Conditional.NA;
        int register = 0;
        int ext_reg = 0;
        int ext_data = 0;
        Size ext_size = Size.Unsized;

        switch(ch) {
            case '#':
                mode = AddressingMode.IMMEDIATE;
                memory_read = parseValue(operand.substring(1));
                register = 4;
                if (size == Size.Long) {
                    bytes = 4;
                } else {
                    bytes = 2;
                }
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
            default:
                if (lower.startsWith("-(") && lower.endsWith(")")) {
                    // -(a0)
                    mode = AddressingMode.INDIRECT_POST;
                    register = Integer.parseInt(lower.substring(3, 4));
                } else if (lower.startsWith("(") && lower.endsWith(")+")) {
                    // (a0)+
                    mode = AddressingMode.INDIRECT_PRE;
                    register = Integer.parseInt(lower.substring(2, 3));
                } else if (lower.startsWith("(") && lower.endsWith(")")) {
                    mode = AddressingMode.INDIRECT;
                    register = Integer.parseInt(lower.substring(2, 3));
                } else if (lower.contains("(") && lower.endsWith(")")) {
                    int indexOpen = lower.indexOf('(');
                    int indexClose = lower.indexOf(')');
                    // $1234(a1) / $12(a0, a1.w)
                    if (lower.contains(",")) {
                        if (lower.contains("pc")) {
                            // $1234(pc)
                            mode = AddressingMode.PC_DISP;
                            memory_read = parseValue(lower.substring(0, indexOpen));
                            bytes = 2;
                        } else {
                            // $1234(a1)
                            mode = AddressingMode.INDIRECT_DISP;
                            memory_read = parseValue(lower.substring(0, indexOpen));
                            bytes = 2;
                            register = Integer.parseInt(lower.substring(indexOpen + 2, indexOpen + 3));
                        }
                    } else {
                        if (lower.contains("pc")) {
                            // $12(pc, d0.w)
                            mode = AddressingMode.PC_INDEX;
                            ext_data = parseValue(lower.substring(0, indexOpen));
                            bytes = 2;
                            register = Integer.parseInt(lower.substring(indexOpen + 2, indexOpen + 3));
                            // todo parse
                            ext_reg = 0;
                            ext_size = Size.Word;
                        } else {
                            // $12(a0, d0.w)
                            mode = AddressingMode.INDIRECT_INDEX;
                            ext_data = parseValue(lower.substring(0, indexOpen));
                            register = Integer.parseInt(lower.substring(indexOpen + 2, indexOpen + 3));
                            // todo parse
                            ext_reg = 0;
                            ext_size = Size.Word;
                            bytes = 2;
                        }
                    }
                } else if (lower.endsWith(".w")) {
                    mode = AddressingMode.ABSOLUTE_NEAR;
                } else if (lower.endsWith(".l")) {
                    mode = AddressingMode.ABSOLUTE_FAR;
                }
                break;
        }

        return new AssembledOperand(lower, bytes, memory_read, mode, conditional, register, ext_reg, ext_data, ext_size);
    }

    public int parseValue(String value) {
        if (value.charAt(0) == '$') {
            return Integer.parseInt(value.substring(1), 16);
        } else if (value.charAt(0) == '%') {
            return Integer.parseInt(value.substring(1), 2);
        } else {
            return Integer.parseInt(value);
        }
    }

    public void printSymbols() {
        for (Map.Entry<String, Integer> entry : labelLocations.entrySet()) {
            System.out.println(String.format("%32s %08x", entry.getKey(), entry.getValue()));
        }
    }

    public static void main(String[] args) throws IOException {
        MemorySpace memory = new MemorySpace(512);
        Assembler asm = new Assembler(memory);
        String filename = "test.asm";

        if (args.length > 0) {
            filename = args[0];
        }

        FileOutputStream out = new FileOutputStream("b.out");
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;

        while ((line = reader.readLine()) != null) {
            DisassembledInstruction dis = asm.parseLine(line);

            System.out.println(dis);

            List<Byte> bytes = dis.bytes();

            for (Byte byt : bytes) {
                out.write((byt & 0xff));
            }
        }

        out.close();
        reader.close();

        asm.printSymbols();
    }

}
