/*
 * Copyright 2005 Joe Walker
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
package uk.ltd.getahead.dwr.dwrp;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import uk.ltd.getahead.dwr.AccessControl;
import uk.ltd.getahead.dwr.Calls;
import uk.ltd.getahead.dwr.ClientScript;
import uk.ltd.getahead.dwr.Creator;
import uk.ltd.getahead.dwr.CreatorManager;
import uk.ltd.getahead.dwr.HtmlConstants;
import uk.ltd.getahead.dwr.HttpRequest;
import uk.ltd.getahead.dwr.HttpResponse;
import uk.ltd.getahead.dwr.MarshallException;
import uk.ltd.getahead.dwr.Marshaller;
import uk.ltd.getahead.dwr.Replies;
import uk.ltd.getahead.dwr.Reply;
import uk.ltd.getahead.dwr.TypeHintContext;
import uk.ltd.getahead.dwr.WebContextFactory;
import uk.ltd.getahead.dwr.util.JavascriptUtil;
import uk.ltd.getahead.dwr.util.Logger;
import uk.ltd.getahead.dwr.util.Messages;

/**
 * @author Joe Walker [joe at getahead dot ltd dot uk]
 */
public class DwrpMarshaller implements Marshaller
{
    /* (non-Javadoc)
     * @see uk.ltd.getahead.dwr.Marshaller#marshallInbound(uk.ltd.getahead.dwr.HttpRequest)
     */
    public Calls marshallInbound(HttpRequest request) throws MarshallException
    {
        RequestParser requestParser = new RequestParser();
        CallsWithContext calls = requestParser.parseRequest(request);

        // Debug the environment
        if (log.isDebugEnabled() && calls.getCallCount() > 0)
        {
            // We can just use 0 because they are all shared
            InboundContext inctx = calls.getCallWithContext(0).getInboundContext();
            StringBuffer buffer = new StringBuffer();

            for (Iterator it = inctx.getInboundVariableNames(); it.hasNext();)
            {
                String key = (String) it.next();
                InboundVariable value = inctx.getInboundVariable(key);
                if (key.startsWith(ConversionConstants.INBOUND_CALLNUM_PREFIX) &&
                    key.indexOf(ConversionConstants.INBOUND_CALLNUM_SUFFIX + ConversionConstants.INBOUND_KEY_ENV) != -1)
                {
                    buffer.append(key + '=' + value.toString() + ", "); //$NON-NLS-1$
                }
            }

            if (buffer.length() > 0)
            {
                log.debug("Environment:  " + buffer.toString()); //$NON-NLS-1$
            }
        }

        for (int callNum = 0; callNum < calls.getCallCount(); callNum++)
        {
            CallWithContext call = calls.getCallWithContext(callNum);
            InboundContext inctx = call.getInboundContext();


            // Get a list of the available matching methods with the coerced
            // parameters that we will use to call it if we choose to use
            // that method.
            Creator creator = creatorManager.getCreator(call.getScriptName());

            // Which method are we using?
            Method method = findMethod(call);
            call.setMethod(method);
            if (method == null)
            {
                String name = call.getScriptName() + '.' + call.getMethodName();
                throw new IllegalArgumentException(Messages.getString("DefaultRemoter.UnknownMethod", name)); //$NON-NLS-1$
            }

            // Check this method is accessible
            String reason = accessControl.getReasonToNotExecute(creator, call.getScriptName(), method);
            if (reason != null)
            {
                throw new SecurityException(Messages.getString("ExecuteQuery.AccessDenied")); //$NON-NLS-1$
            }

            // Convert all the parameters to the correct types
            Object[] params = new Object[method.getParameterTypes().length];
            for (int j = 0; j < method.getParameterTypes().length; j++)
            {
                Class paramType = method.getParameterTypes()[j];
                InboundVariable param = inctx.getParameter(callNum, j);
                TypeHintContext incc = new TypeHintContext(method, j);

                params[j] = converterManager.convertInbound(paramType, param, inctx, incc);
            }

            call.setParameters(params);
        }

        return calls;
    }

    /**
     * Find the method the best matches the method name and parameters
     * @param call The function call we are going to make
     * @return A matching method, or null if one was not found.
     */
    private Method findMethod(CallWithContext call)
    {
        if (call.getScriptName() == null)
        {
            throw new IllegalArgumentException(Messages.getString("DefaultRemoter.MissingClassParam")); //$NON-NLS-1$
        }

        if (call.getMethodName() == null)
        {
            throw new IllegalArgumentException(Messages.getString("DefaultRemoter.MissingMethodParam")); //$NON-NLS-1$
        }

        Creator creator = creatorManager.getCreator(call.getScriptName());
        Method[] methods = creator.getType().getMethods();
        List available = new ArrayList();

        methods:
        for (int i = 0; i < methods.length; i++)
        {
            // Check method name and access
            if (methods[i].getName().equals(call.getMethodName()))
            {
                // Check number of parameters
                if (methods[i].getParameterTypes().length == call.getInboundContext().getParameterCount())
                {
                    // Clear the previous conversion attempts (the param types
                    // will probably be different)
                    call.getInboundContext().clearConverted();

                    // Check parameter types
                    for (int j = 0; j < methods[i].getParameterTypes().length; j++)
                    {
                        Class paramType = methods[i].getParameterTypes()[j];
                        if (!converterManager.isConvertable(paramType))
                        {
                            // Give up with this method and try the next
                            continue methods;
                        }
                    }

                    available.add(methods[i]);
                }
            }
        }

        // Pick a method to call
        if (available.size() > 1)
        {
            log.warn("Warning multiple matching methods. Using first match."); //$NON-NLS-1$
        }

        if (available.isEmpty())
        {
            String name = call.getScriptName() + '.' + call.getMethodName();
            throw new IllegalArgumentException(Messages.getString("ExecuteQuery.UnknownMethod", name)); //$NON-NLS-1$
        }

        // At the moment we are just going to take the first match, for a
        // later increment we might pick the best implementation
        return (Method) available.get(0);
    }

