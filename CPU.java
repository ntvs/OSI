public class CPU {
    
    private boolean halt;
    private long error;

    public CPU() {
        this.halt = false;
        this.error = SystemConstants.OK;
    }

    public void cycle() {

        //Cycle the CPU until state is set to halt or an error is set
        while (!halt && error >= 0) {

            updateRegisters();
            
            long[] operands = parseOperands();
            //[0] = opCode
            //[1] = op1Mode
            //[2] = op1GPR
            //[3] = op2Mode
            //[4] = op2GPR

            if (operands[1] <= 6 && operands[3] <= 6 && operands[1] >= 0 && operands[3] >= 0 && operands[2] <= 8 && operands[4] <= 8 && operands[2] >= 0 && operands[4] >= 0) {
                

                // switch ((int)operands[0]) {
                //         case 0: //halt
                //             this.halt = true;
                //             System.out.printf("%n$ %d HALT: Halt encountered.%n", error);
                //             break;

                //         case 1: //add
                //             long status = fetchOperand();
                //             break;
                        
                // }


            } else {
                this.error = SystemConstants.ERROR_INVALID_OPERANDS;
                System.out.printf("%n$ ERROR %d INVALID ADDRESS: One of the operands contain an invalid mode or attempts to access an invalid GPR number.%n", error);
            }

            printOperands(operands);
            this.halt = true;
        }

    }

    public void updateRegisters() {
        long programCounter = Main.getPC();

        if (0 <= programCounter && programCounter <= SystemConstants.VALID_PROGRAM_AREA) {

            Main.setMAR(programCounter); //set MAR to the value of the PC
            Main.incrementPC(); //PC now contains the addr of the next word while MAR has the current word

            long word = Main.getHypoMemory(Main.getMAR()); //Retrieve word from memory using address stored in MAR (but why not use PC directly?)
            Main.setMBR(word); //Load the word into the MBR

        } else {
            this.error = SystemConstants.ERROR_INVALID_ADDRESS;
            System.out.printf("%n$ ERROR %d INVALID ADDRESS: The program counter points to a negative address or an address outside the valid program area.%n", error);
        }

        Main.setIR(Main.getMBR()); //Get the MBR value and store it in the IR (Why even store it in the MBR if it can be stored directly in the IR?)
    }

    public long[] parseOperands() {
        //Separate the opcode from the word and leave the rest in remainder
        //Opcode tells CPU what mode it should be in
        long opCode = (Main.getIR()) / 10000; //extracts the first digit from the word so 15060 / 10000 = 1.506 = opCode 1
        long remainder = (Main.getIR()) % 10000; //extracts everything else except the first digit... so 5060

        //Extract information about the first operand... for example, in 5060, op1=50 op2=60
        //Each one of the operands are split into 2 parts, first digit being the "mode" for that operand 
        //Second digit of the operand is one of the GPRs
        long op1Mode = remainder / 1000;
        remainder = remainder % 1000;

        long op1GPR = remainder / 100;
        remainder = remainder % 100;

        long op2Mode = remainder / 10;
        remainder = remainder % 10;

        long op2GPR = remainder / 1;

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
    
    // public long fetchOperand(long mode, long gpr, long operandAddr, long operandValue) {

    //     switch((int)mode) {
    //         case 1: //register mode
    //             break;
    //     }

    // }

}