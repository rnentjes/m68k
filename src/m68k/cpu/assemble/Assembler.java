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
import java.text.ParseException;
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
        // data transfer instructions
        commandMapping.put("exg", new EXG(null));
        commandMapping.put("lea", new LEA(null));
        commandMapping.put("link", new LINK(null));
        commandMapping.put("move", new MOVE(null));
        commandMapping.put("movea", new MOVE(null));
        commandMapping.put("movem", new MOVEM(null));

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

    public int getPc() {
        return pc;
    }

    public void setPc(int pc) {
        this.pc = pc;
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

    public DisassembledInstruction parseLine(String line) throws ParseException {
        return parseLine(-1, line);
    }

    public DisassembledInstruction parseLine(int lineNumber, String line) throws ParseException {
        OperandParser operandParser = new OperandParser();
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
                setLabel(parts[0].trim(), operandParser.parseValue(parts[1].trim()));
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
            operand1 = operandParser.parse(size, pc, lineNumber, op1);
        }
        if (numberOfParts >= 2) {
            operand2 = operandParser.parse(size, pc, lineNumber,op2);
        }

        if (commandMapping.get(command) == null) {
            throw new ParseException("Mnemonic '"+command+"' not found!", lineNumber);
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

    public void printSymbols() {
        for (Map.Entry<String, Integer> entry : labelLocations.entrySet()) {
            System.out.println(String.format("%32s %08x", entry.getKey(), entry.getValue()));
        }
    }

    public static void main(String[] args) throws IOException, ParseException {
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
