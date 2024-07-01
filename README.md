# DEH-Capella

The DEH Capella is a Java plugin Application that makes use of the [Comet SDKJ](https://github.com/STARIONGROUP/COMET-SDKJ-Community-Edition),
which is available on Maven Central. It allows users to interactively exchange data between models built with the [Capella](https://www.eclipse.org/capella/) software and a ECSS-E-TM-10-25A data source.

The DEH Capella adapter is supported by all Capella versions starting from Capella 5.0 including Capella 6.x.

## Installing the DEH-Capella adapter

- Download the [latest release](https://github.com/STARIONGROUP/DEH-Capella/releases/latest).
- Drag and drop the downloaded zip file onto the "Install new Software" window of Capella and the select the DEH Capella Adapter plugin in the list.
- Uncheck the checkbox "Group Items by category" in the case no feature are visible.
- Follow the plugin installation process in Capella.
- The same step applies for updating the adapter.

## Operating the DEH-Capella adapter

- After installing the adpter.
- A Comet icon ![Comet](https://github.com/STARIONGROUP/DEH-CommonJ/blob/master/src/main/resources/icon16.png?raw=true)  in the main toolbar gives access to show/hide all the views of the adapter.
- The Hub panel is the one that allows to connect to a Comet webservice/ECSS-E-TM-10-25A data source. Once there is a Comet model open, and a Capella project open. Mapping between models can achieved in any direction.
- To initialize a new mapping, there is a Map action available in the context menus of Project browsers such as the one from Capella and the ElementDefinitions and Requirements ones from the adapter panels.
- The Impact View panel is where Impact on target models can be previewed/transfered. Also from this view mapping information can be loaded/saved.
- The standard Error Log panel displays the output of the adapter which can be shown from there: *Window -> Show View -> Other -> General -> Error Log*.

## License

The libraries contained in the DEH Capella are provided to the community under the GNU Lesser General Public License. Because we make the software available with the LGPL, it can be used in both open source and proprietary software without being required to release the source code of your own components.

## Contributions

Contributions to the code-base are welcome. However, before we can accept your contributions we ask any contributor to sign the Contributor License Agreement (CLA) and send this digitaly signed to s.gerene@stariongroup.eu. You can find the CLA's in the CLA folder.

### Manual deployment from the project

- Build the plugin feature project
- In the Overview tab, you can click on Exporting > Export Wizard.
  - In the output folder, you will have all the files necessary.
  - Drag and drop the newly created feature folder onto the "Install new Software" window of Capella and the select the DEH Capella Adapter plugin in the list.
  - Follow the plugin installation process in Capella
