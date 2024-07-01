/*
 * MappedElementRowViewModelTestFixture.java
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
package ViewModels.Rows;

import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.polarsys.capella.core.data.la.LogicalComponent;
import org.polarsys.capella.core.data.requirement.SystemUserRequirement;

import Enumerations.MappingDirection;
import Reactive.ObservableValue;
import Utils.Ref;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import io.reactivex.functions.Consumer;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.sirius.business.api.session.Session;
import org.eclipse.sirius.business.api.session.SessionManager;
import org.eclipse.sirius.viewpoint.description.Viewpoint;

public class MappedElementRowViewModelTestFixture
{
    private MappedDstRequirementRowViewModel dstRequirementRowViewModel;
    private MappedDstRequirementRowViewModel hubRequirementRowViewModel;
    private MappedElementDefinitionRowViewModel dstElementRowViewModel;
    private MappedElementDefinitionRowViewModel hubElementRowViewModel;

    @BeforeEach
    public void Setup()
    {
        var requirement = mock(SystemUserRequirement.class);
        when(requirement.getName()).thenReturn("user requirement");
        this.dstRequirementRowViewModel = new MappedDstRequirementRowViewModel(requirement, MappingDirection.FromDstToHub);
        this.hubRequirementRowViewModel = new MappedDstRequirementRowViewModel(new cdp4common.engineeringmodeldata.Requirement(), requirement, MappingDirection.FromHubToDst);
        
        var logicalComponent = mock(LogicalComponent.class);
        when(logicalComponent.getName()).thenReturn("la component");
        this.dstElementRowViewModel = new MappedElementDefinitionRowViewModel(logicalComponent, MappingDirection.FromDstToHub);
        this.hubElementRowViewModel = new MappedElementDefinitionRowViewModel(new ElementDefinition(), logicalComponent, MappingDirection.FromHubToDst);
    }
    
    @Test
    public void GetHubElementRepresentation()
    {
        assertNotNull(this.dstElementRowViewModel.GetHubElementRepresentation());
        assertNotNull(this.hubElementRowViewModel.GetHubElementRepresentation());
        assertNotNull(this.dstRequirementRowViewModel.GetHubElementRepresentation());
        assertNotNull(this.hubRequirementRowViewModel.GetHubElementRepresentation());
    }
    
    @Test
    public void VerifyGetDstElementRepresentation()
    {
        assertNotNull(this.dstElementRowViewModel.GetDstElementRepresentation());
        assertNotNull(this.hubElementRowViewModel.GetDstElementRepresentation());
        assertNotNull(this.dstRequirementRowViewModel.GetDstElementRepresentation());
        assertNotNull(this.hubRequirementRowViewModel.GetDstElementRepresentation());
    }
}
