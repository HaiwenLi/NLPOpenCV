package Util;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import javax.imageio.ImageIO;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.resizers.configurations.ScalingMode;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

/*
 * ConvertPDFToPng class: 将PDF简历转化为PNG图像(缩略图)，用于在Dashboard页面显示用户已经上传的简历文件
 */

public class ConvertPDFToPng {
	// pdfFilename为PDF文件路径 ，resolution为不同分辨率
	public boolean ConvertPDFToImage(String pdfFilename, int resolution){
		if (!pdfFilename.endsWith(".pdf") && !pdfFilename.endsWith(".PDF")){
			return false;
		}
		
		File file = new File(pdfFilename);
		try {
			RandomAccessFile raf = new RandomAccessFile(file, "r");
		    FileChannel channel = raf.getChannel();
		    try {
				MappedByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
				PDFFile pdffile = new PDFFile(buf);
				
				// Only process the first page
				PDFPage page = pdffile.getPage(0);
			    Rectangle rect = new Rectangle(0, 0, ((int) page.getBBox().getWidth()),
                                              ((int) page.getBBox().getHeight()));
			    
                Image img = page.getImage(rect.width*resolution, rect.height*resolution, rect, null, true, true);
                BufferedImage tag = new BufferedImage(rect.width*resolution, rect.height*resolution, BufferedImage.TYPE_INT_RGB);
                tag.getGraphics().drawImage(img, 0, 0, rect.width*resolution, rect.height*resolution, null);
                
                String getFilename = file.getAbsolutePath(); // file.getName();
                String filename = getFilename.substring(0, getFilename.lastIndexOf("."));
                
                // 保存PNG
                FileOutputStream out = new FileOutputStream(filename + "-Page0.png");
                ImageIO.write(tag, "png", out);
                out.close();				
                channel.close();
                raf.close();	
                return true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}	
		return false;
	}
	
	// 生成PNG缩约图
	public void Thumb(String pngFilename, String outputFilename, double scale) throws IOException {
		String [] inputFilename = {pngFilename};
		Thumbnails.of(inputFilename).scale(scale).scalingMode(ScalingMode.BICUBIC).toFile(outputFilename);    
	}
	
	// 对PDF文件首页生成缩略图
	public boolean GenerateThumbnails(String pdfFilename,String thumbnail_filename, int type){
		// Check whether the PNG file has been created
		String pngFilename = pdfFilename.substring(0, pdfFilename.lastIndexOf(".")) + "-Page0.png";
		File pngFile = new File(pngFilename);
		
		if (!pngFile.exists()){
			this.ConvertPDFToImage(pdfFilename, 2);// resolution: 2
		}
		
		try{
			if (type == 0){ // small
				this.Thumb(pngFilename,thumbnail_filename,0.4);
			} 
			else if (type == 1){ // large
				this.Thumb(pngFilename,thumbnail_filename,0.6);
			}
			return true;
		} catch (IOException e){
			e.printStackTrace();
		}
		return false;
	}
}