    /* (non-Javadoc)
     * @see uk.ltd.getahead.dwr.Marshaller#marshallOutbound(uk.ltd.getahead.dwr.Replies)
     */
    public HttpResponse marshallOutbound(final Replies replies) throws MarshallException
    {
        // We build the answer up in a StringBuffer because that makes is easier
        // to debug, and because that's only what the compiler does anyway.
        final StringBuffer buffer = new StringBuffer();

        // if we are in html (iframe mode) we need to direct script to the parent
        String prefix = replies.isXhrMode() ? "" : "window.parent."; //$NON-NLS-1$ //$NON-NLS-2$

        // iframe mode starts as HTML, so get into script mode
        if (!replies.isXhrMode())
        {
            buffer.append("<script type='text/javascript'>\n"); //$NON-NLS-1$
        }

        // Are there any outstanding reverse-ajax scripts to be passed on?
        List scripts = WebContextFactory.get().getBrowser().removeAllScripts();
        for (Iterator it = scripts.iterator(); it.hasNext();)
        {
            ClientScript script = (ClientScript) it.next();
            buffer.append(script);
            buffer.append('\n');
        }

        OutboundContext converted = new OutboundContext();
        for (int i = 0; i < replies.getReplyCount(); i++)
        {
            Reply reply = replies.getReply(i);

            // The existance of a throwable indicates that something went wrong
            if (reply.getThrowable() != null)
            {
                Throwable ex = reply.getThrowable();
                OutboundVariable ov = convertException(ex, converted);

                buffer.append(ov.getInitCode());
                buffer.append(prefix);
                buffer.append("DWREngine._handleServerError('"); //$NON-NLS-1$
                buffer.append(reply.getId());
                buffer.append("', "); //$NON-NLS-1$
                buffer.append(ov.getAssignCode());
                buffer.append(");\n"); //$NON-NLS-1$

                log.warn("--Erroring: id[" + reply.getId() + "] message[" + ex.toString() + ']'); //$NON-NLS-1$ //$NON-NLS-2$
            }
            else
            {
                Object data = reply.getReply();
                OutboundVariable ov = converterManager.convertOutbound(data, converted);

                buffer.append(ov.getInitCode());
                buffer.append(prefix);
                buffer.append("DWREngine._handleResponse('"); //$NON-NLS-1$
                buffer.append(reply.getId());
                buffer.append("', "); //$NON-NLS-1$
                buffer.append(ov.getAssignCode());
                buffer.append(");\n"); //$NON-NLS-1$
            }
        }

        // iframe mode needs to get out of script mode
        if (!replies.isXhrMode())
        {
            buffer.append("</script>\n"); //$NON-NLS-1$
        }

        final String replyString = buffer.toString();
        log.debug(replyString);

        return new HttpResponse()
        {
            public String getMimeType()
            {
                return replies.isXhrMode() ? HtmlConstants.MIME_PLAIN : HtmlConstants.MIME_HTML;
            }

            public byte[] getBody()
            {
                return replyString.getBytes();
            }
        };
    }

    /**
     * Convert an exception into an outbound variable
     * @param th The exception to be converted
     * @param converted The conversion context
     * @return A new outbound exception
     */
    private OutboundVariable convertException(Throwable th, OutboundContext converted)
    {
        try
        {
            if (converterManager.isConvertable(th.getClass()))
            {
                return converterManager.convertOutbound(th, converted);
            }
        }
        catch (MarshallException ex)
        {
            log.warn("Exception while converting. Exception to be converted: " + th, ex); //$NON-NLS-1$
        }

        // So we will have to create one for ourselves
        OutboundVariable ov = new OutboundVariable();
        String varName = converted.getNextVariableName();
        ov.setAssignCode(varName);
        ov.setInitCode("var " + varName + " = \"" + JavascriptUtil.escapeJavaScript(th.getMessage()) + "\";"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        return ov;
    }

    /* (non-Javadoc)
     * @see uk.ltd.getahead.dwr.Marshaller#isConvertable(java.lang.Class)
     */
    public boolean isConvertable(Class paramType)
    {
        return converterManager.isConvertable(paramType);
    }

    /**
     * Accessor for the DefaultCreatorManager that we configure
     * @param converterManager The new DefaultConverterManager
     */
    public void setConverterManager(ConverterManager converterManager)
    {
        this.converterManager = converterManager;
    }

    /**
     * Accessor for the DefaultCreatorManager that we configure
     * @param creatorManager The new DefaultConverterManager
     */
    public void setCreatorManager(CreatorManager creatorManager)
    {
        this.creatorManager = creatorManager;
    }

    /**
     * Accessor for the security manager
     * @param accessControl The accessControl to set.
     */
    public void setAccessControl(AccessControl accessControl)
    {
        this.accessControl = accessControl;
    }

    /**
     * How we convert parameters
     */
    protected ConverterManager converterManager = null;

    /**
     * How we create new beans
     */
    protected CreatorManager creatorManager = null;

    /**
     * The security manager
     */
    protected AccessControl accessControl = null;

    /**
     * The log stream
     */
    private static final Logger log = Logger.getLogger(DwrpMarshaller.class);
}