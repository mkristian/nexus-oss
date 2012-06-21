/**
 * Sonatype Nexus (TM) Open Source Version
 * Copyright (c) 2007-2012 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://links.sonatype.com/products/nexus/oss/attributions.
 *
 * This program and the accompanying materials are made available under the terms of the Eclipse Public License Version 1.0,
 * which accompanies this distribution and is available at http://www.eclipse.org/legal/epl-v10.html.
 *
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc. "Sonatype" and "Sonatype Nexus" are trademarks
 * of Sonatype, Inc. Apache Maven is a trademark of the Apache Software Foundation. M2eclipse is a trademark of the
 * Eclipse Foundation. All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.plugin.deploy;

import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * Deploys the (previously) locally staged artifacts.
 * 
 * @author cstamas
 * @since 2.1
 * goal deploy-staged
 */
public class DeployStagedMojo
    extends AbstractDeployMojo
{
    @Override
    public void execute()
        throws MojoExecutionException, MojoFailureException
    {
        if ( isThisLastProject() )
        {
            failIfOffline();

            try
            {
                uploadStagedArtifacts();
            }
            catch ( ArtifactDeploymentException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
        }
    }
}
