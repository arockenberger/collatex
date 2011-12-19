/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.interedition.collatex.input;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;

import com.google.common.collect.Lists;

import eu.interedition.collatex.TokenNormalizer;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.Token;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class WitnessTeiBuilder extends WitnessStreamBuilder {
  DocumentBuilder builder = null;
  XPathFactory factory;
  private final TokenNormalizer tokenNormalizer;

  public WitnessTeiBuilder(TokenNormalizer tokenNormalizer1) {
    super(tokenNormalizer1);
    this.tokenNormalizer = tokenNormalizer1;
    DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
    try {
      builder = domFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      throw new InternalError("Internal error. Can't create document builder!");
    }
    factory = XPathFactory.newInstance();
  }

  @Override
  public Witness build(InputStream inputStream) throws SAXException, IOException {
    SimpleWitness witness = new SimpleWitness(UUID.randomUUID().toString());
    Document doc = getXmlDocument(inputStream);

    XPath xpath = factory.newXPath();
    Object result;
    result = evaluate(xpath, doc, "/TEI/text/body//w");
    NodeList nodes = (NodeList) result;
    List<Token> tokenList = Lists.newArrayList();
    if (nodes == null || nodes.getLength() == 0) {//get text from 'p' elements
      result = evaluate(xpath, doc, "/TEI/text/body//p");
      nodes = (NodeList) result;
      StringBuilder builder1 = new StringBuilder();
      for (int i = 0; i < nodes.getLength(); i++) {
        String value = nodes.item(i).getTextContent();
        builder1.append(value);
      }
      Witness w = build(builder1.toString());
      tokenList = w.getTokens();
    } else { // get text from prepared 'w' elements 
      for (int i = 0; i < nodes.getLength(); i++) {
        String value = nodes.item(i).getTextContent();
        tokenList.add(new SimpleToken(witness, tokenList.size(), value, tokenNormalizer.apply(value)));
      }
    }
    witness.setTokens(tokenList);
    return witness;
  }

  private Object evaluate(XPath xpath, Document doc, String exprression) {
    XPathExpression expr;
    try {
      expr = xpath.compile(exprression);
    } catch (XPathExpressionException e) {
      throw new InternalError("Incorrect xpath expression!");
    }

    Object result;
    try {
      result = expr.evaluate(doc, XPathConstants.NODESET);
    } catch (XPathExpressionException e) {
      throw new InternalError("Incorrect xpath expression!");
    }
    return result;
  }

  private Document getXmlDocument(InputStream xmlInputStream) throws SAXException, IOException {
    Document doc = null;
    //    domFactory.setNamespaceAware(true);
    doc = builder.parse(xmlInputStream);
    return doc;
  }

}