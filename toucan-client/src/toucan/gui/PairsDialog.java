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
 * Title: VistaDialog
 * Description: Dialog to run the AVID/LAGAN/VISTA/BLASTZ service
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 2.0
 */


public class PairsDialog
    extends JDialog
    implements ActionListener {

  private JButton ok, cancel;
  private JTextField txt6, txt7;
  private MainFrame owner;
  private Vector pairsVec;
  private JComboBox algoCmb, soapCmb;
  private JList pairsList;
  public int minId, winLength, revCompl;

  public PairsDialog(MainFrame window) {
    super(window, "Auto-align Web Service", true);
    owner = window;
    pairsVec = new Vector();
    Gene g;
    for (Iterator it = window.gl.list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      if(g.parent==null && g.orthoList.list.size()>0)
        for(Iterator it2 = g.orthoList.list.iterator();it2.hasNext();){
          pairsVec.add(g.name + "-" + ( (Gene) it2.next()).name);
        }
    }
    pairsList = new JList(pairsVec);
    pairsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    JScrollPane pairsListScrollPane = new JScrollPane(pairsList);
    if (pairsVec.size()>5) pairsList.setVisibleRowCount(5);
    else pairsList.setVisibleRowCount(pairsVec.size());
    Object[] algos = new Object[2];
    algos[0]="AVID";
    algos[1]="LAGAN";
//    algos[2]="BLASTZ";
    algoCmb = new JComboBox(algos);
    JLabel label1 = new JLabel("Service URL *");
    JLabel label4 = new JLabel("Choose one or more pairs");
    JLabel label5 = new JLabel("Algorithm ");
    JLabel label6 = new JLabel("P3: Minimal % idendity (Integer)");
    JLabel label7 = new JLabel("P4: Window length in bp (Integer)");
    soapCmb = new JComboBox(GlobalProperties.getSOAPServers());
    soapCmb.setEditable(true);
    txt6 = new JTextField("75", 20);
    txt7 = new JTextField("100", 20);

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
    pane4.add(pairsListScrollPane);
    JPanel pane5 = new JPanel(lay);
    pane5.add(label5);
    pane5.add(algoCmb);
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
    mainPanel.add(pane6);
    mainPanel.add(pane7);

    mainPanel.add(buttonPanel);

    getContentPane().add(mainPanel);

    this.pack();

  }

  public void actionPerformed(ActionEvent e) {
    try {
      if (e.getSource() == ok) {
        if (((String)soapCmb.getSelectedItem()).equalsIgnoreCase(""))
          JOptionPane.showMessageDialog(this,
              "Please fill in all required (*) fields", "Missing values",
                                        JOptionPane.ERROR_MESSAGE);
        else {

          Object[] pairs = pairsList.getSelectedValues();
          Object[] msg = new Object[3];
          msg[0] = "Note: a backup SOAP server at another location \n will be used automatically if the chosen server is down.";
          //msg[0] = "The service will be started when you press OK";
          //  msg[1] = "The results of all pairs will be annotated automatically.";
          //  msg[2] = "If you close the Toucan application, the results will be lost";
            JOptionPane.showMessageDialog(this, msg);
          setVisible(false);
          for (int i =0;i<pairs.length;i++){
            String temp = ( (String) pairs[i]);
            String parentName = temp.substring(0,temp.indexOf("-"));
            String childName = temp.substring(temp.indexOf("-")+1,temp.length());

            owner.writeToStatus("parentName="+parentName);
            ServiceRunner sr = null;
            if(algoCmb.getSelectedIndex()==0)
              sr = new ServiceRunner(owner, ServiceRunner.AVID);
            else if(algoCmb.getSelectedIndex()==1)
              sr = new ServiceRunner(owner, ServiceRunner.LAGAN);
            else if(algoCmb.getSelectedIndex()==2)
              sr = new ServiceRunner(owner, ServiceRunner.BLASTZ);
            sr.setSoapServerUrl((String)soapCmb.getSelectedItem());
            sr.setConfirmAnnot(false);
            if (!txt6.getText().equals("")) sr.setMcl(new Integer(txt6.getText()).
                intValue());
            if (!txt7.getText().equals("")) sr.setWl(new Integer(txt7.getText()).
                intValue());
            Gene g1 = (Gene) owner.gl.get(parentName);
            Gene g2 = (Gene) owner.gl.get(childName);
            sr.setSeq1(g1.getFastaStr());
            sr.setSeq2(g2.getFastaStr());
            sr.setId1(g1.name);
            sr.setId2(g2.name);
            sr.start();
          }
        }
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
    }
  }

}
