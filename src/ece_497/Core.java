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

        if (ue.isAdmitted()) {
            ue.setReason("UE is already admitted");
            return false;
        }
        
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
        int ueId = ue.getIdInt();
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
            ue.setReason("UE is unknown");
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
                    ue.setReason("priority did not meet threshold at capacity");
                    authResult = false;
                }
                // if the UE is capable of premption,
                if (premptCap) {
                    int ueKickId = 0;
                    boolean flag = false;
                    for (int p = threshold; p < priority && flag == false; p++) {
                        // Look for a UE with a lower priority to kick
                        for (int k=1; k <= udm.getRegistered_ue().getRowCount(); k++) {
                            // if an entry is prempt vulnerbale,
                            if(Integer.parseInt(udm.getRegistered_UECellValue(k,4).toString()) == 1){
                                // check if priority is less than threshold
                                if (Integer.parseInt(udm.getRegistered_UECellValue(k, 1).toString())<=threshold) {
                                    // if so, store row index
                                    ueKickId = k;
                                    flag = true;
                                }

                            }
                        }
                    }

                    if (flag == true){
                        // remove entered ue from DB? add check in UDM
                        if (udm.getEntered_ue().get(ueKickId).getBandInt() > 0) {
                            udm.removeEntry(udm.getEntered_ue().get(ueKickId));
                        } 
                        // notify prempted UE of kicking
                        udm.getEntered_ue().get(ueKickId).gotPremptUE();
                    }
                    
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
        int GEN_ALLO = 0;
        int DUR_MAX = 99;
        try {
            BANDALLO = ARP.get("gfbr");
            GEN_ALLO = ARP.get("gen_allo");
            DUR_MAX = ARP.get("dur_max");
        }  catch (Exception e) {
            e.printStackTrace();
        }
        // System.out.println("the band allo is: "+Integer.toString(BANDALLO));
        
        // Retrieve UE properties
        int ue_id = ue.getIdInt();

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
                if (Integer.parseInt(knownUE.getValueAt(n,3).toString()) == 1) {
                    preCap = true;
                } else {
                    preCap = false;
                }
                if (Integer.parseInt(knownUE.getValueAt(n,4).toString()) == 1) {
                    preVul = true;
                } else {
                    preVul = false;
                }
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
                // set allocated flag
                allocated_flag = true;
            } 
            // If there is not sufficient bandwidth free 
            else {
                // If the UE is prempt capable
                if (preCap) {
                    // check through the current data pipe
                    for (int p = 0; p < udm.getData_bd().getRowCount(); p++) {
                        // if an existing entry is prempt vulnerable and has a long enough duration
                        if ((udm.getData_bd().getValueAt(p, 2).toString() == "true") && ((Integer)dataBD.getValueAt(p,5) >= DUR_MAX)) {
                            // notify prempted UE, reset UE Object's values
                            int kickId = Integer.parseInt(dataBD.getValueAt(p,0).toString());
                            // notify prempted UE of kicking
                            udm.getEnteredUEInst(kickId).gotPremptUE();
                            // replace the values in the bandwidth pipe 
                            dataBD.setValueAt(ue_id, p,0);
                            dataBD.setValueAt(preVul,p,1);
                            dataBD.setValueAt(preVul,p,2);
                            dataBD.setValueAt(0, p, 5) ;
                            dataBD.setValueAt(BANDALLO, p, 4);
                            // update the data bandwidth pipe in udm
                            udm.setData_bd(dataBD);
                            // give the bandwidth to the UE
                            ue.setBandAllo("D", BANDALLO);
                            // set allocated flag
                            allocated_flag = true;
                            break;
                        }
                    }
                }
                // If the UE is not prempt capable 
                else {
                    // check if general data pipe has enough to spare
                    if (genWidth >= GEN_ALLO) {
                        // give UE the bandwidth
                        ue.setBandAllo("G",BANDALLO);
                        // update the general bandwidth
                        genWidth -= BANDALLO;
                        // update the availabe general width in the udm
                        udm.setGeneral_width(genWidth);
                       // add entry in udm
                        JTable tempGBD = makeRow(generalBD, ue);
                       // print the updated pipe
                        udm.printDB(tempGBD, "General");
                        // set allocated flag
                        allocated_flag = true;
                    }
                }
            }
            // For Video App 
        } else if (type.equals("V")) {
            // If there is sufficient bandwidth available
            if (videoWidth >= BANDALLO) {
                // update the available video bandwidth
                videoWidth -= BANDALLO;
                // update to UDM
                udm.setVideo_width(videoWidth);
                // give the bandwidth to the UE
                ue.setBandAllo("V", BANDALLO);
                // add entry in udm
                JTable tempVBD = makeRow(videoBD, ue);
                udm.setData_bd(tempVBD);
                // print the updated pipe
                udm.printDB(tempVBD, "Video");
                // set allocated flag
                allocated_flag = true;
            } else {
                // If the UE is Prempt Capable
                if (preCap) {
                    // check through the current video pipe
                    for (int p = 0; p < udm.getVideo_bd().getRowCount(); p++) {
                        // if an existing entry is prempt vulnerable and the duration is long enough
                        if (udm.getVideo_bd().getValueAt(p, 2) == "true" && (Integer)videoBD.getValueAt(p,5) > DUR_MAX) {
                            // notify prempted UE, reset UE Object's values
                            int kickId = Integer.parseInt(dataBD.getValueAt(p,0).toString());
                            // notify prempted UE of kicking
                            udm.getEnteredUEInst(kickId).gotPremptUE();
                            // replace the values in the bandwidth pipe 
                            videoBD.setValueAt(ue_id, p,0);
                            videoBD.setValueAt(preVul,p,1);
                            videoBD.setValueAt(preVul,p,2);
                            videoBD.setValueAt(0, p, 5) ;
                            videoBD.setValueAt(BANDALLO, p, 4);
                            // update the video bandwidth pipe in udm
                            udm.setVideo_bd(videoBD);
                            // give the bandwidth to the UE
                            ue.setBandAllo("V", BANDALLO);
                            // set allocated flag
                            allocated_flag = true;
                            break;
                        }
                    }
                // If the UE is not Prempt Capable
                } else {
                    // check if general data pipe has enough to spare
                    if (genWidth >= GEN_ALLO) {
                        // give UE the bandwidth
                        ue.setBandAllo("G",BANDALLO);
                        // update the general bandwidth
                        genWidth -= BANDALLO;
                        // update the availabe general width in the udm
                        udm.setGeneral_width(genWidth);
                       // add entry in udm
                        JTable tempGBD = makeRow(generalBD, ue);
                       // print the updated pipe
                        udm.printDB(tempGBD, "General");
                        // set allocated flag
                        allocated_flag = true;
                    }
                }
            }
        } else if (type.equals("G")) {
            // Handle general type
            if (genWidth >= BANDALLO) {
                // update the available general bandwidth available
                genWidth -= BANDALLO;
                // update to UDM
                udm.setGeneral_width(genWidth);
                // give the bandwidth to the UE
                ue.setBandAllo("G", BANDALLO);
                // add entry in udm
                JTable tempGBD = makeRow(generalBD, ue);
                udm.setGeneral_bd(tempGBD);
                // print the updated pipe
                udm.printDB(tempGBD, "General");
                // set allocated flag
                allocated_flag = true;
            } else {
                if (preCap) {
                    // check through the current video pipe
                    for (int p = 0; p < udm.getGeneral_bd().getRowCount(); p++) {
                        // if an existing entry is prempt vulnerable
                        if (udm.getGeneral_bd().getValueAt(p, 2) == "true") {
                            // notify prempted UE, reset UE Object's values
                            udm.getEntered_ue().get(p).gotPremptUE();
                            // replace the values in the bandwidth pipe 
                            generalBD.setValueAt(ue_id, p,0);
                            generalBD.setValueAt(preVul,p,1);
                            generalBD.setValueAt(preVul,p,2);
                            generalBD.setValueAt(0, p, 5) ;
                            generalBD.setValueAt(BANDALLO, p, 4);
                            // update the video bandwidth pipe in udm
                            udm.setGeneral_bd(generalBD);
                            // give the bandwidth to the UE
                            ue.setBandAllo("V", BANDALLO);
                            // set allocated flag
                            allocated_flag = true;
                            break;
                        }
                    }
                }
            }
        } else {
            // Invalid Bandwidth Type Requested
            allocated_flag = false;
        }
        
        // update duration, for kicking
        udm.updateDuration();
        return allocated_flag;
    }

    //  PCF \\
    //      \\
    // A-Type QoS profile, standard parameters for all UE's
    public synchronized HashMap<String, Integer> PCF(UE ue) {
        HashMap<String, Integer> ret = new HashMap<String, Integer>();
        // SET BY NETWORK OWNERS..
        int MAX_CAP = 20;
        int P_THRESHOLD = 2;
        int B_ALLO_STANDARD = 15;
        int GEN_B_ALLO = 10;
        int DUR_MAX = 25;
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
        int gen_allo = 0;
        // if high enough priority and not callbared, allow general bandwidth allocation
        if (ue.getPriority() > P_THRESHOLD && !ue.isCallbar()) {
            gen_allo = GEN_B_ALLO;
        }
        ret.put("gen_allo", gen_allo);
                
        ret.put("dur_max", DUR_MAX);

        return ret;
    }

    public JTable makeRow(JTable table, UE ue) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        String[] row = new String[6];
        row[0]=(ue.getIdS());
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
