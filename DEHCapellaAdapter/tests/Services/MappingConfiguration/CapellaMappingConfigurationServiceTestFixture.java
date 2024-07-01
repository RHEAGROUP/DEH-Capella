/*
 * CapellaMappingConfigurationServiceTestFixture.java
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
package Services.MappingConfiguration;

import static org.mockito.Mockito.*;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.polarsys.capella.core.data.capellacore.CapellaElement;

import Enumerations.MappingDirection;
import HubController.IHubController;
import Services.CapellaSession.CapellaSessionRelatedBaseTestFixture;
import Services.CapellaSession.ICapellaSessionService;
import Services.CapellaTransaction.ICapellaTransactionService;
import Utils.Ref;
import ViewModels.Interfaces.IMappedElementRowViewModel;
import cdp4common.commondata.*;
import cdp4common.engineeringmodeldata.ElementDefinition;
import cdp4common.engineeringmodeldata.RequirementsSpecification;
import io.reactivex.Observable;

public class CapellaMappingConfigurationServiceTestFixture extends CapellaSessionRelatedBaseTestFixture
{
    private IHubController hubController;
    private CapellaMappingConfigurationService service;
    private ICapellaSessionService sessionService;
    private URI sessionUri;
    private ElementDefinition elementDefinition0;
    private cdp4common.engineeringmodeldata.Requirement requirement;
    private ICapellaTransactionService transactionService;

    /**
     * @throws java.lang.Exception
     */
    @BeforeEach
    public void setUp() throws Exception
    {
        this.hubController = mock(IHubController.class);
        when(this.hubController.GetIsSessionOpenObservable()).thenReturn(Observable.fromArray(true, false));
        this.elementDefinition0 = new ElementDefinition(UUID.randomUUID(), null, null);
        this.requirement = new cdp4common.engineeringmodeldata.Requirement(UUID.randomUUID(), null, null);
        var requirementsSpecification = new RequirementsSpecification();
        requirementsSpecification.getRequirement().add(this.requirement);
                
        
        this.sessionService = mock(ICapellaSessionService.class);
        this.transactionService = mock(ICapellaTransactionService.class);
        this.service = new CapellaMappingConfigurationService(this.hubController, this.sessionService, this.transactionService);
    }

    @Test
    public void VerifyLoadMapping()
    {
        assertDoesNotThrow(() -> this.service.LoadMapping(new ArrayList()));

        this.sessionUri = URI.createURI("t.e.s.t");
        var session = this.GetSession(this.sessionUri);
        var elements = this.GetSessionElements(session, CapellaElement.class);
        
        var sessionAndObjectsMap = new HashMap<URI, List<CapellaElement>>();
        
        sessionAndObjectsMap.putIfAbsent(this.sessionUri, elements);
        
        when(this.sessionService.GetAllCapellaElementsFromOpenSessions()).thenReturn(sessionAndObjectsMap);
        
        var result = new Ref<Collection<IMappedElementRowViewModel>>(null);
        assertDoesNotThrow(() -> result.Set(this.service.LoadMapping()));
        assertTrue(result.Get().isEmpty());
        
        var componentExternalId0 = new CapellaExternalIdentifier();
        componentExternalId0.Identifier = this.LogicalComponentId;
        componentExternalId0.MappingDirection = MappingDirection.FromDstToHub;
        
        var componentExternalId1 = new CapellaExternalIdentifier();
        componentExternalId1.Identifier = UUID.randomUUID().toString();
        componentExternalId1.MappingDirection = MappingDirection.FromHubToDst;
                
        var requirementExternalId = new CapellaExternalIdentifier();
        requirementExternalId.Identifier = this.UserRequirementId;
        requirementExternalId.MappingDirection = MappingDirection.FromDstToHub;
        
        this.service.correspondences.add(MutableTriple.of(UUID.randomUUID(), requirementExternalId, this.requirement.getIid()));
        this.service.correspondences.add(MutableTriple.of(UUID.randomUUID(), componentExternalId1, this.elementDefinition0.getIid()));

        when(this.hubController.TryGetThingById(any(UUID.class), any(Ref.class))).thenAnswer(new Answer<Boolean>() 
        {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable 
            {
                var arguments = invocation.getArguments();
                var thing = (Thing)(((UUID)arguments[0]).equals(elementDefinition0.getIid()) ? elementDefinition0 : requirement);
                ((Ref<Thing>)arguments[1]).Set(thing);
                return true;
            }});
        
        assertDoesNotThrow(() -> result.Set(this.service.LoadMapping()));
        assertFalse(result.Get().isEmpty());
        assertEquals(1, result.Get().size());
        this.service.correspondences.add(MutableTriple.of(UUID.randomUUID(), componentExternalId0, this.elementDefinition0.getIid()));
        assertDoesNotThrow(() -> result.Set(this.service.LoadMapping()));
        assertFalse(result.Get().isEmpty());
        assertEquals(2, result.Get().size());
    }

    @Test
    public void VerifyCreateExternalIdentifierMap()
    {
        final var name = "name";
        final var modelName = "modelName";
        var newMappingConfiguration = this.service.CreateExternalIdentifierMap(name, modelName, true);
        
        assertEquals(name, newMappingConfiguration.getName());
        assertEquals(modelName, newMappingConfiguration.getExternalModelName());
        assertEquals(DstController.DstController.THISTOOLNAME, newMappingConfiguration.getExternalToolName());
    }
}
