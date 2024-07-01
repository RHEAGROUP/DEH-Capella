/*
 * ClonedReferenceElement.java
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

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.polarsys.capella.core.data.capellacore.CapellaElement;

/**
 * The ClonedReferenceElement is a POJO class that represents a cloned element with it's original reference
 * 
 * @param <TElement> the type of the cloned {@linkplain CapellaElement}
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class ClonedReferenceElement<TElement extends CapellaElement>
{
    /**
     *  Backing field for {@linkplain #GetClone()}
     */
    private TElement clone;

    /**
     * Gets the cloned reference to the {@linkplain #TElement}
     * 
     * @return the {@linkplain #TElement}
     */
    public TElement GetClone()
    {
        return this.clone;
    }

    /**
     * Backing field for {@linkplain #GetOriginal()}
     */
    private TElement original;
    
    /**
     * Gets the original reference to the {@linkplain #TElement}
     * 
     * @return the {@linkplain #TElement}
     */
    public TElement GetOriginal()
    {
        return this.original;
    }
    
    /**
     * Initializes a new {@linkplain ClonedReferenceElement}
     * 
     * @param original the {@linkplain #TElement} original reference
     */
    public ClonedReferenceElement(TElement original) 
    {
        this.clone = EcoreUtil.copy(original);
        this.original = original;
    }
}
