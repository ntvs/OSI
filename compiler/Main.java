import java.util.*;

public class Main {
    public static void main(String[] args) {

        AbsoluteLoader loader = new AbsoluteLoader();
        ArrayList<String[]> asm = loader.load();

        asm = loader.parseMain(asm);
        String[] labels = loader.returnLabels(asm);
        asm = loader.parseLabels(asm);

        System.out.printf("%nLabels: %s%n", Arrays.toString(labels));

        System.out.printf("%n");

        if (asm.size() != 0) {
            for (String[] array: asm) {
                System.out.printf("%s%n", Arrays.toString(array));
            }

            System.out.printf("%n");
        }

        loader.translate(asm, labels);

        System.out.printf("%n");
        
        // for (String[] array: loader.getSymbolOccurrances()) {
        //     System.out.printf("%s%n", Arrays.toString(array));
        // }

    }
}