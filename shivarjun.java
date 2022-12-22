/*
    Name:Shivarjun Umesha 
    ID No:1002059222
    Assignment3 FileSystem 
*/
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.nio.file.Path;
import java.nio.file.Paths;

public class  shivarjun{
    // saves the meta data of open disk and holds data for new disk to be created.
    public static class FileSystem{
        public int metaBlocks = 1;
        public int numFNT;
        public int numSavedBlocks;
        public int totNumBlocks;
        public int freeBlocks;
        public String name;
        public String date;
        public String time;
    }
    //openedFS:  
    static File openedFS = null;
    static RandomAccessFile raf = null;

    //fs will save the data for the disk to be created and metaData will save the metaData of the opened file
    static FileSystem fs = new FileSystem();
    static FileSystem metaData = new FileSystem();
    static byte [] nullB = new byte[128];
    static boolean ifCreated = false;
    static boolean ifFormated = false;
    static boolean isOpen = false;

    //format the newly created FS to specify its FNT size
    public static void formatFS(){
        int fnt = (int)(fs.totNumBlocks * 0.22); //FNT
        fs.numFNT = fnt;
        fs.numSavedBlocks = fs.totNumBlocks - (fs.metaBlocks + fs.numFNT); // total - (metaBlocks + numberFNTs)
        fs.freeBlocks = fs.numSavedBlocks;
        System.out.println("File System has been formatted.");
    }
    //save the newly created FS to the hard disk
    public static void saveFS(String name){
        fs.name = name; // file systems name
        DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss"); // create date
        String [] t = df.format(new Date()).split(" ");
        fs.date = t[0];
        fs.time = t[1];
        try{
            File f = new File(name);
            RandomAccessFile ram = new RandomAccessFile(f,"rw");
            ram.seek(0);
            ram.writeInt(fs.totNumBlocks);
            ram.writeInt(fs.metaBlocks);
            ram.writeInt(fs.numFNT);
            ram.writeInt(fs.numSavedBlocks);
            ram.writeInt(fs.freeBlocks);
            String utfName = new String(fs.name.getBytes("UTF-8"));
            ram.writeUTF(utfName);
            String utfDate = new String(fs.date.getBytes("UTF-8"));
            ram.writeUTF(utfDate);
            String utfTime = new String(fs.time.getBytes("UTF-8"));
            ram.writeUTF(utfTime);
            int j = fs.metaBlocks * 128;
            for(int i=0 ; i<fs.numFNT ; i++){
                ram.seek(j);
                ram.writeUTF(new String("".getBytes("UTF-8")));
                j += 64;
                ram.seek(j);
                ram.writeUTF(new String("".getBytes("UTF-8")));
                j += 64;
            }
            for(int i=0 ; i<fs.numSavedBlocks ; i++){
                ram.write(nullB);
            }
            ram.close();
            System.out.println(name+ " has been saved successfully into the File System");
        }
        catch(Exception e){
            System.out.println("Error: File System could not be saved");
        }
    }
    public static boolean loadMeta(){
        try{
            raf.seek(0);
            metaData.totNumBlocks = raf.readInt();
            metaData.metaBlocks = raf.readInt();
            metaData.numFNT = raf.readInt();
            metaData.numSavedBlocks = raf.readInt();
            metaData.freeBlocks = raf.readInt();
            metaData.name = raf.readUTF();
            metaData.date = raf.readUTF();
            //metaData.rename = raf.readUTF();
            metaData.time = raf.readUTF();
        }
        catch(Exception e){
            System.out.println("Error: could not load meta data");
            return false;
        }
        return true;
    }
    public static boolean isBlockAvailable(byte [] b){
        for(int i=0 ; i<b.length ; i++)
            if(b[i] != 0)
                return false;
        return true;
    }

    //open the specified disk 
    public static boolean openFS(String name){
        try{
            if(raf != null)
                raf.close();
            File f = new File(name);
            RandomAccessFile ram = new RandomAccessFile(f,"rw");
            openedFS = f;
            raf = ram;
            isOpen = true;
            if(!loadMeta()){
                return false;
            }
            System.out.println(name+" opened successfully");
            return true;
        }
        catch(Exception e){
            System.out.println("Unable to open File System(check the disk's name etc)...");
            return false;
        }

    }

