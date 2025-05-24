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

import java.io.*;
import java.net.*;
import java.util.*;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.dnd.*;
import javax.swing.*;

import org.biojava.bio.seq.*;


import toucan.*;
import toucan.util.*;

import toucan.PWM;
import toucan.PWMList;
import toucan.gui.WMPanel;

import com.Ostermiller.util.Browser;

/**
 * Title: TOUCAN Main window
 * Description: Integrated workbench for cis-regulatory sequence analysis
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 3.1.1
 */

public class MainFrame
  extends JFrame
  implements ActionListener, ClipboardOwner, DropTargetListener {

  private static final long serialVersionUID = 1L;
  public String version = "3.1.1";
  public String homeDir = "";
  private JMenuBar menu = new JMenuBar();
  private JMenuItem openItem, saveItem, saveItem2,
      clearItem, newWindowItem, aboutItem, helpItem, addItem, exportItem,
      loadGffItem, zoomInItem, zoomOutItem, getInfoItem, normalizeItem;
 
  //public static DefaultListModel listModel;
  //public static DefaultListModel featShowListModel;
  public DefaultListModel listModel;
  public DefaultListModel featShowListModel;
  private JLabel img;
  private JList list;
  private JList featShowList;
  private JSplitPane splitPane, infoPane, topleftPane, newPane;

  /*  public static GeneList gl = new GeneList();
    public static String glName = "";
    public static GeneList[] glArr = new GeneList[2];
    public static String[] glArrNames = new String[2];*/
  public GeneList gl = new GeneList();
  public String glName = "";
  public GeneList[] glArr = new GeneList[2];
  public String[] glArrNames = new String[2];
  //JScrollPane picturePane;
  DroppableScrollPane picturePane;
  private JTextArea geneText;
  private JTextArea featText;
  
  /*private static JTextArea statusText;
     private static JScrollPane statusPane;
     public static int selectedGene = 0;
     public static int selectedBp = 0;
     public static GeneList subList = new GeneList();
     public static ArrayList featList;
     public static JDialog selectFrame;
     public static JDialog featFrame;
     public static JPopupMenu featPop, genePop, subPop;
     public static TreeMap currentlySelected;*/
  private JTextArea statusText;
  private JScrollPane statusPane;
  public int selectedGene = 0;
  public int selectedBp = 0;
  public GeneList subList = new GeneList();
  public ArrayList featList;
  public JDialog selectFrame;
  public JDialog featFrame;
  public JPopupMenu featPop, genePop, subPop;
  public TreeMap currentlySelected;
  public boolean fillYesNo;
  public double scalingFactor = 0.5d;
  public URL url = null, bigUrl = null;
  public static Image toucanImg;
  public String idFile, xmlFile;
  public String externalDb;
  public int bpAround;
  public int bpBefore;
  public int bpWithin;
  public String retrieveType;
  public String upstreamType;
  public String dbType;
  public String idString;
  public boolean infoRetrieved = false;
  public String liteDbName;
  public int windowLength;
  public int overlap;
  public int offset;
  public boolean revCompl = false;
  public String onlineSource;
  public String failed = "";
  public Clipboard clipboard;
  public boolean propProblem = false;
  public ArrayList listOfModules;
  private int MAXLENGTH = 10000;
  private int featIndex;
  private JMenu featSubMenu;

  public MainFrame() throws Exception {
    setTitle("TOUCAN "+version);
    Toolkit k = getToolkit();
    Dimension scrSize = k.getScreenSize();
    this.setBounds(0, 0, scrSize.width,
                   scrSize.height);
    final ClassLoader cl = ResourceAnchor.class.getClassLoader();
    url = cl.getResource("images/toucan_small.jpg");
    if (url == null) {
      toucanImg = java.awt.Toolkit.getDefaultToolkit().getImage(
          "images/toucan_small.jpg");
    }
    else {
      //System.out.println("Found image at: " + url);
      toucanImg = java.awt.Toolkit.getDefaultToolkit().getImage(url);
    }
    setIconImage(toucanImg);
    statusText = new JTextArea("");
    statusPane = new JScrollPane(statusText);
    //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setJMenuBar(menu);
    JMenu fileMenu = new JMenu("File");
    JMenu helpMenu = new JMenu("Help");
    JMenu opMenu = new JMenu("Annotation");
    JMenu viewMenu = new JMenu("View");
    JMenu toolsMenu = new JMenu("Tools");
    JMenu motifsMenu = new JMenu("Motifs");
    JMenu denovoMenu = new JMenu("De novo discovery");
    JMenu pwmMenu = new JMenu("PWM scoring");
    JMenu moduleMenu = new JMenu("Modules");
    
    JMenu alignMenu = new JMenu("Alignment");
    JMenu diMenu = new JMenu("2 Seq");
    JMenu diAutoMenu = new JMenu("All pairwise");
    JMenu exportSubMenu = new JMenu("Export");
    JMenu soapMenu = new JMenu("Services");
    JMenu ensemblMenu = new JMenu("From Ensembl");
    JMenu annotateMenu = new JMenu("Annotation");
    JMenu onlineMenu = new JMenu("Get_Seq");
    JMenu xemblMenu = new JMenu("From EMBL");
    JMenu windowMenu = new JMenu("Window");
    openItem = new JMenuItem("Load Seq");
    openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
        ActionEvent.CTRL_MASK));
    addItem = new JMenuItem("Add Seq");
    addItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                                                  ActionEvent.CTRL_MASK));

    //modules
    listOfModules = new ArrayList();

    //clipboard
    clipboard = getToolkit().getSystemClipboard();

    //properties
    GlobalProperties.readProperties();

    //species from Ensembl
    //writeToStatus("Making species from properties file...");

    this.writeToStatus("Supporting Ensembl release: " +
                       GlobalProperties.getEnsemblRelease());
    
    //Create a file chooser
    final JFileChooser fc = new JFileChooser(homeDir);
    fc.addChoosableFileFilter(new SomeFileFilter.SeqFileFilter());
    openItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int returnVal = fc.showOpenDialog(MainFrame.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          try {
            String fileName = fc.getSelectedFile().getAbsolutePath();
            MainFrame.this.writeToStatus("Opened " + fileName);
            //cleanup
            cls();
            //
            setGeneList(fileName);
            setImg();
          }
          catch (Exception exc) {
            exc.printStackTrace();
            if (exc.getMessage() != null && !exc.getMessage().equals(""))
              JOptionPane.showMessageDialog(MainFrame.this, exc.getMessage(),
                                            "Error",
                                            JOptionPane.ERROR_MESSAGE);
            else {
              JOptionPane.showMessageDialog(MainFrame.this,
                                            "File could not be opened. See Java console for the Exception.",
                                            "Error", JOptionPane.ERROR_MESSAGE);
            }
          }
        }
        else {
          MainFrame.this.writeToStatus("Open command cancelled by user.");
        }
      }
    });
    fileMenu.add(openItem);

    addItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        fc.setMultiSelectionEnabled(true);
        int returnVal = fc.showOpenDialog(MainFrame.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          File[] fileArr = fc.getSelectedFiles();
          for (int i = 0; i < fileArr.length; i++) {
            String fileName = ( (File) fileArr[i]).getAbsolutePath();
            MainFrame.this.writeToStatus("Opened " + fileName);
            if (gl == null) gl = new GeneList();
            try {
              gl.constructFromLargeEmblFile(fileName);
            }
            catch (Exception exc1) {
              try {
                gl.constructFromLargeFastaFile(fileName);
              }
              catch (Exception exc2) {
                try {
                  gl.constructFromLargeGbFile(fileName);
                }
                catch (Exception exc3) {
                  exc3.printStackTrace();
                }
              }
            }
          }
          fc.setMultiSelectionEnabled(false);
          try {
            MainFrame.this.gl.updateGeneNames();
            setImg();
          }
          catch (Exception exc) {
            //exc.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame.this, exc.getMessage(),
                                          "Error", JOptionPane.ERROR_MESSAGE);
          }
        }
        else {
          MainFrame.this.writeToStatus("Open command cancelled by user.");
        }
      }
    });
    fileMenu.add(addItem);
    fileMenu.addSeparator();

    loadGffItem = new JMenuItem("Annotate GFF");
    loadGffItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int returnVal = fc.showOpenDialog(MainFrame.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
          try {
            String fileName = fc.getSelectedFile().getAbsolutePath();
            MainFrame.this.writeToStatus("Opened " + fileName);
            gl.addGffFromFile(fileName);
            setImg();
          }
          catch (Exception exc) { //exc.printStackTrace();
          }
        }
        else {
          MainFrame.this.writeToStatus("Open command cancelled by user.");
        }
      }
    });
    fileMenu.add(loadGffItem);
    fileMenu.addSeparator();

    saveItem2 = new JMenuItem("Save sublist");
    saveItem2.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int overwrite = JOptionPane.YES_OPTION;
        int returnVal = JFileChooser.APPROVE_OPTION;
        while (returnVal != JFileChooser.CANCEL_OPTION &&
               overwrite != JOptionPane.CANCEL_OPTION) {
          returnVal = fc.showSaveDialog(MainFrame.this);
          if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
              String fileName = fc.getSelectedFile().getAbsolutePath();
              if (fc.getSelectedFile().exists()) {
                overwrite = JOptionPane.showConfirmDialog(fc,
                    "File already exists. Overwrite?", "Overwrite",
                    JOptionPane.YES_NO_CANCEL_OPTION);
              }
              if (overwrite == JOptionPane.YES_OPTION) {
                if (fileName.endsWith(".embl"))
                  MainFrame.this.subList.writeToLargeEmblFile(fileName);
                else if (fileName.endsWith(".tfa") || fileName.endsWith("fasta"))
                  MainFrame.this.subList.writeToLargeFastaFile(fileName);
                else if (fileName.endsWith(".gb"))
                  MainFrame.this.subList.writeToLargeGbFile(fileName);
                else
                  JOptionPane.showMessageDialog(MainFrame.this,
                                                "Please use .embl, .gb, .tfa or .fasta extension",
                                                "Error",
                                                JOptionPane.ERROR_MESSAGE);
                MainFrame.this.writeToStatus("saved " + fileName);
                break;
              }
            }
            catch (Exception exc) { //exc.printStackTrace();
            }
          }
          else {
            MainFrame.this.writeToStatus("Open command cancelled by user.");
          }
        }
      }
    });
    fileMenu.add(saveItem2);

    JMenuItem openSubListItem = new JMenuItem("Open Sublist");
    openSubListItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try{
          MainFrame f = MainFrame.this.newWindow();
          f.setGeneList(MainFrame.this.subList);
          f.setImg();
        }
        catch(Exception ex){
          ex.printStackTrace();
        }
      }
    });
    fileMenu.add(openSubListItem);


    //save parent list
    saveItem = new JMenuItem("Save list");
    saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
        ActionEvent.CTRL_MASK));
    saveItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int overwrite = JOptionPane.YES_OPTION;
        int returnVal = JFileChooser.APPROVE_OPTION;
        while (returnVal != JFileChooser.CANCEL_OPTION &&
               overwrite != JOptionPane.CANCEL_OPTION) {
          returnVal = fc.showSaveDialog(MainFrame.this);
          if (returnVal == JFileChooser.APPROVE_OPTION) {
            if (fc.getSelectedFile().exists()) {
              overwrite = JOptionPane.showConfirmDialog(fc,
                  "File already exists. Overwrite?", "Overwrite",
                  JOptionPane.YES_NO_CANCEL_OPTION);
            }
            if (overwrite == JOptionPane.YES_OPTION) {
              try {
                String fileName = fc.getSelectedFile().getAbsolutePath();
                if (fileName.endsWith(".embl"))

                  //MainFrame.gl.writeToLargeEmblFile(fileName);
                  gl.writeToLargeEmblFile(fileName);
                else if (fileName.endsWith(".tfa") || fileName.endsWith("fasta"))

                  //MainFrame.gl.writeToLargeFastaFile(fileName);
                  gl.writeToLargeFastaFile(fileName);
                else if (fileName.endsWith(".bin"))

                  //MainFrame.gl.serialize(fileName);
                  gl.serialize(fileName);
                else
                  JOptionPane.showMessageDialog(MainFrame.this,
                                                "Please use .embl, .tfa, .fasta, or .bin extension",
                                                "Error",
                                                JOptionPane.ERROR_MESSAGE);

                MainFrame.this.writeToStatus("saved " + fileName);
              }
              catch (Exception exc) { //exc.printStackTrace();
              }
              break;
            }
          }
          else {
            MainFrame.this.writeToStatus("Open command cancelled by user.");
          }
        }
      }
    });
    fileMenu.add(saveItem);



    saveItem = new JMenuItem("Without N's");
    saveItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int overwrite = JOptionPane.YES_OPTION;
        int returnVal = JFileChooser.APPROVE_OPTION;
        while (returnVal != JFileChooser.CANCEL_OPTION &&
               overwrite != JOptionPane.CANCEL_OPTION) {
          returnVal = fc.showSaveDialog(MainFrame.this);
          if (returnVal == JFileChooser.APPROVE_OPTION) {
            if (fc.getSelectedFile().exists()) {
              overwrite = JOptionPane.showConfirmDialog(fc,
                  "File already exists. Overwrite?", "Overwrite",
                  JOptionPane.YES_NO_CANCEL_OPTION);
            }
            if (overwrite == JOptionPane.YES_OPTION) {
              try {
                String fileName = fc.getSelectedFile().getAbsolutePath();
                if (fileName.endsWith(".embl"))
                  gl.saveEmblSplitForMotifScanner(fileName);
                else if (fileName.endsWith(".tfa") || fileName.endsWith("fasta"))
                  gl.saveFastaSplitForMotifScanner(fileName);
                else
                  JOptionPane.showMessageDialog(MainFrame.this,
                                                "Please use .embl, .tfa or .fasta extension",
                                                "Error",
                                                JOptionPane.ERROR_MESSAGE);

                MainFrame.this.writeToStatus("saved " + fileName);
              }
              catch (Exception exc) { //exc.printStackTrace();
              }
              break;
            }
          }
          else {
            MainFrame.this.writeToStatus("Open command cancelled by user.");
          }
        }
      }
    });
    exportSubMenu.add(saveItem);

    final JFileChooser fcFig = new JFileChooser(homeDir);
    fcFig.addChoosableFileFilter(new SomeFileFilter.FigFileFilter());
    exportItem = new JMenuItem("Figure");
    exportItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int overwrite = JOptionPane.YES_OPTION;
        int returnVal = JFileChooser.APPROVE_OPTION;
        while (returnVal != JFileChooser.CANCEL_OPTION &&
               overwrite != JOptionPane.CANCEL_OPTION) {
          returnVal = fcFig.showSaveDialog(MainFrame.this);
          if (returnVal == JFileChooser.APPROVE_OPTION) {
            if (fcFig.getSelectedFile().exists()) {
              overwrite = JOptionPane.showConfirmDialog(fcFig,
                  "File already exists. Overwrite?", "Overwrite",
                  JOptionPane.YES_NO_CANCEL_OPTION);
            }
            if (overwrite == JOptionPane.YES_OPTION) {
              try {
                String fileName = fcFig.getSelectedFile().getAbsolutePath();
                MainFrame.this.exportImg(fileName);
                MainFrame.this.writeToStatus("exported " + fileName);
              }
              catch (Exception exc) { //exc.printStackTrace();
              }
              ;
              break;
            }
          }
          else {
            MainFrame.this.writeToStatus("Open command cancelled by user.");
          }
        }
      }
    });
    exportItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
        ActionEvent.CTRL_MASK));
    exportSubMenu.add(exportItem);

    final JFileChooser fcGff = new JFileChooser(homeDir);
    fcGff.addChoosableFileFilter(new SomeFileFilter.GffFileFilter());
    JMenuItem gffItem = new JMenuItem("GFF");
    gffItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int overwrite = JOptionPane.YES_OPTION;
        int returnVal = JFileChooser.APPROVE_OPTION;
        while (returnVal != JFileChooser.CANCEL_OPTION &&
               overwrite != JOptionPane.CANCEL_OPTION) {
          returnVal = fcGff.showSaveDialog(MainFrame.this);
          if (returnVal == JFileChooser.APPROVE_OPTION) {
            if (fcGff.getSelectedFile().exists()) {
              overwrite = JOptionPane.showConfirmDialog(fcGff,
                  "File already exists. Overwrite?", "Overwrite",
                  JOptionPane.YES_NO_CANCEL_OPTION);
            }
            if (overwrite == JOptionPane.YES_OPTION) {
              try {
                String fileName = fcGff.getSelectedFile().getAbsolutePath();
                //gl.writeGFF(fileName);
                toucan.util.Tools.stringToFile(gl.toGFF(null), fileName);
                MainFrame.this.writeToStatus("exported " + fileName);
              }
              catch (Exception exc) { //exc.printStackTrace();
              }
              ;
              break;
            }
          }
          else {
            MainFrame.this.writeToStatus("Open command cancelled by user.");
          }
        }
      }
    });
    exportSubMenu.add(gffItem);

    final JFileChooser fcFreq = new JFileChooser(homeDir);
    fcFreq.addChoosableFileFilter(new SomeFileFilter.TxtFileFilter());
    JMenuItem freqItem = new JMenuItem("Frequencies");
    freqItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int overwrite = JOptionPane.YES_OPTION;
        int returnVal = JFileChooser.APPROVE_OPTION;
        while (returnVal != JFileChooser.CANCEL_OPTION &&
               overwrite != JOptionPane.CANCEL_OPTION) {
          returnVal = fcFreq.showSaveDialog(MainFrame.this);
          if (returnVal == JFileChooser.APPROVE_OPTION) {
            if (fcFreq.getSelectedFile().exists()) {
              overwrite = JOptionPane.showConfirmDialog(fcFreq,
                  "File already exists. Overwrite?", "Overwrite",
                  JOptionPane.YES_NO_CANCEL_OPTION);
            }
            if (overwrite == JOptionPane.YES_OPTION) {
              try {
                String fileName = fcFreq.getSelectedFile().getAbsolutePath();
                gl.writeFreqFile(fileName);
                MainFrame.this.writeToStatus("exported " + fileName);
              }
              catch (Exception exc) { //exc.printStackTrace();
              }
              ;
              break;
            }
          }
          else {
            MainFrame.this.writeToStatus("Open command cancelled by user.");
          }
        }
      }
    });
    exportSubMenu.add(freqItem);

    final JFileChooser fcMtr = new JFileChooser(GlobalProperties.getCurrentDir());
    fcMtr.addChoosableFileFilter(new SomeFileFilter.TxtFileFilter());
    JMenuItem bnItem = new JMenuItem("Matrix");
    bnItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int overwrite = JOptionPane.YES_OPTION;
        int returnVal = JFileChooser.APPROVE_OPTION;
        while (returnVal != JFileChooser.CANCEL_OPTION &&
               overwrite != JOptionPane.CANCEL_OPTION) {
          returnVal = fcMtr.showSaveDialog(MainFrame.this);
          GlobalProperties.setCurrentDir(fcMtr.getSelectedFile().getParent());
          if (returnVal == JFileChooser.APPROVE_OPTION) {
            if (fcMtr.getSelectedFile().exists()) {
              overwrite = JOptionPane.showConfirmDialog(fcMtr,
                  "File already exists. Overwrite?", "Overwrite",
                  JOptionPane.YES_NO_CANCEL_OPTION);
            }
            if (overwrite == JOptionPane.YES_OPTION) {
              try {
                String fileName = fcMtr.getSelectedFile().getAbsolutePath();
                gl.score4Bn(fileName, -1, null);
                MainFrame.this.writeToStatus("exported " + fileName);
              }
              catch (Exception exc) { //exc.printStackTrace();
              }
              ;
              break;
            }
          }
          else {
            MainFrame.this.writeToStatus("Open command cancelled by user.");
          }
        }
      }
    });
    exportSubMenu.add(bnItem);

    final JFileChooser fcTfa = new JFileChooser(homeDir);
    JMenuItem tfaItem = new JMenuItem("Separate fastaA");
    tfaItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        int overwrite = JOptionPane.YES_OPTION;
        int returnVal = JFileChooser.APPROVE_OPTION;
        while (returnVal != JFileChooser.CANCEL_OPTION &&
               overwrite != JOptionPane.CANCEL_OPTION) {
          returnVal = fcTfa.showSaveDialog(MainFrame.this);
          GlobalProperties.setCurrentDir(fcTfa.getSelectedFile().getParent());
          if (returnVal == JFileChooser.APPROVE_OPTION) {
            if (fcTfa.getSelectedFile().exists()) {
              overwrite = JOptionPane.showConfirmDialog(fcMtr,
                  "File already exists. Overwrite?", "Overwrite",
                  JOptionPane.YES_NO_CANCEL_OPTION);
            }
            if (overwrite == JOptionPane.YES_OPTION) {
              try {
                File newDir = fcTfa.getSelectedFile();
                if (newDir.mkdir()) gl.writeDistinctFastaFiles(newDir.
                    getAbsolutePath() + newDir.separator);
                else throw new Exception("Could not create directory " +
                                         newDir.getAbsolutePath());

              }
              catch (Exception exc) {
                //exc.printStackTrace();
                JOptionPane.showMessageDialog(MainFrame.this, exc.getMessage(),
                                              "Error",
                                              JOptionPane.ERROR_MESSAGE);
              }
              break;
            }
          }
          else {
            MainFrame.this.writeToStatus("Open command cancelled by user.");
          }
        }
      }
    });
    exportSubMenu.add(tfaItem);

    fileMenu.add(exportSubMenu);
    fileMenu.addSeparator();

    clearItem = new JMenuItem("Clear sublist");
    clearItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MainFrame.this.listModel.clear();
        MainFrame.this.subList = new GeneList();
      }
    });
    fileMenu.add(clearItem);

    clearItem = new JMenuItem("Clear all");
    clearItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cls();
      }
    });
    fileMenu.add(clearItem);

    fileMenu.addSeparator();

    JMenuItem newWndItem = new JMenuItem("New Window");
    newWndItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          MainFrame.this.newWindow();
        }
        catch (Exception ex) {
          ex.printStackTrace();
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "Sorry, could not create a new window.",
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    });
    fileMenu.add(newWndItem);
    fileMenu.addSeparator();

    JMenuItem prefsItem = new JMenuItem("Preferences");
    prefsItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        PreferencesDialog pd = new PreferencesDialog(MainFrame.this);
        pd.setLocation(200, 200);
        pd.setVisible(true);
      }
    });
    //fileMenu.add(prefsItem);
    //fileMenu.addSeparator();

    JMenuItem exitItem = new JMenuItem("Exit");
    exitItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispose();
        System.exit(0);
      }
    });
    fileMenu.add(exitItem);

    //TODO, this doesn't work yet
    //the featShowListModel and probably other variables have to be grouped together for each genelist
    //so not only the genelist should be replaced in a new window, but also sublist, features, etc.
    newWindowItem = new JMenuItem("New");
    newWindowItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (glArr[0] == null) glArr[0] = gl;
        else if (glArr[1] == null) glArr[1] = gl;
        else if (glArr[2] == null) glArr[2] = gl;
        else {
          JOptionPane.showMessageDialog(MainFrame.this, "Maximum 4 gene lists",
                                        "Error", JOptionPane.ERROR_MESSAGE);
          return;
        }
        gl = new GeneList();
        try {
          setImg();
        }
        catch (Exception exc) {
          exc.printStackTrace();
        }
      }
    });
    windowMenu.add(newWindowItem);
    opMenu.addSeparator();

    //JavaHelp
    /*HelpSet hs = null;
           try {
     //URL hsURL = new URL((new File(".")).toURL(), "help/HelpSet.hs");
     //URL hsURL = getClass().getResource("/help/HelpSet.hs");
     URL hsURL =  cl.getResource("help/HelpSet.hs");
     if (hsURL==null) hsURL = new URL("file://"+System.getProperty("user.dir")+"/help/HelpSet.hs");
     writeToStatus("Loading Help files from "+hsURL+"...");
     hs = new HelpSet(null, hsURL);
     writeToStatus("done");
     //writeToStatus("Found help set at " + hsURL);
     }
     catch (Exception ee) {
       //ee.printStackTrace();
       writeToStatus("HelpSet not found");
       System.exit(0);
     }
     // create HelpBroker from HelpSet
     HelpBroker hb = hs.createHelpBroker();
     // enable function key F1
     hb.enableHelpKey(getRootPane(), "overview", hs);
     //help menu: context sensitive help (CSH)
     helpItem = new JMenuItem("Help");
     //CSH on helpmenu:
     //helpItem.addActionListener(new CSH.DisplayHelpAfterTracking(hb));
     //overview help on helpmenu:
     hb.enableHelpOnButton(helpItem,"overview",hs);
     helpMenu.add(helpItem);
     */

    helpItem = new JMenuItem("Help (in browser)");
    helpMenu.add(helpItem);
    helpItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //BrowserControl.displayURL(
        //    "http://www.esat.kuleuven.ac.be/~saerts/software/toucan.php#documentation");
        try {
          Browser.init();
          Browser.displayURL(
              "http://www.esat.kuleuven.ac.be/~saerts/software/toucan.php#doc");
        }
        catch (Exception ex) {
          JOptionPane.showMessageDialog(MainFrame.this, "Sorry, couldn't open any browser.\n Please visit this URL:\n\n http://www.esat.kuleuven.ac.be/~saerts/software/toucan.php#doc",
                                        "Couldn't open browser",
                                        JOptionPane.INFORMATION_MESSAGE);
        }
      }
    });

    JMenuItem citeItem = new JMenuItem("Citations");
    helpMenu.add(citeItem);
    citeItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String info = "";
        StringBuffer buf = new StringBuffer();
        try {
          InputStream in = cl.getResourceAsStream("html/cite.html");
          if (in == null) in = new FileInputStream("html/cite.html");
          BufferedReader bin = new BufferedReader(new InputStreamReader(in));
          String line;
          line = bin.readLine();
          while (line != null) {
            buf.append(line);
            line = bin.readLine();
          }
          in.close();
        }
        catch (Exception exc) {
          exc.printStackTrace();
        }
        InfoFrame infoFrame = new InfoFrame("Citations and Credits",
                                            buf.toString());
        infoFrame.setVisible(true);

      }
    });

    aboutItem = new JMenuItem("About");
    aboutItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        //ImageIcon icon = new ImageIcon("images\\bioi_logo.png");
        bigUrl = cl.getResource("images/toucan_large.jpg");
        ImageIcon icon;
        if (bigUrl == null) icon = new ImageIcon("images/toucan_large.jpg");
        else icon = new ImageIcon(bigUrl);
        JOptionPane.showMessageDialog(MainFrame.this, "Toucan v." + version + " \n\n Developed by: \n Stein Aerts \n Peter Van Loo \n\n Contact: stein.aerts@med.kuleuven.ac.be \n http://www.esat.kuleuven.ac.be/~dna/BioI/ \n\n 2005 Copyright University of Leuven",
                                      "About", JOptionPane.INFORMATION_MESSAGE,
                                      icon);
      }
    });
    helpMenu.add(aboutItem);

    //Motifs

    JMenuItem iupacItem = new JMenuItem("Consensus match");
    iupacItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String iupac = "";
        if (gl == null) {
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "Your genelist is empty!", "Error",
                                        JOptionPane.ERROR_MESSAGE);
        }
        else {
          try {
            iupac = JOptionPane.showInputDialog(MainFrame.this,
                                                "Enter regular expression (IUPAC symbols)",
                                                "GC[A,T]C{1,3}");
            if (iupac != null && !iupac.equalsIgnoreCase("")) {
              gl.annotateIupac(iupac, iupac); //objMap is updated within gl
              setImg();
            }
          }
          catch (Exception exc) {
            JOptionPane.showMessageDialog(MainFrame.this,
                                          "Iupac annotation failed", "Error",
                                          JOptionPane.ERROR_MESSAGE);
            //exc.printStackTrace();
          }
        }
      }
    });
    motifsMenu.add(iupacItem);

    JMenuItem clusterItem = new JMenuItem("Consensus cluster");
    clusterItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String iupac = "";
        if (gl == null) {
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "Your genelist is empty!", "Error",
                                        JOptionPane.ERROR_MESSAGE);
        }
        else {
          try {
            iupac = JOptionPane.showInputDialog(MainFrame.this,
                                                "Enter regular expression (IUPAC symbols)",
                                                "GC[A,T]C{1,3}");
            if (iupac != null && !iupac.equalsIgnoreCase("")) {
              gl.annotateIupacWindow(iupac, iupac,200,50,1); //objMap is updated within gl
              setImg();
            }
          }
          catch (Exception exc) {
            JOptionPane.showMessageDialog(MainFrame.this,
                                          "Iupac annotation failed", "Error",
                                          JOptionPane.ERROR_MESSAGE);
            //exc.printStackTrace();
          }
        }
      }
    });
    //motifsMenu.add(clusterItem);

    JMenuItem pairItem = new JMenuItem("Consensus pair");
    pairItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        String iupac1 = "",iupac2="";
        if (gl == null) {
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "Your genelist is empty!", "Error",
                                        JOptionPane.ERROR_MESSAGE);
        }
        else {
          try {
            iupac1 = JOptionPane.showInputDialog(MainFrame.this,
                                                "Enter first regular expression (IUPAC symbols)",
                                                "GC[A,T]C{1,3}");
            iupac2 = JOptionPane.showInputDialog(MainFrame.this,
                    "Enter second regular expression (IUPAC symbols)",
                    "GC[A,T]C{1,3}");
            int windowSize = Integer.parseInt(JOptionPane.showInputDialog(MainFrame.this,"Window size","200"));
            int stepSize = Integer.parseInt(JOptionPane.showInputDialog(MainFrame.this,"Step size","200"));

            if (iupac1 != null && !iupac1.equalsIgnoreCase("") && iupac2 != null && !iupac2.equalsIgnoreCase("") ) {
              gl.annotateIupacPair(iupac1, iupac1,iupac2,iupac2,windowSize,stepSize); //objMap is updated within gl
              setImg();
            }
          }
          catch (Exception exc) {
            JOptionPane.showMessageDialog(MainFrame.this,
                                          "Iupac annotation failed", "Error",
                                          JOptionPane.ERROR_MESSAGE);
            //exc.printStackTrace();
          }
        }
      }
    });
    //motifsMenu.add(pairItem);

    JMenuItem scannerItem = new JMenuItem("MotifScanner");
    scannerItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
       //if (gl.getMaxLength()>MAXLENGTH){
       //   JOptionPane.showMessageDialog(MainFrame.this,"Maximum length of an individual sequence is "+MAXLENGTH+"\n Scanning longer sequences is of no use anyway","Missing values",JOptionPane.ERROR_MESSAGE);
       // }
       // else {
        try {
          /*String ref = "Please cite the JASPAR resource if you use it:\n";
                     ref = ref +
              "Sandelin A, Alkema W, Engstrom P, Wasserman WW, Lenhard B.\n";
                     ref = ref + "JASPAR: an open-access database for eukaryotic transcription factor binding profiles.\n";
           ref = ref + "Nucleic Acids Res. 2004 Jan 1;32(1):D91-4. \n\n\n";
                     ref = ref + "And the TRANSFAC resource:\n";
                     ref = ref + "Wingender, E., Chen, X., Fricke, E., Geffers, R., Hehl, R., Liebich, I., Krull, M., Matys, V., Michael, H., Ohnh�user, R., Pr��, M., Schacherer, F., Thiele, S. and Urbach, S.\n";
           ref = ref + "The TRANSFAC system on gene expression regulation.\n";
                     ref = ref + "Nucleic Acids Res., 29, 281-283 (2001).\n\n";
                     ref = ref +
              "The TRANSFAC database is free for users from non-profit organizations only. \n";
                     ref = ref + "Users from commercial enterprises have to license the TRANSFAC database and accompanying programs. \n";
                     ref = ref + "Please read the DISCLAIMER at http://www.gene-regulation.com/pub/databases/transfac/disclaimer.html\n\n";
                     JOptionPane.showMessageDialog(MainFrame.this, ref);*/
          MotifScannerDialog scand = new MotifScannerDialog(MainFrame.this);
          scand.setLocation(200, 200);
          scand.setVisible(true);
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
        //}
      }
    });
    pwmMenu.add(scannerItem);

    JMenuItem locatorItem = new JMenuItem("MotifLocator");
    locatorItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MotifLocatorDialog locd = new MotifLocatorDialog(MainFrame.this);
        locd.setLocation(200, 200);
        locd.setVisible(true);
      }
    });
    pwmMenu.add(locatorItem);

    JMenuItem cloverItem = new JMenuItem("Clover");
    cloverItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        CloverDialog locd = new CloverDialog(MainFrame.this);
        locd.setLocation(200, 200);
        locd.setVisible(true);
      }
    });
    motifsMenu.add(cloverItem);

    
    JMenuItem moduleSearcherItem = new JMenuItem("ModuleSearcher");
    moduleSearcherItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (gl.list.size() > 1) {
          ModuleSearcherDialog md = new ModuleSearcherDialog(MainFrame.this);
          md.setLocation(200, 200);
          md.setVisible(true);
        }
        else {
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "At least 2 sequences are needed for this action",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
        }
      }
    });
    moduleMenu.add(moduleSearcherItem);

    JMenuItem moduleScannerItem = new JMenuItem("ModuleScanner");
    moduleScannerItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        System.out.println("gl=" + gl + "|" + gl.list.size());
        /*        if(gl!=null && gl.list.size()>0){
                  JOptionPane.showMessageDialog(MainFrame.this,
                                                "Sorry, but a clean window is needed to display the results. Either do \"Clear all\", or open a new TOUCAN instance.",
                                                "Error",
                                                JOptionPane.ERROR_MESSAGE);
                }
                else{*/
        ModuleScannerDialog md = new ModuleScannerDialog(MainFrame.this);
        md.setLocation(200, 200);
        md.setVisible(true);
        //}
      }
    });
    moduleMenu.add(moduleScannerItem);
    //motifsMenu.addSeparator();

    JMenuItem samplerItem = new JMenuItem("MotifSampler");
    samplerItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (gl.getMaxLength() > MAXLENGTH) {
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "Maximum length of an individual sequence is " +
                                        MAXLENGTH +
                                        "\n Scanning longer sequences is of no use anyway",
                                        "Missing values",
                                        JOptionPane.ERROR_MESSAGE);
        }
        else if (gl.list.size() < 2) {
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "At least 2 sequences are needed for this action",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
        }
        else {
          MotifSamplerDialog md = new MotifSamplerDialog(MainFrame.this);
          md.setLocation(200, 200);
          md.setVisible(true);
        }
      }
    });
    denovoMenu.add(samplerItem);
    //motifsMenu.addSeparator();

    JMenuItem oligoanalysisItem = new JMenuItem("RSAT: oligo-analysis");
    oligoanalysisItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (gl.getMaxLength() > MAXLENGTH) {
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "Maximum length of an individual sequence is " +
                                        MAXLENGTH +
                                        "\n Scanning longer sequences is of no use anyway",
                                        "Missing values",
                                        JOptionPane.ERROR_MESSAGE);
        }
        /*else if (gl.list.size() < 2) {
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "At least 2 sequences are needed for this action",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
        }*/
        else {
          OligoAnalysisDialog oad = new OligoAnalysisDialog(MainFrame.this);
          oad.setLocation(200, 200);
          oad.setVisible(true);
        }
      }
    });
    denovoMenu.add(oligoanalysisItem);
    //motifsMenu.addSeparator();
    
    JMenuItem footItem = new JMenuItem("FootPrinter 2.0");
    footItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (gl.list.size() > 1) {
          /*String ref = "Blanchette, M. and Tompa, M. (2002) \n";
                     ref = ref + "Discovery of Regulatory Elements by a Computational Method for Phylogenetic Footprinting. \n";
                     ref = ref + "Genome Research, 12(5), 739-748  \n\n";
                     ref = ref +
              "Blanchette, M., Schwikowski, B., and Tompa, M. (2002) \n";
           ref = ref + "Algorithms for Phylogenetic Footprinting. \n";
           ref = ref + "Journal of Computational Biology, 9(2), 211-223. \n";
                     JOptionPane.showMessageDialog(MainFrame.this, ref);*/
          FootPrinterDialog fd = new FootPrinterDialog(MainFrame.this);
          fd.setLocation(200, 200);
          fd.setVisible(true);
        }
        else {
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "At least 2 sequences are needed for this action",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
        }
      }
    });
    //motifsMenu.add(footItem);
    //motifsMenu.addSeparator();

    //JMenu statsMenu = new JMenu("Statistics");

