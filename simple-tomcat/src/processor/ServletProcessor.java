package processor;

import connector.*;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class ServletProcessor {

    URLClassLoader getServletLoader() throws MalformedURLException {
        File webroot = new File(ConnectorUtils.WEB_ROOT);
        URL webrootUrl = webroot.toURI().toURL();
        return new URLClassLoader(new URL[]{webrootUrl});
    }

    Servlet getServlet(URLClassLoader loader, Request request) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        /**
         * /servlet/TimeServlet
         */
        String uri = request.getRequestURI();
        String servletName = uri.substring(uri.lastIndexOf("/") + 1);

        Class servletClass = loader.loadClass(servletName);
        Servlet servlet = (Servlet) servletClass.newInstance();
        return servlet;
    }

    public void process(Request request, Response response) throws IOException, ServletException {
        URLClassLoader loader = getServletLoader();
        try {
            Servlet servlet = getServlet(loader, request);
            ServletRequest requestFace = new RequestFacade(request);
            ServletResponse responseFace = new ResponseFacade(response);
            servlet.service(requestFace, responseFace);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

    }


}
