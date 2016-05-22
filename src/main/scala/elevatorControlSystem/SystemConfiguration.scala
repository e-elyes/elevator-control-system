package elevatorControlSystem

/**
  * An information holder for [[ElevatorControlSystemImpl]]
  *
  * @param elevators a list of elevators that will be managed by the system
  * @param floorsNumbersRange an interval of integers
  */
case class SystemConfiguration(elevators: Seq[Elevator], floorsNumbersRange: Range) {
  // An elevator control system managing a building with only one floor makes no sens
  assert(floorsNumbersRange.length > 1, "invalid floorsNumbersRange !")

  // the system could not work with duplicated elevator IDs
  assert(elevators == elevators.distinct, "duplicated elevator IDs !")
}
