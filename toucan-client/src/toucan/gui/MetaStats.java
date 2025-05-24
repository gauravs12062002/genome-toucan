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
import java.awt.datatransfer.*;

/**
 * Title: MetaStats
 * Description: Dialog to run a binomial meta-analysis to find the over-represented motifs, taking the number of genes that have motifs into account
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 2.0
 */

public class MetaStats extends JFrame implements ClipboardOwner{

  JScrollPane resultsPane;
  JLabel label1;
  JLabel label2;
  JPanel panel1 = new JPanel();
  JPanel panel2 = new JPanel();
  JPanel panel3 = new JPanel();
  JPanel topPanel = new JPanel();
  JTextField refFile = new JTextField(15);
  JButton refFileButton = new JButton("Browse");
  JTextField expFreqFile = new JTextField(15);
  JButton expFreqFileButton = new JButton("Browse");
  JButton startButton = new JButton("Start");
  JButton stopButton = new JButton("Stop");
  DefaultTableModel model;
  JTable table;
  JPopupMenu pop;
  public HashSet replacedFreqs = new HashSet();
  private MainFrame owner;

  public MetaStats(MainFrame mFrame) {
    try {
      owner = mFrame;
      jbInit();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    label1 = new JLabel("Annotated Reference Set: ");
    panel1.add(refFile);
    panel1.add(refFileButton);
    label2 = new JLabel("(OR) Expected Frequencies File: ");
    panel2.add(expFreqFile);
    panel2.add(expFreqFileButton);
    panel3.add(startButton);
    //panel3.add(stopButton);
    topPanel.setLayout(new BoxLayout(topPanel,BoxLayout.Y_AXIS));
    topPanel.add(label1);
    topPanel.add(panel1);
    topPanel.add(label2);
    topPanel.add(panel2);
    topPanel.add(panel3);
    this.getContentPane().add(topPanel,BorderLayout.NORTH);

    model = new DefaultTableModel();

    //model.addColumn("Feature Name");
    //model.addColumn("Prob(occ{b}>=n)");
    //model.addColumn("Sig");

    table = new JTable(model)
    /*{
     public Component prepareRenderer(TableCellRenderer renderer,
                                         int rowIndex, int vColIndex) {
            Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
            if (MetaStats.this.replacedFreqs.contains(new Integer(rowIndex))) {
                c.setBackground(Color.yellow);
            } else {
                // If not shaded, match the table's background
                c.setBackground(getBackground());
            }
            return c;
     }
    }*/ ;
    table.setSelectionBackground(Color.red);
    table.doLayout();

    final JFileChooser fc = new JFileChooser(GlobalProperties.getCurrentDir());
    fc.addChoosableFileFilter(new SomeFileFilter.SeqFileFilter());
    refFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                int returnVal = fc.showOpenDialog(MetaStats.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                  GlobalProperties.setCurrentDir(fc.getSelectedFile().getParent());
                    try{
                      refFile.setText(fc.getSelectedFile().getAbsolutePath());
                    }
                    catch(Exception exc){exc.printStackTrace();}
                } else {}
            }
    });

    final JFileChooser fc2 = new JFileChooser(GlobalProperties.getCurrentDir());
    fc2.addChoosableFileFilter(new SomeFileFilter.TxtFileFilter());
    expFreqFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                int returnVal = fc2.showOpenDialog(MetaStats.this);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    //File file = fc.getSelectedFile();
                    try{
                      expFreqFile.setText(fc2.getSelectedFile().getAbsolutePath());
                    }
                    catch(Exception exc){exc.printStackTrace();}
                } else {}
            }
    });

    pop = new JPopupMenu();
    JMenuItem copyItem = new JMenuItem("Copy");
    copyItem.addActionListener(new ActionListener(){
    public void actionPerformed(ActionEvent e){
     StringBuffer buf = new StringBuffer();
     Clipboard clipboard = getToolkit ().getSystemClipboard ();
     int col = table.getColumnCount();
     for (int i=0;i<table.getRowCount();++i){
      if (table.isRowSelected(i)){
        for (int j=0;j<col;++j){
                buf.append(table.getValueAt(i,j));
                if (j<col-1) buf.append("\t");
        }
        buf.append("\n");
      }
     }
     StringSelection sel = new StringSelection(buf.toString());
     clipboard.setContents(sel,MetaStats.this);
    }
    });
    pop.add(copyItem);

    table.addMouseListener(new MouseListener(){
    public void mousePressed(MouseEvent e){
           maybeShow(e);
    }
    public void mouseReleased(MouseEvent e){
           maybeShow(e);
    }
    public void mouseExited(MouseEvent e){
    }
    public void mouseEntered(MouseEvent e){
    }
    public void mouseClicked(MouseEvent e){
    }
    private void maybeShow(MouseEvent e){
            if(e.isPopupTrigger()) pop.show(e.getComponent(),e.getX(),e.getY());
    }
    });

    startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                   TreeMap objects=null;
                   if(!refFile.getText().equalsIgnoreCase("") && !expFreqFile.getText().equalsIgnoreCase("")){
                         JOptionPane.showMessageDialog(MetaStats.this,"Choose either one or another","Error",JOptionPane.ERROR_MESSAGE);
                   }
                   else if (refFile.getText().equalsIgnoreCase("") && expFreqFile.getText().equalsIgnoreCase("")){
                         JOptionPane.showMessageDialog(MetaStats.this,"Specify a file for the expected frequencies","Error",JOptionPane.ERROR_MESSAGE);
                   }
                   else if (!refFile.getText().equalsIgnoreCase("")){
                        try{
                          objects = owner.gl.MetaBinom(refFile.getText(),false);
                        }
                        catch(Exception exc){
                          exc.printStackTrace();
                          JOptionPane.showMessageDialog(MetaStats.this,"There were problems with the Binomial Analysis...","Error",JOptionPane.ERROR_MESSAGE);
                        }
                   }
                   else if (!expFreqFile.getText().equalsIgnoreCase("")){
                         try{objects = owner.gl.MetaBinom(expFreqFile.getText(),true);}
                         catch(Exception exc){
                          exc.printStackTrace();
                          JOptionPane.showMessageDialog(MetaStats.this,"There were problems with the Binomial Analysis...","Error",JOptionPane.ERROR_MESSAGE);
                        }
                   }
                   if(objects!=null){
                       Vector colVector = new Vector();
                       colVector.add("Feature Name");
                       //colVector.add("n");
                       colVector.add("MetaProb");
                       //colVector.add("Sig");
                       Vector dataVector = new Vector();
                       Vector row;
                       //Object[] keyArr = objects.keySet().toArray();
                       //for (int i = keyArr.length-1;i>=0;--i){
                       int i=0;
                       replacedFreqs = new HashSet();
                       for (Iterator it = objects.keySet().iterator();it.hasNext();){
                          //Object key = keyArr[i];
                          i++;
                          Object key = it.next();
                          //Factor o = (Factor)objects.get(key);
                          //if(!o.freqReplaced){
                            row = new Vector();
                            //row.add(o.objectDescriptor);
                            //row.add(new Integer(o.nrOccInSet));
                            //row.add(new Double(o.pValue));
                            //row.add(new Double(Rounding.toString(o.sig,3)));
                            row.add(key);
                            row.add(objects.get(key));
                            dataVector.add(row);
                          //}
                          //else replacedFreqs.add(new Integer(i));
                       }
                       model.setDataVector(dataVector,colVector);
                       MetaStats.this.sortAllRowsBy(model,1,true);
                   }
            }
    });


    //table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    resultsPane = new JScrollPane(table);
    this.getContentPane().add(resultsPane,BorderLayout.SOUTH);
    this.setIconImage(MainFrame.toucanImg);
    this.setTitle("Find over-represented features");
    this.pack();
    this.setVisible(true);
  }


public void lostOwnership(Clipboard clipboard, Transferable contents) {
       //writeToStatus("Clipboard contents replaced");
    }

public void sortAllRowsBy(DefaultTableModel model, int colIndex, boolean ascending) {
    Vector data = model.getDataVector();
    Collections.sort(data, new ColumnSorter(colIndex, ascending));
    model.fireTableStructureChanged();
}



}
