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

import toucan.util.GlobalProperties;
import toucan.gui.MainFrame;
import toucan.*;
import toucan.util.*;
import java.awt.Color;
import java.util.*;
import java.io.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.program.gff.*;
import org.biojava.bio.seq.db.*;
import org.w3c.dom.*;
import java.awt.image.*;
import java.awt.*;
import distributions.BinomialDistribution;
import distributions.ChiSquareDistribution;
import org.apache.tools.ant.filters.*;

/**
 * Title: GeneList
 * Description: A genelist contains an arraylist ("list") of Gene objects.
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 2.0
 */

public class GeneList
    implements Serializable {

  private MainFrame owner;

  public GeneList() {
    owner = null;
  }

  public GeneList(MainFrame mFrame) {
    idSet = new HashSet();
    owner = mFrame;
  }

  //global variables for the image
  public double scale;
  public final int horOffSet = 150;
  public final int horSpaceRight = 50;
  public final int vertPaneOffSet = 29; //empirical
  public final int horPaneOffSet = 6; //empirical
  public final int vertOffSet = 190 + vertPaneOffSet;
  public int geneSpacing = 50;
  public final int defaultScaling = 1500;
  public boolean minScale = false;

  /**
   * The list member is an ArrayList with org.biojava.bio.seq.Sequence objects
   */
  public ArrayList list = new ArrayList();
  public String name;
  public HashSet idSet;
  public int T = 0; //for binomial analysis
  public TreeMap objectMap;
  public HashMap minScoreMap = new HashMap();
  public HashMap maxScoreMap = new HashMap();

  public void add(Gene gene) {
    list.add(gene);
  }
  

  public void remove(Gene gene) {
    writeToStatus("Removing gene=" + gene.name);
    if (gene.parent != null && gene.parent.orthoList.list.contains(gene)) {
      gene.parent.orthoList.list.remove(gene);
      System.out.println("ortholist size=" + gene.parent.orthoList.list.size());
    }
    if (gene.orthoList.list.size() > 0) {
      for (Iterator it = gene.orthoList.list.iterator(); it.hasNext(); ) {
        ( (Gene) it.next()).parent = null;
      }
    }
    list.remove(gene);
  }

  /**
   * Removes the element at the specified position in this list.
   * Shifts any subsequent elements to the left (subtracts one from their indices).
   */
  public void remove(int nr) {
    Gene g = (Gene) list.get(nr);
    this.remove(g);
  }

  public void removeEmpties() {
    Gene g;
    for (int i = 0; i < list.size(); i++) {
      writeToStatus("In removal: " + i + ": " + this.list.size());
      g = (Gene) list.get(i);
      if (g.seq == null || g.seq.length() == 0) {
        list.remove(i);
        writeToStatus("Removed " + g.name);
      }
      else writeToStatus("in else: " + g.name + "|" + g.seq.seqString());
    }
  }

  public GeneList cutInPieces(int windowLength, int overlap, int offset) throws
      Exception {
    Gene g;
    GeneList temp;
    GeneList ret = new GeneList(owner);
      for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      temp = g.cutInPieces(windowLength, overlap, offset);
      for (Iterator it2 = temp.list.iterator(); it2.hasNext(); ) {
        g = (Gene) it2.next();
//        System.out.println("2: "+g.name);
        ret.add(g);
      }
    }
    return ret;
  }

  public void writeDistinctFastaFiles(String dir) throws Exception {
    Gene g;
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      g.writeSeqToFastaFile(dir + g.name + ".tfa");
    }
  }

  public GeneList cutAllInPieces(int nrPiecesPerSeq) throws Exception {
    GeneList cutted = new GeneList(owner);
    Gene g, h;
    int bp = 0;
    int start = 1, end = 0, i = 1;
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      bp = g.length / nrPiecesPerSeq;
      while (start < g.length) {
        h = new Gene();
        if ( (start + bp - 1) > g.length) end = g.length;
        else end = start + bp - 1;
        h.setSequence(g.makeSubSeq(start, start + bp - 1), g.name + "_" + i);
        cutted.add(h);
        start = start + bp;
        i++;
      }
      start = 1;
      i = 1;
    }
    return cutted;
  }

  /**
   * emblPath = original file, with N's!
   */
  public void constructFromFastaGff(String emblPath, String gffPath) throws
      Exception {
    GFFEntrySet gffEntries = new GFFEntrySet();
    GFFParser parser = new GFFParser();
    parser.parse(
        new BufferedReader(
        new InputStreamReader(
        new FileInputStream(
        new File(gffPath)))),
        gffEntries.getAddHandler());
    SimpleGFFRecord rec;
    constructFromLargeEmblFile(emblPath);
    HashMap map = this.makeHashMap();
    Gene g = null;
    int offset = 0;
    String id = "";
    for (Iterator it = gffEntries.lineIterator(); it.hasNext(); ) {
      rec = (SimpleGFFRecord) it.next();
      id = rec.getSeqName().trim();
      if (id.indexOf("@") != -1) {
        offset = new Integer(id.substring(id.indexOf("@") + 1)).intValue();

        rec.setStart(rec.getStart() + offset);
        rec.setEnd(rec.getEnd() + offset);
        rec.setSeqName(id.substring(0, id.indexOf("@")));
      }
      g = (Gene) map.get(rec.getSeqName().trim());
      try {
        //g.annotateStrandedFeature(rec.getStart(),rec.getEnd(),rec.getStrand(),rec.getFeature());
        g.annotateStrandedFeature(rec.getStart(), rec.getEnd(), rec.getStrand(),
                                  rec.getFeature(), rec.getScore(),
                                  (String) rec.getGroupAttributes().get("site"),
                                  rec.getSource());
      }
      catch (Exception e) {
        //writeToStatus(rec.getSeqName()+"|"+rec.getStart()+"|"+rec.getScore()+"|"+rec.getSource()+"|"+rec.getFeature());
        e.printStackTrace();
      }
    }
  }

  public void addGffFromString(String gff) throws Exception {
    GFFEntrySet gffEntries = new GFFEntrySet();
    GFFParser parser = new GFFParser();
    parser.parse(
        new BufferedReader(
        new StringReader(
        gff)),
        gffEntries.getAddHandler());
    this.addGff(gffEntries);
  }

  public void addGffFromFile(String gffPath) throws Exception {
    GFFEntrySet gffEntries = new GFFEntrySet();
    GFFParser parser = new GFFParser();
    parser.parse(
        new BufferedReader(
        new InputStreamReader(
        new FileInputStream(
        new File(gffPath)))),
        gffEntries.getAddHandler());
    this.addGff(gffEntries);
  }

  public void addGff(GFFEntrySet gffEntries) throws Exception {
    SimpleGFFRecord rec;
    //HashMap map = this.makeHashMap();
    Gene g = null;
    int offset = 0;
    String id = "";
    for (Iterator it = gffEntries.lineIterator(); it.hasNext(); ) {
      try {
        rec = (SimpleGFFRecord) it.next();
      }
      catch (ClassCastException ce) {
        //ce.printStackTrace();
        continue;
      }
      id = rec.getSeqName().trim().toUpperCase();
      //footprinter annotation is negative
      if(rec.getStart()<0) rec.setStart(this.get(id).length + rec.getStart());
      if(rec.getEnd()<0) rec.setEnd(this.get(id).length + rec.getEnd());
      //System.out.println(rec.getStart());
      //System.out.println(rec.getEnd());
      if (id.indexOf("@") != -1) {
        String subid = id.substring(id.indexOf("@") + 1);
        if (subid.indexOf("@") != -1) {
          subid = subid.substring(0, subid.indexOf("@"));
        }
        try {
          offset = new Integer(subid).intValue();
        }
        catch (Exception exc) {
          throw new Exception("Problems with offset parsing ( '@' )");
        }
        rec.setStart(rec.getStart() + offset);
        rec.setEnd(rec.getEnd() + offset);
        rec.setSeqName(id.substring(0, id.indexOf("@")));
      }
      g = (Gene) get(rec.getSeqName().trim());
      //System.out.println("gene id='"+rec.getSeqName().trim()+"'");
      //System.out.println(g);
      if (g == null)
        g = (Gene) get(rec.getSeqName().trim().toUpperCase());
      if (g == null)
        g = (Gene) get(rec.getSeqName().trim().toLowerCase());
      if (g == null)
        g = (Gene) get(rec.getSeqName().replace('_','%'));
      if (g == null)
        System.out.println("Gene not found!\t'"+rec.getSeqName()+"'");
      String motifId = "", site = "";
      try {
        //writeToStatus(rec.getComment());
        //g.annotateStrandedFeature(rec.getStart(),rec.getEnd(),rec.getStrand(),rec.getFeature());
        try {
          site = (String) ( (ArrayList) rec.getGroupAttributes().get("site")).
              get(0);
        }
        catch (NullPointerException ne) {//there is no site attribute, that's OK
        }
        ;
        if (rec.getFeature().equalsIgnoreCase("binding_site") ||
            rec.getFeature().equalsIgnoreCase("misc_feature")) {
          //writeToStatus(rec.getGroupAttributes());
          //System.out.println(rec.getGroupAttributes());
          try {
            motifId = (String) ( (ArrayList) rec.getGroupAttributes().get("id")).
                get(0);

              //System.out.println(rec.getStart()+"|"+rec.getEnd()+"|"+rec.getStrand()+"|"+motifId+"|"+rec.getScore()+"|"+
              //                          site+"|"+rec.getSource());
              g.annotateStrandedFeature(rec.getStart(), rec.getEnd(),
                                        rec.getStrand(), motifId, rec.getScore(),
                                        site, rec.getSource());

          }
          catch (NullPointerException ne) {
            try {
              motifId = (String) ( (ArrayList) rec.getGroupAttributes().get(
                  "type")).get(0);
              g.annotateStrandedFeature(rec.getStart(), rec.getEnd(),
                                        rec.getStrand(), motifId, rec.getScore(),
                                        site, rec.getSource());
            }
            catch (NullPointerException ne2) {
              if (g != null) g.annotateStrandedFeature(rec.getStart(),
                  rec.getEnd(), rec.getStrand(), rec.getSource(), rec.getScore(),
                  site, rec.getSource());
            }
          }

        }
        else

          //here the feature gets annotated with "misc_feature, \type=rec.getFeature()
          g.annotateStrandedFeature(rec.getStart(), rec.getEnd(), rec.getStrand(),
                                    rec.getFeature(), rec.getScore(), site,
                                    rec.getSource());
      }
      catch (Exception e) {
        //writeToStatus(rec.getSeqName()+"|"+rec.getStart()+"|"+rec.getScore()+"|"+rec.getSource()+"|"+rec.getFeature());
        e.printStackTrace();
      }
    }
    updateObjectMap(false);
  }
  
  public GeneList generateRandomSubList(int size){
  	GeneList toRet=new GeneList();
  	for (int i=0;i<size;i++){
  		int index = (int)Math.floor(Math.random() * this.list.size());
  		while(toRet.list.contains(this.list.get(index))){
  			index = (int)Math.floor(Math.random() * this.list.size());
  		}
  		toRet.add((Gene)this.list.get(i));
  	}	
  	return toRet;
  }
  
  public Gene getRandomGene(){
  	GeneList toRet=new GeneList();
  	int index = (int)Math.floor(Math.random() * this.list.size());
  	return (Gene) this.list.get(index);
  }

  public HashSequenceDB makeSeqDb() throws Exception {
    Gene g;
    HashSequenceDB seqDB = new HashSequenceDB(IDMaker.byName);
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      g = (Gene) i.next();
      seqDB.addSequence(g.seq);
    }
    return seqDB;
  }

  public HashMap makeHashMap() {
    HashMap ret = new HashMap();
    Gene g;
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      g = (Gene) i.next();
      ret.put(g.name, g);
      System.out.println("gname='"+g.name+"'");
    }
    return ret;
  }

  public void checkAmbiguousFeatures(String featStr, String annKey,
                                     String annValue) {
    Gene g;
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      g.checkAmbiguousFeatures(featStr, annKey, annValue);
    }
  }

  public void constructFromIdFile(String path, String idType) throws Exception {
    BufferedReader in = new BufferedReader(new FileReader(path));
    String line = in.readLine();
    Gene g;
    while (line != null) {
      g = new Gene();
      if (!line.equalsIgnoreCase("")) {
        if (idType.startsWith("Ensembl")) g.ensembl = line;
        else if (idType.equalsIgnoreCase("HUGO")) g.hugo = line;
        else if (idType.startsWith("Acc") || idType.equalsIgnoreCase("accNr"))
          g.accNr = line;
        else if (idType.startsWith("Locus")) g.locusLink = line;
        //if (idType.equalsIgnoreCase("GO")) g = line;
        else throw new Exception("no matching idType");
        add(g);
      }
      line = in.readLine();
    }
    in.close();
  }

  public void constructFromEnsemblList(ArrayList in) {
    Gene g;
    for (Iterator it = in.iterator(); it.hasNext(); ) {
      g = new Gene();
      g.ensembl = (String) it.next();
      add(g);
    }
  }

  public void constructFromExternalDbFile(String path, String externalDb,
                                          int externalDbId) throws Exception {
    BufferedReader in = new BufferedReader(new FileReader(path));
    String line = in.readLine();
    Gene g;
    while (line != null) {
      g = new Gene();
      if (!line.equalsIgnoreCase("")) {
        g.externalDbEntry = line.trim();
        g.externalDb = externalDb;
        g.externalDbId = externalDbId;
        add(g);
      }
      line = in.readLine();
    }
    in.close();
  }

  public void constructFromExternalDbString(String idString, String externalDb,
                                            int externalDbId) throws Exception {
    StringTokenizer tok = new StringTokenizer(idString, ",");
    Gene g;
    String id;
    while (tok.hasMoreElements()) {
      g = new Gene();
      id = (String) tok.nextElement();
      if (!id.equalsIgnoreCase("")) {
        g.externalDbEntry = id.trim();
        g.externalDb = externalDb;
        g.externalDbId = externalDbId;
        add(g);
      }
    }
  }

  /**
   * @params  idString: comma separated list of id's
   */
  public void constructFromIdString(String idString, String idType) throws
      Exception {
    StringTokenizer tok = new StringTokenizer(idString, ",");
    Gene g;
    String id;
    while (tok.hasMoreElements()) {
      g = new Gene();
      id = (String) tok.nextElement();
      if (!id.equalsIgnoreCase("")) {
        if (idType.startsWith("Ensembl")) g.ensembl = id;
        else if (idType.equalsIgnoreCase("HUGO")) g.hugo = id;
        else if (idType.startsWith("Acc") || idType.equalsIgnoreCase("accNr"))
          g.accNr = id;
        else if (idType.startsWith("Locus")) g.locusLink = id;
        else throw new Exception("no matching idType");
        add(g);
      }
    }
  }

  public void updateInfoProps(String mysql, String userName, String passwd) throws
      Exception {
    Gene g;
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      try {
        g.updateInfoProps(mysql, userName, passwd);
      }
      catch (Exception e) {
        //e.printStackTrace();
        writeToStatus(g.ensembl + ": " + e.getMessage());
      }
    }
  }

  public void setEnsemblSpecies(EnsemblSpecies spec) {
    Gene g;
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      try {
        g.setEnsemblSpecies(spec);
      }
      catch (Exception e) {
        writeToStatus(g.name + ": " + e.getMessage());
      }
    }
  }

  //order of id's : accNr,unigene,locuslink,name,swissprot,ensembl
  public void constructFromCsv(String path) throws Exception {
    BufferedReader in = new BufferedReader(new FileReader(path));
    StringTokenizer tok;
    String line = in.readLine();
    Gene g;
    while (line != null) {
      g = new Gene();
      if (!line.equalsIgnoreCase("")) {
        tok = new StringTokenizer(line, ",");
        while (tok.hasMoreElements()) {
          //g.accNr = (String)tok.nextElement();
          g.uniGene = (String) tok.nextElement();
          g.locusLink = (String) tok.nextElement();
          g.name = (String) tok.nextElement();
          g.swissProt = (String) tok.nextElement();
          g.ensembl = (String) tok.nextElement();
        }
        add(g);
      }
      line = in.readLine();
    }
    in.close();
  }

  public void constructFromNameFile(String path) throws Exception {
    BufferedReader in = new BufferedReader(new FileReader(path));
    String line = in.readLine();
    Gene g;
    while (line != null) {
      g = new Gene();
      if (!line.equalsIgnoreCase("")) g.name = line;
      add(g);
      line = in.readLine();
    }
    in.close();
  }

  public void setSeqFromEmbl() throws Exception {
    Gene g = null;
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      try {
        g.setSeqFromEmbl();
      }
      catch (Exception e) {
        e.printStackTrace();
        writeToStatus("No EMBL entry for " + g.name);
      }
    }
  }

  public void setTranscriptDiff(String tblName) throws Exception {
    Gene g = null;
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      g.setTranscriptDiff(tblName);
    }
  }
  
  public void normalizeScores(String source)throws Exception{
	  Gene g;
	  double max=0.0d;
      double min=100000000;
      double temp,norm;  
      Feature f;
      for (Iterator it = list.iterator(); it.hasNext(); ) {
	      g = (Gene) it.next();
	       for (Iterator it2 = g.seq.features(); it2.hasNext(); ) {
	        f = (Feature) it2.next();
	        if (source.equalsIgnoreCase(f.getSource()) || (f.getAnnotation().containsProperty("source") && source.equalsIgnoreCase((String)f.getAnnotation().getProperty("source")))) {
	        		temp = Double.parseDouble((String)f.getAnnotation().getProperty("score"));
	        		if(temp>max) max=temp;
	        		if(temp<min) min=temp;
	        }
	     }
	    }
	  this.minScoreMap.put(source,new Double(min));
	  this.maxScoreMap.put(source,new Double(max));
	  
	  for (Iterator it = list.iterator(); it.hasNext(); ) {
	      g = (Gene) it.next();
	       for (Iterator it2 = g.seq.features(); it2.hasNext(); ) {
	        f = (Feature) it2.next();
	        if (source.equalsIgnoreCase(f.getSource()) || (f.getAnnotation().containsProperty("source") && source.equalsIgnoreCase((String)f.getAnnotation().getProperty("source")))) {
	        		temp = Double.parseDouble((String)f.getAnnotation().getProperty("score"));
	        		norm = (temp-min)/(max-min);
	        		f.getAnnotation().setProperty("normscore",String.valueOf(norm));
	        		//System.out.println("normalized score for "+f.getSequence().getName()+"|"+f.getType()+" is "+norm);
	        }
	     }
	    }
	  
	  
  }

  public String toGFF(Object[] sources) {
    Gene g;
    StringBuffer buf = new StringBuffer();
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      buf.append(g.toGFF(sources));
    }
    return buf.toString();
  }

  public GeneList getSubSequences(String featureSource, String partType,
                                  int bpBefore, int bpAfter,boolean revComplIfMinusStrand) throws Exception {
    Gene g;
    GeneList ret = new GeneList(owner);
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      for (Iterator it2 = g.getSubSequences(featureSource, partType, bpBefore,
                                            bpAfter,revComplIfMinusStrand).list.iterator();
           it2.hasNext(); ) {
        ret.add( (Gene) it2.next());
      }
    }
    return ret;
  }

  public GeneList getSubSequences(String featStr, String annKey,
                                  String annValue, String partType,
                                  int bpBefore, int bpAfter,boolean revComplIfMinusStrand) throws Exception {
    Gene g;
    GeneList ret = new GeneList(owner);
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      for (Iterator it2 = g.getSubSequences(featStr, annKey, annValue, partType,
                                            bpBefore, bpAfter,revComplIfMinusStrand).list.iterator();
           it2.hasNext(); ) {
        ret.add( (Gene) it2.next());
      }
    }
    return ret;
  }
  
  public void maskSubSequences(String featStr, String annKey,
        String annValue, String partType,
        int bpBefore, int bpAfter) throws Exception {
Gene g;
GeneList ret = new GeneList(owner);
for (Iterator it = list.iterator(); it.hasNext(); ) {
	g = (Gene) it.next();
	g.maskSubSequences(featStr, annKey, annValue, partType,
	                  bpBefore, bpAfter);
}
}

  public void writeGFF(String path) throws Exception {
    FileOutputStream fout = new FileOutputStream(path);
    GFFWriter writer = new GFFWriter(
        new PrintWriter(new OutputStreamWriter(fout)));
    SequencesAsGFF seqsAsGFF = new SequencesAsGFF();
    Gene g;
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      seqsAsGFF.processSequence(g.seq, writer);
    }
    fout.close();
  }

  public void reverseComplement() throws Exception {
    Gene g;
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      if (!g.cdsPositive) g.reverseComplement();
    }
  }

  public void constructFromXmlFile(String path) throws Exception {
    writeToStatus("Building genelist from XML file");
    Document xml = Tools.createXmlDocFromFile(path);
    NodeList list = xml.getElementsByTagName("gene");
    Gene hg;
    Node g;
    for (int i = 0; i < list.getLength(); i++) {
      hg = new Gene();
      g = list.item(i);
      NodeList items = g.getChildNodes();
      for (int j = 0; j < items.getLength(); j++) {
        //writeToStatus(items.item(j).getChildNodes().item(0).getNodeName());
        //writeToStatus(items.item(j).getNodeName());
        if ( (items.item(j).getNodeName()).equalsIgnoreCase("id")) {
          if ( (items.item(j).getAttributes().item(0).getNodeValue()).
              equalsIgnoreCase("accNr")) {
            if (items.item(j).getChildNodes().item(0) != null) hg.cloneAcc =
                items.item(j).getChildNodes().item(0).getNodeValue();
            writeToStatus(hg.cloneAcc + " | ");
          }
          if ( (items.item(j).getAttributes().item(0).getNodeValue()).
              equalsIgnoreCase("ensembl")) {
            if (items.item(j).getChildNodes().item(0) != null) hg.ensembl =
                items.item(j).getChildNodes().item(0).getNodeValue();
            writeToStatus(hg.ensembl + " | ");
          }
          if ( (items.item(j).getAttributes().item(0).getNodeValue()).
              equalsIgnoreCase("locuslink")) {
            if (items.item(j).getChildNodes().item(0) != null) hg.locusLink =
                items.item(j).getChildNodes().item(0).getNodeValue();
            writeToStatus(hg.locusLink + " | ");
          }
        }
        if ( (items.item(j).getNodeName()).equalsIgnoreCase("name")) {
          if (items.item(j).getChildNodes().item(0) != null) hg.name = items.
              item(j).getChildNodes().item(0).getNodeValue();
          writeToStatus(hg.name + " | ");
        }
        if ( (items.item(j).getNodeName()).equalsIgnoreCase("descr")) {
          if (items.item(j).getChildNodes().item(0) != null) hg.desc = items.
              item(j).getChildNodes().item(0).getNodeValue();
          writeToStatus(hg.desc + " | ");
        }
      }
      if (!idSet.contains(hg.name)) {
        this.add(hg);
        idSet.add(hg.name);
      }
      writeToStatus(".");
    }
    writeToStatus("\n");
  }

  public GeneList filterOnFeatNr(String featStr, String annKey, String annValue,
                                 int min) {
    Gene g;
    GeneList newList = new GeneList(owner);
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      if (g.nrFeatures(featStr, annKey, annValue) >= min) newList.add(g);
    }
    return newList;
  }

  public GeneList filterEnsembl() {
    GeneList ret = new GeneList(owner);
    HashSet ens = new HashSet();
    Gene g;
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      if (g.ensembl != null && !g.ensembl.equalsIgnoreCase("")) {
        if (!ens.contains(g.ensembl)) {
          ens.add(g.ensembl);
          ret.add(g);
        }
      }
    }
    return ret;
  }

  public GeneList filterNoEnsembl() {
    GeneList ret = new GeneList(owner);
    HashSet ens = new HashSet();
    Gene g;
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      if (g.ensembl == null || g.ensembl.equalsIgnoreCase("")) {
        if (!ens.contains(g.cloneAcc)) {
          ens.add(g.cloneAcc);
          ret.add(g);
        }
      }
    }
    return ret;
  }

  private String makeCompleteXml() {
    StringBuffer buf = new StringBuffer();
    buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    buf.append("\n");
    //buf.append("<!DOCTYPE AlignmentPlot SYSTEM \"AlignmentPlot.dtd\">");
    buf.append("\n");
    buf.append("<AlignmentPlot plotID=\"test.xml\">");
    buf.append("\n");
    TreeMap objMap = this.getObjects();
    for (Iterator i = objMap.keySet().iterator(); i.hasNext(); ) {
      Factor myObj = (Factor) objMap.get(i.next());
      buf.append("<Factor objectID=\"" + myObj.objectID + "\">");
      buf.append("<objectType>" + myObj.objectType + "</objectType>");
      buf.append("<objectDescriptor>" + myObj.objectDescriptor +
                 "</objectDescriptor>");
      buf.append("<objectColor>" + myObj.objectColor.getRGB() +
                 "</objectColor>");
      buf.append("<objectFill>" + myObj.objectFill + "</objectFill>");
      buf.append("</Factor>");
      buf.append("\n");
    }
    int cnt = 0;
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      Gene hg = (Gene) i.next();
      ArrayList gObjList = hg.getFactorInstances(objMap);
      buf.append(hg.makeSeqXmlString(gObjList, "seq" + (cnt++)));
    }
    buf.append("</AlignmentPlot>");
    buf.append("\n");

    return buf.toString();
  }

  private String makeCompleteXml(TreeMap objMap) {
    StringBuffer buf = new StringBuffer();
    buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    buf.append("\n");
    //buf.append("<!DOCTYPE AlignmentPlot SYSTEM \"AlignmentPlot.dtd\">");
    buf.append("\n");
    buf.append("<AlignmentPlot plotID=\"test.xml\">");
    buf.append("\n");
    for (Iterator i = objMap.keySet().iterator(); i.hasNext(); ) {
      Factor myObj = (Factor) objMap.get(i.next());
      buf.append("<Factor objectID=\"" + myObj.objectID + "\">");
      buf.append("<objectType>" + myObj.objectType + "</objectType>");
      buf.append("<objectDescriptor>" + myObj.objectDescriptor +
                 "</objectDescriptor>");
      if (myObj.objectColor != null)
        buf.append("<objectColor>" + myObj.objectColor.getRGB() +
                   "</objectColor>");
      else
        buf.append("<objectColor>" + myObj.randomColor() + "</objectColor>");
      buf.append("<objectFill>" + myObj.objectFill + "</objectFill>");
      buf.append("</Factor>");
      buf.append("\n");
    }
    int cnt = 0;
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      Gene hg = (Gene) i.next();
      ArrayList gObjList = hg.getFactorInstances(objMap);
      buf.append(hg.makeSeqXmlString(gObjList, "seq" + (cnt++)));
    }
    buf.append("</AlignmentPlot>");
    buf.append("\n");

    return buf.toString();
  }

  /**
   * objMap = null if all objects are to be used
   */
  public ArrayList getObjInstList(TreeMap objMap) {
    if (objMap == null) objMap = this.getObjects();
    ArrayList instList = new ArrayList();
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      Gene hg = (Gene) i.next();
      instList.add(hg.getFactorInstances(objMap));
    }
    return instList;
  }

  public int getMaxLength() {
    int max = 0;
    Gene g;
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      if (g.length > max) max = g.length;
    }
    return max;
  }

  public int getMaxSeqLength() {
    int max = 0;
    Gene g;
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      if (g.length > max) max = g.length;
    }
    return max;
  }

  public BufferedImage createImg(String title, double scalingFactor,
                                 TreeMap objMap, boolean legendYesNo) {
    if (list.size() == 0) return null;
    if (objMap == null) objMap = this.getObjects();
    ArrayList instList = null;
    Gene hg = null;
    int nrSeq = list.size();
    int nrObj = objMap.size();
    //calculate proper scale
    int max = getMaxLength();
    scalingFactor = defaultScaling * scalingFactor;
    scale = (double) max / (double) scalingFactor;
    if (scale < 0.25d) {
      scale = 0.1d;
      minScale = true;
    }
    else if (scale < 0.5d && scale >= 0.25) {
      scale = 0.25d;
      minScale = false;
    }
    else if (scale < 1.0d && scale >= 0.5d) {
      scale = 0.5d;
      minScale = false;
    }
    else { //>=1
      scale = new Double(scale).intValue();
      minScale = false;
    }
    int maxLength = new Double(max / scale).intValue();
    //writeToStatus("scalingfactor="+scalingFactor+"  scale="+scale+ "max= "+max);
    //Objects

    HashMap legendMap = new HashMap();
    HashMap fillMap = new HashMap();
    HashMap showMap = new HashMap();
    Factor o = null;
    FactorInstance oi = null;
    Color c = null;
    for (Iterator it = objMap.keySet().iterator(); it.hasNext(); ) {
      o = (Factor) objMap.get(it.next());
      legendMap.put(o.objectID, o.objectColor);
      fillMap.put(o.objectID, new Boolean(o.objectFill));
      showMap.put(o.objectID, new Boolean(o.objectShow));
    }

    //Sequences
    int xBase = horOffSet;
    int imgWidth = maxLength + horOffSet + horSpaceRight;
    int imgHeight;
    if (legendYesNo) imgHeight = (nrSeq * 100) + 400;
    else imgHeight = nrSeq * 50 + 200;
    int linePos = 0;
    int xpos = xBase, ypos = linePos;

    java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(
        imgWidth, imgHeight, java.awt.image.BufferedImage.TYPE_INT_RGB);
    java.awt.Graphics2D g = img.createGraphics();
    g.setColor(Color.white);
    g.fillRect(2, 2, imgWidth - 4, imgHeight - 4);
    //g.setColor(Color.white);
    g.setColor(Color.black);
    //title
    g.setFont(new Font("Helvetica", Font.BOLD, 18));
    g.drawString(title, (imgWidth / 2) - 50, 35);
    linePos = linePos + 100;

    //rulers
    g.setFont(new Font("Helvetica", Font.PLAIN, 12));
    xpos = xBase;
    ypos = linePos;
    g.drawLine(xpos, ypos, xpos + (maxLength), ypos);
    ypos = ypos - 5;
    int rulePos = 0;
    while (rulePos < max) {
      g.drawLine(xpos, ypos, xpos, ypos + 10);
      g.drawString("" + (rulePos), xpos - 10, ypos - 10);
      xpos = xpos + 100;
      rulePos = rulePos + new Double(100 * scale).intValue();
    }
    linePos = linePos + 40;
    //2nd ruler: minus
    xpos = xBase;
    ypos = linePos;
    g.drawLine(xpos, ypos, xpos + (maxLength), ypos);
    ypos = ypos - 5;
    xpos = xBase + maxLength;
    rulePos = 0;
    while (rulePos < max) {
      g.drawLine(xpos, ypos, xpos, ypos + 10);
      g.drawString("-" + (rulePos), xpos - 10, ypos - 10);
      xpos = xpos - 100;
      rulePos = rulePos + new Double(100 * scale).intValue();
    }

    int baseHeight = 10;
    int height = baseHeight;
    int width = 0;
    String seqName = "";
    for (int k = 0; k < nrSeq; k++) {
      hg = (Gene) list.get(k);
      xpos = xBase;
      //g.setColor(Color.white);
      g.setColor(Color.black);
      linePos = linePos + geneSpacing;
      ypos = linePos;
      //seqName + line
      if (!hg.externalDbEntry.equalsIgnoreCase("")) seqName = hg.externalDbEntry;
      //else if (!hg.externalDbEntry.equalsIgnoreCase("")) seqName = hg.
      //    externalDbEntry;
      else if (!hg.ensembl.equalsIgnoreCase("")) seqName = hg.ensembl;
      else seqName = hg.name;
      //if (seqName != null && seqName.startsWith("ENSG") &&
      //    seqName.length() > 13) seqName = seqName.substring(9);
      //if (seqName != null && seqName.startsWith("ENSMUSG") &&
      //    seqName.length() > 16) seqName = seqName.substring(12);
      //if (seqName != null && seqName.startsWith("ENSRNOG") &&
      //    seqName.length() > 16) seqName = seqName.substring(12);
      //species
      //if (hg.species != null && !hg.species.name.equalsIgnoreCase("")) seqName =
      //    seqName + " (" + hg.species.name.substring(0, 1) + ")";
      g.setFont(new Font("Helvetica", Font.PLAIN, 10));
      if (seqName != null) g.drawString(seqName, xBase - horOffSet + 10, ypos);
      g.setFont(new Font("Helvetica", Font.PLAIN, 14));
      g.drawLine(xpos, ypos, xpos + new Double(hg.length / scale).intValue(),
                 ypos);
      int strand = 0, start = 0, stop = 0;
      double normScore=1.0d;
      String type;
      boolean fill = true, show=true;
      instList = hg.getFactorInstances(objMap);
      for (Iterator it = instList.iterator(); it.hasNext(); ) {
        oi = (FactorInstance) it.next();
        ypos = linePos;
        normScore=oi.instanceNormScore;
       // height = baseHeight;
        //first try for adjusting height
        height = baseHeight + new Double(baseHeight*normScore).intValue();
        strand = oi.instanceStrand;
        if (strand == 1) ypos = ypos - height;
        else if (strand == -1);
        else if (strand == 0) {
          ypos = ypos - 10;
          height = height + 10;
        }
        start = oi.instanceStart - 1;
        if (start == -1) {
          start = 0;
        }

        stop = oi.instanceStop;
        if (start < 0 || stop > hg.length)continue;
        type = oi.instanceType;
        c = (Color) legendMap.get(type);
        g.setColor(c);
        fill = ( (Boolean) fillMap.get(type)).booleanValue();
        show = ( (Boolean) showMap.get(type)).booleanValue();
        xpos = xBase + new Double(start / scale).intValue();
        width = new Double( (stop - start) / scale).intValue();
        if(show){
          if (fill) g.fillRect(xpos, ypos, width, height);
          else g.drawRect(xpos, ypos, width, height);
        }
        g.setStroke(new BasicStroke(1.0f));
      }
    }
    //legend
    if (legendYesNo) {
      g.setFont(new Font("Helvetica", Font.PLAIN, 16));
      ypos = ypos + 30;
      for (Iterator it = objMap.keySet().iterator(); it.hasNext(); ) {
        ypos = ypos + 15;
        xpos = xBase;
        o = (Factor) objMap.get(it.next());
        c = (Color) legendMap.get(o.objectID);
        g.setColor(c);
        g.fillRect(xpos, ypos, 10, 10);
        g.drawString(o.objectDescriptor, xpos + 15, ypos + 8);
      }
    }
    return img;
  }

  /*public BufferedImage createScoreImg(String title, double scalingFactor, TreeMap objMap,boolean legendYesNo){
    if (list.size()==0) return null;
    if (objMap==null) objMap = this.getObjects();
    ArrayList instList = null;
    Gene hg = null;
    int nrSeq = list.size();
    int nrObj = objMap.size();
    //calculate proper scale
    int max = getMaxLength();
    scalingFactor = defaultScaling*scalingFactor;
    //scale = max/1500;
    scale = (new Double(max/scalingFactor)).intValue();
    if (scale==0) scale = 1;
    int maxLength = max/scale;
    //writeToStatus("scale="+scale+ "max= "+max);
    //Objects

    HashMap legendMap = new HashMap();
    HashMap fillMap = new HashMap();
    Factor o = null;
    FactorInstance oi = null;
    Color c=null;
    boolean[] fillArr = new boolean[nrObj];
    for (Iterator it = objMap.keySet().iterator();it.hasNext();){
        o = (Factor)objMap.get(it.next());
        legendMap.put(o.objectID,o.objectColor);
        fillMap.put(o.objectID,new Boolean(o.objectFill));
    }

    //Sequences
    int xBase = horOffSet;
    int imgWidth= maxLength + xBase*2;
    int imgHeight;
    if(legendYesNo) imgHeight=(nrSeq*300)+400;
    else imgHeight = nrSeq*200 + 200;
    int linePos = 0;
    int xpos=xBase,ypos=linePos;
    java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(imgWidth,imgHeight,java.awt.image.BufferedImage.TYPE_INT_RGB);
    java.awt.Graphics2D g = img.createGraphics();
    g.setColor(Color.white);
    g.fillRect(2,2,imgWidth-4,imgHeight-4);
    //g.setColor(Color.white);
    g.setColor(Color.black);
    //title
    g.setFont(new Font("Helvetica",Font.BOLD,18));
    g.drawString(title,(imgWidth/2)-50,35);
    linePos = linePos+100;
    int barHeight=0;
    //rulers
    g.setFont(new Font("Helvetica",Font.PLAIN,12));
    xpos = xBase;
    ypos=linePos;
    g.drawLine(xpos,ypos,xpos+(maxLength),ypos);
    ypos=ypos-5;
    int rulePos = 0;
    while (rulePos<max){
        g.drawLine(xpos,ypos,xpos,ypos+10);
        g.drawString(""+(rulePos),xpos-10,ypos-10);
        xpos=xpos+100;
        rulePos = rulePos + (100*scale);
    }
    linePos = linePos+40;
    //2nd ruler: minus
    xpos = xBase;
    ypos=linePos;
    g.drawLine(xpos,ypos,xpos+(maxLength),ypos);
    ypos=ypos-5;
    xpos = xBase+maxLength;
    rulePos = 0;
    while (rulePos<max){
        g.drawLine(xpos,ypos,xpos,ypos+10);
        g.drawString("-"+(rulePos),xpos-10,ypos-10);
        xpos=xpos-100;
        rulePos = rulePos + (100*scale);
    }

    int featStrand = 0;
    int baseHeight = 10;
    int height = baseHeight;
    int width = 0;
    String seqName = "";
    for (int k=0;k<nrSeq;k++){
        hg = (Gene) list.get(k);
        xpos = xBase;
        //g.setColor(Color.white);
        g.setColor(Color.black);
        linePos=linePos+geneSpacing;
        ypos = linePos;
        g.setFont(new Font("Helvetica",Font.PLAIN,14));
        //seqName + line
        if (!hg.hugo.equalsIgnoreCase("")) seqName = hg.hugo;
   else if (!hg.externalDbEntry.equalsIgnoreCase("")) seqName=hg.externalDbEntry;
        else seqName = hg.name;
        if (seqName!=null && seqName.startsWith("ENSG") && seqName.length()>13) seqName = seqName.substring(9);
        if (seqName!=null && seqName.startsWith("ENSMUSG") && seqName.length()>16) seqName = seqName.substring(12);
        if (seqName!=null && seqName.startsWith("ENSRNOG") && seqName.length()>16) seqName = seqName.substring(12);

        //species
        if(hg.species!=null && !hg.species.name.equalsIgnoreCase("")) seqName = seqName+" ("+hg.species.name.substring(0,1)+")";
        if (seqName!=null) g.drawString(seqName,xBase-80,ypos);
        g.drawLine(xpos,ypos,xpos+(hg.length/scale),ypos);
        int cnt=0;
        int strand=0,start=0,stop=0,score=0;
        String type;
        boolean fill = true;
        instList = hg.getFactorInstances(objMap);
        for (Iterator it = instList.iterator();it.hasNext();){
            oi = (FactorInstance) it.next();
            ypos = linePos;
            height = baseHeight;
            strand = oi.instanceStrand;
            if (strand==1) ypos=ypos-10;
            else if (strand==-1);
            else if (strand==0) {ypos=ypos-10; height=height+10;}
            start = oi.instanceStart;
            stop = oi.instanceStop;
            if(start<0 || stop>hg.length) continue;
            score = oi.instanceScore;
            type = oi.instanceType;
            c = (Color)legendMap.get(type);
            g.setColor(c);
            fill = ((Boolean)fillMap.get(type)).booleanValue();
            xpos = xBase + (start)/scale;
            width = (stop - start)/scale;
            if (fill) g.fillRect(xpos,ypos,width,height);
            else g.drawRect(xpos,ypos,width,height);
            g.setStroke(new BasicStroke(1.0f));
        }
        if(hg.scoreArr!=null){
          linePos=linePos+2*geneSpacing;
          xpos = xBase;
          ypos = linePos;
          g.drawLine(xpos,ypos,xpos+(hg.length/scale),ypos);
          //guides
          g.setColor(Color.lightGray);
          for (int l=1;l<6;l++){
            g.drawLine(xpos, ypos-(l*(geneSpacing/5)), xpos + (hg.length / scale), ypos-(l*(geneSpacing/5)));
          }
          g.setColor(Color.red);
          for (int i=0;i<hg.length;i++){
            barHeight = (int)(hg.scoreArr[i]*geneSpacing);
            g.drawLine(xpos,ypos,xpos,ypos-barHeight);
            xpos = xBase + i/scale;
          }
        }
      }
      //legend
      if(legendYesNo){
        g.setFont(new Font("Helvetica",Font.PLAIN,16));
        ypos=ypos+30;
        for (Iterator it = objMap.keySet().iterator();it.hasNext();){
            ypos=ypos+15;
            xpos = xBase;
            o = (Factor)objMap.get(it.next());
            c = (Color)legendMap.get(o.objectID);
            g.setColor(c);
            g.fillRect(xpos,ypos,10,10);
            g.drawString(o.objectDescriptor,xpos+15,ypos+8);
        }
      }
      return img;
     }

   */

  public Document getXmlDoc() throws Exception {
    String s = makeCompleteXml();
    Document xmlDoc = toucan.util.XMLParser.parseString(s);
    return xmlDoc;
  }

  public Document getXmlDoc(TreeMap objMap) throws Exception {
    String s = makeCompleteXml(objMap);
    Document xmlDoc = toucan.util.XMLParser.parseString(s);
    return xmlDoc;
  }

  /**
   * Get the intersection of the objects of all sequences in the list
   */
  public TreeMap getObjects() {
    if (objectMap == null) updateObjectMap(false);
    //updateObjectMap();
    return objectMap;
  }

  /**
   * Get the intersection of the objects of all sequences in the list
   */
  public void updateObjectMap(boolean remake) {
    //System.out.println("updating object map");
    if (objectMap == null || remake) {
      objectMap = new TreeMap();
    }
    Gene hg = null;
    //update objMap of each gene
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      hg = (Gene) it.next();
      hg.updateObjectMap();
    }
    int cnt = 0;
    //list of colors to be used for CDS,gene,mRNA, etc.
    ArrayList trueColorList = new ArrayList();
    trueColorList.add(Color.blue);
    trueColorList.add(Color.red);
    trueColorList.add(Color.green);
    trueColorList.add(Color.gray);
    trueColorList.add(Color.magenta);
    trueColorList.add(Color.cyan);
    trueColorList.add(Color.yellow);
    trueColorList.add(new Color(100, 100, 0));
    trueColorList.add(new Color(50, 0, 50));
    trueColorList.add(new Color(0, 150, 150));
    int nextColor = 0;
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      hg = (Gene) i.next();
      TreeMap gObjMap = hg.getObjects();
      for (Iterator j = gObjMap.keySet().iterator(); j.hasNext(); ) {
        Object key = j.next();
        if (!objectMap.containsKey(key)) {
          Factor o = (Factor) gObjMap.get(key);
          //o.objectID = (new Integer(cnt++)).toString();
          o.objectID = "" + objectMap.size();
          //for CDS, gene, exon, etc:
          if (!o.objectType.equalsIgnoreCase("misc_feature")) {
            o.objectFill = false;
            if (nextColor < trueColorList.size())
              o.setColor( (Color) trueColorList.get(nextColor++));
          }
          //frequency of each object within whole set
          if (!o.objectType.equalsIgnoreCase("misc_feature")) {
            o.nrOccInSet = nrOfFeatures(o.objectDescriptor, null, null);
          }
          else {
            //writeToStatus(o.objectDescriptor+"|"+o.objectType);
            if (o.objectDescriptor.startsWith("syntenic") ||
                o.objectDescriptor.startsWith("ensg") ||
                o.objectDescriptor.startsWith("ensmusg") ||
                o.objectDescriptor.indexOf("avid") != -1 ||
                o.objectDescriptor.indexOf("blastz") != -1 ||
                o.objectDescriptor.indexOf("lagan") != -1 ||
                o.objectDescriptor.indexOf("cluster") != -1 ||
                o.objectDescriptor.equalsIgnoreCase("CpG Island"))
              o.objectFill = false;
            o.nrOccInSet = nrOfFeatures("misc_feature", "type",
                                        o.objectDescriptor);
          }
          objectMap.put(key, o);
          //writeToStatus(o.objectID +"|"+o.objectDescriptor +"|"+o.objectType +"|"+o.objectColor.toString());;
        }
      }
    }
  }

  public HashSet getDistinctFeatSources() {
    HashSet sources = new HashSet();
    Gene hg;
    HashSet gs;
    Object o;
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      hg = (Gene) i.next();
      gs = hg.getDistinctFeatSources();
      for (Iterator j = gs.iterator(); j.hasNext(); ) {
        o = j.next();
        if (!sources.contains(o))
          sources.add(o);
      }
    }
    return sources;
  }

  public void writeXmlToFile(String path) throws Exception {
    String s = makeCompleteXml();
    FileOutputStream fout = new FileOutputStream(path);
    OutputStreamWriter out = new OutputStreamWriter(fout);
    out.write(s);
    out.close();
    fout.close();
  }

  public void writeXmlToFile(String path, TreeMap objMap) throws Exception {
    String s = makeCompleteXml(objMap);
    FileOutputStream fout = new FileOutputStream(path);
    OutputStreamWriter out = new OutputStreamWriter(fout);
    out.write(s);
    out.close();
    fout.close();
  }

  public HashMap getFreqMap() {
    HashMap freqMap = new HashMap();
    TreeMap objects = this.getObjects();
    Factor o;
    Double F;
    for (Iterator it = objects.keySet().iterator(); it.hasNext(); ) {
      o = (Factor) objects.get(it.next());
      F = new Double(new Double(o.nrOccInSet).doubleValue() /
                     new Double(getTotalLength() * 2).doubleValue());
      freqMap.put(o.objectDescriptor, F);
    }
    return freqMap;
  }

  public void writeFreqFile(String path) throws Exception {
    FileOutputStream fout = new FileOutputStream(path);
    OutputStreamWriter out = new OutputStreamWriter(fout);
    HashMap freqMap = this.getFreqMap();
    Double F;
    String key;
    for (Iterator it = freqMap.keySet().iterator(); it.hasNext(); ) {
      key = (String) it.next();
      F = (Double) freqMap.get(key);
      out.write(key + "\t" + F.doubleValue() + "\n");
    }
    out.close();
    fout.close();
  }

  public GeneList createModuleGeneList(String featName1, String annKey1,
                                       String annValue1, String featName2,
                                       String annKey2, String annValue2,
                                       int maxBpBetween, int aroundMiddle) throws
      Exception {
    GeneList gl = new GeneList(owner);
    Gene hg = null, g = null;
    Sequence s;
    ArrayList seqList;
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      hg = (Gene) i.next();
      seqList = hg.selectModule(featName1, annKey1, annValue1, featName2,
                                annKey2, annValue2, maxBpBetween, aroundMiddle);
      for (Iterator it = seqList.iterator(); it.hasNext(); ) {
        g = new Gene();
        s = (Sequence) it.next();
        g.setSequence(s, null);
        gl.list.add(g);
      }
    }
    return gl;
  }

  public String namesToPhylTree(boolean commaYesNo) {
    return namesToPhylTreeHelp(this.list, commaYesNo);
  }

  public String familyNamesToPhylTree(String parent, boolean commaYesNo) {
    Gene p = this.get(parent);
    ArrayList list = (ArrayList)p.orthoList.list.clone();
    list.add(p);
    return namesToPhylTreeHelp(list, commaYesNo);
  }

  private String namesToPhylTreeHelp(ArrayList l, boolean commaYesNo) {
    String tree = GlobalProperties.getEnsemblPhylogeneticTree();
    Gene g;
    EnsemblSpecies species[] = GlobalProperties.getSpeciesArr();
    /* The next array will contain a list of genes for all species in ensembl +
       for all other species */
    String allGenesOfSpecies[] = new String[species.length+1];
    for (int i = 0; i < species.length+1; i++) {
      allGenesOfSpecies[i] = "(";
    }
    for (Iterator it = l.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      boolean found = false;
      for (int i = 0; i < species.length; i++) {
        if (g.species !=null && g.species.longName!=null && g.species.longName == species[i].longName) {
          allGenesOfSpecies[i] = allGenesOfSpecies[i] + g.name + " ";
          found = true;
        }
      }
      if (!found) {
        allGenesOfSpecies[species.length] = allGenesOfSpecies[species.length] + g.name + " ";
      }
    }
    for (int i = 0; i < species.length+1; i++) {
      allGenesOfSpecies[i] = allGenesOfSpecies[i] + ")";
    }
    for (int i = 0; i < species.length; i++) {
      tree = tree.replaceAll(species[i].longName, allGenesOfSpecies[i]);
    }
    tree = tree.replaceAll("other", allGenesOfSpecies[species.length]);
    tree = tree.replaceAll(" \\)","\\)");
    tree = collapseTree(tree);
    if (commaYesNo) tree = tree.replaceAll(" ",",");
    return tree;
  }

  private String collapseTree(String tree) {
    String newTree = tree.replaceAll("\\(\\)","");
    newTree = newTree.replaceAll("\\( ","\\(");
    newTree = newTree.replaceAll("\\) \\)","\\)\\)");
    boolean atEnd = false;
    if (newTree.length()<tree.length()) atEnd = true;
    int openBrackett = -1;
    int closedBrackett = 0;
    while (!atEnd) {
      openBrackett = newTree.indexOf("(",openBrackett+1);
      if (openBrackett == -1) {
        atEnd = true;
        break;
      }
      closedBrackett = findCorrespondingBrackett(newTree, openBrackett);
      String subTree = newTree.substring(openBrackett+1,closedBrackett);
      if (subTree.indexOf(" ") == -1 ||
          (subTree.charAt(0) == '(' && findCorrespondingBrackett(subTree,0) == subTree.length()-1)) {
        newTree = newTree.substring(0,openBrackett) + subTree + newTree.substring(closedBrackett+1,newTree.length());
      }
    }
    if (newTree.length()<tree.length()) return collapseTree(newTree);
    else return tree;
  }

  private int findCorrespondingBrackett(String tree, int openBrackett) {
    boolean found = false;
    int level = 0;
    int pos = openBrackett;
    while (!found) {
      pos++;
      char c = tree.charAt(pos);
      if (c == '(')
        level++;
      else if (c == ')')
        level--;
      if (level == -1) return pos;
    }
    return -1;
  }

  public void annotateCpG() throws Exception {
    Gene hg = null;
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      hg = (Gene) i.next();
      //writeToStatus("CpG annotation for "+hg.name+" | "+hg.length);
      try {
        hg.annotateCpG();
      }
      catch (Exception e) {
        writeToStatus("... CpG annotation didn't work for " + hg.name);
        e.printStackTrace();
      }
    }
    updateObjectMap(false);

  }

  public void annotateCGdinuclHuman() throws Exception {
    Gene hg = null;
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      hg = (Gene) i.next();
      //writeToStatus("CpG annotation for "+hg.name+" | "+hg.length);
      try {
        hg.annotateCGdinuclHuman();
      }
      catch (Exception e) {
        writeToStatus("... CG/GC/CC/GG annotation didn't work for " +
                                hg.name);
        e.printStackTrace();
      }
    }
    updateObjectMap(false);
  }

  public void annotateATdinuclDroso() throws Exception {
    Gene hg = null;
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      hg = (Gene) i.next();
      //writeToStatus("CpG annotation for "+hg.name+" | "+hg.length);
      try {
        hg.annotateATdinuclDroso();
      }
      catch (Exception e) {
        writeToStatus("... AA/TT/AT/TA annotation didn't work for " +
                                hg.name);
        e.printStackTrace();
      }
    }
    updateObjectMap(false);
  }

  /*
     public void annotateEponineTSS(double d) throws Exception{
         Gene hg = null;
         for (Iterator i = list.iterator(); i.hasNext();){
             hg = (Gene)i.next();
             try{
             hg.annotateEponineTSS(d);
      }catch(Exception e){writeToStatus("... Eponine TSS annotation didn't work for "+hg.name);e.printStackTrace();}
         }
     }
   */

  public int annotateMotifWindow(String motifName, String motif,
                                 int windowLength, int step, int minNrMotifs) throws
      Exception {
    int cnt = 0;
    Gene hg = null;
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      hg = (Gene) i.next();
      cnt = cnt +
          hg.annotateMotifWindow(motifName, motif, windowLength, step,
                                 minNrMotifs);
    }
    return cnt;
  }

  public void annotateSingleMotif(String motifName, String motif, double score, String source) throws
      Exception {
    Gene hg = null;
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      hg = (Gene) i.next();
      //writeToStatus("Motif annotation for "+hg.name+" | "+hg.length);
      try {
        hg.annotateSingleMotif(motifName, motif, score, source);
      }
      catch (Exception e) {
        e.printStackTrace();
        writeToStatus("... Motif annotation didn't work for" +
                                hg.name);
      }
    }
  }
  
  public int annotateIupac(String name, String iupac) throws Exception{
	  return annotateIupac(name,iupac,0.0d, "Manual");
  }

  public int annotateIupac(String name, String iupac, double score, String source) throws Exception {
    int toRet=0;
  	Gene hg = null;
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      hg = (Gene) i.next();
      //writeToStatus("Motif annotation for "+hg.name+" | "+hg.length);
      try {
        toRet += hg.annotateIupac(name, iupac, score, source);
      }
      catch (Exception e) {
        e.printStackTrace();
        writeToStatus("... iupac annotation didn't work for" +
                                hg.name);
      }
    }
    updateObjectMap(false);
    return toRet;
  }

  public void annotateIupacWindow (String motifName, String iupac,
                                 int windowLength, int step, int minNrMotifs) throws Exception {
    Gene hg = null;
    int cnt;
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      hg = (Gene) i.next();
      cnt=0;
      //writeToStatus("Motif annotation for "+hg.name+" | "+hg.length);
      try {
        cnt = hg.annotateIupacWindow (motifName, iupac,
                                 windowLength, step, minNrMotifs);
        if(cnt>0) System.out.println(hg.name);
      }
      catch (Exception e) {
        e.printStackTrace();
        writeToStatus("... iupac annotation didn't work for" +
                                hg.name);
      }
    }
    updateObjectMap(false);
  }
  
  public void annotateIupacPair (String motifName1, String iupac1,String motifName2,String iupac2,
        int windowLength, int step) throws Exception {
Gene hg = null;
int cnt;
for (Iterator i = list.iterator(); i.hasNext(); ) {
hg = (Gene) i.next();
cnt=0;
//writeToStatus("Motif annotation for "+hg.name+" | "+hg.length);
try {
cnt = hg.annotateIupacPair(motifName1, iupac1,motifName2,iupac2,
        windowLength, step);
//if(cnt>0) System.out.println(hg.name);
}
catch (Exception e) {
e.printStackTrace();
writeToStatus("... iupac annotation didn't work for" +
       hg.name);
}
}
updateObjectMap(false);
}

  public void annotateMotifPairPost (String motifName1,String motifName2,
        int windowLength,String name) throws Exception {
	Gene hg = null;
	int cnt;
	for (Iterator i = list.iterator(); i.hasNext(); ) {
	hg = (Gene) i.next();
	cnt=0;
	try {
	cnt = hg.annotateMotifPairPost(motifName1, motifName2,
	        windowLength,name);
	//if(cnt>0) System.out.println(hg.name);
	}
	catch (Exception e) {
	e.printStackTrace();
	writeToStatus("... iupac annotation didn't work for" +
	       hg.name);
	}
	}
	updateObjectMap(false);
}
  
  
  public void annotateMotifPairPostNoOverlap (String motifName1,String motifName2,
        int windowLength,String name) throws Exception {
	Gene hg = null;
	int cnt;
	for (Iterator i = list.iterator(); i.hasNext(); ) {
	hg = (Gene) i.next();
	cnt=0;
	try {
	cnt = hg.annotateMotifPairPostNoOverlap(motifName1, motifName2,
	        windowLength,name);
	//if(cnt>0) System.out.println(hg.name);
	}
	catch (Exception e) {
	e.printStackTrace();
	writeToStatus("... iupac annotation didn't work for" +
	       hg.name);
	}
	}
	updateObjectMap(false);
}
  
  
  public void annotatePWM(PWM pwm,double threshold,String pwmName){
  	Gene hg = null;
	int cnt;
	for (Iterator i = list.iterator(); i.hasNext(); ) {
	hg = (Gene) i.next();
	try {
		hg.annotatePWM(pwm,threshold,pwmName);
	}
	catch (Exception e) {
	e.printStackTrace();
	writeToStatus("... PWM annotation didn't work for" +
	       hg.name);
	}
	}
	updateObjectMap(false);
  }

  public void slidingWindow (ArrayList motifNameList,
        int windowSize,int stepSize,double minRatio) throws Exception {
	Gene hg = null;
	int cnt;
	for (Iterator i = list.iterator(); i.hasNext(); ) {
	hg = (Gene) i.next();
	cnt=0;
	try {
	cnt = hg.slidingWindow(motifNameList,windowSize,stepSize,minRatio);
	//if(cnt>0) System.out.println(hg.name);
	}
	catch (Exception e) {
	e.printStackTrace();
	writeToStatus("... iupac annotation didn't work for" +
	       hg.name);
	}
	}
	updateObjectMap(false);
}

  public void removeFeatures(String objectDescriptor) throws Exception{
    Gene hg = null;
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      hg = (Gene) i.next();
      hg.removeFeatures(objectDescriptor);
    }
  }

  public void printFeatures() {
    Gene hg = null;
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      hg = (Gene) i.next();
      hg.printFeatures();
    }
  }


  public void removeObject(String objectDescriptor){
    Gene hg = null;
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      hg = (Gene) i.next();
      hg.removeObject(objectDescriptor);
    }
    objectMap.remove(objectDescriptor);
  }

  /**
   * @return the total number of basepairs in the list, SINGLE stranded
   * do times 2, for total number of possible binding start sites, etc.
   */
  public int getTotalLength() {
    int totalLength = 0;
    Gene g = null;
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      g = (Gene) i.next();
      totalLength = totalLength + g.length;
    }
    return totalLength;
  }

  public int nrOfFeatures(String featName, String annKey, String annValue) {
    int nr = 0;
    Gene g = null;
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      g = (Gene) i.next();
      nr = nr + g.nrFeatures(featName, annKey, annValue);
    }
    return nr;
  }

  public ArrayList allNrFeatures(String featName) {
    ArrayList featMapList = new ArrayList();
    Gene g = null;
    HashMap featMap;
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      g = (Gene) i.next();
      featMap = g.allNrFeatures(featName);
      featMapList.add(featMap);
    }
    return featMapList;
  }

  /**
   * @return  number of occurences in all sequences of a certain feature divided by the total lenght of all sequences
   */
  public double featFrequency(String featName, String annKey, String annValue) {
    double freq = new Integer(nrOfFeatures(featName, annKey, annValue)).
        doubleValue() / new Integer(getTotalLength()).doubleValue();
    return (freq);
  }

  public StringBuffer toFasta() {
    StringBuffer buf = new StringBuffer();
    Gene hg = null;
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      hg = (Gene) i.next();
      buf.append(">" + hg.name + "\n");
      buf.append(hg.seq.seqString() + "\n");
    }
    return buf;
  }

  public void objColorsToComment() throws Exception{
    //put it in the annotation of the first gene
    Gene g = (Gene)this.list.get(0);
    Factor f;
    for (Iterator it = objectMap.keySet().iterator(); it.hasNext(); ) {
      f = (Factor) objectMap.get(it.next());
      g.addToComment("color=" + f.objectDescriptor + "|" +
                     f.objectColor.getRed() + "|" + f.objectColor.getGreen() +
                     "|" + f.objectColor.getBlue() + ";");
    }
  }

  public void writeToLargeEmblFile(String path) throws Exception {
    //save the object colors
    objColorsToComment();
    File output = new File(path);
    if (output.isFile()) {
      output.delete();
      //writeToStatus("Overwriting file...");
    }
    output.createNewFile();
    BufferedWriter out = new BufferedWriter(new FileWriter(output.getPath(), true));
    Gene hg = null;
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      hg = (Gene) i.next();
      if (hg.length > 0) {
        out.write("ID   " + hg.name + "\r\n");
        out.flush();
        hg.appendSeqToEmblFile(path);
      }
    }
    out.flush();
    out.close();
  }

  public void appendToLargeEmblFile(String path) throws Exception {
   File output = new File(path);
   BufferedWriter out = new BufferedWriter(new FileWriter(output.getPath(), true));
   Gene hg = null;
   for (Iterator i = list.iterator(); i.hasNext(); ) {
     hg = (Gene) i.next();
     if (hg.length > 0) {
       out.write("ID   " + hg.name + "\r\n");
       out.flush();
       hg.appendSeqToEmblFile(path);
     }
   }
   out.flush();
   out.close();
 }


  public void writeToLargeGbFile(String path) throws Exception {
    File output = new File(path);
    if (output.isFile()) {
      output.delete();
      //writeToStatus("Overwriting file...");
    }
    output.createNewFile();
    BufferedWriter out = new BufferedWriter(new FileWriter(output.getPath(), true));
    Gene hg = null;
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      hg = (Gene) i.next();
      out.write("ID   " + hg.name + ";\r\n");
      out.flush();
      hg.appendSeqToGbFile(path);
    }
    out.close();
  }

  public String getFastaStr() throws Exception {
    StringBuffer buf = new StringBuffer();
    Gene hg = null;
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      hg = (Gene) i.next();
      buf.append(hg.getFastaStr());
    }
    return buf.toString();
  }

  public String getEmblStr() throws Exception {
    StringBuffer buf = new StringBuffer();
    Gene hg = null;
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      hg = (Gene) i.next();
      buf.append(hg.getEmblStr());
    }
    return buf.toString();
  }

  public String getFamilyFastaStr(String parentName) throws Exception {
    Gene p = this.get(parentName);
    StringBuffer buf = new StringBuffer();
    Gene hg = null;
    buf.append(p.getFastaStr());
    for (Iterator i = p.orthoList.list.iterator(); i.hasNext(); ) {
      hg = (Gene) i.next();
      buf.append(hg.getFastaStr());
    }
    return buf.toString();
  }

  public void writeToLargeFastaFile(String path) throws Exception {
    File output = new File(path);
    if (output.isFile()) {
      output.delete();
      //writeToStatus("Overwriting file...");
    }
    output.createNewFile();
    BufferedWriter out = new BufferedWriter(new FileWriter(output.getPath(), true));
    Gene hg = null;
    for (Iterator i = list.iterator(); i.hasNext(); ) {
      hg = (Gene) i.next();
      hg.appendSeqToFastaFile(path);
    }
    out.close();
  }

  public void constructFromLargeEmblStr(String embl) throws Exception {
    BufferedReader br = new BufferedReader(new InputStreamReader(new StringInputStream(embl)));
    constructFromLargeEmblBr(br);
  }

  public void constructFromLargeEmblFile(String path) throws Exception {
    BufferedReader br = new BufferedReader(new FileReader(path));
    constructFromLargeEmblBr(br);
  }

  public void constructFromLargeEmblBr(BufferedReader br) throws Exception {
    Gene gene;
    Sequence seq;
    SequenceIterator stream = null;

    stream = SeqIOTools.readEmbl(br);

    while (stream.hasNext()) {
      seq = stream.nextSequence();
      gene = new Gene();
      gene.seq = seq;
      gene.name = seq.getName();
      //Set ensembl id if this is the ID used as AC in the seq file
      if (gene.seq.getAnnotation().containsProperty("OS"))
        gene.species = GlobalProperties.getSpecies( (String) gene.seq.
            getAnnotation().getProperty("OS"));
  try {
        if (gene.species != null) { //then it -probably- originally came from Ensembl and was saved
          gene.ensembl = gene.name;
        }
        else if (gene.name.length() > 13 && gene.name.startsWith("ENS") &&
                 (gene.ensembl.equals("") || gene.ensembl == null)) {
          gene.ensembl = gene.name;
          if(gene.ensembl.indexOf("_")!=-1) gene.ensembl=gene.ensembl.substring(0,gene.ensembl.indexOf("_"));
          if(gene.ensembl.startsWith("ENSG")) gene.species = GlobalProperties.getSpecies("human");
          if(gene.ensembl.startsWith("ENSMUSG")) gene.species = GlobalProperties.getSpecies("mouse");
        }
      }
      catch (Exception e) {}
      gene.length = seq.length();
      list.add(gene);
    }
    br.close();

    //Set object colors if they were saved
    updateObjectMap(true);
    String cc = "", temp;
    ArrayList ccs = new ArrayList();
    gene = (Gene)list.get(0);
    String objDescr, green,red,blue;
    Color objColor;
    if (gene.seq.getAnnotation().containsProperty("CC")) {
      try {
        cc = (String) gene.seq.getAnnotation().getProperty("CC");
      }
      catch (ClassCastException cce) {
        ccs = (ArrayList) gene.seq.getAnnotation().getProperty("CC");
        for (Iterator iter = ccs.iterator(); iter.hasNext(); ) {
          cc += (String) iter.next();
        }
      }
      if (cc.indexOf("color") != -1) {
        StringTokenizer tok = new StringTokenizer(cc, ";");
        StringTokenizer tok2;
        while (tok.hasMoreElements()) {
          temp = (String) tok.nextElement();
          if(temp.indexOf("color")!=-1){
            temp = temp.substring(temp.indexOf("color=")+6, temp.length());
            tok2 = new StringTokenizer(temp,"|");
            objDescr = (String) tok2.nextElement();
            red = (String)tok2.nextElement();
            green = (String)tok2.nextElement();
            blue = (String)tok2.nextElement();
            System.out.println(red+"-"+green+"-"+blue);
            objColor = new Color(Integer.parseInt(red),Integer.parseInt(green),Integer.parseInt(blue));
            System.out.println(objDescr + "=" + objColor);
            if(objectMap.get(objDescr)!=null){
              ( (Factor) objectMap.get(objDescr)).objectColor = objColor;
              ( (Factor) objectMap.get(objDescr)).setImageIcon();
            }
          }
        }
      }
    }

    //cdspositive?
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      gene = (Gene) it.next();
      try{
        if (!gene.cdsStrandPositive("gene", gene.name)) gene.cdsPositive = false;
        else gene.cdsPositive=true;
      }
      catch(Exception e){
        System.out.println(e.getMessage());
      }
    }

    //orthos
    cc = "";
    ccs = new ArrayList();
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      gene = (Gene) it.next();
      if (gene.seq.getAnnotation().containsProperty("CC")) {
        try {
          cc = (String) gene.seq.getAnnotation().getProperty("CC");
        }
        catch (ClassCastException cce) {
          ccs = (ArrayList) gene.seq.getAnnotation().getProperty("CC");
          for (Iterator iter = ccs.iterator();iter.hasNext();){
           cc+=(String)iter.next();
          }
        }
        if (cc.indexOf("ortho") != -1) {
          StringTokenizer tok = new StringTokenizer(cc, ";");
          while (tok.hasMoreElements()) {
            temp = (String) tok.nextElement();
            if(temp.indexOf("ortho")!=-1){
	    temp = temp.substring(temp.indexOf("ortho=") + 6, temp.length());
            if (gene.orthoList.get(temp) == null) {
              if(get(temp)!=null){
                gene.orthoList.add(this.get(temp));
                get(temp).setParent(gene);
              }
            }
	    }
          }
        }
      }
    }
  }

  public void constructFromLargeGbFile(String path) throws Exception {
    Gene gene;
    Sequence seq;
    SequenceIterator stream = null;
    BufferedReader br = null;
    br = new BufferedReader(new FileReader(path));
    stream = SeqIOTools.readGenbank(br);
    while (stream.hasNext()) {
      seq = stream.nextSequence();
      gene = new Gene();
      gene.seq = seq;
      gene.name = seq.getName();
      gene.length = seq.length();
      list.add(gene);
    }
    br.close();
  }

  public void constructFromLargeFastaString(String str) throws Exception {
    Gene gene;
    Sequence seq;
    SequenceIterator stream = null;
    BufferedReader br = null;
    br = new BufferedReader(new StringReader(str));
    stream = SeqIOTools.readFastaDNA(br);
    while (stream.hasNext()) {
      seq = stream.nextSequence();
      gene = new Gene();
      gene.seq = seq;
      gene.name = seq.getName();
      gene.length = seq.length();
      list.add(gene);
    }
  }

  public void constructFromLargeFastaFile(String path) throws Exception {
    Gene gene;
    Sequence seq;
    SequenceIterator stream = null;
    BufferedReader br = null;
    br = new BufferedReader(new FileReader(path));
    stream = SeqIOTools.readFastaDNA(br);
    while (stream.hasNext()) {
      seq = stream.nextSequence();
      gene = new Gene();
      gene.seq = seq;
      gene.name = seq.getName();
      gene.length = seq.length();
      list.add(gene);
    }
    br.close();
  }

  public void union(GeneList gl) {
    if (gl != null && gl.list.size() > 0) {
      for (Iterator it = gl.list.iterator(); it.hasNext(); ) {
        this.add( (Gene) it.next());
      }
    }
  }

  /**
   * if group doesn't equal -1 then the group number is added as an extra column
   * @param   uniqueFeatureSet: if null is given, then this set is calculated from the genelist itself.
   */
  public void score4Bn(String csvPath, int group, Set uniqueFeatureSet) throws
      Exception {
    ArrayList featMapList = allNrFeatures("misc_feature");
    if (uniqueFeatureSet == null) {
      uniqueFeatureSet = Tools.createUniqueFeatSet(featMapList);
    }
    String[] nameArr = new String[list.size()];
    for (int i = 0; i < list.size(); i++) {
      nameArr[i] = ( (Gene) list.get(i)).name;
    }
    ArrayList featList = new ArrayList();
    for (Iterator it = uniqueFeatureSet.iterator(); it.hasNext(); ) {
      featList.add( (String) it.next());
    }
    Collections.sort(featList);
    Object[] featArr = featList.toArray();

    //writeToStatus(uniqueFeatureSet);
    int[][] motifMatrix = new int[nameArr.length][featArr.length];
    HashMap featMap;
    for (int i = 0; i < featMapList.size(); i++) {
      featMap = (HashMap) featMapList.get(i);
      if (featMap != null) {
        for (int j = 0; j < featArr.length; j++) {
          if (featMap.containsKey(featArr[j])) {
            motifMatrix[i][j] = ( (Integer) featMap.get(featArr[j])).intValue();
            //motifMatrix[i][j] = 1;
          }
          else motifMatrix[i][j] = 0;
          //writeToStatus(motifMatrix[i][j]+" ");
        }
        //writeToStatus(nameArr[i]);
      }
    }

    //write matrix to file as csv
    FileOutputStream fout = new FileOutputStream(csvPath);
    OutputStreamWriter out = new OutputStreamWriter(fout);
    StringBuffer buf = new StringBuffer();
    //buf.append("geneName,");
    for (int j = 0; j < featArr.length; j++) {
      buf.append(featArr[j]);
      buf.append(",");
    }
    buf.deleteCharAt(buf.length() - 1);
    //group
    if (group != -1) buf.append(",group");
    buf.append("\n");

    for (int i = 0; i < nameArr.length; i++) {
      //buf.append(nameArr[i]);
      //buf.append(",");
      for (int j = 0; j < featArr.length; j++) {
        String temp = "" + motifMatrix[i][j];
        if (temp.length() > 4) temp = temp.substring(0, 4);
        buf.append(temp);
        buf.append(",");
      }
      buf.deleteCharAt(buf.length() - 1);
      if (group != -1) buf.append("," + group);
      buf.append("\n");
    }
    out.write(buf.toString());
    out.close();
    fout.close();
  }

  public GeneList splitForMotifScanner() throws Exception {
    Gene g;
    GeneList temp = new GeneList(owner);
    GeneList def = new GeneList(owner);
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      temp = g.splitForMotifScanner();
      def.union(temp);
    }
    return def;
  }

  public void trimFlankingNs() throws Exception {
    Gene g;
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      g.trimFlankingNs();
    }
  }

  public void saveEmblSplitForMotifScanner(String path) throws Exception {
    GeneList list = this.splitForMotifScanner();
    list.writeToLargeEmblFile(path);
  }

  public void saveFastaSplitForMotifScanner(String path) throws Exception {
    GeneList list = this.splitForMotifScanner();
    list.writeToLargeFastaFile(path);
  }

  public void saveGbSplitForMotifScanner(String path) throws Exception {
    GeneList list = this.splitForMotifScanner();
    list.writeToLargeGbFile(path);
  }

  public void removeNs() throws Exception {
    Gene g;
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      g.removeNs();
    }
  }

  /**
   * Currently: take a average motifLength of 10
   * Writes all T, n,n_ref, length_ref to tab-separated file
   */
  public void prepBinom(String refEmblPath, String outPath, String matrixPath) throws
      Exception {
    ArrayList output = new ArrayList();
    GeneList ref = new GeneList(owner);
    ref.constructFromLargeEmblFile(refEmblPath);
    TreeMap refMap = ref.getObjects();
    TreeMap objMap = this.getObjects();
    int T = 0;
    Gene g;
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      T = T + 1 + g.length - 10; //motifLength is very difficult to get here...
    }
    T = T * 2;
    int n;
    int n_r;
    int rl;
    double f;
    String out = "";

    FileOutputStream fout = new FileOutputStream(outPath);
    OutputStreamWriter writer = new OutputStreamWriter(fout);

    FileOutputStream mout = new FileOutputStream(matrixPath);
    OutputStreamWriter mwriter = new OutputStreamWriter(mout);

    for (Iterator j = objMap.keySet().iterator(); j.hasNext(); ) {
      String key = (String) j.next();
      Factor o = (Factor) objMap.get(key);
      if (o.objectType.equalsIgnoreCase("misc_feature")) {
        n = o.nrOccInSet;
        if (refMap.containsKey(key)) {
          Factor r = (Factor) refMap.get(key);
          //f = r.nrOccInSet/ref.getTotalLength();
          n_r = r.nrOccInSet;
        }
        //else f = 0d;
        else n_r = 0;
        //out = o.objectDescriptor+"\t"+T+"\t"+n+"\t"+f+"\n";
        writer.write(T + "\t" + n + "\t" + n_r + "\t" + ref.getTotalLength() +
                     "\n");
        mwriter.write(o.objectDescriptor + "\n");
        //writeToStatus(out);

      }
    }
    writer.close();
    mwriter.close();
    fout.close();
    mout.close();
  }

  /**
   * @return TreeMap holding all distinct objects, ordered the key=sig value
   */
  /*
      public TreeMap binom(String refEmblPath)throws Exception{
   ArrayList output = new ArrayList();
   GeneList ref = new GeneList();
   ref.constructFromLargeEmblFile(refEmblPath);

   TreeMap refMap = ref.getObjects();
   TreeMap objMap = this.getObjects();
   TreeMap returnMap = new TreeMap();
   Gene g;
   for (Iterator it = list.iterator();it.hasNext();){
     g = (Gene)it.next();
     T = T + 1 + g.length -10; //motifLength is very difficult to get here...
   }
   T = T*2;
   double f=0d;
   Double F = null;
   double prob;
   BinomialDistribution dist = new BinomialDistribution();
   for (Iterator j = objMap.keySet().iterator(); j.hasNext();){
         prob=0d;
      String key = (String)j.next();
         Factor o = (Factor)objMap.get(key);
      if (o.objectType.equalsIgnoreCase("misc_feature")){
     if (refMap.containsKey(key)) {
        Factor r = (Factor)refMap.get(key);
     F = new Double(new Double(r.nrOccInSet).doubleValue()/new Double(ref.getTotalLength()*2).doubleValue());
     dist.setParameters(T,F.doubleValue());
     for(int k=0;k<o.nrOccInSet;++k){
   prob = prob + dist.getDensity(Double.parseDouble(new Integer(k).toString()));
     }
     o.pValue = 1 - prob;
     if (Double.isNaN(o.pValue)) o.sig = Double.NaN;
     else o.sig = -log10(o.pValue * objMap.size());
     }
     else o.pValue = Double.NaN;
         }
      returnMap.put(new Double(o.sig),o);
      //writeToStatus(o.objectDescriptor+"\t"+o.pValue+"\t"+o.sig);
   }
   return returnMap;
     }
   */

  public HashMap readFreqFile(String freqFilePath) throws Exception {
    HashMap freqMap = new HashMap();
    BufferedReader in = new BufferedReader(new FileReader(freqFilePath));
    String line = in.readLine();
    StringTokenizer tok;
    String matrixName = "";
    String fStr = "";
    while (line != null) {
      if (!line.equalsIgnoreCase("")) {
        tok = new StringTokenizer(line, "\t");
        while (tok.hasMoreElements()) {
          matrixName = (String) tok.nextElement();
          fStr = (String) tok.nextElement();
          freqMap.put(matrixName, new Double(fStr));
        }
      }
      line = in.readLine();
    }
    in.close();
    return freqMap;
  }

  public TreeMap binom(String filePath, boolean freqFile, int T) throws Exception {
    HashMap freqMap;
    if (freqFile) {
      freqMap = this.readFreqFile(filePath);
    }
    else {
      GeneList ref = new GeneList(owner);
      ref.constructFromLargeEmblFile(filePath);
      freqMap = ref.getFreqMap();
    }
    double minFreq = getMin(freqMap);
    TreeMap objMap = this.getObjects();
    TreeMap returnMap = new TreeMap();
    Gene g;
    if(T==-1){
      T = getT();
    }
    writeToStatus("T= " + T);
    Double F;
    double cdf;
    BinomialDistribution dist = new BinomialDistribution();
    for (Iterator j = objMap.keySet().iterator(); j.hasNext(); ) {
      cdf = 0d;
      String key = (String) j.next();
      Factor o = (Factor) objMap.get(key);
      writeToStatus(o.objectType + "|" + o.objectDescriptor);
      if (o.objectType.equalsIgnoreCase("misc_feature")) {
        if (freqMap.containsKey(key)) {
          F = (Double) freqMap.get(key);
        }
        else {
          F = new Double(minFreq / 2);
          o.freqReplaced = true;
        }
        writeToStatus(o.objectDescriptor + ": Expected frequency= " +
                                F.toString());
        dist.setParameters(T, F.doubleValue());
        cdf = dist.getCDF(o.nrOccInSet);
        writeToStatus("CDF="+cdf);
        writeToStatus("Nr of occurences= " + o.nrOccInSet);
        /*for (int k = 0; k < o.nrOccInSet; ++k) {
          prob = prob +
              dist.getDensity(Double.parseDouble(new Integer(k).toString()));
          writeToStatus(prob+"|");
        }
        */
        writeToStatus("\n");
        //if (prob > 1.0d) prob = (Double.MAX_VALUE - 1) / Double.MAX_VALUE;
        //o.pValue = 1.0d - prob;
        o.pValue=1.0d-cdf;
        o.sig = -log10(o.pValue * objMap.size());
        writeToStatus(o.pValue + "|" + o.sig);
        returnMap.put(o.objectDescriptor, o);
      }
      //returnMap.put(new Double(o.sig),o);
      //writeToStatus(o.objectDescriptor+"\t"+o.pValue+"\t"+o.sig);
    }
    return returnMap;
  }

  public int getT(){
    Gene g;
    T = 0;
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      T = T + 1 + g.length - 10; //motifLength is very difficult to get here...
    }
    T = T * 2;
    return T;
  }

  public TreeMap MetaBinom(String filePath, boolean freqFile) throws Exception {
    HashMap freqMap;
    if (freqFile) {
      freqMap = this.readFreqFile(filePath);
    }
    else {
      GeneList ref = new GeneList(owner);
      ref.constructFromLargeEmblFile(filePath);
      freqMap = ref.getFreqMap();
    }
    double minFreq = getMin(freqMap);
    TreeMap objMap;
    TreeMap returnMap = new TreeMap();
    TreeMap[] binomMaps = new TreeMap[list.size()];
    HashSet distKeys = new HashSet();
    Gene g;
    int index =0;
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      objMap = g.getObjects();
      binomMaps[index] = new TreeMap();
      T = 2 * (g.length - 10); //motifLength is very difficult to get here...
      writeToStatus("T= " + T);
      Double F=null;
      double prob;
      BinomialDistribution dist = new BinomialDistribution();
      for (Iterator j = objMap.keySet().iterator(); j.hasNext(); ) {
        prob = 0d;
        String key = (String) j.next();
        Factor o = (Factor) objMap.get(key);
        writeToStatus(o.objectType + "|" + o.objectDescriptor);
        if (o.objectType.equalsIgnoreCase("misc_feature")) {
          if (freqMap.containsKey(key)) {
            F = (Double) freqMap.get(key);
            if (!distKeys.contains(key)) distKeys.add(key);
          }
          writeToStatus(o.objectDescriptor + ": Expected frequency= " +
                                  F.toString());
          dist.setParameters(T, F.doubleValue());
          writeToStatus("Nr of occurences= " + o.nrOccInSet);
          for (int k = 0; k < o.nrOccInSet; ++k) {
            prob = prob +
                dist.getDensity(Double.parseDouble(new Integer(k).toString()));
            //writeToStatus(prob+"|");
          }
        System.out.println("prob = "+prob);
        //writeToStatus("\n");
          if (prob > 1.0d) prob = (Double.MAX_VALUE - 1) / Double.MAX_VALUE;
          o.pValue = 1.0d - prob;
          o.sig = -log10(o.pValue * objMap.size());
          writeToStatus(o.pValue + "|" + o.sig);
          System.out.println("object="+o.objectDescriptor);
          binomMaps[index].put(o.objectDescriptor, o);
        }
      }
      index++;
    }

    System.out.println("done with the gene-wise binomials");
    double pval=0,temp;
    String key;
    Factor fact;
    ChiSquareDistribution chi = new ChiSquareDistribution();
    chi.setDegrees(2*binomMaps.length);
    for (Iterator iter = distKeys.iterator();iter.hasNext();){
      key = (String)iter.next();
      for(int j = 0;j<binomMaps.length;j++){
        if(binomMaps[j].containsKey(key)){
          temp = ( (Factor) binomMaps[j].get(key)).pValue; //bonferroni correction !
          pval += ( -2 * Math.log(temp));
        }
      }
      pval = 1-chi.getCDF(pval);
      System.out.println("pval="+pval);
      returnMap.put(key,new Double(pval));
      pval=0;
    }
    return returnMap;
  }


  public double log10(double x) {
    return Math.log(x) / Math.log(10.0);
  }

  /**
   * Get the minimal frequency from the frequency file (Doubles)
   */
  public double getMin(HashMap freqMap) {
    Double min = new Double(1.0E-10d);
    Double temp;
    for (Iterator it = freqMap.keySet().iterator(); it.hasNext(); ) {
      temp = (Double) freqMap.get(it.next());
      if (temp.doubleValue() < min.doubleValue()) min = temp;
    }
    return min.doubleValue();
  }

  public void addPositionScores(String fileName) throws Exception {
    HashMap ids = new HashMap();
    for (int i = 0; i < this.list.size(); i++) {
      ids.put( ( (Gene) list.get(i)).name, new Integer(i));
    }
    BufferedReader in = new BufferedReader(new FileReader(fileName));
    String name;
    Gene g = null;
    int j = 0;
    double[] tempArr;
    String line = in.readLine();
    while (line != null) {
      if (line.startsWith(">")) {
        name = line.substring(1);
        //writeToStatus(name);
        if (ids.containsKey(name)) {
          g = (Gene) list.get( ( (Integer) ids.get(name)).intValue());
          //writeToStatus("Gene is "+g.name);
          tempArr = new double[g.length];
          j = 0;
          line = in.readLine();
          while (line != null && !line.startsWith(">")) {
            tempArr[j] = Double.parseDouble(line);
            j++;
            line = in.readLine();
          }
          if (tempArr.length == g.length)
            g.scoreArr = tempArr;
          else
            throw new Exception("Wrong score length for " + g.name);
        }
        else {
          writeToStatus("No such ID found");
          line = in.readLine();
          while (line != null && !line.startsWith(">")) {
            line = in.readLine();
          }
        }
      }
      else
        throw new Exception("Wrong file format, > with id is needed ");
    }
    in.close();
  }

  public Gene get(String name) {
    Gene g;
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      if (g.name.equalsIgnoreCase(name))
        return g;
    }
    return null;
  }

  public GeneList getLike(String name) {
    Gene g;
    GeneList toRet = new GeneList();
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      if (g.name.indexOf(name)!=-1)
        toRet.add(g);
    }
    return toRet;
  }

  
  public boolean containsPairs() {
    Gene g;
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      if (g.parent == null && g.orthoList.list.size() > 0)
        return true;
    }
    return false;
  }
  
  public String containsGeneWithName(String q){
  	Gene g;
  	for(Iterator it=list.iterator();it.hasNext();){
  		g=(Gene)it.next();
  		if(g.name.indexOf(q)!=-1)
  			return g.name;
  	}
  	return null;
  }

  public boolean containsFamilies() {
    Gene g;
    for (Iterator it = list.iterator(); it.hasNext(); ) {
      g = (Gene) it.next();
      if (g.parent == null && g.orthoList.list.size() > 0)
        return true;
    }
    return false;
  }

  public void serialize(String file) throws Exception {
    FileOutputStream f = new FileOutputStream(file);
    ObjectOutputStream s = new ObjectOutputStream(f);
    s.writeObject(this);
    s.flush();
    s.close();
  }

  public static GeneList deserialize(File file) throws Exception {
    FileInputStream in = new FileInputStream(file);
    ObjectInputStream s = new ObjectInputStream(in);
    GeneList toReturn = (GeneList) s.readObject();
    return toReturn;
  }

  public void writeToStatus(String message) {
    if(owner != null) owner.writeToStatus(message);
    else System.out.println(message);
  }

  public static void main(String[] args) throws Exception {
    GeneList gl = new GeneList();
    gl.constructFromLargeFastaFile("/home/saerts/projects/data/dm2/chromFa/chr2L.tfa");
    //System.out.println("name='"+((Gene)gl.list.get(0)).name+"'");
    //System.out.println("name='"+((Gene)gl.list.get(1)).name+"'");
    //gl.addGffFromFile("/home/saerts/tmp/dp2/avid.gff");
    //gl.printFeatures();
  }

  public String getCountMatrixStr (Factor f) throws Exception{
    String toRet=">";
    int[][] mx = this.getCountMatrix(f);
    toRet += f.objectDescriptor + "\t" + mx[0].length;
    toRet+="\n";
    for (int j =0;j<mx[0].length;j++){
      for (int i = 0;i<mx.length;i++){
        toRet+=mx[i][j]+"\t";
      }
      toRet +="\n";
    }
    toRet += "<";
    return toRet;

  }
  
  //clusterbuster format
  public String getCountMatrixStrCB (Factor f) throws Exception{
    String toRet=">";
    int[][] mx = this.getCountMatrix(f);
    toRet += f.objectDescriptor;
    toRet+="\n";
    for (int j =0;j<mx[0].length;j++){
      for (int i = 0;i<mx.length;i++){
        toRet+=mx[i][j]+"\t";
      }
      toRet +="\n";
    }
    return toRet;

  }
  
  public void appendCountMatrixToFile(Factor f,String format,String path)throws Exception{
  	File output = new File(path);
    BufferedWriter out = new BufferedWriter(new FileWriter(output.getPath(), true));
  	if(format.equalsIgnoreCase("ahab")){
  		out.write(getCountMatrixStr(f));
  	}
  	else if(format.equalsIgnoreCase("INCLUSive")){
  		out.write(getCountMatrixStrSCD(f));
  	}
  	else if(format.equalsIgnoreCase("ClusterBuster")){
  		out.write(getCountMatrixStrCB(f));
  	}
  	out.flush();
  	out.close();
  }

  public String getCountMatrixStrSCD (Factor f) throws Exception{
    String toRet="#INCLUSive Motif Model v1.0\n";
    toRet += "#ID = "+f.objectDescriptor+"\n";   
    
	int[][] mx = this.getCountMatrix(f);
	toRet += "#W = "+mx[0].length + "\n";
	toRet += "#Consensus = \n";
    for (int j =0;j<mx[0].length;j++){
      for (int i = 0;i<mx.length;i++){
        toRet+=mx[i][j]+"\t";
      }
      toRet +="\n";
    }
    toRet += "\n";

    return toRet;

  }

  public int[][] getCountMatrix (Feature f) throws Exception{
    int[][] toRet = null;
    String annKey, annValue, featStr;
    GeneList sub = new GeneList();
    Gene g;
    if (f.getType().equalsIgnoreCase("misc_feature")) {
      annKey = "type";
      annValue = (String) f.getAnnotation().getProperty("type");
      sub = getSubSequences("misc_feature", annKey, annValue,
                            "around",
                            0, 0,true);
    }
    else {
      featStr = f.getType();
      sub = getSubSequences(featStr, null, null,
                            "around",
                            0, 0,true);
    }
    int w = ((Gene)sub.list.get(0)).length;
    //System.out.println(w);
    toRet = new int[4][w];
    for(int i=0;i<4;i++){
      for (int j=0;j<w;j++){
        toRet[i][j]=0;
      }
    }
    String s;
    for (Iterator it = sub.list.iterator(); it.hasNext() ;){
      g=(Gene)it.next();
      s = g.seq.seqString();
      for (int j = 0; j < w; j++) {
        if (s.charAt(j) == 'a')
          toRet[0][j]++;
        else if (s.charAt(j) == 'c')
          toRet[1][j]++;
        else if (s.charAt(j) == 'g')
          toRet[2][j]++;
        else if (s.charAt(j) == 't')
          toRet[3][j]++;

      }
    }


    return toRet;
  }

  public int[][] getCountMatrix (Factor f) throws Exception{
    int[][] toRet = null;
    String annKey, annValue, featStr;
    GeneList sub = new GeneList();
    Gene g;
    if (f.objectType.equalsIgnoreCase("misc_feature")) {
      annKey = "type";
      annValue = (String) f.objectDescriptor;
      sub = getSubSequences("misc_feature", annKey, annValue,
                            "around",
                            0, 0,true);
    }
    else {
      featStr = f.objectDescriptor;
      sub = getSubSequences(featStr, null, null,
                            "around",
                            0, 0,true);
    }
    int w = ((Gene)sub.list.get(0)).length;
    //System.out.println(w);
    toRet = new int[4][w];
    for(int i=0;i<4;i++){
      for (int j=0;j<w;j++){
        toRet[i][j]=0;
      }
    }
    String s;
    for (Iterator it = sub.list.iterator(); it.hasNext() ;){
      g=(Gene)it.next();
      s = g.seq.seqString();
      if(g.length!=w){
      	//System.out.println("length problem: w="+w+" while g.length="+g.length+"  // name = "+g.name+" and seq="+s);
      }
      //System.out.println(s);
      else{
      	for (int j = 0; j < w; j++) {
       
        //if(g.seq.symbolAt(j).equals(DNATools.a()))
        if (s.charAt(j) == 'a')
          toRet[0][j]++;
        else if (s.charAt(j) == 'c')
          toRet[1][j]++;
        else if (s.charAt(j) == 'g')
          toRet[2][j]++;
        else if (s.charAt(j) == 't')
          toRet[3][j]++;
      }
      }
    }


    return toRet;
  }




  public void updateGeneNames() throws Exception{
    Gene g;
for(Iterator it = list.iterator();it.hasNext();){
  g = (Gene)it.next();
  g.updateGeneName();
}
}

  }
