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
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.io.StringReader;
import java.util.Iterator; 

public class PWMList extends ArrayList{

	public String name;
	public String source;
	
	public void constructFromMxFile(String path) throws Exception{ //rows: different matrices
		BufferedReader in  = new BufferedReader(new FileReader(path));
		constructFromMxBuffReader(in);
	}
	
	public void constructFromMxString(String str) throws Exception{
		BufferedReader in  = new BufferedReader(new StringReader(str));
		constructFromMxBuffReader(in);
	}
	
	public void constructFromMxBuffReader(BufferedReader in) throws Exception{ //rows: different matrices
        String line=in.readLine();
        String name;
        int w;
        PWM pwm;
        while (line!=null){
        		if(line.startsWith(">")){
        			String[] temp = line.split("\t");
        			pwm = new PWM();
        			pwm.name = temp[0].substring(1,temp[0].length());
        			//System.out.println(pwm.name);
        			pwm.w = Integer.parseInt(temp[1]);
        			pwm.counts = new double[pwm.w][4];
        			//System.out.println(pwm.w);
        			for (int i=0;i<pwm.w;i++){
        				line = in.readLine();
        				String[] acgt = line.split("\t");
        				for (int j=0;j<4;j++){
        					pwm.counts[i][j]=Integer.parseInt(acgt[j]);
        				}
        			}
        			pwm.setDistsFromCounts(true);
        			pwm.setWmFromDists();
        			add(pwm);
        		}
        		line=in.readLine();
        		if(line.startsWith("<")){
        			line = in.readLine();
        		}
        		else System.out.println("Something wrong with closing sign <");
        }
	}
	
	public double getTotalInfoContent()throws Exception{
		PWM wm;
		double info=0.0d;
		for(Iterator it = this.iterator();it.hasNext();){
			wm=(PWM)it.next();
			info+=wm.getMyInfoContent();
		}
		return info;
	}
	
	
}

