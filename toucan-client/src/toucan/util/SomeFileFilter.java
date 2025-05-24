
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
import java.io.File;
import javax.swing.*;
import javax.swing.filechooser.*;

/**
 * Title: SomeFileFilter
 * Description:
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 2.0
 */


public class SomeFileFilter {

public static class FigFileFilter extends FileFilter {

    // Accept all directories and all jpg files
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension="";
	try{
	  extension = toucan.util.Tools.getExtension(f);
        }catch(Exception e){}
	if (extension != null) {
            if (extension.equals("jpg") ||
                extension.equals("png"))   {
                    return true;
            } else {
                return false;
            }
    	}

        return false;
    }

    // The description of this filter
    public String getDescription() {
        return "jpg, png";
    }
}

public static class HtmlFileFilter extends FileFilter {

    // Accept all directories and all html files
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = toucan.util.Tools.getExtension(f);
	if (extension != null) {
            if (extension.equals("html") ||
	       extension.equals("htm"))
	      {
                    return true;
            } else {
                return false;
            }
    	}

        return false;
    }

    // The description of this filter
    public String getDescription() {
        return "HTML files";
    }
}

public static class XmlFileFilter extends FileFilter {

    // Accept all directories and all html files
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = toucan.util.Tools.getExtension(f);
	if (extension != null) {
            if (extension.equals("xml") ||
	       extension.equals("txt"))
	      {
                    return true;
            } else {
                return false;
            }
    	}

        return false;
    }

    // The description of this filter
    public String getDescription() {
        return "XML files";
    }
}

public static class GffFileFilter extends FileFilter {

    // Accept all directories and all html files
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension="";
	try{
	  extension = toucan.util.Tools.getExtension(f);
	}catch(Exception e){}
	if (extension != null) {
            if (extension.equals("gff") ||
	       extension.equals("txt"))
	      {
                    return true;
            } else {
                return false;
            }
    	}

        return false;
    }

    // The description of this filter
    public String getDescription() {
        return "gff";
    }
}

public static class SeqFileFilter extends FileFilter {

    // Accept all directories and all embl files
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension="";
	try{
	  extension = toucan.util.Tools.getExtension(f);
	}catch(Exception e){}
	if (extension != null) {
            if (extension.equals("embl") ||
                extension.equals("tfa") ||
                extension.equals("fasta") ||
                extension.equals("gb") ||
                extension.equals("genbank")||
		extension.equals("out")||
		extension.equals("gff")||
                extension.equals("bin")||
                extension.equals("txt"))   {
                    return true;
            } else {
                return false;
            }
    	}

        return false;
    }

    // The description of this filter
    public String getDescription() {
        return "embl, tfa, fasta, gb, genbank, bin";
    }
}

public static class TxtFileFilter extends FileFilter {

    // Accept all directories and all embl files
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

	String extension="";
	try{
	  extension = toucan.util.Tools.getExtension(f);
	}catch(Exception e){}
	if (extension != null) {
            if (extension.equals("txt") ||
                extension.equals("csv") ||
                extension.equals("bg") ||
		extension.equals("mtrx") ||
                extension.equals("freq") ||
		extension.equals(""))   {
                    return true;
            } else {
                return false;
            }
    	}

        return false;
    }

    // The description of this filter
    public String getDescription() {
        return "Text files";
    }
}

}
