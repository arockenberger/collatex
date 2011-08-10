package eu.interedition.text.repository;

import eu.interedition.text.QName;
import eu.interedition.text.util.SimpleXMLParserConfiguration;
import eu.interedition.text.xml.XMLParserModule;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.util.List;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class XMLParserConfigurationBean extends SimpleXMLParserConfiguration {

  public Set<QName> getIncluded() {
    return included;
  }

  @JsonDeserialize(contentAs = QNameBean.class)
  public void setIncluded(Set<QName> included) {
    this.included = included;
  }

  public Set<QName> getExcluded() {
    return excluded;
  }

  @JsonDeserialize(contentAs = QNameBean.class)
  public void setExcluded(Set<QName> excluded) {
    this.excluded = excluded;
  }

  public Set<QName> getLineElements() {
    return lineElements;
  }

  @JsonDeserialize(contentAs = QNameBean.class)
  public void setLineElements(Set<QName> lineElements) {
    this.lineElements = lineElements;
  }

  public Set<QName> getContainerElements() {
    return containerElements;
  }

  @JsonDeserialize(contentAs = QNameBean.class)
  public void setContainerElements(Set<QName> containerElements) {
    this.containerElements = containerElements;
  }

  public Set<QName> getNotableElements() {
    return notableElements;
  }

  @JsonDeserialize(contentAs = QNameBean.class)
  public void setNotableElements(Set<QName> notableElements) {
    this.notableElements = notableElements;
  }

  @JsonIgnore
  @Override
  public List<XMLParserModule> getModules() {
    return super.getModules();
  }

  @JsonIgnore
  @Override
  public int getTextBufferSize() {
    return super.getTextBufferSize();
  }

  @JsonIgnore
  @Override
  public void setTextBufferSize(int textBufferSize) {
    super.setTextBufferSize(textBufferSize);
  }
}