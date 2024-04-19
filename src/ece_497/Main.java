package ece_497;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Main {
        public static void main(String[] args) throws Exception {
        
        System.out.println("Welcome to the 5G Network");
        
        for(int i=0; i<=4; i++) {
            Core core = new Core();

            ArrayList<UE> ueArray = new ArrayList<>();
            String filename = "TestCase0"+(Integer.toString(i))+".csv";


            System.out.println();
            System.out.println("*********************");
            System.out.println("*** "+filename+" ***");
    
                    //   1     2        3        4            5          6
            // Headers: UE,Priority,Callbar,Prempt_Capable,Prempt_Vul,AppType
            JTable input = CSVToJTable(filename);
            
            // Loop through input UE data
            for (int j = 1; j < input.getRowCount(); j++) {
                boolean result = false;

                boolean tempCap;
                boolean tempVul;
                if (Integer.parseInt(input.getValueAt(j,3).toString()) == 1) {
                    tempCap = true;
                } else {
                    tempCap = false;
                }
                if (Integer.parseInt(input.getValueAt(j,4).toString()) == 1) {
                    tempVul = true;
                } else {
                    tempVul = false;
                }

                // Create UE object with provided information from files
                UE ue = new UE(input.getValueAt(j, 0).toString(), Integer.parseInt(input.getValueAt(j, 1).toString()), 
                                Integer.parseInt(input.getValueAt(j, 2).toString()), tempCap, tempVul, input.getValueAt(j, 5).toString());
                ueArray.add(ue);
                
                // Call admission and service functions
                result = AN(ue, core);

                System.out.println();
                // Print statement
                if (result) {
                    System.out.println("** UE "+ue.getIdS()+" has been admitted **");
                    System.out.println("** Bandwidth Allocated: "+ue.getBandInt()+" Mbps.");
                }
                else {
                    System.out.println("** UE "+ue.getIdS()+" has not been admitted **");
                    System.out.println("** Due to: "+ue.getReason()+".");
                }
            }
        }
    }

    public static boolean AN(UE ue, Core core) {
        return core.AMF(ue);
    }

    // helper function
    public static JTable CSVToJTable(String filename) {
        JTable table = new JTable();
        table.setName(filename);
        
        // Create a table model
        DefaultTableModel model = new DefaultTableModel();
        // Headers: UE,Priority,Callbar,Prempt_Capable,Prempt_Vul,AppType
        model.addColumn("UE"); // 0
        model.addColumn("Priority"); // 1
        model.addColumn("Callbar"); // 2
        model.addColumn("Prempt_Capable"); // 3
        model.addColumn("Prempt_Vul"); // 4
        model.addColumn("AppType"); // 5
        

        // Read CSV file and add rows to the table
        try {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            try {
                while ((line = br.readLine()) != null) {
                    String[] data = line.split(","); 
                    model.addRow(data);
                } 
            } catch(Exception e) {
                    e.printStackTrace();
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
