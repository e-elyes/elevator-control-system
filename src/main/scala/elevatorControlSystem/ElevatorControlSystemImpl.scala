package elevatorControlSystem

/**
  * An implementation of [[ElevatorControlSystem]]
  *
  * @param config the system configuration
  */
class ElevatorControlSystemImpl(config: SystemConfiguration) extends ElevatorControlSystem {

  override protected def elevators: Seq[Elevator] = config.elevators

  override def elevatorStatus(elevatorID: Int): Option[ElevatorStatus] = elevators find (_.id == elevatorID) map (_.status)

  override def allElevatorsStatus(): Map[ElevatorID, ElevatorStatus] = elevators.map(e => (e.id, e.status)).toMap

  override def updateElevatorCurrentFloor(elevatorID: Int, floorNumber: Int): UpdateResult =
    if (isValidFloorNumber(floorNumber)) elevatorByID(elevatorID) flatMap { elevator =>
      val newStatus = elevator.status.copy(currentFloorNumber = floorNumber)
      updateElevatorStatus(elevatorID, newStatus)
    }
    else None

  override def requestPickup(elevatorID: Int, floorNumber: Int, direction: Direction): UpdateResult =
    if (isValidFloorNumber(floorNumber)) addRequestToElevator(elevatorID, PickupRequest(floorNumber, direction))
    else None

  override def submitGoalFloor(elevatorID: Int, floorNumber: Int): UpdateResult =
    if (isValidFloorNumber(floorNumber)) addRequestToElevator(elevatorID, FloorRequest(floorNumber))
    else None

  override def nextStep(): Unit =
    elevators foreach ( elevator => updateElevatorCurrentFloor(elevator.id, elevator.nextFloorNumber))

  /**
    * Apply to a given elevator status a requests scheduling algorithm
    *
    * @param status the status to reschedule its requests
    * @return None if the status indicates that the elevator is staying idle, else Some(the updated status)
    */
  private def rescheduleRequests(status: ElevatorStatus): UpdateResult = status.nextMoveDirection map { nextMoveDirection =>
    val currentFloorNumber = status.currentFloorNumber

    //0) eliminate all done requests for the current floor and also the submitted but invalid requests
    val requests = status.requests.filterNot {
      case FloorRequest(x) => x == currentFloorNumber
      case PickupRequest(x, dir) =>
        (x == currentFloorNumber && dir == nextMoveDirection) ||
          (x == config.floorsNumbersRange.head && dir == Down) ||
          (x == config.floorsNumbersRange.last && dir == Up)
    }

    //1) sort the resulting list in ascendant order
    val orderedRequests = requests.sortBy(_.floorNumber)

    //2) split the sorted requests list into 2 lists the first containing all request that has
    // 'floorNumber <= currentFloorNumber' (when nextMoveDirection is Down) or 'floorNumber < currentFloorNumber' (when nextMoveDirection is Up)
    // and the second list will contain the rest of the other elements
    val requestsFromDownFloors = orderedRequests.filter( n =>
      if (nextMoveDirection == Down) n.floorNumber <= currentFloorNumber
      else n.floorNumber < currentFloorNumber)
    val requestsFromUpperFloors = orderedRequests diff requestsFromDownFloors

    //3) split requestsFromDownFloors and group the resulting lists by direction
    val down1 = requestsFromDownFloors.filterRequestsByDirection(Down).reverse
    val up1 = requestsFromDownFloors diff down1

    //4) split requestsFromUpperFloors and group the resulting lists by direction
    val down2 = requestsFromUpperFloors.filterRequestsByDirection(Down).reverse
    val up2 = requestsFromUpperFloors diff down2

    val res = if (nextMoveDirection == Down) down1 ++ up1 ++ up2 ++ down2 else up2 ++ down2 ++ down1 ++ up1

    //5) construct and return the updated status of the current elevator
    val newOrderedGoalFloorNumbers = res.map(_.floorNumber).distinct
    status.copy(orderedGoalFloorNumbers = newOrderedGoalFloorNumbers, requests = requests)
  }

