package elevatorControlSystem

/**
  * Possible actions that could be executed on the Elevator Control System
  */
trait ElevatorControlSystem {
  type ElevatorID = Int
  type UpdateResult = Option[ElevatorStatus]

  /**
    * @return the list of elevators that are managed by the system
    */
  protected def elevators: Seq[Elevator]

  /**
    * Retrieve all existing elevators statuses in the system
    *
    * @return a map of all elevators ids and their correspondent status
    */
  def allElevatorsStatus(): Map[ElevatorID, ElevatorStatus]

  /**
    * Retrieve the current status of an existing elevator
    *
    * @param elevatorID an elevator id
    * @return Some([[ElevatorStatus]]) if an elevator with the provided id exists, None if not
    */
  def elevatorStatus(elevatorID: Int): Option[ElevatorStatus]

  /**
    * Update the current floor number of an existing elevator
    *
    * @param elevatorID an elevator id
    * @param floorNumber the newly floor number at which the elevator just arrived
    * @return the new elevator status if the submission was successful,
    *         None if there is no elevator with the provided id
    */
  def updateElevatorCurrentFloor(elevatorID: Int, floorNumber: Int): UpdateResult

  /**
    * Add a [[PickupRequest]] to an existing elevator status
    *
    * @param elevatorID an elevator id
    * @param floorNumber the floor number where the request occured
    * @param direction the desired direction by the user
    * @return the new elevator status if the submission was successful,
    *         None if there is no elevator with the provided id
    */
  def requestPickup(elevatorID: Int, floorNumber: Int, direction: Direction): UpdateResult

  /**
    * Add a [[FloorRequest]] to an existing elevator status
    *
    * @param elevatorID an elevator id
    * @param floorNumber the goal floor
    * @return the new elevator status if the submission was successful,
    *         None if there is no elevator with the provided id
    */
  def submitGoalFloor(elevatorID: Int, floorNumber: Int): UpdateResult

  /**
    * Moving all elevators to their next floor by
    * incrementing or decrementing their current floor number
    * which will automatically trigger all the elevators statuses update
    */
  def nextStep(): Unit
}
