package ece_497;

import java.util.*;

public class UE {
    // VARIABLES
    private String idS;
    private int idInt;
    private int priority;
    private boolean callbar;
    private boolean premtCapable;
    private boolean premtVul;
    private String appType = "G";
    private boolean admitted;
    private boolean known;
    private HashMap<String,Integer> band_allo;
    private String notes;
    private String reason;
    
    // CONSTRUCTOR
    public UE(String id, int priority, int callbar, boolean premtCapable, boolean premtVul, String appType) {
        this.idS = id;
        this.idInt = Integer.parseInt(id);
        this.priority = priority;
        if (callbar > 0) { this.callbar = true; }
        else { this.callbar = false; }
        this.premtCapable = premtCapable;
        this.premtVul = premtVul;
        this.appType = appType;
        this.admitted = false;
        this.known = false;
        HashMap<String, Integer> temp = new HashMap<String, Integer>();
        temp.put("N", 0);
        this.band_allo = temp;
        this.notes = "";
        this.reason = "";
    }

    // GETTERS/SETTERS
    public String getIdS() {
        return this.idS;
    }

    public int getIdInt() {
        return this.idInt;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isCallbar() {
        return callbar;
    }

    public void setCallbar(boolean callbar) {
        this.callbar = callbar;
    }

    public boolean isPremtCapable() {
        return premtCapable;
    }

    public void setPremtCapable(boolean premtCapable) {
        this.premtCapable = premtCapable;
    }

    public boolean isPremtVul() {
        return premtVul;
    }

    public void setPremtVul(boolean premtVul) {
        this.premtVul = premtVul;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public boolean isAdmitted() {
        return admitted;
    }

    public void setAdmitted(boolean admitted) {
        this.admitted = admitted;
    }

    public boolean isKnown() {
        return known;
    }

    public void setKnown(boolean known) {
        this.known = known;
    }
    
    public HashMap<String,Integer> getBandAllo() {
        return this.band_allo;
    }
    
    public void setBandAllo(String type, Integer amount) {
        this.band_allo.clear();
        this.band_allo.put(type, amount);

        // print statement?
    }

    public int getBandInt() {
        Set<String> type = this.band_allo.keySet();
        String t = null;
        Iterator<String> iterator = type.iterator();
        if (iterator.hasNext()) {
            t = iterator.next();
            return this.band_allo.get(t);
        }
        return 0;
    }

    public String getReason() {
        return this.reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
    // get kicked
    public void gotPremptUE(){
        this.admitted = false;
        this.reason = "UE was prempted due to capacity";
        this.setBandAllo("N", 0);   
        System.out.println();
        // print statement to user
        System.out.println("Premption Note:");
        System.out.println("** UE "+this.idS+" has been prempted due to capacity. **");
    }

    public String getNotes() {
        return this.notes;
    }

    public void addNotes(String notes) {
        this.notes = this.notes+notes;
    }
}
