package org.sonatype.nexus.ruby;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.zip.GZIPInputStream;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.sonatype.nexus.ruby.cuba.DefaultRubygemsFileSystem;
import org.sonatype.nexus.ruby.layout.CachingStoreFacade;
import org.sonatype.nexus.ruby.layout.FileSystemStoreFacade;
import org.sonatype.nexus.ruby.layout.GETLayout;
import org.sonatype.nexus.ruby.layout.StoreFacade;

@RunWith(Parameterized.class)
public class GETLayoutTest
    extends TestCase
{
    private static File proxyBase() throws IOException
    {
        File base = new File( "target/proxy" );
        FileUtils.deleteDirectory( base );
        return base;
    }
    
    @Parameters
    public static Collection<Object[]> stores() throws IOException{
        return Arrays.asList( new Object[][]{ 
            { new FileSystemStoreFacade( new File( "src/test/repo" ) ) },
            { new CachingStoreFacade( proxyBase(),  new File( "src/test/repo" ).toURI().toURL() )
              {

                  protected URL toUrl( RubygemsFile file ) throws MalformedURLException
                  {
                      return new URL( baseurl + file.storagePath() );
                  }
              } 
            }
        } );
    }

    private final DefaultRubygemsFileSystem bootstrap;

    public GETLayoutTest( StoreFacade store )
    {
        bootstrap = new DefaultRubygemsFileSystem( new GETLayout( new DefaultRubygemsGateway( new TestScriptingContainer() ), 
                                                         store ) );
     
    }
    
    @Test
    public void testSpecsZippedIndex()
        throws Exception
    {        
        String[] pathes = { "/specs.4.8.gz",
                            "/prerelease_specs.4.8.gz",
                            "/latest_specs.4.8.gz" }; 
        assertFiletypeWithPayload( pathes, FileType.SPECS_INDEX, InputStream.class );
    }
    
    @Test
    public void testSpecsUnzippedIndex()
        throws Exception
    {        
        String[] pathes = { "/specs.4.8",
                            "/prerelease_specs.4.8",
                            "/latest_specs.4.8" }; 
        assertFiletypeWithPayload( pathes, FileType.SPECS_INDEX, GZIPInputStream.class );
    }

    @Test
    public void testSha1()
        throws Exception
    {        
        String[] pathes = { "/maven/releases/rubygems/zip/maven-metadata.xml.sha1",
                            "/maven/releases/rubygems/zip/2.0.2/zip-2.0.2.gem.sha1",
                            "/maven/releases/rubygems/zip/2.0.2/zip-2.0.2.pom.sha1",
                            "/maven/prereleases/rubygems/pre/maven-metadata.xml.sha1",
                            "/maven/prereleases/rubygems/pre/0.1.0.beta-SNAPSHOT/maven-metadata.xml.sha1",
                            "/maven/prereleases/rubygems/pre/0.1.0.beta-SNAPSHOT/pre-0.1.0.beta-123213123.gem.sha1",
                            "/maven/prereleases/rubygems/pre/0.1.0.beta-SNAPSHOT/pre-0.1.0.beta-123213123.pom.sha1",
                            "/maven/releases/rubygems/pre/maven-metadata.xml.sha1",
                            "/maven/releases/rubygems/pre/0.1.0.beta/pre-0.1.0.beta.gem.sha1",
                            "/maven/releases/rubygems/pre/0.1.0.beta/pre-0.1.0.beta.pom.sha1" }; 
        String[] shas = { "3498b89783698a7590890fc4159e84ea80ab2cbe", "6fabc32da123f7013b2db804273df428a50bc6a4", 
                          "a289cc8017a52822abf270722f7b003d039baef9", "24f33a26c5edd889ce3dcfd9e038af900ba10564", 
                          "d1ef40d6775396c6bec855037a1ff6dcb34afdbd", "b7311d2f46398dbe40fd9643f3d4e5d473574335",
                          "fb3e466464613ee33b5e2366d0eac789df6af583", "81bed0dbaef593e31578f5814304f991f55ff7d4",
                          "b7311d2f46398dbe40fd9643f3d4e5d473574335", 
                          // TODO this one is wrong since it should be different from the snapshot pom !!!
                          "fb3e466464613ee33b5e2366d0eac789df6af583" };

        assertFiletypeWithPayload( pathes, FileType.SHA1, shas );
    }

    @Test
    public void testGemArtifact()
        throws Exception
    {        
        String[] pathes = { "/maven/releases/rubygems/zip/2.0.2/zip-2.0.2.gem", 
                            "/maven/releases/rubygems/pre/0.1.0.beta/pre-0.1.0.beta.gem", 
                            "/maven/prereleases/rubygems/pre/0.1.0.beta-SNAPSHOT/pre-0.1.0.beta-123213123.gem" };
        assertFiletypeWithPayload( pathes, FileType.GEM_ARTIFACT, InputStream.class );
        pathes = new String[] { "/maven/releases/rubygems/hufflepuf/0.1.0/hufflepuf-0.1.0.gem",
                                "/maven/releases/rubygems/hufflepuf/0.1.0/hufflepuf-0.2.0.gem" };
        RubygemsFile[] result = assertFiletypeWithPayload( pathes, FileType.GEM_ARTIFACT, InputStream.class );
        for( RubygemsFile file: result )
        {
            GemArtifactFile a = file.isGemArtifactFile();
            assertThat( a.gem( null ).filename(), is( "hufflepuf-" + a.version() + "-universal-java-1.5" ) );
        }
        
    }
    
    @Test
    public void testPom()
        throws Exception
    {        
        String[] pathes = { "/maven/releases/rubygems/zip/2.0.2/zip-2.0.2.pom",
                            "/maven/releases/rubygems/pre/0.1.0.beta/jbundler-0.1.0.beta.pom",
                            "/maven/prereleases/rubygems/pre/0.1.0.beta-SNAPSHOT/jbundler-0.1.0.beta-123213123.pom" };
        assertFiletypeWithPayload( pathes, FileType.POM, ByteArrayInputStream.class );
    }
    
    @Test
    public void testMavenMetadata()
        throws Exception
    {        
        String[] pathes = { "/maven/releases/rubygems/zip/maven-metadata.xml",
                            "/maven/releases/rubygems/pre/maven-metadata.xml" ,
                            "/maven/prereleases/rubygems/pre/maven-metadata.xml" };
        String[] xmls = {
                  "<metadata>\n"
                + "  <groupId>rubygems</groupId>\n"
                + "  <artifactId>zip</artifactId>\n"
                + "  <versioning>\n"
                + "    <versions>\n"
                + "      <version>2.0.2</version>\n"
                + "    </versions>\n"
                + "    <lastUpdated>2014</lastUpdated>\n"
                + "  </versioning>\n"
                + "</metadata>\n",

                  "<metadata>\n"
                + "  <groupId>rubygems</groupId>\n"
                + "  <artifactId>pre</artifactId>\n"
                + "  <versioning>\n"
                + "    <versions>\n"
                + "    </versions>\n"
                + "    <lastUpdated>2014</lastUpdated>\n"
                + "  </versioning>\n"
                + "</metadata>\n",
                
                  "<metadata>\n"
                + "  <groupId>rubygems</groupId>\n"
                + "  <artifactId>pre</artifactId>\n"
                + "  <versioning>\n"
                + "    <versions>\n"
                + "      <version>0.1.0.beta-SNAPSHOT</version>\n"
                + "    </versions>\n"
                + "    <lastUpdated>2014</lastUpdated>\n"
                + "  </versioning>\n"
                + "</metadata>\n"
        };
        assertFiletypeWithPayload( pathes, FileType.MAVEN_METADATA, xmls );
    }

    @Test
    public void testMavenMetadataSnapshot()
        throws Exception
    {        
        String[] pathes = { "/maven/prereleases/rubygems/pre/0.1.0-SNAPSHOT/maven-metadata.xml" };
        String[] xmls = {
               "<metadata>\n"
             + "  <groupId>rubygems</groupId>\n"
             + "  <artifactId>pre</artifactId>\n"
             + "  <versioning>\n"
             + "    <versions>\n"
             + "      <snapshot>\n"
             + "        <timestamp>2014</timestamp>\n"
             + "        <buildNumber>1</buildNumber>\n"
             + "      </snapshot>\n"
             + "      <lastUpdated>2014</lastUpdated>\n"
             + "      <snapshotVersions>\n"
             + "        <snapshotVersion>\n"
             + "          <extension>gem</extension>\n"
             + "          <value>0.1.0-2014-1</value>\n"
             + "          <updated>2014</updated>\n"
             + "        </snapshotVersion>\n"
             + "        <snapshotVersion>\n"
             + "          <extension>pom</extension>\n"
             + "          <value>0.1.0-2014-1</value>\n"
             + "          <updated>2014</updated>\n"
             + "        </snapshotVersion>\n"
             + "      </snapshotVersions>\n"
             + "    </versions>\n"
             + "  </versioning>\n"
             + "</metadata>\n"
        };
        assertFiletypeWithPayload( pathes, FileType.MAVEN_METADATA_SNAPSHOT, xmls );
    }

    @Test
    public void testBundlerApi()
        throws Exception
    {        
        String[] pathes = { "/api/v1/dependencies?gems=zip,pre" };
        assertFiletypeWithPayload( pathes, FileType.BUNDLER_API, org.sonatype.nexus.ruby.ByteArrayInputStream.class );
    }

    
    @Test
    public void testApiV1Gems()
        throws Exception
    {        
        String[] pathes = { "/api/v1/gems" };
        assertNull( pathes );
    }

    @Test
    public void testApiV1ApiKey()
        throws Exception
    {        
        String[] pathes = { "/api/v1/api_key" };
        assertFiletypeWithNullPayload( pathes, FileType.API_V1 );
    }


    @Test
    public void testDependency()
        throws Exception
    {        
        String[] pathes = { "/api/v1/dependencies?gems=zip", "/api/v1/dependencies/pre.json.rz", "/api/v1/dependencies/z/zip.json.rz" };
        assertFiletypeWithPayload( pathes, FileType.DEPENDENCY, InputStream.class );
    }

    @Test
    public void testGemspec()
        throws Exception
    {        
        String[] pathes = { "/quick/Marshal.4.8/zip-2.0.2.gemspec.rz", "/quick/Marshal.4.8/z/zip-2.0.2.gemspec.rz" };
        assertFiletypeWithPayload( pathes, FileType.GEMSPEC, InputStream.class );
    }
    
    @Test
    public void testGem()
        throws Exception
    {        
        String[] pathes = { "/gems/pre-0.1.0.beta.gem", "/gems/p/pre-0.1.0.beta.gem" };
        assertFiletypeWithPayload( pathes, FileType.GEM, InputStream.class );
    }
   
    @Test
    public void testDirectory()
        throws Exception
    {        
        String[] pathes = { "/",  "/api", "/api/", "/api/v1", "/api/v1/", 
                            "/api/v1/dependencies", "/gems/", "/gems",
                            "/maven/releases/rubygems/jbundler",
                            "/maven/releases/rubygems/jbundler/1.2.3", 
                            "/maven/prereleases/rubygems/jbundler",
                            "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT", 
                          };
        assertFiletypeWithNullPayload( pathes, FileType.DIRECTORY );
    }
    
    @Test
    public void testNotFound()
        throws Exception
    {        
        String[] pathes = { "/asa", "/asa/", "/api/a", "/api/v1ds","/api/v1/ds", 
                            "/api/v1/dependencies/jbundler.jsaon.rz", "/api/v1/dependencies/b/bundler.json.rzd",
                            "/api/v1/dependencies/basd/bundler.json.rz",
                            "/quick/Marshal.4.8/jbundler.jssaon.rz", "/quick/Marshal.4.8/b/bundler.gemspec.rzd",
                            "/quick/Marshal.4.8/basd/bundler.gemspec.rz", 
                            "/gems/jbundler.jssaonrz", "/gems/b/bundler.gemsa",
                            "/gems/basd/bundler.gem", 
                            "/maven/releasesss/rubygemsss/a", 
                            "/maven/releases/rubygemsss/jbundler", 
                            "/maven/releases/rubygems/jbundler/1.2.3/jbundler-1.2.3.gema",
                            "/maven/releases/rubygems/jbundler/1.2.3/jbundler-1.2.3.pom2", 
                            "/maven/releases/rubygems/jbundler/1.2.3/jbundler-1.2.3.gem.sha", 
                            "/maven/releases/rubygems/jbundler/1.2.3/jbundler-1.2.3.pom.msa", 
                            "/maven/prereleases/rubygemsss/jbundler", 
                            "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT/maven-metadata.xml.sha1a",
                            "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT/jbundler-1.2.3-123213123.gem.sh1",
                            "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT/jbundler-1.2.3-123213123.pom.sha", 
                            "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT/jbundler-1.2.3-123213123.gema", 
                            "/maven/prereleases/rubygems/jbundler/1.2.3-SNAPSHOT/jbundler-1.2.3-123213123.pom2", 
                          };
        assertFiletypeWithNullPayload( pathes, FileType.NOT_FOUND );
    }

    protected void assertFiletype( String[] pathes, FileType type )
    {
        for( String path : pathes )
        {
            RubygemsFile file = bootstrap.get( path );
            assertThat( path, file.type(), equalTo( type ) );
            assertThat( path, file.get(), notNullValue() );
            assertThat( path, file.hasException(), is( false ) );
        }
    }

    protected void assertFiletypeWithPayload( String[] pathes, FileType type, String[] payloads )
    {
        int index = 0;
        for( String path : pathes )
        {
            RubygemsFile file = bootstrap.get( path );
            assertThat( path, file.type(), equalTo( type ) );
            assertThat( path, file.get(), is( instanceOf( ByteArrayInputStream.class ) ) );
            assertThat( path, file.hasException(), is( false ) );
            assertThat( path, readPayload( file ).replaceAll( "[0-9]{8}\\.?[0-9]{6}", "2014" ), equalTo( payloads[ index ++ ] ) );
        }
    }

    protected String readPayload( RubygemsFile file )
    {
        ByteArrayInputStream b = (ByteArrayInputStream) file.get();
        byte[] bb = new byte[ b.available() ];
        try
        {
            b.read( bb );
        }
        catch (IOException e)
        {
            new RuntimeException( e );
        }
        return new String( bb );
    }
    
    protected RubygemsFile[] assertFiletypeWithPayload( String[] pathes, FileType type, Class<?> payload )
    {
        RubygemsFile[] result = new RubygemsFile[ pathes.length ];
        int index = 0;
        for( String path : pathes )
        {
            RubygemsFile file = bootstrap.get( path );
            assertThat( path, file.type(), equalTo( type ) );
            assertThat( path, file.get(), is( instanceOf( payload ) ) );
            assertThat( path, file.hasException(), is( false ) );
            result[ index ++ ] = file;
        }
        return result;
    }

    protected void assertFiletypeWithNullPayload( String[] pathes, FileType type )
    {
        for( String path : pathes )
        {
            RubygemsFile file = bootstrap.get( path );
            assertThat( path, file.type(), equalTo( type ) );
            assertThat( path, file.get(), nullValue() );
            assertThat( path, file.hasException(), is( false ) );
        }
    }
    
    protected void assertIOException( String[] pathes, FileType type )
    {
        for( String path : pathes )
        {
            RubygemsFile file = bootstrap.get( path );
            assertThat( path, file.type(), equalTo( type ) );
            assertThat( path, file.getException(), is( instanceOf( IOException.class ) ) );
        }
    }

    protected void assertNull( String[] pathes )
    {
        for( String path : pathes )
        {
            assertThat( path, bootstrap.get( path ), nullValue() );
        }
    }
}
