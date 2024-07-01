/*
 * RequirementToRequirementsSpecificationMappingRuleTestFixture.java
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
package MappingRules;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.cs.ComponentPkg;
import org.polarsys.capella.core.data.fa.FaPackage;
import org.polarsys.capella.core.data.requirement.Requirement;
import org.polarsys.capella.core.data.requirement.RequirementsPkg;

import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.MappingConfiguration.ICapellaMappingConfigurationService;
import Utils.Stereotypes.CapellaComponentCollection;
import Utils.Stereotypes.CapellaRequirementCollection;
import ViewModels.Rows.MappedDstRequirementRowViewModel;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.sitedirectorydata.DomainOfExpertise;

/**
 * The {@linkplain RequirementToRequirementsSpecificationMappingRuleTestFixture} is 
 */
class RequirementToRequirementsSpecificationMappingRuleTestFixture
{
    private ICapellaMappingConfigurationService mappingConfigurationService;
    private IHubController hubController;
    private RequirementToRequirementsSpecificationMappingRule mappingRule;
    private DomainOfExpertise domain;
    private Iteration iteration;
    private CapellaRequirementCollection elements;
    private RequirementsPkg capellaRequirementPackage0;
    private Requirement capellaRequirement2;    
    private Requirement capellaRequirement1;
    private Requirement capellaRequirement0;
    private RequirementsPkg capellaRequirementPackage1;
    private RequirementsPkg capellaRequirementPackage2;
    private cdp4common.engineeringmodeldata.Requirement requirement0;
    private ComponentPkg capellaPackage;
    private cdp4common.engineeringmodeldata.Requirement requirement1;

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    public void setUp() throws Exception
    {
        this.hubController = mock(IHubController.class);
        this.mappingConfigurationService = mock(ICapellaMappingConfigurationService.class);
        
        this.SetupElements();
        when(this.hubController.GetOpenIteration()).thenReturn(this.iteration);
        
        this.mappingRule = new RequirementToRequirementsSpecificationMappingRule(this.hubController, this.mappingConfigurationService);
    }

    @Test
    public void VerifyTransform()
    {
        assertDoesNotThrow(() -> this.mappingRule.Transform(null));
        assertDoesNotThrow(() -> this.mappingRule.Transform(mock(List.class)));
        var result = this.mappingRule.Transform(this.elements);
        assertEquals(3, result.size());
        assertTrue(result.get(0).GetHubElement() != null);
        assertEquals(1, result.get(2).GetHubElement().getContainerOfType(RequirementsSpecification.class).getGroup().size());
        assertEquals(result.get(2).GetHubElement().getContainerOfType(RequirementsSpecification.class).getGroup().get(0), 
                result.get(1).GetHubElement().getContainerOfType(RequirementsSpecification.class).getGroup().get(0));
    }

    private void SetupElements()
    {
        this.elements = new CapellaRequirementCollection();
        
        this.domain = new DomainOfExpertise(UUID.randomUUID(), null, null);
        this.iteration = new Iteration(UUID.randomUUID(), null, null);
        
        this.requirement0 = new cdp4common.engineeringmodeldata.Requirement();
        this.requirement0.setShortName("REQS0");
        this.requirement1 = new cdp4common.engineeringmodeldata.Requirement();
        this.requirement1.setShortName("REQS1");
        
        var requirementsSpecification = new RequirementsSpecification();
        requirementsSpecification.setShortName("capellaRequirementPackage0");
        
        this.iteration.getRequirementsSpecification().add(requirementsSpecification);
        
        this.capellaRequirement0 = mock(Requirement.class);
        when(this.capellaRequirement0.getName()).thenReturn("capellaRequirement0");
        this.capellaRequirement1 = mock(Requirement.class);
        when(this.capellaRequirement1.getName()).thenReturn("capellaRequirement1");
        this.capellaRequirement2 = mock(Requirement.class);
        when(this.capellaRequirement2.getName()).thenReturn("capellaRequirement2");
        
        this.capellaPackage = mock(ComponentPkg.class);
        when(this.capellaPackage.getName()).thenReturn("capellaPackage");
        this.capellaRequirementPackage0 = mock(RequirementsPkg.class);
        when(this.capellaRequirementPackage0.getName()).thenReturn("capellaRequirementPackage0");
        this.capellaRequirementPackage1 = mock(RequirementsPkg.class);
        when(this.capellaRequirementPackage1.getName()).thenReturn("capellaRequirementPackage1");
        this.capellaRequirementPackage2 = mock(RequirementsPkg.class);
        when(this.capellaRequirementPackage2.getName()).thenReturn("capellaRequirementPackage2");
        var containedElements = new BasicEList<EObject>();
        containedElements.add(this.capellaRequirementPackage1);
        containedElements.add(this.capellaRequirementPackage2);
        var containedRequirements0 = new BasicEList<EObject>();
        containedRequirements0.add(this.capellaRequirement1);
        containedRequirements0.add(this.capellaRequirement2);
        var containedRequirements1 = new BasicEList<EObject>();
        containedRequirements1.add(this.capellaRequirement0);

        when(this.capellaRequirement0.eContainer()).thenReturn(this.capellaRequirementPackage2);
        when(this.capellaRequirement1.eContainer()).thenReturn(this.capellaRequirementPackage1);
        when(this.capellaRequirement2.eContainer()).thenReturn(this.capellaRequirementPackage1);
        when(this.capellaPackage.eContainer()).thenReturn(null);
        when(this.capellaRequirementPackage0.eContainer()).thenReturn(this.capellaPackage);
        when(this.capellaRequirementPackage1.eContainer()).thenReturn(this.capellaRequirementPackage0);
        when(this.capellaRequirementPackage2.eContainer()).thenReturn(this.capellaRequirementPackage0);
        
        var packageResource0 = mock(Resource.class);        
        when(packageResource0.getURI()).thenReturn(URI.createURI("packageResource0"));
        when(this.capellaPackage.eResource()).thenReturn(packageResource0);
        
        var packageResource1 = mock(Resource.class);  
        when(packageResource1.getURI()).thenReturn(URI.createURI("packageResource1"));
        when(this.capellaRequirementPackage0.eResource()).thenReturn(packageResource1);
        
        var packageResource2 = mock(Resource.class);        
        when(packageResource2.getURI()).thenReturn(URI.createURI("packageResource2"));
        when(this.capellaRequirementPackage1.eResource()).thenReturn(packageResource2);
        
        var packageResource3 = mock(Resource.class);        
        when(packageResource3.getURI()).thenReturn(URI.createURI("packageResource3"));
        when(this.capellaRequirementPackage2.eResource()).thenReturn(packageResource3);
        
        when(this.capellaPackage.eContents()).thenReturn(new BasicEList<>());
        when(this.capellaRequirementPackage0.eContents()).thenReturn(containedElements);
        when(this.capellaRequirementPackage1.eContents()).thenReturn(containedRequirements0);
        when(this.capellaRequirementPackage2.eContents()).thenReturn(containedRequirements1);
        
        this.elements.add(new MappedDstRequirementRowViewModel(this.requirement0, this.capellaRequirement0, MappingDirection.FromDstToHub));
        this.elements.add(new MappedDstRequirementRowViewModel(this.capellaRequirement1, MappingDirection.FromDstToHub));
        this.elements.add(new MappedDstRequirementRowViewModel(this.capellaRequirement2, MappingDirection.FromDstToHub));
    }
}
