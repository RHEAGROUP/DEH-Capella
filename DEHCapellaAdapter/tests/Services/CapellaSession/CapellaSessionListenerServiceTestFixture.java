/*
 * CapellaSessionServiceTestFixture.java
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

import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import Reactive.ObservableValue;
import Utils.Ref;
import io.reactivex.functions.Consumer;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.sirius.viewpoint.description.Viewpoint;

public class CapellaSessionListenerServiceTestFixture
{
    private CapellaSessionListenerService service;
    private ObservableValue<Session> sessionAdded;
    private ObservableValue<Session> sessionRemoved;

    @BeforeEach
    public void Setup()
    {
        this.sessionAdded = new ObservableValue<Session>(Session.class);
        this.sessionRemoved = new ObservableValue<Session>(Session.class);

        var sessionListener = mock(ICapellaSessionListenerService.class);
        
        when(sessionListener.SessionAdded()).thenReturn(this.sessionAdded.Observable());
        when(sessionListener.SessionRemoved()).thenReturn(this.sessionRemoved.Observable());
                
        this.service = new CapellaSessionListenerService();
    }

    @Test
    public void VerifySessionEvents()
    {
        assertNotNull(this.service.SessionAdded());
        assertNotNull(this.service.SessionRemoved());
        assertNotNull(this.service.SessionUpdated());
        
        var timesSessionEventRaised = new Ref<>(Integer.class, 0);
        Consumer<? super Session> incrementTimeSessionEventRaised = x -> timesSessionEventRaised.Set(timesSessionEventRaised.Get() + 1);
        
        this.service.SessionAdded().subscribe(incrementTimeSessionEventRaised);
        this.service.SessionRemoved().subscribe(incrementTimeSessionEventRaised);
        this.service.SessionUpdated().subscribe(incrementTimeSessionEventRaised);
        
        assertDoesNotThrow(() -> this.service.notifyRemoveSession(mock(Session.class)));
        assertDoesNotThrow(() -> this.service.notifyAddSession(mock(Session.class)));
        assertDoesNotThrow(() -> this.service.notify(mock(Session.class), 0));
        assertDoesNotThrow(() -> this.service.notify(mock(Session.class), 1));
        assertDoesNotThrow(() -> this.service.notify(mock(Session.class), 3));
        assertEquals(3, timesSessionEventRaised.Get());
    }
    
    @Test
    public void VerifyViewpointEvents()
    {
        assertDoesNotThrow(() -> this.service.viewpointSelected(mock(Viewpoint.class)));
        assertDoesNotThrow(() -> this.service.viewpointDeselected(mock(Viewpoint.class)));
    }
}
