package correcter;

import java.io.*;
import java.util.Random;

public class EncoderDecoder {

    public static void send(String inputFileName, String outputFileName) {
        try (FileInputStream inputStream = new FileInputStream(inputFileName)) {
            try (FileOutputStream outputStream = new FileOutputStream(outputFileName)) {
                int byteFromFile = inputStream.read();
                while (byteFromFile != -1) {
                    byteFromFile ^= 1 << 2; // introduce error
                    outputStream.write(byteFromFile);
                    byteFromFile = inputStream.read();
                }
                inputStream.close();
            } catch (IOException e) {
                System.out.printf("An exception occurred: %s\n", e.getMessage());
            }
        } catch (IOException e) {
            System.out.printf("No file found.\n Make sure %s exists.\n", inputFileName);
        }
    }

    public static void encode(String inputFileName, String outputFileName) {
        try (FileInputStream inputStream = new FileInputStream(inputFileName)) {
            StringBuilder fileInBinary = new StringBuilder();
            int byteFromFile = inputStream.read();

            // read all bits into a string
            while (byteFromFile != -1) {
                String binaryString = Integer.toBinaryString(byteFromFile);
                if (binaryString.length() < 8) {
                    binaryString = "0".repeat(8 - binaryString.length()) + binaryString;
                }
                fileInBinary.append(binaryString);
                byteFromFile = inputStream.read();
            }

            // duplicate bits and add parity into new string
            char[] bitsFromFile = fileInBinary.toString().toCharArray();
            StringBuilder encodedFileInBinary = new StringBuilder();
            int index = 0;
            int bitCounter = 1;
            int parity = 0;
            while (true) {
                int nextParityPart = index < bitsFromFile.length ? bitsFromFile[index] : 0;
                if (bitCounter == 1) {
                    parity = nextParityPart;
                } else {
                    parity ^= nextParityPart;
                }

                if (index < bitsFromFile.length) {
                    encodedFileInBinary.append(bitsFromFile[index]);
                    encodedFileInBinary.append(bitsFromFile[index]);
                    index++;
                } else {
                    encodedFileInBinary.append(0);
                    encodedFileInBinary.append(0);
                }
                bitCounter++;

                if (bitCounter > 3) { // every 3 bits add parity bits
                    encodedFileInBinary.append(Math.abs(Character.getNumericValue(parity)));
                    encodedFileInBinary.append(Math.abs(Character.getNumericValue(parity)));
                    bitCounter = 1;
                }

                if (bitCounter == 1 && index >= bitsFromFile.length) {
                    break;
                }
            }
            inputStream.close();

            System.out.println(fileInBinary.toString());
            System.out.println(encodedFileInBinary.toString());

            try (FileOutputStream outputStream = new FileOutputStream(outputFileName)) {
                for (int i = 0; i < encodedFileInBinary.length(); i += 8) {
                    String binaryString = encodedFileInBinary.substring(i, i + 8);
                    outputStream.write(Integer.parseInt(binaryString, 2));
                }
            } catch (IOException e) {
                System.out.printf("An exception occurred: %s\n", e.getMessage());
            }
        } catch (IOException e) {
            System.out.printf("No file found.\n Make sure %s exists.\n", inputFileName);
        }
    }

    public static void decode(String inputFileName, String outputFileName) {
        try (FileInputStream inputStream = new FileInputStream(inputFileName)) {
            StringBuilder decodedFileInBinary = new StringBuilder();
            int byteFromFile = inputStream.read();
            while (byteFromFile != -1) {
                String binaryString = Integer.toBinaryString(byteFromFile);
                if (binaryString.length() < 8) {
                    binaryString = "0".repeat(8 - binaryString.length()) + binaryString;
                }
                System.out.print(binaryString);
                decodedFileInBinary.append(getBits(binaryString));
                byteFromFile = inputStream.read();
            }
            System.out.println();
            System.out.println(decodedFileInBinary.toString());
            inputStream.close();

            decodedFileInBinary.setLength(decodedFileInBinary.length() - decodedFileInBinary.length() % 8);

            try (FileOutputStream outputStream = new FileOutputStream(outputFileName)) {
                for (int i = 0; i < decodedFileInBinary.length(); i += 8) {
                    if (i + 8 < decodedFileInBinary.length()) {
                        String binaryString = decodedFileInBinary.substring(i, i + 8);
                        outputStream.write(Integer.parseInt(binaryString, 2));
                    } else {
                        String binaryString = decodedFileInBinary.substring(i);
                        outputStream.write(Integer.parseInt(binaryString, 2));
                    }
                }
            } catch (IOException e) {
                System.out.printf("An exception occurred: %s\n", e.getMessage());
            }
        } catch (IOException e) {
            System.out.printf("No file found.\n Make sure %s exists.\n", inputFileName);
        }
    }

    private static String getBits(String byteInBinary) {
        String parityString = byteInBinary.substring(6);
        int parityBit;
        // find parity bit, if parity bit has error, return all bits
        if (parityString.equals("11")) {
            parityBit = 1;
        } else if (parityString.equals("00")) {
            parityBit = 0;
        } else {
            return String.format("%c%c%c", byteInBinary.charAt(0), byteInBinary.charAt(2), byteInBinary.charAt(4));
        }

        StringBuilder bitString = new StringBuilder();
        int errorPosition = 0;
        for (int i = 0; i < 6; i += 2) {
            String pair = byteInBinary.substring(i, i + 2);
            if (pair.equals("11")) {
                bitString.append(1);
                parityBit ^= 1;
            } else if (pair.equals("00")) {
                bitString.append(0);
                parityBit ^= 0;
            } else {
                errorPosition = i / 2;
            }
        }
        bitString.insert(errorPosition, parityBit);
        return bitString.toString();
    }

    public static String send(String input) {
        StringBuilder errorInput = new StringBuilder();
        for (int i = 0; i < input.length(); i += 3) {
            errorInput.append(introduceError(input.substring(i, Math.min(i + 3, input.length()))));
        }
        return errorInput.toString();
    }

    private static String introduceError(String subString) {
        StringBuilder errorSubString = new StringBuilder(subString);
        Random random = new Random();
        // generate random number 0 - subString.length, this is the index to get an error
        int errorIndex = random.nextInt(subString.length());
        // generate random number 32 - 126, this is the error symbol that will replace the original symbol
        char errorSymbol;
        do {
            errorSymbol = (char) (random.nextInt(126 - 32 + 1) + 32);
        } while (errorSymbol == subString.charAt(errorIndex));
        errorSubString.setCharAt(errorIndex, errorSymbol);
        return errorSubString.toString();
    }

    public static String encode(String input) {
        StringBuilder encodedInput = new StringBuilder();

        for (char symbol : input.toCharArray()) {
            for (int i = 0; i < 3; i++) {
                encodedInput.append(symbol);
            }
        }

        return encodedInput.toString();
    }

    public static String decode(String input) {
        if (input.length() % 3 != 0) {
            return "Input not encoded.";
        }

        char[] inputArray = input.toCharArray();
        StringBuilder decodedInput = new StringBuilder();
        // every 3 symbols, get the common occurrence
        for (int i = 0; i < inputArray.length; i += 3) {
            if (inputArray[i] == inputArray[i + 1] || inputArray[i] == inputArray[i + 2]) {
                decodedInput.append(inputArray[i]);
            } else {
                decodedInput.append(inputArray[i + 1]);
            }
        }

        return decodedInput.toString();
    }
}