//    JMenuItem statsItem = new JMenuItem("Setwise over-represented");
    JMenuItem statsItem = new JMenuItem("Stat. Over-rep. (binomial)");
    statsItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Stats statFrame = new Stats(MainFrame.this);
      }
    });
    //  statsMenu.add(statsItem);
    pwmMenu.add(statsItem);

    JMenuItem metaStatsItem = new JMenuItem("Multiple-hit over-rep");
    metaStatsItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MetaStats statFrame = new MetaStats(MainFrame.this);
      }
    });
//statsMenu.add(metaStatsItem);
//motifsMenu.add(statsMenu);

//Alignments
    JMenuItem alignItem = new JMenuItem("AVID");
    alignItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (gl.list.size() > 1) {
          /*String ref = "";
                     ref = ref + "Bray N, Dubchak I, Pachter L.\n";
                     ref = ref + "AVID: A global alignment program. \n";
                     ref = ref + "Genome Res. 2003 Jan;13(1):97-102.\n\n";
                     ref = ref + "Mayor C, Brudno M, Schwartz JR, Poliakov A, Rubin EM, Frazer KA, Pachter LS, Dubchak I.  \n";
                     ref = ref +
           "VISTA: Visualizing Global DNA Sequence Alignments of Arbitrary Length. \n";
                     ref = ref + "Bioinformatics. 2000 Nov;16(11):1046-7. \n";
                     JOptionPane.showMessageDialog(MainFrame.this, ref);
           */
          VistaDialog dd = new VistaDialog(MainFrame.this);
          dd.setLocation(200, 200);
          dd.setVisible(true);
        }
        else {
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "At least 2 sequences are needed for this action",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
        }
      }
    });
    alignMenu.add(alignItem);

    JMenuItem laganItem = new JMenuItem("LAGAN");
    laganItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (gl.list.size() > 1) {
          LaganDialog dd = new LaganDialog(MainFrame.this);
          dd.setLocation(200, 200);
          dd.setVisible(true);
        }
        else {
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "At least 2 sequences are needed for this action",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
        }
      }
    });
    alignMenu.add(laganItem);

    JMenuItem blastzItem = new JMenuItem("BLASTZ");
    blastzItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (gl.list.size() > 1) {
          BlastzDialog dd = new BlastzDialog(MainFrame.this);
          dd.setLocation(200, 200);
          dd.setVisible(true);
        }
        else {
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "At least 2 sequences are needed for this action",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
        }
      }
    });
