/*
 * Gality
 *
 * Copyright (c) 2015 Andreas Follner
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 *
 */



package gality;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Gality
 * @author Andreas Follner
 *
 */
public class Program {

	private static boolean STRICT = false;
	private static int cnt_useful = 0;
	private static int cnt_gadgets = 0;
	private static int cnt_gd_mov_data = 0;
	private static int cnt_gd_ar = 0;
	private static int cnt_log = 0;
	private static int cnt_cf = 0;
	private static int cnt_misc = 0;
	private static int cnt_ret = 0;
	private static int cnt_flags = 0;
	private static int cnt_string = 0;
	private static int cnt_nop = 0;
	private static int cnt_shift_rot = 0;
	private static int cnt_fp = 0;
	private static BufferedWriter output = null;
	private static double total_score = 0.0;
	private static int total_kept = 0;
	private static int tmp_unmapped = 0;
	private static final int MAX_RET = 16;


	public static void main(String[] args) throws Exception {
		
		// parse arguments
		String ropgadget_output_file = args[0];
		String output_file = args[1];
		System.out.println("Reading gadgets from: '" + ropgadget_output_file +"'");
		System.out.println("Writing result to   : '" + output_file +"'");

		String[] strArray1 = new String[]{ "pop ", "push ", "add ", "sub ", "adc ", "dec ", "inc ", "neg", "not", "mov ", "sbb ", "xchg ", "xor " };
		String[] strArray2 = new String[]{ "pop", "push", "movzx", "mov ", "xchg", "lea", "cmov", "movabs" };
		String[] strArray3 = new String[]{ "add ", "sub ", "inc", "dec", "xor ", "neg", "not", "sbb ", "adc ", "mul ", "div ", "imul", "idiv" };
		String[] strArray4 = new String[]{ "cmp", "and ", "or ", "test" };
		String[] strArray5 = new String[]{ "call", "sysenter", "enter ", "int ", "int1", "jmp", "je", "jne", "jo", "jp", "js", "lcall", "ljmp", "jg", "jge", "ja", "jae", "jb", "jbe", "jl", "jle", "jno", "jnp", "jns", "loop", "jrcxz" };
		String[] strArray6 = new String[]{ "shl", "shr", "sar", "sal", "ror", "rol", "rcr", "rcl" };
		String[] strArray7 = new String[]{ "xlatb", "sti", "std", "stc", "lahf", "hlt", "cwde", "cmc", "cli", "cld", "clc", "cdq" };
		String[] strArray8 = new String[]{ "wait", "set", "out", "in ", "leave", "insb", "insd", "insw", "ins " };
		String[] strArray9 = new String[]{ "stosd", "stosb", "scas", "salc", "sahf", "lods", "movs" };
		String[] strArray10 = new String[]{ "ret" };
		String[] strArray11 = new String[]{ "nop" };
		String[] strArray12 = new String[]{ "divps", "mulps", "movups", "movaps", "addps", "rcpss", "sqrtss", "maxps", "minps", "andps", "orps", "xorps", "cmpps", "vsubpd", "vpsubsb", "vmulss", "vminsd", "ucomiss", "subss", "subps", "subsd", "divss", "addss", "addsd", "cvtpi2ps", "cvtps2pd", "cvtsd2ss", "cvtsi2sd", "cvtsi2ss", "cvtss2sd", "mulsd", "mulss", "fmul", "fdiv", "fcomp", "fadd" };

		BufferedReader streamReader1 = null;

		try {
			streamReader1 = new BufferedReader(new FileReader(new File(ropgadget_output_file)));
			output =  new BufferedWriter(new FileWriter(output_file));
		} catch (Exception ex) {
			System.err.println("Error reading from {0}. Message = {1}" + ropgadget_output_file + ex);
			System.exit(-1);
		}

		if (streamReader1 == null) {
			System.err.println("Error when reading input file: reader is null.") ;
			System.exit(-1);
		}

		System.out.println("Sit back, we're finding your gadgets... (this can take a few seconds)");
		
		String str1 = new String();

		// While there lines in the ROPgadget output file
		while ((str1 = streamReader1.readLine()) != null) {
			// Check to see if the line has a colom, which indicates a gadget (rather than header or footer)
			if (str1.contains(" : ")) {
				// Split the gadget portion up into individual instructions
				String[] strArray13 = str1.substring(str1.indexOf(" : ") + 3, str1.length()).split(";");			
				// Consider the gadget useful if it starts with an instruction in strArr1
				if (Arrays.stream(strArray1).filter(instr -> strArray13[0].startsWith(instr)).count() > 0)
					++Program.cnt_useful;
				
				// Count up the number of each category of gadget
				if (Arrays.stream(strArray3).filter(instr -> strArray13[0].startsWith(instr)).count() > 0)
					++Program.cnt_gd_ar;
				else if (Arrays.stream(strArray2).filter(instr -> strArray13[0].startsWith(instr)).count() > 0)
					++Program.cnt_gd_mov_data;
				else if (Arrays.stream(strArray5).filter(instr -> strArray13[0].startsWith(instr)).count() > 0)
					++Program.cnt_cf;
				else if (Arrays.stream(strArray4).filter(instr -> strArray13[0].startsWith(instr)).count() > 0)
					++Program.cnt_log;
				else if (Arrays.stream(strArray6).filter(instr -> strArray13[0].startsWith(instr)).count() > 0)
					++Program.cnt_shift_rot;
				else if (Arrays.stream(strArray10).filter(instr -> strArray13[0].startsWith(instr)).count() > 0)
					++Program.cnt_ret;
				else if (Arrays.stream(strArray7).filter(instr -> strArray13[0].startsWith(instr)).count() > 0)
					++Program.cnt_flags;
				else if (Arrays.stream(strArray9).filter(instr -> strArray13[0].startsWith(instr)).count() > 0)
					++Program.cnt_string;
				else if (Arrays.stream(strArray11).filter(instr -> strArray13[0].startsWith(instr)).count() > 0)
					++Program.cnt_nop;
				else if (Arrays.stream(strArray8).filter(instr -> strArray13[0].startsWith(instr)).count() > 0)
					++Program.cnt_misc;
				else if (Arrays.stream(strArray12).filter(instr -> strArray13[0].startsWith(instr)).count() > 0)
					++Program.cnt_fp;

			}

		}
		streamReader1.close();
		// Output the cagtegory numbers.
		Program.output.write("Arithmetic Gadgets: " + (Object)Program.cnt_gd_ar+ "\n");
		Program.output.write("Data-Move Gadgets: " + (Object)Program.cnt_gd_mov_data+ "\n");
		Program.output.write("Control-Flow Gadgets: " + (Object)Program.cnt_cf+ "\n");
		Program.output.write("Logic Gadgets: " + (Object)Program.cnt_log+ "\n");
		Program.output.write("RETs: " + (Object)Program.cnt_ret+ "\n");
		Program.output.write("Shift/Rot Gadgets: " + (Object)Program.cnt_shift_rot+ "\n");
		Program.output.write("Flag Gadgets: " + (Object)Program.cnt_flags+ "\n");
		Program.output.write("String Gadgets: " + (Object)Program.cnt_string+ "\n");
		Program.output.write("NOP Gadgets: " + (Object)Program.cnt_nop+ "\n");
		Program.output.write("Floating Point: " + (Object)Program.cnt_fp+ "\n");
		Program.output.write("Misc Gadgets: " + (Object)Program.cnt_misc+ "\n");
		Program.output.write("Total: " + (Object)(Program.cnt_gd_ar + Program.cnt_gd_mov_data + Program.cnt_cf + Program.cnt_log + Program.cnt_ret + Program.cnt_misc + Program.cnt_shift_rot + Program.cnt_flags + Program.cnt_string + Program.cnt_nop + Program.cnt_fp)+ "\n");
		BufferedReader streamReader2 = new BufferedReader(new FileReader(ropgadget_output_file));
		List<String> stringList1 = new ArrayList<String>();
		List<String> stringList2 = new ArrayList<String>();
		List<String> stringList3 = new ArrayList<String>();
		List<String> stringList4 = new ArrayList<String>();
		List<String> stringList5 = new ArrayList<String>();
		List<String> stringList6 = new ArrayList<String>();
		List<String> stringList7 = new ArrayList<String>();
		List<String> stringList8 = new ArrayList<String>();
		List<String> stringList9 = new ArrayList<String>();
		List<String> stringList10 = new ArrayList<String>();
		List<String> stringList11 = new ArrayList<String>();
		String str2 = new String();
		// Iterate through the file again
		while ((str2 = streamReader2.readLine()) != null)
		{ // Check if its a gadget
			if (str2.contains(" : "))
			{	// Get just the gadget part and split it up (again)
				String gadget = str2.substring(str2.indexOf(" : ") + 3, str2.length());
				String[] strArray13 = gadget.split(";");
				
				// Check for parameter loading gadgets (rcx, rdx, r8, r9)
				if (strArray13[0].contains("pop rcx") || strArray13[0].contains("mov rcx"))
				{	//  If the instruction pops to the register, is immediately followed by the ret, doesn't have an offensive return adjustment (i.e. is byte aligned and less than 16 bytes) and the only instructions are the pop and the ret, add this gadget to stringlist1 (good rcx gadgets)
					if (strArray13[0].contains("pop rcx") && strArray13[1].contains("ret") && (Program.get_ret_offset(strArray13[1]) % 4 == 0 && Program.get_ret_offset(strArray13[1]) <= 16) && strArray13.length == 2)
						stringList1.add(gadget);
					// Otherwise, as long as the gadget preserves the register add it to string list 2 (bad rcx gadgets)					
					else if (Program.preserves_reg(gadget))
						stringList2.add(gadget);

				}
				// Same as previous block, except for rdx
				if (strArray13[0].contains("pop rdx") || strArray13[0].contains("mov rdx"))
				{
					if (strArray13[0].contains("pop rdx") && strArray13[1].contains("ret") && (Program.get_ret_offset(strArray13[1]) % 4 == 0 && Program.get_ret_offset(strArray13[1]) <= 16) && strArray13.length == 2)
						stringList3.add(gadget);
					else if (Program.preserves_reg(gadget))
						stringList4.add(gadget);

				}
				// Same as previous block, except for r8
				if (strArray13[0].contains("pop r8") || strArray13[0].contains("mov r8"))
				{
					if (strArray13[0].contains("pop r8") && strArray13[1].contains("ret") && (Program.get_ret_offset(strArray13[1]) % 4 == 0 && Program.get_ret_offset(strArray13[1]) <= 16) && strArray13.length == 2)
						stringList5.add(gadget);
					else if (Program.preserves_reg(gadget))
						stringList6.add(gadget);

				}
				// Same as previous block, except for r9
				if (strArray13[0].contains("pop r9") || strArray13[0].contains("mov r9"))
				{
					if (strArray13[0].contains("pop r9") && strArray13[1].contains("ret") && (Program.get_ret_offset(strArray13[1]) % 4 == 0 && Program.get_ret_offset(strArray13[1]) <= 16) && strArray13.length == 2)
						stringList7.add(gadget);
					else if (Program.preserves_reg(gadget))
						stringList8.add(gadget);

				}
				// For each instruction in the gadget
				for (String str3 : strArray13)
				{	// Check to see if the gadget is a good or bad stack pivot gadget (strlist 9 or 10 respectively)
					if (str3.contains("xchg") && str3.contains("rsp") && !str3.contains("["))
						stringList9.add(gadget);

					if (str3.contains(" rsp") && (str3.contains("mov") || str3.contains("xchg") || str3.contains("pop")))
						stringList10.add(gadget);

					if (str3.contains(" esp") && (str3.contains("mov") || str3.contains("xchg") || str3.contains("pop")))
						stringList10.add(gadget);

				}
				// Check if the gadget is a call reg gadget without memory accesses (e.g. call rax, call r8) (what was this used for in the paper?)
				if (strArray13.length == 1 && (strArray13[0].contains("call") || strArray13[0].contains("jmp")) && (!strArray13[0].contains("0x") && !strArray13[0].contains("ptr")))
					stringList11.add(gadget);

			}

		}
		streamReader2.close();
		Program.output.write("Gadgets for loading params:\n");
		Program.output.write("Good gadgets: rcx: " + (Object)stringList1.size() + "; rdx: " + (Object)stringList3.size() + "; r8: " + (Object)stringList5.size() + "; r9: " + (Object)stringList7.size() + "\n");
		Program.output.write("Bad gadgets: rcx: " + (Object)stringList2.size() + "; rdx: " + (Object)stringList4.size() + "; r8: " + (Object)stringList6.size() + "; r9: " + (Object)stringList8.size() +"\n");
		Program.output.write("Good / bad stack pivots: " + (Object)stringList9.size() + " / " + (Object)stringList10.size() +"\n");
		Program.output.write("Call reg: " + (Object)stringList11.size() +"\n");
		
		// Make a third pass through the gadgets
		BufferedReader streamReader3 = new BufferedReader(new FileReader(new File(ropgadget_output_file)));
		String str4 = new String();
		while ((str4 = streamReader3.readLine()) != null)
		{	// Get just gadgets
			if (str4.contains(" : "))
			{	// Split into array of instructions
				String str3 = str4.substring(str4.indexOf(" : ") + 3, str4.length());
				String[] strArray13 = str3.split(";");
				// Determine if we are going to score this gadget: It starts with a useful instruction, preserves the register of interest in the gadget, and ends in a ret (ROP only).
				if (Arrays.stream(strArray1).filter(instr -> strArray13[0].startsWith(instr)).count() > 0 && Program.preserves_reg(str3) && strArray13[strArray13.length - 1].contains("ret"))
				{	// If the gadget contains a return offset, check that it is byte aligned and less than 16 bytes. If so, or if no offset, score the gadget.
					if (strArray13[strArray13.length - 1].contains("0x"))
					{
						int retOffset = Program.get_ret_offset(strArray13[strArray13.length - 1]);
						if (retOffset % 4 == 0 && retOffset <= 16){
							Program.found_gadget(str3);
						}

					}
					else{
						Program.found_gadget(str3);
					}
				}
				// TODO: Here is where we can add JOP/COP scoring, an else if that grabs JOP/COP gadgets we consider useful.

			}

		}
		// Print put gadget quality metric data
		streamReader3.close();
		System.out.println("Done.");
		Program.output.write("Kept " + (Object)Program.cnt_gadgets + " gadgets.\n");
		Program.output.write("Average gadget score: " + (Object)(Program.total_score / (double)Program.total_kept) +"\n");
		Program.output.close();
	}

