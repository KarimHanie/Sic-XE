
import java.io.File;
import java.util.Scanner;

import javax.sound.sampled.SourceDataLine;
import javax.swing.plaf.synth.SynthStyle;
public class SicXE {
    public static void main(String[] args) {
        int count = 0;//counter to know the size of arrays 
        
        try {
            Scanner scanner = new Scanner(new File("D:\\1.aast\\1.college\\term 5\\system programming\\sicxe.txt"));
            while (scanner.hasNextLine()) {// chech if scanner has next line so i will increase value of couner 
                String data = scanner.nextLine();
                count++;
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }
        String[] label = new String[count];
        String[] inst = new String[count];
        String[] refe = new String[count];
        String[] LC = new String[count];
        String[] OC = new String[count];
        count = 0;//reset value  of counter so i can start over 
        try {
            Scanner scanner = new Scanner(new File("D:\\1.aast\\1.college\\term 5\\system programming\\sicxe.txt"));
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] c = line.split("\t"); // split each line by tab
                // as line.split return array so 'C' will store the length of that array  
                label[count] = c[0];
                inst[count] = c[1];
                refe[count] = c[2];
                count++;
            }
            // method return String array assign it's value to LC " lcoation counter "array   
            LC = LocationCounter(inst, refe, LC.length,label);
            // method return String array assign it's value to OC" Object code"
            OC = ObjectCode(label, inst, refe, LC, LC.length); 
            //to handle negative displacement 
            // i want to add the first 3bytes + last 3bits of displacement
            // like 3f2ffffffec9 >> i want to take first 3 bits + last 3 bits  
            for (int i = 0; i < OC.length; i++) {
                if (OC[i].contains("fff")) {
                    System.out.println(" ffff:"+OC[i]+""+i);
                    String start = OC[i].substring(0, 3);
                    String end = OC[i].substring(8, 11);;
                    OC[i] = start + end;
                }
            }
            System.out.println("Program:");
            System.out.println("-------------");
            for (int i = 0; i < LC.length; i++) {
                System.out.printf("%s%10s%10s%10s%10s",LC[i],label[i],inst[i],refe[i],OC[i].toUpperCase()+"\n");
            }
            System.out.println();
            SymbolTable(label, LC, LC.length);
            System.out.println();
            System.out.println("HTE record:");
            System.out.println("-------------");
            HTErecord(label, inst, refe, LC, OC);
            System.out.println("new values : ");
            RREFF(inst, label, refe, LC, LC.length);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
    // method send to it inst , refe , lc.length 
    public static String[] LocationCounter(String[] I, String[] r, int count,String[] Label) {
        String[] LC = new String[count];
        LC[0] = LC[1] =  r[0] + "000";
        String F ="";
        for (int i = 2; i < LC.length; i++) {//start from index 2 as the first 2 indexes already have values 
                                             // which means I shifted the array by 1 that's why I decremented it later on  
            if (I[i - 1].equalsIgnoreCase("RESW")) {
                //check if prev instruction is RESW if so i will multiply ref value *3 after 
                // then i will change prev location counter into decimal and i will add two values 
                // then i will convert it to decimal 
                int num = Integer.parseInt(r[i - 1]);
                num = num * 3;
                int num2 = Integer.parseInt(LC[i - 1], 16);
                num2 = num2 + num;
                LC[i]= String.format("%04X",num2).toUpperCase();
            } else if (I[i - 1].equalsIgnoreCase("RESB")) {
                // same as above but here i change value into decimal and add it to prev location counter  
                int num = Integer.parseInt(LC[i - 1], 16);
                int num2 = Integer.parseInt(r[i - 1]);
                num2 = num2 + num;
                LC[i]= String.format("%04X",num2).toUpperCase();
            } else if (I[i - 1].equalsIgnoreCase("Byte") && r[i - 1].contains("C")) {
                // in case prev instruction was byte and character 
                //then i will get the length of the reference-3 and count and add it to prev location counter 
                int num = Integer.parseInt(LC[i - 1], 16);
                num = num + (r[i - 1].length() - 3);
                LC[i]=String.format("%04X", num).toUpperCase();
            } else if (I[i - 1].equalsIgnoreCase("Byte") && r[i - 1].contains("X")) {
                // in case it was byte and X them i will get the length of array -3 and divide it by 2 " cause 2 hexa = 1 byte"
                int num = Integer.parseInt(LC[i - 1], 16);
                num = num + ((r[i - 1].length() - 3) / 2);
                LC[i] = String.format("%04X",num).toUpperCase();   
            } else if (I[i - 1].contains("+")) {
                // in case instruction contains + that's means it is formate 4 then i will add 4 to prev location counter 
                int num = Integer.parseInt(LC[i - 1], 16);
                num = num + 4;
                LC[i] =String.format("%04x",num).toUpperCase();   
            }else if (I[i - 1].contains("$")) {
            
                
                /*for(int k=0;k<Label.length;k++){
                    if(Label[k].equalsIgnoreCase("F")){
                        F = LC[k];
                    }
                }*/
                 
                int num = Integer.parseInt(LC[i - 1], 16);
                num = num + 6;
                LC[i] =String.format("%04x",num).toUpperCase();   
            }else if (r[i-1].contains("*")){
                String secondVAlue="";
                String valueofconstant =LC[i-1];
                String VSP[]=r[i].split("-");
                for(int k=0;k<Label.length;k++){
                    if(Label[k].equalsIgnoreCase(VSP[1])){
                        secondVAlue=LC[k];
                    }
                }
                System.out.println("MAxlen"+valueofconstant);
                System.out.println("buffer"+secondVAlue);
                int V= Integer.parseInt(valueofconstant,16)-Integer.parseInt(secondVAlue,16);
                System.out.println("value of V "+String.format("%04X", V));
                
                LC[i]=String.format("%04X",V);

            } 
            
            else {
                // check the rest of the instruction from table to get formate  of each one 
                int num = 0;
                Converter.initialize();
                for (int j = 0; j < Converter.OPTAB.length; j++) {
                    if (I[i - 1].equalsIgnoreCase(Converter.OPTAB[j][0])) {
                        num = Integer.parseInt(Converter.OPTAB[j][1], 16);
                        break;
                    }
                }
                int num2 = Integer.parseInt(LC[i - 1], 16);
                num = num + num2;
                LC[i]= String.format("%04X",num);
            }
        }
        return (LC);// String array reference 
    }
    //method take label array ref , Location counter array ref, length of location counter length  
    public static void SymbolTable(String[] l, String[] LC, int count) {
        String[] la = new String[count];
        System.out.println("Symbol Table:");
        System.out.println("-------------");
        for (int i = 0; i < count; i++) {
            if (l[i].isEmpty() == false) {
                System.out.println(l[i] + "\t" + LC[i]);
            }
        }
    }
    public static void RREFF(String[] ins, String[] l ,String[] r, String[] LC, int count ){
     String regla = "";
     String reglalc = "";
     int reglaValue;
     String prev = "";
        for (int i = 0; i < count; i++) {
            if (r[i].isEmpty() == false) {
                if(r[i] == "REGF"){
                    regla = l[i];
                    System.out.println("hello");
                    System.out.println(l[i]);
                }
                
            }
        }
        for (int i = 0; i < count; i++) {
            if (l[i].isEmpty() == false) {
                if (l[i] ==  regla){
                        reglalc = LC[i];
                        System.out.println("hello");
                }
            }}
            for (int i = 0; i < count; i++) {
                if (ins[i].isEmpty() == false) {
                    if(ins[i].contains("$")){
                        prev = LC[i-1]; 
                        System.out.println("hello");
                    }
                    
                }
            }
            System.out.println(regla);
            System.out.println(reglalc);
            //reglaValue = (Integer.parseInt(reglalc,16)-Integer.parseInt(prev,16))/2;
            
            //System.out.println(reglaValue);


    }

   // method take label ,instructions, reference ,location counter, location counter length 
    public static String[] ObjectCode(String[] l, String[] I, String[] R, String[] LC, int count) {
        String[] OC = new String[count];
        OC[0] = "";
        Converter.initialize();
        String num = "";
        String format = "";
        for (int i = 1; i < OC.length; i++) {
            // flag carry values of n i x b p e
            String[] flag = new String[]{"0", "0", "0", "0", "0", "0"};
            /* in this fore loop > handle + condition 
               after replacing the + > call method in converter to get instruction's format and opcode 
                also search for the opcode for the rest of instructions that dont have + on it 
               */
            for (int j = 0; j < Converter.OPTAB.length; j++) {
                if (I[i].replace("+", "").equalsIgnoreCase(Converter.OPTAB[j][0])) {
                    // change opcode value to binary number 
                    num = Integer.toBinaryString(Integer.parseInt(Converter.OPTAB[j][2], 16));
                    format = Converter.OPTAB[j][1];
                    break;
                }
            }
            String op = Zero1(num);// - 2 bit // send num of +instruction to method zero 1
                                              // change deximal number into binary and remove last 2 bits return string value 
            if (I[i].equalsIgnoreCase("RSUB")) {
                //N I X B P E
                flag[0] = flag[1] = "1";
                for (int j = 0; j < 6; j++) {
                    op += flag[j];// add opcode + flag 
                }
                // since it is  RSUB so it doesnt have any address so i will add 3 zeros
                // convert from binary into decimal 
                OC[i] = op = Integer.toHexString(Integer.parseInt(op, 2)) + "000";
            } else if (I[i].equalsIgnoreCase("Word")) {
                // convert String of reference into decimal then convert him again into hexa in 6 bits
                String d = R[i];
                int de = Integer.parseInt(d);
                d = Integer.toHexString(de);
                de = Integer.parseInt(d);
                OC[i] = String.format("%06d", de);
            } else if (I[i].equalsIgnoreCase("Byte") && R[i].contains("X")) {
                // incase byte X then i will replace x'' and i will add rest of String cause its already hexa 
                String re = R[i];
                String ref = re.replace("X", "");
                ref = ref.replace("'", "");
                OC[i] = ref;
            } else if (I[i].equalsIgnoreCase("Byte") && R[i].contains("C")) {
                // incase byte C then i will replace c'' and i will change the rest of string into ascii code 
                // by casting from string to integer 
                String result = "";
                String re = R[i];
                String ref = re.replace("C", "");
                ref = ref.replace("'", "");
                for (int h = 0; h < ref.length(); h++) {
                    int ascii = ref.charAt(h);
                    result = result + Integer.toHexString(ascii);
                }
                OC[i] = result;
            } else if (R[i].contains("#")) {//immediate 
                String disp = "";
                if (I[i].contains("+")) {// 1. incase it was immediate and the instruction has + on it then flag5 =1 " e"
                    flag[5] = "1";
                }
                flag[1] = "1";           // 2. flag1 =1 "I"
                disp = R[i].replace("#", ""); // i will replace # and store what come after it in disp variable 
                char ce = disp.charAt(0);     // store first character in disp to check if its a digit or not 
                if (Character.isDigit(ce)) {    // incase first character was a digit then i will add opcode +flag
                    for (int j = 0; j < 6; j++) {  // add value of opcode to flag 
                        op += flag[j];
                    }
                    if (I[i].contains("+")) {// incase instruction contian + then i will represesnt displacement in 5 digits 
                        disp = Zero2(Integer.toHexString(Integer.parseInt(disp)), 5);
                    } else {// else mean foramte 3 then i will represent dip in 3 digits 
                        disp = Zero2(Integer.toHexString(Integer.parseInt(disp)), 3);
                    }
                    // convert opcode into dicemal then hexa + dip and send  them to zero2 to represent in 6 bits 
                    OC[i] = op = Zero2(Integer.toHexString(Integer.parseInt(op, 2)) + disp, 6);
                } else {// incase #with variable 
                    for (int j = 0; j < l.length; j++) {
                        if (disp.equalsIgnoreCase(l[j])) { // search in label to match disp 
                            disp = LC[j]; // disp=location counter 
                            break;
                        }
                    }
                    if (I[i].contains("+")) {// if instruction contain + then 
                        for (int j = 0; j < 6; j++) {
                            op += flag[j];  // add opcode to flagbits 
                        }
                        // represent object code in 8 bits >> 3bytes opcode+ flagbits and 5 bits address 
                        OC[i] = op = Zero2(Integer.toHexString(Integer.parseInt(op, 2)) + Zero2(disp, 5), 8);
                    } else {// incase # with variable 
                        int res = Integer.parseInt(disp, 16) - Integer.parseInt(LC[i + 1], 16);// calc displacement using pc at first then check if its in range or not 
                        if (res <= 2047 && res >= -2048) {
                            flag[4] = "1";// if in range then flag 4 'P' equal 4 
                            for (int j = 0; j < 6; j++) {
                                op += flag[j];
                            }
                        } else {
                            flag[3] = "1"; // if not then w use base then flag 3 'B' =1 
                            for (int j = 0; j < 6; j++) {
                                op += flag[j];
                            }
                        }
                        OC[i] = op = Integer.toHexString(Integer.parseInt(op, 2)) + Zero2(Integer.toHexString(res), 3);
                    }
                }
            } else if (I[i].equalsIgnoreCase("RESW") || I[i].equalsIgnoreCase("RESB") || I[i].equalsIgnoreCase("Base") || I[i].equalsIgnoreCase("End")) {
                OC[i] = op = "------";// cause they just have no object code so i'll just ignore them 
            } else if (R[i].contains("@")) {// incase Indirect means i=0 n=1
                flag[0] = "1";
                String disp = R[i].replace("@", "");
                // same as # check pc or base , search for label that match disp...etc
                for (int j = 0; j < l.length; j++) {
                    if (disp.equalsIgnoreCase(l[j])) {
                        disp = LC[j];
                        break;
                    }
                }
                int res = Integer.parseInt(disp, 16) - Integer.parseInt(LC[i + 1], 16);
                if (res <= 2047 && res >= -2048) {
                    flag[4] = "1";
                    for (int j = 0; j < 6; j++) {
                        op += flag[j];
                    }
                } else {
                    flag[3] = "1";
                    for (int j = 0; j < 6; j++) {
                        op += flag[j];
                    }
                }
                OC[i] = op = Integer.toHexString(Integer.parseInt(op, 2)) + Zero2(Integer.toHexString(res), 3);
            } else if (format.equalsIgnoreCase("2")) {// incase format 2 then register-to-register operations
                op = Integer.toHexString(Integer.parseInt(num, 2));// get opcode of instruction
                String[] reg = new String[]{"A", "X", "L", "b", "s", "t", "f"};// all register we have arranged based on thier value 1-2-3-4-5-6-7
                int in = 0;
                if (R[i].contains(",")) {
                    String[] reg2 = R[i].split(",");// after spliting reference we have new string 
                    // scan that string and match it with the reg array 
                    for (int j = 0; j < reg2.length; j++) {
                        for (int k = 0; k < reg.length; k++) {
                            if (reg2[j].equalsIgnoreCase(reg[k])) {
                                in = k;// value of counter k equal value of that register 
                                System.out.println("value of in"+in);
                                op += Integer.toHexString(in);
                                System.out.println("value of in op "+op);

                                break;
                            }
                        }
                    }
                } else {// incase i have only 1 reg then that means second reg equal 0 
                    String ref = R[i];
                    for (int j = 0; j < reg.length; j++) {
                        if (ref.equalsIgnoreCase(reg[j])) {
                            in = j;
                            break;
                        }
                    }
                    op += Integer.toHexString(in);
                    int z = 4 - op.length();// to add missing reg values as a zero's
                    for (int j = 0; j < z; j++) {
                        op = op + "0";
                    }
                }
                OC[i] = op;
            } else {// if simple which mean n=1, i=1
                flag[0] = flag[1] = "1";
                String disp = R[i];
                int res = 0;
                if (R[i].contains(",X")) {// incase ,x then flag 2 >> x=1 
                    disp = R[i].replace(",X", "");
                    flag[2] = "1";
                }
                for (int j = 0; j < l.length; j++) {// search label to find address of reference = disp
                    if (disp.equalsIgnoreCase(l[j])) {
                        disp = LC[j];
                        break;
                    }
                }
                if (I[i].contains("+")) {// if instruction format 4 then flag 5 =1 and addresss will ne disp in 5 bits 
                    flag[5] = "1";
                    for (int j = 0; j < 6; j++) {
                        op += flag[j];
                    }
                    OC[i] = op = Zero2(Integer.toHexString(Integer.parseInt(op, 2)) + Zero2(disp, 5), 8);
                }
                if (!I[i].contains("+") && !disp.contains(",X")) { // if not format 4 and doesnt have ,x then format 3  
                                                                   // search label to match that reference and i will use pc as default 
                                                                    // check if in range or not
                    res = Integer.parseInt(disp, 16) - Integer.parseInt(LC[i + 1], 16); // subtract address of disp - and address of the next instruction to be fetched
                    if (res <= 2047 && res >= -2048) {
                        flag[4] = "1";   // if in range then p =1 amd b =0 
                        for (int j = 0; j < 6; j++) {
                            op += flag[j];
                        }
                    } else {
                        flag[3] = "1";// else b=1 and p=0
                        String d = "";
                        for (int j = 0; j < 6; j++) {
                            op += flag[j];
                        }
                        for (int j = 0; j < l.length; j++) {
                            if (I[j].equalsIgnoreCase("Base")) {// i will search for base cause 
                                d = R[j];                       // i need the value of his reference - that why we put value of R[j] in d we will use it later to search in label 
                                break;
                            }
                        }
                        for (int j = 0; j < LC.length; j++) {
                            if (l[j].equalsIgnoreCase(d)) {// here we used variable d to search in label and then will take address 
                                res = Integer.parseInt(disp, 16) - Integer.parseInt(LC[j], 16);
                                break;
                            }
                        }
                    }
                    OC[i] = op = Zero2(Integer.toHexString(Integer.parseInt(op, 2)) + Zero2(Integer.toHexString(res), 3), 6);
                }
            }
        }
        return OC;
    }

    public static void HTErecord(String[] l, String[] I, String[] R, String[] LC, String[] OC) {
        int len1 = Integer.parseInt(LC[LC.length - 1], 16) - Integer.parseInt(LC[1], 16);// length of program 
        System.out.print("H" + " " + l[0] + " " + Zero2(R[0].toUpperCase(), 6) + " " + Zero2(Integer.toHexString(len1).toUpperCase(), 6));
        int t = 1;
        String te = "";
        int count = t;
        for (int i = t; i > 0; i++) {
            if (I[i].equalsIgnoreCase("end")) {
                System.out.println();
                break;
            } else if (I[i].equalsIgnoreCase("Base")) {
                count++;
            } else if (I[i].equalsIgnoreCase("RESW") || I[i].equalsIgnoreCase("RESB")) {
                t = i+1 ;
                te = te + "\n";
                count = 0;
            } else if (count == 10) {
                te = te + " " + OC[i];
                te = te + "\n";
                count = 0;
            } else {
                te = te + " " + OC[i];
                count++;
            }
        }
        String[] x = te.split("\n");
        String start = "";
        String end = "";
        for (int i = 0; i < x.length; i++) {
            if (!x[i].isEmpty()){
                String[] z = x[i].split(" ");
                start = z[1];
                end = z[z.length - 1];
                int index = SearchOCS(OC, start);
                int len2 = Integer.parseInt(LC[SearchOCE(OC, end, index) + 1], 16) - Integer.parseInt(LC[index], 16);// calac length of t record 
                x[i] = Zero2(Integer.toHexString(len2).toUpperCase(), 2) + x[i];
            }
        }
        for (int i = 0; i < x.length; i++) {
            if (!x[i].isEmpty()) {
                String[] z = x[i].split(" ");
                x[i] = "T" + " " + Zero2(LC[SearchOCS(OC, z[1])], 6) + " " + x[i];
                System.out.println(x[i].toUpperCase());
            }
        }
        String m = "";
        for (int i = 0; i < I.length; i++) {
            if (I[i].contains("+") && !R[i].contains("#") && !R[i].contains("@")) {
                int c = Integer.parseInt(LC[i], 16);
                m = "M" + " " + Zero2(Integer.toHexString(c + 1), 6) + " " + "05";
                System.out.println(m);
            }
        }
        System.out.println("E" + " " + Zero2(LC[0], 6));
    }

    public static int SearchOCS(String[] OC, String t) {
        int index = 0;
        for (int i = 1; i < OC.length; i++) {
            if (OC[i].equalsIgnoreCase(t)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public static int SearchOCE(String[] OC, String t, int index2) {
        int index = 0;
        for (int i = index2; i < OC.length; i++) {
            if (OC[i].equalsIgnoreCase(t)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public static String Zero1(String z) {
        int num2 = 8 - z.length();
        for (int i = 0; i < num2; i++) {
            z = "0" + z;
        }
        // 0001 0011
        // opcode 6bit
            //0001 00 
        z = z.substring(0, z.length() - 2);
        return z;
    }

    public static String Zero2(String z, int num) {
        int num2 = num - z.length();
        for (int i = 0; i < num2; i++) {
            z = "0" + z;
        }
        return z;
    }
}
