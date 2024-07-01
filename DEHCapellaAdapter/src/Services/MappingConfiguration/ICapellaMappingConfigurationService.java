/*
 * ICapellaMappingConfigurationService.java
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

import java.util.Collection;
import java.util.UUID;

import Enumerations.CapellaArchitecture;
import Enumerations.MappingDirection;
import ViewModels.Interfaces.IMappedElementRowViewModel;
import cdp4common.engineeringmodeldata.ExternalIdentifierMap;

/**
 * The {@linkplain ICapellaMappingConfigurationService} is the main interface definition for the {@linkplain CapellaMappingConfigurationService}
 */
public interface ICapellaMappingConfigurationService extends IMappingConfigurationService<CapellaExternalIdentifier>
{
    /**
     * Loads the mapping configuration and generates the map result respectively
     * 
     * @return a {@linkplain Collection} of {@linkplain IMappedElementRowViewModel}
     */
    Collection<IMappedElementRowViewModel> LoadMapping();

    /**
     * Adds one correspondence to the {@linkplain ExternalIdentifierMap}
     * 
     * @param internalId the {@linkplain UUID} that identifies the thing to correspond to
     * @param externalId the {@linkplain Object} that identifies the object to correspond to
     * @param mappingDirection the {@linkplain MappingDirection} the mapping belongs to
     * @param targetArchitecture the target {@linkplain CapellaArchitecture}
     */
    void AddToExternalIdentifierMap(UUID internalId, String externalId, CapellaArchitecture targetArchitecture,
            MappingDirection mappingDirection);
}
