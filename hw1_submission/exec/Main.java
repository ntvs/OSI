// Nick Ribeiro
// Homework 1
// 02-09-22

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

//************************************************************
//
// INTERFACE: SystemConstants
//
// Task performed
//    Contains all constants
//    This includes all errors, system settings, execution times, and fetch modes
//
// Input Parameters
//    None
//
// Output Parameters
//    None
//
// Function Return Value
//    None
//
//************************************************************
interface SystemConstants {

    //Settings
    public int WORDSIZE = 10000;
    public int GPRSIZE = 8;
    public long VALID_PROGRAM_AREA = 2999;

    //Fetch mode constants
    public long FETCH_REGISTER = 1;
    public long FETCH_REGISTER_DEFERRED = 2;
    public long FETCH_AUTO_INCREMENT = 3;
    public long FETCH_AUTO_DECREMENT = 4;
    public long FETCH_DIRECT = 5;
    public long FETCH_IMMEDIATE = 6;

    //Execution time constants
    public long TIME_HALT = 12;
    public long TIME_ADD = 3;
    public long TIME_SUBTRACT = 3;
    public long TIME_MULTIPLY = 6;
    public long TIME_DIVIDE = 6;
    public long TIME_MOVE = 2;
    public long TIME_BRANCH = 2;
    public long TIME_BRANCH_PLUS = 4;
    public long TIME_BRANCH_MINUS = 4;
    public long TIME_BRANCH_ZERO = 4;
    public long TIME_PUSH = 2;
    public long TIME_POP = 2;
    public long TIME_SYSTEM_CALL = 12; 

    //Error constants
    public long OK = 0;
    public long ERROR = -1;
    public long ERROR_INVALID_ADDRESS = -2;
    public long ERROR_INVALID_OPERANDS = -3;
    public long ERROR_INVALID_FETCH_MODE = -4;
    public long ERROR_IMMEDIATE_DESTINATION = -5;
    public long ERROR_FATAL_RUNTIME = -6;
    public long ERROR_INVALID_OPCODE = -7;
    public long ERROR_FILE_NOT_FOUND = -8;
    public long ERROR_FORMATTING = -9;
    public long ERROR_WORD_LENGTH = -10;

}


//************************************************************
//
// CLASS: Main
//
// Task performed
//    The place where every method is called from and contains all hardware
//    Also has accessors and mutators to interact with the hardware
//
// Input Parameters
//    None
//
// Output Parameters
//    None
//
// Function Return Value
//    None
//
//************************************************************
public class Main {

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
    private static long sp; //Stack pointer


    //************************************************************
    //
    // METHOD: Main
    //
    // Task performed
    //    Invokes initializeSystem() method to initialize all hardware values to 0
    //    Then creates AbsoluteLoader object and calls load() method to load user program
    //    Next, calls dumpMemory() method to list all values of the hardware after the program loads
    //    Now, a new CPU object is created and it begins the CPU cycle with cycle() method
    //    Finally, uses dumpMemory() again to display the hardware values after the program finishes
    //
    // Input Parameters
    //    None
    //
    // Output Parameters
    //    None
    //
    // Function Return Value
    //    None
    //
    //************************************************************
    public static void main(String[] args) {

        //Informational messages
        System.out.printf("%n%n[HYPO]");

        //Initialize everything to 0
        initializeSystem();

        //Call the AbsoluteLoader
        AbsoluteLoader absoluteLoader = new AbsoluteLoader(); //New instance
        pc = absoluteLoader.load(); //Set program counter to the AbsoluteLoader return value
                                    //Note that the mutator is not used. This is to suppress
                                    //the program counter error message if the file is not found 

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


    //************************************************************
    // METHOD: InitializeSystem
    //
    // Task Description:
    //     Set all global system hardware components to 0 
    //
    // Input Parameters
    //    None
    //
    // Output Parameters
    //    None
    //
    // Function Return Value
    //    None
    //************************************************************
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

        System.out.printf("%nSystem initialized...%n");
    }


    //************************************************************
    // Function: DumpMemory
    //
    // Task Description:
    //    Displays a string passed as one of the  input parameter.
    //    Displays content of GPRs, SP, PC, PSR, system Clock and
    //    the content of specified memory locations in a specific format.
    //
    // Input Parameters
    //    String                String to be displayed
    //    StartAddress          Start address of memory location - First location to print
    //    Size                  Number of locations to dump
    // Output Parameters
    //    None            
    //
    // Function Return Value
    //    None                
    //************************************************************
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
        System.out.printf("%n[MEMORY DUMP]%nInput parameter string: \"%s\"%n%n", inputParameterString);
        
        //Print GPR labels + SP AND PC labels
        System.out.printf("%-8s%-8s%-8s%-8s%-8s%-8s%-8s%-8s%-8s%-8s%-8s", "GPRs:", "G0", "G1", "G2", "G3", "G4", "G5", "G6", "G7", "SP", "PC");
        
        //Print values stored in the GPRs
        System.out.printf("%n%-8s%-8d%-8d%-8d%-8d%-8d%-8d%-8d%-8d%-8d%-8d", " ", gpr[0], gpr[1], gpr[2], gpr[3], gpr[4], gpr[5], gpr[6], gpr[7], sp, pc);

        System.out.printf("%n");
        
