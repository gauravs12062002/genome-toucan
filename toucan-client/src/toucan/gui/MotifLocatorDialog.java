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
import toucan.ext.SOAPClient;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import toucan.*;
import org.w3c.dom.*;
import java.io.*;

/**
 * Title: MotifLocatorDialog
 * Description: Dialog to run the MotifLocator service
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 2.0
 */


public class MotifLocatorDialog extends JDialog implements ActionListener{

  private JButton ok,cancel;
  private JTextField txt5,txt6,txt7,txt8,txt9;
  private JButton get5,get7,browse5,browse7;
  private MainFrame owner;
  private HashMap bgParams,mtrxParams;
  private boolean gotParams=false;
  private Document paramXml;
  private JFileChooser fc;
  private String str5,str7;
  private JComboBox subjectCombo, soapCmb;

  public MotifLocatorDialog(MainFrame window) {
    super(window,"MotifLocator Web Service",true);
    owner = window;
    JLabel label1 = new JLabel("Service URL *");
    JLabel label4 = new JLabel("P1: fastA sequences * (String)");
    JLabel label5 = new JLabel("P2: PWM Database * (String)");
    JLabel label7 = new JLabel("P4: Background Model * (String)");
    JLabel label6 = new JLabel("P3: Threshold (double)");
    JLabel label8 = new JLabel("P5: PWM Subset (String)");
    JLabel label9 = new JLabel("P6: Strand (Integer)");
    JLabel label10 = new JLabel("P7: Type of score (String)");
    soapCmb = new JComboBox(GlobalProperties.getSOAPServers());
    soapCmb.setEditable(true);
    Object[] subjects = new Object[2];
    subjects[0] = "Use complete active sequence set";
    subjects[1] = "Use sublist";
    subjectCombo = new JComboBox(subjects);
    txt5 = new JTextField("Choose get or browse",20); //mtrx
    txt5.setEditable(false);
    txt6 = new JTextField("0.9",20);//prior
    txt7 = new JTextField("Choose get or browse",20);//bgModel
    txt7.setEditable(false);
    txt8 = new JTextField(20);//mtrx list
    txt9 = new JTextField("1",20);//Strand
    get5 = new JButton("GET");
    get7 = new JButton("GET");
    browse5 = new JButton("Browse");
    browse7 = new JButton("Browse");
    get5.addActionListener(this);
    get7.addActionListener(this);
    browse5.addActionListener(this);
    browse7.addActionListener(this);
    ok = new JButton("OK");
    cancel = new JButton("Cancel");
    ok.setPreferredSize(new Dimension(80,20));
    cancel.setPreferredSize(new Dimension(80,20));
    ok.addActionListener(this);
    cancel.addActionListener(this);

    GridLayout lay = new GridLayout(0,2,0,0);
    JPanel pane1 = new JPanel(lay);
    pane1.add(label1);
    pane1.add(soapCmb);
    JPanel pane4 = new JPanel(lay);
    pane4.add(label4);
    pane4.add(subjectCombo);
    JPanel pane5 = new JPanel(lay);
    JPanel pane5bis = new JPanel();
    pane5bis.setLayout(new BoxLayout(pane5bis, BoxLayout.X_AXIS));
    pane5bis.add(label5);
    JPanel buttonPane5 = new JPanel();
    buttonPane5.add(get5);
    buttonPane5.add(browse5);
    pane5bis.add(buttonPane5);
    pane5.add(pane5bis);
    pane5.add(txt5);

    JPanel pane6 = new JPanel(lay);
    pane6.add(label6);
    pane6.add(txt6);
    JPanel pane7 = new JPanel(lay);
    JPanel pane7bis = new JPanel();
    pane7bis.setLayout(new BoxLayout(pane7bis, BoxLayout.X_AXIS));
    pane7bis.add(label7);
    JPanel buttonPane7 = new JPanel();
    buttonPane7.add(get7);
    buttonPane7.add(browse7);
    pane7bis.add(buttonPane7);
    pane7.add(pane7bis);
    pane7.add(txt7);

    JPanel pane8 = new JPanel(lay);
    pane8.add(label8);
    pane8.add(txt8);
    JPanel pane9 = new JPanel(lay);
    pane9.add(label9);
    pane9.add(txt9);
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
    mainPanel.add(buttonPanel);

    getContentPane().add(mainPanel);

    fc = new JFileChooser(GlobalProperties.getCurrentDir());

    this.pack();

  }

