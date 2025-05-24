/* This file is part of TOUCAN
 *
 * Copyright (C) 2001,2002,2003,2004 Stein Aerts, University of Leuven, Belgium.
 * Contact: <stein.aerts@esat.kuleuven.ac.be>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
package toucan.ext;

import org.apache.axis.client.Service;
import org.apache.axis.client.Call;
import org.apache.axis.encoding.XMLType;
import javax.xml.rpc.ParameterMode;
import java.net.URL;
import toucan.util.Tools;

/**
 * Title: SOAPClient
 * Description: communication with the ToucanSOAPService
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 2.0
 */

public class SOAPClient {

  /**MotifScanner**/
  public static String runMotifScanner(String soapServerUrl,String fasta, String mtrx, double prior, String bg, String list, int strand) throws Exception {
    Call call = buildCall("runMotifScanner",soapServerUrl);
    System.out.println("Parameters are:");
    call.addParameter("fasta", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\tfasta=" + fasta);
    call.addParameter("mtrx", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\tmtrx=" + mtrx);
    call.addParameter("prior", XMLType.XSD_DOUBLE, ParameterMode.IN); System.out.println("\tprior=" + prior);
    call.addParameter("bg", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\tbg=" + bg);
    call.addParameter("list", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\tlist=" + list);
    call.addParameter("strand", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\tstrand=" + strand);
    call.setReturnType(XMLType.XSD_STRING);
    String ret = (String)call.invoke(new Object[] {fasta,mtrx,new Double(prior),bg,list,new Integer(strand)});
    System.out.println("Return is:\n>>" + ret + "<<");
    return ret;
  }
  
  /**Clover**/
  public static String runClover(String soapServerUrl,String fasta, String mtrx, double threshold,int nrRand, String bg) throws Exception {
    Call call = buildCall("runClover",soapServerUrl);
    System.out.println("Parameters are:");
    call.addParameter("fasta", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\tfasta=" + fasta);
    call.addParameter("mtrx", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\tmtrx=" + mtrx);
    call.addParameter("threshold", XMLType.XSD_DOUBLE, ParameterMode.IN); System.out.println("\tthreshold=" + threshold);
    call.addParameter("nrRand", XMLType.XSD_DOUBLE, ParameterMode.IN); System.out.println("\tnrRand=" + nrRand);
    call.addParameter("bg", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\tbg=" + bg);
    call.setReturnType(XMLType.XSD_STRING);
    String ret = (String)call.invoke(new Object[] {fasta,mtrx,new Double(threshold),new Integer(nrRand),bg});
    System.out.println("Return is:\n>>" + ret + "<<");
    return ret;
  }


  /**MotifLocator**/
  public static String runMotifLocator(String soapServerUrl,String fasta, String mtrx, double threshold, String bg, String list, int strand) throws Exception {
    Call call = buildCall("runMotifLocator",soapServerUrl);
    System.out.println("Parameters are:");
    call.addParameter("fasta", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\tfasta=" + fasta);
    call.addParameter("mtrx", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\tmtrx=" + mtrx);
    call.addParameter("prior", XMLType.XSD_DOUBLE, ParameterMode.IN); System.out.println("\tthreshold=" + threshold);
    call.addParameter("bg", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\tbg=" + bg);
    call.addParameter("list", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\tlist=" + list);
    call.addParameter("strand", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\tstrand=" + strand);
    call.setReturnType(XMLType.XSD_STRING);
    String ret = (String)call.invoke(new Object[] {fasta,mtrx,new Double(threshold),bg,list,new Integer(strand)});
    System.out.println("Return is:\n>>" + ret + "<<");
    return ret;
  }

  /**MotifSampler**/
  public static String runMotifSampler(String soapServerUrl,String fasta, String bg, int strand,
                                double prior, int mnr, int len, int overlap,
                                int runs) throws Exception {
    Call call = buildCall("runMotifSampler",soapServerUrl);
    System.out.println("Parameters are:");
    call.addParameter("fasta", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\tfasta=" + fasta);
    call.addParameter("bg", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\tbg=" + bg);
    call.addParameter("strand", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\tstrand=" + strand);
    call.addParameter("prior", XMLType.XSD_DOUBLE, ParameterMode.IN); System.out.println("\tprior=" + prior);
    call.addParameter("mnr", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\tmnr=" + mnr);
    call.addParameter("len", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\tlen=" + len);
    call.addParameter("overlap", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\toverlap=" + overlap);
    call.addParameter("runs", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\truns=" +runs);
    call.setReturnType(XMLType.XSD_STRING);
    String ret = (String)call.invoke(new Object[] {fasta,bg,new Integer(strand),new Double(prior),new Integer(mnr),new Integer(len),new Integer(overlap),new Integer(runs)});
    System.out.println("Return is:\n>>" + ret + "<<");
    return ret;
  }

  /**ModuleSearcher**/
  public static String runModuleSearcher(String soapServerUrl, String fasta, int algorithm, int nrElements,
                                         int size, boolean overlap, boolean penalisation, String exclude, int nrModules) throws Exception {
      Call call = buildCall("runModuleSearcher",soapServerUrl);
      System.out.println("Parameters are:");
      call.addParameter("fasta", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\tfasta=" + fasta);
      call.addParameter("algorithm", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\talgorithm=" + algorithm);
      call.addParameter("nrElements", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\tnrElements=" + nrElements);
      call.addParameter("size", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\tsize=" + size);
      call.addParameter("overlap", XMLType.XSD_BOOLEAN, ParameterMode.IN); System.out.println("\toverlap=" + overlap);
      call.addParameter("penalisation", XMLType.XSD_BOOLEAN, ParameterMode.IN); System.out.println("\tpenalisation=" + penalisation);
      call.addParameter("exclude", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\texclude=" + exclude);
      call.addParameter("nrModules", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\tnrModules=" + nrModules);
      call.setReturnType(XMLType.XSD_STRING);
      String ret = (String)call.invoke(new Object[] {fasta,new Integer(algorithm),new Integer(nrElements),new Integer(size),new Boolean(overlap),new Boolean(penalisation),
                                       exclude,new Integer(nrModules)});
      System.out.println("Return is:\n>>" + ret + "<<");
      return ret;
    }

    /**ModuleScanner**/
  public static String runModuleScanner(String soapServerUrl,String subject, int subjectType, String module, int size,boolean overlap,int cutOff, boolean penalizeShort, boolean takeLogOfInput) throws Exception {
      Call call = buildCall("runModuleScanner",soapServerUrl);
      System.out.println("Parameters are:");
      call.addParameter("subject", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\tsubject=" + subject);
      call.addParameter("subjectType", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\tsubjectType=" + subjectType);
      call.addParameter("module", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\tmodule=" + module);
      call.addParameter("size", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\tsize=" + size);
      call.addParameter("overlap", XMLType.XSD_BOOLEAN, ParameterMode.IN); System.out.println("\toverlap=" + overlap);
      call.addParameter("cutOff", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\tcutOff=" + cutOff);
      call.addParameter("penalizeShort", XMLType.XSD_BOOLEAN, ParameterMode.IN); System.out.println("\tpenalizeShort=" + penalizeShort);
      call.addParameter("takeLogOfInput", XMLType.XSD_BOOLEAN, ParameterMode.IN); System.out.println("\ttakeLogOfInput=" + takeLogOfInput);
      call.setReturnType(XMLType.XSD_STRING);
      String ret = (String)call.invoke(new Object[] {subject,new Integer(subjectType),module,new Integer(size),new Boolean(overlap),new Integer(cutOff),new Boolean(penalizeShort),new Boolean(takeLogOfInput)});
      System.out.println("Return is:\n>>" + ret + "<<");
      return ret;
    }


    /**FootPrinter**/
    public static String runFootPrinter(String soapServerUrl,String fasta, String tree, String seq_type,
                                 int size, float max_mut,
                                 float max_mut_per_branch, int subreg_size,
                                 float subreg_change_cost, boolean triplet_filt,
                                 boolean pair_filt, boolean post_filt,
                                 float insdel_cost, float inv_cost) throws Exception {
      Call call = buildCall("runFootPrinter",soapServerUrl);
      System.out.println("Parameters are:");
      call.addParameter("fasta", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\tfasta=" + fasta);
      call.addParameter("tree", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\ttree=" + tree);
      call.addParameter("seq_type", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\t seq_type=" + seq_type);
      call.addParameter("size", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\tsize=" + size);
      call.addParameter("max_mut", XMLType.XSD_FLOAT, ParameterMode.IN); System.out.println("\t max_mut=" + max_mut);
      call.addParameter("max_mut_per_branch", XMLType.XSD_FLOAT, ParameterMode.IN); System.out.println("\t max_mut_per_branch=" + max_mut_per_branch);
      call.addParameter("subreg_size", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\t subreg_size=" + subreg_size);
      call.addParameter("subreg_change_cost", XMLType.XSD_FLOAT, ParameterMode.IN); System.out.println("\t subreg_change_cost=" + subreg_change_cost);
      call.addParameter("triplet_filt", XMLType.XSD_BOOLEAN, ParameterMode.IN); System.out.println("\t triplet_filt=" + triplet_filt);
      call.addParameter("pair_filt", XMLType.XSD_BOOLEAN, ParameterMode.IN); System.out.println("\t pair_filt=" + pair_filt);
      call.addParameter("post_filt", XMLType.XSD_BOOLEAN, ParameterMode.IN); System.out.println("\t post_filt=" + post_filt);
      call.addParameter("insdel_cost", XMLType.XSD_FLOAT, ParameterMode.IN); System.out.println("\t insdel_cost=" + insdel_cost);
      call.addParameter("inv_cost", XMLType.XSD_FLOAT, ParameterMode.IN); System.out.println("\t inv_cost=" + inv_cost);
      call.setReturnType(XMLType.XSD_STRING);
      String ret = (String)call.invoke(new Object[] {fasta,tree,seq_type, new Integer(size), new Float(max_mut),
                                 new Float(max_mut_per_branch), new Integer(subreg_size),
                                 new Float(subreg_change_cost), new Boolean(triplet_filt),
                                 new Boolean(pair_filt), new Boolean(post_filt),
                                 new Float(insdel_cost), new Float(inv_cost)});
      System.out.println("Return is:\n>>" + ret + "<<");
      return ret;
    }

    /**AVID/VISTA**/
    public static String runAvid(String soapServerUrl,String seq1, String seq2, int mcl, int wl, int rev,
                        String id1, String id2) throws Exception {
  Call call = buildCall("runAvid",soapServerUrl);
  System.out.println("Parameters are:");
  call.addParameter("seq1", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\t seq1=" + seq1);
  call.addParameter("seq2", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\t seq2=" + seq2);
  call.addParameter("mcl", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\t mcl=" + mcl);//minimal conservation level
  call.addParameter("wl", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\t wl=" + wl);//window length
  call.addParameter("rev", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\t rev=" + rev);
  call.addParameter("id1", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\t id1=" + id1);
  call.addParameter("id2", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\t id2=" + id2);
  call.setReturnType(XMLType.XSD_STRING);
  String ret = (String)call.invoke(new Object[] {seq1,seq2,new Integer(mcl),new Integer(wl),new Integer(rev),id1,id2});
  System.out.println("Return is:\n>>" + ret + "<<");
  return ret;
}

/**LAGAN+VISTA**/
    public static String runLagan(String soapServerUrl,String seq1, String seq2, int mcl, int wl, int rev,
                        String id1, String id2) throws Exception {
  Call call = buildCall("runLagan",soapServerUrl);
  System.out.println("Parameters are:");
  call.addParameter("seq1", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\t seq1=" + seq1);
  call.addParameter("seq2", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\t seq2=" + seq2);
  call.addParameter("mcl", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\t mcl=" + mcl);//minimal conservation level
  call.addParameter("wl", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\t wl=" + wl);//window length
  call.addParameter("rev", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\t rev=" + rev);
  call.addParameter("id1", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\t id1=" + id1);
  call.addParameter("id2", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\t id2=" + id2);
  call.setReturnType(XMLType.XSD_STRING);
  String ret = (String)call.invoke(new Object[] {seq1,seq2,new Integer(mcl),new Integer(wl),new Integer(rev),id1,id2});
  System.out.println("Return is:\n>>" + ret + "<<");
  return ret;
}

/**MLAGAN+VISTA**/
    public static String runMlagan(String soapServerUrl,String multiSeq, String tree, int mcl, int wl) throws Exception {
  Call call = buildCall("runMlagan",soapServerUrl);
  System.out.println("Parameters are:");
  call.addParameter("multiSeq", XMLType.XSD_STRING, ParameterMode.IN); //System.out.println("\t multiseq=" + multiSeq);
  call.addParameter("tree", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\t tree=" + tree);
  call.addParameter("mcl", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\t mcl=" + mcl);//minimal conservation level
  call.addParameter("wl", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\t wl=" + wl);//window length
  call.setReturnType(XMLType.XSD_STRING);
  String ret = (String)call.invoke(new Object[] {multiSeq,tree,new Integer(mcl),new Integer(wl)});
  System.out.println("Return is:\n>>" + ret + "<<");
  return ret;
}


/**BlASTZ+PIP**/
    public static String runBlastz(String soapServerUrl,String seq1, String seq2, int mcl, int wl, int rev,
                        String id1, String id2) throws Exception {
  Call call = buildCall("runBlastz",soapServerUrl);
  System.out.println("Parameters are:");
  call.addParameter("seq1", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\t seq1=" + seq1);
  call.addParameter("seq2", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\t seq2=" + seq2);
  call.addParameter("mcl", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\t mcl=" + mcl);//minimal conservation level
  call.addParameter("wl", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\t wl=" + wl);//window length
  call.addParameter("rev", XMLType.XSD_INT, ParameterMode.IN); System.out.println("\t rev=" + rev);
  call.addParameter("id1", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\t id1=" + id1);
  call.addParameter("id2", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\t id2=" + id2);
  call.setReturnType(XMLType.XSD_STRING);
  String ret = (String)call.invoke(new Object[] {seq1,seq2,new Integer(mcl),new Integer(wl),new Integer(rev),id1,id2});
  System.out.println("Return is:\n>>" + ret + "<<");
  return ret;
}

/**RepeatMasker**/
public static String runRepeatMasker(String soapServerUrl, String fasta, String species) throws Exception {
  Call call = buildCall("runRepeatMasker",soapServerUrl);
  System.out.println("Parameters are:");
  call.addParameter("fasta", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\tfasta=" + fasta);
  call.addParameter("species", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\tgff=" + species);
  call.setReturnType(XMLType.XSD_STRING);
  String ret = (String)call.invoke(new Object[] {fasta,species});
  System.out.println("Return is:\n>>" + ret + "<<");
  return ret;
}

  public static String getParams(boolean debug,String soapServerUrl) throws Exception {
    Call call = buildCall("getParamsFromFile",soapServerUrl);
    System.out.println("Parameters are:");
    call.addParameter("debug", XMLType.XSD_BOOLEAN, ParameterMode.IN); //System.out.println("\tdebug=" + debug);
    call.setReturnType(XMLType.XSD_STRING);
    String ret = (String)call.invoke(new Object[] {new Boolean(debug)});
    System.out.println("Return is:\n>>" + ret + "<<");
    return ret;
  }

  public static String getMotifNames(boolean debug,String soapServerUrl) throws Exception {
    Call call = buildCall("getMotifNamesFromFile",soapServerUrl);
    System.out.println("Parameters are:");
    call.addParameter("debug", XMLType.XSD_BOOLEAN, ParameterMode.IN); //System.out.println("\tdebug=" + debug);
    call.setReturnType(XMLType.XSD_STRING);
    String ret = (String)call.invoke(new Object[] {new Boolean(debug)});
    System.out.println("Return is:\n>>" + ret + "<<");
    return ret;
  }


  /**Query on database**/
  public static String[][] executeQuery(String dbDriver, String dbURL, String dbUser, String dbPassword, String dbQuery,String soapServerUrl) throws Exception {
    Call call = buildCall("executeQuery",soapServerUrl);
    System.out.println("Parameters are: ");
    call.addParameter("dbDriver", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\tdbDriver=" + dbDriver);
    call.addParameter("dbURL", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\tdbUrl=" + dbURL);
    call.addParameter("dbUser", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\tdbUser=" + dbUser);
    call.addParameter("dbPassword", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\tdbPassword=" + dbPassword);
    call.addParameter("dbQuery", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\tdbQuery=" + dbQuery);
    call.setReturnType(XMLType.XSD_STRING);
    String ret = (String)call.invoke(new Object[] {dbDriver, dbURL, dbUser, dbPassword, dbQuery});
    System.out.println("Return is:\n>>" + ret + "<<");
    return Tools.stringToArray2D(ret);
  }

  public static boolean isAvailable(String serviceName,String soapServerUrl) {
    try {
      Call call = buildCall("isAvailable",soapServerUrl);
      System.out.println("Parameters are: ");
      call.setReturnType(XMLType.XSD_BOOLEAN);
      Boolean ret = null;
      call.addParameter("serviceName", XMLType.XSD_STRING, ParameterMode.IN); System.out.println("\tserviceName: " + serviceName);
        ret = (Boolean) call.invoke(new Object[] {serviceName});
      System.out.println("Return is: " + ret);
      return ret.booleanValue();
    }
    catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  private static Call buildCall(String method,String soapServerUrl) throws Exception {
    System.out.println("\n");
    System.out.println("Building call for " + method);
    Service service = new Service();
    Call call = (Call) service.createCall();
    call.setTargetEndpointAddress(new URL(soapServerUrl));
    call.setOperationName(method);
    call.setTimeout(new Integer(18000000)); // time out after 5 hours of waiting
    return call;
  }
}
