package ece_497;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class Core {

    // at the control plane, each function can call udm.
    private final UserDataManagement udm;

    // Constructor \\
    public Core() {
        this.udm = new UDM();
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
        getEntered_ue.add(ue);

        // Allocation and Retention Policy
        HashMap<String, Object> ARP = new HashMap<String, Object>();
        ARP = PCF(ue);
        int capacity = ARP.get("capacity");
        int threshold = ARP.get("threshold");
        boolean premptCap = ARP.get("premptCap");
        boolean premptVul = ARP.get("premptVul");

        int count = getCount();
        
        for (int i = 0; i < getKnown_ue().getRowCount(); i++) {
            if (ueId == Integer.parseInt(getKnown_UECellValue(i,0).toString)){
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
                        for (int k=1; k <= getRegistered_ue().getRowCount(); k++) {
                            // if a registered ue is prempt vul, get
                            if(Boolean.parseBoolean(getRegistered_UECellValue(k,5).toString())){
                                // check if priority is less than threshold
                                if (Integer.parseInt(getRegistered_UECellValue(k, 2).toString())<=p) {
                                    // if so, store row index
                                    ueKickId = k;
                                    flag = true;
                                }

                            }
                        }
                    }
                
                    // reflect through entered_ue (thru udm in future), no need for note?
                    getEntered_ue().get(ueKickId).gotPremptUE();
                }
            } else {
                // Handle authentication for UE within capacity
                // Update count, add UE to registered list
                JTable newReg = JTableRegistered(ue);
                JTable temp = getRegistered_ue();
                temp.add(newReg);
                setRegistered_UE(temp);
                count++;
            }
        }
        // update count in UDM
        setCount(count);
        return authResult;
    }

    // SMF \\
    //     \\
    public synchronized boolean SMF(UE ue) {
        boolean allocated_flag = false;
        String type = "N";
        boolean preCap = false;
        boolean preVul = false;
        HashMap<String, Object> ARP = new HashMap<String, Object>();

        // call pcf here
        ARP = PCF(ue);
        // return an hashmap of policy and allo?
        // set bandallo in pcf based on priority and app type
        int BANDALLO = policy.get("band");
        // System.out.println("the band allo is: "+Integer.toString(BANDALLO));
        
        // Retrieve UE properties
        int ue_id = ue.getId();
        
        // Check UE properties based on known data
        for (int n = 0; n < known_ue.getRowCount(); n++) {
            if (ue_id == Integer.parseInt(known_ue.getValueAt(n,0).toString())) {
                type = (String)known_ue.getValueAt(n,5);
                // System.out.println("type: "+type);
                preCap = Boolean.parseBoolean(known_ue.getValueAt(n,3).toString());
                preVul = Boolean.parseBoolean(known_ue.getValueAt(n,4).toString());
            }
        }
        
        // Data, d
        if (type.equals("D")) {
            if (data_width >= BANDALLO) {
                data_width -= BANDALLO;
                ue.setBandAllo("D", BANDALLO);
                JTable temp = JTableBandwidth("ue_id", ue);
                getData_bd().add(temp);
                allocated_flag = true;
            } else {
                if (preCap) {
                    for (int p = 0; p < getData_bd().getRowCount(); p++) {
                        if ((boolean)data_bd.getValueAt(p, 3) && !allocated_flag) {
                            entered_ue.get(p).gotPremptUE();
                            video_bd.setValueAt(ue_id, p,0);
                            video_bd.setValueAt(preVul,p,2);
                            video_bd.setValueAt(0, p, 5) ;
                            video_bd.setValueAt(BANDALLO, p, 4);
                            ue.setBandAllo("D", BANDALLO);
                            allocated_flag = true;
                        }
                    }
                } else {
                    if (general_width >= 120) {
                        ue.setBandAllo("G",BANDALLO);
                        general_width -= BANDALLO;
                        JTable temp = JTableBandwidth("ue_id", ue);
                        data_bd.add(temp);
                        allocated_flag = true;
                    }
                }
            }
        } else if (type.equals("V")) {
            // Handle video type
            if (video_width >= 10) {
                ue.setBandAllo("V",BANDALLO);
                video_width -= BANDALLO;
                JTable temp = JTableBandwidth("ue_id", ue);
                video_bd.add(temp);
                allocated_flag = true;
            } else {
                if (preCap) {
                    for (int p = 0; p < video_bd.getRowCount(); p++) {
                        if ((boolean)video_bd.getValueAt(p,3) && !allocated_flag) {
                            entered_ue.get(p).gotPremptUE();
                            // remove from video_bd
                            video_bd.setValueAt(ue_id, p,0);
                            video_bd.setValueAt(preVul,p,2);
                            video_bd.setValueAt(0, p, 5) ;
                            video_bd.setValueAt(BANDALLO, p, 4);
                            ue.setBandAllo("V",BANDALLO);
                            allocated_flag = true;
                        }
                    }
                } else {
                    // Handle the case when video bandwidth is insufficient
                    if (general_width >= 120) {
                        ue.setBandAllo("G",BANDALLO);
                        general_width -= BANDALLO;
                        JTable temp = JTableBandwidth("ue_id", ue);
                        general_bd.add(temp);
                        allocated_flag = true;
                    }
                }
            }
        } else if (type.equals("G")) {
            // Handle general type
            if (general_width >= 10) {
                ue.setBandAllo("G",BANDALLO);
                general_width -= BANDALLO;
                JTable temp = JTableBandwidth("ue_id", ue);
                general_bd.add(temp);
                allocated_flag = true;
            } else {
                if (preCap) {
                    for (int p = 0; p < general_bd.getRowCount(); p++) {
                        // if someone in general is prempt vulnerable and the current ue is not allocated
                        if ((boolean)general_bd.getValueAt(p, 3) && !allocated_flag) {
                            entered_ue.get(p).gotPremptUE();
                            // remove from general
                            general_bd.setValueAt(ue_id, p,1);
                            general_bd.setValueAt(preVul,p,3);
                            general_bd.setValueAt(0, p, 5) ;
                            general_bd.setValueAt(BANDALLO, p, 4);
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
        
        updateDuration();
        return allocated_flag;
    }

    //  PCF \\
    //      \\
    // A-Type QoS profile, standard parameters for all UE's
    public synchronized HashMap<String, Object> PCF(UE ue) {
        HashMap<String, Integer> ret = new HashMap<String, Integer>();
        int MAX_CAP = 20;
        int P_THRESHOLD = 5;
        int B_ALLO_STANDARD = 10;
        // add prempts 
        boolean premptCap = ue.isPremtCapable();
        boolean premptVul = ue.isPremtVul();

        // Determine the capacity of the network
        int capacity = MAX_CAP;
        ret.put("capacity", capacity);
        // Determine the priority threshold of the network
        int priorityThreshold = P_THRESHOLD;
        ret.put("threshold", priorityThreshold);
        // Determine how many MBPS will be allocated to each UE
        // garanteed flow bit rate, just the standard
        int gfbr = B_ALLO_STANDARD;
        // maximum flow bit rate, based on current bandwidth open and 
        int mfbr = 
        ret.put("gfbr", grbr);
        ret.put("mfbr", mfbr);
                
        return ret;
    }
}