//    diMenu.add(blastzItem);

    JMenuItem pairsItem = new JMenuItem("All pairwise");
    pairsItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (gl.list.size() < 1) {
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "At least 2 sequences are needed for this action",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
        }
        else if (!gl.containsPairs()) {
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "Sorry, no pairs found.", "Error",
                                        JOptionPane.ERROR_MESSAGE);
        }
        else {
          PairsDialog pd = new PairsDialog(MainFrame.this);
          pd.setLocation(200, 200);
          pd.setVisible(true);
        }
      }
    });
    alignMenu.add(pairsItem);

    JMenuItem multItem = new JMenuItem("Multiple");
    multItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (gl.list.size() < 1) {
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "At least 2 sequences are needed for this action",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
        }
        else if (!gl.containsFamilies()) {
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "Sorry, no families found.", "Error",
                                        JOptionPane.ERROR_MESSAGE);
        }
        else {
          MultiAlignDialog md = new MultiAlignDialog(MainFrame.this);
          md.setLocation(200, 200);
          md.setVisible(true);
        }
      }
    });
    //alignMenu.add(multItem);

    JMenuItem cpgItem = new JMenuItem("CpG Island - Human");
    cpgItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G,
                                                  ActionEvent.CTRL_MASK));
    cpgItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (gl == null) {
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "Your genelist is empty!", "Error",
                                        JOptionPane.ERROR_MESSAGE);
        }
        else {
          try {
            gl.annotateCpG();
            setImg();
          }
          catch (Exception exc) {
            JOptionPane.showMessageDialog(MainFrame.this,
                                          "CpG island annotation failed",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            //exc.printStackTrace();
          }
        }
      }
    });
    toolsMenu.add(cpgItem);

    JMenuItem revComplItem = new JMenuItem("RevCompl Negatives");
    revComplItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          gl.reverseComplement();
          setImg();
        }
        catch (Exception exc) {
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "Reverse Complement failed", "Error",
                                        JOptionPane.ERROR_MESSAGE);
          //exc.printStackTrace();
        }
      }
    });
    toolsMenu.add(revComplItem);

    JMenuItem cutItem = new JMenuItem("Cut in pieces");
    cutItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        BNSelect sel = new BNSelect(MainFrame.this);
        sel.setLocation(200, 200);
        sel.setVisible(true);
      }
    });
    toolsMenu.add(cutItem);

    JMenuItem repeatMaskerItem = new JMenuItem("RepeatMasker");
    repeatMaskerItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        
      	if (gl.list == null || gl.list.size() < 1) {
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "At least 1 sequence is needed for this action",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
        }
     else if (gl.getMaxLength() > 100000 || gl.getTotalLength() > 500000){
           JOptionPane.showMessageDialog(MainFrame.this,"Sorry, but for such long sequences or so many sequences, please use RepeatMasker locally. This job would overload our servers.","Problem",JOptionPane.ERROR_MESSAGE);
     }
        else {
          RepeatMaskerDialog md = new RepeatMaskerDialog(MainFrame.this);
          md.setLocation(200, 200);
          md.setVisible(true);
        }
      }
    });
    toolsMenu.addSeparator();
    toolsMenu.add(repeatMaskerItem);

    zoomInItem = new JMenuItem("Zoom In");
    zoomInItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,
        ActionEvent.CTRL_MASK));
    zoomInItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (gl == null) {
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "Your genelist is empty!", "Error",
                                        JOptionPane.ERROR_MESSAGE);
        }
        else {
          if (MainFrame.this.gl.minScale) {
            JOptionPane.showMessageDialog(MainFrame.this,
                                          "Sorry, maximal zooming reached.",
                                          "Info",
                                          JOptionPane.INFORMATION_MESSAGE);
          }
          else {
            try {
              scalingFactor = scalingFactor * 2;
              MainFrame.this.writeToStatus("scalingFactor =" + scalingFactor);
              setImgSelectedFeatures();
            }
            catch (Exception exc) {
              JOptionPane.showMessageDialog(MainFrame.this, "Scaling failed",
                                            "Error", JOptionPane.ERROR_MESSAGE);
              //exc.printStackTrace();
            }
          }
        }
      }
    });
    viewMenu.add(zoomInItem);

    zoomOutItem = new JMenuItem("Zoom Out");
    zoomOutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS,
        ActionEvent.CTRL_MASK));
    zoomOutItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (gl == null) {
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "Your genelist is empty!", "Error",
                                        JOptionPane.ERROR_MESSAGE);
        }
        else {
          try {
            scalingFactor = scalingFactor * 0.5d;
            MainFrame.this.writeToStatus("scalingFactor =" + scalingFactor);
            setImgSelectedFeatures();
          }
          catch (Exception exc) {
            JOptionPane.showMessageDialog(MainFrame.this, "Scaling failed",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            //exc.printStackTrace();
          }
        }
      }
    });
    viewMenu.add(zoomOutItem);

    normalizeItem = new JMenuItem("Feature heights");
    normalizeItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (gl == null) {
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "Your genelist is empty!", "Error",
                                        JOptionPane.ERROR_MESSAGE);
        }
        else {
          try {
            gl.normalizeScores("oligo-analysis");
            gl.normalizeScores("MotifLocator");
            gl.normalizeScores("ModuleScanner");
            gl.normalizeScores("MotifSampler");
            gl.normalizeScores("MotifScanner");
            
            MainFrame.this.writeToStatus("Scores normalized");
            setImgSelectedFeatures();
          }
          catch (Exception exc) {
            JOptionPane.showMessageDialog(MainFrame.this, "Normalization failed",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            exc.printStackTrace();
          }
        }
      }
    });
    viewMenu.add(normalizeItem);
    
    //ensembl menu
    //JMenuItem newListItem = new JMenuItem("New/Add");
    JMenuItem ensemblItem = new JMenuItem("Ensembl/BioMart");
    ensemblItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
        ActionEvent.CTRL_MASK));
    ensemblItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        LoadSeq load = new LoadSeq(MainFrame.this);
        load.setLocation(200, 200);
        load.setVisible(true);
      }
    });
    
    /*getInfoItem = new JMenuItem("Get Info");
    getInfoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
        ActionEvent.CTRL_MASK));
    getInfoItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (e.getSource() == getInfoItem) {
          GeneInfo ginfo = new GeneInfo(MainFrame.this);
          ginfo.setLocation(200, 200);
          ginfo.setVisible(true);
        }
      }
    });
    ensemblMenu.add(getInfoItem);
*/

    //XEMBL menu
    JMenuItem newXemblItem = new JMenuItem("New/Add");
    newXemblItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        LoadXembl load = new LoadXembl(MainFrame.this);
        load.setLocation(200, 200);
        load.setVisible(true);
      }
    });
    //xemblMenu.add(newXemblItem);

    onlineMenu.add(ensemblItem);
    onlineMenu.add(newXemblItem);

    annotateMenu.add(loadGffItem);
    
    motifsMenu.add(pwmMenu);
    motifsMenu.add(denovoMenu);
    motifsMenu.add(moduleMenu);
    
    
    menu.add(fileMenu);
    menu.add(viewMenu);
    menu.add(annotateMenu);
    menu.add(onlineMenu);
    menu.add(alignMenu);
    menu.add(motifsMenu);
    menu.add(toolsMenu);
    //menu.add(windowMenu);
    menu.add(helpMenu);

    //**************************************************************************

    listModel = new DefaultListModel();
    list = new JList(listModel);
    list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    JScrollPane listPane = new JScrollPane(list);
    listPane.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder("Subsequence list"),
        BorderFactory.createEmptyBorder(0, 0, 0, 0)));

    //popup menu

    subPop = new JPopupMenu();
    JMenuItem subDelItem = new JMenuItem("Remove");
    subDelItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object[] selected = list.getSelectedValues();
        Gene g;
        int found = -1;
        int ind = 0;
        for (Iterator it = subList.list.iterator(); it.hasNext(); ) {
          g = (Gene) it.next();
          ind++;
        }
        for (int i = 0; i < selected.length; i++) {
          for (Iterator it = subList.list.iterator(); it.hasNext(); ) {
            g = (Gene) it.next();
            //System.out.println(g.name+"|"+selected[i]);
            if (g.name.equals( (String) selected[i])) {
              found = subList.list.indexOf(g);
              listModel.removeElement(selected[i]);
              break;
            }
          }
          subList.remove(found);
        }
      }
    });
    subPop.add(subDelItem);

    list.addMouseListener(new MouseListener() {
      public void mousePressed(MouseEvent e) {
        maybeShow(e);
      }

      public void mouseReleased(MouseEvent e) {
        maybeShow(e);
      }

      public void mouseExited(MouseEvent e) {
      }

      public void mouseEntered(MouseEvent e) {
      }

      public void mouseClicked(MouseEvent e) {
      }

      private void maybeShow(MouseEvent e) {
        if (e.isPopupTrigger() && subList.list.size() > 0) {
          if (list.getSelectedValues().length < 2) {
            Point p = e.getPoint();
            int index = list.locationToIndex(p);
            list.setSelectedIndex(index);
          }
          subPop.show(e.getComponent(), e.getX(), e.getY());
        }
      }
    });

    //*************************************************
     //feature popup menu
    featPop = new JPopupMenu();
    JMenuItem colorItem = new JMenuItem("Set Color");
    colorItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object[] selected = featShowList.getSelectedValues();
        //if (selected.length!=1)
        //    JOptionPane.showMessageDialog(MainFrame.this,"You can select only one feature \n for this action.","Error",JOptionPane.ERROR_MESSAGE);
        //else{
        java.awt.Color c = JColorChooser.showDialog(MainFrame.this,
            "Choose feature color", ( (Factor) selected[0]).objectColor);
        for (int i = 0; i < selected.length; ++i) {
          ( (Factor) selected[i]).setColor(c);
          try {
            setImgSelectedFeatures();
          }
          catch (Exception ex) { //ex.printStackTrace();
          }
        }
        //}
      }
    });
    featPop.add(colorItem);

    JMenuItem fillItem = new JMenuItem("Toggle fill");
    fillItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object[] selected = featShowList.getSelectedValues();
        int go = JOptionPane.NO_OPTION;
        boolean tooMuch = false;
        if (selected.length > 10 && gl.list.size() > 5) {
          tooMuch = true;
          go = JOptionPane.showConfirmDialog(MainFrame.this,
                                             "This may take a while (lots of genes and lots of features!) \n Continue?",
                                             "Warning",
                                             JOptionPane.WARNING_MESSAGE);
        }
        if (!tooMuch || go == JOptionPane.YES_OPTION) {
          Factor obj = null;
          for (int i = 0; i < selected.length; ++i) {
            obj = (Factor) selected[i];
            if (obj.objectFill) obj.objectFill = false;
            else obj.objectFill = true;
            try {
              setImgSelectedFeatures();
            }
            catch (Exception ex) { //ex.printStackTrace();
            }
          }
        }
      }
    });
    featPop.add(fillItem);

    JMenuItem featInfoItem = new JMenuItem("Info");
    featInfoItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object[] selected = featShowList.getSelectedValues();
        if (selected.length != 1)
          JOptionPane.showMessageDialog(MainFrame.this,
                                        "You can select only one feature \n for this action.",
                                        "Error",
                                        JOptionPane.ERROR_MESSAGE);
        else {
          Factor o = (Factor) selected[0];
          String infoStr = o.objectType + "\n" + "Type/id= " +
              o.objectDescriptor + "\n Source= " + o.objectSource;
          JOptionPane.showMessageDialog(MainFrame.this, infoStr, "Feature Info",
                                        JOptionPane.INFORMATION_MESSAGE);
        }
      }
    });
    featPop.add(featInfoItem);

    JMenuItem showItem = new JMenuItem("Show");
    showItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object[] selectedArr = featShowList.getSelectedValues();
        for (int i = 0; i < selectedArr.length; ++i) {
          //String key = ( (Factor) selectedArr[i]).objectDescriptor;
          //if (!currentlySelected.containsKey(key))
          //  currentlySelected.put(key, selectedArr[i]);
          ( (Factor) selectedArr[i]).objectShow = true;
        }
        try {
          MainFrame.this.setImgSelectedFeatures();
        }
        catch (Exception exc) {
          //exc.printStackTrace();
          JOptionPane.showMessageDialog(MainFrame.this, exc.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    });
    featPop.addSeparator();
    featPop.add(showItem);

    JMenuItem noShowItem = new JMenuItem("Don't show");
    noShowItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object[] selectedArr = featShowList.getSelectedValues();
        for (int i = 0; i < selectedArr.length; ++i) {
          //currentlySelected.remove( ( (Factor) selectedArr[i]).objectDescriptor);
          ( (Factor) selectedArr[i]).objectShow = false;
        }
        try {
          MainFrame.this.setImgSelectedFeatures();
        }
        catch (Exception exc) {
          //exc.printStackTrace();
          JOptionPane.showMessageDialog(MainFrame.this, exc.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    });
    featPop.add(noShowItem);

    JMenuItem removeFeatItem = new JMenuItem("Remove");
    removeFeatItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object[] selectedArr = featShowList.getSelectedValues();
        String id="";
        try {
          for (int i = 0; i < selectedArr.length; ++i) {
            id = ( (Factor) selectedArr[i]).objectDescriptor;
            System.out.println("To be removed: "+id);
            MainFrame.this.gl.removeFeatures(id);
            MainFrame.this.gl.removeObject(id);
          }
          //MainFrame.this.gl.printFeatures();
          MainFrame.this.setImgSelectedFeatures();
        }
        catch (Exception exc) {
          exc.printStackTrace();
          JOptionPane.showMessageDialog(MainFrame.this, exc.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    });
    featPop.add(removeFeatItem);

    JMenuItem moduleFeatItem = new JMenuItem("To ModuleScanner");
    moduleFeatItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object[] selectedArr = featShowList.getSelectedValues();
        String moduleStr="[";
        String temp;
        try {
          for (int i = 0; i < selectedArr.length; ++i) {
            temp = ( (Factor) selectedArr[i]).objectDescriptor;
            if(temp.startsWith("Mod")){
              temp = temp.substring(3,temp.length());
            }
            moduleStr += ",";
          }
          moduleStr = moduleStr.substring(0,moduleStr.length()-1) + "]";
          ModuleScannerDialog md = new ModuleScannerDialog(MainFrame.this);
          md.setLocation(200, 200);
          md.setVisible(true);
          md.txt3.setText(moduleStr);

          System.out.println(moduleStr);
        }
        catch (Exception exc) {
          exc.printStackTrace();
          JOptionPane.showMessageDialog(MainFrame.this, exc.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
      }
    });
    featPop.add(moduleFeatItem);
    
    JMenuItem annotFeatItem = new JMenuItem("AnnotateAgain");
    annotFeatItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	double threshold = Double.parseDouble(JOptionPane.showInputDialog(MainFrame.this,
                "Threshold (0.0-1.0)",
                "0.001"));
      	Object[] selectedArr = featShowList.getSelectedValues();
        String temp,o="";
        String mx = "";
        try {
          for (int i = 0; i < selectedArr.length; ++i) {
            o = ( (Factor) selectedArr[i]).objectDescriptor;
            mx+=gl.getCountMatrixStr((Factor) selectedArr[i]) + "\n";
          }
        
      PWMList pwmList = new PWMList();
      System.out.println("size of PWMList is "+pwmList.size());
      pwmList.constructFromMxString(mx);
	 	PWM pwm;
	 	for (Iterator it = pwmList.iterator();it.hasNext();){
	 		pwm = (PWM)it.next();
	 		gl.annotatePWM(pwm,threshold,"PWM-"+o);
	 	}	
	 	setImg();
        }
        catch (Exception exc) {
          exc.printStackTrace();
          JOptionPane.showMessageDialog(MainFrame.this, exc.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
      
        }
      });
    featPop.add(annotFeatItem);


    
    JMenuItem slidingFeatItem = new JMenuItem("Sliding window");
    slidingFeatItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	double threshold = Double.parseDouble(JOptionPane.showInputDialog(MainFrame.this,
                "minRatio",
                "0.8"));
      	int windowSize = Integer.parseInt(JOptionPane.showInputDialog(MainFrame.this,
                "Window size",
                "150"));
      	int stepSize = Integer.parseInt(JOptionPane.showInputDialog(MainFrame.this,
                "Step size",
                "10"));
      	Object[] selectedArr = featShowList.getSelectedValues();
        String temp,o="";
        ArrayList motifs = new ArrayList();
        try {
          for (int i = 0; i < selectedArr.length; ++i) {
            o = ( (Factor) selectedArr[i]).objectDescriptor;
            motifs.add(o);
          }
        gl.slidingWindow(motifs,windowSize,stepSize,threshold);
      
	 	setImg();
        }
        catch (Exception exc) {
          exc.printStackTrace();
          JOptionPane.showMessageDialog(MainFrame.this, exc.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
      
        }
      });
    featPop.add(slidingFeatItem);

    JMenuItem pairFeatItem = new JMenuItem("MotifPair_noOverlap");
    pairFeatItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	int windowSize = Integer.parseInt(JOptionPane.showInputDialog(MainFrame.this,
                "Window size",
                "100"));
        Object[] selectedArr = featShowList.getSelectedValues();
        String temp,o="";
        ArrayList motifs = new ArrayList();
        try {
          for (int i = 0; i < selectedArr.length; ++i) {
            o = ( (Factor) selectedArr[i]).objectDescriptor;
            motifs.add(o);
          }
        if(motifs.size()==2){
        	gl.annotateMotifPairPostNoOverlap((String)motifs.get(0),(String)motifs.get(1),windowSize,"name");
        }
      
	 	setImg();
        }
        catch (Exception exc) {
          exc.printStackTrace();
          JOptionPane.showMessageDialog(MainFrame.this, exc.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
      
        }
      });
    featPop.add(pairFeatItem);
    
    
    
