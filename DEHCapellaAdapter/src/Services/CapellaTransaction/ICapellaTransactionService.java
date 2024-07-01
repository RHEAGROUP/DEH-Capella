/*
 * ICapellaTransactionService.java
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
package Services.CapellaTransaction;

import java.util.Collection;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.EnumerationPropertyType;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.cs.BlockArchitecture;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.information.Unit;
import org.polarsys.capella.core.data.information.datatype.DataType;
import org.polarsys.capella.core.data.information.datatype.PhysicalQuantity;
import org.polarsys.capella.core.data.la.LogicalComponent;
import org.polarsys.capella.core.data.pa.PhysicalComponent;
import org.polarsys.capella.core.model.helpers.BlockArchitectureExt.Type;

import Enumerations.CapellaArchitecture;

/**
 * The ICapellaTransactionService is the interface definition for {@linkplain CapellaTransactionService}
 */
public interface ICapellaTransactionService
{
    /**
     * Verifies that the provided {@linkplain #TElement} is a clone
     * 
     * @param <TElement> the type of the element
     * @param element the {@linkplain #TElement} to check
     * @return an assert
     */
    <TElement extends EObject> boolean IsCloned(TElement element);

    /**
     * Clones the original and returns the clone or returns the clone if it already exist
     * 
     * @param <TElement> the type of the original {@linkplain CapellaElement}
     * @param original the original {@linkplain #TElement}
     * @return a clone of the {@linkplain #original}
     */
    <TElement extends CapellaElement> TElement Clone(TElement original);

    /**
     * Gets a read only {@linkplain Collection} of the clones reference of type {@linkplain #TElement}
     *  
     * @param <TElement> the type of element to retrieve
     * @param clazz the {@linkplain Class} type of element 
     * @return a {@linkplain Collection} of {@linkplain String} UUID and {@linkplain CapellaReferenceElement}
     */
    <TElement extends CapellaElement> Collection<ClonedReferenceElement<? extends CapellaElement>> GetClones(Class<TElement> clazz);

    /**
     * Gets a read only {@linkplain Collection} of the clones reference
     * 
     * @return a {@linkplain Collection} of {@linkplain String} UUID and {@linkplain CapellaReferenceElement}
     */
    Map<String, ClonedReferenceElement<? extends CapellaElement>> GetClones();

    /**
     * Reset the clones references, the new ones and the registered target architecture
     */
    void Reset();

    /**
     * Adds the provided {@linkplain Unit} to the {@linkplain DataPackage} of the current project
     * 
     * @param newUnit the new {@linkplain Unit}
     */
    void AddReferenceDataToDataPackage(Unit newUnit);

    /**
     * Adds the provided {@linkplain DataType} to the {@linkplain DataPackage} of the current project
     * 
     * @param newDataType the new {@linkplain DataType}
     */
    void AddReferenceDataToDataPackage(DataType newDataType);

    /**
     * Adds the provided {@linkplain EnumerationPropertyType} to the {@linkplain DataPackage} of the current project
     * 
     * @param newDataType the new {@linkplain DataType}
     */
    void AddReferenceDataToDataPackage(EnumerationPropertyType enumerationPropertyType);

    /**
     * Gets the {@linkplain ClonedReferenceElement} where the element id == the provided {@linkplain #TElement} id
     * 
     * @param <TElement> the type of the element
     * @param element the element
     * @return a {@linkplain ClonedReferenceElement} of type {@linkplain #TElement}
     */
    <TElement extends CapellaElement> ClonedReferenceElement<TElement> GetClone(TElement element);

    /**
     * Commits the provided transaction
     * 
     * @param transactionMethod the {@linkplain Runnable} to execute inside the transaction
     * @return a value indicating whether the operation succeed
     */
    boolean Commit(Runnable transactionMethod);

    /**
     * Verifies that the provided {@linkplain #TElement} is a clone or a new element
     * 
     * @param <TElement> the type of the element
     * @param element the {@linkplain #TElement} to check
     * @return an assert
     */
    <TElement extends EObject> boolean IsClonedOrNew(TElement element);

    /**
     * Initializes a new {@linkplain NamedElement} from the specified {@linkplain #Class}, also sets the name of the {@linkplain NamedElement} if the element does not exist yet
     * 
     * @param <TInstance> the {@linkplain Type} of {@linkplain CapellaElement}
     * @param clazz the {@linkplain Class} of {@linkplain #TInstance}
     * @param name the name of the newly created {@linkplain CapellaElement}, used to query the {@linkplain #newReferences} collection
     * @return an instance of the provided type
     */
    <TInstance extends NamedElement> TInstance Create(Class<TInstance> clazz, String name);

    /**
     * Initializes a new {@linkplain CapellaElement} from the specified {@linkplain #Class}
     * 
     * @param <TInstance> the {@linkplain Type} of {@linkplain CapellaElement}
     * @param clazz the {@linkplain Class} of {@linkplain #TInstance}
     * @return an instance of the provided type
     */
    <TInstance extends CapellaElement> TInstance Create(Class<TInstance> clazz);

    /**
     * Gets the {@linkplain CapellaElement} where the element id == the provided id
     * 
     * @param <TElement> the type of the element
     * @param id the {@linkplain String} id of the queried element
     * @param elementType the {@linkplain Class} of the queried element
     * @return a {@linkplain ClonedReferenceElement} of type {@linkplain #TElement}
     */
    <TElement extends CapellaElement> TElement GetNew(String id, Class<TElement> elementType);

    /**
     * Register the target {@linkplain CapellaArchitecture} for the specified {@linkplain CapellaElement}
     * 
     * @param capellaElement the {@linkplain CapellaElement}
     * @param targetArchitecture the target {@linkplain CapellaArchitecture}
     */
    void RegisterTargetArchitecture(CapellaElement capellaElement, CapellaArchitecture targetArchitecture);

    /**
     * Gets the registered target {@linkplain CapellaArchitecture} for the specified {@linkplain CapellaElement}
     * 
     * @param capellaElement the {@linkplain CapellaElement}
     * @return the {@linkplain CapellaArchitecture}
     */
    CapellaArchitecture GetTargetArchitecture(CapellaElement capellaElement);

    /**
     * Initializes a new {@linkplain CapellaElement} from the specified {@linkplain #Class}, and registers the target {@linkplain CapellaArchitecture}
     * 
     * @param <TInstance> the {@linkplain Type} of {@linkplain CapellaElement}
     * @param clazz the {@linkplain Class} of {@linkplain #TInstance}
     * @param name the name of the newly created {@linkplain CapellaElement}, used to query the {@linkplain #newReferences} collection
     * @param targetArchitecture the {@linkplain CapellaArchitecture} to register for the new element
     * @return an instance of the provided type
     */
    <TInstance extends NamedElement> TInstance Create(Class<TInstance> clazz, String name, CapellaArchitecture targetArchitecture);

    /**
     *  Verifies that the provided {@linkplain #TElement} is a new element
     *  
     * @param <TElement> the type of the element
     * @param element the {@linkplain #TElement} to check
     * @return an assert
     */
    <TElement extends CapellaElement> boolean IsNew(TElement element);
    
    /**
     * Gets the original reference from the {@linkplain ClonedReferenceElement} where the element id == the provided {@linkplain #TElement} id.
     * In the case the provided element is not a clone, it is returned.
     * 
     * @param <TElement> the type of the element
     * @param element the element
     * @return a {@linkplain #TElement}
     */
    <TElement extends CapellaElement> TElement GetOriginal(TElement element);
}
