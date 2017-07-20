package com.onlinephotosubmission.csv_importer;

/**
 * Created by Brandon on 7/6/2017.
 */
class CardHolder {

    public static final String[] headerTypes = {"Email", "ORG_ID", "Campus", "Notes"};

    private static int emailIndex;
    private static int idIndex;
    private static int campusIndex;
    private static int notesIndex;
    private String email;
    private String ID;
    private String campus;
    private String notes;
    private String inputString;
    private String delimiter;

    CardHolder() {
    }

    CardHolder(String delimiter, String inputString) {
        this.inputString = inputString;
        this.delimiter = delimiter;
        this.parseInputString();
    }

    public static int getEmailIndex() {
        return emailIndex;
    }

    public static void setEmailIndex(int emailIndex) {

        CardHolder.emailIndex = emailIndex;
    }

    public static int getIdIndex() {
        return idIndex;
    }

    public static void setIdIndex(int idIndex) {
        CardHolder.idIndex = idIndex;
    }

    public static int getCampusIndex() {
        return campusIndex;
    }

    public static void setCampusIndex(int campusIndex) {
        CardHolder.campusIndex = campusIndex;
    }

    public static int getNotesIndex() {
        return notesIndex;
    }

    public static void setNotesIndex(int notesIndex) {
        CardHolder.notesIndex = notesIndex;
    }

    public void setDelimiter(String delimiter) {this.delimiter = delimiter;}

    String getEmail() { return email; }

    void setEmail(String inputEmail) {
        email = inputEmail;
    }

    public String getID() {
        return ID;
    }

    void setID(String inputID) {
        ID = inputID;
    }

    public String getCampus() { return campus; }

    void setCampus(String inputCampus) {
        campus = inputCampus;
    }

    public String getNotes() {
        return notes;
    }

    void setNotes(String inputNotes) {
        notes = inputNotes;
    }

    public void parseInputString() {
        String[] cardHolderData = inputString.split(delimiter);

        email = cardHolderData[emailIndex];
        ID = cardHolderData[idIndex];
        campus = cardHolderData[campusIndex];
        notes = cardHolderData[notesIndex];
    }

    @Override
    public String toString() {
        return email + "," + ID + "," + campus + "," + notes;
    }

    public boolean validate() {
        if (email.isEmpty() || ID.isEmpty() || campus.isEmpty() || notes.isEmpty())
            return false;
        else
            return true;
    }
}
