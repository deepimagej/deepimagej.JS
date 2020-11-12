/*
 * DeepImageJ
 * 
 * https://deepimagej.github.io/deepimagej/
 *
 * Conditions of use: You are free to use this software for research or educational purposes. 
 * In addition, we expect you to include adequate citations and acknowledgments whenever you 
 * present or publish results that are based on it.
 * 
 * Reference: DeepImageJ: A user-friendly plugin to run deep learning models in ImageJ
 * E. Gomez-de-Mariscal, C. Garcia-Lopez-de-Haro, L. Donati, M. Unser, A. Munoz-Barrutia, D. Sage. 
 * Submitted 2019.
 *
 * Bioengineering and Aerospace Engineering Department, Universidad Carlos III de Madrid, Spain
 * Biomedical Imaging Group, Ecole polytechnique federale de Lausanne (EPFL), Switzerland
 *
 * Corresponding authors: mamunozb@ing.uc3m.es, daniel.sage@epfl.ch
 *
 */

/*
 * Copyright 2019. Universidad Carlos III, Madrid, Spain and EPFL, Lausanne, Switzerland.
 * 
 * This file is part of DeepImageJ.
 * 
 * DeepImageJ is free software: you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation, either 
 * version 3 of the License, or (at your option) any later version.
 * 
 * DeepImageJ is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with DeepImageJ. 
 * If not, see <http://www.gnu.org/licenses/>.
 */

package deepimagej.tools;

import ij.IJ;
import ij.ImagePlus;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class CompactMirroring {
	
	public static ImagePlus mirrorXY(ImagePlus imp, 
							   int paddingXLeft, int paddingXRight,
							   int paddingYTop, int paddingYBottom,
							   int paddingZFront, int paddingZBack) {
		int nx = imp.getWidth();
		int ny = imp.getHeight();
		int nc = imp.getNChannels();
		int nz = imp.getNSlices();
		int nt = imp.getNFrames();
		ImagePlus out = IJ.createImage("Mirror", "32-bits", nx + paddingXLeft + paddingXRight,
										ny + paddingYTop + paddingYBottom, nc, nz + paddingZFront + paddingZBack, nt);
		for(int c=0; c<nc; c++)
			for(int z=0; z<nz; z++)
				for(int t=0; t<nt; t++) {
					imp.setPositionWithoutUpdate(c + 1, z + 1, t + 1);
					out.setPositionWithoutUpdate(c + 1, z + paddingZFront + 1, t + 1);
					ImageProcessor ip = imp.getProcessor();
					ImageProcessor op = mirrorXY(ip, paddingXLeft, paddingXRight,
							   					 paddingYTop, paddingYBottom);
					out.setProcessor(op);
					if (z < paddingZFront) {
						out.setPositionWithoutUpdate(c + 1, z + 1, t + 1);
					} else if (z >= nz - paddingZBack) {
						int sliceZBack = 2 * nz - z;
						out.setPositionWithoutUpdate(c + 1, sliceZBack, t + 1);
					}
					out.setProcessor(op);
				}
		return out;
	}
	
	private static ImageProcessor mirrorXY(ImageProcessor ip,
								   int paddingXLeft, int paddingXRight,
								   int paddingYTop, int paddingYBottom) {
		int nx = ip.getWidth();
		int ny = ip.getHeight();
		FloatProcessor fp = new FloatProcessor(nx + paddingXLeft + paddingXRight,
												ny + paddingYTop + paddingYBottom);
		int periodX = 2*nx - 2;
		int periodY = 2*ny - 2;
		int xm, ym;
		for(int x = -paddingXLeft; x<nx+paddingXRight; x++) {
			xm = mirror(x, nx, periodX);
			for(int y = -paddingYTop; y<ny+paddingYBottom; y++) {
				ym = mirror(y, ny, periodY);
				double v = ip.getPixelValue(xm, ym);
				fp.putPixelValue(x+paddingXLeft, y+paddingYTop, v);
			}
		}
		return fp;
	}
	
	private static int mirror(int a, int n, int period) {
		while (a < 0)
			a += period;
		while (a >= n) {
			a = period - a;
			a = (a < 0 ? -a : a);
		}
		return a;
	}
}
