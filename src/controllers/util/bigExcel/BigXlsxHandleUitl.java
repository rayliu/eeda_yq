package controllers.util.bigExcel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Date;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.SAXHelper;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler;
import org.apache.poi.xssf.eventusermodel.XSSFSheetXMLHandler.SheetContentsHandler;
import org.apache.poi.xssf.extractor.XSSFEventBasedExcelExtractor;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFComment;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;

import controllers.profile.LoginUserController;

/**
 * A rudimentary XLSX -> CSV processor modeled on the
 * POI sample program XLS2CSVmra from the package
 * org.apache.poi.hssf.eventusermodel.examples.
 * As with the HSSF version, this tries to spot missing
 *  rows and cells, and output empty entries for them.
 * <p/>
 * Data sheets are read using a SAX parser to keep the
 * memory footprint relatively small, so this should be
 * able to read enormous workbooks.  The styles table and
 * the shared-string table must be kept in memory.  The
 * standard POI styles table class is used, but a custom
 * (read-only) class is used for the shared string table
 * because the standard POI SharedStringsTable grows very
 * quickly with the number of unique strings.
 * <p/>
 * For a more advanced implementation of SAX event parsing
 * of XLSX files, see {@link XSSFEventBasedExcelExtractor}
 * and {@link XSSFSheetXMLHandler}. Note that for many cases,
 * it may be possible to simply use those with a custom 
 * {@link SheetContentsHandler} and no SAX code needed of
 * your own!
 */
public class BigXlsxHandleUitl {
    /**
     * Uses the XSSF Event SAX helpers to do most of the work
     *  of parsing the Sheet XML, and outputs the contents
     *  as a (basic) CSV.
     */
    private class SheetToCSV implements SheetContentsHandler {
        private boolean firstCellOfRow = false;
        private int currentRow = -1;
        private int currentCol = -1;
        public String[] titleArray = new String[14];
        Record order  = new Record();
        
        private void outputMissingRows(int number) {
            for (int i=0; i<number; i++) {
                for (int j=0; j<minColumns; j++) {
                    output.append(',');
                }
                output.append('\n');
            }
        }

        @Override
        public void startRow(int rowNum) {
            // If there were gaps, output the missing rows
            outputMissingRows(rowNum-currentRow-1);
            // Prepare for this row
            firstCellOfRow = true;
            currentRow = rowNum;
            currentCol = -1;
        }

        @Override
        public void endRow(int rowNum) {
//            System.out.println("currentRow:"+currentRow+", rowNum:"+rowNum);
            // Ensure the minimum number of columns
//            for (int i=currentCol; i<minColumns; i++) {
//                output.append(',');
//            }
//            output.append('\n');
            order = new Record();
            if((currentRow)%1000==0){
				System.out.println(currentRow);
			}
//            if(currentRow>100)
//                return;
        }

        
        @Override
        public void cell(String cellReference, String formattedValue,
                XSSFComment comment) {
            //currentRow =0,  titles
        	
//            if(currentRow>100)
//                return;
//            
//            if (firstCellOfRow) {
//                firstCellOfRow = false;
//            } else {
//                output.append(',');
//            }

            // gracefully handle missing CellRef here in a similar way as XSSFCell does
            if(cellReference == null) {
                cellReference = new CellAddress(currentRow, currentCol).formatAsString();
            }

            // Did we miss any cells?
            int thisCol = (new CellReference(cellReference)).getCol();
//            int missedCols = thisCol - currentCol - 1;
//            for (int i=0; i<missedCols; i++) {
//                output.append(',');
//            }
//            currentCol = thisCol;
//            
//            // Number or string?
//            try {
//                //noinspection ResultOfMethodCallIgnored
//                Double.parseDouble(formattedValue);
//                output.append(formattedValue);
//            } catch (NumberFormatException e) {
//                output.append('"');
//                output.append(formattedValue);
//                output.append('"');
//            }
            
            if(currentRow == 0){
            	if("maktx".equals(formattedValue)){
            		formattedValue = "item_name";
            		titleArray[thisCol] = formattedValue;
            	}else if("matnr".equals(formattedValue)){
            		formattedValue = "item_no";
            		titleArray[thisCol] = formattedValue;
            	}else if("idnrk".equals(formattedValue)){
            		formattedValue = "part_no";
            		titleArray[thisCol] = formattedValue;
            	}else if("ojtxp".equals(formattedValue)){
            		formattedValue = "part_name";
            		titleArray[thisCol] = formattedValue;
            	}else if("bdmng".equals(formattedValue)){
            		formattedValue = "amount";
            		titleArray[thisCol] = formattedValue;
            	}else if("meins".equals(formattedValue)){
            		formattedValue = "unit";
            		titleArray[thisCol] = formattedValue;
            	}else if("pid".equals(formattedValue)){
            		formattedValue = "node";
            		titleArray[thisCol] = formattedValue;
            	}
        	}else{
        		String title = titleArray[thisCol];
        		if(thisCol == 13){
        			order.set("creator", 67); 
        			order.set("create_time", new Date());
    				order.set("office_id", 1); 
    				Db.save("wmsproduct", order);
        		}else{
        			if(title != null){
            			order.set(title, formattedValue);
            		}
        		}
        	}
        	
            
        }
        
//        @Override
//        public void cell(String cellReference, String formattedValue,
//                XSSFComment comment) {
//            //currentRow =0,  titles
//            
//            if(currentRow>100)
//                return;
//            
//            if (firstCellOfRow) {
//                firstCellOfRow = false;
//            } else {
//                output.append(',');
//            }
//
//            // gracefully handle missing CellRef here in a similar way as XSSFCell does
//            if(cellReference == null) {
//                cellReference = new CellAddress(currentRow, currentCol).formatAsString();
//            }
//
//            // Did we miss any cells?
//            int thisCol = (new CellReference(cellReference)).getCol();
//            int missedCols = thisCol - currentCol - 1;
//            for (int i=0; i<missedCols; i++) {
//                output.append(',');
//            }
//            currentCol = thisCol;
//            
//            // Number or string?
//            try {
//                //noinspection ResultOfMethodCallIgnored
//                Double.parseDouble(formattedValue);
//                output.append(formattedValue);
//            } catch (NumberFormatException e) {
//                output.append('"');
//                output.append(formattedValue);
//                output.append('"');
//            }
//            
//        }

