package eu.interedition.collatex.matrixlinker;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.collect.ArrayTable;
import com.google.common.collect.Lists;

import eu.interedition.collatex.Token;
import eu.interedition.collatex.graph.VariantGraph;
import eu.interedition.collatex.graph.VariantGraphVertex;
import eu.interedition.collatex.matching.Matches;

public class MatchMatrix {
  static Logger LOG = LoggerFactory.getLogger(MatchMatrix.class);

  public static MatchMatrix create(VariantGraph base, Iterable<Token> witness, Comparator<Token> comparator) {
    base.rank();
    Matches matches = Matches.between(base.vertices(), witness, comparator);
    MatchMatrix arrayTable = new MatchMatrix(base.vertices(), witness);
    Set<Token> unique = matches.getUnique();
    Set<Token> ambiguous = matches.getAmbiguous();
    int column = 0;
    for (Token t : witness) {
      if (unique.contains(t)) {
        arrayTable.set(matches.getAll().get(t).get(0).getRank() - 1, column, true);
      } else {
        if (ambiguous.contains(t)) {
          for (VariantGraphVertex vgv : matches.getAll().get(t)) {
            arrayTable.set(vgv.getRank() - 1, column, true);
          }
        }
      }
      column++;
    }
    return arrayTable;
  }

  private final ArrayTable<VariantGraphVertex, Token, Boolean> sparseMatrix;

  public MatchMatrix(Iterable<VariantGraphVertex> vertices, Iterable<Token> witness) {
    sparseMatrix = ArrayTable.create(vertices, witness);
  }

  public Boolean at(int row, int column) {
    return Objects.firstNonNull(sparseMatrix.at(row, column), false);
  }

  public void set(int row, int column, boolean value) {
    sparseMatrix.set(row, column, value);
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    ArrayList<String> colLabels = columnLabels();
    for (String cLabel : colLabels) {
      result.append(" ").append(cLabel);
    }
    result.append("\n");
    int colNum = sparseMatrix.columnKeyList().size();
    ArrayList<String> rLabels = rowLabels();
    int row = 0;
    for (String label : rLabels) {
      result.append(label);
      for (int col = 0; col < colNum; col++)
        result.append(" ").append(at(row++, col));
      result.append("\n");
    }
    return result.toString();
  }

  public String toHtml() {
    StringBuilder result = new StringBuilder("<table>\n<tr><td></td>\n");
    ArrayList<String> colLabels = columnLabels();
    for (String cLabel : colLabels) {
      result.append("<td>").append(cLabel).append("</td>");
    }
    result.append("</tr>\n");
    int colNum = sparseMatrix.columnKeyList().size();
    ArrayList<String> rLabels = rowLabels();
    int row = 0;
    for (String label : rLabels) {
      result.append("<tr><td>").append(label).append("</td>");
      for (int col = 0; col < colNum; col++)
        if (at(row, col))
          result.append("<td BGCOLOR=\"lightgreen\">M</td>");
        else
          result.append("<td></td>");
      result.append("</tr>\n");
      row++;
    }
    result.append("</table>");
    return result.toString();
  }

  public String toHtml(Archipelago arch) {
    int mat[] = new int[rowNum()];
    for (Island isl : arch.iterator()) {
      for (Coordinates c : isl) {
        mat[c.row] = c.column;
      }
    }
    StringBuilder result = new StringBuilder("<table>\n<tr><td></td>\n");
    ArrayList<String> colLabels = columnLabels();
    for (String cLabel : colLabels) {
      result.append("<td>").append(cLabel).append("</td>");
    }
    result.append("</tr>\n");
    ArrayList<String> rLabels = rowLabels();
    int row = 0;
    for (String label : rLabels) {
      result.append("<tr><td>").append(label).append("</td>");
      if (mat[row] > 0) {
        result.append("<td colspan=\"").append(mat[row]).append("\"></td>").append("<td BGCOLOR=\"lightgreen\">M</td>");
      }
      result.append("</tr>\n");
      row++;
    }
    result.append("</table>");
    return result.toString();
  }

  public ArrayList<String> rowLabels() {
    ArrayList<String> labels = new ArrayList<String>();
    for (VariantGraphVertex vgv : sparseMatrix.rowKeyList()) {
      String token = vgv.toString();
      int pos = token.lastIndexOf(":");
      if (pos > -1) {
        labels.add(token.substring(pos + 2, token.length() - 2));
      }
    }
    return labels;
  }

