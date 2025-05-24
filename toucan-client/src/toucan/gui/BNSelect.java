
/*This file is part of TOUCAN
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
/**
 * Title: BNSelect
 * Description: Dialog to cut all sequences in pieces of a certain length; the new set replaces the parent set
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 2.0
 */

public class BNSelect extends JDialog implements ActionListener{

  private JButton ok;
  private JButton cancel;
  private MainFrame window;
  private JTextField windowLength;
  private JTextField offset;
  private JTextField overlap;
  public boolean cancelled=false;

  public BNSelect(MainFrame window) {
    super(window,"Window Cut",true);
    this.window = window;

    JPanel buttonPane = new JPanel();
    ok = new JButton("OK");
    ok.addActionListener(this);
    ok.setPreferredSize(new Dimension(80,20));
    cancel = new JButton("Cancel");
    cancel.setPreferredSize(new Dimension(80,20));
    cancel.addActionListener(this);
    buttonPane.add(ok);
    buttonPane.add(cancel);
    getContentPane().add(buttonPane,BorderLayout.SOUTH);
    JPanel panel1 = new JPanel();
    JLabel label1 = new JLabel("WindowLength:");
    JLabel label2 = new JLabel("Overlap:");
    JLabel label3 = new JLabel("Offset:");
    windowLength = new JTextField("100",5);
    overlap = new JTextField("0",5);
    offset = new JTextField("0",5);
    panel1.add(label1);
    panel1.add(windowLength);
    panel1.add(label2);
    panel1.add(overlap);
    panel1.add(label3);
    panel1.add(offset);
    getContentPane().add(panel1,BorderLayout.NORTH);
    this.pack();
  }

  public void actionPerformed(ActionEvent e){
      if (e.getSource()==ok){
	try{
          window.gl = window.gl.cutInPieces(new Integer(windowLength.getText()).intValue(),new Integer(overlap.getText()).intValue(),new Integer(offset.getText()).intValue());
          window.setImg();
	}
	catch(Exception exc){
	  exc.printStackTrace();
	  JOptionPane.showMessageDialog(BNSelect.this,exc.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);
	}
	setVisible(false);
      }

      else if (e.getSource()==cancel)
	   cancelled = true;
	   setVisible(false);
  }

}
