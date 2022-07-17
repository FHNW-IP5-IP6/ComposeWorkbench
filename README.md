# ComposeWorkbench
Compose desktop library to build large Applications by combining existing Modules.
# What is a ComposeWorkbench
The ComposeWorkbench provides common application structures to custom modules. It supports two types of modules, explorers and editors. Once embedded in the ComposeWorkbench these modules can be dragged and dropped and the window management is taken care of. There is also support for custom commands and a messaging system that allows communication between the individual modules.

Explorer: An Explorer is a module who's main purpose is to display Data. For example: a list of customers or products.
TODO: restrictions

Editor: An Editor is a module who's main purpose is to edit a given Dataset.  
TODO: restrictions

Each module needs a type which defines the type of data the module can handle. For example an explorer of type "customer" can display customers and an editor of type "customer" can edit a given customer.

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
TODO: Register defines how to display

Use the workbench to register editors and explorers by calling the exposed functions. By registering Editors and Explorers you tell the ComposeWorkbench which Types of Data are supported and how they can be explored and edited.

``Workbench.registerExplorer`` takes a type and the content. The type is a String and specifies which data the explorer shows and the content is a @Composable function which defines the content of the explorer.

``Workbench.registerEditor`` takes a type a loader and the content. The type specifies which data the editor can edit. The loader is a function which takes an ID of type int and returns the DataModel of the editor. The content is a @Composable function which defines the content of the editor.

### Request Modules
TODO: Request defines what to display

Use the workbench to request editors and explorers by calling the exposed functions. 