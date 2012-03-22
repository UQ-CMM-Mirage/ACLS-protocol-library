package au.edu.uq.cmm.aclslib.message;

/**
 * The base interface for ACLS message classes.  It should be noted that 
 * the use-cases for these classes mean that we need to reflect some 
 * aspects of the protocol that are not best-practice in protocol design.
 * We had to choose between clean modeling and lots of complex code to 
 * map from the model to reality (and back), or an unclean (leaky) model.
 * We chose the latter, in the anticipation that the protocol will be
 * redesigned from the ground up at some point.
 * 
 * @author scrawley
 */
public interface Message {
    
    /**
     * Turn this Message into a textual ACLS message.
     * 
     * @param obscurePasswords if true, replace any password field with some XXX's.
     * @return the message in text form.
     */
    String unparse(boolean obscurePasswords);
}
