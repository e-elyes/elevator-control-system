import elevatorControlSystem._
import org.specs2._
import org.specs2.specification.Tables

class ElevatorControlSystemSpec extends Specification with Tables {
  sequential
  def is = s2"""
  The Elevator Control System should
    be able to return
      the status of a specific elevator   $statusOfSpecificElevator
      a list of statuses of all elevators $statusesOfAllElevators

    return a None value and maintain the same in case of an update with invalid values such
      a non-existent elevator id                 $nonExistentElevatorID
      an out of range floor number               $outOfRangeFloorNumber
      a down pick-up request in the bottom floor $bottomFloorInvalidRequest
      an up pick-up request in the highest floor $highestFloorInvalidRequest

    update the and return the correspondent updated elevator status when receiving
      a goal floor submission                         $goalFloorSubmission
      a pickup request                                $pickupRequest
      an update for an elevator current floor changed $elevatorCurrentFloorChanged

    reschedule the elevators next floor goal lists whenever it receives an update $rescheduleGoalFloorNumbers

    update elevators statuses when executing next step $nextSteps

"""

  def goalFloorSubmission = {
    val goalFloorNumber = 3
    val requestsToAdd = Seq(FloorRequest(goalFloorNumber))
    val expectedRequests = elevator1.status.requests ++ requestsToAdd

    elevatorControlSystem.submitGoalFloor(elevator1.id, goalFloorNumber) should
      beSome{ x: ElevatorStatus => x.requests should_=== expectedRequests }
  }

  def pickupRequest = {
    val goalFloorNumber = 5
    val requestsToAdd = Seq(PickupRequest(goalFloorNumber, Up))
    val expectedRequests = elevator2.status.requests ++ requestsToAdd

    elevatorControlSystem.requestPickup(elevator2.id, goalFloorNumber, Up) should
      beSome{ x: ElevatorStatus => x.requests should_=== expectedRequests }
  }

  def elevatorCurrentFloorChanged = {
    val newFloorNumber = 6
    val expectedElevatorStatus = elevator3.status.copy(currentFloorNumber = newFloorNumber)

    (elevatorControlSystem.updateElevatorCurrentFloor(elevator3.id, newFloorNumber) should beSome(expectedElevatorStatus)) and
      checkElevatorStatus(elevator3, expectedElevatorStatus)
  }

  def nonExistentElevatorID = {
    val originalStatuses = elevatorControlSystem.allElevatorsStatus()
    val elevatorStatus = elevatorControlSystem.elevatorStatus(-1)
    val updateElevatorCurrentFloor = elevatorControlSystem.updateElevatorCurrentFloor(-1, 1)
    val requestPickup = elevatorControlSystem.requestPickup(-1, 2, Up)
    val submitGoalFloor = elevatorControlSystem.submitGoalFloor(-1, 3)

    (elevatorStatus should beNone) and
      (updateElevatorCurrentFloor should beNone) and
      (requestPickup should beNone) and
      (submitGoalFloor should beNone) and
      (elevatorControlSystem.allElevatorsStatus() should_=== originalStatuses)
  }

  def outOfRangeFloorNumber = {
    val originalStatuses = elevatorControlSystem.allElevatorsStatus()
    val updateElevatorCurrentFloor = elevatorControlSystem.updateElevatorCurrentFloor(1, -1)
    val requestPickup = elevatorControlSystem.requestPickup(1, -2, Up)
    val submitGoalFloor = elevatorControlSystem.submitGoalFloor(1, -3)

    (updateElevatorCurrentFloor should beNone) and
      (requestPickup should beNone) and
      (submitGoalFloor should beNone) and
      (elevatorControlSystem.allElevatorsStatus() should_=== originalStatuses)
  }

  def bottomFloorInvalidRequest = {
    elevatorControlSystem.updateElevatorCurrentFloor(elevator6.id, 6)
    elevatorControlSystem.requestPickup(elevator6.id, config.floorsNumbersRange.head, Down)
    elevatorControlSystem.elevatorStatus(elevator6.id) should beSome { s: ElevatorStatus => s.requests should beEmpty }
  }

