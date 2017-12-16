package dom5utils;
/* This file is part of dom5utils.
 *
 * dom5utils is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * dom5utils is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with dom5utils.  If not, see <http://www.gnu.org/licenses/>.
 */
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class NationStatIndexer extends AbstractStatIndexer {
	
	enum unitType {
		PRETENDER(2, "pretender_types_by_nation"),
		UNPRETENDER(1, "unpretender_types_by_nation"),
		TROOP(0, "fort_troop_types_by_nation"),
		UNKNOWN(-1, ""),
		LEADER(-2, "fort_leader_types_by_nation"),
		NONFORT_TROOP(-3, "nonfort_troop_types_by_nation"),
		NONFORT_LEADER(-4, "nonfort_leader_types_by_nation"),
		COAST_TROOP(-5, "coast_troop_types_by_nation"),
		COAST_LEADER(-6, "coast_leader_types_by_nation");
		
		private int id;
		private String filename;
		
		unitType(int i, String filename) {
			id = i;
			this.filename = filename;
		}
		public static unitType fromValue(int id) {
	        for (unitType aip: values()) {
	            if (aip.getId() == id) {
	                return aip;
	            }
	        }
	        return null;
	    }
		public int getId() { return id;}
		public String getFilename() { return filename;}
	}
	
	public static void main(String[] args) {
		run();
	}
	
	public static void run() {
		FileInputStream stream = null;
		try {
	        long startIndex = Starts.NATION;
	        int ch;

			stream = new FileInputStream(EXE_NAME);			
			stream.skip(startIndex);
			
			XSSFWorkbook wb = NationStatIndexer.readFile("BaseN_Template.xlsx");
			
			FileOutputStream fos = new FileOutputStream("BaseN.xlsx");
			XSSFSheet sheet = wb.getSheetAt(0);

			// name
			InputStreamReader isr = new InputStreamReader(stream, "ISO-8859-1");
	        Reader in = new BufferedReader(isr);
	        int rowNumber = 1;
			while ((ch = in.read()) > -1) {
				StringBuffer name = new StringBuffer();
				while (ch != 0) {
					name.append((char)ch);
					ch = in.read();
				}
				if (name.length() == 0) {
					continue;
				}
				if (name.toString().equals("end")) {
					break;
				}
				in.close();

				stream = new FileInputStream(EXE_NAME);		
				startIndex = startIndex + Starts.NATION_SIZE;
				stream.skip(startIndex);
				isr = new InputStreamReader(stream, "ISO-8859-1");
		        in = new BufferedReader(isr);

				XSSFRow row = sheet.createRow(rowNumber);
				XSSFCell cell1 = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
				cell1.setCellValue(rowNumber-1);
				XSSFCell cell = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
				cell.setCellValue(name.toString());
				rowNumber++;
			}
			in.close();
			stream.close();

			// epithet
			putString(sheet, 36l, 2, Starts.NATION, Starts.NATION_SIZE);
			
			// abbreviation
			putString(sheet, 72l, 3, Starts.NATION, Starts.NATION_SIZE);

			// file_name_base
			putString(sheet, 77l, 4, Starts.NATION, Starts.NATION_SIZE);
			
			wb.write(fos);
			fos.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
				
		// attributes_by_nation
		try {
	        long startIndex = Starts.NATION;
	        int ch;

			stream = new FileInputStream(EXE_NAME);			
			stream.skip(startIndex);
			
			XSSFWorkbook wb = NationStatIndexer.readFile("attributes_by_nation_Template.xlsx");
			
			FileOutputStream fos = new FileOutputStream("attributes_by_nation.xlsx");
			XSSFSheet sheet = wb.getSheetAt(0);

			// name
			InputStreamReader isr = new InputStreamReader(stream, "ISO-8859-1");
	        Reader in = new BufferedReader(isr);
	        int rowNumber = 1;
	        List<Attribute> attributes = new ArrayList<Attribute>();
			while ((ch = in.read()) > -1) {
				StringBuffer name = new StringBuffer();
				while (ch != 0) {
					name.append((char)ch);
					ch = in.read();
				}
				if (name.length() == 0) {
					continue;
				}
				if (name.toString().equals("end")) {
					break;
				}
				in.close();
				
				long newIndex = startIndex+172l;
				
				int attrib = getBytes4(newIndex);
				long valueIndex = newIndex + 388l;
				long value = getBytes4(valueIndex);
				while (attrib != 0) {
					attributes.add(new Attribute(rowNumber-1, attrib, value));
					newIndex+=4;
					valueIndex+=8;
					attrib = getBytes4(newIndex);
					value = getBytes4(valueIndex);
				}
				rowNumber++;

				stream = new FileInputStream(EXE_NAME);		
				startIndex = startIndex + Starts.NATION_SIZE;
				stream.skip(startIndex);
				isr = new InputStreamReader(stream, "ISO-8859-1");
		        in = new BufferedReader(isr);
				
			}
			
			Set<Integer> heroes = new TreeSet<Integer>();
			int rowNum = 1;
			for (Attribute attribute : attributes) {
				XSSFRow row = sheet.createRow(rowNum);
				XSSFCell cell1 = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
				cell1.setCellValue(attribute.object_number);
				XSSFCell cell2 = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
				cell2.setCellValue(attribute.attribute);
				cell2 = row.getCell(2, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
				cell2.setCellValue(attribute.raw_value);
				if (attribute.attribute >= 139 && attribute.attribute <= 150) {
					heroes.add((int)attribute.raw_value);
				}
				rowNum++;
			}
			
			for (Integer hero : heroes) {
				System.out.println(hero);
			}

			in.close();
			stream.close();

			wb.write(fos);
			fos.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		// *_types_by_nation
		try {
	        long startIndex = Starts.NATION;
	        int ch;

			stream = new FileInputStream(EXE_NAME);			
			stream.skip(startIndex);
			
			// name
			InputStreamReader isr = new InputStreamReader(stream, "ISO-8859-1");
	        Reader in = new BufferedReader(isr);
	        int rowNumber = 1;
	        
	        Map<unitType, List<Troops>> unitMap = new HashMap<unitType, List<Troops>>();
			while ((ch = in.read()) > -1) {
				StringBuffer name = new StringBuffer();
				while (ch != 0) {
					name.append((char)ch);
					ch = in.read();
				}
				if (name.length() == 0) {
					continue;
				}
				if (name.toString().equals("end")) {
					break;
				}
				in.close();
				
				long newIndex = startIndex+1328l;
				
				unitType type = unitType.TROOP;
				if (unitMap.get(type) == null) {
					unitMap.put(type, new ArrayList<Troops>());
				}
				
				int attrib = getBytes4(newIndex);
				while (attrib != 0) {
					if (attrib < 0) {
						if (attrib != -1) {
							type = unitType.fromValue(attrib);
							if (unitMap.get(type) == null) {
								unitMap.put(type, new ArrayList<Troops>());
							}
						}
					} else {
						unitMap.get(type).add(new Troops(rowNumber-1, attrib));
					}
					newIndex+=4;
					attrib = getBytes4(newIndex);
				}
				rowNumber++;

				stream = new FileInputStream(EXE_NAME);		
				startIndex = startIndex + Starts.NATION_SIZE;
				stream.skip(startIndex);
				isr = new InputStreamReader(stream, "ISO-8859-1");
		        in = new BufferedReader(isr);
				
			}
			
			for (Map.Entry<unitType, List<Troops>> entry : unitMap.entrySet()) {
				if (entry.getKey() == unitType.UNKNOWN) { continue; }
				XSSFWorkbook wb = NationStatIndexer.readFile(entry.getKey().getFilename() + "_Template.xlsx");
				FileOutputStream fos = new FileOutputStream(entry.getKey().getFilename() + ".xlsx");
				XSSFSheet sheet = wb.getSheetAt(0);
				int rowNum = 1;
				for (Troops troop : entry.getValue()) {
					XSSFRow row = sheet.createRow(rowNum);
					XSSFCell cell1 = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					cell1.setCellValue(troop.monster_number);
					XSSFCell cell2 = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					cell2.setCellValue(troop.nation_number);
					rowNum++;
				}
				wb.write(fos);
				fos.close();
			}

			in.close();
			stream.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// un/pretender_types_by_nation
		try {
	        long startIndex = Starts.NATION;
	        int ch;

			stream = new FileInputStream(EXE_NAME);			
			stream.skip(startIndex);
			
			// name
			InputStreamReader isr = new InputStreamReader(stream, "ISO-8859-1");
	        Reader in = new BufferedReader(isr);
	        int rowNumber = 1;
	        
	        Map<unitType, List<Troops>> unitMap = new HashMap<unitType, List<Troops>>();
			while ((ch = in.read()) > -1) {
				StringBuffer name = new StringBuffer();
				while (ch != 0) {
					name.append((char)ch);
					ch = in.read();
				}
				if (name.length() == 0) {
					continue;
				}
				if (name.toString().equals("end")) {
					break;
				}
				in.close();
				
				long newIndex = startIndex+1496l;
				
				int attrib = getBytes4(newIndex);
				while (attrib != 0) {
					if (attrib < 0) {
						if (unitMap.get(unitType.UNPRETENDER) == null) {
							unitMap.put(unitType.UNPRETENDER, new ArrayList<Troops>());
						}
						unitMap.get(unitType.UNPRETENDER).add(new Troops(rowNumber-1, Math.abs(attrib)));
					} else {
						if (unitMap.get(unitType.PRETENDER) == null) {
							unitMap.put(unitType.PRETENDER, new ArrayList<Troops>());
						}
						unitMap.get(unitType.PRETENDER).add(new Troops(rowNumber-1, attrib));
					}
					newIndex+=4;
					attrib = getBytes4(newIndex);
				}
				rowNumber++;

				stream = new FileInputStream(EXE_NAME);		
				startIndex = startIndex + Starts.NATION_SIZE;
				stream.skip(startIndex);
				isr = new InputStreamReader(stream, "ISO-8859-1");
		        in = new BufferedReader(isr);
				
			}
			
			for (Map.Entry<unitType, List<Troops>> entry : unitMap.entrySet()) {
				if (entry.getKey() == unitType.UNKNOWN) { continue; }
				XSSFWorkbook wb = NationStatIndexer.readFile(entry.getKey().getFilename() + "_Template.xlsx");
				FileOutputStream fos = new FileOutputStream(entry.getKey().getFilename() + ".xlsx");
				XSSFSheet sheet = wb.getSheetAt(0);
				int rowNum = 1;
				for (Troops troop : entry.getValue()) {
					XSSFRow row = sheet.createRow(rowNum);
					XSSFCell cell1 = row.getCell(0, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					cell1.setCellValue(troop.monster_number);
					XSSFCell cell2 = row.getCell(1, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
					cell2.setCellValue(troop.nation_number);
					rowNum++;
				}
				wb.write(fos);
				fos.close();
			}

			in.close();
			stream.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}	
	
	private static class Troops {
		int nation_number;
		int monster_number;

		public Troops(int nation_number, int monster_number) {
			super();
			this.nation_number = nation_number;
			this.monster_number = monster_number;
		}
		
	}
}
