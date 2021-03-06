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
public class ADDA implements InstructionHandler
{
	protected final Cpu cpu;

	public ADDA(Cpu cpu)
	{
		this.cpu = cpu;
	}

	public final void register(InstructionSet is)
	{
		int base;
		Instruction i;

		// destination An
		for(int sz = 0; sz < 2; sz++)
		{
			if(sz == 0)
			{
				// adda word
				base = 0xd0c0;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return adda_word(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Word);
					}
				};
			}
			else
			{
				// adda long
				base = 0xd1c0;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return adda_long(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Long);
					}
				};
			}
			for(int reg = 0; reg < 8; reg++)
			{
				for(int ea_mode = 0; ea_mode < 8; ea_mode++)
				{
					for(int ea_reg = 0; ea_reg < 8; ea_reg++)
					{
						if(ea_mode == 7 && ea_reg > 4)
							break;
						is.addInstruction(base + (reg << 9) + (ea_mode << 3) + ea_reg, i);
					}
				}
			}
		}
	}

    @Override
    public DisassembledInstruction assemble(int address, AssembledInstruction instruction, Labels labels) {
        int opcode = 0xd000;

        switch(instruction.size) {
            case Word:
                opcode |= 3 << 6;
                break;
            case Long:
                opcode |= 7 << 6;
                break;
        }

        AssembledOperand op1 = (AssembledOperand)instruction.op1;
        AssembledOperand op2 = (AssembledOperand)instruction.op2;

        opcode |= op1.mode.bits() << 3;
        opcode |= op1.register;

        opcode |= op2.register << 9;

        return new DisassembledInstruction(address, opcode, instruction.instruction,
                new DisassembledOperand(op1.operand, op1.bytes, op1.memory_read),
                new DisassembledOperand(op2.operand, op2.bytes, op2.memory_read));
    }

    protected final int adda_word(int opcode)
	{
		Operand src = cpu.resolveSrcEA((opcode >> 3) & 0x07, (opcode & 0x07), Size.Word);
		// should this be sign extended ?
		int s = src.getWord();
		int reg = (opcode >> 9) & 0x07;
		int d = cpu.getAddrRegisterWord(reg);
		int r = s + d;
		cpu.setAddrRegisterLong(reg, r);

		//No flags affected

		return 8 + src.getTiming();
	}

	protected final int adda_long(int opcode)
	{
		Operand src = cpu.resolveSrcEA((opcode >> 3) & 0x07, (opcode & 0x07), Size.Long);
		int s = src.getLong();
		int reg = (opcode >> 9) & 0x07;
		int d = cpu.getAddrRegisterLong(reg);
		int r = s + d;
		cpu.setAddrRegisterLong(reg, r);

		//No flags affected

		return 6 + src.getTiming();
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode, Size sz)
	{
		DisassembledOperand src = cpu.disassembleSrcEA(address + 2, (opcode >> 3) & 0x07, (opcode & 0x07), sz);
		DisassembledOperand dst = new DisassembledOperand("a" + ((opcode >> 9) & 0x07));

		return new DisassembledInstruction(address, opcode, "adda" + sz.ext(), src, dst);
	}
}
