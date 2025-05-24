
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
// Imports
import java.awt.*;
import javax.swing.*;
import javax.swing.ListCellRenderer.*;
import toucan.*;

/**
 * Title: CustomCellRenderer
 * Description:
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 2.0
 */


class CustomCellRenderer
	extends		JLabel
	implements	ListCellRenderer
{

	private MainFrame window;

	public CustomCellRenderer(MainFrame window)
	{
		this.window = window;
		setOpaque(true);

	}

	public Component getListCellRendererComponent(
			JList list, Object value, int index,
			boolean isSelected, boolean cellHasFocus )
	{
		// Display the text for this item
		setText(value.toString());

		// Set the correct image
		Factor o = (Factor) window.featShowListModel.get(index);
		try{
		  //if(window.currentlySelected.containsKey(o.objectDescriptor)){
                  if(o.objectShow){
				  ImageIcon ic = o.objectIcon;
				  setIcon( ic );
		  }
		  else setIcon(null);
		}
		catch(Exception exc){}
		// Draw the correct colors and font
		if( isSelected )
		{
			// Set the color and font for a selected item
			setBackground( Color.blue );
			setForeground( Color.white );
			setFont( new Font( "Roman", Font.PLAIN, 12 ) );
		}
		else
		{
			// Set the color and font for an unselected item
			setBackground( Color.white );
			setForeground( Color.black );
			setFont( new Font( "Roman", Font.PLAIN, 12 ) );
		}

		return this;
	}
}

