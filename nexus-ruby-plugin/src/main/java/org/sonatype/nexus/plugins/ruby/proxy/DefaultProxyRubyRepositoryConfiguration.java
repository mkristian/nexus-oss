package org.sonatype.nexus.plugins.ruby.proxy;

import org.sonatype.nexus.proxy.repository.AbstractProxyRepositoryConfiguration;

import org.codehaus.plexus.util.xml.Xpp3Dom;

public class DefaultProxyRubyRepositoryConfiguration
    extends AbstractProxyRepositoryConfiguration
{
  public static final String ARTIFACT_MAX_AGE = "artifactMaxAge";

  public static final String METADATA_MAX_AGE = "metadataMaxAge";

  public DefaultProxyRubyRepositoryConfiguration(Xpp3Dom configuration) {
    super(configuration);
  }

  public int getArtifactMaxAge() {
    return Integer.parseInt(getNodeValue(getRootNode(), ARTIFACT_MAX_AGE, "-1"));
  }

  public void setArtifactMaxAge(int age) {
    setNodeValue(getRootNode(), ARTIFACT_MAX_AGE, String.valueOf(age));
  }

  public int getMetadataMaxAge() {
    return Integer.parseInt(getNodeValue(getRootNode(), METADATA_MAX_AGE, "1440"));
  }

  public void setMetadataMaxAge(int age) {
    setNodeValue(getRootNode(), METADATA_MAX_AGE, String.valueOf(age));
  }
}
