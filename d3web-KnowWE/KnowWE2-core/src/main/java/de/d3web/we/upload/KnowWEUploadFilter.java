package de.d3web.we.upload;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import de.d3web.we.core.KnowWEFacade;



public class KnowWEUploadFilter implements Filter {

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain arg2) throws IOException, ServletException {
		PrintWriter writer = res.getWriter();
		
		if (req instanceof HttpServletRequest) {
			boolean isMultipart = ServletFileUpload
					.isMultipartContent((HttpServletRequest) req);
			if (isMultipart) {
				FileItemFactory factory = new DiskFileItemFactory();
				ServletFileUpload upload = new ServletFileUpload(factory);
				List<FileItem> items = null;
				try {
					items = upload.parseRequest((HttpServletRequest) req);
				} catch (FileUploadException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				KnowWEFacade env = KnowWEFacade.getInstance();
				String result = env.uploadFiles(items);
				if(result.startsWith("redirect:")) {
					String url = result.substring("redirect:".length());
					if(res instanceof HttpServletResponse) {
						((HttpServletResponse)res).sendRedirect(url);
					}
				}else {
				writer.write(result);
				}
			} else {
				writer.write("no mulitpart");
			}
		} else {
			writer.write("no HttpServletRequest");
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub

	}

}
