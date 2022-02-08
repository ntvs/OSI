# "HYPO" hypothetical machine architecture simulation

Created:
1. Static class Main
    - Hardware declarations as long variables (memory array, mar, mbr, clock, gpr array, ir, psr, pc, and sp)
    - initializeSystem() method to initialize all hardware values to 0
    - dumpMemory() method to list all values of the hardware upon invocation
    - Accessors and mutators to manipulate the system hardware

2. Class AbsoluteLoader
    - Constructors that accept name of file to read
    - load() method that:
        1. Read file line by line
        2. Splits the two arguments in each line
        3. Parses the long values
        4. Checks to see if the line is formatted correctly, if address is out of range, and if the instruction is 6 digits or less
        5. Stores the instructions at the specified indices in the memory array
        6. Returns value that the program counter should be set to (the next instruction)
    - Problems:
        1. Scanner does not close for every case

3. Class CPU
    - Constructor
    - updateRegisters() method that:
        1. Sets the MAR to the current address in the program counter
        2. Increments the PC to the next address
        3. Retrieves the word from memory using the address stored in the MAR
        4. Loads the word into the MBR
        5. Sets the instruction register to the instruction stored in the MBR
    - parseOperands() method that:
        1. Mathematically splits the instruction into an array of 5
        2. Returns the array of operands from the 5-6 digit instruction
        - [0] = opCode
        - [1] = op1Mode
        - [2] = op1GPR
        - [3] = op2Mode
        - [4] = op2GPR
    - fetchOperand() method that:
        1. Manipulates the values in a set of variables. These variables mimmick the values and addresses of op1 and op2 in a real machine
        2. Does this based on the mode of each op. Each op is made up of two digits, the mode in which it should be manipulated and a GPR value
        - Misinterpretations:
            - Case 2, 3, 4, 5: changed isValidGPR() to isValidProgramArea() since these modes assume the contents of operandAddr is an address and not a number
            - The true use of this method is to set the operand values based on what the 5-6 digit instruction is saying... so think of it kind of as "setValue()"
            - The cycle method then performs operations with these values
            - The value of what the operands become relies on what the program counter is pointing to and which mode they are in according to the instruction
            - It important to consider the value of the PC and how it changes when writing instructions for this reason
    - cycle() method that:
        1. Updates the registers and progresses the PC by 1
        2. Parses the operands from the instruction in memory
        3. Performs operations based on the 1 or 2 digit opcode in the operands array
        4. Repeats 1-3 until the CPU error variable is negative or the halt variable is true
    - CPU operation modes 10-12 not yet implemented

4. Interface SystemConstants
    - Machine settings
    - Fetch modes
    - Execution times
    - Error codes