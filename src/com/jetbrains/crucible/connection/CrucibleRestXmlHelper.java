package com.jetbrains.crucible.connection;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * User : ktisha
 */
public final class CrucibleRestXmlHelper {

  private CrucibleRestXmlHelper() {
  }

  @NotNull
  public static String getChildText(Element node, String childName) {
    final Element child = node.getChild(childName);
    if (child != null) {
      return child.getText();
    }
    return "";
  }

  @Nullable
  public static String getChildTextOrNull(Element node, String childName) {
    final Element child = node.getChild(childName);
    if (child != null) {
      return child.getText();
    }
    return null;
  }


  public static CrucibleVersionInfo parseVersionNode(Element element) {
    return new CrucibleVersionInfo(getChildText(element, "releaseNumber"), getChildText(element, "buildDate"));
  }
}
