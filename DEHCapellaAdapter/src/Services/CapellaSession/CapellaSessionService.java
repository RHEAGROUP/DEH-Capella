/*
 * CapellaModelService.java
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
package Services.CapellaSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.sirius.business.api.session.Session;
import org.polarsys.capella.core.data.capellacore.CapellaElement;
import org.polarsys.capella.core.data.capellamodeller.Project;
import org.polarsys.capella.core.data.cs.BlockArchitecture;
import org.polarsys.capella.core.data.cs.Component;
import org.polarsys.capella.core.data.pa.PhysicalComponent;
import org.polarsys.capella.core.model.helpers.BlockArchitectureExt;
import org.polarsys.capella.core.model.helpers.BlockArchitectureExt.Type;

import Enumerations.CapellaArchitecture;
import Reactive.ObservableValue;
import Services.NavigationService.INavigationService;
import ViewModels.CapellaObjectBrowser.Rows.RootRowViewModel;
import io.reactivex.Observable;

/**
 * The {@linkplain CapellaModelService} is the service providing easier access to the capella {@linkplain Sessions}s
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public class CapellaSessionService implements ICapellaSessionService
{
    /**
     * The current class Logger
     */
    private final Logger logger = LogManager.getLogger();
    
    /**
     * The {@linkplain ICapellaSessionListenerService} that observe changes on the {@linkplain SessionManager}
     */
    private final ICapellaSessionListenerService sessionListener;

    /**
     * The {@linkplain ISiriusSessionManagerWrapper} instance
     */
    private ISiriusSessionManagerWrapper sessionManager;
    
    /**
     * Backing field for {@linkplain HasAnyOpenSessionObservable}
     */
    private ObservableValue<Boolean> hasAnyOpenSession = new ObservableValue<>(false, Boolean.class);

    /**
     * Gets the {@linkplain Observable} of value indicating whether there is any session open
     * 
     * @return an {@linkplain Observable} of {@linkplain Boolean}
     */
    @Override
    public Observable<Boolean> HasAnyOpenSessionObservable()
    {
        return this.hasAnyOpenSession.Observable();
    }
    
    /**
     * Backing field for {@linkplain HasAnyOpenSessionObservable}
     */
    private ObservableValue<Session> sessionUpdated = new ObservableValue<>();

    /**
     * Gets the {@linkplain Observable} of {@linkplain Session} that indicates when the emitted session gets saved
     * 
     * @return an {@linkplain Observable} of {@linkplain Session}
     */
    @Override
    public Observable<Session> SessionUpdated()
    {
        return this.sessionUpdated.Observable();
    }

    /**
     * Backing field for the {@linkplain #GetCurrentSession()}
     */
    private Session currentSession;
    
    /**
     * Gets the current {@linkplain Session} to work with
     * 
     * @return the {@linkplain Session}
     */
    @Override
    public Session GetCurrentSession()
    {
        if(currentSession == null) 
        {
            this.currentSession = new ArrayList<>(this.GetOpenSessions()).get(0);
        }
        else if(!this.GetOpenSessions().contains(this.currentSession))
        {
            this.currentSession = null;
        }
        
        return this.currentSession;
    }
    
    /**
     * Sets the {@linkplain #GetCurrentSession()}
     * @param session the new {@linkplain Session}
     */
    @Override
    public void SetCurrentSession(Session session)
    {
        this.currentSession = session;
    }
        
    /**
     * Initializes a new {@linkplain CapellaSessionService}
     * 
     * @param sessionListener the {@linkplain ICapellaSessionListenerService} instance
     * @param sessionManager the {@linkplain ISiriusSessionManagerWrapper} instance
     */
    public CapellaSessionService(ICapellaSessionListenerService sessionListener, ISiriusSessionManagerWrapper sessionManager)
    {
        this.sessionListener = sessionListener;
        this.sessionManager = sessionManager;
        
        this.hasAnyOpenSession.Value(this.sessionManager.HasAnyOpenSession());
        
        this.sessionManager.AddListener(this.sessionListener);

        this.sessionListener.SessionUpdated()
            .subscribe(x -> 
            {
                this.hasAnyOpenSession.Value(this.sessionManager.HasAnyOpenSession());
                this.sessionUpdated.Value(x);
            });
            
        this.sessionListener.SessionAdded()
            .subscribe(x -> 
            {
                this.hasAnyOpenSession.Value(this.sessionManager.HasAnyOpenSession());
                this.SetCurrentSession(x);
            });
        
        this.sessionListener.SessionRemoved()
            .subscribe(x -> 
            {
                this.hasAnyOpenSession.Value(this.sessionManager.HasAnyOpenSession());
                this.SetCurrentSession(null);
            });
    }

    /**
     * Gets the session corresponding to a semantic {@linkplain EObject} 
     * 
     * @param object the {@linkplain EObject} to retrieve the session it belongs to
     * @return the corresponding {@linkplain Session}
     */
    @Override
    public Session GetSession(EObject object)
    {
        return this.sessionManager.GetSession(object);
    }
        
    /**
     * Gets the value emitted by {@linkplain HasAnyOpenSessionObservable} indicating whether there is any session open
     * 
     * @return a {@linkplain Boolean} value
     */
    @Override
    public boolean HasAnyOpenSession()
    {
        return this.sessionManager.HasAnyOpenSession();
    }
    
    /**
     * Gets the open {@linkplain Session}s
     * 
     * @return a {@linkplain Collection} of {@linkplain Session}
     */
    @Override
    public Collection<Session> GetOpenSessions()
    {
        return this.sessionManager.GetSessions();
    }
    
    /**
     * Gets the models of the active sessions
     * 
     * @return a {@linkplain RootRowViewModel}, or null if no active session is found
     */
    @Override
    public RootRowViewModel GetModels()
    {
        if(!this.sessionManager.HasAnyOpenSession())
        {
            this.logger.info("No active session has been found!");
            return null;
        }
        
        var rootRowViewModel = new RootRowViewModel("Capella Models");
        
        this.ProcessSessionsElements((uri, notifiers) -> rootRowViewModel.GetContainedRows().add(new RootRowViewModel(URI.decode(uri.lastSegment()), notifiers)));
        
        return rootRowViewModel;
    }

    /**
     * Loops through all {@linkplain Notifier} element from all the open {@linkplain Session}s
     * and accept a {@linkplain BiConsumer} on them
     * 
     * @param action the {@linkplain BiConsumer} of {@linkplain URI} and {@linkplain ArrayList} of {@linkplain Notifier}.
     * The {@linkplain URI} identifies the {@linkplain Session} to which the elements from the array belongs to
     */
    private void ProcessSessionsElements(BiConsumer<URI, ArrayList<Notifier>> action)
    {
        for (var session : this.sessionManager.GetSessions())
        {
            Notifier element;
            var elements = new ArrayList<Notifier>();
            var contents = session.getTransactionalEditingDomain().getResourceSet().getAllContents();
            
            while(contents.hasNext() && (element = contents.next()) !=null)
            {
                elements.add((Notifier)element);
            }
            
            action.accept(session.getSessionResource().getURI(), elements);
        }
    }
    
    /**
     * Gets all the {@linkplain CapellaElement} from the currently open {@linkplain Session}s
     * 
     * @return a {@linkplain HashMap} of {@linkplain URI} and a {@linkplain List} of {@linkplain CapellaElement}
     */
    @Override
    public HashMap<URI, List<CapellaElement>> GetAllCapellaElementsFromOpenSessions()
    {
        var sessionAndObjectsMap = new HashMap<URI, List<CapellaElement>>();
        
        this.ProcessSessionsElements((uri, notifiers) -> 
        {
            var elements = notifiers.stream()
                    .filter(x -> x instanceof CapellaElement)
                    .map(x -> (CapellaElement)x)
                    .collect(Collectors.toList());
            
            sessionAndObjectsMap.putIfAbsent(uri, elements);
        });
        
        return sessionAndObjectsMap;
    }

    /**
     * Gets the top element from the {@linkplain Session} that owns the provided {@linkplain CapellaElement} in the Physical Architecture package
     * 
     * @param referenceElement the {@linkplain CapellaElement} used to search the correct session
     * @return a {@linkplain CapellaElement}
     */
    @Override
    public CapellaElement GetTopElement(CapellaElement referenceElement)
    {
        return this.GetTopElement(referenceElement, Type.PA);
    }

    /**
     * Gets the top element from the {@linkplain Session} in the provided {@linkplain CapellaArchitecture}
     * 
     * @param architecture the {@linkplain CapellaArchitecture} from which to get the top element
     * @return a {@linkplain CapellaElement}
     */
    @Override
    public Component GetTopElement(CapellaArchitecture architecture)
    {
        return this.GetTopElement(this.GetProject(), architecture.GetType());
    }

    /**
     * Gets the top element from the provided {@linkplain Session} in the Physical Architecture package
     * 
     * @param session the {@linkplain Session}
     * @return a {@linkplain PhysicalComponent}
     */
    @Override
    public PhysicalComponent GetTopElement(Session session)
    {
        return (PhysicalComponent) this.GetTopElement(this.GetProject(session), Type.PA);
    }
    
    /**
     * Gets the top element from the {@linkplain #GetCurrentSession()} in the Physical Architecture package
     * 
     * @return a {@linkplain PhysicalComponent}
     */
    @Override
    public PhysicalComponent GetTopElement()
    {
        return (PhysicalComponent) this.GetTopElement(this.GetProject(), Type.PA);
    }
        
    /**
     * Gets the top element from the {@linkplain Session} that owns the provided {@linkplain CapellaElement}
     * 
     * @param referenceElement the {@linkplain CapellaElement} used to search the correct session
     * @param architectureType the architecture {@linkplain Type}
     * @return a {@linkplain Component}
     */
    @Override
    public Component GetTopElement(CapellaElement referenceElement, Type architectureType)
    {
        return GetTopElement(this.GetProject(referenceElement), architectureType);
    }

    /**
     * Gets the top element from the {@linkplain Session} that owns the provided {@linkplain CapellaElement}
     * 
     * @param project the {@linkplain Project} used to search the correct session
     * @param architectureType the architecture {@linkplain Type}
     * @return a {@linkplain CapellaElement}
     */
    @Override
    public Component GetTopElement(Project project, Type architectureType)
    {
        var architecture = BlockArchitectureExt.getBlockArchitecture(architectureType, project);
        return architecture.getSystem();
    }
            
    /**
     * Gets the {@linkplain Project} element from the {@linkplain Session} that owns the provided {@linkplain CapellaElement}
     * 
     * @param referenceElement the {@linkplain CapellaElement} used to search the correct session
     * @return a {@linkplain CapellaElement}
     */
    @Override
    public Project GetProject(CapellaElement referenceElement)
    {
        var session = this.GetSession(referenceElement);
        return this.GetProject(session);
    }


    /**
     * Gets the {@linkplain Project} element from the {@linkplain #GetCurrentSession()}
     * 
     * @param session the {@linkplain Session}
     * @return a {@linkplain Project} element
     */
    @Override
    public Project GetProject()
    {
        return this.GetProject(this.GetCurrentSession());
    }
    
    /**
     * Gets the {@linkplain Project} element from the provided {@linkplain Session}
     * 
     * @param session the {@linkplain Session}
     * @return a {@linkplain Project} element
     */
    @Override
    public Project GetProject(Session session)
    {
        var contents = session.getTransactionalEditingDomain().getResourceSet().getAllContents();
        
        Notifier element;
        
        while(contents.hasNext() && (element = contents.next()) !=null)
        {
            if(element instanceof Project)
            {
                return (Project)element;
            }
        }
        
        return null;
    }

    /**
     * Gets the {@linkplain BlockArchitecture} that matches the {@linkplain CapellaArchitecture} provided
     * 
     * @param targetArchitecture the {@linkplain CapellaArchitecture}
     * @return the {@linkplain BlockArchitecture}
     */
    @Override
    public BlockArchitecture GetArchitectureInstance(CapellaArchitecture targetArchitecture)
    {
        return BlockArchitectureExt.getBlockArchitecture(targetArchitecture.GetType(), this.GetProject());
    }
}