  def highestFloorInvalidRequest = {
    elevatorControlSystem.updateElevatorCurrentFloor(elevator6.id, 6)
    elevatorControlSystem.requestPickup(elevator6.id, config.floorsNumbersRange.last, Up)
    elevatorControlSystem.elevatorStatus(elevator6.id) should beSome { s: ElevatorStatus => s.requests should beEmpty }
  }

  def statusOfSpecificElevator = {
    elevatorControlSystem.elevatorStatus(4) should beSome(elevator4Status)
  }

  def statusesOfAllElevators = {
    val allStatuses = elevatorControlSystem.allElevatorsStatus()
    allStatuses.keys.toSeq.sorted should_=== config.elevators.map(_.id).sorted
  }

  def rescheduleGoalFloorNumbers = {
    val elevatorID = elevator5.id

    lazy val startingFloorNumber1 = 4
    lazy val requests1 = Seq(PickupRequest(0, Up),PickupRequest(8, Down))
    lazy val expectedOrderedGoalFloorNumbers1 = Seq(0, 8)

    lazy val startingFloorNumber2 = 4
    lazy val requests2 = Seq(FloorRequest(2), PickupRequest(3, Down), PickupRequest(8, Down))
    lazy val expectedOrderedGoalFloorNumbers2 = Seq(3, 2, 8)

    lazy val startingFloorNumber3 = 3
    lazy val requests3 = Seq(PickupRequest(5, Down), PickupRequest(4, Up))
    lazy val expectedOrderedGoalFloorNumbers3 = Seq(4, 5)

    lazy val startingFloorNumber4 = 4
    lazy val requests4 = Seq(PickupRequest(5, Down))
    lazy val expectedOrderedGoalFloorNumbers4 = Seq(5)

    lazy val startingFloorNumber5 = 2
    lazy val requests5 = Seq(
      PickupRequest(0, Up),
      PickupRequest(1, Down),
      PickupRequest(3, Down),
      PickupRequest(4, Up),
      PickupRequest(5, Down),
      PickupRequest(6, Up),
      PickupRequest(7, Down),
      PickupRequest(8, Up)) // invalid
    lazy val expectedOrderedGoalFloorNumbers5 = Seq(1, 0, 4, 6, 7, 5, 3)

    lazy val startingFloorNumber6 = 3
    lazy val requests6 = Seq(
      PickupRequest(0, Down), // invalid
      PickupRequest(1, Up),
      PickupRequest(2, Down),
      PickupRequest(4, Up),
      PickupRequest(5, Down),
      PickupRequest(6, Up),
      PickupRequest(7, Down),
      PickupRequest(8, Up)) // invalid
    lazy val expectedOrderedGoalFloorNumbers6 = Seq(2, 1, 4, 6, 7, 5)

    lazy val startingFloorNumber7 = 4
    lazy val requests7 = Seq(
      PickupRequest(0, Up),
      PickupRequest(7, Down),
      PickupRequest(5, Down),
      PickupRequest(3, Down),
      PickupRequest(6, Up),
      PickupRequest(1, Down),
      PickupRequest(8, Down),
      FloorRequest(2))
    lazy val expectedOrderedGoalFloorNumbers7 = Seq(3, 2, 1, 0, 6, 8, 7, 5)

    lazy val startingFloorNumber8 = 4
    lazy val requests8 = Seq(
      FloorRequest(5),
      FloorRequest(4))
    lazy val expectedOrderedGoalFloorNumbers8 = Seq(5)

    submitElevatorRequestsAndCheckScheduling(elevatorID, startingFloorNumber1, requests1)(expectedOrderedGoalFloorNumbers1) and
    submitElevatorRequestsAndCheckScheduling(elevatorID, startingFloorNumber2, requests2)(expectedOrderedGoalFloorNumbers2) and
    submitElevatorRequestsAndCheckScheduling(elevatorID, startingFloorNumber3, requests3)(expectedOrderedGoalFloorNumbers3) and
    submitElevatorRequestsAndCheckScheduling(elevatorID, startingFloorNumber4, requests4)(expectedOrderedGoalFloorNumbers4) and
    submitElevatorRequestsAndCheckScheduling(elevatorID, startingFloorNumber5, requests5)(expectedOrderedGoalFloorNumbers5) and
    submitElevatorRequestsAndCheckScheduling(elevatorID, startingFloorNumber6, requests6)(expectedOrderedGoalFloorNumbers6) and
    submitElevatorRequestsAndCheckScheduling(elevatorID, startingFloorNumber7, requests7)(expectedOrderedGoalFloorNumbers7) and
    submitElevatorRequestsAndCheckScheduling(elevatorID, startingFloorNumber8, requests8)(expectedOrderedGoalFloorNumbers8)
  }

