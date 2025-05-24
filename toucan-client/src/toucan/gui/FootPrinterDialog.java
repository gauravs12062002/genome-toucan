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
import toucan.*;
import toucan.util.GlobalProperties;

/**
 * Title: FootPrinterDialog
 * Description: Dialog to run the FootPrinter service
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 2.0
 */


public class FootPrinterDialog extends JDialog implements ActionListener{

  private JButton ok,cancel;
  public JTextField txt5,txt6,txt7,txt8,txt9,txt10;
  private MainFrame owner;
  public JComboBox subjectCombo, soapCmb, cmbSeqType, cmbMotifSize, cmbMaxPars, cmbMaxMutBranch, cmbLossesYesNo,
                   cmbSignif, cmbLossCost, cmbSubChangeCost, cmbSubSize;

  public FootPrinterDialog(MainFrame window) {
    super(window,"FootPrinter 2.0 Web Service",true);
    owner = window;
    JLabel label1 = new JLabel("Service URL *");
    JLabel label4 = new JLabel("Input sequences * (String)");
    JLabel label5 = new JLabel("Phylogenetic tree * (String)");
    JLabel label6 = new JLabel("Sequence type (String)");
    JLabel label7 = new JLabel("Motif size * (Integer)");
    JLabel label8 = new JLabel("Maximum parsimony score (Float)");
    JLabel label9 = new JLabel("Maximum nr mutations per branch (Float)");
    JLabel label10 = new JLabel("Allow regulatory element losses (Boolean)");
    JLabel label11 = new JLabel("Spanned tree significance level (String)");
    JLabel label12 = new JLabel("Motif loss cost (Float)");
    JLabel label13 = new JLabel("Subregion size (Integer)");
    JLabel label14 = new JLabel("Subregion change cost (Float)");
    JLabel label15 = new JLabel("Triplet filtering (Boolean)");
    JLabel label16 = new JLabel("Pair filtering (Boolean)");
    JLabel label17 = new JLabel("Post filtering (Boolean)");
    JLabel label18 = new JLabel("Insertion & deletion cost(Float)");
    JLabel label19 = new JLabel("Inversion cost (Float)");

    soapCmb = new JComboBox(GlobalProperties.getSOAPServers());
    soapCmb.setEditable(true);

    Object[] subjects = new Object[2];
    subjects[0] = "Use complete active sequence set";
    subjects[1] = "Use sublist";
    subjectCombo = new JComboBox(subjects);
    txt5 = new JTextField(window.gl.namesToPhylTree(true),20);

    Object[] cmbItems = new Object[3];
    cmbItems[0]="upstream";
    cmbItems[1]="downstream";
    cmbItems[2]="other";
    cmbSeqType = new JComboBox(cmbItems);

    cmbItems = new Object[7];
    cmbItems[0]="6";
    cmbItems[1]="7";
    cmbItems[2]="8";
    cmbItems[3]="9";
    cmbItems[4]="10";
    cmbItems[5]="11";
    cmbItems[6]="12";
    cmbMotifSize = new JComboBox(cmbItems);
    cmbMotifSize.setSelectedIndex(4);

    cmbItems = new Object[7];
    cmbItems[0]="0";
    cmbItems[1]="1";
    cmbItems[2]="2";
    cmbItems[3]="3";
    cmbItems[4]="4";
    cmbItems[5]="5";
    cmbItems[6]="6";
    cmbMaxPars = new JComboBox(cmbItems);
    cmbMaxPars.setSelectedIndex(2);

    cmbItems = new Object[2];
    cmbItems[0]="1";
    cmbItems[1]="2";
    cmbMaxMutBranch = new JComboBox(cmbItems);
    cmbMaxMutBranch.setSelectedIndex(0);

    cmbItems = new Object[1];
    cmbItems[0]="No (ignore next two options)";
    //cmbItems[1]="Yes";
    cmbLossesYesNo = new JComboBox(cmbItems);

    cmbItems = new Object[3];
    cmbItems[0]="Somewhat significant";
    cmbItems[1]="Significant";
    cmbItems[2]="Very significant";
    cmbSignif = new JComboBox(cmbItems);
    cmbSignif.setSelectedIndex(1);

    cmbItems = new Object[4];
    cmbItems[0]="0.5";
    cmbItems[1]="1";
    cmbItems[2]="1.5";
    cmbItems[3]="2";
    cmbLossCost = new JComboBox(cmbItems);
    cmbLossCost.setSelectedIndex(1);

    cmbItems = new Object[5];
    cmbItems[0]="0";
    cmbItems[1]="0.5";
    cmbItems[2]="1";
    cmbItems[3]="1.5";
    cmbItems[4]="2";
    cmbSubChangeCost = new JComboBox(cmbItems);
    cmbSubChangeCost.setSelectedIndex(2);

    cmbItems = new Object[6];
    cmbItems[0]="20";
    cmbItems[1]="50";
    cmbItems[2]="100";
    cmbItems[3]="200";
    cmbItems[4]="500";
    cmbItems[5]="1000";
    cmbSubSize = new JComboBox(cmbItems);
    cmbSubSize.setSelectedIndex(2);

    ok = new JButton("OK");
    cancel = new JButton("Cancel");
    ok.setPreferredSize(new Dimension(80,20));
    cancel.setPreferredSize(new Dimension(80,20));
    ok.addActionListener(this);
    cancel.addActionListener(this);

    GridLayout lay = new GridLayout(0,2,10,0);
    JPanel pane1 = new JPanel(lay);
    pane1.add(label1);
    pane1.add(soapCmb);
    JPanel pane4 = new JPanel(lay);
    pane4.add(label4);
    pane4.add(subjectCombo);
    JPanel pane5 = new JPanel(lay);
    pane5.add(label5);
    pane5.add(txt5);
    JPanel pane6 = new JPanel(lay);
    pane6.add(label6);
    pane6.add(cmbSeqType);
    JPanel pane7 = new JPanel(lay);
    pane7.add(label7);
    pane7.add(cmbMotifSize);
    JPanel pane8 = new JPanel(lay);
    pane8.add(label8);
    pane8.add(cmbMaxPars);
    JPanel pane9 = new JPanel(lay);
    pane9.add(label9);
    pane9.add(cmbMaxMutBranch);
    JPanel pane10 = new JPanel(lay);
    pane10.add(label10);
    pane10.add(cmbLossesYesNo);
    JPanel pane11 = new JPanel(lay);
    pane11.add(label11);
    pane11.add(cmbSignif);
    JPanel pane12 = new JPanel(lay);
    pane12.add(label12);
    pane12.add(cmbLossCost);
    JPanel pane13 = new JPanel(lay);
    pane13.add(label13);
    pane13.add(cmbSubSize);
    JPanel pane14 = new JPanel(lay);
    pane14.add(label14);
    pane14.add(cmbSubChangeCost);
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(ok);
    buttonPanel.add(cancel);

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
    mainPanel.add(pane1);
    mainPanel.add(pane4);
    mainPanel.add(pane5);
    mainPanel.add(pane7);
    mainPanel.add(pane6);
    mainPanel.add(pane8);
    mainPanel.add(pane9);
    mainPanel.add(pane13);
    mainPanel.add(pane14);
    mainPanel.add(pane10);
    mainPanel.add(pane11);
    mainPanel.add(pane12);

    mainPanel.add(buttonPanel);

    getContentPane().add(mainPanel);

    this.pack();

  }