//    JMenuItem mxItem = new JMenuItem("Count matrix");
  JMenu mxSubMenu = new JMenu("Count matrix");
  featPop.add(mxSubMenu);
  
  JMenuItem scdItem = new JMenuItem("INCLUSive format");
  scdItem.addActionListener(new ActionListener() {
    public void actionPerformed(ActionEvent e) {
      Object[] selectedArr = featShowList.getSelectedValues();
      String temp,o;
      String mx = "";
      try {
        for (int i = 0; i < selectedArr.length; ++i) {
          o = ( (Factor) selectedArr[i]).objectDescriptor;
          mx+=gl.getCountMatrixStrSCD((Factor) selectedArr[i]) + "\n";
          System.out.println(gl.getCountMatrixStrSCD((Factor) selectedArr[i]));
        }
      }
      catch (Exception exc) {
        exc.printStackTrace();
        JOptionPane.showMessageDialog(MainFrame.this, exc.getMessage(),
                                      "Error", JOptionPane.ERROR_MESSAGE);
      }
      InfoFrame f = new InfoFrame("Count matrices",mx);
      f.setVisible(true);
    }
  });
  mxSubMenu.add(scdItem);
  
  
  JMenuItem ahabItem = new JMenuItem("Ahab format");
    ahabItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object[] selectedArr = featShowList.getSelectedValues();
        String temp,o;
        String mx = "";
        try {
          for (int i = 0; i < selectedArr.length; ++i) {
            o = ( (Factor) selectedArr[i]).objectDescriptor;
            mx+=gl.getCountMatrixStr((Factor) selectedArr[i]) + "\n";
            System.out.println(gl.getCountMatrixStr((Factor) selectedArr[i]));
          }
        }
        catch (Exception exc) {
          exc.printStackTrace();
          JOptionPane.showMessageDialog(MainFrame.this, exc.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
        InfoFrame f = new InfoFrame("Count matrices",mx);
        f.setVisible(true);
      }
    });
    mxSubMenu.add(ahabItem);
    
    JMenuItem cbItem = new JMenuItem("ClusterBuster format");
    cbItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object[] selectedArr = featShowList.getSelectedValues();
        String temp,o;
        String mx = "";
        try {
          for (int i = 0; i < selectedArr.length; ++i) {
            o = ( (Factor) selectedArr[i]).objectDescriptor;
            mx+=gl.getCountMatrixStrCB((Factor) selectedArr[i]) + "\n";
            System.out.println(gl.getCountMatrixStrCB((Factor) selectedArr[i]));
          }
        }
        catch (Exception exc) {
          exc.printStackTrace();
          JOptionPane.showMessageDialog(MainFrame.this, exc.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
        InfoFrame f = new InfoFrame("Count matrices",mx);
        f.setVisible(true);
      }
    });
    mxSubMenu.add(cbItem);

    JMenuItem infoItem = new JMenuItem("Total infoContent");
    infoItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
      	Object[] selectedArr = featShowList.getSelectedValues();
        String temp,o;
        String mx = "";
        try {
          for (int i = 0; i < selectedArr.length; ++i) {
            o = ( (Factor) selectedArr[i]).objectDescriptor;
            mx+=gl.getCountMatrixStr((Factor) selectedArr[i]) + "\n";
          }
        
      PWMList pwmList = new PWMList();
      pwmList.constructFromMxString(mx);
      String str = "Total IC = "+pwmList.getTotalInfoContent()+"\n";
      str += "Total IC / # genes = "+pwmList.getTotalInfoContent()/gl.list.size() +"\n";
      str += "Average IC / # genes = "+(pwmList.getTotalInfoContent()/pwmList.size())/gl.list.size();
      
	 	InfoFrame f = new InfoFrame("Total information content",str);
        f.setVisible(true);
        }
        catch (Exception exc) {
          exc.printStackTrace();
          JOptionPane.showMessageDialog(MainFrame.this, exc.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
	 	
      }
      });
    featPop.add(infoItem);
    
    JMenuItem logoItem = new JMenuItem("Logos");
    logoItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        Object[] selectedArr = featShowList.getSelectedValues();
        String temp,o;
        String mx = "";
        try {
          for (int i = 0; i < selectedArr.length; ++i) {
            o = ( (Factor) selectedArr[i]).objectDescriptor;
            mx+=gl.getCountMatrixStr((Factor) selectedArr[i]) + "\n";
          }
        
      PWMList pwmList = new PWMList();
      pwmList.constructFromMxString(mx);
	 	PWM pwm;
	 	JFrame frame = new JFrame("PWM viewer");
	 	JPanel pane = new JPanel();
	 	JLabel lab;
	 	pane.setLayout(new GridLayout(pwmList.size()*3,1));
	 	for (Iterator it = pwmList.iterator();it.hasNext();){
	 		pwm = (PWM)it.next();
	 		WMPanel wmv = new WMPanel(pwm.wm);
	 		pane.add(new JLabel(pwm.name + "\t" + pwm.getMyInfoContent()));
	 		pane.add(wmv);
	 		pane.add(new JLabel(""));
	 	}	
		 frame.getContentPane().add(pane);
		 frame.pack();
		 frame.setVisible(true);
      
        }
        catch (Exception exc) {
          exc.printStackTrace();
          JOptionPane.showMessageDialog(MainFrame.this, exc.getMessage(),
                                        "Error", JOptionPane.ERROR_MESSAGE);
        }
      
        
        }
      });
    featPop.add(logoItem);

    //*************************************************

     //*************************************************
      //gene popup menu
    genePop = new JPopupMenu();
    JMenu subGeneMenu = new JMenu("Gene");
    genePop.add(subGeneMenu);
    
    JMenuItem selectItem = new JMenuItem("Cut");
    selectItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (featList != null) {
          //MainFrame.this.showDialog(featList);
          SelectDialog sel = new SelectDialog(MainFrame.this);
          sel.setLocation(200, 200);
          sel.setVisible(true);
        }
      }
    });
    subGeneMenu.add(selectItem);

    JMenuItem removeItem = new JMenuItem("Remove");
    removeItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (JOptionPane.showConfirmDialog(fc,
                                          "Sure to delete this sequence?",
                                          "Confirm",
                                          JOptionPane.YES_NO_OPTION) ==
            JOptionPane.YES_OPTION) {
          try {
            gl.remove(MainFrame.this.selectedGene - 1);
            if (gl.list.size() == 0) {
              cls();
            }
            else {
              //MainFrame.this.gl.updateObjectMap(true);
              MainFrame.this.setImgSelectedFeatures();
            }
          }
          catch (Exception exc) {
            exc.printStackTrace();
          }
        }
      }
    });
    subGeneMenu.add(removeItem);

    JMenuItem rcItem = new JMenuItem("RevCompl");
    rcItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          Gene g = (Gene) gl.list.get(selectedGene - 1);
          g.reverseComplement();
          MainFrame.this.setImgSelectedFeatures();
        }
        catch (Exception exc) {}
      }
    });
    subGeneMenu.add(rcItem);

    JMenuItem clipItem = new JMenuItem("Copy fastA to clipboard");
    clipItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          Gene g = (Gene) gl.list.get(selectedGene - 1);
          StringBuffer buf = new StringBuffer();
          buf.append(">");
          if (!g.ensembl.equals("")) buf.append(g.ensembl);
          else buf.append(g.name);
          buf.append("\n");
          buf.append(g.seq.seqString());
          buf.append("\n");
          StringSelection sel = new StringSelection(buf.toString());
          clipboard.setContents(sel, MainFrame.this);
        }
        catch (Exception exc) {}
      }
    });
    subGeneMenu.add(clipItem);
    
    JMenuItem viewSeqItem = new JMenuItem("View sequence");
    viewSeqItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          Gene g = (Gene) gl.list.get(selectedGene - 1);
          StringBuffer buf = new StringBuffer();
          buf.append(">");
          if (!g.ensembl.equals("")) buf.append(g.ensembl);
          else buf.append(g.name);
          buf.append("\n");
          buf.append(g.seq.seqString());
          buf.append("\n");
