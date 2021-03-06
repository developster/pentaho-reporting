/*!
* This program is free software; you can redistribute it and/or modify it under the
* terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
* Foundation.
*
* You should have received a copy of the GNU Lesser General Public License along with this
* program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
* or from the Free Software Foundation, Inc.,
* 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*
* This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
* without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
* See the GNU Lesser General Public License for more details.
*
* Copyright (c) 2002-2013 Pentaho Corporation..  All rights reserved.
*/

package org.pentaho.reporting.engine.classic.webapp.servlet;

import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.pdf.PdfReportUtil;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URL;

/**
 * Processes a report.
 *
 * @author Thomas Morgner
 */
public class PdfReportServlet extends HttpServlet {
  public PdfReportServlet() {
  }

  public void init() throws ServletException {
    ClassicEngineBoot.getInstance().start();
  }

  protected void doGet( final HttpServletRequest request,
                        final HttpServletResponse response ) throws ServletException, IOException {
    final String reportDefinition = request.getParameter( "name" );
    final URL reportUrl = getServletContext().getResource( reportDefinition );
    if ( reportUrl == null ) {
      response.sendError( HttpServletResponse.SC_BAD_REQUEST );
      return;
    }

    try {
      final ResourceManager resourceManager = new ResourceManager();
      final Resource resource = resourceManager.createDirectly( reportUrl, MasterReport.class );
      final MasterReport report = (MasterReport) resource.getResource();
      report
        .setReportEnvironment( new SessionReportEnvironment( report.getReportEnvironment(), request.getSession() ) );
      response.setContentType( "application/pdf" );

      final ServletOutputStream stream = response.getOutputStream();
      if ( PdfReportUtil.createPDF( report, stream ) == false ) {
        log( "Failed to process the report" );
        response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
        return;
      }
      stream.flush();
    } catch ( ResourceException e ) {
      log( "Failed to parse report", e );
      response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
    } catch ( ReportProcessingException e ) {
      log( "Failed to process report", e );
      response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
    }
  }

  protected void doPost( final HttpServletRequest httpServletRequest,
                         final HttpServletResponse httpServletResponse ) throws ServletException, IOException {
    doGet( httpServletRequest, httpServletResponse );
  }
}
