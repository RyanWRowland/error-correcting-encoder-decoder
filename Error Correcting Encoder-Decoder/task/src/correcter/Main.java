package correcter;

public class Main {
    public static void main(String[] args) {
//        Scanner scanner = new Scanner(System.in);
//        String input = scanner.nextLine();
//        System.out.println(input);
//
//        String encodedInput = EncoderDecoder.encode(input);
//        System.out.println(encodedInput);
//
//        String errorInput = EncoderDecoder.send(encodedInput);
//        System.out.println(errorInput);
//
//        String decodedInput = EncoderDecoder.decode(errorInput);
//        System.out.println(decodedInput);

//        EncoderDecoder.send("send.txt", "received.txt");
//        EncoderDecoder.encode("send.txt", "encoded.txt");
//        EncoderDecoder.send("encoded.txt", "received.txt");
//        EncoderDecoder.decode("received.txt", "decoded.txt");

        HammingCode.encode("send.txt", "encoded.txt");
        HammingCode.send("encoded.txt", "received.txt");
        HammingCode.decode("received.txt", "decoded.txt");
    }
}
