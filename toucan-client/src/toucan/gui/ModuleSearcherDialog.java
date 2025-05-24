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
import java.util.*;
import toucan.util.GlobalProperties;
import toucan.*;
import toucan.ext.SOAPClient;
import org.w3c.dom.*;
import toucan.util.*;

/**
 * Title: ModuleSearcherDialog
 * Description: Dialog to run the ModuleSearcher service
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be> & Peter Van Loo <Peter.VanLoo@med.kuleuven.ac.be>
 * @version 2.0
 */

public class ModuleSearcherDialog
    extends JDialog
    implements ActionListener {

  private JButton ok, cancel;
  private JTextField elementsText, sizeText, modulesText, txt11;
  private MainFrame owner;
  private JComboBox cmbAlgorithm, soapCmb;
  private JCheckBox checkOverlap, checkPenalisation;
  private Vector featVec;
  private JList featList;
  private JButton get11;
  private Vector motifNames;
  private boolean gotMotifNames=false;
  private Document paramXml;

  public ModuleSearcherDialog(MainFrame window) {
    super(window, "ModuleSearcher Web Service", true);
    owner = window;
    JLabel label1 = new JLabel("Service URL *");
    JLabel label4 = new JLabel("Feature source (String) *");
    JLabel label6 = new JLabel("Nr elements (Integer) *");
    JLabel label7 = new JLabel("Size (Integer) *");
    JLabel label8 = new JLabel("Algorithm");
    JLabel label9 = new JLabel("Allow overlap");
    JLabel label10 = new JLabel("Use penalisation of incomplete instances");
    JLabel label11 = new JLabel("Exclude matrices (comma separated) (String)");
    JLabel label12 = new JLabel("Nr of top scoring modules to return (Integer) *");

    get11 = new JButton("GET");
    get11.addActionListener(this);

    elementsText = new JTextField("5", 20);
    sizeText = new JTextField("200", 20);

    soapCmb = new JComboBox(GlobalProperties.getSOAPServers());
    soapCmb.setEditable(true);

    featVec = new Vector();
    HashSet temp = window.gl.getDistinctFeatSources();
    int scanInd = 0;
    String str;
    int ind = 0;
    for (Iterator it = temp.iterator(); it.hasNext(); ) {
      str = (String) it.next();
      featVec.add(str);
      if (str.equalsIgnoreCase("MotifScanner"))
        scanInd = ind;
      ind++;
    }
    featList = new JList(featVec);
    featList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    //if (scanInd != 0)
      featList.setSelectedIndex(scanInd);
    txt11 = new JTextField("", 20);
    modulesText = new JTextField("1", 20);
    JScrollPane featListScrollPane = new JScrollPane(featList);
    if (featVec.size() > 5) featList.setVisibleRowCount(5);
    else featList.setVisibleRowCount(featVec.size());

    Object [] cmbItems = new Object[2];
    cmbItems[0] = "Genetic Algorithm";
    cmbItems[1] = "A* search";
    cmbAlgorithm = new JComboBox(cmbItems);
    cmbAlgorithm.setSelectedIndex(0);

    checkOverlap = new JCheckBox();
    checkPenalisation = new JCheckBox("",true);

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
    pane4.add(featListScrollPane);
    JPanel pane6 = new JPanel(lay);
    pane6.add(label6);
    pane6.add(elementsText);
    JPanel pane7 = new JPanel(lay);
    pane7.add(label7);
    pane7.add(sizeText);
    JPanel pane8 = new JPanel(lay);
    pane8.add(label8);
    pane8.add(cmbAlgorithm);
    JPanel pane9 = new JPanel(lay);
    pane9.add(label9);
    pane9.add(checkOverlap);
    JPanel pane10 = new JPanel(lay);
    pane10.add(label10);
    pane10.add(checkPenalisation);
    JPanel pane11 = new JPanel(lay);
    JPanel pane11bis = new JPanel();
    pane11bis.setLayout(new BoxLayout(pane11bis, BoxLayout.X_AXIS));
    pane11bis.add(label11);
    JPanel buttonPane11 = new JPanel();
    buttonPane11.add(get11);
    pane11bis.add(buttonPane11);
    pane11.add(pane11bis);
    pane11.add(txt11);
    JPanel pane12 = new JPanel(lay);
    pane12.add(label12);
    pane12.add(modulesText);
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(ok);
    buttonPanel.add(cancel);

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.add(pane1);
    mainPanel.add(pane4);
    mainPanel.add(pane8);
    mainPanel.add(pane6);
    mainPanel.add(pane7);
    mainPanel.add(pane9);
    mainPanel.add(pane10);
    mainPanel.add(pane11);
    mainPanel.add(pane12);
    mainPanel.add(buttonPanel);

    getContentPane().add(mainPanel);

    pack();

  }

  public void actionPerformed(ActionEvent e) {
    try {
      if (e.getSource() == ok) {
        if (featList.getSelectedIndices().length == 0 ||
            ((String)soapCmb.getSelectedItem()).equalsIgnoreCase("") ||
            elementsText.getText().equalsIgnoreCase("") ||
            sizeText.getText().equalsIgnoreCase("") ||
            modulesText.getText().equalsIgnoreCase("")) {
            JOptionPane.showMessageDialog(this,
                                          "Please fill in all required (*) fields",
                                          "Missing values",
                                          JOptionPane.ERROR_MESSAGE);
          }
        else {
          ServiceRunner sr = new ServiceRunner(owner,
                                               ServiceRunner.MODULESEARCHER);
          sr.setGFFStr(owner.gl.toGFF(featList.getSelectedValues()));
          sr.setSoapServerUrl((String)soapCmb.getSelectedItem());

          sr.setNrElements(Integer.parseInt(elementsText.getText()));
          sr.setSize(Integer.parseInt(sizeText.getText()));
          sr.setAlgorithm(cmbAlgorithm.getSelectedIndex());
          sr.setOverlap(checkOverlap.isSelected());
          sr.setPenalisation(checkPenalisation.isSelected());
          sr.setExclude((String) txt11.getText());
          sr.setNrModules(Integer.parseInt(modulesText.getText()));

          Object[] msg = new Object[3];
          msg[0] = "This job will be performed remotely. \n You will be notified by TOUCAN when the results are ready.";
          //msg[0] = "Note: a backup SOAP server at another location \n will be used automatically if the chosen server is down.";
          //msg[0] = "The service will be started when you press OK";
          //msg[1] = "You will be notified when the results are ready";
          //msg[2] = "If you close the Toucan application, the results will be lost";
          JOptionPane.showMessageDialog(this, msg);
          sr.start();
          setVisible(false);
        }
      }
      else if (e.getSource() == get11) {
        MultiInputDialog mid = new MultiInputDialog(owner, getMotifNames());
        mid.pack();
        mid.setVisible(true);
        Object[] values = mid.getSelectedValues();
        String temp = "";
        for(int i = 0; i<values.length;i++){
          temp += values[i] + ",";
        }
        temp = temp.substring(0, temp.length()-1);
        txt11.setText(temp);
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

  public void motifNames(String soapUrl) throws Exception {
    try {
      String xmlStr = SOAPClient.getMotifNames(GlobalProperties.getDebugYesNo(),soapUrl);
      this.motifNames = new Vector();
      if (xmlStr != null) {
        paramXml = XMLParser.parseString(xmlStr);
      }
      else {
        throw new Exception("Could not get parameters - Service may be down");
      }
      NodeList nl = paramXml.getElementsByTagName("motif");
      Node n;
      NamedNodeMap map;
      String id, name, descr;
      for (int i = 0; i < nl.getLength(); i++) {
        n = nl.item(i);
        map = n.getAttributes();
        name = n.getChildNodes().item(0).getChildNodes().item(0).getNodeValue();
        motifNames.add(name);
      }
      gotMotifNames = true;
    }
    catch (Exception exc) {
      exc.printStackTrace();
      /*if(exc.getMessage().indexOf("Connection refused")!=-1){
        JOptionPane.showMessageDialog(owner,
                                    "This server seems unavailable, please choose another server",
                                    "Error", JOptionPane.ERROR_MESSAGE);
      }*/
      if(!(soapUrl).equals(GlobalProperties.getSOAPBackup())){
          motifNames(GlobalProperties.getSOAPBackup());
       }
      else
      JOptionPane.showMessageDialog(owner,"This SOAP server is not available, please choose another server in the start dialog","Warning",JOptionPane.WARNING_MESSAGE);
    }
  }

  public Vector getMotifNames() throws Exception{
    if (!gotMotifNames) {
      motifNames((String)soapCmb.getSelectedItem());
    }
    return this.motifNames;
  }

}
