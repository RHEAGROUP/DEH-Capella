/*
 * ICapellaSelectionService.java
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
package Services.CapellaSelection;

import java.util.Collection;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.ui.ISelectionService;

import io.reactivex.Observable;

/**
 * The {@linkplain ICapellaSelectionService} is the interface definition for the {@linkplain CapellaSelectionService}
 */
public interface ICapellaSelectionService
{
    /**
     * Gets an {@linkplain Observable} of {@linkplain EObject} that yields whenever the selection has changed
     * 
     * @return an {@linkplain Observable} of {@linkplain EObject}
     */
    Observable<EObject> SelectionChanged();

    /**
     * Gets the selected items from the {@linkplain ISelectionService}
     * 
     * @return a {@linkplain Collection} of {@linkplain EObject}
     */
    Collection<EObject> GetSelection();
}