//          StringSelection sel = new StringSelection(buf.toString());
          InfoFrame f = new InfoFrame("Sequence for "+g.name,buf.toString());
          f.setVisible(true);
          //clipboard.setContents(sel, MainFrame.this);
        }
        catch (Exception exc) {}
      }
    });
    subGeneMenu.add(viewSeqItem);

    JMenuItem exonItem = new JMenuItem("Remove other exons");
    exonItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          Gene g = (Gene) gl.list.get(selectedGene - 1);
          g.removeOtherExons();
          MainFrame.this.setImg();
        }
        catch (Exception exc) {}
      }
    });
    subGeneMenu.add(exonItem);

    JMenuItem slItem = new JMenuItem("Copy to sublist");
    slItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        try {
          Gene g = (Gene) gl.list.get(selectedGene - 1);
          MainFrame.this.addToNameList(g.name);
          MainFrame.this.subList.add((Gene)g.clone());
        }
        catch (Exception exc) {}
      }
    });
    subGeneMenu.add(slItem);
    
    featSubMenu = new JMenu("View feature seq");
    genePop.add(featSubMenu);
    /*JMenuItem item;
    System.out.println(featList.size());
    
    if(featList!=null && featList.size()>0){
	    String[] featStrings = new String[featList.size()];
		for (int i=0;i<featList.size();i++){
		 Feature f = (Feature)featList.get(i);
		 if (f.getType().equalsIgnoreCase("misc_feature")) featStrings[i] = (String)f.getAnnotation().getProperty("type");
		 else featStrings[i] = f.getType();
		}
		for (int i=0;i<featStrings.length;i++){
			featIndex=i;
			item=new JMenuItem(featStrings[i]);
			item.addActionListener(new ActionListener() {
			      public void actionPerformed(ActionEvent e) {
			        try {
			        	Gene g = (Gene) gl.list.get(selectedGene - 1);	
			        	Feature f = (Feature)featList.get(featIndex);
			        	InfoFrame infoFrame = new InfoFrame("Sequence of selected feature",g.seq.subStr(f.getLocation().getMin(),f.getLocation().getMax()));
			        	infoFrame.setVisible(true);
			        	
			        }
			        catch (Exception exc) {}
			      }
			    });
			
			featSubMenu.add(item);
		}
    }*/

    //*************************************************
     //Feature list left
    featShowListModel = new DefaultListModel();
    featShowList = new JList(featShowListModel);
    featShowList.setCellRenderer(new CustomCellRenderer(MainFrame.this));
    featShowList.setSelectionMode(ListSelectionModel.
                                  MULTIPLE_INTERVAL_SELECTION);
    JScrollPane featPane = new JScrollPane(featShowList);
    featPane.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder("Feature list"),
        BorderFactory.createEmptyBorder(0, 0, 0, 0)));
    featShowList.addKeyListener(new KeyListener() {
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          Object[] selectedArr = featShowList.getSelectedValues();
          //currentlySelected = new TreeMap();
          Factor temp;
          for (Iterator it = gl.getObjects().keySet().iterator(); it.hasNext(); ) {
            temp = (Factor) gl.getObjects().get(it.next());
            temp.objectShow = false;
          }
          for (int i = 0; i < selectedArr.length; ++i) {
            //  currentlySelected.put( ( (Factor) selectedArr[i]).objectDescriptor,
            //                        selectedArr[i]);
            ( (Factor) selectedArr[i]).objectShow = true;
          }

          try {
            MainFrame.this.setImgSelectedFeatures();
          }
          catch (Exception exc) {
            //exc.printStackTrace();
            JOptionPane.showMessageDialog(MainFrame.this, exc.getMessage(),
                                          "Error", JOptionPane.ERROR_MESSAGE);
          }
        }
      }

      public void keyReleased(KeyEvent e) {
      }

      public void keyTyped(KeyEvent e) {
      }
    });
    //add popuplistener
    featShowList.addMouseListener(new MouseListener() {
      public void mousePressed(MouseEvent e) {
        maybeShow(e);
      }

      public void mouseReleased(MouseEvent e) {
        maybeShow(e);
      }

      public void mouseExited(MouseEvent e) {
      }

      public void mouseEntered(MouseEvent e) {
      }

      public void mouseClicked(MouseEvent e) {
      }

      private void maybeShow(MouseEvent e) {
        if (e.isPopupTrigger()) {
          if (featShowList.getSelectedValues().length < 2) {
            Point p = e.getPoint();
            int index = featShowList.locationToIndex(p);
            featShowList.setSelectedIndex(index);
          }
          featPop.show(e.getComponent(), e.getX(), e.getY());
        }
      }
    });

    topleftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listPane, featPane);

    //start with empty pane...
    img = new JLabel();
    picturePane = new DroppableScrollPane(this,img);

    picturePane.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder("Sequence set"),
        BorderFactory.createEmptyBorder(0, 0, 0, 0)));

    splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                               topleftPane, picturePane);
    splitPane.setOneTouchExpandable(true);
    //splitPane.setDividerLocation(150);

    geneText = new JTextArea("Left mouse click on a gene to display info.");
    geneText.setLineWrap(true);
    geneText.setEditable(false);
    JScrollPane geneScrollPane = new JScrollPane(geneText);
    geneScrollPane.setVerticalScrollBarPolicy(JScrollPane.
                                              VERTICAL_SCROLLBAR_AS_NEEDED);
    geneScrollPane.setHorizontalScrollBarPolicy(JScrollPane.
                                                HORIZONTAL_SCROLLBAR_NEVER);
    geneScrollPane.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder("Gene Info"),
        BorderFactory.createEmptyBorder(0, 0, 0, 0)));

    featText = new JTextArea("Left mouse click on a feature to display info.");
    featText.setLineWrap(true);
    featText.setEditable(false);
    JScrollPane featScrollPane = new JScrollPane(featText);
    featScrollPane.setVerticalScrollBarPolicy(JScrollPane.
                                              VERTICAL_SCROLLBAR_AS_NEEDED);
    featScrollPane.setHorizontalScrollBarPolicy(JScrollPane.
                                                HORIZONTAL_SCROLLBAR_NEVER);
    featScrollPane.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder("Feature Info"),
        BorderFactory.createEmptyBorder(0, 0, 0, 0)));

    statusText.setLineWrap(true);
    statusText.setEditable(false);
    statusPane.setVerticalScrollBarPolicy(JScrollPane.
                                          VERTICAL_SCROLLBAR_AS_NEEDED);
    statusPane.setHorizontalScrollBarPolicy(JScrollPane.
                                            HORIZONTAL_SCROLLBAR_NEVER);
    statusPane.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createTitledBorder("Status"),
        BorderFactory.createEmptyBorder(0, 0, 0, 0)));

    JSplitPane geneFeatPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                             geneScrollPane,
                                             featScrollPane);
    geneFeatPane.setDividerLocation(scrSize.width / 2);
    //infoPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, geneScrollPane,
    //                          featScrollPane);
    infoPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, geneFeatPane,
                              statusPane);
    infoPane.setDividerLocation( (scrSize.height * 10) / 100);
    infoPane.setOneTouchExpandable(true);
    newPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                             splitPane, infoPane);
    //newPane.setDividerSize(5);
    newPane.setDividerLocation( (scrSize.height * 75) / 100);
    newPane.setOneTouchExpandable(true);

    //Provide minimum sizes for the two components in the split pane
    Dimension minimumSize = new Dimension(50, 50);
    listPane.setMinimumSize(minimumSize);
    picturePane.setMinimumSize(minimumSize);

    //Provide a preferred size for the split pane
    newPane.setPreferredSize(new Dimension(400, 200));
    //newPane.setMinimumSize(new Dimension(400, 350));
    infoPane.setPreferredSize(new Dimension(50, 20));

    getContentPane().add(newPane);

    //newPane.setDividerLocation(480);

    //mouse stuff
    picturePane.addMouseListener(new MouseAdapter() {
    
    	private Point pressed;
    	
    	public void mouseClicked(MouseEvent e) {
      }

      public void mousePressed(MouseEvent e) {
        //all clicks
        pressed = e.getPoint();
        //setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        pressed.x = pressed.x + picturePane.getViewport().getViewPosition().x;
        pressed.y = pressed.y + picturePane.getViewport().getViewPosition().y;
        MainFrame.this.selectedGene = MainFrame.this.getGeneNr(pressed);
        MainFrame.this.selectedBp = MainFrame.this.getBp(pressed);
        try {
          MainFrame.this.displayGeneInfo(selectedGene);
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
        MainFrame.this.displayFeatInfo(selectedGene, selectedBp);
        //right click
        if (0 < selectedGene && selectedGene <= gl.list.size() &&
            e.isPopupTrigger()) {
        		genePop.show(e.getComponent(), e.getX(), e.getY());
        		
        		//dynamically put features in submenu
        		//featSubMenu = new JMenu("View feature seq");
        		featSubMenu.removeAll();
        		JMenuItem item;
        		if(featList!=null && featList.size()>0){
        		    String[] featStrings = new String[featList.size()];
        			for (int i=0;i<featList.size();i++){
        			 Feature f = (Feature)featList.get(i);
        			 if (f.getType().equalsIgnoreCase("misc_feature")) featStrings[i] = (String)f.getAnnotation().getProperty("type");
        			 else featStrings[i] = f.getType();
        			}
        			for (int i=0;i<featStrings.length;i++){
        				featIndex=i;
        				item=new JMenuItem(featStrings[i]);
        				item.addActionListener(new ActionListener() {
        				      public void actionPerformed(ActionEvent e) {
        				        try {
        				        	Gene g = (Gene) gl.list.get(selectedGene - 1);	
        				        	Feature f = (Feature)featList.get(featIndex);
        				        	InfoFrame infoFrame = new InfoFrame("Sequence of selected feature",g.seq.subStr(f.getLocation().getMin(),f.getLocation().getMax()));
        				        	infoFrame.setVisible(true);
        				        	
        				        }
        				        catch (Exception exc) {}
        				      }
        				    });
        				featSubMenu.add(item);
        			}
        	    }
        		
        }
        //left click + ctrl
        else {
          if (e.isControlDown()) {
            if (featList != null) MainFrame.this.showDialog(featList);
          }
          else if (e.isAltDown()) {
            if (featList != null) MainFrame.this.showDialog(featList);
          }
        }
      }

      public void mouseReleased(MouseEvent e) {
        if (0 < selectedGene && selectedGene <= gl.list.size() && e.isPopupTrigger()) 
        		genePop.show(e.getComponent(), e.getX(), e.getY());
        else if (0 < selectedGene && selectedGene <= gl.list.size()){
        		//Node node = new Node(pressed.x, pressed.y, e.getX() - pressed.x, e.getY() - pressed.y, Color.BLACK);
			//nodes.add(node);
        		//img.p.paintComponents(node);
			setCursor(Cursor.getDefaultCursor());
			//repaint();        	
        }
        		
      }
    });

  } // end constructor

  

