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
package toucan.gui;

import toucan.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import toucan.*;
import org.biojava.bio.seq.Sequence;
import toucan.*;
import java.io.FileInputStream;
import org.ensembl.driver.*;

/**
 * Title: LoadSeq
 * Description: Dialog to load sequences and orthologous sequences from the Ensembl database, based on any identifier
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 2.0
 */

public class LoadSeq
    extends JDialog
    implements ActionListener {

  private JButton ok;
  private JButton cancel;
  private JButton idFileButton;
  private MainFrame owner;
  private JComboBox cmbDbType, cmbFileType, cmbRetrieve, cmbExternals;
  private JList homolList;
  private JTextField idFile;
  private JTextField bpBeforeTxt, bpWithinTxt;
  private JCheckBox addOrNew,revCompAuto;
  private DefaultComboBoxModel externalItems;
  private Vector homolItemsVec;
  private String failed = "";
  public final int ONE_SECOND = 1000;
  public ProgressMonitor progressMonitor;
  public javax.swing.Timer timer;
  public static int lengthOfTask;
  private int current = 0;
  private String statMessage;
  public GeneList fullGl;
  private String dbUrl;
  private ArrayList inputList;
  private int dbId;
  private String externalDb;
  public String retrieve;
  public Object[] homolRetrieve;

  public LoadSeq(MainFrame window) {
    super(window, "New GeneList from identifiers", true);
    owner = window;
    JPanel buttonPane = new JPanel();
    ok = new JButton("OK");
    ok.addActionListener(this);
    ok.setPreferredSize(new Dimension(80, 20));
    cancel = new JButton("Cancel");
    cancel.setPreferredSize(new Dimension(80, 20));
    cancel.addActionListener(this);
    buttonPane.add(ok);
    buttonPane.add(cancel);
    getContentPane().add(buttonPane, BorderLayout.SOUTH);
    JPanel filePanel = new JPanel();
    idFile = new JTextField(25);
    idFileButton = new JButton("Browse");
    idFileButton.addActionListener(this);
    JPanel topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
    JLabel label = new JLabel();
    label.setText("ID-list or ID-file:");
    filePanel.add(label);
    filePanel.add(idFile);
    filePanel.add(idFileButton);
    topPanel.add(filePanel);
    getContentPane().add(topPanel, BorderLayout.NORTH);

    JPanel ensPanel = new JPanel();
    ensPanel.setLayout(new BoxLayout(ensPanel, BoxLayout.Y_AXIS));
    JPanel topEnsPanel = new JPanel();
    topEnsPanel.setLayout(new BoxLayout(topEnsPanel, BoxLayout.X_AXIS));
    Object[] fileItems = new Object[2];
    fileItems[0] = "Comma separated";
    fileItems[1] = "ID file";
    cmbFileType = new JComboBox(fileItems);
    topEnsPanel.add(cmbFileType);

    Object[] dbItems = new Object[GlobalProperties.getSpeciesArr().length];
    EnsemblSpecies es;
    for (int i = 0; i < GlobalProperties.getSpeciesArr().length; i++) {
      es = GlobalProperties.getSpeciesArr()[i];
      dbItems[i] = es.name;
    }
    cmbDbType = new JComboBox(dbItems);
    cmbDbType.addActionListener(this);
    cmbDbType.setPreferredSize(new Dimension(100, 20));
    topEnsPanel.add(cmbDbType);
    externalItems = new DefaultComboBoxModel();
    //homolItems = new DefaultComboBoxModel();
    homolItemsVec = new Vector();
    updateCmbs(0);

    cmbExternals = new JComboBox(externalItems);
    cmbExternals.setPreferredSize(new Dimension(100, 20));
    topEnsPanel.add(cmbExternals);

    ensPanel.add(topEnsPanel);

    JPanel bottomEnsPanel = new JPanel();
    Object[] retrieveItems = new Object[3];
    retrieveItems[0] = "Complete gene";
    retrieveItems[1] = "5' Upstream CDS";
    retrieveItems[2] = "5' Upstream Exon1";
    cmbRetrieve = new JComboBox(retrieveItems);
    bottomEnsPanel.add(cmbRetrieve);
    JLabel bpLabel = new JLabel("bp before:");
    bottomEnsPanel.add(bpLabel);
    bpBeforeTxt = new JTextField("2000", 5);
    bottomEnsPanel.add(bpBeforeTxt);
    JLabel bpWithinLabel = new JLabel("bp within:");
    bottomEnsPanel.add(bpWithinLabel);
    bpWithinTxt = new JTextField("200", 5);
    bottomEnsPanel.add(bpWithinTxt);

    ensPanel.add(bottomEnsPanel);

    JPanel lastEnsPanel = new JPanel();
    JPanel checkboxPanel = new JPanel();
    checkboxPanel.setLayout(new BoxLayout(checkboxPanel, BoxLayout.Y_AXIS));
    addOrNew = new JCheckBox("Add to current list", true);
    revCompAuto = new JCheckBox("RevCompl Automatically", true);
    checkboxPanel.add(addOrNew);
    checkboxPanel.add(revCompAuto);
    lastEnsPanel.add(checkboxPanel);
    JLabel homolLabel = new JLabel("Add (multiple) orthologs:");
    lastEnsPanel.add(homolLabel);

    homolList = new JList(homolItemsVec);
    homolList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    JScrollPane homolListScrollPane = new JScrollPane(homolList);
    homolList.setVisibleRowCount(5);
    //cmbHomol = new JComboBox(homolItems);
    lastEnsPanel.add(homolListScrollPane);
    ensPanel.add(lastEnsPanel);
    addWindowListener(new WindowAdapter() {
      public void windowOpened(WindowEvent e) {
        idFile.requestFocus();
      }
    });
    getContentPane().add(ensPanel, BorderLayout.CENTER);

    timer = new javax.swing.Timer(100, new TimerListener());

    this.pack();
  }

  private void updateCmbs(int index) {
    externalItems.removeAllElements();
    for (Iterator it = GlobalProperties.getSpeciesArr()[index].externalNames.
         iterator(); it.hasNext(); ) {
      externalItems.addElement(it.next());
    }
    homolItemsVec.removeAllElements();
    for (Iterator it = GlobalProperties.getSpeciesArr()[index].homologs.
         iterator(); it.hasNext(); ) {
      homolItemsVec.addElement(it.next());
    }
  }

  public void actionPerformed(ActionEvent e) {
    if (e.getSource() == ok) {
      try {
        if (idFile.getText().equalsIgnoreCase("")) {
          JOptionPane.showMessageDialog(LoadSeq.this, "No file specified",
                                        "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }
        /*if (this.cmbRetrieve.getSelectedIndex() == 0) {
          int yesno = JOptionPane.showConfirmDialog(this,
              "Genes with " + this.bpBeforeTxt.getText() +
              " bp around will be retrieved. Continue?");
          if (yesno == JOptionPane.CANCEL_OPTION ||
              yesno == JOptionPane.NO_OPTION)
            return;
        }*/
        dbId = ( (Integer) GlobalProperties.getSpeciesArr()[cmbDbType.
                getSelectedIndex()].externalIds.get(cmbExternals.
            getSelectedIndex())).intValue();

        externalDb = (String) cmbExternals.getSelectedItem();

        dbUrl = "jdbc:mysql://" + GlobalProperties.getEnsemblMysql() + ":" +
            GlobalProperties.getEnsemblMysqlPort() + "/" +
            GlobalProperties.getSpeciesArr()[cmbDbType.getSelectedIndex()].core +
            "?user=" + GlobalProperties.getEnsemblUser() + "&password=" +
            GlobalProperties.getEnsemblPass();

        if (cmbFileType.getSelectedIndex() == 0) { //comma separated
          //newGl.constructFromExternalDbString(idFile.getText(),(String)cmbExternals.getSelectedItem(),dbId);
          inputList = Tools.arrayListFromCommaString(idFile.getText());
        }
        else if (cmbFileType.getSelectedIndex() == 1) { //ID file
          //newGl.constructFromExternalDbFile(idFile.getText(),(String)cmbExternals.getSelectedItem(),dbId);
          inputList = Tools.arrayListFromFile(idFile.getText());
        }

        fullGl = new GeneList();

        //////////////////////////////////////////
        //Get the Ensembl ID's for this list
        ////////////////////////////////////////////
        //writeToStatus("ext="+externalDb);
        if (!externalDb.equalsIgnoreCase("ensembl")) {
          //for (Iterator it = inputList.iterator();it.hasNext();){
          //  fullGl.list.addAll(Tools.glFromExternal((String)it.next(),externalDb,dbId,dbUrl).list);
          //}

          //retrieve all at once, using a in('..','..') query
          //I don't know how to use a progress bar here. It seems that, once the tasks are starting, that
          //the code goes further with the sequence retrieval, while it should wait.
          owner.writeToStatus("Querying Ensembl to get gene_stable_ids");
          try{
            fullGl.list.addAll(Tools.glFromExternalList(inputList, externalDb,
                dbId, dbUrl).list);
          }
          catch(java.sql.SQLException se){
            JOptionPane.showMessageDialog(LoadSeq.this, "Database error... is Ensembl down?",
                                        "Error", JOptionPane.ERROR_MESSAGE);
            return;
          }

          /*
                       progressMonitor = new ProgressMonitor(LoadSeq.this,
            "Querying for Ensembl genes",
            "", 0, 10);
                       progressMonitor.setProgress(0);
                       progressMonitor.setMillisToDecideToPopup(20);
                       ok.setEnabled(false);
                       timer.start();
                       goGenes();
           */
        }

        else {
         String prefixString = GlobalProperties.getSpeciesArr()[cmbDbType.getSelectedIndex()].prefix;
         for(int it=0;it<inputList.size();it++) {
           inputList.set(it, ((String) inputList.get(it)).toUpperCase());
         }
         if(!((String)inputList.get(0)).startsWith(prefixString)) {
           if (JOptionPane.showConfirmDialog(LoadSeq.this,
               "Your IDs don't start with " + prefixString + ", are you sure you have entered Ensembl IDs?") !=
               JOptionPane.YES_OPTION)
             return;
         }
       fullGl.constructFromEnsemblList(inputList);
       }

        //////////////////////////////////////////
        //species information
        //////////////////////////////////////////
        fullGl.setEnsemblSpecies(GlobalProperties.getSpeciesArr()[cmbDbType.
                                 getSelectedIndex()]);
        this.homolRetrieve = homolList.getSelectedValues();
        this.retrieve = ( (String) cmbRetrieve.getSelectedItem());

        //////////////////////////////////////////
        //Now get the sequences for all the ensembl genes
        //////////////////////////////////////////
        LoadSeq.lengthOfTask = fullGl.list.size();
        progressMonitor = new ProgressMonitor(LoadSeq.this,
                                              "Retrieving sequences from Ensembl",
                                              "", 0, LoadSeq.lengthOfTask);
        progressMonitor.setProgress(0);
        progressMonitor.setMillisToDecideToPopup(20);
        ok.setEnabled(false);
        owner.setStatus("Querying Ensembl...");
        timer.start();
        go(GlobalProperties.getSpeciesArr()[cmbDbType.getSelectedIndex()],
           new Integer(this.bpBeforeTxt.getText()).intValue(),
           new Integer(this.bpWithinTxt.getText()).intValue());
        owner.clearStatus();
        //retrieveEnsembl(GlobalProperties.getSpeciesArr()[cmbDbType.getSelectedIndex()].http,new Integer(this.bpBeforeTxt.getText()).intValue(),new Integer(this.bpWithinTxt.getText()).intValue());
      }
      catch (Exception exc) {
        exc.printStackTrace();
        JOptionPane.showMessageDialog(LoadSeq.this, exc.getMessage(), "Error",
                                      JOptionPane.ERROR_MESSAGE);
      }
      setVisible(false);
    }
    else if (e.getSource() == idFileButton) {
      final JFileChooser fc = new JFileChooser(GlobalProperties.getCurrentDir());
      fc.addChoosableFileFilter(new SomeFileFilter.SeqFileFilter());
      int returnVal = fc.showOpenDialog(LoadSeq.this);
      GlobalProperties.setCurrentDir(fc.getSelectedFile().getParent());
      if (returnVal == JFileChooser.APPROVE_OPTION) {
        try {
          idFile.setText(fc.getSelectedFile().getAbsolutePath());
        }
        catch (Exception exc) {
          exc.printStackTrace();
        }
      }
      else {}
    }
    else if (e.getSource() == cmbDbType) {
      updateCmbs(cmbDbType.getSelectedIndex());
    }
    else if (e.getSource() == cancel)
      setVisible(false);

  }

  //private void retrieveEnsembl(String spec, int bpBefore,int bpWithin) throws Exception{
  class ActualTask {
    ActualTask(EnsemblSpecies spec, int bpBefore, int bpWithin) {
      Gene g = null;
      Gene hom;
      failed = "";
      int index = 0;
      Sequence tempSeq;

      EnsemblSpecies spec2 = null;

      Properties eProps = new Properties();
      eProps.setProperty("ensembl_driver", GlobalProperties.getEnsemblDriver());
      eProps.setProperty("path", GlobalProperties.getEnsemblPath());
      eProps.setProperty("jdbc_driver", GlobalProperties.getJdbcDriver());
      eProps.setProperty("host", GlobalProperties.getEnsemblMysql());
      eProps.setProperty("port", GlobalProperties.getEnsemblMysqlPort());
      eProps.setProperty("user", GlobalProperties.getEnsemblUser());
      eProps.setProperty("password", GlobalProperties.getEnsemblPass());
      eProps.setProperty("database", spec.core);

      try {
        owner.writeToStatus("task length=" + LoadSeq.lengthOfTask);
        while (current < LoadSeq.lengthOfTask) {
          g = (Gene) fullGl.list.get(index);
          hom = null;
          if (g.seq == null) {
            org.ensembl.driver.CoreDriver humanDriver = null;
            try {
              eProps.setProperty("database", spec.core);
              eProps.setProperty("name","");
//              org.ensembl.registry.Registry dr = org.ensembl.registry.Registry.createDefaultRegistry();
//              humanDriver = dr.getGroup("human").getCoreDriver();
              humanDriver = org.ensembl.driver.DriverManager.loadDriver(eProps);
              owner.writeToStatus("Driver loaded (Ensembl - " + spec.longName + ")");
            }
            catch (Exception e) {
              e.printStackTrace();
            }

            try {
              g.setSeqFromEnsembl(humanDriver, bpBefore);
              humanDriver.closeAllConnections();
            }
            catch (Exception ex) {
              //ex.printStackTrace();
              if (ex.getMessage() != null &&
                  ex.getMessage().startsWith("No such CDS")) {
                failed = failed + "- " + g.externalDbEntry + " (" + g.ensembl +
                    "): " + g.name + " >> CDS feature not found \n";
              }
              else {
                failed = failed + "- " + g.externalDbEntry + " (" + g.ensembl +
                    ") >> Gene not found \n\tCheck ID or chosen ID type !\n";
                ex.printStackTrace();
              }
              fullGl.list.remove(index--);
            }

            if (g.seq != null) {
              owner.writeToStatus(g.ensembl + "|" + g.name + "|" +
                                      g.seq.getName());
              g.seq.getAnnotation().setProperty("OS", g.species.name);
              //System.out.println("OS set to "+g.species.name);
            }
              //homolog
            if (LoadSeq.this.homolRetrieve != null && homolRetrieve.length > 0) {
              for (int i = 0; i < homolRetrieve.length; i++) {
                if (homolRetrieve[i] != null) {
                  spec2 = GlobalProperties.getSpeciesArr()[getSpeciesIndex((String)
                      LoadSeq.this.homolRetrieve[i])];
                  eProps.setProperty("database", spec2.core);

                  String dbUrl2 = "jdbc:mysql://" +
                      GlobalProperties.getMartMysql() + "/" +
                      GlobalProperties.getEnsemblMart() + "?user=" +
                      GlobalProperties.getEnsemblUser() + "&password=" +
                      GlobalProperties.getEnsemblPass();

                  System.out.println("homolRetrieve[i]="+(String)homolRetrieve[i]);
                  
                  Object[] orthos = g.getEnsemblOrthologs(dbUrl2, (String)homolRetrieve[i]);
//                  Object[] orthos = g.getEnsemblOrthologs("http://www.biomart.org/biomart/martservice", spec2.name.toLowerCase());
                  for (int o = 0;o<orthos.length;o++){
                    hom = new Gene();
                    //hom.ensembl = g.getEnsemblOrtholog(dbUrl,
                    //    (String) homolRetrieve[i]);
                    hom.ensembl = (String)orthos[o];
                    owner.writeToStatus("Ortholog gene = " + hom.ensembl);
                    owner.writeToStatus("spec2= " + spec2.longName + "|" +
                                            spec2.name);
                    hom.species = spec2;
                    try {
                      org.ensembl.driver.CoreDriver mouseDriver = org.ensembl.driver.DriverManager.loadDriver(eProps);
                      owner.writeToStatus("Driver loaded (Ensembl - " +
                                         spec2.longName + ")");
                      hom.setSeqFromEnsembl(mouseDriver, bpBefore);
                      mouseDriver.closeAllConnections();
                    }
                    catch (Exception ex) {
                      if (ex.getMessage() != null &&
                          ex.getMessage().startsWith("No such CDS")) {
                        failed = failed + "- homolog for " + g.externalDbEntry +
                            " (" + hom.ensembl + ")" + g.name +
                            " >> CDS feature not found \n";
                        ex.printStackTrace();
                      }
                      else {
                        failed = failed + "- homolog for " + g.externalDbEntry +
                            " (" + hom.ensembl + ") >> Gene not found \n";
                        ex.printStackTrace();
                      }
                    }
                    ;
                    if (hom.seq != null) {
                      owner.writeToStatus(hom.ensembl + "|" + hom.name +
                                              "|" +
                                              hom.seq.getName());
                      fullGl.list.add(index + 1, hom);
                      owner.writeToStatus("homolog: " + hom.name + "|" +
                                              hom.ensembl);
                      index++;
                      hom.setEnsemblSpecies(GlobalProperties.getSpeciesArr()[
                                            getSpeciesIndex( (String)
                          homolRetrieve[i])]);
                      hom.setParent(g);
                      //Todo: does this require a lot of memore? Check !!
                      g.orthoList.add(hom);
                      g.addToComment("ortho=" + hom.name + ";");
                      System.out.println("COMMENT+"+g.seq.getAnnotation().getProperty("CC"));
                      //hom.addToComment("parent="+g.name);
                      hom.seq.getAnnotation().setProperty("OS",
                          hom.species.longName);
                    }
                  }
                }
              }

            }
          } //if
          current++;
          statMessage = "Completed " + current + " out of " +
              LoadSeq.lengthOfTask + ".";
          index++;
        } //while

        //further cutting
        for (Iterator it = fullGl.list.iterator();it.hasNext();){
          g = (Gene) it.next();

          if (retrieve.equalsIgnoreCase("complete")) {
            //do nothing, we already have our gene
          }
          else if (retrieve.equalsIgnoreCase("5' Upstream CDS")) { // = upstream of CDS
            try {
              tempSeq = g.selectBeforeFeat("CDS", "gene", g.name, bpBefore,
                                           bpWithin, 0);
              //if no such feature can be found, then this method returns null so:
              if (tempSeq != null) g.setSequence(tempSeq, g.name);
            }
            catch (Exception ex) {
              ex.printStackTrace();
            }
          }
          else if (retrieve.equalsIgnoreCase("5' Upstream Exon1")) { // = upstream of TSS
            try {
              tempSeq = g.selectBeforeFeat("exon", "gene", g.name, bpBefore,
                                           bpWithin, 0);
              //if no such feature can be found, then this method returns null so:
              if (tempSeq != null) g.setSequence(tempSeq, g.name);

              /*if (g.seq != null && g.cdsPositive) g.setSequence(g.
                  makeMySubSeq(1, bpBefore + bpWithin, null), g.name);
              else if (g.seq != null && !g.cdsPositive) g.setSequence(g.
                  makeMySubSeq(g.length - bpBefore - bpWithin, g.length, null),
                  g.name);*/
            }
            catch (Exception ex) {
              ex.printStackTrace();
            }
          }

          //set OS
          //g.seq.getAnnotation().setProperty("OS", g.species.name);
          //System.out.println("OS set to "+g.species.name);

          //Automatically rev. compl.
          //if (!g.cdsPositive){
          //  g.reverseComplement();
          //}

        }
        if(revCompAuto.isSelected()) fullGl.reverseComplement();
        if (!addOrNew.isSelected()) owner.gl = fullGl;
        else owner.gl.union(fullGl);
        if (!failed.equalsIgnoreCase("")) {
          JOptionPane.showMessageDialog(LoadSeq.this,
              "Retrieval and/or parsing problems occurred for:\n " +
                                        failed.substring(0, failed.length() - 1),
                                        "Warning", JOptionPane.WARNING_MESSAGE);
        }
        owner.setImg();
      }
      catch (Exception e) {
        JOptionPane.showMessageDialog(LoadSeq.this, e.getMessage(), "Error",
                                      JOptionPane.ERROR_MESSAGE);
        owner.writeToStatus(e.getMessage());
        e.printStackTrace();
      } //catch
    }
  }

  //not in use currently....
  class GetGenesTask {
    GetGenesTask() {
      Gene g = null;
      failed = "";
      int index = 0;
      try {
        while (current < LoadSeq.lengthOfTask) {
          fullGl.list.addAll(Tools.glFromExternal( (String) inputList.get(index),
                                                  externalDb, dbId, dbUrl).list);
          current++;
          statMessage = "Completed " + current + " out of " +
              LoadSeq.lengthOfTask + ".";
          index++;
        }
      }
      catch (Exception e) {
        JOptionPane.showMessageDialog(LoadSeq.this, e.getMessage(), "Error",
                                      JOptionPane.ERROR_MESSAGE);
        e.printStackTrace();
      }
    }
  }

  public String getSpeciesLongName(String specName) {
    for (int i = 0; i < GlobalProperties.getSpeciesArr().length; i++) {
      if (GlobalProperties.getSpeciesArr()[i].name.equalsIgnoreCase(specName) ||
          GlobalProperties.getSpeciesArr()[i].shortName.equalsIgnoreCase(
          specName) ||
          GlobalProperties.getSpeciesArr()[i].longName.
          equalsIgnoreCase(specName) ||
          GlobalProperties.getSpeciesArr()[i].name.endsWith(specName) ||
          GlobalProperties.getSpeciesArr()[i].shortName.endsWith(specName) ||
          GlobalProperties.getSpeciesArr()[i].longName.endsWith(specName))
        return GlobalProperties.getSpeciesArr()[i].longName;
    }
    return null;
  }

  public int getSpeciesIndex(String specName) {
    //specName = specName.substring(specName.indexOf("_")); //take last part of species name
    for (int i = 0; i < GlobalProperties.getSpeciesArr().length; i++) {
      if (GlobalProperties.getSpeciesArr()[i].name.equalsIgnoreCase(specName) ||
          GlobalProperties.getSpeciesArr()[i].longName.equalsIgnoreCase(
          specName) ||
          GlobalProperties.getSpeciesArr()[i].shortName.
          equalsIgnoreCase(specName) ||
          GlobalProperties.getSpeciesArr()[i].name.endsWith(specName) ||
          GlobalProperties.getSpeciesArr()[i].longName.equalsIgnoreCase(
          specName) ||
          GlobalProperties.getSpeciesArr()[i].shortName.
          equalsIgnoreCase(specName))
        return i;
    }
    return -1;
  }

  //Stuff for the progress monitor
  void go(final EnsemblSpecies spec, final int bpBefore, final int bpWithin) {
    current = 0;
    final SwingWorker worker = new SwingWorker() {
      public Object construct() {
        return new ActualTask(spec, bpBefore, bpWithin);
      }
    };
    worker.start();
  }

  void goGenes() {
    current = 0;
    final SwingWorker worker = new SwingWorker() {
      public Object construct() {
        owner.writeToStatus("lengthoftask 1 = " + LoadSeq.lengthOfTask);
        return new GetGenesTask();
      }
    };
    worker.start();
  }

  int getLengthOfTask() {
    return LoadSeq.lengthOfTask;
  }

  /**
   * Called from ProgressBarDemo to find out how much has been done.
   * @return current
   */
  int getCurrent() {
    return current;
  }

  void stop() {
    current = LoadSeq.lengthOfTask;
  }

  /**
   * Called from ProgressBarDemo to find out if the task has completed.
   * @return statMessage
   */
  boolean done() {
    if (current >= LoadSeq.lengthOfTask)
      return true;
    else
      return false;
  }

  String getMessage() {
    return statMessage;
  }

  /**
   * The actionPerformed method in this class
   * is called each time the Timer "goes off".
   */
  class TimerListener
      implements ActionListener {
    public void actionPerformed(ActionEvent evt) {
      if (progressMonitor.isCanceled() || done()) {
        progressMonitor.close();
        stop();
        Toolkit.getDefaultToolkit().beep();
        timer.stop();
        if (done()) {
          owner.writeToStatus("Sequence retrieval done");
        }
        ok.setEnabled(true);
      }
      else {
        progressMonitor.setNote(getMessage());
        progressMonitor.setProgress(getCurrent());
        //taskOutput.append(task.getMessage() + newline);
        //taskOutput.setCaretPosition(
        //taskOutput.getDocument().getLength());
      }
    }
  }

  public static void main(String[] args) throws Exception{
    Gene g = new Gene();
    g.ensembl = "ENSG00000164896";
    Properties props = new Properties();
    props.load(new FileInputStream("/home/saerts/tmp/ensembl.properties"));
    org.ensembl.driver.CoreDriver humanDriver = org.ensembl.driver.DriverManager.load(props);
    g.setSeqFromEnsembl(humanDriver,10000);
    System.out.println(g.seq.seqString());
  }
}
