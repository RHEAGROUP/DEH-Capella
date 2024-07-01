/*
 * CapellaObjectBrowserViewModelTestFixture.java
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
package ViewModels.CapellaObjectBrowser;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.polarsys.capella.core.data.pa.PhysicalComponent;
import org.polarsys.capella.core.data.pa.impl.PhysicalComponentImpl;

import Services.CapellaSession.ICapellaSessionService;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.sirius.business.api.session.Session;

import ViewModels.CapellaObjectBrowser.Rows.ElementRowViewModel;
import ViewModels.CapellaObjectBrowser.Rows.RootRowViewModel;

class CapellaObjectBrowserViewModelTestFixture
{
    private CapellaObjectBrowserViewModel viewModel;
    private ICapellaSessionService capellaSessionService;

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    public void setUp() throws Exception
    {
        this.capellaSessionService = mock(ICapellaSessionService.class);
        this.viewModel = new CapellaObjectBrowserViewModel(this.capellaSessionService);
    }

    @Test
    public void VerifyOnSelectionChanged()
    {
        var selectedElements = new ArrayList<ElementRowViewModel<?>>();
        this.viewModel.GetSelectedElement().subscribe(x -> selectedElements.add(x));
        final ElementRowViewModel rowViewModel = mock(ElementRowViewModel.class);
        this.viewModel.OnSelectionChanged(rowViewModel);
        assertSame(rowViewModel, selectedElements.get(selectedElements.size() - 1));
    }
    
    @Test
    public void VerifyBuildTree()
    {
        var isTheTreeVisibleValues = new ArrayList<Boolean>();
        this.viewModel.IsTheTreeVisible().subscribe(x -> isTheTreeVisibleValues.add(x));
        var elements = new ArrayList<EObject>();
        PhysicalComponent physicalComponent = mock(PhysicalComponent.class);
        when(physicalComponent.eContents()).thenReturn(new BasicEList());
        elements.add(physicalComponent);
        
        RootRowViewModel rootRowViewModel = new RootRowViewModel("af", elements);
        
        when(this.capellaSessionService.GetModels()).thenReturn(rootRowViewModel);
        
        assertDoesNotThrow(() -> this.viewModel.BuildTree(elements));
        assertDoesNotThrow(() -> this.viewModel.BuildTree(null));
        var session = mock(Session.class);
        var sessionResource = mock(Resource.class);
        when(sessionResource.getURI()).thenReturn(URI.createURI("ur.i"));
        when(session.getSessionResource()).thenReturn(sessionResource);
        when(this.capellaSessionService.GetSession(any(EObject.class))).thenReturn(session);
        assertDoesNotThrow(() -> this.viewModel.BuildTree(elements));
        assertEquals(3, isTheTreeVisibleValues.size());
        assertTrue(isTheTreeVisibleValues.get(0));
        assertTrue(isTheTreeVisibleValues.get(1));
        assertTrue(isTheTreeVisibleValues.get(2));
    }    
}
