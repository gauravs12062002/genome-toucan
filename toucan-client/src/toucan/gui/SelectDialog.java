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
import toucan.*;
import org.biojava.bio.seq.*;
import java.util.*;
import javax.swing.JOptionPane;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JFrame;
import java.beans.*; //Property change stuff
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;

/**
 * Title: SelectDialog
 * Description: Dialog to cut pieces out of a certain sequence. Used in a gene popup window.
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 2.0
 */


public class SelectDialog extends JDialog{

  private JComboBox featCmb,wayCmb;
  private JTextField befTxt,startTxt;
  private JTextField aftTxt,endTxt;
  private MainFrame owner;

  public SelectDialog(MainFrame window) {

        super(window,"Cut sequence",true);
        owner = window;
	String[] featStrings = new String[owner.featList.size()];
	for (int i=0;i<owner.featList.size();i++){
	 Feature f = (Feature)owner.featList.get(i);
	 if (f.getType().equalsIgnoreCase("misc_feature")) featStrings[i] = (String)f.getAnnotation().getProperty("type");
	 else featStrings[i] = f.getType();
	}
	featCmb = new JComboBox(featStrings);

	String[] wayString = new String[3];
	wayString[0] = "Around";
	wayString[1] = "Left side";
	wayString[2] = "Right side";

	wayCmb = new JComboBox(wayString);

	final int numButtons = 2;
        JRadioButton[] radioButtons = new JRadioButton[numButtons];
        final ButtonGroup group = new ButtonGroup();
        radioButtons[0] = new JRadioButton("Around Feature");
        radioButtons[0].setActionCommand("feat");
        radioButtons[1] = new JRadioButton("Specify");
        radioButtons[1].setActionCommand("specify");
        for (int i = 0; i < numButtons; i++) {
            group.add(radioButtons[i]);
        }
        radioButtons[0].setSelected(true);

        //radiobuttons to choose whether to take only this feature or all such features
        JRadioButton[] oneAll = new JRadioButton[5];
        final ButtonGroup oneAllGroup = new ButtonGroup();
        oneAll[0] = new JRadioButton("Only this feature");
        oneAll[0].setActionCommand("one");
        oneAll[1] = new JRadioButton("All features with the same type on this sequence");
        oneAll[1].setActionCommand("allSeqType");
        oneAll[2] = new JRadioButton("All features with the same source on this sequence");
        oneAll[2].setActionCommand("allSeqSource");
        oneAll[3] = new JRadioButton("All features with the same type on all sequences");
        oneAll[3].setActionCommand("allSetType");
        oneAll[4] = new JRadioButton("All features with the same source on all sequences");
        oneAll[4].setActionCommand("allSetSource");
        oneAllGroup.add(oneAll[0]);
        oneAllGroup.add(oneAll[1]);
        oneAllGroup.add(oneAll[2]);
        oneAllGroup.add(oneAll[3]);
        oneAllGroup.add(oneAll[4]);
        oneAll[0].setSelected(true);

	JRadioButton[] radioGoal = new JRadioButton[2];
        final ButtonGroup groupGoal = new ButtonGroup();
        radioGoal[0] = new JRadioButton("Add to sublist");
        radioGoal[0].setActionCommand("sub");
        radioGoal[1] = new JRadioButton("Replace parent sequence");
        radioGoal[1].setActionCommand("replace");
        for (int i = 0; i < 2; i++) {
            groupGoal.add(radioGoal[i]);
        }
        radioGoal[0].setSelected(true);

        JButton submitButton = new JButton("OK");
	submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int start=0,end=0;
		String goalCommand = groupGoal.getSelection().getActionCommand();
		String command = group.getSelection().getActionCommand();
                String oneAllCmd = oneAllGroup.getSelection().getActionCommand();
                Gene parent = owner.getSelectedGene();
                GeneList gl = new GeneList();
                Gene g = new Gene();
                if (command.equalsIgnoreCase("feat")) {
		     try{
                       if(oneAllCmd.startsWith("all") && goalCommand.equalsIgnoreCase("replace")){
                         throw new Exception("You can only add multiple subsequences to the sublist");
                       }
                       Feature f = (Feature)owner.featList.get(featCmb.getSelectedIndex());
                       int bpBefore = new Integer(befTxt.getText()).intValue();
                       int bpAfter = new Integer(aftTxt.getText()).intValue();
                       //one
                       if(oneAllCmd.equalsIgnoreCase("one")){
                         if(wayCmb.getSelectedIndex()==0){ //around
                           start = f.getLocation().getMin()-bpBefore;
                           end = f.getLocation().getMax()+bpAfter;
                         }
                         else if(wayCmb.getSelectedIndex()==1){ //left side
                           start = f.getLocation().getMin()-bpBefore;
                           end = f.getLocation().getMin()+bpAfter;
                           //g.setSequence(parent.makeMySubSeq(f.getLocation().getMin()-bpBefore,f.getLocation().getMin()+bpAfter),null);
                         }
                         else if(wayCmb.getSelectedIndex()==2){ //right side
                           start = f.getLocation().getMax()-bpBefore;
                           end = f.getLocation().getMax()+bpAfter;
                           //g.setSequence(parent.makeMySubSeq(f.getLocation().getMax()-bpBefore,f.getLocation().getMax()+bpAfter),null);
                         }
                         g.setSequence(parent.makeMySubSeq(start,end,parent.name+"_"+start+"_"+end),parent.name+"_"+start+"_"+end);
                         gl.add(g);
		      }
                      //all on this seq
                      else if(oneAllCmd.startsWith("allSeq")){
                        if(oneAllCmd.startsWith("allSeqType")){
                          String featStr = null, annKey = null, annValue = null;
                          if (f.getType().equalsIgnoreCase("misc_feature")) {
                            annKey = "type";
                            annValue = (String) f.getAnnotation().getProperty("type");
                            gl = parent.getSubSequences("misc_feature", annKey, annValue,
                                                        wayCmb.getSelectedItem().toString(),
                                                        bpBefore, bpAfter,false);
                          }
                          else {
                            featStr = f.getType();
                            gl = parent.getSubSequences(featStr, null, null,
                                                        wayCmb.getSelectedItem().toString(),
                                                        bpBefore, bpAfter,false);
                          }
                        }
                        else if (oneAllCmd.startsWith("allSeqSource")){
                          if(f.getAnnotation().containsProperty("source")){
                            System.out.println(f.getAnnotation().
                                                        getProperty("source"));
                            gl = parent.getSubSequences( (String) f.getAnnotation().
                                                        getProperty("source"),
                                                        wayCmb.getSelectedItem().toString(),
                                                        bpBefore, bpAfter,false);
                          }
                          else{
                            gl = parent.getSubSequences(f.getSource(),
                                                        wayCmb.getSelectedItem().toString(),
                                                        bpBefore, bpAfter,false);
                          }
                        }
                      }
                      //all on the complete set
                      else if (oneAllCmd.startsWith("allSet")){
                        if(oneAllCmd.startsWith("allSetType")){
                          String featStr = null, annKey = null, annValue = null;
                          if (f.getType().equalsIgnoreCase("misc_feature")) {
                            annKey = "type";
                            annValue = (String) f.getAnnotation().getProperty("type");
                            gl = owner.gl.getSubSequences("misc_feature", annKey, annValue,
                                                        wayCmb.getSelectedItem().toString(),
                                                        bpBefore, bpAfter,false);
                          }
                          else {
                            featStr = f.getType();
                            gl = owner.gl.getSubSequences(featStr, null, null,
                                                        wayCmb.getSelectedItem().toString(),
                                                        bpBefore, bpAfter,false);
                          }
                        }
                        else if (oneAllCmd.startsWith("allSetSource")){
                          if(f.getAnnotation().containsProperty("source")){
                            System.out.println(f.getAnnotation().
                                                        getProperty("source"));
                            gl = owner.gl.getSubSequences( (String) f.getAnnotation().
                                                        getProperty("source"),
                                                        wayCmb.getSelectedItem().toString(),
                                                        bpBefore, bpAfter,false);
                          }
                          else{
                            gl = owner.gl.getSubSequences(f.getSource(),
                                                        wayCmb.getSelectedItem().toString(),
                                                        bpBefore, bpAfter,false);
                          }

                                 //gl = owner.gl.getSubSequences(f.getSource(),
                                 //                             wayCmb.getSelectedItem().toString(),
                                 //                             bpBefore, bpAfter);
                        }
                      }
		     }catch(Exception exc){
                       if(exc.getMessage()!=null && !exc.getMessage().equalsIgnoreCase("")){
                         JOptionPane.showMessageDialog(SelectDialog.this,exc.getMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
                         exc.printStackTrace();
                       }
                       else
                         JOptionPane.showMessageDialog(SelectDialog.this,"This is not possible","ERROR",JOptionPane.ERROR_MESSAGE);
                         exc.printStackTrace();
		      }
		}
		else if (command.equalsIgnoreCase("specify")) {
		    try{
		      start = new Integer(startTxt.getText()).intValue();
		      end = new Integer(endTxt.getText()).intValue();
		      if(goalCommand.equalsIgnoreCase("replace")){
                          g.setSequence(parent.makeMySubSeq(start,end,parent.name),parent.name);
		      }
                      else{
                        g.setSequence(parent.makeMySubSeq(start,end,parent.name+"_"+start+"_"+end),parent.name+"_"+start+"_"+end);
                      }
                      gl.add(g);
		    }catch(Exception exc){exc.printStackTrace();}
		}
		if(goalCommand.equalsIgnoreCase("sub")){
		  //g.name = g.name +"_"+start+"_"+end;
		  //g.ensembl = g.ensembl +"_"+start+"_"+end;
		  for(Iterator it=gl.list.iterator();it.hasNext();){
                    g = (Gene)it.next();
                    SelectDialog.this.owner.subList.add(g);
                    SelectDialog.this.owner.addToNameList(g.name);
                    setVisible(false);
		  }
		}
		else if (goalCommand.equalsIgnoreCase("replace")){
                  try{
                    if(gl.list.size()>1)
                      throw new Exception("You can only add multiple subsequences to the sublist");
		    //if the parent seq is replaced, then no _start_end needed anymore + will get too confusion if a subseq of this is taken (_start_end_start2_end2 etc.)
                    if(g.seq!=null) parent.setSequence(g.seq,g.seq.getName().substring(0,g.seq.getName().indexOf("_")));
                    else throw new Exception("Sequence cutting failed");
		    SelectDialog.this.owner.setImgSelectedFeatures();
		    setVisible(false);
		  }catch(Exception exc){
                        JOptionPane.showMessageDialog(SelectDialog.this,exc.getMessage(),"ERROR",JOptionPane.ERROR_MESSAGE);
                  }
		}
		return;
            }
        });

	JButton cancel = new JButton("Cancel");
	cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });


	JLabel befLabel = new JLabel("Bp left: ");
	JLabel aftLabel = new JLabel("Bp right: ");
	befTxt = new JTextField("0");
	befTxt.setColumns(5);
	aftTxt = new JTextField("0");
	aftTxt.setColumns(5);

	JLabel startLabel = new JLabel("From: ");
	JLabel endLabel = new JLabel("To: ");
	startTxt = new JTextField("1");
	startTxt.setColumns(5);
	endTxt = new JTextField("100");
	endTxt.setColumns(5);

	JPanel top = new JPanel();
	top.setBorder(BorderFactory.createLineBorder(Color.gray,1));
        top.setLayout(new FlowLayout());
	top.add(radioButtons[0]);
	top.add(featCmb);
	top.add(wayCmb);
	top.add(befLabel);
	top.add(befTxt);
	top.add(aftLabel);
	top.add(aftTxt);

        JPanel oneAllPanel = new JPanel();
        oneAllPanel.setLayout(new BoxLayout(oneAllPanel,BoxLayout.Y_AXIS));
        oneAllPanel.add(oneAll[0]);
        oneAllPanel.add(oneAll[1]);
        oneAllPanel.add(oneAll[2]);
        oneAllPanel.add(oneAll[3]);
        oneAllPanel.add(oneAll[4]);

        JPanel center = new JPanel();
	center.setBorder(BorderFactory.createLineBorder(Color.gray,1));
        center.setLayout(new FlowLayout());
	center.add(radioButtons[1]);
	center.add(startLabel);
	center.add(startTxt);
	center.add(endLabel);
	center.add(endTxt);

	JPanel box = new JPanel();
	box.setLayout(new BoxLayout(box,BoxLayout.Y_AXIS));
	box.add(radioGoal[0]);
	box.add(radioGoal[1]);

	JPanel bottom = new JPanel();
	//bottom.setBorder(BorderFactory.createLineBorder(Color.blue,1));
	bottom.add(box);
	bottom.add(submitButton);
	bottom.add(cancel);

	JPanel sep = new JPanel();
	sep.setPreferredSize(new Dimension(100,10));
	JPanel sep1 = new JPanel();
	sep1.setPreferredSize(new Dimension(100,10));
	//sep.setBorder(BorderFactory.createLineBorder(Color.gray,2));
	JPanel sep2 = new JPanel();
	sep2.setPreferredSize(new Dimension(100,10));
	//sep2.setBorder(BorderFactory.createLineBorder(Color.gray,2));

	Container con = this.getContentPane();
	con.setLayout(new BoxLayout(con,BoxLayout.Y_AXIS));
	con.add(sep1);
	con.add(top);
        con.add(oneAllPanel);
	con.add(sep);
	con.add(center);
	con.add(sep2);
	con.add(bottom);
	this.pack();
  }
}
