package com.onlinephotosubmission.csv_importer;

import java.util.Arrays;

/**
 * Created by Brandon on 7/6/2017.
 */
class CardHolder {

    private static String[] header;
    private static int supportingDocsRequiredIndex = -1;
    private static int cardholderGroupIndex = -1;
    private static int managerEmailIndex = -1;
    private static int managerIdentifierIndex = -1;
    private static int managerCardholderGroupNameIndex = -1;

    private static final int EMAIL_INDEX = 0;
    private static final int ID_INDEX = 1;
    private static final String SUPPORTING_DOCS_REQD_HEADER = "SupportingDocumentsRequired";
    private static final String EMAIL_GROUP_HEADER = "EmailGroup";
    private static final String CARDHOLDER_GROUP_HEADER = "CardholderGroup";
    private static final String CARDHOLDER_GROUP_NAME_HEADER = "CardholderGroupName";
    private static final String MANAGER_EMAIL_HEADER = "ManagerEmail";
    private static final String MANAGER_IDENTIFIER_HEADER = "ManagerIdentifier";
    private static final String MANAGER_CARDHOLDER_GROUP_NAME_HEADER = "ManagerCardholderGroupName";
    private String email;
    private String id;
    private String supportingDocsRequired;
    private String cardholderGroupName;
    private String managerEmail;
    private String managerIdentifier;
    private String managerCardholderGroupName;
    private String inputString;
    private String delimiter;
    String[] fieldValues;

    CardHolder() {

    }

