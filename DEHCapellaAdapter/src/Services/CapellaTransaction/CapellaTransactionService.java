/*
 * CapellaTransactionService.java
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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.polarsys.capella.common.ef.command.AbstractReadWriteCommand;
import org.polarsys.capella.common.helpers.TransactionHelper;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.EnumerationPropertyType;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.information.DataPkg;
import org.polarsys.capella.core.data.information.Unit;
import org.polarsys.capella.core.data.information.datatype.DataType;
import org.polarsys.capella.core.model.helpers.BlockArchitectureExt;
import org.polarsys.capella.core.model.helpers.BlockArchitectureExt.Type;

import Enumerations.CapellaArchitecture;
import Services.CapellaSession.ICapellaSessionService;
import Utils.Ref;
import Utils.Stereotypes.StereotypeUtils;

/**
 * The CapellaTransactionService is a service that takes care of clones and transactions in Capella
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class CapellaTransactionService implements ICapellaTransactionService
{
    /**
     * The current class logger
     */
    private final Logger Logger = LogManager.getLogger();
    
    /**
     * The {@linkplain ICapellaSessionService}
     */
    private final ICapellaSessionService sessionService;
    
    /**
     * Backing field for {@linkplain #GetClones(Class)} and {@linkplain #GetClones()}
     */
    private HashMap<String, ClonedReferenceElement<? extends CapellaElement>> cloneReferences = new HashMap<>();
    
    /**
     * Holds the newly created {@linkplain CapellaElement} for future reference such as in {@linkplain #IsClonedOrNew(EObject)}, {@linkplain #GetNew(String, Class)}
     */
    private HashMap<String, CapellaElement> newReferences = new HashMap<>();

    /**
     * Holds the associated target {@linkplain CapellaArchitecture} by {@linkplain CapellaElement} id. Typically {@linkplain Requirement}
     */
    private HashMap<String, CapellaArchitecture> registeredTargetArchitecture = new HashMap<>();
    
    /**
     * Gets a read only {@linkplain Collection} of the clones reference
     * 
     * @return a {@linkplain Collection} of {@linkplain String} UUID and {@linkplain CapellaReferenceElement}
     */
    @Override
    public Map<String, ClonedReferenceElement<? extends CapellaElement>> GetClones()
    {
        return Collections.unmodifiableMap(cloneReferences);
    }
    
    /**
     * Gets a read only {@linkplain Collection} of the clones reference of type {@linkplain #TElement}
     *  
     * @param <TElement> the type of element to retrieve
     * @param clazz the {@linkplain Class} type of element 
     * @return a {@linkplain Collection} of {@linkplain String} UUID and {@linkplain CapellaReferenceElement}
     */
    @Override
    public <TElement extends CapellaElement> Collection<ClonedReferenceElement<? extends CapellaElement>> GetClones(Class<TElement> clazz)
    {
        return Collections.unmodifiableCollection(cloneReferences.values().stream()
                .filter(x -> clazz.isAssignableFrom(x.GetOriginal().getClass()))
                .collect(Collectors.toList()));
    }
    
    /**
     * Initializes a new {@linkplain CapellaTransactionService}
     * 
     * @param sessionService the {@linkplain ICapellaSessionService}
     */
    public CapellaTransactionService(ICapellaSessionService sessionService)
    {
        this.sessionService = sessionService;
    }
    
    /**
     * Gets the {@linkplain ClonedReferenceElement} where the element id == the provided {@linkplain #TElement} id
     * 
     * @param <TElement> the type of the element
     * @param element the element
     * @return a {@linkplain ClonedReferenceElement} of type {@linkplain #TElement}
     */
    @SuppressWarnings("unchecked")
    public <TElement extends CapellaElement> ClonedReferenceElement<TElement> GetClone(TElement element)
    {
        return (ClonedReferenceElement<TElement>) this.cloneReferences.get(element.getId());
    }

    /**
     * Gets the {@linkplain CapellaElement} where the element id == the provided id
     * 
     * @param <TElement> the type of the element
     * @param id the {@linkplain String} id of the queried element
     * @param elementType the {@linkplain Class} of the queried element
     * @return a {@linkplain ClonedReferenceElement} of type {@linkplain #TElement}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <TElement extends CapellaElement> TElement GetNew(String id, Class<TElement> elementType)
    {
        return (TElement) this.newReferences.get(id);
    }
    
    /**
     * Clones the original and returns the clone or returns the clone if it already exist
     * 
     * @param <TElement> the type of the original {@linkplain CapellaElement}
     * @param original the original {@linkplain #TElement}
     * @return a clone of the {@linkplain #original}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <TElement extends CapellaElement> TElement Clone(TElement original)
    {
        if(original == null)
        {
            return null;
        }
        
        if(this.cloneReferences.containsKey(original.getId()))
        {
            return (TElement) this.cloneReferences.get(original.getId()).GetClone();
        }
        else
        {
            var clonedReference = new ClonedReferenceElement<TElement>(original);
            this.cloneReferences.put(original.getId(), clonedReference);
            return clonedReference.GetClone();
        }
    }
    
    /**
     * Verifies that the provided {@linkplain #TElement} is a clone
     * 
     * @param <TElement> the type of the element
     * @param element the {@linkplain #TElement} to check
     * @return an assert
     */
    @Override
    public <TElement extends EObject> boolean IsCloned(TElement element)
    {
        if(!(element instanceof CapellaElement))
        {
            return false;
        }
        
        return this.cloneReferences.containsKey(((CapellaElement)element).getId()) 
                && this.cloneReferences.get(((CapellaElement)element).getId()).GetClone() == element;
    }    

    /**
     * Verifies that the provided {@linkplain #TElement} is a clone or a new element
     * 
     * @param <TElement> the type of the element
     * @param element the {@linkplain #TElement} to check
     * @return an assert
     */
    public <TElement extends EObject> boolean IsClonedOrNew(TElement element)
    {
        if(!(element instanceof CapellaElement))
        {
            return false;
        }
        
        if(this.IsCloned(element)) 
        {
            return true;
        }

        return this.IsNew((CapellaElement)element);
    }

    /**
     *  Verifies that the provided {@linkplain #TElement} is a new element
     *  
     * @param <TElement> the type of the element
     * @param element the {@linkplain #TElement} to check
     * @return an assert
     */
    @Override
    public <TElement extends CapellaElement> boolean IsNew(TElement element)
    {
        return this.newReferences.containsKey(((CapellaElement)element).getId())
                && this.newReferences.get(((CapellaElement)element).getId()) == element;
    }

    /**
     * Gets the original reference from the {@linkplain ClonedReferenceElement} where the element id == the provided {@linkplain #TElement} id.
     * In the case the provided element is not a clone, it is returned.
     * 
     * @param <TElement> the type of the element
     * @param element the element
     * @return a {@linkplain #TElement}
     */
    public <TElement extends CapellaElement> TElement GetOriginal(TElement element)
    {
        return this.IsCloned(element) ? this.GetClone(element).GetOriginal() : element;
    }
    
    /**
     * Initializes a new {@linkplain CapellaElement} from the specified {@linkplain #Class}, and registers the target {@linkplain CapellaArchitecture}
     * 
     * @param <TInstance> the {@linkplain Type} of {@linkplain CapellaElement}
     * @param clazz the {@linkplain Class} of {@linkplain #TInstance}
     * @param name the name of the newly created {@linkplain CapellaElement}, used to query the {@linkplain #newReferences} collection
     * @param targetArchitecture the {@linkplain CapellaArchitecture} to register for the new element
     * @return an instance of the provided type
     */
    @Override
    public <TInstance extends NamedElement> TInstance Create(Class<TInstance> clazz, String name, CapellaArchitecture targetArchitecture)
    {
        var newElement = this.Create(clazz, name);
        this.RegisterTargetArchitecture(newElement, targetArchitecture);
        return newElement;
    }

    /**
     * Initializes a new {@linkplain CapellaElement} from the specified {@linkplain #Class}
     * 
     * @param <TInstance> the {@linkplain Type} of {@linkplain CapellaElement}
     * @param clazz the {@linkplain Class} of {@linkplain #TInstance}
     * @param name the name of the newly created {@linkplain CapellaElement}, used to query the {@linkplain #newReferences} collection
     * @return an instance of the provided type
     */
    @Override
    public <TInstance extends NamedElement> TInstance Create(Class<TInstance> clazz, String name)
    {        
        var newElement = this.Create(clazz);
        
        if(newElement != null)
        {
            newElement.setName(name);
        }
        
        return newElement;
    }
    
    /**
     * Initializes a new {@linkplain CapellaElement} from the specified {@linkplain #Class}
     * 
     * @param <TInstance> the {@linkplain Type} of {@linkplain CapellaElement}
     * @param clazz the {@linkplain Class} of {@linkplain #TInstance}
     * @return an instance of the provided type
     */
    @Override
    public <TInstance extends CapellaElement> TInstance Create(Class<TInstance> clazz)
    {
        var eClassAndFactory = StereotypeUtils.GetEClassAndFactory(clazz.getSimpleName());
        
        if (eClassAndFactory.getLeft() != null && eClassAndFactory.getRight() != null && eClassAndFactory.getLeft() instanceof EClass) 
        {
            var reference = clazz.cast(eClassAndFactory.getRight().create((EClass)eClassAndFactory.getLeft()));
            this.newReferences.put(reference.getId(), reference);
            return reference;
        }
        
        this.Logger.error(String.format("Could not initialize the tpe [%s]", clazz.getSimpleName()));
        return null;
    }    

    /**
     * Gets the registered target {@linkplain CapellaArchitecture} for the specified {@linkplain CapellaElement}
     * 
     * @param capellaElement the {@linkplain CapellaElement}
     * @return the {@linkplain CapellaArchitecture}
     */
    @Override
    public CapellaArchitecture GetTargetArchitecture(CapellaElement capellaElement)
    {
        return this.registeredTargetArchitecture.get(capellaElement.getId());
    }    

    /**
     * Register the target {@linkplain CapellaArchitecture} for the specified {@linkplain CapellaElement}
     * 
     * @param capellaElement the {@linkplain CapellaElement}
     * @param targetArchitecture the target {@linkplain CapellaArchitecture}
     */
    @Override
    public void RegisterTargetArchitecture(CapellaElement capellaElement, CapellaArchitecture targetArchitecture)
    {
        this.registeredTargetArchitecture.putIfAbsent(capellaElement.getId(), targetArchitecture);
    }
    
    /**
     * Reset the clones references, the new ones and the registered target architecture
     */
    @Override
    public void Reset()
    {
        this.cloneReferences.clear();
        this.newReferences.clear();
        this.registeredTargetArchitecture.clear();
    }
    
    /**
     * Adds the provided {@linkplain DataType} to the {@linkplain DataPackage} of the current project
     * 
     * @param newDataType the new {@linkplain DataType}
     */
    @Override
    public void AddReferenceDataToDataPackage(DataType newDataType)
    {
        this.AddReferenceDataToDataPackage(x -> x.getOwnedDataTypes(), newDataType);
    }
   
    /**
     * Adds the provided {@linkplain Unit} to the {@linkplain DataPackage} of the current project
     * 
     * @param newUnit the new {@linkplain Unit}
     */
    @Override
    public void AddReferenceDataToDataPackage(Unit newUnit)
    {
        this.AddReferenceDataToDataPackage(x -> x.getOwnedUnits(), newUnit);
    }

    /**
     * Adds the provided {@linkplain EnumerationPropertyType} to the {@linkplain DataPackage} of the current project
     * 
     * @param newDataType the new {@linkplain DataType}
     */
    @Override
    public void AddReferenceDataToDataPackage(EnumerationPropertyType enumerationPropertyType)
    {
        this.AddReferenceDataToDataPackage(x -> x.getOwnedEnumerationPropertyTypes(), enumerationPropertyType);
    }
    
    /**
     * Applies the provided {@linkplain Function} in order to add the {@linkplain #TElement} to the right collection
     * 
     * @param <TElement> the type of {@linkplain NamedElement} to add to the data package
     * @param getDataPackageElementCollectionFunction the {@linkplain Function} that takes a {@linkplain DataPkg} as parameter and return a {@linkplain EList} of {@linkplain #TElement}
     * @param newElement the new {@linkplain #TElement} to add
     */
    private <TElement extends NamedElement> void AddReferenceDataToDataPackage(Function<DataPkg, EList<TElement>> getDataPackageElementCollectionFunction, TElement newElement)
    {
        var project = this.sessionService.GetProject(this.sessionService.GetCurrentSession());
        
        TransactionHelper.getExecutionManager(project).execute(new AbstractReadWriteCommand()
        {
            @Override
            public void run()
            {
                var architecture = BlockArchitectureExt.getBlockArchitecture(Type.SA, project);
                var dataPackage = BlockArchitectureExt.getDataPkg(architecture, true);
                getDataPackageElementCollectionFunction.apply(dataPackage).add(newElement);
                Logger.info(String.format("%s %s has been added to %s", newElement.getClass().getSimpleName(), newElement.getName(), dataPackage.getName()));
            }
        });
    }

    /**
     * Commits the provided transaction
     * 
     * @param transactionMethod the {@linkplain Runnable} to execute inside the transaction
     * @return a value indicating whether the operation succeed
     */
    @Override
    public boolean Commit(Runnable transactionMethod)
    {
        this.Logger.info("Begin commiting transaction to Capella");
        var project = this.sessionService.GetProject();
        var result = new Ref<>(Boolean.class, false);
        TransactionHelper.getExecutionManager(project).execute(new CapellaTransaction(transactionMethod, result));
        this.Reset();
        this.Logger.info("End commiting transaction to Capella");
        return result.Get();
    }
}
