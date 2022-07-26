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

Use the workbench to register editors and explorers by calling the exposed functions.

//TODO: Make this look better
<table>
<tr>
<th> 

``Workbench.registerExplorer<C>``

</th>
<th> 

``Workbench.registerEditor<C>``

</th>
</tr>
<tr>
<td>

Register an Explorer with the following arguments:
- C: Any - controller used in view to manage and display data
- type: String - the type of Data handled by this Module 
- title: (C) -> String - the display title of the explorer
- init: (C, MQClient) -> Unit - initialize messaging
- explorerView: @Composable(C) -> Unit - displayable content
```
    workbench.registerExplorer<EmployeeController>(
        type =  "Employees",
        title = { it.title() },
        init =  {c, m -> },
    ) { c ->
        //The @Composable explorer view goes here
        Text(text = "This is an Explorer")
    }
```

</td>
<td>

Register and Editor with the following arguments:
- C: Any - controller used in view to manage and display data
- type: String - the type of Data handled by this Module
- title: (C) -> String - the display title of the editor
- initController: (Int, MQClient) -> C - initialize a controller for the given data id and setup messaging
- icon: ImageVector - icon used for the editor (Optional)
- onClose: (C, MQClient) -> ActionResult - callback when closing the editor (Optional)
- onSave: (C, MQClient) -> ActionResult - callback when saving the editor (Optional)
- editorView: @Composable(C) -> Unit - displayable content
```
    workbench.registerEditor<EmployeeController>(
                type =              "Employee",
                title =             { it.name },
                initController =    { employeeController },
        ){ c ->
            //The @Composable editor view goes here
            Text(text = "This is an Editor")
        }
    }
```

</td>
</tr>
</table>

#### OnClose OnSave
Closing and Saving an Editor will publish a message even if the callback is not specified. Both callbacks require an ActionResult as return value. This ActionResult has a success flag which is a Boolean and a message. Use the predefined functions success() and failure(msg: String). The current action will be aborted should the returned ActionResults success flag be false.

- OnClose is called whenever an Editor is closed: Use this to execute additional actions or custom messages
- OnSave is called whenever Save All is executed or the Editor has unsaved state and is closed: Use this for validation or to execute additional actions or custom messages

### Request Modules
Once a Module is registered it can be requested. Explorers and Editors can be requested multiple times. An Explorer for example can be requested with different data subsets, like one explorer for employees grouped by country. It is also possible to have one Explorer for Lists which can handle all needed Data. Editors are requested whenever a data record needs editing.
Use the workbench to request editors and explorers by calling the exposed functions.

<table>
<tr>
<th> 

``Workbench.requestExplorer<C>``

</th>
<th> 

``Workbench.requestEditor<C>``

</th>
</tr>
<tr>
<td>

Request an Explorer with the following arguments:
- type: String - the type of Data for which an Explorer is requested
- c: C - controller used in view to manage and display data
- listed: boolean - defines if explorer is added to menu and can be reopened (Optional)
- location: ExplorerLocation - the location for this Explorer (Optional)
- shown: boolean - defines if the explorer is shown when starting the Workbench (Optional)
```
    workbench.requestExplorer<EmployeeController>(
        type =      "Employees",
        c =         EmployeeController(), 
    )
``` 

</td>
<td>

Request an Editor with the following arguments:
- type: String -
- id: Int -
```
    workbench.requestEditor<EmployeeController>(
        type = "Employee", 
        id =    1234
    )
```

</td>
</tr>
</table>

TODO: make example for this and add screenshots?


### Commands
//TODO: is this a thing?

### Messaging
