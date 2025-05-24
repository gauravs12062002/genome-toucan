
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
import java.util.*;
import java.io.*;

/**
 * Title: PreferencesDialog
 * Description:
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 2.0
 */


public class PreferencesDialog extends JDialog implements ActionListener{

  private MainFrame window;
  //private ArrayList labels;
  //private ArrayList txtFields;
  private TreeMap labelMap,txtMap;
  private JButton save,cancel;

  public PreferencesDialog(MainFrame window) {
    super();
    this.window=window;

	 JPanel buttonPane = new JPanel();
	 save = new JButton("Save");
	 cancel = new JButton("Cancel");
	 save.addActionListener(this);
	 cancel.addActionListener(this);
	 buttonPane.add(save);
	 buttonPane.add(cancel);
         getContentPane().add(buttonPane,BorderLayout.NORTH);
	 pack();
  }

  public void actionPerformed(ActionEvent e){
         if (e.getSource()==save){
	    try{

	    setVisible(false);
	    }catch(Exception exc){exc.printStackTrace();JOptionPane.showMessageDialog(this,"Problems writing properties file \n"+exc.getMessage(),"Error",JOptionPane.ERROR_MESSAGE);}
         }
	 else if (e.getSource()==cancel){
	  setVisible(false);
	 }
  }
}