/*class Node extends Rectangle {
	
	private Color color;
	
	Node(int x, int y, int width, int height, Color color) {
		super(x, y, width, height);
		this.color = color;
	}
	
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(color);
		g2.draw(this);
	}
}*/
  
  
  public void cls() {
    img.setIcon(null);
    listModel.clear();
    gl = new GeneList();
    subList = new GeneList();
    featShowListModel.clear();
    featText.replaceRange("", 0, featText.getText().length());
    geneText.replaceRange("", 0, geneText.getText().length());
    this.clearStatus();
  }

  public void setImg() throws Exception {
    if (gl.list.size() == 0)return;
    currentlySelected = fillFeatShowList();
    BufferedImage featImg = gl.createImg(" ", scalingFactor, currentlySelected, false);
    ImageIcon i = new ImageIcon(featImg);
    img.setIcon(i);
    img.setVerticalAlignment(JLabel.TOP);
    img.setPreferredSize(new Dimension(i.getIconWidth(),
                                       i.getIconHeight()));
    img.revalidate();
  }

  public void setImgSelectedFeatures() throws Exception {
    BufferedImage featImg = this.gl.createImg(" ", scalingFactor,
                                              currentlySelected, false);
    ImageIcon i = new ImageIcon(featImg);
    img.setIcon(i);
    img.setVerticalAlignment(JLabel.TOP);
    img.setPreferredSize(new Dimension(i.getIconWidth(),
                                       i.getIconHeight()));
    img.revalidate();
    fillFeatShowList();
    featShowList.updateUI();
    //featShowList.revalidate();
  }

  public TreeMap fillFeatShowList() {
    featShowListModel.clear();
    TreeMap objMap = this.gl.getObjects();
    for (Iterator i = objMap.keySet().iterator(); i.hasNext(); ) {
      Factor myObj = (Factor) objMap.get(i.next());
      this.featShowListModel.addElement(myObj);
    }
    featShowList.updateUI();
    return objMap;
  }

  public void exportImg(String path) throws Exception {
    BufferedImage featImg = gl.createImg(" ", scalingFactor, currentlySelected, true);
    if (path.endsWith("jpg"))
      toucan.util.Tools.writeImgToFile(featImg, path, "jpg");
    else if (path.endsWith("tiff"))
      toucan.util.Tools.writeImgToFile(featImg, path, "tiff");
    else
      toucan.util.Tools.writeImgToFile(featImg, path, "png");
  }

  public void setGeneList(String path) throws Exception {
    gl = null;
    System.gc();
    gl = new GeneList();
    if (path.endsWith(".embl"))
      gl.constructFromLargeEmblFile(path);
    else if (path.endsWith(".tfa") || path.endsWith("fasta"))
      gl.constructFromLargeFastaFile(path);
    else if (path.endsWith(".gb"))
      gl.constructFromLargeGbFile(path);
    else if (path.endsWith(".bin"))
      gl = GeneList.deserialize(new File(path));
    else
      JOptionPane.showMessageDialog(MainFrame.this,
                                    "Only .embl, .tfa, .fasta, and .gb files",
                                    "Error", JOptionPane.ERROR_MESSAGE);

    subList = null;
    subList = new GeneList();
    gl.updateGeneNames();//to replace _ by % because _ is used in the sublist
  }

  public void displayGeneInfo(int geneNr) throws Exception {
    Gene g;
    try {
      g = (Gene) gl.list.get(geneNr - 1);
    }
    catch (Exception e) {
      return;
    }
    geneText.replaceRange("", 0, geneText.getText().length());
    if (g.seq == null) geneText.append("No sequence for this gene");
    else {
      geneText.append("Name=" + g.name + "\n");
      if (g.hugo != null && !g.hugo.equals("")) geneText.append("HUGO=" +
          g.hugo + "\n");
      if (g.locusLink != null && !g.locusLink.equals("")) geneText.append(
          "locusLink=" + g.locusLink + "\n");
      if (g.ensembl != null && !g.ensembl.equals("")) geneText.append(
          "ensembl=" + g.ensembl + "\n");
      geneText.append("\nAnnotation:\n\n");
      for (Iterator ai = g.seq.getAnnotation().asMap().entrySet().iterator();
           ai.hasNext(); ) {
        Map.Entry me = (Map.Entry) ai.next();
        if (me.getKey().toString() != "XX" && me.getValue().toString() != "" &&
            me.getValue().toString() != null) {
          geneText.append("" + me.getKey());
          geneText.append("\t");
          geneText.append("" + me.getValue());
          geneText.append("\r\n");
        }
      }
      //test
      geneText.append("\n");
      if (g.parent != null) geneText.append("Parent of " + g.ensembl + " is " +
                                            g.parent.ensembl + "\n");
      if (g.orthoList.list.size() > 0) {
        geneText.append("Children (orthologs) of " + g.name + " are ");
        for (Iterator it2 = g.orthoList.list.iterator(); it2.hasNext(); ) {
          geneText.append( ( (Gene) it2.next()).name + ",");
        }
      }
      if (g.orthoList.list.size() == 0 && g.parent == null) {
        geneText.append("No ortho-family relations known for this gene.\n");
      }
    }

  }

  public void displayFeatInfo(int geneNr, int bp) {
  	Gene g;
    try {
      g = (Gene) gl.list.get(geneNr - 1);
    }
    catch (Exception e) {
      return;
    }
    featText.replaceRange("", 0, featText.getText().length());
    featList = g.getOverlappingFeatures(bp);
    for (Iterator it = featList.iterator(); it.hasNext(); ) {
      Feature f = (Feature) it.next();
      featText.append(f.getType());
      featText.append("\r\n");
      featText.append("Source: " + f.getSource());
      featText.append("\r\n");
      featText.append(f.getLocation().toString());
      featText.append("\r\n");
      for (Iterator ai = f.getAnnotation().asMap().entrySet().iterator();
           ai.hasNext(); ) {
        Map.Entry me = (Map.Entry) ai.next();
        if (me.getKey().toString() != "XX" && me.getValue() != null &&
            me.getValue().toString() != "" && me.getValue().toString() != null) {
          featText.append("" + me.getKey());
          featText.append(" : ");
          featText.append("" + me.getValue());
          featText.append("\r\n");
        }
      }
      featText.append("\r\n");
    }
  }

  public void setStatus(String status) {
    statusText.replaceRange("Status: ", 0, statusText.getText().length());
    statusText.append(status);
  }

  public int getGeneNr(Point p) {
    int nr = 0;
    double temp = (p.getY() - gl.vertOffSet + gl.geneSpacing / 2) /
        gl.geneSpacing;
    return new Double(temp).intValue() + 1;
  }

  public int getBp(Point p) {
    int bp = 0;
    double temp = (p.getX() - gl.horOffSet - gl.horPaneOffSet) * gl.scale;
    return new Double(temp).intValue() + 1;
  }

