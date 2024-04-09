package ece_497;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public interface UDM {

    // my metaphorical UDR
    private static JTable known_ue;
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
        this.known_ue = Main.CSVToJTable("known_ue.csv");
        this.entered_ue = new ArrayList<UE>();
        this.registered_ue = new JTable();
        this.general_width = 500;
        this.data_width = 500;
        this.video_width = 500;
        this.general_bd = JTableBandwidth("general_bd", null);
        this.data_bd = JTableBandwidth("data_bd", null);
        this.video_bd = JTableBandwidth("video_bd", null);
        this.count = 0; 
    }

    public void writeToResource(String path, UE ue){

    }

    public Object readFromResource(String path, UE ue) {

    }

    // helper
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

    // helper
    public synchronized JTable JTableBandwidth(String name, UE ue) {
        JTable table = new JTable();
        table.setName(name);
        DefaultTableModel model = new DefaultTableModel();
        table = new JTable(model);
        // Headers: UE,Prempt_Capable,Prempt_Vul,AppType, Bandwidth, Duration
        model.addColumn("UE");
        model.addColumn("Prempt_Capable");
        model.addColumn("Prempt_Vul");
        model.addColumn("AppType");
        model.addColumn("Bandwidth");
        model.addColumn("Duration");

        // SET THE VALUE IN THE TABLE
        if(ue != null){
            String[] row = new String[6];
            row[0]=(Integer.toString(ue.getId()));
            row[1]=(Boolean.toString(ue.isPremtCapable()));
            row[2]=(Boolean.toString(ue.isPremtVul()));
            row[3]=(ue.getAppType());
            row[4]=(Integer.toString(ue.getBandInt()));
            row[5]=("0");
            model.addRow(row);
        }

        // build the jtable
        table = new JTable(model);
        JFrame frame = new JFrame();
        frame.add(new JScrollPane(table));


        return table;
    }

    // helper
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
            row[0]=(Integer.toString(ue.getId()));
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
}
