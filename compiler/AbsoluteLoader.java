import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class AbsoluteLoader {
    
    public final String[] keys = {"Long", "Halt", "Add", "Subtract", "Multiply", "Division", "Move", "Branch", "BrOnMinus", "BrOnPlus", "BrOnZero", "Push", "Pop"};
    public final List<String> KEYWORDS = Arrays.asList(keys);

    private ArrayList<String[]> symbolTable = new ArrayList<String[]>();
    private ArrayList<String[]> symbolOccurrances = new ArrayList<String[]>();

    //SCANNERs - USER INPUT + FILE READER
    private Scanner input = new Scanner(System.in);
    private Scanner fileReader;

    private File programFile; //contains the program file

    //Constructors
    public AbsoluteLoader(String fileName) {
        input.close();
        setFile(fileName);
    }
    public AbsoluteLoader() {
        System.out.printf("%n$ Enter program to compile >>> ");
        String fileName = input.nextLine();
        input.close();

        setFile(fileName);
    }

    //Creates file object based on a provided string
    public void setFile(String fileName) {
        programFile = new File(fileName);
    }


    public ArrayList<String[]> load() {

        long error = 0;
        ArrayList<String[]> fileLines = new ArrayList<String[]>();
        
        //Checks to see if file exits
        try {
            //If yes, proceed with scanning the file
            this.fileReader = new Scanner(programFile);

        } catch(FileNotFoundException e) {

            //If no, print the error
            System.out.printf("%n$ The file \"%s\" was not found.%n", programFile.getAbsolutePath());
            error = SystemConstants.ERROR;
        }
        
        //Proceed only if there is no error
        if (error == 0) {

            int lineCount = 1; //Keeps track of how many lines read for error purposes

            //Read the file until there are no more lines
            while(fileReader.hasNextLine()) {
                String data = fileReader.nextLine(); //Grab the entire line
                String[] contents = data.split(":"); // split by :
                
                //Trim spaces from arguments
                for (int i = 0; i < contents.length; i++) {
                    contents[i] = contents[i].trim();
                }

                fileLines.add(contents);

                lineCount++; //Increment the line count
            }
        }

        return fileLines;
    } 

    //Removes the first line "main:  Function" from the list
    public ArrayList<String[]> parseMain(ArrayList<String[]> asm) {

        int count = 0;
        ArrayList<String[]> result = new ArrayList<String[]>();

        for (String[] array: asm) {

            if (!array[0].equals("main")) {
                result.add(array);
            }

            count++;
        }

        return result;
    }

    //Returns array or every label in the ASM
    public String[] returnLabels(ArrayList<String[]> asm) {

        int count = 0;

        for (String[] array: asm) {
            if (array.length == 3) {
                count++;
            }
        }

        String[] result = new String[count];

        int i = 0;

        for (String[] array: asm) {
            if (array.length == 3) {
                result[i] = array[0];
                
                String[] entry = {array[0], Integer.toString(i)};

                if (!symbolTable.contains(entry[0])) {
                    
                    symbolTable.add(entry);
                }

                i++;
            }
        }

        return result; 

    }

    public ArrayList<String[]> parseLabels(ArrayList<String[]> asm) {

        int count = 0;
        ArrayList<String[]> result = new ArrayList<String[]>();
        

        for (String[] array: asm) {

            if (array.length == 3) {
                String[] newArray = {array[1], array[2]};
                result.add(newArray);
                count++;
            } else {
                result.add(array);
            }
        }

        return result;

    }

    public void translate(ArrayList<String[]> asm, String[] labels) {
        int address =  0;
        int count = 0;
        for (String[] array: asm) {

            String command = "";
            String[] arguments = {"", ""};
            String[] arguments2 = {"", ""};
            String[] entry = {"", ""};

            if (array.length > 1) {
                arguments = array[1].split(",");
            }

            switch(array[0]) {
                case "Origin":
                    address = Integer.parseInt(array[1]);
                    break;

                case "Long":
                    System.out.printf("%d %s%n", address, array[1]);

                    address++;
                    break;

                case "Add":

                    command = "1";

                    for (int i = 0; i < 2; i++) {
                        if (Arrays.asList(labels).contains(arguments[i])) {
                            address++;

                            command += "50";
                            arguments2[i] = String.format("%d %s", address, arguments[i]);
                        } else if (isInteger(arguments[i])) {
                            address++;
                            command += "60";
                            arguments2[i] = String.format("%d %s", address, arguments[i]);
                        } else {
                            String[] whichGPR = arguments[i].split("");
                            command += String.format("1%s", whichGPR[3]);
                        }
                    }

                    if (arguments2[0].length() == 0 && arguments2[1].length() == 0) {
                        System.out.printf("%d %s%n", address, command);
                    } else if (arguments2[0].length() != 0 && arguments2[1].length() == 0) {
                        System.out.printf("%d %s%n", address-1, command);
                        System.out.printf("%d %s%n", address, arguments[0]);
                    } else if (arguments2[0].length() == 0 && arguments2[1].length() != 0) {
                        System.out.printf("%d %s%n", address-1, command);
                        System.out.printf("%d %s%n", address, arguments[1]);
                    } else {
                        System.out.printf("%d %s%n", address-2, command);
                        System.out.printf("%d %s%n", address-1, arguments[0]);
                        System.out.printf("%d %s%n", address, arguments[1]);
                    }

                    address++;

                    break;

                case "Subtract":

                    command = "2";

                    for (int i = 0; i < 2; i++) {
                        if (Arrays.asList(labels).contains(arguments[i])) {
                            address++;

                            command += "50";
                            arguments2[i] = String.format("%d %s", address, arguments[i]);
                        } else if (isInteger(arguments[i])) {
                            address++;
                            command += "60";
                            arguments2[i] = String.format("%d %s", address, arguments[i]);
                        } else {
                            String[] whichGPR = arguments[i].split("");
                            command += String.format("1%s", whichGPR[3]);
                        }
                    }

                    if (arguments2[0].length() == 0 && arguments2[1].length() == 0) {
                        System.out.printf("%d %s%n", address, command);
                    } else if (arguments2[0].length() != 0 && arguments2[1].length() == 0) {
                        System.out.printf("%d %s%n", address-1, command);
                        System.out.printf("%d %s%n", address, arguments[0]);
                    } else if (arguments2[0].length() == 0 && arguments2[1].length() != 0) {
                        System.out.printf("%d %s%n", address-1, command);
                        System.out.printf("%d %s%n", address, arguments[1]);
                    } else {
                        System.out.printf("%d %s%n", address-2, command);
                        System.out.printf("%d %s%n", address-1, arguments[0]);
                        System.out.printf("%d %s%n", address, arguments[1]);
                    }

                    address++;

                    break;

                case "Multiply":

                    command = "3";

                    for (int i = 0; i < 2; i++) {
                        if (Arrays.asList(labels).contains(arguments[i])) {
                            address++;

                            command += "50";
                            arguments2[i] = String.format("%d %s", address, arguments[i]);
                        } else if (isInteger(arguments[i])) {
                            address++;
                            command += "60";
                            arguments2[i] = String.format("%d %s", address, arguments[i]);
                        } else {
                            String[] whichGPR = arguments[i].split("");
                            command += String.format("1%s", whichGPR[3]);
                        }
                    }

                    if (arguments2[0].length() == 0 && arguments2[1].length() == 0) {
                        System.out.printf("%d %s%n", address, command);
                    } else if (arguments2[0].length() != 0 && arguments2[1].length() == 0) {
                        System.out.printf("%d %s%n", address-1, command);
                        System.out.printf("%d %s%n", address, arguments[0]);
                    } else if (arguments2[0].length() == 0 && arguments2[1].length() != 0) {
                        System.out.printf("%d %s%n", address-1, command);
                        System.out.printf("%d %s%n", address, arguments[1]);
                    } else {
                        System.out.printf("%d %s%n", address-2, command);
                        System.out.printf("%d %s%n", address-1, arguments[0]);
                        System.out.printf("%d %s%n", address, arguments[1]);
                    }

                    address++;

                    break;

                case "Divide":

                    command = "4";

                    for (int i = 0; i < 2; i++) {
                        if (Arrays.asList(labels).contains(arguments[i])) {
                            address++;

                            command += "50";
                            arguments2[i] = String.format("%d %s", address, arguments[i]);
                        } else if (isInteger(arguments[i])) {
                            address++;
                            command += "60";
                            arguments2[i] = String.format("%d %s", address, arguments[i]);
                        } else {
                            String[] whichGPR = arguments[i].split("");
                            command += String.format("1%s", whichGPR[3]);
                        }
                    }

                    if (arguments2[0].length() == 0 && arguments2[1].length() == 0) {
                        System.out.printf("%d %s%n", address, command);
                    } else if (arguments2[0].length() != 0 && arguments2[1].length() == 0) {
                        System.out.printf("%d %s%n", address-1, command);
                        System.out.printf("%d %s%n", address, arguments[0]);
                    } else if (arguments2[0].length() == 0 && arguments2[1].length() != 0) {
                        System.out.printf("%d %s%n", address-1, command);
                        System.out.printf("%d %s%n", address, arguments[1]);
                    } else {
                        System.out.printf("%d %s%n", address-2, command);
                        System.out.printf("%d %s%n", address-1, arguments[0]);
                        System.out.printf("%d %s%n", address, arguments[1]);
                    }

                    address++;

                    break;

                case "Move":

                    command = "5";

                    for (int i = 0; i < 2; i++) {
                        if (Arrays.asList(labels).contains(arguments[i])) {
                            address++;

                            command += "50";
                            arguments2[i] = String.format("%d %s", address, arguments[i]);
                        } else if (isInteger(arguments[i])) {
                            address++;
                            command += "60";
                            arguments2[i] = String.format("%d %s", address, arguments[i]);
                        } else {
                            String[] whichGPR = arguments[i].split("");
                            command += String.format("1%s", whichGPR[3]);
                        }
                    }

                    if (arguments2[0].length() == 0 && arguments2[1].length() == 0) {
                        System.out.printf("%d %s%n", address, command);
                    } else if (arguments2[0].length() != 0 && arguments2[1].length() == 0) {
                        System.out.printf("%d %s%n", address-1, command);
                        System.out.printf("%d %s%n", address, arguments[0]);
                    } else if (arguments2[0].length() == 0 && arguments2[1].length() != 0) {
                        System.out.printf("%d %s%n", address-1, command);
                        System.out.printf("%d %s%n", address, arguments[1]);
                    } else {
                        System.out.printf("%d %s%n", address-2, command);
                        System.out.printf("%d %s%n", address-1, arguments[0]);
                        System.out.printf("%d %s%n", address, arguments[1]);
                    }

                    address++;

                    break;

                case "Branch":

                    command = "60000";

                    address++;
                    arguments2[0] = String.format("%d %s", address, arguments[0]);

                    System.out.printf("%d %s%n", address-1, command);
                    System.out.printf("%d %s%n", address, arguments[0]);

                    address++;

                    break;

                case "BrOnMinus":

                    command = "7";

                    
                    if (Arrays.asList(labels).contains(arguments[0])) {
                        address++;
                        
                        command += "5000";
                        arguments2[0] = String.format("%d %s", address, arguments[0]);
                        address++;
                    } else if (isInteger(arguments[0])) {
                        address++;
                        command += "6000";
                        arguments2[0] = String.format("%d %s", address, arguments[0]);
                        address++;
                    } else {
                        String[] whichGPR = arguments[0].split("");
                        command += String.format("1%s00", whichGPR[3]);
                        address++;
                    }
                    

                    if (arguments2[0].length() == 0) {
                        System.out.printf("%d %s%n", address-1, command);
                        System.out.printf("%d %s%n", address, arguments[0]);
                    } else {
                        System.out.printf("%d %s%n", address-2, command);
                        System.out.printf("%d %s%n", address-1, arguments[0]);
                        System.out.printf("%d %s%n", address, arguments[1]);
                    }

                    address++;

                    break;

                case "BrOnPlus":

                    command = "8";

                    
                    if (Arrays.asList(labels).contains(arguments[0])) {
                        address++;

                        command += "5000";
                        arguments2[0] = String.format("%d %s", address, arguments[0]);
                        address++;
                    } else if (isInteger(arguments[0])) {
                        address++;
                        command += "6000";
                        arguments2[0] = String.format("%d %s", address, arguments[0]);
                        address++;
                    } else {
                        String[] whichGPR = arguments[0].split("");
                        command += String.format("1%s00", whichGPR[3]);
                        address++;
                    }
                    

                    if (arguments2[0].length() == 0) {
                        System.out.printf("%d %s%n", address-1, command);
                        System.out.printf("%d %s%n", address, arguments[0]);
                    } else {
                        System.out.printf("%d %s%n", address-2, command);
                        System.out.printf("%d %s%n", address-1, arguments[0]);
                        System.out.printf("%d %s%n", address, arguments[1]);
                    }

                    address++;

                    break;

                case "Halt":

                    command = "0";
                    System.out.printf("%d %s%n", address, command);

                    address++;
                    break;

                case "End":
                    address = -1;
                    System.out.printf("%d %s%n", address, arguments[0]);
                    break;
            }

            count++;
        }
    }

    public ArrayList<String[]> getSymbolTable() {
        return this.symbolTable;
    }
    public ArrayList<String[]> getSymbolOccurrances() {
        return this.symbolOccurrances;
    }

    public boolean isInteger(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}