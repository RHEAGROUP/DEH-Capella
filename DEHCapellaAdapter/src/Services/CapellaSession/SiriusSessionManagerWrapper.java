/*
 * SiriusSessionManagerWrapper.java
 *
 * Copyright (c) 2020-2024 Starion Group S.A.
 *
 * Author: Sam Gerené, Alex Vorobiev, Nathanael Smiechowski 
 *
 * This file is part of DEH-Capella
 *
 * The DEH-Capella is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * The DEH-Capella is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package Services.CapellaSession;

import java.util.Collection;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.sirius.business.api.session.SessionManagerListener;

import Annotations.ExludeFromCodeCoverageGeneratedReport;

/**
 * The SiriusSessionManagerWrapper is a wrapper class around the {@linkplain SessionManager.INSTANCE}
 */
@ExludeFromCodeCoverageGeneratedReport
public class SiriusSessionManagerWrapper implements ISiriusSessionManagerWrapper
{
    /**
     * The current class Logger
     */
    private final Logger logger = LogManager.getLogger();
    
    /**
     * Gets the {@linkplain SessionManager} instance
     * @return
     */
    private SessionManager GetManager()
    {
        return SessionManager.INSTANCE;
    }
    
    /**
     * Add a new listener for the session manager
     * 
     * @param listener the new {@linkplain SessionManagerListener}
     */
    @Override
    public void AddListener(SessionManagerListener listener)
    {
        this.GetManager().addSessionsListener(listener);
    }
    
    /**
     * Gets all the open {@linkplain Session}
     * 
     * @return a {@linkplain Collection} of {@linkplain Session}
     */
    @Override
    public Collection<Session> GetSessions()
    {
        return this.GetManager().getSessions();
    }
    
    /**
     * Tries to return the session corresponding to a semantic 
     * 
     * @param object any semantic {@linkplain EObject}
     * @return the corresponding {@linkplain Session}
     */
    @Override
    public Session GetSession(EObject object)
    {
        return this.GetManager().getSession(object);
    }
    
    /**
     * Gets a value indicating whether there is any active session in the workspace
     * 
     * @return a {@linkplain boolean}
     */
    @Override
    public boolean HasAnyOpenSession()
    {
        try
        {
            return !this.GetManager().getSessions().isEmpty();
        }
        catch(Exception exception)
        {
            this.logger.catching(exception);
            return false;
        }
    }
    
}
