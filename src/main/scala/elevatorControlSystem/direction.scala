package elevatorControlSystem

sealed trait Direction
case object Up extends Direction
case object Down extends Direction