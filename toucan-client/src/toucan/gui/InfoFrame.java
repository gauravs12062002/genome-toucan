
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

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLDocument;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class InfoFrame extends JFrame implements ActionListener {

  private JTextArea infoText;
  private JTextPane infoPane;
  private JScrollPane infoScroll;
  private JPanel buttonPane;
  private JButton okButton;

  public InfoFrame(String title, String info) {
    super(title);
    init(info);
  }

  private void init(String info) {
    // text pane for info
    infoPane = new JTextPane();
    infoPane.setEditable(false);
    infoPane.setCaretPosition(0);
    infoPane.setContentType("text");
    infoPane.setText(info);
    // scroll pane to put text area on
    infoScroll = new JScrollPane(infoPane);
    infoScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    infoScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
    // add everything to frame and visualize
    getContentPane().add(infoScroll);
    setLocation(50,50);
    // get dimensions of screen
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    setSize(new Dimension(screenSize.width*3/4, screenSize.height*3/4));
    infoPane.setCaretPosition(0);
  }

  public void cursorToTop() {
    //infoText.setCaretPosition(0);
    infoPane.setCaretPosition(0);
  }

  public void actionPerformed(ActionEvent ae) {
    if (ae.getActionCommand().equals("dialogOk")) {
      setVisible(false);
    }
  }
}
