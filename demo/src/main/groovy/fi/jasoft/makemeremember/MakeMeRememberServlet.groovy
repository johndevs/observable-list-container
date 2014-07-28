package fi.jasoft.makemeremember

import com.vaadin.server.VaadinServlet

import javax.servlet.annotation.WebInitParam
import javax.servlet.annotation.WebServlet

@WebServlet(
    asyncSupported=false,
    urlPatterns=["/*","/VAADIN/*"],
    initParams=[
        @WebInitParam(name="ui", value="fi.jasoft.makemeremember.MakeMeRememberUI"),
	    @WebInitParam(name="widgetset", value="fi.jasoft.makemeremember.Widgetset")
    ])
public class MakeMeRememberServlet extends VaadinServlet { }
