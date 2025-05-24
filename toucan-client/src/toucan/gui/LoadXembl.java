
/*This file is part of TOUCAN
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
import javax.swing.table.*;
import java.util.*;
import toucan.*;
import javax.swing.border.*;
/**
 * Title: LoadXembl
 * Description: Dialog to load sequences from the EMBL nucleotide database, based on accession numbers
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 2.0
 */


public class LoadXembl extends JDialog implements ActionListener{

  private JButton ok;
  private JButton cancel;
  private JButton idFileButton,xmlFileButton;
  private MainFrame owner;
  private JComboBox idType;
  private JComboBox cmbFileType;
  private JTextField idFile,xmlFile;
  private JTextField bpBeforeTxt,bpWithinTxt;
  private JCheckBox addOrNew;
  private String failed="";
  public final int ONE_SECOND = 1000;
  private ProgressMonitor progressMonitor;
  private javax.swing.Timer timer;
  private int lengthOfTask;
  private int current = 0;
  private String statMessage;
  private GeneList newGl;

  public LoadXembl(MainFrame window) {
    super(window,"New GeneList from identifiers",true);
    owner = window;
    JPanel buttonPane = new JPanel();
    ok = new JButton("OK");
    ok.addActionListener(this);
    ok.setPreferredSize(new Dimension(80,20));
    cancel = new JButton("Cancel");
    cancel.setPreferredSize(new Dimension(80,20));
    cancel.addActionListener(this);
    buttonPane.add(ok);
    buttonPane.add(cancel);
    getContentPane().add(buttonPane,BorderLayout.SOUTH);
    JPanel filePanel = new JPanel();
    idFile = new JTextField("U43746",25);
    idFileButton = new JButton("Browse");
    idFileButton.addActionListener(this);
    JPanel topPanel = new JPanel();
    topPanel.setLayout(new BoxLayout(topPanel,BoxLayout.Y_AXIS));
    JLabel label = new JLabel();
    label.setText("ID-list or ID-file:");
    filePanel.add(label);
    filePanel.add(idFile);
    filePanel.add(idFileButton);
    topPanel.add(filePanel);
    getContentPane().add(topPanel,BorderLayout.NORTH);

    JPanel ensPanel = new JPanel();
    ensPanel.setLayout(new BoxLayout(ensPanel,BoxLayout.Y_AXIS));
    JPanel topEnsPanel = new JPanel();
    topEnsPanel.setLayout(new BoxLayout(topEnsPanel,BoxLayout.X_AXIS));
    Object[] fileItems = new Object[2];
    fileItems[0]="Comma separated";
    fileItems[1]="ID file";
    //fileItems[2]="XML file";
    cmbFileType = new JComboBox(fileItems);
    //topEnsPanel.add(fileLabel);
    topEnsPanel.add(cmbFileType);

    Object[] typeItems = new Object[1];
    typeItems[0] = "AccNr";
    //TODO: HUGO, locuslink, GO
    idType = new JComboBox(typeItems);
    idType.setPreferredSize(new Dimension(100,20));
    topEnsPanel.add(idType);

    ensPanel.add(topEnsPanel);

    JPanel bottomEnsPanel = new JPanel();
    addOrNew = new JCheckBox("Add to current list",true);
    bottomEnsPanel.add(addOrNew);

    ensPanel.add(bottomEnsPanel);
    addWindowListener( new WindowAdapter() {
       public void windowOpened( WindowEvent e ){
	    idFile.requestFocus();
	 }
       } );
    getContentPane().add(ensPanel,BorderLayout.CENTER);
    timer = new javax.swing.Timer(100, new TimerListener());
    this.pack();
  }

