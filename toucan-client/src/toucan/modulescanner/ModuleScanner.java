package toucan.modulescanner;

import java.rmi.Remote;

public interface ModuleScanner extends Remote{

    public final static int GFFSTRING = 1;
    public final static int LOCALGFFDBNAME = 2;
    public final static int LOCALEMBLDBPATH = 3;
    public final static int EMBLSTRING = 4;

  String run(String dbURL, String dbUser, String dbPasswd, String subject, int subjectType, String module, int size,boolean overlap,int cutOff, boolean penalizeShort, boolean takeLogOfInput) throws Exception;
}