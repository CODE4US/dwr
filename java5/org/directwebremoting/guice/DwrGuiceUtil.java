/*
 * Copyright 2007 Tim Peierls
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.directwebremoting.guice;

import com.google.inject.Injector;

import java.util.List;

import javax.servlet.ServletContext;

import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;


/**
 * Utility for retrieving an Injector in a servlet context.
 * @author Tim Peierls [tim at peierls dot net]
 */
class DwrGuiceUtil
{
    /**
     * Returns the Injector instance installed in the current web context.
     */
    public static Injector getInjector()
    {
        return getInjector(getServletContext());
    }


    /**
     * Returns the Injector instance installed in the given ServletContext.
     */
    static Injector getInjector(ServletContext servletContext)
    {
        Injector injector = (Injector) servletContext.getAttribute(INJECTOR);

        if (injector == null)
        {
            throw new IllegalStateException("Cannot find Injector in servlet context."
                + " You need to register a concrete extension of "
                + DwrGuiceServletContextListener.class.getName()
                + " as a servlet context listener in your web.xml.");
        }

        return injector;
    }

    static void installInjector(ServletContext servletContext, Injector injector)
    {
        servletContext.setAttribute(INJECTOR, injector);
    }

    static void uninstallInjector(ServletContext servletContext)
    {
        servletContext.removeAttribute(INJECTOR);
    }

    /**
     * The key under which a provided Injector is stashed in a ServletContext.
     * The name is prefixed by the package to avoid conflicting with other
     * listeners using the same technique.
     */
    private static final String INJECTOR =
        DwrGuiceServletContextListener.class.getPackage().getName() + ".Injector";


    
    static ServletContext getServletContext()
    {
        WebContext webcx = WebContextFactory.get();
        if (webcx != null)
        {
            return webcx.getServletContext();
        }
        else
        {
            return servletContext.get();
        }
    }
    
    static void setServletContext(ServletContext context)
    {
        servletContext.set(context);
    }
    
    static void removeServletContext()
    {
        servletContext.remove();
    }
    
    private static final ThreadLocal<ServletContext> servletContext = 
        new ThreadLocal<ServletContext>();


    
    static String classListToString(List<Class> classList)
    {
        StringBuilder buf = new StringBuilder();
        int count = 0;
        for (Class cls : classList)
        {
            if (count++ > 0) buf.append(", ");
            buf.append(cls.getName());
        }
        return buf.toString();
    }
}