    CardHolder(String delimiter, String inputString) {

        this.inputString = inputString;
        this.delimiter = delimiter;
        this.parseInputString();
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

    public static String[] getHeader() {

        return header;
    }

    public static void setHeader(String[] header) {

        Arrays.parallelSetAll(header, (i) -> header[ i ].replace("\"", "").trim());
        supportingDocsRequiredIndex = Arrays.asList(header).indexOf(SUPPORTING_DOCS_REQD_HEADER);
        cardholderGroupIndex = getCardholderGroupIndex(header);
        managerEmailIndex = Arrays.asList(header).indexOf(MANAGER_EMAIL_HEADER);
        managerIdentifierIndex = Arrays.asList(header).indexOf(MANAGER_IDENTIFIER_HEADER);
        managerCardholderGroupNameIndex = Arrays.asList(header).indexOf(MANAGER_CARDHOLDER_GROUP_NAME_HEADER);
        CardHolder.header = header;
    }

    public static String csvHeader() {

        return String.join(", ", header);
    }

    public void parseInputString() {

        fieldValues = inputString.split(delimiter);
        Arrays.parallelSetAll(fieldValues, (i) -> fieldValues[ i ].trim());

        fieldValues = stripQuotes(fieldValues);

        email = fieldValues[ EMAIL_INDEX ];
        id = fieldValues[ ID_INDEX ];
        if (supportingDocsRequiredIndex >= 0) supportingDocsRequired = fieldValues[ supportingDocsRequiredIndex ];
        if (cardholderGroupIndex >= 0) cardholderGroupName = fieldValues[cardholderGroupIndex];
        if (managerEmailIndex >= 0) managerEmail = fieldValues[ managerEmailIndex ];
        if (managerIdentifierIndex >= 0) managerIdentifier = fieldValues[ managerIdentifierIndex ];
        if (managerCardholderGroupNameIndex >= 0) managerCardholderGroupName = fieldValues[ managerCardholderGroupNameIndex ];
    }

    public String toJSON() {

        return toJSON(false);
    }

    public String toJSON(boolean forUpdate) {

        StringBuilder json = new StringBuilder("{ \"email\":\"" + email + "\",");
        json.append(forUpdate || !hasCustomFields() ? "" : "\"customFields\":");
        if (hasCustomFields())
            json.append(getCustomFieldsAsJSON(forUpdate) + ", ");
        json.append("\"identifier\":\"" + id + "\"" + getSupportingDocsRequiredJSON() + getCardholderGroupJSON() + getManagerEmailJSON() + getManagerIdentifierJSON() + " }");
        return json.toString();
    }

    private boolean hasCustomFields() {
        //there should always be at least 2 columns in the csv, email and identifier
        int customFieldColumns = header.length - 2;

        if (hasCardholderGroup()) customFieldColumns--;
        if (hasManagerEmail()) customFieldColumns--;
        if (hasSupportingDocsRequired()) customFieldColumns--;
        if (hasManagerIdentifier()) customFieldColumns--;
        if (hasManagerCardholderGroupName()) customFieldColumns--;

        return customFieldColumns > 0;
    }

    private boolean hasCardholderGroup() {

        return cardholderGroupIndex > 0;
    }

    private boolean hasManagerEmail() {

        return managerEmailIndex > 0;
    }

    private boolean hasManagerIdentifier() {

        return managerIdentifierIndex > 0;
    }

    private boolean hasManagerCardholderGroupName() {

        return managerCardholderGroupNameIndex > 0;
    }

    private boolean hasSupportingDocsRequired() {

        return supportingDocsRequiredIndex > 0;
    }

    private String getSupportingDocsRequiredJSON() {

        if (supportingDocsRequiredIndex < 0) return "";
        else return ", \"additionalPhotoRequired\":" + supportingDocsRequired;
    }

    private String getCardholderGroupJSON() {

        if (cardholderGroupIndex < 0) return "";
        else return ", \"cardholderGroupName\":\"" + cardholderGroupName + "\"";
    }

    private String getManagerEmailJSON() {

        if (managerEmailIndex < 0) return "";
        else return ", \"managerEmail\":\"" + managerEmail + "\"";
    }

    private String getManagerIdentifierJSON() {

        if (managerIdentifierIndex < 0) return "";
        else return ", \"managerIdentifier\":\"" + managerIdentifier + "\"";
    }

    private String getCustomFieldsAsJSON(boolean forUpdate) {

        StringBuilder customFieldsAsJSON = new StringBuilder(forUpdate ? "" : "{");
        for (int i = 2; i < header.length; i++) {
            if (i == supportingDocsRequiredIndex || i == cardholderGroupIndex || i == managerEmailIndex || i == managerIdentifierIndex) continue;
            customFieldsAsJSON.append("\"" + header[ i ] + "\":\"" + fieldValues[ i ].replaceAll("\"", "") + "\",");
        }
        customFieldsAsJSON.deleteCharAt(customFieldsAsJSON.length() - 1);
        customFieldsAsJSON.append(forUpdate ? "" : "}");
        return customFieldsAsJSON.toString();
    }

    private static int getCardholderGroupIndex(String[] header) {
        if (Arrays.asList(header).contains(CARDHOLDER_GROUP_NAME_HEADER)) {
            return Arrays.asList(header).indexOf(CARDHOLDER_GROUP_NAME_HEADER);
        } else if (Arrays.asList(header).contains(CARDHOLDER_GROUP_HEADER)) {
            return Arrays.asList(header).indexOf(CARDHOLDER_GROUP_HEADER);
        } else if (Arrays.asList(header).contains(EMAIL_GROUP_HEADER)) {
            return Arrays.asList(header).indexOf(EMAIL_GROUP_HEADER);
        } else return -1;
    }

    @Override
    public String toString() {

        return String.join(", ", fieldValues);
    }

    public boolean validate() {

        return (email.isEmpty() || id.isEmpty()) ? false : true;
    }

    private String[] stripQuotes(String[] stringArray) {

        for (int i = 0; i < stringArray.length; i++) {
            stringArray[ i ] = stripQuotes(stringArray[ i ]);
        }
        return stringArray;
    }

    private String stripQuotes(String s) {

        if (s.matches("^\".+\"$"))
            return s.substring(1, s.length() - 1);
        else
            return s;
    }
}
