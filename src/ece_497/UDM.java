package ece_497;
import java.util.*;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;


import java.io.BufferedReader;
import java.io.FileReader;


public class UDM {

    // my metaphorical UDR
    private JTable known_ue;
    private ArrayList<UE> entered_ue;
    private JTable registered_ue;
    private int general_width;
    private int data_width;
    private int video_width;
    private JTable general_bd;
    private JTable data_bd;
    private JTable video_bd;
    private int count;

    public UDM() {
        // starting variables
        this.known_ue = CSVToJTable("KnownUE.csv");
        this.entered_ue = new ArrayList<UE>();
        this.registered_ue = new JTable();
        this.general_width = 150;
        this.data_width = 300;
        this.video_width = 150;
        this.general_bd = JTableBandwidth("general_bd");
        this.data_bd = JTableBandwidth("data_bd");
        this.video_bd = JTableBandwidth("video_bd");
        this.count = 0;
    }

    // Getters
    public ArrayList<UE> getEntered_ue() {
        return this.entered_ue;
    }

    public UE getEnteredUEInst(int ue_id) {
        UE ret = null;

        for (int i=0; i<this.entered_ue.size(); i++) {
            if (this.entered_ue.get(i).getIdInt() == ue_id){
                ret = this.entered_ue.get(i);
            }
        }

        return ret; 
    }

    public JTable getRegistered_ue() {
        return this.registered_ue;
    }

    public JTable getKnown_ue() {
        return this.known_ue;
    }

    public Object getRegistered_UECellValue(int row, int column) {
        return this.registered_ue.getValueAt(row, column);
    }

    public Object getKnown_UECellValue(int row, int column) {
        return this.known_ue.getValueAt(row, column);
    }
    
    public int getGeneral_width() {
        return this.general_width;
    }

    public int getData_width() {
        return this.data_width;
    }

    public int getVideo_width() {
        return this.video_width;
    }

    public JTable getGeneral_bd() {
        return this.general_bd;
    }

    public JTable getData_bd() {
        return this.data_bd;
    }

    public JTable getVideo_bd() {
        return this.video_bd;
    }

    public int getCount() {
        return this.count;
    }

    // Setters
    public void setEntered_ue(ArrayList<UE> entered_ue) {
        this.entered_ue = entered_ue;
    }

    public void setRegistered_ue(JTable regUE) {
        this.registered_ue = regUE;
    }

    public void setRegistered_UECellValue(Object value, int row, int column) {
        this.registered_ue.setValueAt(value, row, column);
    }

    public void setKnown_UECellValue(Object value, int row, int column) {
        this.known_ue.setValueAt(value, row, column);
    }

    public void setGeneral_width(int general_width) {
        this.general_width = general_width;
    }

    public void setData_width(int data_width) {
        this.data_width = data_width;
    }

    public void setVideo_width(int video_width) {
        this.video_width = video_width;
    }

    public void setGeneral_bd(JTable general_bd) {
        this.general_bd = general_bd;
    }

    public void setData_bd(JTable data_bd) {
        this.data_bd = data_bd;
    }

    public void setVideo_bd(JTable video_bd) {
        this.video_bd = video_bd;
    }

    public void setCount(int count) {
        this.count = count;
    }


    public synchronized void removeEntry(UE ue) {
        switch(ue.getBandChar()) {
            case "D":
                for (int i = 0; i < data_bd.getRowCount(); i++) {
                    if (data_bd.getValueAt(i,0) == ue.getIdS()) {
                        ((DefaultTableModel) data_bd.getModel()).removeRow(i);
                    }
                }
                break;
            case "V":
                for (int i = 0; i < video_bd.getRowCount(); i++) {
                    if (video_bd.getValueAt(i,0) == ue.getIdS()) {
                        ((DefaultTableModel) video_bd.getModel()).removeRow(i);
                    }
                }
                break;
            case "G":
                for (int i = 0; i < general_bd.getRowCount(); i++) {
                    if (general_bd.getValueAt(i,0) == ue.getIdS()) {
                        ((DefaultTableModel) general_bd.getModel()).removeRow(i);
                    }
                }
                break;
        }
    }

