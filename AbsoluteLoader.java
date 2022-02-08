import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class AbsoluteLoader {
    
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

    //Loads a program file into the valid program area in main memory
    public long load() {
        
        //Checks to see if file exits
        try {
            //If yes, proceed with scanning the file
            this.fileReader = new Scanner(programFile);
        } catch(FileNotFoundException e) {

            //If no, print the error
            System.out.printf("%n$ The file \"%s\" was not found.%n", programFile.getAbsolutePath());
            return SystemConstants.ERROR;
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
                        return SystemConstants.ERROR;
                    }

                    //Set the value of the specified memory address to the instruciton or number provided
                    Main.setHypoMemory(addr, instruction);

                } else if(addr < 0) {
                    System.out.printf("%n$ End of program reached and program loaded successfully.%n");
                    return instruction;
                }

            } else {
                System.out.printf("%n$ Line %d is not formatted correctly and the program was not loaded.%n", lineCount);
                return SystemConstants.ERROR;
            }

            lineCount++; //Increment the line count
        }

        //Close file reader and return OK if there is no error
        fileReader.close();
        return SystemConstants.OK;
    } 
}