        //Print memory address labels
        System.out.printf("%n%-8s%-8s%-8s%-8s%-8s%-8s%-8s%-8s%-8s%-8s%-8s", "Addr:", "+0", "+1", "+2", "+3", "+4", "+5", "+6", "+7", "+8", "+9");
        
        //While the address is within the word size and within the end address
        while (addr < hypoMemory.length && addr < endAddress) {

            //Print the address number
            System.out.printf("%n%-8d", addr);
            String addressValues = "";
            
            //Attempt to print the next 10 address values
            for (int i = 0; i < 10; i++) {

                //If the address is less than the word size and less than
                //the end address, print it
                if (addr < hypoMemory.length && addr < endAddress) {
                    addressValues += String.format("%-8d", hypoMemory[(int)addr]);
                }
                //Increment the address no matter what, it should increment 10 times
                addr++;
            }

            //If the address is within the word size limit and the end address limit
            //print out its value
            System.out.printf(addressValues);
        }

        //Print clock and PSR values
        System.out.printf("%n%nClock: %d%nPSR: %d", clock, psr);

        System.out.printf("%n");

        System.out.printf("========================================================================================%n");

    }


    //************************************************************
    // ACCESSOR METHODS
    //
    // Task Description:
    //    Allows for retrieving values of private hardware variables with
    //    built-in validation
    //
    // Input Parameters
    //    getHypoMemory() - long location
    //    getGPR()        - long register
    //
    // Output Parameters
    //    None          
    //
    // Function Return Value
    //    all             - the value of the hardware
    //    getHypoMemory() - SystemConstants.ERROR_INVALID_ADDRESS
    //    getGPR()        - SystemConstants.ERROR_INVALID_ADDRESS                
    //************************************************************

    //This goes to a specified location in memory
    //And returns whatever is stored there if the location is valid
    public static long getHypoMemory(long location) {

        //Note that the isValidProgramArea method is not used.
        //This is because accessing memory outside the valid area may be needed
        if (location < hypoMemory.length && location > -1) {
            return hypoMemory[(int)location];
        } else {
            error = SystemConstants.ERROR_INVALID_ADDRESS;
            System.out.printf("%n$ ERROR %d: Location %d is greater than the word size or negative and cannot be accessed!%n", error, location);
            return error;
        }
    }

    //This goes to a specified GPR
    //And returns whatever is stored there if the GPR is valid
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


    //************************************************************
    // MUTATOR METHODS
    //
    // Task Description:
    //    Allows for setting values of private hardware variables with
    //    built-in validation
    //
    // Input Parameters
    //    all             - long value
    //    getHypoMemory() - long location, long instruction
    //    getGPR()        - long register, long value
    //
    // Output Parameters
    //    None          
    //
    // Function Return Value
    //    all             - void
    //    getHypoMemory() - SystemConstants.ERROR_INVALID_ADDRESS
    //    getGPR()        - SystemConstants.ERROR_INVALID_ADDRESS
    //    setPC()         - SystemConstants.ERROR_INVALID_ADDRESS
    //    incrementPC()   - SystemConstants.ERROR_INVALID_ADDRESS                
    //************************************************************
    public static void setError(long code) {
        error = code;
    }
    
    public static void setHypoMemory(long location, long instruction) {

        //Note that the isValidProgramArea method is not used.
        //This is because allocating memory outside the valid area may be needed
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

    //Sets the program counter if the provided address is within the valid program area
    public static void setPC(long addr) {
        if (isValidProgramArea(addr)) {
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


    //************************************************************
    // BOOLEAN METHODS
    //
    // Task Description:
    //    Checks if a given address is within the valid program area
    //
    // Input Parameters
    //    long addr
    //
    // Output Parameters
    //    None          
    //
    // Function Return Value
    //    true
    //    false             
    //************************************************************
    public static boolean isValidProgramArea(long addr) {
        return addr < SystemConstants.VALID_PROGRAM_AREA+1 && addr > -1;
    }

}


//************************************************************
//
// CLASS: AbsoluteLoader
//
// Task performed
//    Gets a filename thru user input or by manually setting the file path
//    Has a load method which loads that file into main memory and returns PC value 
//    if loaded successfully
//
// Input Parameters
//    Optional: file path in constructor
//
// Output Parameters
//    None
//
// Function Return Value
//    None
//
//************************************************************
class AbsoluteLoader {
    
    //SCANNERs - USER INPUT + FILE READER
    public Scanner input = new Scanner(System.in);
    public Scanner fileReader;

    private File programFile; //contains the program file

    //Constructors
    public AbsoluteLoader(String fileName) {
        input.close();
        setFile(fileName);
    }
    public AbsoluteLoader() {
        System.out.printf("%n$ Enter program file name >>> ");
        String fileName = input.nextLine();
        input.close();

        setFile(fileName);
    }

    //Creates file object based on a provided string
    public void setFile(String fileName) {
        programFile = new File(fileName);
    }


    //********************************************************************
    // Function: load
    //
    // Task Description:
    //    Open the file containing HYPO machine user program and 
    //    load the content into HYPO memory.
    //    On successful load, return the PC value in the End of Program line.
    //    On failure, display appropriate error message and return appropriate error code
    //
    // Input Parameters
    //    filename from the instance variable
    //
    // Output Parameters
    //    None
    //
    // Function Return Value will be one of the following:
    //    ERROR_FILE_NOT_FOUND       Unable to open the file/not found
    //    ERROR_INVALID_ADDRESS      Invalid address error
    //    ERROR_WORD_LENGTH          Instruction or number exceeds 6 digits
    //    ERROR_FORMATTING           Line is not formatted correctly
    //    ErrorNoEndOfProgram        Missing end of program indicator
    //    0 - Valid address range    Successful Load, valid PC value
    //************************************************************
    public long load() {
        
        //Checks to see if file exits
        try {
            //If yes, proceed with scanning the file
            this.fileReader = new Scanner(programFile);
        } catch(FileNotFoundException e) {

            //If no, print the error
            System.out.printf("%n$ The file \"%s\" was not found.%n", programFile.getAbsolutePath());
            return SystemConstants.ERROR_FILE_NOT_FOUND;
        }

        int lineCount = 0; //Keeps track of how many lines read for error purposes

        //Read the file until there are no more lines
        while(fileReader.hasNextLine()) {
            String data = fileReader.nextLine(); //Grab the entire line
            String[] contents = data.split(" "); //Each line should have 2 values separated by a space. Split the line by a space
            
            //Check if line is in the correct format
            //If the array resulting from the split does not contain exactly 2 elements, the line is not formatted correctly
            if (contents.length == 2) {
                //The first element should be considered an address
                //The second element should be considered an instruction
                long addr = Long.parseLong(contents[0]);
                long instruction = Long.parseLong(contents[1]);

                //If the address is within the valid program area, go to that memory location and load the instruction
                //Otherwise if the address is -1 or lower, the end of the program has been reached
                if (Main.isValidProgramArea(addr)) {
                    
                    //Each instruction or number can only be a maximum of 6 digit. If not, the instruction is invalid
                    if(instruction > 999999) {
                        System.out.printf("%n$ The instruction at line %d is longer than 6 digits and the program was not loaded.%n", lineCount);
                        return SystemConstants.ERROR_WORD_LENGTH;
                    }

                    //Set the value of the specified memory address to the instruciton or number provided
                    Main.setHypoMemory(addr, instruction);

                //When the line reads -1, finish loading the program
                } else if(addr == -1) {
                    System.out.printf("%n$ End of program reached and program loaded successfully.%n");
                    return instruction;

                //Else, report invalid address error
                } else {
                    System.out.printf("%n$ Address %d in line %d is not a valid!%n", addr, lineCount);
                    return SystemConstants.ERROR_INVALID_ADDRESS;
                }

            } else {
                System.out.printf("%n$ Line %d is not formatted correctly and the program was not loaded.%n", lineCount);
                return SystemConstants.ERROR_FORMATTING;
            }

            lineCount++; //Increment the line count
        }



        //Close file reader and return OK if there is no error
        fileReader.close();
        return SystemConstants.OK;
    } 
}


//************************************************************
//
// CLASS: CPU
//
// Task performed
//    Runs the program that has been loaded in main memory
//    Performs operations based on the program
//
// Input Parameters
//    None
//
// Output Parameters
//    None
//
// Function Return Value
//    None
//
//************************************************************
class CPU {
    
    //Instance variables
    private long cycles; //Holds the number of cycles that has occurred

    private boolean halt; //halt status
    private long error; //error status

    private long op1Value; //final value container of op 1 used for CPU operations
    private long op2Value; //final value container of op 2 used for CPU operations

    private long op1Address; //address container used to determine value 1
    private long op2Address; //address container used to determine value 2

    //Constructor - set everything to 0
    public CPU() {
        this.cycles = 0;
        this.halt = false;
        this.error = SystemConstants.OK;
        this.op1Value = 0;
        this.op2Value = 0;
        this.op1Address = 0;
        this.op2Address = 0;
    }


    //************************************************************
    // Function: cycle
    //
    // Task Description:
    //    Performs CPU cycles until an error occurs or halt is issued
    //    Executes the program in main memory
    //    It does this based on the final values of operands determined
    //    by the fetchOperand() method
    //    The CPU then adjust memory values accordingly.
    //
    // Input Parameters
    //    None
    //
    // Output Parameters
    //    None
    //
    // Function Return Value
    //    ERROR_IMMEDIATE_DESTINATION       Destination cannot be immediate value when writing
    //    ERROR_FATAL_RUNTIME               Division by 0
    //    ERROR_INVALID_OPCODE              Given CPU mode does not exist
    //    ERROR_INVALID_OPERANDS            op1Mode, op2Mode, op1GPR, or op2GPR are outside valid ranges
    //************************************************************
    public void cycle() {

        //Cycle the CPU until state is set to halt or an error is set
        //Original condition: !halt && error >= 0
        //Debugging condition: !halt && error >= 0 && cycles < 1   <- specify the # of cycles you want
        while (!halt && error >= 0) {

            updateRegisters();
            
            long[] operands = parseOperands(); //Parse each digit from the 6 digit number in the IR
            //[0] = opCode
            //[1] = op1Mode
            //[2] = op1GPR
            //[3] = op2Mode
            //[4] = op2GPR

            if (operands[1] <= 6 && operands[3] <= 6 && operands[1] >= 0 && operands[3] >= 0 && operands[2] <= 8 && operands[4] <= 8 && operands[2] >= 0 && operands[4] >= 0) {
                
                long status;
                long result;

                switch ((int)operands[0]) {
                    
                    //Issue a halt: this mode will stop the CPU

                    case 0: //halt
                        this.halt = true;
                        System.out.printf("%n$ %d HALT: Halt encountered.%n", error);
                        Main.incrementClock(SystemConstants.TIME_HALT);
                        break;


                    // MATH/MOVE MODES 1-5:
                    // Go to each one to read more
                    //
                    // 1: Addition
                    //
                    // 2: Subtraction
                    //
                    // 3: Multiplication
                    //
                    // 4: Division
                    //
                    // 5: MOVE

                    //Addition mode: Add value 1 and value 2 together after setting them.
                    // Then, based on what mode op1 is, write the difference to a location or discard it

                    case 1: //add
                        //Set value 1
                        status = fetchOperand(operands[1], operands[2], 1, 1);
                        if (status < 0) {
                            break;
                        }
                        //Set value 2
                        status = fetchOperand(operands[3], operands[4], 2, 2);
                        if (status < 0) {
                            break;
                        }
                        
                        //Add both values
                        result = this.op1Value + this.op2Value;

                        //If op1Mode = register mode
                        if (operands[1] == SystemConstants.FETCH_REGISTER) {

                            //Store the sum in the GPR specified by op1GPR
                            Main.setGPR(operands[2], result);
                        } else if (operands[1] == SystemConstants.FETCH_IMMEDIATE) {

                            //Do not store the sum anywhere and report an error
                            this.error = SystemConstants.ERROR_IMMEDIATE_DESTINATION;
                            System.out.printf("%n$ ERROR %d IMMEDIATE DESTINATION: Destination operand cannot be the immediate value when adding.%n", error);
                        } else {

                            //When in ANY other mode, store the sum in the address
                            //specified in the op1Address variable
                            Main.setHypoMemory(this.op1Address, result);
                        }
                        Main.incrementClock(SystemConstants.TIME_ADD);
                        break;


                    //Subtraction mode: Subtract value 2 from value 1 after fetching them.
                    // Then, based on what mode op1 is, write the difference to a location or discard it

                    case 2: //subtract
                        //Set value 1
                        status = fetchOperand(operands[1], operands[2], 1, 1);
                        if (status < 0) {
                            break;
                        }
                        //Set value 2
                        status = fetchOperand(operands[3], operands[4], 2, 2);
                        if (status < 0) {
                            break;
                        }
                        
                        //Subtract value 2 from value 1
                        result = this.op1Value - this.op2Value;

                        //If op1Mode = register mode
                        if (operands[1] == SystemConstants.FETCH_REGISTER) {

                            //Store the difference in the GPR specified by op1GPR
                            Main.setGPR(operands[2], result);
                        } else if (operands[1] == SystemConstants.FETCH_IMMEDIATE) {

                            //Do not store the sum anywhere and report an error
                            this.error = SystemConstants.ERROR_IMMEDIATE_DESTINATION;
                            System.out.printf("%n$ ERROR %d IMMEDIATE DESTINATION: Destination operand cannot be the immediate value when subtracting.%n", error);
                        } else {

                            //When in ANY other mode, store the sum in the address
                            //specified in the op1Address variable
                            Main.setHypoMemory(this.op1Address, result);
                        }
                        Main.incrementClock(SystemConstants.TIME_SUBTRACT);
                        break;
                    

                    //Multiplication mode: multiply value 1 by value 2 after fetching them.
                    // Then, based on what mode op1 is, write the result to a location or discard it

                    case 3: //multiply
                        //Set value 1
                        status = fetchOperand(operands[1], operands[2], 1, 1);
                        if (status < 0) {
                            break;
                        }
                        //Set value 1
                        status = fetchOperand(operands[3], operands[4], 2, 2);
                        if (status < 0) {
                            break;
                        }
                        
                        //Find their product
                        result = this.op1Value * this.op2Value;

                        //If op1Mode = register mode
                        if (operands[1] == SystemConstants.FETCH_REGISTER) {

                            //Store the result in the GPR specified by op1GPR
                            Main.setGPR(operands[2], result);
                        } else if (operands[1] == SystemConstants.FETCH_IMMEDIATE) {

                            //Do not store the result anywhere and report an error
                            this.error = SystemConstants.ERROR_IMMEDIATE_DESTINATION;
                            System.out.printf("%n$ ERROR %d IMMEDIATE DESTINATION: Destination operand cannot be the immediate value when multiplying.%n", error);
                        } else {

                            //When in ANY other mode, store the result in the address
                            //specified in the op1Address variable
                            Main.setHypoMemory(this.op1Address, result);
                        }
                        Main.incrementClock(SystemConstants.TIME_MULTIPLY);
                        break;
                    

                    //Division mode: divide value 1 by value 2 after fetching them.
                    // Then, based on what mode op1 is, write the result to a location or discard it

                    case 4: //division
                        //Set value 1
                        status = fetchOperand(operands[1], operands[2], 1, 1);
                        if (status < 0) {
                            break;
                        }
                        //Set value 1
                        status = fetchOperand(operands[3], operands[4], 2, 2);
                        if (status < 0) {
                            break;
                        }

                        //Check value 2 for 0, as division by 0 is impossible
                        if (op2Value != 0) {
                            result = this.op1Value / this.op2Value;
                        } else {
                            this.error = SystemConstants.ERROR_FATAL_RUNTIME;
                            System.out.printf("%n$ FATAL RUNTIME ERROR %d: Cannot divide by zero.%n", error);
                            break;
                        }

                        //If op1Mode = register mode
                        if (operands[1] == SystemConstants.FETCH_REGISTER) {

                            //Store the result in the GPR specified by op1GPR
                            Main.setGPR(operands[2], result);
                        } else if (operands[1] == SystemConstants.FETCH_IMMEDIATE) {

                            //Do not store the result anywhere and report an error
                            this.error = SystemConstants.ERROR_IMMEDIATE_DESTINATION;
                            System.out.printf("%n$ ERROR %d IMMEDIATE DESTINATION: Destination operand cannot be the immediate value when dividing.%n", error);
                        } else {

                            //When in ANY other mode, store the result in the address
                            //specified in the op1Address variable
                            Main.setHypoMemory(this.op1Address, result);
                        }
                        Main.incrementClock(SystemConstants.TIME_DIVIDE);
                        break;
                    

                    //Move mode: set value 1 equal to whatever value 2 is after fetching them
                    // Then, based on what mode op1 is, write value 1 to a location or discard it

                    case 5: //move
                        //Set value 1
                        status = fetchOperand(operands[1], operands[2], 1, 1);
                        if (status < 0) {
                            break;
                        }
                        //Set value 2
                        status = fetchOperand(operands[3], operands[4], 2, 2);
                        if (status < 0) {
                            break;
                        }
                        
                        this.op1Value = this.op2Value;

                        //If op1Mode = register mode
                        if (operands[1] == SystemConstants.FETCH_REGISTER) {

                            //Store value 1 in the GPR specified by op1GPR
                            Main.setGPR(operands[2], this.op1Value);
                        } else if (operands[1] == SystemConstants.FETCH_IMMEDIATE) {

                            //Do not store value 1 anywhere and report an error
                            this.error = SystemConstants.ERROR_IMMEDIATE_DESTINATION;
                            System.out.printf("%n$ ERROR %d IMMEDIATE DESTINATION: Destination operand cannot be the immediate value when moving.%n", error);
                        } else {

                            //When in ANY other mode, store value 1 in the address
                            //specified in the op1Address variable
                            Main.setHypoMemory(this.op1Address, this.op1Value);
                        }
                        Main.incrementClock(SystemConstants.TIME_MOVE);
                        break;


                    // BRANCH MODES 6-9
                    // 
                    // 6: Assumes 1 input - looks at the value stored at the memory address that the PC is pointing to
                    //      - So it will look at the next line and assume a memory address is stored there
                    //      - Then it will jump to the memory location specified
                    //
                    // 7: Looks at the value of operand 1 & value stored at memory address that PC points to
                    //      - One of the next lines must specify an address to jump to
                    //      - IF value 1 is negative and nonzero, reset the PC to a specified value
                    //      - otherwise, skip the next line and proceed
                    //
                    // 8: Looks at the value of operand 1 & value stored at memory address that PC points to
                    //      - One of the next lines must specify an address to jump to
                    //      - IF value 1 is positive and nonzero, reset the PC to a specified value
                    //      - otherwise, skip the next line and proceed
                    //
                    // 9: Looks at the value of operand 1 & value stored at memory address that PC points to
                    //      - One of the next lines must specify an address to jump to
                    //      - IF value 1 is ZERO, reset the PC to a specified value
                    //      - otherwise, skip the next line and proceed

                    case 6: //branch/jump instruction
                        //Set the PC to the next value in the memory address that the PC points too
                        Main.setPC(Main.getHypoMemory(Main.getPC()));
                        Main.incrementClock(SystemConstants.TIME_BRANCH);
                        break;

                    case 7: //branch on minus
                        //Set value 1 and only value 1 - NOT both values
                        status = fetchOperand(operands[1], operands[2], 1, 1);
                        if (status < 0) {
                            break;
                        }
                        
                        //This assumes that the next line contains an address
                        // If value 1 is negative and nonzero
                        if (this.op1Value < 0) {
                            //Go to the next line and get that address
                            //THEN set the PC to that address
                            Main.setPC(Main.getHypoMemory(Main.getPC()));
                        } else {
                            //Otherwise, skip the next line and proceed
                            Main.incrementPC();
                        }
                        Main.incrementClock(SystemConstants.TIME_BRANCH_MINUS);
                        break;

                    case 8: //branch on plus
                        //Set value 1 and only value 1 - NOT both values
                        status = fetchOperand(operands[1], operands[2], 1, 1);
                        if (status < 0) {
                            break;
                        }

                        //This assumes that the next line contains an address
                        // If value 1 is positive and nonzero
                        if (this.op1Value > 0) {

                            //Go to the next line and assume it contains an address
                            //get that address
                            //THEN set the PC to that address
                            //This is the location the program will jump to
                            //if value 1 is positive and nonzero
                            Main.setPC(Main.getHypoMemory(Main.getPC()));
                        } else {

                            //Otherwise, skip the next line and proceed
                            Main.incrementPC();
                        }
                        Main.incrementClock(SystemConstants.TIME_BRANCH_PLUS);
                        break;

                    case 9: //branch on zero
                        //Set value 1 and only value 1 - NOT both values
                        status = fetchOperand(operands[1], operands[2], 1, 1);
                        if (status < 0) {
                            break;
                        }

                        //This assumes that the next line contains an address
                        // If value 1 IS ZERO
                        if (this.op1Value == 0) {

                            //Go to the next line and get that address
                            //THEN set the PC to that address
                            Main.setPC(Main.getHypoMemory(Main.getPC()));
                        } else {
                            //Otherwise, skip the next line and proceed
                            Main.incrementPC();
                        }
                        Main.incrementClock(SystemConstants.TIME_BRANCH_PLUS);
                        break;


                    // STACK MODES 10-11 - not yet implemented
                    // 
                    // 10.
                    //
                    // 11.
                    //

                    case 10: //push if stack is not full
                        status = fetchOperand(operands[1], operands[2], 1, 1);
                        if (status < 0) {
                            break;
                        }
                        System.out.printf("%n$ Case 10: push to stack.%n");
                        break;
                    
                    case 11: //pop if stack is not empty
                        status = fetchOperand(operands[1], operands[2], 1, 1);
                        if (status < 0) {
                            break;
                        }
                        System.out.printf("%n$ Case 11: pop from stack.%n");
                        break;


                    // SYSTEM MODE 12 - not yet implemented
                    // 

                    case 12:
                        long systemCallID = Main.getHypoMemory(Main.getPC());
                        Main.incrementPC();
                        System.out.printf("%n$ Case 12: System call.%n");
                        break;
                    
                    default:
                        this.error = SystemConstants.ERROR_INVALID_OPCODE;
                        System.out.printf("%n$ ERROR %d INVALID OPCODE: Opcode %d does not exist.%n", error, operands[0]);
                        break;
                }


            } else {
                this.error = SystemConstants.ERROR_INVALID_OPERANDS;
                System.out.printf("%n$ ERROR %d INVALID OPERANDS: One of the operands contain an invalid mode or attempts to access an invalid GPR number.%n", error);
            }

            this.cycles++;

            //DEBUGGING
            //this.halt = true;
            //printOperands(operands);
            
            //String cycleNumber = String.format("Cycle number %d", cycles);
            //Main.dumpMemory(cycleNumber, 0, 100);
        }

    }


    //************************************************************
    // Function: updateRegisters
    //
    // Task Description:
    //    Updates MAR, MBR, IR, and PC at the beginning of every CPU cycle
    //
    // Input Parameters
    //    None
    //
    // Output Parameters
    //    None
    //
    // Function Return Value
    //    ERROR_INVALID_ADDRESS         Program counter contains an invalid address
    //************************************************************
    public void updateRegisters() {
        long programCounter = Main.getPC();

        //Proceed only if 0 <= PC <= 2999 - Non negative and within the valid program area
        if (0 <= programCounter && programCounter <= SystemConstants.VALID_PROGRAM_AREA) {

            Main.setMAR(programCounter); //set MAR to the value of the PC
            Main.incrementPC(); //PC now contains the addr of the next word while MAR has the current word

            long word = Main.getHypoMemory(Main.getMAR()); //Retrieve word from memory using address stored in MAR
            Main.setMBR(word); //Load the word into the MBR

        } else {
            this.error = SystemConstants.ERROR_INVALID_ADDRESS;
            System.out.printf("%n$ ERROR %d INVALID ADDRESS: The program counter points to a negative address or an address outside the valid program area.%n", error);
        }

        Main.setIR(Main.getMBR()); //Get the MBR value and store it in the IR
    }


    //************************************************************
    // Function: parseOperands
    //
    // Task Description:
    //    Splits 5-6 digit line from the IR into an array of 5 separate values
    //
    // Input Parameters
    //    Main class IR instance variable
    //
    // Output Parameters
    //    None
    //
    // Function Return Value
    //    long[] operands
    //************************************************************
    public long[] parseOperands() {
        //Separate the opcode from the word and leave the rest in remainder
        //Opcode tells CPU what mode it should be in
        long opCode = (Main.getIR()) / 10000; //extracts the first digit from the word. example: 15060 / 10000 = 1.506 = opCode 1
        long remainder = (Main.getIR()) % 10000; //extracts everything else except the first digit... so 5060

        //Extract information about the operands... for example, in 5060, op1=50 op2=60
        //Each one of the operands are split into 2 parts, first digit being the "mode" for that operand 
        //Second digit of the operand is one of the GPRs (or usually 0 if a GPR is not used in that instance)

        //Mathematically extract the first digit of op1 (op1Mode)
        long op1Mode = remainder / 1000;
        remainder = remainder % 1000;

        //Mathematically extract the second digit of op1 (op1GPR)
        long op1GPR = remainder / 100;
        remainder = remainder % 100;

        //Mathematically extract the first digit of op2 (op2Mode)
        long op2Mode = remainder / 10;
        remainder = remainder % 10;

        //Mathematically extract the second digit of op2 (op2GPR)
        long op2GPR = remainder / 1;

        //Return all the operands as an array
        long[] operands = {opCode, op1Mode, op1GPR, op2Mode, op2GPR};

        return operands;
    }


    //************************************************************
    // Function: printOperands
    //
    // Task Description:
    //    Simple array printing method to print the array of operands for debugging
    //
    // Input Parameters
    //    long[] operands
    //
    // Output Parameters
    //    None
    //
    // Function Return Value
    //    None
    //************************************************************
    public void printOperands(long[] operands) {
        System.out.printf("%nOperands array: ");
        for (int i = 0; i < 5; i++) {
            System.out.printf("%d ", operands[i]);
        }
        System.out.printf("%n");
    }
    

    //************************************************************
    // Function: FetchOperand
    //
    // Task Description:
    //    Method/switch statement that can set op value 1 or op value 2 that 
    //    the CPU uses for operations based on the mode passed into it
    //
    // Input Parameters
    //    mode          long describing which fetch mode to use
    //    gpr           long describing which GPR to access
    //    operandAddr   integer describing which address variable to access, 1 or 2
    //    operandValue  integer describing which value variable to access, 1 or 2
    // Output Parameters
    //    op1Address/op2Address     Address container used to determine value 1 or 2
    //    op1Value/op2Value         Final value container of op 1 or 2 used for CPU operations
    //
    // Function Return Value
    //    OK                        On successful fetch
    //    ERROR_INVALID_ADDRESS     Attempts to set or access invalid memory address
    //    ERROR_INVALID_FETCH_MODE  Fetch mode does not exist
    //************************************************************
    public long fetchOperand(long mode, long gpr, int operandAddr, int operandValue) {
        //mode: long describing which fetch mode to use
        //gpr: long describing which GPR to access
        //operandAddr: integer describing which address variable to access, 1 or 2
        //operandValue: integer describing which value variable to access, 1 or 2

        switch((int)mode) {
            
            // GPR MODES 1-4: these modes deal with GPRs
            //
            // 1: Read and write to a GPR when performing math operations or moves (op1)
            //
            // 2: A GPR contains a memory location. Retrieve your value from there
            //      - Read and write to that location when performing math operations or moves (op1)
            //
            // 3: A GPR contains a memory location. Retrieve your value from there
            //      - Read and write to that location when performing math operations or moves (op1)
            //      - Increment that GPR's value after using it
            //
            // 4: A GPR contains a memory location. Retrieve your value from there
            //      - Read and write to that location when performing math operations or moves (op1)
            //      - Decrement that GPR's value after using it

            //Register mode: This mode retrieves from a GPR but will also lock you into writing 
            //to the same GPR for the current cycle (op1).

            case 1: //register mode - This mode will go to the specified GPR and retrieve its value
                setOpAddress(operandAddr, -200); //Set the specified op address var to a negative num
                
                //Set the specified op value var to the value that is in the specified GPR
                setOpValue(operandValue, Main.getGPR(gpr));
                break;


            //Register deferred mode: This mode retrieves a location from a GPR 
            //but will also lock you into writing to the same memory location for the current cycle (op1).
            //Use it when you want to fetch a memory location from a GPR

            case 2: //register deferred mode - "Op addr in GPR and value in memory"
                
                //In this case, there is something inside the GPR specified by the var gpr
                //Retrieve that value
                //Put it inside op address 1 or 2
                setOpAddress(operandAddr, Main.getGPR(gpr));

                //When in fetch mode 2, the assumption is that the
                //specified GPR contains another memory location...
                if (Main.isValidProgramArea(getOpAddress(operandAddr))) {

                    //Go to the memory address that the GPR points to
                    //Get whatever is stored there and set it as value either 1 or 2
                    setOpValue(operandValue, Main.getHypoMemory(operandAddr));
                } else {
                    this.error = SystemConstants.ERROR_INVALID_ADDRESS;
                    System.out.printf("%n$ ERROR %d INVALID ADDRESS: GPR %d is invalid in case 2.%n", error, operandAddr);
                    return this.error;
                }
                break;


            //Autoincrement mode: SAME AS MODE 2 - This mode retrieves a location from a GPR 
            //but will also lock you into writing to the same memory location for the current cycle (op1).
            //Use it when you want to fetch a memory location from a GPR

            //However, after retrieving an address from said GPR, move the contained address to the one after it
            //So now the GPR will contain the address directly after the address it previously contained

            case 3: //autoincrement mode (Op addr in GPR and value in memory)
                
                //In this case, there is something inside the GPR specified by the var gpr
                //Retrieve that value
                //Put it inside op address 1 or 2
                setOpAddress(operandAddr, Main.getGPR(gpr));

                //When in fetch mode 3, the assumption is that the
                //specified GPR contains another memory location...
                if (Main.isValidProgramArea(getOpAddress(operandAddr))) {

                    //Go to the memory address that the GPR points to
                    //Get whatever is stored there and set it as value either 1 or 2
                    setOpValue(operandValue, Main.getHypoMemory(getOpAddress(operandAddr)));

                    //Now, increment the value of that GPR by 1
                    //So essentially, move the address contained by the GPR to the next address after it
                    Main.setGPR(gpr, (Main.getGPR(gpr))+1);

                } else {
                    this.error = SystemConstants.ERROR_INVALID_ADDRESS;
                    System.out.printf("%n$ ERROR %d INVALID ADDRESS: GPR %d is invalid in case 3.%n", error, operandAddr);
                }
                break;


            //Autodecrement mode: SAME AS MODE 3 - This mode retrieves a location from a GPR 
            //but will also lock you into writing to the same memory location for the current cycle (op1).
            //Use it when you want to fetch a memory location from a GPR

            //However, after retrieving an address from said GPR, move the contained address to the one before it
            //So now the GPR will contain the address directly before the address it previously contained

            case 4: //autodecrement mode 
                Main.setGPR(gpr, (Main.getGPR(gpr))-1); //decrement register value by 1

                //In this case, there is something inside the GPR specified by the var gpr
                //Retrieve that value
                //Put it inside op address 1 or 2
                setOpAddress(operandAddr, Main.getGPR(gpr)); //Set address var 1 or 2 to the contents of said GPR

                if (Main.isValidProgramArea(getOpAddress(operandAddr))) {
                    
                    setOpValue(operandValue, Main.getHypoMemory(getOpAddress(operandAddr)));

                } else {
                    this.error = SystemConstants.ERROR_INVALID_ADDRESS;
                    System.out.printf("%n$ ERROR %d INVALID ADDRESS: GPR %d is invalid in case 4.%n", error, operandAddr);
                }
                break;


            // LINE MODES 5-6: these modes deal with the following line
            //
            // 5: The next line contains a memory location. Retrieve your value from there
            //      - Read and write to that location when performing math operations or moves (op1)
            //
            // 6: The next line contains the value you want to use. Retrieve it directly from there
            //      - Cannot write to the next line when performing math operations or moves (op1)

            //Direct mode: this mode looks at the next line. It assumes whatever value contained in the memory address
            // in the next line is another memory address. Then, it will go to that location and retrieve whatever
            // value is there.
            // This mode will lock you into writing to the same memory location for the current cycle (op1).
            // Use it when you want to fetch from a remote memory location anywhere 

            case 5: //direct mode - "Op address is in the instruction that is in the program counter"
                //Do need to check if PC value is valid because validation is already performed when updating the PC value
                
                //In this case, the PC points to a memory location
                //Retrieve the contents of that memory location
                //Put it inside op address 1 or 2
                setOpAddress(operandAddr, Main.getHypoMemory(Main.getPC()));
                Main.incrementPC(); // -> make sure the PC is moved forward since it should always point to the next place

                //When in fetch mode 5, the assumption is that the
                //location specified by the PC contains another memory location... 
                if (Main.isValidProgramArea(getOpAddress(operandAddr))) {

                    //Go to the memory address in the PC (AKA the next line)
                    //Get whatever is stored there and set it as value either 1 or 2
                    setOpValue(operandValue, Main.getHypoMemory(getOpAddress(operandAddr)));
                } else {
                    this.error = SystemConstants.ERROR_INVALID_ADDRESS;
                    System.out.printf("%n$ ERROR %d INVALID ADDRESS: Op address %d contains %d in case 5.%n", error, operandAddr, getOpAddress(operandAddr));
                }
                break;

            
            //Immediate mode: this mode looks at the next line. It assumes whatever value contained in the memory address
            // in the next line is the value you want to use.
            // Use it when you want to fetch a value directly from the next line
            // This mode locks you from writing (op1) because the CPU will not allow writing in immediate mode. 

            case 6: //immediate mode - "Op value is in the instruction"
                //Do need to check if PC value is valid because validation is already performed when updating the PC value
                setOpAddress(operandAddr, -300);

                //In this case, the program counter contains a memory location
                //Go to that memory location and retrieve the contents of what is stored there
                //Place it inside op value 1 or 2

                //When using mode 6, the assumption is that the
                //location specified by the PC contains a value
                //This is why we do not need to check if that number is
                //in the valid program area, as it is a number and not an address
                setOpValue(operandValue, Main.getHypoMemory(Main.getPC()));
                Main.incrementPC();
                break;

            default: //invalid mode
                this.error = SystemConstants.ERROR_INVALID_FETCH_MODE;
                System.out.printf("%n$ ERROR %d INVALID FETCH MODE: Fetch mode %d does not exist.%n", error, mode);
                break;
        }

        return this.error;
    }


    //************************************************************
    // ACCESSOR METHODS
    //
    // Task Description:
    //    Allows for retrieving values of private variables with
    //    built-in validation
    //
    // Input Parameters
    //    getOpAddress() - int operandAddr
    //    getOpValue()   - int operandValue
    //
    // Output Parameters
    //    None          
    //
    // Function Return Value
    //    getOpAddress() - op1 or op2 address instance variable
    //    getOpValue()   - op1 or op2 value instance variable   
    //    -9000          - impossible address which cycle() method will catch and report error            
    //************************************************************
    public long getOpAddress(int operandAddr) {
        if (operandAddr == 1) {
            return this.op1Address;
        } else if (operandAddr == 2) {
            return this.op2Address; 
        } else {
            return -9000;
        }
    }

    public long getOpValue(int operandValue) {
        if (operandValue == 1) {
            return this.op1Value;
        } else if (operandValue == 2) {
            return this.op2Value;
        } else {
            return -9000;
        }
    }

    public long getCycles() {
        return this.cycles;
    }


    //************************************************************
    // MUTATOR METHODS
    //
    // Task Description:
    //    Allows for setting values of private variables
    //
    // Input Parameters
    //    setOpAddress() - int operandAddr, long value
    //    setOpValue()   - int operandValue, long value
    //
    // Output Parameters
    //    None          
    //
    // Function Return Value
    //    all             - void              
    //************************************************************
    public void setOpAddress(int operandAddr, long value) {
        if (operandAddr == 1) {
            this.op1Address = value; 
        } else if (operandAddr == 2) {
            this.op2Address = value; 
        }
    }

    public void setOpValue(int operandValue, long value) {
        if (operandValue == 1) {
            this.op1Value = value;
        } else if (operandValue == 2) {
            this.op2Value = value;
        }
    }


    //************************************************************
    // BOOLEAN METHODS
    //
    // Task Description:
    //    Checks if a given GPR is within 0-7
    //
    // Input Parameters
    //    long gpr
    //
    // Output Parameters
    //    None          
    //
    // Function Return Value
    //    true
    //    false             
    //************************************************************
    public boolean isValidGPR(long gpr) {
        return gpr > -1 && gpr < 8;
    }

}