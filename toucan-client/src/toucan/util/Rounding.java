
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
package toucan.util;

/**
 * Title: Rounding
 * Description:
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 2.0
 */


import java.lang.Math;

  public class Rounding
  {
    public static String toString (double d, int place)
    {
      if (place <= 0)
        return ""+(int)(d+((d > 0)? 0.5 : -0.5));
      String s = "";
      if (d < 0)
        {
  	s += "-";
  	d = -d;
        }
      d += 0.5*Math.pow(10,-place);
      if (d > 1)
        {
  	int i = (int)d;
  	s += i;
  	d -= i;
        }
      else
        s += "0";
      if (d > 0)
        {
  	d += 1.0;
  	String f = ""+(int)(d*Math.pow(10,place));
  	s += "."+f.substring(1);
        }
      return s;
    }
  }
