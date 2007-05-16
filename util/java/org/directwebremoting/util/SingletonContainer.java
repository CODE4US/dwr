package org.directwebremoting.util;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.directwebremoting.WebContextFactory.WebContextBuilder;
import org.directwebremoting.extend.ConverterManager;
import org.directwebremoting.impl.ContainerUtil;
import org.directwebremoting.impl.DefaultContainer;
import org.directwebremoting.impl.DwrXmlConfigurator;
import org.directwebremoting.impl.StartupUtil;
import org.directwebremoting.util.FakeServletConfig;
import org.directwebremoting.util.FakeServletContext;

/**
 * @author Joe Walker [joe at getahead dot ltd dot uk]
 */
public class SingletonContainer
{
    /**
     *
     */
    public SingletonContainer() throws Exception
    {
        try
        {
            // Setup the DWR container
            container = ContainerUtil.createDefaultContainer(servletConfig);
            ContainerUtil.setupDefaultContainer(container, servletConfig);

            webContextBuilder = StartupUtil.initWebContext(servletConfig, servletContext, container);
            StartupUtil.initServerContext(servletConfig, servletContext, container);
            ContainerUtil.prepareForWebContextFilter(servletContext, servletConfig, container, webContextBuilder, null);

            ContainerUtil.configureFromSystemDwrXml(container);

            DwrXmlConfigurator local = new DwrXmlConfigurator();
            local.setClassResourceName("/dwr.xml");
            local.configure(container);

            ContainerUtil.configureFromInitParams(container, servletConfig);

            ContainerUtil.publishContainer(container, servletConfig);
        }
        finally
        {
            if (webContextBuilder != null)
            {
                webContextBuilder.unset();
            }
        }
    }

    /**
     * 
     */
    public void engageThread()
    {
        webContextBuilder.set(null, null, servletConfig, servletContext, container);
    }

    /**
     * 
     */
    public void disengageThread()
    {
        webContextBuilder.unset();
    }

    /**
     *
     */
    public ConverterManager getConverterManager()
    {
        return (ConverterManager) container.getBean(ConverterManager.class.getName());
    }

    /**
     * @return the container
     */
    public DefaultContainer getContainer()
    {
        return container;
    }

    /**
     * @return the servletConfig
     */
    public ServletConfig getServletConfig()
    {
        return servletConfig;
    }

    /**
     * @return the servletContext
     */
    public ServletContext getServletContext()
    {
        return servletContext;
    }

    /**
     * @return the webContextBuilder
     */
    public WebContextBuilder getWebContextBuilder()
    {
        return webContextBuilder;
    }

    private DefaultContainer container;

    private WebContextBuilder webContextBuilder;

    private ServletContext servletContext = new FakeServletContext();

    private ServletConfig servletConfig = new FakeServletConfig("dwr-test", servletContext);
}