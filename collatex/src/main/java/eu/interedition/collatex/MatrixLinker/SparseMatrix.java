package eu.interedition.collatex.MatrixLinker;

import java.util.ArrayList;

import com.google.common.collect.ArrayTable;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.VariantGraphVertex;

public class SparseMatrix  {
	
	
	private ArrayTable<VariantGraphVertex, Token, Boolean> sparseMatrix;

	public SparseMatrix(Iterable<VariantGraphVertex> vertices, Iterable<Token> witness) {
    sparseMatrix = ArrayTable.create(vertices,witness);
	}

	

	public void set(int row, int column, boolean value) {
		sparseMatrix.set(row, column, value);
  }

	public boolean at(int row, int column) {
		Boolean result = sparseMatrix.at(row,column);
	  if(result==null)
	  	return false;
	  return result;
  }
	
  @Override
  public String toString() {
  	String result = "";
  	ArrayList<String> colLabels = columnLabels();
  	for(String cLabel : colLabels) {
	      result += " " + cLabel;
		}
  	result += "\n";
  	int colNum = sparseMatrix.columnKeyList().size();
  	ArrayList<String> rLabels = rowLabels();
  	int row = 0;
  	for(String label : rLabels) {
      result += label;
      for(int col=0; col<colNum;col++)
      	result += " " + at(row++,col);
      result += "\n";
  	}
		return result;
  }

  public String toHtml() {
  	String result = "<table>\n<tr><td></td>\n";
  	ArrayList<String> colLabels = columnLabels();
  	for(String cLabel : colLabels) {
	      result += "<td>" + cLabel + "</td>";
		}
  	result += "</tr>\n";
  	int colNum = sparseMatrix.columnKeyList().size();
  	ArrayList<String> rLabels = rowLabels();
  	int row = 0;
  	for(String label : rLabels) {
      result += "<tr><td>" + label + "</td>";
      for(int col=0; col<colNum;col++)
      	if(at(row,col))
      		result += "<td BGCOLOR=\"lightgreen\">M</td>";
      	else
      		result += "<td></td>";
      result += "</tr>\n";
      row++;
	  }
  	result += "</table>";
		return result;
  }
  
  public ArrayList<String> rowLabels() {
  	ArrayList<String> labels = new ArrayList<String>();
  	for(VariantGraphVertex vgv : sparseMatrix.rowKeyList()) {
		  String token = vgv.toString();
		  int pos = token.lastIndexOf(":");
		  if(pos>-1) {
		  	labels.add(token.substring(pos+2, token.length()-2));
		  }
  	}
    return labels;
  }
  
  public ArrayList<String> columnLabels() {
  	ArrayList<String> labels = new ArrayList<String>();
  	for(Token t : sparseMatrix.columnKeyList()) {
		  String token = t.toString();
		  int pos = token.lastIndexOf(":");
		  if(pos>-1) {
		    labels.add(token.substring(pos+2, token.length()-1));
		  }
  	}
    return labels;
  }

  public ArrayList<Coordinate> allTrues() {
  	ArrayList<Coordinate> pairs = new ArrayList<Coordinate>();
  	int rows = rowNum();
  	int cols = colNum();
  	for(int i=0; i<rows; i++) {
  		for(int j=0; j<cols; j++) {
  			if(at(i,j))
  				pairs.add(new Coordinate(i,j));
  			System.out.println();
  		}
  	}
  	return pairs;
  }
  
  public int rowNum() {
		return sparseMatrix.rowKeyList().size();
  }
  
  public int colNum() {
		return sparseMatrix.columnKeyList().size();
  }



	public ArrayList<ArrayList<Coordinate>> getIslands() {
		ArrayList<ArrayList<Coordinate>> islands = new ArrayList<ArrayList<Coordinate>>();
		ArrayList<Coordinate> allTrue = allTrues();
		for(Coordinate c: allTrue) {
			System.out.println("next coordinate: "+c);
			boolean found = false;
			while(!found) {
				for(ArrayList<Coordinate> alc : islands) {
					System.out.println("inspect island");
					for(Coordinate otherC : alc) {
						System.out.println("next on island: "+otherC);
						if(c.borders(otherC)) {
							System.out.println("add to island");
							alc.add(c);
							found = true;
						}
						if(found)
							break;
					}
					if(found)
						break;
				}
				if(!found) {
					System.out.println("new island");
					ArrayList<Coordinate> island = new ArrayList<Coordinate>();
					island.add(c);
					islands.add(island);
				}
				found = true;
			}
		}
	  // TODO Auto-generated method stub
	  return islands;
  }
}
