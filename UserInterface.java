import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class UserInterface {

    
    private static final String DESKTOP_DIRECTORY = "C:/Users/VICTUS/Desktop/";  
    private static final String DOCUMENTS_DIRECTORY = "C:/Users/VICTUS/Documents/";  

    public static String getInputCode() {
        try (Scanner scanner = new Scanner(System.in)) {

            System.out.println("Please choose your input method:");
            System.out.println("1: Enter code via text");
            System.out.println("2: Provide a file name");

            int choice = scanner.nextInt();
            scanner.nextLine();

            String code = "";
            if (choice == 1) {
                code = getTextInput(scanner);
            } else if (choice == 2) {
                code = getFileInput(scanner);
            } else {
                System.out.println("Invalid choice. Exiting.");
                System.exit(1);
            }

            return code;
        }
    }

    private static String getTextInput(Scanner scanner) {
        System.out.println("Enter your code (finish with a single line 'END'):");
        StringBuilder codeBuilder = new StringBuilder();
        String line;
        while (!(line = scanner.nextLine()).equals("END")) {
            codeBuilder.append(line).append("\n");
        }
        return codeBuilder.toString();
    }

    private static String getFileInput(Scanner scanner) {
        System.out.println("Enter the file name:");
        String fileName = scanner.nextLine();

        Path filePath = locateFile(fileName);

        if (filePath == null) {
            System.out.println("File does not exist in Desktop or Documents directories, or it cannot be read. Exiting.");
            System.exit(1);
        }

        String fileType = detectFileType(fileName);
        if (fileType.equals("Unknown")) {
            System.out.println("Unsupported file type. Please provide a C or Java file.");
            System.exit(1);
        }

        try {
            String code = new String(Files.readAllBytes(filePath));
            if (code.trim().isEmpty()) {
                System.out.println("The file is empty. Exiting.");
                System.exit(1);
            }
            return code;
        } catch (IOException e) {
            System.out.println("Error reading the file. Exiting.");
            e.printStackTrace();
            System.exit(1);
            return "";
        }
    }

    private static Path locateFile(String fileName) {
        Path desktopPath = Paths.get(DESKTOP_DIRECTORY, fileName);
        Path documentsPath = Paths.get(DOCUMENTS_DIRECTORY, fileName);

        if (Files.exists(desktopPath) && Files.isReadable(desktopPath)) {
            return desktopPath;
        } else if (Files.exists(documentsPath) && Files.isReadable(documentsPath)) {
            return documentsPath;
        } else {
            return null;
        }
    }

    public static String detectFileType(String fileName) {
        if (fileName.endsWith(".c")) {
            return "C";
        } else if (fileName.endsWith(".java")) {
            return "Java";
        } else {
            return "Unknown";
        }
    }

    public static String determineLanguage(String code) {
        if (code.contains("import") || code.contains("public class")) {
            return "Java";
        } else if (code.contains("#include") || code.contains("int main")) {
            return "C";
        } else {
            return "Unsupported";
        }
    }
}
