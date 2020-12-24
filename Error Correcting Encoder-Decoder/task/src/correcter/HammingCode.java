package correcter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class HammingCode {

    public static void send(String inputFileName, String outputFileName) {
        System.out.println();
        System.out.println("Transmitting...");
        try (FileInputStream inputStream = new FileInputStream(inputFileName)) {
            try (FileOutputStream outputStream = new FileOutputStream(outputFileName)) {
                int byteFromFile = inputStream.read();
                while (byteFromFile != -1) {
                    String binaryString = Integer.toBinaryString(byteFromFile);
                    if (binaryString.length() < 8) {
                        binaryString = "0".repeat(8 - binaryString.length()) + binaryString;
                    }
                    System.out.println(binaryString);
                    byteFromFile ^= 1 << 2; // introduce error
                    binaryString = Integer.toBinaryString(byteFromFile);
                    if (binaryString.length() < 8) {
                        binaryString = "0".repeat(8 - binaryString.length()) + binaryString;
                    }
                    System.out.println(binaryString);
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
        System.out.println();
        System.out.println("Encoding...");
        try (FileInputStream inputStream = new FileInputStream(inputFileName)) {
                StringBuilder encodedFileInBinary = new StringBuilder();
                int byteFromFile = inputStream.read();

                // read all bytes
                while (byteFromFile != -1) {
                    // convert bytes to binary strings
                    String binaryString = Integer.toBinaryString(byteFromFile);
                    if (binaryString.length() < 8) {
                        binaryString = "0".repeat(8 - binaryString.length()) + binaryString;
                    }
                    // split 8 bit strings into 4 bits, encode and save to file
                    encodedFileInBinary.append(hamming(binaryString.substring(0, 4)));
                    encodedFileInBinary.append(hamming(binaryString.substring(4)));

                    byteFromFile = inputStream.read();
                }
                inputStream.close();
            try (FileOutputStream outputStream = new FileOutputStream(outputFileName)) {
                for (int i = 0; i < encodedFileInBinary.length(); i += 8) {
                    String binaryString;
                    if (i + 8 < encodedFileInBinary.length()) {
                        binaryString = encodedFileInBinary.substring(i, i + 8);
                    } else {
                        binaryString = encodedFileInBinary.substring(i);
                    }
                    outputStream.write(Integer.parseInt(binaryString, 2));
                }
            } catch (IOException e) {
                System.out.printf("An exception occurred: %s\n", e.getMessage());
            }
        } catch (IOException e) {
            System.out.printf("No file found.\n Make sure %s exists.\n", inputFileName);
        }
    }

    // apply hamming code to 4 bits, return 8 bits
    // 4 significant, 3 parity, and a trailing 0
    private static String hamming(String bitString) {
        System.out.println(bitString);
        char[] bits = bitString.toCharArray();
        int d3 = Character.getNumericValue(bits[0]);
        int d5 = Character.getNumericValue(bits[1]);
        int d6 = Character.getNumericValue(bits[2]);
        int d7 = Character.getNumericValue(bits[3]);
        int p1 = (d3 + d5 + d7) % 2;
        int p2 = (d3 + d6 + d7) % 2;
        int p4 = (d5 + d6 + d7) % 2;

        String hammed = String.format("%d%d%d%d%d%d%d0", p1, p2, d3, p4, d5, d6, d7);
        System.out.println(hammed);
        return hammed;
    }

    public static void decode(String inputFileName, String outputFileName) {
        System.out.println();
        System.out.println("Decoding...");
        try (FileInputStream inputStream = new FileInputStream(inputFileName)) {
            StringBuilder decodedFileInBinary = new StringBuilder();
            int byteFromFile = inputStream.read();
            while (byteFromFile != -1) {
                String binaryString = Integer.toBinaryString(byteFromFile);
                if (binaryString.length() < 8) {
                    binaryString = "0".repeat(8 - binaryString.length()) + binaryString;
                }
                decodedFileInBinary.append(getBits(binaryString));
                byteFromFile = inputStream.read();
            }
            inputStream.close();
            try (FileOutputStream outputStream = new FileOutputStream(outputFileName)) {
                for (int i = 0; i < decodedFileInBinary.length(); i += 8) {
                    String binaryString;
                    if (i + 8 < decodedFileInBinary.length()) {
                        binaryString = decodedFileInBinary.substring(i, i + 8);
                    } else {
                        binaryString = decodedFileInBinary.substring(i);
                    }
                    outputStream.write(Integer.parseInt(binaryString, 2));
                }
            } catch (IOException e) {
                System.out.printf("An exception occurred: %s\n", e.getMessage());
            }
        } catch (IOException e) {
            System.out.printf("No file found.\n Make sure %s exists.\n", inputFileName);
        }
    }

    // check and correct for errors then return significant bits
    // positions -1 for array access
    private static String getBits(String bitString) {
        char[] bits = bitString.toCharArray();

        // get bits
        int p1 = Character.getNumericValue(bits[0]);
        int p2 = Character.getNumericValue(bits[1]);
        int d3 = Character.getNumericValue(bits[2]);
        int p4 = Character.getNumericValue(bits[3]);
        int d5 = Character.getNumericValue(bits[4]);
        int d6 = Character.getNumericValue(bits[5]);
        int d7 = Character.getNumericValue(bits[6]);

        // check parity bits
        int p1Check = (d3 + d5 + d7) % 2;
        int p2Check = (d3 + d6 + d7) % 2;
        int p4Check = (d5 + d6 + d7) % 2;

        int errorPosition = 0;
        if (p1 != p1Check) {
            errorPosition += 1;
        }
        if (p2 != p2Check) {
            errorPosition += 2;
        }
        if (p4 != p4Check) {
            errorPosition += 4;
        }

        // errorPosition 0 indicates no errors or error is in ignored bit
        // if errorPosition is 1, 2, or 4, bad bit is a parity bit and significant bits are good
        if (errorPosition != 0 && errorPosition != 1 && errorPosition != 2 && errorPosition != 4) {
            // flip bad bit
            bits[errorPosition - 1] = bits[errorPosition - 1] == '0' ? '1' : '0';
        }

        String significantBits = String.format("%c%c%c%c", bits[2], bits[4], bits[5], bits[6]);
        System.out.println(significantBits);
        System.out.println(bitString);
        return significantBits;
    }
}