  public List<VariantGraphVertex> rowVertices() {
    List<VariantGraphVertex> vertices = Lists.newArrayList();
    for (VariantGraphVertex vgv : sparseMatrix.rowKeyList()) {
      if (vgv.toString().contains(":")) {
        vertices.add(vgv);
      }
    }
    return vertices;
  }

  public ArrayList<String> columnLabels() {
    ArrayList<String> labels = new ArrayList<String>();
    for (Token t : sparseMatrix.columnKeyList()) {
      String token = t.toString();
      int pos = token.lastIndexOf(":");
      if (pos > -1) {
        labels.add(token.substring(pos + 2, token.length() - 1));
      }
    }
    return labels;
  }

  public List<Token> columnTokens() {
    List<Token> tokens = Lists.newArrayList();
    for (Token t : sparseMatrix.columnKeyList()) {
      if (t.toString().contains(":")) {
        tokens.add(t);
      }
    }
    return tokens;
  }

  public ArrayList<Coordinates> allMatches() {
    ArrayList<Coordinates> pairs = new ArrayList<Coordinates>();
    int rows = rowNum();
    int cols = colNum();
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        if (at(i, j)) pairs.add(new Coordinates(i, j));
      }
    }
    return pairs;
  }

  public int rowNum() {
    return rowLabels().size();
  }

  public int colNum() {
    return columnLabels().size();
  }

  public ArrayList<Island> getIslands() {
    ArrayList<Island> islands = new ArrayList<Island>();
    ArrayList<Coordinates> allTrue = allMatches();
    for (Coordinates c : allTrue) {
      //			System.out.println("next coordinate: "+c);
      boolean found = false;
      while (!found) {
        for (Island alc : islands) {
          //					System.out.println("inspect island");
          if (alc.neighbour(c)) {
            alc.add(c);
            found = true;
          }
          if (found) break;
        }
        if (!found) {
          //					System.out.println("new island");
          Island island = new Island();
          island.add(c);
          islands.add(island);
        }
        found = true;
      }
    }
    return islands;
  }

  public static class Coordinates implements Comparable<Coordinates> {
    int row;
    int column;

    public Coordinates(int row, int column) {
      this.column = column;
      this.row = row;
    }

    Coordinates(Coordinates other) {
      this(other.row, other.column);
    }

    public int getRow() {
      return row;
    }

    public int getColumn() {
      return column;
    }

    public boolean sameColumn(Coordinates c) {
      return c.column == column;
    }

    public boolean sameRow(Coordinates c) {
      return c.row == row;
    }

    public boolean bordersOn(Coordinates c) {
      return (Math.abs(this.row - c.getRow()) == 1) && (Math.abs(this.column - c.getColumn()) == 1);
    }

    @Override
    public boolean equals(Object o) {
      if (o != null & o instanceof Coordinates) {
        final Coordinates c = (Coordinates) o;
        return (this.row == c.getRow() && this.column == c.getColumn());
      }
      return super.equals(o);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(row, column);
    }

    @Override
    public int compareTo(Coordinates o) {
      final int result = column - o.column;
      return (result == 0 ? row - o.row : result);
    }

    @Override
    public String toString() {
      return "(" + row + "," + column + ")";
    }
  }

  /**
   * An DirectedIsland is a collections of Coordinates all on the same
   * diagonal. The direction of this diagonal can be -1, 0, or 1.
   * The zero is for a DirectedIsland of only one Coordinate.
   * Directions 1 and -1 examples
   * Coordinates (0,0) (1,1) have Direction 1
   * Coordinates (1,1) (2,1) have Direction -1
   * I.e. if the row-cordinate gets larger and the col-coordinate also, the
   * direction is 1 (positive) else it is -1 (negative)
   */
  public static class Island implements Iterable<Coordinates> {

    private int direction = 0;
    private final List<Coordinates> islandCoordinates = Lists.newArrayList();

    public Island() {}

    public Island(Island other) {
      for (Coordinates c : other.islandCoordinates) {
        add(new Coordinates(c));
      }
    }

    public Island(Coordinates first, Coordinates last) {
      add(first);
      Coordinates newCoordinate = first;
      while (!newCoordinate.equals(last)) {
        newCoordinate = new Coordinates(newCoordinate.getRow() + 1, newCoordinate.getColumn() + 1);
        //        LOG.info("{}", newCoordinate);
        add(newCoordinate);
      }
    }

    public boolean add(Coordinates coordinates) {
      boolean result = false;
      if (islandCoordinates.isEmpty()) {
        result = islandCoordinates.add(coordinates);
      } else if (!contains(coordinates) && neighbour(coordinates)) {
        if (direction == 0) {
          Coordinates existing = islandCoordinates.get(0);
          direction = (existing.row - coordinates.row) / (existing.column - coordinates.column);
          result = islandCoordinates.add(coordinates);
        } else {
          Coordinates existing = islandCoordinates.get(0);
          if (existing.column != coordinates.column) {
            int new_direction = (existing.row - coordinates.row) / (existing.column - coordinates.column);
            if (new_direction == direction) result = islandCoordinates.add(coordinates);
          }
        }
      }
      return result;
    }

    public int direction() {
      return direction;
    }

    public Island removePoints(Island di) {
      Island result = new Island(this);
      for (Coordinates c : di) {
        result.removeSameColOrRow(c);
      }
      return result;
    }

    public Coordinates getCoorOnRow(int row) {
      for (Coordinates coor : islandCoordinates) {
        if (coor.getRow() == row) return coor;
      }
      return null;
    }

    public Coordinates getCoorOnCol(int col) {
      for (Coordinates coor : islandCoordinates) {
        if (coor.getColumn() == col) return coor;
      }
      return null;
    }

    public void merge(Island di) {
      for (Coordinates c : di) {
        add(c);
      }
    }

    /**
     * Two islands are competitors if there is a horizontal or
     * vertical line which goes through both islands
     */
    public boolean isCompetitor(Island isl) {
      for (Coordinates c : isl) {
        for (Coordinates d : islandCoordinates) {
          if (c.sameColumn(d) || c.sameRow(d)) return true;
        }
      }
      return false;
    }

    public boolean contains(Coordinates c) {
      return islandCoordinates.contains(c);
    }

    public boolean neighbour(Coordinates c) {
      if (contains(c)) return false;
      for (Coordinates islC : islandCoordinates) {
        if (c.bordersOn(islC)) {
          return true;
        }
      }
      return false;
    }

    public Coordinates getLeftEnd() {
      Coordinates coor = islandCoordinates.get(0);
      for (Coordinates c : islandCoordinates) {
        if (c.column < coor.column) coor = c;
      }
      return coor;
    }

    public Coordinates getRightEnd() {
      Coordinates coor = islandCoordinates.get(0);
      for (Coordinates c : islandCoordinates) {
        if (c.column > coor.column) coor = c;
      }
      return coor;
    }

    @Override
    public Iterator<Coordinates> iterator() {
      return Collections.unmodifiableList(islandCoordinates).iterator();
    }

    protected boolean removeSameColOrRow(Coordinates c) {
      ArrayList<Coordinates> remove = new ArrayList<Coordinates>();
      for (Coordinates coor : islandCoordinates) {
        if (coor.sameColumn(c) || coor.sameRow(c)) {
          remove.add(coor);
        }
      }
      if (remove.isEmpty()) return false;
      for (Coordinates coor : remove) {
        islandCoordinates.remove(coor);
      }
      return true;
    }

    public boolean overlap(Island isl) {
      for (Coordinates c : isl) {
        if (contains(c) || neighbour(c)) return true;
      }
      return false;
    }

    public int size() {
      return islandCoordinates.size();
    }

    public void clear() {
      islandCoordinates.clear();
    }

    public int value() {
      final int size = size();
      return (size < 2 ? size : direction + size * size);
    }

    @Override
    public int hashCode() {
      return Objects.hashCode(islandCoordinates);
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null) return false;

      if (!obj.getClass().equals(Island.class)) return false;

      Island isl = (Island) obj;
      if (isl.size() != size()) return false;

      boolean result = true;
      for (Coordinates c : isl) {
        result &= this.contains(c);
      }
      return result;
    }

    @Override
    public String toString() {
      return MessageFormat.format("Island ({0}-{1}) size: {2}", islandCoordinates.get(0), islandCoordinates.get(islandCoordinates.size() - 1), size());
      //      return Iterables.toString(islandCoordinates);
    }
  }
}
