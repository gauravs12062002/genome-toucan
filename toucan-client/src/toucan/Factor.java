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
package toucan;
import java.awt.Color;
import java.lang.Math;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.Serializable;
/**
 * Title: Factor
 * Description: More of an abstract meaning; can be any type of annotation on a gene. Examples are CDS, 5'UTR, transcription factors, etc.
* A factor has 'instances' on a sequence, represented by FactorInstance class
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 2.0
 */

public class Factor implements Serializable {

  public Factor() {
         objectID="";
         objectType="";
         objectDescriptor="";
	 objectColor = this.randomColor();
	 objectFill = true;
	 freqReplaced = false;
	 setImageIcon();
         objectSource="";
         objectShow=true;
  }

  //Constructor if a color is given
  public Factor(Color c) {
         objectID="";
         objectType="";
         objectDescriptor="";
	 objectColor = c;
	 objectFill = true;
	 nrOccInSet = 0;
	 setImageIcon();
         objectSource="";
         objectShow=true;
  }

  public String objectID,objectType,objectDescriptor,objectSource;
  public Color objectColor;
  public boolean objectFill,freqReplaced,objectShow;
  public int nrOccInSet;
  public double pValue; //e.g. prob(occ) of binomial formula
  public double expFreq; //from reference set
  public double sig;
  public ImageIcon objectIcon;

  //overload toString() method:
  public String toString(){
   return this.objectDescriptor;
  }

  public void setImageIcon(){
   BufferedImage img = new BufferedImage(10,10,BufferedImage.TYPE_INT_RGB);
   Graphics2D g = img.createGraphics();
   g.setColor(this.objectColor);
   g.fillRect(0,0,img.getWidth(),img.getHeight());
   this.objectIcon = new ImageIcon(img);
  }

  public void setColor(Color c){
   objectColor = c;
   setImageIcon();
  }

  public Color randomColor(){
	 int rval = (int)Math.floor(Math.random() * 256);
         int gval = (int)Math.floor(Math.random() * 256);
         int bval = (int)Math.floor(Math.random() * 256);
         return new Color(rval,gval,bval);
  }

}
