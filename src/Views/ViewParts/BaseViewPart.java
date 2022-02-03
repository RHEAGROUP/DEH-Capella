/*
 * BaseViewPart.java
 *
 * Copyright (c) 2020-2021 RHEA System S.A.
 *
 * Author: Sam Geren�, Alex Vorobiev, Nathanael Smiechowski, Antoine Th�ate
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
package Views.ViewParts;

import java.awt.Frame;

import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.ViewPart;

import ViewModels.Interfaces.IViewModel;
import Views.Interfaces.IView;

/**
 * The {@linkplain BaseViewPart} is the base {@linkplain ViewPart} for the adapter panels that integrate with Capella
 * 
 * @param <TViewModel> the type of the view model the inheriting view belongs to
 * @param <TView> the type of the view that is enclosed by the inheriting view
 */
@Annotations.ExludeFromCodeCoverageGeneratedReport
public abstract class BaseViewPart<TViewModel extends IViewModel, TView extends JPanel> extends ViewPart implements IView<TViewModel>
{
    /**
     * The current class logger
     */
    private Logger logger = LogManager.getLogger();
    
    /**
     * The AWT container that allows to display Swing components
     */
    protected Frame Container;
    
    /**
     * The Composite view element container that contains the {@linkplain Container}
     */
    private Composite composite;

    /**
     * The key that identify this view in the dock layout manager of the MagicDraw / Cameo software
     */
    private String panelId;

    /**
     * Gets the value of this associated unique panel dock key
     * 
     * @return a {@linkplain String} containing the key
     */
    public String GetPanelDockKey()
    {
        return this.panelId;
    }
    
    /**
     * An assert whether this view is visible
     */
    private boolean isVisibleInTheDock = true;
    
    /**
     * The {@link TViewModel} as the data context of this view
     */
    protected TViewModel DataContext;
    
    /**
     * The {@linkplain TView} this view wraps
     */
    protected TView View;
    
    
    /**
     * Initializes a new {@linkplain BaseViewPart}
     * 
     * @param panelId the {@linkplain String} key to identify the view in the IViewPart collection of the client
     */
    protected BaseViewPart(String panelId)
    {
        super();
        this.panelId = panelId;
    }
    
    /**
     * Creates the part control for this {@linkplain ViewPart}
     * 
     * @param parent the {@linkplain Composite} parent
     */
    protected void CreateBasePartControl(Composite parent)
    {
        this.composite = new Composite(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND);
        this.Container = SWT_AWT.new_Frame(this.composite);
    }

    /**
     * Sets the focus on the view element of choice
     */
    @Override
    public void setFocus()
    {
        this.composite.setFocus();
    }
    
    
    /**
     * Show or Hide this {@link MagicDrawBasePanel}
     * @param workbenchPage 
     * 
     * @param dockingManager The {@link DockingManager} that is allowed to hide or show this frame
     */
    public void ShowHide(IWorkbenchPage workbenchPage)
    {
        try
        {
            if(this.isVisibleInTheDock)
            {
                workbenchPage.hideView(workbenchPage.findView(this.panelId));
                this.isVisibleInTheDock = false;
            }
            else
            {
                workbenchPage.showView(this.panelId);
                this.isVisibleInTheDock = true;
            }
        }
        catch(Exception exception)
        {
            this.logger.catching(exception);
        }
    }

    /**
     * Sets the DataContext
     * 
     * @param viewModel the {@link IViewModel} to assign
     */
    @SuppressWarnings("unchecked")
    @Override
    public void SetDataContext(IViewModel viewModel)
    {
        this.DataContext = (TViewModel)viewModel;   
        this.Bind();
    }

    /**
     * Gets the DataContext
     * 
     * @return An {@link IViewModel}
     */
    @Override
    public TViewModel GetDataContext()
    {
        return this.DataContext;
    }
}
