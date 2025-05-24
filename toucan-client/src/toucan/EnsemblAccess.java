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

import java.io.*;
import java.util.*;
import org.ensembl.datamodel.*;
import org.ensembl.driver.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.*;
import org.biojava.bio.seq.impl.*;



import toucan.util.GlobalProperties;

/**
 * Title: EnsemblAccess
 * Description: Use ENSJ library to retrieve sequences and annotation from the Ensembl database. The access properties are taken from the basics.properties file.
 * Copyright:    Copyright (c) 2004, 2005
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be> & Peter Van Loo
 * @version 2.0
 */

public class EnsemblAccess {
  public EnsemblAccess(Properties driverProps) throws Exception {
    driver = DriverManager.load(driverProps);
    spec = driverProps.getProperty("species");
  }

  public EnsemblAccess(CoreDriver driver, String species) {
    this.driver = driver;
    this.spec = species;
  }

  private CoreDriver driver;
  private String spec;

  /**
   *
   * @param ensembl String
   * @param type String : [flank,cdsupstream,geneupstream,genedownstream,cdsdownstream,introns]
   * @param bp int
   * @throws Exception
   * @return Sequence
   */
  public org.biojava.bio.seq.Sequence getBiojavaSeqFromEnsembl(String ensembl,
      String type, int bp) throws Exception {
    boolean vega = false;
    if (spec.toString().toLowerCase().indexOf("vega")!=-1) {
      vega = true;
      System.out.println("vega true");
    }
    System.out.println("vega false");
    if (ensembl.equals("") || ensembl == null || ensembl.equals("null"))throw new
        Exception("Ensembl ID is empty");
    boolean atTheEnd = false;
    boolean atTheBegin = false;
    GeneAdaptor geneAdaptor = driver.getGeneAdaptor();
    String stableId = ensembl;
    System.out.println("Stable id: " + stableId);
    org.ensembl.datamodel.Gene gene = geneAdaptor.fetch(stableId);

    if (gene == null)
      return null;

    //sequence
    SequenceAdaptor seqAd = driver.getSequenceAdaptor();
    org.ensembl.datamodel.Location paddedLocation = null;
    System.out.println("Getting location of gene...");

    // note: gene.getLocation().transform(-bp, + bp) does not throw any exception if the
    // scaffold/chromosome boundary is violated... (This was not so in previous ENSJ cores...)
    // in version 23.5, transform gives a location with start and end >= 1
    // furthermore, end can go over the end boundary...
    // note: this is tested using the ENSEMBL 23 and 24 databases.
    // so, the next code is obsolete:
    /*try {
      paddedLocation = gene.getLocation().transform( -bp, +bp);
      System.out.println("got it");
         }
         //if the gene lies at the beginning or at the end...
         catch (org.ensembl.datamodel.InvalidLocationException ile) {
      //try if it lies at the end => still upstream sequence possible
      try {
        paddedLocation = gene.getLocation().transform( -bp, 0);
        if (gene.getLocation().getStrand() == 1)
          atTheEnd = true;
        else atTheBegin = true;
      }
      //else at the beginning => still seq at the end possible
      catch (org.ensembl.datamodel.InvalidLocationException ile2) {
        try {
          paddedLocation = gene.getLocation().transform(0, bp);
          if (gene.getLocation().getStrand() == 1)
            atTheBegin = true;
          else atTheEnd = true;
        }
        catch (org.ensembl.datamodel.InvalidLocationException ile3) {
          paddedLocation = gene.getLocation();
          atTheBegin = true;
          atTheEnd = true;
        }
      }
         }
         catch (java.lang.IncompatibleClassChangeError ic) {
      ic.printStackTrace();
         }*/

    // this is the replacement:
    paddedLocation = gene.getLocation().transform( -bp, +bp);
    System.out.println("got it");
    int startdifference = java.lang.Math.abs(gene.getLocation().getStart() - paddedLocation.getStart());
    int enddifference = java.lang.Math.abs(gene.getLocation().getEnd() - paddedLocation.getEnd());
    if (startdifference < bp) {
      atTheBegin = true;
    }
    //this does not yet cover the case where end goes over the end boundary.
    //org.ensembl.datamodel.Sequence.fetch also works if paddedLocation exceeds boundaries
    //it then only returns the known part of the sequence
    //org.ensembl.datamodel.Sequence.getLocation() returns a clone of paddedLocation,
    //even if not all is in sequence...
    //so we have to use sequence length to determine if we get over the other boundary...
    //AND we have to adapt paddedLocation and sequence.location to avoid problems further on...
    //enddifference has to be updated as well...
      System.out.println("Getting sequence...");

      org.ensembl.datamodel.Sequence sequence;
      //if we need to get a sequence from VEGA, use a driver for non VEGA to get it...
      // this is a rather ugly workaround... Currently only works for human VEGA sequences..
      if (vega) {
        Properties eProps = null;
        eProps = new Properties();
        eProps.setProperty("ensembl_driver", GlobalProperties.getEnsemblDriver());
        eProps.setProperty("path", GlobalProperties.getEnsemblPath());
        eProps.setProperty("jdbc_driver", GlobalProperties.getJdbcDriver());
        eProps.setProperty("host", GlobalProperties.getEnsemblMysql());
        eProps.setProperty("port", GlobalProperties.getEnsemblMysqlPort());
        eProps.setProperty("user", GlobalProperties.getEnsemblUser());
        eProps.setProperty("password", GlobalProperties.getEnsemblPass());
        eProps.setProperty("database", GlobalProperties.getSpecies(spec.toLowerCase().replaceAll("_vega","")).core); // here: only human VEGA sequences...
        eProps.setProperty("name","");
        org.ensembl.driver.CoreDriver driverNonVega = org.ensembl.driver.DriverManager.load(eProps);
        SequenceAdaptor seqAdNonVega = driverNonVega.getSequenceAdaptor();
        sequence = seqAdNonVega.fetch(paddedLocation);
        driverNonVega.closeAllConnections();
      }
      else {
        sequence = seqAd.fetch(paddedLocation);
      }

      // so, this should do the rest:
    if (paddedLocation.getLength()>sequence.getString().length()) {
      atTheEnd = true;
      paddedLocation.setEnd(paddedLocation.getStart()+sequence.getString().length()-1);
      sequence.setLocation(paddedLocation);
      enddifference = java.lang.Math.abs(gene.getLocation().getEnd() - paddedLocation.getEnd());
    }
      System.out.println("got it");
    Alphabet dna = DNATools.getDNA();
    SymbolTokenization dnaToke = dna.getTokenization("token");
    org.biojava.bio.seq.Sequence bjSeq = null;
    bjSeq = new SimpleSequence(new SimpleSymbolList(dnaToke, sequence.getString()),
                               ensembl, ensembl, new SimpleAnnotation());
    //**********************************************
     //Ensembl gives, if the gene-location is on the negative strand, the reverse complemented sequence!!
     //We always need the direct strand, so here we rev.compl. for a gene on the - strand. This statement is double checked.
    if (gene.getLocation().getStrand() == -1) {
      bjSeq = new SimpleSequence(DNATools.reverseComplement(bjSeq), ensembl,
                                 ensembl, new SimpleAnnotation());
    }
    //***********************************************

     org.ensembl.datamodel.Location geneLoc = gene.getLocation();
    int geneStart = geneLoc.getStart();
    int geneEnd = geneLoc.getEnd();
    int geneLength = geneLoc.getLength();
    int padStart;
    if (!atTheBegin) padStart = geneStart - bp;
    else padStart = geneStart - startdifference;
    int padEnd;
    if (!atTheEnd) padEnd = geneEnd + bp;
    else padEnd = geneEnd + enddifference;
    int padLength = padEnd - padStart + 1;
    int start = 0, end = 0;

    System.out.println(geneStart + "|" + geneEnd + "|" + geneLength + "|" +
                       padStart + "|" + padEnd + "|" + padLength);

    //get all genes, also the other in the up/downstream region
    paddedLocation.setStrand(0);
    //System.out.println(paddedLocation);
    List genes = geneAdaptor.fetch(paddedLocation);

    StrandedFeature.Template template = null;
    ExternalRef ref;
    //gene feature
    for (Iterator geneIt = genes.iterator(); geneIt.hasNext(); ) {
      gene = (org.ensembl.datamodel.Gene) geneIt.next();
      System.out.println(gene.getAccessionID());
      template = new StrandedFeature.Template();
      template.type = "gene";
      template.source = "Ensembl";
      if (gene.getLocation().getStrand() == 1)
        template.strand = StrandedFeature.POSITIVE;
      else if (gene.getLocation().getStrand() == -1)
        template.strand = StrandedFeature.NEGATIVE;
      start = gene.getLocation().getStart() - padStart + 1;
      if (start < 1) start = 1;
      end = gene.getLocation().getEnd() - padStart + 1;
      if (end > padLength) end = padLength;
      template.location = new RangeLocation(start, end);
      template.annotation = new SimpleAnnotation();
      System.out.println("Getting accession ID...");
      template.annotation.setProperty("gene", gene.getAccessionID());
      System.out.println("got it");
      System.out.println("Getting description...");
      template.annotation.setProperty("descr", gene.getDescription());
      System.out.println("got it");
      System.out.println("No external refs are retrieved (takes too long); use Ensembl->Info to get them.");
      //template.annotation.setProperty("name",gene.getDisplayName());
      //External refs:
      System.out.println("No external refs are retrieved for the transcripts (takes too long); use Ensembl->Info to get them.");
      /*
             System.out.println("Getting external refs...");
       for (Iterator it = gene.getExternalRefs().iterator(); it.hasNext(); ) {
        ref = (ExternalRef) it.next();
       System.out.println(ref.getExternalDatabase().getName()+"|"+ref.getDisplayID());
        template.annotation.setProperty(ref.getExternalDatabase().getName(),
                                        ref.getDisplayID());
             }
             System.out.println("Got them");
       */
      bjSeq.createFeature(template);
    }

    Transcript t;
    //CDS
    for (Iterator geneIt = genes.iterator(); geneIt.hasNext(); ) {
      gene = (org.ensembl.datamodel.Gene) geneIt.next();
      System.out.println("Getting transcripts for gene...");
      List transcripts = gene.getTranscripts();
      System.out.println("got them");
      org.ensembl.datamodel.Location tempLoc;
      System.out.println("Building features:");
      System.out.println("CDS...");
      for (Iterator it = transcripts.iterator(); it.hasNext(); ) {
        t = (Transcript) it.next();
        template.location = org.biojava.bio.symbol.Location.empty;
        try{
          for (Iterator it2 = t.getTranslation().getCodingLocations().iterator();
               it2.hasNext(); ) {
            tempLoc = (org.ensembl.datamodel.Location) it2.next();
            template.type = "CDS";
            template.source = "Ensembl";
            if (t.getLocation().getStrand() == 1)
              template.strand = StrandedFeature.POSITIVE;
            else if (t.getLocation().getStrand() == -1)
              template.strand = StrandedFeature.NEGATIVE;
            start = tempLoc.getStart() - padStart + 1;
            end = tempLoc.getEnd() - padStart + 1;
            if (start < padLength && end > 1) { //otherwise don't add it to the location, out of sight anyway
              if (start < 1)
                start = 1;
              if (end > padLength)
                end = padLength;
              template.location = LocationTools.union(template.location,
                  new RangeLocation(start, end));
              template.annotation = new SimpleAnnotation();
              template.annotation.setProperty("gene", gene.getAccessionID());
              template.annotation.setProperty("transcript_id", t.getAccessionID());
              template.annotation.setProperty("protein_id",
                                              t.getTranslation().getAccessionID());
              // Note: this is not the ideal solution!!
              if (vega) {
                template.annotation.setProperty("translation",
                                                "");
              }
              else {
                template.annotation.setProperty("translation",
                                                t.getTranslation().getPeptide());
              }
              //External refs
              /*
               System.out.println("Getting external refs for transcript...");
               for (Iterator it3 = t.getExternalRefs().iterator(); it3.hasNext(); ) {
                ref = (ExternalRef) it3.next();
               System.out.println(ref.getExternalDatabase().getName()+"|"+ref.getDisplayID());
               template.annotation.setProperty(ref.getExternalDatabase().getName(),
                                                ref.getDisplayID());
                           }
                           System.out.println("got them");
               */
            }
          }
          bjSeq.createFeature(template);
        }
        catch(java.lang.NullPointerException npe){
          //npe.printStackTrace();
          System.out.println("Some transcripts apparently didn't have coding locations");
        }
        catch(java.lang.StringIndexOutOfBoundsException se){
          System.out.println("Something wrong with location of translated peptide -> could not set the peptide sequence.");
        }
      }
      System.out.println("done");
    }

    //5primeUTR
    System.out.println("5'UTR...");
    org.ensembl.datamodel.Location utr;
    for (Iterator geneIt = genes.iterator(); geneIt.hasNext(); ) {
      gene = (org.ensembl.datamodel.Gene) geneIt.next();
      List transcripts = gene.getTranscripts();
      for (Iterator it = transcripts.iterator(); it.hasNext(); ) {
        t = (Transcript) it.next();
        List utrs = t.getFivePrimeUTR();
        try{
          for (Iterator utrIt = utrs.iterator(); utrIt.hasNext(); ) { //shouldn't there be only 1 UTR per transcript?
            utr = (org.ensembl.datamodel.Location) utrIt.next();
            template = new StrandedFeature.Template();
            template.type = "FivePrimeUTR";
            template.source = "Ensembl";
            template.annotation = new SimpleAnnotation();
            template.annotation.setProperty("transcript_id", t.getAccessionID());
            if (utr.getStrand() == 1)
              template.strand = StrandedFeature.POSITIVE;
            else if (utr.getStrand() == -1)
              template.strand = StrandedFeature.NEGATIVE;
            start = utr.getStart() - padStart + 1;
            end = utr.getEnd() - padStart + 1;
            if (start < padLength && end > 1) {
              if (start < 1)
                start = 1;
              if (end > padLength)
                end = padLength;
              template.location = new RangeLocation(start, end);
              bjSeq.createFeature(template);
            }
          }
        }
        catch(java.lang.NullPointerException npe){
          //npe.printStackTrace();
          System.out.println("Some transcripts apparently didn't have UTRs");
        }
      }
      System.out.println("done");

    }

    //3primeUTR
    System.out.println("3'UTR...");
    for (Iterator geneIt = genes.iterator(); geneIt.hasNext(); ) {
      gene = (org.ensembl.datamodel.Gene) geneIt.next();
      List transcripts = gene.getTranscripts();
      org.ensembl.datamodel.Location tempLoc;
      for (Iterator it = transcripts.iterator(); it.hasNext(); ) {
        t = (Transcript) it.next();
        List utrs = t.getThreePrimeUTR();
        try {
          for (Iterator utrIt = utrs.iterator(); utrIt.hasNext(); ) {
            utr = (org.ensembl.datamodel.Location) utrIt.next();
            template = new StrandedFeature.Template();
            template.type = "ThreePrimeUTR";
            template.source = "Ensembl";
            template.annotation = new SimpleAnnotation();
            template.annotation.setProperty("transcript_id", t.getAccessionID());
            if (utr.getStrand() == 1)
              template.strand = StrandedFeature.POSITIVE;
            else if (utr.getStrand() == -1)
              template.strand = StrandedFeature.NEGATIVE;
            start = utr.getStart() - padStart + 1;
            end = utr.getEnd() - padStart + 1;
            if (start < padLength && end > 1) {
              if (start < 1)
                start = 1;
              if (end > padLength)
                end = padLength;
              template.location = new RangeLocation(start, end);
              bjSeq.createFeature(template);
            }
          }
        }
            catch (java.lang.NullPointerException npe) {
              //npe.printStackTrace();
              System.out.println("Some transcripts apparently didn't have UTRs");
            }
      }
      System.out.println("done");
    }

    //Exons
    System.out.println("Exons...");
    Exon e;
    for (Iterator geneIt = genes.iterator(); geneIt.hasNext(); ) {
      gene = (org.ensembl.datamodel.Gene) geneIt.next();
          List exons = gene.getExons();
          System.out.println("got them...");
      for (Iterator it = exons.iterator(); it.hasNext(); ) {
        e = (Exon) it.next();
        template = new StrandedFeature.Template();
        template.type = "exon";
        template.source = "Ensembl";
        if (e.getLocation().getStrand() == 1)
          template.strand = StrandedFeature.POSITIVE;
        else if (e.getLocation().getStrand() == -1)
          template.strand = StrandedFeature.NEGATIVE;
        start = e.getLocation().getStart() - padStart + 1;
        end = e.getLocation().getEnd() - padStart + 1;
        if (start < padLength && end > 1) {
          if (start < 1)
            start = 1;
          if (end > padLength)
            end = padLength;
          template.location = new RangeLocation(start, end);
          template.annotation = new SimpleAnnotation();
          template.annotation.setProperty("gene", gene.getAccessionID());
          template.annotation.setProperty("exon_id", e.getAccessionID());
          template.annotation.setProperty("descr", e.getDescription());
        //external refs
        //for (Iterator it2 = gene.getExternalRefs().iterator(); it2.hasNext(); ) {
            //ref = (ExternalRef) it2.next();
            //template.annotation.setProperty(ref.getExternalDatabase().getName(),ref.getDisplayID());
          //}
          bjSeq.createFeature(template);
        }
      }
      System.out.println("done");
      System.out.println("Gene structures ready");
    }

    bjSeq.getAnnotation().setProperty("chromStart",new Integer(paddedLocation.getStart()));
    bjSeq.getAnnotation().setProperty("chromEnd",new Integer(paddedLocation.getEnd()));
    if(paddedLocation.getCoordinateSystem().getName().equalsIgnoreCase("chromosome"))
      bjSeq.getAnnotation().setProperty("chrom",new String(paddedLocation.getSeqRegionName()));
    else
      System.out.println("Genes not on chromosomes...");

    geneAdaptor = null;
    gene = null;
    seqAd = null;
    return bjSeq;
  }

  /**
   * TESTING
   */
  public static void main(String[] args) throws Exception {
    Properties props = new Properties();
    File propFile = new File(
        "ensembl-db.properties");
    InputStream in = new FileInputStream(propFile);
    props.load(in);
    in.close();
    EnsemblAccess ea = new EnsemblAccess(props);
    org.biojava.bio.seq.Sequence seq = ea.getBiojavaSeqFromEnsembl(
        "ENSG00000139618", null, 2000);
    System.out.println(seq.subStr(1, 500));
    System.out.println("\n\n");
    System.out.println(seq.subStr(seq.length() - 500, seq.length()));
    toucan.Gene g = new toucan.Gene();
    g.setSequence(seq, null);
    g.reverseComplement();
    System.out.println("\n\n");
    System.out.println(g.seq.subStr(1, 500));
    System.out.println("\n\n");
    System.out.println(g.seq.subStr(seq.length() - 500, seq.length()));
    System.out.println("cdspositive= " + g.cdsPositive);
    System.out.println(seq.subStr(1, 10));
  }

}
