package spoon.examples;

public class MainProgram {
    public static void main(String[] args) throws Exception {
        spoon.Launcher.main(new String[] {
                "-p", "spoon.examples.ParcelableTransPhaseOneProcessor","-i","/home/ericliu/Downloads/ClipboardService.java"
        });
    }
}
