/*
 * IDstController.java
 *
 * Copyright (c) 2020-2024 Starion Group S.A.
 *
 * Author: Sam Gerené, Alex Vorobiev, Nathanael Smiechowski 
 *
 * This file is part of DEH-MDSYSML
 *
 * The DEH-MDSYSML is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * The DEH-MDSYSML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package DstController;

import java.util.Collection;
import java.util.function.Predicate;

import org.apache.commons.lang3.tuple.Pair;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.EnumerationPropertyType;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.capellacore.Trace;
import org.polarsys.capella.core.data.information.datatype.DataType;

import Enumerations.MappingDirection;
import Reactive.ObservableCollection;
import Services.MappingEngineService.IMappableThingCollection;
import Utils.Ref;
import ViewModels.Rows.MappedElementRowViewModel;
import cdp4common.commondata.DefinedThing;
import cdp4common.commondata.NamedThing;
import cdp4common.commondata.ShortNamedThing;
import cdp4common.commondata.Thing;
import cdp4common.engineeringmodeldata.BinaryRelationship;
import cdp4common.sitedirectorydata.EnumerationParameterType;
import cdp4common.sitedirectorydata.MeasurementScale;
import io.reactivex.Observable;

/**
 * The {@linkplain IDstController} is the interface definition for the {@linkplain DstController}
 */
public interface IDstController extends IDstControllerBase<NamedElement>
{
    /**
     * Gets the {@linkplain ObservableCollection} of mapped {@linkplain Trace}s
     * 
     * @return a {@linkplain ObservableCollection} of mapped {@linkplain Trace}s
     */
    ObservableCollection<Trace> GetMappedBinaryRelationshipsToTraces();
    
    /**
     * Gets the {@linkplain ObservableCollection} of mapped {@linkplain BinaryRelationship}s
     * 
     * @return a {@linkplain ObservableCollection} of mapped {@linkplain BinaryRelationship}s
     */
    ObservableCollection<BinaryRelationship> GetMappedTracesToBinaryRelationships();
    
    /**
     * Transfers all the {@linkplain Thing} contained in the {@linkplain dstMapResult} to the Hub
     * 
     * @return a pair of value where one indicates that all transfer could be completed and 
     * the other one indicates whether the mapping configuration should be persisted
     */
    Pair<Boolean, Boolean> TransferToHub();

    /**
     * Transfers the selected things to be transfered depending on the current {@linkplain MappingDirection}
     * 
     * @return a value indicating that all transfer could be completed
     */
    boolean Transfer();

    /**
     * Maps the {@linkplain input} by calling the {@linkplain IMappingEngine}
     * and assign the map result to the dstMapResult or the hubMapResult
     * 
     * @param input the {@linkplain IMappableThingCollection} in other words the  {@linkplain Collection} of {@linkplain Object} to map
     * @param mappingDirection the {@linkplain MappingDirection} towards the {@linkplain IMappableThingCollection} maps to
     * @return a {@linkplain boolean} indicating whether the mapping operation went well
     */
    boolean Map(IMappableThingCollection input, MappingDirection mappingDirection);

    /**
     * Loads the saved mapping and applies the mapping rule to the loaded things
     */
    void LoadMapping();

    /**
     * Switches the {@linkplain MappingDirection}
     * 
     * @return the new {@linkplain MappingDirection}
     */
    MappingDirection ChangeMappingDirection();

    /**
     * Gets the current {@linkplain MappingDirection} from {@linkplain currentMappingDirection}
     * 
     * @return the {@linkplain MappingDirection}
     */
    MappingDirection CurrentMappingDirection();

    /**
     * Gets the {@linkplain Observable} of {@linkplain MappingDirection} from {@linkplain currentMappingDirection}
     * 
     * @return a {@linkplain Observable} of {@linkplain MappingDirection}
     */
    Observable<MappingDirection> GetMappingDirection();

    /**
     * Gets the {@linkplain ObservableCollection} of {@linkplain Thing} that are selected for transfer to the Hub
     * 
     * @return an {@linkplain ObservableCollection} of {@linkplain Thing}
     */
    ObservableCollection<Thing> GetSelectedDstMapResultForTransfer();

