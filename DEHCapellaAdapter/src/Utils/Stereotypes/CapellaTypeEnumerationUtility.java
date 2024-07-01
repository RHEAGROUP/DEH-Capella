/*
 * CapellaTypeEnumerationUtility.java
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
package Utils.Stereotypes;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.polarsys.capella.core.data.capellacore.CapellaElement;

/**
 * The {@linkplain CapellaTypeEnumerationUtility} is a utility class to help deal with java enumerators {@linkplain RequirementType} and {@linkplain ComponentType}
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public final class CapellaTypeEnumerationUtility
{
    /**
     * The current class logger
     */
    private static final Logger logger = LogManager.getLogger();
    
    /**
     * Initializes a new {@linkplain CapellaTypeEnumerationUtility}, unused
     */
    private CapellaTypeEnumerationUtility() { }

    /**
     * Gets an instance of {@linkplain #TEnum} with the {@linkplain classType} matching the provided {@linkplain Class} 
     * 
     * @param <TEnum> the {@linkplain Enum} type to return
     * @param classType the {@linkplain class} that could match the {@linkplain classType} from one of the possible enumeration value
     * @return a {@linkplain #TEnum}
     */
    @SuppressWarnings("unchecked")
    public static <TEnum extends Enum<?> & ICapellaTypeEnumeration<?, ?>> TEnum From(Class<? extends CapellaElement> classType)
    {
        Function<TEnum[], Optional<TEnum>> result = x -> Arrays.asList(x)
                .stream()
                .filter(e -> e.ClassType() == classType)
                .findFirst();
        
        var component = result.apply((TEnum[]) ComponentType.values());
        
        if(component.isPresent())
        {
            return component.get();
        }
        
        var requirement = result.apply((TEnum[]) RequirementType.values());
        
        if(requirement.isPresent())
        {
            return requirement.get();
        }
        
        return null;
    }
    
    /**
     * Gets an instance of {@linkplain ComponentType} from the provided {@linkplain String}
     * 
     * @param valueOrLabel a {@linkplain String} that could potentially match the {@linkplain Label} of the enum value
     * @return a {@linkplain ComponentType}
     */
    @SuppressWarnings("finally")
    public static ComponentType ComponentTypeFrom(String valueOrLabel)
    {
        for(var value : ComponentType.values())
        {
            if(VerifyEnumerationValue(valueOrLabel, value)) 
            {
                return value;
            }
        }
        
        try
        {
            return ComponentType.valueOf(valueOrLabel);
        }
        finally
        {
            return null;
        }
    }
    
    /**
     * Gets an instance of {@linkplain RequirementType} with the provided {@linkplain String}
     * 
     * @param valueOrLabel a {@linkplain String} that could potentially match the {@linkplain Label} of the enum value
     * @return a {@linkplain RequirementType}
     */
    @SuppressWarnings("finally")
    public static RequirementType RequirementTypeFrom(String valueOrLabel)
    {
        for(var value : RequirementType.values())
        {
            if(VerifyEnumerationValue(valueOrLabel, value)) 
            {
                return value;
            }
        }
                
        try
        {
            return RequirementType.valueOf(valueOrLabel);
        }
        finally
        {
            return null;
        }
    }

    /**
     * Assert that the provided {@linkplain #value} matches the provided {@linkplain #valueOrLabel}
     * 
     * @param valueOrLabel a {@linkplain String} that could potentially match the {@linkplain Label} of the enum value
     * @param value the current {@linkplain ICapellaTypeEnumeration} value
     * @return a value indicating whether the {@linkplain #value} meets the requirements in order to match the provided {@linkplain #valueOrLabel}
     */
    private static boolean VerifyEnumerationValue(String valueOrLabel, ICapellaTypeEnumeration<?, ?> value)
    {
        return value.Label().equalsIgnoreCase(valueOrLabel) || (value.ClassType() != null 
                && value.ClassType().getSimpleName().equalsIgnoreCase(valueOrLabel));
    }
}
