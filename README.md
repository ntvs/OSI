# "HYPO" hypothetical machine architecture simulation

Created:
1. Static class Main
    - Hardware declarations as long variables (memory array, mar, mbr, clock, gpr array, ir, psr, pc, and sp)
    - initializeSystem() method to initialize all hardware values to 0
    - dumpMemory() method to list all values of the hardware upon invocation

2. Class AbsoluteLoader
    - Constructors that accept name of file to read
    - load() method that:
        1. Read file line by line
        2. Splits the two arguments in each line
        3. Parses the long values
        4. Checks to see if the line is formatted correctly, if address is out of range, and if the instruction is 6 digits or less
        5. Stores the instructions at the specified indices in the memory array
        6. Returns value that the program counter should be set to (the next instruction)