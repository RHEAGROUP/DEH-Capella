/*
 * CapellaArchitecture.java
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

package Enumerations;

import org.polarsys.capella.common.data.modellingcore.TraceableElement;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.cs.BlockArchitecture;
import org.polarsys.capella.core.data.cs.ComponentArchitecture;
import org.polarsys.capella.core.model.helpers.BlockArchitectureExt;
import org.polarsys.capella.core.model.helpers.BlockArchitectureExt.Type;

/**
 * The {@linkplain CapellaArchitecture} enumerates the possible Capella Architecture {@linkplain ComponentArchitecture} to use as target when mapping from the Hub
 */
public enum CapellaArchitecture
{
    /**
     * Represents the {@linkplain org.polarsys.capella.core.data.oa.OperationalAnalysis} package
     */
    OperationalAnalysis(org.polarsys.capella.core.data.oa.OperationalAnalysis.class, Type.OA),

    /**
     * Represents the {@linkplain org.polarsys.capella.core.data.ctx.SystemAnalysis} package
     */
    SystemAnalysis(org.polarsys.capella.core.data.ctx.SystemAnalysis.class, Type.SA),

    /**
     * Represents the {@linkplain org.polarsys.capella.core.data.la.LogicalArchitecture} package
     */
    LogicalArchitecture(org.polarsys.capella.core.data.la.LogicalArchitecture.class, Type.LA),
    
    /**
     * Represents the {@linkplain org.polarsys.capella.core.data.pa.PhysicalArchitecture} package
     */
    PhysicalArchitecture(org.polarsys.capella.core.data.pa.PhysicalArchitecture.class, Type.PA),    

    /**
     * Represents the {@linkplain org.polarsys.capella.core.data.epbs.EPBSArchitecture} package
     */
    EPBSArchitecture(org.polarsys.capella.core.data.epbs.EPBSArchitecture.class, Type.EPBS);
    
    /**
     * Holds the represented {@linkplain Class} of {@linkplain BlockArchitecture};
     */
    private Class<? extends BlockArchitecture> architectureClass;
    
    /**
     * Holds the corresponding {@linkplain Type}
     */
    private Type type;
    
    /**
     * Initializes a new {@linkplain CapellaArchitecture}, hides the default constructor
     */
    private CapellaArchitecture(Class<? extends BlockArchitecture> architectureClass, Type type) 
    {
        this.architectureClass = architectureClass;
        this.type = type;
    }

    /**
     * Gets the {@linkplain CapellaArchitecture} that corresponds to the provided {@linkplain BlockArchitecture}
     * 
     * @param architectureInstance the {@linkplain BlockArchitecture} instance
     * @return the {@linkplain CapellaArchitecture}
     */
    public static CapellaArchitecture From(BlockArchitecture architectureInstance)
    {
        for (var architecture : CapellaArchitecture.values())
        {
            if(architecture.architectureClass.isInstance(architectureInstance))
            {
                return architecture;
            }
        }
        
        return null;
    }
    
    /**
     * Gets the {@linkplain CapellaArchitecture} that corresponds to {@linkplain BlockArchitecture} of the provided {@linkplain TraceableElement}
     * 
     * @param element the {@linkplain TraceableElement} instance
     * @return the {@linkplain CapellaArchitecture}
     */
    public static CapellaArchitecture From(TraceableElement element)
    {
        var architectureInstance = BlockArchitectureExt.getRootBlockArchitecture(element);
        
        for (var architecture : CapellaArchitecture.values())
        {
            if(architecture.architectureClass.isInstance(architectureInstance))
            {
                return architecture;
            }
        }
        
        return null;
    }

    /**
     * Verifies whether this represented capella architecture is the same as the one containing the provided {@linkplain CapellaElement}
     * 
     * @param element the {@linkplain CapellaElement}
     * @return a {@linkplain boolean}
     */
    public boolean AreSameArchitecture(CapellaElement element)
    {
        return this.architectureClass.isInstance(BlockArchitectureExt.getRootBlockArchitecture(element));
    }
    
    /**
     * Gets the corresponding {@linkplain Type}
     * 
     * @return a {@linkplain Type}
     */
    public Type GetType()
    {
        return this.type;
    }
    
    /**
     * Gets the corresponding {@linkplain BlockArchitecture} {@linkplain Class}
     * 
     * @return a {@linkplain Class} of {@linkplain BlockArchitecture}
     */
    public Class<? extends BlockArchitecture> GetArchitectureClass()
    {
        return this.architectureClass;
    }
}