  def nextSteps = {
    val idleElevator = Elevator(1)
    val e1 = Elevator(2)
    val e2 = Elevator(3)
    val e3 = Elevator(4)

    val elevators = Seq(idleElevator, e1, e2)
    val floorsNumbersRange = 0 to 8
    val config = SystemConfiguration(elevators, floorsNumbersRange)
    val elevatorControlSystem = new ElevatorControlSystemImpl(config)

    elevatorControlSystem.updateElevatorCurrentFloor(e1.id, floorsNumbersRange.head)
    elevatorControlSystem.updateElevatorCurrentFloor(e2.id, floorsNumbersRange.last)
    elevatorControlSystem.updateElevatorCurrentFloor(e3.id, 5)

    elevatorControlSystem.requestPickup(e1.id, 2, Up)
    elevatorControlSystem.requestPickup(e2.id, 6, Down)
    elevatorControlSystem.submitGoalFloor(e2.id, 4)
    elevatorControlSystem.requestPickup(e3.id, 6, Up)

    elevatorControlSystem.nextStep()

    (elevatorControlSystem.elevatorStatus(idleElevator.id) should beSome { x: ElevatorStatus => x.currentFloorNumber should_=== 0 }) and
      (elevatorControlSystem.elevatorStatus(e1.id) should beSome { x: ElevatorStatus => x.currentFloorNumber should_=== 1 }) and
      (elevatorControlSystem.elevatorStatus(e2.id) should beSome { x: ElevatorStatus => x.currentFloorNumber should_=== 7 })
  }

  private def submitElevatorRequestsAndCheckScheduling(elevatorID: Int, currentFloorNumber: Int, elevatorRequests: Seq[ElevatorRequest])(expectedOrderedGoalFloorNumbers: Seq[Int]) = {
    elevatorControlSystem.updateElevatorCurrentFloor(elevatorID, currentFloorNumber)
    val oldStatus = elevatorControlSystem.elevatorStatus(elevatorID).get
    submitRequests(elevatorID, elevatorRequests)
    elevatorControlSystem.elevatorStatus(elevatorID) should beSome { x: ElevatorStatus => x.orderedGoalFloorNumbers should_=== expectedOrderedGoalFloorNumbers }
  }

  private def submitRequests(elevatorID: Int, elevatorRequests: Seq[ElevatorRequest]) = {
    elevatorControlSystem.cancelAllElevatorRequests(elevatorID)
    elevatorRequests foreach {
      case PickupRequest(num, dir) => elevatorControlSystem.requestPickup(elevatorID, num, dir)
      case FloorRequest(num)       => elevatorControlSystem.submitGoalFloor(elevatorID, num)
    }
  }

  private def checkElevatorStatus(elevator: Elevator, expectedElevatorStatus: ElevatorStatus) = {
    (elevatorControlSystem.elevatorStatus(elevator.id) should beSome(expectedElevatorStatus)) and
      (elevatorControlSystem.allElevatorsStatus().find(_._2 == expectedElevatorStatus) map(_._2) should beSome(expectedElevatorStatus))
  }

  lazy val elevator1 = Elevator(1)
  lazy val elevator2 = Elevator(2)
  lazy val elevator3 = Elevator(3)
  lazy val elevator4 = Elevator(4)
  lazy val elevator5 = Elevator(5)
  lazy val elevator6 = Elevator(6)

  lazy val elevator4Status = ElevatorStatus(4, Seq(6), Seq(FloorRequest(6)))

  lazy val elevators = Seq(elevator1, elevator2, elevator3, elevator4, elevator5, elevator6)
  lazy val floorsNumbersRange = 0 to 8
  lazy val config = SystemConfiguration(elevators, floorsNumbersRange)

  lazy val elevatorControlSystem = new ElevatorControlSystemImpl(config)
  elevatorControlSystem.updateElevatorCurrentFloor(elevator4.id, 4)
  elevatorControlSystem.submitGoalFloor(elevator4.id, 6)
}