    // helper function
    public synchronized void updateDuration() {
        for (int i = 0; i < general_bd.getRowCount(); i++) {
            int temp = 0;
            temp = Integer.parseInt(general_bd.getValueAt(i,5).toString());
            temp++;
            general_bd.setValueAt(temp, i, 5);
        }
        for (int i = 0; i < video_bd.getRowCount(); i++) {
            int temp = 0;
            temp = Integer.parseInt(video_bd.getValueAt(i,5).toString());
            temp++;
            video_bd.setValueAt(temp, i, 5);
        }
        for (int i = 0; i < data_bd.getRowCount(); i++) {
            int temp = 0;
            temp = Integer.parseInt(data_bd.getValueAt(i,5).toString());
            temp++;
            data_bd.setValueAt(temp, i, 5);
        }

    }

    // helper function
    public JTable CSVToJTable(String filename) {
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
            while ((line = br.readLine()) != null) {
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

    // helper function
    public synchronized JTable JTableBandwidth(String name) {
        JTable table;
        DefaultTableModel model = new DefaultTableModel();
        table = new JTable(model);
        table.setName(name);
        // Headers: UE,Prempt_Capable,Prempt_Vul,AppType, Bandwidth, Duration
        model.addColumn("UE");
        model.addColumn("Prempt_Capable");
        model.addColumn("Prempt_Vul");
        model.addColumn("AppType");
        model.addColumn("Bandwidth");
        model.addColumn("Duration");

        JFrame frame = new JFrame();
        frame.add(new JScrollPane(table));
        return table;
    }

    // helper function
    public synchronized JTable JTableRegistered(UE ue) {
        JTable table = new JTable();
        // Create a table model
        DefaultTableModel model = new DefaultTableModel();
        // Headers: UE,Priority,Callbar,Prempt_Capable,Prempt_Vul,AppType
        model.addColumn("UE");
        model.addColumn("Priority");
        model.addColumn("Callbar");
        model.addColumn("Prempt_Capable");
        model.addColumn("Prempt_Vul");
        model.addColumn("AppType");

        if(ue != null){
            String[] row = new String[6];
            row[0]=(ue.getIdS());
            row[1]=(Integer.toString(ue.getPriority()));
            row[2]=(Boolean.toString(ue.isCallbar()));
            row[3]=(Boolean.toString(ue.isPremtCapable()));
            row[4]=(Boolean.toString(ue.isPremtVul()));
            row[5]=(ue.getAppType());
            model.addRow(row);
        }
        // build the jtable
        table = new JTable(model);
        JFrame frame = new JFrame();
        frame.add(new JScrollPane(table));

        return table;

    }

    void printDB(JTable table, String db) {
        // get the table model
        TableModel model = table.getModel();

        System.out.println();
        System.out.println("\t* "+db+" Pipe *");
        System.out.print("\t");
        // print column headers
        for (int i = 0; i < model.getColumnCount(); i++) {
            if (i == 3) {}
                else {
                    System.out.print(model.getColumnName(i) + "\t\t");
                }
        }
        System.out.println();

        // print table data
        for (int row = 0; row < model.getRowCount(); row++) {
            System.out.print("| ");
            String temp = "\t\t";
            for (int col = 0; col < model.getColumnCount(); col++) {
                if (col >= (model.getColumnCount()-1)) { temp = ""; }
                if (col == 3) {} // skip printing apptype
                else {
                    System.out.print(String.valueOf(table.getValueAt(row, col))+ temp);
                }
               temp = temp + "\t";
            }
            System.out.print("|");
            // blank new line
            System.out.println();
        }
    }
}