  /**
    * Delete all pending requests of an elevator
    *
    * @param elevatorID an elevator id
    * @return the new status of the elevator if this latter was found, None if not
    */
  def cancelAllElevatorRequests(elevatorID: ElevatorID): Option[ElevatorStatus] = withCheckedElevator(elevatorID) (elevator =>
    updateElevatorStatus(elevator, elevator.status.copy(orderedGoalFloorNumbers = Seq.empty, requests = Seq())))

  /**
    * Add request to an existing elevator
    *
    * @param elevatorID an elevator id
    * @param newRequest the request to add
    * @return Some(updated elevator status) if the update was successful, None if the elevator was not found
    */
  private def addRequestToElevator(elevatorID: Int, newRequest: ElevatorRequest) = elevatorByID(elevatorID) flatMap { elevator =>
    val oldRequests = elevator.status.requests
    val updatedStatus = elevator.status.copy(requests = oldRequests ++ Seq(newRequest))

    updateElevatorStatus(elevator.id, updatedStatus)
  }

  private def updateElevatorStatus(elevatorID: Int, newStatus: ElevatorStatus): UpdateResult = withCheckedElevator(elevatorID) (elevator =>
    updateElevatorStatus(elevator, newStatus))

  /**
    * Reschedule a given status and affect it to a given elevator
    *
    * @param elevator the elevator to update
    * @param status the new status
    * @return the new affected status to the elevator
    */
  private def updateElevatorStatus(elevator: Elevator, status: ElevatorStatus): ElevatorStatus = {
    val newStatus = rescheduleRequests(status) getOrElse status
    elevator.status = newStatus
    newStatus
  }

  /**
    * Try to retrieve an elevator by its ID from the system elevators list
    *
    * @param elevatorID an elevator id
    * @return Some([[Elevator]]) if the elevator was found, None if not
    */
  private def elevatorByID(elevatorID: Int): Option[Elevator] = elevators find (_.id == elevatorID)

  /**
    * Check if the provided number is within the system floor numbers range
    *
    * @param floorNumber a floor number to check
    * @return true if provided floor number is supported by the system, false if not
    */
  private def isValidFloorNumber(floorNumber: Int): Boolean = config.floorsNumbersRange.contains(floorNumber)

  /**
    * Try to retrieve the elevator by its ID from system elevators list and applies a function on it
    *
    * @param elevatorID an elevator id
    * @param f the function to apply on the retrieved elevator
    * @return Some(result of the function f) when elevator was found, None if the elevator was not found
    */
  private def withCheckedElevator[A](elevatorID: Int)(f: Elevator => A): Option[A] = elevatorByID(elevatorID) map f

  private implicit class EnrichedFloorRequest(requests: Traversable[ElevatorRequest]) {
    /**
      * Filter a requests list by a given direction
      * [[FloorRequest]] are automatically taken
      *
      * @param direction the desired requests direction
      * @return a list of [[ElevatorStatus]]
      */
    def filterRequestsByDirection(direction: Direction): Seq[ElevatorRequest] =
      requests.filter {
        case PickupRequest(_, dir) => dir == direction
        case FloorRequest(_)       => true
      }.toSeq
  }

  private implicit class EnrichedElevator(elevator: Elevator) {
    /**
      * @return increments or decrements the elevator currentFloorNumber
      *         depending on the next move direction
      */
    def nextFloorNumber: Int = {
      val floorNumber = elevator.nextMoveDirection match {
        case Some(Up)   => elevator.status.currentFloorNumber + 1
        case Some(Down) => elevator.status.currentFloorNumber - 1
        case _          => elevator.status.currentFloorNumber
      }
      assert(config.floorsNumbersRange.contains(floorNumber), "the next floor number should not be out of the config.floorsNumbersRange")
      floorNumber
    }
  }
}
