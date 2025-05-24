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
package toucan.gui;
import java.awt.*;
import javax.swing.*;
import org.biojava.bio.*;
import org.biojava.bio.dist.Distribution;
import org.biojava.bio.dist.DistributionTools;
import org.biojava.bio.dp.WeightMatrix;
import org.biojava.bio.gui.*;
import java.awt.image.*;
import java.awt.*;
import java.io.*;

public class WMPanel extends JPanel {
    private WeightMatrix wm;
    private DistributionLogo[] logos;


    public WMPanel(WeightMatrix wm) throws Exception{
        super();
        this.wm = wm;
        setBackground(Color.white);
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        try {
            setLayout(new GridLayout(1, wm.columns()));
            logos = new DistributionLogo[wm.columns()];
            for (int pos = 0; pos < wm.columns(); ++pos) {
                Distribution dist = wm.getColumn(pos);
                DistributionLogo dl = new DistributionLogo();
                dl.setRenderingHints(hints);
                dl.setBackground(Color.white);
                dl.setOpaque(true);
                dl.setDistribution(dist);
                dl.setPreferredSize(new Dimension(40,50));
                dl.setLogoPainter(new TextLogoPainter());
                dl.setStyle(new DNAStyle());
               
                /*BufferedImage bufIm = new BufferedImage(300, 300,
                BufferedImage.TYPE_INT_RGB);
                dl.getGraphics().drawImage(bufIm,0,0,null);
                File outfile = new File("/Users/saerts/tmp/motif.jpeg");
                FileOutputStream outstream = new FileOutputStream(outfile);
                com.sun.image.codec.jpeg.JPEGImageEncoder encoder =
                com.sun.image.codec.jpeg.JPEGCodec.createJPEGEncoder(outstream);
                encoder.encode(bufIm);
                //Graphics g = bufIm.createGraphics();
                */
                add(dl);
                logos[pos] = dl;
            }
        } catch (BioException ex) {
            throw new BioError(ex);
        }
    }
}