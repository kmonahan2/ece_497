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
            JTable input = CSVToJTable(filename);
            
            // Loop through input UE data
            for (int j = 1; j < input.getRowCount(); j++) {
                boolean result = false;
                
                // Create UE object with provided information from files
                UE ue = new UE(Integer.parseInt(input.getValueAt(j, 0).toString()), Integer.parseInt(input.getValueAt(j, 1).toString()), 
                                Integer.parseInt(input.getValueAt(j, 2).toString()), Boolean.parseBoolean(input.getValueAt(j, 3).toString()), 
                                Boolean.parseBoolean(input.getValueAt(j, 4).toString()), input.getValueAt(j, 4).toString());
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

    // helper function
    public static JTable CSVToJTable(String filename) {
        JTable table = new JTable();
        table.setName(filename);
        
        // Create a table model
        DefaultTableModel model = new DefaultTableModel();
        // Headers: UE,Priority,Callbar,Prempt_Capable,Prempt_Vul,AppType
        model.addColumn("UE");
        model.addColumn("Priority");
        model.addColumn("Callbar");
        model.addColumn("Prempt_Capable");
        model.addColumn("Prempt_Vul");
        model.addColumn("AppType");
        

        // Read CSV file and add rows to the table
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = br.readLine()) != null) throw Exception {
                String[] data = line.split(","); // Assuming CSV uses comma as delimiter, change accordingly
                model.addRow(data);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // build the jtable
        table = new JTable(model);
        JFrame frame = new JFrame();
        frame.add(new JScrollPane(table));

        return table;
    }
}
