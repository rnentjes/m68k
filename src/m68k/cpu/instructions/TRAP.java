package m68k.cpu.instructions;

import m68k.cpu.*;
import m68k.cpu.assemble.AssembledInstruction;
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
public class TRAP implements InstructionHandler
{
	protected final Cpu cpu;

	public TRAP(Cpu cpu)
	{
		this.cpu = cpu;
	}

	public final void register(InstructionSet is)
	{
		int base = 0x4e40;
		Instruction i = new Instruction() {
			public int execute(int opcode)
			{
				return trap(opcode);
			}
			public DisassembledInstruction disassemble(int address, int opcode)
			{
				return disassembleOp(address, opcode);
			}
		};

		for(int v = 0; v < 16; v++)
		{
			is.addInstruction(base + v, i);
		}
	}

    @Override
    public DisassembledInstruction assemble(int address, AssembledInstruction instruction, Labels labels) {
        int opcode = 0x4e40;

        DisassembledOperand op1 = instruction.op1;

        if (op1.memory_read > 15) {
            throw new IllegalArgumentException("Value out of range for trap instuction "+op1.memory_read);
        }

        opcode |= op1.memory_read;

        return new DisassembledInstruction(address, opcode, instruction.instruction,
                new DisassembledOperand(op1.operand, 0, op1.memory_read));
    }

    protected final int trap(int opcode)
	{
		int v = (opcode & 0x0f);
		cpu.raiseException(32 + v);
		return 34;
	}

	protected final DisassembledInstruction disassembleOp(int address, int opcode)
	{
		DisassembledOperand op = new DisassembledOperand(String.format("#%d", (opcode & 0x0f)));
		return new DisassembledInstruction(address, opcode, "trap", op);
	}
}
