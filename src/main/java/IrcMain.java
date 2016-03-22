import sun.misc.Regexp;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IrcMain {

    private static String nick;
    private static String username;
    private static String realName;
    private static PrintWriter out;
    private static Scanner in;

    public static void main(String[] args) throws IOException {
        Scanner console = new Scanner(System.in);

        System.out.print("Enter a nickname: ");
        nick = console.nextLine();

        System.out.print("Enter a username: ");
        username = console.nextLine();

        System.out.print("Enter your full name: ");
        realName = console.nextLine();

        Socket socket = new Socket("chat.freenode.net", 6667);

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new Scanner(socket.getInputStream());

        write("NICK", nick);
        write("USER", username + " 0 * :" + realName);

        while (in.hasNext()) {
            String serverMessage = in.nextLine();
            System.out.println("<<< " + serverMessage);
            Pattern randomNumberRegex = Pattern.compile(".* PRIVMSG ([^\\s]*) .*random (\\d+)$");
            Matcher m = randomNumberRegex.matcher(serverMessage);

            if (serverMessage.startsWith("PING")) {
                String pingContents = serverMessage.split(" ", 2)[1];
                write("PONG", pingContents);
            } else if (serverMessage.contains(" 376 ")) {
                write("JOIN", "#reddit-dailyprogrammer,#botters-test");
            } else if (serverMessage.contains(" 366 ") && serverMessage.contains("#botters-test")) {
                write("PRIVMSG", "#botters-test :Test message");
            } else if (m.matches()) {
                String channel = m.group(1);
                try {
                    int bound = Integer.parseInt(m.group(2));
                    int randomNumber = new Random().nextInt(bound);
                    write("PRIVMSG", channel + " :Your random number is " + randomNumber);
                } catch (NumberFormatException e) {
                    write("PRIVMSG", channel + " :Hmm, that didn't look like a number to me.");
                }
            }
        }

        in.close();
        out.close();
        socket.close();

        System.out.println("Done!");
    }

    private static void write(String command, String message) {
        String fullMessage = command + " " + message;
        System.out.println(">>> " + fullMessage);
        out.print(fullMessage + "\r\n");
        out.flush();
    }
}
