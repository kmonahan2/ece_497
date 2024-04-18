package ece_497;
import java.util.*;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;


public class Core {

    // at the control plane, each function can call udm.
    private UDM udm = new UDM();

    // Constructor \\
    public Core() {
    }

    // AMF \\
    //     \\
    public boolean AMF (UE ue) {
        boolean admissionResult = false;
        
        admissionResult = AUSF(ue);
        // if admitted, try to allocated bandwidth
        if (admissionResult) {
            // Set up Session For Bandwidth
            SMF(ue);
            ue.setAdmitted(true);
        }

        return admissionResult;
    }

    // AUSF \\
    //      \\
    public boolean AUSF (UE ue) {
        // initalize local variables
        boolean authResult = false;
        boolean cb = false;
        int priority = 0;
        int ueId = ue.getId();
        ArrayList<UE> temp = udm.getEntered_ue();
        temp.add(ue);   
        udm.setEntered_ue(temp);

        // Allocation and Retention Policy
        HashMap<String, Integer> ARP = new HashMap<String, Integer>();
        ARP = PCF(ue);
        int capacity = ARP.get("capacity");
        int threshold = ARP.get("threshold");
        boolean premptCap = ue.isPremtCapable();

        int count = udm.getCount();
        
        for (int i = 0; i < udm.getKnown_ue().getRowCount(); i++) {
            if (ueId == Integer.parseInt(udm.getKnown_UECellValue(i,0).toString())){
                authResult = true;
                ue.setKnown(true);
                priority = ue.getPriority();
                cb = ue.isCallbar();
            }
        }
        
        if (!authResult) {
            // Handle unknown UE
            ue.setKnown(false);
            ue.setReason("UE is unkown");
        } else if (cb) {
            // Handle callbar UE
            authResult = false;
            ue.setReason("UE is callbarred");
        } else {
            // Handle authentication based on priority and count
            if (count >= capacity) {
                if (priority <= threshold) {
                    // Handle priority not meeting threshold at capacity
                    // Set notes accordingly
                    ue.setReason("priority did not meet threshold");
                    authResult = false;
                // if the UE is capable of premption,
                } else if (premptCap) {
                    int ueKickId = 0;
                    boolean flag = false;
                    for (int p = threshold; p < priority && flag == false; p++) {
                        // Look for a UE with a lower priority to kick
                        for (int k=1; k <= udm.getRegistered_ue().getRowCount(); k++) {
                            // if a registered ue is prempt vul, get
                            if(Boolean.parseBoolean(udm.getRegistered_UECellValue(k,5).toString())){
                                // check if priority is less than threshold
                                if (Integer.parseInt(udm.getRegistered_UECellValue(k, 2).toString())<=p) {
                                    // if so, store row index
                                    ueKickId = k;
                                    flag = true;
                                }

                            }
                        }
                    }
                
                    // reflect through entered_ue (thru udm in future), no need for note?
                    udm.getEntered_ue().get(ueKickId).gotPremptUE();
                    // remove entered ue from DB? add check in UDM
                }
            } else {
                // Handle authentication for UE within capacity
                // Update count, add UE to registered list
                JTable newReg = udm.JTableRegistered(ue);
                JTable tempReg = udm.getRegistered_ue();
                tempReg.add(newReg);
                udm.setRegistered_ue(tempReg);
                count++;
                udm.setCount(count);
            }
        }
        // update count in UDM
        udm.setCount(count);
        udm.updateDuration();
        return authResult;
    }

