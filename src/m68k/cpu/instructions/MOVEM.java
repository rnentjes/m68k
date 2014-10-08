package m68k.cpu.instructions;

import m68k.cpu.*;
import m68k.cpu.assemble.AddressingMode;
import m68k.cpu.assemble.AssembledInstruction;
import m68k.cpu.assemble.AssembledOperand;

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

public class MOVEM implements InstructionHandler
{
	protected final Cpu cpu;

	private static final int[] M2R_Timing = {
			0, 0, 12, 12, 0, 16, 18, 16, 20, 16, 18
	};
	private static final int[] R2M_Timing = {
			0, 0, 8, 0, 8, 12, 14, 12, 16
	};

	public MOVEM(Cpu cpu)
	{
		this.cpu = cpu;
	}

	public final void register(InstructionSet is)
	{
		int base;
		Instruction i;

		// reg to mem
		for(int sz = 0; sz < 2; sz++)
		{
			if(sz == 0)
			{
				base = 0x4880;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return movem_word_r2m(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Word, true);
					}
				};
			}
			else
			{
				base = 0x48c0;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return movem_long_r2m(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Long, true);
					}
				};
			}

			for(int ea_mode = 2; ea_mode < 8; ea_mode++)
			{
				if(ea_mode == 3)
					continue;

				for(int ea_reg = 0; ea_reg < 8; ea_reg++)
				{
					if(ea_mode == 7 && ea_reg > 1)
						break;

					is.addInstruction(base + (ea_mode << 3) + ea_reg, i);
				}
			}
		}

		// mem to reg
		for(int sz = 0; sz < 2; sz++)
		{
			if(sz == 0)
			{
				base = 0x4c80;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return movem_word_m2r(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Word, false);
					}
				};
			}
			else
			{
				base = 0x4cc0;
				i = new Instruction() {
					public int execute(int opcode)
					{
						return movem_long_m2r(opcode);
					}
					public DisassembledInstruction disassemble(int address, int opcode)
					{
						return disassembleOp(address, opcode, Size.Long, false);
					}
				};
			}

			for(int ea_mode = 2; ea_mode < 8; ea_mode++)
			{
				if(ea_mode == 4)
					continue;

				for(int ea_reg = 0; ea_reg < 8; ea_reg++)
				{
					if(ea_mode == 7 && ea_reg > 1)
						break;

					is.addInstruction(base + (ea_mode << 3) + ea_reg, i);
				}
			}
		}
	}

    @Override
    public DisassembledInstruction assemble(int address, AssembledInstruction instruction) {
        int opcode = 0x4880;

        switch(instruction.size) {
            case Long:
                opcode |= 1 << 6;
                break;
        }

        AssembledOperand op1 = (AssembledOperand)instruction.op1;
        AssembledOperand op2 = (AssembledOperand)instruction.op2;

        int memory_read1 = op1.memory_read;
        int memory_read2 = op2.memory_read;

        int bytes1 = op1.bytes;
        int bytes2 = op2.bytes;

        if (op1.mode == AddressingMode.REGISTER_LIST) {
            bytes1 = 2;
            memory_read1 = op1.register;

            opcode |= op2.mode.bits() << 3;
            opcode |= op2.register;
        } else if (op2.mode == AddressingMode.REGISTER_LIST) {
            // direction to registers
            opcode |= 0x400;

            // turn mask around
            int register = op1.register;
            int result = 0;
            for (int bit = 0; bit < 32; bit++) {
                result |= register & 0x1;
                result = result << 1;
                register = register >> 1;
            }

            bytes2 = 2;
            memory_read2 = register;

            opcode |= op1.mode.bits() << 3;
            opcode |= op1.register;
        } else {
            // todo: illegal operant
        }

        return new DisassembledInstruction(address, opcode, instruction.instruction,
                new DisassembledOperand(op1.operand, bytes1, memory_read1),
                new DisassembledOperand(op2.operand, bytes2, memory_read2));
    }

    protected final int movem_word_r2m(int opcode)
	{
		int reglist = cpu.fetchPCWord();
		Operand dst = cpu.resolveDstEA((opcode >> 3) & 0x07, opcode & 0x07, Size.Word);
		int address = dst.getComputedAddress();
		int count;

		//true up if -(An) addressing mode
		if(((opcode >> 3) & 0x07) == 4)
		{
			count = putMultipleWordPreDec(opcode & 0x07, reglist, address);
		}
		else
		{
			count = putMultipleWord(reglist, address);
		}

		return R2M_Timing[dst.index()] + (count << 2);	//reg count * 4
	}

	protected final int movem_long_r2m(int opcode)
	{
		int reglist = cpu.fetchPCWord();
		Operand dst = cpu.resolveDstEA((opcode >> 3) & 0x07, opcode & 0x07, Size.Long);
		int address = dst.getComputedAddress();
		int count;

		//true up if -(An) addressing mode
		if(((opcode >> 3) & 0x07) == 4)
		{
			count = putMultipleLongPreDec(opcode & 0x07, reglist, address);
		}
		else
		{
			count = putMultipleLong(reglist, address);
		}

		return R2M_Timing[dst.index()] + (count << 3);	//reg count * 8
	}

	protected final int movem_word_m2r(int opcode)
	{
		int reglist = cpu.fetchPCWord();
		Operand src = cpu.resolveSrcEA((opcode >> 3) & 0x07, opcode & 0x07, Size.Word);
		int address = src.getComputedAddress();
		int count;

		// true up if (An)+ addressing mode
		if(((opcode >> 3) & 0x07) == 3)
		{
			count = getMultipleWordPostInc(opcode & 0x07, reglist, address);
		}
		else
		{
			count = getMultipleWord(reglist, address);
		}

		return M2R_Timing[src.index()] + (count << 2);	//reg count * 4
	}

	protected final int movem_long_m2r(int opcode)
	{
		int reglist = cpu.fetchPCWord();
		Operand src = cpu.resolveSrcEA((opcode >> 3) & 0x07, opcode & 0x07, Size.Long);
		int address = src.getComputedAddress();
		int count;

		//true up if (An)+ addressing mode
		if(((opcode >> 3) & 0x07) == 4)
		{
			count = getMultipleLongPostInc(opcode & 0x07, reglist, address);
		}
		else
		{
			count = getMultipleLong(reglist, address);
		}

		return M2R_Timing[src.index()] + (count << 3);	//reg count * 8
	}

	public final DisassembledInstruction disassembleOp(int address, int opcode, Size sz, boolean reg_to_mem)
	{
		DisassembledOperand src;
		DisassembledOperand dst;
		int mode = (opcode >> 3) & 0x07;
		int reg = (opcode & 0x07);
		int reglist = cpu.readMemoryWord(address + 2);
		boolean reversed = (mode == 4);

		if(reg_to_mem)
		{
			//registers to memory
			src = new DisassembledOperand(regListToString(reglist, reversed), 2, reglist);
			dst = cpu.disassembleDstEA(address + 4, mode, reg, sz);
		}
		else
		{
			// memory to registers
			src = cpu.disassembleSrcEA(address + 4, mode, reg, sz);
			dst = new DisassembledOperand(regListToString(reglist, reversed), 2, reglist);
		}

		return new DisassembledInstruction(address, opcode, "movem" + sz.ext(), src, dst);
	}

	protected final String regListToString(int reglist, boolean reversed)
	{
		StringBuilder sb = new StringBuilder();
		int first = -1;
		int count = 0;

		if(!reversed)
		{
			//normal mode lsb = d0
			char prefix = 'd';
			int mask = 1;

			for(int i = 0; i < 2; i++)
			{
				for(int n = 0; n < 8; n++, mask <<= 1)
				{
					if((reglist & mask) != 0)
					{
						if(first != -1)
						{
							count++;
						}
						else
						{
							first = n;
						}
					}
					else
					{
						if(first != -1)
						{
							if(sb.length() > 0)
								sb.append('/');

							sb.append(prefix);
							sb.append(first);
							if(count == 1)
							{
								sb.append('/');
								sb.append(prefix);
								sb.append(n - 1);
							}
							else if(count > 1)
							{
								sb.append('-');
								sb.append(prefix);
								sb.append(n - 1);
							}

							count = 0;
							first = -1;
						}
					}
				}

				if(first != -1)
				{
					if(sb.length() > 0)
						sb.append('/');

					sb.append(prefix);
					sb.append(first);
					if(count == 1)
					{
						sb.append('/');
						sb.append(prefix);
						sb.append(7);
					}
					else if(count > 1)
					{
						sb.append('-');
						sb.append(prefix);
						sb.append(7);
					}

					count = 0;
					first = -1;
				}

				prefix = 'a';
			}
		}
		else
		{
			//reverse mode for -(an) lsb = a7
			char prefix = 'd';
			int mask = 0x8000;

			for(int i = 0; i < 2; i++)
			{
				for(int n = 0; n < 8; n++, mask >>= 1)
				{
					if((reglist & mask) != 0)
					{
						if(first != -1)
						{
							count++;
						}
						else
						{
							first = n;
						}
					}
					else
					{
						if(first != -1)
						{
							if(sb.length() > 0)
								sb.append('/');

							sb.append(prefix);
							sb.append(first);
							if(count == 1)
							{
								sb.append('/');
								sb.append(prefix);
								sb.append(n - 1);
							}
							else if(count > 1)
							{
								sb.append('-');
								sb.append(prefix);
								sb.append(n - 1);
							}

							count = 0;
							first = -1;
						}
					}
				}

				if(first != -1)
				{
					if(sb.length() > 0)
						sb.append('/');

					sb.append(prefix);
					sb.append(first);
					if(count == 1)
					{
						sb.append('/');
						sb.append(prefix);
						sb.append(7);
					}
					else if(count > 1)
					{
						sb.append('-');
						sb.append(prefix);
						sb.append(7);
					}

					count = 0;
					first = -1;
				}

				prefix = 'a';
			}
		}

		return sb.toString();
	}

	protected int getMultipleWord(int reglist, int address)
	{
		int bit = 1;
		int regcount = 0;
		int start = address;

		//assumes d0 is first bit
		for(int n = 0; n < 8; n++)
		{
			if((reglist & bit) != 0)
			{
				//ensure these are sign extended properly
				cpu.setDataRegisterLong(n, cpu.readMemoryWordSigned(start));
				start += 2;
				regcount++;
			}
			bit <<= 1;
		}

		for(int n = 0; n < 8; n++)
		{
			if((reglist & bit) != 0)
			{
				//to ensure these are sign extended properly
				cpu.setAddrRegisterLong(n, cpu.readMemoryWordSigned(start));
				start += 2;
				regcount++;
			}
			bit <<= 1;
		}

		return regcount;
	}

	protected final int getMultipleWordPostInc(int reg, int reglist, int address)
	{
		int bit = 1;
		int regcount = 0;
		int start = address;

		//assumes d0 is first bit
		for(int n = 0; n < 8; n++)
		{
			if((reglist & bit) != 0)
			{
				//ensure these are sign extended properly
				cpu.setDataRegisterLong(n, cpu.readMemoryWordSigned(start));
				start += 2;
				regcount++;
			}
			bit <<= 1;
		}

		for(int n = 0; n < 8; n++)
		{
			if((reglist & bit) != 0)
			{
				//to ensure these are sign extended properly
				cpu.setAddrRegisterLong(n, cpu.readMemoryWordSigned(start));
				start += 2;
				regcount++;
			}
			bit <<= 1;
		}

		cpu.setAddrRegisterLong(reg, start);
		return regcount;
	}

	protected int getMultipleLong(int reglist, int address)
	{
		int bit = 1;
		int regcount = 0;
		int start = address;

		//assumes d0 is first bit
		for(int n = 0; n < 8; n++)
		{
			if((reglist & bit) != 0)
			{
				cpu.setDataRegisterLong(n, cpu.readMemoryLong(start));
				start += 4;
				regcount++;
			}
			bit <<= 1;
		}

		for(int n = 0; n < 8; n++)
		{
			if((reglist & bit) != 0)
			{
				cpu.setAddrRegisterLong(n, cpu.readMemoryLong(start));
				start += 4;
				regcount++;
			}
			bit <<= 1;
		}

		return regcount;
	}

	protected final int getMultipleLongPostInc(int reg, int reglist, int address)
	{
		int bit = 1;
		int regcount = 0;
		int start = address;

		//assumes d0 is first bit
		for(int n = 0; n < 8; n++)
		{
			if((reglist & bit) != 0)
			{
				cpu.setDataRegisterLong(n, cpu.readMemoryLong(start));
				start += 4;
				regcount++;
			}
			bit <<= 1;
		}

		for(int n = 0; n < 8; n++)
		{
			if((reglist & bit) != 0)
			{
				cpu.setAddrRegisterLong(n, cpu.readMemoryLong(start));
				start += 4;
				regcount++;
			}
			bit <<= 1;
		}

		cpu.setAddrRegisterLong(reg, start);
		return regcount;
	}


	protected final int putMultipleWord(int reglist, int address)
	{
		int bit = 1;
		int regcount = 0;
		int start = address;

		//assumes d0 is first bit
		for(int n = 0; n < 8; n++)
		{
			if((reglist & bit) != 0)
			{
				cpu.writeMemoryWord(start, cpu.getDataRegisterWord(n));
				start += 2;
				regcount++;
			}
			bit <<= 1;
		}

		for(int n = 0; n < 8; n++)
		{
			if((reglist & bit) != 0)
			{
				cpu.writeMemoryWord(start, cpu.getAddrRegisterWord(n));
				start += 2;
				regcount++;
			}
			bit <<= 1;
		}

		return regcount;
	}

	protected final int putMultipleWordPreDec(int reg, int reglist, int address)
	{
		int bit = 1;
		int regcount = 0;
		int start = address;

		//assumes a7 is first bit
		for(int n = 0; n < 8; n++)
		{
			if((reglist & bit) != 0)
			{
				start -= 2;
				cpu.writeMemoryWord(start, cpu.getAddrRegisterWord(7 - n));
				regcount++;
			}
			bit <<= 1;
		}

		for(int n = 0; n < 8; n++)
		{
			if((reglist & bit) != 0)
			{
				start -= 2;
				cpu.writeMemoryWord(start, cpu.getDataRegisterWord(7 - n));
				regcount++;
			}
			bit <<= 1;
		}

		cpu.setAddrRegisterLong(reg, start);
		return regcount;
	}

	protected final int putMultipleLong(int reglist, int address)
	{
		int bit = 1;
		int regcount = 0;
		int start = address;

		//assumes d0 is first bit
		for(int n = 0; n < 8; n++)
		{
			if((reglist & bit) != 0)
			{
				cpu.writeMemoryLong(start, cpu.getDataRegisterLong(n));
				start += 4;
				regcount++;
			}
			bit <<= 1;
		}

		for(int n = 0; n < 8; n++)
		{
			if((reglist & bit) != 0)
			{
				cpu.writeMemoryLong(start, cpu.getAddrRegisterLong(n));
				start += 4;
				regcount++;
			}
			bit <<= 1;
		}

		return regcount;
	}

	protected final int putMultipleLongPreDec(int reg, int reglist, int address)
	{
		int bit = 1;
		int regcount = 0;
		int start = address;

		//assumes a7 is first bit
		for(int n = 0; n < 8; n++)
		{
			if((reglist & bit) != 0)
			{
				start -= 4;
				cpu.writeMemoryLong(start, cpu.getAddrRegisterLong(7 - n));
				regcount++;
			}
			bit <<= 1;
		}

		for(int n = 0; n < 8; n++)
		{
			if((reglist & bit) != 0)
			{
				start -= 4;
				cpu.writeMemoryLong(start, cpu.getDataRegisterLong(7 - n));
				regcount++;
			}
			bit <<= 1;
		}

		cpu.setAddrRegisterLong(reg, start);
		return regcount;
	}

}
