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


    public int load() {
        
        try {
            this.fileReader = new Scanner(programFile);
        } catch(FileNotFoundException e) {
            System.out.printf("%n$ The file \"%s\" was not found.%n", programFile.getAbsolutePath());
            return SystemConstants.ERROR;
        }

        int lineCount = 0;

        while(fileReader.hasNextLine()) {
            String data = fileReader.nextLine();
            String[] contents = data.split(" ");
            
            //Check if line is in the correct format
            if (contents.length == 2) {
                long addr = Long.parseLong(contents[0]);
                long instruction = Long.parseLong(contents[1]);

                if (addr >= 0) {

                    if(instruction > 999999) {
                        System.out.printf("%n$ The instruction at line %d is longer than 6 digits and was not loaded.%n", lineCount);
                        return SystemConstants.ERROR;
                    }

                    Main.setHypoMemory(addr, instruction);
                } else if(addr < 0) {
                    System.out.printf("%n$ Negative memory addresses are not allowed and the instruction at line %d was not loaded.%n", lineCount);
                    return SystemConstants.ERROR;
                }

            } else {
                System.out.printf("%n$ Line %d is not formatted correctly.%n", lineCount);
            }

            lineCount++;
        }

        fileReader.close();
        return SystemConstants.OK;
    } 
}