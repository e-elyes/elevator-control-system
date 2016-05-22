import elevatorControlSystem._
import scala.io.StdIn.readLine

object Main extends App {
  val elevator1 = Elevator(id = 1)
  val elevator2 = Elevator(id = 2)
  val elevator3 = Elevator(id = 3)
  val elevator4 = Elevator(id = 4)
  val elevators = Seq(elevator1, elevator2, elevator3, elevator4)
  val configuration = SystemConfiguration(elevators, 0 to 8)
  val elevatorControlSystem = new ElevatorControlSystemImpl(configuration)

  elevatorControlSystem.updateElevatorCurrentFloor(elevator4.id, 4)
  elevatorControlSystem.submitGoalFloor(elevator4.id, 6)

  elevatorControlSystem.updateElevatorCurrentFloor(1, 4)
  elevatorControlSystem.requestPickup(elevator1.id, 0, Up)
  elevatorControlSystem.requestPickup(elevator1.id, 7, Down)
  elevatorControlSystem.requestPickup(elevator1.id, 5, Up)
  elevatorControlSystem.requestPickup(elevator1.id, 4, Down)
  elevatorControlSystem.requestPickup(elevator1.id, 3, Down)
  elevatorControlSystem.requestPickup(elevator1.id, 6, Up)
  elevatorControlSystem.requestPickup(elevator1.id, 1, Down)
  elevatorControlSystem.requestPickup(elevator1.id, 8, Down)
  elevatorControlSystem.submitGoalFloor(elevator1.id, 2)

  elevatorControlSystem.submitGoalFloor(elevator2.id, 0)
  elevatorControlSystem.submitGoalFloor(elevator2.id, 7)
  elevatorControlSystem.submitGoalFloor(elevator2.id, 6)
  elevatorControlSystem.submitGoalFloor(elevator2.id, 2)
  elevatorControlSystem.submitGoalFloor(elevator2.id, 1)
  elevatorControlSystem.submitGoalFloor(elevator2.id, 4)
  elevatorControlSystem.submitGoalFloor(elevator2.id, 8)
  elevatorControlSystem.updateElevatorCurrentFloor(elevator2.id, 8)

  elevatorControlSystem.requestPickup(elevator3.id, 3, Up)
  elevatorControlSystem.requestPickup(elevator3.id, 0, Up)
  elevatorControlSystem.updateElevatorCurrentFloor(elevator3.id, 0)

  var stepNumber = 0

  println("\nPlease press [enter] to execute next step or tape 'exit' to quit simulation.")

  while (shouldContinueSimulation) {
    stepNumber += 1
    println(s"Step $stepNumber :")
    elevatorControlSystem.allElevatorsStatus foreach (x => println(formatElevatorStatus(x._1, x._2)))
    println()
    elevatorControlSystem.nextStep()
  }

  println("Bye !")

  private def shouldContinueSimulation: Boolean = ! (readLine() equalsIgnoreCase "exit")

  private def formatElevatorStatus(elevatorID: Int, status: ElevatorStatus): String = {
    val pendingRequestsString = if (status.requests.nonEmpty) s"have pending requests : ${formatRequests(sortRequestsByPriority(status))}" else "have no pending requests"

    if (status.nextMoveDirection.nonEmpty)
      s"Elevator $elevatorID is at floor number ${status.currentFloorNumber}, moving ${status.nextMoveDirection.get} to floor number ${status.orderedGoalFloorNumbers.head} and $pendingRequestsString"
    else
      s"Elevator $elevatorID is idle at floor number ${status.currentFloorNumber}."
  }

  private def sortRequestsByPriority(status: ElevatorStatus): Seq[ElevatorRequest] =
    status.orderedGoalFloorNumbers.flatMap(n => status.requests.find(_.floorNumber == n))

  private def formatRequests(requests: Seq[ElevatorRequest]): String = requests map {
    case FloorRequest(x)         => x.toString + '·'
    case PickupRequest(nbr, dir) => nbr.toString + (if (dir == Up) '↑' else '↓')
  } mkString ", "
}
