package org.sonatype.nexus.ruby;

public enum FileType {

    GEM( "application/x-rubygems" ),
    GEMSPEC( "application/x-ruby-marshal" ),
    DEPENDENCY( "application/json" ),
    MAVEN_METADATA( "application/xml" ),
    MAVEN_METADATA_SNAPSHOT( "application/xml" ),
    POM( "application/xml" ),
    SPECS_INDEX( "application/x-ruby-marshal" ),
    DIRECTORY( "application/octet" ), 
    BUNDLER_API( "application/x-ruby-marshal" ), 
    API_V1( "application/x-rubygems" ), 
    GEM_ARTIFACT( "application/x-rubygems" ), 
    NOT_FOUND( null ), 
    SHA1( "text/plain" );

    private FileType(String mime){
        this.mime = mime;
    }

    private final String mime;

    public String mime()
    {
        return this.mime;
    }
}