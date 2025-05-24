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
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

import toucan.*;
import toucan.ext.SOAPClient;
import org.w3c.dom.*;
import java.io.*;
import RSATWS.RSATWSPortTypeProxy;
import RSATWS.SupportedOrganismsRequest;
import RSATWS.SupportedOrganismsResponse;
/**
 * Title: MotifSamplerDialog
 * Description: Dialog to run the MotifSampler service
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 2.0
 */


public class OligoAnalysisDialog extends JDialog implements ActionListener{

  private JButton ok,cancel;
  private JTextField orgTxt,lengthTxt,strandTxt,overlapTxt,sigTxt;
  private JButton orgGetButton;
  private MainFrame owner;
  private HashMap bgParams;
  private boolean gotParams=false;
  private Document paramXml;
  private JFileChooser fc;
  private String str5;
  private JComboBox subjectCombo, soapCmb, bgsCombo;


  public OligoAnalysisDialog(MainFrame window) {
    super(window,"RSAT oligo-analysis Web Service",true);
    owner = window;
    JLabel urlLabel = new JLabel("Service URL");
    JLabel seqLabel = new JLabel("Sequences");
    JLabel orgLabel = new JLabel("Organism");
    JLabel bgLabel = new JLabel("Background");
    JLabel strandLabel = new JLabel("Strand");
    JLabel lengthLabel = new JLabel("Oligo length");
    JLabel overlapLabel = new JLabel("No overlap");
    JLabel sigLabel = new JLabel("Minimal sig value");
    soapCmb = new JComboBox(GlobalProperties.getSOAPServers());
    soapCmb.setEditable(true);
    Object[] subjects = new Object[2];
    subjects[0] = "Use complete active sequence set";
    subjects[1] = "Use sublist";
    subjectCombo = new JComboBox(subjects);
    Object[] bgs = new Object[3];
    bgs[0]="upstream-noorf";
    bgs[1]="upstream";
    bgs[2]="intergenic";
    bgsCombo=new JComboBox(bgs);
    orgTxt = new JTextField("Press GET button",20); //organism
    orgTxt.setEditable(false);
    strandTxt = new JTextField("2",20);//strand
    lengthTxt = new JTextField("6",20);//motifLength
    overlapTxt = new JTextField("1",20);//overlap
    sigTxt = new JTextField("1",20);//min sig
    
    orgGetButton = new JButton("GET");
    orgGetButton.addActionListener(this);
    ok = new JButton("OK");
    cancel = new JButton("Cancel");
    ok.setPreferredSize(new Dimension(80,20));
    cancel.setPreferredSize(new Dimension(80,20));
    ok.addActionListener(this);
    cancel.addActionListener(this);

    GridLayout lay = new GridLayout(0,2,13,0);
    JPanel pane1 = new JPanel(lay);
    pane1.add(urlLabel);
    //pane1.add(soapCmb);
    JPanel pane4 = new JPanel(lay);
    pane4.add(seqLabel);
    pane4.add(subjectCombo);
    JPanel pane5 = new JPanel(lay);
    JPanel pane5bis = new JPanel();
    pane5bis.setLayout(new BoxLayout(pane5bis, BoxLayout.X_AXIS));
    pane5bis.add(orgLabel);
    //pane5.add(txt5);
    JPanel buttonPane5 = new JPanel();
    buttonPane5.add(orgGetButton);
    //buttonPane5.add(browse5);
    pane5bis.add(buttonPane5);
    pane5.add(pane5bis);
    pane5.add(orgTxt);

    JPanel bgPanel = new JPanel(lay);
    pane4.add(bgLabel);
    pane4.add(bgsCombo);
    
    JPanel pane6 = new JPanel(lay);
    pane6.add(strandLabel);
    pane6.add(strandTxt);
    JPanel pane7 = new JPanel(lay);
    pane7.add(lengthLabel);
    pane7.add(lengthTxt);
    JPanel pane8 = new JPanel(lay);
    pane8.add(overlapLabel);
    pane8.add(overlapTxt);
    JPanel pane9 = new JPanel(lay);
    pane9.add(sigLabel);
    pane9.add(sigTxt);
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(ok);
    buttonPanel.add(cancel);

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
    mainPanel.add(pane1);
    mainPanel.add(pane4);
    mainPanel.add(pane5);
    mainPanel.add(pane6);
    mainPanel.add(pane7);
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
          if(orgTxt.getText().equalsIgnoreCase("Press GET button"))
        	  
	      JOptionPane.showMessageDialog(this,"Please fill in all required (*) fields","Missing values",JOptionPane.ERROR_MESSAGE);
          else{
        	  ServiceRunner sr = new ServiceRunner(owner,ServiceRunner.OLIGOANALYSIS);
              
            if (subjectCombo.getSelectedIndex() == 0) {
              sr.setFastaStr(owner.gl.getFastaStr());
            }
            else if (subjectCombo.getSelectedIndex() == 1) {
              sr.setFastaStr(owner.subList.getFastaStr().replace('_', '@'));
            }
            if(!orgTxt.getText().equals("")) sr.setoaOrg(orgTxt.getText());
            if(!overlapTxt.getText().equals("")) sr.setoaOverlap(Integer.parseInt(overlapTxt.getText()));
            sr.setBg(bgsCombo.getSelectedItem().toString());
            if(!sigTxt.getText().equals("")) sr.setoaMinSig(Integer.parseInt(sigTxt.getText()));
            if(!lengthTxt.getText().equals("")) sr.setoaLength(Integer.parseInt(lengthTxt.getText()));
            if(!strandTxt.getText().equals("")) sr.setoaStrand(Integer.parseInt(strandTxt.getText()));
            
            
   	       Object[] msg = new Object[3];
               msg[0] = "This job will be performed remotely. \n You will be notified by TOUCAN when the results are ready.";
 	     JOptionPane.showMessageDialog(this,msg);
             sr.start();
             setVisible(false);
	   }
	}
	else if (e.getSource()==orgGetButton){
	  HashMap bgMap = getBgParams();
	  Object[] possibleValues = bgMap.keySet().toArray();
	  Arrays.sort(possibleValues);
	  JOptionPane dialog = new JOptionPane();
	  Object selectedValue = dialog.showInputDialog( this,
		      "Choose one of the database values:",
		      "Select", JOptionPane.QUESTION_MESSAGE,
		      null, possibleValues, possibleValues[0] );
	  orgTxt.setText((String)bgMap.get(selectedValue));
	  str5 = orgTxt.getText();
	}
	else if (e.getSource()==cancel){
	 setVisible(false);
	}
    }
    catch(NumberFormatException nfe){
       JOptionPane.showMessageDialog(this,"Wrong format for some parameters","Error",JOptionPane.ERROR_MESSAGE);
    }
    catch(Exception exc){
       JOptionPane.showMessageDialog(this,exc.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
    }
  }

  public void getParams(String soapUrl)throws Exception{
    try{
      if(((String)soapCmb.getSelectedItem()).equalsIgnoreCase(""))
	      JOptionPane.showMessageDialog(this,"Please fill in all required (*) fields","Missing values",JOptionPane.ERROR_MESSAGE);
      else{
       this.bgParams = new HashMap();
       RSATWSPortTypeProxy proxy = new RSATWSPortTypeProxy();
       /* prepare the parameters */
        SupportedOrganismsRequest parameters = new SupportedOrganismsRequest();
       /* Call the service */
       System.out.println("Calling RSAT server...");
       SupportedOrganismsResponse res = proxy.supported_organisms(parameters);
       /* Process results  */
       //Report the remote command
       System.out.println("Command used on the server:"+ res.getCommand());
       //Report the result
       System.out.println("Gene(s) info(s):\n"+ res.getClient());
	   String resultStr=res.getClient();
	   //System.out.println("result="+resultStr);
	   String[][] suppOrgs = Tools.stringToArray2D(resultStr);
	   //System.out.println(suppOrgs[0][0]);
	   for(int i=0;i<suppOrgs.length;i++){
//		   for(int j=0;j<suppOrgs[0].length;j++){
		   		//System.out.println(suppOrgs[i][0].trim()+"|"+suppOrgs[i][1].trim());
			   //bgParams.put(suppOrgs[i][1].trim(),suppOrgs[i][0].trim());
		   System.out.println(suppOrgs[i][0].trim());
		   if(!suppOrgs[i][0].trim().startsWith("#")){
			   bgParams.put(suppOrgs[i][0].trim(),suppOrgs[i][0].trim());
		   }
//		   }
	   }
      // bgParams.put(descr,name);
	 
	gotParams=true;
      }
    }
    /*catch(org.xml.sax.SAXParseException spe){
      JOptionPane.showMessageDialog(owner, "This SOAP server is not available, please choose another server in the start dialog",
                                      "Information", JOptionPane.INFORMATION_MESSAGE);
    }
    catch(java.net.ConnectException ce){
      JOptionPane.showMessageDialog(owner, "This server is either not known or not available, please choose another server in the start dialog",
                                      "Information", JOptionPane.INFORMATION_MESSAGE);
    }*/
    catch(Exception exc){
      exc.printStackTrace();
      if(!soapUrl.equals(GlobalProperties.getSOAPBackup())){
        getParams(GlobalProperties.getSOAPBackup());
     }
     else
     /*if(exc.getMessage().indexOf("prolog")!=-1){
        JOptionPane.showMessageDialog(owner, "This SOAP server is not available, please choose another server in the start dialog",
                                      "Information", JOptionPane.INFORMATION_MESSAGE);
      }
      else*/
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

}

