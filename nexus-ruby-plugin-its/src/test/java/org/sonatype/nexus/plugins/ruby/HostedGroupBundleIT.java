package org.sonatype.nexus.plugins.ruby;


public class HostedGroupBundleIT extends BundleITBase
{
    public HostedGroupBundleIT()
    {
        super( "gemshostgroup" );
    }

    @Override
    protected void testAfterBundleComplete()
    {
        assertHostedFiles();
    }

}