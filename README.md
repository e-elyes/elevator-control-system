# Elevator Control System
This is an implementation of an elevator control system simulator.

-----------------------
Development environment
-----------------------
- Programming language : Scala (2.11.7)
- Test framework       : specs2 (3.7.2)
- Build tool           : SBT
- JDK version          : 1.8.0_51
- IDE                  : IntelliJ IDEA (2016.1.1)

-----------------------
Run & Test Instructions
-----------------------
This application was test driven developed, you can run the tests
and eventually read the specs using the sbt command:
sbt> test

You can run the main class Main.scala which will show you
the instructions for time-stepping the simulation:
sbt> run

---------------------------
Data Structures, Interfaces
---------------------------
The main interface in this system is :

trait ElevatorControlSystem {
  type ElevatorID = Int
  type UpdateResult = Option[ElevatorStatus]

  protected def elevators: Seq[Elevator]
  def allElevatorsStatus(): Map[ElevatorID, ElevatorStatus]
  def elevatorStatus(elevatorID: Int): Option[ElevatorStatus]
  def updateElevatorCurrentFloor(elevatorID: Int, floorNumber: Int): UpdateResult
  def requestPickup(elevatorID: Int, floorNumber: Int, direction: Direction): UpdateResult
  def submitGoalFloor(elevatorID: Int, floorNumber: Int): UpdateResult
  def nextStep(): Unit
}

* Please see the scaladoc for detailed description.

----------------------------
Algorithm Decisions & Issues
----------------------------
Although the FCFS algorithm could be already a solution for the scheduling
problem in this system, it will be a very bad solution in real life.
So a better solution is needed for such a system.

Lets start by fixing the requirements that a best algorithm should fulfill in our case :
(1) The system should avoid the starvation problem, where a user request will never be executed!
(2) The elevator should not take any of its users in the opposite direction indicated in their pick-up request,
   only if the user changed mind and requested a floor that is not in its pick-up request direction.
(3) An elevator should never make an unnecessary action that costs time like stopping in an unrequested floor.

After evaluating and manually executing some already known algorithms for Disk Scheduling
which is a relatively close field to our studied case, I ended up with a modified version
of the SCAN algorithm.

The SCAN algorithm consists in scanning the requests list down towards the nearest end
and then when it hits the bottom it scans up servicing the requests that it didn't get going down.

By taking just this principle and applying it to our system, it will fulfill the points (1) and (3)
of our predefined requirements but not the (2) one.

So a solution for that could be making a new version of this algorithm that explicitly ignore some
requests in one move direction and service them later in the opposite direction.

---------------------------------
Algorithm Design & Implementation
---------------------------------
The algorithm needs the current floor number and a list of requests which
initially is sorted by priority. The first element of the list has the highest priority.

1) It will firstly eliminate from the list all 'invalid' requests and also the ones
that are coming from the current floor.

2) It will then distinguish between the requests coming from the upper floors
and requests coming from the lower ones, and puts them into two separate
lists lets say downRequests and upRequests.

3) And then it will decide in which direction should go by taking the first
element of the original requests list and evaluating its direction. Lets name
it nextMoveDirection.

4) Sort the downRequests and upRequests lists by direction, the elements with
same direction as nextMoveDirection comes first.

5) Reassemble the requests list : if nextMoveDirection is Down then downRequests comes first
and then upRequests, if nextMoveDirection is Up then the final list will be upRequests-downRequests.

* Implementation: Please see the rescheduleRequests method source code
