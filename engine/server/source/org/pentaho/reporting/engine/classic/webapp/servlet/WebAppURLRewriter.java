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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.ArrayList;

import org.pentaho.reporting.engine.classic.core.modules.output.table.html.URLRewriteException;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.URLRewriter;
import org.pentaho.reporting.libraries.repository.ContentEntity;
import org.pentaho.reporting.libraries.repository.ContentIOException;
import org.pentaho.reporting.libraries.repository.ContentLocation;

/**
 * Rewrites the URLs generated by the report engine's HTML output to match the content-repository servlet.
 *
 * @author Thomas Morgner
 */
public class WebAppURLRewriter implements URLRewriter
{
  private String pattern;
  private String encoding;

  public WebAppURLRewriter(final String pattern,
                           final String encoding)
  {
    this.pattern = pattern;
    this.encoding = encoding;
  }

  public String rewrite(final ContentEntity contentEntry,
                        final ContentEntity dataEntity) throws URLRewriteException
  {
    try
    {
      final ArrayList<String> entityNames = new ArrayList<String>();
      entityNames.add(dataEntity.getName());

      ContentLocation location = dataEntity.getParent();
      while (location != null)
      {
        entityNames.add(location.getName());
        location = location.getParent();
      }

      final ArrayList<String> contentNames = new ArrayList<String>();
      location = dataEntity.getRepository().getRoot();

      while (location != null)
      {
        contentNames.add(location.getName());
        location = location.getParent();
      }

      // now remove all path elements that are equal ..
      while (contentNames.isEmpty() == false && entityNames.isEmpty() == false)
      {
        final String lastEntity = entityNames.get(entityNames.size() - 1);
        final String lastContent = contentNames.get(contentNames.size() - 1);
        if (lastContent.equals(lastEntity) == false)
        {
          break;
        }
        entityNames.remove(entityNames.size() - 1);
        contentNames.remove(contentNames.size() - 1);
      }

      final StringBuilder b = new StringBuilder(100);
      for (int i = entityNames.size() - 1; i >= 0; i--)
      {
        final String name = entityNames.get(i);
        b.append(name);
        if (i != 0)
        {
          b.append('/');
        }
      }

      final String filename = URLEncoder.encode(b.toString(), encoding);

      if (pattern == null)
      {
        return filename;
      }

      return MessageFormat.format(pattern, filename);
    }
    catch (ContentIOException cioe)
    {
      throw new URLRewriteException("Failed to compute path", cioe);
    }
    catch (UnsupportedEncodingException e)
    {
      throw new URLRewriteException("Failed to encode path", e);
    }

  }
}
