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
package toucan.util;

import toucan.*;
import org.w3c.dom.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.seq.io.*;
import org.biojava.bio.seq.db.*;
import org.biojava.bio.program.gff.*;
import com.sun.media.jai.codec.*;
import java.sql.*;
import java.net.*;



/**
 * Title: Tools
* Description:Collection of tools for sequence analysis. Methods are static so you don't have to instantiate an object; just
 *              call the methods on the class itself.
 * Copyright:    Copyright (c) 2004
 * Company: University of Leuven, Department of Electrical Engineering ESAT-SCD, Belgium
 * @author Stein Aerts <stein.aerts@esat.kuleuven.ac.be>
 * @version 2.0
 */


public class Tools {

  public Tools() {
  }

  public static void writeImgToFile(BufferedImage img,String path, String type) throws Exception{
         PrintWriter out;
         File file = new File(path);
         FileOutputStream outb = new FileOutputStream(file);

         //JPEG
         if(type.equalsIgnoreCase("jpg")){
           /*com.sun.image.codec.jpeg.JPEGImageEncoder encoder = com.sun.image.codec.
               jpeg.JPEGCodec.createJPEGEncoder(outb);
           encoder.encode(img);*/
           ImageEncoder enc = ImageCodec.createImageEncoder("JPEG", outb, null);
           enc.encode(img);
         }

         //PNG
         if(type.equalsIgnoreCase("png")){
           ImageEncoder enc = ImageCodec.createImageEncoder("PNG", outb, null);
           enc.encode(img);
         }

         //TIFF
         if(type.equalsIgnoreCase("tiff")){
           ImageEncoder enc = ImageCodec.createImageEncoder("TIFF", outb, null);
           enc.encode(img);
         }
         outb.close();
  }

  public static String parseMatInsp (String htmlPath, String analysisName, int totalBpInSet) throws Exception{
    String seqName="",matrixName="",info="",seq="",strand="";
    int start=0,end=0,index=0;
    double re=0d,opt=0d,coreSim=0d,matrixSim=0d,freq=0d;
    StringBuffer xmlStr = new StringBuffer();
    xmlStr.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    //xmlStr.append("<!DOCTYPE MatInspOutput SYSTEM \"MatInspOutput.dtd\">");
    xmlStr.append("<MatInspOutput analysis=\""+analysisName+"\">");
    BufferedReader in = new BufferedReader(new FileReader(htmlPath));
    String s = in.readLine();
    while (s!=null)
    {
      index = s.indexOf("<H3><A NAME=\"map\">");
      if (index!=-1){
         while (s!=null && !s.startsWith("<H2>Inspecting sequence")){
               s = in.readLine();
         }
         if (s==null) break;
      }
      index = s.indexOf("<H2>Inspecting sequence");
      if (index!=-1){
         if (seqName.equals("")) xmlStr.append("<seq>");
         else xmlStr.append("</seq>\n<seq>");
         seqName = s.substring(24,s.indexOf("(1 -")-1);
         System.out.println("\n");
         System.out.println(seqName);
         xmlStr.append("<seqName>");
         xmlStr.append(seqName);
         xmlStr.append("</seqName>");

      }
      index = s.indexOf("matrix_help.pl?NAME=V$");
      if (index!=-1){
        s = s.substring(s.indexOf("'MatrixList');return false\">"));
        matrixName = s.substring(28,s.indexOf("</A>"));
        //System.out.println(matrixName);
        s = in.readLine();
        s = s.substring(15);
        info = s.substring(0,s.indexOf("<TD>"));
        //System.out.println(info);
        s = s.substring(s.indexOf("<TD>")+4);
        String reStr = s.substring(0,s.indexOf("<TD>"));
        //System.out.println(reStr);
        if (reStr.startsWith("&lt;")) reStr = reStr.substring(4);
        re = Double.parseDouble(reStr);
        String optStr = s.substring(s.indexOf("<TD>")+4);
        //System.out.println(optStr);
        opt = Double.parseDouble(optStr);
        s = in.readLine();
        String startStr = s.substring(4,s.indexOf("&nbsp;-&nbsp;"));
        //System.out.println(startStr);
        start = Integer.parseInt(startStr);
        String endStr = s.substring(s.indexOf("&nbsp;-&nbsp;")+13);
        end = Integer.parseInt(endStr);
        //System.out.println(endStr);
        s = in.readLine();
        strand = s.substring(5,6);
        s = s.substring(11);
        String coreSimStr = s.substring(0,s.indexOf("<TD>"));
        coreSim = Double.parseDouble(coreSimStr);
        //System.out.println(coreSimStr);
        s = s.substring(s.indexOf("<TD>")+4);
        String matrixSimStr = s.substring(0,s.indexOf("<TD"));
        matrixSim = Double.parseDouble(matrixSimStr);
        //System.out.println(matrixSimStr);
        s = s.substring(s.indexOf("<TD align=left>")+15);
        String coloredSeq = s;
        seq = seq + s.substring(0,s.indexOf("<FONT COLOR=RED>"));
        int ind = s.indexOf("<FONT COLOR=RED>");
        while (ind!=-1){
              s = s.substring(ind+16);
              seq = seq + s.substring(0,s.indexOf("</FONT>"));
              s = s.substring(s.indexOf("</FONT>")+7);
              if (s.indexOf("<FONT COLOR=RED>") != -1) {
                 seq = seq + s.substring(0,s.indexOf("<FONT COLOR=RED>"));
                 ind = s.indexOf("<FONT COLOR=RED>");
              }
              else {
                   seq = seq + s;
                   ind = -1;
              }
        }
        xmlStr.append("<matrixMatch>");
        xmlStr.append("<matrix>");
        xmlStr.append("<name>");
        xmlStr.append(matrixName);
        xmlStr.append("</name>");
        xmlStr.append("<info>");
        xmlStr.append(info);
        xmlStr.append("</info>");
        xmlStr.append("<re>");
        xmlStr.append(re);
        xmlStr.append("</re>");
        xmlStr.append("<opt>");
        xmlStr.append(opt);
        xmlStr.append("</opt>");
        xmlStr.append("<length>");
        xmlStr.append(seq.length());
        xmlStr.append("</length>");
        xmlStr.append("</matrix>");
        xmlStr.append("<match>");
        xmlStr.append("<start>");
        xmlStr.append(start);
        xmlStr.append("</start>");
        xmlStr.append("<end>");
        xmlStr.append(end);
        xmlStr.append("</end>");
        xmlStr.append("<strand>");
        xmlStr.append(strand);
        xmlStr.append("</strand>");
        xmlStr.append("<coreSim>");
        xmlStr.append(coreSim);
        xmlStr.append("</coreSim>");
        xmlStr.append("<matrixSim>");
        xmlStr.append(matrixSim);
        xmlStr.append("</matrixSim>");
        xmlStr.append("<sequence>");
        xmlStr.append(seq);
        xmlStr.append("</sequence>");
        //xmlStr.append("<coloredSeq>");
        //xmlStr.append(coloredSeq);
        //xmlStr.append("</coloredSeq>");
        xmlStr.append("</match>");
        xmlStr.append("</matrixMatch>");
        xmlStr.append("\n");

        seq = "";
      }
      s = in.readLine();
    }
    xmlStr.append("</seq>");

    //Statistics: read html file again...
    String nrMatchesStr="",nrSeqStr="",reStr="";
    int nrMatches=0,nrSeq=0;

    xmlStr.append("<statistics>");
    in = new BufferedReader(new FileReader(htmlPath));
    s = in.readLine();
    //remove first part
    index = s.indexOf("<H2><A NAME=\"statistics\">Statistics:</A></H2>");
    while (index==-1){
       s = in.readLine();
       index = s.indexOf("<H2><A NAME=\"statistics\">Statistics:</A></H2>");
    }
    System.out.println("stats part...");
    while (s!=null)
    {
      index = s.indexOf("matrix_help.pl?NAME=V$");
      if (index!=-1){
        s = s.substring(s.indexOf("'MatrixList');return false\">"));
        matrixName = s.substring(28,s.indexOf("</A><TD>re:")).trim();
        //System.out.println(matrixName);
        reStr = s.substring(s.indexOf("</A><TD>re:")+11).trim();
        //System.out.println("re: "+reStr);
        s = in.readLine();
        if (s==null) break;
        s = s.substring(17);
        nrMatchesStr = s.substring(0,s.indexOf("matches")).trim();
        //System.out.println("matches: "+nrMatchesStr);
        nrMatches = Integer.parseInt(nrMatchesStr);
        s = s.substring(s.indexOf("<TD align=right>")+16);
        nrSeqStr = s.substring(0,s.indexOf("seq")).trim();
        //System.out.println("seq: "+nrSeqStr);
        nrSeq = Integer.parseInt(nrSeqStr);
        if (reStr.startsWith("&lt;")) reStr = reStr.substring(4);
        re = Double.parseDouble(reStr);

        xmlStr.append("<stat>");
        xmlStr.append("<matrixName>");
        xmlStr.append(matrixName);
        xmlStr.append("</matrixName>");
        xmlStr.append("<re>");
        xmlStr.append(re);
        xmlStr.append("</re>");
        xmlStr.append("<nrMatches>");
        xmlStr.append(nrMatches);
        xmlStr.append("</nrMatches>");
        xmlStr.append("<nrSeq>");
        xmlStr.append(nrSeq);
        xmlStr.append("</nrSeq>");
        xmlStr.append("<f>");

        freq = (Double.parseDouble(""+nrMatches)/totalBpInSet);
        xmlStr.append(""+freq);
        xmlStr.append("</f>");
        xmlStr.append("</stat>");
        xmlStr.append("\n");
      }
      else s = in.readLine();
    }

    xmlStr.append("</statistics>");
    xmlStr.append("\n");
    xmlStr.append("</MatInspOutput>");

    return xmlStr.toString();
    //return XMLParser.parseString(xmlStr.toString());
  }

