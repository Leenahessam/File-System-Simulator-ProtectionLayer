package mypackage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        System.out.print("Login \nusername: ");
        Scanner scan = new Scanner(System.in);
        String name = scan.nextLine();
        System.out.print("password: ");
        String pass = scan.nextLine();
        Command command = new Command();
        boolean check = command.Login(name, pass);
        if (check == false)
            System.out.println("can't find user.");
        else {
            command.organize();
            // System.out.println("commands \n----------------------------------------\n");
            while (true) {
                System.out.println("\nEnter your command:\t(Enter 0 to Terminate)");
                String com = scan.nextLine();
                com += " ";
                if (!command.ManageCommand(com)) {
                    command.saveToVF();
                    try {
                        command.saveToUsers();
                    } catch (IOException E) {
                        E.getMessage();
                    }
                    try {
                        command.saveToCap();
                    } catch (IOException e) {
                        e.getMessage();
                    }

                    break;
                }
            }
        }
    }
}
