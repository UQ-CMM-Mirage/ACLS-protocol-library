package au.edu.uq.cmm.aclslib.message;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * This the base class for the ACLS request and response reader classes.
 * It defines the tokenization regexes, and provides some simple parser 
 * utility methods.  
 * 
 * Tokenization is implemented using a Scanner, and some switching of 
 * the delimiter regexes.  This is approach is necessary because of 
 * the context sensitive nature of the message syntax.
 * 
 * @author scrawley
 */
public class AbstractReader {

    private static final Pattern TIMESTAMP_DELIMITERS = 
            Pattern.compile("(?=\\|)");
    private static final Pattern NOTE_DELIMITERS = 
            Pattern.compile("(?=[~|])");
    private static final Pattern ACCOUNT_DELIMITERS = 
            Pattern.compile("(?=[&\\];|])");
    private static final Pattern SYSTEM_PASSWORD_DELIMITERS = 
            Pattern.compile("(?=[/|])");
    private static final Pattern FACILITY_DELIMITERS = 
            Pattern.compile("(?=[?|])");
    private static final Pattern SUBFACILITY_DELIMITERS = 
            Pattern.compile("(?=[?;|])");
    private static final Pattern ORGANIZATION_DELIMITERS = 
            Pattern.compile("(?=[?|])");
    private static final Pattern PASSWORD_DELIMITERS = 
            Pattern.compile("(?=[/|])");
    private static final Pattern NAME_DELIMITERS = 
            Pattern.compile("(?=[/|])");
    private static final Pattern DRIVE_DELIMITERS = 
            Pattern.compile("(?=\\])");
    private static final Pattern FOLDER_DELIMITERS = 
            Pattern.compile("(?=\\[)");
    private static final Pattern ACCESS_DELIMITERS = 
            Pattern.compile("(?=\\~)");
    private static final Pattern ACCESS_PASSWORD_DELIMITERS = 
            Pattern.compile("(?=\\|)");
    static final Pattern DEFAULT_DELIMITERS = 
            Pattern.compile("(?<=[/:|\\[\\];~&?])|(?=[/:|\\[\\];~&?])");
    
    private Logger log;
    
    public AbstractReader(Logger log) {
        this.log = log;
    }
    
    protected final Scanner createLineScanner(BufferedReader source) {
        String line;
        try {
            line = source.readLine();
            if (line == null) {
                return new Scanner("");
            }
            log.debug("Raw request/response line is (" + line + ")");
            line += "\r\n";
        } catch (IOException ex) {
            log.error("Unexpected IO error while creating buffer", ex);
            throw new AssertionError("UTF-8 not supported");
        }
        Scanner scanner = new Scanner(line);
        scanner.useDelimiter(DEFAULT_DELIMITERS);
        return scanner;
    }

    protected void expect(Scanner source, String expected) {
        if (!source.hasNext()) {
            throw new MessageSyntaxException(
                    "Expected '" + expected + "' but got end-of-message");
        }
        expect(source.next(), expected);
    }

    protected void expect(String token, String expected) {
        if (!expected.equals(token)) {
            throw new MessageSyntaxException(
                    "Expected '" + expected + "' but got '" + token + "'");
        }
    }

    protected void expectEnd(Scanner source) {
        if (source.hasNext()) {
            String token = source.next().trim();
            if (token.equals(AbstractMessage.DELIMITER)) {
                token = source.next().trim();
            }
            // I don't understand why, but in some cases the "status" message seems to
            // be repeated at the end of a response.  The standard clients don't make 
            // any use of this (as far as I can tell) so I'm treating it as noise. 
            if (!token.isEmpty() & !token.equalsIgnoreCase(AbstractMessage.ACCEPTED_IP_TAG)) {
                throw new MessageSyntaxException(
                        "Unexpected token at end of message: '" + token);
            }
        }
    }
    
    protected String nextName(Scanner source) {
        return nextWithAltDelimiter(source, NAME_DELIMITERS);
    }
    
    protected String nextAccount(Scanner source) {
        return nextWithAltDelimiter(source, ACCOUNT_DELIMITERS);
    }
    
    protected String nextFacility(Scanner source) {
        return nextWithAltDelimiter(source, FACILITY_DELIMITERS);
    }
    
    protected String nextSubfacility(Scanner source) {
        return nextWithAltDelimiter(source, SUBFACILITY_DELIMITERS);
    }
    
    protected String nextOrganization(Scanner source) {
        return nextWithAltDelimiter(source, ORGANIZATION_DELIMITERS);
    }
    
    protected String nextNotes(Scanner source) {
        return nextWithAltDelimiter(source, NOTE_DELIMITERS);
    }
    
    protected String nextPassword(Scanner source) {
        return nextWithAltDelimiter(source, PASSWORD_DELIMITERS);
    }
    
    protected String nextSystemPassword(Scanner source) {
        return nextWithAltDelimiter(source, SYSTEM_PASSWORD_DELIMITERS);
    }
    
    protected String nextTimestamp(Scanner source) {
        return nextWithAltDelimiter(source, TIMESTAMP_DELIMITERS);
    }
    
    protected String nextDriveName(Scanner source) {
        return nextWithAltDelimiter(source, DRIVE_DELIMITERS);
    }
    
    protected String nextFolderName(Scanner source) {
        return nextWithAltDelimiter(source, FOLDER_DELIMITERS);
    }
    
    protected String nextAccessName(Scanner source) {
        return nextWithAltDelimiter(source, ACCESS_DELIMITERS);
    }
    
    protected String nextAccessPassword(Scanner source) {
        return nextWithAltDelimiter(source, ACCESS_PASSWORD_DELIMITERS);
    }
    
    private String nextWithAltDelimiter(Scanner source, Pattern altDelimiter) {
        Pattern delimiter = source.delimiter();
        try {
            source.useDelimiter(altDelimiter);
            return source.next();
        } finally {
            source.useDelimiter(delimiter);
        }
    }
}
