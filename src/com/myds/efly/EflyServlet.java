package com.myds.efly;
import java.io.IOException;
import javax.servlet.http.*;

@SuppressWarnings("serial")
public class EflyServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");
		resp.getWriter().println("Hello, world");
		System.out.println(req.getParameter("model"));
		System.out.println("sdfsdf");
	}
}
