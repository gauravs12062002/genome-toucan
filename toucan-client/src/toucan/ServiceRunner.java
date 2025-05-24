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
package toucan;

import toucan.util.GlobalProperties;
import javax.swing.JOptionPane;
import toucan.gui.MainFrame;
import toucan.ext.SOAPClient;
import javax.swing.JDialog;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import javax.swing.JFileChooser;
import toucan.util.SomeFileFilter;
import toucan.modulescanner.ModuleScanner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import toucan.util.XMLParser;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Vector;
import org.w3c.dom.NamedNodeMap;

import RSATWS.OligoAnalysisRequest;
import RSATWS.OligoAnalysisResponse;
import RSATWS.PurgeSequenceRequest;
import RSATWS.PurgeSequenceResponse;
import RSATWS.RSATWSPortTypeProxy;

/**
 * Title: TOUCAN
 * Description: Integrated workbench for cis-regulatory sequence analysis
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 2.0
 */

public class ServiceRunner
    extends Thread {

  public static final int MOTIFSCANNER = 1;
  public static final int MOTIFLOCATOR = 2;
  public static final int MOTIFSAMPLER = 3;
  public static final int MODULESEARCHER = 4;
  public static final int AVID = 5;
  public static final int FOOTPRINTER = 6;
  public static final int LAGAN = 7;
  public static final int BLASTZ = 8;
  public static final int MLAGAN = 9;
  public static final int MODULESCANNER = 10;
  public static final int REPEATMASKER = 11;
  public static final int OLIGOANALYSIS = 12;
  public static final int CLOVER = 13;
  

  private int serviceToRun;
  private MainFrame owner;

  //all services
  private String fastaStr;
  private String soapServerUrl;
  private String[] soapServerUrls;

  //MotifScanner & MotifLocator & MotifSampler parameters
  private double prior = 0.2d;
  private int strand = 1;
  private String bg = null;

 //MotifLocator
  private double threshold = 0.9;

  //MotifScanner & MotifLocator parameters
  private String mtrx = null;
  private String list = "";
  private String returnedFASTA;
  private String returnedGFF;
  private String returnedMTRX;
  private String returnedXML;

  //MotifSampler parameters
  private int motifLength = 8;
  private int nrMotifs = 4;
  private int motifOverlap = 1;
  private int nrRuns = 1;
  
  //oligo-analysis parameters
  private String oaOrg;
  private String oaBg;
  private int oaLength;
  private int oaOverlap;
  private int oaStrand;
  private int oaMinSig;
  
  //clover parameters
  private double cthreshold=0.01;
  private int nrRand=10;

  //ModuleSearcher parameters
  private String exclude, GFFStr;
  private int nrElements = 5, size = 200, algorithm = 0, nrModules = 1;
  private boolean overlap = false, penalisation = false;

  //FootPrinter parameters
  private String tree, seq_type;
  private int subreg_size;
  private boolean triplet_filt, pair_filt, post_filt;
  private float max_mut, max_mut_per_branch, subreg_change_cost, insdel_cost,
      inv_cost;

  //AVID parameters
  private String seq1, seq2, id1, id2;
  private int mcl = 75, wl = 100, rev = 0;

  //ModuleScanner parameters
  int subjectType;
  private String subject;
  private String returnedStr;
  private boolean penalizeShort = false;
  private boolean takeLogOfInput = true;
  private String module;
  private int cutOff = 20;

  //RepeatMasker parameters
  private String species;
  private boolean replaceWithNs = false;

  //params
  //public HashMap bgParams, mtrxParams, dbParams;
  //public Vector motifNames;
  //public boolean gotParams = false, gotMotifNames=false;
  //public Document paramXml;


  private boolean confirmAnnot = true;

  public ServiceRunner(MainFrame mFrame, int service) {
    serviceToRun = service;
    owner = mFrame;
  }

  public void run() {
    try {
      if (serviceToRun == MOTIFSCANNER) {
        if (mtrx == null || bg == null) {
          throw new Exception(
              "Unable to run MotifScanner: Matrix and Background model should be given");
        }
        returnedGFF = SOAPClient.runMotifScanner(soapServerUrl, fastaStr,
              mtrx,
              prior, bg,
              list, strand);
        handleReturnedGFF();
      }
      if (serviceToRun == MOTIFLOCATOR) {
        if (mtrx == null || bg == null) {
          throw new Exception(
              "Unable to run MotifLocator: Matrix and Background model should be given");
        }
        returnedGFF = SOAPClient.runMotifLocator(soapServerUrl, fastaStr, mtrx,
                                                 threshold, bg,
                                                 list, strand);
        
        handleReturnedGFF();
      }
      if (serviceToRun == CLOVER) {
          if (mtrx == null || bg == null) {
            throw new Exception(
                "Unable to run Clover: Matrix and Background sequences should be given");
          }
          returnedGFF = SOAPClient.runClover(soapServerUrl, fastaStr, mtrx, cthreshold,nrRand, bg);                                                   
          
          handleReturnedGFF();
      }
      if (serviceToRun == MODULESEARCHER) {
        returnedGFF = SOAPClient.runModuleSearcher(soapServerUrl, GFFStr, algorithm, nrElements, size, overlap, penalisation, exclude, nrModules);
        handleReturnedGFF();
      }
      if (serviceToRun == MODULESCANNER) {
        returnedXML = SOAPClient.runModuleScanner(soapServerUrl, subject,
                                                  subjectType, module, size,
                                                  overlap, cutOff,
                                                  penalizeShort, takeLogOfInput);
        if(returnedXML == null){
          throw new Exception("No result was returned. Either the server is down, or this combination has no hits");
        }
        if (subjectType == ModuleScanner.EMBLSTRING ||
            subjectType == ModuleScanner.LOCALEMBLDBPATH) { //returned type is EMBL
          this.handleReturnedGffFasta(); //parses returnedXML, sets returnedGFF and returnedFASTA
          if (owner.gl != null && owner.gl.list.size() > 0) {
            MainFrame f = owner.newWindow();
            f.gl = new GeneList();
            //f.gl.constructFromLargeEmblStr(returnedStr);
            f.gl.constructFromLargeFastaString(returnedFASTA);
            f.gl.addGffFromString(returnedGFF);
            f.setImg();
          }
          else {
            owner.gl = new GeneList();
            //owner.gl.constructFromLargeEmblStr(returnedStr);
            owner.gl.constructFromLargeFastaString(returnedFASTA);
            owner.gl.addGffFromString(returnedGFF);
            owner.setImg();
          }
        }
      }
      
      if (serviceToRun == FOOTPRINTER) {
        returnedGFF = SOAPClient.runFootPrinter(soapServerUrl, fastaStr, tree,
                                                seq_type, size, max_mut,
                                                max_mut_per_branch, subreg_size,
                                                subreg_change_cost,
                                                triplet_filt, pair_filt,
                                                post_filt, insdel_cost,
                                                inv_cost);
        handleReturnedGFF();
        tree = "";
      }
      if (serviceToRun == AVID) {
        returnedGFF = SOAPClient.runAvid(soapServerUrl, seq1, seq2, mcl, wl,
                                         rev, id1, id2);
        handleReturnedGFF();
      }
      if (serviceToRun == LAGAN) {
        returnedGFF = SOAPClient.runLagan(soapServerUrl, seq1, seq2, mcl, wl,
                                          rev, id1, id2);
        handleReturnedGFF();
      }
      if (serviceToRun == BLASTZ) {
        System.out.println("server="+soapServerUrl);
        returnedGFF = SOAPClient.runBlastz(soapServerUrl, seq1, seq2, mcl, wl,
                                           rev, id1, id2);
        handleReturnedGFF();
      }
      if (serviceToRun == MLAGAN) {
        returnedGFF = SOAPClient.runMlagan(soapServerUrl, fastaStr, tree, mcl, wl).replaceAll("stompunt",".");
      //multiSeq needs to be set in fastaStr
        //if(returnedGFF != null) returnedGFF=returnedGFF.replace('-','.');
        handleReturnedGFF();
      }
      else if (serviceToRun == MOTIFSAMPLER) {
        if (bg == null) {
          throw new Exception(
              "Unable to run MotifSampler: Background model should be given");
        }
        returnedXML = SOAPClient.runMotifSampler(soapServerUrl, fastaStr, bg,
                                                 strand, prior,
                                                 nrMotifs, motifLength,
                                                 motifOverlap, nrRuns);
        handleReturnedXML(); //GFF + MTRX
      }
      else if (serviceToRun == OLIGOANALYSIS) {
          if (oaOrg == null) {
            throw new Exception(
                "Unable to run oligo-analysis: organism should be given");
          }
          RSATWSPortTypeProxy proxy = new RSATWSPortTypeProxy();
          PurgeSequenceRequest purgeSeqParams = new PurgeSequenceRequest();
          // The result will stay in a file on the server
          purgeSeqParams.setOutput("server");
          // Output from retrieve-seq part is used as input here
          //purgeSeqParams.setTmp_infile(retrieveSeqFileServer);
          purgeSeqParams.setSequence(fastaStr);
          /* Call the service */
          System.out.println("Purge-sequence: sending request to the server...");
          PurgeSequenceResponse res2 = proxy.purge_seq(purgeSeqParams);
          /* Process results  */
          //Report the remote command
          System.out.println("Command used on the server:"+ res2.getCommand());
          //Report the server file location
          String purgeSeqFileServer = res2.getServer();
          System.out.println("Result file on the server::\n"+ res2.getServer());
          /** Oligo-analysis part **/
          /* prepare the parameters */
          OligoAnalysisRequest oligoParams = new OligoAnalysisRequest();
          // Output from purge-seq part is used as input here
          oligoParams.setTmp_infile(purgeSeqFileServer);
          oligoParams.setOrganism(oaOrg);
          // Length of patterns to be discovered
          oligoParams.setLength(oaLength);
          // Type of background used
          oligoParams.setBackground("upstream-noorf");
          // Returned statistics
          oligoParams.setStats("occ,proba,rank");
          // Do not allow overlapping patterns
          oligoParams.setNoov(new Integer(oaOverlap));
          // Search on both strands
          oligoParams.setStr(new Integer(oaStrand));
          System.out.println("strand="+oaStrand);
          // Sort the result according to score
          oligoParams.setSort(new Integer(1));
          // Lower limit to score is 0, less significant patterns are not displayed
          oligoParams.setLth("occ_sig "+oaMinSig);
          /* Call the service */
          System.out.println("Oligo-analysis: sending request to the server...");
          owner.writeToStatus("Oligo-analysis: waiting for results from RSAT server...");
          OligoAnalysisResponse res3 = proxy.oligo_analysis(oligoParams);
          owner.writeToStatus("Oligo-analysis done.");
          /* Process results  */
          //Report the remote command
          System.out.println("Command used on the server:"+ res3.getCommand());
          //Report the result
          System.out.println("Discovered oligo(s):\n"+ res3.getClient());
          if(res3.getClient() == null || res3.getClient().equalsIgnoreCase("")){
        	  	JOptionPane.showMessageDialog(owner,"No over-represented oligos found.","Info",JOptionPane.INFORMATION_MESSAGE);
          }
          String oaRes[][]=toucan.util.Tools.stringToArray2D(res3.getClient());
          boolean gotSomething=false;
          for (int i=0;i<oaRes.length;i++){
        	  	if(!oaRes[i][0].startsWith("#")){
        	  		ServiceRunner.this.owner.gl.annotateIupac(oaRes[i][1],oaRes[i][0],Double.parseDouble(oaRes[i][7]), "oligo-analysis");
        	  		gotSomething=true;
        	  	}
          }
          owner.gl.normalizeScores("oligo-analysis");
          if(!gotSomething){
        	  JOptionPane.showMessageDialog(owner,"No over-represented oligos found.","Info",JOptionPane.INFORMATION_MESSAGE);
          }
  	  	  ServiceRunner.this.owner.setImgSelectedFeatures();
          //Report the server file location
          System.out.println("Result file on the server::\n"+ res3.getServer());

        }
      else if (serviceToRun == REPEATMASKER) {
        returnedXML = SOAPClient.runRepeatMasker(soapServerUrl, fastaStr,
                                                 species);
        handleReturnedXMLRepeatMasker(); //FASTA + GFF
      }
    }
    /*catch(org.xml.sax.SAXParseException spe){
      JOptionPane.showMessageDialog(owner, "This SOAP server is not available, please choose another server in the start dialog",
                                      "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    catch(java.rmi.ConnectIOException ioe){
      JOptionPane.showMessageDialog(owner, "This SOAP server is not available, please choose another server in the start dialog",
                                      "Information", JOptionPane.INFORMATION_MESSAGE);
    }*/
    catch (Exception e) {
      e.printStackTrace();
      /*if(e.getMessage().indexOf("Connection refused")!=-1)
        JOptionPane.showMessageDialog(owner, "This server is not available, please choose another server in the start dialog",
                                      "Information", JOptionPane.INFORMATION_MESSAGE);
      else if(e.getMessage().indexOf("prolog")!=-1){
        JOptionPane.showMessageDialog(owner, "This SOAP server is not available, please choose another server in the start dialog",
                                      "Information", JOptionPane.INFORMATION_MESSAGE);
      }
      else*/

      if(!soapServerUrl.equals(GlobalProperties.getSOAPBackup())){
      soapServerUrl = GlobalProperties.getSOAPBackup();
      this.run();
    }
    else 	   
    	JOptionPane.showMessageDialog(owner, "This SOAP server is not available, please choose another server in the start dialog.",
  			//JOptionPane.showMessageDialog(owner, "All SOAP servers have been tried but the service could not be run. Please report tzthe developers ",
  				        "Warning",
        JOptionPane.WARNING_MESSAGE);
  }

  }

  public void handleReturnedGFF() {
    if (!confirmAnnot && returnedGFF != null &&
        !returnedGFF.equalsIgnoreCase("") && !returnedGFF.startsWith("Error") &&
        returnedGFF.indexOf("Return string empty!") == -1 && returnedGFF.indexOf("Connection refused") == -1) {
      try {
        ServiceRunner.this.owner.gl.addGffFromString(returnedGFF);
        ServiceRunner.this.owner.setImgSelectedFeatures();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
    else {
      if (returnedGFF == null) {
        JOptionPane.showMessageDialog(owner,
                                      "Service failed: no result file \n Server may be down \n Please select another server in the start dialog",
                                      "Error",
                                      JOptionPane.ERROR_MESSAGE);
        return;
      }
      if (returnedGFF.equalsIgnoreCase("")) {
        JOptionPane.showMessageDialog(owner, "No hits were found. \n\n Try different input parameters \nvalues to increase the number of solutions.",
                                      "", JOptionPane.INFORMATION_MESSAGE);
        return;
      }
      if (returnedGFF.startsWith("Error")) {
        JOptionPane.showMessageDialog(owner,returnedGFF,
                                      "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      if (returnedGFF.indexOf("Connection refused") !=-1) {
        JOptionPane.showMessageDialog(owner, "This server is not available, please choose another server in the start dialog",
                                      "Error", JOptionPane.ERROR_MESSAGE);
        return;
      }
      if (returnedGFF.indexOf("Return string empty!") != -1) {
        JOptionPane.showMessageDialog(owner, "No hits found.",
                                      "", JOptionPane.INFORMATION_MESSAGE);
        return;
      }
      owner.writeToStatus("Service done");
      Object[] msg = new Object[3];
      msg[0] = "Service results are ready";
      msg[1] = "Choose YES to add the results to the current gene list";
      msg[2] = "Choose NO to save the results";

      final JOptionPane optionPane = new JOptionPane(msg,
          JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION);
      final JDialog dialog = new JDialog(owner, "service results", false);
      dialog.setContentPane(optionPane);

      optionPane.addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent e) {
          String prop = e.getPropertyName();
          if (dialog.isVisible() && e.getSource() == optionPane &&
              (prop.equals(JOptionPane.VALUE_PROPERTY) ||
               prop.equals(JOptionPane.INPUT_VALUE_PROPERTY))) {
            try {
              int ret = ( (Integer) optionPane.getValue()).intValue();
              if (ret == JOptionPane.YES_OPTION) {
                //ServiceRunner.this.owner.gl.addGffFromString(returnedGFF);
                //ServiceRunner.this.owner.setImg();
                //Set prevFeatSet = new HashSet();
                //prevFeatSet.addAll( (Set) ServiceRunner.this.owner.gl.
                //                   getObjects().keySet());
                //System.out.println(prevFeatSet);
                ServiceRunner.this.owner.gl.addGffFromString(returnedGFF);
                //TreeMap currFeatMap = ServiceRunner.this.owner.gl.getObjects();
                //System.out.println(currFeatMap);
                //Object temp;
                //for (Iterator it = currFeatMap.keySet().iterator(); it.hasNext(); ) {
                //  temp = it.next();
                //  System.out.print(temp);
                //  if (!prevFeatSet.contains(temp)) {
                //    ServiceRunner.this.owner.currentlySelected.put(temp,
                //        currFeatMap.get(temp));
                //    System.out.println(" added");
                //  }
                //  else System.out.println("not added");
                //}
                ServiceRunner.this.owner.setImgSelectedFeatures();
              }
              if (ret == JOptionPane.NO_OPTION) {
                JFileChooser fc = new JFileChooser(GlobalProperties.
                    getCurrentDir());
                fc.addChoosableFileFilter(new SomeFileFilter.SeqFileFilter());
                int returnVal = fc.showSaveDialog(owner);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                  try {
                    String fileName = fc.getSelectedFile().getAbsolutePath();
                    owner.writeToStatus("to save " + fileName);
                    toucan.util.Tools.stringToFile(returnedGFF, fileName);
                    GlobalProperties.setCurrentDir(fc.getSelectedFile().
                        getParent());
                  }
                  catch (Exception exc) {
                    exc.printStackTrace();
                  }
                }
                else {
                  owner.writeToStatus("Open command cancelled by user.");
                }
              }
            }
            catch (Exception exc) {
              JOptionPane.showMessageDialog(owner,
                                            "Problems with service results, here's what I know: " +
                                            exc.getMessage(), "Error",
                                            JOptionPane.ERROR_MESSAGE);
              exc.printStackTrace();
            }
            dialog.setVisible(false);
          }
        }
      });
      dialog.pack();
      dialog.setLocation(200, 200);
      dialog.setVisible(true);
    }
  }

  public void handleReturnedXML() {
    if (returnedXML == null || returnedXML.equals("")) {
      JOptionPane.showMessageDialog(owner,
                                    "Service failed: no result file \n Server may be down \n Please select another server in the start dialog",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (returnedXML.indexOf("Connection refused") !=-1) {
      JOptionPane.showMessageDialog(owner,
          "This server is not available, please choose another server in the start dialog",
                                    "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (returnedXML.startsWith("Error")) {
      JOptionPane.showMessageDialog(owner, "Server may be down, \n please select another server in the start dialog. \n Please select another server in the start dialog \n You can mail this error message to the developers, see About",
                                    "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    //xml parsing... to get gff and mtrx separately
    try {
      Document doc = XMLParser.parseString(returnedXML);
      NodeList nl = doc.getElementsByTagName("gff");
      Node n = null;
      n = nl.item(0).getChildNodes().item(0);
      if(n==null){
        JOptionPane.showMessageDialog(owner, "\n No motifs found.",
                                    "Results", JOptionPane.INFORMATION_MESSAGE);
      return;
      }
      returnedGFF = n.getNodeValue();
      nl = doc.getElementsByTagName("mtrx");
      n = nl.item(0).getChildNodes().item(0);
      returnedMTRX = n.getNodeValue();
    }
    catch (Exception exc) {
      exc.printStackTrace();
    }

    //gffFile = toucan.util.Tools.stringToFile(gffStr,gffFilePath);
    //mtrxFile = toucan.util.Tools.stringToFile(mtrxStr,mtrxFilePath);
    owner.writeToStatus("Service done");
    Object[] msg = new Object[3];
    msg[0] = "Service results are ready";
    msg[1] = "Choose YES to add the results to the current gene list (and not save the matrices)";
    msg[2] = "Choose NO to save both gff and matrices to file";

    final JOptionPane optionPane = new JOptionPane(msg,
        JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION);
    final JDialog dialog = new JDialog(owner, "service results", false);
    dialog.setContentPane(optionPane);

    optionPane.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
        if (dialog.isVisible() && e.getSource() == optionPane &&
            (prop.equals(JOptionPane.VALUE_PROPERTY) ||
             prop.equals(JOptionPane.INPUT_VALUE_PROPERTY))) {
          try {
            int ret = ( (Integer) optionPane.getValue()).intValue();
            if (ret == JOptionPane.YES_OPTION) {
              owner.gl.addGffFromString(returnedGFF);
              owner.setImg();
            }
            if (ret == JOptionPane.NO_OPTION) {
              JFileChooser fc = new JFileChooser(GlobalProperties.getCurrentDir());
              fc.addChoosableFileFilter(new SomeFileFilter.SeqFileFilter());
              int returnVal = fc.showSaveDialog(owner);
              GlobalProperties.setCurrentDir(fc.getSelectedFile().getParent());
              if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                  String fileName = fc.getSelectedFile().getAbsolutePath();
                  owner.writeToStatus("Saving " + fileName + " and " +
                                      fileName +
                                      ".mtrx");
                  //gff
                  toucan.util.Tools.stringToFile(returnedGFF, fileName);
                  //mtrx
                  toucan.util.Tools.stringToFile(returnedMTRX,
                                                 fileName + ".mtrx");
                }
                catch (Exception exc) {
                  exc.printStackTrace();
                }
              }
              else {
                owner.writeToStatus("Open command cancelled by user.");
              }
            }
          }
          catch (Exception exc) {
            JOptionPane.showMessageDialog(owner,
                                          "Problems with service results, here's what I know: " +
                                          exc.getMessage(), "Error",
                                          JOptionPane.ERROR_MESSAGE);
            //exc.printStackTrace();
          }
          dialog.setVisible(false);
        }
      }
    });
    dialog.pack();
    dialog.setLocation(200, 200);
    dialog.setVisible(true);
  }

  public void handleReturnedGffFasta(){
  if (returnedXML == null || returnedXML.equals("")) {
    JOptionPane.showMessageDialog(owner,
                                  "Service failed: no result file \n Server may be down \n please select another server in the start dialog",
                                  "Error",
                                  JOptionPane.ERROR_MESSAGE);
    return;
  }
  if (returnedXML.startsWith("Error")) {
    JOptionPane.showMessageDialog(owner, returnedXML + "\n Server may be down, \n please select another server in the start dialog. You can mail this error message to the developers, see About",
                                  "Error", JOptionPane.ERROR_MESSAGE);
    return;
  }
  if (returnedXML.indexOf("Connection refused") !=-1) {
    JOptionPane.showMessageDialog(owner,
        "This server is not available, please choose another server in the start dialog",
                                  "Error", JOptionPane.ERROR_MESSAGE);
    return;
  }
  //xml parsing... to get gff and mtrx separately
  try {
    Document doc = XMLParser.parseString(returnedXML);
    NodeList nl = doc.getElementsByTagName("FASTA");
    Node n = null;
    n = nl.item(0).getChildNodes().item(0);
    returnedFASTA = n.getNodeValue();
    nl = doc.getElementsByTagName("GFF");
    n = nl.item(0).getChildNodes().item(0);
    returnedGFF = n.getNodeValue();
  }
  catch (Exception exc) {
    exc.printStackTrace();
  }

  }

  public void handleReturnedXMLRepeatMasker() {
    if (returnedXML == null || returnedXML.equals("")) {
      JOptionPane.showMessageDialog(owner,
                                    "Service failed: no result file \n Server may be down \n please select another server in the start dialog",
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (returnedXML.startsWith("Error")) {
      JOptionPane.showMessageDialog(owner, returnedXML + "\n Server may be down, \n please select another server in the start dialog. You can mail this error message to the developers, see About",
                                    "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    if (returnedXML.indexOf("Connection refused") !=-1) {
      JOptionPane.showMessageDialog(owner,
          "This server is not available, please choose another server in the start dialog",
                                    "Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    //xml parsing... to get gff and mtrx separately
    try {
      Document doc = XMLParser.parseString(returnedXML);
      NodeList nl = doc.getElementsByTagName("FASTA");
      Node n = null;
      n = nl.item(0).getChildNodes().item(0);
      returnedFASTA = n.getNodeValue();
      nl = doc.getElementsByTagName("GFF");
      n = nl.item(0).getChildNodes().item(0);
      returnedGFF = n.getNodeValue();
      if (returnedGFF.indexOf("No repeats found") != -1) {
        returnedGFF = "";
        JOptionPane.showMessageDialog(owner,
                                      "No repeats found in these sequences",
                                      "", JOptionPane.INFORMATION_MESSAGE);
        return;
      }
    }
    catch (Exception exc) {
      exc.printStackTrace();
    }

    //gffFile = toucan.util.Tools.stringToFile(gffStr,gffFilePath);
    //mtrxFile = toucan.util.Tools.stringToFile(mtrxStr,mtrxFilePath);
    owner.writeToStatus("Service done");
    Object[] msg = new Object[3];
    msg[0] = "Service results are ready";
    msg[1] = "Choose YES to add the results to the current gene list";
    msg[2] = "Choose NO to save both gff and masked sequences to file";

    final JOptionPane optionPane = new JOptionPane(msg,
        JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_CANCEL_OPTION);
    final JDialog dialog = new JDialog(owner, "service results", false);
    dialog.setContentPane(optionPane);

    optionPane.addPropertyChangeListener(new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent e) {
        String prop = e.getPropertyName();
        if (dialog.isVisible() && e.getSource() == optionPane &&
            (prop.equals(JOptionPane.VALUE_PROPERTY) ||
             prop.equals(JOptionPane.INPUT_VALUE_PROPERTY))) {
          try {
            int ret = ( (Integer) optionPane.getValue()).intValue();
            if (ret == JOptionPane.YES_OPTION) {
              if (replaceWithNs) {
                GeneList gl2 = new GeneList();
                gl2.constructFromLargeFastaString(returnedFASTA);
                for (Iterator it = gl2.list.listIterator(); it.hasNext(); ) {
                  Gene g = (Gene) it.next();
                  Gene g2 = owner.gl.get(g.name);
                  g2.setSeqStr(g.seq.seqString());
                }
              }
              owner.gl.addGffFromString(returnedGFF);
              owner.setImg();
            }
            if (ret == JOptionPane.NO_OPTION) {
              JFileChooser fc = new JFileChooser(GlobalProperties.getCurrentDir());
              fc.addChoosableFileFilter(new SomeFileFilter.SeqFileFilter());
              int returnVal = fc.showSaveDialog(owner);
              GlobalProperties.setCurrentDir(fc.getSelectedFile().getParent());
              if (returnVal == JFileChooser.APPROVE_OPTION) {
                try {
                  String fileName = fc.getSelectedFile().getAbsolutePath();
                  owner.writeToStatus("Saving " + fileName + " and " +
                                      fileName +
                                      ".gff");
                  //fasta
                  toucan.util.Tools.stringToFile(returnedFASTA, fileName);
                  //gff
                  toucan.util.Tools.stringToFile(returnedGFF,
                                                 fileName + ".gff");
                }
                catch (Exception exc) {
                  exc.printStackTrace();
                }
              }
              else {
                owner.writeToStatus("Open command cancelled by user.");
              }
            }
          }
          catch (Exception exc) {
            JOptionPane.showMessageDialog(owner,
                                          "Problems with service results, here's what I know: " +
                                          exc.getMessage(), "Error",
                                          JOptionPane.ERROR_MESSAGE);
            exc.printStackTrace();
          }
          dialog.setVisible(false);
        }
      }
    });
    dialog.pack();
    dialog.setLocation(200, 200);
    dialog.setVisible(true);
  }

  public void setPrior(double prior) {
    this.prior = prior;
  }

  public void setBg(String bg) {
    this.bg = bg;
  }

  public void setMtrx(String mtrx) {
    this.mtrx = mtrx;
  }

  public void setStrand(int strand) {
    this.strand = strand;
  }

  public void setList(String list) {
    this.list = list;
  }

  public void setFastaStr(String fastaStr) {
    this.fastaStr = fastaStr;
  }

  public void setSpecies(String spec) {
    this.species = spec;
  }

  public void setReplaceWithNs(boolean rep) {
    this.replaceWithNs = rep;
  }

  public void setGFFStr(String GFF) {
    this.GFFStr = GFF;
  }

  public void setNrMotifs(int nrMotifs) {
    this.nrMotifs = nrMotifs;
  }

  public void setMotifLength(int motifLength) {
    this.motifLength = motifLength;
  }
  
  public void setoaLength(int l) {
	    this.oaLength = l;
  }

  public void setoaMinSig(int l) {
	    this.oaMinSig = l;
}
  public void setoaOverlap(int l) {
	    this.oaOverlap = l;
}
  public void setoaBg(String l) {
	    this.oaBg = l;
}
  public void setoaOrg(String l) {
	    this.oaOrg = l;
}
  public void setoaStrand(int l) {
	    this.oaStrand = l;
}



  public void setMotifOverlap(int motifOverlap) {
    this.motifOverlap = motifOverlap;
  }

  public void setNrRuns(int nrRuns) {
    this.nrRuns = nrRuns;
  }

  public void setThreshold(double threshold) {
    this.threshold = threshold;
  }

  public void setcThreshold(double threshold) {
	    this.cthreshold  = threshold;
	  }

  public void setNrRand(int nrRand) {
	    this.nrRand  = nrRand;
	  }

  
  public void setNrElements(int nrElements) {
    this.nrElements = nrElements;
  }

  public void setNrModules(int nrMod) {
    this.nrModules = nrMod;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public void setExclude(String exclude) {
    this.exclude = exclude;
  }

  public void setOverlap(boolean overlap) {
    this.overlap = overlap;
  }

  public void setAlgorithm(int alg) {
    this.algorithm = alg;
  }

  public void setPenalisation(boolean pen) {
    this.penalisation = pen;
  }

  public void setTree(String tree) {
    this.tree = tree;
  }

  public void setSeq_type(String seq_type) {
    this.seq_type = seq_type;
  }

  public void setMax_mut(float max_mut) {
    this.max_mut = max_mut;
  }

  public void setMax_mut_per_branch(float max_mut_per_branch) {
    this.max_mut_per_branch = max_mut_per_branch;
  }

  public void setSubreg_change_cost(float subreg_change_cost) {
    this.subreg_change_cost = subreg_change_cost;
  }

  public void setSubreg_size(int subreg_size) {
    this.subreg_size = subreg_size;
  }

  public void setTriplet_filt(boolean triplet_filt) {
    this.triplet_filt = triplet_filt;
  }

  public void setPair_filt(boolean pair_filt) {
    this.pair_filt = pair_filt;
  }

  public void setPost_filt(boolean post_filt) {
    this.post_filt = post_filt;
  }

  public void setInsdel_cost(float insdel_cost) {
    this.insdel_cost = insdel_cost;
  }

  public void setInv_cost(float inv_cost) {
    this.inv_cost = inv_cost;
  }

  public void setWl(int wl) {
    this.wl = wl;
  }

  public void setMcl(int mcl) {
    this.mcl = mcl;
  }

  public void setRev(int rev) {
    this.rev = rev;
  }

  public void setSeq1(String seq1) {
    this.seq1 = seq1;
  }

  public void setSeq2(String seq2) {
    this.seq2 = seq2;
  }

  public void setId1(String id1) {
    this.id1 = id1;
  }

  public void setId2(String id2) {
    this.id2 = id2;
  }

  public void setConfirmAnnot(boolean confirmAnnot) {
    this.confirmAnnot = confirmAnnot;
  }

  public void setSubjectType(int subjectType) {
    this.subjectType = subjectType;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public void setPenalizeShort(boolean penalizeShort) {
    this.penalizeShort = penalizeShort;
  }

  public void setTakeLogOfInput(boolean takeLogOfInput) {
    this.takeLogOfInput = takeLogOfInput;
  }

  public void setModule(String module) {
    this.module = module;
  }

  public void setCutOff(int cutOff) {
    this.cutOff = cutOff;
  }

  public void setSoapServerUrl(String url) {
    this.soapServerUrl = url;
  }

}
