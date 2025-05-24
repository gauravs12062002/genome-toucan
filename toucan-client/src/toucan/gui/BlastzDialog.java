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
 * Title: BlastzDialog
 * Description: Dialog to run the AVID/VISTA service
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 2.0
 */


public class BlastzDialog
    extends JDialog
    implements ActionListener {

  private JButton ok, cancel;
  private JTextField txt6, txt7;
  private MainFrame owner;
  private Vector glVec;
  private JComboBox geneCmb1, geneCmb2, soapCmb;
  public int gene1, gene2, minId, winLength, revCompl;

  public BlastzDialog(MainFrame window) {
    super(window, "BLASTZ Web Service", true);
    owner = window;
    Gene g;
    glVec = new Vector();
    for (Iterator it = window.gl.list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      glVec.add(g.name);
    }
    JLabel label1 = new JLabel("Service URL *");
    JLabel label4 = new JLabel("P1: fastA Seq1 * ");
    JLabel label5 = new JLabel("P2: fastA Seq2 * ");
    JLabel label6 = new JLabel("P3: Minimal % idendity (Integer)");
    JLabel label7 = new JLabel("P4: Window length in bp (Integer)");
    soapCmb = new JComboBox(GlobalProperties.getSOAPServers());
    soapCmb.setEditable(true);
    geneCmb1 = new JComboBox(glVec);
    geneCmb2 = new JComboBox(glVec);
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
    pane4.add(geneCmb1);
    JPanel pane5 = new JPanel(lay);
    pane5.add(label5);
    pane5.add(geneCmb2);
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

          ServiceRunner sr = new ServiceRunner(owner, ServiceRunner.BLASTZ);
          sr.setSoapServerUrl((String)soapCmb.getSelectedItem());
          if (!txt6.getText().equals(""))sr.setMcl(new Integer(txt6.getText()).intValue());
          if (!txt7.getText().equals(""))sr.setWl(new Integer(txt7.getText()).intValue());
          Gene g1 = (Gene) owner.gl.list.get(geneCmb1.getSelectedIndex());
          Gene g2 = (Gene) owner.gl.list.get(geneCmb2.getSelectedIndex());
          sr.setSeq1(g1.getFastaStr());
          sr.setSeq2(g2.getFastaStr());
          sr.setId1(g1.name);
          sr.setId2(g2.name);
          Object[] msg = new Object[3];
          msg[0] = "This job will be performed remotely. \n You will be notified by TOUCAN when the results are ready.";
          //msg[0] = "The service will be started when you press OK";
          //msg[1] = "You will be notified when the results are ready";
          //msg[2] =               "If you close the Toucan application, the results will be lost";
          JOptionPane.showMessageDialog(this, msg);
          sr.start();
          setVisible(false);
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