        @Override
        public void headerFooter(String text, boolean isHeader, String tagName) {
            // Skip, no headers or footers in CSV
            System.out.println("text:"+text+", isHeader:"+isHeader+", tagName:"+tagName);
        }
    }


    ///////////////////////////////////////

    private final OPCPackage xlsxPackage;

    /**
     * Number of columns to read starting with leftmost
     */
    private final int minColumns;

    /**
     * Destination for data
     */
    private final PrintStream output;

    /**
     * Creates a new XLSX -> CSV converter
     *
     * @param pkg        The XLSX package to process
     * @param output     The PrintStream to output the CSV to
     * @param minColumns The minimum number of columns to output, or -1 for no minimum
     */
    public BigXlsxHandleUitl(OPCPackage pkg, PrintStream output, int minColumns) {
        this.xlsxPackage = pkg;
        this.output = output;
        this.minColumns = minColumns;
    }

    /**
     * Parses and shows the content of one sheet
     * using the specified styles and shared-strings tables.
     *
     * @param styles The table of styles that may be referenced by cells in the sheet
     * @param strings The table of strings that may be referenced by cells in the sheet
     * @param sheetInputStream The stream to read the sheet-data from.

     * @exception java.io.IOException An IO exception from the parser,
     *            possibly from a byte stream or character stream
     *            supplied by the application.
     * @throws SAXException if parsing the XML data fails.
     */
    public void processSheet(
            StylesTable styles,
            ReadOnlySharedStringsTable strings,
            SheetContentsHandler sheetHandler, 
            InputStream sheetInputStream) throws IOException, SAXException {
        DataFormatter formatter = new DataFormatter();
        InputSource sheetSource = new InputSource(sheetInputStream);
        try {
            XMLReader sheetParser = SAXHelper.newXMLReader();
            ContentHandler handler = new XSSFSheetXMLHandler(
                  styles, null, strings, sheetHandler, formatter, false);
            sheetParser.setContentHandler(handler);
            sheetParser.parse(sheetSource);
         } catch(ParserConfigurationException e) {
            throw new RuntimeException("SAX parser appears to be broken - " + e.getMessage());
         }
    }

    /**
     * Initiates the processing of the XLS workbook file to CSV.
     *
     * @throws IOException If reading the data from the package fails.
     * @throws SAXException if parsing the XML data fails.
     */
    public void process() throws IOException, OpenXML4JException, SAXException {
        ReadOnlySharedStringsTable strings = new ReadOnlySharedStringsTable(this.xlsxPackage);
        XSSFReader xssfReader = new XSSFReader(this.xlsxPackage);
        StylesTable styles = xssfReader.getStylesTable();
        XSSFReader.SheetIterator iter = (XSSFReader.SheetIterator) xssfReader.getSheetsData();
        int index = 0;
        while (iter.hasNext()) {
            InputStream stream = iter.next();
            String sheetName = iter.getSheetName();
            this.output.println();
            this.output.println(sheetName + " [index=" + index + "]:");
            processSheet(styles, strings, new SheetToCSV(), stream);
            stream.close();
            ++index;
        }
        
    }

    public static void processFile(String filePath) throws Exception {
//        String filePath = "/Users/a13570610691/Downloads/SapBomMay31.xlsx";

        File xlsxFile = new File(filePath);
        if (!xlsxFile.exists()) {
            System.err.println("Not found or not a file: " + xlsxFile.getPath());
            return;
        }

        int minColumns = -1;
//        if (args.length >= 2)
//            minColumns = Integer.parseInt(args[1]);

        // The package open is instantaneous, as it should be.
        OPCPackage p = OPCPackage.open(xlsxFile.getPath(), PackageAccess.READ);//
        BigXlsxHandleUitl xlsx2csv = new BigXlsxHandleUitl(p, System.out, minColumns);
        xlsx2csv.process();
        p.close();
    }
    
    public static void main(String[] args) throws Exception {
        processFile("C:/Users/Administrator/Desktop/product数据/SAP BOM May 31.xlsx");
    }
}
