package passengers;

import buildings.Floor;
import buildings.FloorObserver;
import elevators.Elevator;
import elevators.ElevatorObserver;

/**
 * A passenger that is either waiting on a floor or riding an elevator.
 */
public abstract class Passenger implements FloorObserver, ElevatorObserver {
	// An enum for determining whether a Passenger is on a floor, an elevator, or busy (visiting a room in the building).
	public enum PassengerState {
		WAITING_ON_FLOOR,
		ON_ELEVATOR,
		BUSY
	}
	
	// A cute trick for assigning unique IDs to each object that is created. (See the constructor.)
	private static int mNextId;
	protected static int nextPassengerId() {
		return ++mNextId;
	}
	
	private final int mIdentifier;
	private PassengerState mCurrentState;
	
	public Passenger() {
		mIdentifier = nextPassengerId();
		mCurrentState = PassengerState.WAITING_ON_FLOOR;
	}
	
	public void setState(PassengerState state) {
		mCurrentState = state;
	}
        public PassengerState getState(){
            return mCurrentState;
        }
	
	/**
	 * Gets the passenger's unique identifier.
         * @return 
	 */
	public int getId() {
		return mIdentifier;
	}
	
	
	/**
	 * Handles an elevator arriving at the passenger's current floor.
         * @param floor
	 */
	@Override
	public void elevatorArriving(Floor floor, Elevator elevator) {
		// This is a sanity check. A Passenger should never be observing a Floor they are not waiting on.
		if (floor.getWaitingPassengers().contains(this) && mCurrentState == PassengerState.WAITING_ON_FLOOR) {
			Elevator.Direction elevatorDirection = elevator.getCurrentDirection();
			if(null!=elevatorDirection) 
                    // TODO: check if the elevator is either NOT_MOVING, or is going in the direction that this passenger wants.
                    // If so, this passenger becomes an observer of the elevator.
                    switch (elevatorDirection) {
                        case NOT_MOVING:
                            elevator.addObserver(this);
                            break;
                        case MOVING_UP:
                            if(this.getDestination()>elevator.getCurrentFloor().getNumber()){
                                elevator.addObserver(this);
                            }
                            break;
                        case MOVING_DOWN:
                            if(this.getDestination()<elevator.getCurrentFloor().getNumber()){
                                elevator.addObserver(this);
                            }
                            break;
                        default:
                            break;
                    }
                        //System.out.println("Passenger: Elevator arriving");
		}
		// This else should not happen if your code is correct. Do not remove this branch; it reveals errors in your code.
		else {
			throw new RuntimeException("Passenger " + toString() + " is observing Floor " + floor.getNumber() + " but they are " +
			 "not waiting on that floor.");
		}
	}
	
	/**
	 * Handles an observed elevator opening its doors. Depart the elevator if we are on it; otherwise, enter the elevator.
         * @param elevator
	 */
	@Override
	public void elevatorDoorsOpened(Elevator elevator) {
		// The elevator is arriving at our destination. Remove ourselves from the elevator, and stop observing it.
		// Does NOT handle any "next" destination...
                //System.out.println("Passenger: Elevator doors opened");

		if (mCurrentState == PassengerState.ON_ELEVATOR && elevator.getCurrentFloor().getNumber() == getDestination()) {
			// TODO: remove this passenger from the elevator, and as an observer of the elevator. Call the
			// leavingElevator method to allow a derived class to do something when the passenger departs.
			// Set the current state to BUSY.
                        
			elevator.removePassenger(this);
                        elevator.removeObserver(this);
                        this.leavingElevator(elevator);
                        this.mCurrentState=PassengerState.BUSY;
			//System.out.println("Passenger: leaving elevator");

		}
		// The elevator has arrived on the floor we are waiting on. If the elevator has room for us, remove ourselves
		// from the floor, and enter the elevator.
		else if (mCurrentState == PassengerState.WAITING_ON_FLOOR) {
			// TODO: determine if the passenger will board the elevator using willBoardElevator.
			// If so, remove the passenger from the current floor, and as an observer of the current floor;
			// then add the passenger as an observer of and passenger on the elevator. Then set the mCurrentState
			// to ON_ELEVATOR.
                        if(this.willBoardElevator(elevator)==true){
                            //System.out.println(elevator.getPassenger());
                            //System.out.println(elevator.getCurrentFloor().getNumber());
                            //System.out.println(elevator.getCurrentFloor().getobserver().size());
                            elevator.getCurrentFloor().removeWaitingPassenger(this);
                            elevator.getCurrentFloor().removeObserver(this);
                            elevator.addPassenger(this);
                            //elevator.addObserver(this);
                            this.mCurrentState=PassengerState.ON_ELEVATOR;
                        }
                        else{
                            elevator.removeObserver(this);
                        }
                        //System.out.println(this.toString()+"Passenger: will board elevator");
			
			
		}
	}
	
	/**
	 * Returns the passenger's current destination (what floor they are travelling to).
         * @return 
	 */
	public abstract int getDestination();
	
	/**
	 * Called to determine whether the passenger will board the given elevator that is moving in the direction the
	 * passenger wants to travel.
	 */
	protected abstract boolean willBoardElevator(Elevator elevator);
	
	/**
	 * Called when the passenger is departing the given elevator.
         * @param elevator
	 */
	protected abstract void leavingElevator(Elevator elevator);
	
	// This will be overridden by derived types.
	@Override
	public String toString() {
		return Integer.toString(getDestination());
	}
	
	@Override
	public void directionRequested(Floor sender, Elevator.Direction direction) {
		// Don't care.
	}
	
	@Override
	public void elevatorWentIdle(Elevator elevator) {
		// Don't care about this.
	}
	
	// The next two methods allow Passengers to be used in data structures, using their id for equality. Don't change 'em.
	@Override
	public int hashCode() {
		return Integer.hashCode(mIdentifier);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Passenger passenger = (Passenger)o;
		return mIdentifier == passenger.mIdentifier;
	}
	
}