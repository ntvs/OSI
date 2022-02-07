public class CPU {
    
    //Instance variables
    private long cycles;

    private boolean halt;
    private long error;

    private long op1Value;
    private long op2Value;

    private long op1Address;
    private long op2Address;

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
                    case 0: //halt
                        this.halt = true;
                        System.out.printf("%n$ %d HALT: Halt encountered.%n", error);
                        Main.incrementClock(SystemConstants.TIME_HALT);
                        break;

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
                    
                    case 3: //multiply
                        status = fetchOperand(operands[1], operands[2], 1, 1);
                        if (status < 0) {
                            break;
                        }
                        status = fetchOperand(operands[3], operands[4], 2, 2);
                        if (status < 0) {
                            break;
                        }
                        
                        result = this.op1Value * this.op2Value;

                        //If op1Mode = register mode
                        if (operands[1] == SystemConstants.FETCH_REGISTER) {
                            Main.setGPR(operands[2], result);
                        } else if (operands[1] == SystemConstants.FETCH_IMMEDIATE) {
                            this.error = SystemConstants.ERROR_IMMEDIATE_DESTINATION;
                            System.out.printf("%n$ ERROR %d IMMEDIATE DESTINATION: Destination operand cannot be the immediate value when multiplying.%n", error);
                        } else {
                            Main.setHypoMemory(this.op1Address, result);
                        }
                        Main.incrementClock(SystemConstants.TIME_MULTIPLY);
                        break;
                    
                    case 4: //division
                        status = fetchOperand(operands[1], operands[2], 1, 1);
                        if (status < 0) {
                            break;
                        }
                        status = fetchOperand(operands[3], operands[4], 2, 2);
                        if (status < 0) {
                            break;
                        }
                        
                        if (op2Value != 0) {
                            result = this.op1Value / this.op2Value;
                        } else {
                            this.error = SystemConstants.ERROR_FATAL_RUNTIME;
                            System.out.printf("%n$ FATAL RUNTIME ERROR %d: Cannot divide by zero.%n", error);
                            break;
                        }

                        //If op1Mode = register mode
                        if (operands[1] == SystemConstants.FETCH_REGISTER) {
                            Main.setGPR(operands[2], result);
                        } else if (operands[1] == SystemConstants.FETCH_IMMEDIATE) {
                            this.error = SystemConstants.ERROR_IMMEDIATE_DESTINATION;
                            System.out.printf("%n$ ERROR %d IMMEDIATE DESTINATION: Destination operand cannot be the immediate value when dividing.%n", error);
                        } else {
                            Main.setHypoMemory(this.op1Address, result);
                        }
                        Main.incrementClock(SystemConstants.TIME_DIVIDE);
                        break;
                    
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

                    //BRANCH MODES 6-9
                    // 
                    // 6: Assumes no inputs - only looks at program counter
                    //      - Do nothing and just go to the next line
                    //
                    // 7: Only looks at the value of operand 1 & PC
                    //      - IF value 1 is negative and nonzero, reset the PC to a specified value
                    //      - otherwise, skip the next line and proceed
                    //
                    // 8: Only looks at the value of operand 1 & PC
                    //      - IF value 1 is positive and nonzero, reset the PC to a specified value
                    //      - otherwise, skip the next line and proceed
                    //
                    // 8: Only looks at the value of operand 1 & PC
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

                            //Go to the next line and get that address
                            //THEN set the PC to that address
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
                System.out.printf("%n$ ERROR %d INVALID ADDRESS: One of the operands contain an invalid mode or attempts to access an invalid GPR number.%n", error);
            }

            printOperands(operands);

            this.cycles++;

            //DEBUGGING
            //this.halt = true;
            
            //String cycleNumber = String.format("Cycle number %d", cycles);
            //Main.dumpMemory(cycleNumber, 0, 100);
        }

    }

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

    public void printOperands(long[] operands) {
        System.out.printf("%nOperands array: ");
        for (int i = 0; i < 5; i++) {
            System.out.printf("%d ", operands[i]);
        }
        System.out.printf("%n");
    }
    
    public long fetchOperand(long mode, long gpr, int operandAddr, int operandValue) {
        //mode: long describing which fetch mode to use
        //gpr: long describing which GPR to access
        //operandAddr: integer describing which address variable to access, 1 or 2
        //operandValue: integer describing which value variable to access, 1 or 2

        switch((int)mode) {
            case 1: //register mode - This mode will go to the specified GPR and retrieve its value
                setOpAddress(operandAddr, -200); //Set the specified op address var to a negative num
                
                //
                setOpValue(operandValue, Main.getGPR(gpr)); //Set the specified op value var to the value that is in the specified GPR
                break;

            case 2: //register deferred mode - "Op addr in GPR and value in memory"
                
                //In this case, there is something inside the GPR specified by the var gpr
                //Retrieve that value
                //Put it inside op address 1 or 2
                //Is it possible to store a value in the GPR beyond 2999?
                setOpAddress(operandAddr, Main.getGPR(gpr));

                if (Main.isValidProgramArea(getOpAddress(operandAddr))) {
                    setOpValue(operandValue, Main.getHypoMemory(operandAddr));
                } else {
                    this.error = SystemConstants.ERROR_INVALID_ADDRESS;
                    System.out.printf("%n$ ERROR %d INVALID ADDRESS: GPR %d is invalid in case 2.%n", error, operandAddr);
                    return this.error;
                }
                break;

            case 3: //autoincrement mode (Op addr in GPR and value in memory)
                
                //In this case, there is something inside the GPR specified by the var gpr
                //Retrieve that value
                //Put it inside op address 1 or 2
                setOpAddress(operandAddr, Main.getGPR(gpr));

                if (Main.isValidProgramArea(getOpAddress(operandAddr))) {
                    setOpValue(operandValue, Main.getHypoMemory(getOpAddress(operandAddr)));

                    Main.setGPR(gpr, (Main.getGPR(gpr))+1);

                } else {
                    this.error = SystemConstants.ERROR_INVALID_ADDRESS;
                    System.out.printf("%n$ ERROR %d INVALID ADDRESS: GPR %d is invalid in case 3.%n", error, operandAddr);
                }
                break;

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

    //Accessors
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

    //Mutators
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

    //Boolean methods
    public boolean isValidGPR(long gpr) {
        return gpr >= 0 && gpr <= 8;
    }

}