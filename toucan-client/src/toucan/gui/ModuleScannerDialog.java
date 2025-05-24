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
import toucan.*;
import org.w3c.dom.*;
import toucan.util.*;
import toucan.ext.SOAPClient;
import toucan.modulescanner.ModuleScanner;

/**
 * Title: ModuleScannerDialog
 * Description: Dialog to run the ModuleScanner service
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be> & Peter Van Loo <Peter.VanLoo@med.kuleuven.ac.be>
 * @version 2.0
 */


public class ModuleScannerDialog
    extends JDialog
    implements ActionListener {

  private JButton ok, cancel;
  private JTextField txt2, txt4, txt5;
  public JTextField txt3;
  private JButton get2,get3;
  private MainFrame owner;
  private HashMap bgParams, mtrxParams, dbParams;
  private Vector motifNames;
  private boolean gotParams = false, gotMotifNames=false;
  private Document paramXml;
  private JComboBox soapCmb;
  private JCheckBox penalizeCheck, overlapCheck;

  public ModuleScannerDialog(MainFrame window) {
    super(window, "ModuleScanner Web Service", true);
    owner = window;
    JLabel label1 = new JLabel("Service URL *");
    JLabel label2 = new JLabel("Subject * (String)");//subjecttype will be derived from the subject.
    JLabel label3 = new JLabel("Module * (String)");
    JLabel label4 = new JLabel("Size in basepairs * (int)");
    JLabel label5 = new JLabel("Top N to return * (int)");
    JLabel label6 = new JLabel("Overlap * (boolean)");
    JLabel label7 = new JLabel("Penalize short modules * (boolean)");

    soapCmb = new JComboBox(GlobalProperties.getSOAPServers());
    soapCmb.setEditable(true);
    txt2 = new JTextField("Press GET and choose a DB", 20); //subject
    txt3 = new JTextField("Choose get or enter String", 20); //module
    txt4 = new JTextField("200", 20); //size
    txt5 = new JTextField("20", 20); //cutoff

    get2 = new JButton("GET");
    get2.addActionListener(this);
    get3 = new JButton("GET");
    get3.addActionListener(this);

    overlapCheck = new JCheckBox();
    penalizeCheck = new JCheckBox("",true);

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
    JPanel pane2 = new JPanel(lay);
    JPanel pane2bis = new JPanel();
    pane2bis.setLayout(new BoxLayout(pane2bis, BoxLayout.X_AXIS));
    pane2bis.add(label2);
    JPanel buttonPane2 = new JPanel();
    buttonPane2.add(get2);
    pane2bis.add(buttonPane2);
    pane2.add(pane2bis);
    pane2.add(txt2);
    JPanel pane3 = new JPanel(lay);
    JPanel pane3bis = new JPanel();
    pane3bis.setLayout(new BoxLayout(pane3bis, BoxLayout.X_AXIS));
    pane3bis.add(label3);
    JPanel buttonPane3 = new JPanel();
    buttonPane3.add(get3);
    pane3bis.add(buttonPane3);
    pane3.add(pane3bis);
    pane3.add(txt3);
    JPanel pane4 = new JPanel(lay);
    pane4.add(label4);
    pane4.add(txt4);
    JPanel pane5 = new JPanel(lay);
    pane5.add(label5);
    pane5.add(txt5);
    JPanel pane6 = new JPanel(lay);
    pane6.add(label6);
    pane6.add(overlapCheck);
    JPanel pane7 = new JPanel(lay);
    pane7.add(label7);
    pane7.add(penalizeCheck);
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(ok);
    buttonPanel.add(cancel);

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.add(pane1);
    mainPanel.add(pane2);
    mainPanel.add(pane3);
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
        if (((String)soapCmb.getSelectedItem()).equalsIgnoreCase("") ||
            txt2.getText().equalsIgnoreCase("Press GET and choose a DB") ||
            txt2.getText().equalsIgnoreCase("") ||
            txt3.getText().equalsIgnoreCase("Choose get or enter String") ||
            txt3.getText().equalsIgnoreCase("") ||
            txt4.getText().equalsIgnoreCase("") ||
            txt5.getText().equalsIgnoreCase("")) {
          JOptionPane.showMessageDialog(this,
                                        "Please fill in all required (*) fields",
                                        "Missing values",
                                        JOptionPane.ERROR_MESSAGE);
        }
        else {
          ServiceRunner sr = new ServiceRunner(owner,ServiceRunner.MODULESCANNER);
          sr.setSoapServerUrl((String)soapCmb.getSelectedItem());
          if(txt2.equals("Use active sequence set as DB")){
            sr.setSubject(owner.gl.toGFF(null));
            sr.setSubjectType(ModuleScanner.GFFSTRING);
          }
          else{
            sr.setSubject(txt2.getText());
            sr.setSubjectType(ModuleScanner.LOCALEMBLDBPATH);
          }
          sr.setSize(Integer.parseInt(txt4.getText()));
          sr.setCutOff(Integer.parseInt(txt5.getText()));
          sr.setModule(txt3.getText());
          sr.setOverlap(overlapCheck.isSelected());
          sr.setPenalizeShort(penalizeCheck.isSelected());
          Object[] msg = new Object[3];
          msg[0] = "This job will be performed remotely. \n You will be notified by TOUCAN when the results are ready.";
          //msg[0] = "Note: a backup SOAP server at another location \n will be used automatically if the chosen server is down.";
          //msg[0] = "The service will be started when you press OK";
          //msg[1] = "You will be notified when the results are ready";
          //msg[2] =               "If you close the Toucan application, the results will be lost";
          JOptionPane.showMessageDialog(this, msg);
          sr.start();
          setVisible(false);
        }
      }
      else if (e.getSource() == get2) {
        HashMap dbMap = getDbParams();
        Object[] possibleValues = dbMap.keySet().toArray();
        JOptionPane dialog = new JOptionPane();
        Object selectedValue = dialog.showInputDialog(this,
            "Choose one of the database values:",
            "Select", JOptionPane.QUESTION_MESSAGE,
            null, possibleValues, possibleValues[0]);
        txt2.setText( (String) dbMap.get(selectedValue));
      }
      else if (e.getSource() == get3) {
        MultiInputDialog mid = new MultiInputDialog(owner,getMotifNames());
        mid.pack();
        mid.setVisible(true);
        Object[] values = mid.getSelectedValues();
        String temp="[";
        for(int i = 0; i<values.length;i++){
          temp += values[i] + ",";
        }
        temp = temp.substring(0, temp.length()-1)+"]";
        txt3.setText(temp);
      }
      else if (e.getSource() == cancel) {
        setVisible(false);
      }
    }
    catch (NumberFormatException nfe) {
      JOptionPane.showMessageDialog(this, "Wrong format for some parameters",
                                    "Error", JOptionPane.ERROR_MESSAGE);
      nfe.printStackTrace();
    }
    catch (Exception exc) {
      JOptionPane.showMessageDialog(this, exc.getMessage(), "Error",
                                    JOptionPane.ERROR_MESSAGE);
      exc.printStackTrace();
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
      if(!(soapUrl).equals(GlobalProperties.getSOAPBackup())){
      motifNames(GlobalProperties.getSOAPBackup());
   }else
     JOptionPane.showMessageDialog(owner,"This SOAP server is not available, please choose another server in the start dialog","Warning",JOptionPane.WARNING_MESSAGE);
    }
  }

  public void getParams(String soapUrl) throws Exception {
    try {
      String xmlStr = SOAPClient.getParams(GlobalProperties.getDebugYesNo(),soapUrl);
      this.bgParams = new HashMap();
      this.mtrxParams = new HashMap();
      this.dbParams = new HashMap();
      if (xmlStr != null) {
        paramXml = XMLParser.parseString(xmlStr);
      }
      else {
        throw new Exception("Could not get parameters - Service may be down");
      }
      NodeList nl = paramXml.getElementsByTagName("param");
      Node n;
      NamedNodeMap map;
      String id, name, descr;
      for (int i = 0; i < nl.getLength(); i++) {
        n = nl.item(i);
        map = n.getAttributes();
        if (map.getNamedItem("type").getNodeValue().equalsIgnoreCase("bg")) {
          id = map.getNamedItem("id").getNodeValue();
          name = n.getChildNodes().item(0).getChildNodes().item(0).getNodeValue();
          descr = n.getChildNodes().item(1).getChildNodes().item(0).
              getNodeValue();
          bgParams.put(descr, name);
        }
        else if (map.getNamedItem("type").getNodeValue().equalsIgnoreCase(
            "mtrx")) {
          id = map.getNamedItem("id").getNodeValue();
          name = n.getChildNodes().item(0).getChildNodes().item(0).getNodeValue();
          descr = n.getChildNodes().item(1).getChildNodes().item(0).
              getNodeValue();
          mtrxParams.put(descr, name);
        }
        else if (map.getNamedItem("type").getNodeValue().equalsIgnoreCase(
            "db_embl")) {
          id = map.getNamedItem("id").getNodeValue();
          name = n.getChildNodes().item(0).getChildNodes().item(0).getNodeValue();
          descr = n.getChildNodes().item(1).getChildNodes().item(0).
              getNodeValue();
          dbParams.put(descr, name);
        }
      }
      gotParams = true;
    }
    catch (Exception exc) {
      exc.printStackTrace();
      /*if(exc.getMessage().indexOf("Connection refused")!=-1){
        JOptionPane.showMessageDialog(owner,
                                    "This server seems unavailable, please choose another server",
                                    "Error", JOptionPane.ERROR_MESSAGE);
      }
      */
     if(!soapUrl.equals(GlobalProperties.getSOAPBackup())){
       getParams(GlobalProperties.getSOAPBackup());
    }
      else
        JOptionPane.showMessageDialog(owner,"This SOAP server is not available, please choose another server in the start dialog","Warning",JOptionPane.WARNING_MESSAGE);
    }
  }

  public HashMap getBgParams() throws Exception {
    if (gotParams) {
      return this.bgParams;
    }
    else {
      getParams((String)soapCmb.getSelectedItem());
      return bgParams;
    }
  }

  public HashMap getMtrxParams() throws Exception {
    if (gotParams) {
      return this.mtrxParams;
    }
    else {
      getParams((String)soapCmb.getSelectedItem());
      return mtrxParams;
    }
  }

  public Vector getMotifNames() throws Exception{
    if (!gotMotifNames) {
      motifNames((String)soapCmb.getSelectedItem());
    }
    return this.motifNames;
  }

  public HashMap getDbParams() throws Exception {
    if (gotParams) {
      return this.dbParams;
    }
    else {
      getParams((String)soapCmb.getSelectedItem());
      return dbParams;
    }
  }

}
