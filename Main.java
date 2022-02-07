import java.util.*;
import java.io.FileNotFoundException;

public class Main {

    //SCANNER - USER INPUT
    public static Scanner input = new Scanner(System.in);

    //Machine error variable (Separate from CPU error variable)
    private static long error;

    //HARDWARE
    //Hypo main memory
    private static long[] hypoMemory; //Each word should contain a 6 integer value

    //Hypo memory registers
    private static long mar; //Memory address register
    private static long mbr; //Memory buffer register

    //Clock
    private static long clock;

    //CPU registers (GPR)
    private static long[] gpr;

    //CPU registers
    private static long ir; //Instruction register
    private static long psr; //Processor status register
    private static long pc; //Program counter
    private static long sp; 


    //Main method
    public static void main(String[] args) {

        //Informational messages
        System.out.printf("%n%n[HYPO]");

        initializeSystem();
        System.out.printf("%nSystem initialized...%n");

        //Call the AbsoluteLoader
        AbsoluteLoader absoluteLoader = new AbsoluteLoader(); //New instance
        setPC(absoluteLoader.load()); //Set program counter to the AbsoluteLoader return value 

        //Proceed only if there has been no error from the AbsoluteLoader (error indicated by a negative PC)
        if (pc > 0) {
            System.out.printf("%n$ Program counter set to %d...%n", pc);

            dumpMemory("Dump after loading user program", 0, 100);

            //Instantiate a new CPU
            CPU cpu = new CPU();
            cpu.cycle(); //Perform the CPU cycle

            //Dump memory after X CPU cycles
            String dumpAfterX = String.format("Dump after %d CPU cycle(s)", cpu.getCycles());
            dumpMemory(dumpAfterX, 0, 100);

            //Display MAR, MBR, and IR values
            System.out.printf("%nMAR: %d, MBR: %d, IR: %d%n", mar, mbr, ir);
        }

        System.out.printf("%n%n");

    }


    //Initialize the system - Set everything to 0
    public static void initializeSystem() {
        error = 0;
        hypoMemory = new long[SystemConstants.WORDSIZE];
        mar = 0;
        mbr = 0;
        clock = 0;
        gpr = new long[SystemConstants.GPRSIZE];
        ir = 0;
        psr = 0;
        pc = 0;
        sp = 0;
    }


    //Dump memory
    public static void dumpMemory(String inputParameterString, long startAddress, long size) {
        //inputParameterString = a string to print out when the dump occurs
        //startAddress = beginning address of memory to dump
        //size = used to calculate the last address of memory to dump

        //Calculate last address
        long addr = startAddress;
        long endAddress = startAddress + size;

        System.out.printf("%n========================================================================================");

        //State that a memory dump is occurring and state the input parameter string
        //Also print out "GPRs"
        System.out.printf("%n[MEMORY DUMP]%nInput parameter string: \"%s\"%n%nGPRs:\t", inputParameterString);
        
        //Print GPR labels
        for (int i = 0; i < gpr.length; i++) {
            System.out.printf("G%d\t", i);
        }
        //Print SP and PC labels
        System.out.printf("SP\tPC\t");

        //Print values stored in the GPRs
        System.out.printf("%n\t");
        for (int i = 0; i < gpr.length; i++) {
            System.out.printf(" %d\t", gpr[i]);
        }
        //Print SP and PC values
        System.out.printf(" %d\t %d\t", sp, pc);

        System.out.printf("%n");
        
        //Print address labels
        System.out.printf("%nAddr:\t");
        for (int i = 0; i < 10; i++) {
            System.out.printf("+%d\t", i);
        }
        
        //While the address is within the word size and within the end address
        while (addr < hypoMemory.length && addr < endAddress) {

            //Print the address number
            System.out.printf("%n%d\t", addr);

            for (int i = 0; i < 10; i++) {
                //If the address is within the word size limit and the end address limit
                //print out its value
                if (addr < hypoMemory.length && addr < endAddress) {
                    System.out.printf(" %d\t", hypoMemory[(int)addr]);
                }
                //Move the address forward - repeat 10 times
                addr++;
            }

        }

        //Print clock and PSR values
        System.out.printf("%n%nClock: %d%nPSR: %d", clock, psr);

        System.out.printf("%n");

        System.out.printf("========================================================================================%n");

    }


    //Accessors

    //This goes to a specified location in memory
    //And returns whatever is stored there if the location is valid
    public static long getHypoMemory(long location) {
        if (location < hypoMemory.length && location > -1) {
            return hypoMemory[(int)location];
        } else {
            error = SystemConstants.ERROR_INVALID_ADDRESS;
            System.out.printf("%n$ ERROR %d: Location %d is greater than the word size or negative and cannot be accessed!%n", error, location);
            return error;
        }
    }

    public static long getGPR(long register) {
        if (register < gpr.length) {
            return gpr[(int)register];
        } else {
            error = SystemConstants.ERROR_INVALID_ADDRESS;
            System.out.printf("%n$ ERROR %d: GPR %d does not exist and cannot be accessed!%n", error, register);
            return error;
        }
    }

    public static long getMBR() {
        return mbr;
    }
    public static long getMAR() {
        return mar;
    }
    public static long getIR() {
        return ir;
    }
    public static long getSP() {
        return sp;
    }

    public static long getPC() {
        return pc;
    }

    //Mutators
    public static void setError(long code) {
        error = code;
    }
    
    public static void setHypoMemory(long location, long instruction) {
        if (location < hypoMemory.length && location > -1) {
            hypoMemory[(int)location] = instruction;
        } else {
            error = SystemConstants.ERROR_INVALID_ADDRESS;
            System.out.printf("%n$ ERROR %d: Location %d is greater than the word size and cannot be accessed!%n", error, location);
        }
    }

    public static void setGPR(long register, long value) {
        if (register < gpr.length) {
            gpr[(int)register] = value;
        } else {
            error = SystemConstants.ERROR_INVALID_ADDRESS;
            System.out.printf("%n$ ERROR %d: GPR %d does not exist and cannot be set!%n", error, register);
        }
    }

    public static void setMBR(long word) {
        mbr = word;
    }
    public static void setMAR(long word) {
        mar = word;
    }
    public static void setIR(long word) {
        ir = word;
    }

    public static void setPC(long addr) {
        if (addr < SystemConstants.VALID_PROGRAM_AREA && addr > -1) {
            pc = addr;
        } else {
            error = SystemConstants.ERROR_INVALID_ADDRESS;
            pc = error;
            System.out.printf("%n$ ERROR %d: PC cannot be set to %d.%n", error, addr);
        }   
    }
    public static void incrementPC() {
        setPC(pc+1);
    }

    public static void incrementClock(long time) {
        clock += time;
    } 
    public static void incrementClock() {
        incrementClock(1);
    }

    //Boolean methods
    public static boolean isValidProgramArea(long addr) {
        return addr < SystemConstants.VALID_PROGRAM_AREA+1 && addr > -1;
    }

}