	private static void found_gadget(String line) throws Exception {
		// MDB Redundant check eliminated (preserves_reg())
		++Program.cnt_gadgets;

		int spOffset = Program.calculate_sp_offset(line);
		double num = Program.calc_score(line) + (double)spOffset;
		Program.total_score += num;
		++Program.total_kept;
	}

	private static int get_ret_offset(String ins) throws Exception {
		if (ins.trim().equals("ret") || ins.trim().equals("retf")) {
			return 0;
		}

		if (ins.contains("0x")) {
			return Integer.parseInt(ins.substring(ins.lastIndexOf("0x") + 2, ins.length()).trim(), 16);
		}

		return Integer.parseInt(ins.substring(ins.lastIndexOf(" ") + 1, ins.length()).trim(), 16);
	}

	private static int get_offset(String ins) throws Exception {
		String str = ins.substring(ins.lastIndexOf(",") + 1, ins.length());
		int result = 0;
		if (str.contains("0x") && !str.contains("+") && !str.contains("-")) {
			result = Integer.parseInt(str.substring(3).trim(), 16);
		} else if (str.contains("-0x")){
			result = Integer.parseInt(str.substring(4).trim(), 16);
			result *= -1;
		}
		else {
			result = Integer.parseInt(str.trim());
		} 
		return result;
	}

