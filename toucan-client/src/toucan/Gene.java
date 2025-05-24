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
import jodd.util.StringOutputStream;
import toucan.util.*;

import java.util.*;
import java.io.*;
import java.net.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.impl.*;
import org.apache.oro.text.regex.*;
import java.sql.*;
import org.w3c.dom.*;
import java.util.Properties;
import java.awt.image.*;
import org.biojava.bio.dp.*;


/**
 * Title: Gene
 * Description: A gene encloses a sequence, and implements several methods to manipulate, annotate, retreive, etc. this sequence.
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 2.0
 */

public class Gene
    implements Cloneable,Serializable {

  public Gene() {
    cloneAcc = "";
    accNr = "";
    uniGene = "";
    name = "";
    desc = "";
    function = "";
    homoloGene = "";
    geneCard = "";
    locusLink = "";
    cgap = "";
    chrom = "";
    goList = null;
    mgi = "";
    tigr = "";
    swissProt = "";
    ensembl = "";
    strand = 0;
    length = 0;
    seq = null;
    hugo = "";
    utrStart = 0;
    transcriptStartDiff = 0;
    transcriptEndDiff = 0;
    transcriptDiff = 0;
    ensOrtho = "";
    externalDb = externalDbEntry = "";
    scoreArr = null;
    orthoList = new GeneList();
    parent = null;
    startIdentifier="ensembl";
  }

  public String externalDb, externalDbEntry, hugo, cloneAcc, accNr, uniGene,
      name, desc, function, homoloGene, geneCard, locusLink, cgap, tigr, mgi,
      chrom, swissProt, ensembl, ensOrtho, startIdentifier;
  public ArrayList goList; //an ArrayList with GoEntry objects
  public int length, nrMotifsDirect, nrMotifsRev, utrStart, transcriptStartDiff,
      transcriptEndDiff, transcriptDiff;
  double expNr;
  public int strand, externalDbId;
  public Sequence seq;
  public String tempPath = "";
  public boolean cdsPositive;
  private TreeMap objects;
  public Properties infoProps;
  public EnsemblSpecies species;
  public double[] scoreArr;
  public Gene parent;
  public GeneList orthoList;

  public void setSeqStr(String seqStr) throws Exception {
    Alphabet dna = DNATools.getDNA();
    SymbolTokenization dnaToke = dna.getTokenization("token");
    SimpleSequence seq2 = new SimpleSequence(new SimpleSymbolList(dnaToke, seqStr),
                                             seq.getURN(), seq.getName(), seq.getAnnotation());
    Feature.Template templ = null;
    try{
    	for (Iterator it = seq.features(); it.hasNext(); ) {
      StrandedFeature f = (StrandedFeature) it.next();
      templ = f.makeTemplate();
      seq2.createFeature(templ);
    }
    }
    catch(NullPointerException e){
    	
    }
    seq = seq2;
  }

  public void setSequence(Sequence s, String nameStr) throws Exception {
    seq = s;
    if (nameStr == null && seq != null) name = seq.getName();
    else name = nameStr;
    //if(name.indexOf(".")!=-1) name = name.substring(0,name.indexOf("."))+name.substring(name.indexOf(".")+1,name.length());
    if (seq == null)
      length = 0;
    else {
      try {
        length = s.length();
      }
      catch (Exception e) {
        System.out.println(name + ": Length could not be set");
      }
    }
  }

  public void setParent(Gene g){
    this.parent = g;
  }

  /**
   * @param nameStr
   *        if nameStr=null then the name of the sequence (in the annotation) will be used
   */
  public void setEmblSeqFromFile(String path, String nameStr) {
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(path));
      setEmblSeqFromBuffReader(br, nameStr);
      br.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void setEmblSeqFromBuffReader(BufferedReader buf, String nameStr) {
    SequenceIterator stream = null;
    stream = SeqIOTools.readEmbl(buf);
    if (stream.hasNext()) {
      try {
        seq = stream.nextSequence();
        System.out.println("seq="+seq);
        //this is in order to set the seqName to the nameStr, since there is no setName method
        if (nameStr != null) seq = SequenceTools.subSequence(seq, 1, seq.length(),
            nameStr);
      }
      catch (Exception e2) {
        System.out.println(e2.getMessage());
        e2.printStackTrace();
        return; //no good sequence was found!
      }
    }
    if (nameStr != null)
      name = nameStr;
    else
      name = seq.getName();
      //if (nameStr==null && (name==null || name.equalsIgnoreCase(""))) name = seq.getName();
      //else {
      //  name = nameStr;
      //}
    if (name != null && name.length() > 13 && name.startsWith("ENS") &&
        (ensembl.equals("") || ensembl == null)) ensembl = name;
    if (name == null && ensembl != null && !ensembl.equalsIgnoreCase("")) name =
        ensembl;
    length = seq.length();
  }

  /**
   * @param nameStr
   *        if nameStr=null then the name of the sequence (in the annotation) will be used
   */
  public void setGbSeqFromFile(String path, String nameStr) {
    BufferedReader br = null;
    try {
      br = new BufferedReader(new FileReader(path));
      setGbSeqFromBuffReader(br, nameStr);
      br.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void setGbSeqFromBuffReader(BufferedReader buf, String nameStr) {
    SequenceIterator stream = null;
    stream = SeqIOTools.readGenbank(buf);
    if (stream.hasNext()) {
      try {
        seq = stream.nextSequence();
        //this is in order to set the seqName to the nameStr, since there is no setName method
        if (nameStr != null) seq = SequenceTools.subSequence(seq, 1, seq.length(),
            nameStr);
      }
      catch (Exception e2) {
        e2.printStackTrace();
        return; //no good sequence was found!
      }
    }
    if (nameStr != null)
      name = nameStr;
    else
      name = seq.getName();
      //if (nameStr==null && (name==null || name.equalsIgnoreCase(""))) name = seq.getName();
      //else {
      //  name = nameStr;
      //}
    if (name != null && name.length() > 13 && name.startsWith("ENS") &&
        (ensembl.equals("") || ensembl == null)) ensembl = name;
    if (name == null && ensembl != null && !ensembl.equalsIgnoreCase("")) name =
        ensembl;
    length = seq.length();
  }

  /**
   * This is a method to get the sequence from file.
   * @return nothing
   * @param path
   *        path on disk where file is located
   */
  /**
     public void setGbSeqFromFile(String path,String nameStr) throws Exception{
    SequenceIterator stream = null;
    BufferedReader br = null;
    br = new BufferedReader(new FileReader(path));
    stream = SeqIOTools.readGenbank(br);
    if (stream.hasNext()){
      seq = stream.nextSequence();
    }
    if (nameStr==null) name = seq.getName();
    else name = nameStr;
    length = seq.length();
     }
   **/

  public void setFastaSeqFromFile(String path, String nameStr) throws Exception {
    SequenceIterator stream = null;
    BufferedReader br = null;
    br = new BufferedReader(new FileReader(path));
    stream = SeqIOTools.readFastaDNA(br);
    if (stream.hasNext()) {
      seq = stream.nextSequence();
    }
    if (nameStr == null) name = seq.getName();
    else name = nameStr;
    length = seq.length();
    br.close();
  }

  public GeneList retrieveAllAccSeq() throws Exception {
    //System.out.println("Getting sequences from Entrez Nucleotide ");
    Gene tempGene;
    GeneList outList = new GeneList();
    //Seqs from accNr's
    ArrayList accNrList;
    String accNr;
    if (!locusLink.equalsIgnoreCase("")) {
      accNrList = Tools.getAccNrFromLocusLink(new Integer(locusLink).intValue());
      for (Iterator iter = accNrList.iterator(); iter.hasNext(); ) {
        accNr = (String) iter.next();
        //System.out.println("accNr="+accNr);
        tempGene = new Gene();
        tempGene.name = name;
        tempGene.locusLink = locusLink;
        tempGene.setSeqFromAccNr(accNr);
        tempGene.name = accNr;
        outList.add(tempGene);
        //System.out.println(".");
      }
    }
    else throw new Exception("Empty locuslink ID");
    //System.out.println("\n");
    return outList;
  }

  /**
   * make new sequences without N's and write to fasta
   * @throws Exception
   * @return GeneList
   */
  public GeneList splitForMotifScanner() throws Exception {
    if (seq == null)return null;
    PatternCompiler compiler = new Perl5Compiler();
    PatternMatcher matcher = new Perl5Matcher();
    Pattern pattern = compiler.compile("[Nn]+");
    PatternMatcherInput input;
    MatchResult result;
    int groups;
    String dnaStr = seq.seqString();
    String newStr;
    SimpleSequence newSeq;
    Alphabet dna = DNATools.getDNA();
    SymbolTokenization dnaToke = dna.getTokenization("token");
    input = new PatternMatcherInput(dnaStr);
    GeneList list = new GeneList();
    Gene g;
    int i = 0;
    int start = 1;
    while (matcher.contains(input, pattern)) {
      newStr = input.preMatch();
      //cut away possbile leading N's
      if (newStr.equalsIgnoreCase("")) {
        //nothing
      }
      else {
        g = new Gene();
        newSeq = new SimpleSequence(new SimpleSymbolList(dnaToke, newStr),
                                    "subpart", seq.getName(),
                                    new SimpleAnnotation());
        if (i > 0) newSeq.setName(seq.getName() + "@" + start);
        else newSeq.setName(seq.getName());
        g.setSequence(newSeq, null);
        list.add(g);
      }
      start = start + matcher.getMatch().endOffset(0);
      input = new PatternMatcherInput(input.postMatch());
      i++;
    }
    //add last remaining piece
    newStr = input.toString();
    if (newStr.length() > 0) {
      g = new Gene();
      newSeq = new SimpleSequence(new SimpleSymbolList(dnaToke, newStr),
                                  "subpart", seq.getName(),
                                  new SimpleAnnotation());
      if (i > 0) newSeq.setName(seq.getName() + "@" + start);
      else newSeq.setName(seq.getName());
      g.setSequence(newSeq, null);
      list.add(g);
    }
    return list;
  }

  public void trimFlankingNs() throws Exception {
    PatternCompiler compiler = new Perl5Compiler();
    PatternMatcher matcher = new Perl5Matcher();
    Pattern pattern = compiler.compile("[Nn]+");
    PatternMatcherInput input;
    MatchResult result;
    int groups;
    String dnaStr = seq.seqString();
    String newStr;
    SimpleSequence newSeq;
    Alphabet dna = DNATools.getDNA();
    SymbolTokenization dnaToke = dna.getTokenization("token");
    input = new PatternMatcherInput(dnaStr);
    int i = 0;
    int start = 1;
    if (matcher.contains(input, pattern)) {
      newStr = input.preMatch();
      if (newStr.equalsIgnoreCase("")) { //started with N's
        newSeq = new SimpleSequence(new SimpleSymbolList(dnaToke,
            input.postMatch()), "subpart", seq.getName(), new SimpleAnnotation());
        this.setSequence(newSeq, null);
      }
    }
    dnaStr = seq.seqString();
    while (matcher.contains(input, pattern)) {
      newStr = input.postMatch();
      if (newStr.equalsIgnoreCase("")) { //ended with N's
        newSeq = new SimpleSequence(new SimpleSymbolList(dnaToke,
            input.preMatch()), "subpart", seq.getName(), new SimpleAnnotation());
        this.setSequence(newSeq, null);
      }
    }
  }

  public void printFeatures(){
    Feature temp;
    for(Iterator it = seq.features();it.hasNext();){
      temp = (Feature) it.next();
      System.out.println(temp.getSource()+"|"+temp.getType()+"|"+temp.getAnnotation());
    }

  }

  public void removeObject(String objectDescriptor){
    objects.remove(objectDescriptor);
  }

  public void removeFeatures(String objectDescriptor) throws Exception{
    Feature temp;
    ArrayList v = new ArrayList();
    for(Iterator it = seq.features();it.hasNext();){
      temp = (Feature)it.next();
      if(temp.getType().equalsIgnoreCase(objectDescriptor)||temp.getSource().equalsIgnoreCase(objectDescriptor) || (temp.getAnnotation().containsProperty("id") && ((String)temp.getAnnotation().getProperty("id")).equalsIgnoreCase(objectDescriptor)) || (temp.getAnnotation().containsProperty("type") && ((String)temp.getAnnotation().getProperty("type")).equalsIgnoreCase(objectDescriptor)) ){
        //seq.removeFeature(temp);
        v.add(temp);
      }
    }
    for(int i=0;i<v.size();i++)
      seq.removeFeature((Feature)v.get(0));
  }

  public void removeNs() throws Exception {
    PatternCompiler compiler = new Perl5Compiler();
    PatternMatcher matcher = new Perl5Matcher();
    Pattern pattern = compiler.compile("[Nn]+");
    PatternMatcherInput input;
    MatchResult result;
    int groups;
    String dnaStr = seq.seqString();
    String newStr = "";
    SimpleSequence newSeq;
    Alphabet dna = DNATools.getDNA();
    SymbolTokenization dnaToke = dna.getTokenization("token");
    input = new PatternMatcherInput(dnaStr);
    GeneList list = new GeneList();
    Gene g;
    int i = 0;
    int start = 1;
    while (matcher.contains(input, pattern)) {
      newStr = newStr + input.preMatch();
      start = matcher.getMatch().endOffset(0) + 1;
      input = new PatternMatcherInput(input.postMatch());
      i++;
    }
    //add last remaining piece
    newStr = input.toString();
    //put this sequence into this gene
    newSeq = new SimpleSequence(new SimpleSymbolList(dnaToke, newStr), "no-N",
                                seq.getName(), new SimpleAnnotation());
    this.setSequence(newSeq, this.name);
  }

  /**
   * Go to the locuslink record of the gene using the locuslink id
   * Take all genomic gb accession numbers from "GenBank Sequences"
   * Check all gb entries and check length of region upstream of CDS
   * Take max length
   * @param fig Make a feature map of all genomic gb entries
   * @param embl Write to large embl file
   */
  public void setLongestAccSeq(boolean fig, boolean embl) throws Exception {
    if (locusLink.equals("") || locusLink == null || locusLink.equals("null"))throw new
        Exception(name + ": Empty locuslink ID");
    GeneList gl = retrieveAllAccSeq();
    if (gl.list.size() == 0)throw new Exception("No seq for " + name);
    Sequence seq;
    if (fig) {
      Document xml = gl.getXmlDoc();
      BufferedImage img = toucan.util.Tools.createImg("test", xml);
      toucan.util.Tools.writeImgToFile(img,
                                       "D:\\SAE\\temp\\" + name + "_allAcc.jpg",
                                       "jpg");
    }
    if (embl) gl.writeToLargeEmblFile("D:\\SAE\\temp\\" + name + "_allAcc.embl");
    int max = 0;
    Gene tg, best = this;
    for (Iterator it = gl.list.iterator(); it.hasNext(); ) {
      tg = (Gene) it.next();
      seq = tg.selectBeforeFeat("CDS", null, null, 10000, 100, 0);
      if (seq != null && seq.length() > max) {
        max = seq.length();
        best = tg;
      }
    }
    System.out.println("Got seq from NCBI (using best AccNr: " + best.name +
                       ")");
    this.setSequence(best.seq, name);
  }

  /**
   * @deprecated
   * @param dbType String
   * @param chrom int
   * @param start int
   * @param end int
   * @throws Exception
   */
  public void setChromSeqFromEnsembl(String dbType, int chrom, int start,
                                     int end) throws Exception {
    String
        ensId = this.ensembl,
        protocol = "http://",
        host = "www.ensembl.org/",
        path1 = "exportview?tab=embl&context=&ftype=gene&type=basepairs&band_end=&embl_format=embl&marker_end=&bp_end=",
        path2 = "&marker_start=&bp_start=",
        path3 = "&band_start=&id=&window_name=results&chr=",
        path4 = "&out=text&con_start=&con_end=&btnsubmit=Export";
    if (dbType.equalsIgnoreCase("Mus_musculus") ||
        dbType.equalsIgnoreCase("Mouse")) host = host + "Mus_musculus/";
    if (dbType.equalsIgnoreCase("Homo_sapiens") ||
        dbType.equalsIgnoreCase("Human")) host = host + "Homo_sapiens/";
    String url = protocol + host + path1 + end + path2 + start + path3 + chrom +
        path4;
    URL uniUrl = new URL(url);
    URLConnection con = uniUrl.openConnection();
    BufferedReader in = new BufferedReader(new InputStreamReader(con.
        getInputStream()));

    String s = in.readLine();
    //save retrieved embl sequence temporarily to disk
    String fileName = "temp.embl";
    File seqFile = new File(tempPath + fileName);
    PrintWriter pr = new PrintWriter(new FileOutputStream(seqFile), true);
    while (s != null) {
      //-------------------------
      //temporarily put this code here, it appears that the embl file contains strange things, especially for mouse...
      PatternCompiler compiler = new Perl5Compiler();
      PatternMatcher matcher = new Perl5Matcher();
      Pattern pattern = compiler.compile("(\\d+\\.*\\d+)-(\\d+\\.*\\d+):");
      PatternMatcherInput input;
      MatchResult result;
      if (matcher.contains(s, pattern)) {
        s = s.substring(0,
            matcher.getMatch().beginOffset(0)).concat(s.
            substring(matcher.getMatch().endOffset(0)));
      }
      //------------------------
      pr.println(s);
      s = in.readLine();
    }
    ;
    pr.close();
    this.setEmblSeqFromFile(tempPath + fileName, null);

    //setEmblSeqFromBuffReader(in,null);

    if (seq == null)throw new Exception("Could not set Ensembl sequence for " +
                                        name + "(" + ensembl + ")");
    else {
      name = seq.getName();
      length = seq.length();
      System.out.println(name + ": Got seq from ensembl");
    }
  }

  /**
   * @deprecated
   */
  public void setSeqFromEnsembl(String dbType, int bpAroundCDS) throws
      Exception {
    if (ensembl.equals("") || ensembl == null || ensembl.equals("null"))throw new
        Exception("Ensembl ID is empty");
    String
        ensId = this.ensembl,
        protocol = "http://",
        host = "www.ensembl.org/",
        path1 =
        "exportview?btnsubmit=Export&con_end=&type=feature&con_start=&chr=&id=",
        path2 = "&band_start=&bp_start=&context=",
        path3 = "&marker_start=&bp_end=&out=text&marker_end=&embl_format=embl&band_end=&ftype=gene&tab=embl&embl_gene=on";
    if (dbType.equalsIgnoreCase("Mus_musculus") ||
        dbType.equalsIgnoreCase("Mouse")) host = host + "Mus_musculus/";
    if (dbType.equalsIgnoreCase("Homo_sapiens") ||
        dbType.equalsIgnoreCase("Human")) host = host + "Homo_sapiens/";
    String url = protocol + host + path1 + ensId + path2 + bpAroundCDS + path3;

    //System.out.println(url);

    URL uniUrl = new URL(url);
    URLConnection con = uniUrl.openConnection();
    BufferedReader in = new BufferedReader(new InputStreamReader(con.
        getInputStream()));

    String s = in.readLine();
    //save retrieved embl sequence temporarily to disk
    //String fileName = "temp.embl";
    //File seqFile = new File(tempPath+fileName);
    //PrintWriter pr = new PrintWriter(new FileOutputStream(seqFile),true);

    //StringWriter sw = new StringWriter();
    //PrintWriter pr = new PrintWriter(sw);
    StringBuffer buf = new StringBuffer();

    while (s != null) {
      //-------------------------
      //temporarily put this code here, it appears that the embl file contains strange things, especially for mouse...
      PatternCompiler compiler = new Perl5Compiler();
      PatternMatcher matcher = new Perl5Matcher();
      //Pattern pattern = compiler.compile("(\\d\\.\\d+)-(\\d+\\.*\\d+):");
      Pattern pattern = compiler.compile("(\\d\\.\\d+)-(\\d+):");
      PatternMatcherInput input;
      MatchResult result;
      if (matcher.contains(s, pattern)) {
        s = s.substring(0,
            matcher.getMatch().beginOffset(0)).concat(s.
            substring(matcher.getMatch().endOffset(0)));
      }
      //------------------------
      //pr.println(s);
      buf.append(s);
      s = in.readLine();
    }
    ;
    //pr.close();

    //this.setEmblSeqFromFile(tempPath+fileName,null);

    setEmblSeqFromBuffReader(new BufferedReader(new StringReader(buf.toString())), null);

    if (seq == null)throw new Exception("Could not set Ensembl sequence for " +
                                        name + "(" + ensembl + ")");
    else {
      name = seq.getName();
      length = seq.length();
      //strand
      if (cdsStrandPositive("gene", name)) cdsPositive = true;
      else cdsPositive = false;
      System.out.println(name + ": Got seq from ensembl");
    }
  }

  public void setSeqFromEnsembl(org.ensembl.driver.CoreDriver driver, int bpBefore) throws
      Exception {
    System.out.println("species="+species);
    this.setSeqFromEnsembl(new EnsemblAccess(driver, species.name), bpBefore);
  }

/*  public void setSeqFromEnsembl(Properties props, int bpBefore) throws
      Exception {
    org.ensembl.driver.CoreDriver driver = org.ensembl.driver.DriverManager.load(
        props);
    this.setSeqFromEnsembl(new EnsemblAccess(driver, species.name), bpBefore);
  }*/

  public void setSeqFromEnsembl(EnsemblAccess ea, int bpBefore) throws
      Exception {
    setSequence(ea.getBiojavaSeqFromEnsembl(ensembl, null, bpBefore), null);
    if (seq == null) throw new Exception("Could not set Ensembl sequence for " +
                                        name + "(" + ensembl + ")");
    else {
      length = seq.length();
      try {
        if (cdsStrandPositive("gene", name)) cdsPositive = true;
        else cdsPositive = false;
      }
      catch (Exception ex) {
        ex.printStackTrace();
        throw ex;
      }
      System.out.println(name + ": Got seq from ensembl");
    }
  }

  /**
   * @deprecated
   * @param protocol String
   * @param host String
   * @param spec String
   * @param path1 String
   * @param path2 String
   * @param path3 String
   * @param bpAroundCDS int
   * @throws Exception
   */
  public void setSeqFromEnsembl(String protocol, String host, String spec,
                                String path1, String path2, String path3,
                                int bpAroundCDS) throws Exception {
    if (ensembl.equals("") || ensembl == null || ensembl.equals("null"))throw new
        Exception("Ensembl ID is empty");
    String ensId = this.ensembl;
    String newUrl = protocol + host + spec + "/" + path1 + ensId + path2 +
        bpAroundCDS + path3;

    URL fetch = new URL(newUrl);
    BufferedReader in = new BufferedReader(new InputStreamReader(fetch.
        openStream()));

    setEmblSeqFromBuffReader(in, ensembl);

    /*
         String s = in.readLine();
         StringBuffer buf = new StringBuffer();

         while (s!=null){
      //-------------------------
      //temporarily put this code here, it appears that the embl file contains strange things, especially for mouse...
      PatternCompiler compiler = new Perl5Compiler();
      PatternMatcher matcher  = new Perl5Matcher();
      //Pattern pattern = compiler.compile("(\\d\\.\\d+)-(\\d+\\.*\\d+):");
      Pattern pattern = compiler.compile("(\\d.\\d+-\\d+:)");
      PatternMatcherInput input;
      MatchResult result;
      if (matcher.contains(s,pattern)){
        s = s.substring(0,matcher.getMatch().beginOffset(0)).concat(s.substring(matcher.getMatch().endOffset(0)));
      }
      buf.append(s);
      buf.append("\n");
      s = in.readLine();
         }
         System.out.println(buf.toString());
         setEmblSeqFromBuffReader(new BufferedReader(new StringReader(buf.toString())),null);

     */

    if (seq == null)throw new Exception("Could not set Ensembl sequence for " +
                                        name + "(" + ensembl + ")");
    else {

      //21/11/2002: remove this line since ID is not Chromosome... etc
      //
      //name = seq.getName();

      length = seq.length();
      //strand
      try {
        if (cdsStrandPositive("gene", name)) cdsPositive = true;
        else cdsPositive = false;
      }
      catch (Exception ex) {
        throw ex;
      }
      System.out.println(name + ": Got seq from ensembl");
    }
  }

  public void setSeqFromEmbl() throws Exception { //uses the gene's accession number
    if (accNr.equals("") || accNr == null || accNr.equals("null"))throw new
        Exception("AccNr is empty");
    String protocol = "http://";
    String host = "www.ebi.ac.uk/";
    String path1 = "cgi-bin/dbfetch?id=";
    String path2 = "&format=embl&style=raw";
    String url = protocol + host + path1 + accNr + path2;
    System.out.println(url);
    URL uniUrl = new URL(url);
    URLConnection con = uniUrl.openConnection();
    BufferedReader in = new BufferedReader(new InputStreamReader(con.
        getInputStream()));
    setEmblSeqFromBuffReader(in, null);
    if (seq != null) {
      name = seq.getName();
      length = seq.length();
      System.out.println(name + ": Got seq from embl (" + accNr + ")");
    }
    else throw new Exception("Could not find (" + accNr + ")");
  }

  public void setSeqFromAccNr(String accNr) throws Exception {
    if (accNr == null || accNr.equals(""))throw new Exception("Empty accNr");
    String
        protocol = "http://",
        host = "www.ncbi.nlm.nih.gov/",
        path1 = "entrez/query.fcgi?db=nucleotide&cmd=search&term=";
    String url = protocol + host + path1 + accNr;
    URL uniUrl = new URL(url);
    URLConnection con = uniUrl.openConnection();
    BufferedReader in = new BufferedReader(new InputStreamReader(con.
        getInputStream()));
    String s = in.readLine();
    String fileName = "temp.embl";
    File seqFile = new File(tempPath + fileName);
    PrintWriter pr = new PrintWriter(new FileOutputStream(seqFile), true);

    //find gi
    String gi = "";
    int ind = 0;
    while (s != null) {
      ind = s.indexOf("<a href=\"http://www.ncbi.nlm.nih.gov:80/entrez/query.fcgi?cmd=Retrieve&amp;db=nucleotide&amp;list_uids=");
      if (ind != -1) {
        s = s.substring(ind + 103);
        gi = s.substring(0, s.indexOf("&amp;dopt=GenBank"));
        //System.out.println("gi="+gi);
        break;
      }
      s = in.readLine();
    }

    //retrieve seq using gi
    path1 = "entrez/query.fcgi?db=Nucleotide&cmd=text&dopt=GenBank&uid=";
    url = protocol + host + path1 + gi;
    uniUrl = new URL(url);
    con = uniUrl.openConnection();
    in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    s = in.readLine();
    while (s != null) {
      pr.println(s);
      s = in.readLine();
    }
    pr.close();
    this.setGbSeqFromFile(tempPath + fileName, null);
    name = seq.getName();
    length = seq.length();

    //http://www.ncbi.nlm.nih.gov/entrez/utils/pmfetch.fcgi?db=Nucleotide&id=7510&report=gen&mode=text
    //http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?db=nucleotide&cmd=search&term=X06833
    //<a href="http://www.ncbi.nlm.nih.gov:80/entrez/query.fcgi?cmd=Retrieve&amp;db=nucleotide&amp;list_uids=
  }

  public GeneList getSubSequences(String featStr, String annKey,
                                  String annValue, String partType,
                                  int bpBefore, int bpAfter, boolean revComplIfMinusStrand) throws Exception {
    GeneList ret = new GeneList();
    //
    FeatureFilter featFilter = new FeatureFilter.ByType(featStr);
    FeatureFilter featFilter2;
    FeatureFilter theFilter;
    if (annKey != null && annValue != null) {
      featFilter2 = new FeatureFilter.ByAnnotation(annKey, annValue);
      theFilter = new FeatureFilter.And(featFilter, featFilter2);
    }
    else theFilter = featFilter;
    FeatureHolder fh = seq.filter(theFilter, false);
    Gene g = null;
    int start = 0, end = 0;
    //System.out.println("Number of features to select = "+fh.countFeatures());
    for (Iterator it = fh.features(); it.hasNext(); ) {
      g = new Gene();
      //I changed Feature into StrandedFeature... see if it always works...
      Object o = it.next();
      try{
      StrandedFeature f = (StrandedFeature) o;
      Location loc = f.getLocation();
      if (partType.toLowerCase().startsWith("around")) {
    	  	//System.out.println("location:"+f.getLocation().getMin()+"-"+f.getLocation().getMax());
        start = f.getLocation().getMin() - bpBefore;
        end = f.getLocation().getMax() + bpAfter;
      }
      else if (partType.toLowerCase().startsWith("left")) {
        start = f.getLocation().getMin() - bpBefore;
        end = f.getLocation().getMin() + bpAfter;
      }
      else if (partType.toLowerCase().startsWith("right")) {
        start = f.getLocation().getMax() - bpBefore;
        end = f.getLocation().getMax() + bpAfter;
      }
      //it appears that this is needed
      //start++;
      //end++;
      g.setSequence(this.makeMySubSeq(start, end,
                                      name + "_" + start + "_" + end),
                    name + "_" + start + "_" + end);
      if(revComplIfMinusStrand && f.getStrand() ==StrandedFeature.NEGATIVE)
        g.reverseComplement();
      }
      //for non-stranded features
      catch(Exception e){
      	Feature f = (Feature) o;
        Location loc = f.getLocation();
        if (partType.toLowerCase().startsWith("around")) {
          start = f.getLocation().getMin() - bpBefore;
          end = f.getLocation().getMax() + bpAfter;
        }
        else if (partType.toLowerCase().startsWith("left")) {
          start = f.getLocation().getMin() - bpBefore;
          end = f.getLocation().getMin() + bpAfter;
        }
        else if (partType.toLowerCase().startsWith("right")) {
          start = f.getLocation().getMax() - bpBefore;
          end = f.getLocation().getMax() + bpAfter;
        }
        g.setSequence(this.makeMySubSeq(start, end,
                                        name + "_" + start + "_" + end),
                      name + "_" + start + "_" + end);
      }
      ret.add(g);
    }
    return ret;
  }
  
public void maskSubSequences(String featStr, String annKey,
	        String annValue, String partType,
	        int bpBefore, int bpAfter) throws Exception {
	
	FeatureFilter featFilter = new FeatureFilter.ByType(featStr);
	FeatureFilter featFilter2;
	FeatureFilter theFilter;
	if (annKey != null && annValue != null) {
	featFilter2 = new FeatureFilter.ByAnnotation(annKey, annValue);
	theFilter = new FeatureFilter.And(featFilter, featFilter2);
	}
	else theFilter = featFilter;
	FeatureHolder fh = seq.filter(theFilter, false);
	Gene g = null;
	int start = 0, end = 0;
	//System.out.println("Number of features to select = "+fh.countFeatures());
	for (Iterator it = fh.features(); it.hasNext(); ) {
		g = new Gene();
		//I changed Feature into StrandedFeature... see if it always works...
		Object o = it.next();
		
		Feature f = (Feature) o;
		Location loc = f.getLocation();
		if (partType.toLowerCase().startsWith("around")) {
		start = f.getLocation().getMin() - bpBefore;
		end = f.getLocation().getMax() + bpAfter;
		}
		else if (partType.toLowerCase().startsWith("left")) {
		start = f.getLocation().getMin() - bpBefore;
		end = f.getLocation().getMin() + bpAfter;
		}
		else if (partType.toLowerCase().startsWith("right")) {
		start = f.getLocation().getMax() - bpBefore;
		end = f.getLocation().getMax() + bpAfter;
		}
		this.maskFromTo(start,end);
	}
}

  /**
   * @param organism  "mouse" or "human"
   */
  public void setSeqFromNcbi(String organism) throws Exception {
    String part1 = "http://www.ncbi.nlm.nih.gov/LocusLink/LocRpt.cgi?l=";
    String mapUrl = "";
    int index = -1, index2 = -1;
    String url = part1 + locusLink;
    URL uniUrl = new URL(url);
    URLConnection con = uniUrl.openConnection();
    BufferedReader in = new BufferedReader(new InputStreamReader(con.
        getInputStream()));
    String s = in.readLine();
    while (s != null) {
      if (organism.equalsIgnoreCase("mouse")) index = s.indexOf(
          "http://www.ncbi.nlm.nih.gov/cgi-bin/Entrez/mouse_mapv.cgi?");
      if (organism.equalsIgnoreCase("human")) index = s.indexOf(
          "http://www.ncbi.nlm.nih.gov/cgi-bin/Entrez/maps.cgi?");
      if (index != -1) {
        s = s.substring(index);
        index2 = s.indexOf("onMouseOver");
        if (index2 != -1) {
          mapUrl = s.substring(0, index2 - 2);
          String nameStr = mapUrl.substring(mapUrl.indexOf("query=") + 6);
          name = nameStr;
          break;
        }
      }
      s = in.readLine();
    }
    if (mapUrl.equalsIgnoreCase(""))throw new Exception(
        "Nothing found in locusLink for this ID");

    String downUrl = "";
    uniUrl = new URL(mapUrl);
    con = uniUrl.openConnection();
    in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    s = in.readLine();
    while (s != null) {
      index = s.indexOf("Download/View");
      if (index != -1) {
        s = s.substring(0, index - 2);
        index2 = s.indexOf("bp");
        if (index2 != -1) {
          downUrl = s.substring(index2 + 27);
          downUrl = "http://www.ncbi.nlm.nih.gov/cgi-bin/Entrez/" + downUrl;
          break;
        }
      }
      s = in.readLine();
    }
    if (downUrl.equalsIgnoreCase(""))throw new Exception(
        "No contig could be selected");
    //
    String saveUrl = "";
    try {
      uniUrl = new URL(downUrl);
      con = uniUrl.openConnection();
    }
    catch (MalformedURLException me) {
      me.printStackTrace();
    }
    in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    s = in.readLine();
    while (s != null) {
      index = s.indexOf("Save to Disk");
      if (index != -1) {
        s = s.substring(0, index - 37);
        index2 = s.indexOf("href");
        if (index2 != -1) {
          saveUrl = s.substring(index2 + 6);
          saveUrl = "http://www.ncbi.nlm.nih.gov" + saveUrl + "&txt=on&view=gb";
          break;
        }
      }
      s = in.readLine();
    }
    if (saveUrl.equalsIgnoreCase(""))throw new Exception("No download possible");
    //
    String fileName = this.tempPath + "temp.gb";
    uniUrl = new URL(saveUrl);
    con = uniUrl.openConnection();
    in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    File form = new File(fileName);
    PrintWriter pr = new PrintWriter(new FileOutputStream(form), true);
    s = in.readLine();
    while (s != null) {
      index = s.indexOf("<");
      while (index != -1) {
        s = s.substring(0, index) + s.substring(index + 1);
        index = s.indexOf("<");
      }
      pr.println(s);
      s = in.readLine();
    }
    ;
    pr.close();
    setGbSeqFromFile(fileName, name);
    //name = seq.getName(); -> name was already set based on gene name from locuslink
    length = seq.length();
  }

  /**
   * @deprecated
   * @throws Exception
   */
  public void setEnsIdFromLocuslink() throws Exception {
    Connection kaka = null;
    String dbURL2 =
        "jdbc:mysql://ensembldb.ensembl.org/current?user=anonymous&password=";
    Statement s = null, s2 = null;
    ResultSet rs = null, rs2 = null, rs3 = null;
    Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    kaka = DriverManager.getConnection(dbURL2);
    s = kaka.createStatement();
    s2 = kaka.createStatement();
    rs = s.executeQuery("select o.ensembl_id,o.ensembl_object_type from objectXref o,Xref x where x.externalDBId = 10 and x.dbprimary_id = " +
                        this.locusLink + " and o.xrefId = x.xrefId");
    if (rs.next()) {
      rs2 = s2.executeQuery("SELECT g.stable_id FROM transcript t, gene_stable_id g where t.gene_id=g.gene_id and t.translation_id=" +
                            rs.getString("ensembl_id"));
      if (rs2.next()) {
        this.ensembl = rs2.getString(1);
        System.out.println("Got Ensembl id: " + this.ensembl);
      }
    }
    kaka.close();
  }

  /**
   * @deprecated
   * @param tbl_name String
   * @throws Exception
   */
  public void setMouseOrtholog(String tbl_name) throws Exception {
    Connection kaka = null;
    String dbURL2 = "jdbc:mysql://ensembldb.ensembl.org/" + tbl_name +
        "?user=anonymous&password=";
    Statement s = null;
    ResultSet rs = null;
    Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    kaka = DriverManager.getConnection(dbURL2);
    s = kaka.createStatement();
    rs = s.executeQuery("select m.gene_stable_id from gene g,gene_mouse m where g.gene_id=m.gene_id and g.gene_stable_id='" +
                        this.ensembl + "'");
    if (rs.next()) {
      this.ensOrtho = rs.getString(1);
    }
    kaka.close();
  }

  /**
   * @param dbURL for example "jdbc:mysql://ensembldb.ensembl.org/mus_musculus_core_8_3c?user=anonymous&password="
   * @param homolSpecies for example "human"
   * @return the member "ensOrtho" will be set to the Ensembl stable_gene_id of the orthologous gene
   */
  public String getEnsemblOrtholog(String dbURL, String homolSpecies) throws
      Exception {
    String toRet=null;
    Connection kaka = null;
    Statement s = null;
    ResultSet rs = null;
    Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    kaka = DriverManager.getConnection(dbURL);
    s = kaka.createStatement();
    String str = "select h.homol_stable_id from " +
        species.shortName.toLowerCase() + "_gene_ensembl__homologs_" +
        homolSpecies + "__dm h where h.gene_stable_id='" + this.ensembl + "'";
    rs = s.executeQuery(str);
    if (rs.next()) {
      toRet = rs.getString("homol_stable_id");
    }
    kaka.close();
    return toRet;
  }

  
  
  public Object[] getEnsemblOrthologsMartView(String martServiceURL, String homolSpecies) throws
  Exception {
	  String query = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE Query><Query  virtualSchemaName = \"default\" header = \"0\" count = \"\" softwareVersion = \"0.5\" >";
	  query+="<Dataset name = \""+species.shortName+"_gene_ensembl\" interface = \"default\" >";
	  query+="<Attribute name = \""+homolSpecies+"_ensembl_gene\" />";
	  query+="<Filter name = \"ensembl_gene_id\" value = \""+this.ensembl+"\"/>";
      query+="</Dataset></Query>";
      System.out.println(query);
      String tab = Tools.post2Mart(query,martServiceURL);
      System.out.println(tab);
      String[][] res = Tools.stringToArray2D(tab);      
      ArrayList temp = new ArrayList();
      for(int i=0;i<res.length;i++){
          if(!res[i][0].contains("ERROR")) temp.add(res[i][0]);
      }
	  return temp.toArray();
  }
  
  
  /**
   * @param dbURL for example "jdbc:mysql://ensembldb.ensembl.org/mus_musculus_core_8_3c?user=anonymous&password="
   * @param homolSpecies for example "human"
   * @return the member "ensOrtho" will be set to the Ensembl stable_gene_id of the orthologous gene
   */
  public Object[] getEnsemblOrthologs(String dbURL, String homolSpecies) throws
      Exception {
    String[] toRet=null;
    Connection kaka = null;
    Statement s = null;
    ResultSet rs = null;
    Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    kaka = DriverManager.getConnection(dbURL);
    s = kaka.createStatement();
    String str = "select h.homol_stable_id from " +
        species.shortName.toLowerCase() + "_gene_ensembl__homologs_" +
        homolSpecies + "__dm h where h.gene_stable_id='" + this.ensembl + "'";
    rs = s.executeQuery(str);
    ArrayList temp = new ArrayList();
    while (rs.next()) {
      //toRet = rs.getString("homol_stable_id");
      temp.add(rs.getString("homol_stable_id"));
    }
    kaka.close();
    return temp.toArray();
  }


  /**
   * @deprecated
   * @param tbl_name String
   * @throws Exception
   */
  public void setHumanOrtholog(String tbl_name) throws Exception {
    Connection kaka = null;
    String dbURL2 = "jdbc:mysql://ensembldb.ensembl.org/" + tbl_name +
        "?user=anonymous&password=";
    Statement s = null;
    ResultSet rs = null;
    Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    kaka = DriverManager.getConnection(dbURL2);
    s = kaka.createStatement();
    rs = s.executeQuery("select m.gene_stable_id from gene g,gene_human m where g.gene_id=m.gene_id and g.gene_stable_id='" +
                        this.ensembl + "'");
    if (rs.next()) {
      this.ensOrtho = rs.getString(1);
    }
    kaka.close();
  }

  /**
   * @deprecated
   * @throws Exception
   */
  public void setEnsIdFromAccNr() throws Exception {
    Connection kaka = null;
    String dbURL2 =
        "jdbc:mysql://ensembldb.ensembl.org/current?user=anonymous&password=";
    Statement s = null, s2 = null;
    ResultSet rs = null, rs2 = null, rs3 = null;
    Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    kaka = DriverManager.getConnection(dbURL2);
    s = kaka.createStatement();
    s2 = kaka.createStatement();
    rs = s.executeQuery("select o.ensembl_id,o.ensembl_object_type from objectXref o,Xref x where x.externalDBId = 2 and x.dbprimary_id = '" +
                        this.accNr + "' and o.xrefId = x.xrefId");
    if (rs.next()) {
      rs2 = s2.executeQuery("SELECT g.stable_id FROM transcript t, gene_stable_id g where t.gene_id=g.gene_id and t.translation_id=" +
                            rs.getString("ensembl_id"));
      if (rs2.next()) {
        this.ensembl = rs2.getString(1);
      }
    }
    kaka.close();
  }

  /**
 * @deprecated
 * @throws Exception
 */
  public void setEnsIdFromName() throws Exception {
    Connection kaka = null;
    String dbURL2 =
        "jdbc:mysql://ensembldb.ensembl.org/current?user=anonymous&password=";
    Statement s = null, s2 = null;
    ResultSet rs = null, rs2 = null, rs3 = null;
    Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    kaka = DriverManager.getConnection(dbURL2);
    s = kaka.createStatement();
    s2 = kaka.createStatement();
    rs = s.executeQuery("select o.ensembl_id,o.ensembl_object_type from objectXref o,Xref x where x.externalDBId = 5 and x.display_id = '" +
                        this.name + "' and o.xrefId = x.xrefId");
    if (rs.next()) {
      rs2 = s2.executeQuery("SELECT g.stable_id FROM transcript t, gene_stable_id g where t.gene_id=g.gene_id and t.translation_id=" +
                            rs.getString("ensembl_id"));
      if (rs2.next()) {
        this.ensembl = rs2.getString(1);
      }
    }
    System.out.println("Ensembl id for " + this.name + " is " + this.ensembl);
        kaka.close();
  }

  /**
 * @deprecated
 * @throws Exception
 */
public void setEnsIdFromHugo() throws Exception {
    Connection kaka = null;
    String dbURL2 =
        "jdbc:mysql://ensembldb.ensembl.org/current?user=anonymous&password=";
    Statement s = null, s2 = null;
    ResultSet rs = null, rs2 = null, rs3 = null;
    Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    kaka = DriverManager.getConnection(dbURL2);
    s = kaka.createStatement();
    s2 = kaka.createStatement();
    rs = s.executeQuery("select o.ensembl_id,o.ensembl_object_type from objectXref o,Xref x where x.externalDBId = 5 and x.display_id = '" +
                        this.hugo + "' and o.xrefId = x.xrefId");
    if (rs.next()) {
      rs2 = s2.executeQuery("SELECT g.stable_id FROM transcript t, gene_stable_id g where t.gene_id=g.gene_id and t.translation_id=" +
                            rs.getString("ensembl_id"));
      if (rs2.next()) {
        this.ensembl = rs2.getString(1);
      }
    }
    System.out.println("Ensembl id for " + this.hugo + " is " + this.ensembl);
    kaka.close();
  }

  public Properties updateInfoProps(String mysql, String userName,
                                    String passwd) throws Exception {
    if (species == null) throw new Exception("Species not known");
    if (infoProps == null) infoProps = new Properties();
    String dbURL = "jdbc:mysql://" + mysql + "/" + GlobalProperties.getEnsemblMart() + "?user=" +
        userName + "&password=" + passwd;
    if (species.core.indexOf("vega")!=-1) {
      dbURL = "jdbc:mysql://" + mysql + "/" + GlobalProperties.getVegaMart() + "?user=" +
        userName + "&password=" + passwd;
    }
    //description
    this.setDescrFromEnsId(dbURL);
    //externalDB values
    dbURL = "jdbc:mysql://" + mysql + "/" + species.core + "?user=" +
        userName + "&password=" + passwd;
    for (int i = 0; i < species.externalIds.size(); i++) {
      String key = (String) species.externalNames.get(i);
      int id = ( (Integer) species.externalIds.get(i)).intValue();
      String value = retrieveExternalDbValue(id, dbURL);
      infoProps.setProperty(key, value);
      System.out.println("(" + id + ") " + key + "= " + value);
    }
    infoProps.setProperty("Description", this.desc);
    infoProps.setProperty("Ensembl", ensembl);
    return infoProps;
  }

  /**
 * @deprecated
 * @throws Exception
 */
public GeneList glFromExternalDbId(String dbURL) throws Exception {
    GeneList gl = new GeneList();
    Gene g;
    if (externalDb.equalsIgnoreCase("ensembl")) {
      ensembl = externalDbEntry;
      return null;
    }
    Connection kaka = null;
    Statement s = null, s2 = null;
    ResultSet rs = null, rs2 = null, rs3 = null;
    Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    kaka = DriverManager.getConnection(dbURL);
    s = kaka.createStatement();
    s2 = kaka.createStatement();
    String sql = "select o.ensembl_id,o.ensembl_object_type from object_xref o,xref x where x.external_db_id = " +
        externalDbId + " and (x.display_label = '" + externalDbEntry +
        "' or x.dbprimary_acc = '" + externalDbEntry +
        "') and o.xref_id = x.xref_id";
    rs = s.executeQuery(sql);
    System.out.println("Ensembl ID's for " + this.externalDbEntry + " are: ");
    while (rs.next()) {
      sql = "SELECT g.stable_id FROM transcript t, gene_stable_id g where t.gene_id=g.gene_id and t.translation_id=" +
          rs.getString("ensembl_id");
      rs2 = s2.executeQuery(sql);
      while (rs2.next()) {
        //System.out.println("cloning...");
        //g = (Gene)this.clone();
        g = new Gene();
        g.externalDbEntry = this.externalDbEntry;
        g.externalDbId = this.externalDbId;
        g.ensembl = rs2.getString(1);
        System.out.println(g.ensembl + ",");
        gl.add(g);
      }
    }
    kaka.close();
    System.out.println("Total of " + gl.list.size() + " found");
    return gl;
  }

  public Object clone() {
    try {
      return super.clone();
    }
    catch (CloneNotSupportedException e) {
      throw new InternalError("Problem with Cloneable");
    }
  }

  /**
 * @deprecated
 * @throws Exception
 */
public void setEnsIdFromExternalDbId(String dbURL) throws Exception {
    if (externalDb.equalsIgnoreCase("ensembl")) {
      ensembl = externalDbEntry;
      return;
    }
    Connection kaka = null;
    Statement s = null, s2 = null;
    ResultSet rs = null, rs2 = null, rs3 = null;
    Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    kaka = DriverManager.getConnection(dbURL);
    s = kaka.createStatement();
    s2 = kaka.createStatement();
    rs = s.executeQuery("select o.ensembl_id,o.ensembl_object_type from object_xref o,xref x where x.external_db_id = " +
                        externalDbId + " and (x.display_label = '" +
                        externalDbEntry + "' or x.dbprimary_acc = '" +
                        externalDbEntry + "') and o.xref_id = x.xref_id");
    if (rs.next()) {
      rs2 = s2.executeQuery("SELECT g.stable_id FROM transcript t, gene_stable_id g where t.gene_id=g.gene_id and t.translation_id=" +
                            rs.getString("ensembl_id"));
      if (rs2.next()) {
        this.ensembl = rs2.getString(1);
      }
    }
    System.out.println("Ensembl id for " + externalDbEntry + " is " +
                       this.ensembl);
    kaka.close();
  }

public String retrieveExternalDbValue(int externalId, String dbURL) throws
      Exception {
    String temp = "";
    Connection kaka = null;
    Statement s = null, s2 = null;
    ResultSet rs = null, rs2 = null, rs3 = null;
    Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    kaka = DriverManager.getConnection(dbURL);
    s = kaka.createStatement();
    s2 = kaka.createStatement();
    rs = s.executeQuery("SELECT l.translation_id FROM transcript t, translation l,gene_stable_id g where t.transcript_id=l.transcript_id and t.gene_id=g.gene_id and g.stable_id='" +
                        this.ensembl + "'");
    while (rs.next()) {
      rs2 = s2.executeQuery(
          "select x.display_label from object_xref o,xref x where x.external_db_id = " +
          externalId + " and o.ensembl_id=" + rs.getString("translation_id") +
          " and o.xref_id = x.xref_id");
      while (rs2.next()) {
        temp = temp + rs2.getString(1) + "|";
      }
    }
    kaka.close();
    if (temp.length() > 0)return temp.substring(0, temp.length() - 1);
    else return temp;
  }

  /**
 * @deprecated
 * @throws Exception
 */
public void setLocuslinkFromEnsId() throws Exception {
    Connection kaka = null;
    String dbURL2 =
        "jdbc:mysql://ensembldb.ensembl.org/current?user=anonymous&password=";
    Statement s = null, s2 = null;
    ResultSet rs = null, rs2 = null, rs3 = null;
    Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    kaka = DriverManager.getConnection(dbURL2);
    s = kaka.createStatement();
    s2 = kaka.createStatement();
    rs = s.executeQuery("SELECT t.translation_id FROM transcript t, gene_stable_id g where t.gene_id=g.gene_id and g.stable_id='" +
                        this.ensembl + "'");
    if (rs.next()) {
      rs2 = s2.executeQuery("select x.dbprimary_id from objectXref o,Xref x where x.externalDBId = 10 and o.ensembl_id=" +
                            rs.getString("translation_id") +
                            " and o.xrefId = x.xrefId");
      if (rs2.next()) {
        this.locusLink = rs2.getString(1);
      }
    }
    System.out.println("Locuslink id for " + this.name + " is " +
                       this.locusLink);
    kaka.close();
  }

  /**
 * @deprecated
 * @throws Exception
 */
public void setHugoFromEnsId() throws Exception {
    Connection kaka = null;
    String dbURL2 =
        "jdbc:mysql://ensembldb.ensembl.org/current?user=anonymous&password=";
    Statement s = null, s2 = null;
    ResultSet rs = null, rs2 = null, rs3 = null;
    Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    kaka = DriverManager.getConnection(dbURL2);
    s = kaka.createStatement();
    s2 = kaka.createStatement();
    rs = s.executeQuery("SELECT t.translation_id FROM transcript t, gene_stable_id g where t.gene_id=g.gene_id and g.stable_id='" +
                        this.ensembl + "'");
    if (rs.next()) {
      rs2 = s2.executeQuery("select x.display_id from objectXref o,Xref x where x.externalDBId = 5 and o.ensembl_id=" +
                            rs.getString("translation_id") +
                            " and o.xrefId = x.xrefId");
      if (rs2.next()) {
        this.hugo = rs2.getString(1);
      }
    }
    System.out.println("HUGO for " + this.name + " is " + this.hugo);
    kaka.close();
  }

  /**
 * @deprecated
 * @throws Exception
 */
public void setAccNrFromEnsId() throws Exception {
    Connection kaka = null;
    String dbURL2 =
        "jdbc:mysql://ensembldb.ensembl.org/current?user=anonymous&password=";
    Statement s = null, s2 = null;
    ResultSet rs = null, rs2 = null, rs3 = null;
    Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    kaka = DriverManager.getConnection(dbURL2);
    s = kaka.createStatement();
    s2 = kaka.createStatement();
    rs = s.executeQuery("SELECT t.translation_id FROM transcript t, gene_stable_id g where t.gene_id=g.gene_id and g.stable_id='" +
                        this.ensembl + "'");
    if (rs.next()) {
      rs2 = s2.executeQuery("select x.dbprimary_id from objectXref o,Xref x where x.externalDBId = 2 and o.ensembl_id=" +
                            rs.getString("translation_id") +
                            " and o.xrefId = x.xrefId");
      accNr = "";
      while (rs2.next()) {
        accNr = accNr + rs2.getString(1) + "|";
      }
    }
    System.out.println("Accession numbers for " + this.name + " are " +
                       this.accNr);
    kaka.close();
  }

  /**
 * @deprecated
 */
public void setDescrFromEnsId() {
  }

  //on the ensmart database!
public void setDescrFromEnsId(String dbURL) throws Exception {
    Connection kaka = null;
    Statement s = null;
    ResultSet rs = null;
    Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    kaka = DriverManager.getConnection(dbURL);
    s = kaka.createStatement();
    /*rs = s.executeQuery("select description from gene_description d, gene_stable_id g where g.gene_id=d.gene_id and g.stable_id='" +
                        this.ensembl + "'");*/
    String query = "select description from " + this.species.shortName + "_gene_ensembl__gene__main where gene_stable_id='" +
                        this.ensembl + "'";
    if (species.core.indexOf("vega")!=-1) {
      query = "select description from " + this.species.shortName + "_gene_vega__gene__main where gene_stable_id='" +
                          this.ensembl + "'";
    }
    //System.out.println(query);
    rs = s.executeQuery(query);
    if (rs.next()) {
      desc = rs.getString(1);
    }
    if (desc==null) {
      desc = "";
    }
    kaka.close();
  }

  /**
   * @deprecated
   */
public void setEnsIdFromAcc() throws Exception {
    if (accNr == null || accNr.equals(""))throw new Exception(
        "accNr is not set");
    String
        protocol = "http://",
        host = "www.ensembl.org/Homo_sapiens/",
        path1 = "textview?num=20&fmt=c&idx=All&q=",
        path2 = "&search.x=44&search.y=14",
        url = protocol + host + path1 + accNr + path2,
        ensId = "";
    URL uniUrl = new URL(url);
    URLConnection con = uniUrl.openConnection();
    BufferedReader in = new BufferedReader(new InputStreamReader(con.
        getInputStream()));
    String s = in.readLine();
    while (s != null) {
      if (s.indexOf("<A HREF=\"/perl/geneview?gene=") != -1) {
        int pos = s.indexOf("geneview");
        ensId = s.substring(pos + 14, pos + 29);
      }
      s = in.readLine();
    }
    if (ensId.equals(""))throw new Exception(
        "No ensembl id found for this AccNr");
    else ensembl = ensId;
  }

  public void setTranscriptDiff(String dbName) throws Exception { //difference in bp between transcript start and coding start
    Connection kaka = null;
    String dbURL = "jdbc:mysql://ensembldb.ensembl.org/" + dbName +
        "?user=anonymous&password=";
    Statement s = null;
    ResultSet rs = null;
    Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    kaka = DriverManager.getConnection(dbURL);
    s = kaka.createStatement();
    rs = s.executeQuery("select min(abs(coding_start - transcript_chrom_start)), min(abs(transcript_chrom_end - coding_end)), avg(transcript_chrom_strand) from gene_structure where gene_stable_id='" +
                        this.ensembl + "'");
    if (rs.next()) {
      //System.out.println(rs.getString(3));
      if (rs.getString(3) != null && rs.getString(3).startsWith("1"))this.
          transcriptDiff = new Integer(rs.getString(1)).intValue();
      else if (rs.getString(3) != null && rs.getString(3).startsWith("-1"))this.
          transcriptDiff = new Integer(rs.getString(2)).intValue();
      else throw new Exception("Transcript strand could not be found");
    }

    kaka.close();
  }

  public double GCContent(SymbolList seq) {
    int gc = 0;
    for (int pos = 1; pos <= seq.length(); ++pos) {
      org.biojava.bio.symbol.Symbol sym = seq.symbolAt(pos);
      if (sym == org.biojava.bio.seq.DNATools.g() ||
          sym == org.biojava.bio.seq.DNATools.c())
        ++gc;
    }
    double content = (gc * 100) / seq.length();
    return content;
  }

  public double ATContent(SymbolList seq) {
    int at = 0;
    for (int pos = 1; pos <= seq.length(); ++pos) {
      org.biojava.bio.symbol.Symbol sym = seq.symbolAt(pos);
      if (sym == org.biojava.bio.seq.DNATools.a() ||
          sym == org.biojava.bio.seq.DNATools.t())
        ++at;
    }
    double content = (at * 100) / seq.length();
    return content;
  }

  public void annotateCpG() throws Exception {
    //put windowLength hardcoded because nrCgrandom depends on windowLenght
    double nrCgRandom = 28d; // 2,5% x 200bp window x 2 strands = 10 but that is NOT stringent enough
    int windowLength = 200;
    int step = 100;
    String motif = "cg";
    //
    PatternMatcher matcher;
    PatternCompiler compiler;
    Pattern pattern = null;
    PatternMatcherInput input;
    MatchResult result;
    compiler = new Perl5Compiler();
    matcher = new Perl5Matcher();
    pattern = compiler.compile(motif);
    String dnaStr;
    int nrOfOcc = 0;
    int nrConsec = 0;
    double gcContent = 0d;
    double cgValue = 0d;
    int index = 1;
    SymbolList window = null;
    int i = 1;
    while (index < seq.length() - windowLength) {
      window = seq.subList(index, index + windowLength);
      //direct
      dnaStr = window.seqString();
      input = new PatternMatcherInput(dnaStr);
      while (matcher.contains(input, pattern)) {
        nrOfOcc++;
      }
      //reverse complement
      window = DNATools.reverseComplement(window);
      dnaStr = window.seqString();
      input = new PatternMatcherInput(dnaStr);
      while (matcher.contains(input, pattern)) {
        nrOfOcc++;
      }
      gcContent = GCContent(window);
      cgValue = (nrCgRandom / nrOfOcc);
      if (cgValue < 0.6d && gcContent > 50.0d) {
        nrConsec++;
      }
      else if (nrConsec > 0) {
        int start = (index - (nrConsec) * step);
        int stop = index + windowLength;
        nrConsec = 0;
        Feature.Template template = new Feature.Template();
        template.type = "misc_feature";
        template.location = new RangeLocation(start, stop);
        template.annotation = new SimpleAnnotation();
        template.annotation.setProperty("type", "CpG Island");
        seq.createFeature(template);
      }
      nrOfOcc = 0;
      index = index + step;
    }
  }

  public void annotateCGdinuclHuman() throws Exception {
    //put windowLength hardcoded because nrCgrandom depends on windowLenght
    double nrCG_GC_CC_GG_bg = 50d; //20% x 200bp window = 40 but not stringent enough!
    int windowLength = 200;
    int step = 100;
    String motif = "";
    String motif1 = "cg";
    String motif2 = "gc";
    String motif3 = "cc";
    String motif4 = "gg";
    String[] motifs = {
        motif1, motif2, motif3, motif4};
    //
    PatternMatcher matcher;
    PatternCompiler compiler;
    Pattern pattern = null;
    PatternMatcherInput input;
    MatchResult result;
    compiler = new Perl5Compiler();
    matcher = new Perl5Matcher();
    int nrOfOcc = 0;
    int nrConsec = 0;
    double gcContent = 0d;
    double cgValue = 0d;
    SymbolList window = null;
    String dnaStr;
    int index = 1;
    int i = 1;
    while (index < seq.length() - windowLength) {
      window = seq.subList(index, index + windowLength);
      //direct
      dnaStr = window.seqString();

      for (int m = 0; m < 4; m++) {
        input = new PatternMatcherInput(dnaStr);
        motif = motifs[m];
        //System.out.println("motif="+motif);
        pattern = compiler.compile(motif);
        while (matcher.contains(input, pattern)) {
          nrOfOcc++;
        }
      }
      //reverse complement
      //window = DNATools.reverseComplement(window);
      //dnaStr = window.seqString();
      //input = new PatternMatcherInput(dnaStr);
      //while (matcher.contains(input, pattern)) {
      //  nrOfOcc++;
      //}
      gcContent = GCContent(window);
      cgValue = (nrCG_GC_CC_GG_bg / nrOfOcc);

      //System.out.println("Nrofocc = "+nrOfOcc);
      //System.out.println("   GCcontent = " + gcContent);
      //System.out.println("   pos=" + index);
      //System.out.println("   nrConsec=" + nrConsec);
      //System.out.println("   Seq = " + dnaStr);
      //System.out.println("");

      if (cgValue < 0.6d && gcContent > 50.0d) {
        nrConsec++;
      }
      else if (nrConsec > 0) {
        int start = (index - (nrConsec) * step);
        int stop = index + windowLength;
        nrConsec = 0;
        Feature.Template template = new Feature.Template();
        template.type = "misc_feature";
        template.location = new RangeLocation(start, stop);
        template.annotation = new SimpleAnnotation();
        template.annotation.setProperty("type", "CG/GC/GG/CC Island");
        seq.createFeature(template);
      }
      nrOfOcc = 0;
      index = index + step;
    }
  }

  public void annotateATdinuclDroso() throws Exception {
    //put winowLength hardcoded because nrCgrandom depends on windowLenght
    double nr_AA_TT_AT_TA_bg = 33d;
    int windowLength = 100;
    int step = 50;
    String motif = "";
    String motif1 = "aa";
    String motif2 = "tt";
    String motif3 = "at";
    String motif4 = "ta";
    String[] motifs = {
        motif1, motif2, motif3, motif4};
    //
    PatternMatcher matcher;
    PatternCompiler compiler;
    Pattern pattern = null;
    PatternMatcherInput input;
    MatchResult result;
    compiler = new Perl5Compiler();
    matcher = new Perl5Matcher();
    int nrOfOcc = 0;
    int nrConsec = 0;
    double atContent = 0d;
    double atValue = 0d;
    SymbolList window = null;
    String dnaStr;
    int index = 1;
    int i = 1;
    while (index < seq.length() - windowLength) {
      window = seq.subList(index, index + windowLength);
      //direct
      dnaStr = window.seqString();

      for (int m = 0; m < 4; m++) {
        input = new PatternMatcherInput(dnaStr);
        motif = motifs[m];
        //System.out.println("motif="+motif);
        pattern = compiler.compile(motif);
        while (matcher.contains(input, pattern)) {
          nrOfOcc++;
        }
      }
      //reverse complement
      //window = DNATools.reverseComplement(window);
      //dnaStr = window.seqString();
      //input = new PatternMatcherInput(dnaStr);
      //while (matcher.contains(input, pattern)) {
      //  nrOfOcc++;
      //}
      atContent = ATContent(window);
      atValue = (nr_AA_TT_AT_TA_bg / nrOfOcc);

      /*System.out.println("Nrofocc = "+nrOfOcc);
                System.out.println("   GCcontent = " + atContent);
                System.out.println("   pos=" + index);
                System.out.println("   nrConsec=" + nrConsec);
                System.out.println("   Seq = " + dnaStr);
                System.out.println("");
       */

      if (atValue < 0.6d && atContent > 60.0d) {
        nrConsec++;
      }
      else if (nrConsec > 0) {
        int start = (index - (nrConsec) * step);
        int stop = index + windowLength;
        nrConsec = 0;
        Feature.Template template = new Feature.Template();
        template.type = "misc_feature";
        template.location = new RangeLocation(start, stop);
        template.annotation = new SimpleAnnotation();
        template.annotation.setProperty("type", "AA/TT/AT/TA Island");
        seq.createFeature(template);
      }
      nrOfOcc = 0;
      index = index + step;
    }
  }

  public int annotateMotifWindow(String motifName, String motif,
                                 int windowLength, int step, int minNrMotifs) throws
      Exception {
    PatternCompiler compiler = new Perl5Compiler();
    PatternMatcher matcher = new Perl5Matcher();
    Pattern pattern = compiler.compile(motif);
    int totalLength = 0;
    int nrOfOccPlus = 0;
    int nrOfOccNeg = 0;
    int totalNrOfOcc = 0;
    PatternMatcherInput input;
    MatchResult result;
    int groups;
    SymbolList window = null;
    int index = 1;
    String dnaStr = "";
    int cnt = 0;
    while (index < seq.length() - windowLength) {
      window = seq.subList(index, index + windowLength);
      //direct
      dnaStr = window.seqString();
      input = new PatternMatcherInput(dnaStr);
      while (matcher.contains(input, pattern)) {
        nrOfOccPlus++;
      }
      //reverse complement
      dnaStr = DNATools.reverseComplement(window).seqString();
      input = new PatternMatcherInput(dnaStr);
      while (matcher.contains(input, pattern)) {
        nrOfOccNeg++;
      }
      if ( (nrOfOccPlus + nrOfOccNeg) > minNrMotifs) {
        Feature.Template template = new Feature.Template();
        template.source = "Manual";
        template.type = "misc_feature";
        template.location = new RangeLocation(index, index + windowLength);
        template.annotation = new SimpleAnnotation();
        //template.annotation.setProperty("note","type=motifWindow of "+motifName+"; >"+minNrMotifs+" (on both strands); windowLength="+windowLength);
        template.annotation.setProperty("type", motifName+"-cluster");
        template.annotation.setProperty("note", "motifWindow");
        template.annotation.setProperty("windowLength",
                                        new Integer(windowLength).toString());
        template.annotation.setProperty("step", new Integer(step).toString());
        template.annotation.setProperty("minNrMotifs",
                                        new Integer(minNrMotifs).toString());
        seq.createFeature(template);
        cnt++;
      }
      nrOfOccPlus = 0;
      nrOfOccNeg = 0;
      index = index + step;
    }
    return cnt;
  }

  
  public int slidingWindow(ArrayList motifNames, int windowSize, int stepSize, double minRatio) throws Exception {
  	HashMap motifs = new HashMap();
  	String str;
  	for(Iterator it = motifNames.iterator() ;it.hasNext() ;){
  		str = (String)it.next();
  		//System.out.println(str);
  		motifs.put(str,new Boolean(false));
  	}
  	int index=1;
//  	boolean[] found = new boolean[motifNames.size()];
//  	for (int i=0;i<found.length;i++){
//  		found[i]=false;
//  	}
  	RangeLocation loc;
  	int cnt=0;
  	Feature f;
  	int nr;
  	int instNr;
  	double ratio;
	//System.out.println(this.name);
  	while (index < seq.length() - windowSize) {
  		
  		String type = "cluster-";
  		for(Iterator it = motifNames.iterator() ;it.hasNext() ;){
  	  		str = (String)it.next();
  	  		motifs.put(str,new Boolean(false));
  	  		type+=str+"-";
  	  	}
  		
  		instNr=0;
		loc = new RangeLocation(index,index+windowSize);
		//System.out.print("window "+loc.getMin()+"-"+loc.getMax()+": ");
		FeatureFilter featFilterWin = new FeatureFilter.OverlapsLocation(loc);
  		FeatureHolder fhWin = seq.filter(featFilterWin, false);
  		//System.out.print(fhWin.countFeatures() +" features: ");
  		for(Iterator it = fhWin.features();it.hasNext();){
  			f = (Feature)it.next();
  			if(motifs.containsKey((String)f.getAnnotation().getProperty("type"))){
  				instNr++;
  				motifs.put(((String)f.getAnnotation().getProperty("type")),new Boolean(true));
  			}
  		//	System.out.print(f.getAnnotation().getProperty("type")+" ("+motifs.get((String)f.getAnnotation().getProperty("type"))+") ");
  		}
  		//System.out.println();
  		int hits=0;
  		//for(int i=0;i<found.length;i++){
  		//	if(found[i]) hits++;
  		//}
  		for(Iterator it = motifs.keySet().iterator();it.hasNext();){
  			if(((Boolean)motifs.get(it.next())).booleanValue()) hits++;
  		}
//  		System.out.println(name+"|"+hits+"|"+motifs.size()+"|"+ratio);
  		ratio=(double)hits/motifs.size();
  		if(ratio>=minRatio){
  			Feature.Template template = new Feature.Template();
		    	template.source = "Manual";
		    	template.type = "misc_feature";
		    	template.location = loc;
		    	template.annotation = new SimpleAnnotation();
		    	template.annotation.setProperty("type", type);
		    	template.annotation.setProperty("note", "cluster");
		    	template.annotation.setProperty("score", ""+hits);
		    	template.annotation.setProperty("score2", ""+instNr);
		    	template.annotation.setProperty("windowLength",
		    	               new Integer(windowSize).toString());
		    	seq.createFeature(template);
  			cnt++;
  		}
  		index=index+stepSize;
	}
  	
  	//and then the remaining piece of DNA at the end
  	instNr=0;
	loc = new RangeLocation(index,seq.length());
	FeatureFilter featFilterWin = new FeatureFilter.OverlapsLocation(loc);
		FeatureHolder fhWin = seq.filter(featFilterWin, false);
		for(Iterator it = fhWin.features();it.hasNext();){
			f = (Feature)it.next();
			if(motifs.containsKey((String)f.getAnnotation().getProperty("type"))){
				instNr++;
				motifs.put(((String)f.getAnnotation().getProperty("type")),new Boolean(true));
			}
		}
		int hits=0;
		for(Iterator it = motifs.keySet().iterator();it.hasNext();){
			if(((Boolean)motifs.get(it.next())).booleanValue()) hits++;
		}
		if((hits/motifs.size())>=minRatio){
	    	Feature.Template template = new Feature.Template();
	    	template.source = "Manual";
	    	template.type = "misc_feature";
	    	template.location = loc;
	    	template.annotation = new SimpleAnnotation();
	    	template.annotation.setProperty("type", "cluster");
	    	template.annotation.setProperty("note", "motifWindow");
	    	template.annotation.setProperty("score", ""+instNr);
	    	template.annotation.setProperty("windowLength",
	    	               new Integer(windowSize).toString());
	    	seq.createFeature(template);
			cnt++;
		}
  	
	return cnt;
  }

  public int annotateMotifPairPost(String motifName1, String motifName2,
        int windowLength,String name) throws Exception{

  	FeatureFilter featFilter1 = new FeatureFilter.ByAnnotation("type",motifName1);
  	FeatureFilter featFilter2 = new FeatureFilter.ByAnnotation("type",motifName2);

  	FeatureHolder fh = seq.filter(featFilter1, false);
    Feature f;
    int cnt=0;
    for(Iterator it = fh.features();it.hasNext();){
    		f = (Feature)it.next();
    		RangeLocation loc = new RangeLocation(f.getLocation().getMin()-windowLength,f.getLocation().getMax()+windowLength);
    	    FeatureFilter featFilterWin = new FeatureFilter.OverlapsLocation(loc);
    	    FeatureFilter featFilter = new FeatureFilter.And(featFilterWin,featFilter2);
    	    FeatureHolder fhFinal = seq.filter(featFilter, false);
    	    if(fhFinal.countFeatures()>0){
    	    		System.out.println(name+"|"+loc.getMin()+"|"+loc.getMax());
	    	    	Feature.Template template = new Feature.Template();
	    	    	template.source = "Manual";
	    	    	template.type = "misc_feature";
	    	    	template.location = loc;
	    	    	template.annotation = new SimpleAnnotation();
	    	    	template.annotation.setProperty("type", "cluster");
	    	    	template.annotation.setProperty("note", "motifWindow");
	    	    	template.annotation.setProperty("name", name);
	    	    	template.annotation.setProperty("windowLength",
	    	    	               new Integer(windowLength).toString());
	    	    	seq.createFeature(template);
	    	    	cnt++;
    	    }

    }

  	return cnt;
  }
  
  
  public int annotateMotifPairPostNoOverlap(String motifName1, String motifName2,
        int windowLength,String name) throws Exception{

  	FeatureFilter featFilter1 = new FeatureFilter.ByAnnotation("type",motifName1);
  	FeatureFilter featFilter2 = new FeatureFilter.ByAnnotation("type",motifName2);

  	FeatureHolder fh = seq.filter(featFilter1, false);
    Feature f;
    int cnt=0;
    for(Iterator it = fh.features();it.hasNext();){
    		f = (Feature)it.next();
    		//left of motif1
    		RangeLocation loc = new RangeLocation(f.getLocation().getMin()-windowLength,f.getLocation().getMin());
    	    FeatureFilter featFilterWin = new FeatureFilter.OverlapsLocation(loc);
    	    FeatureFilter featFilter = new FeatureFilter.And(featFilterWin,featFilter2);
    	    FeatureHolder fhFinal = seq.filter(featFilter, false);
    	    if(fhFinal.countFeatures()>0){
    	    		System.out.println(name+"|"+loc.getMin()+"|"+loc.getMax());
	    	    	Feature.Template template = new Feature.Template();
	    	    	template.source = "Manual";
	    	    	template.type = "misc_feature";
	    	    	RangeLocation featLoc = new RangeLocation(f.getLocation().getMin()-windowLength,f.getLocation().getMax());
	    	    	template.location = featLoc;
	    	    	template.annotation = new SimpleAnnotation();
	    	    	template.annotation.setProperty("type", "cluster");
	    	    	template.annotation.setProperty("note", "motifWindow");
	    	    	template.annotation.setProperty("name", name);
	    	    	template.annotation.setProperty("windowLength",
	    	    	               new Integer(windowLength).toString());
	    	    	seq.createFeature(template);
	    	    	cnt++;
    	    }
    	    
    	    //right of motif1
    	    loc = new RangeLocation(f.getLocation().getMax(),f.getLocation().getMax()+windowLength);
    	    featFilterWin = new FeatureFilter.OverlapsLocation(loc);
    	    featFilter = new FeatureFilter.And(featFilterWin,featFilter2);
    	    fhFinal = seq.filter(featFilter, false);
    	    if(fhFinal.countFeatures()>0){
    	    		System.out.println(name+"|"+loc.getMin()+"|"+loc.getMax());
	    	    	Feature.Template template = new Feature.Template();
	    	    	template.source = "Manual";
	    	    	template.type = "misc_feature";
	    	    	RangeLocation featLoc = new RangeLocation(f.getLocation().getMin(),f.getLocation().getMax()+windowLength);
	    	    	template.location = featLoc;
	    	    	template.annotation = new SimpleAnnotation();
	    	    	template.annotation.setProperty("type", "cluster");
	    	    	template.annotation.setProperty("note", "motifWindow");
	    	    	template.annotation.setProperty("name", name);
	    	    	template.annotation.setProperty("windowLength",
	    	    	               new Integer(windowLength).toString());
	    	    	seq.createFeature(template);
	    	    	cnt++;
    	    }

    }

  	return cnt;
  }
  
  

  //doesn't work properly !! gives false hits
  public int annotateMotifPair(String motifName1, String motif1,String motifName2,String motif2,
        int windowLength, int step) throws Exception {
    System.out.println(motif1+"|"+motif2);
    PatternCompiler compiler = new Perl5Compiler();
    PatternMatcher matcher = new Perl5Matcher();

    Pattern pattern = compiler.compile(motif1);
    int totalLength = 0;
    int nrOfOccPlusa = 0;
    int nrOfOccNega = 0;
    int nrOfOccPlusb = 0;
    int nrOfOccNegb = 0;
    PatternMatcherInput input;
    MatchResult result;
    int groups;
    SymbolList window = null;
    int index = 1;
    String dnaStr = "";
    int cnt = 0;
    while (index < seq.length() - windowLength) {
      window = seq.subList(index, index + windowLength);
      //direct
      dnaStr = window.seqString();
      input = new PatternMatcherInput(dnaStr);
      while (matcher.contains(input, pattern)) {
        nrOfOccPlusa++;
      }
      //motif2
      pattern = compiler.compile(motif2);
      input = new PatternMatcherInput(dnaStr);
      while (matcher.contains(input, pattern)) {
        nrOfOccPlusb++;
      }
      //reverse complement
      pattern = compiler.compile(motif1);
      dnaStr = DNATools.reverseComplement(window).seqString();
      input = new PatternMatcherInput(dnaStr);
      while (matcher.contains(input, pattern)) {
        nrOfOccNega++;
      }
      //motif2
      pattern = compiler.compile(motif2);
      input = new PatternMatcherInput(dnaStr);
      while (matcher.contains(input, pattern)) {
        nrOfOccNegb++;
      }
      System.out.println(index+"|"+nrOfOccPlusa+"|"+nrOfOccNega+"|"+nrOfOccPlusb+"|"+nrOfOccNegb);
      if ( ((nrOfOccPlusa + nrOfOccNega) > 0 ) && ((nrOfOccPlusb + nrOfOccNegb) > 0)) {
        //System.out.println(index+"|"+nrOfOccPlus1+"|"+nrOfOccNeg1+"|"+nrOfOccPlus2+"|"+nrOfOccNeg2);
	Feature.Template template = new Feature.Template();
        template.source = "Manual";
        template.type = "misc_feature";
        template.location = new RangeLocation(index, index + windowLength);
        template.annotation = new SimpleAnnotation();
        //template.annotation.setProperty("note","type=motifWindow of "+motifName+"; >"+minNrMotifs+" (on both strands); windowLength="+windowLength);
        template.annotation.setProperty("type", motifName1+"-"+motifName2+"-cluster");
        template.annotation.setProperty("note", "motifWindow");
        template.annotation.setProperty("windowLength",
        new Integer(windowLength).toString());
        template.annotation.setProperty("step", new Integer(step).toString());
        seq.createFeature(template);
        cnt++;
      }
      nrOfOccPlusa = 0;
      nrOfOccNega = 0;
      nrOfOccPlusb = 0;
      nrOfOccNegb = 0;
      index = index + step;
    }
    return cnt;
  }


  /*
     public void annotateEponineTSS(double d)throws Exception{
        InputSource inputsource;
        java.io.InputStream inputstream = (eponine.ScanApp.class).getClassLoader().getResourceAsStream("eponine-model.xml");
        if(inputstream == null)
        {
            System.err.println("No default model available: must specify -model on the command line");
            System.exit(1);
        }
        inputsource = new InputSource(inputstream);
        EponineXML eponinexml = new EponineXML();
        DOMParser domparser = new DOMParser();
        domparser.parse(inputsource);
   org.w3c.dom.Element element = domparser.getDocument().getDocumentElement();
   GLMClassificationModel glmclassificationmodel = eponinexml.xmlToModel(element);
    Object obj = this.seq;
        //if(flag)
        //    obj = DNATools.reverseComplement(sequence);
        //else
        //    obj = sequence;
        FastModel fastmodel = new FastModel(glmclassificationmodel, ((SymbolList) (obj)), 1000);
        int i = -1000;
        int j = -1000;
        double d1 = 0.0D;
        int k = 1;
        for(int l = 1; l < ((SymbolList) (obj)).length(); l++)
        {
            double d2 = fastmodel.score(l);
            if(d2 > d)
            {
                d1 = Math.max(d1, d2);
                i = l;
                if(j < 0)
                    j = l;
            } else
            if(i > 0 && l - i > k)
            {
                Feature.Template template = new Feature.Template();
                template.type = "misc_feature";
                template.location = new RangeLocation(j,i);
                System.out.println(template.location);
     template.annotation = new SimpleAnnotation();
                template.annotation.setProperty("type","Eponine TSS");
                template.annotation.setProperty("score",""+d1);
     seq.createFeature(template);

                i = -1000;
                j = -1000;
                d1 = 0.0D;
            }
        }
     }
   */

  public GeneList getSubSequences(String featureSource, String partType,
                                  int bpBefore, int bpAfter,boolean revComplIfMinusStrand) throws Exception {
    GeneList ret = new GeneList();
    FeatureFilter featFilter1 = new FeatureFilter.BySource(featureSource);
    FeatureFilter featFilter2 = new FeatureFilter.ByAnnotation("source",featureSource);
    FeatureFilter featFilter = new FeatureFilter.Or(featFilter1,featFilter2);
    FeatureHolder fh = seq.filter(featFilter, false);
    Gene g = null;
    int start = 0, end = 0;
    //System.out.println("Number of features to select = "+fh.countFeatures());
    for (Iterator it = fh.features(); it.hasNext(); ) {
      g = new Gene();
      StrandedFeature f = (StrandedFeature) it.next();
      Location loc = f.getLocation();
      if (partType.toLowerCase().startsWith("around")) {
        start = f.getLocation().getMin() - bpBefore;
        end = f.getLocation().getMax() + bpAfter;
      }
      else if (partType.toLowerCase().startsWith("left")) {
        start = f.getLocation().getMin() - bpBefore;
        end = f.getLocation().getMin() + bpAfter;
      }
      else if (partType.toLowerCase().startsWith("right")) {
        start = f.getLocation().getMax() - bpBefore;
        end = f.getLocation().getMax() + bpAfter;
      }
      //it appears that this is needed
      start++;
      end++;
      g.setSequence(this.makeMySubSeq(start, end,
                                      name + "_" + start + "_" + end),
                    name + "_" + start + "_" + end);
      if(revComplIfMinusStrand && f.getStrand() ==StrandedFeature.NEGATIVE)
        g.reverseComplement();
      ret.add(g);
    }
    return ret;

  }

  public StringBuffer toGFF(Object[] sources) {
    HashSet src = new HashSet();
    if (sources != null) {
      for (int i = 0; i < sources.length; i++) {
        src.add(sources[i]);
      }
    }
    StringBuffer buf = new StringBuffer();
    Object o;
    StrandedFeature sf = null;
    Feature f;
    for (Iterator it = seq.features(); it.hasNext(); ) {
      f = (Feature) it.next();
      if (sources == null || src.contains(f.getSource()) || (f.getAnnotation().containsProperty("source") && src.contains(f.getAnnotation().getProperty("source")))) {
        buf.append(seq.getName());
        buf.append("\t");
        try {
          buf.append(f.getAnnotation().getProperty("source"));
        }
        catch (java.util.NoSuchElementException exc) {
          buf.append(f.getSource());
        }
        buf.append("\t");
        buf.append(f.getType());
        buf.append("\t");
        buf.append(f.getLocation().getMin());
        buf.append("\t");
        buf.append(f.getLocation().getMax());
        buf.append("\t");
        try {
          buf.append(f.getAnnotation().getProperty("score"));
        }
        catch (java.util.NoSuchElementException exc) {
          buf.append(".");
        }
        buf.append("\t");
        if (f instanceof SimpleStrandedFeature) {
          sf = (SimpleStrandedFeature) f;
          buf.append(sf.getStrand().getToken());
          buf.append("\t");
        }
        else {
          buf.append(".");
          buf.append("\t");
        }
        buf.append(".");
        buf.append("\t");
        for (Iterator it2 = sf.getAnnotation().keys().iterator(); it2.hasNext(); ) {
          o = it2.next();
          buf.append(o.toString());
          buf.append(" \"");
          buf.append(sf.getAnnotation().getProperty(o));
          buf.append("\";");
        }
        buf.deleteCharAt(buf.length() - 1);
        buf.append("\n");
      }
    }
    return buf;
  }

  public void writeSeqToEmblFile(String path) throws Exception {
    SequenceFormat format = new EmblLikeFormat();
    OutputStream out = new FileOutputStream(path);
    format.writeSequence(seq, new PrintStream(out));
    out.close();
  }

  public void writeSeqToFastaFile(String path) throws Exception {
    SequenceFormat format = new FastaFormat();
    OutputStream out = new FileOutputStream(path);
    if (seq != null) format.writeSequence(seq, new PrintStream(out));
    out.close();
  }

  /**
 * @deprecated
 */
public String getFastaString(String name) {
    String str;
    if (name == null) str = ">" + this.name + "\n";
    else str = ">" + name + "\n";
    str = str + seq.seqString();
    return str;
  }

  public void appendSeqToEmblFile(String path) throws Exception {
  	    SequenceFormat format = new EmblLikeFormat();
  	    OutputStream out = new FileOutputStream(path, true);
  	    try {
  	      format.writeSequence(seq, new PrintStream(out));
  	    }
  	    catch (Exception e) {
  	      System.out.println(e.getMessage());
  	    }
  	    out.close();
  	  }


  public String getFastaStr() throws Exception {
    StringOutputStream out = new StringOutputStream();
    SeqIOTools.writeFasta(out, seq);
    return out.toString();
  }

  public String getEmblStr() throws Exception {
    StringOutputStream out = new StringOutputStream();
    SeqIOTools.writeEmbl(out, seq);
    return out.toString();
  }

  public void appendSeqToFastaFile(String path) throws Exception {
    SequenceFormat format = new FastaFormat();
    OutputStream out = new FileOutputStream(path, true);
    try {
      format.writeSequence(seq, new PrintStream(out));
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
    }
    out.close();
  }

  public void appendSeqToGbFile(String path) throws Exception {
    SequenceFormat format = new GenbankFormat();
    OutputStream out = new FileOutputStream(path, true);
    try {
      format.writeSequence(seq, new PrintStream(out));
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
    }
    out.close();
  }

  public void writeSeqToGbFile(String path) throws Exception {
    SequenceFormat format = new GenbankFormat();
    OutputStream out = new FileOutputStream(path);
    format.writeSequence(seq, new PrintStream(out));
    out.close();
  }

  /**
   * This method creates a subsequence object, with all features
   * This method doesn't take the strand into account, just selects before and after in the same
   * way for + and - features.
   */
  public Sequence selectAroundFeat(String featStr, String annKey,
                                   String annValue, int bpBefore, int bpAfter) throws
      Exception {
    FeatureFilter featFilter = new FeatureFilter.ByType(featStr);
    FeatureFilter featFilter2 = new FeatureFilter.ByAnnotation(annKey, annValue);
    FeatureFilter theFilter;
    if (annKey != null && annValue != null) theFilter = new FeatureFilter.And(
        featFilter, featFilter2);
    else theFilter = featFilter;
    FeatureHolder fh = seq.filter(theFilter, false);
    SimpleSequence subseq = null;
    if (fh.countFeatures() > 0) {
      Feature f = (Feature) fh.features().next();
      Location loc = f.getLocation();
      subseq = (SimpleSequence) makeSubSeq(loc.getMin() - bpBefore + 1,
                                           loc.getMax() + bpAfter);
      //System.out.println(subseq.getName()+"|"+subseq.length());
    }
    return subseq;
  }

  public StrandedFeature selectFeature(String featStr, String annKey,
                                       String annValue) {
    FeatureFilter featFilter = new FeatureFilter.ByType(featStr);
    FeatureFilter featFilter2 = new FeatureFilter.ByAnnotation(annKey, annValue);
    FeatureFilter theFilter;
    if (annKey != null && annValue != null) theFilter = new FeatureFilter.And(
        featFilter, featFilter2);
    else theFilter = featFilter;
    FeatureHolder fh = seq.filter(theFilter, false);
    StrandedFeature f = null;
    if (fh.countFeatures() > 0) {
      f = (StrandedFeature) fh.features().next();
    }
    return f;
  }

  public ArrayList selectAroundFeature(String featStr, String annKey,
                                       String annValue, int bpBefore,
                                       int bpAfter) throws Exception {
    ArrayList seqList = new ArrayList();
    StrandedFeature f = null;
    int featStrand = 0;
    Location loc;
    SymbolList around = null;
    StrandedFeature.Template temp = new StrandedFeature.Template();
    Sequence aroundSeq = null;
    for (Iterator i = seq.features(); i.hasNext(); ) {
      aroundSeq = null;
      try {
        f = (StrandedFeature) i.next();
      }
      catch (Exception e) {
        //e.printStackTrace();
        //System.out.println("Not a stranded feature in "+seq.getName()+"|"+f.getType());
      }
      if (f.getType().equalsIgnoreCase(featStr)) {
        Annotation an = f.getAnnotation();
        for (Iterator j = an.keys().iterator(); j.hasNext(); ) {
          Object key = j.next();
          Object value = an.getProperty(key);
          if (key.toString().equalsIgnoreCase(annKey) &&
              value.toString().equalsIgnoreCase(annValue)) {
            loc = f.getLocation();
            if (f.getStrand().getValue() == 1) {
              featStrand = 1;
              around = seq.subList(loc.getMin() - bpBefore + 1,
                                   loc.getMax() + bpAfter);
              temp.type = featStr;
              temp.strand = StrandedFeature.POSITIVE;
              temp.source = "";
              temp.annotation = Annotation.EMPTY_ANNOTATION;
              temp.location = new RangeLocation(bpBefore + 1,
                                                bpBefore +
                                                (loc.getMax() - loc.getMin()));
              aroundSeq = new SimpleSequence(around, "around", seq.getName(),
                                             new SimpleAnnotation());
              aroundSeq.createFeature(temp);
            }
            else if (f.getStrand().getValue() == -1) {
              featStrand = -1;
              around = seq.subList(loc.getMin() - bpBefore + 1,
                                   loc.getMax() + bpAfter);
              temp.type = featStr;
              temp.strand = StrandedFeature.NEGATIVE;
              temp.source = "";
              temp.annotation = Annotation.EMPTY_ANNOTATION;
              temp.location = new RangeLocation(bpAfter + 1,
                                                bpAfter +
                                                (loc.getMax() - loc.getMin()));
              aroundSeq = new SimpleSequence(around, "around", seq.getName(),
                                             new SimpleAnnotation());
              aroundSeq.createFeature(temp);
              //around = DNATools.reverseComplement(around);
            }
          } //end if
        } //end for
      }
      if (aroundSeq != null) seqList.add(aroundSeq);
    }
    return seqList;
  }

  /**
   * @deprecated
   * For this method, there can only be one gene present in the sequence!!!
   * No gene identifiers are used. So first use SelectBeforeFeat(CDS) to select the upstream
   * region of a CDS. Aaight.
   * Returns null if there is a problem.
   * bpBefore and bpAfter are only in case of no promoter present.
   */
  public Sequence selectPromoterRegion(int bpBefore, int bpAfter) throws
      Exception {
    //check if there's only 1 CDS
    FeatureFilter cdsFilter = new FeatureFilter.ByType("CDS");
    FeatureHolder fh = seq.filter(cdsFilter, false);
    if (fh.countFeatures() != 1)throw new Exception(name +
        ": 0 or more than 1 CDS!. Remove one from the seq file");
    //Take promoter if annotation is present
    Sequence toReturn;
    FeatureFilter promFilter = new FeatureFilter.ByType("promoter");
    fh = seq.filter(promFilter, false);
    if (fh.countFeatures() > 1)throw new Exception(
        "0 or more than 1 real promoter!. Remove one from the seq file");
    toReturn = selectAroundFeat("promoter", null, null, 0, 0);
    if (toReturn != null) {
      System.out.println(name + ": real promoter selected");
      return toReturn;
    }

    //Promoter predictions
    FeatureFilter promFilter1 = new FeatureFilter.ByType("misc_feature");
    FeatureFilter promFilter2 = new FeatureFilter.ByAnnotation("type",
        "promoter");
    promFilter = new FeatureFilter.And(promFilter1, promFilter2);
    fh = seq.filter(promFilter, false);
    if (fh.countFeatures() > 1)throw new Exception(name +
        ": 0 or more than 1 predicted promoter!. Remove one from the seq file");
    toReturn = this.selectAroundFeat("misc_feature", "type", "promoter", 10, 10);
    if (toReturn != null) {
      System.out.println(name + ": predicted promoter selected");
      return toReturn;
    }

    //CpG island next to CDS
    toReturn = this.selectSeqAroundGivenFeat(selectFeatNextToCDS(null, null,
        "misc_feature", "type", "CpG Island"), 0, 0, false);
    if (toReturn != null) {
      System.out.println(name + ": CpG Island selected");
      return toReturn;
    }
    //first exon
    //only 1 exon 1
    toReturn = selectBeforeFeat("exon", "number", "1", bpBefore, bpAfter, 0);
    if (toReturn != null) {
      System.out.println(name + ": before exon 1 selected");
      return toReturn;
    }
    else {
      //CDS doesn't have to be specified, we presume there is only 1 (otherwise we wouldn't be here, see first stmt)
      toReturn = this.selectSeqBeforeGivenFeat(this.selectFeatNextToCDS(null, null,
          "exon", null, null), bpBefore, bpAfter);
      //toReturn = selectSeqAroundFeatNextToCDS("exon",null,null,bpBefore,bpAfter);
      if (toReturn != null) {
        System.out.println(name + ": before closest exon selected");
        return toReturn;
      }
    }
    toReturn = this.selectSeqBeforeGivenFeat(this.selectFeatNextToCDS(null, null,
        "prim_transcript", null, null), bpBefore, bpAfter);
    //toReturn = selectBeforeFeat("prim_transcript",null,null,bpBefore,bpAfter);
    if (toReturn != null) {
      System.out.println(name + ": before primary transcript selected");
      return toReturn;
    }
    toReturn = this.selectSeqBeforeGivenFeat(this.selectFeatNextToCDS(null, null,
        "mRNA", null, null), bpBefore, bpAfter);
    //toReturn = selectBeforeFeat("mRNA",null,null,bpBefore,bpAfter);
    if (toReturn != null) {
      System.out.println(name + ": before mRNA selected");
      return toReturn;
    }
    toReturn = selectBeforeFeat("CDS", null, null, bpBefore, bpAfter, 0);
    if (toReturn != null) {
      System.out.println(name + ": before CDS selected");
      return toReturn;
    }
    toReturn = selectBeforeFeat("gene", null, null, bpBefore, bpAfter, 0);
    if (toReturn != null) {
      System.out.println(name + ": before gene selected");
      return toReturn;
    }
    System.out.println(name + ":  Nothing could be selected )-:");
    return null;
  }

  /**
   * returns true if the CDS of the given gene is on the positive strand
   */
  public boolean cdsStrandPositive(String cdsAnnKey, String cdsAnnValue) throws
      Exception {
    FeatureFilter cdsFilter = new FeatureFilter.ByType("CDS");
    FeatureFilter exonFilter = new FeatureFilter.ByType("exon");
    FeatureFilter genFilter = new FeatureFilter.ByType("gene");
    cdsFilter = new FeatureFilter.Or(cdsFilter, exonFilter);
    cdsFilter = new FeatureFilter.Or(cdsFilter, genFilter);
    //so far we have all cds, exon, gene features
    //now select only those with the given cdsAnnKey and cdsAnnValue (e.g., gene=ENSMUSG00000026208)
    FeatureFilter cdsgeneFilter;
    if (cdsAnnKey != null && cdsAnnValue != null) {
      FeatureFilter geneFilter = new FeatureFilter.ByAnnotation(cdsAnnKey,
          cdsAnnValue);
      cdsgeneFilter = new FeatureFilter.And(cdsFilter, geneFilter);
    }
    else {
      cdsgeneFilter = cdsFilter;
    }
    FeatureHolder fh = seq.filter(cdsgeneFilter, false);
    StrandedFeature sf = null;
    //select first CDS if ambiguous
    if (fh.countFeatures() >= 1) {
      int min = seq.length();
      int max = 0;
      StrandedFeature tempf;
      for (Iterator it = fh.features(); it.hasNext(); ) {
        tempf = (StrandedFeature) it.next();
        if (tempf.getStrand() == StrandedFeature.POSITIVE ||
            tempf.getStrand() == StrandedFeature.UNKNOWN) {
          if (tempf.getLocation().getMin() < min) {
            min = tempf.getLocation().getMin();
            sf = tempf;
          }
        }
        else {
          if (tempf.getLocation().getMax() > max) {
            max = tempf.getLocation().getMax();
            sf = tempf;
          }
        }
      }
    }
    //fh is empty...
    else {
      throw new Exception(cdsAnnKey + "="+cdsAnnValue+"=> No such CDS present");
    }
    if (sf.getStrand() == StrandedFeature.POSITIVE)return true;
    else return false;
  }

  public void removeOtherExons() throws Exception {
    //for now: rev compl if on negative
    if(!cdsPositive) this.reverseComplement();
    if(!cdsPositive) throw new Exception("Sorry, could not reverse complement while this was needed for exon removal");
    FeatureFilter exonFilter = new FeatureFilter.ByType("exon");
      FeatureHolder fh = seq.filter(exonFilter, false);
      if (fh.countFeatures() > 0) {
        //get most  exon
        int max = 0;
        Feature tempf;
        String gene;
        for (Iterator it = fh.features(); it.hasNext(); ) {
          tempf = (Feature) it.next();
          gene = (String) tempf.getAnnotation().getProperty("gene");
          System.out.println("gene="+gene);
          if (!gene.equalsIgnoreCase(this.name) && !gene.equalsIgnoreCase(this.ensembl)) {
            if (tempf.getLocation().getMax() > max) {
              max = tempf.getLocation().getMax();
              System.out.println("max="+max);
            }
          }
        }
        if (max > 0) {
          setSequence(makeMySubSeq(max, seq.length(),this.name),this.name);
        }
      }
    }


  /**
   * - Use for sequences that have been reverse complemented if CDS was on minus strand
   * - rightbound can be for example the start of the exon of the gene under investigation
   * - if another exon is found left of the rightbound, then all sequence from the beginning of the
   * sequence until the end of that exon is removed
   * - exons on + or - strand doesn't matter
   */
  public void removeOtherExonsAtLeft(int rightBound) throws Exception {
    FeatureFilter exonFilter = new FeatureFilter.ByType("exon");
    FeatureHolder fh = seq.filter(exonFilter, false);
    if (fh.countFeatures() > 0) {
      //get most  exon
      int max = 0;
      Feature tempf;
      for (Iterator it = fh.features(); it.hasNext(); ) {
        tempf = (Feature) it.next();
        if (tempf.getLocation().getMin() < rightBound) {
          if (tempf.getLocation().getMax() > max) {
            max = tempf.getLocation().getMax();
          }
        }
      }
      if (max > 0) {
        this.seq = this.makeMySubSeq(max, seq.length(), null);
      }
    }
  }

  /**
   */
  public void removeOtherExonsAtRight(int leftBound) throws Exception {
    FeatureFilter exonFilter = new FeatureFilter.ByType("exon");
    FeatureHolder fh = seq.filter(exonFilter, false);
    if (fh.countFeatures() > 0) {
      //get most right exon
      int min = 999999;
      Feature tempf;
      for (Iterator it = fh.features(); it.hasNext(); ) {
        tempf = (Feature) it.next();
        if (tempf.getLocation().getMax() > leftBound) {
          if (tempf.getLocation().getMin() < min) {
            min = tempf.getLocation().getMin();
          }
        }
      }
      if (min < 999999) {
        this.seq = this.makeMySubSeq(1, min, null);
      }
    }
  }

  public Sequence getSecondLeftExon() throws Exception {
    FeatureFilter exonFilter = new FeatureFilter.ByType("exon");
    FeatureHolder fh = seq.filter(exonFilter, false);
    if (fh.countFeatures() > 0) {
      int min = 999999;
      Feature tempf, first, second;
      TreeMap exonMap = new TreeMap();
      for (Iterator it = fh.features(); it.hasNext(); ) {
        tempf = (Feature) it.next();
        exonMap.put(new Integer(tempf.getLocation().getMin()), tempf);
      }
      second = (Feature) exonMap.get(exonMap.keySet().toArray()[1]); //second element, was sorted in Treemap automatically
      return makeMySubSeq(second.getLocation().getMin(),
                          second.getLocation().getMax(), this.name);
    }
    else return null;
  }

  /**
   * This method selects the feature next to the CDS with given properties.
   * If these properties are null, than there should only be one CDS
   */
  public StrandedFeature selectFeatNextToCDS(String cdsAnnKey,
                                             String cdsAnnValue, String featStr,
                                             String annKey, String annValue) throws
      Exception {
    FeatureFilter cdsFilter = new FeatureFilter.ByType("CDS");
    FeatureFilter cdsgeneFilter;
    if (cdsAnnKey != null && cdsAnnValue != null) {
      FeatureFilter geneFilter = new FeatureFilter.ByAnnotation(cdsAnnKey,
          cdsAnnValue);
      cdsgeneFilter = new FeatureFilter.And(cdsFilter, geneFilter);
    }
    else {
      cdsgeneFilter = cdsFilter;
    }

    FeatureHolder fh = seq.filter(cdsgeneFilter, false);
    //if (fh.countFeatures()!=1) throw new Exception("The CDS is ambiguous");

    StrandedFeature sf = null;
    //select first CDS if ambiguous
    if (fh.countFeatures() >= 1) {
      int min = seq.length();
      int max = 0;
      StrandedFeature tempf;
      for (Iterator it = fh.features(); it.hasNext(); ) {
        tempf = (StrandedFeature) it.next();
        if (tempf.getStrand() == StrandedFeature.POSITIVE ||
            tempf.getStrand() == StrandedFeature.UNKNOWN) {
          if (tempf.getLocation().getMin() < min) {
            min = tempf.getLocation().getMin();
            sf = tempf;
          }
          //System.out.println(name + "(+): feature= "+tempf.getLocation().getMin()+"-"+tempf.getLocation().getMax()+"|"+tempf.getStrand().getToken());
        }
        else {
          if (tempf.getLocation().getMax() > max) {
            max = tempf.getLocation().getMax();
            sf = tempf;
          }
          //System.out.println(name + "(-): feature= "+tempf.getLocation().getMin()+"-"+tempf.getLocation().getMax()+"|"+tempf.getStrand().getToken());
        }
      }
    }

    //StrandedFeature sf = (StrandedFeature)fh.features().next();
    int cdsStrand = sf.getStrand().getValue();
    Location cdsLoc = sf.getLocation();
    Location loc = null, featLoc = null;
    StrandedFeature feat = null;
    SymbolList around = null;
    StrandedFeature.Template temp = new StrandedFeature.Template();
    Sequence aroundSeq = null;
    Annotation ann = null;
    FeatureFilter featFilter;
    FeatureFilter featFilter1 = new FeatureFilter.ByType(featStr);
    if (annKey != null && annValue != null) {
      FeatureFilter featFilter2 = new FeatureFilter.ByAnnotation(annKey,
          annValue);
      featFilter = new FeatureFilter.And(featFilter1, featFilter2);
    }
    else featFilter = featFilter1;
    fh = seq.filter(featFilter, false);
    int min = 10000;
    int dist = 10001;
    for (Iterator it = fh.features(); it.hasNext(); ) {
      sf = (StrandedFeature) it.next();
      loc = sf.getLocation();
      if (cdsStrand == 1) {
        dist = cdsLoc.getMin() - loc.getMax();
        if (dist > 0 && dist < min) {
          min = dist;
          featLoc = loc;
          feat = sf;
          ann = sf.getAnnotation();
        }
      }
      else {
        dist = loc.getMin() - cdsLoc.getMax();
        if (dist > 0 && dist < min) {
          min = dist;
          featLoc = loc;
          feat = sf;
          ann = sf.getAnnotation();
        }
      }
    }
    return feat;
  }

  /**
   *
   */
  public StrandedFeature selectFeatNextToSeqEnd(String featStr, String annKey,
                                                String annValue) throws
      Exception {
    StrandedFeature sf = null;
    Location loc = null, featLoc = null;
    StrandedFeature feat = null;
    SymbolList around = null;
    StrandedFeature.Template temp = new StrandedFeature.Template();
    Sequence aroundSeq = null;
    Annotation ann = null;
    FeatureFilter featFilter;
    FeatureFilter featFilter1 = new FeatureFilter.ByType(featStr);
    if (annKey != null && annValue != null) {
      FeatureFilter featFilter2 = new FeatureFilter.ByAnnotation(annKey,
          annValue);
      featFilter = new FeatureFilter.And(featFilter1, featFilter2);
    }
    else featFilter = featFilter1;
    FeatureHolder fh = seq.filter(featFilter, false);
    int min = 10000;
    int dist = 10001;
    for (Iterator it = fh.features(); it.hasNext(); ) {
      sf = (StrandedFeature) it.next();
      loc = sf.getLocation();
      dist = seq.length() - loc.getMax();
      if (dist > 0 && dist < min) {
        min = dist;
        featLoc = loc;
        feat = sf;
        ann = sf.getAnnotation();
      }
    }
    return feat;
  }

  /**
   * @param b_compl if true then the complementary sequence it taken for features on - strand
   * if false then the input strand is taken for any feature.
   */
  public Sequence selectSeqAroundGivenFeat(StrandedFeature feat, int bpBefore,
                                           int bpAfter, boolean b_compl) throws
      Exception {
    if (feat == null)return null;
    SymbolList around = null;
    Sequence aroundSeq = null;
    Location featLoc = feat.getLocation();
    if (feat.getStrand().getValue() == 1) {
      int start = featLoc.getMin() - bpBefore + 1;
      if (start < 1) start = 1;
      int stop = featLoc.getMax() + bpAfter + 1;
      if (stop > seq.length()) stop = seq.length();
      around = seq.subList(start, stop);
      aroundSeq = new SimpleSequence(around, "around", seq.getName(),
                                     Annotation.EMPTY_ANNOTATION);
    }
    else if (feat.getStrand().getValue() == -1) {
      int start = featLoc.getMin() - bpAfter + 1;
      if (start < 1) start = 1;
      int stop = featLoc.getMax() + bpBefore;
      if (stop > seq.length()) stop = seq.length();
      around = seq.subList(start, stop);
      if (!b_compl) aroundSeq = new SimpleSequence(around, "around",
          seq.getName(), Annotation.EMPTY_ANNOTATION);
      else if (b_compl) aroundSeq = new SimpleSequence(DNATools.
          reverseComplement(around), "around", seq.getName(),
          Annotation.EMPTY_ANNOTATION);
    }

    return aroundSeq;
  }

  public Sequence selectSeqBeforeGivenFeat(StrandedFeature feat, int bpBefore,
                                           int bpAfter) {
    if (feat == null)return null;
    SymbolList around = null;
    Sequence aroundSeq = null;
    Location featLoc = feat.getLocation();
    if (feat.getStrand().getValue() == 1) {
      int start = featLoc.getMin() - bpBefore;
      if (start < 1) start = 1;
      int stop = featLoc.getMin() + bpAfter;
      if (stop > seq.length()) stop = seq.length();
      around = seq.subList(start, stop);
      aroundSeq = new SimpleSequence(around, "around", seq.getName(),
                                     Annotation.EMPTY_ANNOTATION);
    }
    else if (feat.getStrand().getValue() == -1) {
      int start = featLoc.getMax() - bpAfter;
      if (start < 1) start = 1;
      int stop = featLoc.getMax() + bpBefore;
      if (stop > seq.length()) stop = seq.length();
      around = seq.subList(start, stop);
      aroundSeq = new SimpleSequence(around, "around", seq.getName(),
                                     Annotation.EMPTY_ANNOTATION);
    }

    return aroundSeq;
  }

  /**
   * @deprecated
   * This method selects the seq around the given feature next to the CDS where the //gene= feature
   * has the same name as the name of the gene object.
   */
  public Sequence selectSeqAroundFeatNextToCDS(String featStr, String annKey,
                                               String annValue, int bpBefore,
                                               int bpAfter) throws Exception {

    FeatureFilter cdsFilter = new FeatureFilter.ByType("CDS");
    FeatureFilter geneFilter = new FeatureFilter.ByAnnotation("gene", name);
    FeatureFilter cdsgeneFilter = new FeatureFilter.And(cdsFilter, geneFilter);

    //FeatureHolder fh = seq.filter(cdsFilter,false);
    FeatureHolder fh = seq.filter(cdsgeneFilter, false);

    if (fh.countFeatures() == 0)return null;
    StrandedFeature sf = (StrandedFeature) fh.features().next();
    int cdsStrand = sf.getStrand().getValue();
    Location cdsLoc = sf.getLocation();
    Location loc = null, featLoc = null;
    StrandedFeature feat = null;
    SymbolList around = null;
    StrandedFeature.Template temp = new StrandedFeature.Template();
    Sequence aroundSeq = null;
    Annotation ann = null;
    FeatureFilter featFilter;
    FeatureFilter featFilter1 = new FeatureFilter.ByType(featStr);
    if (annKey != null && annValue != null) {
      FeatureFilter featFilter2 = new FeatureFilter.ByAnnotation(annKey,
          annValue);
      featFilter = new FeatureFilter.And(featFilter1, featFilter2);
    }
    else featFilter = featFilter1;
    fh = seq.filter(featFilter, false);
    int min = 10000;
    int dist = 10001;
    for (Iterator it = fh.features(); it.hasNext(); ) {
      sf = (StrandedFeature) it.next();
      loc = sf.getLocation();
      if (cdsStrand == 1) {
        dist = cdsLoc.getMin() - loc.getMax();
        if (dist > 0 && dist < min) {
          min = dist;
          featLoc = loc;
          feat = sf;
          ann = sf.getAnnotation();
        }
      }
      else {
        dist = loc.getMin() - cdsLoc.getMax();
        if (dist > 0 && dist < min) {
          min = dist;
          featLoc = loc;
          feat = sf;
          ann = sf.getAnnotation();
        }
      }
    }
    //make sequence using the found location

    if (featLoc == null)return null;

    if (feat.getStrand().getValue() == 1) {
      int start = featLoc.getMin() - bpBefore;
      if (start < 1) start = 1;
      int stop = featLoc.getMax() + bpAfter;
      if (stop > seq.length()) stop = seq.length();
      around = seq.subList(start, stop);
      //temp.type = featStr;
      //temp.strand = StrandedFeature.POSITIVE;
      //temp.source = "";
      //temp.annotation = ann;
      //temp.location = new RangeLocation(bpBefore,bpBefore+(featLoc.getMax()-featLoc.getMin()));
      //aroundSeq = new SimpleSequence(around, "around", seq.getName(),new SimpleAnnotation());
      //aroundSeq.createFeature(temp);
      aroundSeq = new SimpleSequence(around, "around", seq.getName(),
                                     Annotation.EMPTY_ANNOTATION);
    }
    else if (feat.getStrand().getValue() == -1) {
      int start = featLoc.getMin() - bpAfter;
      if (start < 1) start = 1;
      int stop = featLoc.getMax() + bpBefore;
      if (stop > seq.length()) stop = seq.length();
      around = seq.subList(start, stop);
      //temp.type = featStr;
      //temp.strand = StrandedFeature.NEGATIVE;
      //temp.source = "";
      //temp.annotation = ann;
      //temp.location = new RangeLocation(bpAfter,bpAfter+(featLoc.getMax()-featLoc.getMin()));
      //aroundSeq = new SimpleSequence(around, "around", seq.getName(),new SimpleAnnotation());
      //aroundSeq.createFeature(temp);
      aroundSeq = new SimpleSequence(around, "around", seq.getName(),
                                     Annotation.EMPTY_ANNOTATION);
    }

    return aroundSeq;
  }

  /**
   * @returns ArrayList of sequences
   */
  public ArrayList selectRealStrAroundFeature(String featStr, String annKey,
                                              String annValue, int bpBefore,
                                              int bpAfter) throws Exception {
    ArrayList seqList = new ArrayList();
    StrandedFeature f = null;
    int featStrand = 0;
    Location loc;
    SymbolList around = null;
    Sequence aroundSeq = null;
    StrandedFeature.Template temp = new StrandedFeature.Template();
    for (Iterator i = seq.features(); i.hasNext(); ) {
      aroundSeq = null;
      around = null;
      try {
        f = (StrandedFeature) i.next();
      }
      catch (Exception e) {
        System.out.println("Not a stranded feature in " + seq.getName() + ": " +
                           f.getType());
        //e.printStackTrace();
      }
      if (f.getType().equalsIgnoreCase(featStr)) {
        Annotation an = f.getAnnotation();
        for (Iterator j = an.keys().iterator(); j.hasNext(); ) {
          Object key = j.next();
          Object value = an.getProperty(key);
          if (key.toString().equalsIgnoreCase(annKey) &&
              value.toString().equalsIgnoreCase(annValue)) {
            loc = f.getLocation();
            //System.out.println("tcf location: "+loc +" for "+seq.getName());
            around = seq.subList(loc.getMin() - bpBefore + 1,
                                 loc.getMax() + bpAfter);
            if (f.getStrand().getValue() == -1) around = DNATools.
                reverseComplement(around);
            aroundSeq = new SimpleSequence(around, "around", "name",
                                           Annotation.EMPTY_ANNOTATION);
          }
        }
      }
      if (aroundSeq != null) seqList.add(aroundSeq);
    }
    return seqList;
  }

  public void checkAmbiguousFeatures(String featStr, String annKey,
                                     String annValue) {
    FeatureFilter featFilter = new FeatureFilter.ByType(featStr);
    FeatureFilter featFilter2 = new FeatureFilter.ByAnnotation(annKey, annValue);
    FeatureFilter theFilter;
    if (annKey != null && annValue != null) theFilter = new FeatureFilter.And(
        featFilter, featFilter2);
    else theFilter = featFilter;
    FeatureHolder fh = seq.filter(theFilter, false);
    if (fh.countFeatures() > 1) System.out.println(name + ": Feature " +
        featStr + " is ambiguous. Remove one of them.");
    if (fh.countFeatures() == 0) System.out.println(name +
        ": There is no such feature");
  }

  /**
   * This method creates a subsequence object, with all features
   * if there are more features with this annotation, the first is taken (or the last if on - strand)
   * @param minOffset = bp to be subtracted from the beginning and end of the feature
   */
  public Sequence selectBeforeFeat(String featStr, String annKey,
                                   String annValue, int bpBefore, int bpAfter,
                                   int minOffset) throws Exception {
    if (seq == null) return null;
    FeatureFilter featFilter = new FeatureFilter.ByType(featStr);
    FeatureFilter featFilter2 = new FeatureFilter.ByAnnotation(annKey, annValue);
    FeatureFilter theFilter;
    if (annKey != null && annValue != null) theFilter = new FeatureFilter.And(
        featFilter, featFilter2);
    else theFilter = featFilter;
    FeatureHolder fh = seq.filter(theFilter, false);
    StrandedFeature sf = null;
    SimpleSequence subseq = null;

    if (fh.countFeatures() == 0) return null;
    //if (fh.countFeatures()>1) throw new Exception (name +": Feature "+featStr+" is ambiguous. Remove one of them.");

    //check if more than one feature with this annotation
    if (fh.countFeatures() >= 1) {
      int min = seq.length();
      int max = 0;
      StrandedFeature tempf;
      for (Iterator it = fh.features(); it.hasNext(); ) {
        //while(fh.features().hasNext()){
        // tempf = (StrandedFeature)fh.features().next();
        tempf = (StrandedFeature) it.next();
        if (tempf.getStrand() == StrandedFeature.POSITIVE ||
            tempf.getStrand() == StrandedFeature.UNKNOWN) {
          if (tempf.getLocation().getMin() < min) {
            min = tempf.getLocation().getMin();
            sf = tempf;
          }
          //System.out.println(name + "(+): feature= "+tempf.getLocation().getMin()+"-"+tempf.getLocation().getMax()+"|"+tempf.getStrand().getToken());
        }
        else {
          if (tempf.getLocation().getMax() > max) {
            max = tempf.getLocation().getMax();
            sf = tempf;
          }
          //System.out.println(name + "(-): feature= "+tempf.getLocation().getMin()+"-"+tempf.getLocation().getMax()+"|"+tempf.getStrand().getToken());
        }
      }
    }
    //System.out.println(name + ": selected feature= "+sf.getLocation().getMin()+"-"+sf.getLocation().getMax()+"|"+sf.getStrand().getToken());
    //if (fh.countFeatures()==1){
    //    try{
    //        sf = (StrandedFeature)fh.features().next();
    //    } catch (Exception e){
    //              //not a stranded feature - > just select before as if it was on the + strand
    //                  System.out.println(name+": not a stranded feature selected");
    //                  Feature f = (Feature)fh.features().next();
    //                  Location loc = f.getLocation();
    //                  if ((loc.getMin()-bpBefore)<0) bpBefore=loc.getMin();
    //                  subseq = (SimpleSequence) makeSubSeq(loc.getMin()-bpBefore,loc.getMin()+bpAfter);
    //                  return subseq;
    //    }
    Location loc = sf.getLocation();
    if (sf.getStrand().getValue() == 1) {
      if ( (loc.getMin() - minOffset - bpBefore) < 0) {
        bpBefore = loc.getMin() - minOffset - 1;
        if (bpBefore < 0) bpBefore = 0; //offset goes out of sequence
      }
      //if ( (loc.getMin() - minOffset + bpAfter) > loc.getMax()) {
      //bpAfter = loc.getMax() - loc.getMin() + minOffset;
      //this is changed on 29aug 2005: now also for positive strand, bpAfter the exon is taken, even if this is longer than the feature itself	
      if ( (loc.getMin() - minOffset + bpAfter) > seq.length()) {
              bpAfter = seq.length() - loc.getMin() + minOffset;
      }
      System.out.println("POS "+bpBefore+"|"+bpAfter);
      //subseq = (SimpleSequence) makeSubSeq(loc.getMin()-bpBefore,loc.getMin()+bpAfter);
      subseq = (SimpleSequence) makeMySubSeq(loc.getMin() - minOffset -
                                             bpBefore,
                                             loc.getMin() - minOffset + bpAfter, null);
    }
    else if (sf.getStrand().getValue() == -1) {
      if ( (loc.getMax() + minOffset + bpBefore) > seq.length()) {
        bpBefore = seq.length() - loc.getMax() - minOffset;
        if (bpBefore < 0) bpBefore = 0;
      }
// P      if ( (loc.getMin() + minOffset - bpAfter) < 0) bpAfter = loc.getMax() +
// P         minOffset - 1;
     if ( (loc.getMax() + minOffset - bpAfter) < 0)  //P
       bpAfter = loc.getMax() + minOffset - 1; // P
      //subseq = (SimpleSequence) makeSubSeq(loc.getMax()-bpAfter,loc.getMax()+bpBefore);
     System.out.println("NEG "+bpBefore+"|"+bpAfter);
      subseq = (SimpleSequence) makeMySubSeq(loc.getMax() + minOffset - bpAfter,
                                             loc.getMax() + minOffset +
                                             bpBefore, null);
    }
    //System.out.println(this.name+"|"+subseq.length());
    //}
    return subseq;
  }
  
  //WARNING: all annotation/features are lost !! 
  //This works only on fasta files
  public void maskFromTo(int start,int end) throws Exception{
  	SimpleSequence returnSeq;
    if (start < 1) start = 1;
    if (end > length) end = length;
    int length=end-start+1;
    char[] ns = new char[length];
    for(int i =0;i<ns.length;i++){
    		ns[i]='n';
    }
    String nsStr=new String(ns);
    String newseq = seq.seqString().substring(0,start-1)+nsStr+seq.seqString().substring(end,seq.length());
    //System.out.println(newseq);
    returnSeq = new SimpleSequence(DNATools.createDNA(newseq),
        seq.getURN(), seq.getName(), seq.getAnnotation());
    setSequence(returnSeq,this.name);
    //return returnSeq;
  }

  /**
   * TODO: non-contiguous locations that overlap the subseq location
   */
  public SimpleSequence makeMySubSeq(int start, int end, String newName) throws
      Exception {
    SimpleSequence returnSeq;
    if (start < 1) start = 1;
    if (end > length) end = length;
    if (newName == null) returnSeq = new SimpleSequence(seq.subList(start, end),
        seq.getURN(), seq.getName(), seq.getAnnotation());
    else returnSeq = new SimpleSequence(seq.subList(start, end), newName,
                                        newName, seq.getAnnotation());
    //System.out.println("length of subseq="+returnSeq.length());
    Object temp;
    Feature.Template templ = null;
    for (Iterator i = seq.features(); i.hasNext(); ) {
      temp = i.next();
      try {
        StrandedFeature f = (StrandedFeature) temp;
        if ( (f.getLocation().getMax() > start &&
              f.getLocation().getMax() < end) ||
            (f.getLocation().getMin() < end && f.getLocation().getMin() > start)) {
          templ = f.makeTemplate();
          if (f.getLocation().isContiguous()){
          if (f.getLocation().getMin() < start) templ.location = new
              RangeLocation(1, f.getLocation().getMax() - start);
          else if (f.getLocation().getMax() > end) templ.location = new
              RangeLocation(f.getLocation().getMin() - start, end - start);
          else templ.location = new RangeLocation(f.getLocation().getMin() -
                                                  start,
                                                  f.getLocation().getMax() - start);
          }
          else{
            templ.location = null;
            for (Iterator it = f.getLocation().blockIterator(); it.hasNext();){
                Location loc = (Location)it.next();
                if (templ.location == null) {
                  if (loc.getMin() < start) templ.location = new RangeLocation(1, loc.getMax() - start);
                  else if (loc.getMax() > end) templ.location = new RangeLocation(loc.getMin() - start, end - start);
                  else templ.location = new RangeLocation(loc.getMin()-start,loc.getMax()-start);
                }
                else {
                  if (loc.getMin() < start) templ.location = templ.location.union(new
                      RangeLocation(1, loc.getMax() - start));
                  else if (loc.getMax() > end) templ.location = templ.location.
                      union(new RangeLocation(loc.getMin() - start, end - start));
                  else templ.location = templ.location.union(new RangeLocation(loc.getMin()-start,loc.getMax()-start));
                }
            }
          }
          returnSeq.createFeature(templ);
        }
      }
      catch (Exception e) { //not stranded
        try {
          Feature f = (Feature) temp;
          if ( (f.getLocation().getMax() > start &&
                f.getLocation().getMax() < end) ||
              (f.getLocation().getMin() < end &&
               f.getLocation().getMin() > start)) {
            templ = f.makeTemplate();
            //if (f.getLocation().isContiguous()){
            if (f.getLocation().getMin() < start) templ.location = new
                RangeLocation(1, f.getLocation().getMax() - start);
            else if (f.getLocation().getMax() > end) templ.location = new
                RangeLocation(f.getLocation().getMin() - start + 1, end - start);
            else templ.location = new RangeLocation(f.getLocation().getMin() -
                start + 1, f.getLocation().getMax() - start);
            //}
            //else{
            //  for (Iterator it = f.getLocation().blockIterator(); it.hasNext();){
            //      Location loc = (Location)it.next();
            //      if (f.getLocation().getMin()<start) templ.location = new RangeLocation(1,f.getLocation().getMax()-start);
            //      else if (f.getLocation().getMax()>end) templ.location = new RangeLocation(f.getLocation().getMin()-start,end-start);
            //      else templ.location = new RangeLocation(f.getLocation().getMin()-start,f.getLocation().getMax()-start);
            //  }
            //}
            returnSeq.createFeature(templ);
          }
        }
        catch (Exception ex) {
          ex.printStackTrace();
        }
      }
    }
    return returnSeq;
  }

  public SimpleSequence makeSubSeq(int start, int end) throws Exception {
    SubSequence subseq = new SubSequence(seq, start, end);
    System.out.println(start + "|" + end);
    Sequence tempSeq = (Sequence)new ViewSequence(subseq);

    //testing -----------------------
    System.out.println("\n");
    for (Iterator it = seq.features(); it.hasNext(); ) {
      Object temp = it.next();
      try {
        StrandedFeature f = (StrandedFeature) temp;
        System.out.println(name + ": " + f.getType() + "|" +
                           f.getStrand().getToken() + "|" + f.getLocation() +
                           "|" + f.getAnnotation());
      }
      catch (Exception e) {
        Feature f = (Feature) temp;
        System.out.println(name + ": " + f.getType() + "|" + f.getLocation() +
                           "|" + f.getAnnotation());
      }
    }
    System.out.println("\n");

    System.out.println("\n");
    for (Iterator it = subseq.features(); it.hasNext(); ) {
      Object temp = it.next();
      try {
        StrandedFeature f = (StrandedFeature) temp;
        System.out.println(name + ": " + f.getType() + "|" +
                           f.getStrand().getToken() + "|" + f.getLocation() +
                           "|" + f.getAnnotation());
      }
      catch (Exception e) {
        Feature f = (Feature) temp;
        System.out.println(name + ": " + f.getType() + "|" + f.getLocation() +
                           "|" + f.getAnnotation());
      }
    }
    System.out.println("\n");

    //testing:
    SimpleSequence returnSeq = new SimpleSequence(tempSeq.subList(1,
        tempSeq.length()), tempSeq.getURN(), tempSeq.getName(),
                                                  tempSeq.getAnnotation());

    //end testing----------------------------------------------------


    //this part doesn't seem necessary anymore in the new biojava...
    //It was here to set locations to 1 if they were minus and to the max if the exceeded it
    /*
       //set locations right...
           Feature f = null;
       SimpleSequence returnSeq = new SimpleSequence(tempSeq.subList(1,tempSeq.length()),tempSeq.getURN(),tempSeq.getName(),Annotation.EMPTY_ANNOTATION);
       for (Iterator i = tempSeq.features(); i.hasNext(); ) {
              f = (Feature)i.next();
              Feature.Template templ = f.makeTemplate();
     if (f.getLocation().getMin()<1 || f.getLocation().getMax()>returnSeq.length()) {
                 //returnSeq.removeFeature(f);
                 if (f.getLocation().getMin()<1 && f.getLocation().getMax()>returnSeq.length()) templ.location = new RangeLocation(1,returnSeq.length());
                 else if (f.getLocation().getMin()<1) templ.location = new RangeLocation(1,f.getLocation().getMax());
                 else if (f.getLocation().getMax()>returnSeq.length()) templ.location = new RangeLocation(f.getLocation().getMin(),returnSeq.length());
              }
              returnSeq.createFeature(templ);
           }
           returnSeq.setName(seq.getName());
     */
    return returnSeq;
  }

  public SimpleSequence makeSubSeq2(int start, int end) throws Exception {
    Sequence smallSequence = new SubSequence(this.seq, start, end);
    SimpleSequence returnSeq = new SimpleSequence(smallSequence.subList(1,
        smallSequence.length()), seq.getURN(), seq.getName(),
                                                  smallSequence.getAnnotation());

    //testing
    for (Iterator it = smallSequence.features(); it.hasNext(); ) {
      Object temp = it.next();
      try {
        StrandedFeature f = (StrandedFeature) temp;
        System.out.println(name + ": " + f.getType() + "|" +
                           f.getStrand().getToken() + "|" + f.getLocation() +
                           "|" + f.getAnnotation());
      }
      catch (Exception e) {
        Feature f = (Feature) temp;
        System.out.println(name + ": " + f.getType() + "|" + f.getLocation() +
                           "|" + f.getAnnotation());
      }
    }

    return returnSeq;
  }

  /**
   * This method creates a new sequence object, where only the selected feature is annotated
   * All other annotation is removed.
   * @deprecated
   */
  public Sequence selectBeforeFeature(String featStr, String annKey,
                                      String annValue, int bpBefore,
                                      int bpAfter) throws Exception {
    StrandedFeature f = null;
    int featStrand = 0;
    Location loc;
    SymbolList before = null;
    StrandedFeature.Template temp = new StrandedFeature.Template();
    Sequence beforeSeq = null;
    for (Iterator i = seq.features(); i.hasNext(); ) {
      try {
        f = (StrandedFeature) i.next();
      }
      catch (Exception e) {
        //e.printStackTrace();
        System.out.println("Not a stranded feature in " + seq.getName() + ": " +
                           f.getType());
      }
      if (f.getType().equalsIgnoreCase(featStr)) {
        Annotation an = f.getAnnotation();
        for (Iterator j = an.keys().iterator(); j.hasNext(); ) {
          Object key = j.next();
          Object value = an.getProperty(key);
          if (key.toString().equalsIgnoreCase(annKey) &&
              value.toString().equalsIgnoreCase(annValue)) {
            loc = f.getLocation();
            if (f.getStrand().getValue() == 1) {
              featStrand = 1;
              if ( (loc.getMin() - bpBefore) < 0) bpBefore = loc.getMin();
              before = seq.subList(loc.getMin() - bpBefore,
                                   loc.getMin() + bpAfter);
              temp.type = featStr;
              temp.strand = StrandedFeature.POSITIVE;
              temp.source = "";
              temp.annotation = Annotation.EMPTY_ANNOTATION;
              temp.location = new RangeLocation(bpBefore, (bpBefore + bpAfter));
              beforeSeq = new SimpleSequence(before, "before", seq.getName(),
                                             new SimpleAnnotation());
              beforeSeq.createFeature(temp);
            }
            else if (f.getStrand().getValue() == -1) {
              featStrand = -1;
              if ( (loc.getMax() + bpBefore) > seq.length()) bpBefore = seq.
                  length() - loc.getMax();
              before = seq.subList(loc.getMax() - bpAfter,
                                   loc.getMax() + bpBefore);
              temp.type = featStr;
              temp.strand = StrandedFeature.NEGATIVE;
              temp.source = "";
              temp.annotation = Annotation.EMPTY_ANNOTATION;
              temp.location = new RangeLocation(1, bpAfter);
              beforeSeq = new SimpleSequence(before, "before", seq.getName(),
                                             new SimpleAnnotation());
              beforeSeq.createFeature(temp);
              before = DNATools.reverseComplement(before);
            }
          } //end if
        } //end for
      }
    }
    return beforeSeq;
  }

  public ArrayList selectModule(String featName1, String annKey1,
                                String annValue1, String featName2,
                                String annKey2, String annValue2,
                                int maxBpBetween, int aroundMiddle) throws
      Exception {
    ArrayList seqList = new ArrayList();
    FeatureFilter tempFilter1 = new FeatureFilter.ByType(featName1);
    FeatureFilter tempFilter2 = new FeatureFilter.ByAnnotation(annKey1,
        annValue1);
    FeatureFilter featFilter1 = new FeatureFilter.And(tempFilter1, tempFilter2);
    tempFilter1 = new FeatureFilter.ByType(featName2);
    tempFilter2 = new FeatureFilter.ByAnnotation(annKey2, annValue2);
    FeatureFilter featFilter2 = new FeatureFilter.And(tempFilter1, tempFilter2);

    FeatureHolder fh1 = seq.filter(featFilter1, false);
    FeatureHolder fh2 = seq.filter(featFilter2, false);

    FeatureFilter theFilter = new FeatureFilter.Or(featFilter1, featFilter2);
    FeatureHolder fh = seq.filter(theFilter, false);
    Feature f1 = null, f2 = null;
    SimpleSequence s = null;
    int nr = 0;
    for (Iterator i = fh1.features(); i.hasNext(); ) {
      f1 = (Feature) i.next();
      //System.out.println(f.getType()+"|"+f.getLocation().getMin()+"|"+f.getLocation().getMax());
      for (Iterator j = fh2.features(); j.hasNext(); ) {
        s = null;
        f2 = (Feature) j.next();
        int range = f1.getLocation().getMin() - f2.getLocation().getMax();
        int range2 = f2.getLocation().getMin() - f1.getLocation().getMax();
        if (range < maxBpBetween && (range) > 0) {
          int middle = f1.getLocation().getMin() - (range / 2);
          int start = middle - aroundMiddle;
          if (start < 1) start = 1;
          int end = middle + aroundMiddle;
          if (end > seq.length()) end = seq.length();
          s = (SimpleSequence)this.makeSubSeq(start, end);
        }
        else if (range2 < maxBpBetween && range2 > 0) {
          int middle = f1.getLocation().getMin() + (range2 / 2);
          int start = middle - aroundMiddle;
          if (start < 1) start = 1;
          int end = middle + aroundMiddle;
          if (end > seq.length()) end = seq.length();
          s = (SimpleSequence)this.makeSubSeq(start, end);
        }
        if (s != null) {
          s.setName(this.name + "_" + nr);
          nr++;
          seqList.add(s);
        }
      }
    }
    return seqList;
  }

  /**
   * @deprecated
   * @param bpBefore int
   * @param bpAfter int
   * @param dbName String
   * @throws Exception
   */
  public void setSeqBeforeTranscriptStart(int bpBefore, int bpAfter,
                                          String dbName) throws Exception {
    setTranscriptDiff(dbName);
    System.out.println(name + "|" + this.transcriptDiff);
    setSequence(selectBeforeFeat("CDS", "gene", this.name, bpBefore, bpAfter,
                                 this.transcriptDiff), this.name);
  }

  public void reverseComplement() throws Exception {
    SymbolList temp = seq.subList(1, seq.length());
    temp = DNATools.reverseComplement(temp);
    //System.out.println(temp.seqString());
    SimpleSequence ret = new SimpleSequence(temp, seq.getURN(), seq.getName(),
                                            seq.getAnnotation());
    //Annotation ann = seq.getAnnotation();
    //Object o;
    //Feature f;
    Feature f = null;
    //Feature.Template t;
    int l = seq.length();
    StrandedFeature.Template t = null;
    Object itNext=null;
    for (Iterator it = seq.features(); it.hasNext(); ) {
      try {
      	itNext = it.next();
        f = (StrandedFeature) itNext;
        //f=(StrandedFeature)o;
        //System.out.println(f.getLocation()+"|"+f.getType());
        t = (StrandedFeature.Template) f.makeTemplate();
        //System.out.println(((StrandedFeature)f).getStrand());
        if ( ( (StrandedFeature) f).getStrand() == StrandedFeature.POSITIVE)
          t.strand = StrandedFeature.NEGATIVE;
        else if ( ( (StrandedFeature) f).getStrand() ==
                 StrandedFeature.NEGATIVE)
          t.strand = StrandedFeature.POSITIVE;
        else t.strand = StrandedFeature.UNKNOWN;
        if (f.getLocation().isContiguous()) {
          t.location = new RangeLocation(l - f.getLocation().getMax() + 1,
                                         l - f.getLocation().getMin() + 1);
        }
        else { //no contiguous location!
          Iterator iter = f.getLocation().blockIterator();
          //System.out.println(f.getType());
          Location tempLoc = (Location) iter.next();
          Location tempLoc2 = (Location) iter.next();
          t.location = LocationTools.union(new RangeLocation(l - tempLoc.getMax() +
              1, l - tempLoc.getMin() + 1),
                                           new RangeLocation(l - tempLoc2.getMax() +
              1, l - tempLoc2.getMin() + 1));
          while (iter.hasNext()) {
            //tempLoc = (Location)f.getLocation().blockIterator().next();
            tempLoc = (Location) iter.next();
            tempLoc2 = new RangeLocation(l - tempLoc.getMax() + 1,
                                         l - tempLoc.getMin() + 1);
            t.location = LocationTools.union(t.location, tempLoc2);
          }
        }
        t.annotation = f.getAnnotation();
        ret.createFeature(t);
        //System.out.println(t.location+"|"+t.type+"|"+t.strand);
      }
      catch (java.lang.IllegalArgumentException iae) {
        System.out.println("Problems rev-complementing feature " + f.getType() +
                           "|" + f.getLocation().getMin() + "|" +
                           f.getLocation().getMax());
        //System.out.println("Problems rev-complementing feature "+t.type+"|"+t.location.getMin()+"|"+t.location.getMax());
      }
      catch (org.biojava.bio.BioException be) {
        String genefeat = "N.A.";
        if (f.getAnnotation().keys().contains("gene"))
          genefeat = (String) f.getAnnotation().getProperty("gene");
        System.out.println("Problems rev-complementing feature " + f.getType() +
                           "|" + genefeat + "|" +
                           f.getLocation().getMin() + "|" +
                           f.getLocation().getMax());
        //System.out.println("Problems rev-complementing feature "+t.type+"|"+t.location.getMin()+"|"+t.location.getMax());
      }
      catch (Exception e) {
        /** @todo  */
        f = (Feature)itNext;
             
            
            Feature.Template t2 = (Feature.Template) f.makeTemplate();
            if (f.getLocation().isContiguous()) {
              t2.location = new RangeLocation(l - f.getLocation().getMax() + 1,
                                             l - f.getLocation().getMin() + 1);
            }
            else { //no contiguous location!
              Iterator iter = f.getLocation().blockIterator();
              //System.out.println(f.getType());
              Location tempLoc = (Location) iter.next();
              Location tempLoc2 = (Location) iter.next();
              t2.location = LocationTools.union(new RangeLocation(l - tempLoc.getMax() +
                  1, l - tempLoc.getMin() + 1),
                                               new RangeLocation(l - tempLoc2.getMax() +
                  1, l - tempLoc2.getMin() + 1));
              while (iter.hasNext()) {
                //tempLoc = (Location)f.getLocation().blockIterator().next();
                tempLoc = (Location) iter.next();
                tempLoc2 = new RangeLocation(l - tempLoc.getMax() + 1,
                                             l - tempLoc.getMin() + 1);
                t2.location = LocationTools.union(t.location, tempLoc2);
              }
            }
            t2.annotation = f.getAnnotation();
            ret.createFeature(t2);
         
    }//end for
      }//end catch
    this.seq = ret;
    if(cdsPositive) cdsPositive = false;
    else if(!cdsPositive) cdsPositive = true;
  }

  public GeneList cutInPieces(int windowLength, int overlap, int offset) throws
      Exception {
    //System.out.println(name + "(" + length + "): ");
    GeneList gl = new GeneList();
    Gene g;
    int pos = offset + 1;
    int i = 1;
    boolean last = false;
    while (pos <= length) {
      if (pos + windowLength > length) {
        windowLength = length - pos + 1;
        last = true;
      }
      g = new Gene();
      g.setSequence(makeMySubSeq(pos, pos + windowLength - 1, name + "-" + (i)),
                    name + "-" + (i));
      i++;
      //System.out.println("g=" + g.name + "(" + g.length + ")");
      System.out.println(">" + g.name + "\n" + g.seq.seqString());
      
      gl.add(g);
      //System.out.println(".");
      if (!last) pos = pos + windowLength - overlap;
      else break;
      //System.out.println("pos="+pos);
    }
    System.out.println("\n");
    return gl;
  }

  public double calculateFrequency(String featName, String annKey,
                                   String annValue) {
    FeatureFilter featFilter = new FeatureFilter.ByAnnotation(annKey, annValue);
    FeatureHolder fh = seq.filter(featFilter, false);
    double freq = fh.countFeatures() / this.seq.length();
    return freq;
  }

  public int nrFeatures(String featName, String annKey, String annValue) {
    //FeatureFilter featFilter = new FeatureFilter.ByType(featName);
    //FeatureFilter featFilter2 = new FeatureFilter.ByAnnotation(annKey,annValue);
    //FeatureFilter theFilter;
    //if (annKey!=null && annValue!=null) theFilter = new FeatureFilter.And(featFilter,featFilter2);
    //else theFilter = featFilter;
    if (seq == null)return 0;
    FeatureFilter featFilter;
    FeatureHolder fh;
    if (annKey != null && annValue != null) {
      featFilter = new FeatureFilter.And(new FeatureFilter.ByAnnotation(annKey,
          annValue), new FeatureFilter.ByType(featName));
      fh = seq.filter(featFilter, false);
    }
    else {
      featFilter = new FeatureFilter.ByType(featName);
      fh = seq.filter(featFilter, false);
    }
    //System.out.println(seq+"|"+featFilter);
    //FeatureHolder fh = seq.filter(theFilter,false);
    return fh.countFeatures();
  }

  public HashMap allNrFeatures(String featName) {
    HashMap featMap = new HashMap();
    FeatureFilter featFilter = new FeatureFilter.ByType(featName);
    FeatureHolder fh = seq.filter(featFilter, false);
    String type = "";
    int nr = 0;
    for (Iterator it = fh.features(); it.hasNext(); ) {
      Annotation ann = ( (Feature) it.next()).getAnnotation();
      type = (String) ann.getProperty("type");
      nr = nrFeatures(featName, "type", type);
      //double freq = (new Double(""+nr)).doubleValue()/(new Double(""+length).doubleValue())*1000;
      if (!featMap.containsKey(type)) featMap.put(type, new Integer(nr));
      //System.out.println(type+"|"+freq);
    }
    return featMap;
  }
  
  public void annotatePWM(PWM pwm,double threshold,String pwmName) throws Exception{

    WeightMatrixAnnotator wma = new WeightMatrixAnnotator(pwm.wm, ScoreType.ODDS,threshold,"misc_feature");
    
    Alphabet dna = DNATools.getDNA();
    SymbolTokenization dnaToke = dna.getTokenization("token");
    Sequence seqTemp;
    seqTemp = new SimpleSequence(new SimpleSymbolList(dnaToke, seq.seqString()),
                                             "temp", "temp", Annotation.EMPTY_ANNOTATION);
    seqTemp = wma.annotate(seqTemp);
    StrandedFeature.Template st;
    Feature f;
    StrandedFeature sf;
    double score;
    for (Iterator it = seqTemp.features(); it.hasNext(); ) {
        f = (Feature)it.next();
        score = ((Double)f.getAnnotation().getProperty("score")).doubleValue() ;
        this.annotateStrandedFeature(f.getLocation().getMin(),f.getLocation().getMax(),StrandedFeature.POSITIVE,pwmName,score,seqTemp.subStr(f.getLocation().getMin(),f.getLocation().getMax()),f.getSource());
    }
    
    String site="";
	this.reverseComplement();
	seqTemp = new SimpleSequence(new SimpleSymbolList(dnaToke, seq.seqString()),
            "temp", "temp", Annotation.EMPTY_ANNOTATION);
seqTemp = wma.annotate(seqTemp);
for (Iterator it = seqTemp.features(); it.hasNext(); ) {
f = (Feature)it.next();
score = ((Double)f.getAnnotation().getProperty("score")).doubleValue() ;
//site = DNATools.reverseComplement(seqTemp.subList(f.getLocation().getMin(),f.getLocation().getMax())).seqString();
site = seqTemp.subStr(f.getLocation().getMin(),f.getLocation().getMax())+"-rev";
this.annotateStrandedFeature(f.getLocation().getMin(),f.getLocation().getMax(),StrandedFeature.POSITIVE,pwmName,score,site,f.getSource());
}
this.reverseComplement();
    
    /*seqTemp = new SimpleSequence(new SimpleSymbolList(dnaToke, seq.seqString()),
        "temp", "temp", Annotation.EMPTY_ANNOTATION); 
	Gene tg = new Gene();
    tg.setSequence(seqTemp,"temp");
	tg.reverseComplement();
    tg.seq = wma.annotate(tg.seq);
    tg.reverseComplement();
    for (Iterator it = seqTemp	.features(); it.hasNext(); ) {
        f = (Feature)it.next();
        this.annotateStrandedFeature(f.getLocation().getMin(),f.getLocation().getMax(),StrandedFeature.NEGATIVE,pwmName,Double.parseDouble((String)f.getAnnotation().getProperty("score")),"",f.getSource());
    }
    */
    
    
    
   /* System.out.println(this.name);
    for (Iterator it = seq.features(); it.hasNext(); ) {
      f = (Feature)it.next();
      System.out.println(f.getType()+"|"+f.getSource());
      for (Iterator it2 = f.getAnnotation().keys().iterator(); it2.hasNext();){
      	System.out.println(it2.next());
      }
      Location loc = f.getLocation();
      //if(f.getType().equalsIgnoreCase("hit")){
      	System.out.println("Match at " + loc.getMin()+"-"+loc.getMax());
      	if(f.getAnnotation().containsProperty("score")) System.out.println("\tscore : "+f.getAnnotation().getProperty("score"));
      //}
    }
    */
  }

  public int annotateIupac(String name, String iupac, double score, String source) throws Exception {
    char c;
    String ret = "";
    iupac = iupac.toLowerCase();
    for (int i = 0; i < iupac.length(); i++) {
      c = iupac.charAt(i);
      if (c == 'r') ret += "[ag]";
      else if (c == 'y') ret += "[ct]";
      else if (c == 'w') ret += "[at]";
      else if (c == 's') ret += "[gc]";
      else if (c == 'm') ret += "[ac]";
      else if (c == 'k') ret += "[gt]";
      else if (c == 'h') ret += "[act]";
      else if (c == 'b') ret += "[gct]";
      else if (c == 'v') ret += "[gac]";
      else if (c == 'd') ret += "[gat]";
      else if (c == 'n') ret += "[gact]";
      //if (c=='t' || c=='g' || c=='c' || c=='a') ret += c;
      else ret += c;
    }
    //System.out.println("IUPAC conversion: "+ret);
    return this.annotateSingleMotif(name, ret, score, source);
  }

  public int annotateIupacWindow(String motifName, String iupac,
                                 int windowLength, int step, int minNrMotifs) throws Exception {
    char c;
    String ret = "";
    iupac = iupac.toLowerCase();
    for (int i = 0; i < iupac.length(); i++) {
      c = iupac.charAt(i);
      if (c == 'r') ret += "[ag]";
      else if (c == 'y') ret += "[ct]";
      else if (c == 'w') ret += "[at]";
      else if (c == 's') ret += "[gc]";
      else if (c == 'm') ret += "[ac]";
      else if (c == 'k') ret += "[gt]";
      else if (c == 'h') ret += "[act]";
      else if (c == 'b') ret += "[gct]";
      else if (c == 'v') ret += "[gac]";
      else if (c == 'd') ret += "[gat]";
      else if (c == 'n') ret += "[gact]";
      //if (c=='t' || c=='g' || c=='c' || c=='a') ret += c;
      else ret += c;
    }
    //System.out.println("IUPAC conversion: "+ret);
    return annotateMotifWindow(motifName, ret,
                                 windowLength, step, minNrMotifs);
  }

  public String iupacToRegEx(String iupac){
  	char c;
  	String ret = "";
  	iupac = iupac.toLowerCase();
  	for (int i = 0; i < iupac.length(); i++) {
  	c = iupac.charAt(i);
  	if (c == 'r') ret += "[ag]";
  	else if (c == 'y') ret += "[ct]";
  	else if (c == 'w') ret += "[at]";
  	else if (c == 's') ret += "[gc]";
  	else if (c == 'm') ret += "[ac]";
  	else if (c == 'k') ret += "[gt]";
  	else if (c == 'h') ret += "[act]";
  	else if (c == 'b') ret += "[gct]";
  	else if (c == 'v') ret += "[gac]";
  	else if (c == 'd') ret += "[gat]";
  	else if (c == 'n') ret += "[gact]";
  	else ret += c;
  	}
  	return ret;
  }

  public int annotateIupacPair(String motifName1, String iupac1, String motifName2, String iupac2,
        int windowLength, int step) throws Exception {

return annotateMotifPair(motifName1, iupacToRegEx(iupac1),motifName2,iupacToRegEx(iupac2),windowLength, step);
}

  public void getUCSCSeq(String dbName, String id, String idType, String dbUrl,int bpUp)throws Exception{
     dbUrl = "jdbc:mysql://genome-mysql.cse.ucsc.edu/"+dbName+"?user=genome&password=";
     String sql, sql1, result;
    Connection conn = null;
    Statement s, s1, s2 = null;
    ResultSet rs, rs1, rs2 = null;
    Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    conn = DriverManager.getConnection(dbUrl);
    s = conn.createStatement();
    s1 = conn.createStatement();
    s2 = conn.createStatement();



  }

  public int annotateSingleMotif(String motifName, String motif, double score, String source) throws  
  Exception {
  	int ret=0;
    PatternCompiler compiler = new Perl5Compiler();
    PatternMatcher matcher = new Perl5Matcher();
    Pattern pattern = compiler.compile(motif);
    PatternMatcherInput input;
    MatchResult result;
    int groups;
    SymbolList dna = seq.subList(1, seq.length());
    //direct
    String dnaStr = dna.seqString();
    input = new PatternMatcherInput(dnaStr);
    while (matcher.contains(input, pattern)) {
      StrandedFeature.Template template = new StrandedFeature.Template();
      template.type = "misc_feature";
      template.source = source;
      template.strand = StrandedFeature.POSITIVE;
      template.location = new RangeLocation(matcher.getMatch().beginOffset(0)+1,
                                            matcher.getMatch().endOffset(0));
      template.annotation = Annotation.EMPTY_ANNOTATION;
      template.annotation = new SimpleAnnotation();
      template.annotation.setProperty("score", String.valueOf(score));
      template.annotation.setProperty("type", motifName);
      template.annotation.setProperty("note", "Single Motif");
      template.annotation.setProperty("occ", matcher.getMatch().toString());
      seq.createFeature(template);
      ret++;
    }
    //reverse complement
    dnaStr = DNATools.reverseComplement(dna).seqString();
    input = new PatternMatcherInput(dnaStr);
    while (matcher.contains(input, pattern)) {
      StrandedFeature.Template template = new StrandedFeature.Template();
      template.type = "misc_feature";
      template.source = source;
      template.strand = StrandedFeature.NEGATIVE;
      template.location = new RangeLocation(seq.length() -
                                            matcher.getMatch().endOffset(0)+1,
                                            seq.length() -
                                            matcher.getMatch().beginOffset(0));
      template.annotation = new SimpleAnnotation();
      template.annotation.setProperty("type", motifName);
      template.annotation.setProperty("note", "Single Motif");
      template.annotation.setProperty("score", String.valueOf(score));
      template.annotation.setProperty("occ", matcher.getMatch().toString());
      seq.createFeature(template);
      ret++;
    }
    return ret;
  }

  public void annotateFeature(int start, int end, String type) throws Exception {
    Feature.Template template = new Feature.Template();
    template.type = "misc_feature";
    template.location = new RangeLocation(start, end);
    template.annotation = new SimpleAnnotation();
    template.annotation.setProperty("type", type);
    seq.createFeature(template);
  }


  public void annotateStrandedFeature(int start, int end, String strand,
                                      String type) throws Exception {
    StrandedFeature.Template template = new StrandedFeature.Template();
    template.type = "misc_feature";
    if (strand.equals("+")) template.strand = StrandedFeature.POSITIVE;
    else if (strand.equals("-")) template.strand = StrandedFeature.NEGATIVE;
    else template.strand = StrandedFeature.UNKNOWN;
    template.location = new RangeLocation(start, end);
    template.annotation = new SimpleAnnotation();
    template.annotation.setProperty("type", type);
    seq.createFeature(template);
  }

  /**
   * same as previous but takes a Strand object instead of its token
   */
  public void annotateStrandedFeature(int start, int end,
                                      StrandedFeature.Strand strand,
                                      String type) throws Exception {
    StrandedFeature.Template template = new StrandedFeature.Template();
    template.type = "misc_feature";
    template.strand = strand;
    template.location = new RangeLocation(start, end);
    template.annotation = new SimpleAnnotation();
    template.annotation.setProperty("type", type);
    seq.createFeature(template);
  }

  public void annotateStrandedFeature(int start, int end,
                                      StrandedFeature.Strand strand,
                                      String type, double score, String site,
                                      String source) throws Exception {
    try{
      StrandedFeature.Template template = new StrandedFeature.Template();
      template.source = source;
      template.type = "misc_feature";
      template.strand = strand;
      template.location = new RangeLocation(start, end);
      template.annotation = new SimpleAnnotation();
      template.annotation.setProperty("type", type);
      template.annotation.setProperty("score", Double.toString(score));
      template.annotation.setProperty("site", site);
      template.annotation.setProperty("source", source);
      seq.createFeature(template);
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  /**
   * @return  xmlDocument to be used in a plotviewer;
   * @dtd     AlignmentPlot.dtd
   * @deprecated
   */
  public Document makeCompleteXml() throws Exception {
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
      buf.append("</Factor>");
      buf.append("\n");
    }
    ArrayList objList = this.getFactorInstances(objMap);
    buf.append(makeSeqXmlString(objList, "seq1"));
    buf.append("</AlignmentPlot>");
    buf.append("\n");

    Document xmlDoc = toucan.util.XMLParser.parseString(buf.toString());
    return xmlDoc;
  }

  public String makeSeqXmlString(ArrayList objList, String seqId) {
    StringBuffer buf = new StringBuffer();
    buf.append("<aSequence seqID=\"" + seqId + "\">");
    buf.append("\n");
    buf.append("<descriptor>" + name + "</descriptor>");
    buf.append("\n");
    buf.append("<length>" + length + "</length>");
    buf.append("\n");
    int cnt = 0;
    FactorInstance oi = null;
    for (Iterator i = objList.iterator(); i.hasNext(); ) {
      cnt++;
      oi = (FactorInstance) i.next();
      buf.append("<FactorInstance instanceID=\"" + cnt + "\">");
      buf.append("<instanceStrand>" + oi.instanceStrand + "</instanceStrand>");
      buf.append("<instanceStart>" + oi.instanceStart + "</instanceStart>");
      buf.append("<instanceStop>" + oi.instanceStop + "</instanceStop>");
      buf.append("<instanceScore>" + oi.instanceScore + "</instanceScore>");
      buf.append("<instanceType>" + oi.instanceType + "</instanceType>");
      buf.append("</FactorInstance>");
      buf.append("\n");
    }
    buf.append("</aSequence>");
    return buf.toString();
  }

  /**
   * HashMap: key= type of feature (e.g. "TCF3-site")
   * value = Factor object
   */
  public TreeMap getObjects() {
    if (objects == null) updateObjectMap();
    return objects;
  }

  /**
   * HashMap: key= type of feature (e.g. "TCF3-site")
   * value = Factor object
   */
  public TreeMap updateObjectMap() {
    if (objects == null) {
      objects = new TreeMap();
    }
    if (seq == null)return objects;
    Factor myObj;
    Feature f = null;
    for (Iterator i = seq.features(); i.hasNext(); ) {
      try {
        f = (Feature) i.next();
      }
      catch (Exception e) {
        //e.printStackTrace();
        //System.out.println("Not a stranded feature in "+seq.getName()+"|"+f.getType());
      }
      //System.out.println(f.getType());
      //for misc_features
      if (f.getType().equalsIgnoreCase("misc_feature") || f.getType().equalsIgnoreCase("hit")) {
        //System.out.println("misc found");
        Annotation an = f.getAnnotation();
        for (Iterator j = an.keys().iterator(); j.hasNext(); ) {
          Object key = j.next();
          Object value = an.getProperty(key);
          //System.out.println(key.toString()+"|"+value.toString());
          if (key.toString().equalsIgnoreCase("type") &&
              !objects.containsKey(value.toString())) {
            myObj = new Factor();
            myObj.objectID = "" + objects.size();
            myObj.objectType = "misc_feature";
            myObj.objectSource = f.getSource();
            myObj.objectDescriptor = value.toString();
            objects.put(myObj.objectDescriptor, myObj);
          }
        }
      }
      //For all other features
      //else if (f.getType().equalsIgnoreCase("CDS") && !objects.containsKey(f.getType())) {
      //else if (!f.getType().equalsIgnoreCase("source") && !f.getType().equalsIgnoreCase("intron") && !objects.containsKey(f.getType())) {
      else if (!f.getType().equalsIgnoreCase("source") &&
               !objects.containsKey(f.getType())) {
        myObj = new Factor();
        myObj.objectID = "" + objects.size();
        myObj.objectType = "Original feature";
        myObj.objectDescriptor = f.getType();
        myObj.objectSource = f.getSource();
        objects.put(myObj.objectDescriptor, myObj);
      }
    }
    return objects;
  }

  public HashSet getDistinctFeatSources() {
    HashSet gs = new HashSet();
    Feature f = null;
    String source;
    for (Iterator i = seq.features(); i.hasNext(); ) {
      f = (Feature) i.next();
      if (f.getAnnotation().containsProperty("source"))
          source = (String)f.getAnnotation().getProperty("source");
      else source = f.getSource();
      if (!gs.contains(source))
        gs.add(source);
    }
    return gs;
  }

  public ArrayList getFactorInstances(TreeMap objMap) {
    ArrayList instList = new ArrayList();
    if (seq == null)return instList;
    FactorInstance oi = null;
    Factor obj = null;
    String type = "";
    for (Iterator i = seq.features(); i.hasNext(); ) {
      Object temp = i.next();
      try {
        StrandedFeature sf = (StrandedFeature) temp;
        type = sf.getType();
        if ((type.equalsIgnoreCase("misc_feature") || type.equalsIgnoreCase("hit")) &&
            sf.getAnnotation().keys().contains("type")) {
          type = (String) sf.getAnnotation().getProperty("type");
          //System.out.println("type="+type);
        }
        if (objMap.containsKey(type)) {
          obj = (Factor) objMap.get(type);
          Location tempLoc;
          for (Iterator it = sf.getLocation().blockIterator(); it.hasNext(); ) {
            tempLoc = (Location) it.next();
            oi = new FactorInstance();
            oi.instanceStrand = sf.getStrand().getValue();
            oi.instanceStart = tempLoc.getMin();
            oi.instanceStop = tempLoc.getMax();
            oi.instanceType = obj.objectID;
            try{
            	oi.instanceScore = Double.parseDouble((String)sf.getAnnotation().getProperty("score"));
            	oi.instanceNormScore = Double.parseDouble((String)sf.getAnnotation().getProperty("normscore"));
            }catch(Exception ex){
            	System.out.println("No scores defined");
            	//ex.printStackTrace();
            }
            instList.add(oi);
          }
          //oi = new FactorInstance();
          //oi.instanceStrand = sf.getStrand().getValue();
          //oi.instanceStart = sf.getLocation().getMin();
          //oi.instanceStop = sf.getLocation().getMax();
          //oi.instanceType = obj.objectID;
          //instList.add(oi);
        }
      }
      catch (Exception e) {
        Feature f = (Feature) temp;
        type = f.getType();
        if ((type.equalsIgnoreCase("misc_feature") || type.equalsIgnoreCase("hit"))&&
            f.getAnnotation().keys().contains("type")) {
          type = (String) f.getAnnotation().getProperty("type");
          //System.out.println("type="+type);
        }
        if (objMap.keySet().contains(type)) {
          obj = (Factor) objMap.get(type);
          oi = new FactorInstance();
          oi.instanceStrand = 0;
          oi.instanceStart = f.getLocation().getMin();
          oi.instanceStop = f.getLocation().getMax();
          oi.instanceType = obj.objectID;
          instList.add(oi);
        }
      } //end catch
    }
    return instList;
  }

  public ArrayList getOverlappingFeatures(int bp) {
    PointLocation loc = new PointLocation(bp);
    //FeatureFilter sourceF = new FeatureFilter.ByType("source");
    FeatureFilter featFilter = new FeatureFilter.OverlapsLocation(loc);
    //featFilter = new FeatureFilter.AndNot(featFilter,sourceF);
    ArrayList ret = new ArrayList();
    if (seq == null)return ret;
    FeatureHolder fh = seq.filter(featFilter, false);
    for (Iterator it = fh.features(); it.hasNext(); ) {
      ret.add(it.next());
    }
    return ret;
  }

  public ArrayList getOverlappingFeatures(int from, int to) {
    RangeLocation loc = new RangeLocation(from,to);
    FeatureFilter featFilter = new FeatureFilter.OverlapsLocation(loc);
    ArrayList ret = new ArrayList();
    if (seq == null)return ret;
    FeatureHolder fh = seq.filter(featFilter, false);
    for (Iterator it = fh.features(); it.hasNext(); ) {
      ret.add(it.next());
    }
    return ret;
  }

  public void setEnsemblSpecies(EnsemblSpecies spec) {
    this.species = spec;
  }

  public void addToComment(String cc) throws Exception{
    if(seq.getAnnotation().containsProperty("CC"))
      cc = seq.getAnnotation().getProperty("CC") + cc;
    seq.getAnnotation().setProperty("CC",cc);
  }

  /**
   * Testing
   */
  public static void main(String[] args) throws Exception {
  	try {
        
        Gene g = new Gene();
        g.setSequence(new SimpleSequence(DNATools.createDNA("aaaaaggggggaagcgcaaa"),"test","test",null),"test");
        //g.maskFromTo(5,8);
        g.annotateIupac("gggggg","gggggg", 0.0d, "Manual");
        g.maskSubSequences("misc_feature","type","gggggg","around",-1,-2);
        //g.annotateIupac("gcgc","gcgc");
        //g.annotateIupac("ggaa","ggaa");
        //g.writeSeqToEmblFile("/Users/saerts/tmp/test.embl");
      }
      catch (IllegalSymbolException ex) {
        //this will happen if you use a character in one of your strings that is
        //not an accepted IUB Character for that Symbol.
        ex.printStackTrace();
      }
  }

  public void setSeqName(String name,boolean emptyAnnot) throws Exception {
    Alphabet dna = DNATools.getDNA();
    SymbolTokenization dnaToke = dna.getTokenization("token");
    SimpleSequence seq2;
    if(emptyAnnot)
      seq2 = new SimpleSequence(new SimpleSymbolList(dnaToke, seq.seqString()),
                                             name, name, Annotation.EMPTY_ANNOTATION);
  else
     seq2 = new SimpleSequence(new SimpleSymbolList(dnaToke, seq.seqString()),
                                             name, name, seq.getAnnotation());

    Feature.Template templ = null;
    for (Iterator it = seq.features(); it.hasNext(); ) {
      StrandedFeature f = (StrandedFeature) it.next();
      templ = f.makeTemplate();
      seq2.createFeature(templ);
    }
    seq = seq2;
    this.name = seq.getName();
    System.out.println("Name has been changed to "+name);
  }





  public void updateGeneName()throws Exception{
    if(this.name.indexOf("_")!=-1 || this.seq.getName().indexOf("_")!=-1){
      this.setSeqName(seq.getName().replace('_','%'),true);
    }
  }

}