    /**
     * Gets the {@linkplain ObservableCollection} of that are selected for transfer to the 
     * 
     * @return an {@linkplain ObservableCollection} of {@linkplain CapellaElement}
     */
    ObservableCollection<CapellaElement> GetSelectedHubMapResultForTransfer();

    /**
     * Gets The {@linkplain ObservableCollection} of DST map result
     * 
     * @return an {@linkplain ObservableCollection} of {@linkplain MappedElementRowViewModel}
     */
    ObservableCollection<MappedElementRowViewModel<DefinedThing, NamedElement>> GetDstMapResult();

    /**
     * Gets The {@linkplain ObservableCollection} of Hub map result
     * 
     * @return an {@linkplain ObservableCollection} of {@linkplain MappedElementRowViewModel}
     */
    ObservableCollection<MappedElementRowViewModel<DefinedThing, NamedElement>> GetHubMapResult();

    /**
     * Gets an {@linkplain Observable} of value indicating whether there is any session open in Capella
     * 
     * @return {@linkplain Observable} of {@linkplain Boolean} 
     */
    Observable<Boolean> HasAnyOpenSessionObservable();
    
    /**
     * Tries to get the corresponding element that answer to the provided {@linkplain Predicate}
     * 
     * @param <TElement> the type of {@linkplain CapellaElement} to query
     * @param predicate the {@linkplain Predicate} to verify in order to match the element
     * @param refElement the {@linkplain Ref} of {@linkplain #TElement}
     * @return a value indicating whether the {@linkplain CapellaElement} has been found
     */
    <TElement extends CapellaElement> boolean TryGetElementBy(Predicate<? super CapellaElement> predicate, Ref<TElement> refElement);
    
    /**
     * Tries to get the corresponding element that has the provided Id
     * 
     * @param <TElement> the type of {@linkplain CapellaElement} to query
     * @param elementId the {@linkplain String} id of the searched element
     * @param refElement the {@linkplain Ref} of {@linkplain #TElement}
     * @return a value indicating whether the {@linkplain CapellaElement} has been found
     */
    <TElement extends CapellaElement> boolean TryGetElementById(String elementId, Ref<TElement> refElement);
    
    /**
     * Tries to get the corresponding element based on the provided {@linkplain DefinedThing} name or short name. 
     * 
     * @param <TElement> the type of {@linkplain CapellaElement} to query
     * @param thing the {@linkplain DefinedThing} that can potentially match a {@linkplain #TElement} 
     * @param refElement the {@linkplain Ref} of {@linkplain #TElement}
     * @return a value indicating whether the {@linkplain CapellaElement} has been found
     */
    <TElement extends CapellaElement> boolean TryGetElementByName(DefinedThing thing, Ref<TElement> refElement);

    /**
     * Tries to get a {@linkplain DataType} that matches the provided {@linkplain MeasurementScale}
     * 
     * @param <TThing> the type of {@linkplain Thing} that is {@linkplain NamedThing} and {@linkplain ShortNamedThing}
     * @param thing the {@linkplain #TThing} of reference
     * @param referenceElement a {@linkplain CapellaElement} that will point to the right session
     * @param refDataType the {@linkplain Ref} of {@linkplain DataType}
     * @return a {@linkplain boolean}
     */
    <TThing extends NamedThing & ShortNamedThing> boolean TryGetDataType(TThing thing, CapellaElement referenceElement, Ref<DataType> refDataType);

    /**
     * Tries to get a {@linkplain EnumerationPropertyType} that matches the provided {@linkplain EnumerationParameterType}
     * 
     * @param thing the {@linkplain #TThing} of reference
     * @param referenceElement a {@linkplain CapellaElement} that will point to the right session
     * @param refDataType the {@linkplain Ref} of {@linkplain DataType}
     * @return a {@linkplain boolean}
     */
    boolean TryGetEnumerationPropertyType(EnumerationParameterType thing, CapellaElement referenceElement, Ref<EnumerationPropertyType> refDataType);

    /**
     * Gets a value indicating whether there is any session open in Capella
     * 
     * @return a {@linkplain Boolean} 
     */
    boolean HasAnyOpenSession();
}