	// MDB - Unneeded function eliminated

	private static boolean preserves_reg(String gadget) throws Exception {
		// Determine the register to protect, its the destination of the first instruction (some exceptions)
		String[] strArray1 = new String[]{ "rax", "eax", "ax", "al", "ah" };
		String[] strArray2 = new String[]{ "rbx", "ebx", "bx", "bl", "bh" };
		String[] strArray3 = new String[]{ "rcx", "ecx", "cx", "cl", "ch" };
		String[] strArray4 = new String[]{ "rdx", "edx", "dx", "dl", "dh" };
		String[] strArray5 = new String[]{ "rsi", "esi", "si", "sil" };
		String[] strArray6 = new String[]{ "rdi", "edi", "di", "dil" };
		String[] strArray7 = new String[]{ "rbp", "ebp", "bp", "bpl" };
		String[] strArray8 = new String[]{ "rsp", "esp", "sp", "spl" };
		String[] strArray9 = new String[]{ "r8", "r8d", "r8w", "r8b" };
		String[] strArray10 = new String[]{ "r9", "r9d", "r9w", "r9b" };
		String[] strArray11 = new String[]{ "r10", "r10d", "r10w", "r10b" };
		String[] strArray12 = new String[]{ "r11", "r11d", "r11w", "r11b" };
		String[] strArray13 = new String[]{ "r12", "r12d", "r12w", "r12b" };
		String[] strArray14 = new String[]{ "r13", "r13d", "r13w", "r13b" };
		String[] strArray15 = new String[]{ "r14", "r14d", "r14w", "r14b" };
		String[] strArray16 = new String[]{ "r15", "r15d", "r15w", "r15b" };
		String[] strArray17 = new String[4];
		String[] strArray18 = new String[]{ " mov ", " and ", " or ", "add ", "sub ", "inc ", "dec ", " sbb ", " adc ", " mul ", " div ", " xor ", " neg ", " not ", " shl ", " shr ", " sar ", " sal ", " ror ", " rol ", " rcr ", " rcl " };
		String[] strArray19 = new String[]{ "pop ", "cvttss2si" };
		String[] strArray20 = gadget.split(";");
		String regToProtect = Program.get_reg_to_protect(strArray20[0]);
		if (Arrays.stream(strArray3).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray17 = strArray3;
		else if (Arrays.stream(strArray4).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray17 = strArray4;
		else if (Arrays.stream(strArray9).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray17 = strArray9;
		else if (Arrays.stream(strArray10).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray17 = strArray10;
		else if (Arrays.stream(strArray1).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray17 = strArray1;
		else if (Arrays.stream(strArray2).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray17 = strArray2;
		else if (Arrays.stream(strArray5).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray17 = strArray5;
		else if (Arrays.stream(strArray6).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray17 = strArray6;
		else if (Arrays.stream(strArray7).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray17 = strArray7;
		else if (Arrays.stream(strArray8).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray17 = strArray8;
		else if (Arrays.stream(strArray11).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray17 = strArray11;
		else if (Arrays.stream(strArray12).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray17 = strArray12;
		else if (Arrays.stream(strArray13).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray17 = strArray13;
		else if (Arrays.stream(strArray14).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray17 = strArray14;
		else if (Arrays.stream(strArray15).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray17 = strArray15;
		else if (Arrays.stream(strArray16).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray17 = strArray16;
		else
			strArray17[0] = "";                
		
		// If there is a register to protect
		if (!strArray17[0].equals(""))
		{
			for (int i = 1; i < strArray20.length; i++) 
			{
				String str1 = strArray20[i];
				Program.process_ins(str1);
				if (Arrays.stream(strArray17).filter(instr -> str1.contains(instr)).count() > 0)
				{
					if (Arrays.stream(strArray19).filter(instr -> str1.contains(instr)).count() > 0)
						return false;

					if (Arrays.stream(strArray18).filter(instr -> str1.contains(instr)).count() > 0 && str1.contains(","))
					{
						String[] strArray21 = str1.split(",");
						String str2 = strArray21[0];
						String str3 = strArray21[1];
						String reg = Program.get_reg(str2);
						if (str2.contains("mov") 
								&& Arrays.stream(strArray17).filter(instr -> str2.contains(instr)).count() > 0 
								&& reg.length() == 3 
								&& ((reg.charAt(0) == 'r' || reg.charAt(0) == 'e') 
										&& !str2.contains("[")) || Program.STRICT 
								&& str2.contains("[") 
								&& Arrays.stream(strArray17).filter(instr -> str2.contains(instr)).count() > 0 || (Program.STRICT 
										&& Arrays.stream(strArray17).filter(instr -> reg.contains(instr)).count() > 0 
										&& (reg.length() == 3 && reg.charAt(0) != 'r') && reg.charAt(1) != '1' || Program.STRICT 
										&& Arrays.stream(strArray17).filter(instr -> str2.contains(instr)).count() > 0 
										&& !str2.contains("[")))
							return false;

					}

				}

			}
		}

		return true;
	}

	// Calculates the SP offset like in the paper
	private static int calculate_sp_offset(String gadget) throws Exception {
		String[] strArray = gadget.split(";");
		int num1 = 0;
		int num2 = 0;
		Program.get_reg_to_protect(strArray[0]);
		for (String ins : strArray)
		{
			if (ins.contains("push"))
				num1 -= 8;

			if (ins.contains("pop"))
			{
				if (ins.contains("pop rsp"))
					return 1;

				num1 += 8;
			}

			if (ins.contains("leave") && num2 != 0)
				return 2;

			if (ins.contains("ret"))
				num1 += Program.get_ret_offset(ins);

			if (ins.contains("add esp") || ins.contains("add rsp"))
				num1 += Program.get_offset(ins);

			if (ins.contains("sub esp") || ins.contains("sub rsp"))
				num1 += Program.get_offset(ins);

			if ((ins.contains("mov rsp") || ins.contains("mov esp")) && num2 != 0 || ins.contains("xchg") && (ins.contains("rsp") || ins.contains("esp")) && num2 != 0)
				return 4;

			++num2;
		}
		return num1 < 0 ? 2 : 0;
	}

	private static double calc_score(String gadget) throws Exception {
		String[] strArray1 = new String[]{ "pop", "push", "mov ", "xchg", "lea", "cmov", "movabs", "movzx", "movsx" };
		String[] strArray2 = new String[]{ "add ", "sub ", "inc ", "dec ", "xor ", "neg", "not", "sbb ", "adc ", "mul ", "div ", "imul", "idiv" };
		String[] strArray3 = new String[]{ "cmp", "and ", "or ", "test" };
		String[] strArray4 = new String[]{ "call", "sysenter", "enter ", "int ", "int1", "jmp", "je", "jne", "jo", "jp", "js", "lcall", "ljmp", "jg", "jge", "ja", "jae", "jb", "jbe", "jl", "jle", "jno", "jnp", "jns", "loop", "jrcxz" };
		String[] strArray5 = new String[]{ "shl", "shr", "sar", "sal", "ror", "rol", "rcr", "rcl" };
		String[] strArray6 = new String[]{ "xlatb", "sti", "std", "stc", "lahf", "hlt", "cwde", "cmc", "cli", "cld", "clc", "cdq" };
		String[] strArray7 = new String[]{ "wait", "set", "out", "in ", "leave", "insb", "insd", "insw", "ins " };
		String[] strArray8 = new String[]{ "stosd", "stosb", "scas", "salc", "sahf", "lods", "movs" };
		String[] strArray9 = new String[]{ "ret" };
		String[] strArray10 = new String[]{ "nop" };
		String[] strArray11 = new String[]{ "divps", "mulps", "movups", "movaps", "addps", "rcpss", "sqrtss", "maxps", "minps", "andps", "orps", "xorps", "cmpps", "vsubpd", "vpsubsb", "vmulss", "vminsd", "ucomiss", "subss", "subps", "subsd", "divss", "addss", "addsd", "cvtpi2ps", "cvtps2pd", "cvtsd2ss", "cvtsi2sd", "cvtsi2ss", "cvtss2sd", "mulsd", "mulss", "fmul", "fdiv", "fcomp", "fadd" };
		String[] strArray12 = new String[]{ "pxor", "movd", "movq" };
		String[] strArray13 = new String[]{ "rax", "eax", "ax", "al", "ah" };
		String[] strArray14 = new String[]{ "rbx", "ebx", "bx", "bl", "bh" };
		String[] strArray15 = new String[]{ "rcx", "ecx", "cx", "cl", "ch" };
		String[] strArray16 = new String[]{ "rdx", "edx", "dx", "dl", "dh" };
		String[] strArray17 = new String[]{ "rsi", "esi", "si", "sil" };
		String[] strArray18 = new String[]{ "rdi", "edi", "di", "dil" };
		String[] strArray19 = new String[]{ "rbp", "ebp", "bp", "bpl" };
		String[] strArray20 = new String[]{ "rsp", "esp", "sp", "spl" };
		String[] strArray21 = new String[]{ "r8", "r8d", "r8w", "r8b" };
		String[] strArray22 = new String[]{ "r9", "r9d", "r9w", "r9b" };
		String[] strArray23 = new String[]{ "r10", "r10d", "r10w", "r10b" };
		String[] strArray24 = new String[]{ "r11", "r11d", "r11w", "r11b" };
		String[] strArray25 = new String[]{ "r12", "r12d", "r12w", "r12b" };
		String[] strArray26 = new String[]{ "r13", "r13d", "r13w", "r13b" };
		String[] strArray27 = new String[]{ "r14", "r14d", "r14w", "r14b" };
		String[] strArray28 = new String[]{ "r15", "r15d", "r15w", "r15b" };

		String[] condBranchInstrs = new String[]{ "jo", "jno", "js", "jns", "je,", "jz", "jne", "jnz", "jb", "jnae", "jc", "jnb", "jae", "jnc", "jbe", "jna", "ja", "jnbe", "jl", "jnge", "jnl", "jge", "jle", "jng", "jg", "jnle", "jp", "jpe", "jnp", "jpo", "jcxz", "jecxz" };

		String[] strArray29 = gadget.split(";");
		double num = 0.0;
		String[] strArray30 = new String[4];
		String regToProtect = Program.get_reg_to_protect(strArray29[0]);
		if (Arrays.stream(strArray15).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray30 = strArray15;
		else if (Arrays.stream(strArray16).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray30 = strArray16;
		else if (Arrays.stream(strArray21).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray30 = strArray21;
		else if (Arrays.stream(strArray22).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray30 = strArray22;
		else if (Arrays.stream(strArray13).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray30 = strArray13;
		else if (Arrays.stream(strArray14).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray30 = strArray14;
		else if (Arrays.stream(strArray17).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray30 = strArray17;
		else if (Arrays.stream(strArray18).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray30 = strArray18;
		else if (Arrays.stream(strArray19).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray30 = strArray19;
		else if (Arrays.stream(strArray20).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray30 = strArray20;
		else if (Arrays.stream(strArray23).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray30 = strArray23;
		else if (Arrays.stream(strArray24).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray30 = strArray24;
		else if (Arrays.stream(strArray25).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray30 = strArray25;
		else if (Arrays.stream(strArray26).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray30 = strArray26;
		else if (Arrays.stream(strArray27).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
			strArray30 = strArray27;
		else if (Arrays.stream(strArray28).filter(instr -> regToProtect.startsWith(instr)).count() > 0)
		{
			strArray30 = strArray28;
		}
		else
		{
			strArray30[0] = "XXXX";
			strArray30[1] = "XXXX";
			strArray30[2] = "XXXX";
			strArray30[3] = "XXXX";
		}                
		// Iterate through instructions in gadget (strArr29)		
		for (int i = 1; i < strArray29.length; i++)
		{
			String ins = strArray29[i];
			if (Arrays.stream(strArray1).filter(instr -> ins.contains(instr)).count() > 0)
			{
				if (!ins.contains("push"))
				{
					String[] strArray31 = Program.process_ins(ins);
					if (strArray31.length == 2)
					{
						if (strArray31[1].equals("rsp") || strArray31[1].equals("esp"))
							num += 2.0;
						else if (Arrays.stream(strArray30).filter(instr -> strArray31[1].contains(instr)).count() > 0)
							++num;
						else
							num += 0.5;  
					}
					else if (strArray31.length == 3)
					{
						if (strArray31[1].equals("rsp") || strArray31[1].equals("esp") || strArray31[0].equals("xchg") && (strArray31[2].equals("rsp") || strArray31[2].equals("esp")))
							num += 2.0;
						else if (Arrays.stream(strArray30).filter(instr -> strArray31[1].contains(instr)).count() > 0)
							++num;
						else
							num += 0.5;  
					}

				}

			}
			else if (Arrays.stream(strArray2).filter(instr -> ins.contains(instr)).count() > 0)
			{
				String[] strArray31 = Program.process_ins(ins);
				if (strArray31.length == 2)
				{
					if (strArray31[1].equals("rsp") || strArray31[1].equals("esp"))
						num += 2.0;
					else if (Arrays.stream(strArray30).filter(instr -> strArray31[1].contains(instr)).count() > 0)
						++num;
					else
						num += 0.5;  
				}
				else if (strArray31[1].equals("rsp") || strArray31[1].equals("esp"))
					num += 2.0;
				else if (Arrays.stream(strArray30).filter(instr -> strArray31[1].contains(instr)).count() > 0)
					++num;
				else
					num += 0.5;   
			}
			else if (Arrays.stream(strArray5).filter(instr -> ins.contains(instr)).count() > 0)
			{
				String[] strArray31 = Program.process_ins(ins);
				if (strArray31.length == 3)
				{
					if (strArray31[1].equals("rsp") || strArray31[1].equals("esp"))
						num += 3.0;
					else if (Arrays.stream(strArray30).filter(instr -> strArray31[1].contains(instr)).count() > 0)
						num += 1.5;
					else
						num += 0.5;  
				}

			}
			else if (!(Arrays.stream(strArray3).filter(instr -> ins.contains(instr)).count() > 0) 
					&& !(Arrays.stream(strArray4).filter(instr -> ins.contains(instr)).count() > 0) 
					&& (!(Arrays.stream(strArray6).filter(instr -> ins.contains(instr)).count() > 0) 
							&& !(Arrays.stream(strArray7).filter(instr -> ins.contains(instr)).count() > 0)) 
					&& (!(Arrays.stream(strArray8).filter(instr -> ins.contains(instr)).count() > 0) 
							&& !(Arrays.stream(strArray9).filter(instr -> ins.contains(instr)).count() > 0) 
							&& (!(Arrays.stream(strArray10).filter(instr -> ins.contains(instr)).count() > 0) 
									&& !(Arrays.stream(strArray11).filter(instr -> ins.contains(instr)).count() > 0))) 
					&& !(Arrays.stream(strArray12).filter(instr -> ins.contains(instr)).count() > 0)){
				++Program.tmp_unmapped;
			}
			// MDB - Check to see if the gadget contains an unconditional branch.  If it occurs, increase score by 2.0.			
			else if (Arrays.stream(condBranchInstrs).filter(instr -> ins.contains(instr)).count() > 0){
				num += 2.0;
			}
		}
		return num;
	}

	private static String get_reg_to_protect(String ins) throws Exception {
		String str = "";
		if (ins.contains("pop ") || ins.contains("dec ") || (ins.contains("inc ") || ins.contains("neg ")) || ins.contains("not "))
			str = ins.substring(4, ins.length());
		else if (ins.contains("xchg "))
			str = ins.split(",")[1].trim();
		else if (ins.contains("add ") || ins.contains("adc ") || (ins.contains("sub ") || ins.contains("sbb ")) || (ins.contains("xor ") || ins.contains("mov ")))
		{
			String[] strArray = ins.split(",");
			str = strArray[0].substring(4, strArray[0].length());
		}

		return str;
	}

	private static String[] process_ins(String ins) throws Exception {
		ins = ins.trim();
		String[] strArray1 = null;
		if (ins.contains(","))
		{
			String[] strArray2 = ins.split(",");
			String ins1 = strArray2[0].trim();
			String str = strArray2[1].trim();
			String reg = Program.get_reg(ins1);
			strArray1 = new String[]{ ins1.substring(0, ins1.indexOf(" ")), reg, str };
		}
		else if (ins.contains(" "))
		{
			String[] strArray2 = ins.split(" ");
			strArray1 = new String[]{ strArray2[0].trim(), Program.get_reg(strArray2[1].trim()) };
		}
		else
			strArray1 = new String[]{ ins };  
		return strArray1;
	}

	private static String get_reg(String ins) throws Exception {
		if (ins.contains("rax"))
			return "rax";

		if (ins.contains("eax"))
			return "eax";

		if (ins.contains("ax"))
			return "ax";

		if (ins.contains("al"))
			return "al";

		if (ins.contains("ah"))
			return "ah";

		if (ins.contains("rbx"))
			return "rbx";

		if (ins.contains("ebx"))
			return "ebx";

		if (ins.contains("bx"))
			return "bx";

		if (ins.contains("bl"))
			return "bl";

		if (ins.contains("bh"))
			return "bh";

		if (ins.contains("rcx"))
			return "rcx";

		if (ins.contains("ecx"))
			return "ecx";

		if (ins.contains("cx"))
			return "cx";

		if (ins.contains("cl"))
			return "cl";

		if (ins.contains("ch"))
			return "ch";

		if (ins.contains("rdx"))
			return "rdx";

		if (ins.contains("edx"))
			return "edx";

		if (ins.contains("dx"))
			return "dx";

		if (ins.contains("dl"))
			return "dl";

		if (ins.contains("dh"))
			return "dh";

		if (ins.contains("rsi"))
			return "rsi";

		if (ins.contains("esi"))
			return "esi";

		if (ins.contains("sil"))
			return "sil";

		if (ins.contains("si"))
			return "si";

		if (ins.contains("rdi"))
			return "rdi";

		if (ins.contains("edi"))
			return "edi";

		if (ins.contains("dil"))
			return "dil";

		if (ins.contains("di"))
			return "di";

		if (ins.contains("rbp"))
			return "rbp";

		if (ins.contains("ebp"))
			return "ebp";

		if (ins.contains("bpl"))
			return "bpl";

		if (ins.contains("bp"))
			return "bp";

		if (ins.contains("rsp"))
			return "rsp";

		if (ins.contains("esp"))
			return "esp";

		if (ins.contains("spl"))
			return "spl";

		if (ins.contains("sp"))
			return "sp";

		if (ins.contains("r8d"))
			return "r8d";

		if (ins.contains("r8w"))
			return "r8w";

		if (ins.contains("r8b"))
			return "r8b";

		if (ins.contains("r8"))
			return "r8";

		if (ins.contains("r9d"))
			return "r9d";

		if (ins.contains("r9w"))
			return "r9w";

		if (ins.contains("r9b"))
			return "r9b";

		if (ins.contains("r9"))
			return "r9";

		if (ins.contains("r10d"))
			return "r10d";

		if (ins.contains("r10w"))
			return "r10w";

		if (ins.contains("r10b"))
			return "r10b";

		if (ins.contains("r10"))
			return "r10";

		if (ins.contains("r11d"))
			return "r11d";

		if (ins.contains("r11w"))
			return "r11w";

		if (ins.contains("r11b"))
			return "r11b";

		if (ins.contains("r11"))
			return "r11";

		if (ins.contains("r12d"))
			return "r12d";

		if (ins.contains("r12w"))
			return "r12w";

		if (ins.contains("r12b"))
			return "r12b";

		if (ins.contains("r12"))
			return "r12";

		if (ins.contains("r13d"))
			return "r13d";

		if (ins.contains("r13w"))
			return "r13w";

		if (ins.contains("r13b"))
			return "r13b";

		if (ins.contains("r13"))
			return "r13";

		if (ins.contains("r14d"))
			return "r14d";

		if (ins.contains("r14w"))
			return "r14w";

		if (ins.contains("r14b"))
			return "r14b";

		if (ins.contains("r14"))
			return "r14";

		if (ins.contains("r15d"))
			return "r15d";

		if (ins.contains("r15w"))
			return "r15w";

		if (ins.contains("r15b"))
			return "r15b";

		return ins.contains("r15") ? "r15" : "";
	}

}


