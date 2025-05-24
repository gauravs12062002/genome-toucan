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
import java.util.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import toucan.*;

/**
 * Title: RepeatMaskerDialog
 * Description: Dialog to run the RepeatMasker service
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Peter Van Loo <Peter.VanLoo@med.kuleuven.ac.be>
 * @version 2.0
 */

public class RepeatMaskerDialog extends JDialog implements ActionListener {

  private JButton ok, cancel;
  private JTextField txt4,txt5;
  private JComboBox speciesBox;
  private MainFrame owner;
  private JComboBox soapCmb;
  private Vector genesVec;
  private JList genesList;
  private JComboBox cmbReplaceWithNs;

  public RepeatMaskerDialog(MainFrame window) {
    super(window,"RepeatMasker Web Service",true);
    owner = window;
    JLabel label1 = new JLabel("Service URL *");
    JLabel label4 = new JLabel("P1: Sequences * (String)");
    JLabel label5 = new JLabel("P2: Species");
    JLabel label6 = new JLabel("P3: Mask repeats with N's");

    soapCmb = new JComboBox(GlobalProperties.getSOAPServers());
    soapCmb.setEditable(true);
    txt4 = new JTextField("Use active sequence set",20);
    txt4.setEditable(false);
    txt5 = new JTextField("human",20); //species
    txt5.setEditable(false);

    Object[] speciesItems = new Object[26];
    speciesItems[0] = "human";
    speciesItems[1] = "mouse";
    speciesItems[2] = "rat";
    speciesItems[3] = "rodent";
    speciesItems[4] = "cow";
    speciesItems[5] = "pig";
    speciesItems[6] = "artiodactyl";
    speciesItems[7] = "cat";
    speciesItems[8] = "dog";
    speciesItems[9] = "carnivore";
    speciesItems[10] = "mammal";
    speciesItems[11] = "chicken";
    speciesItems[12] = "xenopus";
    speciesItems[13] = "fugu";
    speciesItems[14] = "danio";
    speciesItems[15] = "vertebrate";
    speciesItems[16] = "cionasav";
    speciesItems[17] = "cionaint";
    speciesItems[18] = "drosophila";
    speciesItems[19] = "anopheles";
    speciesItems[20] = "elegans";
    speciesItems[21] = "diatom";
    speciesItems[22] = "arabidopsis";
    speciesItems[23] = "wheat";
    speciesItems[24] = "maize";
    speciesItems[25] = "oryza";
    speciesBox = new JComboBox(speciesItems);

    genesVec = new Vector();
    Gene g;
    for (Iterator it = window.gl.list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      genesVec.add(g.name);
    }
    genesList = new JList(genesVec);
    genesList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    JScrollPane genesListScrollPane = new JScrollPane(genesList);
    if (genesVec.size() > 8) genesList.setVisibleRowCount(8);
    else genesList.setVisibleRowCount(genesVec.size());

    Object[] cmbItems = new Object[2];
    cmbItems[0] = "Yes";
    cmbItems[1] = "No";
    cmbReplaceWithNs = new JComboBox(cmbItems);

    ok = new JButton("OK");
    cancel = new JButton("Cancel");
    ok.setPreferredSize(new Dimension(80, 20));
    cancel.setPreferredSize(new Dimension(80, 20));
    ok.addActionListener(this);
    cancel.addActionListener(this);

    GridLayout lay = new GridLayout(0,2,13,0);
    JPanel pane0 = new JPanel(lay);
    JPanel pane1 = new JPanel(lay);
    pane1.add(label1);
    pane1.add(soapCmb);
    JPanel pane4 = new JPanel(lay);
    pane4.add(label4);
    pane4.add(genesListScrollPane);
    JPanel pane5 = new JPanel(lay);
    pane5.add(label5);
    pane5.add(speciesBox);
    JPanel pane6 = new JPanel(lay);
    pane6.add(label6);
    pane6.add(cmbReplaceWithNs);
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(ok);
    buttonPanel.add(cancel);

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
    mainPanel.add(pane0);
    mainPanel.add(pane1);
    mainPanel.add(pane4);
    mainPanel.add(pane5);
    mainPanel.add(pane6);
    mainPanel.add(buttonPanel);

    getContentPane().add(mainPanel);

    this.pack();

  }

  public void actionPerformed(ActionEvent e){
    try{
        if (e.getSource()==ok){
          if(((String)soapCmb.getSelectedItem()).equalsIgnoreCase("") || genesList.getSelectedIndices().length == 0)
              JOptionPane.showMessageDialog(this,"Please fill in all required (*) fields","Missing values",JOptionPane.ERROR_MESSAGE);
          else{
            ServiceRunner sr = new ServiceRunner(owner, ServiceRunner.REPEATMASKER);
            sr.setSoapServerUrl((String)soapCmb.getSelectedItem());

            String fasta = "";
            Object[] genes = genesList.getSelectedValues();
            for (int i = 0; i < genes.length; i++) {
              String geneString = (String)genes[i];
              Gene gene = owner.gl.get(geneString);
              fasta = fasta + gene.getFastaStr();
            }
            System.out.println(fasta);
            sr.setFastaStr(fasta);
            sr.setSpecies((String)speciesBox.getSelectedItem());
            if (cmbReplaceWithNs.getSelectedIndex() == 0)
              sr.setReplaceWithNs(true);
            else sr.setReplaceWithNs(false);
            Object[] msg = new Object[3];
            msg[0] = "Note: a backup SOAP server at another location \n will be used automatically if the chosen server is down.";
               //msg[0] = "The service will be started when you press OK";
            //msg[1] = "You will be notified when the results are ready";
            //msg[2] = "If you close the Toucan application, the results will be lost";
            JOptionPane.showMessageDialog(this,msg);
            sr.start();
            setVisible(false);
           }
        }
        else if (e.getSource()==cancel){
         setVisible(false);
        }
    }
    catch(NumberFormatException nfe){
       JOptionPane.showMessageDialog(this,"Wrong format for some parameters","Error",JOptionPane.ERROR_MESSAGE);
    }
    catch(Exception exc){
      exc.printStackTrace();
      JOptionPane.showMessageDialog(this,exc.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
    }
  }

}
