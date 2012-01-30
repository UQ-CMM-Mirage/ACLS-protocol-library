package au.edu.uq.cmm.aclslib.config;

/**
 * Configuration details API for a type of datafile to be grabbed
 * from a facility.
 * 
 * @author scrawley
 */
public interface DatafileTemplateConfig {

    /**
     * Get the regexes used to match the data file.
     */
    String getFilePattern();

    /**
     * The data file's notional mimeType
     */
    String getMimeType();

    /**
     * If true, the datafile is an optional member of the dataset
     */
    boolean isOptional();
}