    // SMF \\
    //     \\
    public synchronized boolean SMF(UE ue) {
        boolean allocated_flag = false;
        String type = "N";
        boolean preCap = false;
        boolean preVul = false;
        HashMap<String, Integer> ARP = new HashMap<String, Integer>();

        // call pcf here
        ARP = PCF(ue);
        // return an hashmap of policy and allo?
        // set bandallo in pcf based on priority and app type
        int BANDALLO = 0;
        try {
            BANDALLO = ARP.get("gfbr");
        }  catch (Exception e) {
            e.printStackTrace();
        }
        // System.out.println("the band allo is: "+Integer.toString(BANDALLO));
        
        // Retrieve UE properties
        int ue_id = ue.getId();

        // retrieve temporary db variables from udm
        JTable knownUE = udm.getKnown_ue();
        ArrayList<UE> enteredUE = udm.getEntered_ue();
        JTable generalBD = udm.getGeneral_bd();
        int genWidth = udm.getGeneral_width();
        JTable dataBD = udm.getData_bd();
        int dataWidth = udm.getData_width();
        JTable videoBD = udm.getVideo_bd();
        int videoWidth = udm.getVideo_width();

        
        // Check UE properties based on known data
        for (int n = 0; n < knownUE.getRowCount(); n++) {
            if (ue_id == Integer.parseInt(knownUE.getValueAt(n,0).toString())) {
                type = (String)knownUE.getValueAt(n,5);
                // System.out.println("type: "+type);
                preCap = Boolean.parseBoolean(knownUE.getValueAt(n,3).toString());
                preVul = Boolean.parseBoolean(knownUE.getValueAt(n,4).toString());
            }
        }
        
        // Data, d
        if (type.equals("D")) {
            // If there is enough data bandwidth to allocate
            if (dataWidth >= BANDALLO) {
                // update the available data bandwidth available
                dataWidth -= BANDALLO;
                // update to UDM
                udm.setData_width(dataWidth);
                // give the bandwidth to the UE
                ue.setBandAllo("D", BANDALLO);
                // add entry in udm
                JTable tempDBD = makeRow(dataBD, ue);
                udm.setData_bd(tempDBD);
                // print the updated pipe
                udm.printDB(tempDBD, "Data");
                // mark ue as allocated
                allocated_flag = true;
            } 
            // If there is not sufficient bandwidth free 
            else {
                // If the UE is prempt capable
                if (preCap) {
                    // check through the current data pipe
                    for (int p = 0; p < udm.getData_bd().getRowCount(); p++) {
                        // if an existing entry is prempt vulnerable
                        if ((boolean)udm.getData_bd().getValueAt(p, 3)) {
                            enteredUE.get(p).gotPremptUE();
                            dataBD.setValueAt(ue_id, p,0);
                            dataBD.setValueAt(preVul,p,2);
                            dataBD.setValueAt(0, p, 5) ;
                            dataBD.setValueAt(BANDALLO, p, 4);
                            udm.setVideo_bd(videoBD);
                            ue.setBandAllo("D", BANDALLO);
                            allocated_flag = true;
                        }
                    }
                }
                // If the UE is not prempt capable 
                else {
                    if (genWidth >= 120) {
                        ue.setBandAllo("G",BANDALLO);
                        genWidth -= BANDALLO;
                        udm.setGeneral_width(genWidth);
                        JTable temp = udm.JTableBandwidth("ue_id", ue);
                        dataBD.add(temp);
                        udm.setData_bd(dataBD);
                        allocated_flag = true;
                    }
                }
            }
        } else if (type.equals("V")) {
            // Handle video type
            if (videoWidth >= 10) {
                ue.setBandAllo("V",BANDALLO);
                videoWidth -= BANDALLO;
                udm.setVideo_width(videoWidth);
                JTable tempVBD = makeRow(videoBD, ue);
                udm.setVideo_bd(tempVBD);
                allocated_flag = true;
            } else {
                if (preCap) {
                    for (int p = 0; p < videoBD.getRowCount(); p++) {
                        if ((boolean)videoBD.getValueAt(p,3) && !allocated_flag) {
                            enteredUE.get(p).gotPremptUE();
                            // remove from video_bd
                            videoBD.setValueAt(ue_id, p,0);
                            videoBD.setValueAt(preVul,p,2);
                            videoBD.setValueAt(0, p, 5) ;
                            videoBD.setValueAt(BANDALLO, p, 4);
                            udm.setVideo_bd(videoBD);
                            ue.setBandAllo("V",BANDALLO);
                            allocated_flag = true;
                        }
                    }
                } else {
                    // Handle the case when video bandwidth is insufficient
                    if (genWidth >= 120) {
                        ue.setBandAllo("G",BANDALLO);
                        genWidth -= BANDALLO;
                        udm.setGeneral_width(genWidth);
                        JTable tempGBD = makeRow(generalBD, ue);
                        udm.setGeneral_bd(tempGBD);
                        allocated_flag = true;
                    }
                }
            }
        } else if (type.equals("G")) {
            // Handle general type
            if (genWidth >= 10) {
                ue.setBandAllo("G",BANDALLO);
                genWidth -= BANDALLO;
                udm.setGeneral_width(genWidth);
                // add new entry to bandwidth 
                JTable tempGBD = makeRow(generalBD, ue);
                udm.setGeneral_bd(tempGBD);
                allocated_flag = true;
            } else {
                if (preCap) {
                    for (int p = 0; p < generalBD.getRowCount(); p++) {
                        // if someone in general is prempt vulnerable and the current ue is not allocated
                        if ((boolean)generalBD.getValueAt(p, 3) && !allocated_flag) {
                            enteredUE.get(p).gotPremptUE();
                            // remove from general
                            generalBD.setValueAt(ue_id, p,1);
                            generalBD.setValueAt(preVul,p,3);
                            generalBD.setValueAt(0, p, 5) ;
                            generalBD.setValueAt(BANDALLO, p, 4);
                            udm.setGeneral_bd(generalBD);
                            ue.setBandAllo("G",BANDALLO);
                            allocated_flag = true;

                        }
                    }
                }
            }
        } else {
            // Invalid Bandwidth Type Requested
            allocated_flag = false;
        }
        
        udm.updateDuration();
        return allocated_flag;
    }

    //  PCF \\
    //      \\
    // A-Type QoS profile, standard parameters for all UE's
    public synchronized HashMap<String, Integer> PCF(UE ue) {
        HashMap<String, Integer> ret = new HashMap<String, Integer>();
        int MAX_CAP = 20;
        int P_THRESHOLD = 5;
        int B_ALLO_STANDARD = 10;
        // Determine the capacity of the network
        int capacity = MAX_CAP;
        ret.put("capacity", capacity);
        // Determine the priority threshold of the network
        int priorityThreshold = P_THRESHOLD;
        ret.put("threshold", priorityThreshold);
        // Determine how many MBPS will be allocated to each UE
        // garanteed flow bit rate, just the standard
        int gfbr = B_ALLO_STANDARD;
        ret.put("gfbr", gfbr);
                
        return ret;
    }

    public JTable makeRow(JTable table, UE ue) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        String[] row = new String[6];
        row[0]=(Integer.toString(ue.getId()));
        row[1]=(Boolean.toString(ue.isPremtCapable()));
        row[2]=(Boolean.toString(ue.isPremtVul()));
        row[3]=(ue.getAppType());
        row[4]=(Integer.toString(ue.getBandInt()));
        row[5]=("0");
        model.addRow(row);
        table = new JTable(model);
        return table;
    }
}
