package com.onlinephotosubmission.csv_importer;

import java.util.Arrays;

/**
 * Created by Brandon on 7/6/2017.
 */
class CardHolder {

    private static String[] header;
    private static int supportingDocsRequiredIndex = -1;
    private static int emailGroupIndex = -1;

    private static final int EMAIL_INDEX = 0;
    private static final int ID_INDEX = 1;
    private static final String SUPPORTING_DOCS_REQD_HEADER = "SupportingDocumentsRequired";
    private static final String EMAIL_GROUP_HEADER = "EmailGroup";
    private String email;
    private String id;
    private String supportingDocsRequired;
    private String emailGroupName;
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

        Arrays.parallelSetAll(header, (i) -> header[ i ].trim());
        supportingDocsRequiredIndex = Arrays.asList(header).indexOf(SUPPORTING_DOCS_REQD_HEADER);
        emailGroupIndex = Arrays.asList(header).indexOf(EMAIL_GROUP_HEADER);
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
        if (emailGroupIndex >= 0) emailGroupName = fieldValues[ emailGroupIndex ];
    }

    public String toJSON() {

        return toJSON(false);
    }

    public String toJSON(boolean forUpdate) {

        StringBuilder json = new StringBuilder("{ \"email\":\"" + email + "\",");
        json.append(forUpdate || header.length <= 2 ? "" : "\"customFields\":");
        if (header.length > 2)
            json.append(getCustomFieldsAsJSON(forUpdate) + ", ");
        json.append("\"identifier\":\"" + id + "\"" + getSupportingDocsRequiredJSON() + getEmailGroupJSON() + " }");
        return json.toString();
    }

    private String getSupportingDocsRequiredJSON() {

        if (supportingDocsRequiredIndex < 0) return "";
        else return ", \"additionalPhotoRequired\":" + supportingDocsRequired;
    }

    private String getEmailGroupJSON() {

        if (emailGroupIndex < 0) return "";
        else return ", \"emailGroupName\":\"" + emailGroupName + "\"";
    }

    private String getCustomFieldsAsJSON(boolean forUpdate) {

        StringBuilder customFieldsAsJSON = new StringBuilder(forUpdate ? "" : "{");
        for (int i = 2; i < header.length; i++) {
            if (i == supportingDocsRequiredIndex || i == emailGroupIndex) continue;
            customFieldsAsJSON.append("\"" + header[ i ] + "\":\"" + fieldValues[ i ].replaceAll("\"", "") + "\",");
        }
        customFieldsAsJSON.deleteCharAt(customFieldsAsJSON.length() - 1);
        customFieldsAsJSON.append(forUpdate ? "" : "}");
        return customFieldsAsJSON.toString();
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
