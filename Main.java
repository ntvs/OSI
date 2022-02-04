import java.util.*;
import java.io.FileNotFoundException;

public class Main {

    //SCANNER - USER INPUT
    public static Scanner input = new Scanner(System.in);

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

        System.out.printf("%n%n[HYPO]");

        initializeSystem();
        System.out.printf("%nSystem initialized...%n");

        AbsoluteLoader absoluteLoader = new AbsoluteLoader("programs/loop.txt");
        pc = absoluteLoader.load(); //Set program counter to the AL return value

        System.out.printf("%n$ Program counter set to %d...%n", pc);

        dumpMemory("Dump after PC set", 0, 100);

        CPU cpu = new CPU();
        cpu.cycle();

        dumpMemory("Dump after 1 CPU cycle", 0, 100);
        System.out.printf("%n%nMAR: %d, MBR: %d, IR: %d%n%n", mar, mbr, ir);

    }


    //Initialize the system
    public static void initializeSystem() {
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
                //print it out
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
    public static long getHypoMemory(long location) {
        if (location < hypoMemory.length) {
            return hypoMemory[(int)location];
        } else {
            System.out.printf("%n$ Location %d is greater than the word size and cannot be accessed!", location);
            return SystemConstants.ERROR;
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

    public static long getPC() {
        return pc;
    }

    //Mutators
    public static void setHypoMemory(long location, long instruction) {
        if (location < hypoMemory.length) {
            hypoMemory[(int)location] = instruction;
        } else {
            System.out.printf("%n$ Location %d is greater than the word size and cannot be accessed!", location);
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
        if (addr < hypoMemory.length) {
            pc = addr;
        } else {
            System.out.printf("%n$ PC cannot be set to %d.", addr);
        }   
    }
    public static void incrementPC() {
        setPC(pc+1);
    }

}