  public void actionPerformed(ActionEvent e){
    try{
        if (e.getSource()==ok) {
            ServiceRunner sr = new ServiceRunner(owner,ServiceRunner.FOOTPRINTER);
            sr.setSoapServerUrl((String)soapCmb.getSelectedItem());
            if (subjectCombo.getSelectedIndex() == 0) {
              sr.setFastaStr(owner.gl.getFastaStr());
            }
            else if (subjectCombo.getSelectedIndex() == 1) {
              sr.setFastaStr(owner.subList.getFastaStr().replace('_', '@'));
            }
            sr.setTree(txt5.getText());
            sr.setSeq_type((String)cmbSeqType.getSelectedItem());
            sr.setSize(new Integer((String)cmbMotifSize.getSelectedItem()).intValue());
            sr.setMax_mut(new Integer((String)cmbMaxPars.getSelectedItem()).intValue());
            sr.setMax_mut_per_branch(new Float((String)cmbMaxMutBranch.getSelectedItem()).floatValue());
            sr.setSubreg_size(new Integer((String)cmbSubSize.getSelectedItem()).intValue());
            sr.setSubreg_change_cost(new Integer((String)cmbSubChangeCost.getSelectedItem()).intValue());
            sr.setPair_filt(false);
            sr.setPost_filt(false);
            sr.setTriplet_filt(false);
            sr.setInv_cost(-999f);
            sr.setInsdel_cost(-999f);
            Object[] msg = new Object[3];
            msg[0] = "This job will be performed remotely. \n You will be notified by TOUCAN when the results are ready.";
          //msg[0] = "Note: a backup SOAP server at another location \n will be used automatically if the chosen server is down.";
            //msg[0] = "The service will be started when you press OK";
            //msg[1] = "You will be notified when the results are ready";
            //msg[2] = "If you close the Toucan application, the results will be lost";
            JOptionPane.showMessageDialog(this,msg);
            sr.start();
            setVisible(false);
        }
        else if (e.getSource()==cancel){
         setVisible(false);
        }
    }
    catch (NumberFormatException nfe){
      JOptionPane.showMessageDialog(this,"Wrong format for some parameters","Error",JOptionPane.ERROR_MESSAGE);
    }
    catch(Exception exc){
      JOptionPane.showMessageDialog(this,exc.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
    }
  }

}
