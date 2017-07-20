package com.onlinephotosubmission.csv_importer;

/**
 * Created by Brandon on 7/6/2017.
 */
class CardHolder {

    public static final String[] headerTypes = {"Email", "id", "Campus", "Notes"};

    private static Integer organizationId;

    private static int emailIndex;
    private static int idIndex;
    private static int campusIndex;
    private static int notesIndex;
    private String email;
    private String id;
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

    public void setDelimiter(String delimiter) {

        this.delimiter = delimiter;
    }

    String getEmail() {

        return email;
    }

    void setEmail(String inputEmail) {

        email = inputEmail;
    }

    public String getId() {

        return id;
    }

    void setId(String inputID) {

        id = inputID;
    }

    public String getCampus() {

        return campus;
    }

    void setCampus(String inputCampus) {

        campus = inputCampus;
    }

    public String getNotes() {

        return notes;
    }

    void setNotes(String inputNotes) {

        notes = inputNotes;
    }

    public static int getOrganizationId() {

        return organizationId;
    }

    public static void setOrganizationId(int organizationId) throws IllegalAccessException {

        if (CardHolder.organizationId != null) {
            throw new IllegalAccessException("Organization id can only be set once and never modified.");
        }

        CardHolder.organizationId = organizationId;
    }

    public static void setHeaderIndexes(String header) {

        String[] Header = header.split(Main.delimiter);
        for (int i = 0; i < Header.length; i++) {
            for (int j = 0; j < headerTypes.length; j++) {
                if (headerTypes[ j ].equals(Header[ i ])) {
                    if (i == 0) { setEmailIndex(j); }
                    if (i == 1) { setIdIndex(j); }
                    if (i == 2) { setCampusIndex(j); }
                    if (i == 3) { setNotesIndex(j); }
                }
            }
        }
    }

    public static String csvHeader() {

        String[] header = new String[ headerTypes.length ];
        header[ emailIndex ] = headerTypes[ 0 ];
        header[ idIndex ] = headerTypes[ 1 ];
        header[ campusIndex ] = headerTypes[ 2 ];
        header[ notesIndex ] = headerTypes[ 3 ];
        return String.join(", ", header);
    }

    public void parseInputString() {

        String[] cardHolderData = inputString.split(delimiter);

        email = cardHolderData[ emailIndex ];
        id = cardHolderData[ idIndex ];
        campus = cardHolderData[ campusIndex ];
        notes = cardHolderData[ notesIndex ];
    }

    public String toJSON() {

        String customFieldsAsJSON = "{" + "\"Campus\":\"" + campus + "\"," + "\"Notes\":\"" + notes + "\"}";
        return "{ \"email\":\"" + email + "\"," + "\"organization\":{\"id\":" + organizationId + "}," + "\"customFields\":" + customFieldsAsJSON + ", " + "\"identifier\":\"" + id + "\" }";
    }

    @Override
    public String toString() {

        return email + "," + id + "," + campus + "," + notes;
    }

    public boolean validate() {

        if (email.isEmpty() || id.isEmpty() || campus.isEmpty() || notes.isEmpty()) return false;
        else return true;
    }
}
