/*
 * CapellaSessionRelatedBaseTestFixture.java
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
package Services.CapellaSession;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.AbstractTreeIterator;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.sirius.business.api.session.Session;
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

/**
 * The CapellaSessionSetupUtils is utility class for unit test that needs to access capella {@linkplain Session} object
 */
public abstract class CapellaSessionRelatedBaseTestFixture
{
    /**
     * The {@linkplain LogicalComponent} id contained in the {@linkplain Session} set up in the {@linkplain SetupSession} 
     */
    protected final String LogicalComponentId = UUID.randomUUID().toString();
    
    /**
     * The {@linkplain LogicalComponent} contained in the {@linkplain Session} set up in the {@linkplain SetupSession} 
     */
    protected final LogicalComponent LogicalComponent;

    /**
     * The {@linkplain SystemUserRequirement} id contained in the {@linkplain Session} set up in the {@linkplain SetupRequirementPackage} 
     */
    protected final String UserRequirementId = UUID.randomUUID().toString();
    
    /**
     * Initializes a new {@linkplain CapellaSessionUtility}
     */
    protected CapellaSessionRelatedBaseTestFixture()
    {
        this.LogicalComponent = mock(LogicalComponent.class);
        when(this.LogicalComponent.getId()).thenReturn(this.LogicalComponentId);
    }
    
    /**
     * Setup one session and return it
     * 
     * @param sessionUri the URI of the session
     * @return a mocked {@linkplain Session}
     */
    protected Session GetSession(URI sessionUri)
    {
        var session = mock(Session.class);
        var sessionResource = mock(Resource.class);
        when(sessionResource.getURI()).thenReturn(sessionUri);
        when(session.getSessionResource()).thenReturn(sessionResource);
        TransactionalEditingDomain transactionalEditingDomain = mock(TransactionalEditingDomain.class);
        ResourceSet resourceSet = mock(ResourceSet.class);
        
        var sessionObjects = new ArrayList<Notifier>();
        
        var project = mock(Project.class);
        var systemEngineerings = new BasicEList<EObject>();
        var systemEngineering = mock(SystemEngineering.class);
        var logicalArchitecture = mock(LogicalArchitecture.class);
        var structure = mock(ComponentPkg.class);
   
        var property = this.GetProperty(Property.class);
        
        when(this.LogicalComponent.eContents()).thenReturn(new BasicEList<EObject>(Arrays.asList(property)));
        when(structure.eContents()).thenReturn(new BasicEList<EObject>(Arrays.asList(this.LogicalComponent)));
        
        var requirementPackage = this.GetRequirementPackage();
        when(logicalArchitecture.eContents()).thenReturn(new BasicEList<EObject>(Arrays.asList(structure, requirementPackage)));
        when(systemEngineering.eContents()).thenReturn(new BasicEList<EObject>(Arrays.asList(logicalArchitecture)));
        systemEngineerings.add(systemEngineering);
        
        when(project.eContents()).thenReturn(systemEngineerings);
        
        var tree = new AbstractTreeIterator<Notifier>(project)
                {
                    @Override
                    protected Iterator<? extends Notifier> getChildren(Object object)
                    {
                        var eContents = ((EObject)object).eContents();
                        
                        if(eContents == null || eContents.isEmpty()) 
                        {
                            return Collections.emptyIterator(); 
                        }
                        
                        return eContents.iterator();
                    }            
                };
                
        when(resourceSet.getAllContents()).thenReturn(tree);
        when(transactionalEditingDomain.getResourceSet()).thenReturn(resourceSet);
        when(session.getTransactionalEditingDomain()).thenReturn(transactionalEditingDomain);
        return session;
    }
    
    /**
     * Gets the session elements as a {@linkplain List} of {@linkplain CapellaElement}
     * 
     * @param <TElement> the type of element to return
     * @param session the {@linkplain Session}
     * @param clazz the {@linkplain Class} of {@linkplain TElement}
     * @return a {@linkplain List} of {@linkplain CapellaElement}
     */
    protected static <TElement extends Notifier> List<TElement> GetSessionElements(Session session, Class<TElement> clazz)
    {
        Notifier element;
        var elements = new ArrayList<TElement>();
        var contents = session.getTransactionalEditingDomain().getResourceSet().getAllContents();
        
        while(contents.hasNext() && (element = contents.next()) !=null)
        {
            if(clazz.isAssignableFrom(element.getClass()))
            {
                elements.add((TElement)element);
            }
        }
        
        return elements;
    }

    /**
     * Initializes a mocked {@linkplain RequirementsPkg}
     * 
     * @return the {@linkplain RequirementPkg}
     */
    protected RequirementsPkg GetRequirementPackage()
    {
        var requirementPackage = mock(RequirementsPkg.class);
        var systemNonFunctionalRequirement = mock(SystemNonFunctionalRequirement.class);
        var systemNonFunctionalInterfaceRequirement = mock(SystemNonFunctionalInterfaceRequirement.class);
        var systemFunctionalRequirement = mock(SystemFunctionalRequirement.class);
        var systemFunctionalInterfaceRequirement = mock(SystemFunctionalInterfaceRequirement.class);
        var systemUserRequirement = mock(SystemUserRequirement.class);
        when(systemUserRequirement.getId()).thenReturn(this.UserRequirementId);
        
        when(requirementPackage.eContents()).thenReturn(
                new BasicEList<EObject>(
                        Arrays.asList(systemNonFunctionalRequirement, systemNonFunctionalInterfaceRequirement, 
                                systemFunctionalRequirement, systemFunctionalInterfaceRequirement, systemUserRequirement)));
        
        return requirementPackage;
    }

    /**
     * Initializes a mocked {@linkplain Property}
     * 
     * @return the {@linkplain Property}
     */
    public Property GetProperty(Class<? extends Property> propertyClass)
    {
        var property = mock(propertyClass);
        
        var booleanPropertyValue = mock(BooleanPropertyValue.class);
        var floatPropertyValue = mock(FloatPropertyValue.class);
        var integerPropertyValue = mock(IntegerPropertyValue.class);
        var stringPropertyValue = mock(StringPropertyValue.class);
        
        when(property.eContents()).thenReturn(new BasicEList<EObject>(
                Arrays.asList(booleanPropertyValue, floatPropertyValue, integerPropertyValue, stringPropertyValue)));
        
        var minCardinality = mock(NumericValue.class);
        when(property.getOwnedMinCard()).thenReturn(minCardinality);
        var maxCardinality = mock(NumericValue.class);
        when(property.getOwnedMaxCard()).thenReturn(maxCardinality);
        var defaultValue = mock(NumericValue.class);
        when(property.getOwnedDefaultValue()).thenReturn(defaultValue);
        return property;
    }
}
