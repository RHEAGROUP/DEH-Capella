/*
 * ICapellaSessionListenerService.java
 *
 * Copyright (c) 2020-2021 RHEA System S.A.
 *
 * Author: Sam Geren�, Alex Vorobiev, Nathanael Smiechowski, Antoine Th�ate
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

import io.reactivex.Observable;

/**
 * The {@linkplain ICapellaSessionListenerService} is the main interface definition for the {@linkplain CapellaSessionListenerService}
 */
public interface ICapellaSessionListenerService extends SessionManagerListener
{
    /**
     * The {@linkplain Observable} of {@linkplain Session} when the one {@linkplain Session} gets updated
     */
    Observable<Session> SessionUpdated();

    /**
     * The {@linkplain Observable} of {@linkplain Session} when the one {@linkplain Session} gets removed from the {@linkplain SessionManager}
     */
    Observable<Session> SessionRemoved();

    /**
     * The {@linkplain Observable} of {@linkplain Session} when the one {@linkplain Session} gets added to the {@linkplain SessionManager}
     */
    Observable<Session> SessionAdded();
}
