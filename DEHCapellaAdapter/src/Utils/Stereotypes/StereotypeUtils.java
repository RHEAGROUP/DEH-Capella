/*
 * StereotypeUtils.java
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
package Utils.Stereotypes;

import static Utils.Operators.Operators.AreTheseEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.ENamedElement;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.polarsys.capella.core.data.capellacommon.CapellacommonPackage;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellacore.CapellacorePackage;
import org.polarsys.capella.core.data.capellacore.NamedElement;
import org.polarsys.capella.core.data.cs.BlockArchitecture;
import org.polarsys.capella.core.data.cs.CsPackage;
import org.polarsys.capella.core.data.fa.FaPackage;
import org.polarsys.capella.core.data.information.InformationPackage;
import org.polarsys.capella.core.data.information.Unit;
import org.polarsys.capella.core.data.information.datatype.DatatypePackage;
import org.polarsys.capella.core.data.information.datavalue.DataValue;
import org.polarsys.capella.core.data.information.datavalue.DatavaluePackage;
import org.polarsys.capella.core.data.information.datavalue.EnumerationLiteral;
import org.polarsys.capella.core.data.information.datavalue.LiteralBooleanValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralNumericValue;
import org.polarsys.capella.core.data.information.datavalue.LiteralStringValue;
import org.polarsys.capella.core.data.la.LaPackage;
import org.polarsys.capella.core.data.pa.PaPackage;
import org.polarsys.capella.core.data.pa.deployment.DeploymentPackage;
import org.polarsys.capella.core.data.requirement.Requirement;
import org.polarsys.capella.core.data.requirement.RequirementPackage;
import org.polarsys.capella.core.data.requirement.RequirementsPkg;
import org.polarsys.capella.core.model.helpers.BlockArchitectureExt.Type;
import org.polarsys.kitalpha.emde.model.Element;

import Enumerations.CapellaArchitecture;
import Utils.Ref;

/**
 * The {@linkplain StereotypeUtils}  provides useful methods on Capella components
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public final class StereotypeUtils
{ 
    /**
     * Gets a 10-25 compliant short name from the provided stereotype name
     * 
     * @param name the {@linkplain String} name to base the short name on
     * @return a {@linkplain string}
     */
    public static String GetShortName(String name)
    {
        return name.replaceAll("[^a-zA-Z0-9-]|\\s", "").toLowerCase();
    }

    /**
     * Gets a 10-25 compliant short name from the provided stereotype name
     * 
     * @param namedElement the {@linkplain ENamedElement} to base the short name on its name
     * @return a {@linkplain string}
     */
    public static String GetShortName(ENamedElement namedElement)
    {
        return GetShortName(namedElement.getName());
    }

    /**
     * Gets a 10-25 compliant short name from the provided stereotype name
     * 
     * @param namedElement the {@linkplain NamedElement} to base the short name on its name
     * @return a {@linkplain string}
     */
    public static String GetShortName(NamedElement namedElement)
    {
        return GetShortName(namedElement.getName());
    }    

    /**
     * Gets a 10-25 compliant short name from the provided {@linkplain Requirement} requirement ID or name
     * 
     * @param requirement the {@linkplain Requirement} to base the short name on its name
     * @return a {@linkplain string}
     */
    public static String GetShortName(Requirement requirement)
    {
        if(StringUtils.isBlank(requirement.getRequirementId()))
        {
            return GetShortName(requirement.getName());
        }

        return requirement.getRequirementId();        
    }
    
    /**
     * Gets the children from the provided {@linkplain EObject} if they are assignable from with the provided {@linkplain Class}
     * 
     * @param <TCapellaElement> the {@linkplain Type} to look for 
     * @param element the {@linkplain EObject} to get the children from
     * @param clazz the {@linkplain Class} of the {@linkplain #TCapellaElement} parameter
     * @return a {@linkplain Collection} of element typed as the one specified by the {@linkplain #TCapellaElement} parameter
     */
    @SuppressWarnings("unchecked")
    public static <TCapellaElement> Collection<TCapellaElement> GetChildren(EObject element, Class<TCapellaElement> clazz)
    {
        var result = new ArrayList<TCapellaElement>();
        
        if(element == null || element.eContents() == null)
        {
            return result;
        }
        
        for (var child : element.eContents())
        {
            if(clazz.isAssignableFrom(child.getClass())) 
            {
                result.add((TCapellaElement) child);
            }
        }
        
        return result;   
    }
    
    /**
     * Gets the children from the provided {@linkplain EObject} as a stream-able {@linkplain Collection}
     * 
     * @param element the {@linkplain EObject} to get the children from
     * @return a {@linkplain Collection} of {@linkplain EObject}
     */
    public static Collection<EObject> GetChildren(EObject element)
    {
        return GetChildren(element, EObject.class);
    }
    
    /**
     * Verifies that the provided {@linkplain EObject} parent is the lawful parent of the provided {@linkplain CapellaElement} child
     * 
     * @param parent the {@linkplain EObject} parent
     * @param child the {@linkplain CapellaElement} child
     * @return a value indicating whether the parent is one of the child,
     * if true, it also means that the parent is {@linkplain RequirementsPkg}
     */
    public static boolean IsParentOf(EObject parent, CapellaElement child)
    {
        try
        {
            return child.eContainer() instanceof RequirementsPkg 
                    && parent instanceof RequirementsPkg 
                    && EcoreUtil.isAncestor(parent, child);
        }
        catch(Exception exception)
        {
            LogManager.getLogger().catching(exception);
            return child.eContainer() == parent; 
        }
    }
  
    /**
     * Verifies the provided {@linkplain Element} element is owned by the provided {@linkplain Element} parent
     * 
     * @param element the {@linkplain EObject} to verify
     * @param parent the {@linkplain EObject} parent
     * @return a value indicating whether the element is owned by the specified parent
     */
    public static boolean IsOwnedBy(CapellaElement element, CapellaElement parent)
    {
        if(parent.eContents().isEmpty())
        {
            return false;
        }
        
        for (var child : parent.eContents().stream()
                .filter(x -> x instanceof CapellaElement)
                .map(x -> (CapellaElement)x)
                .collect(Collectors.toList()))
        {
            if(AreTheseEquals(child.getId(), element.getId()))
            {
                return true;
            }
            
            if(!child.eContents().isEmpty() && IsOwnedBy(element, child))
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Attempts to retrieve the parent of parent of the provided {@linkplain Class} element. 
     * Hence this is not always possible if the user decides to structure its SysML project differently.
     * However, this feature is only a nice to have.
     *  
     * @param requirement the {@linkplain Requirement} element to get the parent from
     * @param possibleParent the {@linkplain Ref} of {@linkplain RequirementsPkg}
     * @return a value indicating whether the name of the parent was retrieved with success
     */
    public static boolean TryGetPossibleRequirementsSpecificationElement(Requirement requirement, Ref<RequirementsPkg> possibleParent)
    {
        RequirementsPkg lastElement = null;
        EObject currentElement = requirement.eContainer();
        
        while(!possibleParent.HasValue() && currentElement != null)
        {  
            if(currentElement instanceof RequirementsPkg)
            {
                lastElement = (RequirementsPkg)currentElement;
            }
            else
            {
                possibleParent.Set(lastElement);
            }

            currentElement = lastElement == null ? requirement.eContainer() : lastElement.eContainer();
        }
        
        return possibleParent.HasValue();
    }
    
    /**
     * Gets the {@linkplain EClass} that corresponds to the provided {@linkplain String} className 
     * 
     * @param className the {@linkplain Class} name
     * @return the {@linkplain EClassifier} {@linkplain EClass}
     */
    public static Pair<EClassifier, EFactory> GetEClassAndFactory(String className)
    {
        for (var ePackage : StereotypeUtils.GetEPackages())
        {
            var eClass = ePackage.getEClassifier(className);
            
            if(eClass != null)
            {
                return Pair.of(eClass, ePackage.getEFactoryInstance());
            }
        }
        
        return Pair.of(null, null);
    }

    /**
     * Gets a {@linkplain List} of {@linkplain EPackage}s instance
     * 
     * @return {@linkplain List} of {@linkplain EPackage}
     */
    private static List<EPackage> GetEPackages()
    {
        return Arrays.asList(PaPackage.eINSTANCE, LaPackage.eINSTANCE, FaPackage.eINSTANCE, RequirementPackage.eINSTANCE, CapellacorePackage.eINSTANCE,
                InformationPackage.eINSTANCE, DatavaluePackage.eINSTANCE, DatatypePackage.eINSTANCE, CapellacommonPackage.eINSTANCE, 
                CsPackage.eINSTANCE, DeploymentPackage.eINSTANCE);
    }

    /**
     * Gets the containing {@linkplain CapellaArchitecture} of the provided {@linkplain CapellaElemet}
     * 
     * @param capellaElement the {@linkplain CapellaElement}
     * @return the {@linkplain CapellaArchitecture}
     */
    @SuppressWarnings("null")
    public static CapellaArchitecture GetArchitecture(CapellaElement capellaElement)
    {
        var parent = capellaElement.eContainer();
        BlockArchitecture architecture;
        
        while(!(parent instanceof BlockArchitecture && (architecture = (BlockArchitecture)parent) != null))
        {
            parent = parent.eContainer();
        }
        
        return CapellaArchitecture.From(architecture);
    }
    
    /**
     * Gets the highest {@linkplain RequirementPkg} that contains the provided {@linkplain Requirement}
     * 
     * @param element the {@linkplain Requirement} from which to find the highest parent
     * @return a {@linkplain RequirementPkg}
     */
    public static RequirementsPkg GetTopRequirementPakage(Requirement element)
    {
        var parent = element.eContainer();
        RequirementsPkg previousParent = null;
        
        while(parent instanceof RequirementsPkg)
        {
            previousParent = (RequirementsPkg)parent;
            parent = parent.eContainer();
        }
        
        return previousParent;
    }

    /**
     * Gets value representation string out of the specified {@linkplain LiteralNumericValue}
     * 
     * @param value the {@linkplain LiteralNumericValue}
     * @return a {@linkplain String}
     */
    public static String GetValueRepresentation(LiteralNumericValue value)
    {         
        var unit = StereotypeUtils.GetUnitRepresention(value);
        
        return String.format("%s%s", value.getValue(), unit == null ? StereotypeUtils.GetTypeRepresentation(value) : unit);
    }
    
    /**
     * Gets the type of the provided value as string
     * 
     * @param value the {@linkplain LiteralNumericValue}
     * @return a {@linkplain String}
     */
    private static String GetTypeRepresentation(LiteralNumericValue value)
    {
        if(value.getType() != null)
        {
            return String.format(" %s", value.getType().getName());
        }
        
        return " ";
    }

    /**
     * Gets the {@linkplain Unit} as string
     * 
     * @param value the {@linkplain LiteralNumericValue}
     * @return a {@linkplain String}
     */
    private static String GetUnitRepresention(LiteralNumericValue value)
    {
        if(value.getUnit() != null)
        {
            return String.format(" [%s]", value.getUnit().getName());
        }
        
        return null;
    }
    
    /**
     * Gets the value representation based on the represented {@linkplain TElement}
     * 
     * @param dataValue the {@linkplain DataValue} 
     * 
     * @return a {@linkplain String}
     */
    public static String GetValueRepresentation(DataValue dataValue)
    {
        if(dataValue instanceof LiteralNumericValue)
        {
            return StereotypeUtils.GetValueRepresentation((LiteralNumericValue)dataValue);
        }
        else if(dataValue instanceof LiteralBooleanValue) 
        {
            return (String.valueOf(((LiteralBooleanValue)dataValue).isValue()));
        }
        else if(dataValue instanceof LiteralStringValue) 
        {
            return (((LiteralStringValue)dataValue).getValue());
        }
        else if(dataValue instanceof EnumerationLiteral)
        {
            return ((EnumerationLiteral)dataValue).getDomainValue().getName();
        }   
        
        return "";
    }
}
