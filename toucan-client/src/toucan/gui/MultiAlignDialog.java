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
import toucan.util.GlobalProperties;
import java.util.*;
import toucan.*;

/**
 * Title: MultiAlignDialog
 * Description: Dialog to run the AVID/VISTA service
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 2.0
 */


public class MultiAlignDialog
    extends JDialog
    implements ActionListener {

  private JButton ok, cancel;
  private JTextField txt6, txt7, treeTxt;
  private MainFrame window;
  private JComboBox algoCmb, soapCmb;
  private JComboBox parentCmb;
  public int minId, winLength, revCompl;

  public MultiAlignDialog(MainFrame window) {
    super(window, "Multiple Alignment Web Service", true);
    this.window = window;
    Vector parents = new Vector();
    Gene g;
    for (Iterator it = window.gl.list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      if(g.parent==null && g.orthoList.list.size()>0)
         parents.add(g.name);
    }
    parentCmb = new JComboBox(parents);
    parentCmb.addActionListener(this);
    Object[] algos = new Object[1];
    algos[0]="MLAGAN";
    algoCmb = new JComboBox(algos);
    JLabel label1 = new JLabel("Service URL *");
    JLabel label4 = new JLabel("Choose a parent gene");
    JLabel label5 = new JLabel("Algorithm ");
    JLabel treeLabel = new JLabel("Tree");
    JLabel label6 = new JLabel("P3: Minimal % idendity (Integer)");
    JLabel label7 = new JLabel("P4: Window length in bp (Integer)");
    soapCmb = new JComboBox(GlobalProperties.getSOAPServers());
    soapCmb.setEditable(true);
    txt6 = new JTextField("75", 20);
    txt7 = new JTextField("100", 20);
    treeTxt = new JTextField("",20);
    updateTreeTxt((String) parents.get(0));

    ok = new JButton("OK");
    cancel = new JButton("Cancel");
    ok.setPreferredSize(new Dimension(80, 20));
    cancel.setPreferredSize(new Dimension(80, 20));
    ok.addActionListener(this);
    cancel.addActionListener(this);

    GridLayout lay = new GridLayout(0, 2, 10, 0);
    JPanel pane1 = new JPanel(lay);
    pane1.add(label1);
    pane1.add(soapCmb);
    JPanel pane4 = new JPanel(lay);
    pane4.add(label4);
    pane4.add(parentCmb);
    JPanel pane5 = new JPanel(lay);
    pane5.add(label5);
    pane5.add(algoCmb);
    JPanel treePane = new JPanel(lay);
    treePane.add(treeLabel);
    treePane.add(treeTxt);
    JPanel pane6 = new JPanel(lay);
    pane6.add(label6);
    pane6.add(txt6);
    JPanel pane7 = new JPanel(lay);
    pane7.add(label7);
    pane7.add(txt7);
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(ok);
    buttonPanel.add(cancel);

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.add(pane1);
    mainPanel.add(pane4);
    mainPanel.add(pane5);
    mainPanel.add(treePane);
    mainPanel.add(pane6);
    mainPanel.add(pane7);

    mainPanel.add(buttonPanel);

    getContentPane().add(mainPanel);

    this.pack();

  }

  private void updateTreeTxt(String geneName) {
    treeTxt.setText(window.gl.familyNamesToPhylTree(geneName,false));
  }

  public void actionPerformed(ActionEvent e) {
    try {
      if (e.getSource() == ok) {
        if (((String)soapCmb.getSelectedItem()).equalsIgnoreCase(""))
          JOptionPane.showMessageDialog(this,
              "Please fill in all required (*) fields", "Missing values",
                                        JOptionPane.ERROR_MESSAGE);
        else {
          //check tree
          int nr = window.gl.get((String)parentCmb.getSelectedItem()).orthoList.list.size() + 1;
          int nrBrack = (treeTxt.getText().split("\\(").length-1+treeTxt.getText().split("\\(").length-1);
          System.out.println("nr of brackets= "+nrBrack);
          System.out.println("nr="+nr);
          if(treeTxt.getText().indexOf(",")!=-1 || nrBrack!=((2*nr)-2)){
            JOptionPane.showMessageDialog(this,
              "Tree should contain 2n-2 brackets to denote bifurcation, without commas. Like \n(A ((B C) D)). This usually means multiple (>2) orthologs were found, and Toucan could not determine their phylogenetic relationship.", "Wrong format",
                                        JOptionPane.ERROR_MESSAGE);
          return;
          }

          String selected = (String)parentCmb.getSelectedItem();
          Object[] msg = new Object[3];
          msg[0] = "Note: a backup SOAP server at another location \n will be used automatically if the chosen server is down.";
          //msg[0] = "The service will be started when you press OK";
          //  msg[1] = "The results of all pairs will be annotated automatically.";
          //  msg[2] = "If you close the Toucan application, the results will be lost";
            JOptionPane.showMessageDialog(this, msg);
          setVisible(false);
            ServiceRunner sr = null;
            if(algoCmb.getSelectedIndex()==0)
              sr = new ServiceRunner(window,ServiceRunner.MLAGAN);
            sr.setSoapServerUrl((String)soapCmb.getSelectedItem());
            sr.setConfirmAnnot(true);
            if (!txt6.getText().equals("")) sr.setMcl(new Integer(txt6.getText()).
                intValue());
            if (!txt7.getText().equals("")) sr.setWl(new Integer(txt7.getText()).
                intValue());
  //the replacement of . with - is needed because some gene names contain a . and mlagan crashes on that
      //the - is replaced by . again in the returned string of mlagan
            //sr.setFastaStr(window.gl.getFamilyFastaStr((String)parentCmb.getSelectedItem()).replace('.','-'));
            sr.setFastaStr(window.gl.getFamilyFastaStr((String)parentCmb.getSelectedItem()).replaceAll("\\.","stompunt"));
            sr.setTree(treeTxt.getText().replaceAll("\\.","stompunt"));
            sr.start();
        }
      }
      else if (e.getSource() == parentCmb) {
        updateTreeTxt((String)parentCmb.getSelectedItem());
      }
      else if (e.getSource() == cancel) {
        setVisible(false);
      }
    }
    catch (NumberFormatException nfe) {
      JOptionPane.showMessageDialog(this, "Wrong format for some parameters",
                                    "Error", JOptionPane.ERROR_MESSAGE);
    }
    catch (Exception exc) {
      JOptionPane.showMessageDialog(this, exc.getMessage(), "Error",
                                    JOptionPane.ERROR_MESSAGE);
      exc.printStackTrace();
    }
  }

}