//  public static Gene getSelectedGene() {
  public Gene getSelectedGene() {
    return (Gene) gl.list.get(selectedGene - 1);
  }

  public void showDialog(ArrayList featList) {
    SelectDialog sel = new SelectDialog(MainFrame.this);
    sel.setLocation(200, 200);
    sel.setVisible(true);
  }

//  public static void addToNameList(String name) {
  public void addToNameList(String name) {
    listModel.addElement(name);
  }

  public void lostOwnership(Clipboard clipboard, Transferable contents) {
    //writeToStatus("Clipboard contents replaced");
  }

  public MainFrame newWindow() throws Exception {
    MainFrame f = new MainFrame();
    System.out.println("homedir="+homeDir);
    f.homeDir = homeDir;
    f.setVisible(true);
    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    return f;
  }

  public static void main(String args[]) throws Exception {
    MainFrame f = new MainFrame();
    f.setVisible(true);
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    //JOptionPane.showMessageDialog(f, "Important notification:\nThe services may be DOWN at certain moments between Monday 6 Sep and Friday 10 Sep \ndue to electrical power interruptions in the building. \nWe're very sorry for this inconvenience.",
    //                              "Notice", JOptionPane.INFORMATION_MESSAGE);

  }

  public void writeToStatus(String string) {
    if (string != null) {
      if (statusText != null) statusText.append(string + "\n");
      if (statusPane != null) statusPane.getVerticalScrollBar().setValue(
          statusText.getHeight() -
          statusText.getVisibleRect().
          height);
    }
  }

  public void clearStatus() {
    statusText.setText("");
  }

  public void actionPerformed(ActionEvent e) {

  }

  public void drop(DropTargetDropEvent event) {

    if ((event.getSourceActions() & DnDConstants.ACTION_COPY) != 0)
            event.acceptDrop(DnDConstants.ACTION_COPY);
        else {
            event.rejectDrop();
            System.out.println("REJECT DROP");
            return;
        }

        Transferable transferable = event.getTransferable();
        DataFlavor[] flavors = event.getCurrentDataFlavors();

      for (int i = 0; i < flavors.length; i++)   {
                 DataFlavor dataFlavor = flavors[i];
         try  {
            if (dataFlavor.equals(DataFlavor.javaFileListFlavor)) {
                                java.util.List fileList = (java.util.List) transferable.getTransferData(dataFlavor);
                setGeneList(((File) fileList.get(0)).getPath()+((File) fileList.get(0)).getName());
                System.out.println("Drop file");
                                event.dropComplete(true);
                                return;
            }
            else if (dataFlavor.equals(DataFlavor.stringFlavor))  {
                String s = (String) transferable.getTransferData(dataFlavor);
                setGeneList(s);
                System.out.println("Drop string");
                event.dropComplete(true);
                                return;
            }
         } catch(Exception e)  {
           e.printStackTrace();
                        event.dropComplete(false);
                        return;
         }
      }
      event.dropComplete(false);
  }


    public void dragEnter(DropTargetDragEvent e) { }
    public void dragExit(DropTargetEvent e) { }
    public void dragOver(DropTargetDragEvent e) { }
    public void dropActionChanged(DropTargetDragEvent e) { }



  public void setGeneList(GeneList gl) throws Exception{
    this.gl = gl;
    gl.updateGeneNames();
  }

}
