/*
 * CapellaSessionListenerService.java
 *
 * Copyright (c) 2020-2024 Starion Group S.A.
 *
 * Author: Sam Gerené, Alex Vorobiev, Nathanael Smiechowski, Antoine Théate
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

import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManagerListener;
import org.eclipse.sirius.viewpoint.description.Viewpoint;

import Reactive.ObservableValue;
import io.reactivex.Observable;

/**
 * The {@linkplain CapellaSessionListenerService} is an implementation of the {@linkplain SessionManagerListener} for the Capella adapter.
 * This class observes changes in sirius {@linkplain SessionManager}.
 */
public class CapellaSessionListenerService implements ICapellaSessionListenerService
{    
    /**
     * Backing field for {@linkplain SessionAdded}
     */
    private ObservableValue<Session> sessionAdded = new ObservableValue<Session>(Session.class);
    
    /**
     * The {@linkplain Observable} of {@linkplain Session} when the one {@linkplain Session} gets added to the {@linkplain SessionManager}
     * 
     * @return an {@linkplain Observable} of {@linkplain Session}
     */
    @Override
    public Observable<Session> SessionAdded()
    {
        return this.sessionAdded.Observable();
    }
    
    /**
     * Backing field for {@linkplain SessionRemoved}
     */
    private ObservableValue<Session> sessionRemoved = new ObservableValue<Session>(Session.class);

    /**
     * The {@linkplain Observable} of {@linkplain Session} when the one {@linkplain Session} gets removed from the {@linkplain SessionManager}
     * 
     * @return an {@linkplain Observable} of {@linkplain Session}
     */
    @Override
    public Observable<Session> SessionRemoved()
    {
        return this.sessionRemoved.Observable();
    }
    
    /**
     * Backing field for {@linkplain SessionUpdated}
     */
    private ObservableValue<Session> sessionUpdated = new ObservableValue<Session>(Session.class);
    
    /**
     * The {@linkplain Observable} of {@linkplain Session} when the one {@linkplain Session} gets updated
     * 
     * @return an {@linkplain Observable} of {@linkplain Session}
     */
    @Override
    public Observable<Session> SessionUpdated()
    {
        return this.sessionUpdated.Observable();
    }
    
    /**
     * Called when a new session has been added in the manager
     * 
     * @param newSession the {@linkplain Session} that got added
     */
    @Override
    public void notifyAddSession(Session newSession)
    {
        this.sessionAdded.Value(newSession);
    }

    /**
     * Called when a new session has been removed from the manager
     * 
     * @param removedSession the {@linkplain Session} that got removed
     */
    @Override
    public void notifyRemoveSession(Session removedSession)
    {
        this.sessionRemoved.Value(removedSession);
    }

    /**
     * Occurs when the session manager gets an update in the sessions status
     * 
     * @param updatedSession the {@linkplain Session} that was added, updated or removed 
     * @param notificationKind the notification number identifying the kind of notification
     */
    @Override
    public void notify(Session updatedSession, int notificationKind)
    {
        if(notificationKind == 3)
        {
            this.sessionUpdated.Value(updatedSession);
        }
    }
    
    /**
     * Invoked when a viewpoint is selected. Unused
     * 
     * @param selectedViewPoint the selected {@linkplain ViewPoint}
     */
    @Override
    public void viewpointSelected(Viewpoint selectedViewPoint) { }

    /**
     * Invoked when a viewpoint is selected. Unused
     * 
     * @param deselectedViewPoint the deselected {@linkplain ViewPoint}
     */
    @Override
    public void viewpointDeselected(Viewpoint deselectedViewPoint) { }
}
