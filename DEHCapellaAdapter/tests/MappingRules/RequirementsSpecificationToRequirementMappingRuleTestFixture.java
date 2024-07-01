/*
 * RequirementsSpecificationToRequirementMappingRuleTestFixture.java
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
package MappingRules;

import static org.mockito.Mockito.*;

import java.util.List;
import java.util.UUID;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;

import static org.mockito.ArgumentMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.capellacore.Namespace;
import org.polarsys.capella.core.data.capellacore.Structure;
import org.polarsys.capella.core.data.cs.ComponentPkg;
import org.polarsys.capella.core.data.requirement.Requirement;
import org.polarsys.capella.core.data.requirement.RequirementsPkg;
import org.polarsys.capella.core.data.requirement.SystemNonFunctionalInterfaceRequirement;
import org.polarsys.capella.core.data.requirement.SystemNonFunctionalRequirement;

import DstController.IDstController;
import Enumerations.CapellaArchitecture;
import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.CapellaTransaction.CapellaTransactionService;
import Services.CapellaTransaction.ICapellaTransactionService;
import Services.MappingConfiguration.ICapellaMappingConfigurationService;
import Services.MappingConfiguration.IMappingConfigurationService;
import Utils.Stereotypes.CapellaRequirementCollection;
import Utils.Stereotypes.HubRequirementCollection;
import Utils.Stereotypes.RequirementType;
import Utils.Stereotypes.StereotypeUtils;
import ViewModels.Rows.MappedHubRequirementRowViewModel;
import ViewModels.Rows.MappedDstRequirementRowViewModel;
import cdp4common.engineeringmodeldata.Iteration;
import cdp4common.engineeringmodeldata.RequirementsGroup;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import cdp4common.sitedirectorydata.Category;
import cdp4common.sitedirectorydata.DomainOfExpertise;

/**
 * The RequirementsSpecificationToRequirementMappingRuleTestFixture is
 */
class RequirementsSpecificationToRequirementMappingRuleTestFixture
{
    private ICapellaMappingConfigurationService mappingConfigurationService;
    private IDstController dstController;
    private IHubController hubController;
    private RequirementsSpecificationToRequirementMappingRule mappingRule;
    private HubRequirementCollection elements;
    private Iteration iteration;
    private RequirementsSpecification requirementsSpecification0;
    private RequirementsSpecification requirementsSpecification1;
    private DomainOfExpertise domain;
    private RequirementsGroup requirementsGroup2;
    private RequirementsGroup requirementsGroup1;
    private RequirementsGroup requirementsGroup0;
    private cdp4common.engineeringmodeldata.Requirement requirement5;
    private cdp4common.engineeringmodeldata.Requirement requirement4;
    private cdp4common.engineeringmodeldata.Requirement requirement3;
    private cdp4common.engineeringmodeldata.Requirement requirement2;
    private cdp4common.engineeringmodeldata.Requirement requirement1;
    private cdp4common.engineeringmodeldata.Requirement requirement0;
    private Category systemNonFunctionalInterfaceRequirementCategory;
    private Category systemNonFunctionalRequirementCategory;
    private Category systemFunctionalRequirementCategory;
    private Category systemFunctionalInterfaceRequirementCategory;
    private ICapellaTransactionService transactionService;

    @BeforeEach
    void Setup()
    {
        this.hubController = mock(IHubController.class);
        this.dstController = mock(IDstController.class);
        this.mappingConfigurationService = mock(ICapellaMappingConfigurationService.class);
        this.transactionService = mock(ICapellaTransactionService.class);
        
        this.SetupElements();
        
        this.mappingRule = new RequirementsSpecificationToRequirementMappingRule(this.hubController, this.mappingConfigurationService, this.transactionService);
        this.mappingRule.dstController = this.dstController;
    }
    
    @Test
    void VerifyTransform()
    {
        when(this.transactionService.Create(any(Class.class), any(String.class), any(CapellaArchitecture.class)))
                    .thenAnswer(x -> 
                    {
                        if(x.getArgument(1, String.class).contains("requirement"))
                        {
                            Class classArgument = x.getArgument(0, Class.class);                            
                            return mock(classArgument);
                        }
                        
                        var packageMocked = mock(RequirementsPkg.class);
                        when(packageMocked.getOwnedRequirements()).thenReturn(new BasicEList<>());
                        when(packageMocked.getOwnedRequirementPkgs()).thenReturn(new BasicEList<>());
                        return packageMocked;
                    });
                    
        assertDoesNotThrow(() -> this.mappingRule.Transform(null));
        assertDoesNotThrow(() -> this.mappingRule.Transform(mock(List.class)));
        var result = this.mappingRule.Transform(this.elements);
        assertEquals(6, result.size());
        assertTrue(result.get(0).GetDstElement() != null);
        var requirement2Container = (RequirementsPkg)result.get(2).GetDstElement().eContainer();
        assertSame(result.get(1).GetDstElement().eContainer(), requirement2Container);
        
        assertTrue(RequirementType.FunctionalInterface.ClassType().isInstance(result.get(0).GetDstElement()));
        assertTrue(RequirementType.NonFunctionalInterface.ClassType().isInstance(result.get(1).GetDstElement()));
        assertTrue(RequirementType.NonFunctional.ClassType().isInstance(result.get(2).GetDstElement()));
        assertTrue(RequirementType.Functional.ClassType().isInstance(result.get(3).GetDstElement()));
        assertTrue(RequirementType.NonFunctionalInterface.ClassType().isInstance(result.get(4).GetDstElement()));
        assertTrue(RequirementType.User.ClassType().isInstance(result.get(5).GetDstElement()));
    }

