package m68k.cpu.instructions;

import m68k.cpu.*;
import m68k.cpu.assemble.AddressingMode;
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

public class SUBX implements InstructionHandler
{
	protected final Cpu cpu;

	public SUBX(Cpu cpu)
	{
		this.cpu = cpu;
	}

	public final void register(InstructionSet is)
	{
		int base;
		Instruction i;

		// register mode
		for(int sz = 0; sz < 3; sz++)
		{
			if(sz == 0)
			{
				// subx byte (reg)
				base = 0x9100;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return subx_byte_reg(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Byte);
					}
				};
			}
			else if(sz == 1)
			{
				// subx word (reg)
				base = 0x9140;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return subx_word_reg(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Word);
					}
				};
			}
			else
			{
				// subx long (reg)
				base = 0x9180;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return subx_long_reg(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Long);
					}
				};
			}
			for(int regx = 0; regx < 8; regx++)
			{
				for(int regy = 0; regy < 8; regy++)
				{
					is.addInstruction(base + (regx << 9) + regy, i);
				}
			}
		}

		// Memory mode
		for(int sz = 0; sz < 3; sz++)
		{
			if(sz == 0)
			{
				// subx byte (mem)
				base = 0x9108;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return subx_byte_mem(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Byte);
					}
				};
			}
			else if(sz == 1)
			{
				// subx word (mem)
				base = 0x9148;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return subx_word_mem(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Word);
					}
				};
			}
			else
			{
				// subx long (mem)
				base = 0x9188;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return subx_long_mem(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Long);
					}
				};
			}
			for(int regx = 0; regx < 8; regx++)
			{
				for(int regy = 0; regy < 8; regy++)
				{
					is.addInstruction(base + (regx << 9) + regy, i);
				}
			}
		}
	}

    @Override
    public DisassembledInstruction assemble(int address, AssembledInstruction instruction, Labels labels) {
        int opcode = 0x9100;

        switch (instruction.size) {
            case Long:
                opcode |= 0x80;
                break;
            default:
                opcode |= 0x40;
                break;
        }

        AssembledOperand op1 = (AssembledOperand)instruction.op1;
        AssembledOperand op2 = (AssembledOperand)instruction.op2;

        if (op1.mode == AddressingMode.INDIRECT_PRE && op2.mode == AddressingMode.INDIRECT_PRE) {
            opcode |= 0x8;
        } else if (op1.mode != AddressingMode.IMMEDIATE_DATA || op2.mode != AddressingMode.IMMEDIATE_DATA) {
            // error
            throw new IllegalStateException("Illegal addressing mode!");
        }

        opcode |= op1.register;
        opcode |= op2.register << 9;

        return new DisassembledInstruction(address, opcode, instruction.instruction,
                new DisassembledOperand(op1.operand, op1.bytes, op1.memory_read),
                new DisassembledOperand(op2.operand, op2.bytes, op2.memory_read));
    }

    protected int subx_byte_reg(int opcode)
	{
		int s = cpu.getDataRegisterByteSigned((opcode & 0x07));
		int d = cpu.getDataRegisterByteSigned((opcode >> 9) & 0x07);
		int r = d - s - (cpu.isFlagSet(Cpu.X_FLAG) ? 1 : 0);
		cpu.setDataRegisterByte((opcode >> 9) & 0x07, r);
		cpu.calcFlags(InstructionType.SUBX, s, d, r, Size.Byte);
		return 4;
	}

	protected int subx_word_reg(int opcode)
	{
		int s = cpu.getDataRegisterWordSigned((opcode & 0x07));
		int d = cpu.getDataRegisterWordSigned((opcode >> 9) & 0x07);
		int r = d - s - (cpu.isFlagSet(Cpu.X_FLAG) ? 1 : 0);
		cpu.setDataRegisterByte((opcode >> 9) & 0x07, r);
		cpu.calcFlags(InstructionType.SUBX, s, d, r, Size.Word);
		return 4;
	}

	protected int subx_long_reg(int opcode)
	{
		int s = cpu.getDataRegisterLong((opcode & 0x07));
		int d = cpu.getDataRegisterLong((opcode >> 9) & 0x07);
		int r = d - s - (cpu.isFlagSet(Cpu.X_FLAG) ? 1 : 0);
		cpu.setDataRegisterLong((opcode >> 9) & 0x07, r);
		cpu.calcFlags(InstructionType.SUBX, s, d, r, Size.Long);
		return 8;
	}

	protected int subx_byte_mem(int opcode)
	{
		int rx = (opcode >> 9) & 0x07;
		int ry = (opcode & 0x07);
		cpu.decrementAddrRegister(rx, 1);
		cpu.decrementAddrRegister(ry, 1);
		int s = cpu.readMemoryByteSigned(cpu.getAddrRegisterLong(ry));
		int d = cpu.readMemoryByteSigned(cpu.getAddrRegisterLong(rx));
		int r = d - s - (cpu.isFlagSet(Cpu.X_FLAG) ? 1 : 0);
		cpu.writeMemoryByte(cpu.getAddrRegisterLong(rx), r);
		cpu.calcFlags(InstructionType.SUBX, s, d, r, Size.Byte);
		return 18;
	}

	protected int subx_word_mem(int opcode)
	{
		int rx = (opcode >> 9) & 0x07;
		int ry = (opcode & 0x07);
		cpu.decrementAddrRegister(rx, 2);
		cpu.decrementAddrRegister(ry, 2);
		int s = cpu.readMemoryWordSigned(cpu.getAddrRegisterLong(ry));
		int d = cpu.readMemoryWordSigned(cpu.getAddrRegisterLong(rx));
		int r = d - s - (cpu.isFlagSet(Cpu.X_FLAG) ? 1 : 0);
		cpu.writeMemoryWord(cpu.getAddrRegisterLong(rx), r);
		cpu.calcFlags(InstructionType.SUBX, s, d, r, Size.Word);
		return 18;
	}

	protected int subx_long_mem(int opcode)
	{
		int rx = (opcode >> 9) & 0x07;
		int ry = (opcode & 0x07);
		cpu.decrementAddrRegister(rx, 4);
		cpu.decrementAddrRegister(ry, 4);
		int s = cpu.readMemoryLong(cpu.getAddrRegisterLong(ry));
		int d = cpu.readMemoryLong(cpu.getAddrRegisterLong(rx));
		int r = d - s - (cpu.isFlagSet(Cpu.X_FLAG) ? 1 : 0);
		cpu.writeMemoryLong(cpu.getAddrRegisterLong(rx), r);
		cpu.calcFlags(InstructionType.SUBX, s, d, r, Size.Byte);
		return 30;
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode, Size sz)
	{
		DisassembledOperand src;
		DisassembledOperand dst;

		if((opcode & 0x08) == 0)
		{
			// data reg mode
			src = new DisassembledOperand("d" + (opcode & 0x07));
			dst = new DisassembledOperand("d" + ((opcode >> 9) & 0x07));
		}
		else
		{
			//memory mode
			src = new DisassembledOperand("-(a" + (opcode & 0x07) + ")");
			dst = new DisassembledOperand("-(a" + ((opcode >> 9) & 0x07) + ")");
		}

		return new DisassembledInstruction(address, opcode, "subx" + sz.ext(), src, dst);
	}
}