  public void actionPerformed(ActionEvent e){
    try{
        if (e.getSource()==ok){
          //lengthRestriction
           if(((String)soapCmb.getSelectedItem()).equalsIgnoreCase("") || txt5.getText().equalsIgnoreCase("") || txt7.getText().equalsIgnoreCase("") || txt5.getText().equalsIgnoreCase("Choose get or browse") || txt7.getText().equalsIgnoreCase("Choose get or browse"))
              JOptionPane.showMessageDialog(this,"Please fill in all required (*) fields","Missing values",JOptionPane.ERROR_MESSAGE);
           else{
             ServiceRunner sr = new ServiceRunner(owner,ServiceRunner.MOTIFLOCATOR);
             sr.setSoapServerUrl((String)soapCmb.getSelectedItem());
             if (subjectCombo.getSelectedIndex() == 0) {
               sr.setFastaStr(owner.gl.getFastaStr());
             }
             else if (subjectCombo.getSelectedIndex() == 1) {
               sr.setFastaStr(owner.subList.getFastaStr().replace('_', '@'));
             }
             sr.setMtrx(str5);
             if(!txt6.getText().equals("")) sr.setThreshold(new Double(txt6.getText()).doubleValue());
             sr.setBg(str7);
             sr.setList(txt8.getText());
             if (!txt9.getText().equals("")) sr.setStrand(new Integer(txt9.getText()).intValue());
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
        }
        else if (e.getSource()==get5){
          //HashMap mtrxMap = new HashMap();
          HashMap mtrxMap = getMtrxParams();
          Object[] possibleValues = mtrxMap.keySet().toArray();
          Arrays.sort(possibleValues);
          JOptionPane dialog = new JOptionPane();
          Object selectedValue = dialog.showInputDialog(this,
                      "Choose one of the database values:",
                      "Select", JOptionPane.QUESTION_MESSAGE,
                      null, possibleValues, possibleValues[0] );
          txt5.setText((String)mtrxMap.get(selectedValue));
          str5 = txt5.getText();
        }
        else if (e.getSource()==get7){
          //HashMap bgMap = new HashMap();
          HashMap bgMap = getBgParams();
          Object[] possibleValues = bgMap.keySet().toArray();
          Arrays.sort(possibleValues);
          JOptionPane dialog = new JOptionPane();
          Object selectedValue = dialog.showInputDialog( this,
                      "Choose one of the database values:",
                      "Select", JOptionPane.QUESTION_MESSAGE,
                      null, possibleValues, possibleValues[0] );
          txt7.setText((String)bgMap.get(selectedValue));
          str7 = txt7.getText();
        }
        else if (e.getSource()==browse5){
          fc.addChoosableFileFilter(new SomeFileFilter.TxtFileFilter());
          int returnVal = fc.showOpenDialog(owner);
          if (returnVal == JFileChooser.APPROVE_OPTION) {
                        try{
                          File file = fc.getSelectedFile();
                          String fileName = fc.getSelectedFile().getAbsolutePath();
                          owner.writeToStatus("Opened "+fileName);
                          str5 = toucan.util.Tools.fileToString(file);
                          txt5.setText(file.getName());
                          GlobalProperties.setCurrentDir(fc.getSelectedFile().getParent());
                        }
                        catch(Exception exc){exc.printStackTrace();}
          } else {
            owner.writeToStatus("Open command cancelled by user.");
          }
        }
        else if (e.getSource()==browse7){
          fc.addChoosableFileFilter(new SomeFileFilter.TxtFileFilter());
          int returnVal = fc.showOpenDialog(owner);
          if (returnVal == JFileChooser.APPROVE_OPTION) {
                        try{
                          File file = fc.getSelectedFile();
                          String fileName = fc.getSelectedFile().getAbsolutePath();
                          owner.writeToStatus("Opened "+fileName);
                          str7 = toucan.util.Tools.fileToString(file);
                          txt7.setText(file.getName());
                        }
                        catch(Exception exc){exc.printStackTrace();}
          } else {
            owner.writeToStatus("Open command cancelled by user.");
          }
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

  public void getParams(String soapUrl)throws Exception{
    try{
      String xmlStr = SOAPClient.getParams(GlobalProperties.getDebugYesNo(),soapUrl);
      this.bgParams = new HashMap();
      this.mtrxParams = new HashMap();
      if(xmlStr!=null) paramXml = XMLParser.parseString(xmlStr);
      else throw new Exception("Could not get parameters - Service may be down");
      NodeList nl = paramXml.getElementsByTagName("param");
      Node n;
      NamedNodeMap map;
      String id,name,descr;
      for (int i=0;i<nl.getLength();i++){
        n = nl.item(i);
        map = n.getAttributes();
        if (map.getNamedItem("type").getNodeValue().equalsIgnoreCase("bg")){
           id = map.getNamedItem("id").getNodeValue();
           name = n.getChildNodes().item(0).getChildNodes().item(0).getNodeValue();
           descr = n.getChildNodes().item(1).getChildNodes().item(0).getNodeValue();
           bgParams.put(descr,name);
        }
        else if (map.getNamedItem("type").getNodeValue().equalsIgnoreCase("mtrx")){
           id = map.getNamedItem("id").getNodeValue();
           name = n.getChildNodes().item(0).getChildNodes().item(0).getNodeValue();
           descr = n.getChildNodes().item(1).getChildNodes().item(0).getNodeValue();
           mtrxParams.put(descr,name);
        }
      }
      gotParams=true;
    }
    catch(Exception exc){
     exc.printStackTrace();
     /*if(exc.getMessage().indexOf("Connection refused")!=-1){
        JOptionPane.showMessageDialog(owner,
                                    "This server seems unavailable, please choose another server",
                                    "Error", JOptionPane.ERROR_MESSAGE);
      }*/
     if(!(soapUrl).equals(GlobalProperties.getSOAPBackup())){
       getParams(GlobalProperties.getSOAPBackup());
    }
      else
     JOptionPane.showMessageDialog(owner,"This SOAP server is not available, please choose another server in the start dialog","Warning",JOptionPane.WARNING_MESSAGE);
    }
  }

  public HashMap getBgParams()throws Exception{
   if(gotParams) return this.bgParams;
   else{
    getParams((String)soapCmb.getSelectedItem());
    return bgParams;
   }
  }

  public HashMap getMtrxParams()throws Exception{
   if(gotParams) return this.mtrxParams;
   else{
    getParams((String)soapCmb.getSelectedItem());
    return mtrxParams;
   }
  }

}
