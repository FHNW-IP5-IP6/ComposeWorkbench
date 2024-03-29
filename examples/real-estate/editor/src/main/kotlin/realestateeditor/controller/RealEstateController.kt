package realestateeditor.controller

import allpurpose.controller.EditorController
import allpurpose.model.Attribute
import realestateeditor.controller.RealEstateAction.Delete
import realestateeditor.controller.RealEstateAction.Redo
import realestateeditor.controller.RealEstateAction.Save
import realestateeditor.controller.RealEstateAction.Undo
import realestateeditor.controller.RealEstateAction.UpdateCity
import realestateeditor.controller.RealEstateAction.UpdateDescription
import realestateeditor.controller.RealEstateAction.UpdateMarketValue
import realestateeditor.controller.RealEstateAction.UpdateStreet
import realestateeditor.controller.RealEstateAction.UpdateStreetNumber
import realestateeditor.controller.RealEstateAction.UpdateType
import realestateeditor.controller.RealEstateAction.UpdateYearOfConstruction
import realestateeditor.controller.RealEstateAction.UpdateZipCode
import realestateeditor.data.RealEstateData
import realestateeditor.data.RealEstateRepository
import realestateeditor.data.RealEstateType
import realestateeditor.model.RealEstateEditorState


class RealEstateController(
    data: RealEstateData,
    repo: RealEstateRepository,
    val onChange: (field: String, value: String, someDataChanged: Boolean) -> Unit = {_, _, _ -> },
    val onDelete: () -> Unit = {}
) :
    EditorController<RealEstateData, RealEstateAction, RealEstateEditorState>(initialState = RealEstateEditorState(data = data),
                                                                                      repo = repo) {


    override fun executeAction(action: RealEstateAction, currentState: RealEstateEditorState) : RealEstateEditorState? {

        val data = currentState.data

        return when (action) {
            is UpdateType               -> updateType(action.newValue, data)              ?.insertInto(currentState)
            is UpdateStreet             -> updateStreet(action.newValue, data)            .insertInto(currentState)
            is UpdateStreetNumber       -> updateStreetNumber(action.newValue, data)      .insertInto(currentState)
            is UpdateZipCode            -> updateZipCode(action.newValue, data)           .insertInto(currentState)
            is UpdateCity               -> updateCity(action.newValue, data)              .insertInto(currentState)
            is UpdateYearOfConstruction -> updateYearOfConstruction(action.newValue, data).insertInto(currentState)
            is UpdateMarketValue        -> updateMarketValue(action.newValue, data)       .insertInto(currentState)
            is UpdateDescription        -> updateDescription(action.newValue, data)       .insertInto(currentState)

            is Undo                     -> undo()
            is Redo                     -> redo()

            is Save                     -> save(currentState)
            is Delete                   -> delete(data)
        }
    }


    override fun delete(data: RealEstateData): RealEstateEditorState? {
        val result = super.delete(data)
        onDelete()

        return result
    }

    override fun sendUpdateNotifications(oldData: RealEstateData, newData: RealEstateData, someDataChanged: Boolean) {
        sendAttributeChange(oldData.type,               newData.type,               someDataChanged)
        sendAttributeChange(oldData.street,             newData.street,             someDataChanged)
        sendAttributeChange(oldData.streetNumber,       newData.streetNumber,       someDataChanged)
        sendAttributeChange(oldData.city,               newData.city,               someDataChanged)
        sendAttributeChange(oldData.zipCode,            newData.zipCode,            someDataChanged)
        sendAttributeChange(oldData.yearOfConstruction, newData.yearOfConstruction, someDataChanged)
        sendAttributeChange(oldData.marketValue,        newData.marketValue,        someDataChanged)
        sendAttributeChange(oldData.description,        newData.description,        someDataChanged)
    }

    private fun<T> sendAttributeChange(oldAttribute: Attribute<T>, newAttribute: Attribute<T>, someDataChanged: Boolean){
        if(oldAttribute.value != newAttribute.value){
            onChange(newAttribute.dbName, newAttribute.valueAsText, someDataChanged)
        }
    }

    private fun updateType(value: RealEstateType, data: RealEstateData) : RealEstateData? =
        if (data.type.value != value) data.copy(type = data.type.copy(value = value, valueAsText = value.translation)) else null

    private fun updateStreet(valueAsString: String, data: RealEstateData): RealEstateData =
        data.copy(street = data.street.copyString(valueAsText = valueAsString))


    private fun updateStreetNumber(valueAsText: String, data: RealEstateData) : RealEstateData =
        data.copy(streetNumber = data.streetNumber.copyString(valueAsText = valueAsText))


    private fun updateZipCode(valueAsText: String, data: RealEstateData) : RealEstateData =
        data.copy(zipCode = data.zipCode.copy(valueAsText = valueAsText,
                                                  checker = zipCodeRegex,
                                                converter = { it.toInt() })
        )


    private fun updateCity(valueAsText: String, data: RealEstateData) : RealEstateData =
        data.copy(city = data.city.copyString(valueAsText = valueAsText))


    private fun updateYearOfConstruction(valueAsText: String, data: RealEstateData) : RealEstateData =
        data.copy(yearOfConstruction = data.yearOfConstruction.copy(valueAsText = valueAsText,
                                                                        checker = yearRegex,
                                                                      converter = { it.toInt() })
        )


    private fun updateMarketValue(valueAsText: String, data: RealEstateData) : RealEstateData =
        data.copy(marketValue = data.marketValue.copy(valueAsText = valueAsText,
                                                          checker = intCHRegex,
                                                        converter = { it.replace("$chGroupingSeparator", "").toInt() },
                                                        formatter = { it.format("%,d") })
        )


    private fun updateDescription(valueAsText: String, data: RealEstateData) : RealEstateData =
        data.copy(description = data.description.copy(valueAsText = valueAsText,
                                                          checker = asciiTextRegex,
                                                        converter = { it },
                                                        formatter = { it })
        )



    private fun RealEstateData.insertInto(editorState: RealEstateEditorState): RealEstateEditorState =
        editorState.copy(data = this)


}