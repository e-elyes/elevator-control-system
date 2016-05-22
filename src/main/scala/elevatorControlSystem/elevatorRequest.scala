package elevatorControlSystem

/**
  * Serves as a parent type for all requests
  */
sealed trait ElevatorRequest {
  /** @return a goal floor number when [[FloorRequest]] or
    *         the source floor number of the request in case of [[PickupRequest]]*/
  def floorNumber: Int
}

/**
  * An implementation of the [[ElevatorRequest]]
  *
  * @param floorNumber the goal floor number to which the elevator user wants to go
  */
case class FloorRequest(floorNumber: Int) extends ElevatorRequest

/**
  * An implementation of the [[ElevatorRequest]]
  *
  * @param floorNumber the source floor number of the pickup request
  * @param direction the direction in which the user wants to go once he takes the elevator
  */
case class PickupRequest(floorNumber: Int, direction: Direction) extends ElevatorRequest