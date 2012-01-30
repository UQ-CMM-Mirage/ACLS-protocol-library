package au.edu.uq.cmm.aclslib.config;

public class StaticDatafileTemplateConfig implements DatafileTemplateConfig {

    private boolean optional;
    private String mimeType;
    private String filePattern;
    
    public StaticDatafileTemplateConfig() {
        super();
    }

    public String getFilePattern() {
        return filePattern;
    }

    public String getMimeType() {
        return mimeType;
    }

    public boolean isOptional() {
        return optional;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setFilePattern(String filePattern) {
        this.filePattern = filePattern;
    }

}
