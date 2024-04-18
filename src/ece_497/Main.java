package ece_497;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Main {
    static Core Core = new Core();

    public static void main(String[] args) throws Exception {
        
        System.out.println("Welcome to the 5G Network");
        
        for(int i=0; i<=4; i++) {
            ArrayList<UE> ueArray = new ArrayList<>();
            String filename = "TestCase0"+(Integer.toString(i))+".csv";

            System.out.println("*** "+filename+" ***");
    
                    //   1     2        3        4            5          6
            // Headers: UE,Priority,Callbar,Prempt_Capable,Prempt_Vul,AppType
            JTable input = UDM.CSVToJTable(filename);
            
            // Loop through input UE data
            for (int i = 1; i < input.getRowCount(); i++) {
                boolean result = false;
                
                // Create UE object with provided information from files
                UE ue = new UE(Integer.parseInt(input.getValueAt(i, 0).toString()), Integer.parseInt(input.getValueAt(i, 1).toString()), 
                                Integer.parseInt(input.getValueAt(i, 2).toString()), Boolean.parseBoolean(input.getValueAt(i, 3).toString()), 
                                Boolean.parseBoolean(input.getValueAt(i, 4).toString()), input.getValueAt(i, 4).toString());
                ueArray.add(ue);
                
                // Call admission and service functions
                result = AN(ue);

                // Print statement
                if (result) {
                    System.out.println("** UE "+ue.getId()+" has been admitted **");
                    System.out.println("  Bandwidth Allocated: "+ue.getBandInt()+" Mbps.");
                }
                else {
                    System.out.println("** UE "+ue.getId()+" has not been admitted **");
                    System.out.println(" Due to: "+ue.getReason()+".");
                }
            }
        }
    }

    public static boolean AN(UE ue) {
        return Core.AMF(ue);
    }
}