  /*
  public static void createSeqDB(String indexName,String formatName,String alphaName){
  try {
      File indexFile = new File(indexName);
      File indexList = new File(indexName + ".list");
      Alphabet alpha = resolveAlphabet(alphaName);
      SymbolParser sParser = alpha.getParser("token");
      SequenceFormat sFormat = null;
      SequenceBuilderFactory sFact = null;
      if(formatName.equals("fasta")) {
	  sFormat = new FastaFormat();
	  sFact = new FastaDescriptionLineParser.Factory(SimpleSequenceBuilder.FACTORY);
      } else if(formatName.equals("embl")) {
	  sFormat = new EmblLikeFormat();
	  sFact = new EmblProcessor.Factory(SimpleSequenceBuilder.FACTORY);
      } else if (formatName.equals("swissprot")) {
	  sFormat = new EmblLikeFormat();
	  sFact = new SwissprotProcessor.Factory(SimpleSequenceBuilder.FACTORY);
      } else {
	  throw new Exception("Format must be one of {embl, fasta, swissprot}");
      }
      TabIndexStore tis = new TabIndexStore(
        indexFile,
        indexList,
        indexName,
        sFormat,
        sFact,
        sParser
      );
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }*/

  private static Alphabet resolveAlphabet(String alphaName) throws IllegalArgumentException {
    alphaName = alphaName.toLowerCase();
    if(alphaName.equals("dna")) {
      return DNATools.getDNA();
    } else if(alphaName.equals("protein")) {
      return ProteinTools.getAlphabet();
    } else {
      throw new IllegalArgumentException("Could not find alphabet for " + alphaName);
    }
  }

