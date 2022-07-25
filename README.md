# ComposeWorkbench
Compose desktop library to build large Applications by combining existing Modules.
# What is a ComposeWorkbench
The ComposeWorkbench provides common application structures to custom modules. It supports two types of modules, explorers and editors. Once embedded in the ComposeWorkbench these modules can be dragged and dropped and the window management is taken care of. There is also support for custom commands and a messaging system that allows communication between the individual modules.

Explorer: An Explorer is a module who's main purpose is to display data. For example: a list of customers or products.

Editor: An Editor is a module who's main purpose is to edit a given data record. For example a single customer or product.

# Implementation
## Gradle
## Maven
# Usage
Check the \demo directory for examples on how to use ComposeWorkbench

To create a ComposeWorkbench app start wit an instance of a Workbench
``
val workbench: Workbench = Workbench("Sample App")
``

### Register Modules
Editors and Explorers must be registered before they can be used (requested). The two main attributes in the registration are the content and the key. The content is the actual composable code of the Module the key defines the type of data the Module can handle. By registering Editors and Explorers you tell the ComposeWorkbench which Types of Data are supported and how they can be explored and edited.

TODO: describe the generic type

Use the workbench to register editors and explorers by calling the exposed functions.

``Workbench.registerExplorer`` Register an Explorer with the following arguments:
- type: String - the type of Data handled by this Module 
- title: (controller) -> String - the display title of the explorer
```
    workbench.registerExplorer<EmployeeController>(
        type =  "Employees",
        title = { it.title() }
    ) { m, c ->
        //The @Composable content goes here
    }
```

``Workbench.registerEditor`` Register and Editor with the following arguments:
- type: String - the type of Data handled by this Module
- loader: (Int) -> controller - 
- icon: ImageVector - Icon used for the editor
- title: (controller) -> String - the display title of the editor
- onClose: 
- onSave:
```
    workbench.registerEditor<EmployeeController>(
                type =      "Employee",
                loader =    { employeeController },
                icon =      Icons.Custom.EmployeeEdit,
                title =     { it.name },
                onClose =   {m, c ->  },
                onSave =    {m, c -> }
        ){m, c ->
            //The @Composable content goes here
        }
    }
```

### Request Modules
Once a Module is registered it can be requested. Explorers and Editors can be requested multiple times. An Explorer for example can be requested with different data subsets, like one explorer for employees grouped by country. It is also possible to have one Explorer for Lists which can handle all needed Data. Editors are requested whenever a data record needs editing.

Use the workbench to request editors and explorers by calling the exposed functions.
``Workbench.requestExplorer`` Request an Explorer with the following arguments:
- type: String - 
- m:  
- default: boolean -
- location: ExplorerLocation -
- shown: boolean -
```
    workbench.requestExplorer(
        type =      "Employees",
        m =         explorerModel[3], 
        default =   true, 
        location =  ExplorerLocation.BOTTOM, 
        shown =     false
    )
```
``Workbench.requestEditor`` Request an Editor with the following arguments:
- type: String -
- id: Int -
```
    workbench.requestEditor<CityState>(
        type = "City", 
        id =    1234
    )
```

### Commands
//TODO: is this a thing?

### Messaging
