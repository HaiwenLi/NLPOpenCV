package Util;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

public class TextLocations extends PDFTextStripper{
	/**
	 *  Instantiate a new PDFTextStripper object for get text positions
	 *  for all characters in the file
	 */
	public float MinLineSeperateWidth = 2.0f;
	private Set<Float> FontSizes;
	private double[] PageMargin;
	private List<String> TextStream;
	private List<TextPosition> CharPositionList;
	private PDDocument Doc;
	
	public TextLocations(PDDocument document) throws IOException{
		this.Doc = document;
		this.TextStream = new ArrayList<String>();
		this.CharPositionList = new ArrayList<TextPosition>();
		this.FontSizes = new HashSet<Float>();
		PageMargin = new double[4];
	}
	
	protected void GetCharPosition(){
		if (this.Doc != null){
			this.setSortByPosition(true);
			this.setSuppressDuplicateOverlappingText(true);
			this.setStartPage(0);
			this.setEndPage(Doc.getNumberOfPages());
			
			try{
				Writer dummy = new OutputStreamWriter(new ByteArrayOutputStream());
	            this.writeText(Doc, dummy);
			} catch (IOException e){
				System.out.println(e.getMessage());
			}
		}
	}
	
	/**
     * Override the default functionality of PDFTextStripper.
     */
    @Override
    protected void writeString(String string, List<TextPosition> textPositions) throws IOException
    {
    	// getXDirAdj(): This will get the text direction adjusted x position of the character.
    	// getYDirAdj(): This will get the y position of the text, adjusted so that 0,0 is upper 
    	//               left and it is adjusted based on the text direction.
    	// getXScale():  This will get the X scaling factor.
    	// getYScale():  This will get the Y scaling factor.
    	// getFontSize():     This will get the font size that has been set with the "Tf" operator (Set text font and size).
    	// getFontSizeInPt(): This will get the font size in pt.
    	// getDir(): Return the direction/orientation of the string in this object based on its text matrix.
    	// getHeightDir(): 	  This will get the maximum height of all characters in this string.
    	// getWidthDirAdj():  This will get the width of the string when text direction adjusted coordinates are used.
    	// getWidthOfSpace(): This will get the width of a space character.
    	// getUnicode():      Return the string of characters stored in this object.
    	
        for (TextPosition text : textPositions){
        	CharPositionList.add(text); 
        	FontSizes.add(text.getFontSize());
//        	System.out.println("String[" + text.getXDirAdj() + ","
//                    + text.getYDirAdj() + " fs=" + text.getFontSize() + " xscale="
//                    + text.getXScale() + " height=" + text.getHeightDir() + " space="
//                    + text.getWidthOfSpace() + " width="
//                    + text.getWidthDirAdj() + "]" + text.getUnicode());
        }
    }
	
    public int[] GetFontRange(){
    	int[] fontRange = new int[2];
    	fontRange[0] = 1000;
    	fontRange[1] = 0;
    	
    	for (float font_size : FontSizes){
    		int size = (int) font_size;
    		if (size <= fontRange[0]) { fontRange[0] = size; }
    		if (size > fontRange[1]) { fontRange[1] = size; }
    	}
    	return fontRange;
    }
    
    public double[] GetPageMargin(){
    	return this.PageMargin;
    }
    
    public List<String> ExtractText(){
    	TextStream.clear();
    	CharPositionList.clear();
    	FontSizes.clear();
    	GetCharPosition();
    	
    	if (!CharPositionList.isEmpty()){
	    	float line_y = 0f, y = 0f;
	    	String strLine = "", word = "";
	    	
	    	for (int i=0; i<CharPositionList.size(); ++i){
	    		TextPosition pos = CharPositionList.get(i);
	    		y = pos.getYDirAdj();
	    		
	    		if (i ==0){
	    			this.PageMargin[0] = pos.getXDirAdj();
	    			this.PageMargin[2] = pos.getYDirAdj();
	    			this.PageMargin[1] = this.PageMargin[0];
	    			this.PageMargin[3] = this.PageMargin[2];
	    		} else{
	    			if (this.PageMargin[0] > pos.getXDirAdj()){ this.PageMargin[0] = pos.getXDirAdj(); }
	    			if (this.PageMargin[1] <= pos.getXDirAdj()){ this.PageMargin[1] = pos.getXDirAdj(); }
	    			if (this.PageMargin[2] > pos.getYDirAdj()){ this.PageMargin[2] = pos.getYDirAdj(); }
	    			if (this.PageMargin[3] <= pos.getYDirAdj()){ this.PageMargin[3] = pos.getYDirAdj(); }
	    		}
	    		
	    		// Get the y position of each character and judge whether they are in the same line
	    		if (i == 0){
	    			MinLineSeperateWidth = pos.getHeight();
	    			line_y = y;
	    		}
	    		word = pos.getUnicode();
				word = word.replaceAll("[\\f\\n\\r\\t\\v]+",""); // remove all empty character except for space
				word = word.replaceAll(" +", "");                // merge many spaces into one space
				
    			if (Math.abs(y - line_y) < MinLineSeperateWidth){
    				if (word.length()>0){ strLine += word; }
    			}
    			else{
    				TextStream.add(strLine);
    				strLine = word;
    				line_y = y;
    				MinLineSeperateWidth = pos.getHeight();
    			}
	    	}
	    	if (strLine.length()>0){ TextStream.add(strLine); }
    	}
    	return TextStream;
    }
}