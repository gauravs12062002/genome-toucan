
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
package toucan;
import java.util.*;
/**
 * Title: EnsemblSpecies
 * Description: Holds information (e.g., for which species ortholog mappings are available) about a species that is available in Ensembl. For each species, an EnsemblSpecies class is built from the information in the properties file.
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 2.0
 */


public class EnsemblSpecies {

  public String name,core,lite,prefix,shortName,longName;
  public int id;
  public Vector homologs;
  public Vector externalNames;
  public Vector externalIds;

  public EnsemblSpecies() {
    name=longName=shortName=core=lite=prefix="";
    homologs = new Vector();
    //homologs.add("none");
    externalNames = new Vector();
    externalNames.add("Ensembl");
    externalIds = new Vector();
    externalIds.add(new Integer(-999));
  }

  public void setHomologs(){

  }

}
