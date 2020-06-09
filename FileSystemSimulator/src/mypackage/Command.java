package mypackage;

import java.io.*;
import java.util.*;

import static java.lang.Integer.parseInt;

public class Command {
    public static Vector<String> Blocks = new Vector<>();
    public static Vector<String> Directories = new Vector<>();
    public static Vector<ContiguousAlloc> conAlloc = new Vector<>();
    public static Vector<IndexedAlloc> IndAlloc = new Vector<>();
    public static Vector<User> Users = new Vector<>();
    public static Vector<Capabilities> capability = new Vector<>();
    public static int num;
    public static String fileName = "DiskStructure.vfs";
    public String current = "";


    public Command() throws FileNotFoundException {
        File f = new File("users.txt");
        Scanner myReader = new Scanner(f);
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            String[] user = data.split(" ", data.length() - 1);
            User myUser = new User();
            myUser.name = user[0];
            myUser.password = user[1];
            Users.add(myUser);
        }

        File f2 = new File("capabilities.txt");
        Scanner read = new Scanner(f2);
        while (read.hasNextLine()) {
            String data = read.nextLine();
            data += ",";
            String[] cap = data.split(",", data.length() - 1);
            Capabilities temp = new Capabilities();
            temp.dir = cap[0];
            for (int i = 1; i < cap.length - 1; i++) {
                temp.Username.add(cap[i]);
                i++;
                temp.capabilities.add(cap[i]);
            }
            capability.add(temp);
        }
    }

    public boolean Login(String name, String password) {
        for (int i = 0; i < Users.size(); i++) {
            if (Users.get(i).name.equals(name) && Users.get(i).password.equals(password)) {
                current = Users.get(i).name;
                return true;
            }
        }
        return false;
    }

    public void organize() {
        Scanner scan = new Scanner(System.in);

        System.out.println("Which allocation method will you use ?\n1- Contiguous allocation  2- Indexed allocation");
        num = scan.nextInt();
        try {
            File f = new File(fileName);
            Scanner myReader = new Scanner(f);
            int flag = 0;
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if (data.equals("*")) {
                    flag++;
                    continue;
                }
                //adding blocks
                if (flag == 0) {
                    for (int i = 0; i < data.length(); i++)
                        Blocks.add(data.charAt(i) + "");
                }
                //adding the Files allocating blocks
                int x = 0;
                int len = 0;
                if (flag == 1) {
                    Vector<String> FileAlloc = new Vector<>();
                    for (int i = 0; i < data.length(); i++) {
                        if (data.charAt(i) == ' ' || data.charAt(i) == '/') {
                            String temp = data.substring(x, x + len);
                            x += len + 1;
                            len = 0;
                            FileAlloc.add(temp);
                            temp = "";
                            continue;
                        }
                        len++;
                    }

                    //contiguous allocation
                    if (num == 1) {
                        ContiguousAlloc temp = new ContiguousAlloc();
                        temp.path = "";
                        for (int i = 0; i < data.length(); i++) {
                            if (data.charAt(i) == ' ')
                                break;
                            temp.path += data.charAt(i) + "";
                        }
                        for (int j = 0; j < FileAlloc.size() - 1; j++) {
                            if (FileAlloc.get(j).contains(".")) {
                                temp.dir.add(FileAlloc.get(j));
                                temp.start = parseInt(FileAlloc.get(j + 1));
                                temp.length = parseInt(FileAlloc.get(j + 2));
                                conAlloc.add(temp);
                                break;
                            }
                            temp.dir.add(FileAlloc.get(j));
                        }
                    } else if (num == 2) {
                        //String Data = data.replaceAll("\\s", "");
                        IndexedAlloc temp = new IndexedAlloc();
                        temp.path = "";
                        for (int j = 0; j < FileAlloc.size() - 1; j++) {
                            if (FileAlloc.get(j).contains(".")) {
                                temp.dir.add(FileAlloc.get(j));
                                temp.index = parseInt(FileAlloc.get(j + 1));
                                j += 2;
                                while (!FileAlloc.get(j).equals(",")) {
                                    temp.blocks.add(parseInt(FileAlloc.get(j)));
                                    j++;
                                }
                                break;
                            }
                            temp.dir.add(FileAlloc.get(j));
                        }
                        for (int i = 0; i < data.length(); i++) {
                            if (data.charAt(i) == ' ')
                                break;
                            temp.path += data.charAt(i) + "";
                        }
                        IndAlloc.add(temp);
                    }
                }
                if (flag == 2)
                    Directories.add(data);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public int bestFit(int len) {
        int min = Integer.MAX_VALUE;
        int counter = 0;
        int start;
        int index = -1;
        for (int i = 0; i < Blocks.size(); i++) {
            if (Blocks.get(i).equals("0")) {
                start = i;
                i++;
                counter = 1;
                while (!Blocks.get(i).equals("1")) {
                    counter++;
                    if (i == Blocks.size() - 1)
                        break;
                    i++;
                }
                if (counter < len)
                    continue;
                else if (counter == len) {
                    index = start;
                    min = counter;
                } else if (counter < min) {
                    min = counter;
                    index = start;
                }
            }
        }
        return index;
    }

    public void CreateFile(String[] command, String com) {
        command[1] += "/";
        String dir[] = command[1].split("/", com.length() - 1);
        String direct = "";
        int len = parseInt(command[command.length - 2]);
        for (int i = 0; i < dir.length - 2; i++) {
            direct += dir[i];
            if (i != dir.length - 3)
                direct += "/";
        }
        if (Directories.contains(direct) && !Directories.contains(command[1].substring(0, command[1].length() - 1))) {
            //contiguous allocation
            if (num == 1) {
                int start = bestFit(len);
                if (start == -1)
                    System.out.println("No enough space");
                else {
                    for (int i = start; i < start + len; i++)
                        Blocks.set(i, "1");
                    ContiguousAlloc temp = new ContiguousAlloc();
                    temp.path = command[1].substring(0, command[1].length() - 1);
                    for (int i = 0; i < dir.length; i++)
                        temp.dir.add(dir[i]);
                    temp.start = start;
                    temp.length = len;
                    conAlloc.add(temp);
                    Directories.add(temp.path);
                }
            }
            //indexed allocation
            else if (num == 2) {
                int added = 0;
                Vector<Integer> index = new Vector();
                for (int i = 0; i < Blocks.size(); i++) {
                    if (added == len + 1)
                        break;
                    if (Blocks.get(i).equals("0"))   //index
                    {
                        index.add(i);
                        added++;
                    }
                }
                if (added == len + 1) {
                    IndexedAlloc temp = new IndexedAlloc();
                    temp.index = index.get(0);
                    temp.path = command[1].substring(0, command[1].length() - 1);
                    for (int i = 0; i < dir.length; i++)
                        temp.dir.add(dir[i]);
                    for (int i = 1; i < index.size(); i++)
                        temp.blocks.add(index.get(i));
                    IndAlloc.add(temp);
                    Directories.add(temp.path);
                    for (int i = 0; i < index.size(); i++)
                        Blocks.set(index.get(i), "1");

                } else
                    System.out.println("No enough space");
            }
        } else
            System.out.println("can't create such file ");

    }

    public void CreateFolder(String[] command, String com) {
        command[1] += "/";
        String dir[] = command[1].split("/", com.length() - 1);
        String direct = "";
        for (int i = 0; i < dir.length - 2; i++) {
            direct += dir[i];
            if (i != dir.length - 3)
                direct += "/";
        }
        if (Directories.contains(direct) && !Directories.contains(command[1].substring(0, command[1].length() - 1)))
            Directories.add(command[1].substring(0, command[1].length() - 1));
        else
            System.out.println("Can't create folder with such name ");
    }

    public void DeleteFile(String path) {
        Boolean check = false;
        for (int i = 0; i < Directories.size(); i++) {
            if (Directories.get(i).equals(path)) {
                check = true;
                Directories.remove(i);
                if (num == 1) {
                    int start = -1;
                    int length = 0;
                    for (int j = 0; j < conAlloc.size(); j++) {
                        if (conAlloc.get(j).path.equals(path)) {
                            start = conAlloc.get(j).start;
                            length = conAlloc.get(j).length;
                            conAlloc.remove(j);
                            break;
                        }
                    }
                    for (int j = start; j < start + length; j++)
                        Blocks.set(j, "0");

                } else if (num == 2) {
                    for (int j = 0; j < IndAlloc.size(); j++) {
                        System.out.println(IndAlloc.get(j).path);
                        if (IndAlloc.get(j).path.equals(path)) {
                            Blocks.set(IndAlloc.get(j).index, "0");
                            for (int k = 0; k < IndAlloc.get(j).blocks.size(); k++)
                                Blocks.set(IndAlloc.get(j).blocks.get(k), "0");
                            IndAlloc.remove(j);
                            break;
                        }
                    }
                }
                break;
            }
        }
        if (check == false)
            System.out.println("File not found ");
    }

    public void DeleteFolder(String[] command) {
        boolean check = false;
        for (int i = 0; i < Directories.size(); i++) {
            if (Directories.get(i).contains(command[1] + "/") || Directories.get(i).equals(command[1])) {
                check = true;
                if (Directories.get(i).contains("."))
                    DeleteFile(Directories.get(i));
                else
                    Directories.remove(i);
                i--;
            }
        }
        if (check == false)
            System.out.println("folder not found in this directory");
    }

    public void DisplayDiskStatus() {
        System.out.println("Disk status ");
        System.out.println("-----------------------------------------------------------");
        int counter0 = 0;
        int counter1 = 0;
        for (int i = 0; i < Blocks.size(); i++) {
            if (Blocks.get(i).equals("0"))
                counter0++;
            else if (Blocks.get(i).equals("1"))
                counter1++;
        }
        System.out.println("Free space : " + counter0 + " KB");
        System.out.println("Allocated space : " + counter1 + " KB");
        System.out.println("Empty blocks on the disk : ");
        if (num == 1) {
            for (int j = 0; j < conAlloc.size(); j++) {
                int end = conAlloc.get(j).start + conAlloc.get(j).length;
                System.out.println("From : " + conAlloc.get(j).start + " To : " + end);
            }
        } else if (num == 2) {
            for (int i = 0; i < IndAlloc.size(); i++) {
                System.out.println("Block : " + IndAlloc.get(i).index);
                for (int j = 0; j < IndAlloc.get(i).blocks.size(); j++)
                    System.out.println("Block : " + IndAlloc.get(i).blocks.get(j));
            }
        }
    }

    public void DisplayDiskStructure() {
        System.out.println("Disk Structure ");
        System.out.println("-----------------------------------------------------------");
        for (int i = 0; i < Directories.size(); i++)
            System.out.println(Directories.get(i));
    }

    public void TellUser() {
        System.out.println("User " + current);
    }

    public void CreateUser(String name, String pass) {
        if (current.equals("admin")) {
            boolean check = true;
            for (int i = 0; i < Users.size(); i++) {
                if ((Users.get(i).name.equals(name))) {
                    check = false;
                    break;
                }
            }
            if (check) {
                User New = new User();
                New.name = name;
                New.password = pass;
                Users.add(New);
                System.out.println("User is added successfully ");
            } else
                System.out.println("User already exists ");
        } else
            System.out.println("Sorry only admins can create users ");
    }

    public void Grant(String name, String dir, String cap) {
        if (current.equals("admin")) {
            boolean check = false;
            if (Directories.contains(dir) && !dir.contains(".txt")) {
                for (User user : Users) {
                    if (user.name.equals(name)) {
                        check = true;
                        break;
                    }
                }
                if (check) {
                    boolean check2 = false;
                    for (Capabilities capabilities : capability) {
                        if (capabilities.dir.equals(dir)) {
                            capabilities.Username.add(name);
                            capabilities.capabilities.add(cap);
                            check2 = true;
                            break;
                        }
                    }
                    if (!check2) {
                        Capabilities temp = new Capabilities();
                        temp.dir = dir;
                        temp.Username.add(name);
                        temp.capabilities.add(cap);
                        capability.add(temp);
                        System.out.println(name + " has granted permission successfully!");
                    }
                } else {
                    System.out.println("can't find user ");
                }
            } else
                System.out.println("No such directory ");
        } else
            System.out.println("only available for admin ");

    }

    public void DeleteUser(String User) {
        boolean found = false;
        if (current.equals("admin")) {
            for (int i = 0; i < Users.size(); i++) {
                if (Users.get(i).name.equals(User)) {
                    Users.remove(i);
                    found = true;
                    break;
                }
            }
            if (found == false)
                System.out.println("There is no such user with this name");
        } else
            System.out.println("Only admin can access this command");
    }

    public boolean ManageCommand(String com) {
        String[] command = com.split(" ", com.length() - 1);
        boolean avail = false;
        String capable = "";
        String dir = "";
        if (command[0].equals("CreateFolder") || command[0].equals("DeleteFolder")) {
            for (int i = 0; i < capability.size(); i++) {
                for (int j = 0; j < capability.get(i).Username.size(); j++) {
                    if (capability.get(i).Username.get(j).equals(current)) {
                        capable = capability.get(i).capabilities.get(j);
                        dir = capability.get(i).dir;
                        avail = true;
                        break;
                    }
                }
                if (avail)
                    break;
            }

        }
        //CreateFile
        if (command[0].equals("CreateFile"))
            CreateFile(command, com);
            //CreateFolder
        else if (command[0].equals("CreateFolder")) {
            if (avail && capable.charAt(0) == '1' || current.equals("admin")) {
                if (command[1].contains(dir))
                    CreateFolder(command, com);
                else
                    System.out.println("This directory isn't available for this user");
            } else
                System.out.println("Sorry user doesn't have that authority");
        }
        //DeleteFile
        else if (command[0].equals("DeleteFile"))
            DeleteFile(command[1]);
            //DeleteFolder
        else if (command[0].equals("DeleteFolder")) {
            if (avail && capable.charAt(1) == '1' || current.equals("admin")) {
                if (command[1].contains(dir))
                    DeleteFolder(command);
                else
                    System.out.println("This directory isn't available for this user");
            }
            else
                System.out.println("Sorry user doesn't have that authority");
        }
        //DisplayDiskStatus
        else if (command[0].equals("DisplayDiskStatus"))
            DisplayDiskStatus();
            //DisplayDiskStructure
        else if (command[0].equals("DisplayDiskStructure"))
            DisplayDiskStructure();
            //Login
        else if (command[0].equals("Login")) {
            boolean check = Login(command[1], command[2]);
            if (!check)
                System.out.println("Can't find User ");
        }
            //Telluser
        else if (command[0].equals("TellUser"))
            TellUser();
            //CreateUser
        else if (command[0].equals("CreateUser"))
            CreateUser(command[1], command[2]);
            //Grant
        else if (command[0].equals("Grant"))
            Grant(command[1], command[2], command[3]);
            //DeleteUser
        else if (command[0].equals("DeleteUser"))
            DeleteUser(command[1]);
        else
            return false;

        // saveToVF();
        return true;
    }



    public void saveToVF() {
        try {
            BufferedWriter writer = null;
            writer = new BufferedWriter(new FileWriter(fileName));
            writer.write(Blocks.get(0));
            for (int i = 1; i < Blocks.size(); i++)
                writer.append(Blocks.get(i));
            writer.append("\n*\n");
            if (num == 1) {
                for (ContiguousAlloc contiguousAlloc : conAlloc) {
                    writer.append(contiguousAlloc.path).append(" ");
                    writer.append(String.valueOf(contiguousAlloc.start)).append(" ");
                    writer.append(String.valueOf(contiguousAlloc.length)).append(" , \n");
                }
            } else if (num == 2) {
                for (IndexedAlloc indexedAlloc : IndAlloc) {
                    writer.append(indexedAlloc.path).append(" ").append(String.valueOf(indexedAlloc.index)).append(" ");
                    for (int j = 0; j < indexedAlloc.blocks.size(); j++) {
                        writer.append(String.valueOf(indexedAlloc.blocks.get(j))).append(" ");
                    }
                    writer.append(", \n");
                }
            }

            writer.append('*');
            for (String directory : Directories) {
                writer.append('\n');
                writer.append(directory);
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveToUsers() throws IOException {
        try {
            BufferedWriter writer = null;
            writer = new BufferedWriter(new FileWriter("users.txt"));
            writer.write(Users.get(0).name);
            writer.append(" ").append(Users.get(0).password).append(" ");
            for (int i = 1; i < Users.size(); i++) {
                User user = Users.get(i);
                writer.append("\n");
                writer.append(user.name).append(" ").append(user.password).append(" ");
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveToCap() throws IOException {
        try {
            BufferedWriter writer = null;
            writer = new BufferedWriter(new FileWriter("capabilities.txt"));
            for (int i1 = 0; i1 < capability.size(); i1++) {
                Capabilities cap = capability.get(i1);
                if (i1 == 0) {
                    writer.write(cap.dir);
                    writer.append(",");
                } else
                    writer.append(cap.dir).append(",");

                for (int i = 0; i < cap.Username.size(); i++) {
                    if (i == cap.Username.size() - 1) {
                        writer.append(cap.Username.get(i)).append(",").append(cap.capabilities.get(i));
                        writer.append("\n");
                        break;
                    }
                    writer.append(cap.Username.get(i)).append(",").append(cap.capabilities.get(i)).append(",");
                }
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



