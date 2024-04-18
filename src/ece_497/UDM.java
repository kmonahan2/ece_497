package ece_497;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class UDM {

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

    // Getters
    public ArrayList<UE> getEntered_ue() {
        return this.entered_ue;
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
        printDB(general_bd, "general");
    }

    public void setData_bd(JTable data_bd) {
        this.data_bd = data_bd;
        printDB(data_bd, "data");
    }

    public void setVideo_bd(JTable video_bd) {
        this.video_bd = video_bd;
        printDB(videp_bd, "video");
    }

    public void setCount(int count) {
        this.count = count;
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

    void printDB(JTable table, String db) {
        // get the table model
        TableModel model = table.getModel();

        // print column headers
        for (int i = 0; i < model.getColumnCount(); i++) {
            System.out.print(model.getColumnName(i) + "\t");
        }
        // blank new line
        System.out.println();

        // print table data
        for (int row = 0; row < model.getRowCount(); row++) {
            for (int col = 0; col < model.getColumnCount(); col++) {
                System.out.print(model.getValueAt(row, col) + "\t");
            }
            // blank new line
            System.out.println();
        }
    }
}