    private void SetupElements()
    {
        this.elements = new HubRequirementCollection();
        
        this.domain = new DomainOfExpertise(UUID.randomUUID(), null, null);
        this.iteration = new Iteration(UUID.randomUUID(), null, null);
        
        this.requirementsSpecification0 = new RequirementsSpecification();
        this.requirementsSpecification0.setShortName("REQS0");
        this.requirementsSpecification0.setName("REQS0");
        this.requirementsSpecification1 = new RequirementsSpecification();
        this.requirementsSpecification1.setShortName("REQS1");
        this.requirementsSpecification1.setName("REQS1");
        
        this.requirementsGroup0 = new RequirementsGroup();
        this.requirementsGroup0.setName("group0");
        this.requirementsGroup0.setShortName("group0");
        this.requirementsGroup1 = new RequirementsGroup();
        this.requirementsGroup1.setName("group1");
        this.requirementsGroup1.setShortName("group1");
        this.requirementsGroup2 = new RequirementsGroup();
        this.requirementsGroup2.setName("group2");
        this.requirementsGroup2.setShortName("group2");
        
        this.requirementsSpecification0.getGroup().add(requirementsGroup0);
        this.requirementsGroup0.getGroup().add(requirementsGroup1);
        
        this.requirement0 = new cdp4common.engineeringmodeldata.Requirement();
        this.requirement0.setName("requirement0");
        this.requirement1 = new cdp4common.engineeringmodeldata.Requirement();
        this.requirement1.setName("requirement1");
        this.requirement2 = new cdp4common.engineeringmodeldata.Requirement();
        this.requirement2.setName("requirement2");
        this.requirement3 = new cdp4common.engineeringmodeldata.Requirement();
        this.requirement3.setName("requirement3");
        this.requirement4 = new cdp4common.engineeringmodeldata.Requirement();
        this.requirement4.setName("requirement4");
        this.requirement5 = new cdp4common.engineeringmodeldata.Requirement();
        this.requirement5.setName("requirement5");
        
        this.systemNonFunctionalRequirementCategory = new Category();
        this.systemNonFunctionalRequirementCategory.setName("SystemNonFunctionalRequirement");        
        this.systemNonFunctionalInterfaceRequirementCategory = new Category();
        this.systemNonFunctionalInterfaceRequirementCategory.setName("SystemNonFunctionalInterfaceRequirement");        
        this.systemFunctionalInterfaceRequirementCategory = new Category();
        this.systemFunctionalInterfaceRequirementCategory.setName("SystemFunctionalInterfaceRequirement");        
        this.systemFunctionalRequirementCategory = new Category();
        this.systemFunctionalRequirementCategory.setName("SystemFunctionalRequirement");

        this.requirement0.getCategory().add(this.systemFunctionalInterfaceRequirementCategory);
        this.requirement1.getCategory().add(this.systemNonFunctionalInterfaceRequirementCategory);
        this.requirement2.getCategory().add(this.systemNonFunctionalRequirementCategory);
        this.requirement3.getCategory().add(this.systemFunctionalRequirementCategory);
        this.requirement3.getCategory().add(this.systemNonFunctionalInterfaceRequirementCategory);
        
        this.requirement0.setGroup(this.requirementsGroup1);
        this.requirement1.setGroup(this.requirementsGroup2);
        this.requirement2.setGroup(this.requirementsGroup2);
        
        this.requirementsSpecification0.getRequirement().add(this.requirement0);
        this.requirementsSpecification1.getRequirement().add(this.requirement1);
        this.requirementsSpecification1.getRequirement().add(this.requirement2);
        this.requirementsSpecification1.getRequirement().add(this.requirement3);
        this.requirementsSpecification1.getRequirement().add(this.requirement4);
        this.requirementsSpecification1.getRequirement().add(this.requirement5);
        
        this.iteration.getRequirementsSpecification().add(this.requirementsSpecification0);
        this.iteration.getRequirementsSpecification().add(this.requirementsSpecification1);
                
        this.elements.add(new MappedHubRequirementRowViewModel(this.requirement0, MappingDirection.FromHubToDst));
        this.elements.add(new MappedHubRequirementRowViewModel(this.requirement1, MappingDirection.FromHubToDst));
        this.elements.add(new MappedHubRequirementRowViewModel(this.requirement2, MappingDirection.FromHubToDst));
        this.elements.add(new MappedHubRequirementRowViewModel(this.requirement3, MappingDirection.FromHubToDst));
        
        this.elements.add(new MappedHubRequirementRowViewModel(this.requirement4, 
                new CapellaTransactionService(null).Create(SystemNonFunctionalInterfaceRequirement.class), MappingDirection.FromHubToDst));
        
        this.elements.add(new MappedHubRequirementRowViewModel(this.requirement5, MappingDirection.FromHubToDst));
    }
}
