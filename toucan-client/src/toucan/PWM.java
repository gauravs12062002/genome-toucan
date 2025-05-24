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

import org.biojava.bio.dist.*;
import org.biojava.bio.dp.*;
import org.biojava.bio.seq.*;
import org.biojava.bio.symbol.*;
import org.biojava.bio.dist.Distribution;
import org.biojava.bio.dp.WeightMatrix;


public class PWM {

	public String name;
	public int w;
	public double[][] counts;
	public Distribution[] dists;
	public WeightMatrix wm;
	
	public int getW(){
		return counts.length;
	}
	
	public void setDistsFromCounts(boolean pseudoCountYesNo) throws Exception{
	 	FiniteAlphabet alpha = DNATools.getDNA();
	    AlphabetIndex index = AlphabetManager.getAlphabetIndex(alpha);
	    this.dists = new Distribution[counts.length];
	    try {
	for (int k=0;k<counts.length;k++){
	    	Count c = new IndexedCount(alpha);
	      c.increaseCount(DNATools.a(),counts[k][0]);
	      c.increaseCount(DNATools.c(),counts[k][1]);
	      c.increaseCount(DNATools.g(),counts[k][2]);
	      c.increaseCount(DNATools.t(),counts[k][3]);
	      
	      if(pseudoCountYesNo){
	      	c.increaseCount(DNATools.a(),0.25);
		      c.increaseCount(DNATools.c(),0.25);
		      c.increaseCount(DNATools.g(),0.25);
		      c.increaseCount(DNATools.t(),0.25);
		      	
	      }
	      
	      //System.out.println("COUNT");
	      
	      //for (int i = 0; i < alpha.size(); i++) {
	      //  AtomicSymbol s = (AtomicSymbol)index.symbolForIndex(i);
	        //System.out.println(s.getName()+" : "+c.getCount(s));
	      //}
	      
	      //transformer en Distribution
	      dists[k] = DistributionTools.countToDistribution(c);
	      
	      //System.out.println("\nDISTRIBUTION");
	      
	      //for (int i = 0; i < alpha.size(); i++) {
	       // Symbol s = index.symbolForIndex(i);
	        //System.out.println(s.getName()+" : "+dists[k].getWeight(s));
	      //}
	}
	    }
	    catch (Exception ex) {
	      ex.printStackTrace();
	    }
	 }
	
	public void setWmFromDists() throws Exception{
		wm = new SimpleWeightMatrix(dists);
	}
	
	public double getInfoContent(){
	    double info = 0.0;
	    for (int i = 0; i < dists.length; i++) {
	      info += DistributionTools.bitsOfInformation(dists[i]);  
	    }
	    return info;
	  }
	
	public double getMyInfoContent()throws Exception{
		double snfA=0.283012;
		double snfC=0.220988;
		double snfG=0.219651;
		double snfT=0.276349;
		double info = 0.0;
		for (int i = 0; i < dists.length; i++) {
			info += dists[i].getWeight(DNATools.a()) * Math.log(dists[i].getWeight(DNATools.a()) / snfA);
			info += dists[i].getWeight(DNATools.c()) * Math.log(dists[i].getWeight(DNATools.c()) / snfC);
			info += dists[i].getWeight(DNATools.g()) * Math.log(dists[i].getWeight(DNATools.g()) / snfG);
			info += dists[i].getWeight(DNATools.t()) * Math.log(dists[i].getWeight(DNATools.t()) / snfT);
		}		
		return info;
	}
	
	public double getEntropy(){
		double info = 0.0;
	    for (int i = 0; i < dists.length; i++) {
	      info += DistributionTools.totalEntropy(dists[i]);
	      
	    }
	    return info;
	  }



}
