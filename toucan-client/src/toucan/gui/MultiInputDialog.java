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
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import java.awt.Dimension;
import java.awt.Container;
import java.util.Vector;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * Title: MultiInputDialog
 * Description: Generic use
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 2.0
 */

public class MultiInputDialog extends JDialog implements ActionListener{

  public JList list;
  private MainFrame window;

  public MultiInputDialog(MainFrame window, Vector values){
    super(window,"Select multiple PWMs",true);
    init(values);
  }

  public void init(Vector values){
    list = new JList(values);
    list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    JScrollPane listScrollPane = new JScrollPane(list);
    list.setVisibleRowCount(10);
    JButton addButton = new JButton("Ok");
    addButton.addActionListener(this);
    addButton.setMnemonic('o');
    addButton.setActionCommand("ok");
    JButton cancelButton = new JButton("Cancel");
    cancelButton.addActionListener(this);
    cancelButton.setMnemonic('c');
    cancelButton.setActionCommand("cancel");
    JPanel buttonPane = new JPanel();
    buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
    buttonPane.setBorder(BorderFactory.createEmptyBorder(0,10,10,10));
    buttonPane.add(Box.createHorizontalGlue());
    buttonPane.add(addButton);
    buttonPane.add(Box.createRigidArea(new Dimension(10,0)));
    buttonPane.add(cancelButton);
    Container dialogContent = getContentPane();
    dialogContent.setLayout(new BoxLayout(dialogContent, BoxLayout.Y_AXIS));
    dialogContent.add(listScrollPane);
    dialogContent.add(buttonPane);
    this.pack();
  }
  public void actionPerformed(ActionEvent ae) {
      if (ae.getActionCommand().equals("cancel")) {
        setVisible(false);
      }
      if (ae.getActionCommand().equals("ok")) {
        setVisible(false);
      }
  }

  public Object[] getSelectedValues(){
    return list.getSelectedValues();
  }

}
