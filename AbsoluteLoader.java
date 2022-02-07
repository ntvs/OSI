import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class AbsoluteLoader {
    
    //SCANNERs - USER INPUT + FILE READER
    public Scanner input = new Scanner(System.in);
    public Scanner fileReader;

    private File programFile;

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

    
    public void setFile(String fileName) {
        programFile = new File(fileName);
    }


    public long load() {
        
        try {
            this.fileReader = new Scanner(programFile);
        } catch(FileNotFoundException e) {
            System.out.printf("%n$ The file \"%s\" was not found.%n", programFile.getAbsolutePath());
            return SystemConstants.ERROR;
        }

        int lineCount = 0;

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

                //If the address is greater than or equal to 0, go to that memory location and load the instruction
                //Otherwise if the address is -1 or lower, the end of the program has been reached
                if (addr >= 0) {
                    
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

            lineCount++;
        }

        fileReader.close();
        return SystemConstants.OK;
    } 
}