    //list meta data 
    public static void list(){
        System.out.println("----------------File System Information--------");
        System.out.println("Name: "+ metaData.name);
        System.out.println("Created on: "+metaData.date +" "+ metaData.time);
        System.out.println("Size: "+metaData.totNumBlocks+" Blocks");
        System.out.println("Meta data size is "+metaData.metaBlocks+" Blocks");
        System.out.println("FNT size is "+metaData.numFNT+" Blocks");
        System.out.println("Total saved to data: "+metaData.numSavedBlocks+" Blocks");
        System.out.println("Number of blocks available to save data: "+metaData.freeBlocks+" Blocks");
        System.out.println("--------------List of Files-----------");
        try{
            int j = metaData.metaBlocks*128;
            String temp;
            int inode;
            raf.seek(j);
            for(int i=0 ; i<metaData.numFNT*2 ; i++){
                temp = raf.readUTF();
                if(!temp.equals("")){
                    inode = raf.readInt();
                    raf.seek(inode);
                    raf.readInt();
                    raf.readInt();
                    System.out.println("Name :"+temp+"\tSize (Bytes):"+raf.readInt()+"\tDate :"+raf.readUTF()+"\tTime :"+raf.readUTF()+"\tUser :"+raf.readUTF());
                }
                j += 64;
                raf.seek(j);
            }
        }
        catch(Exception e){
            System.out.println("Error");
        }

    }

