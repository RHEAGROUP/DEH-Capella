/*
 * ComponentType.java
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.la.LogicalComponent;
import org.polarsys.capella.core.data.pa.PhysicalComponent;

/**
 * The {@linkplain ComponentType} is an enumerations of all mappable Capella {@linkplain Component} type
 */
public enum ComponentType implements ICapellaTypeEnumeration<ComponentType, Component>
{
    /**
     * Represents a {@linkplain PhysicalComponent}
     */
    Physical("Physical Component", PhysicalComponent.class),
    
    /**
     * Represents a {@linkplain LogicalComponent}
     */
    Logical("Logical Component", LogicalComponent.class);
    
    /**
     * The current class logger
     */
    private static final Logger logger = LogManager.getLogger();

    /**
     * The {@linkplain Class} represented by this enum value
     */
    private final Class<? extends Component> classType;
    
    /**
     * The display-able label that represent this enum value
     */
    public final String label;
    
    /**
     * Initializes a new {@linkplain ComponentType}
     * 
     * @param label the label associated to the enumeration value
     * @param classType the class associated to the enumeration value
     */
    private ComponentType(String label, Class<? extends Component> classType)
    {
        this.label = label;
        this.classType = classType;
    }

    /**
     * Returns the name of this enum constant, as contained in the declaration
     * 
     * @return a {@linkplain String}
     */
    @Override
    public String toString()
    {
        return this.label;
    }
    
    /**
     * Gets the {@linkplain Class} of the instance of the enumeration value
     * 
     * @return a {@linkplain Class} of {@linkplain CapellaElement}
     */
    @Override
    public Class<? extends Component> ClassType()
    {
        return this.classType;
    }
    
    /**
     * Gets the {@linkplain String} display-able label
     * 
     * @return a {@linkplain String}
     */
    @Override
    public String Label()
    {
        return this.label;
    }
}
