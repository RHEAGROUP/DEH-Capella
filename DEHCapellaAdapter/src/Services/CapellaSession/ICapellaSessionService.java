/*
 * ICapellaSessionService.java
 *
 * Copyright (c) 2020-2022 RHEA System S.A.
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
package Services.CapellaSession;

import java.util.HashMap;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.business.api.session.Session;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.pa.PhysicalComponent;
import org.polarsys.capella.core.model.helpers.BlockArchitectureExt.Type;

import ViewModels.CapellaObjectBrowser.Rows.RootRowViewModel;
import io.reactivex.Observable;

/**
 * The {@linkplain ICapellaSessionService} is the main interface definition for {@linkplain CapellaSessionService}
 */
public interface ICapellaSessionService
{
    /**
     * Gets an {@linkplain Observable} of value indicating whether there is any session open
     * 
     * @return an {@linkplain Observable} of {@linkplain Boolean}
     */
    Observable<Boolean> HasAnyOpenSessionObservable();

    /**
     * Gets the models of the active sessions
     * 
     * @return a {@linkplain RootRowViewModel}, or null if no active session is found
     */
    RootRowViewModel GetModels();

    /**
     * Gets the session corresponding to a semantic {@linkplain EObject} 
     * 
     * @param object the {@linkplain EObject} to retrieve the session it belongs to
     * @return the corresponding {@linkplain Session}
     */
    Session GetSession(EObject object);

    /**
     * Gets the value emitted by {@linkplain HasAnyOpenSessionObservable} indicating whether there is any session open
     * 
     * @return a {@linkplain Boolean} value
     */
    boolean HasAnyOpenSession();

    /**
     * Gets all the {@linkplain CapellaElement} from the currently open {@linkplain Session}s
     * 
     * @return a {@linkplain HashMap} of {@linkplain URI} and a {@linkplain List} of {@linkplain CapellaElement}
     */
    HashMap<URI, List<CapellaElement>> GetAllCapellaElementsFromOpenSessions();

    /**
     * Gets the {@linkplain Observable} of {@linkplain Session} that indicates when the emitted session gets saved
     * 
     * @return an {@linkplain Observable} of {@linkplain Session}
     */
    Observable<Session> SessionUpdated();

    /**
     * Gets the {@linkplain Project} from the {@linkplain Session} that owns the provided {@linkplain CapellaElement}
     * 
     * @param referenceElement the {@linkplain CapellaElement} used to search the correct session
     * @return a {@linkplain CapellaElement}
     */
    Project GetProject(CapellaElement referenceElement);

    /**
     * Gets the top element from the {@linkplain Session} that owns the provided {@linkplain CapellaElement}
     * 
     * @param referenceElement the {@linkplain CapellaElement} used to search the correct session
     * @param architectureType the architecture {@linkplain Type}
     * @return a {@linkplain CapellaElement}
     */
    CapellaElement GetTopElement(CapellaElement referenceElement, Type architectureType);

    /**
     * Gets the top element from the {@linkplain Session} that owns the provided {@linkplain CapellaElement} in the Physical Architecture package
     * 
     * @param referenceElement the {@linkplain CapellaElement} used to search the correct session
     * @return a {@linkplain CapellaElement}
     */
    CapellaElement GetTopElement(CapellaElement referenceElement);

    /**
     * Gets the top element from the {@linkplain Session} that owns the provided {@linkplain CapellaElement}
     * 
     * @param project the {@linkplain Project} used to search the correct session
     * @param architectureType the architecture {@linkplain Type}
     * @return a {@linkplain CapellaElement}
     */
    Component GetTopElement(Project project, Type architectureType);

    /**
     * Gets the top element from the provided {@linkplain Session} in the Physical Architecture package
     * 
     * @param session the {@linkplain Session}
     * @return a {@linkplain PhysicalComponent}
     */
    PhysicalComponent GetTopElement(Session session);

    /**
     * Gets the open {@linkplain Session}s
     * 
     * @return a {@linkplain List} of {@linkplain Session}
     */
    List<Session> GetOpenSessions();

    /**
     * Gets the {@linkplain Project} element from the provided {@linkplain Session}
     * 
     * @param session the {@linkplain Session}
     * @return a {@linkplain Project} element
     */
    Project GetProject(Session session);
}