  public void actionPerformed(ActionEvent e){
      if (e.getSource()==ok){
	try{
	  if (idFile.getText().equalsIgnoreCase("")) {
	   JOptionPane.showMessageDialog(LoadXembl.this,"No file specified","Error",JOptionPane.ERROR_MESSAGE);
	   return;
	  }
	  //window.onlineSource="xembl"; /** @todo  */
	  owner.onlineSource="embl";
	  owner.externalDb = (String)this.idType.getSelectedItem();
          //if (!addOrNew.isSelected()) window.gl = new GeneList();
	  newGl = new GeneList();
	  if (cmbFileType.getSelectedIndex()==0){ //comma separated
	       newGl.constructFromIdString(idFile.getText(),"AccNr");
	  }
	  if (cmbFileType.getSelectedIndex()==1){ //ID file
	       newGl.constructFromIdFile(idFile.getText(),"AccNr");
	  }
	  lengthOfTask=newGl.list.size();
	  progressMonitor = new ProgressMonitor(LoadXembl.this,
					  "Retrieving sequences from EMBL",
					  "", 0, newGl.list.size());
	  progressMonitor.setProgress(0);
	  progressMonitor.setMillisToDecideToPopup(20);
	  ok.setEnabled(false);
	  timer.start();
	  go();

	}
	catch(Exception exc){
	  exc.printStackTrace();
	  JOptionPane.showMessageDialog(LoadXembl.this,exc.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
	}
	setVisible(false);
      }
      else if (e.getSource()==idFileButton){
	final JFileChooser fc = new JFileChooser(GlobalProperties.getCurrentDir());
	fc.addChoosableFileFilter(new SomeFileFilter.SeqFileFilter());
	int returnVal = fc.showOpenDialog(LoadXembl.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
	    try{
	      idFile.setText(fc.getSelectedFile().getAbsolutePath());
              GlobalProperties.setCurrentDir(fc.getSelectedFile().getParent());
	    }
	    catch(Exception exc){exc.printStackTrace();}
        } else {}
      }
      else if (e.getSource()==cancel)
	   setVisible(false);
  }

  /** @todo  */
  /*
  public void retrieveXembl()throws Exception{
    failed="";
    Gene g;
    for (Iterator it = window.gl.list.iterator();it.hasNext();){
      g = (Gene)it.next();
      try{
        if (g.seq==null) g.setSeqFromXembl();
      }
      catch(Exception e){
	  failed = failed+g.accNr+"\n";
	  writeToStatusln(e.getMessage());
      }
    }
    if(!failed.equalsIgnoreCase("")) {
	JOptionPane.showMessageDialog(MainFrame.this,"Following genes could not be found:\n "+failed.substring(0,failed.length()-1),"Warning",JOptionPane.WARNING_MESSAGE);
    }
    setImg();
  }
  */

  //public void retrieveEmbl()throws Exception{
  class ActualTask {
    ActualTask () {
      failed="";
      Gene g;
      //for (Iterator it = window.gl.list.iterator();it.hasNext();){
      int index=0;
      try{
	owner.writeToStatus("task length="+lengthOfTask);
	while(current<lengthOfTask){
	  //g = (Gene)it.next();
	  g = (Gene)newGl.list.get(index);
	  try{
	    if (g.seq==null) g.setSeqFromEmbl();
	  }
	  catch(Exception e){
	      failed = failed+g.accNr+"\n";
	      owner.writeToStatus(e.getMessage());
	    }
	  if (g.seq==null) g.name = g.accNr;
	  current++;
	  statMessage = "Completed " + current + " out of " + lengthOfTask + ".";
	  index++;
	}//while
	if (!addOrNew.isSelected()) owner.gl = newGl;
	else owner.gl.union(newGl);
	if(!failed.equalsIgnoreCase("")) {
	    JOptionPane.showMessageDialog(LoadXembl.this,"Following genes could not be found:\n "+failed.substring(0,failed.length()-1),"Warning",JOptionPane.WARNING_MESSAGE);
	}
	owner.setImg();
      }//try
      catch(Exception ex){ex.printStackTrace();}
    }
  }

    //Stuff for the progress monitor
   void go() {
        current = 0;
        final SwingWorker worker = new SwingWorker() {
            public Object construct() {
                return new ActualTask();
            }
        };
        worker.start();
  }

  int getLengthOfTask() {
      return lengthOfTask;
  }

  /**
   * Called from ProgressBarDemo to find out how much has been done.
   */
  int getCurrent() {
      return current;
  }

  void stop() {
      current = lengthOfTask;
  }

  /**
   * Called from ProgressBarDemo to find out if the task has completed.
   */
  boolean done() {
      if (current >= lengthOfTask)
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
    class TimerListener implements ActionListener {
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
            } else {
                progressMonitor.setNote(getMessage());
                progressMonitor.setProgress(getCurrent());
                //taskOutput.append(task.getMessage() + newline);
                //taskOutput.setCaretPosition(
                    //taskOutput.getDocument().getLength());
            }
        }
    }

}
