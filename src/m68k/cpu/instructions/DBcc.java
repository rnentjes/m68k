package m68k.cpu.instructions;

import m68k.cpu.*;
import m68k.cpu.assemble.AssembledInstruction;
import m68k.cpu.assemble.AssembledOperand;
import m68k.cpu.assemble.Labels;

/*
//  M68k - Java Amiga MachineCore
//  Copyright (c) 2008-2010, Tony Headford
//  All rights reserved.
//
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
//  following conditions are met:
//
//    o  Redistributions of source code must retain the above copyright notice, this list of conditions and the
//       following disclaimer.
//    o  Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
//       following disclaimer in the documentation and/or other materials provided with the distribution.
//    o  Neither the name of the M68k Project nor the names of its contributors may be used to endorse or promote
//       products derived from this software without specific prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
//  INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
//  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
//  SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
//  SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
//  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
//  OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
*/
public class DBcc implements InstructionHandler
{
	protected final Cpu cpu;
	protected static final String[] names = { "dbt", "dbra", "dbhi", "dbls", "dbcc", "dbcs", "dbne", "dbeq",
												"dbvc", "dbvs", "dbpl", "dbmi", "dbge", "dblt", "dbgt", "dble"};
	public DBcc(Cpu cpu)
	{
		this.cpu = cpu;
	}

	public void register(InstructionSet is)
	{
		int base = 0x50c8;
		Instruction i = new Instruction() {
			public int execute(int opcode)
			{
				return dbxx(opcode);
			}
			public DisassembledInstruction disassemble(int address, int opcode)
			{
				return disassembleOp(address, opcode);
			}
		};

		for(int cc = 0; cc < 16; cc++)
		{
			for(int r = 0; r < 8; r++)
			{
				is.addInstruction(base + (cc << 8) + r, i);
			}
		}
	}

    @Override
    public DisassembledInstruction assemble(int address, AssembledInstruction instruction, Labels labels) {
        int opcode = nameToOpcode(instruction.instruction);

        if (opcode == -1) {
            throw new IllegalStateException("Unknown opcode "+instruction.instruction);
        }

        opcode = opcode << 8;
        opcode |= 0x50c8;

        AssembledOperand op1 = (AssembledOperand)instruction.op1;
        AssembledOperand op2 = (AssembledOperand)instruction.op2;

        int labelAddress;

        labelAddress = labels.getLabel(op2.operand, address + 2, true, Size.Word);

        int offset = 0;

        if (labelAddress >= 0) {
            offset = labelAddress - address - 2;
        }

        int bytes = 2;
        int memory_read = offset;

        opcode |= op1.register;

        return new DisassembledInstruction(address, opcode, instruction.instruction,
                new DisassembledOperand(op1.operand, op1.bytes, op2.memory_read),
                new DisassembledOperand(op2.operand, bytes, memory_read));
    }

    protected int nameToOpcode(String name) {
        name = name.trim().toLowerCase();

        if (name.indexOf('.') > -1) {
            name = name.substring(0, name.indexOf('.'));
        }

        for (int index = 0; index < names.length; index++) {
            if (name.equals(names[index])) {
                return index;
            }
        }

        return -1;
    }

    protected final int dbxx(int opcode)
	{
		int reg = (opcode & 0x07);
		int pc = cpu.getPC();
		int dis = cpu.fetchPCWordSigned();
		int time;
		int count = cpu.getDataRegisterWordSigned(reg) - 1;
		cpu.setDataRegisterWord(reg, count);

		if(cpu.testCC((opcode >> 8) & 0x0f))
		{
			// condition met
			time = 12;
		}
		else
		{
			if(count == -1)
			{
				// counter expired
				time = 14;
			}
			else
			{
				// loop
				cpu.setPC(pc + dis);
				time = 10;
			}
		}
		return time;
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode)
	{
		int cc = (opcode >> 8) & 0x0f;
		int dis = cpu.readMemoryWordSigned(address + 2);

		DisassembledOperand reg = new DisassembledOperand(String.format("d%d", (opcode & 0x07)));
		//word displacement
		DisassembledOperand where = new DisassembledOperand(String.format("$%08x", dis + address + 2), 2, dis);

		return new DisassembledInstruction(address, opcode, names[cc], reg, where);
	}
}
