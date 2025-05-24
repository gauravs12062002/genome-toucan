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
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.table.*;
import java.util.*;
import toucan.*;
import toucan.util.GlobalProperties;
import java.awt.datatransfer.*;

/**
 * Title: GeneInfo
 * Description: Dialog to retrieve annotation (GO, InterPro, etc.) for each gene from Ensembl
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 2.0
 */
public class GeneInfo extends JFrame implements ClipboardOwner {

  JTable table;
  DefaultTableModel model;
  JButton saveButton1;
  JButton refresh;
  JPanel panel1;
  JScrollPane infoPane;
  JPopupMenu pop;
  //progress monitor stuff
  public final static int ONE_SECOND = 1000;
  private ProgressMonitor progressMonitor;
  private javax.swing.Timer timer;
  private int lengthOfTask;
  private int current = 0;
  private String statMessage;
  private MainFrame owner;

  public GeneInfo(MainFrame mFrame) {
    try {
      owner = mFrame;
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    saveButton1 = new JButton("Save");
    panel1 = new JPanel();
    refresh = new JButton("Update");

    timer = new javax.swing.Timer(100, new TimerListener());
    lengthOfTask = owner.gl.list.size();
    //Update button action
    refresh.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
       //writeToStatus("Retrieving info from Ensembl");
       progressMonitor = new ProgressMonitor(GeneInfo.this,
                                             "Retrieving info from Ensembl",
                                             "", 0, owner.gl.list.size());
       progressMonitor.setProgress(0);
       progressMonitor.setMillisToDecideToPopup(20);
       refresh.setEnabled(false);
       timer.start();
       go();
      }
    });

    panel1.add(refresh);
    makeTableModel();
    table = new JTable(model);
    table.doLayout();
    infoPane = new JScrollPane(table);
    this.getContentPane().add(infoPane,BorderLayout.NORTH);
    this.getContentPane().add(panel1,BorderLayout.CENTER);
    //this.getContentPane().add(refresh);
    this.setIconImage(MainFrame.toucanImg);
    this.setTitle("Gene Information from Ensembl");

    //------------------------
    //POPUP Menu for copying info to clipboard
    pop = new JPopupMenu();
    JMenuItem copyItem = new JMenuItem("Copy");
    copyItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	StringBuffer buf = new StringBuffer();
	Clipboard clipboard = getToolkit().getSystemClipboard ();
	int col = table.getColumnCount();
	for (int i = 0; i < table.getRowCount(); ++i) {
         if (table.isRowSelected(i)) {
	   for (int j = 0;j < col; ++j) {
             buf.append(table.getValueAt(i,j));
             if (j<col-1) buf.append("\t");
           }
           buf.append("\n");
         }
       }
	StringSelection sel = new StringSelection(buf.toString());
	clipboard.setContents(sel,GeneInfo.this);
      }
    });
    pop.add(copyItem);
    table.addMouseListener(new MouseListener() {
      public void mousePressed(MouseEvent e) {
        maybeShow(e);
      }
      public void mouseReleased(MouseEvent e) {
        maybeShow(e);
      }
      public void mouseExited(MouseEvent e) {}
      public void mouseEntered(MouseEvent e) {}
      public void mouseClicked(MouseEvent e) {}
      private void maybeShow(MouseEvent e) {
        if(e.isPopupTrigger()) pop.show(e.getComponent(),e.getX(),e.getY());
      }
    });
    //------------------------

    this.pack();
  }

  public DefaultTableModel makeTableModel(){
    model = new DefaultTableModel();
    //model.addColumn("Ensembl");
    Gene g;
    //columns
    Vector colNames = new Vector();
    colNames.add("Ensembl");
    colNames.add("Species");
    for (Iterator it = owner.gl.list.iterator();it.hasNext();){
       g = (Gene)it.next();
       if(g.infoProps!=null) {
	 Enumeration enumer=g.infoProps.propertyNames();
	 while (enumer.hasMoreElements()){
	  String key = (String)enumer.nextElement();
	  //int j;
	  //for(j=0;j<model.getColumnCount();j++){
	   //if(model.getColumnName(j).equals(key))
	    //break;
	  //}
	  //if (j>=model.getColumnCount())//not found in for loop above
	     //model.addColumn(key);
	  if(!colNames.contains(key))
	     colNames.add(key);
	 }
       }
    }
    //data
    Vector rowData;
    //Object[] rowData;
    Vector data = new Vector();
    for (Iterator it = owner.gl.list.iterator();it.hasNext();){
       g = (Gene)it.next();
       rowData = new Vector();
       for(int i=0;i<colNames.size();i++){
	rowData.insertElementAt("",i);
       }
       //writeToStatus(g.ensembl);
       rowData.set(0,g.ensembl);
       if(g.species!=null) rowData.set(1,g.species.name);
       //rowData = new Object[model.getColumnCount()];
       //rowData[0]=g.ensembl;
       if(g.infoProps!=null) {
	 Enumeration enumer=g.infoProps.propertyNames();
	 while (enumer.hasMoreElements()){
	  String key = (String)enumer.nextElement();
	  String value = g.infoProps.getProperty(key);
	  //int j;
	  //for(j=0;j<model.getColumnCount();j++){
	  // if(model.getColumnName(j).equals(key))
	  //  rowData[j]=value;
	    //writeToStatus("added: "+key+"--"+value);
	  //  break;
	  //}
	  rowData.set(colNames.indexOf(key),value);
	 }
       }
       //model.addRow(rowData);
       data.add(rowData);
       //for(int i=0;i<rowData.length;i++){
       //        writeToStatus(rowData[i]);
       //}
    }
    model.setDataVector(data,colNames);
    //table.updateUI();
    return model;
  }


  public void lostOwnership(Clipboard clipboard, Transferable contents) {
       //writeToStatus("Clipboard contents replaced");
  }

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
                    owner.writeToStatus("Retrieved info");
                }
                refresh.setEnabled(true);
            } else {
                progressMonitor.setNote(getMessage());
                progressMonitor.setProgress(getCurrent());
                //taskOutput.append(task.getMessage() + newline);
                //taskOutput.setCaretPosition(
                    //taskOutput.getDocument().getLength());
            }
        }
    }

    /**
     * The actual long running task.  This runs in a SwingWorker thread.
     */
    class ActualTask {
        ActualTask () {
            Gene g;
	       Iterator it=owner.gl.list.iterator();
	       while(current<lengthOfTask && it.hasNext()){
	       //for (Iterator it=MainFrame.gl.list.iterator();it.hasNext();){
	           try{
		     g = (Gene)it.next();
		     //find species in case a seq was loaded from file!
		     if(g.species==null){
		      for (int i=0;i<GlobalProperties.getSpeciesArr().length;i++){
			if(g.ensembl.startsWith(GlobalProperties.getSpeciesArr()[i].prefix)){
			  g.species = GlobalProperties.getSpeciesArr()[i];
			  break;
			}
		      }
		     }
		     g.updateInfoProps(GlobalProperties.getMartMysql(),GlobalProperties.getEnsemblUser(),GlobalProperties.getEnsemblPass());
		     current++;
		     statMessage = "Completed " + current +
                                  " out of " + lengthOfTask + ".";
		     makeTableModel();
                     table.setModel(model);
                     table.setAutoResizeMode(table.AUTO_RESIZE_OFF);
		     table.doLayout();
		     if (current > lengthOfTask) {
                        current = lengthOfTask;
                     }
	           }catch(Exception exc){exc.printStackTrace();}
	       }

        }
    }

}
