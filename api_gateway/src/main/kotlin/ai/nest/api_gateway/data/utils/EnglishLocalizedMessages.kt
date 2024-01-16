package ai.nest.api_gateway.data.utils

import org.koin.core.annotation.Single

@Single
class EnglishLocalizedMessages : LocalizedMessages {
    override val languageCode = "EN"

    // region identity
    override val invalidRequestParameter = "Invalid request parameter"
    override val invalidAddressLocation = "Invalid address location"
    override val userAlreadyExist = "User already exists"
    override val invalidInformation = "Invalid information"
    override val invalidFullName = "Invalid FullName"
    override val invalidUsername = "Invalid username"
    override val passwordCannotBeLessThan8Characters = "Password cannot be less than 8 characters"
    override val usernameCannotBeBlank = "Username cannot be blank"
    override val passwordCannotBeBlank = "Password cannot be blank"
    override val invalidEmail = "Invalid email"
    override val invalidPhone = "Invalid phone"
    override val notFound = "Not found"
    override val invalidCredentials = "Invalid credentials"
    override val userCreatedSuccessfully = "User created successfully 🎉"
    override val unknownError = "Unknown error `¯\\_(ツ)_/¯`"
    override val userNotFound = "User not found"
    override val invalidPermission = "Invalid permission"
    override val alreadyInFavorite = "already in your favorite list"
    // endregion

    // region taxi
    override val taxiCreatedSuccessfully = "Taxi created successfully 🎉"
    override val tripCreatedSuccessfully = "Trip Created Successfully"
    override val tripUpdated = "Trip updated Successfully"
    override val tripCanceled = "Trip Canceled"
    override val tripFinished = "Trip Finished Successfully"
    override val tripArrived = "Trip Arrived Successfully"
    override val receivedNewTrip = "New trip recieved"
    override val newOrderComing = "New Order Coming"
    override val receivedNewDeliveryOrder = "New Order is coming"
    override val taxiUpdateSuccessfully = "Taxi updated successfully 🎉"
    override val taxiDeleteSuccessfully = "Taxi deleted successfully 🎉"
    override val invalidId = "Invalid id"
    override val invalidPlate = "Invalid plate"
    override val invalidColor = "Invalid color"
    override val invalidCarType = "Invalid car type"
    override val seatOutOfRange = "Seat out of range"
    override val invalidLocation = "Invalid location"
    override val invalidRate = "Invalid rate"
    override val invalidDate = "Invalid date"
    override val invalidPrice = "Invalid price"
    override val alreadyExist = "Already exist"
    override val requiredQuery = "Required query"
    override val rideApproved = "Your ride has been approved and taxi on the way to you 🚕"
    override val taxiArrivedToUserLocation = "Taxi arrived , enjoy your ride 😊"
    override val taxiArrivedToDestination = "Your ride has been arrived 🎉"
    // endregion

    //region restaurant
    override val restaurantCreatedSuccessfully = "Restaurant created successfully 🥳"
    override val restaurantUpdateSuccessfully = "Restaurant updated successfully 🥳"
    override val restaurantDeleteSuccessfully = "Restaurant deleted successfully 🥳"
    override val restaurantInvalidId = "invalid id"
    override val restaurantInvalidName = "invalid name"
    override val restaurantInvalidLocation = "invalid location"
    override val restaurantInvalidDescription = "invalid description"
    override val restaurantInvalidPriceLevel = "invalid price level"
    override val restaurantInvalidRate = "invalid rate"
    override val restaurantInvalidPhone = "invalid phone"
    override val restaurantInvalidTime = "invalid time"
    override val restaurantInvalidPage = "invalid page"
    override val restaurantInvalidPageLimit = "invalid page limit"
    override val restaurantInvalidOneOrMoreIds = "invalid one or more ids"
    override val restaurantInvalidPermissionUpdateLocation = "invalid permission update location"
    override val restaurantInvalidUpdateParameter = "invalid update parameter"
    override val restaurantInvalidPropertyRights = "invalid property rights"
    override val restaurantInvalidPrice = "invalid price"
    override val restaurantInvalidCuisineLimit = "invalid cuisine limit"
    override val restaurantInvalidAddress = "invalid address"
    override val restaurantInvalidEmail = "invalid email"
    override val restaurantInvalidRequestParameter = "invalid request parameter"
    override val restaurantErrorAdd = "error add"
    override val restaurantClosed = "restaurant closed"
    override val restaurantInsertOrderError = "insert order error"
    override val restaurantInvalidReceivedOrders = "invalid received orders"
    override val restaurantNotFound = "Sorry, we could not found this restaurant"
    override val deletedSuccessfully = "Deleted successfully "
    override val cuisineNameAlreadyExisted = "Cuisine name already existed"

    override val missingParameter = "Missing parameter"
    override val tokensNotFound = "Tokens not found"
    override val tokenNotRegister = "Token not register"
    override val alreadyUpdated = "This order already finished before"
    override val cancelOrderError = "may be canceled before or it's not on pending status"
    override val orderApproved = "Your order has been approved"
    override val orderCanceled = "unfortunately your order is canceled"
    override val orderInCooking = "Your order is being cooking now"
    override val orderFinished = "Your order has been finished and awaits delivery"
    override val newOrderTitle = "New Order"
    override val newOrderBody = "You have a new order 🚨"
    override val orderApprovedFromDelivery = "Delivery is on the way 🛵"
    override val orderArrivedToRestaurant = "Your order is on the way be ready 🛵"
    override val orderArrivedToClient = "Your order is arrived Yam-mi 🍕"
    override val cartIsAlreadyEmpty = "cart is already empty"
    override val invalidQuantity = "Invalid Quantity"
    //endregion

    // region notification
    override val notificationNotSent = "Notification not sent"
    //endregion

    // region chat
    override val supportAgentNotFound = "There is no one to help you"
    //endregion
}