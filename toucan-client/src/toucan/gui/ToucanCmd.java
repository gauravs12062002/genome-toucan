
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
import toucan.util.*;
import java.awt.*;
import javax.swing.*;
import jargs.gnu.CmdLineParser;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Iterator;
import toucan.*;
/**
 * Title: ToucanCmd
 * Description: Start TOUCAN from the command line, and retrieve some sequences automatically
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 2.0
 */


public class ToucanCmd {

  public ToucanCmd() {
  }

  static MainFrame f;

  private static void printUsage() {
    f.writeToStatus("usage: java -jar toucan.jar");
    f.writeToStatus("[{-s,--species} as in the properties fil (e.g., homo_sapiens,mus_musculus)");
    f.writeToStatus("[{-i,--ids} string with pipe(|)-separated gene, sequence, or transcript identifiers ");
    f.writeToStatus("[{-t,--idtype} type of identifier used (e.g., EMBL, HUGO, Ensembl, LocusLink, ...)");
    f.writeToStatus("[{-r,--retrieve} kind of sequence to retrieve [cds,exon1,full]");
    f.writeToStatus("[{-b,--bpbefore} number of base pairs upstream of 'CDS' or 'Exon1' to be retrieved or around the gene in case of 'full'");
    f.writeToStatus("[{-a,--bpafter} number of base pairs after the start of 'CDS' or 'Exon1' (not applicable for 'full')");
    f.writeToStatus("[{-o,--ortholog} species for which the orthologous sequence should be retrieved (e.g., mus_musculus)");
    f.writeToStatus("[{-h,--help} print this message");

  }


  public static void main(String args[])throws Exception{

    CmdLineParser parser = new CmdLineParser();
    CmdLineParser.Option speciesArg = parser.addStringOption('s', "species");
    CmdLineParser.Option idsArg = parser.addStringOption('i', "ids");
    CmdLineParser.Option idtypeArg = parser.addStringOption('t', "idtype");
    CmdLineParser.Option retrieveArg = parser.addStringOption('r', "retrieve");
    CmdLineParser.Option bpbeforeArg = parser.addIntegerOption('b', "bpbefore");
    CmdLineParser.Option bpafterArg = parser.addIntegerOption('a', "bpafter");
    CmdLineParser.Option orthologArg = parser.addStringOption('o', "ortholog");
    CmdLineParser.Option helpArg = parser.addBooleanOption('h', "help");

    try {
      parser.parse(args);
    }
    catch (CmdLineParser.OptionException e) {
      System.err.println(e.getMessage());
      printUsage();
      System.exit(2);
    }

    String species = (String)parser.getOptionValue(speciesArg);
    String idsStr = (String)parser.getOptionValue(idsArg);
    ArrayList ids = new ArrayList();
    if(idsStr != null){
      StringTokenizer tok = new StringTokenizer(idsStr, "|");
      String temp;
      while (tok.hasMoreElements()) {
        temp = (String) tok.nextElement();
        ids.add(temp.trim());
      }
    }
    String idtype = (String)parser.getOptionValue(idtypeArg);
    String retrieve = (String)parser.getOptionValue(retrieveArg);
    Integer bp = (Integer)parser.getOptionValue(bpbeforeArg);
    Integer bpAfter = (Integer)parser.getOptionValue(bpafterArg);
    String ortholog = (String)parser.getOptionValue(orthologArg);
    Boolean help = (Boolean)parser.getOptionValue(helpArg);

    if (help!=null && help.booleanValue()) {
      printUsage();
      System.exit(2);
    }

    if (idtype == null)
      idtype = "Ensembl";

    if (species == null)
      species = "homo_sapiens";

    if (retrieve == null)
      retrieve = "full";

    if (ortholog == null)
      ortholog = "none";

     f = new MainFrame();
     Toolkit k = f.getToolkit();
     Dimension scrSize = k.getScreenSize();
     f.setBounds(scrSize.width/4,scrSize.height/4,scrSize.width/2,scrSize.height/2);
     f.setVisible(true);

     LoadSeq load = new LoadSeq(f);

     if (ids.size()>0){
       load.fullGl = new GeneList();
       int dbid=0;
       String dburl = "jdbc:mysql://"+GlobalProperties.getEnsemblMysql()+"/"+GlobalProperties.getSpecies(species).core+"?user="+GlobalProperties.getEnsemblUser()+"&password="+GlobalProperties.getEnsemblPass();
       String temp;
       for(int i=0;i<GlobalProperties.getSpecies(species).externalNames.size();i++){
         temp = (String)GlobalProperties.getSpecies(species).externalNames.get(i);
         if(temp.equalsIgnoreCase(idtype))
           dbid = ((Integer)GlobalProperties.getSpecies(species).externalIds.get(i)).intValue();
       }

       if(!idtype.equalsIgnoreCase("ensembl")){
         load.fullGl.list.addAll(Tools.glFromExternalList(ids,idtype,dbid,dburl).list);
       }
       else load.fullGl.constructFromEnsemblList(ids);

       load.fullGl.setEnsemblSpecies(GlobalProperties.getSpecies(species));


       LoadSeq.lengthOfTask = load.fullGl.list.size();
       load.progressMonitor = new ProgressMonitor(load,"Retrieving sequences from Ensembl","", 0, LoadSeq.lengthOfTask);
       load.progressMonitor.setProgress(0);
       load.progressMonitor.setMillisToDecideToPopup(20);
       load.timer.start();
       load.homolRetrieve[0] = ortholog;
       if(retrieve.equalsIgnoreCase("full"))
        load.retrieve="complete";
       else if(retrieve.equalsIgnoreCase("CDS"))
        load.retrieve = "5' Upstream CDS";
       else if (retrieve.equalsIgnoreCase("Exon1"))
         load.retrieve = "5' Upstream Exon1";
       f.writeToStatus("bp="+bp.intValue());
       load.go(GlobalProperties.getSpecies(species),
          bp.intValue(),
          bpAfter.intValue());
     }
  }

}
