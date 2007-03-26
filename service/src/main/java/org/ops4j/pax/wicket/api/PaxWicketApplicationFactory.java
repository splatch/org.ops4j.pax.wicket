/*
 * Copyright 2006 Niclas Hedhman.
 * Copyright 2006 Edward F. Yakop
 *
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.wicket.api;

import java.util.Dictionary;
import java.util.Properties;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.pax.wicket.internal.DelegatingClassResolver;
import org.ops4j.pax.wicket.internal.PaxAuthenticatedWicketApplication;
import org.ops4j.pax.wicket.internal.PaxWicketApplication;
import org.ops4j.pax.wicket.internal.PaxWicketPageFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import wicket.Page;
import wicket.settings.Settings;
import wicket.markup.html.WebPage;
import wicket.protocol.http.IWebApplicationFactory;
import wicket.protocol.http.WebApplication;
import wicket.protocol.http.WicketServlet;

public final class PaxWicketApplicationFactory
    implements IWebApplicationFactory, ManagedService
{

    private BundleContext m_bundleContext;
    private Class<? extends Page> m_homepageClass;
    private Properties m_properties;

    private PaxWicketPageFactory m_pageFactory;
    private DelegatingClassResolver m_delegatingClassResolver;

    private ServiceRegistration m_registration;

    private PaxWicketAuthenticator m_authenticator;
    private Class<? extends WebPage> m_signinPage;

    /**
     * Construct an instance of {@code PaxWicketApplicationFactory} with the specified arguments.
     *
     * @param bundleContext The bundle context. This argument must not be {@code null}.
     * @param homepageClass The homepage class. This argument must not be {@code null}.
     * @param mountPoint The mount point. This argument must not be be {@code null}.
     * @param applicationName The application name. This argument must not be {@code null}.
     *
     * @throws IllegalArgumentException Thrown if one or some or all arguments are {@code null}.
     * @since 1.0.0
     */
    public PaxWicketApplicationFactory( BundleContext bundleContext, Class<? extends Page> homepageClass,
        String mountPoint, String applicationName )
        throws IllegalArgumentException
    {
        NullArgumentException.validateNotNull( bundleContext, "bundleContext" );
        NullArgumentException.validateNotNull( homepageClass, "homepageClass" );
        NullArgumentException.validateNotNull( mountPoint, "mountPoint" );
        NullArgumentException.validateNotEmpty( applicationName, "applicationName" );

        m_properties = new Properties();
        m_homepageClass = homepageClass;
        m_bundleContext = bundleContext;

        setMountPoint( mountPoint );
        setDeploymentMode( false );
        setApplicationName( applicationName );

        String homepageClassName = homepageClass.getName();
        m_properties.setProperty( ContentSource.HOMEPAGE_CLASSNAME, homepageClassName );
    }

    /**
     * Sets the authenticator of this pax application factory.
     * <p>
     * Note: Value changed will only affect wicket application created after this method invocation.
     * </p>
     *
     * @param authenticator The authenticator.
     * @param signInPage The sign in page.
     *
     * @throws IllegalArgumentException Thrown if one of the arguments are {@code null}.
     * @see #register()
     * @since 1.0.0
     */
    public final void setAuthenticator( PaxWicketAuthenticator authenticator, Class<? extends WebPage> signInPage )
        throws IllegalArgumentException
    {
        if ( (authenticator != null && signInPage == null) || (authenticator == null && signInPage != null) )
        {
            throw new IllegalArgumentException( "Both [authenticator] and [signInPage] argument must not be [null]." );
        }

        m_authenticator = authenticator;
        m_signinPage = signInPage;
    }

    /**
     * Clear resources used by this {@code PaxWicketApplicationFactory} instance.
     * <p>
     * Note: dispose does not unregister this {@code PaxWicketApplicationFactory} instance from the OSGi container.
     * </p>
     *
     * @since 1.0.0
     */
    public final void dispose()
    {
        synchronized ( this )
        {
            m_pageFactory.dispose();
            m_delegatingClassResolver.dispose();
        }
    }

    /**
     * Returns the mount point that the wicket application will be accessible. This method must not return {@code null}
     * string.
     *
     * @return The mount point that the wicket application will be accessible.
     *
     * @since 1.0.0
     */
    public final String getMountPoint()
    {
        synchronized ( this )
        {
            return m_properties.getProperty( ContentSource.MOUNTPOINT );
        }
    }

    /**
     * Returns {@code true} if application created by this {@code PaxWicketApplicationFactory} is in deployment mode,
     * {@code false} if application is in debug mode.
     *
     * @return A {@code boolean} indicator whether application created by this {@code PaxWicketApplicationFactory}
     *         instance is in deployment mode.
     *
     * @since 1.0.0
     */
    public final boolean isDeploymentMode()
    {
        String deploymentMode;
        synchronized ( this )
        {
            deploymentMode = m_properties.getProperty( ContentSource.DEPLOYMENT_MODE );
        }
        return Boolean.parseBoolean( deploymentMode );
    }

    /**
     * Sets the deployment mode of application created by this {@code PaxWicketApplicationFactory} instance. Sets to
     * {@code true} if the application should be in {@code deployment mode}, {@code false} if it should be in debug
     * mode.
     * <p>
     * Note: Value changed will only affect wicket application created after this method invocation.
     * </p>
     *
     * @param deploymentMode The deployment mode.
     *
     * @see #register()
     * @since 1.0.0
     */
    public final void setDeploymentMode( boolean deploymentMode )
    {
        String strDepMode = String.valueOf( deploymentMode );
        synchronized ( this )
        {
            m_properties.put( ContentSource.DEPLOYMENT_MODE, strDepMode );
        }
    }

    /**
     * Create application object.
     *
     * @param servlet the wicket servlet
     *
     * @return application object instance.
     *
     * @since 1.0.0
     */
    public final WebApplication createApplication( WicketServlet servlet )
    {
        WebApplication paxWicketApplication;

        synchronized ( this )
        {
            boolean deploymentMode = isDeploymentMode();
            String mountPoint = getMountPoint();
            if ( m_authenticator != null && m_signinPage != null )
            {
                paxWicketApplication = new PaxAuthenticatedWicketApplication( mountPoint, m_homepageClass,
                    m_pageFactory, m_delegatingClassResolver, m_authenticator, m_signinPage, deploymentMode );
            }
            else
            {
                paxWicketApplication = new PaxWicketApplication( mountPoint, m_homepageClass, m_pageFactory,
                    m_delegatingClassResolver, deploymentMode );
            }
        }

        return paxWicketApplication;
    }

    @SuppressWarnings("unchecked")
    public final void updated( Dictionary config )
        throws ConfigurationException
    {
        if ( config == null )
        {
            synchronized ( this )
            {
                m_registration.setProperties( m_properties );
            }

            return;
        }

        String classname = (String) config.get( ContentSource.HOMEPAGE_CLASSNAME );
        if ( classname == null )
        {
            synchronized ( this )
            {
                String homepageClassName = m_homepageClass.getName();
                config.put( ContentSource.HOMEPAGE_CLASSNAME, homepageClassName );
            }
        }
        else
        {
            synchronized ( this )
            {
                try
                {
                    Bundle bundle = m_bundleContext.getBundle();
                    m_homepageClass = bundle.loadClass( classname );
                }
                catch ( ClassNotFoundException e )
                {
                    throw new ConfigurationException( ContentSource.HOMEPAGE_CLASSNAME,
                        "Class not found in application bundle.", e );
                }
            }
        }

        String deploymentMode = (String) config.get( ContentSource.DEPLOYMENT_MODE );
        if ( deploymentMode == null )
        {
            String currentDeploymentMode;
            synchronized ( this )
            {
                currentDeploymentMode = (String) m_properties.get( ContentSource.DEPLOYMENT_MODE );
            }
            config.put( ContentSource.DEPLOYMENT_MODE, currentDeploymentMode );
        }

        String applicationName = (String) config.get( ContentSource.APPLICATION_NAME );
        if ( applicationName == null )
        {
            String currentApplicationName;
            synchronized ( this )
            {
                currentApplicationName = (String) m_properties.get( ContentSource.APPLICATION_NAME );
            }

            config.put( ContentSource.APPLICATION_NAME, currentApplicationName );
        }
        else
        {
            setApplicationName( applicationName );
        }

        String mountPoint = (String) config.get( ContentSource.MOUNTPOINT );
        if ( mountPoint == null )
        {
            String currentMountPoint;
            synchronized ( this )
            {
                currentMountPoint = (String) m_properties.getProperty( ContentSource.MOUNTPOINT );
            }
            config.put( ContentSource.MOUNTPOINT, currentMountPoint );
        }
        else
        {
            setMountPoint( mountPoint );
        }

        m_registration.setProperties( config );
    }

    /**
     * Register this {@code PaxWicketApplicationFactory} instance to OSGi container.
     *
     * @return The service registration.
     *
     * @since 1.0.0
     */
    public final ServiceRegistration register()
    {
        String[] serviceNames =
        {
            PaxWicketApplicationFactory.class.getName(), ManagedService.class.getName()
        };
        Properties serviceProperties;
        synchronized ( this )
        {
            serviceProperties = new Properties( m_properties );
        }
        m_registration = m_bundleContext.registerService( serviceNames, this, serviceProperties );

        return m_registration;
    }

    private void setApplicationName( String applicationName )
    {
        synchronized ( this )
        {
            if ( m_pageFactory != null )
            {
                m_pageFactory.dispose();
            }
            if ( m_delegatingClassResolver != null )
            {
                m_delegatingClassResolver.dispose();
            }

            m_delegatingClassResolver = new DelegatingClassResolver( m_bundleContext, applicationName );
            m_delegatingClassResolver.intialize();

            m_pageFactory = new PaxWicketPageFactory( m_bundleContext, applicationName );
            m_pageFactory.initialize();

            m_properties.setProperty( ContentSource.APPLICATION_NAME, applicationName );
        }
    }

    private void setMountPoint( String mountPoint )
    {
        synchronized ( this )
        {
            m_properties.put( ContentSource.MOUNTPOINT, mountPoint );
        }
    }
}