    //remove the file 
    public static boolean remove(String name){
          try{
            File f = new File(name);
            if(f.delete())
            {
                System.out.println(f.getName()+"  The filesystem has been deleted");
            }
            else{
                System.out.println("failed");
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }

    //put the file into the disk  
    public static boolean put(String name){

        try{
            byte [] tmp = new byte[128];
            String nameA [] = name.split("(\\\\)|(/)");
            File f = new File(name);
            FileInputStream fileInStr = new FileInputStream(f);
            byte[] data = new byte[(int) f.length()];
            fileInStr.read(data);
            fileInStr.close();
            int z = metaData.metaBlocks * 128;
            String tempName;
            raf.seek(z);
            for(int i=0 ; i<metaData.numFNT*2 ; i++){
                tempName = raf.readUTF();
                if(tempName.equals(nameA[nameA.length-1])){
                    System.out.println("File with the same name already exist in the File System...");
                    return false;
                }
                z += 64;
                raf.seek(z);
            }

            if( ((int)(java.lang.Math.ceil((double)(data.length)/128))) <= (metaData.freeBlocks)/2 ){
                int j = metaData.metaBlocks * 128;
                raf.seek(j);
                while(!(raf.readUTF().equals(""))){
                    j += 64;
                    raf.seek(j);
                }
                int FNTi = j;
                j = (metaData.metaBlocks * 128) + (metaData.numFNT * 128);
                raf.seek(j);
                raf.read(tmp);
                while(!isBlockAvailable(tmp)){
                    j += 128;
                    raf.read(tmp);
                }
                int curI = j;
                int freeB;
                int nextI = 0;

                raf.seek(FNTi);
                raf.writeUTF(new String(nameA[nameA.length-1].getBytes("UTF-8")));
                raf.writeInt(curI);

                for( int i=0 ; i < ((int)(java.lang.Math.ceil((double)(data.length)/128))) ; i++ ){
                    j = curI + 128;
                    raf.seek(j);
                    raf.read(tmp);
                    while(!isBlockAvailable(tmp)){
                        j += 128;
                        raf.read(tmp);
                    }
                    freeB = j;
                    j += 128;
                    raf.seek(j);
                    raf.read(tmp);
                    while(!isBlockAvailable(tmp)){
                        j += 128;
                        raf.read(tmp);
                    }
                    nextI = j;
                    if(i==0){
                        raf.seek(curI);
                        raf.writeInt(freeB);
                        raf.writeInt(nextI);
                        raf.writeInt(data.length);
                        DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
                        String [] t = df.format(new Date()).split(" ");
                        raf.writeUTF(new String(t[0].getBytes("UTF-8")));
                        raf.writeUTF(new String(t[1].getBytes("UTF-8")));
                        raf.writeUTF(new String(System.getProperty("user.name").getBytes("UTF-8")));
                    }
                    else{
                        raf.seek(curI);
                        raf.writeInt(freeB);
                        raf.writeInt(nextI);
                    }
                    raf.seek(freeB);
                    byte [] tmpW = new byte[128];
                    for(int x=0 ; x<128 ; x++)
                        tmpW[x] = 0;
                    int y=0;
                    for(int x=128*i ; x<(128*i)+128 && x<data.length ; x++  )
                        tmpW[y++] = data[x];
                    raf.write(tmpW);
                    int tmpI = curI;
                    curI = nextI;
                    nextI = tmpI;
                    metaData.freeBlocks -= 2 ;
                    raf.seek(16);
                    raf.writeInt(metaData.freeBlocks);

                }
                if(nextI != 0){
                    raf.seek(nextI);
                    raf.readInt();
                    raf.writeInt(0);
                }
                return true;
            }
            else System.out.println("Not enough space in the disk. Please free some space.");
        }
        catch(Exception e){
            System.out.println("File not found or FNT becomes full");
        }

        return false;
    }
    
    public static boolean get(String name){
        try{
            int j = metaData.metaBlocks * 128;
            raf.seek(j);
            String temp="";
            int ptr=0;
            int dataP = 0;
            int size = 0;
            for(int i=0 ; i<metaData.numFNT*2 ; i++){
                temp = raf.readUTF();
                if(temp.equals(name))
                    break;
                else{
                    j += 64;
                    raf.seek(j);
                }
            }

            if(temp.equals(name)){
                File f = new File(name+ " disk_copy");
                FileOutputStream fos = new FileOutputStream(f);

                ptr = raf.readInt();
                raf.seek(ptr);
                dataP = raf.readInt();
                ptr = raf.readInt();
                size = raf.readInt();
                byte [] data = new byte[size];
                byte [] tmpData = new byte[128];
                raf.seek(dataP);
                raf.read(tmpData);
                fos.write(tmpData);
                while(ptr!=0){
                    raf.seek(ptr);
                    dataP = raf.readInt();
                    ptr = raf.readInt();

                    raf.seek(dataP);
                    raf.read(tmpData);
                    fos.write(tmpData);

                }
                fos.getChannel().truncate(size);
                fos.close();

                return true;
            }
            else System.out.println("Error: file does not exist");
        }
        catch(Exception e){
            System.out.println("Error: file does not exist.");
        }
        return false;
    }

    public static void user(String name){
    String curUser = System.getProperty("user.name");
        try{
            int count=0;
            int j = metaData.metaBlocks * 128;
            raf.seek(j);
            String temp="";
            String fUser;
            int ptr;
            for(int i=0 ; i<metaData.numFNT*2 ; i++){
                temp = raf.readUTF();
                if(temp.equals("")){
                    ptr = raf.readInt();
                    raf.seek(ptr);
                    raf.readInt();
                    raf.readInt();
                    raf.readInt();
                    raf.readUTF();
                    raf.readUTF();
                    fUser = raf.readUTF();
                    if(fUser.equals(curUser)){
                        raf.seek(ptr);
                        raf.readInt();
                        raf.readInt();
                        raf.readInt();
                        raf.readUTF();
                        raf.readUTF();
                        raf.writeUTF(new String(name.getBytes("UTF-8")));
                        count++;
                    }
                }
                j += 64;
                raf.seek(j);
            }
            if(count > 0) System.out.println(count+" Files ownership changed from "+curUser+" to "+name);
            else System.out.println("No files exist ");
        }
        catch(Exception e){
            System.out.println(curUser);
        }
    }

    public static void link(String source, String newLink){
        try{
            Path regularFile = Paths.get(metaData.name);
            Path link = Paths.get("Linkfile.txt");
            if(Files.exists(link)){
                Files.delete(link);
            }
            else 
             System.out.println(source+ " does not exist");
            Files.createSymbolicLink(link, regularFile);
         System.out.println("Link \""+newLink+"\" to the source file \""+source+"\" is created");
        }
        catch(Exception e){
            System.out.println("Error creating a link");
        }
    }

    //remove the link
    public static void unlink(String name){
        try{
            File myObj = new File("Linkfile.txt"); 
              if (myObj.delete()) { 
                System.out.println("Unlinked file: " + myObj.getName());
              }
            else {
                System.out.println(name+" does not exist");
            }
        }
        catch(Exception e){
            System.out.println("Error");
        }
    }

    public static void main(String[] args) {

        String input = "";
        String name = "";
        Scanner s = new Scanner(System.in);
        System.out.println("\n---------------------Menu---------------------");
        System.out.println("createfs --> To create Filesystem\nformatfs --> To format Filesysytem\nsavefs --> To save the Filesystem\nopenfs --> To open Filesystem\nList --> TO list the files in Filesystem and the disk size\nremove --> To remove the files from Filesystem\nRename --> Rename oldname newname\nput --> Put ExternalFile into disk\nget --> Get ExternalFile into disk\nuser --> User name\nLink --> Link the file\nUnlink --> Unlink the file\nquit --> exit from File System\nHelp --> Help\n");
        System.out.println("Enter command:");
        while(!(input.equals("quit"))){
            System.out.printf(">>");
            input = s.nextLine();
            String [] userInput = input.split(" ");
            switch(userInput[0]){
                case "createfs": 
                    if(!ifCreated){ 
                        int num;
                        try{
                            num = Integer.parseInt(userInput[1]);
                        }
                        catch(Exception e){ 
                            System.out.println("Number of blocks size was not entered");
                            break;
                        }
                        if(num >= 0){
                            ifCreated = true;
                            fs.totNumBlocks = num; // create a file system disk of num blocks
                            System.out.println("Total number of Blocks:");
                            System.out.println(fs.totNumBlocks);
                            System.out.println("File system created");
                            //System.out.println(fs);

                        }
                    }
                    else System.out.println("File system already exists.");
                    break;
                case "formatfs":
                if(userInput.length == 1){
                    if(ifFormated)
                        System.out.println("File system is not yet saved");
                    else if(ifCreated){
                        formatFS();
                        ifFormated = true;
                        ifCreated = false;
                    }
                    else System.out.println("Error: no file system to be formatted"); 
                }
                else System.out.println("Enter valid number of arguments...");
                break;

                case "savefs":
                if(ifFormated){
                    try{
                            name = input.replaceFirst("savefs ", "");
                            if( name.length() <= 5 && name.length() > 0 ){
                                saveFS(name);
                                ifFormated = false;
                            }
                            else{
                                System.out.println("Not a valid name...");
                            }
                       }
                        catch(Exception e){
                            System.out.println("Error: invalid name");
                        }
                    }
                    else System.out.println("Error: file system must be formatted first.");
                    break;
                case "openfs":
                    try{
                        name = input.replaceFirst("openfs ", "");
                        System.out.println(name);
                        if(openFS(name)) isOpen = true;
                        else System.out.println("File system not found");
                     } 
                    catch(Exception e){
                        System.out.println("Error: Invalid name");
                    }
                    break;
                case "list":
                    if(isOpen) list();
                    else System.out.println("Error: File System must be opened first");
                    break;
                case "remove":
                    if(isOpen){
                        try{
                            name = input.replaceFirst("remove ", "");
                            if(remove(name)) 
                            System.out.println("File removed successfully");
                            
                        }
                        catch(Exception e){
                            System.out.println("Error: file not copied");
                        }
                    }
                    else System.out.println("Error: File System must be opened first");
                    break;
                case "put":
                    if(isOpen){
                        try{
                            name = input.replaceFirst("put ", "");
                            if(put(name)) System.out.println("File has been copied to disk");
                            else System.out.println("Error: file not added");
                        }
                        catch(Exception e){
                            System.out.println("Error: Invalid name");
                        }
                    }
                    else System.out.println("Error: File System must be opened first");
                    break;
                case "get":
                    if(isOpen){
                        try{
                            name = input.replaceFirst("get ", "");
                            if(get(name))
                                System.out.println("File has been copied to OS");
                            else System.out.println("Error: file not copied");
                        }
                        catch(Exception e){
                            System.out.println("Error: Invalid name");
                        }
                    }
                    else System.out.println("Error: File System must be opened first");
                    break;

                case "user":
                    if(isOpen){
                        try{
                            //name = input.replaceFirst("user ", "");
                            user(name);
                        }
                        catch(Exception e){
                            System.out.println("Error: Invalid name");
                        }
                    }
                    else System.out.println("Error: File System must be opened first");
                    break;

                case "rename":
                    File file = new File(userInput[1]);
                    File file2 = new File(userInput[2]);
                    file.renameTo(file2);
                    System.out.println("The File system has been renamed");
                    boolean success = file.renameTo(file2);
                    break;

                case "link":
                    if(isOpen){
                        if(userInput.length == 3){
                            try{
                                link(userInput[1],userInput[2]);
                            }
                            catch(Exception e){
                                System.out.println("Error: Invalid names");
                            }
                        }
                        else System.out.println("Enter the correct number of arguments\n\tsource file1 file2...");
                    }
                    else System.out.println("Error: File System must be opened first");
                    break;

                case "unlink":
                    if(isOpen){
                        try{
                             unlink(userInput[1]);
                        }
                        catch(Exception e){
                            System.out.println("Error: Invalid name");
                        }
                    }
                    else System.out.println("Error: File System must be opened first");
                    break;

                case "quit":
                    try{
                        raf.close();
                    }
                    catch(Exception e){}
                    break;

                case "help":
                    {
                        System.out.println("\n---------------------Menu---------------------");
                        System.out.println("createfs --> To create Filesystem\nformatfs --> To format Filesysytem\nsavefs --> To save the Filesystem\nopenfs --> To open Filesystem\nList --> TO list the files in Filesystem and the disk size\nremove --> To remove the files from Filesystem\nRename --> Rename oldname newname\nput --> Put ExternalFile into disk\nget --> Get ExternalFile into disk\nuser --> User name\nLink --> Link the file\nUnlink --> Unlink the file\nquit --> exit from File System\nHelp --> Help\n");
                        break;
                    }
                default:
                    System.out.println("Error: Invalid command");
                    break;
            }
        }
    }
}