  public static String array2DToString(String[][] array) {
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < array.length; i++) {
      for (int j = 0; j < array[i].length; j++) {
        buffer.append(array[i][j] + "\t");
      }
      buffer.append("\n");
    }
    return buffer.toString().trim();
  }

  public static String[][] stringToArray2D(String string) {
    StringTokenizer n_tokener = new StringTokenizer(string.trim(), "\n");
    int rows = 0, cols = 0;
    String[][] array = null;
    if (string != null && !string.equals("")) {
      rows = n_tokener.countTokens(); //System.out.println("Nr. of rows is: " + rows);
      String line = n_tokener.nextToken();
      StringTokenizer t_tokener = new StringTokenizer(line, "\t");
      cols = t_tokener.countTokens(); //System.out.println("Nr. of cols is: " + cols);
      array = new String[rows][cols];
      for (int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
          array[i][j] = t_tokener.nextToken();
        }
        try {
          line = n_tokener.nextToken();
        }
        catch (NoSuchElementException ex) {
          break;
        }
        t_tokener = new StringTokenizer(line, "\t");
      }
    }
    return array;
  }


  public static Document createXmlDocFromFile (String path) throws Exception{
    StringBuffer buf = new StringBuffer();
    FileInputStream in = new FileInputStream(path);
    BufferedReader bin = new BufferedReader(new InputStreamReader(in));
    String line;
    line = bin.readLine();
    while (line != null) {
      buf.append(line);
      line = bin.readLine();
    }
    in.close();
    String xmlStr = buf.toString();
    //System.out.println(xmlStr);
    Document xmlDoc = XMLParser.parseString(xmlStr);
    return xmlDoc;
  }

  public static BufferedImage createImg(String title, Document xmlDoc){
    Gene hg = null;
    Color cpgColor = null;
    Color promColor = null;
    Color spanColor = null;
    NodeList objList = xmlDoc.getElementsByTagName("Factor");
    NodeList seqList = xmlDoc.getElementsByTagName("aSequence");
    int nrSeq = seqList.getLength();
    int nrObj = objList.getLength();
    //calculate proper scale
    int max=0;
    NodeList lengthList = xmlDoc.getElementsByTagName("length");
    for (int i=0; i<nrSeq; i++) {
        Node ln = lengthList.item(i);
        String tempStr = "";
        tempStr = ln.getChildNodes().item(0).getNodeValue();
        //System.out.println(tempStr);
        int temp = new Integer(tempStr).intValue();
        if(temp> max) max = temp;
    }
    int scale = max/1500;
    if (scale==0) scale = 1;
    int maxLength = max/scale;
    //System.out.println("scale="+scale+ "max= "+max);
    //Objects
    ArrayList objArrList = new ArrayList();
    HashMap legendMap = new HashMap();
    Factor o = null;
    Color c=null;
    boolean[] fillArr = new boolean[nrObj];
    for (int k=0;k<nrObj;k++){
        Node on = objList.item(k);
        o = new Factor();
        o.objectID = on.getAttributes().item(0).getNodeValue();
        o.objectType = on.getChildNodes().item(0).getChildNodes().item(0).getNodeValue();
        o.objectDescriptor =  on.getChildNodes().item(1).getChildNodes().item(0).getNodeValue();
        objArrList.add(o);
        //System.out.println(o.objectID +"|"+ o.objectType +"|"+ o.objectDescriptor +" || ");
        if (k==0) c = Color.blue;
        if (k==1) c = Color.orange;
        if (k==2) c = Color.red;
        if (k==3) c = Color.green;
        if (k==4) c = Color.gray;
        if (k==5) c = Color.magenta;
        if (k==6) c = Color.cyan;
        if (k==7) c = Color.yellow;
        if (k==8) c = new Color(100,100,0);
        if (k==9) c = new Color(50,0,50);
        if (k==10) c = new Color(0,150,150);
        if (k>11){
                int rval = (int)Math.floor(Math.random() * 256);
                int gval = (int)Math.floor(Math.random() * 256);
                int bval = (int)Math.floor(Math.random() * 256);
                c = new Color(rval,gval,bval);
        }
        cpgColor = new Color(200,200,0);
        promColor = new Color(0,200,200);
        spanColor = new Color(50,150,250);
        if (o.objectDescriptor.indexOf("CpG")!=-1) c = cpgColor;
        if (o.objectDescriptor.indexOf("promoter")!=-1) c = promColor;
	if (o.objectType.indexOf("Span")!=-1) c = spanColor;
        legendMap.put(o.objectID,c);
    }
    //System.out.println("\n");
    //Sequences
    int xBase = 100;
    int imgWidth= maxLength + xBase*2;
    int imgHeight=(nrSeq*100)+400;
    int linePos = 0;
    int xpos=xBase,ypos=linePos;

    java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(imgWidth,imgHeight,java.awt.image.BufferedImage.TYPE_INT_RGB);
    java.awt.Graphics2D g = img.createGraphics();
    g.fillRect(2,2,imgWidth-4,imgHeight-4);
    //g.setColor(Color.white);
    g.setColor(Color.black);
    //title
    g.setFont(new Font("Arial",Font.BOLD,16));
    g.drawString(title,(imgWidth/2)-50,35);
    linePos = linePos+100;

    //rulers
    g.setFont(new Font("Arial",Font.PLAIN,10));
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

    for (int k=0;k<nrSeq;k++){
        xpos = xBase;
        //g.setColor(Color.white);
        g.setColor(Color.black);
        linePos=linePos+50;
        ypos = linePos;
        g.setFont(new Font("Arial",Font.PLAIN,12));
        String seqName = "";
        int seqLength = 0;
        int cnt=0;
        Node seqNode = seqList.item(k);
        NodeList seqChildren = seqNode.getChildNodes();
        Node n = null;
        int strand=0,start=0,stop=0,score=0;
        String type;
        boolean fill = true;
        for (int i=0;i<seqChildren.getLength();i++){
            n = seqChildren.item(i);
            if (n.getNodeName().equalsIgnoreCase("descriptor")) {
               if (n.hasChildNodes()) seqName = n.getChildNodes().item(0).getNodeValue();
               if (seqName.length()>15) seqName = seqName.substring(13);
	       g.drawString(seqName,xBase-80,ypos);
            }
            if (n.getNodeName().equalsIgnoreCase("length")) {
               if (n.hasChildNodes()) seqLength = new Integer(n.getChildNodes().item(0).getNodeValue()).intValue();
               g.drawLine(xpos,ypos,xpos+(seqLength/scale),ypos);
            }
            if (n.getNodeName().equalsIgnoreCase("objInstance")) {
               NodeList objChildren = n.getChildNodes();
               ypos = linePos;
               height = baseHeight;
               for (int j=0;j<objChildren.getLength();j++){
                   Node no = objChildren.item(j);
                   if (no.getNodeName().equalsIgnoreCase("instanceStrand")) {
                      strand = new Integer(no.getChildNodes().item(0).getNodeValue()).intValue();
                      if (strand==1) ypos=ypos-10;
                      else if (strand==-1);
                      else if (strand==0) {ypos=ypos-10; height=height+10;}
                   }
                   if (no.getNodeName().equalsIgnoreCase("instanceStart")) {
                      start = new Integer(no.getChildNodes().item(0).getNodeValue()).intValue();
                   }
                   if (no.getNodeName().equalsIgnoreCase("instanceStop")) {
                      stop = new Integer(no.getChildNodes().item(0).getNodeValue()).intValue();
                   }
                   if (no.getNodeName().equalsIgnoreCase("instanceScore")) {
                      score = new Integer(no.getChildNodes().item(0).getNodeValue()).intValue();
                   }
                   if (no.getNodeName().equalsIgnoreCase("instanceType")) {
                      type = no.getChildNodes().item(0).getNodeValue();
                      c = (Color)legendMap.get(type);
                      g.setColor(c);
                      if (c.equals(cpgColor) || c.equals(promColor) || c.equals(spanColor)) fill=false;
                      else fill=true;
                      //promoters
                      if (c.equals(promColor)) {
                         float dash[] = {10.0f};
                         g.setStroke(new BasicStroke(1.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,10.0f, dash, 0.0f));
                      }
                      //CpG islands
                      if (c.equals(cpgColor)) {
                         float dash[] = {2.0f};
                         g.setStroke(new BasicStroke(1.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,10.0f, dash, 0.0f));
                      }
		      //spans
		      if (c.equals(spanColor)) {
                         float dash[] = {20.0f};
                         g.setStroke(new BasicStroke(1.0f,BasicStroke.CAP_BUTT,BasicStroke.JOIN_MITER,10.0f, dash, 0.0f));
                      }
                   }
               }
               xpos = xBase + (start)/scale;
               width = (stop - start)/scale;
               if (fill) g.fillRect(xpos,ypos,width,height);
               else g.drawRect(xpos,ypos,width,height);
               g.setStroke(new BasicStroke(1.0f));
            }
        }
      }
      //legend
      g.setFont(new Font("Arial",Font.PLAIN,16));
      ypos=ypos+30;
      for (int k=0;k<nrObj;k++){
          ypos=ypos+15;
          xpos = xBase;
          o = (Factor)objArrList.get(k);
          c = (Color)legendMap.get(o.objectID);
          g.setColor(c);
          g.fillRect(xpos,ypos,10,10);
          g.drawString(o.objectDescriptor,xpos+15,ypos+8);
      }
      return img;
  }

    public static GFFEntrySet parseMatInspectorToGffEntrySet(String htmlPath) throws Exception{
    GFFEntrySet gffSet = new GFFEntrySet();
    SimpleGFFRecord rec;
    String seqName="",matrixName="",info="",seq="",strand="";
    int start=0,end=0,index=0;
    double re=0d,opt=0d,coreSim=0d,matrixSim=0d;
    BufferedReader in = new BufferedReader(new FileReader(htmlPath));
    String s = in.readLine();
    while (s!=null)
    {

      index = s.indexOf("<H3><A NAME=\"map\">");
      if (index!=-1){
         while (s!=null && !s.startsWith("<H2>Inspecting sequence")){
               s = in.readLine();
         }
         if (s==null) break;
      }

      index = s.indexOf("<H2>Inspecting sequence");
      if (index!=-1){
         seqName = s.substring(24,s.indexOf("(1 -")-1);
         System.out.println(seqName);
      }

      index = s.indexOf("matrix_help.pl?NAME=V$");
      if (index!=-1){
        s = s.substring(s.indexOf("'MatrixList');return false\">"));
        matrixName = s.substring(28,s.indexOf("</A>"));
        System.out.println(matrixName);
        s = in.readLine();
        s = s.substring(15);
        info = s.substring(0,s.indexOf("</TD>"));
        System.out.println(info);
        s = s.substring(s.indexOf("</TD>")+9);
        String reStr = s.substring(0,s.indexOf("</TD>"));
        System.out.println(reStr);
        if (reStr.startsWith("&lt;")) reStr = reStr.substring(4);
        re = Double.parseDouble(reStr);
        System.out.println(s);
	s = s.substring(s.indexOf("</TD><TD>")+9);
	String optStr = s.substring(0,s.indexOf("</TD>"));
        System.out.println(optStr);
        opt = Double.parseDouble(optStr);
        s = in.readLine();
        String startStr = s.substring(4,s.indexOf("&nbsp;-&nbsp;"));
        System.out.println(startStr);
        start = Integer.parseInt(startStr);
        String endStr = s.substring(s.indexOf("&nbsp;-&nbsp;")+13,s.indexOf("</TD>"));
        end = Integer.parseInt(endStr);
        System.out.println(endStr);
        s = in.readLine();
        strand = s.substring(5,6);
        s = s.substring(16);
        String coreSimStr = s.substring(0,s.indexOf("</TD>"));
        coreSim = Double.parseDouble(coreSimStr);
        System.out.println(coreSimStr);
        s = s.substring(s.indexOf("<TD>")+4);
        String matrixSimStr = s.substring(0,s.indexOf("</TD"));
        matrixSim = Double.parseDouble(matrixSimStr);
        System.out.println(matrixSimStr);
        s = s.substring(s.indexOf("<TD align=left>")+15);
        String coloredSeq = s.substring(0,s.indexOf("</TD>"));
        seq = seq + s.substring(0,s.indexOf("<FONT COLOR=RED>"));
        int ind = s.indexOf("<FONT COLOR=RED>");
        while (ind!=-1){
              s = s.substring(ind+16);
              seq = seq + s.substring(0,s.indexOf("</FONT>"));
              s = s.substring(s.indexOf("</FONT>")+7);
              if (s.indexOf("<FONT COLOR=RED>") != -1) {
                 seq = seq + s.substring(0,s.indexOf("<FONT COLOR=RED>"));
                 ind = s.indexOf("<FONT COLOR=RED>");
              }
              else {
                   seq = seq + s;
                   ind = -1;
              }
        }
	System.out.println(seq);
        rec = new SimpleGFFRecord();
	rec.setSeqName(seqName);
	rec.setFeature(matrixName);
	rec.setStart(start);
	rec.setEnd(end);
	if (strand.equals("+")) rec.setStrand(StrandedFeature.POSITIVE);
	else if (strand.equals("-")) rec.setStrand(StrandedFeature.NEGATIVE);
        else rec.setStrand(StrandedFeature.UNKNOWN);
	rec.setScore(matrixSim);
	gffSet.add(rec);
	System.out.println(".");
      }
      s = in.readLine();
    }
    return gffSet;
  }

     /**
    * @deprecated
    * TODO: use parseMatInspectorToGFFEntrySet, then write records to DB
    */
  public static void parseMatInspectorToGff(String htmlPath,String gffPath) throws Exception{
    String seqName="",matrixName="",info="",seq="",strand="";
    int start=0,end=0,index=0;
    double re=0d,opt=0d,coreSim=0d,matrixSim=0d;
    BufferedReader in = new BufferedReader(new FileReader(htmlPath));
    String s = in.readLine();
    FileOutputStream fout = new FileOutputStream(gffPath);
    OutputStreamWriter out = new OutputStreamWriter(fout);
    while (s!=null)
    {

      index = s.indexOf("<H3><A NAME=\"map\">");
      if (index!=-1){
         while (s!=null && !s.startsWith("<H2>Inspecting sequence")){
               s = in.readLine();
         }
         if (s==null) break;
      }

      index = s.indexOf("<H2>Inspecting sequence");
      if (index!=-1){
         seqName = s.substring(24,s.indexOf("(1 -")-1);
         System.out.println("\n");
         System.out.println(seqName);
      }

      index = s.indexOf("matrix_help.pl?NAME=V$");
      if (index!=-1){
        s = s.substring(s.indexOf("'MatrixList');return false\">"));
        matrixName = s.substring(28,s.indexOf("</A>"));
        //System.out.println(matrixName);
        s = in.readLine();
        s = s.substring(15);
        info = s.substring(0,s.indexOf("<TD>"));
        //System.out.println(info);
        s = s.substring(s.indexOf("<TD>")+4);
        String reStr = s.substring(0,s.indexOf("<TD>"));
        //System.out.println(reStr);
        if (reStr.startsWith("&lt;")) reStr = reStr.substring(4);
        re = Double.parseDouble(reStr);
        String optStr = s.substring(s.indexOf("<TD>")+4);
        //System.out.println(optStr);
        opt = Double.parseDouble(optStr);
        s = in.readLine();
        String startStr = s.substring(4,s.indexOf("&nbsp;-&nbsp;"));
        //System.out.println(startStr);
        start = Integer.parseInt(startStr);
        String endStr = s.substring(s.indexOf("&nbsp;-&nbsp;")+13);
        end = Integer.parseInt(endStr);
        //System.out.println(endStr);
        s = in.readLine();
        strand = s.substring(5,6);
        s = s.substring(11);
        String coreSimStr = s.substring(0,s.indexOf("<TD>"));
        coreSim = Double.parseDouble(coreSimStr);
        //System.out.println(coreSimStr);
        s = s.substring(s.indexOf("<TD>")+4);
        String matrixSimStr = s.substring(0,s.indexOf("<TD"));
        matrixSim = Double.parseDouble(matrixSimStr);
        //System.out.println(matrixSimStr);
        s = s.substring(s.indexOf("<TD align=left>")+15);
        String coloredSeq = s;
        seq = seq + s.substring(0,s.indexOf("<FONT COLOR=RED>"));
        int ind = s.indexOf("<FONT COLOR=RED>");
        while (ind!=-1){
              s = s.substring(ind+16);
              seq = seq + s.substring(0,s.indexOf("</FONT>"));
              s = s.substring(s.indexOf("</FONT>")+7);
              if (s.indexOf("<FONT COLOR=RED>") != -1) {
                 seq = seq + s.substring(0,s.indexOf("<FONT COLOR=RED>"));
                 ind = s.indexOf("<FONT COLOR=RED>");
              }
              else {
                   seq = seq + s;
                   ind = -1;
              }
        }
        String gffStr = seqName+"\tMatInspector\t"+matrixName+"\t"+start+"\t"+end+"\t"+matrixSim+"\t"+strand+"\t.\t"+"seq \""+seq+"\";\n";
	out.write(gffStr);
	seq = "";
        System.out.println(".");

      }
      s = in.readLine();
    }
    out.close();
  }

     /**
    * @deprecated
    * TODO: use parseMatInspectorToGFFEntrySet, then write records to DB
    */
  public static void parseMatInspectorToDb(String path,String analysis) throws Exception{
    String dbURL = "jdbc:mysql://calvin/sequence";
    String dbUser = "javaUsr";
    String dbPasswd = "java";
    String tblName = "tcf_matinsp";
    Driver d = (Driver)Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    DriverManager.registerDriver(d);
    Connection conn = DriverManager.getConnection(dbURL, dbUser,dbPasswd);
    Statement stat = conn.createStatement();
    String seqName="",matrixName="",info="",seq="",strand="";
    int start=0,end=0,index=0;
    double re=0d,opt=0d,coreSim=0d,matrixSim=0d;

    BufferedReader in = new BufferedReader(new FileReader(path));
    String s = in.readLine();
    while (s!=null)
    {

      index = s.indexOf("<H3><A NAME=\"map\">");
      if (index!=-1){
         while (s!=null && !s.startsWith("<H2>Inspecting sequence")){
               s = in.readLine();
         }
         if (s==null) break;
      }

      index = s.indexOf("<H2>Inspecting sequence");
      if (index!=-1){
         seqName = s.substring(24,s.indexOf("(1 -")-1);
         System.out.println("\n");
         System.out.println(seqName);
      }

      index = s.indexOf("matrix_help.pl?NAME=V$");
      if (index!=-1){
        s = s.substring(s.indexOf("'MatrixList');return false\">"));
        matrixName = s.substring(28,s.indexOf("</A>"));
        //System.out.println(matrixName);
        s = in.readLine();
        s = s.substring(15);
        info = s.substring(0,s.indexOf("<TD>"));
        //System.out.println(info);
        s = s.substring(s.indexOf("<TD>")+4);
        String reStr = s.substring(0,s.indexOf("<TD>"));
        //System.out.println(reStr);
        if (reStr.startsWith("&lt;")) reStr = reStr.substring(4);
        re = Double.parseDouble(reStr);
        String optStr = s.substring(s.indexOf("<TD>")+4);
        //System.out.println(optStr);
        opt = Double.parseDouble(optStr);
        s = in.readLine();
        String startStr = s.substring(4,s.indexOf("&nbsp;-&nbsp;"));
        //System.out.println(startStr);
        start = Integer.parseInt(startStr);
        String endStr = s.substring(s.indexOf("&nbsp;-&nbsp;")+13);
        end = Integer.parseInt(endStr);
        //System.out.println(endStr);
        s = in.readLine();
        strand = s.substring(5,6);
        s = s.substring(11);
        String coreSimStr = s.substring(0,s.indexOf("<TD>"));
        coreSim = Double.parseDouble(coreSimStr);
        //System.out.println(coreSimStr);
        s = s.substring(s.indexOf("<TD>")+4);
        String matrixSimStr = s.substring(0,s.indexOf("<TD"));
        matrixSim = Double.parseDouble(matrixSimStr);
        //System.out.println(matrixSimStr);
        s = s.substring(s.indexOf("<TD align=left>")+15);

        String coloredSeq = s;

        seq = seq + s.substring(0,s.indexOf("<FONT COLOR=RED>"));
        int ind = s.indexOf("<FONT COLOR=RED>");
        while (ind!=-1){
              s = s.substring(ind+16);
              seq = seq + s.substring(0,s.indexOf("</FONT>"));
              s = s.substring(s.indexOf("</FONT>")+7);
              if (s.indexOf("<FONT COLOR=RED>") != -1) {
                 seq = seq + s.substring(0,s.indexOf("<FONT COLOR=RED>"));
                 ind = s.indexOf("<FONT COLOR=RED>");
              }
              else {
                   seq = seq + s;
                   ind = -1;
              }
        }
        String sql = "insert into "+tblName+" values('"+analysis+"','','"+seqName+"','"+matrixName+"','"+info+"',"+re+","+opt+","+start+","+end+",'"+strand+"',"+coreSim+","+matrixSim+",'"+seq+"','"+coloredSeq+"')";
        //System.out.println(sql);
        seq = "";
        System.out.println(".");
        stat.execute(sql);
      }
      s = in.readLine();
    }
    conn.close();
  }

  public static void updateMatchStatsWithLength(String analysis,String statTblName)throws Exception{
              String dbURL = "jdbc:mysql://calvin/sequence";
              String dbUser = "Stein";
              String dbPasswd = "niets";
              Driver d = (Driver)Class.forName("org.gjt.mm.mysql.Driver").newInstance();
              DriverManager.registerDriver(d);
              Connection conn = DriverManager.getConnection(dbURL, dbUser,dbPasswd);
              Statement stat = conn.createStatement();
              Statement stat2 = conn.createStatement(), stat3=conn.createStatement();
              Gene gene;
              String seq;
              ResultSet rs=null,rs2=null;

              rs = stat.executeQuery("select distinct matrixName from "+statTblName);
              while (rs.next()){
                    String matrixName = rs.getString("matrixName");
                    String sql = "select seq from matchout where matrixName = '" +matrixName+ "' and analysis='"+analysis+"' limit 1";
                    System.out.println(sql);
                    rs2 = stat2.executeQuery(sql);
                    if (rs2.next()){
                       stat3.execute("update "+statTblName+" set length="+rs2.getString("seq").length()+" where matrixName ='"+matrixName+"'");
                    }
              }
              conn.close();
  }

  public static void parseMatchOutToDb(String path,String analysis) throws Exception{
    String dbURL = "jdbc:mysql://calvin/sequence";
    String dbUser = "Stein";
    String dbPasswd = "niets";
    Driver d = (Driver)Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    DriverManager.registerDriver(d);
    Connection conn = DriverManager.getConnection(dbURL, dbUser,dbPasswd);
    Statement stat = conn.createStatement();
    String seqName="",matrixName="",factorName="",seq="",strand="";
    int start=0,cnt=0,index=0;
    double re=0d,opt=0d,coreSim=0d,matrixSim=0d;

    BufferedReader in = new BufferedReader(new FileReader(path));
    String s = in.readLine();
    while (s!=null)
    {
      if (s.indexOf("Scanning sequence")!=-1){
         seqName = s.substring(24,s.indexOf(";"));
         if (cnt!=0) System.out.println("  ->"+cnt);
         cnt=0;
         System.out.println(seqName);
         for (int i = 0; i<8; i++) {
             s = in.readLine();
         }
      }
      else if (s.indexOf("V$")!=-1){
            matrixName = s.substring(0,s.indexOf("   "));
            //System.out.println(matrixName);
            int ind = s.indexOf("   ");
            char c = 'q';
            while (c==' '){
                  c = s.charAt(ind++);
            }
            s = s.substring(ind);
            String startStr = s.substring(0,s.indexOf("(")-1);
            //System.out.println("start="+startStr);
            start = Integer.parseInt(startStr.trim());
            strand = s.substring(s.indexOf("(")+1,s.indexOf(")"));
            s = s.substring(s.indexOf(")")+3);
            String coreSimStr = s.substring(0,s.indexOf("  "));
            coreSim = Double.parseDouble(coreSimStr);
            //System.out.println("coreSim="+coreSimStr);
            s = s.substring(s.indexOf("  ")+2);
            String matrixSimStr = s.substring(0,s.indexOf("  "));
            matrixSim = Double.parseDouble(matrixSimStr);
            //System.out.println("matrixSim="+matrixSimStr);
            s = s.substring(s.indexOf("  ")+2);
            seq = s.substring(0,s.indexOf("   "));
            ind = s.indexOf("   ");
            c = s.charAt(ind);
            while (c==' '){
                  c = s.charAt(ind++);
            }
            s = s.substring(ind-1);
            factorName = s.substring(0);

            String sql = "insert into matchOut values('"+analysis+"','','"+seqName+"','"+matrixName+"',"+start+",'"+strand+"',"+coreSim+","+matrixSim+",'"+seq+"','"+factorName+"')";
            //System.out.println(sql);
            System.out.println(".");
            cnt++;
            stat.execute(sql);
      }
      s = in.readLine();
    }
    if (cnt!=0) System.out.println("  ->"+cnt);
    conn.close();
  }

  /**
   * Only takes acc numbers of genomic sequences!
   */
  public static ArrayList getAccNrFromLocusLink (int locuslink)throws Exception{
    ArrayList accList = new ArrayList();
    String dbURL = "jdbc:mysql://calvin/locuslink";
    String dbUser = "Stein";
    String dbPasswd = "niets";
    Driver d = (Driver)Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    DriverManager.registerDriver(d);
    Connection conn = DriverManager.getConnection(dbURL, dbUser,dbPasswd);
    Statement stat = conn.createStatement();
    ResultSet rs = stat.executeQuery("select * from loc2acc where type='g' and locuslink="+locuslink);
    while (rs.next()) {
          accList.add(rs.getString("accNr"));
    }
    conn.close();
    return accList;
  }

  public void motifScanner(String organism,double prior,String bgModel,String outPath,String execPath) throws Exception{
    //TODO: read blocked fasta file & scan

    //mtrx files: currently together with executable:
    String matrixPath = execPath;
    String bgModelPath = execPath + bgModel;
    //matrix file
    if (organism.equalsIgnoreCase("V")) matrixPath = matrixPath + "V_transfac.mtrx";
    else if (organism.equalsIgnoreCase("F")) matrixPath = matrixPath + "F_transfac.mtrx";
    else if (organism.equalsIgnoreCase("P")) matrixPath = matrixPath + "P_transfac.mtrx";
    else if (organism.equalsIgnoreCase("I")) matrixPath = matrixPath + "I_transfac.mtrx";
    else if (organism.equalsIgnoreCase("N")) matrixPath = matrixPath + "N_transfac.mtrx";

    //run it
    //toucan.util.Tools.runCommand(execPath+"MotifScanner " + seqFile + " " + matrixPath + " " + prior + " " + bgModelPath + " > " + outPath + "out.gff");
    //String command = execPath+"MotifScanner " + seqFile + " " + matrixPath + " " + prior + " " + bgModelPath + " > " + outPath + "out.gff";
    //String command = execPath+"MotifScanner " + seqFile + " " + matrixPath + " " + prior + " " + bgModelPath;
    String command = "cd D:\\SAE";
    //Runtime.getRuntime().exec(command);
    Tools.runCommand("echo \"hello\"");
  }


  public static void parseWinMatchOutToDb(String path,String analysis, GeneList gl) throws Exception{
    String dbURL = "jdbc:mysql://calvin/sequence";
    String dbUser = "Stein";
    String dbPasswd = "niets";
    Driver d = (Driver)Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    DriverManager.registerDriver(d);
    Connection conn = DriverManager.getConnection(dbURL, dbUser,dbPasswd);
    Statement stat = conn.createStatement();
    String seqName="",matrixName="",factorName="",seq="",strand="";
    int start=0,cnt=0,index=0;
    double re=0d,opt=0d,coreSim=0d,matrixSim=0d;
    Gene g = null;
    BufferedReader in = new BufferedReader(new FileReader(path));
    String s = in.readLine();
    int geneNr = 0;
    while (s!=null)
    {
      if (s.indexOf("Inspecting sequence")!=-1){
         //seqName = s.substring(24,s.indexOf(";"));
         g = (Gene) gl.list.get(geneNr++);
	 if (geneNr<gl.list.size()) seqName = g.name;
	 //if (geneNr<gl.list.size()) seqName = ((Gene)gl.list.get(geneNr++)).name;
         else break;
	 if (cnt!=0) System.out.println("  ->"+cnt);
         cnt=0;
         //System.out.println(seqName);
         //for (int i = 0; i<2; i++) {
         //    s = in.readLine();
         //}
      }
      else if (s.indexOf("F$")!=-1){
            matrixName = s.substring(0,s.indexOf("|")).trim();
            //System.out.println(matrixName);
            s = s.substring(s.indexOf("|")+1);
            String startStr = s.substring(0,s.indexOf("(")-1).trim();
            //System.out.println("start="+startStr);
            start = Integer.parseInt(startStr.trim());
            strand = s.substring(s.indexOf("(")+1,s.indexOf(")"));
            s = s.substring(s.indexOf("|")+1);
            String coreSimStr = s.substring(0,s.indexOf("|")).trim();
            coreSim = Double.parseDouble(coreSimStr);
            //System.out.println("coreSim="+coreSimStr);
            s = s.substring(s.indexOf("|")+1);
            String matrixSimStr = s.substring(0,s.indexOf("|")).trim();
            matrixSim = Double.parseDouble(matrixSimStr);
            //System.out.println("matrixSim="+matrixSimStr);
            s = s.substring(s.indexOf("|")+1);
            seq = s.trim();
	    int end = start + seq.length()-1;
            //if (seqName.indexOf("'")!=-1) seqName =
	    String sql = "insert into yeast_orfs_matched values(\""+seqName+"\",'Match','"+matrixName+"',"+start+","+end+","+matrixSim+",'"+strand+"',null,\""+seqName+"\","+ g.length +",'"+seq+"')";
            //System.out.println(sql);
            System.out.println(".");
            cnt++;
            stat.execute(sql);
      }
      s = in.readLine();
    }
    if (cnt!=0) System.out.println("  ->"+cnt);
  }





  /**
   * @deprecated
   */
  public static SequenceDB gffToFeatures(String seqPath,String gffPath)throws Exception{
      SequenceDB seqDB = loadSequences(new File(seqPath));
      System.out.println("Sequences:");
      for(SequenceIterator si = seqDB.sequenceIterator(); si.hasNext(); ) {
        Sequence seq = si.nextSequence();
        System.out.println("\t" + seq.getName());
      }
      GFFEntrySet gffEntries = new GFFEntrySet();

      //filter for source
      //GFFRecordFilter.SourceFilter sFilter = new GFFRecordFilter.SourceFilter();
      //sFilter.setSource(source);
      //GFFFilterer filterer = new GFFFilterer(gffEntries.getAddHandler(), sFilter);

      GFFParser parser = new GFFParser();
      parser.parse(
        new BufferedReader(
          new InputStreamReader(
            new FileInputStream(
              new File(gffPath)))),
        gffEntries.getAddHandler());
      // add the features to the sequence

      SequenceDB aSeqDB = new AnnotatedSequenceDB(seqDB, gffEntries.getAnnotator());

      //testing
      for (SequenceIterator it = aSeqDB.sequenceIterator(); it.hasNext();){
        Sequence s = (Sequence) it.nextSequence();
        System.out.println(s.getName());
      }
      PrintWriter out = new PrintWriter(new OutputStreamWriter(System.out));
      GFFWriter writer = new GFFWriter(out);
      SequencesAsGFF seqsAsGFF = new SequencesAsGFF();
      seqsAsGFF.processDB(aSeqDB, writer);

      return aSeqDB;
  }

  public static GFFEntrySet adjustGffForNs(String gffPath)throws Exception{
    GFFEntrySet gffEntries = new GFFEntrySet();
    GFFEntrySet ret = new GFFEntrySet();
    GFFParser parser = new GFFParser();
    parser.parse(
      new BufferedReader(
        new InputStreamReader(
          new FileInputStream(
            new File(gffPath)))),
      gffEntries.getAddHandler());
    SimpleGFFRecord rec;
    String id = "";
    int offset = 0;
    for (Iterator it = gffEntries.lineIterator(); it.hasNext();){
      rec = new SimpleGFFRecord();
      rec = (SimpleGFFRecord)it.next();
      id = rec.getSeqName();
      if (id.indexOf("@")!=-1) {
	  offset = new Integer(id.substring(id.indexOf("@")+1)).intValue();
	  rec.setStart(rec.getStart()+offset);
	  rec.setEnd(rec.getEnd()+offset);
	  rec.setSeqName(id.substring(0,id.indexOf("@")));
	  System.out.println("Adjusted: "+rec.getSeqName()+"|"+rec.getStart()+"|"+rec.getEnd()+"|"+rec.getFeature());
      }
      ret.add(rec);
    }
    System.out.println(ret.toString());
    return ret;
  }

  public static GeneList seqDB2list(SequenceDB db)throws Exception{
   GeneList ret = new GeneList();
   Gene g;
   Sequence s;
   for (SequenceIterator it = db.sequenceIterator();it.hasNext();){
    g = new Gene();
    s = it.nextSequence();
    g.setSequence(s,s.getName());
    ret.add(g);
   }
   return ret;
  }

  public static Set uniqueFeatureSetFromFiles(ArrayList fileList)throws Exception{
   GeneList gl = new GeneList();
   ArrayList featMapList = new ArrayList();
   ArrayList temp;
   for (Iterator it = fileList.iterator();it.hasNext();){
        gl.constructFromLargeEmblFile((String)it.next());
	temp = gl.allNrFeatures("misc_feature");
	System.out.println((String)temp.get(0));
	featMapList.addAll(temp);
   }
   Set uSet = Tools.createUniqueFeatSet(featMapList);
   System.out.println(uSet.size()+"|"+uSet);
   return uSet;
  }

   public static SequenceDB loadSequences(File seqFile)
  throws Exception {
    HashSequenceDB seqDB = new HashSequenceDB(IDMaker.byName);

    BufferedReader br = new BufferedReader(new FileReader(seqFile));
    SequenceIterator stream = SeqIOTools.readFastaDNA(br);

    while(stream.hasNext()) {
      try {
        seqDB.addSequence(stream.nextSequence());
      } catch (Exception se) {
        se.printStackTrace();
      }
    }

    return seqDB;
  }

   public static void parsePromoterInspectorToDb(String path,String analysis) throws Exception{
    String dbURL = "jdbc:mysql://calvin/sequence";
    String dbUser = "Stein";
    String dbPasswd = "niets";
    Driver d = (Driver)Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    DriverManager.registerDriver(d);
    Connection conn = DriverManager.getConnection(dbURL, dbUser,dbPasswd);
    Statement stat = conn.createStatement();
    String seqName="";
    int start=0,end=0,length=0,index=0;

    BufferedReader in = new BufferedReader(new FileReader(path));
    String s = in.readLine();

    //remove first part
    index = s.indexOf("<H2>Inspecting sequence");
    while (index==-1){
       s = in.readLine();
       index = s.indexOf("<H2>Inspecting sequence");
    }

    while (s!=null)
    {
      index = s.indexOf("<H2>Inspecting sequence");
      if (index!=-1){
         int ind = s.indexOf("(1 -");
         //if (ind>20) ind = s.indexOf("[<A HREF");
         seqName = s.substring(24,ind-1);
         //s = s.substring(s.indexOf("[<A HREF=\"http://srs.ebi.ac.uk/srs5bin/cgi-bin/wgetz?-e+[embl-acc:")+66);
         //seqName = s.substring(0,s.indexOf("]\" onClick"));
         System.out.println("\n");
         System.out.println(seqName);
         s = in.readLine();
      }

      index = s.indexOf("<TD align=middle>");
      if (index!=-1){
        s = s.substring(s.indexOf("<TD align=middle>")+17);
        //String startStr = s.substring(0,s.indexOf("<"));
        String startStr = s;
        System.out.println("start ="+startStr);
        start = Integer.parseInt(startStr);
        s = in.readLine();
        s = s.substring(s.indexOf("<TD align=middle>")+17);
        //String endStr = s.substring(0,s.indexOf("<"));
        String endStr = s;
	end = Integer.parseInt(endStr);
        System.out.println("end= "+endStr);
        s = in.readLine();
        s = s.substring(s.indexOf("<TD align=middle>")+17);
        String lengthStr = "";
        //if (s.indexOf("<TR>")!=-1) lengthStr = s.substring(0,s.indexOf("<TR>")-1);
        if (s.indexOf("</TR>")!=-1) lengthStr = s.substring(0,s.indexOf("</TR>")-1);
        else lengthStr = s;
        System.out.println("length="+lengthStr);
        length = Integer.parseInt(lengthStr);
        String sql = "insert into promInsp values('"+analysis+"','"+seqName+"',"+start+","+end+","+length+")";
        System.out.println(sql);
        System.out.println(".");
        stat.execute(sql);
      }
      s = in.readLine();
    }
  }


   public static void parseMatInspStatsToDb(String path,String analysis) throws Exception{
    String dbURL = "jdbc:mysql://calvin/sequence";
    String dbUser = "javaUsr";
    String dbPasswd = "java";
    String tblName = "tcf_stats";
    Driver d = (Driver)Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    DriverManager.registerDriver(d);
    Connection conn = DriverManager.getConnection(dbURL, dbUser,dbPasswd);
    Statement stat = conn.createStatement();
    String nrMatchesStr="",matrixName="",nrSeqStr="",reStr="";
    int start=0,end=0,index=0,nrMatches=0,nrSeq=0;
    double re=0d,opt=0d,coreSim=0d,matrixSim=0d;

    BufferedReader in = new BufferedReader(new FileReader(path));
    String s = in.readLine();

    //remove first part
    index = s.indexOf("<H2><A NAME=\"statistics\">Statistics:</A></H2>");
    while (index==-1){
       s = in.readLine();
       index = s.indexOf("<H2><A NAME=\"statistics\">Statistics:</A></H2>");
    }

    System.out.println("stats part...");
    while (s!=null)
    {
      index = s.indexOf("matrix_help.pl?NAME=V$");
      if (index!=-1){
        s = s.substring(s.indexOf("'MatrixList');return false\">"));
        matrixName = s.substring(28,s.indexOf("</A><TD>re:")).trim();
        //System.out.println(matrixName);
        reStr = s.substring(s.indexOf("</A><TD>re:")+11).trim();
        //System.out.println("re: "+reStr);
        s = in.readLine();
        if (s==null) break;
        s = s.substring(17);
        nrMatchesStr = s.substring(0,s.indexOf("matches")).trim();
        //System.out.println("matches: "+nrMatchesStr);
        nrMatches = Integer.parseInt(nrMatchesStr);
        s = s.substring(s.indexOf("<TD align=right>")+16);
        nrSeqStr = s.substring(0,s.indexOf("seq")).trim();
        //System.out.println("seq: "+nrSeqStr);
        nrSeq = Integer.parseInt(nrSeqStr);
        if (reStr.startsWith("&lt;")) reStr = reStr.substring(4);
        re = Double.parseDouble(reStr);

        String sql = "insert into "+tblName+" values('"+analysis+"','"+matrixName+"',"+re+","+nrMatches+","+nrSeq+",null)";
        //System.out.println(sql);
        System.out.println(".");
        stat.execute(sql);
      }
      else s = in.readLine();
    }
  }

  /**
   * Compare multiple files with ids in a directory and return common id's
   * @param  dirPath = directory with only the files with accession numbers to compare
   * @param  minNrCommon = minimum number of accession numbers that have to be common
   *         e.g. compare 4 files -> accNr has to be present in minimal 3 out of the 4 files
   * @return ArrayList with common ids
   */
  public static ArrayList compareIds (String dirPath,int minNrCommon) throws Exception{
      File dir = new File(dirPath);
      String[] fileNames = dir.list();
      ArrayList files = new ArrayList();
      ArrayList commonIds = new ArrayList();
      for (int i=0;i<fileNames.length;i++){
          files.add(files.get(i));
      }
      ArrayList samples = loadAccNrFiles(dirPath,files);
      HashSet result = compare(samples,minNrCommon);
      Iterator it = result.iterator();
      while (it.hasNext()){
        commonIds.add(it.next());
      }
      return commonIds;
  }

  public static HashSet createUniqueFeatSet (ArrayList featMapList){
         HashMap featMap;
         HashSet uSet = new HashSet();
         for (Iterator it = featMapList.iterator();it.hasNext();){
             featMap = (HashMap)it.next();
             Set keySet = featMap.keySet();
             for (Iterator iter = keySet.iterator();iter.hasNext();){
                 String type = (String)iter.next();
                 if (!uSet.contains(type)) uSet.add(type);
             }
         }
         return uSet;
  }

  /**
    Load accession numbers from file into a hashmap. Put one hashmap for each sample in an arraylist.
  */
  private static ArrayList loadAccNrFiles (String basePath,ArrayList files){
    ArrayList samples = new ArrayList();
    HashSet ids = null;
    Iterator fit = files.iterator();
    String path = "";
    while (fit.hasNext()){
      String file = (String)fit.next() + ".txt";
      path = basePath + file;
      //System.out.println(path);
      ids = new HashSet();
      try{
        BufferedReader in  = new BufferedReader(new FileReader(path));
        String line=in.readLine();
        while (line!=null){
          line = line.trim();
          if (line.endsWith("_at")) {
            line = line.substring(0,line.length()-3);
          }
          if (line.endsWith("_s")) {
            line = line.substring(0,line.length()-2);
          }

          ids.add(line);
          line = in.readLine();
        }
        in.close();
      }
      catch (Exception e){
        System.err.println("File input error");
      }
      samples.add(ids);
    }
    return samples;
  }

  public static String fileToString(String path){
    String ret = "";
    StringBuffer buf = new StringBuffer();
    try{
        BufferedReader in  = new BufferedReader(new FileReader(path));
        String line=in.readLine();
        while (line!=null){
          buf.append(line);
	  buf.append("\n");
          line = in.readLine();
        }
        in.close();
    }
    catch (Exception e){
      System.err.println("File input error");
    }
    return buf.toString();
  }

  public static ArrayList getEnsemblIdsFromExternal(String in,String externalDb,int dbId, String dbURL)throws Exception{
    ArrayList ret = new ArrayList();
    if(externalDb.equalsIgnoreCase("ensembl")){
      ret.add(in);
      return ret;
    }
    Connection kaka=null;
    Statement s = null,s2=null;
    ResultSet rs = null,rs2=null, rs3 = null;
    Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    kaka = DriverManager.getConnection(dbURL);
    s= kaka.createStatement();
    s2 = kaka.createStatement();
    String sql = "select o.ensembl_id,o.ensembl_object_type from object_xref o,xref x where x.external_db_id = "+dbId+ " and (x.display_label = '"+in+"' or x.dbprimary_acc = '"+in+"') and o.xref_id = x.xref_id";
    rs = s.executeQuery(sql);
    System.out.println("Ensembl ID's for "+in+" are: ");
    while (rs.next()){
       sql = "SELECT g.stable_id FROM transcript t, gene_stable_id g where t.gene_id=g.gene_id and t.translation_id="+rs.getString("ensembl_id");
       rs2 = s2.executeQuery(sql);
       while (rs2.next()){
         System.out.println(rs2.getString(1)+",");
         ret.add(rs2.getString(1));
       }
    }
    System.out.println("\n");
    kaka.close();
    return ret;
  }

  public static HashMap getEnsemblIdsFromExternalList(ArrayList in,String species,String identifierType, String martServiceURL)throws Exception,java.sql.SQLException{
	  HashSet ensIds = new HashSet();
	    
	  System.out.println("Querying for Ensembl ID's...");
	    //System.out.println(dbURL);
	    HashMap ret = new HashMap();
	    if(identifierType.equalsIgnoreCase("ensembl")){
	      for (Iterator it = in.iterator();it.hasNext();){
	        Object o = it.next();
	        ret.put(o.toString().trim(),o.toString().trim());
	      }
	      return ret;
	    }
	    else{
	      for (Iterator it = in.iterator();it.hasNext();){
	        ret.put(it.next().toString().toLowerCase().trim(),"");
	      }
	    }
	  String query = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE Query><Query  virtualSchemaName = \"default\" header = \"0\" count = \"\" softwareVersion = \"0.5\" >";
	  query+="<Dataset name = \""+species+"_gene_ensembl\" interface = \"default\" >";
	  query+="<Attribute name = \""+identifierType+"\" />";
      query+="<Attribute name = \"ensembl_gene_id\" />";
      query+="<Filter name = \""+identifierType+"\" value = \""+Tools.arrayList2CommaList(in)+"\"/>";
      query+="</Dataset></Query>";
      System.out.println(query);
      String tab = post2Mart(query,martServiceURL);
      System.out.println(tab);
      String[][] res = Tools.stringToArray2D(tab);
      for(int i=0;i<res.length;i++){
    	  if(!ensIds.contains(res[i][1])){
              ret.put(res[i][0].toLowerCase(),
                      "" + ret.get(res[i][0].toLowerCase()) + "|" + res[i][1]);
              ensIds.add(res[i][1]);
            }
       }
      
      
        System.out.println("Ensembl ID's for "+in+" are: ");
   for (Iterator it=ret.entrySet().iterator();it.hasNext();){
     Map.Entry e = (Map.Entry)it.next();
     System.out.println(e.getKey() + "-" + e.getValue());
   }
      return ret;
  }
  
  public static String post2Mart(String xmlQuery,String martServiceURL) throws Exception{
	  URL url=new URL (martServiceURL+"?query="+xmlQuery);
	  System.out.println(url.toString());
	  URLConnection urlConn= url.openConnection();
	    urlConn.setDoInput (true);
	    urlConn.setDoOutput (true);
	    urlConn.setUseCaches (false);
	    urlConn.setRequestProperty
	    ("Content-Type", "application/x-www-form-urlencoded");
	  urlConn.setRequestProperty
	    ("Content-Type", "application/x-www-form-urlencoded");
	  	DataOutputStream printout = new DataOutputStream (urlConn.getOutputStream ());
	    String content =
	    "query=" + URLEncoder.encode(xmlQuery,"UTF-8");
	    printout.writeBytes (content);
	    printout.flush ();
	    printout.close ();
	    DataInputStream input = new DataInputStream (urlConn.getInputStream ());
	         System.out.println(input);
	    BufferedReader d= new BufferedReader(new InputStreamReader(input));
	    StringBuffer sb = new StringBuffer();
	    String inputLine;
	    while ((inputLine = d.readLine()) != null) {
            sb.append(inputLine + "\n");
	    }
        String str = sb.toString();
	    input.close();  
	    return str;
  }
  
  
  public static HashMap getEnsemblIdsFromExternalList(ArrayList in,String externalDb,int dbId, String dbURL)throws Exception,java.sql.SQLException{
    System.out.println("Querying for Ensembl ID's...");
    //System.out.println(dbURL);
    HashMap ret = new HashMap();
    if(externalDb.equalsIgnoreCase("ensembl")){
      for (Iterator it = in.iterator();it.hasNext();){
        Object o = it.next();
        ret.put(o.toString().trim(),o.toString().trim());
      }
      return ret;
    }
    else{
      for (Iterator it = in.iterator();it.hasNext();){
        ret.put(it.next().toString().toLowerCase().trim(),"");
      }
    }
    Connection kaka=null;
    Statement s = null,s2=null;
    ResultSet rs = null,rs2=null, rs3 = null;
    Class.forName("org.gjt.mm.mysql.Driver").newInstance();
    System.out.println("before");
    //Class.forName("com.caucho.jdbc.mysql.Driver").newInstance();
    kaka = DriverManager.getConnection(dbURL);
    System.out.println("MySQL connection OK");
    s= kaka.createStatement();
    s2 = kaka.createStatement();
    //get all translation ids using 1 query
    //String sql = "select x.display_label,o.ensembl_id,o.ensembl_object_type from object_xref o,xref x where x.external_db_id = "+dbId+ " and (x.display_label in "+Tools.arrayList2SqlInStr(in)+" or x.dbprimary_acc in "+Tools.arrayList2SqlInStr(in)+") and o.xref_id = x.xref_id";
    //use only display_label
    String sql = "select x.display_label,o.ensembl_id,o.ensembl_object_type from object_xref o,xref x where x.external_db_id = "+dbId+ " and x.display_label in "+Tools.arrayList2SqlInStr(in)+" and o.xref_id = x.xref_id";
    System.out.println(sql);
    rs = s.executeQuery(sql);
    System.out.println("SQL query executed");
    String ext,temp="";
    String objType;
    HashSet ensIds = new HashSet();
    while (rs.next()){
      ext = rs.getString("x.display_label");
      objType = rs.getString("ensembl_object_type");
      temp += "'"+rs.getString("ensembl_id")+"',";
       //System.out.println("display_label="+rs.getString("ensembl_id"));
       if(objType.equalsIgnoreCase("Transcript")){
         sql = "SELECT g.stable_id FROM transcript t,gene_stable_id g where t.gene_id=g.gene_id and t.transcript_id=" +
             rs.getString("ensembl_id");
       }
       else if(objType.equalsIgnoreCase("Translation")){
         sql = "SELECT g.stable_id FROM transcript t, translation l,gene_stable_id g where t.gene_id=g.gene_id and t.transcript_id=l.transcript_id and l.translation_id="+rs.getString("ensembl_id");
       }
       else if(objType.equalsIgnoreCase("Gene")) {
         sql = "SELECT g.stable_id FROM gene_stable_id g where g.gene_id="+rs.getString("ensembl_id");
       }
         System.out.println(sql);
       rs2 = s2.executeQuery(sql);
       while (rs2.next()){
         System.out.println(rs2.getString(1));
         if(!ensIds.contains(rs2.getString(1))){
           ret.put(ext.toLowerCase(),
                   "" + ret.get(ext.toLowerCase()) + "|" + rs2.getString(1));
           ensIds.add(rs2.getString(1));
         }
       }
    }
    System.out.println(temp);
    System.out.println("Ensembl ID's for "+in+" are: ");
    for (Iterator it=ret.entrySet().iterator();it.hasNext();){
      Map.Entry e = (Map.Entry)it.next();
      System.out.println(e.getKey() + "-" + e.getValue());
    }
    kaka.close();
    return ret;
  }

  public static GeneList glFromExternal(String in,String externalDb,int dbId, String dbURL){
    GeneList ret = new GeneList();
    try{
      ArrayList ens = getEnsemblIdsFromExternal(in, externalDb,dbId,dbURL);
      Gene g;
      for (Iterator it=ens.iterator();it.hasNext();){
        g = new Gene();
        g.ensembl = (String)it.next();
        g.externalDb = externalDb;
        g.externalDbId = dbId;
        g.externalDbEntry = in;
        ret.add(g);
      }
    }catch(Exception exc){
      exc.printStackTrace();
    }
    return ret;
  }

  public static GeneList glFromExternalList(ArrayList in,String externalDb,int dbId, String dbURL)throws java.sql.SQLException{
    GeneList ret = new GeneList();
    StringTokenizer tok;
    try{
      HashMap ens = getEnsemblIdsFromExternalList(in, externalDb,dbId,dbURL);
//    	HashMap ens = getEnsemblIdsFromExternalList(in,"hsapiens","hgnc_symbol","http://www.biomart.org/biomart/martservice");
      Gene g;
      String key,value,ensId;
      for (Iterator it=ens.keySet().iterator();it.hasNext();){
        key=(String)it.next();
        value = (String)ens.get(key);
        tok = new StringTokenizer(value,"|");
        while(tok.hasMoreElements()){
          ensId = (String)tok.nextElement();
          g = new Gene();
          g.ensembl = ensId;
          g.name = g.ensembl;
          g.externalDb = externalDb;
          g.externalDbId = dbId;
          g.externalDbEntry = key;
          ret.add(g);
        }
      }
    }catch(java.sql.SQLException se){
      se.printStackTrace();
      throw se;
    }
    catch(Exception exc){
      exc.printStackTrace();
    }
    return ret;
  }

  public static void runCommand(String command){
        String s = null;
        try {
            System.out.println("Running a system command...\n");
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader stdInput = new BufferedReader(new
                 InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new
                 InputStreamReader(p.getErrorStream()));
            System.out.println("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }
        }
        catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            System.exit(-1);
        }
  }

  public static ArrayList arrayListFromFile(String path){
    ArrayList list = new ArrayList();
    try{
        BufferedReader in  = new BufferedReader(new FileReader(path));
        String line=in.readLine();
        while (line!=null){
          line = line.trim();
          list.add(line);
          line = in.readLine();
        }
        in.close();
    }
    catch (Exception e){
      System.err.println("File input error");
    }
    return list;
  }

  public static ArrayList arrayListFromCommaString(String str){
    ArrayList list = new ArrayList();
    StringTokenizer tok = new StringTokenizer(str,",");
    while (tok.hasMoreElements()){
      list.add(tok.nextElement());
    }
    return list;
  }

  public static String arrayList2SqlInStr(ArrayList in){
    StringBuffer buf = new StringBuffer();
    buf.append("(");
    for (Iterator it = in.iterator();it.hasNext();){
      buf.append("'");
      buf.append(((String)it.next()).trim());
      buf.append("'");
      buf.append(",");
    }
    return buf.substring(0,buf.length()-1)+")";
  }

  public static String arrayList2CommaList(ArrayList in){
	    StringBuffer buf = new StringBuffer();
	    for (Iterator it = in.iterator();it.hasNext();){
	      buf.append(((String)it.next()).trim());
	      buf.append(",");
	    }
	    return buf.substring(0,buf.length()-1);
	  }
  
  private static HashSet compare(ArrayList samples,int minNrCommon){
    int nrOfSamples = samples.size();
    HashSet result = new HashSet();
    int sum=0;
    for (int i=0;i<nrOfSamples;i++){
      Iterator sample = ((HashSet)samples.get(i)).iterator();
      while (sample.hasNext()){
        String acc = (String)sample.next();
        for (int j=0;j<nrOfSamples;j++){
          HashSet temp = (HashSet)samples.get(j);
          if (temp.contains(acc)) sum++;
        }
        if (sum>minNrCommon && !result.contains(acc)) result.add(acc);
        sum=0;
      }
    }
    return result;

  }

  public static String getExtension(File f){
   String ext = null;
   String s = f.getName();
   int i = s.lastIndexOf('.');
   if (i>0 && i<s.length()-1){
      ext = s.substring(i+1).toLowerCase();
   }
   return ext;
  }

  public static File stringToFile(String string, String path) throws Exception
  {
      File file = new File(path);
      PrintWriter print = new PrintWriter(new FileOutputStream(file));
      print.write(string);
      print.close();
      return file;
  }

  public static File appendToFile(String string, String path) throws Exception
  {
      File file = new File(path);
      PrintWriter print = new PrintWriter(new FileOutputStream(file,true));
      print.write(string);
      print.close();
      return file;
  }

  
  public static String fileToString(File file) throws Exception
  {
      String string = "";
      BufferedReader bufReader = new BufferedReader(new FileReader(file));
      for (String temp = bufReader.readLine(); temp != null; temp = bufReader.readLine())
      {
        string = string + temp + "\n";
      }
      bufReader.close();
      return string;
  }

  /**
   * Main method for testing purposes.
   */
  public static void main (String[] args) throws Exception{
         //Tools.parseMatInspectorToGff("D:\\SAE\\temp\\matinsp.htm","D:\\SAE\\temp\\matinsp.gff");
	 //Document xmlDoc = Tools.createXmlDocFromFile("D:\\SAE\\temp\\test.xml");
         //BufferedImage img = Tools.createImg("title",xmlDoc);
         //Tools.writeImgToFile(img,"D:\\SAE\\temp\\out.jpg");
         //String xmlStr = Tools.parseMatInsp("D:\\SAE\\temp\\mat.html","testing",8500);
         //FileOutputStream fout = new FileOutputStream("D:\\SAE\\temp\\mat.xml");
         //OutputStreamWriter out = new OutputStreamWriter(fout);
         //out.write(xmlStr);
         //out.close();
         //Tools.parseMatInspectorToDb("D:\\SAE\\Projects\\desmoid\\ensembl\\matinspector\\Result_a2_upstream.htm","tcf4targets_upstream");

  }

}
