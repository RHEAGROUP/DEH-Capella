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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.polarsys.capella.core.data.capellacore.BooleanPropertyValue;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.FloatPropertyValue;
import org.polarsys.capella.core.data.capellacore.IntegerPropertyValue;
import org.polarsys.capella.core.data.capellacore.StringPropertyValue;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.capellamodeller.SystemEngineering;
import org.polarsys.capella.core.data.cs.ComponentPkg;
import org.polarsys.capella.core.data.information.Property;
import org.polarsys.capella.core.data.information.datavalue.NumericValue;
import org.polarsys.capella.core.data.la.LogicalArchitecture;
import org.polarsys.capella.core.data.la.LogicalComponent;
import org.polarsys.capella.core.data.requirement.RequirementsPkg;
import org.polarsys.capella.core.data.requirement.SystemFunctionalInterfaceRequirement;
import org.polarsys.capella.core.data.requirement.SystemFunctionalRequirement;
import org.polarsys.capella.core.data.requirement.SystemNonFunctionalInterfaceRequirement;
import org.polarsys.capella.core.data.requirement.SystemNonFunctionalRequirement;
import org.polarsys.capella.core.data.requirement.SystemUserRequirement;

import Reactive.ObservableValue;
import Services.CapellaSelection.ICapellaSelectionService;
import Utils.Ref;
import Utils.Stereotypes.RequirementType;
import ViewModels.CapellaObjectBrowser.Rows.RequirementRowViewModel;
import ViewModels.CapellaObjectBrowser.Rows.RootRowViewModel;
import ViewModels.ObjectBrowser.Interfaces.IHaveContainedRows;
import io.reactivex.Observable;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.AbstractTreeIterator;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.provider.AdapterFactoryTreeIterator;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManager;

public class CapellaSessionServiceTestFixture extends CapellaSessionRelatedBaseTestFixture
{
    private ICapellaSessionService service;
    private ObservableValue<Session> sessionAdded;
    private ObservableValue<Session> sessionRemoved;
    private ObservableValue<Session> sessionUpdated;
    private ISiriusSessionManagerWrapper sessionManager;
    private URI sessionUri;

    @BeforeEach
    public void Setup()
    {
        this.sessionAdded = new ObservableValue<Session>(Session.class);
        this.sessionRemoved = new ObservableValue<Session>(Session.class);
        this.sessionUpdated = new ObservableValue<Session>(Session.class);
        
        var sessionListener = mock(ICapellaSessionListenerService.class);
        this.sessionManager = mock(ISiriusSessionManagerWrapper.class);
        
        when(sessionListener.SessionAdded()).thenReturn(this.sessionAdded.Observable());
        when(sessionListener.SessionRemoved()).thenReturn(this.sessionRemoved.Observable());
        when(sessionListener.SessionUpdated()).thenReturn(this.sessionUpdated.Observable());
                
        this.service = new CapellaSessionService(sessionListener, sessionManager);
    }

    @Test
    public void VerifyHasAnyOpenSession()
    {
        var results = new ArrayList<Boolean>();
        this.service.HasAnyOpenSessionObservable().subscribe(x -> results.add(x));
        this.sessionAdded.Value(mock(Session.class));
        this.sessionRemoved.Value(mock(Session.class));
        
        assertEquals(2, results.size());
        assertFalse(results.get(0));
    }
    
    @Test
    public void VerifyGetModels()
    {
        assertNull(this.service.GetModels());
        this.sessionAdded.Value(mock(Session.class));
        assertNull(this.service.GetModels());
        this.sessionRemoved.Value(mock(Session.class));
        assertNull(this.service.GetModels());
        this.sessionUri = URI.createURI("t.e.s.t");
        var sessions = Arrays.asList(this.GetSession(this.sessionUri));
        when(this.sessionManager.GetSessions()).thenReturn(sessions);
        when(this.sessionManager.HasAnyOpenSession()).thenReturn(true);
        var result = new Ref<RootRowViewModel>(null);
        assertDoesNotThrow(() -> result.Set(this.service.GetModels()));
        assertNotNull(result.Get());
        
        RequirementType requirementType = ((RequirementRowViewModel)
                ((IHaveContainedRows<?>)((IHaveContainedRows<?>)((IHaveContainedRows<?>)
                        result.Get().GetContainedRows().get(0)).GetContainedRows().get(0))
                            .GetContainedRows().get(1)).GetContainedRows().get(4)).GetRequirementType();
        
        assertEquals(null, requirementType);
    }

    @Test
    public void VerifyGetAllCapellaElementsFromOpenSessions()
    {
        this.sessionUri = URI.createURI("t.e.s.t");
        var sessions = Arrays.asList(this.GetSession(this.sessionUri));
        when(this.sessionManager.GetSessions()).thenReturn(sessions);
        
        var result = new Ref<HashMap<URI, List<CapellaElement>>>(null);
        assertDoesNotThrow(() -> result.Set(this.service.GetAllCapellaElementsFromOpenSessions()));
        assertTrue(result.Get().keySet().contains(this.sessionUri));
        assertEquals(16, result.Get().get(this.sessionUri).size());
    }
}
