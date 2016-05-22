package elevatorControlSystem

/**
  * An elevator that could be managed by our system
  *
  * @param id the elevators id
  */
case class Elevator(id: Int) {
  @volatile
  var statusHolder: ElevatorStatus = ElevatorStatus.defaultStatus

  /**
    * A getter for the var [[statusHolder]]
    * @return a copy of the current statusHolder
    */
  def status: ElevatorStatus = statusHolder

  /** A setter for the var [[statusHolder]] */
  def status_=(newStatus: ElevatorStatus): Unit = statusHolder = newStatus

  /**
    * the next direction in which the elevator should go
    * @return None if the elevator should stay idle, Some([[Direction]]) otherwise
    */
  def nextMoveDirection: Option[Direction] = status.nextMoveDirection
}

/**
  * An elevator status at a specific time
  *
  * @param currentFloorNumber the floor number in which the elevator is idle or passing by
  * @param orderedGoalFloorNumbers a list of the next floors numbers in which the elevator will stop or stay idle
  * @param requests a list of requests submitted by users and is sorted by chronological order
  */
case class ElevatorStatus(currentFloorNumber: Int, orderedGoalFloorNumbers: Seq[Int], requests: Seq[ElevatorRequest]) {
  /**
    * Calculate the direction the elevator should follow on the nex step
    *
    * @return None When the elevator should stay idle Some(Direction) otherwise
    */
  def nextMoveDirection: Option[Direction] = {
    val maybeNextFloorNumber = orderedGoalFloorNumbers.filterNot(_ == currentFloorNumber).headOption
    lazy val maybeNextFloorNumberFromRequests = requests.filterNot(_.floorNumber == currentFloorNumber).headOption.map(_.floorNumber)
    val nextFloorNumber = if (maybeNextFloorNumber.isDefined) maybeNextFloorNumber else maybeNextFloorNumberFromRequests

    nextFloorNumber map ( floorNumber => if (currentFloorNumber < floorNumber) Up else Down)
  }
}

/** Companion object for [[ElevatorStatus]] case class. */
object ElevatorStatus {
  /**
    * @return a status for an elevator that is idle
    *         at the floor number 0 and has no pending requests
    */
  def defaultStatus: ElevatorStatus = ElevatorStatus(0, Seq.empty, Seq.empty)
}
